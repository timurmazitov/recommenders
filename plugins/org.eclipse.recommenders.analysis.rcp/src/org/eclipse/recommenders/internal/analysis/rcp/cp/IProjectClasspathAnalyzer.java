/**
 * Copyright (c) 2010, 2011 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 */
package org.eclipse.recommenders.internal.analysis.rcp.cp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.recommenders.internal.analysis.ProjectClasspath;

public interface IProjectClasspathAnalyzer {

    public ProjectClasspath analyze(final IJavaProject project, final IProgressMonitor monitor);
}
