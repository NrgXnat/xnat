/*
 * prefs: org.nrg.prefs.transformers.PreferenceTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

public interface PreferenceTransformer<T> {
    T transform(final String serialized);
}
