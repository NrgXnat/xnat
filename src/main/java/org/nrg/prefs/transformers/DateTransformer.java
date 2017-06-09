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

import java.util.Date;

@Component
public class DateTransformer extends AbstractPreferenceTransformer<Date> {
    @Autowired
    public DateTransformer(final SerializerService serializer) {
        super(serializer);
    }

    @Override
    public Date transform(final String serialized) {
        return new Date(Long.parseLong(serialized));
    }
}
