/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:10 UTC 2016
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
public abstract class AutoProvProcessstepLibrary extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ProvProcessstepLibraryI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoProvProcessstepLibrary.class);
	public static String SCHEMA_ELEMENT_NAME="prov:processStep_library";

	public AutoProvProcessstepLibrary(ItemI item)
	{
		super(item);
	}

	public AutoProvProcessstepLibrary(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoProvProcessstepLibrary(UserI user)
	 **/
	public AutoProvProcessstepLibrary(){}

	public AutoProvProcessstepLibrary(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "prov:processStep_library";
	}

	//FIELD

	private String _Library=null;

	/**
	 * @return Returns the library.
	 */
	public String getLibrary(){
		try{
			if (_Library==null){
				_Library=getStringProperty("library");
				return _Library;
			}else {
				return _Library;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for library.
	 * @param v Value to Set.
	 */
	public void setLibrary(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/library",v);
		_Library=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Version=null;

	/**
	 * @return Returns the version.
	 */
	public String getVersion(){
		try{
			if (_Version==null){
				_Version=getStringProperty("version");
				return _Version;
			}else {
				return _Version;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for version.
	 * @param v Value to Set.
	 */
	public void setVersion(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/version",v);
		_Version=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ProvProcessstepLibraryId=null;

	/**
	 * @return Returns the prov_processStep_library_id.
	 */
	public Integer getProvProcessstepLibraryId() {
		try{
			if (_ProvProcessstepLibraryId==null){
				_ProvProcessstepLibraryId=getIntegerProperty("prov_processStep_library_id");
				return _ProvProcessstepLibraryId;
			}else {
				return _ProvProcessstepLibraryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prov_processStep_library_id.
	 * @param v Value to Set.
	 */
	public void setProvProcessstepLibraryId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prov_processStep_library_id",v);
		_ProvProcessstepLibraryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> getAllProvProcessstepLibrarys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> al = new ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> getProvProcessstepLibrarysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> al = new ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> getProvProcessstepLibrarysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> al = new ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ProvProcessstepLibrary getProvProcessstepLibrarysByProvProcessstepLibraryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("prov:processStep_library/prov_processStep_library_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ProvProcessstepLibrary) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
