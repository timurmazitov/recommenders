/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Proksch - initial API and implementation
 */
package org.eclipse.recommenders.extdoc.rcp.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.recommenders.extdoc.rcp.ExtdocModule.Extdoc;
import org.eclipse.recommenders.extdoc.rcp.Provider;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.NewSelectionEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderActivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDeactivationEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderDelayedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFailedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderFinishedLateEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderNotAvailableEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderOrderChangedEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderSelectionEvent;
import org.eclipse.recommenders.extdoc.rcp.scheduling.Events.ProviderStartedEvent;
import org.eclipse.recommenders.extdoc.rcp.ui.ExtdocIconLoader.Icon;
import org.eclipse.recommenders.extdoc.rcp.ui.OrderChangeHandler.OrderChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

public class ProviderOverviewPart {

    private final EventBus bus;
    private final List<Provider> providers;
    private final ExtdocIconLoader iconLoader;

    private Table table;
    private Map<Provider, TableItem> provider2item = Maps.newHashMap();

    @Inject
    public ProviderOverviewPart(@Extdoc EventBus bus, List<Provider> providers, ExtdocIconLoader iconLoader) {
        this.bus = bus;
        this.providers = providers;
        this.iconLoader = iconLoader;
    }

    public void createControl(Composite parent) {
        Composite tableContent = new Composite(parent, SWT.NONE);
        tableContent.setLayout(GridLayoutFactory.fillDefaults().create());

        table = new Table(tableContent, SWT.CHECK);
        table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        table.addListener(SWT.Selection,
                createListenerThatSeparatesCheckAndSelectionEventsAndPropagatesThemToTheSelectedItem());

        for (Provider p : providers) {
            TableItem item = createItemForProvider(p);
            provider2item.put(p, item);
        }
        addDragAndDropSupport();
    }

    private Listener createListenerThatSeparatesCheckAndSelectionEventsAndPropagatesThemToTheSelectedItem() {
        return new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    event.item.notifyListeners(SWT.CHECK, event);
                } else {
                    event.item.notifyListeners(SWT.Selection, event);
                }
            }
        };
    }

    private TableItem createItemForProvider(final Provider provider) {
        final TableItem item = new TableItem(table, SWT.NONE);
        item.setText(provider.getDescription().getName());
        item.setImage(provider.getDescription().getImage());
        item.setChecked(provider.isEnabled());

        item.addListener(SWT.CHECK, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean isChecked = item.getChecked();
                provider.setEnabled(isChecked);
                if (isChecked) {
                    bus.post(new ProviderActivationEvent(provider));
                } else {
                    bus.post(new ProviderDeactivationEvent(provider));
                }

            }
        });

        item.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                boolean isChecked = item.getChecked();
                if (isChecked) {
                    bus.post(new ProviderSelectionEvent(provider));
                }
            }
        });
        return item;
    }

    private void addDragAndDropSupport() {
        OrderChangeHandler handler = OrderChangeHandler.enable(table);
        handler.addListener(new OrderChangedListener() {
            @Override
            public void orderChanged(int oldIndex, int newIndex) {
                Provider providerReference = providers.get(newIndex);
                Provider movedProvider = providers.remove(oldIndex);
                providers.add(newIndex, movedProvider);
                bus.post(new ProviderOrderChangedEvent(movedProvider, providerReference, oldIndex, newIndex));

                for (Provider p : providers) {
                    TableItem oldItem = provider2item.get(p);
                    TableItem newItem = createItemForProvider(p);
                    provider2item.put(p, newItem);

                    newItem.setForeground(oldItem.getForeground());
                    newItem.setImage(oldItem.getImage());
                    oldItem.dispose();
                }
            }
        });
    }

    @Subscribe
    public void handle(final NewSelectionEvent e) {
        for (Provider p : providers) {
            TableItem oldItem = provider2item.get(p);
            provider2item.put(p, createItemForProvider(p));
            oldItem.dispose();
        }
    }

    @Subscribe
    public void handle(final ProviderNotAvailableEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(iconLoader.getImage(Icon.NOT_AVAILABLE));
        tableItem.setForeground(new Color(table.getDisplay(), 180, 180, 180));
    }

    @Subscribe
    public void handle(final ProviderDeactivationEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(e.provider.getDescription().getImage());
    }

    @Subscribe
    public void handle(final ProviderStartedEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(iconLoader.getImage(Icon.STARTED));
    }

    @Subscribe
    public void handle(final ProviderFinishedEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(e.provider.getDescription().getImage());
    }

    @Subscribe
    public void handle(final ProviderDelayedEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(iconLoader.getImage(Icon.DELAYED));
    }

    @Subscribe
    public void handle(ProviderFinishedLateEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(e.provider.getDescription().getImage());
    }

    @Subscribe
    public void handle(ProviderFailedEvent e) {
        TableItem tableItem = provider2item.get(e.provider);
        tableItem.setImage(iconLoader.getImage(Icon.FAILED));
    }
}