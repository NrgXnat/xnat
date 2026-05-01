/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatUsergroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base.auto;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XdatUsergroupI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * @author XDAT
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Slf4j
public abstract class AutoXdatUsergroup extends BaseElement implements XdatUsergroupI {
    public final static String                  SCHEMA_ELEMENT_NAME = "xdat:userGroup";

    public AutoXdatUsergroup(ItemI item) {
        super(item);
    }

    public AutoXdatUsergroup(UserI user) {
        super(user);
    }

    /*
     * @deprecated Use AutoXdatUsergroup(UserI user)
     **/
    public AutoXdatUsergroup() {
    }

    public AutoXdatUsergroup(Hashtable properties, UserI user) {
        super(properties, user);
    }

    public String getSchemaElementName() {
        return "xdat:userGroup";
    }

    private ArrayList<org.nrg.xdat.om.XdatElementAccess> _ElementAccess = null;

    /**
     * element_access
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementAccess
     */
    public ArrayList<org.nrg.xdat.om.XdatElementAccess> getElementAccess() {
        try {
            if (_ElementAccess == null) {
                _ElementAccess = org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("element_access"));
                return _ElementAccess;
            } else {
                return _ElementAccess;
            }
        } catch (Exception e1) {
            return new ArrayList<>();
        }
    }

    /**
     * Sets the value for element_access.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    public void setElementAccess(ItemI v) throws Exception {
        _ElementAccess = null;
        try {
            if (v instanceof XFTItem) {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_access", v, true);
            } else {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_access", v.getItem(), true);
            }
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
            throw e1;
        }
    }

    /**
     * Removes the element_access of the given index.
     *
     * @param index Index of child to remove.
     */
    @SuppressWarnings("unused")
    public void removeElementAccess(int index) {
        _ElementAccess = null;
        try {
            getItem().removeChild(SCHEMA_ELEMENT_NAME + "/element_access", index);
        } catch (FieldNotFoundException e1) {
            log.error("An exception occurred", e1);
        }
    }

    //FIELD

    private String _Id = null;

    /**
     * @return Returns the ID.
     */
    public String getId() {
        try {
            if (_Id == null) {
                _Id = getStringProperty("ID");
                return _Id;
            } else {
                return _Id;
            }
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
            return null;
        }
    }

    /**
     * Sets the value for ID.
     *
     * @param v Value to Set.
     */
    public void setId(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/ID", v);
            _Id = null;
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
        }
    }

    //FIELD

    private String _Displayname = null;

    /**
     * @return Returns the displayName.
     */
    public String getDisplayname() {
        try {
            if (_Displayname == null) {
                _Displayname = getStringProperty("displayName");
                return _Displayname;
            } else {
                return _Displayname;
            }
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
            return null;
        }
    }

    /**
     * Sets the value for displayName.
     *
     * @param v Value to Set.
     */
    public void setDisplayname(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/displayName", v);
            _Displayname = null;
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
        }
    }

    //FIELD

    private String _Tag = null;

    /**
     * @return Returns the tag.
     */
    public String getTag() {
        try {
            if (_Tag == null) {
                _Tag = getStringProperty("tag");
                return _Tag;
            } else {
                return _Tag;
            }
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
            return null;
        }
    }

    /**
     * Sets the value for tag.
     *
     * @param v Value to Set.
     */
    public void setTag(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/tag", v);
            _Tag = null;
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
        }
    }

    //FIELD

    private Integer _XdatUsergroupId = null;

    /**
     * {@inheritDoc}
     */
    public Integer getXdatUsergroupId() {
        try {
            if (_XdatUsergroupId == null) {
                _XdatUsergroupId = getIntegerProperty("xdat_userGroup_id");
                return _XdatUsergroupId;
            } else {
                return _XdatUsergroupId;
            }
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setXdatUsergroupId(Integer v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/xdat_userGroup_id", v);
            _XdatUsergroupId = null;
        } catch (Exception e1) {
            log.error("An exception occurred", e1);
        }
    }

    @SuppressWarnings("unused")
    public static ArrayList<XdatUsergroup> getAllXdatUsergroups(org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();

        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            log.error("", e);
        }

        al.trimToSize();
        return al;
    }

    @SuppressWarnings("unused")
    public static ArrayList<XdatUsergroup> getAllXdatUsergroups(org.nrg.xft.security.UserI user, boolean preLoad, String sortBy) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();

        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems(sortBy));
        } catch (Exception e) {
            log.error("", e);
        }

        al.trimToSize();
        return al;
    }

    public static ArrayList<XdatUsergroup> getXdatUsergroupsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath, value, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            log.error("", e);
        }

        al.trimToSize();
        return al;
    }

    @SuppressWarnings("unused")
    public static ArrayList<XdatUsergroup> getXdatUsergroupsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            log.error("", e);
        }

        al.trimToSize();
        return al;
    }

    public static XdatUsergroup getXdatUsergroupsByXdatUsergroupId(Object value, org.nrg.xft.security.UserI user, boolean preLoad) {
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:userGroup/xdat_userGroup_id", value, user, preLoad);
            ItemI                                  match = items.getFirst();
            if (match != null) {
                return (XdatUsergroup) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("", e);
        }

        return null;
    }

    @Nullable
    public static XdatUsergroup getXdatUsergroupsById(final Object value, final UserI user, boolean preLoad) {
        try {
            final ItemI match = ItemSearch.GetItem("xdat:userGroup/ID", value, user, preLoad);
            if (match != null) {
                return (XdatUsergroup) BaseElement.GetGeneratedItem(match);
            }
        } catch (Exception e) {
            log.error("An unexpected error occurred trying to retrieve a user group by ID '" + value + "'", e);
        }

        return null;
    }

    @Nonnull
    public static List<XdatUsergroup> getXdatUsergroupsByTag(final Object value, final UserI user, final boolean preLoad) {
        try {
            final ItemCollection items = ItemSearch.GetItems("xdat:userGroup/tag", value, user, preLoad);
            return BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            log.error("", e);
        }

        return Collections.emptyList();
    }

    public static ArrayList wrapItems(final ArrayList items) {
        return BaseElement.WrapItems(items);
    }

    public static ArrayList wrapItems(final ItemCollection items) {
        return wrapItems(items.getItems());
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop) {
        ArrayList<ResourceFile> _return = new ArrayList<>();

        //element_access
        for (XdatElementAccess childElementAccess : this.getElementAccess()) {
            for (ResourceFile rf : childElementAccess.getFileResources(rootPath, preventLoop)) {
                rf.setXpath("element_access[" + childElementAccess.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("element_access/" + childElementAccess.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        return _return;
    }
}
