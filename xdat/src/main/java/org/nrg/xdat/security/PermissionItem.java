/*
 * core: org.nrg.xdat.security.PermissionItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.security;

import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.utils.XftStringUtils;

import java.util.Comparator;


/**
 * PermissionItem represents potential permission objects.
 *
 * @author Tim
 */
public class PermissionItem {
    @SuppressWarnings("RedundantThrows")
    public void set(PermissionCriteriaI c) throws MetaDataException {
        if (c.getField().equalsIgnoreCase(XftStringUtils.StandardizeXMLPath(fullFieldName)) && c.getFieldValue().toString().equalsIgnoreCase(value.toString())) {
            setCreate(c.getCreate());
            setEdit(c.getEdit());
            setDelete(c.getDelete());
            setRead(c.getRead());
            setActivate(c.getActivate());
            setComparison_type("=");
            wasSet = true;
        }
    }

    /**
     * @return The permission display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param name    The display name to set.
     */
    public void setDisplayName(String name) {
        displayName = name;
    }

    /**
     * @return The full field name.
     */
    public String getFullFieldName() {
        return fullFieldName;
    }

    /**
     * @param name    The full field name to set.
     */
    public void setFullFieldName(String name) {
        fullFieldName = name;
    }

    /**
     * @return The short field name.
     */
    @SuppressWarnings("unused")
    public String getShortFieldName() {
        return shortFieldName;
    }

    /**
     * @param name    The short field name to set.
     */
    @SuppressWarnings("unused")
    public void setShortFieldName(String name) {
        shortFieldName = name;
    }

    /**
     * @return Whether the object can be created.
     */
    public boolean canCreate() {
        return create;
    }

    /**
     * @return Whether the object can be deleted.
     */
    public boolean canDelete() {
        return delete;
    }

    /**
     * @return Whether the object can be edited.
     */
    public boolean canEdit() {
        return edit;
    }

    /**
     * @return Whether the object can be read.
     */
    public boolean canRead() {
        return read;
    }

    /**
     * @return Whether the object can be activated.
     */
    @SuppressWarnings("unused")
    public boolean canActivate() {
        return activate;
    }

    /**
     * @return The value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param enable    Whether the object can be created.
     */
    public void setCreate(boolean enable) {
        create = enable;
    }

    /**
     * @param enable    Whether the object can be deleted.
     */
    public void setDelete(boolean enable) {
        delete = enable;
    }

    /**
     * @param enable    Whether the object can be edited.
     */
    public void setEdit(boolean enable) {
        edit = enable;
    }

    /**
     * @param enable    Whether the object can be read.
     */
    public void setRead(boolean enable) {
        read = enable;
    }

    /**
     * @param value    The value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return getFullFieldName() +
               " " + getValue() +
               " " + canCreate() +
               " " + canRead() +
               " " + canEdit() +
               " " + canDelete();
    }

    /**
     * @param activate The activate to set.
     */
    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    /**
     * @param comparison_type The comparison_type to set.
     */
    public void setComparison_type(String comparison_type) {
        this.comparison_type = comparison_type;
    }

    @SuppressWarnings("unused")
    public String getComparison_type() {
        return comparison_type;
    }

    /**
     * @return Returns the authenticated.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * @param authenticated The authenticated to set.
     */
    @SuppressWarnings("unused")
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    /**
     * @return Returns the wasSet.
     */
    public boolean wasSet() {
        return wasSet;
    }

    public static Comparator<PermissionItem> GetComparator() {
        return new PermissionItem().getComparator();
    }

    public Comparator<PermissionItem> getComparator() {
        return new PIComparator();
    }

    public class PIComparator implements Comparator<PermissionItem> {
        public PIComparator() {
        }

        @Override
        public int compare(final PermissionItem item1, final PermissionItem item2) {
            if (item1 == null) {
                if (item2 == null) {
                    return 0;
                } else {
                    return -1;
                }
            }
            if (item2 == null) {
                return 1;
            }

            //noinspection unchecked
            return ((Comparable) item1.getValue()).compareTo(item2.getValue());
        }
    }

    private String  fullFieldName   = null;
    private String  shortFieldName  = null;
    private Object  value           = null;
    private String  displayName     = null;
    private boolean read            = false;
    private boolean create          = false;
    private boolean delete          = false;
    private boolean edit            = false;
    private boolean activate        = false;
    private String  comparison_type = "equals";
    private boolean authenticated   = false;
    private boolean wasSet          = false;
}

