/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package org.eclipse.recommenders.internal.rcp.codecompletion.templates.unit;

import java.util.HashSet;

import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.VmMethodName;
import org.eclipse.recommenders.commons.utils.names.VmTypeName;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.code.CodeBuilder;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.CompletionTargetVariable;
import org.eclipse.recommenders.internal.rcp.codecompletion.templates.types.MethodCall;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CodeBuilderTest.class, CompletionProposalsBuilderTest.class, CompletionTargetVariableBuilderTest.class,
        MethodCallFormatterTest.class, MethodFormatterTest.class, PatternRecommenderTest.class,
        TemplatesCompletionProposalComputerTest.class })
public final class UnitTestSuite {

    private static final CompletionTargetVariable DEFAULTVARIABLE = new CompletionTargetVariable("constructed",
            VmTypeName.get("Lorg/eclipse/swt/widgets/Button"), new HashSet<IMethodName>(), new Region(0, 0), false);
    private static final CompletionTargetVariable CONSTRUCTORVARIABLE = new CompletionTargetVariable("unconstructed",
            VmTypeName.get("Lorg/eclipse/swt/widgets/Button"), new HashSet<IMethodName>(), new Region(0, 0), true);

    private static final IMethodName DEFAULTMETHOD = VmMethodName.get("Lorg/eclipse/swt/widgets/Button.setText()V");
    private static final IMethodName CONSTRUCTORMETHOD = VmMethodName
            .get("Lorg/eclipse/swt/widgets/Button.<init>(Lorg/eclipse/swt/widgets/Composite;Lint;)V");

    private static final MethodCall METHODCALL = new MethodCall(DEFAULTVARIABLE.getName(), DEFAULTMETHOD);
    private static final MethodCall CONSTRUCTORCALL = new MethodCall(CONSTRUCTORVARIABLE.getName(), CONSTRUCTORMETHOD);

    private static final CodeBuilder CODEBUILDERMOCK = new CodeBuilder(
            MethodCallFormatterTest.getMethodCallFormatterMock());

    /**
     * @return the method which is used in most test cases.
     */
    protected static MethodCall getDefaultMethodCall() {
        return METHODCALL;
    }

    /**
     * @return the constructor call which is used in most test cases.
     */
    protected static MethodCall getDefaultConstructorCall() {
        return UnitTestSuite.CONSTRUCTORCALL;
    }

    protected static CompletionTargetVariable getDefaultConstructorTargetVariable() {
        return CONSTRUCTORVARIABLE;
    }

    protected static CodeBuilder getCodeBuilderMock() {
        return CODEBUILDERMOCK;
    }
}
