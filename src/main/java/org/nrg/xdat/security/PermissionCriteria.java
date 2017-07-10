/*
 * core: org.nrg.xdat.security.PermissionCriteria
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xft.ItemI;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.nrg.xdat.security.SecurityManager.*;

/**
 * @author Tim
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class PermissionCriteria implements PermissionCriteriaI {
    private static final String ALL              = "*";
    private static final String EQUALS           = "equals";
    private static final String ACTIVATE_ELEMENT = "active_element";
    private static final String CREATE_ELEMENT   = "create_element";
    private static final String EDIT_ELEMENT     = "edit_element";
    private static final String DELETE_ELEMENT   = "delete_element";
    private static final String READ_ELEMENT     = "read_element";
    private static final String COMPARISON_TYPE  = "comparison_type";
    private static final String FIELD_VALUE      = "field_value";
    private static final String FIELD            = "field";

    private static final Logger logger = LoggerFactory.getLogger(PermissionCriteria.class);

    private String  field      = null;
    private String  comparison = null;
    private Object  value      = null;
    private Boolean canRead    = null;
    private Boolean canEdit    = null;
    private Boolean canCreate  = null;

    private Boolean canDelete   = null;
    private Boolean canActivate = null;

    private boolean authorized = true;

    public PermissionCriteria(String elementName) {
        this.elementName = elementName;
    }

    public PermissionCriteria(String elementName, ItemI i) throws Exception {
        this.elementName = elementName;

        setField(i.getStringProperty(FIELD));
        setFieldValue(i.getProperty(FIELD_VALUE));
        setComparisonType(i.getStringProperty(COMPARISON_TYPE));

        setRead(i.getBooleanProperty(READ_ELEMENT, false));
        setDelete(i.getBooleanProperty(DELETE_ELEMENT, false));
        setEdit(i.getBooleanProperty(EDIT_ELEMENT, false));
        setCreate(i.getBooleanProperty(CREATE_ELEMENT, false));
        setActivate(i.getBooleanProperty(ACTIVATE_ELEMENT, false));

        authorized = i.isActive();
    }

    public static final String SCHEMA_ELEMENT_NAME = "xdat:field_mapping";

    public static String dumpCriteriaList(final List<PermissionCriteriaI> criteria) {
        final StringBuilder dump = new StringBuilder("{} permission criteria found:");
        if (!criteria.isEmpty()) {
            for (PermissionCriteriaI criterion : criteria) {
                dump.append("\n * ").append(criterion.toString());
            }
        }
        return dump.toString();
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getSchemaElementName()
     */
    public String getSchemaElementName() {
        return SCHEMA_ELEMENT_NAME;
    }

    public boolean isActive() {
        return authorized;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getField()
     */
    @Override
    public String getField() {
        return field;
    }

    public String getComparisonType() {
        return (comparison == null) ? EQUALS : comparison;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getFieldValue()
     */
    @Override
    public Object getFieldValue() {
        return value;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getCreate()
     */
    @Override
    public boolean getCreate() {
        return (canCreate == null) ? false : canCreate;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getRead()
     */
    @Override
    public boolean getRead() {
        return (canRead == null) ? false : canRead;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getEdit()
     */
    @Override
    public boolean getEdit() {
        return (canEdit == null) ? false : canEdit;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getDelete()
     */
    @Override
    public boolean getDelete() {
        return (canDelete == null) ? false : canDelete;
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#getActivate()
     */
    @Override
    public boolean getActivate() {
        return (canActivate == null) ? false : canActivate;
    }

    public boolean getAction(final String action) {
        switch (action) {
            case CREATE:
                return getCreate();
            case READ:
                return getRead();
            case DELETE:
                return getDelete();
            case EDIT:
                return getEdit();
            case ACTIVATE:
                return getActivate();
            default:
                throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "Unknown action " + action);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.security.PermissionCriteriaI#canAccess(java.lang.String, java.lang.String, org.nrg.xdat.security.SecurityValues)
     */
    @Override
    public boolean canAccess(final String access, final SecurityValues values) throws Exception {
        if (!getAction(access)) {
            logger.debug("Action {} does not appear to be valid", access);
            return false;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Checking access to action {} with security values {}", access, values.toString());
        }

        // dot syntax
        final Object value = values.getHash().get(getField());

        if (value == null) {
            logger.debug("Tried to check access to action {} with field {}, but that field doesn't exist in the security values", access, getField());
            return false;
        }

        final String fieldValue = value.toString();
        final Object compareTo = getFieldValue();

        if (compareTo == null) {
            logger.debug("Tried to test field {} value {}, but the compare to from getFieldValue() was null, access denied", getField(), fieldValue);
            return false;
        }

        final String compareToString = compareTo.toString();
        if (StringUtils.equals(ALL, compareToString)) {
            logger.debug("Test field {} value {}, the compare to from getFieldValue() was \"{}\", access granted", getField(), fieldValue, ALL);
            return true;
        }

        final String[] parsedValues = StringUtils.split(fieldValue, "\\s*,\\s*");
        if (logger.isDebugEnabled()) {
            logger.debug("Testing {} parsed values against compare-to string {}: {}", parsedValues.length, compareToString, Joiner.on(", ").join(parsedValues));
        }

        for (final String single : parsedValues) {
            logger.debug("Testing field value {} against compare-to value {}", single, compareToString);
            if (StringUtils.equalsIgnoreCase(single, compareToString)) {
                logger.info("Access granted based on field value {} and compare-to value {}", single, compareToString);
                return true;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Access denied with compare-to value matching none of the field values: {}", compareToString, Joiner.on(", ").join(parsedValues));
        }

        return false;
    }

    @SuppressWarnings("unused")
    private void setAction(final String action, final boolean allow) throws Exception {
        switch (action) {
            case CREATE:
                setCreate(allow);
            case READ:
                setRead(allow);
            case DELETE:
                setDelete(allow);
            case EDIT:
                setEdit(allow);
            case ACTIVATE:
                setActivate(allow);
            default:
                throw new NrgServiceRuntimeException(NrgServiceError.UnsupportedFeature, "Unknown action " + action);
        }
    }

    public void setActivate(boolean b) {
        canActivate = b;
    }

    public void setCreate(boolean b) {
        canCreate = b;
    }

    public void setRead(boolean b) {
        canRead = b;
    }

    public void setEdit(boolean b) {
        canEdit = b;
    }

    public void setDelete(boolean b) {
        canDelete = b;
    }

    public void setField(String s) {
        field = XftStringUtils.intern(s);
    }

    public void setFieldValue(Object o) {
        value = (o instanceof String) ? ((String) o).intern() : o;
    }

    public void setComparisonType(String o) {
        comparison = XftStringUtils.intern(o);
    }

    public String toString() {
        return Joiner.on(" ").join(getField(), getFieldValue(), getComparisonType(), getCreate(), getRead(), getEdit(), getDelete(), getActivate());
    }

    final String elementName;

    @Override
    public String getElementName() {
        return elementName;
    }
}
