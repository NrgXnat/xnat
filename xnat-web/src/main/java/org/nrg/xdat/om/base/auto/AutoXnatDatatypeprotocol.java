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
public abstract class AutoXnatDatatypeprotocol extends XnatAbstractprotocol implements org.nrg.xdat.model.XnatDatatypeprotocolI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatDatatypeprotocol.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:datatypeProtocol";

	public AutoXnatDatatypeprotocol(ItemI item)
	{
		super(item);
	}

	public AutoXnatDatatypeprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatDatatypeprotocol(UserI user)
	 **/
	public AutoXnatDatatypeprotocol(){}

	public AutoXnatDatatypeprotocol(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:datatypeProtocol";
	}
	 private org.nrg.xdat.om.XnatAbstractprotocol _Abstractprotocol =null;

	/**
	 * abstractProtocol
	 * @return org.nrg.xdat.om.XnatAbstractprotocol
	 */
	public org.nrg.xdat.om.XnatAbstractprotocol getAbstractprotocol() {
		try{
			if (_Abstractprotocol==null){
				_Abstractprotocol=((XnatAbstractprotocol)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractProtocol")));
				return _Abstractprotocol;
			}else {
				return _Abstractprotocol;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractProtocol.
	 * @param v Value to Set.
	 */
	public void setAbstractprotocol(ItemI v) throws Exception{
		_Abstractprotocol =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractProtocol
	 * set org.nrg.xdat.model.XnatAbstractprotocolI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractprotocolI> void setAbstractprotocol(A item) throws Exception{
	setAbstractprotocol((ItemI)item);
	}

	/**
	 * Removes the abstractProtocol.
	 * */
	public void removeAbstractprotocol() {
		_Abstractprotocol =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> _Definitions_definition =null;

	/**
	 * definitions/definition
	 * @return Returns an List of org.nrg.xdat.om.XnatFielddefinitiongroup
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupI> List<A> getDefinitions_definition() {
		try{
			if (_Definitions_definition==null){
				_Definitions_definition=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("definitions/definition"));
			}
			return (List<A>) _Definitions_definition;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup>();}
	}

	/**
	 * Sets the value for definitions/definition.
	 * @param v Value to Set.
	 */
	public void setDefinitions_definition(ItemI v) throws Exception{
		_Definitions_definition =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/definitions/definition",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/definitions/definition",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * definitions/definition
	 * Adds org.nrg.xdat.model.XnatFielddefinitiongroupI
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupI> void addDefinitions_definition(A item) throws Exception{
	setDefinitions_definition((ItemI)item);
	}

	/**
	 * Removes the definitions/definition of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeDefinitions_definition(int index) throws java.lang.IndexOutOfBoundsException {
		_Definitions_definition =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/definitions/definition",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> getAllXnatDatatypeprotocols(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> al = new ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> getXnatDatatypeprotocolsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> al = new ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> getXnatDatatypeprotocolsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol> al = new ArrayList<org.nrg.xdat.om.XnatDatatypeprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatDatatypeprotocol getXnatDatatypeprotocolsByXnatAbstractprotocolId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:datatypeProtocol/xnat_abstractprotocol_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatDatatypeprotocol) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractProtocol
	        XnatAbstractprotocol childAbstractprotocol = (XnatAbstractprotocol)this.getAbstractprotocol();
	            if (childAbstractprotocol!=null){
	              for(ResourceFile rf: ((XnatAbstractprotocol)childAbstractprotocol).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractProtocol[" + ((XnatAbstractprotocol)childAbstractprotocol).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractProtocol/" + ((XnatAbstractprotocol)childAbstractprotocol).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //definitions/definition
	        for(org.nrg.xdat.model.XnatFielddefinitiongroupI childDefinitions_definition : this.getDefinitions_definition()){
	            if (childDefinitions_definition!=null){
	              for(ResourceFile rf: ((XnatFielddefinitiongroup)childDefinitions_definition).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("definitions/definition[" + ((XnatFielddefinitiongroup)childDefinitions_definition).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("definitions/definition/" + ((XnatFielddefinitiongroup)childDefinitions_definition).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
