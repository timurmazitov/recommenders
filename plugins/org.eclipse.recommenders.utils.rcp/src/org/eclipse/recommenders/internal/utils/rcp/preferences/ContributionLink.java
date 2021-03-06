/**
 * Copyright (c) 2015 Codetrails GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yasser Aziza - initial API and implementation.
 */
package org.eclipse.recommenders.internal.utils.rcp.preferences;

import static java.util.Objects.requireNonNull;
import static org.eclipse.recommenders.utils.rcp.preferences.AbstractLinkContributionPage.COMMAND_HREF_ID;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.recommenders.internal.utils.rcp.l10n.LogMessages;
import org.eclipse.recommenders.utils.Logs;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class ContributionLink implements Comparable<ContributionLink> {

    private final String text;
    private final String commandId;
    private final int priority;
    private final Image icon;

    public ContributionLink(String text, String commandId, int priority, Image icon) {
        this.text = requireNonNull(text);
        this.commandId = requireNonNull(commandId);
        this.priority = requireNonNull(priority);
        this.icon = icon;
    }

    @Override
    public int compareTo(ContributionLink that) {
        return Integer.compare(this.getPriority(), that.getPriority());
    }

    public String getText() {
        return text;
    }

    public String getCommandId() {
        return commandId;
    }

    public Image getIcon() {
        return icon;
    }

    public int getPriority() {
        return priority;
    }

    public Link appendLink(Composite content) {
        Label label = new Label(content, SWT.BEGINNING);
        label.setImage(icon);

        Link link = new Link(content, SWT.BEGINNING);
        link.setText(text);

        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                executeCommand(commandId, e.text);
            }
        });

        return link;
    }

    private void executeCommand(String commandId, String value) {
        ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
        Command command = commandService.getCommand(commandId);

        try {
            IParameter commandParmeter = command.getParameter(COMMAND_HREF_ID);
            if (commandParmeter == null) {
                handlerService.executeCommand(commandId, null);
            } else {
                Parameterization parameterization = new Parameterization(commandParmeter, value);
                ParameterizedCommand parameterizedCommand = new ParameterizedCommand(command,
                        new Parameterization[] { parameterization });
                handlerService.executeCommand(parameterizedCommand, null);
            }
        } catch (Exception e) {
            Logs.log(LogMessages.ERROR_FAILED_TO_EXECUTE_COMMAND, e, commandId);
        }
    }
}
