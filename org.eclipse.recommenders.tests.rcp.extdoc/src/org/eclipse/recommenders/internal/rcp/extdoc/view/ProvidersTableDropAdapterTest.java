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
package org.eclipse.recommenders.internal.rcp.extdoc.view;

import org.eclipse.recommenders.internal.rcp.extdoc.ProviderStore;
import org.eclipse.recommenders.internal.rcp.extdoc.ProvidersComposite;
import org.eclipse.recommenders.internal.rcp.extdoc.UpdateService;
import org.eclipse.recommenders.tests.commons.extdoc.ExtDocUtils;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

public final class ProvidersTableDropAdapterTest {

    @Test
    public void testProvidersTableDropAdapter() {
        final ProvidersTable table = new ProvidersTable(ExtDocUtils.getShell(), new ProviderStore(),
                new UpdateService());
        table.setProvidersComposite(new ProvidersComposite(ExtDocUtils.getShell(), ExtDocUtils.getWorkbenchWindow()));
        final TableItem item = table.addProvider(ProvidersTableTest.mockProviderComposite(), "Test", null);

        final ProvidersTableDropAdapter adapter = new ProvidersTableDropAdapter(table.getTable(), null,
                new ProviderStore());

        adapter.dropTableItem(item, 0);
    }
}
