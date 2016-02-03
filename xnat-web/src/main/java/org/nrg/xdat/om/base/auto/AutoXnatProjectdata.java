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
public abstract class AutoXnatProjectdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatProjectdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatProjectdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:projectData";

	public AutoXnatProjectdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatProjectdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatProjectdata(UserI user)
	 **/
	public AutoXnatProjectdata(){}

	public AutoXnatProjectdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:projectData";
	}

	//FIELD

	private String _Name=null;

	/**
	 * @return Returns the name.
	 */
	public String getName(){
		try{
			if (_Name==null){
				_Name=getStringProperty("name");
				return _Name;
			}else {
				return _Name;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for name.
	 * @param v Value to Set.
	 */
	public void setName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/name",v);
		_Name=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Keywords=null;

	/**
	 * @return Returns the keywords.
	 */
	public String getKeywords(){
		try{
			if (_Keywords==null){
				_Keywords=getStringProperty("keywords");
				return _Keywords;
			}else {
				return _Keywords;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for keywords.
	 * @param v Value to Set.
	 */
	public void setKeywords(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/keywords",v);
		_Keywords=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> _Aliases_alias =null;

	/**
	 * aliases/alias
	 * @return Returns an List of org.nrg.xdat.om.XnatProjectdataAlias
	 */
	public <A extends org.nrg.xdat.model.XnatProjectdataAliasI> List<A> getAliases_alias() {
		try{
			if (_Aliases_alias==null){
				_Aliases_alias=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("aliases/alias"));
			}
			return (List<A>) _Aliases_alias;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatProjectdataAlias>();}
	}

	/**
	 * Sets the value for aliases/alias.
	 * @param v Value to Set.
	 */
	public void setAliases_alias(ItemI v) throws Exception{
		_Aliases_alias =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/aliases/alias",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/aliases/alias",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * aliases/alias
	 * Adds org.nrg.xdat.model.XnatProjectdataAliasI
	 */
	public <A extends org.nrg.xdat.model.XnatProjectdataAliasI> void addAliases_alias(A item) throws Exception{
	setAliases_alias((ItemI)item);
	}

	/**
	 * Removes the aliases/alias of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAliases_alias(int index) throws java.lang.IndexOutOfBoundsException {
		_Aliases_alias =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/aliases/alias",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatPublicationresource> _Publications_publication =null;

	/**
	 * publications/publication
	 * @return Returns an List of org.nrg.xdat.om.XnatPublicationresource
	 */
	public <A extends org.nrg.xdat.model.XnatPublicationresourceI> List<A> getPublications_publication() {
		try{
			if (_Publications_publication==null){
				_Publications_publication=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("publications/publication"));
			}
			return (List<A>) _Publications_publication;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatPublicationresource>();}
	}

	/**
	 * Sets the value for publications/publication.
	 * @param v Value to Set.
	 */
	public void setPublications_publication(ItemI v) throws Exception{
		_Publications_publication =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/publications/publication",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/publications/publication",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * publications/publication
	 * Adds org.nrg.xdat.model.XnatPublicationresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatPublicationresourceI> void addPublications_publication(A item) throws Exception{
	setPublications_publication((ItemI)item);
	}

	/**
	 * Removes the publications/publication of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePublications_publication(int index) throws java.lang.IndexOutOfBoundsException {
		_Publications_publication =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/publications/publication",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresource> _Resources_resource =null;

	/**
	 * resources/resource
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresource
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> List<A> getResources_resource() {
		try{
			if (_Resources_resource==null){
				_Resources_resource=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("resources/resource"));
			}
			return (List<A>) _Resources_resource;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();}
	}

	/**
	 * Sets the value for resources/resource.
	 * @param v Value to Set.
	 */
	public void setResources_resource(ItemI v) throws Exception{
		_Resources_resource =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resources/resource",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resources/resource",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * resources/resource
	 * Adds org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void addResources_resource(A item) throws Exception{
	setResources_resource((ItemI)item);
	}

	/**
	 * Removes the resources/resource of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeResources_resource(int index) throws java.lang.IndexOutOfBoundsException {
		_Resources_resource =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/resources/resource",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> _Studyprotocol =null;

	/**
	 * studyProtocol
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractprotocol
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractprotocolI> List<A> getStudyprotocol() {
		try{
			if (_Studyprotocol==null){
				_Studyprotocol=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("studyProtocol"));
			}
			return (List<A>) _Studyprotocol;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractprotocol>();}
	}

	/**
	 * Sets the value for studyProtocol.
	 * @param v Value to Set.
	 */
	public void setStudyprotocol(ItemI v) throws Exception{
		_Studyprotocol =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/studyProtocol",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/studyProtocol",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * studyProtocol
	 * Adds org.nrg.xdat.model.XnatAbstractprotocolI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractprotocolI> void addStudyprotocol(A item) throws Exception{
	setStudyprotocol((ItemI)item);
	}

	/**
	 * Removes the studyProtocol of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeStudyprotocol(int index) throws java.lang.IndexOutOfBoundsException {
		_Studyprotocol =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/studyProtocol",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatInvestigatordata _Pi =null;

	/**
	 * PI
	 * @return org.nrg.xdat.om.XnatInvestigatordata
	 */
	public org.nrg.xdat.om.XnatInvestigatordata getPi() {
		try{
			if (_Pi==null){
				_Pi=((XnatInvestigatordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("PI")));
				return _Pi;
			}else {
				return _Pi;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for PI.
	 * @param v Value to Set.
	 */
	public void setPi(ItemI v) throws Exception{
		_Pi =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/PI",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/PI",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * PI
	 * set org.nrg.xdat.model.XnatInvestigatordataI
	 */
	public <A extends org.nrg.xdat.model.XnatInvestigatordataI> void setPi(A item) throws Exception{
	setPi((ItemI)item);
	}

	/**
	 * Removes the PI.
	 * */
	public void removePi() {
		_Pi =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/PI",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PiFK=null;

	/**
	 * @return Returns the xnat:projectData/pi_xnat_investigatordata_id.
	 */
	public Integer getPiFK(){
		try{
			if (_PiFK==null){
				_PiFK=getIntegerProperty("xnat:projectData/pi_xnat_investigatordata_id");
				return _PiFK;
			}else {
				return _PiFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:projectData/pi_xnat_investigatordata_id.
	 * @param v Value to Set.
	 */
	public void setPiFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pi_xnat_investigatordata_id",v);
		_PiFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatInvestigatordata> _Investigators_investigator =null;

	/**
	 * investigators/investigator
	 * @return Returns an List of org.nrg.xdat.om.XnatInvestigatordata
	 */
	public <A extends org.nrg.xdat.model.XnatInvestigatordataI> List<A> getInvestigators_investigator() {
		try{
			if (_Investigators_investigator==null){
				_Investigators_investigator=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("investigators/investigator"));
			}
			return (List<A>) _Investigators_investigator;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatInvestigatordata>();}
	}

	/**
	 * Sets the value for investigators/investigator.
	 * @param v Value to Set.
	 */
	public void setInvestigators_investigator(ItemI v) throws Exception{
		_Investigators_investigator =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/investigators/investigator",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/investigators/investigator",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * investigators/investigator
	 * Adds org.nrg.xdat.model.XnatInvestigatordataI
	 */
	public <A extends org.nrg.xdat.model.XnatInvestigatordataI> void addInvestigators_investigator(A item) throws Exception{
	setInvestigators_investigator((ItemI)item);
	}

	/**
	 * Removes the investigators/investigator of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeInvestigators_investigator(int index) throws java.lang.IndexOutOfBoundsException {
		_Investigators_investigator =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/investigators/investigator",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatProjectdataField> _Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an List of org.nrg.xdat.om.XnatProjectdataField
	 */
	public <A extends org.nrg.xdat.model.XnatProjectdataFieldI> List<A> getFields_field() {
		try{
			if (_Fields_field==null){
				_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fields/field"));
			}
			return (List<A>) _Fields_field;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatProjectdataField>();}
	}

	/**
	 * Sets the value for fields/field.
	 * @param v Value to Set.
	 */
	public void setFields_field(ItemI v) throws Exception{
		_Fields_field =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * fields/field
	 * Adds org.nrg.xdat.model.XnatProjectdataFieldI
	 */
	public <A extends org.nrg.xdat.model.XnatProjectdataFieldI> void addFields_field(A item) throws Exception{
	setFields_field((ItemI)item);
	}

	/**
	 * Removes the fields/field of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeFields_field(int index) throws java.lang.IndexOutOfBoundsException {
		_Fields_field =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/fields/field",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
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

	private String _SecondaryId=null;

	/**
	 * @return Returns the secondary_ID.
	 */
	public String getSecondaryId(){
		try{
			if (_SecondaryId==null){
				_SecondaryId=getStringProperty("secondary_ID");
				return _SecondaryId;
			}else {
				return _SecondaryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secondary_ID.
	 * @param v Value to Set.
	 */
	public void setSecondaryId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/secondary_ID",v);
		_SecondaryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdata> getAllXnatProjectdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdata> al = new ArrayList<org.nrg.xdat.om.XnatProjectdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdata> getXnatProjectdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdata> al = new ArrayList<org.nrg.xdat.om.XnatProjectdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdata> getXnatProjectdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdata> al = new ArrayList<org.nrg.xdat.om.XnatProjectdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatProjectdata getXnatProjectdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:projectData/ID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatProjectdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //aliases/alias
	        for(org.nrg.xdat.model.XnatProjectdataAliasI childAliases_alias : this.getAliases_alias()){
	            if (childAliases_alias!=null){
	              for(ResourceFile rf: ((XnatProjectdataAlias)childAliases_alias).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("aliases/alias[" + ((XnatProjectdataAlias)childAliases_alias).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("aliases/alias/" + ((XnatProjectdataAlias)childAliases_alias).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //publications/publication
	        for(org.nrg.xdat.model.XnatAbstractresourceI childPublications_publication : this.getPublications_publication()){
	            if (childPublications_publication!=null){
	              int counterPublications_publication=0;
	              for(java.io.File f: ((XnatAbstractresource)childPublications_publication).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("publications/publication[xnat_abstractresource_id=" + ((XnatAbstractresource)childPublications_publication).getXnatAbstractresourceId() + "]/file/" + counterPublications_publication +"");
	                 rf.setXdatPath("publications/publication/" + ((XnatAbstractresource)childPublications_publication).getXnatAbstractresourceId() + "/" + counterPublications_publication++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //resources/resource
	        for(org.nrg.xdat.model.XnatAbstractresourceI childResources_resource : this.getResources_resource()){
	            if (childResources_resource!=null){
	              int counterResources_resource=0;
	              for(java.io.File f: ((XnatAbstractresource)childResources_resource).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("resources/resource[xnat_abstractresource_id=" + ((XnatAbstractresource)childResources_resource).getXnatAbstractresourceId() + "]/file/" + counterResources_resource +"");
	                 rf.setXdatPath("resources/resource/" + ((XnatAbstractresource)childResources_resource).getXnatAbstractresourceId() + "/" + counterResources_resource++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //studyProtocol
	        for(org.nrg.xdat.model.XnatAbstractprotocolI childStudyprotocol : this.getStudyprotocol()){
	            if (childStudyprotocol!=null){
	              for(ResourceFile rf: ((XnatAbstractprotocol)childStudyprotocol).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("studyProtocol[" + ((XnatAbstractprotocol)childStudyprotocol).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("studyProtocol/" + ((XnatAbstractprotocol)childStudyprotocol).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //PI
	        XnatInvestigatordata childPi = (XnatInvestigatordata)this.getPi();
	            if (childPi!=null){
	              for(ResourceFile rf: ((XnatInvestigatordata)childPi).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("PI[" + ((XnatInvestigatordata)childPi).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("PI/" + ((XnatInvestigatordata)childPi).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //investigators/investigator
	        for(org.nrg.xdat.model.XnatInvestigatordataI childInvestigators_investigator : this.getInvestigators_investigator()){
	            if (childInvestigators_investigator!=null){
	              for(ResourceFile rf: ((XnatInvestigatordata)childInvestigators_investigator).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("investigators/investigator[" + ((XnatInvestigatordata)childInvestigators_investigator).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("investigators/investigator/" + ((XnatInvestigatordata)childInvestigators_investigator).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //fields/field
	        for(org.nrg.xdat.model.XnatProjectdataFieldI childFields_field : this.getFields_field()){
	            if (childFields_field!=null){
	              for(ResourceFile rf: ((XnatProjectdataField)childFields_field).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fields/field[" + ((XnatProjectdataField)childFields_field).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fields/field/" + ((XnatProjectdataField)childFields_field).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
