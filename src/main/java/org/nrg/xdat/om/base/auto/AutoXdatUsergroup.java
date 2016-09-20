/*
 * core: org.nrg.xdat.om.base.auto.AutoXdatUsergroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base.auto;

import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatElementAccess;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XdatUsergroupI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AutoXdatUsergroup extends BaseElement implements XdatUsergroupI {
    public final static org.apache.log4j.Logger logger              = org.apache.log4j.Logger.getLogger(AutoXdatUsergroup.class);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
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
            logger.error(e1);
        }
    }

    @SuppressWarnings("unused")
    public static ArrayList<XdatUsergroup> getAllXdatUsergroups(org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();

        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            logger.error("", e);
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
            logger.error("", e);
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
            logger.error("", e);
        }

        al.trimToSize();
        return al;
    }

    public static ArrayList<XdatUsergroup> getXdatUsergroupsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatUsergroup> al = new ArrayList<>();
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            logger.error("", e);
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
            logger.error("", e);
        }

        return null;
    }

    public static XdatUsergroup getXdatUsergroupsById(Object value, org.nrg.xft.security.UserI user, boolean preLoad) {
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:userGroup/ID", value, user, preLoad);
            ItemI                                  match = items.getFirst();
            if (match != null) {
                return (XdatUsergroup) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        return null;
    }

    public static ArrayList wrapItems(ArrayList items) {
        return BaseElement.WrapItems(items);
    }

    public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items) {
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
