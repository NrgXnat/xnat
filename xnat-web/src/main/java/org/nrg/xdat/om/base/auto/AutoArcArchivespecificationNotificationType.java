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
public abstract class AutoArcArchivespecificationNotificationType extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcArchivespecificationNotificationTypeI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcArchivespecificationNotificationType.class);
	public static String SCHEMA_ELEMENT_NAME="arc:ArchiveSpecification_notification_type";

	public AutoArcArchivespecificationNotificationType(ItemI item)
	{
		super(item);
	}

	public AutoArcArchivespecificationNotificationType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcArchivespecificationNotificationType(UserI user)
	 **/
	public AutoArcArchivespecificationNotificationType(){}

	public AutoArcArchivespecificationNotificationType(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:ArchiveSpecification_notification_type";
	}

	//FIELD

	private String _NotificationType=null;

	/**
	 * @return Returns the notification_type.
	 */
	public String getNotificationType(){
		try{
			if (_NotificationType==null){
				_NotificationType=getStringProperty("notification_type");
				return _NotificationType;
			}else {
				return _NotificationType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for notification_type.
	 * @param v Value to Set.
	 */
	public void setNotificationType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/notification_type",v);
		_NotificationType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _EmailAddresses=null;

	/**
	 * @return Returns the email_addresses.
	 */
	public String getEmailAddresses(){
		try{
			if (_EmailAddresses==null){
				_EmailAddresses=getStringProperty("email_addresses");
				return _EmailAddresses;
			}else {
				return _EmailAddresses;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for email_addresses.
	 * @param v Value to Set.
	 */
	public void setEmailAddresses(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/email_addresses",v);
		_EmailAddresses=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ArcArchivespecificationNotificationTypeId=null;

	/**
	 * @return Returns the arc_ArchiveSpecification_notification_type_id.
	 */
	public Integer getArcArchivespecificationNotificationTypeId() {
		try{
			if (_ArcArchivespecificationNotificationTypeId==null){
				_ArcArchivespecificationNotificationTypeId=getIntegerProperty("arc_ArchiveSpecification_notification_type_id");
				return _ArcArchivespecificationNotificationTypeId;
			}else {
				return _ArcArchivespecificationNotificationTypeId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_ArchiveSpecification_notification_type_id.
	 * @param v Value to Set.
	 */
	public void setArcArchivespecificationNotificationTypeId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_ArchiveSpecification_notification_type_id",v);
		_ArcArchivespecificationNotificationTypeId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> getAllArcArchivespecificationNotificationTypes(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> getArcArchivespecificationNotificationTypesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> getArcArchivespecificationNotificationTypesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType> al = new ArrayList<org.nrg.xdat.om.ArcArchivespecificationNotificationType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcArchivespecificationNotificationType getArcArchivespecificationNotificationTypesByArcArchivespecificationNotificationTypeId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:ArchiveSpecification_notification_type/arc_ArchiveSpecification_notification_type_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcArchivespecificationNotificationType) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	return _return;
}
}
