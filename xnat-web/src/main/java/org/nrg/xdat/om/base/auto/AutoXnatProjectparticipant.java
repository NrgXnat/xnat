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
public abstract class AutoXnatProjectparticipant extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatProjectparticipantI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatProjectparticipant.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:projectParticipant";

	public AutoXnatProjectparticipant(ItemI item)
	{
		super(item);
	}

	public AutoXnatProjectparticipant(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatProjectparticipant(UserI user)
	 **/
	public AutoXnatProjectparticipant(){}

	public AutoXnatProjectparticipant(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:projectParticipant";
	}

	//FIELD

	private String _Label=null;

	/**
	 * @return Returns the label.
	 */
	public String getLabel(){
		try{
			if (_Label==null){
				_Label=getStringProperty("label");
				return _Label;
			}else {
				return _Label;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for label.
	 * @param v Value to Set.
	 */
	public void setLabel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/label",v);
		_Label=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Project=null;

	/**
	 * @return Returns the project.
	 */
	public String getProject(){
		try{
			if (_Project==null){
				_Project=getStringProperty("project");
				return _Project;
			}else {
				return _Project;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for project.
	 * @param v Value to Set.
	 */
	public void setProject(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/project",v);
		_Project=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SubjectId=null;

	/**
	 * @return Returns the subject_ID.
	 */
	public String getSubjectId(){
		try{
			if (_SubjectId==null){
				_SubjectId=getStringProperty("subject_ID");
				return _SubjectId;
			}else {
				return _SubjectId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for subject_ID.
	 * @param v Value to Set.
	 */
	public void setSubjectId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/subject_ID",v);
		_SubjectId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Group=null;

	/**
	 * @return Returns the group.
	 */
	public String getGroup(){
		try{
			if (_Group==null){
				_Group=getStringProperty("group");
				return _Group;
			}else {
				return _Group;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for group.
	 * @param v Value to Set.
	 */
	public void setGroup(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/group",v);
		_Group=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatProjectparticipantId=null;

	/**
	 * @return Returns the xnat_projectParticipant_id.
	 */
	public Integer getXnatProjectparticipantId() {
		try{
			if (_XnatProjectparticipantId==null){
				_XnatProjectparticipantId=getIntegerProperty("xnat_projectParticipant_id");
				return _XnatProjectparticipantId;
			}else {
				return _XnatProjectparticipantId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_projectParticipant_id.
	 * @param v Value to Set.
	 */
	public void setXnatProjectparticipantId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_projectParticipant_id",v);
		_XnatProjectparticipantId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectparticipant> getAllXnatProjectparticipants(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectparticipant> al = new ArrayList<org.nrg.xdat.om.XnatProjectparticipant>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectparticipant> getXnatProjectparticipantsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectparticipant> al = new ArrayList<org.nrg.xdat.om.XnatProjectparticipant>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectparticipant> getXnatProjectparticipantsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectparticipant> al = new ArrayList<org.nrg.xdat.om.XnatProjectparticipant>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatProjectparticipant getXnatProjectparticipantsByXnatProjectparticipantId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:projectParticipant/xnat_projectParticipant_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatProjectparticipant) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
