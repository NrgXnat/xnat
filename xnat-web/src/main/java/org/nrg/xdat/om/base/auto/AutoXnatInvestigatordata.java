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
public abstract class AutoXnatInvestigatordata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatInvestigatordataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatInvestigatordata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:investigatorData";

	public AutoXnatInvestigatordata(ItemI item)
	{
		super(item);
	}

	public AutoXnatInvestigatordata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatInvestigatordata(UserI user)
	 **/
	public AutoXnatInvestigatordata(){}

	public AutoXnatInvestigatordata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:investigatorData";
	}

	//FIELD

	private String _Title=null;

	/**
	 * @return Returns the title.
	 */
	public String getTitle(){
		try{
			if (_Title==null){
				_Title=getStringProperty("title");
				return _Title;
			}else {
				return _Title;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for title.
	 * @param v Value to Set.
	 */
	public void setTitle(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/title",v);
		_Title=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Firstname=null;

	/**
	 * @return Returns the firstname.
	 */
	public String getFirstname(){
		try{
			if (_Firstname==null){
				_Firstname=getStringProperty("firstname");
				return _Firstname;
			}else {
				return _Firstname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for firstname.
	 * @param v Value to Set.
	 */
	public void setFirstname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/firstname",v);
		_Firstname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Lastname=null;

	/**
	 * @return Returns the lastname.
	 */
	public String getLastname(){
		try{
			if (_Lastname==null){
				_Lastname=getStringProperty("lastname");
				return _Lastname;
			}else {
				return _Lastname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for lastname.
	 * @param v Value to Set.
	 */
	public void setLastname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/lastname",v);
		_Lastname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Institution=null;

	/**
	 * @return Returns the institution.
	 */
	public String getInstitution(){
		try{
			if (_Institution==null){
				_Institution=getStringProperty("institution");
				return _Institution;
			}else {
				return _Institution;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for institution.
	 * @param v Value to Set.
	 */
	public void setInstitution(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/institution",v);
		_Institution=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Department=null;

	/**
	 * @return Returns the department.
	 */
	public String getDepartment(){
		try{
			if (_Department==null){
				_Department=getStringProperty("department");
				return _Department;
			}else {
				return _Department;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for department.
	 * @param v Value to Set.
	 */
	public void setDepartment(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/department",v);
		_Department=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Email=null;

	/**
	 * @return Returns the email.
	 */
	public String getEmail(){
		try{
			if (_Email==null){
				_Email=getStringProperty("email");
				return _Email;
			}else {
				return _Email;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for email.
	 * @param v Value to Set.
	 */
	public void setEmail(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/email",v);
		_Email=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Phone=null;

	/**
	 * @return Returns the phone.
	 */
	public String getPhone(){
		try{
			if (_Phone==null){
				_Phone=getStringProperty("phone");
				return _Phone;
			}else {
				return _Phone;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for phone.
	 * @param v Value to Set.
	 */
	public void setPhone(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/phone",v);
		_Phone=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("ID");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ID",v);
		_Id=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatInvestigatordataId=null;

	/**
	 * @return Returns the xnat_investigatorData_id.
	 */
	public Integer getXnatInvestigatordataId() {
		try{
			if (_XnatInvestigatordataId==null){
				_XnatInvestigatordataId=getIntegerProperty("xnat_investigatorData_id");
				return _XnatInvestigatordataId;
			}else {
				return _XnatInvestigatordataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_investigatorData_id.
	 * @param v Value to Set.
	 */
	public void setXnatInvestigatordataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_investigatorData_id",v);
		_XnatInvestigatordataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatInvestigatordata> getAllXnatInvestigatordatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatInvestigatordata> al = new ArrayList<org.nrg.xdat.om.XnatInvestigatordata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatInvestigatordata> getXnatInvestigatordatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatInvestigatordata> al = new ArrayList<org.nrg.xdat.om.XnatInvestigatordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatInvestigatordata> getXnatInvestigatordatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatInvestigatordata> al = new ArrayList<org.nrg.xdat.om.XnatInvestigatordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatInvestigatordata getXnatInvestigatordatasByXnatInvestigatordataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:investigatorData/xnat_investigatorData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatInvestigatordata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
