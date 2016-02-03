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
public abstract class AutoArcProject extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcProjectI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcProject.class);
	public static String SCHEMA_ELEMENT_NAME="arc:project";

	public AutoArcProject(ItemI item)
	{
		super(item);
	}

	public AutoArcProject(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcProject(UserI user)
	 **/
	public AutoArcProject(){}

	public AutoArcProject(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:project";
	}
	 private org.nrg.xdat.om.ArcPathinfo _Paths =null;

	/**
	 * paths
	 * @return org.nrg.xdat.om.ArcPathinfo
	 */
	public org.nrg.xdat.om.ArcPathinfo getPaths() {
		try{
			if (_Paths==null){
				_Paths=((ArcPathinfo)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("paths")));
				return _Paths;
			}else {
				return _Paths;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for paths.
	 * @param v Value to Set.
	 */
	public void setPaths(ItemI v) throws Exception{
		_Paths =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/paths",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/paths",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * paths
	 * set org.nrg.xdat.model.ArcPathinfoI
	 */
	public <A extends org.nrg.xdat.model.ArcPathinfoI> void setPaths(A item) throws Exception{
	setPaths((ItemI)item);
	}

	/**
	 * Removes the paths.
	 * */
	public void removePaths() {
		_Paths =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/paths",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PathsFK=null;

	/**
	 * @return Returns the arc:project/paths_arc_pathinfo_id.
	 */
	public Integer getPathsFK(){
		try{
			if (_PathsFK==null){
				_PathsFK=getIntegerProperty("arc:project/paths_arc_pathinfo_id");
				return _PathsFK;
			}else {
				return _PathsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc:project/paths_arc_pathinfo_id.
	 * @param v Value to Set.
	 */
	public void setPathsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/paths_arc_pathinfo_id",v);
		_PathsFK=null;
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
	 private ArrayList<org.nrg.xdat.om.ArcProperty> _Properties_property =null;

	/**
	 * properties/property
	 * @return Returns an List of org.nrg.xdat.om.ArcProperty
	 */
	public <A extends org.nrg.xdat.model.ArcPropertyI> List<A> getProperties_property() {
		try{
			if (_Properties_property==null){
				_Properties_property=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("properties/property"));
			}
			return (List<A>) _Properties_property;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcProperty>();}
	}

	/**
	 * Sets the value for properties/property.
	 * @param v Value to Set.
	 */
	public void setProperties_property(ItemI v) throws Exception{
		_Properties_property =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/properties/property",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/properties/property",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * properties/property
	 * Adds org.nrg.xdat.model.ArcPropertyI
	 */
	public <A extends org.nrg.xdat.model.ArcPropertyI> void addProperties_property(A item) throws Exception{
	setProperties_property((ItemI)item);
	}

	/**
	 * Removes the properties/property of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeProperties_property(int index) throws java.lang.IndexOutOfBoundsException {
		_Properties_property =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/properties/property",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ArcProjectDescendant> _Pipelines_descendants_descendant =null;

	/**
	 * pipelines/descendants/descendant
	 * @return Returns an List of org.nrg.xdat.om.ArcProjectDescendant
	 */
	public <A extends org.nrg.xdat.model.ArcProjectDescendantI> List<A> getPipelines_descendants_descendant() {
		try{
			if (_Pipelines_descendants_descendant==null){
				_Pipelines_descendants_descendant=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("pipelines/descendants/descendant"));
			}
			return (List<A>) _Pipelines_descendants_descendant;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcProjectDescendant>();}
	}

	/**
	 * Sets the value for pipelines/descendants/descendant.
	 * @param v Value to Set.
	 */
	public void setPipelines_descendants_descendant(ItemI v) throws Exception{
		_Pipelines_descendants_descendant =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelines/descendants/descendant",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelines/descendants/descendant",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * pipelines/descendants/descendant
	 * Adds org.nrg.xdat.model.ArcProjectDescendantI
	 */
	public <A extends org.nrg.xdat.model.ArcProjectDescendantI> void addPipelines_descendants_descendant(A item) throws Exception{
	setPipelines_descendants_descendant((ItemI)item);
	}

	/**
	 * Removes the pipelines/descendants/descendant of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePipelines_descendants_descendant(int index) throws java.lang.IndexOutOfBoundsException {
		_Pipelines_descendants_descendant =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/pipelines/descendants/descendant",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ArcProjectPipeline> _Pipelines_pipeline =null;

	/**
	 * pipelines/pipeline
	 * @return Returns an List of org.nrg.xdat.om.ArcProjectPipeline
	 */
	public <A extends org.nrg.xdat.model.ArcProjectPipelineI> List<A> getPipelines_pipeline() {
		try{
			if (_Pipelines_pipeline==null){
				_Pipelines_pipeline=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("pipelines/pipeline"));
			}
			return (List<A>) _Pipelines_pipeline;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcProjectPipeline>();}
	}

	/**
	 * Sets the value for pipelines/pipeline.
	 * @param v Value to Set.
	 */
	public void setPipelines_pipeline(ItemI v) throws Exception{
		_Pipelines_pipeline =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelines/pipeline",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelines/pipeline",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * pipelines/pipeline
	 * Adds org.nrg.xdat.model.ArcProjectPipelineI
	 */
	public <A extends org.nrg.xdat.model.ArcProjectPipelineI> void addPipelines_pipeline(A item) throws Exception{
	setPipelines_pipeline((ItemI)item);
	}

	/**
	 * Removes the pipelines/pipeline of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePipelines_pipeline(int index) throws java.lang.IndexOutOfBoundsException {
		_Pipelines_pipeline =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/pipelines/pipeline",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the id.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("id");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for id.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/id",v);
		_Id=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _CurrentArc=null;

	/**
	 * @return Returns the current_arc.
	 */
	public String getCurrentArc(){
		try{
			if (_CurrentArc==null){
				_CurrentArc=getStringProperty("current_arc");
				return _CurrentArc;
			}else {
				return _CurrentArc;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for current_arc.
	 * @param v Value to Set.
	 */
	public void setCurrentArc(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/current_arc",v);
		_CurrentArc=null;
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

	private Integer _ArcProjectId=null;

	/**
	 * @return Returns the arc_project_id.
	 */
	public Integer getArcProjectId() {
		try{
			if (_ArcProjectId==null){
				_ArcProjectId=getIntegerProperty("arc_project_id");
				return _ArcProjectId;
			}else {
				return _ArcProjectId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_project_id.
	 * @param v Value to Set.
	 */
	public void setArcProjectId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_project_id",v);
		_ArcProjectId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcProject> getAllArcProjects(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProject> al = new ArrayList<org.nrg.xdat.om.ArcProject>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProject> getArcProjectsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProject> al = new ArrayList<org.nrg.xdat.om.ArcProject>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProject> getArcProjectsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProject> al = new ArrayList<org.nrg.xdat.om.ArcProject>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcProject getArcProjectsByArcProjectId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:project/arc_project_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcProject) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArcProject getArcProjectsById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:project/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcProject) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //paths
	        ArcPathinfo childPaths = (ArcPathinfo)this.getPaths();
	            if (childPaths!=null){
	              for(ResourceFile rf: ((ArcPathinfo)childPaths).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("paths[" + ((ArcPathinfo)childPaths).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("paths/" + ((ArcPathinfo)childPaths).getItem().getPKString() + "/" + rf.getXpath());
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
	
	        //properties/property
	        for(org.nrg.xdat.model.ArcPropertyI childProperties_property : this.getProperties_property()){
	            if (childProperties_property!=null){
	              for(ResourceFile rf: ((ArcProperty)childProperties_property).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("properties/property[" + ((ArcProperty)childProperties_property).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("properties/property/" + ((ArcProperty)childProperties_property).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //pipelines/descendants/descendant
	        for(org.nrg.xdat.model.ArcProjectDescendantI childPipelines_descendants_descendant : this.getPipelines_descendants_descendant()){
	            if (childPipelines_descendants_descendant!=null){
	              for(ResourceFile rf: ((ArcProjectDescendant)childPipelines_descendants_descendant).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("pipelines/descendants/descendant[" + ((ArcProjectDescendant)childPipelines_descendants_descendant).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("pipelines/descendants/descendant/" + ((ArcProjectDescendant)childPipelines_descendants_descendant).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //pipelines/pipeline
	        for(org.nrg.xdat.model.ArcProjectPipelineI childPipelines_pipeline : this.getPipelines_pipeline()){
	            if (childPipelines_pipeline!=null){
	              for(ResourceFile rf: ((ArcProjectPipeline)childPipelines_pipeline).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("pipelines/pipeline[" + ((ArcProjectPipeline)childPipelines_pipeline).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("pipelines/pipeline/" + ((ArcProjectPipeline)childPipelines_pipeline).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
