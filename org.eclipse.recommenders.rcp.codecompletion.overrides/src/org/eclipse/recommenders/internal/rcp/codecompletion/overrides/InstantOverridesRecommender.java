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
package org.eclipse.recommenders.internal.rcp.codecompletion.overrides;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.recommenders.commons.utils.Tuple;
import org.eclipse.recommenders.commons.utils.names.IMethodName;
import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.MethodDeclaration;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.TypeDeclaration;
import org.eclipse.recommenders.internal.rcp.codecompletion.overrides.net.ClassOverridesNetwork;
import org.eclipse.recommenders.internal.rcp.views.recommendations.IRecommendationsViewContentProvider;
import org.eclipse.recommenders.rcp.IRecommendation;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class InstantOverridesRecommender implements IRecommendationsViewContentProvider {
    private final double MIN_PROBABILITY_THRESHOLD = 0.1d;

    private final OverridesModelStore modelStore;

    private ClassOverridesNetwork model;

    private TypeDeclaration type;

    private ITypeName superclass;

    @Inject
    public InstantOverridesRecommender(final OverridesModelStore modelStore) {
        this.modelStore = modelStore;
    }

    public synchronized List<OverridesRecommendation> createRecommendations(final TypeDeclaration type) {
        this.type = type;
        this.superclass = type.superclass;
        if (!isSuperclassSupported()) {
            return Collections.emptyList();
        }
        loadSuperclassModel();
        computeRecommendations();
        return readRecommendations();
    }

    private boolean isSuperclassSupported() {
        return modelStore.hasModel(superclass);
    }

    private void loadSuperclassModel() {
        this.model = modelStore.getModel(superclass);
    }

    private void computeRecommendations() {
        model.clearEvidence();
        for (final MethodDeclaration method : type.methods) {
            model.observeMethodNode(method.superDeclaration);
        }
    };

    private List<OverridesRecommendation> readRecommendations() {
        final List<OverridesRecommendation> res = Lists.newLinkedList();
        for (final Tuple<IMethodName, Double> item : model.getRecommendedMethodOverrides(MIN_PROBABILITY_THRESHOLD, 5)) {
            final IMethodName method = item.getFirst();
            final Double probability = item.getSecond();
            final OverridesRecommendation recommendation = new OverridesRecommendation(method, probability);
            res.add(recommendation);
        }
        return res;
    }

    @Override
    public void attachRecommendations(final ICompilationUnit jdtCompilationUnit,
            final CompilationUnit recCompilationUnit, final Multimap<Object, IRecommendation> recommendations) {
        for (final TypeDeclaration type : recCompilationUnit.allTypes()) {
            final List<OverridesRecommendation> typeRecommendations = createRecommendations(type);
            recommendations.putAll(type, typeRecommendations);
        }
    }
}
