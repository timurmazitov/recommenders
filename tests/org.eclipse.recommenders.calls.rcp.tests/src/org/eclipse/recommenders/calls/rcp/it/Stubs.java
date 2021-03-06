/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.recommenders.calls.rcp.it;

import static com.google.common.base.Optional.fromNullable;
import static org.eclipse.recommenders.coordinates.ProjectCoordinate.UNKNOWN;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.recommenders.calls.ICallModel;
import org.eclipse.recommenders.calls.ICallModelProvider;
import org.eclipse.recommenders.completion.rcp.it.MockedIntelligentCompletionProposalComputer;
import org.eclipse.recommenders.completion.rcp.processable.SessionProcessorDescriptor;
import org.eclipse.recommenders.internal.calls.rcp.CallCompletionSessionProcessor;
import org.eclipse.recommenders.internal.calls.rcp.CallsRcpPreferences;
import org.eclipse.recommenders.internal.completion.rcp.CompletionRcpPreferences;
import org.eclipse.recommenders.internal.completion.rcp.ProposalNameProvider;
import org.eclipse.recommenders.models.UniqueTypeName;
import org.eclipse.recommenders.models.rcp.IProjectCoordinateProvider;
import org.eclipse.recommenders.rcp.SharedImages;
import org.eclipse.recommenders.utils.Recommendation;
import org.eclipse.recommenders.utils.Result;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.ui.IEditorPart;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@SuppressWarnings({ "restriction", "rawtypes", "unchecked" })
public class Stubs {

    private static final class CallModelSpy implements ICallModel {

        private Set<IMethodName> observedCalls = Sets.newHashSet();
        private IMethodName overriddenMethod;
        private IMethodName definedBy;
        private DefinitionKind defType;

        @Override
        public ITypeName getReceiverType() {
            return null;
        }

        @Override
        public void reset() {
        }

        @Override
        public boolean setObservedCalls(Set<IMethodName> observedCalls) {
            this.observedCalls = observedCalls;
            return true;
        }

        @Override
        public boolean setObservedOverrideContext(IMethodName overriddenMethod) {
            this.overriddenMethod = overriddenMethod;
            return true;
        }

        @Override
        public boolean setObservedDefiningMethod(IMethodName definedBy) {
            this.definedBy = definedBy;
            return true;
        }

        @Override
        public boolean setObservedDefinitionKind(DefinitionKind defType) {
            this.defType = defType;
            return true;
        }

        @Override
        public boolean setObservedPattern(String patternName) {
            return true;
        }

        @Override
        public Optional<IMethodName> getObservedOverrideContext() {
            return fromNullable(overriddenMethod);
        }

        @Override
        public Optional<IMethodName> getObservedDefiningMethod() {
            return fromNullable(definedBy);
        }

        @Override
        public Optional<DefinitionKind> getObservedDefinitionKind() {
            return fromNullable(defType);
        }

        @Override
        public ImmutableSet<IMethodName> getObservedCalls() {
            return ImmutableSet.copyOf(observedCalls);
        }

        @Override
        public ImmutableSet<IMethodName> getKnownCalls() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<IMethodName> getKnownDefiningMethods() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<IMethodName> getKnownOverrideContexts() {
            return ImmutableSet.of();
        }

        @Override
        public ImmutableSet<String> getKnownPatterns() {
            return ImmutableSet.of();
        }

        @Override
        public List<Recommendation<IMethodName>> recommendDefinitions() {
            return ImmutableList.of();
        }

        @Override
        public List<Recommendation<String>> recommendPatterns() {
            return ImmutableList.of();
        }

        @Override
        public List<Recommendation<IMethodName>> recommendCalls() {
            return ImmutableList.of();
        }

        @Override
        public ImmutableSet<DefinitionKind> getKnownDefinitionKinds() {
            return ImmutableSet.of();
        }
    }

    public static MockedIntelligentCompletionProposalComputer newCallComputer() {
        IProjectCoordinateProvider pcProvider = mock(IProjectCoordinateProvider.class);

        when(pcProvider.tryToUniqueName((IType) anyObject())).thenAnswer(new Answer<Result<UniqueTypeName>>() {

            @Override
            public Result<UniqueTypeName> answer(InvocationOnMock invocation) throws Throwable {
                // wanna refine this later.
                return Result.of(new UniqueTypeName(UNKNOWN, VmTypeName.OBJECT));
            }
        });

        // refine later
        when(pcProvider.toName((IType) anyObject())).thenReturn(VmTypeName.OBJECT);
        when(pcProvider.toName((IMethod) anyObject())).thenReturn(Optional.of(VmMethodName.NULL));
        Provider pcMetaProvider = mock(Provider.class);
        when(pcMetaProvider.get()).thenReturn(pcProvider);

        ICallModelProvider modelProvider = mock(ICallModelProvider.class);
        when(modelProvider.acquireModel((UniqueTypeName) anyObject()))
                .thenReturn(Optional.<ICallModel>of(new CallModelSpy()));
        Provider modelMetaProvider = mock(Provider.class);
        when(modelMetaProvider.get()).thenReturn(modelProvider);
        CallCompletionSessionProcessor sut = new CallCompletionSessionProcessor(pcMetaProvider, modelMetaProvider,
                new ProposalNameProvider(), new CallsRcpPreferences(), new SharedImages());

        CompletionRcpPreferences preferences = mock(CompletionRcpPreferences.class);
        SessionProcessorDescriptor sessionProcessor = new SessionProcessorDescriptor("", "", "", null, 0, true, "",
                sut);
        when(preferences.getEnabledSessionProcessors()).thenReturn(ImmutableSet.of(sessionProcessor));

        return new MockedIntelligentCompletionProposalComputer(sut, preferences, new Provider<IEditorPart>() {

            @Override
            public IEditorPart get() {
                return (IEditorPart) new CompilationUnitEditor();
            }
        });
    }
}
