// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatElementActionType;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatElementSecurityI;
import org.nrg.xdat.om.XdatElementSecurityListingAction;
import org.nrg.xdat.om.XdatPrimarySecurityField;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatElementSecurity extends org.nrg.xdat.base.BaseElement implements XdatElementSecurityI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatElementSecurity.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:element_security";

	public AutoXdatElementSecurity(ItemI item)
	{
		super(item);
	}

	public AutoXdatElementSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatElementSecurity(UserI user)
	 **/
	public AutoXdatElementSecurity(){}

	public AutoXdatElementSecurity(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:element_security";
	}
	 private ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> _PrimarySecurityFields_primarySecurityField =null;

	/**
	 * primary_security_fields/primary_security_field
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatPrimarySecurityField
	 */
	public ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> getPrimarySecurityFields_primarySecurityField() {
		try{
			if (_PrimarySecurityFields_primarySecurityField==null){
				_PrimarySecurityFields_primarySecurityField=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("primary_security_fields/primary_security_field"));
				return _PrimarySecurityFields_primarySecurityField;
			}else {
				return _PrimarySecurityFields_primarySecurityField;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField>();}
	}

	/**
	 * Sets the value for primary_security_fields/primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityFields_primarySecurityField(ItemI v) throws Exception{
		_PrimarySecurityFields_primarySecurityField =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/primary_security_fields/primary_security_field",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/primary_security_fields/primary_security_field",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the primary_security_fields/primary_security_field of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePrimarySecurityFields_primarySecurityField(int index) throws java.lang.IndexOutOfBoundsException {
		_PrimarySecurityFields_primarySecurityField =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/primary_security_fields/primary_security_field",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatElementActionType> _ElementActions_elementAction =null;

	/**
	 * element_actions/element_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementActionType
	 */
	public ArrayList<org.nrg.xdat.om.XdatElementActionType> getElementActions_elementAction() {
		try{
			if (_ElementActions_elementAction==null){
				_ElementActions_elementAction=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("element_actions/element_action"));
				return _ElementActions_elementAction;
			}else {
				return _ElementActions_elementAction;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatElementActionType>();}
	}

	/**
	 * Sets the value for element_actions/element_action.
	 * @param v Value to Set.
	 */
	public void setElementActions_elementAction(ItemI v) throws Exception{
		_ElementActions_elementAction =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_actions/element_action",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_actions/element_action",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the element_actions/element_action of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeElementActions_elementAction(int index) throws java.lang.IndexOutOfBoundsException {
		_ElementActions_elementAction =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/element_actions/element_action",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatElementSecurityListingAction> _ListingActions_listingAction =null;

	/**
	 * listing_actions/listing_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurityListingAction
	 */
	public ArrayList<org.nrg.xdat.om.XdatElementSecurityListingAction> getListingActions_listingAction() {
		try{
			if (_ListingActions_listingAction==null){
				_ListingActions_listingAction=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("listing_actions/listing_action"));
				return _ListingActions_listingAction;
			}else {
				return _ListingActions_listingAction;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatElementSecurityListingAction>();}
	}

	/**
	 * Sets the value for listing_actions/listing_action.
	 * @param v Value to Set.
	 */
	public void setListingActions_listingAction(ItemI v) throws Exception{
		_ListingActions_listingAction =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/listing_actions/listing_action",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/listing_actions/listing_action",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the listing_actions/listing_action of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeListingActions_listingAction(int index) throws java.lang.IndexOutOfBoundsException {
		_ListingActions_listingAction =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/listing_actions/listing_action",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _ElementName=null;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName(){
		try{
			if (_ElementName==null){
				_ElementName=getStringProperty("element_name");
				return _ElementName;
			}else {
				return _ElementName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/element_name",v);
		_ElementName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecondaryPassword=null;

	/**
	 * @return Returns the secondary_password.
	 */
	public Boolean getSecondaryPassword() {
		try{
			if (_SecondaryPassword==null){
				_SecondaryPassword=getBooleanProperty("secondary_password");
				return _SecondaryPassword;
			}else {
				return _SecondaryPassword;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secondary_password.
	 * @param v Value to Set.
	 */
	public void setSecondaryPassword(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secondary_password",v);
		_SecondaryPassword=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecureIp=null;

	/**
	 * @return Returns the secure_ip.
	 */
	public Boolean getSecureIp() {
		try{
			if (_SecureIp==null){
				_SecureIp=getBooleanProperty("secure_ip");
				return _SecureIp;
			}else {
				return _SecureIp;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure_ip",v);
		_SecureIp=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Secure=null;

	/**
	 * @return Returns the secure.
	 */
	public Boolean getSecure() {
		try{
			if (_Secure==null){
				_Secure=getBooleanProperty("secure");
				return _Secure;
			}else {
				return _Secure;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure.
	 * @param v Value to Set.
	 */
	public void setSecure(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure",v);
		_Secure=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Browse=null;

	/**
	 * @return Returns the browse.
	 */
	public Boolean getBrowse() {
		try{
			if (_Browse==null){
				_Browse=getBooleanProperty("browse");
				return _Browse;
			}else {
				return _Browse;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for browse.
	 * @param v Value to Set.
	 */
	public void setBrowse(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/browse",v);
		_Browse=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence() {
		try{
			if (_Sequence==null){
				_Sequence=getIntegerProperty("sequence");
				return _Sequence;
			}else {
				return _Sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sequence",v);
		_Sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Quarantine=null;

	/**
	 * @return Returns the quarantine.
	 */
	public Boolean getQuarantine() {
		try{
			if (_Quarantine==null){
				_Quarantine=getBooleanProperty("quarantine");
				return _Quarantine;
			}else {
				return _Quarantine;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for quarantine.
	 * @param v Value to Set.
	 */
	public void setQuarantine(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/quarantine",v);
		_Quarantine=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _PreLoad=null;

	/**
	 * @return Returns the pre_load.
	 */
	public Boolean getPreLoad() {
		try{
			if (_PreLoad==null){
				_PreLoad=getBooleanProperty("pre_load");
				return _PreLoad;
			}else {
				return _PreLoad;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pre_load.
	 * @param v Value to Set.
	 */
	public void setPreLoad(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/pre_load",v);
		_PreLoad=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Searchable=null;

	/**
	 * @return Returns the searchable.
	 */
	public Boolean getSearchable() {
		try{
			if (_Searchable==null){
				_Searchable=getBooleanProperty("searchable");
				return _Searchable;
			}else {
				return _Searchable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for searchable.
	 * @param v Value to Set.
	 */
	public void setSearchable(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/searchable",v);
		_Searchable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecureRead=null;

	/**
	 * @return Returns the secure_read.
	 */
	public Boolean getSecureRead() {
		try{
			if (_SecureRead==null){
				_SecureRead=getBooleanProperty("secure_read");
				return _SecureRead;
			}else {
				return _SecureRead;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_read.
	 * @param v Value to Set.
	 */
	public void setSecureRead(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure_read",v);
		_SecureRead=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecureEdit=null;

	/**
	 * @return Returns the secure_edit.
	 */
	public Boolean getSecureEdit() {
		try{
			if (_SecureEdit==null){
				_SecureEdit=getBooleanProperty("secure_edit");
				return _SecureEdit;
			}else {
				return _SecureEdit;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_edit.
	 * @param v Value to Set.
	 */
	public void setSecureEdit(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure_edit",v);
		_SecureEdit=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecureCreate=null;

	/**
	 * @return Returns the secure_create.
	 */
	public Boolean getSecureCreate() {
		try{
			if (_SecureCreate==null){
				_SecureCreate=getBooleanProperty("secure_create");
				return _SecureCreate;
			}else {
				return _SecureCreate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_create.
	 * @param v Value to Set.
	 */
	public void setSecureCreate(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure_create",v);
		_SecureCreate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _SecureDelete=null;

	/**
	 * @return Returns the secure_delete.
	 */
	public Boolean getSecureDelete() {
		try{
			if (_SecureDelete==null){
				_SecureDelete=getBooleanProperty("secure_delete");
				return _SecureDelete;
			}else {
				return _SecureDelete;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_delete.
	 * @param v Value to Set.
	 */
	public void setSecureDelete(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/secure_delete",v);
		_SecureDelete=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Accessible=null;

	/**
	 * @return Returns the accessible.
	 */
	public Boolean getAccessible() {
		try{
			if (_Accessible==null){
				_Accessible=getBooleanProperty("accessible");
				return _Accessible;
			}else {
				return _Accessible;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for accessible.
	 * @param v Value to Set.
	 */
	public void setAccessible(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/accessible",v);
		_Accessible=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Usage=null;

	/**
	 * @return Returns the usage.
	 */
	public String getUsage(){
		try{
			if (_Usage==null){
				_Usage=getStringProperty("usage");
				return _Usage;
			}else {
				return _Usage;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for usage.
	 * @param v Value to Set.
	 */
	public void setUsage(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/usage",v);
		_Usage=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Singular=null;

	/**
	 * @return Returns the singular.
	 */
	public String getSingular(){
		try{
			if (_Singular==null){
				_Singular=getStringProperty("singular");
				return _Singular;
			}else {
				return _Singular;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for singular.
	 * @param v Value to Set.
	 */
	public void setSingular(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/singular",v);
		_Singular=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Plural=null;

	/**
	 * @return Returns the plural.
	 */
	public String getPlural(){
		try{
			if (_Plural==null){
				_Plural=getStringProperty("plural");
				return _Plural;
			}else {
				return _Plural;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for plural.
	 * @param v Value to Set.
	 */
	public void setPlural(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/plural",v);
		_Plural=null;
		} catch (Exception e1) {logger.error(e1);}
	}

    //FIELD

    private String _Code=null;

    /**
     * @return Returns the code.
     */
    public String getCode(){
        try{
            if (_Code==null){
                _Code=getStringProperty("code");
                return _Code;
            }else {
                return _Code;
            }
        } catch (Exception e1) {logger.error(e1);return null;}
    }

    /**
     * Sets the value for code.
     * @param v Value to Set.
     */
    public void setCode(String v){
        try{
        setProperty(SCHEMA_ELEMENT_NAME + "/code",v);
        _Code=null;
        } catch (Exception e1) {logger.error(e1);}
    }

	//FIELD

	private String _Category=null;

	/**
	 * @return Returns the category.
	 */
	public String getCategory(){
		try{
			if (_Category==null){
				_Category=getStringProperty("category");
				return _Category;
			}else {
				return _Category;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for category.
	 * @param v Value to Set.
	 */
	public void setCategory(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/category",v);
		_Category=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementSecurity> getAllXdatElementSecuritys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementSecurity> al = new ArrayList<org.nrg.xdat.om.XdatElementSecurity>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementSecurity> getXdatElementSecuritysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementSecurity> al = new ArrayList<org.nrg.xdat.om.XdatElementSecurity>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementSecurity> getXdatElementSecuritysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementSecurity> al = new ArrayList<org.nrg.xdat.om.XdatElementSecurity>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatElementSecurity getXdatElementSecuritysByElementName(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:element_security/element_name",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatElementSecurity) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArrayList wrapItems(ArrayList items)
	{
		ArrayList al = new ArrayList();
		al = org.nrg.xdat.base.BaseElement.WrapItems(items);
		al.trimToSize();
		return al;
	}

	public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items)
	{
		return wrapItems(items.getItems());
	}
public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
	ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //primary_security_fields/primary_security_field
	        for(XdatPrimarySecurityField childPrimarySecurityFields_primarySecurityField : this.getPrimarySecurityFields_primarySecurityField()){
	            for(ResourceFile rf: childPrimarySecurityFields_primarySecurityField.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("primary_security_fields/primary_security_field[" + childPrimarySecurityFields_primarySecurityField.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("primary_security_fields/primary_security_field/" + childPrimarySecurityFields_primarySecurityField.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //element_actions/element_action
	        for(XdatElementActionType childElementActions_elementAction : this.getElementActions_elementAction()){
	            for(ResourceFile rf: childElementActions_elementAction.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("element_actions/element_action[" + childElementActions_elementAction.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("element_actions/element_action/" + childElementActions_elementAction.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //listing_actions/listing_action
	        for(XdatElementSecurityListingAction childListingActions_listingAction : this.getListingActions_listingAction()){
	            for(ResourceFile rf: childListingActions_listingAction.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("listing_actions/listing_action[" + childListingActions_listingAction.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("listing_actions/listing_action/" + childListingActions_listingAction.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
