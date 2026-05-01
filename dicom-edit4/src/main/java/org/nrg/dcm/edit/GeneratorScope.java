/*
 * DicomEdit: org.nrg.dcm.edit.GeneratorScope
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.edit;

import java.util.HashMap;
import java.util.Map;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;


/**
 * 
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class GeneratorScope {
    private final Logger logger = LoggerFactory.getLogger(GeneratorScope.class);
    private final Map<String,GeneratorCache> maps = Maps.newHashMap();

    private interface GeneratorCache {
        String getValue(final int tag, final Iterable<String> params, final String old) throws ScriptEvaluationException;
    }

    private static final class TagCache implements GeneratorCache {
        private final Map<Integer,Map<String,String>> attrs = Maps.newHashMap();
        private final ValueGenerator generator;

        TagCache(final ValueGenerator generator) {
            this.generator = generator;
        }

        public String getValue(final int tag, final Iterable<String> params, final String old)
        throws ScriptEvaluationException {

            final Integer key = tag;
            if (!attrs.containsKey(key)) {
                attrs.put(key, new HashMap<String,String>());
            }
            final Map<String,String> valueMap = attrs.get(key);
            final String v = valueMap.get(old);
            if (null == v) {
                final String newv = generator.valueFor(old, params);
                valueMap.put(old, newv);
                return newv;
            } else {
                return v;
            }
        }
    }

    public void setGenerator(final String label, final ValueGenerator generator) {
        maps.put(label, new TagCache(generator));
    }

    /**
     * 
     * @param label  The tag label.
     * @param tag    The tag value.
     * @param old prior value 
     * @param values concrete (String) values
     * @return The value as a string.
     * @throws ScriptEvaluationException When an error occurs evaluating the script.
     */
    public String getValue(final String label, final int tag, final String old, final Iterable<String> values)
    throws ScriptEvaluationException {
        logger.trace("generating value for {} {}", label, values);
        if (!maps.containsKey(label)) {
            throw new ScriptEvaluationException("undefined generator " + label);
        }
        return maps.get(label).getValue(tag, values, old);
    }
}
