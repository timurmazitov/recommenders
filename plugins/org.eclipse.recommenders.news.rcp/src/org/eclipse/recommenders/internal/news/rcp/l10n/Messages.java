/**
 * Copyright (c) 2015 Pawel Nowak.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.recommenders.internal.news.rcp.l10n;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.recommenders.internal.news.rcp.l10n.messages"; //$NON-NLS-1$

    public static String FEED_DESCRIPTOR_MALFORMED_URL;

    public static String PREFPAGE_TITLE;
    public static String PREFPAGE_DESCRIPTION;
    public static String PREFPAGE_BUTTON_NEW;
    public static String PREFPAGE_BUTTON_EDIT;
    public static String PREFPAGE_BUTTON_REMOVE;
    public static String PREFPAGE_NOTIFICATION_ENABLEMENT;
    public static String PREFPAGE_WEB_BROWSER_SETTINGS;
    public static String FEED_DIALOG_TITLE_NEW;
    public static String FEED_DIALOG_TITLE_EDIT;
    public static String FIELD_LABEL_FEED_NAME;
    public static String FIELD_LABEL_URL;
    public static String FIELD_LABEL_FEEDS;
    public static String FIELD_LABEL_NEWS_ENABLED;
    public static String FIELD_LABEL_POLLING_INTERVAL;
    public static String FIELD_LABEL_STARTUP_DELAY;
    public static String FEED_TOOLTIP;
    public static String FEED_DIALOG_ERROR_EMPTY_NAME;
    public static String FEED_DIALOG_ERROR_EMPTY_URL;
    public static String FEED_DIALOG_ERROR_PROTOCOL_UNSUPPORTED;
    public static String FEED_DIALOG_ERROR_INVALID_URL;
    public static String FEED_DIALOG_ERROR_POLLING_INTERVAL_INVALID;
    public static String FEED_DIALOG_ERROR_DUPLICATE_FEED;
    public static String FEED_DIALOG_DESCRIPTION;
    public static String FEED_NOT_POLLED_YET;
    public static String FEED_EMPTY;
    public static String FEED_CONTRIBUTED_BY;

    public static String JOB_NAME_POLLING_FEEDS;
    public static String JOB_NAME_READ_NEWS_ITEMS;

    public static String ACTION_LABEL_MARK_AS_READ;
    public static String ACTION_LABEL_PREFERENCES;
    public static String ACTION_LABEL_POLL_NOW;
    public static String ACTION_LABEL_TODAY;
    public static String ACTION_LABEL_YESTERDAY;
    public static String ACTION_LABEL_THIS_WEEK;
    public static String ACTION_LABEL_LAST_WEEK;
    public static String ACTION_LABEL_THIS_MONTH;
    public static String ACTION_LABEL_LAST_MONTH;
    public static String ACTION_LABEL_THIS_YEAR;
    public static String ACTION_LABEL_OLDER_ENTRIES;
    public static String ACTION_LABEL_UNDETERMINED_ENTRIES;
    public static String ACTION_LABEL_ALL_FEEDS_DISABLED;
    public static String ACTION_TOOLTIP_NEWS;
    public static String HINT_MORE_MESSAGES;
    public static String LABEL_DESKTOP_NOTIFICATION_LABEL;
    public static String LABEL_DESKTOP_NOTIFICATION_DESCRIPTION;

    public static String NOTIFICATION_TITLE;
    public static String NOTIFICATION_DESCRIPTION;

    public static String LABEL_READ_FEED;
    public static String LABEL_READ_ITEM;
    public static String LABEL_UNREAD_FEED;
    public static String LABEL_UNREAD_ITEM;

    public static String LOG_ERROR_READING_PROPERTIES;
    public static String LOG_ERROR_WRITING_PROPERTIES;
    public static String LOG_ERROR_FETCHING_MESSAGES;
    public static String LOG_ERROR_CONNECTING_URL_WITH_STATUS_CODE;
    public static String LOG_ERROR_MALFORMED_URL;

    public static String LOG_WARNING_CONNECTING_URL;
    public static String LOG_WARNING_DUPLICATE_FEED;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
