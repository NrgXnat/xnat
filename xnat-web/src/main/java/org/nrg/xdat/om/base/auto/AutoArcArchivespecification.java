/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:05 UTC 2016
 *
 */
package org.nrg.xdat.om.base.auto;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.exception.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoArcArchivespecification extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcArchivespecificationI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcArchivespecification.class);
	public static String SCHEMA_ELEMENT_NAME="arc:ArchiveSpecification";

	public AutoArcArchivespecification(ItemI item)
	{
		super(item);
	}

	public AutoArcArchivespecification(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcArchivespecification(UserI user)
	 **/
	public AutoArcArchivespecification(){}

	public AutoArcArchivespecification(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:ArchiveSpecification";
	}
	 private org.nrg.xdat.om.ArcPathinfo _Globalpaths =null;

	/**
	 * globalPaths
	 * @return org.nrg.xdat.om.ArcPathinfo
	 */
	public org.nrg.xdat.om.ArcPathinfo getGlobalpaths() {
		try{
			if (_Globalpaths==null){
				_Globalpaths=((ArcPathinfo)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("globalPaths")));
				return _Globalpaths;
			}else {
				return _Globalpaths;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for globalPaths.
	 * @param v Value to Set.
	 */
	public void setGlobalpaths(ItemI v) throws Exception{
		_Globalpaths =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/globalPaths",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/globalPaths",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * globalPaths
	 * set org.nrg.xdat.model.ArcPathinfoI
	 */
	public <A extends org.nrg.xdat.model.ArcPathinfoI> void setGlobalpaths(A item) throws Exception{
	setGlobalpaths((ItemI)item);
	}

	/**
	 * Removes the globalPaths.
	 * */
	public void removeGlobalpaths() {
		_Globalpaths =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/globalPaths",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _GlobalpathsFK=null;

	/**
	 * @return Returns the arc:ArchiveSpecification/globalpaths_arc_pathinfo_id.
	 */
	public Integer getGlobalpathsFK(){
		try{
			if (_GlobalpathsFK==null){
				_GlobalpathsFK=getIntegerProperty("arc:ArchiveSpecification/globalpaths_arc_pathinfo_id");
				return _GlobalpathsFK;
			}else {
				return _GlobalpathsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc:ArchiveSpecification/globalpaths_arc_pathinfo_id.
	 * @param v Value to Set.
	 */
	public void setGlobalpathsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/globalpaths_arc_pathinfo_id",v);
		_GlobalpathsFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ArcFieldspecification> _Fieldspecifications_fieldspecification =null;

	/**
	 * fieldSpecifications/fieldSpecification
	 * @return Returns an List of org.nrg.xdat.om.ArcFieldspecification
	 */
	public <A extends org.nrg.xdat.model.ArcFieldspecificationI> List<A> getFieldspecifications_fieldspecification() {
		try{
			if (_Fieldspecifications_fieldspecification==null){
				_Fieldspecifications_fieldspecification=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fieldSpecifications/fieldSpecification"));
			}
			return (List<A>) _Fieldspecifications_fieldspecification;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcFieldspecification>();}
	}

	/**
	 * Sets the value for fieldSpecifications/fieldSpecification.
	 * @param v Value to Set.
	 */
	public void setFieldspecifications_fieldspecification(ItemI v) throws Exception{
		_Fieldspecifications_fieldspecification =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fieldSpecifications/fieldSpecification",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fieldSpecifications/fieldSpecification",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * fieldSpecifications/fieldSpecification
	 * Adds org.nrg.xdat.model.ArcFieldspecificationI
	 */
	public <A extends org.nrg.xdat.model.ArcFieldspecificationI> void addFieldspecifications_fieldspecification(A item) throws Exception{
	setFieldspecifications_fieldspecification((ItemI)item);
	}

	/**
	 * Removes the fieldSpecifications/fieldSpecification of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeFieldspecifications_fieldspecification(int index) throws java.lang.IndexOutOfBoundsException {
		_Fieldspecifications_fieldspecification =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/fieldSpecifications/fieldSpecification",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ArcProject> _Projects_project =null;

	/**
	 * projects/project
	 * @return Returns an List of org.nrg.xdat.om.ArcProject
	 */
	public <A extends org.nrg.xdat.model.ArcProjectI> List<A> getProjects_project() {
		try{
			if (_Projects_project==null){
				_Projects_project=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("projects/project"));
			}
			return (List<A>) _Projects_project;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcProject>();}
	}

	/**
	 * Sets the value for projects/project.
	 * @param v Value to Set.
	 */
	public void setProjects_project(ItemI v) throws Exception{
		_Projects_project =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/projects/project",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/projects/project",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * projects/project
	 * Adds org.nrg.xdat.model.ArcProjectI
	 */
	public <A extends org.nrg.xdat.model.ArcProjectI> void addProjects_project(A item) throws Exception{
	setProjects_project((ItemI)item);
	}

	/**
	 * Removes the projects/project of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeProjects_project(int index) throws java.lang.IndexOutOfBoundsException {
		_Projects_project =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/projects/project",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Emailspecifications_newUserRegistration=null;

	/**
	 * @return Returns the emailSpecifications/new_user_registration.
	 */
	public Boolean getEmailspecifications_newUserRegistration() {
		try{
			if (_Emailspecifications_newUserRegistration==null){
				_Emailspecifications_newUserRegistration=getBooleanProperty("emailSpecifications/new_user_registration");
				return _Emailspecifications_newUserRegistration;
			}else {
				return _Emailspecifications_newUserRegistration;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for emailSpecifications/new_user_registration.
	 * @param v Value to Set.
	 */
	public void setEmailspecifications_newUserRegistration(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/emailSpecifications/new_user_registration",v);
		_Emailspecifications_newUserRegistration=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Emailspecifications_pipeline=null;

	/**
	 * @return Returns the emailSpecifications/pipeline.
	 */
	public Boolean getEmailspecifications_pipeline() {
		try{
			if (_Emailspecifications_pipeline==null){
				_Emailspecifications_pipeline=getBooleanProperty("emailSpecifications/pipeline");
				return _Emailspecifications_pipeline;
			}else {
				return _Emailspecifications_pipeline;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for emailSpecifications/pipeline.
	 * @param v Value to Set.
	 */
	public void setEmailspecifications_pipeline(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/emailSpecifications/pipeline",v);
		_Emailspecifications_pipeline=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Emailspecifications_projectAccess=null;

	/**
	 * @return Returns the emailSpecifications/project_access.
	 */
	public Boolean getEmailspecifications_projectAccess() {
		try{
			if (_Emailspecifications_projectAccess==null){
				_Emailspecifications_projectAccess=getBooleanProperty("emailSpecifications/project_access");
				return _Emailspecifications_projectAccess;
			}else {
				return _Emailspecifications_projectAccess;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for emailSpecifications/project_access.
	 * @param v Value to Set.
	 */
	public void setEmailspecifications_projectAccess(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/emailSpecifications/project_access",v);
		_Emailspecifications_projectAccess=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Emailspecifications_transfer=null;

	/**
	 * @return Returns the emailSpecifications/transfer.
	 */
	public Boolean getEmailspecifications_transfer() {
		try{
			if (_Emailspecifications_transfer==null){
				_Emailspecifications_transfer=getBooleanProperty("emailSpecifications/transfer");
				return _Emailspecifications_transfer;
			}else {
				return _Emailspecifications_transfer;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for emailSpecifications/transfer.
	 * @param v Value to Set.
	 */
	public void setEmailspecifications_transfer(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/emailSpecifications/transfer",v);
		_Emailspecifications_transfer=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Emailspecifications_pageEmail=null;

	/**
	 * @return Returns the emailSpecifications/page_email.
	 */
	public Boolean getEmailspecifications_pageEmail() {
		try{
			if (_Emailspecifications_pageEmail==null){
				_Emailspecifications_pageEmail=getBooleanProperty("emailSpecifications/page_email");
				return _Emailspecifications_pageEmail;
			}else {
				return _Emailspecifications_pageEmail;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for emailSpecifications/page_email.
	 * @param v Value to Set.
	 */
	public void setEmailspecifications_pageEmail(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/emailSpecifications/page_email",v);
		_Emailspecifications_pageEmail=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> _NotificationTypes_notificationType =null;

	/**
	 * notification_types/notification_type
	 * @return Returns an List of org.nrg.xdat.om.ArcArchivespecificationNotificationType
	 */
	public <A extends org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI> List<A> getNotificationTypes_notificationType() {
		try{
			if (_NotificationTypes_notificationType==null){
				_NotificationTypes_notificationType=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("notification_types/notification_type"));
			}
			return (List<A>) _NotificationTypes_notificationType;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType>();}
	}

	/**
	 * Sets the value for notification_types/notification_type.
	 * @param v Value to Set.
	 */
	public void setNotificationTypes_notificationType(ItemI v) throws Exception{
		_NotificationTypes_notificationType =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/notification_types/notification_type",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/notification_types/notification_type",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * notification_types/notification_type
	 * Adds org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI
	 */
	public <A extends org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI> void addNotificationTypes_notificationType(A item) throws Exception{
	setNotificationTypes_notificationType((ItemI)item);
	}

	/**
	 * Removes the notification_types/notification_type of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeNotificationTypes_notificationType(int index) throws java.lang.IndexOutOfBoundsException {
		_NotificationTypes_notificationType =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/notification_types/notification_type",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcm_dcmHost=null;

	/**
	 * @return Returns the dcm/dcm_host.
	 */
	public String getDcm_dcmHost(){
		try{
			if (_Dcm_dcmHost==null){
				_Dcm_dcmHost=getStringProperty("dcm/dcm_host");
				return _Dcm_dcmHost;
			}else {
				return _Dcm_dcmHost;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcm/dcm_host.
	 * @param v Value to Set.
	 */
	public void setDcm_dcmHost(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcm/dcm_host",v);
		_Dcm_dcmHost=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcm_dcmPort=null;

	/**
	 * @return Returns the dcm/dcm_port.
	 */
	public String getDcm_dcmPort(){
		try{
			if (_Dcm_dcmPort==null){
				_Dcm_dcmPort=getStringProperty("dcm/dcm_port");
				return _Dcm_dcmPort;
			}else {
				return _Dcm_dcmPort;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcm/dcm_port.
	 * @param v Value to Set.
	 */
	public void setDcm_dcmPort(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcm/dcm_port",v);
		_Dcm_dcmPort=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcm_dcmAe=null;

	/**
	 * @return Returns the dcm/dcm_ae.
	 */
	public String getDcm_dcmAe(){
		try{
			if (_Dcm_dcmAe==null){
				_Dcm_dcmAe=getStringProperty("dcm/dcm_ae");
				return _Dcm_dcmAe;
			}else {
				return _Dcm_dcmAe;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcm/dcm_ae.
	 * @param v Value to Set.
	 */
	public void setDcm_dcmAe(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcm/dcm_ae",v);
		_Dcm_dcmAe=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcm_httpUrl=null;

	/**
	 * @return Returns the dcm/http_url.
	 */
	public String getDcm_httpUrl(){
		try{
			if (_Dcm_httpUrl==null){
				_Dcm_httpUrl=getStringProperty("dcm/http_url");
				return _Dcm_httpUrl;
			}else {
				return _Dcm_httpUrl;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcm/http_url.
	 * @param v Value to Set.
	 */
	public void setDcm_httpUrl(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcm/http_url",v);
		_Dcm_httpUrl=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Dcm_appletLink=null;

	/**
	 * @return Returns the dcm/applet_link.
	 */
	public Boolean getDcm_appletLink() {
		try{
			if (_Dcm_appletLink==null){
				_Dcm_appletLink=getBooleanProperty("dcm/applet_link");
				return _Dcm_appletLink;
			}else {
				return _Dcm_appletLink;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcm/applet_link.
	 * @param v Value to Set.
	 */
	public void setDcm_appletLink(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/dcm/applet_link",v);
		_Dcm_appletLink=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SiteId=null;

	/**
	 * @return Returns the site_id.
	 */
	public String getSiteId(){
		try{
			if (_SiteId==null){
				_SiteId=getStringProperty("site_id");
				return _SiteId;
			}else {
				return _SiteId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for site_id.
	 * @param v Value to Set.
	 */
	public void setSiteId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/site_id",v);
		_SiteId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SiteAdminEmail=null;

	/**
	 * @return Returns the site_admin_email.
	 */
	public String getSiteAdminEmail(){
		try{
			if (_SiteAdminEmail==null){
				_SiteAdminEmail=getStringProperty("site_admin_email");
				return _SiteAdminEmail;
			}else {
				return _SiteAdminEmail;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for site_admin_email.
	 * @param v Value to Set.
	 */
	public void setSiteAdminEmail(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/site_admin_email",v);
		_SiteAdminEmail=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SiteUrl=null;

	/**
	 * @return Returns the site_url.
	 */
	public String getSiteUrl(){
		try{
			if (_SiteUrl==null){
				_SiteUrl=getStringProperty("site_url");
				return _SiteUrl;
			}else {
				return _SiteUrl;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for site_url.
	 * @param v Value to Set.
	 */
	public void setSiteUrl(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/site_url",v);
		_SiteUrl=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SmtpHost=null;

	/**
	 * @return Returns the smtp_host.
	 */
	public String getSmtpHost(){
		try{
			if (_SmtpHost==null){
				_SmtpHost=getStringProperty("smtp_host");
				return _SmtpHost;
			}else {
				return _SmtpHost;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for smtp_host.
	 * @param v Value to Set.
	 */
	public void setSmtpHost(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/smtp_host",v);
		_SmtpHost=null;
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

	private Boolean _EnableNewRegistrations=null;

	/**
	 * @return Returns the enable_new_registrations.
	 */
	public Boolean getEnableNewRegistrations() {
		try{
			if (_EnableNewRegistrations==null){
				_EnableNewRegistrations=getBooleanProperty("enable_new_registrations");
				return _EnableNewRegistrations;
			}else {
				return _EnableNewRegistrations;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for enable_new_registrations.
	 * @param v Value to Set.
	 */
	public void setEnableNewRegistrations(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/enable_new_registrations",v);
		_EnableNewRegistrations=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _EnableCsrfToken=null;

	/**
	 * @return Returns the enable_csrf_token.
	 */
	public Boolean getEnableCsrfToken() {
		try{
			if (_EnableCsrfToken==null){
				_EnableCsrfToken=getBooleanProperty("enable_csrf_token");
				return _EnableCsrfToken;
			}else {
				return _EnableCsrfToken;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for enable_csrf_token.
	 * @param v Value to Set.
	 */
	public void setEnableCsrfToken(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/enable_csrf_token",v);
		_EnableCsrfToken=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _QuarantineCode=null;

	/**
	 * @return Returns the quarantine_code.
	 */
	public Integer getQuarantineCode() {
		try{
			if (_QuarantineCode==null){
				_QuarantineCode=getIntegerProperty("quarantine_code");
				return _QuarantineCode;
			}else {
				return _QuarantineCode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for quarantine_code.
	 * @param v Value to Set.
	 */
	public void setQuarantineCode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/quarantine_code",v);
		_QuarantineCode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PrearchiveCode=null;

	/**
	 * @return Returns the prearchive_code.
	 */
	public Integer getPrearchiveCode() {
		try{
			if (_PrearchiveCode==null){
				_PrearchiveCode=getIntegerProperty("prearchive_code");
				return _PrearchiveCode;
			}else {
				return _PrearchiveCode;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prearchive_code.
	 * @param v Value to Set.
	 */
	public void setPrearchiveCode(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prearchive_code",v);
		_PrearchiveCode=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ArcArchivespecificationId=null;

	/**
	 * @return Returns the arc_ArchiveSpecification_id.
	 */
	public Integer getArcArchivespecificationId() {
		try{
			if (_ArcArchivespecificationId==null){
				_ArcArchivespecificationId=getIntegerProperty("arc_ArchiveSpecification_id");
				return _ArcArchivespecificationId;
			}else {
				return _ArcArchivespecificationId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_ArchiveSpecification_id.
	 * @param v Value to Set.
	 */
	public void setArcArchivespecificationId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_ArchiveSpecification_id",v);
		_ArcArchivespecificationId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecification> getAllArcArchivespecifications(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecification> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecification>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecification> getArcArchivespecificationsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecification> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecification>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecification> getArcArchivespecificationsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecification> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecification>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcArchivespecification getArcArchivespecificationsByArcArchivespecificationId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:ArchiveSpecification/arc_ArchiveSpecification_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcArchivespecification) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArcArchivespecification getArcArchivespecificationsBySiteId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:ArchiveSpecification/site_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcArchivespecification) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //globalPaths
	        ArcPathinfo childGlobalpaths = (ArcPathinfo)this.getGlobalpaths();
	            if (childGlobalpaths!=null){
	              for(ResourceFile rf: ((ArcPathinfo)childGlobalpaths).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("globalPaths[" + ((ArcPathinfo)childGlobalpaths).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("globalPaths/" + ((ArcPathinfo)childGlobalpaths).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //fieldSpecifications/fieldSpecification
	        for(org.nrg.xdat.model.ArcFieldspecificationI childFieldspecifications_fieldspecification : this.getFieldspecifications_fieldspecification()){
	            if (childFieldspecifications_fieldspecification!=null){
	              for(ResourceFile rf: ((ArcFieldspecification)childFieldspecifications_fieldspecification).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fieldSpecifications/fieldSpecification[" + ((ArcFieldspecification)childFieldspecifications_fieldspecification).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fieldSpecifications/fieldSpecification/" + ((ArcFieldspecification)childFieldspecifications_fieldspecification).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //projects/project
	        for(org.nrg.xdat.model.ArcProjectI childProjects_project : this.getProjects_project()){
	            if (childProjects_project!=null){
	              for(ResourceFile rf: ((ArcProject)childProjects_project).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("projects/project[" + ((ArcProject)childProjects_project).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("projects/project/" + ((ArcProject)childProjects_project).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //notification_types/notification_type
	        for(org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI childNotificationTypes_notificationType : this.getNotificationTypes_notificationType()){
	            if (childNotificationTypes_notificationType!=null){
	              for(ResourceFile rf: ((ArcArchivespecificationNotificationType)childNotificationTypes_notificationType).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("notification_types/notification_type[" + ((ArcArchivespecificationNotificationType)childNotificationTypes_notificationType).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("notification_types/notification_type/" + ((ArcArchivespecificationNotificationType)childNotificationTypes_notificationType).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
