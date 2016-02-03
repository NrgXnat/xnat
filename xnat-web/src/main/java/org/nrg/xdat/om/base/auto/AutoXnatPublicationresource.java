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
public abstract class AutoXnatPublicationresource extends XnatAbstractresource implements org.nrg.xdat.model.XnatPublicationresourceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPublicationresource.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:publicationResource";

	public AutoXnatPublicationresource(ItemI item)
	{
		super(item);
	}

	public AutoXnatPublicationresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPublicationresource(UserI user)
	 **/
	public AutoXnatPublicationresource(){}

	public AutoXnatPublicationresource(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:publicationResource";
	}
	 private org.nrg.xdat.om.XnatAbstractresource _Abstractresource =null;

	/**
	 * abstractResource
	 * @return org.nrg.xdat.om.XnatAbstractresource
	 */
	public org.nrg.xdat.om.XnatAbstractresource getAbstractresource() {
		try{
			if (_Abstractresource==null){
				_Abstractresource=((XnatAbstractresource)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractResource")));
				return _Abstractresource;
			}else {
				return _Abstractresource;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractResource.
	 * @param v Value to Set.
	 */
	public void setAbstractresource(ItemI v) throws Exception{
		_Abstractresource =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractResource",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractResource",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractResource
	 * set org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void setAbstractresource(A item) throws Exception{
	setAbstractresource((ItemI)item);
	}

	/**
	 * Removes the abstractResource.
	 * */
	public void removeAbstractresource() {
		_Abstractresource =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractResource",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
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

	private String _Citation=null;

	/**
	 * @return Returns the citation.
	 */
	public String getCitation(){
		try{
			if (_Citation==null){
				_Citation=getStringProperty("citation");
				return _Citation;
			}else {
				return _Citation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for citation.
	 * @param v Value to Set.
	 */
	public void setCitation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/citation",v);
		_Citation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Abstract=null;

	/**
	 * @return Returns the abstract.
	 */
	public String getAbstract(){
		try{
			if (_Abstract==null){
				_Abstract=getStringProperty("abstract");
				return _Abstract;
			}else {
				return _Abstract;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for abstract.
	 * @param v Value to Set.
	 */
	public void setAbstract(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/abstract",v);
		_Abstract=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Commentary=null;

	/**
	 * @return Returns the commentary.
	 */
	public String getCommentary(){
		try{
			if (_Commentary==null){
				_Commentary=getStringProperty("commentary");
				return _Commentary;
			}else {
				return _Commentary;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for commentary.
	 * @param v Value to Set.
	 */
	public void setCommentary(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/commentary",v);
		_Commentary=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Isprimary=null;

	/**
	 * @return Returns the isPrimary.
	 */
	public Boolean getIsprimary() {
		try{
			if (_Isprimary==null){
				_Isprimary=getBooleanProperty("isPrimary");
				return _Isprimary;
			}else {
				return _Isprimary;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for isPrimary.
	 * @param v Value to Set.
	 */
	public void setIsprimary(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/isPrimary",v);
		_Isprimary=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Doi=null;

	/**
	 * @return Returns the doi.
	 */
	public String getDoi(){
		try{
			if (_Doi==null){
				_Doi=getStringProperty("doi");
				return _Doi;
			}else {
				return _Doi;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for doi.
	 * @param v Value to Set.
	 */
	public void setDoi(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/doi",v);
		_Doi=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pubmed=null;

	/**
	 * @return Returns the pubmed.
	 */
	public String getPubmed(){
		try{
			if (_Pubmed==null){
				_Pubmed=getStringProperty("pubmed");
				return _Pubmed;
			}else {
				return _Pubmed;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pubmed.
	 * @param v Value to Set.
	 */
	public void setPubmed(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pubmed",v);
		_Pubmed=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Medline=null;

	/**
	 * @return Returns the medline.
	 */
	public String getMedline(){
		try{
			if (_Medline==null){
				_Medline=getStringProperty("medline");
				return _Medline;
			}else {
				return _Medline;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for medline.
	 * @param v Value to Set.
	 */
	public void setMedline(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/medline",v);
		_Medline=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Uri=null;

	/**
	 * @return Returns the uri.
	 */
	public String getUri(){
		try{
			if (_Uri==null){
				_Uri=getStringProperty("uri");
				return _Uri;
			}else {
				return _Uri;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for uri.
	 * @param v Value to Set.
	 */
	public void setUri(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/uri",v);
		_Uri=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Other=null;

	/**
	 * @return Returns the other.
	 */
	public String getOther(){
		try{
			if (_Other==null){
				_Other=getStringProperty("other");
				return _Other;
			}else {
				return _Other;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for other.
	 * @param v Value to Set.
	 */
	public void setOther(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/other",v);
		_Other=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		try{
			if (_Type==null){
				_Type=getStringProperty("type");
				return _Type;
			}else {
				return _Type;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/type",v);
		_Type=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPublicationresource> getAllXnatPublicationresources(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPublicationresource> al = new ArrayList<org.nrg.xdat.om.XnatPublicationresource>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPublicationresource> getXnatPublicationresourcesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPublicationresource> al = new ArrayList<org.nrg.xdat.om.XnatPublicationresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPublicationresource> getXnatPublicationresourcesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPublicationresource> al = new ArrayList<org.nrg.xdat.om.XnatPublicationresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByXnatAbstractresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/xnat_abstractresource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByTitle(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/title",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByDoi(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/doi",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByPubmed(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/pubmed",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByMedline(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/medline",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatPublicationresource getXnatPublicationresourcesByUri(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:publicationResource/uri",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPublicationresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	              int counter=0;
	              for(java.io.File f: this.getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("file/" + counter +"");
	                 rf.setXdatPath((counter++) +"");
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	return _return;
}
}
