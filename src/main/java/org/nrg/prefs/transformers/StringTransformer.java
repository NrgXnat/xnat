/*
 * prefs: org.nrg.prefs.transformers.IntegerTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StringTransformer extends AbstractPreferenceTransformer<String> {
    @Autowired
    public StringTransformer(final SerializerService serializer) {
        super(serializer);
    }

    @Override
    public String transform(final String serialized) {
        return serialized;
    }
}
