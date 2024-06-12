/*
 * org.nrg.framework.status.StatusListenerI
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/2/13 12:20 PM
 */
package org.nrg.framework.status;

import java.util.EventListener;

public interface StatusListenerI extends EventListener {
    void notify(StatusMessage message);
}
