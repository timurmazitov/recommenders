/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Johannes Lerch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.ui;

import org.eclipse.recommenders.tests.commons.ui.utils.DefaultUiTest;
import org.eclipse.recommenders.tests.commons.ui.utils.FixtureUtil;
import org.eclipse.recommenders.tests.commons.ui.utils.TestProjectClassesHelper;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Triggers SWTBot to test a given fixture project.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public final class UiTest extends DefaultUiTest {

    private static final String FIXTUREPROJECT = "org.eclipse.recommenders.tests.fixtures.rcp.codecompletion.templates";

    /**
     * @throws Exception
     *             Thrown, when an error occurs during copying the fixture
     *             project into the workspace used during test execution.
     */
    @Test
    @Ignore
    public void testClassesInFixtureProject() throws Exception {
        FixtureUtil.copyProjectToWorkspace(FIXTUREPROJECT);
        final TestProjectClassesHelper helper = new TestProjectClassesHelper(bot);
        helper.searchAndTestClasses(FIXTUREPROJECT);
    }

}
