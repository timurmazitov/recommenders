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

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.recommenders.commons.utils.names.IName;
import org.eclipse.recommenders.rcp.extdoc.ExtDocPlugin;
import org.eclipse.recommenders.rcp.extdoc.IProvider;
import org.eclipse.recommenders.rcp.extdoc.SwtFactory;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Preconditions;

final class CommentsComposite {

    private static Image commentsIcon = ExtDocPlugin.getIcon("eview16/comments.png");
    private static DateFormat dateFormat = DateFormat.getDateInstance();

    private IName element;
    private String keyAppendix;
    private IProvider provider;
    private IUserFeedbackServer server;
    private List<IComment> comments;

    private Composite composite;

    static CommentsComposite create(final IName element, final String keyAppendix, final IProvider provider,
            final IUserFeedback feedback, final IUserFeedbackServer server, final Composite parent) {
        final CommentsComposite comments = new CommentsComposite();
        comments.provider = provider;
        comments.server = Preconditions.checkNotNull(server);
        comments.element = element;
        comments.keyAppendix = keyAppendix;
        comments.comments = new LinkedList<IComment>(feedback.getComments());
        comments.createContents(parent);
        return comments;
    }

    private void createContents(final Composite parent) {
        composite = SwtFactory.createGridComposite(parent, 1, 0, 5, 0, 0);
        createCommentsArea();
    }

    private void createCommentsArea() {
        SwtFactory.createLink(composite, "Show / Add Comments (" + comments.size() + ")", null, commentsIcon, true,
                new MouseAdapter() {
                    @Override
                    public void mouseUp(final MouseEvent event) {
                        displayComments();
                    }
                });
    }

    void displayComments() {
        disposeChildren();

        if (!comments.isEmpty()) {
            for (final IComment comment : comments) {
                final String headLine = String.format("%s, %s", dateFormat.format(comment.getDate()),
                        comment.getUsername());
                SwtFactory.createCLabel(composite, headLine, true, commentsIcon);
                SwtFactory.createLabel(composite, comment.getText(), true);
            }
            SwtFactory.createLabel(composite, "", false);
        }

        displayAddComment();
        layout();
    }

    private void displayAddComment() {
        final Text text = SwtFactory.createTextArea(composite, "", 45, 0);

        final Composite buttons = SwtFactory.createGridComposite(composite, 2, 5, 0, 0, 0);
        SwtFactory.createButton(buttons, "Add Comment", new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                addComment(text.getText());
            }
        });
        SwtFactory.createButton(buttons, "Hide Comments", new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                hideComments();
            }
        });
    }

    void addComment(final String text) {
        final IComment comment = server.addComment(text, element, keyAppendix, provider);
        comments.add(comment);
        displayComments();
    }

    void hideComments() {
        disposeChildren();
        createCommentsArea();
        layout();
    }

    private void disposeChildren() {
        for (final Control child : composite.getChildren()) {
            child.dispose();
        }
    }

    private void layout() {
        composite.layout(true);
        if (composite.getParent().getParent() != null) {
            composite.getParent().getParent().getParent().layout(true);
        }
    }

    void dispose() {
        composite.dispose();
    }
}
