/*
 * prefs: org.nrg.prefs.transformers.IntegerTransformer
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.transformers;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BooleanTransformer extends AbstractPreferenceTransformer<Boolean> {
    @Autowired
    public BooleanTransformer(final SerializerService serializer) {
        super(serializer);
    }

    @Override
    public boolean handles(final Class<?> valueType) {
        return super.handles(valueType) || StringUtils.equals("boolean", valueType.getName());
    }

    @Override
    public Boolean transform(final String serialized) {
        return Boolean.parseBoolean(serialized);
    }
}
