/**
 * Copyright (c) 2010 Darmstadt University of Technology.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andreas Kaluza, Marko Martin, Marcel Bruch - chain completion test scenario definitions 
 */
package data;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

//call chain 1 ok --> 1 element chain does not lead to expected solution 
public class CompletionOnMethodReturn {
    public void method() {
        //@start
        final IWorkbenchHelpSystem c = getPlatform()<@ignore^Space|getHelpSystem.*(1 element).*>
        //@end
        //final IWorkbenchHelpSystem c = getPlatform().getHelpSystem()
        /* calling context --> PlatformUI
         * expected type --> IWorkbenchHelpSystem
         * variable name --> c
         */
    }

    private IWorkbench getPlatform() {
        return PlatformUI.getWorkbench();
    }
}
