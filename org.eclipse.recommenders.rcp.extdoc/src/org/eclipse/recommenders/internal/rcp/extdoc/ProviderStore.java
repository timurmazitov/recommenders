/**
 * Copyright (c) 2011 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.extdoc;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.recommenders.rcp.extdoc.IProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ProviderStore {

    private static final String EXTENSION_ID = "org.eclipse.recommenders.rcp.extdoc.provider";

    private final List<IProvider> providers = new LinkedList<IProvider>();
    private final Map<IProvider, Integer> priorities = new HashMap<IProvider, Integer>();

    public ProviderStore() {
        final IExtensionRegistry reg = Platform.getExtensionRegistry();
        for (final IConfigurationElement element : reg.getConfigurationElementsFor(EXTENSION_ID)) {
            try {
                final IProvider provider = (IProvider) element.createExecutableExtension("class");
                priorities.put(provider, Integer.parseInt(element.getAttribute("priority")));
                providers.add(provider);
            } catch (final CoreException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public ImmutableList<IProvider> getProviders() {
        Collections.sort(providers, new ProviderComparator());
        return ImmutableList.copyOf(providers);
    }

    public final void setProviderPriority(final IProvider provider, final int priority) {
        Preconditions.checkArgument(providers.contains(provider));
        Preconditions.checkArgument(priority > 0);
        ExtDocPlugin.getPreferences().putInt(getPreferenceId(provider), priority);
    }

    private static int getPriorityFromPreferences(final IProvider provider) {
        return ExtDocPlugin.getPreferences().getInt(getPreferenceId(provider), -1);
    }

    private static String getPreferenceId(final IProvider provider) {
        return "priority" + provider.hashCode();
    }

    private final class ProviderComparator implements Comparator<IProvider> {

        @Override
        public int compare(final IProvider provider1, final IProvider provider2) {
            final int priorityPreference1 = getPriorityFromPreferences(provider1);
            final Integer priorityPreference2 = getPriorityFromPreferences(provider2);
            if (priorityPreference1 > -1 || priorityPreference2 > -1) {
                return priorityPreference2.compareTo(priorityPreference1);
            }
            return priorities.get(provider2).compareTo(priorities.get(provider1));
        }
    }

}
