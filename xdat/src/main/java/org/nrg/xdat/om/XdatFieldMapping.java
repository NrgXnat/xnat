/*
 * core: org.nrg.xdat.om.XdatFieldMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.om;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.base.BaseXdatFieldMapping;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;
import java.util.function.Predicate;

/**
 * @author XDAT
 */
@SuppressWarnings("serial")
public class XdatFieldMapping extends BaseXdatFieldMapping {
    public XdatFieldMapping(ItemI item) {
        super(item);
    }

    public XdatFieldMapping(UserI user) {
        super(user);
    }

    /*
     * @deprecated Use BaseXdatFieldMapping(UserI user)
     **/
    public XdatFieldMapping() {
    }

    @SuppressWarnings("unused")
	public XdatFieldMapping(Hashtable properties, UserI user) {
        super(properties, user);
    }

    public void init(String psf, String value, Boolean create, Boolean read, Boolean delete, Boolean edit, Boolean activate) {
        this.setField(psf);
        this.setFieldValue(value);
        this.setCreateElement(create);
        this.setReadElement(read);
        this.setEditElement(edit);
        this.setDeleteElement(delete);
        this.setActiveElement(activate);
        this.setComparisonType("equals");
    }

    public static Filter filter(final String fieldName, final String fieldValue) {
    	return new Filter(fieldName, fieldValue);
	}

    public static class Filter implements Predicate<XdatFieldMapping> {
        Filter(final String fieldName, final String fieldValue) {
            _fieldName = fieldName;
            _fieldValue = fieldValue;
        }

        @Override
        public boolean test(final XdatFieldMapping fieldMapping) {
            return fieldMapping != null && StringUtils.equals(fieldMapping.getField(), _fieldName) && StringUtils.equals(fieldMapping.getFieldValue(), _fieldValue);
        }

        private final String _fieldName;
        private final String _fieldValue;
    }
}
