/**
 * Copyright (c) 2010, 2013 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static org.eclipse.jface.viewers.StyledString.DECORATIONS_STYLER;

import java.util.Dictionary;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.discovery.Catalog;
import org.eclipse.equinox.internal.p2.discovery.DiscoveryCore;
import org.eclipse.equinox.internal.p2.discovery.compatibility.RemoteBundleDiscoveryStrategy;
import org.eclipse.equinox.internal.p2.ui.discovery.util.WorkbenchUtil;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.CatalogConfiguration;
import org.eclipse.equinox.internal.p2.ui.discovery.wizards.DiscoveryWizard;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.rcp.SharedImages.Images;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("restriction")
public class DiscoverCompletionProposal extends AbstractJavaCompletionProposal {

    private static final String PROPOSAL_LABEL = "Nothing found? Discover new extensions to Code Recommenders";
    private static final String PROPOSAL_DESCRIPTION = "There are quite a few extensions available to Code Recommenders. Press return to learn more about Code Recommenders' incubation projects or how to leverage the power of crowd-soucing in your IDE.";
    private static final String PROPOSAL_CATEGORY_NAME = "Eclipse Code Recommenders";
    private static final String DISCOVERY_URL = "http://download.eclipse.org/recommenders/discovery/2.0/directory.xml";
    private static final Object DUMMY_INFO = new Object();

    // leave a bit space for other, maybe more important proposals
    private static final int RELEVANCE = Integer.MAX_VALUE - 10001;

    public DiscoverCompletionProposal(SharedImages images, int invocationOffset) {
        Image image = images.getImage(Images.OBJ_LIGHTBULB);
        StyledString text = new StyledString(PROPOSAL_LABEL, DECORATIONS_STYLER);
        setStyledDisplayString(text);
        setImage(image);
        setRelevance(RELEVANCE);
        setSortString(text.getString());
        setCursorPosition(invocationOffset);
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return DUMMY_INFO;
    }

    @Override
    public IInformationControlCreator getInformationControlCreator() {
        return new IInformationControlCreator() {

            @Override
            public IInformationControl createInformationControl(Shell parent) {
                return new ConfigureContentAssistInformationControl(parent, PROPOSAL_CATEGORY_NAME);
            }
        };
    }

    @Override
    protected boolean isValidPrefix(String prefix) {
        return true;
    }

    @Override
    public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {

        Catalog catalog = new Catalog();
        Dictionary<Object, Object> env = DiscoveryCore.createEnvironment();
        catalog.setEnvironment(env);
        catalog.setVerifyUpdateSiteAvailability(false);

        // add strategy for retrieving remote catalog

        // look for remote descriptor
        RemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new RemoteBundleDiscoveryStrategy();
        remoteDiscoveryStrategy.setDirectoryUrl(DISCOVERY_URL);
        catalog.getDiscoveryStrategies().add(remoteDiscoveryStrategy);

        CatalogConfiguration configuration = new CatalogConfiguration();
        configuration.setShowTagFilter(false);

        DiscoveryWizard wizard = new DiscoveryWizard(catalog, configuration);
        WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(), wizard);
        dialog.open();
    }

    private final class ConfigureContentAssistInformationControl extends AbstractInformationControl {

        private ConfigureContentAssistInformationControl(Shell parentShell, String statusFieldText) {
            super(parentShell, statusFieldText);
            create();
        }

        @Override
        public boolean hasContents() {
            return true;
        }

        @Override
        protected void createContent(Composite parent) {
            Display display = parent.getDisplay();
            Color bg = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            Link link = new Link(parent, SWT.NONE);
            link.setBackground(bg);
            link.setText(PROPOSAL_DESCRIPTION);
        }
    }
}