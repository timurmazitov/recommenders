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
package org.eclipse.recommenders.internal.rcp.extdoc.providers.swt;

import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.recommenders.rcp.extdoc.features.CommunityFeatures;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public final class TextAndFeaturesLine {

    private final StyledText styledText;

    public TextAndFeaturesLine(final Composite parent, final String text, final CommunityFeatures features) {
        final Composite line = SwtFactory.createGridComposite(parent, 2, 10, 0, 0, 0);
        styledText = SwtFactory.createStyledText(line, text, SWT.COLOR_BLACK, true);
        features.loadStarsRatingComposite(line);
    }

    public void createStyleRange(final int start, final int length, final int fontStyle, final boolean makeBlue,
            final boolean makeCodeFont) {
        SwtFactory.createStyleRange(styledText, start, length, fontStyle, makeBlue, makeCodeFont);
    }

}
