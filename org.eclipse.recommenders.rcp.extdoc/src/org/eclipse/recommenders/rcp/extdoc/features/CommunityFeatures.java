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
package org.eclipse.recommenders.rcp.extdoc.features;

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.swt.widgets.Composite;

import com.google.common.base.Preconditions;

public final class CommunityFeatures {

    private IProvider provider;
    private IUserFeedbackServer server;
    private IName element;
    private String keyAppendix;
    private IUserFeedback feedback;

    public static CommunityFeatures create(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedbackServer server) {
        return create(element, keyAppendix, provider, server, server.getUserFeedback(element, keyAppendix, provider));
    }

    public static CommunityFeatures create(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedbackServer server, final IUserFeedback feedback) {
        if (element == null) {
            return null;
        }
        final CommunityFeatures features = new CommunityFeatures();
        features.provider = provider;
        features.server = Preconditions.checkNotNull(server);
        features.element = element;
        features.keyAppendix = keyAppendix;
        features.feedback = feedback;
        return features;
    }

    public CommentsComposite loadCommentsComposite(final Composite parent) {
        return CommentsComposite.create(element, keyAppendix, provider, feedback, server, parent);
    }

    public StarsRatingComposite loadStarsRatingComposite(final Composite parent) {
        return new StarsRatingComposite(element, keyAppendix, provider, feedback, server, parent);
    }

}
