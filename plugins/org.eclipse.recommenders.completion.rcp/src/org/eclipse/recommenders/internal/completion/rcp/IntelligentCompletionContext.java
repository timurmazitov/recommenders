/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.internal.completion.rcp;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.utils.Checks.cast;
import static org.eclipse.recommenders.utils.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.utils.Throws.throwUnhandledException;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.InternalCompletionContext;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnSingleNameReference;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.corext.util.MethodOverrideTester;
import org.eclipse.jdt.internal.corext.util.SuperTypeHierarchyCache;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Region;
import org.eclipse.recommenders.completion.rcp.IIntelligentCompletionContext;
import org.eclipse.recommenders.internal.analysis.codeelements.Variable;
import org.eclipse.recommenders.rcp.RecommendersPlugin;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.CompilerBindings;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.JdtUtils;

import com.google.common.base.Optional;

@SuppressWarnings({ "restriction", "deprecation" })
public class IntelligentCompletionContext implements IIntelligentCompletionContext {

    private final JavaContentAssistInvocationContext jdtCompletionContext;

    private CompilerAstCompletionNodeFinder astCompletionNodeFinder;

    private CompilationUnit jdtCompilationUnit;

    private IntelligentCompletionRequestor completionRequestor;

    private CompletionEngine completionEngine;

    private CompletionParser completionParser;

    private String token;

    private final JavaElementResolver resolver;

    public IntelligentCompletionContext(final JavaContentAssistInvocationContext jdtCtx,
            final JavaElementResolver resolver) {
        ensureIsNotNull(jdtCtx);
        ensureIsNotNull(resolver);
        jdtCompletionContext = jdtCtx;
        this.resolver = resolver;
        jdtCompilationUnit = (org.eclipse.jdt.internal.core.CompilationUnit) jdtCtx.getCompilationUnit();
        initializeCompletionPrefixToken();
        initializeRequestor();
        initializeCompletionEngine();
        performCodeCompletion();
        findCompletionNode();
        clearStateIfNoCompletionNodeFound();
    }

    private void initializeCompletionPrefixToken() {
        try {
            token = jdtCompletionContext.computeIdentifierPrefix().toString();
        } catch (final BadLocationException x) {
            IntelligentCompletionPlugin.logError(x, "Computing token from active editor failed.");
            token = "";
        }
    }

    private void initializeRequestor() {
        completionRequestor = new IntelligentCompletionRequestor(jdtCompilationUnit);
    }

    private void initializeCompletionEngine() {
        try {
            final JavaProject project = (JavaProject) jdtCompilationUnit.getJavaProject();
            final WorkingCopyOwner owner = jdtCompilationUnit.getOwner();
            final SearchableEnvironment s = project.newSearchableNameEnvironment(owner);
            completionEngine = new CompletionEngine(s, completionRequestor, project.getOptions(true), project, owner,
                    new NullProgressMonitor());
        } catch (final JavaModelException x) {
            throwUnhandledException(x);
        }
    }

