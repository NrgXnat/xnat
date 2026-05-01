/*
 * PrearcImporter: org.nrg.Restructurer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg;

import java.io.File;
import java.util.Collection;

import org.nrg.framework.status.StatusProducerI;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public interface Restructurer extends StatusProducerI,Runnable,Iterable<File> {
    String SCANS_DIR = "SCANS";

    Collection<File> getSessions();
}
