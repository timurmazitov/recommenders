/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marcel Bruch - initial API and implementation.
 */
package org.eclipse.recommenders.completion.rcp.chain.jdt;

import static org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Checks.ensureIsNotNull;
import static org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Optional.fromNullable;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.recommenders.completion.rcp.chain.jdt.deps.Optional;

public class CallChainEdge {

    public enum EdgeType {
        METHOD, FIELD, LOCAL_VARIABLE, CAST
    }

    private final IJavaElement element;
    private final Optional<IType> oSourceType;
    private Optional<IType> oReturnType;
    private int dimension;
    private EdgeType edgeType;

    // TODO I don't like var names sourceType javaelement... too generic
    public CallChainEdge(final IType sourceType, final IJavaElement javaElement) {
        this.oSourceType = fromNullable(sourceType);
        ensureIsNotNull(javaElement);
        this.element = javaElement;
        try {
            initializeReturnType();
        } catch (final JavaModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public CallChainEdge(final IJavaElement unresolvedJavaElement) {
        this(null, unresolvedJavaElement);
    }

    private void initializeReturnType() throws JavaModelException {
        String typeSignature = null;
        switch (getEdgeElement().getElementType()) {
        case IJavaElement.FIELD:
            final IField field = getEdgeElement();
            typeSignature = field.getTypeSignature();
            edgeType = EdgeType.FIELD;
            break;
        case IJavaElement.LOCAL_VARIABLE:
            final ILocalVariable local = getEdgeElement();
            typeSignature = local.getTypeSignature();
            edgeType = EdgeType.LOCAL_VARIABLE;
            break;
        case IJavaElement.METHOD:
            final IMethod method = getEdgeElement();
            typeSignature = method.getReturnType();
            edgeType = EdgeType.METHOD;
            break;
        }
        dimension = Signature.getArrayCount(typeSignature.toCharArray());
        oReturnType = InternalAPIsHelper.findTypeFromSignature(typeSignature, element);
    }

    /**
     * Instance of {@link IMethod}, {@link IField}, {@link ILocalVariable}, or {@link IType}(if edge type is cast)
     */
    @SuppressWarnings("unchecked")
    public <T extends IJavaElement> T getEdgeElement() {
        return (T) element;
    }

    public EdgeType getEdgeType() {
        return edgeType;
    }

    public Optional<IType> getReturnType() {
        return oReturnType;
    }

    /**
     * Note, the source or origin type is not necessarily identical to the declaring type of {@link #getEdgeElement()}!
     * For instance, the {@link #getEdgeElement()} may be defined in a superclass of the actual (#getSourceType()).
     */
    public Optional<IType> getSourceType() {
        return oSourceType;
    }

    public boolean isArray() {
        return dimension > 0;
    }

    public int getDimension() {
        return dimension;
    }

    public boolean isAssignableTo(final IType lhsType) {
        ensureIsNotNull(lhsType);
        if (oReturnType.isPresent()) {
            return InternalAPIsHelper.isAssignable(lhsType, oReturnType.get());
        }
        return false;
    }

    public boolean isChainAnchor() {
        return !oSourceType.isPresent();
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof CallChainEdge) {
            final CallChainEdge other = (CallChainEdge) obj;
            return element.equals(other.element);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        try {
            switch (getEdgeElement().getElementType()) {
            case IJavaElement.FIELD:
                return ((IField) getEdgeElement()).getKey();
            case IJavaElement.LOCAL_VARIABLE:
                return ((ILocalVariable) getEdgeElement()).getHandleIdentifier();
            case IJavaElement.METHOD:
                final IMethod m = getEdgeElement();
                return m.getElementName() + m.getSignature();
            }
        } catch (final JavaModelException e) {
            return e.toString();
        }
        return super.toString();
    }
}
