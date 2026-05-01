/*
 * DicomDB: org.nrg.util.Opener
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.util;

import java.io.IOException;
import java.io.InputStream;

public interface Opener<T> {
    InputStream open(T t) throws IOException;
}
