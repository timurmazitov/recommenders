/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp;

import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.mockito.Mockito;

public class TestUtils {

    private static final String TEST_URL = "http://planeteclipse.org/planet/rss20.xml";

    public static FeedDescriptor enabled(String id) {
        IConfigurationElement config = Mockito.mock(IConfigurationElement.class);
        when(config.getAttribute("id")).thenReturn(id);
        when(config.getAttribute("url")).thenReturn(TEST_URL);
        return new FeedDescriptor(config, true);
    }

    public static FeedDescriptor disabled(String id) {
        IConfigurationElement config = Mockito.mock(IConfigurationElement.class);
        when(config.getAttribute("id")).thenReturn(id);
        when(config.getAttribute("url")).thenReturn(TEST_URL);
        return new FeedDescriptor(config, false);
    }

}