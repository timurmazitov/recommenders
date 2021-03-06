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
package org.eclipse.recommenders.utils.rcp.preferences;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.recommenders.internal.utils.rcp.l10n.Messages;
import org.eclipse.recommenders.internal.utils.rcp.preferences.ContributionLink;
import org.eclipse.recommenders.internal.utils.rcp.preferences.ContributionsReader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class AbstractLinkContributionPage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String COMMAND_HREF_ID = "org.eclipse.recommenders.utils.rcp.linkContribution.href"; //$NON-NLS-1$

    private final String preferencePageId;

    public AbstractLinkContributionPage(String preferencePageId) {
        this.preferencePageId = preferencePageId;
    }

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        List<ContributionLink> contributionLinks = getContributionLinks();

        if (!contributionLinks.isEmpty()) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridDataFactory.fillDefaults().applyTo(composite);
            GridLayoutFactory.swtDefaults().margins(0, 5).applyTo(composite);

            Group group = new Group(composite, SWT.NONE);
            group.setText(Messages.PREFPAGE_LINKS_DESCRIPTION);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
            GridLayoutFactory.swtDefaults().numColumns(2).applyTo(group);

            for (ContributionLink link : contributionLinks) {
                link.appendLink(group);
            }
            applyDialogFont(composite);
        }

        return parent;
    }

    public List<ContributionLink> getContributionLinks() {
        return ContributionsReader.readContributionLinks(preferencePageId);
    }
}
