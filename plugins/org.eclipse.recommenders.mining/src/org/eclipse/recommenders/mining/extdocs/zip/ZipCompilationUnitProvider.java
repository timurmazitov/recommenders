/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcel Bruch - Initial API and implementation
 */
package org.eclipse.recommenders.mining.extdocs.zip;

import static org.eclipse.recommenders.utils.GenericEnumerationUtils.iterable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.recommenders.internal.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.mining.extdocs.AlgorithmParameters;
import org.eclipse.recommenders.mining.extdocs.ICompilationUnitProvider;
import org.eclipse.recommenders.mining.extdocs.ISuperclassProvider;
import org.eclipse.recommenders.utils.gson.GsonUtil;
import org.eclipse.recommenders.utils.names.ITypeName;

import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ZipCompilationUnitProvider implements ICompilationUnitProvider, ISuperclassProvider {

    private Multimap<ITypeName, CompilationUnit> cus;
    private final ZipFile zip;

    @Inject
    public ZipCompilationUnitProvider(final AlgorithmParameters config) {
        zip = config.getZip();
        loadCompilationUnits();
    }

    private void loadCompilationUnits() {
        cus = HashMultimap.create();

        for (final ZipEntry entry : iterable(zip.entries())) {
            final Optional<CompilationUnit> option = loadCompilationUnit(entry);
            if (option.isPresent()) {
                final CompilationUnit cu = option.get();
                final ITypeName superclass = cu.primaryType.superclass;
                if (superclass != null) {
                    cus.put(superclass, cu);
                }
            }
        }
    }

    @Override
    public Iterable<CompilationUnit> getCompilationUnits(final ITypeName superclass) {
        return cus.get(superclass);
    }

    private Optional<CompilationUnit> loadCompilationUnit(final ZipEntry entry) {
        try {
            if (entry.getName().endsWith(".json")) {
                final InputStream inputStream = zip.getInputStream(entry);
                final CompilationUnit res = GsonUtil.deserialize(inputStream, CompilationUnit.class);
                if (res.primaryType.superclass == null) {
                    return Optional.absent();
                }
                return Optional.fromNullable(res);
            }
        } catch (final IOException e) {
            // log.warn(String.format("Failed to parse '%s' from zip '%s'",
            // entry, zip.getName()), e);
        }
        return Optional.absent();
    }

    @Override
    public Set<ITypeName> getSuperclasses() {
        return cus.keySet();
    }
}