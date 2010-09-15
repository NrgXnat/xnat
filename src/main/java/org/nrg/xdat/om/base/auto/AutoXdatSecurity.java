// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatActionType;
import org.nrg.xdat.om.XdatElementSecurity;
import org.nrg.xdat.om.XdatInfoentry;
import org.nrg.xdat.om.XdatInfoentryI;
import org.nrg.xdat.om.XdatNewsentry;
import org.nrg.xdat.om.XdatNewsentryI;
import org.nrg.xdat.om.XdatRoleType;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.om.XdatSecurityI;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatSecurity extends org.nrg.xdat.base.BaseElement implements XdatSecurityI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatSecurity.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:security";

	public AutoXdatSecurity(ItemI item)
	{
		super(item);
	}

	public AutoXdatSecurity(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatSecurity(UserI user)
	 **/
	public AutoXdatSecurity(){}

	public AutoXdatSecurity(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:security";
	}
	 private ArrayList<org.nrg.xdat.om.XdatUsergroup> _Groups_group =null;

	/**
	 * groups/group
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatUsergroup
	 */
	public ArrayList<org.nrg.xdat.om.XdatUsergroup> getGroups_group() {
		try{
			if (_Groups_group==null){
				_Groups_group=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("groups/group"));
				return _Groups_group;
			}else {
				return _Groups_group;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatUsergroup>();}
	}

	/**
	 * Sets the value for groups/group.
	 * @param v Value to Set.
	 */
	public void setGroups_group(ItemI v) throws Exception{
		_Groups_group =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/groups/group",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/groups/group",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the groups/group of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeGroups_group(int index) throws java.lang.IndexOutOfBoundsException {
		_Groups_group =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/groups/group",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatUser> _Users_user =null;

	/**
	 * users/user
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatUser
	 */
	public ArrayList<org.nrg.xdat.om.XdatUser> getUsers_user() {
		try{
			if (_Users_user==null){
				_Users_user=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("users/user"));
				return _Users_user;
			}else {
				return _Users_user;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatUser>();}
	}

	/**
	 * Sets the value for users/user.
	 * @param v Value to Set.
	 */
	public void setUsers_user(ItemI v) throws Exception{
		_Users_user =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/users/user",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/users/user",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the users/user of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeUsers_user(int index) throws java.lang.IndexOutOfBoundsException {
		_Users_user =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/users/user",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatRoleType> _Roles_role =null;

	/**
	 * roles/role
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatRoleType
	 */
	public ArrayList<org.nrg.xdat.om.XdatRoleType> getRoles_role() {
		try{
			if (_Roles_role==null){
				_Roles_role=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("roles/role"));
				return _Roles_role;
			}else {
				return _Roles_role;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatRoleType>();}
	}

	/**
	 * Sets the value for roles/role.
	 * @param v Value to Set.
	 */
	public void setRoles_role(ItemI v) throws Exception{
		_Roles_role =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/roles/role",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/roles/role",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the roles/role of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeRoles_role(int index) throws java.lang.IndexOutOfBoundsException {
		_Roles_role =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/roles/role",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatActionType> _Actions_action =null;

	/**
	 * actions/action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionType
	 */
	public ArrayList<org.nrg.xdat.om.XdatActionType> getActions_action() {
		try{
			if (_Actions_action==null){
				_Actions_action=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("actions/action"));
				return _Actions_action;
			}else {
				return _Actions_action;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatActionType>();}
	}

	/**
	 * Sets the value for actions/action.
	 * @param v Value to Set.
	 */
	public void setActions_action(ItemI v) throws Exception{
		_Actions_action =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/actions/action",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/actions/action",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the actions/action of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeActions_action(int index) throws java.lang.IndexOutOfBoundsException {
		_Actions_action =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/actions/action",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XdatElementSecurity> _ElementSecuritySet_elementSecurity =null;

	/**
	 * element_security_set/element_security
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatElementSecurity
	 */
	public ArrayList<org.nrg.xdat.om.XdatElementSecurity> getElementSecuritySet_elementSecurity() {
		try{
			if (_ElementSecuritySet_elementSecurity==null){
				_ElementSecuritySet_elementSecurity=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("element_security_set/element_security"));
				return _ElementSecuritySet_elementSecurity;
			}else {
				return _ElementSecuritySet_elementSecurity;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatElementSecurity>();}
	}

	/**
	 * Sets the value for element_security_set/element_security.
	 * @param v Value to Set.
	 */
	public void setElementSecuritySet_elementSecurity(ItemI v) throws Exception{
		_ElementSecuritySet_elementSecurity =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_security_set/element_security",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/element_security_set/element_security",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the element_security_set/element_security of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeElementSecuritySet_elementSecurity(int index) throws java.lang.IndexOutOfBoundsException {
		_ElementSecuritySet_elementSecurity =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/element_security_set/element_security",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XdatNewsentryI _Newslist_news =null;

	/**
	 * newsList/news
	 * @return org.nrg.xdat.om.XdatNewsentryI
	 */
	public org.nrg.xdat.om.XdatNewsentryI getNewslist_news() {
		try{
			if (_Newslist_news==null){
				_Newslist_news=((XdatNewsentryI)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("newsList/news")));
				return _Newslist_news;
			}else {
				return _Newslist_news;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for newsList/news.
	 * @param v Value to Set.
	 */
	public void setNewslist_news(ItemI v) throws Exception{
		_Newslist_news =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/newsList/news",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/newsList/news",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the newsList/news.
	 * */
	public void removeNewslist_news() {
		_Newslist_news =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/newsList/news",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Newslist_newsFK=null;

	/**
	 * @return Returns the xdat:security/newslist_news_xdat_newsentry_id.
	 */
	public Integer getNewslist_newsFK(){
		try{
			if (_Newslist_newsFK==null){
				_Newslist_newsFK=getIntegerProperty("xdat:security/newslist_news_xdat_newsentry_id");
				return _Newslist_newsFK;
			}else {
				return _Newslist_newsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat:security/newslist_news_xdat_newsentry_id.
	 * @param v Value to Set.
	 */
	public void setNewslist_newsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/newslist_news_xdat_newsentry_id",v);
		_Newslist_newsFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XdatInfoentryI _Infolist_info =null;

	/**
	 * infoList/info
	 * @return org.nrg.xdat.om.XdatInfoentryI
	 */
	public org.nrg.xdat.om.XdatInfoentryI getInfolist_info() {
		try{
			if (_Infolist_info==null){
				_Infolist_info=((XdatInfoentryI)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("infoList/info")));
				return _Infolist_info;
			}else {
				return _Infolist_info;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for infoList/info.
	 * @param v Value to Set.
	 */
	public void setInfolist_info(ItemI v) throws Exception{
		_Infolist_info =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/infoList/info",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/infoList/info",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the infoList/info.
	 * */
	public void removeInfolist_info() {
		_Infolist_info =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/infoList/info",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Infolist_infoFK=null;

	/**
	 * @return Returns the xdat:security/infolist_info_xdat_infoentry_id.
	 */
	public Integer getInfolist_infoFK(){
		try{
			if (_Infolist_infoFK==null){
				_Infolist_infoFK=getIntegerProperty("xdat:security/infolist_info_xdat_infoentry_id");
				return _Infolist_infoFK;
			}else {
				return _Infolist_infoFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat:security/infolist_info_xdat_infoentry_id.
	 * @param v Value to Set.
	 */
	public void setInfolist_infoFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/infolist_info_xdat_infoentry_id",v);
		_Infolist_infoFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _System=null;

	/**
	 * @return Returns the system.
	 */
	public String getSystem(){
		try{
			if (_System==null){
				_System=getStringProperty("system");
				return _System;
			}else {
				return _System;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for system.
	 * @param v Value to Set.
	 */
	public void setSystem(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/system",v);
		_System=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _RequireLogin=null;

	/**
	 * @return Returns the require_login.
	 */
	public Boolean getRequireLogin() {
		try{
			if (_RequireLogin==null){
				_RequireLogin=getBooleanProperty("require_login");
				return _RequireLogin;
			}else {
				return _RequireLogin;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for require_login.
	 * @param v Value to Set.
	 */
	public void setRequireLogin(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/require_login",v);
		_RequireLogin=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatSecurityId=null;

	/**
	 * @return Returns the xdat_security_id.
	 */
	public Integer getXdatSecurityId() {
		try{
			if (_XdatSecurityId==null){
				_XdatSecurityId=getIntegerProperty("xdat_security_id");
				return _XdatSecurityId;
			}else {
				return _XdatSecurityId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_security_id.
	 * @param v Value to Set.
	 */
	public void setXdatSecurityId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_security_id",v);
		_XdatSecurityId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatSecurity> getAllXdatSecuritys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSecurity> al = new ArrayList<org.nrg.xdat.om.XdatSecurity>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSecurity> getXdatSecuritysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSecurity> al = new ArrayList<org.nrg.xdat.om.XdatSecurity>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSecurity> getXdatSecuritysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSecurity> al = new ArrayList<org.nrg.xdat.om.XdatSecurity>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatSecurity getXdatSecuritysByXdatSecurityId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:security/xdat_security_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatSecurity) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XdatSecurity getXdatSecuritysBySystem(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:security/system",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatSecurity) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //groups/group
	        for(XdatUsergroup childGroups_group : this.getGroups_group()){
	            for(ResourceFile rf: childGroups_group.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("groups/group[" + childGroups_group.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("groups/group/" + childGroups_group.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //users/user
	        for(XdatUser childUsers_user : this.getUsers_user()){
	            for(ResourceFile rf: childUsers_user.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("users/user[" + childUsers_user.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("users/user/" + childUsers_user.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //roles/role
	        for(XdatRoleType childRoles_role : this.getRoles_role()){
	            for(ResourceFile rf: childRoles_role.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("roles/role[" + childRoles_role.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("roles/role/" + childRoles_role.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //actions/action
	        for(XdatActionType childActions_action : this.getActions_action()){
	            for(ResourceFile rf: childActions_action.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("actions/action[" + childActions_action.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("actions/action/" + childActions_action.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //element_security_set/element_security
	        for(XdatElementSecurity childElementSecuritySet_elementSecurity : this.getElementSecuritySet_elementSecurity()){
	            for(ResourceFile rf: childElementSecuritySet_elementSecurity.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("element_security_set/element_security[" + childElementSecuritySet_elementSecurity.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("element_security_set/element_security/" + childElementSecuritySet_elementSecurity.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //newsList/news
	        XdatNewsentry childNewslist_news = (XdatNewsentry)this.getNewslist_news();
	            for(ResourceFile rf: childNewslist_news.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("newsList/news[" + childNewslist_news.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("newsList/news/" + childNewslist_news.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	
	        localLoop = preventLoop;
	
	        //infoList/info
	        XdatInfoentry childInfolist_info = (XdatInfoentry)this.getInfolist_info();
	            for(ResourceFile rf: childInfolist_info.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("infoList/info[" + childInfolist_info.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("infoList/info/" + childInfolist_info.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
