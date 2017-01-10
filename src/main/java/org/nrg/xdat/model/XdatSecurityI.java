/*
 * core: org.nrg.xdat.model.XdatSecurityI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Thu Mar 31 18:38:30 CDT 2016
 *
 */
package org.nrg.xdat.model;

import java.util.List;

/**
 * @author XDAT
 *
 */
public interface XdatSecurityI {

	public String getXSIType();

	public void toXML(java.io.Writer writer) throws java.lang.Exception;

	/**
	 * groups/group
	 * @return Returns an List of org.nrg.xdat.model.XdatUsergroupI
	 */
	public <A extends org.nrg.xdat.model.XdatUsergroupI> List<A> getGroups_group();

	/**
	 * groups/group
	 */
	public <A extends org.nrg.xdat.model.XdatUsergroupI> void addGroups_group(A item) throws Exception;

	/**
	 * users/user
	 * @return Returns an List of org.nrg.xdat.model.XdatUserI
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> List<A> getUsers_user();

	/**
	 * users/user
	 */
	public <A extends org.nrg.xdat.model.XdatUserI> void addUsers_user(A item) throws Exception;

	/**
	 * roles/role
	 * @return Returns an List of org.nrg.xdat.model.XdatRoleTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> List<A> getRoles_role();

	/**
	 * roles/role
	 */
	public <A extends org.nrg.xdat.model.XdatRoleTypeI> void addRoles_role(A item) throws Exception;

	/**
	 * actions/action
	 * @return Returns an List of org.nrg.xdat.model.XdatActionTypeI
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> List<A> getActions_action();

	/**
	 * actions/action
	 */
	public <A extends org.nrg.xdat.model.XdatActionTypeI> void addActions_action(A item) throws Exception;

	/**
	 * element_security_set/element_security
	 * @return Returns an List of org.nrg.xdat.model.XdatElementSecurityI
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityI> List<A> getElementSecuritySet_elementSecurity();

	/**
	 * element_security_set/element_security
	 */
	public <A extends org.nrg.xdat.model.XdatElementSecurityI> void addElementSecuritySet_elementSecurity(A item) throws Exception;

	/**
	 * newsList/news
	 * @return org.nrg.xdat.model.XdatNewsentryI
	 */
	public org.nrg.xdat.model.XdatNewsentryI getNewslist_news();

	/**
	 * newsList/news
	 */
	public <A extends org.nrg.xdat.model.XdatNewsentryI> void setNewslist_news(A item) throws Exception;

	/**
	 * @return Returns the xdat:security/newslist_news_xdat_newsentry_id.
	 */
	public Integer getNewslist_newsFK();

	/**
	 * infoList/info
	 * @return org.nrg.xdat.model.XdatInfoentryI
	 */
	public org.nrg.xdat.model.XdatInfoentryI getInfolist_info();

	/**
	 * infoList/info
	 */
	public <A extends org.nrg.xdat.model.XdatInfoentryI> void setInfolist_info(A item) throws Exception;

	/**
	 * @return Returns the xdat:security/infolist_info_xdat_infoentry_id.
	 */
	public Integer getInfolist_infoFK();

	/**
	 * @return Returns the system.
	 */
	public String getSystem();

	/**
	 * Sets the value for system.
	 * @param v Value to Set.
	 */
	public void setSystem(String v);

	/**
	 * @return Returns the require_login.
	 */
	public Boolean getRequireLogin();

	/**
	 * Sets the value for require_login.
	 * @param v Value to Set.
	 */
	public void setRequireLogin(Object v);

	/**
	 * @return Returns the xdat_security_id.
	 */
	public Integer getXdatSecurityId();
}
