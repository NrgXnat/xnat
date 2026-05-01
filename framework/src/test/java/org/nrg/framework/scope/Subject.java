/*
 * framework: org.nrg.framework.scope.Subject
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.scope;

import org.nrg.framework.constants.Scope;

public class Subject extends Wired {
    @Override
    public Scope getScope() {
        return Scope.Subject;
    }
}