    private void performCodeCompletion() {
        final org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCu;
        if (jdtCompilationUnit.isWorkingCopy()) {
            compilerCu = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) jdtCompilationUnit
                    .getOriginalElement();
        } else {
            compilerCu = jdtCompilationUnit;
        }
        completionEngine.complete(compilerCu, getInvocationOffset(), 0, jdtCompilationUnit.getPrimary());
        completionParser = (CompletionParser) completionEngine.getParser();
    }

    private void findCompletionNode() {
        astCompletionNodeFinder = new CompilerAstCompletionNodeFinder();
        final ReferenceContext referenceContext = completionParser.referenceContext;
        if (completionParser.compilationUnit != null) {
            final CompilationUnitDeclaration compilationUnit = completionParser.compilationUnit;
            // completion parser sets this to true after his run and thus
            // prevents visitors to visit this cu a second time. Reset this
            // state:
            compilationUnit.ignoreFurtherInvestigation = false;
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        } else if (referenceContext instanceof CompilationUnitDeclaration) {
            final CompilationUnitDeclaration compilationUnit = cast(referenceContext);
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        } else if (referenceContext instanceof AbstractMethodDeclaration) {
            final CompilationUnitDeclaration compilationUnit = findCompilationUnit((AbstractMethodDeclaration) referenceContext);
            compilationUnit.traverse(astCompletionNodeFinder, compilationUnit.scope);
        }
    }

    private CompilationUnitDeclaration findCompilationUnit(final AbstractMethodDeclaration methodContext) {
        Scope tmp = methodContext.scope;
        while (tmp != null) {
            tmp = tmp.parent;
            if (tmp instanceof CompilationUnitScope) {
                final CompilationUnitScope scope = cast(tmp);
                return scope.referenceContext;
            }
        }
        return null;
    }

    private void clearStateIfNoCompletionNodeFound() {
        if (!astCompletionNodeFinder.isCompletionNodeFound()) {
            astCompletionNodeFinder.clearState();
            jdtCompilationUnit = null;
            completionEngine = null;
            completionParser = null;
            // completionRequestor = null;
            // invocationOffset = -1;
            // jdtCompletionContext = null;
            // token = null;
        }
    }

    public Set<CompletionProposal> getProposals() {
        return completionRequestor.getProposals();
    }

    public int getMaximalProposalRelevance() {
        return completionRequestor.getMaximalProposalRelevance();
    }

    @Override
    public IJavaCompletionProposal toJavaCompletionProposal(final CompletionProposal proposal) {
        return completionRequestor.toJavaCompletionProposal(proposal);
    }

    @Override
    public Set<CompletionProposal> getJdtProposals() {
        return completionRequestor.getProposals();
    }

    @Override
    public InternalCompletionContext getCoreCompletionContext() {
        return completionRequestor.getCompletionContext();
    }

    @Override
    public String getPrefixToken() {
        return token;
    }

    @Override
    public Statement getCompletionNode() {
        return astCompletionNodeFinder.completionNode;
    }

    @Override
    public Statement getCompletionNodeParent() {
        return astCompletionNodeFinder.completionNodeParent;
    }

    @Override
    public Set<FieldDeclaration> getFieldDeclarations() {
        return astCompletionNodeFinder.fieldDeclarations;
    }

    @Override
    public Set<LocalDeclaration> getLocalDeclarations() {
        return astCompletionNodeFinder.localDeclarations;
    }

    @Override
    public Set<MethodDeclaration> getMethodDeclarations() {
        return astCompletionNodeFinder.methodDeclarations;
    }

    @Override
    public ICompilationUnit getCompilationUnit() {
        return jdtCompletionContext.getCompilationUnit();
    }

    @Override
    public IMethodName getEnclosingMethod() {
        final IMethod method = findEnclosingMethod();
        return method == null ? null : resolver.toRecMethod(method);
    }

    private IMethod findEnclosingMethod() {

        final IJavaElement element = findEnclosingElement();
        if (element instanceof IMethod) {
            return cast(element);

        }
        return null;
    }

    private IJavaElement findEnclosingElement() {
        try {
            final CompletionContext coreContext = completionRequestor.getCompletionContext();
            final IJavaElement element = coreContext.getEnclosingElement();
            return element;
        } catch (final RuntimeException e) {
            RecommendersPlugin.logError(e, "error in jdt resolving enclosing element.",
                    completionRequestor.getCompletionContext());
        }
        return null;
    }

    @Override
    public IMethodName getEnclosingMethodsFirstDeclaration() {
        final IMethod method = findEnclosingMethod();
        if (method == null) {
            return null;
        }

        final IMethod firstDeclaration = findFirstDeclaration(method);
        if (firstDeclaration == null) {
            return null;
        }
        return resolver.toRecMethod(firstDeclaration);
    }

    private IMethod findFirstDeclaration(IMethod method) {
        ensureIsNotNull(method);
        method = (IMethod) method.getPrimaryElement();
        try {
            final MethodOverrideTester methodOverrideTester = SuperTypeHierarchyCache.getMethodOverrideTester(method
                    .getDeclaringType());
            return methodOverrideTester.findDeclaringMethod(method, true);
        } catch (final JavaModelException x) {
            RecommendersPlugin.log(x);
        }
        return null;
    }

    private ITypeName getSuperclassOfEnclosingType() {
        try {
            IType enclosingType = findEnclosingType();
            if (enclosingType == null) {
                return null;
            }
            // TODO :: Rework code to resolve supertype name... this is
            // odd/wrong location for this, right?
            enclosingType = JdtUtils.resolveJavaElementProxy(enclosingType);
            final ITypeHierarchy typeHierarchy = SuperTypeHierarchyCache.getTypeHierarchy(enclosingType);
            final IType superclass = typeHierarchy.getSuperclass(enclosingType);
            return superclass == null ? null : resolver.toRecType(superclass);
        } catch (final JavaModelException e) {
            throw throwUnhandledException(e);
        }
    }

    @Override
    public ITypeName getEnclosingType() {
        final IType enclosingType = findEnclosingType();
        return enclosingType == null ? null : resolver.toRecType(enclosingType);
    }

    private IType findEnclosingType() {
        final IJavaElement element = findEnclosingElement();
        if (element instanceof IMethod) {
            return ((IMethod) element).getDeclaringType();
        } else if (element instanceof IField) {
            return ((IField) element).getDeclaringType();
        } else if (element instanceof IType) {
            return (IType) element;
        }
        return null;
    }

    @Override
    public String getReceiverName() {
        return astCompletionNodeFinder.receiverName;
    }

    @Override
    public ITypeName getReceiverType() {
        return CompilerBindings.toTypeName(astCompletionNodeFinder.receiverType).orNull();
    }

    @Override
    public ITypeName getExpectedType() {
        return CompilerBindings.toTypeName(astCompletionNodeFinder.expectedReturnType).orNull();
    }

    @Override
    public boolean expectsReturnValue() {
        return astCompletionNodeFinder.expectsReturnType;
    }

    @Override
    public Region getReplacementRegion() {
        final int end = jdtCompletionContext.getInvocationOffset();
        final int length = token.length();
        final int start = end - length;
        return new Region(start, length);
    }

    @Override
    public JavaContentAssistInvocationContext getOriginalContext() {
        return jdtCompletionContext;
    }

    @Override
    public Variable getVariable() {
        if (isReceiverImplicitThis() || isReceiverExplicitThis()) {
            return Variable.create("this", getSuperclassOfEnclosingType(), getEnclosingMethod());
        }

        if (getReceiverName() != null && getReceiverType() != null) {
            return Variable.create(getReceiverName(), getReceiverType(), getEnclosingMethod());
        }
        final Optional<LocalDeclaration> match = findMatchingLocalVariable(getReceiverName());
        if (!match.isPresent()) {
            return null;
        }
        final LocalDeclaration local = match.get();
        final String name = String.valueOf(local.name);
        final ITypeName type = CompilerBindings.toTypeName(local.type).orNull();
        return Variable.create(name, type, getEnclosingMethod());
    }

    private boolean isReceiverExplicitThis() {
        return "this".equals(getReceiverName());
    }

    @Override
    public boolean isReceiverImplicitThis() {
        return ("".equals(getReceiverName()) || astCompletionNodeFinder.completionNode instanceof CompletionOnSingleNameReference)
                && getReceiverType() == null;
    }

    @Override
    public Variable findMatchingVariable(final String variableName) {
        Optional<? extends AbstractVariableDeclaration> match = findMatchingLocalVariable(getReceiverName());
        if (!match.isPresent()) {
            match = findMatchingFieldDeclaration(variableName);
            if (!match.isPresent()) {
                // finally, try the given name??? XXX what's the meaning of this
                // API call? Why don't use the given variable name directly?
                match = findMatchingLocalVariable(variableName);
            }

        }
        if (!match.isPresent()) {
            return null;
        }

        final AbstractVariableDeclaration var = match.get();
        final String name = String.valueOf(var.name);
        final Optional<ITypeName> oType = CompilerBindings.toTypeName(var.type);

        if (!oType.isPresent()) {
            return null;
        }
        return Variable.create(name, oType.get(), getEnclosingMethod());

    }

    private Optional<LocalDeclaration> findMatchingLocalVariable(final String receiverName) {
        for (final LocalDeclaration local : getLocalDeclarations()) {
            final String name = String.valueOf(local.name);
            if (name.equals(receiverName)) {
                return fromNullable(local);
            }
        }
        return absent();
    }

    private Optional<FieldDeclaration> findMatchingFieldDeclaration(final String receiverName) {
        for (final FieldDeclaration field : getFieldDeclarations()) {
            final String name = String.valueOf(field.name);
            if (name.equals(receiverName)) {
                return fromNullable(field);
            }
        }
        return absent();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(astCompletionNodeFinder, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public int getInvocationOffset() {
        return jdtCompletionContext.getInvocationOffset();
    }
}