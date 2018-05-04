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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xft.ItemI;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.utils.XftStringUtils;

import java.util.Collection;
import java.util.Map;

import static org.nrg.xdat.security.SecurityManager.*;

/**
 * @author Tim
 */
@SuppressWarnings("serial") //$NON-NLS-1$
@Slf4j
public class PermissionCriteria implements PermissionCriteriaI {
    public static final String ALL              = "*";
    public static final String EQUALS           = "equals";
    public static final String ACTIVATE_ELEMENT = "active_element";
    public static final String CREATE_ELEMENT   = "create_element";
    public static final String EDIT_ELEMENT     = "edit_element";
    public static final String DELETE_ELEMENT   = "delete_element";
    public static final String READ_ELEMENT     = "read_element";
    public static final String COMPARISON_TYPE  = "comparison_type";
    public static final String FIELD_VALUE      = "field_value";
    public static final String FIELD            = "field";

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

    public PermissionCriteria(final String elementName, final Map<String, Object> properties) {
        this.elementName = elementName;
        setField((String) properties.get(FIELD));
        setFieldValue(properties.get(FIELD_VALUE));
        setComparisonType((String) properties.get(COMPARISON_TYPE));

        setRead(BooleanUtils.toBoolean((int) properties.get(READ_ELEMENT)));
        setDelete(BooleanUtils.toBoolean((int) properties.get(DELETE_ELEMENT)));
        setEdit(BooleanUtils.toBoolean((int) properties.get(EDIT_ELEMENT)));
        setCreate(BooleanUtils.toBoolean((int) properties.get(CREATE_ELEMENT)));
        setActivate(BooleanUtils.toBoolean((int) properties.get(ACTIVATE_ELEMENT)));

        authorized = StringUtils.equalsAnyIgnoreCase((String) properties.get("active_status"), ViewManager.ACTIVE, ViewManager.LOCKED);
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

    public static String dumpCriteriaList(final Collection<PermissionCriteriaI> criteria) {
        final StringBuilder dump = new StringBuilder(criteria.isEmpty() ? "No" : Integer.toString(criteria.size()));
        dump.append(" permission criteria found");
        if (!criteria.isEmpty()) {
            dump.append(":");
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
            log.info("Action {} does not appear to be valid", access);
            return false;
        }
        if (log.isInfoEnabled()) {
            log.info("Checking access to action {} with security values {}", access, values.toString());
        }

        // dot syntax
        final Object value = values.getHash().get(getField());

        if (value == null) {
            log.info("Tried to check access to action {} with field {}, but that field doesn't exist in the security values", access, getField());
            return false;
        }

        final String fieldValue = value.toString();
        final Object compareTo = getFieldValue();

        if (compareTo == null) {
            log.info("Tried to test field {} value {}, but the compare to from getFieldValue() was null, access denied", getField(), fieldValue);
            return false;
        }

        final String compareToString = compareTo.toString();
        if (StringUtils.equals(ALL, compareToString)) {
            log.info("Test field {} value {}, the compare to from getFieldValue() was \"{}\", access granted", getField(), fieldValue, ALL);
            return true;
        }

        final String[] parsedValues = fieldValue.split("\\s*,\\s*");
        if (log.isInfoEnabled()) {
            log.info("Testing {} parsed values against compare-to string {}: {}", parsedValues.length, compareToString, Joiner.on(", ").join(parsedValues));
        }

        for (final String single : parsedValues) {
            log.info("Testing field value {} against compare-to value {}", single, compareToString);
            if (StringUtils.equalsIgnoreCase(single, compareToString)) {
                log.info("Access granted based on field value {} and compare-to value {}", single, compareToString);
                return true;
            }
        }

        if (log.isInfoEnabled()) {
            log.info("Access denied with compare-to value matching none of the field values: {}", compareToString, Joiner.on(", ").join(parsedValues));
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
