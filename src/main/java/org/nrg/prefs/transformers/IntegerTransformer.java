/*
 * org.nrg.prefs.transformers.IntegerTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

public class IntegerTransformer implements PreferenceTransformer<Integer> {
    @Override
    public Integer transform(final String serialized) {
        return Integer.parseInt(serialized);
    }
}
