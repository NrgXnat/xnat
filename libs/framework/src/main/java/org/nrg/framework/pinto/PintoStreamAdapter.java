/*
 * framework: org.nrg.framework.pinto.PintoStreamAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import java.io.PrintStream;

public interface PintoStreamAdapter {
    abstract public PrintStream getOutputStream();
}
