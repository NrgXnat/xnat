/*
 * web: org.nrg.xnat.services.DataTypeInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

/**
 * Data type information container
 */
public class DataTypeInfo {
    private final String elementName;
    private final String singular;
    private final String plural;
    private final boolean secured;
    private final Long count;

    public DataTypeInfo(String elementName, String singular, String plural, boolean secured, Long count) {
        this.elementName = elementName;
        this.singular = singular;
        this.plural = plural;
        this.secured = secured;
        this.count = count;
    }

    public String getElementName() {
        return elementName;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    public boolean isSecured() {
        return secured;
    }

    public Long getCount() {
        return count;
    }
}
