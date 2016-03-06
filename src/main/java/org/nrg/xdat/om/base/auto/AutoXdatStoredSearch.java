/*
 * org.nrg.xdat.om.base.auto.AutoXdatStoredSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.om.base.auto;

import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XdatStoredSearch;
import org.nrg.xdat.om.XdatStoredSearchAllowedUser;
import org.nrg.xdat.om.XdatStoredSearchGroupid;
import org.nrg.xdat.om.XdatStoredSearchI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AutoXdatStoredSearch extends org.nrg.xdat.base.BaseElement implements XdatStoredSearchI {
    public final static org.apache.log4j.Logger logger              = org.apache.log4j.Logger.getLogger(AutoXdatStoredSearch.class);
    public final static String                  SCHEMA_ELEMENT_NAME = "xdat:stored_search";

    public AutoXdatStoredSearch(ItemI item) {
        super(item);
    }

    public AutoXdatStoredSearch(UserI user) {
        super(user);
    }

    /*
     * @deprecated Use AutoXdatStoredSearch(UserI user)
     **/
    public AutoXdatStoredSearch() {
    }

    public AutoXdatStoredSearch(Hashtable properties, UserI user) {
        super(properties, user);
    }

    /**
     * {@inheritDoc}
     */
    public String getSchemaElementName() {
        return "xdat:stored_search";
    }

    //FIELD

    private String _RootElementName = null;

    /**
     * {@inheritDoc}
     */
    public String getRootElementName() {
        try {
            if (_RootElementName == null) {
                _RootElementName = getStringProperty("root_element_name");
                return _RootElementName;
            } else {
                return _RootElementName;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setRootElementName(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/root_element_name", v);
            _RootElementName = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    private ArrayList<org.nrg.xdat.om.XdatSearchField> _SearchField = null;

    /**
     * {@inheritDoc}
     */
    public ArrayList<org.nrg.xdat.om.XdatSearchField> getSearchField() {
        try {
            if (_SearchField == null) {
                _SearchField = org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("search_field"));
                return _SearchField;
            } else {
                return _SearchField;
            }
        } catch (Exception e1) {
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSearchField(ItemI v) throws Exception {
        _SearchField = null;
        try {
            if (v instanceof XFTItem) {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/search_field", v, true);
            } else {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/search_field", v.getItem(), true);
            }
        } catch (Exception e1) {
            logger.error(e1);
            throw e1;
        }
    }

    /**
     * Removes the search_field of the given index.
     *
     * @param index Index of child to remove.
     */
    @SuppressWarnings("unused")
    public void removeSearchField(int index) {
        _SearchField = null;
        try {
            getItem().removeChild(SCHEMA_ELEMENT_NAME + "/search_field", index);
        } catch (FieldNotFoundException e1) {
            logger.error(e1);
        }
    }

    private ArrayList<org.nrg.xdat.om.XdatCriteriaSet> _SearchWhere = null;

    /**
     * {@inheritDoc}
     */
    public ArrayList<org.nrg.xdat.om.XdatCriteriaSet> getSearchWhere() {
        try {
            if (_SearchWhere == null) {
                _SearchWhere = org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("search_where"));
                return _SearchWhere;
            } else {
                return _SearchWhere;
            }
        } catch (Exception e1) {
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSearchWhere(ItemI v) throws Exception {
        _SearchWhere = null;
        try {
            if (v instanceof XFTItem) {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/search_where", v, true);
            } else {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/search_where", v.getItem(), true);
            }
        } catch (Exception e1) {
            logger.error(e1);
            throw e1;
        }
    }

    /**
     * Removes the search_where of the given index.
     *
     * @param index Index of child to remove.
     */
    @SuppressWarnings("unused")
    public void removeSearchWhere(int index) {
        _SearchWhere = null;
        try {
            getItem().removeChild(SCHEMA_ELEMENT_NAME + "/search_where", index);
        } catch (FieldNotFoundException e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private String _SortBy_elementName = null;

    /**
     * {@inheritDoc}
     */
    public String getSortBy_elementName() {
        try {
            if (_SortBy_elementName == null) {
                _SortBy_elementName = getStringProperty("sort_by/element_name");
                return _SortBy_elementName;
            } else {
                return _SortBy_elementName;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSortBy_elementName(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/sort_by/element_name", v);
            _SortBy_elementName = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private String _SortBy_fieldId = null;

    /**
     * {@inheritDoc}
     */
    public String getSortBy_fieldId() {
        try {
            if (_SortBy_fieldId == null) {
                _SortBy_fieldId = getStringProperty("sort_by/field_ID");
                return _SortBy_fieldId;
            } else {
                return _SortBy_fieldId;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSortBy_fieldId(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/sort_by/field_ID", v);
            _SortBy_fieldId = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    private ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> _AllowedUser = null;

    /**
     * {@inheritDoc}
     */
    public ArrayList<org.nrg.xdat.om.XdatStoredSearchAllowedUser> getAllowedUser() {
        try {
            if (_AllowedUser == null) {
                _AllowedUser = org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("allowed_user"));
                return _AllowedUser;
            } else {
                return _AllowedUser;
            }
        } catch (Exception e1) {
            return new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAllowedUser(ItemI v) throws Exception {
        _AllowedUser = null;
        try {
            if (v instanceof XFTItem) {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_user", v, true);
            } else {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_user", v.getItem(), true);
            }
        } catch (Exception e1) {
            logger.error(e1);
            throw e1;
        }
    }

    /**
     * Removes the allowed_user of the given index.
     *
     * @param index Index of child to remove.
     */
    public void removeAllowedUser(int index) {
        _AllowedUser = null;
        try {
            getItem().removeChild(SCHEMA_ELEMENT_NAME + "/allowed_user", index);
        } catch (FieldNotFoundException e1) {
            logger.error(e1);
        }
    }

    private ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> _AllowedGroups_groupid = null;

    /**
     * allowed_groups/groupID
     *
     * @return Returns an ArrayList of org.nrg.xdat.om.XdatStoredSearchGroupid
     */
    public ArrayList<org.nrg.xdat.om.XdatStoredSearchGroupid> getAllowedGroups_groupid() {
        try {
            if (_AllowedGroups_groupid == null) {
                _AllowedGroups_groupid = org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("allowed_groups/groupID"));
                return _AllowedGroups_groupid;
            } else {
                return _AllowedGroups_groupid;
            }
        } catch (Exception e1) {
            return new ArrayList<>();
        }
    }

    /**
     * Sets the value for allowed_groups/groupID.
     *
     * @param v Value to Set.
     * @throws Exception When an error occurs.
     */
    @SuppressWarnings("unused")
    public void setAllowedGroups_groupid(ItemI v) throws Exception {
        _AllowedGroups_groupid = null;
        try {
            if (v instanceof XFTItem) {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_groups/groupID", v, true);
            } else {
                getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_groups/groupID", v.getItem(), true);
            }
        } catch (Exception e1) {
            logger.error(e1);
            throw e1;
        }
    }

    /**
     * Removes the allowed_groups/groupID of the given index.
     *
     * @param index Index of child to remove.
     */
    public void removeAllowedGroups_groupid(int index) {
        _AllowedGroups_groupid = null;
        try {
            getItem().removeChild(SCHEMA_ELEMENT_NAME + "/allowed_groups/groupID", index);
        } catch (FieldNotFoundException e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private String _Id = null;

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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

    private String _Description = null;

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        try {
            if (_Description == null) {
                _Description = getStringProperty("description");
                return _Description;
            } else {
                return _Description;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDescription(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/description", v);
            _Description = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private String _Layeredsequence = null;

    /**
     * {@inheritDoc}
     */
    public String getLayeredsequence() {
        try {
            if (_Layeredsequence == null) {
                _Layeredsequence = getStringProperty("layeredSequence");
                return _Layeredsequence;
            } else {
                return _Layeredsequence;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setLayeredsequence(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/layeredSequence", v);
            _Layeredsequence = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private Boolean _AllowDiffColumns = null;

    /**
     * {@inheritDoc}
     */
    public Boolean getAllowDiffColumns() {
        try {
            if (_AllowDiffColumns == null) {
                _AllowDiffColumns = getBooleanProperty("allow-diff-columns");
                return _AllowDiffColumns;
            } else {
                return _AllowDiffColumns;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAllowDiffColumns(Object v) {
        try {
            setBooleanProperty(SCHEMA_ELEMENT_NAME + "/allow-diff-columns", v);
            _AllowDiffColumns = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private Boolean _Secure = null;

    /**
     * {@inheritDoc}
     */
    public Boolean getSecure() {
        try {
            if (_Secure == null) {
                _Secure = getBooleanProperty("secure");
                return _Secure;
            } else {
                return _Secure;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setSecure(Object v) {
        try {
            setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure", v);
            _Secure = null;
        } catch (Exception e1) {
            logger.error(e1);
        }
    }

    //FIELD

    private String _BriefDescription = null;

    /**
     * {@inheritDoc}
     */
    public String getBriefDescription() {
        try {
            if (_BriefDescription == null) {
                _BriefDescription = getStringProperty("brief-description");
                return _BriefDescription;
            } else {
                return _BriefDescription;
            }
        } catch (Exception e1) {
            logger.error(e1);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBriefDescription(String v) {
        try {
            setProperty(SCHEMA_ELEMENT_NAME + "/brief-description", v);
            _BriefDescription = null;
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

    @SuppressWarnings("unused")
    public static ArrayList<XdatStoredSearch> getAllXdatStoredSearchs(org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatStoredSearch> al = new ArrayList<>();

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
    public static ArrayList<XdatStoredSearch> getXdatStoredSearchsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatStoredSearch> al = new ArrayList<>();
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath, value, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            logger.error("", e);
        }

        al.trimToSize();
        return al;
    }

    @SuppressWarnings("unused")
    public static ArrayList<XdatStoredSearch> getXdatStoredSearchsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user, boolean preLoad) {
        ArrayList<XdatStoredSearch> al = new ArrayList<>();
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria, user, preLoad);
            al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
        } catch (Exception e) {
            logger.error("", e);
        }

        al.trimToSize();
        return al;
    }

    public static XdatStoredSearch getXdatStoredSearchsById(Object value, org.nrg.xft.security.UserI user, boolean preLoad) {
        try {
            org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:stored_search/ID", value, user, preLoad);
            ItemI                                  match = items.getFirst();
            if (match != null) {
                return (XdatStoredSearch) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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

    public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop) {
        ArrayList<ResourceFile> _return   = new ArrayList<>();
        boolean                 localLoop = preventLoop;

        //search_field
        for (XdatSearchField childSearchField : this.getSearchField()) {
            for (ResourceFile rf : childSearchField.getFileResources(rootPath, localLoop)) {
                rf.setXpath("search_field[" + childSearchField.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("search_field/" + childSearchField.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        localLoop = preventLoop;

        //search_where
        for (XdatCriteriaSet childSearchWhere : this.getSearchWhere()) {
            for (ResourceFile rf : childSearchWhere.getFileResources(rootPath, localLoop)) {
                rf.setXpath("search_where[" + childSearchWhere.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("search_where/" + childSearchWhere.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        localLoop = preventLoop;

        //allowed_user
        for (XdatStoredSearchAllowedUser childAllowedUser : this.getAllowedUser()) {
            for (ResourceFile rf : childAllowedUser.getFileResources(rootPath, localLoop)) {
                rf.setXpath("allowed_user[" + childAllowedUser.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("allowed_user/" + childAllowedUser.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        localLoop = preventLoop;

        //allowed_groups/groupID
        for (XdatStoredSearchGroupid childAllowedGroups_groupid : this.getAllowedGroups_groupid()) {
            for (ResourceFile rf : childAllowedGroups_groupid.getFileResources(rootPath, localLoop)) {
                rf.setXpath("allowed_groups/groupID[" + childAllowedGroups_groupid.getItem().getPKString() + "]/" + rf.getXpath());
                rf.setXdatPath("allowed_groups/groupID/" + childAllowedGroups_groupid.getItem().getPKString() + "/" + rf.getXpath());
                _return.add(rf);
            }
        }

        return _return;
    }
}
