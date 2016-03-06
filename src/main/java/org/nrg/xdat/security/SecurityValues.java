/*
 * org.nrg.xdat.security.SecurityValues
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.security;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author Tim Olsen &lt;tim@deck5consulting.com&gt;
 *
 * Basically just a map of key value pairs identifying items membership.
 *
 * example: a subject that is owned by project one, and shared into project 2
 *
 * <pre>Map{"xnat:subjectData/project":"proj1","xnat:subjectData/sharing/share":"proj2"}</pre>
 */
public class SecurityValues {
    private Map<String, String> hash = Maps.newHashMap();

    public void put(String field, String value) throws Exception {
        hash.put(field, value);
    }

    public void setHash(Map<String, String> h) {
        hash = h;
    }

    public Map<String, String> getHash() {
        return hash;
    }
}
