//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 26, 2004
 */
package org.nrg.xft;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.commons.collections.Predicate;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.om.XdatUserI;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xft.TypeConverter.JavaMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.collections.ItemTrackingCollection;
import org.nrg.xft.compare.ItemEqualityI;
import org.nrg.xft.compare.ItemPKEquality;
import org.nrg.xft.compare.ItemUniqueEquality;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.db.loaders.XFTItemDBLoader;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.InvalidReference;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.MetaDataException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.ItemHtmlBuilder;
import org.nrg.xft.presentation.ItemMerger;
import org.nrg.xft.presentation.ItemPropBuilder;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.ItemSearch.IdentifierResults;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.VelocityUtils;
import org.nrg.xft.utils.XMLUtils;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
/***
 * This is a generic element used to store and manage data in XFT.
 * Each item corresponds to an data type in the schema and to a table in the database.
 *
 * <BR><BR>An item has a name (XSIType) (which specifies the XML Full Name of the element from
 * the schema which this item represents) and a Hashtable of properties
 * (which are stored using the sql-name of the property as corresponds to the database table).
 * XFTItems can contain other XFTItems in their properties hashtable when
 * references are defined for that data type in the schema.
 *
 * @author Tim
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class XFTItem extends GenericItemObject implements ItemI,Cloneable  {
	public static final String EQUALS = "=";
	private static final String COLON = ":";
	private static final String STATUS_STRING = "status";
	private static final String META_DATA_ID = "meta_data_id";
	private static final String _META_DATA___META_DATA_ID = "_meta_data/meta_data_id";
	private static final String SHAREABLE = "shareable";
	private static final String INSERT_DATE = "insert_date";
	private static final String META = "meta";
	private static final String _STATUS = "/status";
	private static final String _META_DATA_META_DATA_ID = "_meta_data.meta_data_id";
	private static final String XDAT_META_ELEMENT = "xdat:meta_element";
	private static final String STATUS = _STATUS;
	private static final String META_STATUS = "meta/status";
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTItem.class);
	private static Hashtable PRE_FORMATTED_ITEMS = new Hashtable();
	public static Comparator COMPARATOR= null;
	public final static String EXTENDED_FIELD_NAME = "extension";
	public final static String EXTENDED_ITEM = "extension_item";

	public final static TypeConverter JAVA_CONVERTER= new TypeConverter(new JavaMapping("xs"));

	private String xmlType = "";
	private GenericWrapperElement element = null;
	private ArrayList<String> keyNames = null;

	private Hashtable fieldIds =  null;//DATA,SINGLE,MULTI
	private Hashtable xmlFieldNames = null;
	private ArrayList allXmlFieldNames = null;

	private ArrayList postLoaded = new ArrayList();

	private ItemI parent = null;
	private String idFieldName = null;

	private ItemI meta = null;
	private ItemCollection history = new ItemCollection();

	private ValidationResults validationResults = null;

	private boolean loading = true;
	private boolean preLoaded = false;
	private boolean pauseDBAccess = false;

	private UserI user = null;

	private boolean verifyXMLPaths = false;

    public boolean modified =false;
    public boolean child_modified =false;
	/**
	 *
	 */
	public XFTItem(){}

	/**
	 * @param e
	 */
	private XFTItem(GenericWrapperElement e) {
		this.setElement(e);
	}

	/**
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public void assignDefaultValues() throws XFTInitException,ElementNotFoundException
	{
		for (final Object[] fieldInfo: this.getPossibleFieldNames())
		{
			final GenericWrapperField f = (GenericWrapperField)fieldInfo[3];
			if (! f.getDefaultValue().equalsIgnoreCase(""))
			{
				if (getProps().get((String)fieldInfo[0]) == null)
				{
					setField((String)fieldInfo[0],f.getDefaultValue());
				}
			}
		}
	}

	/**
	 * @return
	 */
	public static XFTItem NewEmptyItem(UserI user)
	{
	    XFTItem item = new XFTItem();
	    item.setUser(user);
		return item;
	}

	/**
	 * @param e
	 * @param hash
	 * @param throwException
	 * @return
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 * @throws InvalidValueException
	 */
	public static XFTItem NewItem(GenericWrapperElement e, Map hash, boolean throwException, UserI user) throws ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
		XFTItem item = NewItem(e,user);
		item.setProperties(hash,throwException);
		return item;
	}

	/**
	 * @param n  elementName
	 * @param hash  dot-sytax properties
	 * @param throwException
	 * @return
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 * @throws InvalidValueException
	 */
	public static XFTItem NewItem(String n, Map hash, boolean throwException, UserI user) throws ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
		try {
			XFTItem item = NewItem(n,user);
			item.setProperties(hash,throwException);
			return item;
		} catch (XFTInitException e) {
			return null;
		}
	}

	/**
	 * @param n elementName
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static XFTItem NewItem(String n, UserI user) throws XFTInitException,ElementNotFoundException
	{
		GenericWrapperElement e = GenericWrapperElement.GetElement(n);
		return NewItem(e,user);
	}

	/**
	 * @param e
	 * @return
	 */
	public static XFTItem NewItem(GenericWrapperElement e,UserI user)
	{
		return NewPreFormattedItem(e,user);
	}

	/**
	 * @param user
	 * @param name
	 * @return
	 */
	public static XFTItem NewMetaDataElement(UserI user, String name, boolean quarantine, Date insert_date, Object xft_version)
	{
		try {
			GenericWrapperElement e = GenericWrapperElement.GetElement(name);
			XFTItem meta = XFTItem.NewItem(e.getFullXMLName() +"_meta_data",user);
			//meta.setField("meta_element_xdat_meta_element_id",XFTMetaManager.GetElementID(GenericWrapperElement.GetElement(name)));

			if (user != null)
			{
				meta.setDirectProperty("insert_user_xdat_user_id",user.getID());
			}
			meta.setDirectProperty(INSERT_DATE,insert_date);
			meta.setDirectProperty("row_last_modified",insert_date);
			if(xft_version!=null)
				meta.setDirectProperty("xft_version",xft_version);
			meta.setDirectProperty(SHAREABLE,"1");

			if (quarantine)
			    meta.setDirectProperty(STATUS_STRING,ViewManager.QUARANTINE);
			else
			{
			    meta.setDirectProperty(STATUS_STRING,ViewManager.ACTIVE);
				if (user != null)
				{
					meta.setDirectProperty("activation_user_xdat_user_id",user.getID());
				}
				meta.setDirectProperty("activation_date",Calendar.getInstance().getTime());
			}
			return meta;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param e
	 * @return
	 */
	private static XFTItem NewPreFormattedItem(GenericWrapperElement e, UserI user)
	{
	   // org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE:: CREATE ITEM:: NEW ITEM::1");
		if (PRE_FORMATTED_ITEMS.get(e.getFullXMLName())==null)
		{
			XFTItem item = new XFTItem(e);
			try {
				item.getFieldIds();
				item.getXmlFieldNames();
			} catch (XFTInitException e1) {
				e1.printStackTrace();
			} catch (ElementNotFoundException e1) {
				e1.printStackTrace();
			}
			PRE_FORMATTED_ITEMS.put(e.getFullXMLName(),item);
		}

		XFTItem item = (XFTItem)PRE_FORMATTED_ITEMS.get(e.getFullXMLName());
		XFTItem temp = (XFTItem)item.cloneFormat();
		temp.setUser(user);
	   // org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE:: CREATE ITEM:: NEW ITEM::2");
		return temp;
	}

	/**
	 * @param name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public static XFTItem NewPrePopulatedItem(String name, UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    GenericWrapperElement gwe = GenericWrapperElement.GetElement(name);
		return NewPreFormattedItem(gwe,user);
	}
//
//	/**
//	 * @param allowRootRefs
//	 * @param history
//	 * @throws XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws FieldNotFoundException
//	 */
//	public void populateEmptyItems(boolean allowRootRefs,ArrayList history) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
//	{
//		ArrayList localHistory = (ArrayList)history.clone();
//		GenericWrapperElement e = this.getGenericSchemaElement();
//		localHistory.add(e.getFullXMLName());
//		Iterator refs = e.getReferenceFieldsWXMLDisplay(true, true).iterator();
//		while (refs.hasNext()) {
//			GenericWrapperField field = (GenericWrapperField) refs.next();
//			if (field.isReference()) {
//				if (allowRootRefs || field.getXMLDisplay().equalsIgnoreCase("always"))
//				{
//					GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
//					if (!localHistory.contains(ref.getFullXMLName()))
//					{
//						if (e.getAddin().equalsIgnoreCase("") || !ref.getAddin().equalsIgnoreCase(""))
//						{
//							XFTItem item = NewItem(ref);
//							if (field.isMultiple())
//							{
//								setField(field.getId()+ "0",item);
//							}else{
//								setField(field.getId(),item);
//							}
//							item.setParent(this);
//							item.populateEmptyItems(false,localHistory);
//						}
//					}
//				}
//			}
//		}
//	}


	/**
	 * Removes child items which have no properties set.
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public void removeEmptyItems() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{		
		GenericWrapperElement e = this.getGenericSchemaElement();
		Iterator refs = e.getReferenceFieldsWXMLDisplay(true, true).iterator();
		while (refs.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) refs.next();
			if (field.isReference()) {
				GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
				if (e.getAddin().equalsIgnoreCase("") || !ref.getAddin().equalsIgnoreCase(""))
				{
					if (field.isMultiple())
					{
						ArrayList children = getChildItems(field,false,false,this.getUser(),false,null);
						if (children.size()> 0)
						{
						    Iterator iter = children.iterator();
						    int counter = 0;
						    boolean adjustCount = false;
						    int removedItems = 0;
						    while (iter.hasNext())
						    {
						        XFTItem sub = (XFTItem)iter.next();
						        sub.removeEmptyItems();
						        if(sub.hasProperties())
						        {
						            if (!adjustCount)
							        {

							        }else{
							            Object o = props.remove(field.getId() + counter);
							            props.put((field.getId() + (counter-removedItems)).intern(),o);
							        }
						        }else{
								    props.remove(field.getId() + counter);
						            adjustCount=true;
						            removedItems++;
						        }
						        counter++;
						    }
						}
					}else{
						XFTItem sub = (XFTItem)getField(field.getId());
						if (sub != null){
							sub.removeEmptyItems();
							
							if (! sub.hasProperties())
							{
								props.remove(field.getId());
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Returns all possible field ids for this element and its extensions (with DATA,SINGLE,MULTI as values).
	 * @return Returns the fieldIds.
	 */
	public Hashtable getFieldIds() throws XFTInitException,ElementNotFoundException {
		if (fieldIds ==null)
		{
			fieldIds = this.getGenericSchemaElement().getAllFieldIDs();
		}

		return fieldIds;
	}
	/**
	 * @return Returns the xmlFieldNames.
	 */
	public Hashtable getXmlFieldNames() throws XFTInitException,ElementNotFoundException {
		if (xmlFieldNames == null)
		{
			xmlFieldNames = this.getGenericSchemaElement().getAllPossibleXMLFieldNames();
		}
		return xmlFieldNames;
	}

	/**
	 * @param fieldIds The fieldIds to set.
	 */
	public void setFieldIds(Hashtable fieldIds) {
		this.fieldIds = fieldIds;
	}
	/**
	 * @param xmlFieldNames The xmlFieldNames to set.
	 */
	public void setXmlFieldNames(Hashtable xmlFieldNames) {
		this.xmlFieldNames = xmlFieldNames;
	}

	/**
	 * Full XML Type name
	 * @return
	 */
	public String getXSIType() {
		return xmlType;
	}

    public boolean matchXSIType(String t){
        if (this.getXSIType().equalsIgnoreCase(t)){
            return true;
        }else{
            try {
                Iterator iter = getGenericSchemaElement().getExtendedElements().iterator();
                while (iter.hasNext()){
                    ArrayList sub = (ArrayList)iter.next();
                    GenericWrapperElement foreign = (GenericWrapperElement)sub.get(0);
                    if (foreign.getXSIType().equalsIgnoreCase(t)){
                        return true;
                    }
                }
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
        }

        return false;
    }

	/**
	 * If this element has a stored ProperName in the XFTReferenceManager's
	 * proper names hashtable.
	 * @return
	 * @throws ElementNotFoundException
	 */
	public String getProperName() throws ElementNotFoundException
	{
		String proper =  XFTReferenceManager.GetProperName(getXSIType());
		if (proper == null || proper.equalsIgnoreCase(""))
		{
			return getXSIType();
		}else
		{
			return proper;
		}
	}

	/**
	 * @return
	 * @throws ElementNotFoundException
	 */
	public String getFileName() throws ElementNotFoundException
	{
		String proper =  XFTReferenceManager.GetProperName(getXSIType());
		if (proper == null || proper.equalsIgnoreCase(""))
		{
			return this.getGenericSchemaElement().getFormattedName();
		}else
		{
			return proper;
		}
	}

	/**
	 * @return
	 * @throws ElementNotFoundException
	 */
	public String getUniqueFileName() throws ElementNotFoundException
	{
		String proper =  XFTReferenceManager.GetProperName(getXSIType());
		if (proper == null || proper.equalsIgnoreCase(""))
		{
			proper = this.getGenericSchemaElement().getFormattedName();
		}

		try {
            Iterator pks = getPkNames().iterator();
            while (pks.hasNext())
            {
            	String pkName = (String)pks.next();
            	if (getProperty(pkName) != null)
            	{
            		proper += "_" + getProperty(pkName).toString();
            	}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }

		return proper;
	}


	/**
	 * Full XML Type name
	 * @param string
	 */
	public void setXmlType(String string) {
		xmlType = string;
	}

	/**
	 * IF the corresponding element has a field of type ':ID' then that field's name is returned,
	 * ELSE an empty string is returned.
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private String getIdFieldName() throws XFTInitException,ElementNotFoundException
	{
		if (this.idFieldName == null)
		{
			Iterator all = getPossibleFieldNames().iterator();
			while (all.hasNext())
			{
				Object[] fieldInfo = (Object [])all.next();
				if (org.nrg.xft.schema.XMLType.CleanType((String)fieldInfo[1]).equalsIgnoreCase("ID"))
				{
					idFieldName = (String)fieldInfo[0];
					break;
				}
			}

			if (idFieldName== null)
			{
				idFieldName="";
			}
		}
		return idFieldName;
	}

	/**
	 * If the corresponding element has a field of type ':ID'
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public boolean hasIDField() throws XFTInitException,ElementNotFoundException
	{
		if (getIdFieldName().equalsIgnoreCase(""))
		{
			return false;
		}else
		{
			return true;
		}
	}

	/**
	 * IF the coresponding element has an ':ID' field, then that field's value is returned.
	 * Otherwise, null is returned.
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public String getIDValue()  throws XFTInitException,ElementNotFoundException
	{
		if (! getIdFieldName().equalsIgnoreCase(""))
		{
			return (String)getField(getIdFieldName());
		}
		return null;
	}

	/**
	 * IF the corresponding element has any fields of type ':IDREF' then those field's names are returned,
	 * ELSE an empty ArrayList is returned.
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private ArrayList getIDREFFieldNames() throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		Iterator all = getPossibleFieldNames().iterator();
		while (all.hasNext())
		{
			Object[] fieldInfo = (Object [])all.next();
			if (org.nrg.xft.schema.XMLType.CleanType((String)fieldInfo[1]).equalsIgnoreCase("IDREF"))
			{
				al.add(fieldInfo[0]);
			}
		}
		al.trimToSize();
		return al;
	}

	/**
	 * IF the coresponding element has any ':IDREF' fields, then those field's values are returned.
	 * Otherwise, an empty hashtable is returned.
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public Hashtable getIDREFs() throws XFTInitException,ElementNotFoundException
	{
		Hashtable hash = new Hashtable();
		Iterator iter = this.getIDREFFieldNames().iterator();
		while (iter.hasNext())
		{
			String fieldName = (String)iter.next();
			if (getField(fieldName) != null)
			{
				hash.put(fieldName,getField(fieldName));
			}
		}
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
	    Document doc = toXML_Item();
		return XMLUtils.DOMToString(doc);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toXML_HTML()
	{
		try {
            Document doc = XMLWriter.ItemToDOM(this,true,false);
            return XMLUtils.DOMToHTML(doc);
        } catch (Exception e) {
            logger.error("",e);
            Document doc = toXML_Item();
            return XMLUtils.DOMToString(doc);
        }
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toXML_String()
	{
		try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.toXML(baos, true);

            String s=  baos.toString();
            s = s.replace('\n',' ');
            s = s.replace('\r',' ');
            return  s;
        } catch (Exception e) {
            logger.error("",e);
            Document doc = toXML_Item();
            return XMLUtils.DOMToString(doc);
        }
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toXML_String(boolean allowSchemaLocation)
	{
		try {
            Document doc = XMLWriter.ItemToDOM(this,allowSchemaLocation,false);
            return XMLUtils.DOMToString(doc);
        } catch (Exception e) {
            logger.error("",e);
            Document doc = toXML_Item();
            return XMLUtils.DOMToString(doc);
        }
	}

	/**
	 * Translates the XFTItem to a simple XML DOM node.
	 * @param doc
	 * @return
	 */
	public Node toXML(Document doc)
	{
		Node main = doc.createElement("XFTItem");
		Node attr = doc.createAttribute("name");
		attr.setNodeValue(this.getXSIType());
		main.getAttributes().setNamedItem(attr);

        TreeMap ordered = new TreeMap();
        ordered.putAll(props);

        TreeMap items = new TreeMap();
		Iterator enumer = ordered.keySet().iterator();
		while (enumer.hasNext())
		{
			String key = enumer.next().toString();
			Object o = this.getField(key);
			if (o instanceof XFTItem)
			{
                items.put(key, o);
			}else
			{
				Node child = doc.createElement(key);
				Node text = doc.createTextNode(o.toString());
				child.appendChild(text);
				main.appendChild(child);
			}
		}

        Iterator iter = items.keySet().iterator();
        while(iter.hasNext())
        {
            String key = (String)iter.next();
            XFTItem o = (XFTItem)items.get(key);
            Node wrapper = doc.createElement(key);
            Node child = o.toXML(doc);
            wrapper.appendChild(child);
            main.appendChild(wrapper);
        }

		if (history.size()> 0)
		{
			Node historyWrap = doc.createElement("history");
			Iterator iter2 = history.getItemIterator();
			while (iter2.hasNext())
			{
				XFTItem hist = (XFTItem)iter2.next();
				Node child = hist.toXML(doc);
				historyWrap.appendChild(child);
			}
			main.appendChild(historyWrap);
		}

		return main;
	}

	/**
	 * Translates the XFTItem to a simple XML DOM document.
	 * @return
	 */
	public Document toXML() throws Exception
	{
		return XMLWriter.ItemToDOM(this,true,false);
	}

	/**
	 * Translates the XFTItem to a simple XML DOM document.
	 * @return
	 */
	public Document toXML(boolean allowSchemaLocation) throws Exception
	{
		return XMLWriter.ItemToDOM(this,allowSchemaLocation,false);
	}

	/**
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public Document toWebXML(String location,boolean limited) throws Exception
	{
		return XMLWriter.ItemToDOM(this,true,location,limited);
	}

	/**
	 * Outputs a debugging version of the XFTItem into XML format.
	 * @return
	 */
	public Document toXML_Item()
	{
		XMLWriter writer = new XMLWriter();
		Document doc =writer.getDocument();
		Node main = toXML(doc);
		doc.appendChild(main);
		return doc;
	}

	/**
	 * Uses reflection to return a field from a sub item.
	 * @param sub Item.subItem.itemToReturn
	 * @param key
	 * @return
	 */
	public Object findSubValue(String sub, String key)
	{
	    sub = StringUtils.StandardizeXMLPath(sub);
		if (sub.indexOf(XFT.PATH_SEPERATOR) != -1)
		{
			String current = sub.substring(0,sub.indexOf(XFT.PATH_SEPERATOR));
			String theRest = sub.substring(sub.indexOf(XFT.PATH_SEPERATOR) + 1);
			Object o = getField(current);
			if (o != null)
			{
				if (o instanceof XFTItem)
				{
					return ((XFTItem)o).findSubValue(theRest,key);
				}
			}
		}else
		{
			Object o = getField(sub);
			if (o != null)
			{
				if (o instanceof XFTItem)
				{
					return ((XFTItem)o).getField(key);
				}
			}
		}
		return null;
	}

	/**
	 * Searches for a field in this item and its extensions. (null if not found)
	 * @param key sql_name
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public Object findValue(String key)  throws ElementNotFoundException,XFTInitException
	{
		Object value = props.get(key);
		if (value == null)
		{
			if (getGenericSchemaElement().isExtension())
			{
				XFTItem extension = (XFTItem)getField(getGenericSchemaElement().getExtensionFieldName().toLowerCase());
				if (extension != null)
				{
					value = extension.findValue(key);
				}
			}
		}
		return value;
	}

	/**
	 * Populates any referenced items.
	 * @return
	 */
	public ItemI populateRefItems() throws Exception
	{
		try {
			getGenericSchemaElement();

			Iterator iter = element.getReferenceFields(false).iterator();
			while (iter.hasNext())
			{
				GenericWrapperField field = (GenericWrapperField)iter.next();
				if (! field.isMultiple())
				{
					Object o = props.get(field.getName().toLowerCase());
					if (o == null)
					{
						Iterator refs = field.getLocalRefNames().iterator();
						ItemSearch search = new ItemSearch();
						GenericWrapperElement foreign = null;
						while(refs.hasNext())
						{
							ArrayList refMapping = (ArrayList)refs.next();
							o = props.get((String)refMapping.get(0));
							if (o != null)
							{
								foreign = (GenericWrapperElement)refMapping.get(2);
								GenericWrapperField foreignKey = (GenericWrapperField)refMapping.get(1);
								SearchCriteria c = new SearchCriteria(foreignKey,props.get(refMapping.get(0)));
								search.setElement(foreign);
								search.addCriteria(c);
							}
						}
						try {
							ItemCollection items = search.exec(true);
							if (items.size() > 1)
							{
								for (int i =0;i<items.size();i++)
								{
									ItemI temp = ((XFTItem)items.get(i)).populateRefItems();
									this.setChild(field,temp,true);
									temp.setParent(this);
								}
							}else
							{
								ItemI temp = ((XFTItem)items.get(0)).populateRefItems();
								this.setChild(field,((XFTItem)items.get(0)).populateRefItems(),true);
								temp.setParent(this);
							}
						} catch (org.nrg.xft.exception.XFTInitException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * Populates Child items.
	 * @return
	 */
	public ItemI populateChildItems(ArrayList parents, boolean rePopulateRefs) throws org.nrg.xft.exception.XFTInitException
	{
		try {
			Iterator iter = getGenericSchemaElement().getAllFieldsWAddIns(false,true).iterator();
			while (iter.hasNext())
			{
				ArrayList subParents = null;
				GenericWrapperField field = (GenericWrapperField)iter.next();
				if (parents == null)
				{
					subParents = new ArrayList();
				}else
				{
					subParents = (ArrayList)parents.clone();
				}

				if (! subParents.contains(field.getXMLType().getLocalType()))
				{
					if (field.isReference())
					{
						if (field.isMultiple())
						{
							try {
								XFTReferenceI xftRef = field.getXFTReference();
								String key = "";
								ItemCollection items = new ItemCollection();

								if (xftRef.isManyToMany())
								{
									XFTManyToManyReference manyRef = (XFTManyToManyReference)xftRef;

									CriteriaCollection keys= new CriteriaCollection("AND");
									Iterator mappingCols = manyRef.getMappingColumns().iterator();
									while (mappingCols.hasNext())
									{
										XFTMappingColumn mapCol = ((XFTMappingColumn)mappingCols.next());
										if (((String)mapCol.getForeignElement().getFormattedName()).equalsIgnoreCase(getGenericSchemaElement().getFormattedName()))
										{
											SearchCriteria c = new SearchCriteria();
											c.setField_name(manyRef.getMappingTable() + "." + mapCol.getForeignKey().getSQLName());
											c.setValue(getField((String)mapCol.getLocalSqlName()));
											c.setCleanedType(mapCol.getXmlType().getLocalType());
											keys.addClause(c);
										}
									}
									String login = null;
									if (user != null)
									{
									    login = user.getUsername();
									}

									XFTTable table = TableSearch.GetMappingTable(manyRef,keys,login);

									GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();

									while (table.hasMoreRows())
									{
										table.nextRow();

										ArrayList extensions = XFTPseudonymManager.GetExtensionElements(foreign.getFullXMLName());
										if (extensions.size() > 0)
										{
											ArrayList checked = new ArrayList();
											String tempKey = "";
											Iterator extensionIter = extensions.iterator();
											while (extensionIter.hasNext())
											{
												try{
													GenericWrapperElement extensionElement = (GenericWrapperElement)extensionIter.next();
													if (extensionElement.getFullXMLName().equals(foreign.getFullXMLName()) && (extensions.size() > 1))
													{
														//SKIP ELEMENT... Its extensions are specified.
													}else{
														ItemSearch search = new ItemSearch();

														mappingCols = manyRef.getMappingColumns().iterator();
														while (mappingCols.hasNext())
														{
															XFTMappingColumn mapCol = ((XFTMappingColumn)mappingCols.next());
															if (((String)mapCol.getForeignElement().getFormattedName()).equalsIgnoreCase(foreign.getFormattedName()))
															{
																SearchCriteria c = new SearchCriteria();
																c.setFieldWXMLPath(extensionElement.getFullXMLName() + XFT.PATH_SEPERATOR  +mapCol.getForeignKey().getSQLName());
																c.setValue(table.getCellValue(mapCol.getLocalSqlName()));
																c.setCleanedType(mapCol.getXmlType().getLocalType());
																search.addCriteria(c);

																tempKey = mapCol.getLocalSqlName();
															}
														}
														search.setElement(extensionElement);
														items = search.exec(true);

														Iterator subItemIter = items.iterator();
														while (subItemIter.hasNext())
														{
															XFTItem subItem = (XFTItem)subItemIter.next();
															subItem.populateChildItems(subParents,rePopulateRefs);

															this.setChild(field,subItem,true);
															checked.add(subItem.getField(tempKey));
														}
													}
												}catch(ElementNotFoundException e)
												{

												}
											}

//											CHECK TO SEE THAT ALL ROOT ELEMENTS WHERE FOUND IN EXTENSIONS
											ItemSearch search = new ItemSearch();

										   	mappingCols = manyRef.getMappingColumns().iterator();
										   	while (mappingCols.hasNext())
										   	{
											   	XFTMappingColumn mapCol = ((XFTMappingColumn)mappingCols.next());
											   	if (((String)mapCol.getForeignElement().getFormattedName()).equalsIgnoreCase(foreign.getFormattedName()))
											   	{
													   SearchCriteria c = new SearchCriteria();
													   c.setField_name(foreign.getSQLName() + "." + mapCol.getForeignKey().getSQLName());
													   c.setValue(table.getCellValue(mapCol.getLocalSqlName()));
													   c.setCleanedType(mapCol.getXmlType().getLocalType());
													   search.add(c);
											   	}
										   	}
										   	search.setElement(foreign);
											items = search.exec(true);
										  	if (items.size() != checked.size())
											{
											   	Iterator subItemIter = items.iterator();
											   	while (subItemIter.hasNext())
											   	{
												   XFTItem subItem = (XFTItem)subItemIter.next();
												  	Object comparison = subItem.getField(tempKey);
												  	if (! checked.contains(comparison))
												  	{
														subItem.populateChildItems(subParents,rePopulateRefs);
														this.setChild(field,subItem,true);
												  	}
											   	}
											}
										}else
										{
											ItemSearch search = new ItemSearch();

											mappingCols = manyRef.getMappingColumns().iterator();
											while (mappingCols.hasNext())
											{
												XFTMappingColumn mapCol = ((XFTMappingColumn)mappingCols.next());
												if (((String)mapCol.getForeignElement().getFormattedName()).equalsIgnoreCase(foreign.getFormattedName()))
												{
													SearchCriteria c = new SearchCriteria();
													c.setField_name(foreign.getSQLName() + "." + mapCol.getForeignKey().getSQLName());
													c.setValue(table.getCellValue(mapCol.getLocalSqlName()));
													c.setCleanedType(mapCol.getXmlType().getLocalType());
													search.add(c);
												}
											}
											search.setElement(foreign);
											items = search.exec(true);

											Iterator subItemIter = items.iterator();
											while (subItemIter.hasNext())
											{
												XFTItem subItem = (XFTItem)subItemIter.next();
												subItem.populateChildItems(subParents,rePopulateRefs);

												this.setChild(field,subItem,true);
											}
										}
									}
								}else
								{
									XFTSuperiorReference supRef = (XFTSuperiorReference)xftRef;

									ArrayList extensions = XFTPseudonymManager.GetExtensionElements(supRef.getSubordinateElementName());
									if (extensions.size() > 0)
									{
										ArrayList checked = new ArrayList();
										Iterator extensionIter = extensions.iterator();

										while (extensionIter.hasNext())
										{
											GenericWrapperElement extensionElement = (GenericWrapperElement)extensionIter.next();
											ItemSearch search = new ItemSearch();

											key = "";
											Iterator refs = supRef.getKeyRelations().iterator();
											while (refs.hasNext())
											{
												XFTRelationSpecification sub = (XFTRelationSpecification)refs.next();

												SearchCriteria c = new SearchCriteria();
												c.setField_name(extensionElement.getTableAndFieldGrandSQLForExtendedFieldSQLName(supRef.getSubordinateElementName(),sub.getLocalCol()));
												c.setValue(getField(sub.getForeignCol()));
												c.setCleanedType(sub.getSchemaType().getLocalType());
												search.add(c);

												key = sub.getForeignCol();
											}
											search.setElement(extensionElement);
											items = search.exec(true);

											Iterator itemIter = items.iterator();
											while (itemIter.hasNext())
											{
												XFTItem sub = (XFTItem)itemIter.next();
												sub.populateChildItems(subParents,rePopulateRefs);
												this.setChild(field,sub,true);
												checked.add(sub.getField(key));
											}
										}

										// CHECK TO SEE THAT ALL ROOT ELEMENTS WHERE FOUND IN EXTENSIONS
										ItemSearch search = new ItemSearch();

										Iterator refs = supRef.getKeyRelations().iterator();
										while (refs.hasNext())
										{
											XFTRelationSpecification sub = (XFTRelationSpecification)refs.next();

											SearchCriteria c = new SearchCriteria();
											c.setField_name(sub.getLocalTable() + "." + sub.getLocalCol());
											c.setValue(getField(sub.getForeignCol()));
											c.setCleanedType(sub.getSchemaType().getLocalType());
											search.add(c);
										}
										search.setElement(supRef.getSubordinateElement());
										items = search.exec(true);

										if (items.size() != checked.size())
										{
											Iterator itemIter = items.iterator();
											while (itemIter.hasNext())
											{
												XFTItem sub = (XFTItem)itemIter.next();
												Object comparison = sub.getField(key);
												if (! checked.contains(comparison))
												{
													sub.populateChildItems(subParents,rePopulateRefs);
													this.setChild(field,sub,true);
												}
											}
										}
									}else
									{
										ItemSearch search = new ItemSearch();

										Iterator refs = supRef.getKeyRelations().iterator();
										while (refs.hasNext())
										{
											XFTRelationSpecification sub = (XFTRelationSpecification)refs.next();

											SearchCriteria c = new SearchCriteria();
											c.setField_name(sub.getLocalTable() + "." + sub.getLocalCol());
											c.setValue(getField(sub.getForeignCol()));
											c.setCleanedType(sub.getSchemaType().getLocalType());
											search.add(c);
										}
										search.setElement(supRef.getSubordinateElement());
										items = search.exec(true);

										Iterator itemIter = items.iterator();
										while (itemIter.hasNext())
										{
											XFTItem sub = (XFTItem)itemIter.next();
											sub.populateChildItems(subParents,rePopulateRefs);
											this.setChild(field,sub,true);
										}
									}
								}
							} catch (XFTInitException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else
						{
							if (getField(field.getId()) != null)
							{
								XFTItem ref = (XFTItem)getField(field.getId());
								if (! subParents.contains(ref.getXSIType()))
								{
									subParents.add(ref.getXSIType());
									ref.populateChildItems(subParents,rePopulateRefs);
									this.setChild(field,ref,true);
								}
							}
						}
					}
				}
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * If this item, or any of its subItems can be extended to a higher type, then the extension is performed.
	 * @param parents
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws DBPoolException
	 * @throws java.sql.SQLException
	 * @throws Exception
	 */
	private void extendSubItems(ItemTrackingCollection history, boolean allowMultiples) throws XFTInitException,ElementNotFoundException,DBPoolException,java.sql.SQLException,Exception
	{
		history.AddItem(this);
		Iterator iter = this.getGenericSchemaElement().getReferenceFields(true).iterator();
		while (iter.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)iter.next();
			if (f.isMultiple())
			{
				GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();


				if (foreign.getFullXMLName().equalsIgnoreCase(this.getXSIType()) && f.getRelationType().equalsIgnoreCase("single"))
				{
					if (getChildItems(f,allowMultiples,false,user,false,null).size() > 0)
					{
						Iterator children = getChildItems(f,allowMultiples,false,user,false,null).iterator();
						while(children.hasNext())
						{
							XFTItem sub = (XFTItem)children.next();
							if (!history.contains(sub))
							{
								sub.extendSubItems(history,allowMultiples);
							}
						}
					}else{
						//CHECK FOR sub items
						GenericWrapperField primaryKey = (GenericWrapperField)getGenericSchemaElement().getAllPrimaryKeys().get(0);
						ItemSearch search = new ItemSearch();

						search.setUser(this.getUser());

						SearchCriteria c = new SearchCriteria();
						c.setFieldWXMLPath(foreign.getFullXMLName() + XFT.PATH_SEPERATOR + f.getSQLName() + "_" + primaryKey.getSQLName());
						c.setValue(this.getProperty(primaryKey.getXMLPathString(this.getGenericSchemaElement().getFullXMLName())));
						c.setCleanedType(primaryKey.getXMLType().getLocalType());
						search.add(c);
						search.setElement(foreign);
						try {
                            ItemCollection items = search.exec(allowMultiples,false);
                            if (items.size() > 0)
                            {
                            	Iterator newSubs = items.iterator();
                            	while (newSubs.hasNext())
                            	{
                            		XFTItem newSub = (XFTItem)newSubs.next();
                            		if (! history.contains(newSub))
                            		{
                            			newSub.extendSubItems(history,allowMultiples);
                            			this.setChild(f,newSub,true);
                            			newSub.setParent(this);
                            		}
                            	}
                            }
                        } catch (IllegalAccessException e) {
                            logger.error("",e);
                        }
					}
				}else if (foreign.isExtended())
				{
					GenericWrapperField foreignKey = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
					int childCounter =0;
					Iterator children = getChildItems(f,allowMultiples,false,user,false,null).iterator();
					while(children.hasNext())
					{
						XFTItem sub = (XFTItem)children.next();
						String extensionName = sub.getExtensionElement();
						if (extensionName != null)
						{
						    if (! extensionName.equalsIgnoreCase(sub.getXSIType()))
						    {
								GenericWrapperElement extensionElement = GenericWrapperElement.GetElement(extensionName);

								ItemSearch search = new ItemSearch();
								search.setUser(this.getUser());

								SearchCriteria c = new SearchCriteria();
								c.setFieldWXMLPath(foreignKey.getXMLPathString(extensionElement.getFullXMLName()));
								Object v = sub.getProperty(foreignKey.getXMLPathString(foreign.getFullXMLName()));
								c.setValue(v);
								c.setCleanedType(foreignKey.getXMLType().getLocalType());
								search.add(c);
								search.setElement(extensionElement);

								try {
	                                ItemCollection items = search.exec(allowMultiples,false);
	                                if (items.size() > 0)
	                                {
	                                	XFTItem newSub = (XFTItem)items.get(0);
	                                	newSub.extendSubItems(history,allowMultiples);
	                                	this.setChild(f,newSub,childCounter);
	                                	newSub.setParent(this);
	                                }
	                            } catch (IllegalAccessException e) {
	                                this.removeItem(sub);
	                            }
						    }
						}
						childCounter++;
					}
				}else{
					Iterator children = getChildItems(f,allowMultiples,false,user,false,null).iterator();
					while(children.hasNext())
					{
						XFTItem sub = (XFTItem)children.next();

						if (sub != null)
						{
							if (history.contains(sub))
							{
								props.remove(f.getId());
							}else{
								sub.extendSubItems(history,allowMultiples);
							}
						}
					}
				}
			}else{
				XFTItem sub = (XFTItem)getField(f.getId());

				if (sub != null)
				{
					if (!this.getGenericSchemaElement().getExtensionFieldName().equalsIgnoreCase(f.getName()))
					{
					    GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();

						if (foreign.isExtended())
						{
							GenericWrapperField foreignKey = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);

							String extensionName = sub.getExtensionElement();
							if (extensionName != null)
							{
							    if ((! extensionName.equalsIgnoreCase(sub.getXSIType())) && (!extensionName.equalsIgnoreCase(getXSIType())))
							    {
									GenericWrapperElement extensionElement = GenericWrapperElement.GetElement(extensionName);

									ItemSearch search = new ItemSearch();
									search.setUser(this.getUser());

									SearchCriteria c = new SearchCriteria();
									c.setFieldWXMLPath(foreignKey.getXMLPathString(extensionElement.getFullXMLName()));
									Object v = sub.getProperty(foreignKey.getXMLPathString(foreign.getFullXMLName()));
									c.setValue(v);
									c.setCleanedType(foreignKey.getXMLType().getLocalType());
									search.add(c);
									search.setElement(extensionElement);

									try {
		                                ItemCollection items = search.exec(allowMultiples,false);
		                                if (items.size() > 0)
		                                {
		                                	XFTItem newSub = (XFTItem)items.get(0);
		                                	newSub.extendSubItems(history,allowMultiples);
		                                	this.setChild(f,newSub,true);
		                                	newSub.setParent(this);
		                                }
		                            } catch (IllegalAccessException e) {
		                                this.removeItem(sub);
		                            }
							    }
							}
						}else{

							if (history.contains(sub) && f.getXMLDisplay().equalsIgnoreCase("root"))
							{
								props.remove(f.getId());
							}else{
								sub.extendSubItems(history,allowMultiples);
							}
						}
					}else{

						if (history.contains(sub) && f.getXMLDisplay().equalsIgnoreCase("root"))
						{
							props.remove(f.getId());
						}else{
							sub.extendSubItems(history,allowMultiples);
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#extend()
	 */
	public void extend(boolean allowMultiples) throws XFTInitException,ElementNotFoundException,DBPoolException,java.sql.SQLException,Exception
	{
		extendSubItems(new ItemTrackingCollection(),allowMultiples);
	}

	/**
	 * Returns the sql names of the pk fields for this item.
	 * @return ArrayList of strings
	 */
	public ArrayList<String> getPkNames() throws org.nrg.xft.exception.XFTInitException
	{
		if (keyNames == null)
		{
			keyNames = new ArrayList<String>();

			try {
				Iterator keys = getGenericSchemaElement().getAllPrimaryKeys().iterator();

				while (keys.hasNext())
				{
					keyNames.add(((GenericWrapperField)keys.next()).getId());
				}
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			}
		}

		return keyNames;
	}

	/**
	 * @param xmlPath
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 */
	public boolean isPKField(String xmlPath) throws org.nrg.xft.exception.XFTInitException
	{
		xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
	    boolean b= false;
	    try {
			Iterator keys = getGenericSchemaElement().getAllPrimaryKeys().iterator();

			while (keys.hasNext())
			{
				GenericWrapperField key = (GenericWrapperField)keys.next();
				String temp = key.getXMLPathString(getXSIType());
				if (temp.equalsIgnoreCase(xmlPath))
				{
				    return true;
				}
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	public boolean hasPK() throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		boolean hasPK = false;
		Iterator keys = getPkNames().iterator();
		while (keys.hasNext())
		{
			String key = (String)keys.next();
			if (getProperty(this.getXSIType() + XFT.PATH_SEPERATOR + key) != null)
			{
				hasPK = true;
				break;
			}else{
				hasPK = false;
				break;
			}
		}
		return hasPK;
	}

	/**
	 * Hashtable of primaryKeyName/value pairs
	 * @return
	 * @throws XFTInitException 
	 * @throws FieldNotFoundException 
	 * @throws ElementNotFoundException 
	 */
	public Map<String,Object> getPkValues() throws XFTInitException, ElementNotFoundException, FieldNotFoundException
	{
		Map<String,Object> hash = new Hashtable<String,Object>();
		Iterator iter = getPkNames().iterator();
		while (iter.hasNext())
		{
			String key = (String)iter.next();
			if (this.getProperty(this.getXSIType() + XFT.PATH_SEPERATOR + key) != null)
			{
				hash.put(key,this.getProperty(this.getXSIType() + XFT.PATH_SEPERATOR + key));
			}
		}
		return hash;
	}

	/**
	 * Returns the sql names of the pk fields for this item.
	 * @return ArrayList of strings
	 */
	public Map<GenericWrapperField,Object> getPkValuesWTypes() throws org.nrg.xft.exception.XFTInitException
	{
		final Hashtable<GenericWrapperField,Object> hash = new Hashtable<GenericWrapperField,Object>();

		try {
			for (GenericWrapperField gwf:getGenericSchemaElement().getAllPrimaryKeys())
			{
				final String fpath = this.getXSIType() + XFT.PATH_SEPERATOR +gwf.getId();
				if (this.getProperty(fpath) != null)
				{
					hash.put(gwf,this.getProperty(fpath));
				}
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (FieldNotFoundException e) {
			e.printStackTrace();
		}
		return hash;
	}

	/**
	 * @return
	 */
	public Object getPK()
	{
	    try {
	        if (this.hasProperties())
	        {
	            Map pks = getPkValues();
	            if (pks.size()>0)
	            {
	               return pks.values().toArray()[0];
	            }
	        }
        } catch (Exception e) {
            logger.error("",e);
        }

	    return null;
	}


	/**
	 * ArrayList of SearchCriteria of primaryKeyName/value pairs
	 * @return
	 */
	public CriteriaCollection getPkSearch(boolean withChildren) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException
	{
	    if (getGenericSchemaElement().isExtension())
        {
        	try {
                setExtenderName();
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }

	    if (!withChildren)
	    {
			CriteriaCollection search = new CriteriaCollection("AND");
			Iterator iter = this.getGenericSchemaElement().getAllPrimaryKeys().iterator();
			while (iter.hasNext())
			{
				GenericWrapperField key = (GenericWrapperField)iter.next();
				try {
                    SearchCriteria c = new SearchCriteria(key,this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName())));
                    search.add(c);
                } catch (Exception e) {
                    logger.error("",e);
                }
			}
			return search;
	    }else{
	        CriteriaCollection search = new CriteriaCollection("AND");
			Iterator iter = this.getGenericSchemaElement().getAllPrimaryKeys().iterator();
			while (iter.hasNext())
			{
				GenericWrapperField key = (GenericWrapperField)iter.next();
				SearchCriteria c = new SearchCriteria();
				try {
                    c.setFieldWXMLPath(key.getXMLPathString(key.getParentElement().getFullXMLName()));
                    c.setValue(this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName())));
                    search.add(c);
                } catch (Exception e) {
                    logger.error("",e);
                }
			}
			return search;
	    }
	}

	/**
	 * ArrayList of SearchCriteria of uniqueField/value pairs
	 * @return
	 */
	public CriteriaCollection getUniqueSearch() throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,Exception
	{
		CriteriaCollection search = new CriteriaCollection("OR");
		Iterator iter = this.getGenericSchemaElement().getUniqueFields().iterator();
		while (iter.hasNext())
		{
			GenericWrapperField key = (GenericWrapperField)iter.next();
			try {
                Object o = this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName()));
                if (o!= null)
                {
                	SearchCriteria c = new SearchCriteria(key,o);
                	search.addClause(c);
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            }
		}


		Hashtable uHash = this.getGenericSchemaElement().getUniqueCompositeFields();
		if (uHash.size() > 0)
		{
            CriteriaCollection ucs = new CriteriaCollection("OR");
            //MUST BE 'OR' to match database constraints
            //ALSO CONSTRAINTS with null values should not be checked.
		    Enumeration uHashEnum = uHash.keys();
		    while (uHashEnum.hasMoreElements())
		    {
		        String s = (String)uHashEnum.nextElement();
		        ArrayList uniqueComposites = (ArrayList)uHash.get(s);

		        boolean hasNULL = false;
				CriteriaCollection cc = new CriteriaCollection("AND");
				Iterator uCs = uniqueComposites.iterator();
				while (uCs.hasNext())
				{
					GenericWrapperField key = (GenericWrapperField)uCs.next();
					if (key.isReference())
					{
					    Iterator fields = key.getLocalRefNames().iterator();
					    while (fields.hasNext())
					    {
					        ArrayList field = (ArrayList)fields.next();

					        try {
                                Object o = this.getProperty(this.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + (String)field.get(0));
                                if (o!= null)
                                {
                                    try {
                                        SearchCriteria c = new SearchCriteria();
                                        c.setValue(o);
                                        c.setFieldWXMLPath(getXSIType() + XFT.PATH_SEPERATOR + (String)field.get(0));
                                        cc.addClause(c);
                                    } catch (Exception e) {
                                        logger.error("",e);
                                    }
                                }else{
                                    try {
                                        hasNULL=true;
//                                        SearchCriteria c = new SearchCriteria();
//                                        c.setValue("NULL");
//                                        c.setComparison_type(" IS ");
//                                        c.setFieldWXMLPath(getXSIType() + XFT.PATH_SEPERATOR + (String)field.get(0));
//                                        c.setOverrideFormatting(true);
//                                        cc.addClause(c);
                                    } catch (Exception e) {
                                        logger.error("",e);
                                    }
                                }
                            } catch (XFTInitException e) {
                                logger.error("",e);
                            } catch (ElementNotFoundException e) {
                                logger.error("",e);
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
					    }
					}else{
						try {
                            Object o = this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName()));
                            if (o!= null)
                            {
                            	SearchCriteria c = new SearchCriteria(key,o);
                            	cc.addClause(c);
                            }else{
                                hasNULL=true;
//                                SearchCriteria c = new SearchCriteria(key,"NULL");
//                                c.setComparison_type(" IS ");
//                                c.setOverrideFormatting(true);
//                            	cc.addClause(c);
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        } catch (FieldNotFoundException e) {
                            logger.error("",e);
                        }
					}
				}

                if (!hasNULL) ucs.add(cc);
		    }

            if (ucs.numClauses()>0)
            {
                search.add(ucs);
            }
		}

		return search;
	}

    /**
     * @return
     * @throws org.nrg.xft.exception.XFTInitException
     * @throws ElementNotFoundException
     */
    public boolean hasUniques() throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException
    {
        return hasUniques(false);
    }

	/**
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 * @throws ElementNotFoundException
	 */
	public boolean hasUniques(boolean checkExtensions) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException
	{
		if (this.getGenericSchemaElement().hasUniques())
		{
			Iterator iter = this.getGenericSchemaElement().getUniqueFields().iterator();
			while (iter.hasNext())
			{
				GenericWrapperField key = (GenericWrapperField)iter.next();
				try {
                    Object o = this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName()));
                    if (o!= null)
                    {
                    	return true;
                    }
                } catch (Exception e) {
                    logger.error("",e);
                }
			}

			boolean hasComposite = false;
			Hashtable uHash = this.getGenericSchemaElement().getUniqueCompositeFields();
			if (uHash.size() > 0)
			{
			    Enumeration uHashEnum = uHash.keys();
			    while (uHashEnum.hasMoreElements())
			    {
					hasComposite = false;
			        String s = (String)uHashEnum.nextElement();
			        ArrayList uniqueComposites = (ArrayList)uHash.get(s);
			        if (uniqueComposites.size() > 0)
					{
						hasComposite = true;
						Iterator uCs = uniqueComposites.iterator();
						while (uCs.hasNext())
						{
							GenericWrapperField key = (GenericWrapperField)uCs.next();
							try {
                                Object o = this.getProperty(key.getXMLPathString(this.getGenericSchemaElement().getFullXMLName()));
                                if (o== null)
                                {
                                    if (key.isReference())
                                    {
                                        XFTReferenceI ref = key.getXFTReference();
                                        if(ref.isManyToMany()){

                                        }else{
                                            XFTSuperiorReference supRef = (XFTSuperiorReference)ref;
                                            Iterator iter2 =supRef.getKeyRelations().iterator();
                                            while (iter2.hasNext())
                                            {
                                                XFTRelationSpecification spec = (XFTRelationSpecification)iter2.next();
                                               String localCol =  spec.getLocalCol();
                                               o =this.getProperty(localCol);
                                               if (o==null){
                                                   hasComposite=false;
                                                   break;
                                               }
                                            }
                                        }
                                    }
                                }
                                if (o== null){
                                    hasComposite = false;
                                    break;
                                }
                            } catch (Exception e) {
                                logger.error("",e);
                                hasComposite = false;
                            	break;
                            }
						}
					}
			        if (hasComposite)
			        {
			            break;
			        }
			    }
			}
			return hasComposite;
		}else{
            if (checkExtensions){
                if (getGenericSchemaElement().isExtension()){
                    try {
                        XFTItem child = this.getExtensionItem();
                        if (child!=null)
                        {
                            return child.hasUniques(checkExtensions);
                        }
                    } catch (FieldNotFoundException e) {
                        logger.error("",e);
                    }
                }
            }

			return false;
		}
	}

	/**
	 * Returns the GenericWrapperElement for this item from the schema
	 * @return
	 */
	public GenericWrapperElement getGenericSchemaElement() throws ElementNotFoundException
	{
		if (this.element == null)
		{
			try {
				element = GenericWrapperElement.GetElement(this.getXSIType());
			} catch (org.nrg.xft.exception.XFTInitException e) {
				logger.error("",e);
			}
		}
		return element;
	}

    public boolean instanceOf(String xsiType) throws ElementNotFoundException{
        return this.getGenericSchemaElement().instanceOf(xsiType);
    }

	/**
	 * returns the names of the possible fields for this item.
	 * <BR>0: sql name
	 * <BR>1: type
	 * <BR>2: xmlOnly (true | false)
	 * <BR>3: GenericWrapperField (Ref)
	 * @return ArrayList of Object[4]
	 */
	public ArrayList<Object[]> getPossibleFieldNames() throws ElementNotFoundException,XFTInitException
	{
		return this.getGenericSchemaElement().getAllFieldNames();
	}


	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getPossibleValues(java.lang.String)
	 */
	public ArrayList getPossibleValues(String xmlPath) throws Exception
	{
		return GenericWrapperElement.GetPossibleValues(xmlPath);
	}

    public ArrayList getPossibleValuesFromDB(String xmlPath)throws Exception{
        return GenericWrapperElement.GetUniqueValuesForField(xmlPath);
    }

    public ArrayList<XFTItem> getParents(String xsiType) throws ElementNotFoundException{
        ArrayList<XFTItem> parents = new ArrayList<XFTItem>();
        if (this.instanceOf(xsiType)){
            parents.add(this);
            return parents;
        }

        GenericWrapperElement gwe = this.getGenericSchemaElement();
        ArrayList<Object[]> al= gwe.getPossibleParents(true);

        for (Object[] o: al){
            GenericWrapperElement foreign = (GenericWrapperElement)o[0];
            String xmlPath = (String)o[1];

            try {
                ItemSearch is= ItemSearch.GetItemSearch(foreign.getFullXMLName(), user);
                for(GenericWrapperField key: this.getGenericSchemaElement().getAllPrimaryKeys()){
                    is.addCriteria(xmlPath + "/" + key.getName(), this.getProperty(key));
                }

                if (is.getCriteriaCollection().size()>0){
                    Iterator items= is.exec(false).iterator();
                    while(items.hasNext()){
                        XFTItem parent = (XFTItem)items.next();
                        parents.addAll(parent.getParents(xsiType));
                    }
                }
            } catch (Exception e) {
                logger.error("",e);
            }
        }

        return parents;
    }

	/**
	 * Sets the corresponding element for this item, and the item's name
	 * @param element
	 */
	public void setElement(GenericWrapperElement element)
	{
		this.element = element;
		this.xmlType = element.getFullXMLName();
	}

	/**
	 * Compares all properties including primary keys (if ignorePK=false) else compares all non
	 * primary key properties.
	 * @param item2 Item to compare
	 * @param ignorePK true to ignore primary keys in comparison
	 * @return
	 */
	public boolean equals(XFTItem item2, boolean ignorePK) throws org.nrg.xft.exception.XFTInitException
	{
		boolean eq = true;

		if (! this.xmlType.equalsIgnoreCase(item2.getXSIType()))
		{
			return false;
		}

		Hashtable hash1 = props;
		Hashtable hash2 = item2.getProps();

		Enumeration enumer = hash1.keys();
		while (enumer.hasMoreElements())
		{
			String field = (String)enumer.nextElement();
			if ((this.getPkNames().contains(field) && (!ignorePK)) || (! this.getPkNames().contains(field)))
			{
				Object value1 = hash1.get(field);
				if (value1 != null)
				{
					if (! value1.getClass().getName().equalsIgnoreCase("org.nrg.xft.XFTItem"))
					{
						Object value2 = hash2.get(field);
						if (value2 == null)
						{
							return false;
						}else
						{
							if (! value1.toString().equalsIgnoreCase(value2.toString()))
							{
								return false;
							}
						}
					}
				}else
				{
					//VALUE1 is null
					Object value2 = hash2.get(field);
					if (value2 != null)
					{
						return false;
					}
				}

			}
		}

		return eq;
	}

	public void importPK(XFTItem temp)
	{
	    try {
	        Hashtable pkHASH = (Hashtable)temp.getPkValues();
            Enumeration keys= pkHASH.keys();
            while (keys.hasMoreElements())
            {
                String s = (String)keys.nextElement();
                this.setProperty(s,pkHASH.get(s));
            }
        } catch (Exception e2) {
            logger.error("",e2);
        }
	}

	/**
	 * @param temp
	 */
	public void importNonItemFields(XFTItem temp,boolean onlyMetaFields)
	{
		importPK(temp);

		Enumeration enumer = temp.getProps().keys();
		while (enumer.hasMoreElements())
		{
			String field = (String)enumer.nextElement();
			Object o = temp.getProps().get(field);

			if (onlyMetaFields)
			{
				if (! (o instanceof XFTItem))
				{
				    try {
				        if (temp.getGenericSchemaElement().isHiddenFK(field))
					    {
				            if (!o.toString().equals(""))
                        	{
                        	    try {
                                    if (temp.getPkNames().contains(field))
                                    {
                                        getProps().put(field,o);
                                    }
                                } catch (XFTInitException e) {
                                    logger.error("",e);
                                }
                        	}
					    }else{
					        GenericWrapperField gwf= temp.getGenericSchemaElement().getField(field);
	                        if (gwf==null)
	                        {
	                        	if (!o.toString().equals(""))
	                        	{
	                        	    try {
	                                    if (temp.getPkNames().contains(field))
	                                    {
	                                        getProps().put(field,o);
	                                    }
	                                } catch (XFTInitException e) {
	                                    logger.error("",e);
	                                }
	                        	}
	                        }else if (gwf.isReference())
	                        {
	                            try {
	                                GenericWrapperElement e = (GenericWrapperElement)gwf.getReferenceElement();

	                                if (e.getAddin().equals(""))
	                                {
	                                	if (!o.toString().equals(""))
	                                	{
	                                	    try {
	                                            if (temp.getPkNames().contains(field))
	                                            {
	                                                getProps().put(field,o);
	                                            }
	                                        } catch (XFTInitException e1) {
	                                            logger.error("",e1);
	                                        }
	                                	}
	                                }else{
	                                    getProps().put(field,o);
	                                }
	                            } catch (XFTInitException e) {
	                                logger.error("",e);
	                            } catch (ElementNotFoundException e) {
	                                logger.error("",e);
	                            }
	                        }else{
	                            if (!o.toString().equals(""))
	                        	{
	                        	    try {
	                                    if (temp.getPkNames().contains(field))
	                                    {
	                                        getProps().put(field,o);
	                                    }
	                                } catch (XFTInitException e) {
	                                    logger.error("",e);
	                                }
	                        	}
	                        }
					    }
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    }
				}
			}else{
			    if (! (o instanceof XFTItem))
				{
				    getProps().put(field,o);
				}
			}
		}
	}

	/**
	 * returns the names of references elements
	 * @return ArrayList of Strings
	 */
	public ArrayList getRefNames() throws ElementNotFoundException,XFTInitException
	{
		ArrayList al = new ArrayList();
		Iterator iter = this.getGenericSchemaElement().getReferenceFieldsWAddIns().iterator();
		while (iter.hasNext())
		{
			GenericWrapperField field = (GenericWrapperField)iter.next();
			if (field.isReference())
			{
				al.add(field.getReferenceElementName());
			}
		}
		al.trimToSize();
		return al;
	}

	final static String META_ELEMENT=XDAT_META_ELEMENT;
	final static String META_ELEMENT_ID="xdat_meta_element_id";
	final static String META_ELEMENT_NAME="element_name";
	
	/**
	 * Returns ArrayList of XFTItems with matching pks. (DB ACCESS)
	 * @return
	 */
	public ItemCollection getPkMatches(boolean extend) throws DBPoolException,ElementNotFoundException,XFTInitException,java.sql.SQLException,FieldNotFoundException,Exception
	{
		//xdat:meta_element should be fully cached.  It isn't modified at any point.
		try {
			if(this.getXSIType().equals(META_ELEMENT)){
				final Object id=this.getField(META_ELEMENT_ID);
				if(id!=null){
					final ItemI i=(ItemI)CacheManager.GetInstance().retrieve(META_ELEMENT, id);
					if(i!=null){
						final ItemCollection ic = new ItemCollection();
						ic.add(i);
						return ic;
					}
				}
			}
		} catch (Throwable e1) {}
				
		ItemSearch search = new ItemSearch();
		search.setElement(getGenericSchemaElement());
		search.setCriteriaCollection(getPkSearch(false));
        search.setAllowMultipleMatches(false);
        
        List<List<IdentifierResults>> rows=new ArrayList<List<IdentifierResults>>();
        rows.add(new ArrayList<IdentifierResults>());
        
        Map<GenericWrapperField,Object> pks=getPkValuesWTypes();
        for(Map.Entry<GenericWrapperField, Object> entry:pks.entrySet()){
        	rows.get(0).add(new IdentifierResults(entry.getValue(),entry.getKey()));
        }
        
        try {
        	search.setAllowMultiples(false);
        	search.setExtend(extend);
        	ItemCollection items=search.getItemsFromKeys(rows, null);
        	
     //       ItemCollection items= search.exec(false,extend);
            
            if(this.getXSIType().equals(META_ELEMENT) && items.size()==1){
            	try {
            		ItemI i=items.get(0);
					CacheManager.GetInstance().put(META_ELEMENT, i.getProperty(META_ELEMENT_ID), i);
					CacheManager.GetInstance().put(META_ELEMENT, i.getProperty(META_ELEMENT_NAME), i);
				} catch (Throwable e) {}
				
            	return items;
            }else{
            	return items;
            }
        }catch (Exception e) {
            if (e instanceof ItemSearch.MultipleMatchException)
            {
                logger.error(e);
                return new ItemCollection();
            }else{
                throw e;
            }
        }
	}

	public ItemCollection getExtFieldsMatches(boolean includeParent) throws DBPoolException,ElementNotFoundException,XFTInitException,java.sql.SQLException,FieldNotFoundException,Exception
	{
        ItemSearch search = new ItemSearch();
	    try {
	        search = getFieldsMatchSearch(includeParent);
	            search.setAllowMultipleMatches(false);
	            search.setExtend(false);
	            search.setAllowMultiples(false);
	            List<List<IdentifierResults>> keys =  search.getIdentifiers();

	            if (keys.size()>1 || keys.size()==0)
	            {
		            return new ItemCollection();
	            }

	    	    String functionName= element.getTextFunctionName();
	    	    if ((!search.isExtend()) && (element.isExtended() && (!(element.getName().endsWith("meta_data") || element
	                    .getName().endsWith("history")))))
	    	    {
	    	        functionName= GenericWrapperUtils.TXT_EXT_FUNCTION + element.getFormattedName();
	    	    }

                String query = "SELECT " + functionName + "(";

                int count=0;
    		    for (IdentifierResults ir:keys.get(0))
    		    {
    		        if (count++>0){
                        query+=", ";
                    }
    		        query+=ir.getParsedValue();
    		    }
                query+=",0,FALSE,FALSE,FALSE)";

                String s =(String)PoolDBUtils.ReturnStatisticQuery(query,functionName,element.getDbName(),null);
                XFTItem item = XFTItem.PopulateItemFromFlatString(s,user);
                ItemCollection items = new ItemCollection();
                items.add(item);
                return items;
	        } catch (Exception e) {
	            logger.error(e);
	            return new ItemCollection();
	        }
	}




	/**
	 * ArrayList of SearchCriteria of primaryKeyName/value pairs
	 * @return
	 */
	public ItemSearch getFieldsMatchSearch(boolean includeParent) throws org.nrg.xft.exception.XFTInitException,ElementNotFoundException,Exception
	{
	    if (includeParent)
	    {
	        return this.getExtenderItem().getFieldsMatchSearch(false);
	    }else{

		    ItemSearch itemSearch= new ItemSearch();
		    itemSearch.setElement(getGenericSchemaElement());
			try {
	            CriteriaCollection search = new CriteriaCollection("AND");

	            Iterator fieldIter = this.getGenericSchemaElement().getAllFields(false, false).iterator();
	            while (fieldIter.hasNext()) {
	                GenericWrapperField field = (GenericWrapperField) fieldIter.next();
	                if (!field.isReference()) {
	                    if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
	            			try {
	            			    String xmlPath = field.getXMLPathString(this.getGenericSchemaElement().getFullXMLName());
	            			    Object v = this.getProperty(xmlPath);
	            			    if (v == null)
	            			    {
	            			        SearchCriteria c = new SearchCriteria();
	                                c.setValue("NULL");
	                                c.setComparison_type(" IS ");
	                                c.setFieldWXMLPath(xmlPath);
	                                c.setOverrideFormatting(true);
	                                search.add(c);
	            			    }else{
	                                SearchCriteria c = new SearchCriteria(field,v);
	                                search.add(c);
	            			    }
	                        } catch (Exception e) {
	                            logger.error("",e);
	                        }
	                    }
	                } else if (!field.isMultiple() && !field.getName().equalsIgnoreCase(META) && !field.getName().equalsIgnoreCase(this.getGenericSchemaElement().getExtensionFieldName())) {
	                    XFTSuperiorReference supRef = (XFTSuperiorReference) field
	                            .getXFTReference();
	                    if (supRef.getSubordinateElement().equals(this.getGenericSchemaElement())) {
	                        //INPUT has the fk column (check if it is null)
	                        Iterator refsCols = supRef.getKeyRelations().iterator();
	                        while (refsCols.hasNext()) {
	                            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();

	                            String xmlPath = this.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + spec.getLocalCol();
	            			    Object v = this.getProperty(xmlPath);
	            			    if (v == null)
	            			    {
	            			        SearchCriteria c = new SearchCriteria();
	                                c.setValue("NULL");
	                                c.setComparison_type(" IS ");
	                                c.setFieldWXMLPath(xmlPath);
	                                c.setOverrideFormatting(true);
	                                search.add(c);
	            			    }else{
	                		        SearchCriteria c = new SearchCriteria();
	                                c.setValue(v);
	                                c.setFieldWXMLPath(xmlPath);
	                                search.add(c);
	            			    }
	                        }
	                    }
	                }
	            }

	            Iterator iter2 = getGenericSchemaElement().getUndefinedReferences().iterator();
	            while (iter2.hasNext())
	            {
	                GenericWrapperField field = (GenericWrapperField)iter2.next();
	                if (field.isReference() && (!field.isMultiple()))
	                {
	                    XFTSuperiorReference supRef = (XFTSuperiorReference)field.getXFTReference();
	                    Iterator refsCols = supRef.getKeyRelations().iterator();
	                    while (refsCols.hasNext()) {
	                        XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
	                                .next();
	                        String xmlPath = this.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + spec.getLocalCol();
	            		    Object v = this.getProperty(xmlPath);
	            		    if (v == null)
	            		    {
	            		        //IGNORE
	            		    }else{
	            		        SearchCriteria c = new SearchCriteria();
	                            c.setValue(v);
	                            c.setFieldWXMLPath(xmlPath);
	                            search.add(c);
	            		    }

	                    }
	                }
	            }

	            //PROCESS EXTENSIONS
	            GenericWrapperElement extendedE = this.getGenericSchemaElement();
	            while (extendedE.isExtension()){
	                extendedE = GenericWrapperElement.GetElement(extendedE.getExtensionType());

	                fieldIter = extendedE.getAllFields(false, false).iterator();
	                while (fieldIter.hasNext()) {
	                    GenericWrapperField field = (GenericWrapperField) fieldIter.next();
	                    if (!field.isReference()) {

	                        if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
	            				try {
	            				    String xmlPath = field.getXMLPathString(this.getGenericSchemaElement().getFullXMLName());
	            				    Object v = this.getProperty(xmlPath);
	            				    if (v == null)
	            				    {
	            				        SearchCriteria c = new SearchCriteria();
	                                    c.setValue("NULL");
	                                    c.setComparison_type(" IS ");
	                                    c.setFieldWXMLPath(xmlPath);
	                                    c.setOverrideFormatting(true);
	                                    search.add(c);
	            				    }else{
	                    		        SearchCriteria c = new SearchCriteria();
	                                    c.setValue(v);
	                                    c.setFieldWXMLPath(xmlPath);
	                                    search.add(c);
	            				    }
	                            } catch (Exception e) {
	                                logger.error("",e);
	                            }
	                        }
	                    } else if (!field.isMultiple() && !field.getName().equalsIgnoreCase(META) && !field.getName().equalsIgnoreCase(this.getGenericSchemaElement().getExtensionFieldName())) {
	                        XFTSuperiorReference supRef = (XFTSuperiorReference) field
	                        .getXFTReference();
	                        if (supRef.getSubordinateElement().equals(extendedE)) {
	                            //INPUT has the fk column (check if it is null)
	                            Iterator refsCols = supRef.getKeyRelations().iterator();
	                            while (refsCols.hasNext()) {
	                                XFTRelationSpecification spec = (XFTRelationSpecification) refsCols.next();

	                                String xmlPath = this.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + spec.getLocalCol();
	            				    Object v = this.getProperty(xmlPath);
	            				    if (v == null)
	            				    {
	            				        SearchCriteria c = new SearchCriteria();
	                                    c.setValue("NULL");
	                                    c.setComparison_type(" IS ");
	                                    c.setFieldWXMLPath(xmlPath);
	                                    c.setOverrideFormatting(true);
	                                    search.add(c);
	            				    }else{
	                    		        SearchCriteria c = new SearchCriteria();
	                                    c.setValue(v);
	                                    c.setFieldWXMLPath(xmlPath);
	                                    search.add(c);
	            				    }
	                            }
	                        }
	                    }
	                }

	                iter2 = extendedE.getUndefinedReferences().iterator();
	                while (iter2.hasNext())
	                {
	                    GenericWrapperField field = (GenericWrapperField)iter2.next();
	                    if (field.isReference() && (!field.isMultiple()))
	                    {
	                        XFTSuperiorReference supRef = (XFTSuperiorReference)field.getXFTReference();
	                        Iterator refsCols = supRef.getKeyRelations().iterator();
	                        while (refsCols.hasNext()) {
	                            XFTRelationSpecification spec = (XFTRelationSpecification) refsCols
	                                    .next();
	                            String xmlPath = this.getGenericSchemaElement().getFullXMLName() + XFT.PATH_SEPERATOR + spec.getLocalCol();
	            			    Object v = this.getProperty(xmlPath);
	            			    if (v == null)
	            			    {
	            			        //IGNORE
	            			    }else{
	                		        SearchCriteria c = new SearchCriteria();
	                                c.setValue(v);
	                                c.setFieldWXMLPath(xmlPath);
	                                search.add(c);
	            			    }

	                        }
	                    }
	                }
	            }

	    	    itemSearch.setCriteriaCollection(search);
	            return itemSearch;
	        } catch (FieldNotFoundException e) {
	            logger.error(e);
	            throw new Exception(e.getMessage());
	        }
	    }
	}

	/**
	 * Returns ArrayList of XFTItems with matching unique fields. (DB ACCESS)
	 * @return
	 */
	public ItemCollection getUniqueMatches(boolean extend) throws DBPoolException,ElementNotFoundException,XFTInitException,java.sql.SQLException,FieldNotFoundException,Exception
	{
		try {
			if(this.getXSIType().equals(META_ELEMENT)){
				final Object id=this.getField(META_ELEMENT_NAME);
				if(id!=null){
					ItemI i=(ItemI)CacheManager.GetInstance().retrieve(META_ELEMENT, id);
					if(i!=null){
						ItemCollection ic = new ItemCollection();
						ic.add(i);
						return ic;
					}
				}
			}
		} catch (Throwable e1) {}
		
		if (this.getGenericSchemaElement().hasUniques())
		{
			ItemSearch search = new ItemSearch();
			search.setElement(getGenericSchemaElement());
			search.setCriteriaCollection(getUniqueSearch());
            search.setAllowMultipleMatches(false);
			try {
				ItemCollection items= search.exec(false,extend);
	            
	            if(this.getXSIType().equals(META_ELEMENT) && items.size()==1){
	            	try {
	            		ItemI i=items.get(0);
						CacheManager.GetInstance().put(META_ELEMENT, i.getProperty(META_ELEMENT_ID), i);
						CacheManager.GetInstance().put(META_ELEMENT, i.getProperty(META_ELEMENT_NAME), i);
					} catch (Throwable e) {}
					
	            	return items;
	            }else{
	            	return items;
	            }
            }catch (Exception e) {
                if (e instanceof ItemSearch.MultipleMatchException)
                {
                    logger.error(e);
                    return new ItemCollection();
                }else{
                    throw e;
                }
            }
		}
		return new ItemCollection();
	}

	/**
	 * Returns ArrayList of XFTItems where all fields match. (DB ACCESS)
	 * @return
	 */
	public ItemCollection getFieldMatches(boolean extend) throws DBPoolException,ElementNotFoundException,XFTInitException,java.sql.SQLException,FieldNotFoundException,Exception
	{
		ItemSearch search = new ItemSearch();
		Iterator fields = getGenericSchemaElement().getNonMultipleFields().iterator();
		while (fields.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)fields.next();
			if (f.isReference())
			{
				XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
				Iterator keys = ref.getKeyRelations().iterator();
				while (keys.hasNext())
				{
					XFTRelationSpecification spec = (XFTRelationSpecification)keys.next();
					if (getField(spec.getLocalCol()) != null)
					{
						SearchCriteria c = new SearchCriteria();
						c.setField_name(spec.getLocalCol());
						c.setCleanedType(spec.getSchemaType().getLocalType());
						c.setValue(getField(spec.getLocalCol()));
						search.add(c);
					}
				}
			}else
			{
				if (getField(f.getId()) != null)
				{
					SearchCriteria c = new SearchCriteria(f,getField(f.getId()));
					search.add(c);
				}
			}
		}
		search.setElement(getGenericSchemaElement());
		return search.exec(false,extend);
	}

	/**
	 * If the corresponding element for this item stipulates that this item (or any of its extensions)
	 * can have a field of this fieldName, then it is added to the properties.
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public boolean setFieldValue(String fieldName,Object value) throws ElementNotFoundException,XFTInitException
	{
		GenericWrapperElement element = getGenericSchemaElement();
		boolean set = false;
		try {
			if (hasLocalField(fieldName.toLowerCase()))
			{
				setField(fieldName.toLowerCase(),value);
				set =  true;
			}else{
				GenericWrapperField f = element.getFieldBySQLName(fieldName);
				setField(f.getId(),value);
				set =  true;
			}
		} catch (FieldNotFoundException e) {
			if (element.isExtension())
			{
				GenericWrapperField eField = element.getField(element.getExtensionFieldName());
				Object child = this.getField(eField.getId());
				if (child == null)
				{
                    child = XFTItem.NewItem((GenericWrapperElement)eField.getReferenceElement(),user);
                    try {
                        this.setChild(eField,(ItemI)child,true);
                    } catch (FieldNotFoundException e1) {
                        logger.error("",e1);
                    }
                }
                if (child != null)
                {
					if (child instanceof XFTItem)
					{
						XFTItem childItem = (XFTItem)child;
						set = childItem.setFieldValue(fieldName,value);
					}
                }
			}
		}
		return set;
	}

	/**
	 * @param f
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public boolean hasLocalField(String f) throws ElementNotFoundException, XFTInitException
	{
		if (this.getFieldIds().get(f.toLowerCase())!= null)
		{
			return true;
		}else
			return false;
	}

	/**
	 * Adds the provided item (value) as a child of this item.  The replace variable governs whether or
	 * not a previously existing matching element will be replaced... or reconciled.
	 * @param sqlName
	 * @param value
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 */
	public void setChild(XFTFieldWrapper field,ItemI value, boolean replace) throws ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
		if (field != null)
		{
			if (field.isMultiple())
			{
				int counter = 0;
				boolean found = false;
				Iterator children = this.getChildItems(field).iterator();
				XFTItem match = null;
				while (children.hasNext())
				{
					XFTItem currentChild = (XFTItem)children.next();

					counter++;
					try {
                        if (CompareItemsByPKs((XFTItem)value,currentChild))
                        {
                            match = currentChild;
                        	found = true;
                        	break;
                        }
                    } catch (Exception e) {
                        logger.error("",e);
                    }
				}
				if (! found)
				{
					setField(field.getId() + counter,value);
					value.setParent(this);
				}else
				{
				    if (replace)
				    {
						setField(field.getId() + (counter - 1),value);
						value.setParent(this);
				    }else{
				        try {
                            match = ReconcileItems(match,(XFTItem)value,true);
                            setField(field.getId() + (counter - 1),match);
                        } catch (Exception e) {
                            logger.error("",e);
                        }
				    }
				}
			}else
			{
			    if (replace)
			    {
					setField(field.getId(),value);
					value.setParent(this);
			    }else{
			        try {
			            XFTItem match = (XFTItem)getField(field.getId());
			            if (match == null)
			            {
	        				setField(field.getId(),value);
	        				value.setParent(this);
			            }else{
	                        match = ReconcileItems(match,(XFTItem)value,true);
	        				setField(field.getId(),match);
			            }
                    } catch (Exception e) {
                        logger.error("",e);
                    }
			    }
			}
		}else{
			throw new FieldNotFoundException(xmlType);
		}
	}

	/**
	 * @param xmlPath
	 * @param value
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 * @throws Exception
	 */
	public void setChild(String xmlPath,ItemI value, boolean replace) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
		if (f.getParentElement().getFullXMLName().equals(this.getGenericSchemaElement().getFullXMLName()))
		{
			setChild(f,value,replace);
		}else{
		    if (this.getGenericSchemaElement().isExtension()) {
				Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
				if (o instanceof XFTItem)
				{
				    ((XFTItem)o).setChild(xmlPath,value,replace);
				    return;
				}else{

				    XFTItem sub = XFTItem.NewItem((GenericWrapperElement)getGenericSchemaElement().getExtensionField().getReferenceElement(),user);
				    sub.setChild(xmlPath,value,replace);
					this.setChild(getGenericSchemaElement().getExtensionField(),sub,true);
					return;
				}
		    }else{
			    throw new FieldNotFoundException(xmlPath);
			}
		}
	}
    /**
     * @param field
     * @param value
     * @param index
     * @throws ElementNotFoundException
     * @throws XFTInitException
     * @throws FieldNotFoundException
     */
    public void setChild(org.nrg.xft.schema.design.XFTFieldWrapper field,ItemI value, int index,String xsiType) throws ElementNotFoundException,XFTInitException,FieldNotFoundException
    {
        if (field != null)
        {
            if (field.isMultiple())
            {
                if (org.apache.commons.lang.StringUtils.isEmpty(xsiType))
                {
                    setField(field.getId() + index,value);
                    value.setParent(this);
                }

                if (XFTTool.ValidateElementName(xsiType))
                {
                    ArrayList all = getChildItems(field);
                    int totalIndex =0;
                    int instanceIndex=0;
                    Iterator iter = all.iterator();
                    while (iter.hasNext())
                    {
                        XFTItem item = ((ItemI)iter.next()).getItem();
                        if (item.matchXSIType(xsiType))
                        {
                            if (instanceIndex ==index){
                                index = totalIndex;
                                break;
                            }
                            instanceIndex++;
                        }
                        totalIndex++;
                    }
                    setField(field.getId() + index,value);
                    value.setParent(this);
                }else{
                    setField(field.getId() + index,value);
                    value.setParent(this);
                }
            }else
            {
                setField(field.getId(),value);
                value.setParent(this);
            }
        }else{
            throw new FieldNotFoundException(xmlType);
        }
    }
	/**
	 * @param field
	 * @param value
	 * @param index
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 */
	public void setChild(org.nrg.xft.schema.design.XFTFieldWrapper field,ItemI value, int index) throws ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
		setChild(field,value,index,null);
	}

	/**
	 * This item compares the reference items of each item.  It is intended to make sure the
	 * primary item has all of the available referenced items.  If the Reconcile comes to an item
	 * that the secondary item has, but the primary doesn't, then the item is added to the primary item.
	 * The items are compared using their PKs.  If a single reference item has a different item in the primary as
	 * in the secondary, then an exception (References do not match) is thrown.
	 *
	 * @param primary Maintained object
	 * @param secondary object to reconcile into the primary object.
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws Exception
	 */
	public static XFTItem ReconcileItems(XFTItem primary,XFTItem secondary,boolean allowMultiples) throws ElementNotFoundException,XFTInitException,Exception
	{
		//org.nrg.xft.XFT.LogCurrentTime("\tBEGIN RECONCILE :" + primary.getName());
		Iterator iter = primary.getGenericSchemaElement().getReferenceFieldsWAddIns().iterator();
		while (iter.hasNext())
		{
			GenericWrapperField field = (GenericWrapperField)iter.next();
			String refKey = field.getId();
			XFTItem sub1 = (XFTItem)primary.getField(refKey);
			XFTItem sub2 = (XFTItem)secondary.getField(refKey);
			if (sub1 == null && sub2 != null)
			{
				primary.setChild(field,sub2,true);
			}else if ((sub1 != null) && (sub2 != null))
			{
				if (CompareItemsByPKs(sub1,sub2))
				{
					ReconcileItems(sub1,sub2,allowMultiples);
				}else
				{
					//throw new Exception(sub1.getName() + " References do not match");
				}
			}
		}

		//org.nrg.xft.XFT.LogCurrentTime("\tBEGIN RECONCILE :" + primary.getName() + " :: 1");
		if (allowMultiples)
		{

			iter = primary.getGenericSchemaElement().getMultiReferenceFields().iterator();
			while (iter.hasNext())
			{
				//org.nrg.xft.XFT.LogCurrentTime("\t\t\t\tBEGIN RECONCILE :" + primary.getName() + " :: WHILE(1)");
				GenericWrapperField field = (GenericWrapperField)iter.next();
				Iterator secondaries = secondary.getChildItems(field).iterator();
				while (secondaries.hasNext())
				{
					//org.nrg.xft.XFT.LogCurrentTime("\t\t\t\tBEGIN RECONCILE :" + primary.getName() + " :: WHILE(1) WHILE(2)");
					XFTItem multi2 = (XFTItem)secondaries.next();

					boolean match = false;
					Iterator primaries = primary.getChildItems(field).iterator();
					while (primaries.hasNext())
					{
						XFTItem multi1 = (XFTItem)primaries.next();
						if (CompareItemsByPKs(multi1,multi2))
						{
							match = true;
							ReconcileItems(multi1,multi2,allowMultiples);
							break;
						}
					}
					//org.nrg.xft.XFT.LogCurrentTime("\t\t\t\tBEGIN RECONCILE :" + primary.getName() + " :: WHILE(1) WHILE(2)");
					if (!match)
					{
					    //org.nrg.xft.XFT.LogCurrentTime("\t\t\t\t\tBEGIN RECONCILE :" + primary.getName() + " :: WHILE(1) WHILE(2) ADD CHILD");
						primary.setChild(field,multi2,true);
					    //org.nrg.xft.XFT.LogCurrentTime("\t\t\t\t\tEND RECONCILE :" + primary.getName() + " :: WHILE(1) WHILE(2) ADD CHILD");
					}
					//org.nrg.xft.XFT.LogCurrentTime("\t\t\t\tEND RECONCILE :" + primary.getName() + " :: WHILE(1) WHILE(2)");
				}
				//org.nrg.xft.XFT.LogCurrentTime("\t\t\t\tEND RECONCILE :" + primary.getName() + " :: WHILE(1)");
			}
		}
		//org.nrg.xft.XFT.LogCurrentTime("\tBEGIN RECONCILE :" + primary.getName() + " :: 2");

		ArrayList add = new ArrayList();
		Iterator histories1 = primary.getHistory().getItemIterator();
		while (histories1.hasNext())
		{
			XFTItem history1 = (XFTItem)histories1.next();

			Iterator histories2 = secondary.getHistory().getItemIterator();

			while (histories2.hasNext())
			{
				XFTItem history2 = (XFTItem)histories2.next();
				if (! CompareItemsByPKs(history1,history2))
				{
					add.add(history2);
				}
			}
		}
		primary.getHistory().addAll(add);

		//org.nrg.xft.XFT.LogCurrentTime("\tEND RECONCILE :" + primary.getName());
		return primary;
	}

	/**
	 * Returns true if all pk values in the two items match.
	 * @param item1
	 * @param item2
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws InvalidReference
	 */
	public static boolean CompareItemsByPKs(XFTItem newI,XFTItem oldI) throws Exception
	{
		return CompareItemsByPKs(newI,oldI,false,true);
    }

	/**
	 * @param newI
	 * @param oldI
	 * @param allowNewNull
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public static boolean CompareItemsByPKs(XFTItem newI,XFTItem oldI,boolean allowNewNull,boolean checkExtensions) throws Exception
	{
        final ItemEqualityI checker=new ItemPKEquality(allowNewNull, checkExtensions);
        return checker.isEqualTo(newI, oldI);
	}
    /**
     * @param newI
     * @param oldI
     * @param allowNewNull
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public static boolean CompareItemsByUniques(XFTItem newI,XFTItem oldI) throws Exception
    {
        return CompareItemsByUniques(newI, oldI, false);
    }
    /**
     * @param newI
     * @param oldI
     * @param allowNewNull
     * @return
     * @throws ElementNotFoundException
     * @throws XFTInitException
     */
    public static boolean CompareItemsByUniques(XFTItem newI,XFTItem oldI,boolean checkExtensions) throws Exception
    {
        final ItemEqualityI checker=new ItemUniqueEquality(false, checkExtensions);
        return checker.isEqualTo(newI, oldI);

    }
    

	/**
	 * Returns the parent XFTItem
	 * @return
	 */
	public ItemI getParent() {
		return parent;
	}

	/**
	 * Sets the parent XFTItem
	 * @param item
	 */
	public void setParent(ItemI item) {
		parent = (ItemI)item;
	}

	/**
	 * If the corresponding element is extended by another element.
	 * @return
	 * @throws ElementNotFoundException
	 */
	private boolean isExtended() throws ElementNotFoundException
	{
		return this.getGenericSchemaElement().isExtended();
	}

	/**
	 * If the corresponding element is extended by another element and has an Extension Element.
	 * (This means that this item is extended but does not extend anything itself.)
	 * @return
	 * @throws ElementNotFoundException
	 */
	public boolean hasExtendedField() throws ElementNotFoundException
	{
		return this.getGenericSchemaElement().hasExtendedField();
	}

	/**
	 * If this item is extended then it returns the extending item, otherwise
	 * it returns this item.
	 * @return
	 * @throws ElementNotFoundException
	 */
	private XFTItem getExtenderItem() throws ElementNotFoundException
	{
		if (getParent() != null)
		{
			if (((XFTItem)getParent()).getGenericSchemaElement().isExtension())
			{
				if (((XFTItem)getParent()).getGenericSchemaElement().getExtensionType().getFullForeignType().equalsIgnoreCase(this.getXSIType()))
				{
					if (((XFTItem)getParent()).isExtended())
					{
						return ((XFTItem)getParent()).getExtenderItem();
					}else
					{
						return (XFTItem)getParent();
					}
				}else{
					return this;
				}
			}else
			{
				return	this;
			}
		}else{
			return this;
		}
	}

	/**
	 * If this item is extended then it returns the extending item's name, otherwise
	 * it returns this item's name.
	 * @return
	 * @throws ElementNotFoundException
	 */
	private String getExtenderName() throws ElementNotFoundException
	{
		if (getParent() != null)
		{
			if (((XFTItem)getParent()).getGenericSchemaElement().isExtension())
			{
				if (((XFTItem)getParent()).getGenericSchemaElement().getExtensionType().getFullForeignType().equalsIgnoreCase(this.getXSIType()))
				{
					if (((XFTItem)getParent()).isExtended())
					{
						return ((XFTItem)getParent()).getExtenderName();
					}else
					{
						return ((XFTItem)getParent()).getXSIType();
					}
				}else{
					return getXSIType();
				}
			}else
			{
				return	getXSIType();
			}
		}else{
			return getXSIType();
		}
	}

	/**
	 * If the corresponding element is extended and has an Extended field, then
	 * the name of this item's extending item is put into the property of this item
	 * using the EXTENDED_FIELD_NAME specified in this class.
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public void setExtenderName() throws ElementNotFoundException,XFTInitException,Exception
	{
		if (this.getGenericSchemaElement().isExtension())
		{
		    GenericWrapperField f = this.getGenericSchemaElement().getExtensionField();
			XFTItem extItem= (XFTItem)this.getProperty(f);
			if (extItem==null)
			{
			    extItem= XFTItem.NewItem(f.getReferenceElement().getGenericXFTElement(),this.getUser());
			    this.setChild(f,extItem,true);
			}
		    extItem.setExtenderName();

		    //COPY LOCAL PK TO LOWER LEVELS
		    XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
        	Iterator keys =ref.getKeyRelations().iterator();
        	while (keys.hasNext())
        	{
        	    XFTRelationSpecification spec = (XFTRelationSpecification)keys.next();
        	    String foreignColName = spec.getForeignCol();
        	    String localColName = spec.getLocalCol();

        	    Object o = this.getProperty(this.getXSIType() + XFT.PATH_SEPERATOR + foreignColName);
        	    if (o!=null)
        	    {
        	        extItem.setFieldValue(localColName,o);
        	        this.setFieldValue(foreignColName,o);
        	    }
        	}
		}else{

		    if (this.isExtended() && this.hasExtendedField())
			{
				GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(getXSIType() + XFT.PATH_SEPERATOR + EXTENDED_ITEM);
				if (getProperty(f) == null)
				{
					GenericWrapperElement ex = GenericWrapperElement.GetElement(getExtenderName());
					XFTItem item = XFTItem.NewItem((GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable()),null);
					item.setFieldValue("element_name",ex.getFullXMLName());

					setDirectProperty(f,item);
				}
			}

		}
	}

	/**
	 * Inserts this item's pk into any extended items.  (Used behind the scenes only).
	 */
	public void extendPK()
	{
	    try {
            if (this.getGenericSchemaElement().isExtension())
            {
                GenericWrapperField f = this.getGenericSchemaElement().getExtensionField();
            	XFTItem extItem= (XFTItem)this.getProperty(f);
            	if (extItem==null)
            	{
            	    extItem= XFTItem.NewItem(f.getReferenceElement().getGenericXFTElement(),this.getUser());
            	    this.setChild(f,extItem,true);
            	}

            	XFTSuperiorReference ref = (XFTSuperiorReference)f.getXFTReference();
            	Iterator keys =ref.getKeyRelations().iterator();
            	while (keys.hasNext())
            	{
            	    XFTRelationSpecification spec = (XFTRelationSpecification)keys.next();
            	    String foreignColName = spec.getForeignCol();
            	    String localColName = spec.getLocalCol();

            	    Object o = this.getProperty(foreignColName);
            	    if (o!=null)
            	    {
            	        extItem.setFieldValue(localColName,o);
            	    }
            	}

            	if (extItem.getGenericSchemaElement().isExtension())
            	    extItem.extendPK();
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
	}

	/**
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws Exception
	 */
	public String getExtensionElement() throws ElementNotFoundException,XFTInitException,Exception
	{
		if (this.getGenericSchemaElement().isExtension())
		{
			XFTItem sub = getExtensionItem();
			if (sub== null)
				return null;
			else{
				return sub.getExtensionElement();
			}
		}else{
			XFTItem elementID =  (XFTItem)findValue(XFTItem.EXTENDED_ITEM +"_" + XFTManager.GetElementTable().getName());

			if (elementID == null)
			{
				Object eID = findValue(XFTItem.EXTENDED_FIELD_NAME);
				if (eID != null && eID instanceof XFTItem)
				{
					elementID = (XFTItem) elementID;
				}else{
					if(eID != null)
					{
						elementID = (XFTItem)XFTMetaManager.GetElementExtensionItemByID((Integer)eID);
					}
				}
			}

			if (elementID != null && (elementID.getField("element_name")!= null))
			{
				return (String)elementID.getField("element_name");
			}else{
				return null;
			}
		}
	}

	/**
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 */
	public XFTItem getExtensionItem() throws ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
		GenericWrapperField f = this.getGenericSchemaElement().getExtensionField();
		return (XFTItem)this.getProperty(f);
	}

	/**
	 * @return
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 * @throws FieldNotFoundException
	 */
	public XFTItem getExtensionItem(String extensionName) throws ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
	    Iterator extensions = (this.getGenericSchemaElement().getExtendedElements()).iterator();
	    while (extensions.hasNext())
	    {
	        ArrayList al = (ArrayList)extensions.next();
	        SchemaElementI e = (SchemaElementI)al.get(0);
	        if (e.getFullXMLName().equals(extensionName))
	        {
	            return (XFTItem)getProperty((String)al.get(1));
	        }
	    }

	    return null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone()
	{
		return clone(false);
	}
	
	public Object clone(boolean reviveHistory)
	{
		XFTItem item = null;
		
		try {
			item = XFTItem.NewItem((reviveHistory && this.getXSIType().endsWith("_history"))?getXSIType().substring(0,getXSIType().indexOf("_history")):getXSIType(),user);

            for (Map.Entry<String,Object> entry: props.entrySet())
            {
            	final Object key = entry.getKey();
            	try {
            	    final Object o = props.get(key);
            	    if (o instanceof XFTItem)
            	    {
            	        item.getProps().put(key,((XFTItem)o).clone());
            	    }else{
            	        item.getProps().put(key,o);
            	    }
                } catch (RuntimeException e1) {
                    throw e1;
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        
            return item;
	}

	/**
	 * @return
	 */
	public Object cloneFormat()
	{
		XFTItem item = new XFTItem();
		try {
			item.setElement(getGenericSchemaElement());
			try {
				item.setXmlFieldNames(this.getXmlFieldNames());
				item.setFieldIds(this.getFieldIds());
			} catch (XFTInitException e1) {
				e1.printStackTrace();
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		}
		return item;
	}
	
	/**
	 * Copies an XFTItem and its children.  All XNAT-generated fields (pk,fk, & meta-data) are ignored.  If you need an exact copy use clone().
	 * @return
	 */
	public XFTItem copy() {
		XFTItem _new=null;
		try {
			_new=XFTItem.NewItem(this.getGenericSchemaElement(), user);
			Iterator fields = this.getGenericSchemaElement().getAllFields(true,true).iterator();
			while (fields.hasNext())
			{
			    GenericWrapperField f= (GenericWrapperField)fields.next();
			    String xmlPath = f.getXMLPathString();
			    if (f.isReference())
			    {
			        SchemaElementI foreign = f.getReferenceElement();
			        
			        if (foreign.getGenericXFTElement().getAddin().equals(""))
			        {
			            if (f.isMultiple())
			            {
			            	ArrayList<ItemI> children=this.getChildItemCollection(xmlPath).getItems();
			            	for (ItemI child:children){
			            		_new.setProperty(xmlPath, ((XFTItem)child).copy());
			            	}
			            }else{
			            	XFTItem child=(XFTItem)this.getProperty(xmlPath);
			        		if(child!=null)_new.setProperty(xmlPath, (child).copy());
			            }
			        }
			    }else{
			        if (f.getXMLType().getLocalType() != null && f.getExpose())
			        {
			            Object _v=this.getProperty(xmlPath);
			            if(_v!=null)_new.setProperty(xmlPath, _v);
			        }
			    }
			}
		} catch (ElementNotFoundException e) {
			//because this data was already stored, none of these exceptions should occur.
			logger.error(e);
		} catch (XFTInitException e) {
			logger.error(e);
		} catch (FieldNotFoundException e) {
			logger.error(e);
		} catch (InvalidValueException e) {
			logger.error(e);
		}
        
        return _new;
	}

	public static XFTItem PopulateItemFromFlatString(String s,UserI user, boolean allowMultiples) throws ElementNotFoundException,Exception
	{
	    String elementName = s.substring(8,s.indexOf(")"));
		XFTItem item=null;
        try {
            item = XFTItem.NewItem(elementName,user);
            item.populateFromFlatString(s);
            item.setLoading(false);
            if (allowMultiples)
            	item.setPreLoaded(allowMultiples);
            s = null;
            if (user != null)
            {
                user.secureItem(item);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        }
        return item;
	}

	public static XFTItem PopulateItemFromFlatString(String s,UserI user) throws ElementNotFoundException,Exception
	{
	    return PopulateItemFromFlatString(s, user, false);
	}


	/**
	 * If any fields in this Object array have matching header values, then those fields are put into
	 * the XFTItem. The method is recursively called on all of its sub items.
	 * @param row
	 * @param headers
	 * @param name
	 * @return
	 */
	public static XFTItem PopulateItemsFromQueryOrganizer(QueryOrganizer qo,GenericWrapperElement e, ArrayList parents, Hashtable row) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{
		XFTItem item = XFTItem.NewItem(e,qo.getUser());
		try {
			if (! parents.contains(e.getFullXMLName()))
			{
					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::1  (" + name + ")");
					Iterator possibleFieldNames = qo.getAllFields().iterator();
					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::4");
					Hashtable hash = ViewManager.GetFieldMap(e,ViewManager.DEFAULT_LEVEL,true,true);
					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::5");
					while (possibleFieldNames.hasNext())
					{

					    //org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::1");
						String key = (String)possibleFieldNames.next();
						//org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::2");
					    String colName = (String)hash.get(key.toLowerCase());
					    if (colName==null)
					    {
					        colName = (String)qo.translateXMLPath(key);
					    }
					    //String colName = (String)qo.translateXMLPath(key);
					    // org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::3");

						Object v = row.get(colName.toLowerCase());
					   // org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::2");
						if (v != null)
						{
						 //   org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::" + key);
							item.setProperty(key,v);
						 //   org.nrg.xft.XFT.LogCurrentTime("END SET PROPERTY");
						}
					}
					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::6");
			}
		} catch (ElementNotFoundException ex) {
			ex.printStackTrace();
		}

		return item;
	}
//
//	/**
//	 * If any fields in this Object array have matching header values, then those fields are put into
//	 * the XFTItem. The method is recursively called on all of its sub items.
//	 * @param row
//	 * @param headers
//	 * @param name
//	 * @return
//	 */
//	public static XFTItem PopulateItemsFromObjectArray(Object[] row,Hashtable headers,String name, String header,ArrayList parents,boolean withChildren,boolean loadHistory) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
//	{
//		XFTItem item = XFTItem.NewItem(name);
//		try {
//			if (! parents.contains(name))
//			{
//				if (! withChildren)
//				{
//					item.populatePropertiesFromObjectArray(row,headers,item.getGenericSchemaElement().getSQLName(),header);
//
//				}else{
//					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::1  (" + name + ")");
//					ArrayList subParents = (ArrayList)parents.clone();
//					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::2");
//					Iterator possibleFieldNames = ViewManager.GetFieldNames(item.getGenericSchemaElement(),true).iterator();
//					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::3");
//					Hashtable possibleFields = ViewManager.GetFieldMap(item.getGenericSchemaElement(),true);
//					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::4");
//
//					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::5");
//					while (possibleFieldNames.hasNext())
//					{
//						String key = (String)possibleFieldNames.next();
//						String colName = (String)possibleFields.get(key.toLowerCase());
//						Integer index = (Integer)headers.get(colName.toLowerCase());
//						if (index != null)
//						{
//							Object v = row[index.intValue()];
//							if (v != null)
//							{
//							    //org.nrg.xft.XFT.LogCurrentTime("SET PROPERTY");
//								item.setXMLProperty(key,v);
//							}
//						}
//					}
////					//org.nrg.xft.XFT.LogCurrentTime("POPULATE ITEM FROM HASH::6");
//				}
//
//			}
//		} catch (ElementNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		return item;
//	}

	public boolean hasProperty(String id, Object find) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		if (getField(id) != null)
		{
			Object o= getField(id);
			if (o.toString().equals(find.toString()))
			{
			    return true;
			}else{
			    return false;
			}
		}else{
		    id = StringUtils.StandardizeXMLPath(id);
			if (id.indexOf(XFT.PATH_SEPERATOR) == -1)
			{
				try {
					GenericWrapperField f = this.getGenericSchemaElement().getDirectField(id);
					if (f==null)
					{
					    if (this.getGenericSchemaElement().isExtension()) {
							Object sub = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
							if (sub instanceof XFTItem)
							{
							    return ((XFTItem)sub).hasProperty(id,find);

							}else if (sub==null && find.equals("NULL")){
							    return true;
							}else if (sub==null){
                                return false;
                            }else{
							    throw new FieldNotFoundException(id);
							}
					    }else{
						    throw new FieldNotFoundException(id);
						}
					}else{
						Object o= getProperty(f);

						return f.compareValues(o,find);
					}
				} catch (FieldNotFoundException e) {
				    if (this.getGenericSchemaElement().isExtension()) {
						Object sub = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
						if (sub instanceof XFTItem)
						{
						    return ((XFTItem)sub).hasProperty(id,find);

						}else if (sub==null && find.equals("NULL")){
                            return true;
                        }else if (sub==null){
                            return false;
                        }else{
						    throw new FieldNotFoundException(id);
						}
				    }else{
					    return false;
					}
				}
			}else
			{
			    if (verifyXMLPaths)
			    {
				    if (id.substring(0,id.indexOf(XFT.PATH_SEPERATOR)).equals(this.getXSIType()))
				    {
				        try {
		                    id = GenericWrapperElement.GetVerifiedXMLPath(id);
		                } catch (Exception e) {
		                }
				    }
			    }

				String first = id.substring(0,id.indexOf(XFT.PATH_SEPERATOR));
				String parse = id.toString();
				if (first.equalsIgnoreCase(getXSIType()))
				{
					parse = id.substring(id.indexOf(XFT.PATH_SEPERATOR)+1);
				}
				try {
                    return hasXMLProperty(parse,find);
                } catch (FieldNotFoundException e1) {
                    if (this.getGenericSchemaElement().isExtension()) {
						Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
						if (o instanceof XFTItem)
						{
						    return ((XFTItem)o).hasProperty(parse,find);
						}else if (o==null && find.equals("NULL")){
                            return true;
                        }else if (o==null){
                            return false;
                        }else{
						    throw new FieldNotFoundException(id);
						}
				    }else{
					    throw new FieldNotFoundException(id);
					}
                }
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
	 */
	public Object getProperty(String id) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		return getProperty(id,false);
	}

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
     */
    public Object getProperty(String id, boolean allowMultipleValues) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        return getProperty(id,allowMultipleValues,this.getUser());
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
	 */
	public Object getProperty(String id, boolean allowMultipleValues,UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		if (getField(id) != null)
		{
			return getField(id);
		}else{
		    id = StringUtils.StandardizeXMLPath(id);
			if (id.indexOf(XFT.PATH_SEPERATOR) == -1 && id.indexOf("[")==-1)
			{
				try {
					GenericWrapperField f = this.getGenericSchemaElement().getDirectField(id);
					if (f==null)
					{
					    if (this.getGenericSchemaElement().isExtension()) {
							Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName(),allowMultipleValues);
							if (o instanceof XFTItem)
							{
							    return ((XFTItem)o).getProperty(id,allowMultipleValues,user);
							}else if (o==null){
							    return null;
							}else{
							    throw new FieldNotFoundException(id);
							}
					    }else{
						    throw new FieldNotFoundException(id);
						}
					}else if(f.isMultiple()){
                        return getChildItems(f,user,false);
                    }else{
						return getProperty(f,allowMultipleValues);
					}
				} catch (FieldNotFoundException e) {
				    if (this.getGenericSchemaElement().isExtension()) {
						Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName(),allowMultipleValues);
						if (o instanceof XFTItem)
						{
						    return ((XFTItem)o).getProperty(id,allowMultipleValues,user);
						}else if (o==null){
						    return null;
						}else{
						    throw new FieldNotFoundException(id);
						}
				    }else{
					    return null;
					}
				}
			}else if( id.indexOf(XFT.PATH_SEPERATOR) == -1 && EndsWithFilter(id)){
                boolean setIndex = false;
                String expectedXSIType= null;
                Integer multiIndex=0;
                String where = null;
                if (EndsWithFilter(id))
                {
                    Map map = GetFilterOptions(id);
                    if (map.get("@index")!=null){
                        multiIndex = (Integer)map.get("@index");
                        setIndex =true;
                    }
                    if (map.get("@xsi:type")!=null){
                        expectedXSIType = (String)map.get("@xsi:type");
                    }
                    if (map.get("where")!=null){
                        where = (String)map.get("where");
                    }
                    id = CleanFilter(id);
                }
                GenericWrapperField lastField = this.getGenericSchemaElement().getDirectField(id);
                ArrayList subs = this.getChildItems(lastField,expectedXSIType,user,false);

                if (where!=null)
                {
                    int indexEq = where.indexOf(EQUALS);
                    String wField = where.substring(0,indexEq);
                    String wValue = where.substring(indexEq+1);

                    ArrayList newSubs = new ArrayList();
                    Iterator subIter = subs.iterator();

                    while (subIter.hasNext())
                    {
                        ItemI sub = (ItemI)subIter.next();
                        if(sub.hasProperty(wField,wValue)){
                            newSubs.add(sub);
                        }
                    }

                    subs = newSubs;
                }

                if (setIndex)
                {
                    if (subs.size() > multiIndex)
                    {
                        return ((XFTItem)subs.get(multiIndex));
                    }else{
                        return null;
                    }
                }else{
                    if(allowMultipleValues)
                    {
                        return subs;
                    }else{
                        if (subs.size() > multiIndex)
                        {
                            return ((XFTItem)subs.get(multiIndex));
                        }else{
                            return null;
                        }
                    }
                }
            }else
			{
                String parse = id.toString();
                if (id.indexOf(XFT.PATH_SEPERATOR) != -1){
    			    if (verifyXMLPaths)
    			    {
    				    if (id.substring(0,id.indexOf(XFT.PATH_SEPERATOR)).equals(this.getXSIType()))
    				    {
    				        try {
    		                    id = GenericWrapperElement.GetVerifiedXMLPath(id);
    		                } catch (Exception e) {
    		                }
    				    }
    			    }

    				String first = id.substring(0,id.indexOf(XFT.PATH_SEPERATOR));
    				parse = id.toString();
    				if (first.equalsIgnoreCase(getXSIType()))
    				{
    					parse = id.substring(id.indexOf(XFT.PATH_SEPERATOR)+1);
    				}
                }
				try {
                    return getXMLProperty(parse,allowMultipleValues,user);
                } catch (FieldNotFoundException e1) {
                    if (this.getGenericSchemaElement().isExtension()) {
						Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName(),allowMultipleValues);
						if (o instanceof XFTItem)
						{
						    return ((XFTItem)o).getProperty(parse,allowMultipleValues,user);
						}else if (o==null){
						    return null;
						}else{
						    throw new FieldNotFoundException(id);
						}
				    }else{
					    throw new FieldNotFoundException(id);
					}
                }
			}
		}
	}

    /**
     * @param f
     * @return
     */
    public Object getProperty(GenericWrapperField f)
    {
        return getProperty(f,false);
    }

    /**
     * @param f
     * @return
     */
    public Object getProperty(GenericWrapperField f, boolean allowMulitpleReturns)
    {
        return getProperty(f,allowMulitpleReturns,this.getUser());
    }

	/**
	 * @param f
	 * @return
	 */
	public Object getProperty(GenericWrapperField f, boolean allowMulitpleReturns,UserI user)
	{
	    if (f.getXMLType() !=null && f.getXMLType().getLocalType().equalsIgnoreCase("string"))
	    {
	        Object o = getField(f.getId());
	        if (o!=null)
	        {
		        if (o.getClass().getName().equalsIgnoreCase("[B"))
				{
					byte[] b = (byte[]) o;
					java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
					try {
						baos.write(b);
					} catch (IOException e) {
						e.printStackTrace();
					}
					return baos.toString();
				}else{
					return o.toString();
				}
	        }else{
	            return null;
	        }
	    }else{
			return getField(f.getId(),allowMulitpleReturns);
	    }
	}

	/**
	 * Whether this item has the specified property
	 * @param xmlPath
	 * @param find value to find
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	private boolean hasXMLProperty(String xmlPath, Object find) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		GenericWrapperField lastField = null;
		while(xmlPath.indexOf(XFT.PATH_SEPERATOR)!= -1)
		{
			Integer multiIndex = null;
            String expectedXSIType= null;
            String where = null;
			String next = xmlPath.substring(0,xmlPath.indexOf(XFT.PATH_SEPERATOR));
			xmlPath = xmlPath.substring(xmlPath.indexOf(XFT.PATH_SEPERATOR)+1);
			if (EndsWithFilter(next))
			{
                Map map = GetFilterOptions(next);
                if (map.get("@index")!=null){
                    multiIndex = (Integer)map.get("@index");
                }
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                if (map.get("where")!=null){
                    where = (String)map.get("where");
                }

				next = CleanFilter(next);
			}
			if (lastField == null)
			{
				lastField = this.getGenericSchemaElement().getDirectField(next);
			}else{
				lastField = lastField.getDirectField(next);
			}

			if (lastField.isReference())
			{
				if (! lastField.isMultiple())
				{
					ItemI sub = (ItemI)this.getProperty(lastField.getId());
					if (sub != null)
					{
						return sub.hasProperty(xmlPath,find);
					}else{
                        if (find.equals("NULL")){
                            return true;
                        }else{
                            return false;
                        }
					}
				}else{
					ArrayList subs = this.getChildItems(lastField,expectedXSIType,false);

                    if (where!=null)
                    {
                        int indexEq = where.indexOf(EQUALS);
                        String wField = where.substring(0,indexEq);
                        String wValue = where.substring(indexEq+1);

                        ArrayList newSubs = new ArrayList();
                        Iterator subIter = subs.iterator();

                        while (subIter.hasNext())
                        {
                            ItemI sub = (ItemI)subIter.next();
                            if(sub.hasProperty(wField,wValue)){
                                newSubs.add(sub);
                            }
                        }

                        subs = newSubs;
                    }
					if (multiIndex ==null)
					{
					    Iterator subIter = subs.iterator();
					    boolean found = false;

					    while (subIter.hasNext())
					    {
					        ItemI sub = (ItemI)subIter.next();
					        found = sub.hasProperty(xmlPath,find);
					        if (found)break;
					    }

					    return found;
					}else{
						if (subs.size() > multiIndex.intValue())
						{
							return ((ItemI)subs.get(multiIndex.intValue())).hasProperty(xmlPath,find);
						}else{
                            if (find.equals("NULL")){
                                return true;
                            }else{
                                return false;
                            }
						}
					}
				}
			}
		}

		if (lastField == null)
		{
			lastField = this.getGenericSchemaElement().getDirectField(xmlPath);
		}else{
			lastField = lastField.getDirectField(xmlPath);
		}

		Object o = getProperty(lastField,true);
		if (o!=null)
		{
            if (o instanceof ArrayList){
                Iterator iter = ((ArrayList)o).iterator();
                boolean matched = false;
                while (iter.hasNext()){
                    Object temp = iter.next();
                    if (temp instanceof XFTItem){
                        if( ((XFTItem)temp).hasProperty(xmlPath, find)){
                            matched=true;
                            break;
                        }
                    }else{
                        return lastField.compareValues(o,find);
                    }
                }

                return matched;
            }else{
                return lastField.compareValues(o,find);
            }
		}else{
			if (this.getGenericSchemaElement().isExtension()) {
				o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
				if (o instanceof XFTItem)
				{
				    return ((XFTItem)o).hasProperty(xmlPath,find);
				}else{
                    if (find.equals("NULL")){
                        return true;
                    }else{
                        return false;
                    }
				}
		    }else{
                if (find.equals("NULL")){
                    return true;
                }else{
                    return false;
                }
			}
		}
	}

    /**
     * Gets item property by its XML dot-syntax name.
     * @param xmlPath
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws FieldNotFoundException
     */
    @SuppressWarnings("unused")
    private Object getXMLProperty(String xmlPath,boolean allowMultipleValues) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        return getXMLProperty(xmlPath,allowMultipleValues,this.getUser());
    }

	/**
	 * Gets item property by its XML dot-syntax name.
	 * @param xmlPath
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	private Object getXMLProperty(String xmlPath,boolean allowMultipleValues,UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		String original = xmlPath;
		GenericWrapperField lastField = null;
		while(xmlPath.indexOf(XFT.PATH_SEPERATOR)!= -1)
		{
			int multiIndex = 0;
			boolean setIndex = false;
            String expectedXSIType= null;
			String next = xmlPath.substring(0,xmlPath.indexOf(XFT.PATH_SEPERATOR));
			xmlPath = xmlPath.substring(xmlPath.indexOf(XFT.PATH_SEPERATOR)+1);
            String where = null;
			if (EndsWithFilter(next))
			{
                Map map = GetFilterOptions(next);
                if (map.get("@index")!=null){
                    multiIndex = (Integer)map.get("@index");
                    setIndex =true;
                }
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                if (map.get("where")!=null){
                    where = (String)map.get("where");
                }
				next = CleanFilter(next);
			}
			if (lastField == null)
			{
				lastField = this.getGenericSchemaElement().getDirectField(next);
			}else{
				lastField = lastField.getDirectField(next);
			}

			if (lastField.isReference())
			{
				if (! lastField.isMultiple())
				{
					XFTItem sub = (XFTItem)this.getProperty(lastField.getId(),allowMultipleValues,user);
					if (sub != null)
					{
						return sub.getProperty(xmlPath,allowMultipleValues,user);
					}else{
						return null;
					}
				}else{
					ArrayList subs = this.getChildItems(lastField,expectedXSIType,user,false);
                    if (where!=null)
                    {
                        int indexEq = where.indexOf(EQUALS);
                        String wField = where.substring(0,indexEq);
                        String wValue = where.substring(indexEq+1);

                        ArrayList newSubs = new ArrayList();
                        Iterator subIter = subs.iterator();

                        while (subIter.hasNext())
                        {
                            ItemI sub = (ItemI)subIter.next();
                            if(sub.hasProperty(wField,wValue)){
                                newSubs.add(sub);
                            }
                        }

                        subs = newSubs;
                    }
					if (setIndex)
					{
						if (subs.size() > multiIndex)
	                    {
	                        return ((XFTItem)subs.get(multiIndex)).getProperty(xmlPath,allowMultipleValues,user);
	                    }else{
	                        return null;
	                    }
					}else{
						if(allowMultipleValues)
						{
							ArrayList al = new ArrayList();
							Iterator subsIter = subs.iterator();
							while (subsIter.hasNext())
							{
								XFTItem sub = (XFTItem)subsIter.next();
								Object o = sub.getProperty(xmlPath,allowMultipleValues,user);
								if (o instanceof ArrayList)
								{
									al.addAll((ArrayList)o);
								}else if (o !=null){
									al.add(o);
								}
							}
							return al;
						}else{
							if (subs.size() > multiIndex)
		                    {
		                        return ((XFTItem)subs.get(multiIndex)).getProperty(xmlPath,allowMultipleValues,user);
		                    }else{
		                        return null;
		                    }
						}
					}
				}
			}
		}

		if (lastField == null)
		{
			lastField = this.getGenericSchemaElement().getDirectField(xmlPath);
		}else{
            if (EndsWithFilter(xmlPath))
            {
                int multiIndex=0;
    			boolean setIndex = false;
                String expectedXSIType= null;
                String where = null;
                Map map = GetFilterOptions(xmlPath);
                if (map.get("@index")!=null){
                    multiIndex = (Integer)map.get("@index");
                    setIndex =true;
                }
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                if (map.get("where")!=null){
                    where = (String)map.get("where");
                }
                xmlPath = CleanFilter(xmlPath);

                lastField = lastField.getDirectField(xmlPath);
                if (lastField.isReference())
                {
                    if (! lastField.isMultiple())
                    {
                        XFTItem sub = (XFTItem)this.getProperty(lastField.getId(),allowMultipleValues,user);
                        if (sub != null)
                        {
                            return sub.getProperty(lastField.getReferenceElementName().getLocalType(),allowMultipleValues,user);
                        }else{
                            return null;
                        }
                    }else{
                        ArrayList subs = this.getChildItems(lastField,expectedXSIType,user,false);

                        if (where!=null)
                        {
                            int indexEq = where.indexOf(EQUALS);
                            String wField = where.substring(0,indexEq);
                            String wValue = where.substring(indexEq+1);

                            ArrayList newSubs = new ArrayList();
                            Iterator subIter = subs.iterator();

                            while (subIter.hasNext())
                            {
                                ItemI sub = (ItemI)subIter.next();
                                if(sub.hasProperty(wField,wValue)){
                                    newSubs.add(sub);
                                }
                            }

                            subs = newSubs;
                        }

                        if (setIndex)
    					{
                        	if (subs.size() > multiIndex)
                            {
                                return ((XFTItem)subs.get(multiIndex)).getProperty(lastField.getReferenceElementName().getLocalType(),allowMultipleValues,user);
                            }else{
                                return null;
                            }
    					}else{
    						if(allowMultipleValues)
    						{
    							return subs;
    						}else{
    							if (subs.size() > multiIndex)
                                {
                                    return ((XFTItem)subs.get(multiIndex));
                                }else{
                                    return null;
                                }
    						}
    					}
                    }
                }else{
                    throw new FieldNotFoundException(original);
                }
            }else{
                lastField = lastField.getDirectField(xmlPath);
            }
		}

		Object o = getProperty(lastField,allowMultipleValues,user);
		if (o!=null)
		{
		    return o;
		}else{
			if (this.getGenericSchemaElement().isExtension()) {
				o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName(),allowMultipleValues,user);
				if (o instanceof XFTItem)
				{
				    return ((XFTItem)o).getProperty(xmlPath,allowMultipleValues,user);
				}else{
				    return null;
				}
		    }else{
			    return null;
			}
		}
	}

	public static boolean EndsWithFilter(String s)
	{
	    if (StringUtils.EndsWithInt(s))
	    {
	        if(s.indexOf("__") == -1)
	        {
	            return false;
	        }else{
	            return true;
	        }
	    }else if (s.endsWith("]")){
	        return true;
	    }
	    else{
	        return false;
	    }
	}

	public static Map GetFilterOptions(String s)
	{
        Hashtable map = new Hashtable();
	    if (StringUtils.EndsWithInt(s))
	    {
	 	   int index= StringUtils.GetEndingInt(s);
           map.put("@index", new Integer(index));
           s = StringUtils.CleanEndingInt(s);
	    }

        while (s.endsWith("]")){
            int startIndex = s.lastIndexOf('[');
            int endIndex=s.lastIndexOf(']');
            String text= s.substring(startIndex+1, endIndex);

            try {
                Integer index= Integer.parseInt(text);
                map.put("@index", index);
            } catch (NumberFormatException e) {
                //NOT A NUMBER
                int equalsIndex =text.indexOf('=');
                if (equalsIndex!=-1){
                    String start = text.substring(0,equalsIndex);
                    String end = text.substring(equalsIndex+1);
                    end= end.replace("'", "").trim();
                    if (start.startsWith("@")){
                        map.put(start.toLowerCase(), end);
                    }else{

                        map.put("where", text.replace("'", "").trim());
                    }
                }
            }

            s = s.substring(0,startIndex);
        }

        return map;
	}

	public static String CleanFilter(String s)
	{
	    if (StringUtils.EndsWithInt(s))
	    {
		    s = StringUtils.CleanEndingInt(s);
		    return s.substring(0,s.length()-2);
	    }else if (s.endsWith("]")){
	        return s.substring(0,s.indexOf("["));
	    }else{
	        return s;
	    }
	}

	/**
	 * @return
	 */
	public ArrayList getAllXMLFieldNames()
	{
	    if (this.allXmlFieldNames==null)
	    {
	        this.allXmlFieldNames = new ArrayList();
	        try {
	            this.allXmlFieldNames = ViewManager.GetFieldNames(this.getGenericSchemaElement(),true);
            } catch (Exception e) {
                logger.error("",e);
            }
	    }
	    return this.allXmlFieldNames;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public ArrayList getSingleXMLFieldNames() throws Exception
	{
	    return ViewManager.GetFieldNames(this.getGenericSchemaElement(),ViewManager.QUARANTINE,false,true);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#setXMLProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String xmlPath, Object value) throws XFTInitException, ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
	    try {
            setXMLPropertyChild(xmlPath,value,true);
        } catch (FieldNotFoundException e) {
            throw new FieldNotFoundException(xmlPath);
        }catch (InvalidValueException e){
            throw e;
        }catch (Exception e){
            logger.error(e);
            throw new FieldNotFoundException(xmlPath,"Processing Exception");
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#setXMLProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String xmlPath, Object value,boolean parseValue) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
	    try {
            setXMLPropertyChild(xmlPath,value,parseValue);
        } catch (FieldNotFoundException e) {
            throw new FieldNotFoundException(xmlPath);
        }
	}

	/**
	 * @param xmlPath
	 * @return
	 */
	public String getValidXMLProperty(String xmlPath)
	{
	    Iterator iter = this.getAllXMLFieldNames().iterator();
	    while (iter.hasNext())
	    {
	        String s = (String)iter.next();
	        if (s.equalsIgnoreCase(xmlPath))
	        {
	            return s;
	        }
	    }
	    return null;
	}

	@SuppressWarnings("deprecation")
    private void setXMLPropertyChild(String xmlPath, Object value,boolean parseValue) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
        xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
        String originalPath = xmlPath;
		try {
            GenericWrapperField lastField = null;
            while(xmlPath.indexOf(XFT.PATH_SEPERATOR)!= -1)
            {
            	String next = xmlPath.substring(0,xmlPath.indexOf(XFT.PATH_SEPERATOR));
            	xmlPath = xmlPath.substring(xmlPath.indexOf(XFT.PATH_SEPERATOR)+1);
                //org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE(" + next + ") ::1");
            	if (next.equalsIgnoreCase("history"))
            	{
            		XFTItem sub = this.getFirstHistory();
            		sub.setXMLPropertyChild(xmlPath,value,parseValue);
            		return;
            	}else if (!matchXSIType(next))
            	{
            		int multiIndex=0;
                    String where = null;
                    String expectedXSIType= null;
            		if (EndsWithFilter(next))
            		{
            		    //org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE ::1.5");
                        Map map = GetFilterOptions(next);
                        if (map.get("@index")!=null){
                            multiIndex = (Integer)map.get("@index");
                        }
                        if (map.get("@xsi:type")!=null){
                            expectedXSIType = (String)map.get("@xsi:type");
                        }
                        if (map.get("where")!=null){
                            where = (String)map.get("where");
                        }
            			next = CleanFilter(next);
            		}

            		// org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE ::2");
            		if (lastField == null)
            		{
            		    // org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE: NO LAST FIELD::1");
            			lastField = (GenericWrapperField)getXmlFieldNames().get(next.toLowerCase());
            			// org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE: NO LAST FIELD::2");
            		}else{
            		    // org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE: HAS LAST FIELD::1");
            			lastField = (GenericWrapperField)lastField.getAllPossibleXMLFieldNames().get(next.toLowerCase());
            			// org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE: HAS LAST FIELD::2");
            		}

            		// org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE ::3");
            		if (lastField == null)
            		{
            		    if (this.getGenericSchemaElement().isExtension()) {
            				Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
            				if (o instanceof XFTItem)
            				{
            				    ((XFTItem)o).setProperty(next +XFT.PATH_SEPERATOR +xmlPath,value,parseValue);
            				    return;
            				}else{
            				    XFTItem sub = XFTItem.NewItem((GenericWrapperElement)getGenericSchemaElement().getExtensionField().getReferenceElement(),user);
            				    sub.setProperty(next +XFT.PATH_SEPERATOR +xmlPath,value,parseValue);
            					this.setChild(getGenericSchemaElement().getExtensionField(),sub,true);
            					return;
            				}
            		    }else{
                            if (next.indexOf(COLON)==-1)
                                throw new FieldNotFoundException(xmlPath);
            			}
            		}


            		if (lastField !=null && lastField.isReference())
            		{
            			if (! lastField.isMultiple())
            			{
                            GenericWrapperElement foreign;
                            if (expectedXSIType==null){
                                foreign=(GenericWrapperElement)lastField.getReferenceElement();
                            }else{
                                try {
                                    foreign=GenericWrapperElement.GetElement(expectedXSIType);
                                } catch (ElementNotFoundException e) {
                                    foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                }

                            }
            				XFTItem sub = (XFTItem)this.getProperty(lastField.getId());
            				if (sub == null)
            				{
            					sub = XFTItem.NewItem(foreign,user);
            					this.setChild(lastField,sub,true);

            				}
//                            if (!sub.getXSIType().equals(foreign.getXSIType())){
//                                if(!sub.getGenericSchemaElement().isExtensionOf(foreign)){
//                                    if (foreign.isExtensionOf(sub.getGenericSchemaElement())){
//                                        XFTItem newSub = XFTItem.NewItem(foreign,user);
//                                        String path = foreign.getExtensionXMLPath(sub.getGenericSchemaElement());
//                                        newSub.setProperty(path,sub);
//                                        this.setChild(lastField,newSub,true);
//                                    }else{
//                                        throw new InvalidValueException("Pre-existing type (" + sub.getXSIType() + ") is incompatible with requested type (" + foreign.getFullXMLName() + ")");
//                                    }
//                                }
//                            }
            				sub.setXMLPropertyChild(xmlPath,value,parseValue);
            				return;
            			}else{
            			    //	   org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI found::1");
            				ArrayList subs = this.getChildItems(lastField,expectedXSIType,false);

                            if (where!=null)
                            {
                                int indexEq = where.indexOf(EQUALS);
                                String wField = where.substring(0,indexEq);
                                String wValue = where.substring(indexEq+1);

                                ArrayList newSubs = new ArrayList();
                                Iterator subIter = subs.iterator();
                                boolean matched=false;
                                while (subIter.hasNext())
                                {
                                    ItemI sub = (ItemI)subIter.next();
                                    if(sub.hasProperty(wField,wValue)){
                                        matched=true;
                                        newSubs.add(sub);
                                    }
                                }

                                if (!matched){
                                    GenericWrapperElement foreign;
                                    if (expectedXSIType==null){
                                        foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                    }else{
                                        try {
                                            foreign=GenericWrapperElement.GetElement(expectedXSIType);
                                        } catch (ElementNotFoundException e) {
                                            foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                        }

                                    }

                                    int counter = 0;
                                    while (! (counter > multiIndex))
                                    {
                                        if (newSubs.size()<=counter)
                                        {
                                            XFTItem sub = XFTItem.NewItem(foreign,user);
                                            sub.setProperty(wField, wValue);
                                            newSubs.add(sub);
                                            this.setChild(lastField,sub,true);
                                        }
                                        counter++;
                                    }
                                }

                                subs = newSubs;
                            }
            				if (subs.size() > multiIndex)
            				{
            					XFTItem sub = (XFTItem)subs.get(multiIndex);
            					sub.setXMLPropertyChild(xmlPath,value,parseValue);
            					return;
            				}else{
            					int counter = 0;
                                GenericWrapperElement foreign;
                                if (expectedXSIType==null){
                                    foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                }else{
                                    try {
                                        foreign=GenericWrapperElement.GetElement(expectedXSIType);
                                    } catch (ElementNotFoundException e) {
                                        foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                    }

                                }

            					while (! (counter > multiIndex))
            					{
            						if (subs.size()<=counter)
            						{
            							XFTItem sub = XFTItem.NewItem(foreign,user);
            							this.setChild(lastField,sub,true);
            						}
            						counter++;
            					}

            					subs = this.getChildItems(lastField,expectedXSIType,false);
                                XFTItem sub = (XFTItem)subs.get(multiIndex);
                                sub.setXMLPropertyChild(xmlPath,value,parseValue);
                                return;

            				}
            			}
            		}else{
            		}
            	}else{

            		originalPath = originalPath.substring(originalPath.indexOf(XFT.PATH_SEPERATOR)+1);
            	}
            }

            GenericWrapperField secondToLastField = lastField;
            try {
                Integer multiIndex = null;
                String expectedXSIType= null;
                String where =null;
                if (EndsWithFilter(xmlPath))
            	{
                    //   org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD NODE ::1.5");
                    Map map = GetFilterOptions(xmlPath);
                    if (map.get("@index")!=null){
                        multiIndex = (Integer)map.get("@index");
                    }
                    if (map.get("@xsi:type")!=null){
                        expectedXSIType = (String)map.get("@xsi:type");
                    }
                    if (map.get("where")!=null){
                        where = (String)map.get("where");
                    }
            		xmlPath = CleanFilter(xmlPath);

            		if (lastField == null)
            		{
            			lastField = (GenericWrapperField)this.getXmlFieldNames().get(xmlPath.toLowerCase());
            		}else{
            			lastField = (GenericWrapperField)lastField.getAllPossibleXMLFieldNames().get(xmlPath.toLowerCase());
            		}

            		if (lastField.isReference())
            		{
            		}else{
            			throw new FieldNotFoundException(xmlPath);
            		}
            	}else{
            		if (lastField == null)
            		{
            			lastField = (GenericWrapperField)this.getXmlFieldNames().get(xmlPath.toLowerCase());
            		}else{
            			lastField = (GenericWrapperField)lastField.getAllPossibleXMLFieldNames().get(xmlPath.toLowerCase());
            		}

            		if (this.getGenericSchemaElement().isExtension()) {
            			Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
                        if (o instanceof XFTItem)
            			{
            			    try {
                                ((XFTItem)o).setProperty(originalPath,value,parseValue);
                                return;
            			    } catch (FieldNotFoundException e1) {
                            }
            			}else{

            			    XFTItem sub = XFTItem.NewItem((GenericWrapperElement)getGenericSchemaElement().getExtensionField().getReferenceElement(),user);
            			    try {
                                sub.setProperty(originalPath,value,parseValue);
                                this.setChild(getGenericSchemaElement().getExtensionField(),sub,true);
                                return;
                            } catch (FieldNotFoundException e1) {
                            }
            			}
            	    }

            		if (lastField == null)
            		{
            		    if (secondToLastField != null)
            		    {
            			    if (secondToLastField.getXMLType().getLocalType().equalsIgnoreCase("date"))
            			    {
            			        if (xmlPath.equalsIgnoreCase("date") || xmlPath.equalsIgnoreCase("month") || xmlPath.equalsIgnoreCase("year") || xmlPath.equalsIgnoreCase("day"))
            			        {
            			            Object o = (Object)getProperty(secondToLastField.getId());
            			            Date date = null;

            			            if (o instanceof Date)
            			            {
            			                date = (Date)o;
            			            }else{
            			                if (o!= null)
            			                {
            					            try {
                                                date= DateUtils.parseDate(o.toString());
                                            } catch (ParseException e1) {
                                                logger.error(e1);
                                            }
            			                }
            			            }

                                    if (date==null) {
                                        GregorianCalendar cal = new GregorianCalendar(2012, 0, 1);
            			    			date= cal.getTime();
            			            }

            			            if (xmlPath.equalsIgnoreCase("month")){
            			                int i;
                                        try {
                                            i = Integer.parseInt(value.toString());
                                            date.setMonth(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.month");
                                        }
            			            }else if (xmlPath.equalsIgnoreCase("year"))
            			            {
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setYear(i-1900);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.year");
                                        }
            			            }else{
            			                //DATE/DAY
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setDate(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.day");
                                        }
            			            }

            			            setDirectProperty(secondToLastField.getId(),date);
            			            return;
            			        }else{
            						throw new FieldNotFoundException(xmlPath);
            			        }
            			    }else if (secondToLastField.getXMLType().getLocalType().equalsIgnoreCase("dateTime") || secondToLastField.getXMLType().getLocalType().equalsIgnoreCase("string"))
            			    {
            			        if (xmlPath.equalsIgnoreCase("date")
            			                || xmlPath.equalsIgnoreCase("month")
            			                || xmlPath.equalsIgnoreCase("year")
            			                || xmlPath.equalsIgnoreCase("day")
            			                || xmlPath.equalsIgnoreCase("minutes")
            			                || xmlPath.equalsIgnoreCase("hours")
            			                || xmlPath.equalsIgnoreCase("seconds"))
            			        {
            			            Date date = (Date)getProperty(secondToLastField.getId());
            			            if (date==null)
            			            {
            			                GregorianCalendar cal = new GregorianCalendar(1,1,2000);
            			    			date= cal.getTime();
            			            }

            			            if (xmlPath.equalsIgnoreCase("month")){
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setMonth(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.month");
                                        }
            			            }else if (xmlPath.equalsIgnoreCase("year"))
            			            {
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setYear(i-1900);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.year");
                                        }
            			            }else if (xmlPath.equalsIgnoreCase("minutes"))
            			            {
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setMinutes(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.minutes");
                                        }
            			            }else if (xmlPath.equalsIgnoreCase("hours"))
            			            {
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setHours(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.hours");
                                        }
            			            }else if (xmlPath.equalsIgnoreCase("seconds"))
            			            {
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setSeconds(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.seconds");
                                        }
            			            }else{
            			                //DATE/DAY
            			                try {
                                            int i= Integer.parseInt(value.toString());
                                            date.setDate(i);
                                        } catch (NumberFormatException e1) {
                                            throw new InvalidValueException("Invalid Date.day");
                                        }
            			            }

            			            setDirectProperty(secondToLastField.getId(),date);
            			            return;
            			        }else{
            						throw new FieldNotFoundException(xmlPath);
            			        }
            			    }else{
            					throw new FieldNotFoundException(xmlPath);
            			    }
            		    }else{
            				throw new FieldNotFoundException(xmlPath);
            		    }
            		}
            	}

                if (lastField !=null)
                {
                    if (lastField.isReference())
                    {
                        if (value instanceof XFTItem)
                        {
                            if (multiIndex==null && where==null)
                                setChild(lastField,(XFTItem)value,true);
                            else{
                                ArrayList subs = this.getChildItems(lastField,expectedXSIType,false);

                                if (where!=null)
                                {
                                    int indexEq = where.indexOf(EQUALS);
                                    String wField = where.substring(0,indexEq);
                                    String wValue = where.substring(indexEq+1);

                                    ArrayList newSubs = new ArrayList();
                                    Iterator subIter = subs.iterator();

                                    while (subIter.hasNext())
                                    {
                                        ItemI sub = (ItemI)subIter.next();
                                        if(sub.hasProperty(wField,wValue)){
                                            newSubs.add(sub);
                                        }
                                    }

                                    subs = newSubs;
                                }
                                if (subs.size() > multiIndex.intValue())
                                {
                                    setChild(lastField,(ItemI)value,multiIndex.intValue());
                                    return;
                                }else{
                                    int counter = 0;
                                    GenericWrapperElement foreign;
                                    if (expectedXSIType==null){
                                        foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                    }else{
                                        try {
                                            foreign=GenericWrapperElement.GetElement(expectedXSIType);
                                        } catch (ElementNotFoundException e) {
                                            foreign=(GenericWrapperElement)lastField.getReferenceElement();
                                        }

                                    }
                                    while (! (counter > multiIndex.intValue()))
                                    {
                                        if (subs.size()<counter)
                                        {
                                            XFTItem sub = XFTItem.NewItem(foreign,user);
                                            this.setChild(lastField,sub,true);
                                        }
                                        counter++;
                                    }

                                    setChild(lastField,(ItemI)value,multiIndex.intValue(),expectedXSIType);
                                    return;
                                }
                            }
                        }else{
                            GenericWrapperElement foreign = (GenericWrapperElement)lastField.getReferenceElement();
                            if (lastField.isCreatedChild())
                            {
                                ArrayList subs = this.getChildItems(lastField,expectedXSIType,false);

                                if (where!=null)
                                {
                                    int indexEq = where.indexOf(EQUALS);
                                    String wField = where.substring(0,indexEq);
                                    String wValue = where.substring(indexEq+1);

                                    ArrayList newSubs = new ArrayList();
                                    Iterator subIter = subs.iterator();

                                    while (subIter.hasNext())
                                    {
                                        ItemI sub = (ItemI)subIter.next();
                                        if(sub.hasProperty(wField,wValue)){
                                            newSubs.add(sub);
                                        }
                                    }

                                    subs = newSubs;
                                }
                                if (subs.size() > multiIndex.intValue())
                                {
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI found::2");
                                    XFTItem sub = (XFTItem)subs.get(multiIndex.intValue());
                                    sub.setXMLPropertyChild(xmlPath,value,parseValue);
                                    return;
                                }else{
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::1");
                                    int counter = 0;
                                    GenericWrapperElement expectedE;
                                    if (expectedXSIType==null){
                                        expectedE=(GenericWrapperElement)lastField.getReferenceElement();
                                    }else{
                                        try {
                                            expectedE=GenericWrapperElement.GetElement(expectedXSIType);
                                        } catch (ElementNotFoundException e) {
                                            expectedE=(GenericWrapperElement)lastField.getReferenceElement();
                                        }

                                    }
                                    while (! (counter > multiIndex.intValue()))
                                    {
                                        if (subs.size()<=counter)
                                        {
                                            XFTItem sub = XFTItem.NewItem(expectedE,user);
                                            this.setChild(lastField,sub,true);
                                        }
                                        counter++;
                                    }
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::2");

                                    subs = this.getChildItems(lastField,expectedXSIType,false);
                                    XFTItem sub = (XFTItem)subs.get(multiIndex.intValue());
                                    sub.setXMLPropertyChild(xmlPath,value,parseValue);
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::3");
                                    return;
                                }
                            }else if(lastField.isMultiple()){
                                String fieldName="";
                                if (foreign.getDirectField(foreign.getDirectXMLName())!=null)
                                {
                                    fieldName=foreign.getDirectXMLName();
                                }else if (foreign.getDirectField(lastField.getName())!=null){
                                    fieldName=lastField.getName();
                                }else{
                                    throw new FieldNotFoundException(originalPath);
                                }

                                ArrayList subs = this.getChildItems(lastField,expectedXSIType,false);

                                if (where!=null)
                                {
                                    int indexEq = where.indexOf(EQUALS);
                                    String wField = where.substring(0,indexEq);
                                    String wValue = where.substring(indexEq+1);

                                    ArrayList newSubs = new ArrayList();
                                    Iterator subIter = subs.iterator();

                                    while (subIter.hasNext())
                                    {
                                        ItemI sub = (ItemI)subIter.next();
                                        if(sub.hasProperty(wField,wValue)){
                                            newSubs.add(sub);
                                        }
                                    }

                                    subs = newSubs;
                                }
                                if (subs.size() > multiIndex.intValue())
                                {
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI found::2");
                                    XFTItem sub = (XFTItem)subs.get(multiIndex.intValue());
                                    sub.setXMLPropertyChild(fieldName,value,parseValue);
                                    return;
                                }else{
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::1");
                                    int counter = 0;
                                    GenericWrapperElement expectedE;
                                    if (expectedXSIType==null){
                                        expectedE=(GenericWrapperElement)lastField.getReferenceElement();
                                    }else{
                                        try {
                                            expectedE=GenericWrapperElement.GetElement(expectedXSIType);
                                        } catch (ElementNotFoundException e) {
                                            expectedE=(GenericWrapperElement)lastField.getReferenceElement();
                                        }

                                    }
                                    while (! (counter > multiIndex.intValue()))
                                    {
                                        if (subs.size()<=counter)
                                        {
                                            XFTItem sub = XFTItem.NewItem(expectedE,user);
                                            this.setChild(lastField,sub,true);
                                        }
                                        counter++;
                                    }
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::2");

                                    subs = this.getChildItems(lastField,expectedXSIType,false);
                                    XFTItem sub = (XFTItem)subs.get(multiIndex.intValue());
                                    sub.setXMLPropertyChild(fieldName,value,parseValue);
                                    //         org.nrg.xft.XFT.LogCurrentTime("SET XML PROPERTY CHILD::MULTI CREATE::3");
                                    return;
                                }
                            }else{
                                XFTReferenceI ref = lastField.getXFTReference();
                                if (! ref.isManyToMany())
                                {
                                    XFTSuperiorReference sup = (XFTSuperiorReference)ref;
                                    XFTRelationSpecification spec = (XFTRelationSpecification)sup.getKeyRelations().get(0);
                                    setDirectProperty(spec.getLocalCol().toLowerCase(),spec.getSchemaType().parseValue(value));
                                }
                            }
                        }
                    }else{
                        if (parseValue)
                        {
                            setDirectProperty(lastField.getId(),lastField.parseValue(value));
                        }else{
                            setDirectProperty(lastField.getId(),value);
                        }
                    }
                }
            } catch (FieldNotFoundException e) {
            	if (this.hasLocalField(xmlPath))
            	{
            		setDirectProperty(xmlPath,value);
            	}else{
            		throw new FieldNotFoundException(originalPath + " (" + value +")");
            	}
            }
        } catch (RuntimeException e) {
            logger.error("",e);
            throw new FieldNotFoundException(originalPath + " (" + value +")");
        }
		//  org.nrg.xft.XFT.LogCurrentTime("END SET XML PROPERTY ");
	}



	/**
	 * @param sql_name
	 * @param value
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public void setDirectProperty(String sql_name,Object value) throws XFTInitException,ElementNotFoundException
	{
		this.setFieldValue(sql_name,value);
	}

	/**
	 * @param f
	 * @param value
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public void setDirectProperty(GenericWrapperField f,Object value) throws XFTInitException,ElementNotFoundException
	{
		this.setField(f.getId(),value);
	}

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
     */
    public ArrayList<XFTItem> getChildItems(String id, UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(id,null,allowDBAccess,false,user);
    }

    public ArrayList<XFTItem> getChildItems(String id) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(id,null,allowDBAccess,false,this.getUser());
    }

    public ArrayList<XFTItem> getChildItems(String id,boolean allowDBAccess) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        return getChildItems(id,null,allowDBAccess,false,this.getUser());
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
     */
    public ArrayList<XFTItem> getChildItems(String id, String xsiType,boolean allowMultiples, boolean allowDBAccess) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        return getChildItems(id, xsiType,allowMultiples, allowDBAccess,this.getUser());
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getProperty(java.lang.String)
	 */
	public ArrayList<XFTItem> getChildItems(String id, String xsiType,boolean allowMultiples, boolean allowDBAccess,UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    id = StringUtils.StandardizeXMLPath(id);
	    if (id.indexOf(XFT.PATH_SEPERATOR)!=-1)
	    {
	        if (id.substring(0,id.indexOf(XFT.PATH_SEPERATOR)).equalsIgnoreCase(this.getXSIType()))
	        {
			    try {
		            id = GenericWrapperElement.GetVerifiedXMLPath(id);
		        } catch (Exception e) {
		        }
	        }
			String first = id.substring(0,id.indexOf(XFT.PATH_SEPERATOR));
			String parse = id.toString();
			if (first.equalsIgnoreCase(getXSIType()))
			{
				parse = id.substring(id.indexOf(XFT.PATH_SEPERATOR)+1);
			}
			try {
	            return getXMLChildItems(parse,user);
	        } catch (FieldNotFoundException e1) {
	            if (this.getGenericSchemaElement().isExtension()) {
					Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
					if (o instanceof XFTItem)
					{
					    return ((XFTItem)o).getChildItems(id,xsiType,allowDBAccess,false);
					}else{
					    throw new FieldNotFoundException(id);
					}
			    }else{
				    throw new FieldNotFoundException(id);
				}
	        }
	    }else{
	        try {
                GenericWrapperField lastField = this.getGenericSchemaElement().getDirectField(id);

                return getChildItems(lastField,xsiType,user,false);
            } catch (FieldNotFoundException e) {
                if (this.getGenericSchemaElement().isExtension()) {
					Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName());
					if (o instanceof XFTItem)
					{
					    return ((XFTItem)o).getChildItems(id,xsiType,allowDBAccess,false);
					}else{
					    throw new FieldNotFoundException(id);
					}
			    }else{
				    throw new FieldNotFoundException(id);
				}
            }
	    }
	}

    public int nextIndexOfSeperator(String xmlPath){
        int lastIndex = 0;
        int seperatorIndex =xmlPath.indexOf(XFT.PATH_SEPERATOR,lastIndex);
        int openFilterIndex = xmlPath.indexOf("[",lastIndex);

        while (seperatorIndex!=-1 && openFilterIndex!=-1)
        if (openFilterIndex!=-1)
        {
          if (openFilterIndex < seperatorIndex)
          {
              int closeFilterIndex = xmlPath.indexOf("]",openFilterIndex);
              while (closeFilterIndex > seperatorIndex)
              {
                  seperatorIndex = xmlPath.indexOf(XFT.PATH_SEPERATOR,closeFilterIndex);
                  if (seperatorIndex==-1)
                  {
                      break;
                  }
                  openFilterIndex = xmlPath.indexOf("[",closeFilterIndex);
                  if (openFilterIndex==-1)
                  {
                      break;
                  }

                  if (openFilterIndex<seperatorIndex)
                  {
                      closeFilterIndex = xmlPath.indexOf("]",openFilterIndex);
                  }
              }
          }
        }

        return seperatorIndex;
    }

	 /**
	 * Gets item property by its XML dot-syntax name.
	 * @param xmlPath
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 */
	private ArrayList getXMLChildItems(String xmlPath,UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		GenericWrapperField lastField = null;
		xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
		while(xmlPath.indexOf(XFT.PATH_SEPERATOR)!= -1)
		{
			int multiIndex = 0;
            String expectedXSIType = null;
			boolean hasIndex = false;
            int seperatorIndex = xmlPath.indexOf(XFT.PATH_SEPERATOR);
			String next = xmlPath.substring(0,seperatorIndex);
			xmlPath = xmlPath.substring(seperatorIndex+1);
            String where=null;
			if (EndsWithFilter(next))
			{
                Map map = GetFilterOptions(next);
                if (map.get("@index")!=null){
                    hasIndex=true;
                    multiIndex = (Integer)map.get("@index");
                }
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                if (map.get("where")!=null){
                    where = (String)map.get("where");
                }
				next = CleanFilter(next);
			}
			if (lastField == null)
			{
				lastField = this.getGenericSchemaElement().getDirectField(next);
			}else{
				lastField = lastField.getDirectField(next);
			}

			if (lastField.isReference())
			{
				if (! lastField.isMultiple())
				{
					ItemI sub = (ItemI)this.getProperty(lastField.getId());
					if (sub != null)
					{
						return sub.getChildItems(xmlPath);
					}else{
						return null;
					}
				}else{
					ArrayList subs = this.getChildItems(lastField,expectedXSIType,user,false);

                    if (where!=null)
                    {
                        int indexEq = where.indexOf(EQUALS);
                        String wField = where.substring(0,indexEq);
                        String wValue = where.substring(indexEq+1);

                        ArrayList newSubs = new ArrayList();
                        Iterator subIter = subs.iterator();

                        while (subIter.hasNext())
                        {
                            ItemI sub = (ItemI)subIter.next();
                            if(sub.hasProperty(wField,wValue)){
                                newSubs.add(sub);
                            }
                        }

                        subs = newSubs;
                    }
					if (hasIndex)
					{
						if (subs.size() > multiIndex)
						{
							return ((ItemI)subs.get(multiIndex)).getChildItems(xmlPath);
						}else{
							return null;
						}
					}else{
					    ArrayList childItems = new ArrayList();
					    Iterator subIter = subs.iterator();
					    while (subIter.hasNext())
					    {
					        XFTItem sub = (XFTItem)subIter.next();
					        childItems.addAll(sub.getChildItems(xmlPath));
					    }
					    return childItems;
					}
				}
			}
		}

		if (lastField == null)
		{
			lastField = this.getGenericSchemaElement().getDirectField(xmlPath);
		}else{
			lastField = lastField.getDirectField(xmlPath);
		}

		return getChildItems(lastField);
	}

	public boolean isChildOf(String elementName)
	{
	    if (getParent()!=null)
	    {
	        try {
                if (((XFTItem)getParent()).getGenericSchemaElement().instanceOf(elementName))
                {
                    return true;
                }else{
                    return ((XFTItem)getParent()).isChildOf(elementName);
                }
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
	    }
	    return false;
	}

	public void clearChildren(XFTFieldWrapper field1)
	{
	    GenericWrapperField f = (GenericWrapperField)GenericWrapperFactory.GetInstance().convertField(field1);
	    if (f.isMultiple())
			{
		        int counter = 0;

		        while (props.containsKey(f.getId()+counter))
		        {
		            try {
                        Object o =props.remove(f.getId() + counter++);
                        if (o instanceof XFTItem)
                        {
                            ((XFTItem)o).clear();
                        }
                    } catch (RuntimeException e1) {
                    }
		        }
			}else{
			    try {
			        Object o =props.remove(f.getId());
			        if (o instanceof XFTItem)
                    {
                        ((XFTItem)o).clear();
                    }
                } catch (RuntimeException e1) {
                }
			}

		if (this.postLoaded.contains(f.getId().toLowerCase())){
		    this.postLoaded.remove(f.getId().toLowerCase());
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
	 */
	public java.util.ArrayList getChildItems(XFTFieldWrapper field,UserI user,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    boolean allowDBAccess = ! (preLoaded || loading);
	    return getChildItems(field,allowDBAccess,false,user,loadHistory,null);
	}

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,false,this.getUser(),false,null);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,false,this.getUser(),loadHistory,null);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field,boolean loadHistory,ItemCache cache)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,false,this.getUser(),loadHistory,cache);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field, String xsiType,UserI user,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,false,xsiType,user,loadHistory);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field, String xsiType,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,false,xsiType,this.getUser(),loadHistory);
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
	 */
	public java.util.ArrayList getChildItems(XFTFieldWrapper field,boolean allowChildMultiples,UserI user,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    boolean allowDBAccess = ! (preLoaded || loading);
	    return getChildItems(field,allowDBAccess,allowChildMultiples,user,loadHistory,null);
	}

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getChildItems(XFTFieldWrapper field,boolean allowChildMultiples,String xsiType,UserI user,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        boolean allowDBAccess = ! (preLoaded || loading);
        return getChildItems(field,allowDBAccess,allowChildMultiples,xsiType,user,loadHistory);
    }


    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public java.util.ArrayList getCurrentChildItems(XFTFieldWrapper field,UserI user, boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        return getChildItems(field,false,true,user,loadHistory,null);
    }

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    private java.util.ArrayList getChildItems(XFTFieldWrapper field, boolean allowDBAccess,boolean allowChildMultiples,String xsiType,UserI user,boolean loadHistory)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        ArrayList all= getChildItems(field,allowDBAccess,allowChildMultiples,user,loadHistory,null);
        if (org.apache.commons.lang.StringUtils.isEmpty(xsiType))
        {
            return all;
        }

        if (XFTTool.ValidateElementName(xsiType))
        {
            ArrayList matchingSubs = new ArrayList();
            for (int i=0;i<all.size();i++)
            {
                XFTItem child = (XFTItem)all.get(i);
                if (child.matchXSIType(xsiType))
                {
                    matchingSubs.add(child);
                }
            }

            return matchingSubs;
        }else{
            return all;
        }

    }
    
    XFTItemDBLoader loader=null;
    public XFTItemDBLoader getDBLoader(ItemCache cache){
    	if(loader==null){
    		loader=new XFTItemDBLoader(this,cache);
    	}
    	return loader;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItems(org.nrg.xft.schema.design.XFTFieldWrapper)
	 */
	private java.util.ArrayList getChildItems(XFTFieldWrapper field, boolean allowDBAccess,boolean allowChildMultiples, UserI user,boolean loadHistory, ItemCache cache)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		ArrayList al = new ArrayList();
		GenericWrapperField f = (GenericWrapperField)GenericWrapperFactory.GetInstance().convertField(field);
		
		//first check for pre-loaded entries
		int counter = 0;
		if (f.isMultiple())
		{
			while (this.getField(f.getId()+counter)!= null)
			{
				al.add(getField(f.getId()+ counter++));
			}
		}else{
			if (this.getField(f.getId())!=null)
			{
				al.add(getField(f.getId()));
			}
		}

		if ((!loadHistory) && (al.size()>0 || this.postLoaded.contains(f.getId().toLowerCase()) || (!allowDBAccess) || (preLoaded)))
		{
			al.trimToSize();
			return al;
		}else{
		    if (! this.isPauseDBAccess())//if we are allowed to query database
		    {
			    postLoaded.add(f.getId().toLowerCase());
			    if (! this.isChildOf(f.getReferenceElementName().getFullForeignType()))//prevent circular references
			    {
			        logger.debug("Loading: " + this.getXSIType() + "/"+f.getXMLPathString());
				    ItemCollection items = getDBLoader(cache).getCurrentDBChildren(f,user,allowChildMultiples,loadHistory);
				    if (items != null)
				    {
				    	if(loadHistory){
				    		return items.getItems();
				    	}
						Iterator iter = items.getItemIterator();
						while (iter.hasNext())
						{
						    XFTItem child = (XFTItem)iter.next();

						    if (child.getGenericSchemaElement().isExtended() && !child.getXSIType().endsWith("_history"))//make sure we are dealing with the top most element (mrScan rather than imageScan)
						    {
							    try {
			                        String extensionName = child.getExtensionElement();

			                        if (extensionName != null)
			                        {
			        			        logger.debug("Extending: " + child.getXSIType() + "->" +extensionName);
			                        	GenericWrapperElement extensionElement = GenericWrapperElement.GetElement(extensionName);

			                        	ItemSearch search = new ItemSearch();
			                        	search.setUser(this.getUser());

			                        	ItemCollection extendedItems = null;

			                            SearchCriteria c = new SearchCriteria();
			                            GenericWrapperField foreignKey = (GenericWrapperField)child.getGenericSchemaElement().getAllPrimaryKeys().get(0);
			                            c.setFieldWXMLPath(extensionElement + String.valueOf(XFT.PATH_SEPERATOR) + foreignKey.getSQLName());
			                            Object v = child.getProperty(foreignKey.getId());
			                            c.setValue(v);
			                            c.setCleanedType(foreignKey.getXMLType().getLocalType());
			                            search.add(c);
			                            search.setElement(extensionElement);
			                            try {
		                                    extendedItems = search.exec(false,false);

		                                    if (extendedItems.size() > 0)
		                                    {
		                                    	XFTItem newSub = (XFTItem)extendedItems.get(0);
		                                        this.setChild(f,newSub,true);
		                                    	newSub.setParent(this);
		                                    }else{
		                                        this.setChild(f,child,true);
		                                    }
		                                } catch (IllegalAccessException e1) {

		                                }
			                        }else{
									    this.setChild(f,child,true);
								    }
			                    } catch (ElementNotFoundException e) {
			                        logger.error("",e);
			    				    this.setChild(f,child,true);
			                    } catch (XFTInitException e) {
			                        logger.error("",e);
			    				    this.setChild(f,child,true);
			                    } catch (FieldNotFoundException e) {
			                        logger.error("",e);
			    				    this.setChild(f,child,true);
			                    } catch (Exception e) {
			                        logger.error("",e);
			    				    this.setChild(f,child,true);
			                    }
						    }else{
							    this.setChild(f,child,true);
						    }
						}
				    }
					return getChildItems(f,allowDBAccess,allowChildMultiples,user,false,cache);
			    }else{
			        al.trimToSize();
					return al;
			    }
		    }else{
		        al.trimToSize();
				return al;
		    }
		}
	}

    /* (non-Javadoc)
     * @see org.nrg.xft.ItemI#getChildItemCollection(org.nrg.xft.schema.design.XFTFieldWrapper)
     */
    public ItemCollection getChildItemCollection(String field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
    {
        ArrayList al = getChildItems(field);
        ItemCollection items = new ItemCollection();
        items.addAll(al);
        return items;
    }

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItemCollection(org.nrg.xft.schema.design.XFTFieldWrapper)
	 */
	public ItemCollection getChildItemCollection(XFTFieldWrapper field)throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		ArrayList al = getChildItems(field);
		ItemCollection items = new ItemCollection();
		items.addAll(al);
		return items;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#toXML_BOS(java.lang.String)
	 */
	public java.io.ByteArrayOutputStream toXML_BOS(String location) throws Exception
	{
		Document doc = this.toWebXML(location,false);
		return XMLUtils.DOMToBAOS(doc);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#toXML_BOS(java.lang.String)
	 */
	public java.io.ByteArrayOutputStream toXML_BOS(String location,boolean limited) throws Exception
	{
		Document doc = this.toWebXML(location,limited);
		return XMLUtils.DOMToBAOS(doc);
	}

	public boolean checkStatus(Predicate compare,boolean _default) throws MetaDataException{
		try {
            if (getProperty(META_STATUS)!=null)
            {
                String s = (String)getProperty(META_STATUS);
            	if (compare.evaluate(s))
            	{
            		return true;
            	}
            }else if (this.getMeta() != null)
            {
            	try {
            		String s = (String)getMeta().getProperty(meta.getXSIType()  +STATUS);
            		if (compare.evaluate(s))
            		{
            			return true;
            		}
            	} catch (XFTInitException e) {
            		e.printStackTrace();
            	} catch (ElementNotFoundException e) {
            		e.printStackTrace();
            	} catch (FieldNotFoundException e) {
            		e.printStackTrace();
            	}
            }else{
            	if (getXSIType().equalsIgnoreCase(XDAT_META_ELEMENT))
            	{
            		return true;
            	}else{

                        try {
                            Integer info = this.getIntegerProperty(this.getGenericSchemaElement().getMetaDataFieldName());
                            if (info!=null)
                            {
                                XFTItem _meta = ItemSearch.GetItem(this.getXSIType() + _META_DATA_META_DATA_ID,info,null,false);
                                if (meta != null)
                                {
                                    this.setMeta(_meta);
                                    String s = (String)getMeta().getProperty(meta.getXSIType() + _STATUS);
                            		if (compare.evaluate(s))
                            		{
                            			return true;
                            		}
                                }
                            }else{
                                throw new MetaDataException("Missing Meta Data Foreign Key in " + getXSIType() +" Table.");
                            }
                        } catch (MetaDataException e1) {
                            throw e1;
                        } catch (Exception e1) {
                            logger.error("",e1);
                        }

            	}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
		return _default;
	}
	
	/**
	 * whether or not this Item has been activated.
	 * @return
	 */
	public boolean isQuarantine() throws MetaDataException
	{
	    return checkStatus(new Predicate(){
			public boolean evaluate(Object arg0) {
				if(arg0 !=null && arg0 instanceof String){
					return (ViewManager.QUARANTINE.equals(arg0));
				}
				return false;
			}},false);
	}
	
	/**
	 * whether or not this Item has been activated.
	 * @return
	 */
	public boolean isLocked() throws MetaDataException
	{
	    return checkStatus(new Predicate(){
			public boolean evaluate(Object arg0) {
				if(arg0 !=null && arg0 instanceof String){
					return (ViewManager.LOCKED.equals(arg0));
				}
				return false;
			}},false);
	}
	
	/**
	 * whether or not this Item has been activated.
	 * @return
	 */
	public boolean isActive() throws MetaDataException
	{
	    return checkStatus(new Predicate(){
			public boolean evaluate(Object arg0) {
				if(arg0 !=null && arg0 instanceof String){
					return (ViewManager.ACTIVE.equals(arg0) || ViewManager.LOCKED.equals(arg0));
				}
				return false;
			}},false);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#toHTML()
	 */
	public String toHTML() throws Exception
	{
		return this.toHTML(null);
	}
	
	public String toHTML(FlattenedItemA.HistoryConfigI includeHistory) throws Exception
	{
		return ItemHtmlBuilder.build(null,Arrays.asList(
				ItemMerger.merge(
						ItemPropBuilder.build(this.getItem(), includeHistory,null))));
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#save(org.nrg.xft.security.UserI)
	 * 25 references (2/3/12)
	 */
	public boolean save(UserI user, boolean overrideSecurity,boolean allowItemRemoval,EventMetaI c) throws InvalidItemException,Exception
	{
	    if (user == null)
        {
	        if (overrideSecurity)
	        {
           	    boolean q = getGenericSchemaElement().isQuarantine();
           	    return DBAction.StoreItem(this,user,false,q,false,allowItemRemoval,SecurityManager.GetInstance(),c);
	        }else{
	            ItemSearch search = ItemSearch.GetItemSearch("xdat:user",null);
	            search.setAllowMultiples(false);
	           	ItemCollection items = search.exec();
	           	if (items.size() > 0){
	    	        throw new Exception("Error.  Must have a valid user account to perform database updates/inserts.");
	           	}else{
	           	    if (getXSIType().startsWith("xdat"))
	           	    {
		           	    boolean q = getGenericSchemaElement().isQuarantine();
		           	    return DBAction.StoreItem(this,user,false,q,false,allowItemRemoval,SecurityManager.GetInstance(),c);
	           	    }else{
	           	        throw new Exception("Error.  Must have a valid user account to perform database updates/inserts.");
	           	    }
	           	}
	        }
        }else{
            if (overrideSecurity)
            {
	    		boolean q = getGenericSchemaElement().isQuarantine();
	    		return DBAction.StoreItem(this,user,false,q,false,allowItemRemoval,SecurityManager.GetInstance(),c);
            }else{
                String error = user.canStoreItem(this,allowItemRemoval);
                if (error == null)
                {
    	    		boolean q = getGenericSchemaElement().isQuarantine();
    	    		return DBAction.StoreItem(this,user,false,q,false,allowItemRemoval,SecurityManager.GetInstance(),c);
                }else{
                    throw new InvalidPermissionException(error);
                }
            }
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#save(org.nrg.xft.security.UserI)
	 * 3 references (2/3/12)
	 */
	public void save(UserI user, boolean overrideSecurity,boolean quarantine,boolean overrideQuarantine, boolean allowItemRemoval,EventMetaI c) throws Exception
	{
	    if (user == null)
        {
	        if (overrideSecurity)
	        {
	    		boolean q = getGenericSchemaElement().isQuarantine(quarantine);
	    		DBAction.StoreItem(this,user,false,q,overrideQuarantine,allowItemRemoval,SecurityManager.GetInstance(),c);
	        }else{
	            ItemSearch search = ItemSearch.GetItemSearch("xdat:user",null);
	            search.setAllowMultiples(false);
	           	ItemCollection items = search.exec();
	           	if (items.size() > 0){
	    	        throw new Exception("Error.  Must have a valid user account to perform database updates/inserts.");
	           	}else{
	           	    if (getXSIType().startsWith("xdat"))
	           	    {
	    	    		boolean q = getGenericSchemaElement().isQuarantine(quarantine);
                        DBAction.StoreItem(this,user,false,q,overrideQuarantine,allowItemRemoval,SecurityManager.GetInstance(),c);
	           	    }else{
	           	        throw new Exception("Error.  Must have a valid user account to perform database updates/inserts.");
	           	    }
	           	}
	        }
        }else{
            if (overrideSecurity)
            {
	    		boolean q = getGenericSchemaElement().isQuarantine(quarantine);
                DBAction.StoreItem(this,user,false,q,overrideQuarantine,allowItemRemoval,SecurityManager.GetInstance(),c);
            }else{
                String error = user.canStoreItem(this,allowItemRemoval);
                if (error == null)
                {
    	    		boolean q = getGenericSchemaElement().isQuarantine(quarantine);
                    DBAction.StoreItem(this,user,false,q,overrideQuarantine,allowItemRemoval,SecurityManager.GetInstance(),c);
                }else{
                    throw new InvalidPermissionException(error);
                }
            }
        }
	}

	/**
	 * @param history
	 */
	public void addHistoryItem(XFTItem history)
	{
		this.history.addItem(history);
	}

	/**
	 * @return
	 * @throws FieldNotFoundException
	 */
	public ArrayList getHistoryItems() throws FieldNotFoundException
	{
		return history.getItems(this.getXSIType() + "_history" + XFT.PATH_SEPERATOR + "change_date");
	}

	/**
	 * @return
	 */
	public boolean hasHistory() throws XFTInitException,ElementNotFoundException
	{
	    if (isModified())
	    {
			if (history.size() > 0)
			{
				return true;
			}else{
			    populateHistories();
				return true;
			}
	    }else{
	        return false;
	    }
	}

	public void populateHistories() throws XFTInitException,ElementNotFoundException
	{
	    if (isModified())
	    {
	        GenericWrapperElement e = GenericWrapperElement.GetElement(this.getGenericSchemaElement().getFullXMLName() + "_history");

	        try {
                Hashtable hash = (Hashtable)this.getPkValues();

                Object o= null;
                String key = null;

                Enumeration keys = hash.keys();
                while (keys.hasMoreElements())
                {
                    key= (String)keys.nextElement();
                    o = hash.get(key);
                }

                history = ItemSearch.GetItems(e.getFullXMLName()+"." + key,o,getUser(),false);
            } catch (Exception e1) {
                logger.error("",e1);
            }
	    }
	}

	/**
	 * @return
	 */
	public ItemCollection getHistory()
	{
		return history;
	}

	/**
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public XFTItem getFirstHistory() throws XFTInitException,ElementNotFoundException
	{
		if (hasHistory())
		{
			return (XFTItem)history.get(0);
		}else{
			GenericWrapperElement e = GenericWrapperElement.GetElement(this.getGenericSchemaElement().getFullXMLName() + "_history");
			XFTItem hist = XFTItem.NewItem(e,user);
			addHistoryItem(hist);
			return hist;
		}

	}
	/**
	 * @return Returns the meta.
	 */
	public ItemI getMeta() {
		if (meta == null)
		{
		    try {
                if (this.getGenericSchemaElement().getAddin().equals(""))
                {
                	try {
                		GenericWrapperField f = this.getGenericSchemaElement().getField(META);
                		if (f != null)
                		{
                			ArrayList al = this.getChildItems(f);
                			if (al.size() > 0)
                			{
                				meta = (ItemI)al.get(0);
                				if (meta.getProperty(SHAREABLE)!=null || meta.getProperty(INSERT_DATE)!=null)
                				{
                				    return meta;
                				}
                			}
                		}

                		try {
                            if (meta != null)
                            {
                                meta = ItemSearch.GetItem(this.getXSIType() + _META_DATA___META_DATA_ID,meta.getProperty(META_DATA_ID),null,false);
                            }else{
                                Object v = this.getProperty(this.getGenericSchemaElement().getMetaDataFieldName());
                                if (v!=null)
                                {
                                    meta = ItemSearch.GetItem(this.getXSIType() + _META_DATA___META_DATA_ID,v,null,false);
                                }
                            }
                        } catch (Exception e1) {
                            logger.error("",e1);
                        }
                	} catch (ElementNotFoundException e) {
                        logger.error("",e);
                	} catch (XFTInitException e) {
                        logger.error("",e);
                	} catch (FieldNotFoundException e) {
                        logger.error("",e);
                	}
                }else if(this.getGenericSchemaElement().getAddin().equals("history")){
                	try {
						GenericWrapperField f = GenericWrapperElement.GetElement(this.getXSIType().substring(0,this.getXSIType().indexOf("_history"))).getField(META);
						if (f != null)
						{
							ArrayList al = this.getChildItems(f);
							if (al.size() > 0)
							{
								meta = (ItemI)al.get(0);
								if (meta.getProperty(SHAREABLE)!=null || meta.getProperty(INSERT_DATE)!=null)
								{
								    return meta;
								}
							}
						}
					} catch (XFTInitException e) {
                        logger.error("",e);
                	} catch (FieldNotFoundException e) {
                        logger.error("",e);
                	}
                }
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
		}
		return meta;
	}
	/**
	 * @param meta The meta to set.
	 */
	public void setMeta(ItemI meta) {
		this.meta = meta;
	}


	/**
	 * Set status 'quarantine', 'active', or 'locked'
	 * @param user
	 * @throws Exception
	 */
	public void setStatus(UserI user,String status) throws Exception{
		String query = "SELECT " + GenericWrapperUtils.ACT_FUNCTION + this.getGenericSchemaElement().getFormattedName() + "(";
	    ArrayList keys = element.getAllPrimaryKeys();
        Iterator keyIter = keys.iterator();
        String pk = null;
        while (keyIter.hasNext())
        {
            GenericWrapperField sf = (GenericWrapperField)keyIter.next();
            pk = sf.getXMLPathString(element.getXSIType());
            query +=DBAction.ValueParser(getProperty(pk),sf,true);
        }
	    query += "," + user.getID() + ",'" + status + "',true);";
	    PoolDBUtils.ExecuteNonSelectQuery(query,this.getDBName(),user.getUsername());
	    
	    String login="";
	    if(user!=null){
	    	login=user.getUsername();
	    }
	    PoolDBUtils.PerformUpdateTrigger(this, login);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#activate(org.nrg.xft.security.UserI)
	 */
	public void activate(UserI user) throws Exception
	{
	    setStatus(user,ViewManager.ACTIVE);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#quarantine(org.nrg.xft.security.UserI)
	 */
	public void quarantine(UserI user) throws Exception
	{
	    setStatus(user,ViewManager.QUARANTINE);
	}



	public void lock(UserI user) throws Exception
	{
	    setStatus(user,ViewManager.LOCKED);
	}
	


	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#activate(org.nrg.xft.security.UserI)
	 */
	public boolean needsActivation() throws Exception
	{
		XFTItem meta = (XFTItem)getMeta();
		if (meta != null)
		{
			if (((String)meta.getProperty(STATUS_STRING)).equalsIgnoreCase(ViewManager.QUARANTINE))
			{
				return true;
			}
		}

		Iterator iter = getChildItems().iterator();
		while (iter.hasNext())
		{
			XFTItem child = (XFTItem)iter.next();
			if (child.getGenericSchemaElement().getAddin().equals(""))
			{
			    if(child.needsActivation())
			        return true;
			}
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#validate()
	 */
	public ValidationResults validate() throws Exception
	{
	    validationResults= XFTValidator.Validate(this);
	    return validationResults;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public boolean isHistory() throws Exception
	{
	    if (this.getGenericSchemaElement().getName().endsWith("_history"))
	    {
	        return true;
	    }else{
	        return false;
	    }
	}

	/**
	 * @return
	 * @throws Exception
	 */
	public boolean isValid() throws Exception
	{
	    ValidationResults vr = validate();
	    return vr.isValid();
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getCurrentDBVersion()
	 */
	public XFTItem getCurrentDBVersion()
	{
	    try {
            return getCurrentDBVersion(this.getGenericSchemaElement().isPreLoad());
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return null;
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getCurrentDBVersion()
	 */
	public XFTItem getCurrentDBVersion(boolean allowMultiples)
	{
	    ItemSearch search = new ItemSearch();
	    try {
            search.setElement(this.getGenericSchemaElement());
            search.setCriteriaCollection(this.getPkSearch(allowMultiples));
            ItemCollection items = search.exec(allowMultiples);
            return (XFTItem)items.getFirst();
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getCurrentDBVersion()
	 */
	public XFTItem getCurrentDBVersion(boolean allowMultiples,boolean allowExtension)
	{
	    ItemSearch search = new ItemSearch();
	    try {
            search.setElement(this.getGenericSchemaElement());
            search.setCriteriaCollection(this.getPkSearch(allowMultiples));
            ItemCollection items = search.exec(allowMultiples,allowExtension);
            return (XFTItem)items.getFirst();
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getCurrentDBVersion()
	 */
	public ItemI getCurrentDBMatch(boolean extend)
	{
	    try {
            if (this.hasPK())
            {
                return this.getPkMatches(extend).first();
            }

            if (this.hasUniques())
            {
                return this.getUniqueMatches(extend).first();
            }
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
        return null;
	}

	/**
	 * @param refField
	 * @param user
	 * @return ArrayList of Hashtable
	 */
	public ArrayList getChildItemIds(GenericWrapperField refField,UserI user)
	{
	    if (item_counts.containsKey(refField))
	    {
	        return (ArrayList)item_counts.get(refField);
	    }else{
		    ArrayList ids = new ArrayList();
	        try {
			    GenericWrapperElement foreign = (GenericWrapperElement)refField.getReferenceElement();
			    XFTReferenceI ref = refField.getXFTReference();
			    if (ref.isManyToMany())
			    {
			        XFTManyToManyReference many = (XFTManyToManyReference)ref;
			        boolean nullKey = false;
				    String query = "SELECT * FROM " + many.getMappingTable();
				    Iterator iter =many.getMappingColumns().iterator();
				    int counter = 0;
				    while(iter.hasNext())
				    {
				        XFTMappingColumn map = (XFTMappingColumn)iter.next();
				        if ( map.getForeignElement().getFormattedName().equalsIgnoreCase(this.getGenericSchemaElement().getFormattedName()))
		                {
			                Object o = this.getProperty(map.getForeignKey().getXMLPathString());
			                if (o == null)
			                {
			                    nullKey = true;
			                    break;
			                }else{
					            if (counter++==0)
					            {
					                query += " WHERE " + map.getLocalSqlName() + EQUALS + DBAction.ValueParser(o,map.getXmlType().getLocalType(),true);
					            }else{
					                query += " AND " + map.getLocalSqlName() + EQUALS + DBAction.ValueParser(o,map.getXmlType().getLocalType(),true);
					            }
			                }
		                }
				    }

				    query +=" ORDER BY ";
				    iter = many.getMappingColumnsForElement(foreign).iterator();
				    int count=0;
		            while(iter.hasNext())
				    {
				        XFTMappingColumn map = (XFTMappingColumn)iter.next();
				        if (count++>0)
				        {
				            query +=", ";
				        }
				        query += map.getLocalSqlName().toLowerCase();
				    }
				    query +=";";

				    if (nullKey)
				    {
				        return new ArrayList();
				    }else{
						String login = null;
						if (user != null)
						{
						    login = user.getUsername();
						}

			            XFTTable table = TableSearch.Execute(query,foreign.getDbName(),login);
			            if (table.size() > 0)
			            {

				            table.resetRowCursor();
				            while (table.hasMoreRows())
				            {
				                ArrayList al = new ArrayList();
				                Hashtable hash = table.nextRowHash();
				                iter = many.getMappingColumnsForElement(foreign).iterator();
					            while(iter.hasNext())
							    {
							        XFTMappingColumn map = (XFTMappingColumn)iter.next();

							        Object o = hash.get(map.getLocalSqlName().toLowerCase());

					                if (o == null)
					                {
					                    nullKey = true;
					                    break;
					                }else{
							            al.add(DBAction.ValueParser(o,(map.getForeignKey()),true));
					                }
							    }
					            ids.add(al);
				            }

				            if (nullKey)
						    {
						        ids= new ArrayList();
						    }

			            }else{
			                ids= new ArrayList();
			            }
				    }
			    }else{
			        XFTSuperiorReference sup = (XFTSuperiorReference)ref;
			        if (refField.isMultiple())
			        {
			            String query = "SELECT ";
			            Object[][] keyArray=foreign.getSQLKeys();
			            for (int i = 0; i < keyArray.length; i++) {
			                if (i > 0)
			                    query += ",";
			                query += " " + keyArray[i][0];
			            }

			            query += " FROM " + foreign.getSQLName() + " WHERE ";

			            Iterator keys = sup.getKeyRelations().iterator();
				        while (keys.hasNext())
				        {
				            XFTRelationSpecification spec = (XFTRelationSpecification)keys.next();
				            Object localValue = this.getProperty(spec.getForeignCol());
				            if (localValue == null)
				            {
				            }else{
				                query += spec.getLocalCol() + EQUALS +DBAction.ValueParser(localValue,(spec.getForeignKey()),true);
				            }
				        }

				        query +=" ORDER BY ";
				        for (int i = 0; i < keyArray.length; i++) {
			                if (i > 0)
			                    query += ",";
			                query += " " + keyArray[i][0];
			            }

				        String login = null;
				        if (user !=null)
				        {
				            login = user.getUsername();
				        }
				        XFTTable table = TableSearch.Execute(query,element.getDbName(),login);

					    table.resetRowCursor();
						while (table.hasMoreRows())
						{
						    Hashtable row = table.nextRowHash();
						    ArrayList al = new ArrayList();
						    for (int i = 0; i < keyArray.length; i++) {
				                al.add(DBAction.ValueParser(row.get(keyArray[i][0].toString().toLowerCase()),((GenericWrapperField)keyArray[i][3]),true));
				            }
						    ids.add(al);
						}
			        }

			    }
	        } catch (Exception e) {
	            logger.error("",e);
	            return new ArrayList();
	        }
	        item_counts.put(refField,ids);
	        return ids;
	    }
	}


	public int getChildItemCount(GenericWrapperField refField, UserI user)
	{

	    try {
		    return getChildItemIds(refField,user).size();

        } catch (Exception e) {
            logger.error("",e);
            return 0;
        }
	}



	/**
	 * @param child
	 * @throws Exception
	 */
	public void removeItem(ItemI child) throws Exception
	{
	    removeItem((XFTItem)child);
	}

	/**
	 * @param child
	 * @throws Exception
	 */
	public void removeItem(XFTItem child) throws Exception
	{
	    GenericWrapperElement e = this.getGenericSchemaElement();
		Iterator refs = e.getReferenceFieldsWXMLDisplay(true, true).iterator();
		while (refs.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) refs.next();
			if (field.isReference()) {
				GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
				if (e.getAddin().equalsIgnoreCase("") || !ref.getAddin().equalsIgnoreCase(""))
				{
					if (field.isMultiple())
					{
						ArrayList children = getChildItems(field);
						if (children.size()> 0)
						{
						    Iterator iter = children.iterator();
						    int counter = 0;
						    boolean adjustCount = false;
						    while (iter.hasNext())
						    {
						        XFTItem sub = (XFTItem)iter.next();
						        if (!adjustCount)
						        {
							        if(XFTItem.CompareItemsByPKs(sub,child))
								    {
								       props.remove(field.getId() + counter);
								       adjustCount=true;
								    }
						        }else{
						            Object o = props.remove(field.getId() + counter);
						            props.put((field.getId() + (counter-1)).intern(),o);
						        }
						        counter++;
						    }
						}
					}else{
					    XFTItem sub = (XFTItem)getField(field.getId());
						if (sub != null){
							if (XFTItem.CompareItemsByPKs(sub,child))
							{
								props.remove(field.getId());
							}
						}
					}
				}


			}
		}
	}

    public void removeChild(String xmlPath,int index) throws FieldNotFoundException,java.lang.IndexOutOfBoundsException{
        try {
            GenericWrapperElement e = this.getGenericSchemaElement();
            GenericWrapperField field = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
            if (field.getParentElement().getFullXMLName().equals(e.getFullXMLName())){
                if (field.isReference()) {
                    GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
                    if (e.getAddin().equalsIgnoreCase("") || !ref.getAddin().equalsIgnoreCase(""))
                    {
                        if (field.isMultiple())
                        {
                            ArrayList children = getChildItems(field);
                            if (children.size()<=index){
                                throw new java.lang.IndexOutOfBoundsException();
                            }
                            if (children.size()> 0)
                            {
                                Iterator iter = children.iterator();
                                int counter = 0;
                                boolean adjustCount = false;
                                while (iter.hasNext())
                                {
                                    if (!adjustCount)
                                    {
                                        if(counter==index)
                                        {
                                           props.remove(field.getId() + counter);
                                           adjustCount=true;
                                        }
                                    }else{
                                        Object o = props.remove(field.getId() + counter);
                                        if (o!=null)props.put((field.getId() + (counter-1)).intern(),o);
                                        else
                                            break;
                                    }
                                    counter++;
                                }
                            }
                        }else{
                            props.remove(field.getId());
                        }
                    }
                }
            }else{
                if (this.getGenericSchemaElement().isExtension()) {
                    Object o = this.getProperty(this.getGenericSchemaElement().getExtensionFieldName(),false);
                    if (o instanceof XFTItem)
                    {
                        ((XFTItem)o).removeChild(xmlPath,index);
                    }else if (o==null){
                        throw new FieldNotFoundException(xmlPath);
                    }else{
                        throw new FieldNotFoundException(xmlPath);
                    }
                }else{
                    throw new FieldNotFoundException(xmlPath);
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
    }


	/**
	 * @param child
	 * @param user
	 */
	public void removeChildFromDB(String xmlPath,XFTItem child,UserI user,EventMetaI c) throws SQLException,Exception
	{
	    DBAction.RemoveItemReference(this,xmlPath,child,user,c);
		SaveItemHelper.unauthorizedRemoveChild(this,xmlPath,child,user,c);
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canRead(org.nrg.xft.security.UserI)
	 */
	public boolean canRead(UserI user) throws Exception{
	    return user.canRead(this);
	}
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
	 */
	public boolean canEdit(UserI user) throws Exception{
	    return user.canEdit(this);
	}
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canEdit(org.nrg.xft.security.UserI)
	 */
	public boolean canCreate(UserI user) throws Exception{
	    return user.canCreate(this);
	}
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canActivate(org.nrg.xft.security.UserI)
	 */
	public boolean canActivate(UserI user) throws Exception{
	    return user.canActivate(this);
	}
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#canActivate(org.nrg.xft.security.UserI)
	 */
	public boolean canDelete(UserI user) throws Exception{
	    return user.canDelete(this);
	}
    /**
     * @return Returns the validationResults.
     */
    @SuppressWarnings("unused")
    private ValidationResults getValidationResults() {
        return validationResults;
    }

    public Date getActivationDate()
    {
        if (this.getMeta() != null)
		{
	        try {
	            return (Date) this.getMeta().getProperty("activation_date");
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    public Date getLastModified()
    {
        if (this.getMeta() != null)
		{
	        try {
	            return (Date) this.getMeta().getProperty("last_modified");
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    public Date getRowLastModified()
    {
        if (this.getMeta() != null)
		{
	        try {
	            return (Date) this.getMeta().getProperty("row_last_modified");
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    public Long getXFTVersion()
    {
        if (this.getMeta() != null)
		{
	        try {
	            Object o= this.getMeta().getProperty("xft_version");
	            if(o!=null){
		            if(o instanceof Long){
		            	return (Long)o;
		            }else{
		            	return Long.valueOf(o.toString());
		            }
	            }
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    public Date getInsertDate()
    {
        if (this.getMeta() != null)
		{
	        try {
	            return (Date) this.getMeta().getProperty(INSERT_DATE);
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    public String getActivationUser()
    {
        if (this.getMeta() != null)
		{
		    try {
	            return (String) this.getMeta().getProperty("activation_user_login");
	        } catch (XFTInitException e) {
	            logger.error("",e);
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	        }
		}
        return null;
    }

    private XdatUserI insert_user = null;

    public XdatUserI getInsertUser()
    {
        if(insert_user==null)
        {
            if (this.getMeta() != null)
            {
                try {
                    Integer i= (Integer)this.getMeta().getProperty("insert_user_xdat_user_id");

                    if (i!=null){
                        insert_user = XdatUser.getXdatUsersByXdatUserId(i, user, false);
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                }
            }
        }

        if(insert_user==null)
        {
            return null;
        }else{
            return insert_user;
        }
    }

    public String getStatus()
    {
        if (this.getMeta() != null)
		{
		    try {
	            return this.getMeta().getStringProperty(STATUS_STRING);
	        } catch (Exception e) {
	            logger.error("",e);
	        }
		}
        return ViewManager.ACTIVE;//default
    }

    public boolean isModified()
    {
        if (this.getMeta() != null)
		{
			try {
	            return this.getMeta().getBooleanProperty("modified",false);
	        } catch (XFTInitException e) {
	            logger.error("",e);
	            return false;
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	            return false;
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	            return false;
	        }
		}
        return false;
    }

    public boolean isShareable()
    {
        if (this.getMeta() != null)
		{
			try {
	            return this.getMeta().getBooleanProperty(SHAREABLE,false);
	        } catch (XFTInitException e) {
	            logger.error("",e);
	            return false;
	        } catch (ElementNotFoundException e) {
	            logger.error("",e);
	            return false;
	        } catch (FieldNotFoundException e) {
	            logger.error("",e);
	            return false;
	        }
		}
        return false;
    }

    public Integer getMetaDataId()
    {
        if (this.getMeta() != null)
		{
            return ((XFTItem)this.getMeta()).getIntegerProperty(META_DATA_ID);
		}else{
		    return null;
		}
    }
    /**
     * @return Returns the loading.
     */
    public boolean isLoading() {
        return loading;
    }

    /**
     * @param preLoaded The preLoaded to set.
     */
    public void setLoading(boolean l) {
        this.loading = l;

        try {
            Iterator iter = getChildItems().iterator();
            while (iter.hasNext())
            {
            	XFTItem child = (XFTItem)iter.next();
            	child.setLoading(l);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }
    /**
     * @return Returns the preLoaded.
     */
    public boolean isPreLoaded() {
        return preLoaded;
    }
    /**
     * @param preLoaded The preLoaded to set.
     */
    public void setPreLoaded(boolean preLoaded) {
        this.preLoaded = preLoaded;

        try {
            Iterator iter = getChildItems().iterator();
            while (iter.hasNext())
            {
            	XFTItem child = (XFTItem)iter.next();
            	child.setPreLoaded(preLoaded);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }
    /**
     * @return Returns the user.
     */
    public UserI getUser() {
        return user;
    }
    /**
     * @param user The user to set.
     */
    public void setUser(UserI user) {
        this.user = user;
    }
    /**
     * @return Returns the pauseDBAccess.
     */
    public boolean isPauseDBAccess() {
        return pauseDBAccess;
    }
    /**
     * @param pauseDBAccess The pauseDBAccess to set.
     */
    public void setPauseDBAccess(boolean pauseDBAccess) {
        this.pauseDBAccess = pauseDBAccess;
    }

    public XFTItem getItem()
    {
        return this;
    }

    public String getDBName()
    {
        try {
            return this.getGenericSchemaElement().getDbName();
        } catch (ElementNotFoundException e) {
            return "";
        }
    }


    /**
     * @return Returns the verifyXMLPaths.
     */
    public boolean isVerifyXMLPaths() {
        return verifyXMLPaths;
    }
    /**
     * @param verifyXMLPaths The verifyXMLPaths to set.
     */
    public void setVerifyXMLPaths(boolean verifyXMLPaths) {
        this.verifyXMLPaths = verifyXMLPaths;
    }

	public Document toJoinedXML() throws Exception
	{
	    ArrayList al = new ArrayList();
	    al.add(getItem());
	    return XMLWriter.ItemListToDOM(al,false);
	}


	public String output(String templateName)
	{
	    try {
            VelocityUtils.init();
            
            if(templateName==null)templateName=this.getGenericSchemaElement().getFormattedName() +"_text.vm";

            String path = XFTManager.GetInstance().getSourceDir() + "src/templates/text/"+ templateName;
            File f = new File(path);
            if (f.exists())
            {
                VelocityContext context = new VelocityContext();
                context.put("item",this);
                StringWriter sw = new StringWriter();

                Velocity.evaluate(context,sw,"text",FileUtils.GetContents(f));

                return sw.toString();
            }else{
                logger.info("No Velocity TEXT vm found for " + this.getGenericSchemaElement().getFullXMLName() +" at " + path);
                return this.toXML_String();
            }
        } catch (Exception e) {
            logger.error("",e);
            return this.toXML_String();
        }
	}

	public String output()
	{
	    try {
	        return output(null);
        } catch (Exception e) {
            logger.error("",e);
            return this.toXML_String();
        }
	}

	public boolean canBeRootWithBase()
	{
	    try {
            if (this.getGenericSchemaElement().canBeRootWithBase())
            {
                return true;
            }else{
                return false;
            }
        } catch (ElementNotFoundException e) {
            return false;
        }
	}



	public static final String SPECIAL_CHAR1 = "*OPEN*";
	public static final String SPECIAL_CHAR2 = "*CLOSE*";
	public static final String SPECIAL_CHAR3 = "*END_ITEM*";

	public String writeToFlatString(int count) throws IOException {
        String s = new String();
        s+="Item:(" + count + "(";
        s+=this.getXSIType();
        s+=")(";
        Enumeration enumer = props.keys();
        int localCount = count;
        while (enumer.hasMoreElements())
        {
            String key = (String)enumer.nextElement();
            Object o = props.get(key);
            if (o instanceof XFTItem)
            {
                s+="(" +key + ":XFTItem)=(";
                s+= ((XFTItem)o).writeToFlatString(++count) + ")";
            }else if (o instanceof String){
                s+="(" +key + ":)=(";
                o = StringUtils.ReplaceStr(StringUtils.ReplaceStr(((String)o),"(",SPECIAL_CHAR1),")",SPECIAL_CHAR2);

               s+= o + ")";
            }else{
                if (o instanceof Integer)
                {
                    s+="(" +key + ":string)=(";
                    s+= o + ")";
                }else if (o instanceof Float)
                {
                    s+="(" +key + ":float)=(";
                    s+= o + ")";
                }else if (o instanceof Double)
                {
                    s+="(" +key + ":double)=(";
                    s+= o + ")";
                }else{
                    s+="(" +key + ":string)=(";
                    s+= o + ")";
                }
            }
        }
        s+=")"+SPECIAL_CHAR3 +localCount + ")";

        return s;
    }

	public Object parseObject(String type, String value)
	{

	    TypeConverter converter = JAVA_CONVERTER;
	    String className = converter.convert(type);
	    if (className=="")
	    {
	        return value;
	    }else{
	        if (className.equals("java.lang.String"))
	        {
	            value = StringUtils.ReplaceStr(StringUtils.ReplaceStr(value,SPECIAL_CHAR1,"("),SPECIAL_CHAR2,")");
                return value;
	        }else if (XMLType.CleanType(type).equals("date"))
	        {
	            try{
	                return java.sql.Date.valueOf(value);
	            } catch (RuntimeException e) {
                    return value;
                }
	        }else if (XMLType.CleanType(type).equals("dateTime"))
	        {
	            try{
	                return java.sql.Timestamp.valueOf(value);
	            } catch (RuntimeException e) {
	            	Object o;
	                try {
	                    o = DateUtils.parseDateTime(value);
	                    return o;
	                } catch (ParseException e1) {
	                    return value;
	                }
                }
	        }else if (XMLType.CleanType(type).equals("time"))
	        {
	            try {
                    return java.sql.Time.valueOf(value);
                } catch (RuntimeException e) {
                    return value;
                }
	        }else if (className.equals("java.util.Date"))
	        {
	            Object o;
                try {
                    o = DateUtils.parseDateTime(value);
                    return o;
                } catch (ParseException e) {
                    return value;
                }
	        }else if (className.equals("java.lang.Boolean"))
	        {
	            return value;
	        }else{
	            try {
                    Class c = Class.forName(className);
                    Method m = c.getMethod("valueOf",new Class[]{String.class});
                    return m.invoke(null,new Object[]{value});

                } catch (Exception e) {
                    logger.error("",e);
                    return value;
                }
	        }
	    }
	}

	public void populateFromFlatString(String s) throws Exception
	{
	    if (s.startsWith("Item:("))
	    {
	        s = s.substring(6);
	        String localCountSt = s.substring(0,s.indexOf("("));
	        s = s.substring(s.indexOf("(")+1);

	        int index = s.indexOf(")(");

	        //REMOVE NAME(
	        s = s.substring(index+2);

	        while (s.startsWith("("))
	        {
	            String field = s.substring(1,s.indexOf(COLON));
	            s= s.substring(s.indexOf(COLON)+1);
	            String type = s.substring(0,s.indexOf(")=("));

	            s= s.substring(s.indexOf(")=(")+3);

	            if (s.startsWith("Item:("))
	            {
	                String childCountSt = s.substring(6,s.indexOf("(",6));
	                int item_num= Integer.parseInt(childCountSt);
	                int end_index = s.indexOf(SPECIAL_CHAR3 +item_num +")");
	                if (end_index==-1)
	                {
	                    throw new Exception();
	                }
	                end_index += childCountSt.length()+11;
	                String value = s.substring(0,end_index);
	                s = s.substring(end_index +1);

	                String childName = value.substring(7 + childCountSt.length(),value.indexOf(")"));
	        		XFTItem child=null;
	        		child = XFTItem.NewItem(childName,user);
	                child.populateFromFlatString(value);
	                this.setField(field,child);
	            }else{
		            String value = s.substring(0,s.indexOf(")"));
		            s = s.substring(s.indexOf(")")+1);
		            Object o =parseObject(type,value);
		            this.setField(field,o);
	            }
	        }

	        if(isMetaElement()){
	        	this.internValues();
	        }
	        
	        //REMOVE ENDING )
	        s = s.substring(0,s.indexOf(")" +SPECIAL_CHAR3 +localCountSt + ")"));
	    }
	}

	Boolean isMeta=null;
	private boolean isMetaElement(){
		if(isMeta==null){
			if(this.getXSIType().endsWith("_meta_data")){
				isMeta=Boolean.TRUE;
			}else{
				isMeta=Boolean.FALSE;
			}
		}
		
		return isMeta.booleanValue();
	}


    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        String s = (String)in.readObject();
        try {
            String elementName = s.substring(8,s.indexOf(")"));
            setElement(GenericWrapperElement.GetElement(elementName));
            populateFromFlatString(s);
        } catch (Exception e) {
            logger.error("",e);
        }
    }
    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(writeToFlatString(0));
    }

    public static XFTItem SelectItemByIds(GenericWrapperElement element, Object[] ids, UserI user, boolean allowMultiples,boolean preventLoop) throws Exception
    {
        return DBAction.SelectItemByIDs(element,ids,user,allowMultiples,preventLoop);
    }

    public boolean hasXMLContent(boolean allowDBAccess)
    {
        boolean hascontent =false;
        try {

            XMLWrapperElement element = (XMLWrapperElement)XFTMetaManager.GetWrappedElementByName(XMLWrapperFactory.GetInstance(),getXSIType());

            Object[] attributesArray = element.getAttributes().toArray();
            for (int i=0;i<attributesArray.length;i++){
                XMLWrapperField attField = (XMLWrapperField)attributesArray[i];
            	if (attField.isReference())
            	{
            			XFTItem ref = (XFTItem)getProperty(attField.getId());
            			if (ref != null)
            			{
            			    boolean temp= ref.hasXMLContent(allowDBAccess);
            			    if (temp)
            			    {
            			        return true;
            			    }
            			}
            	}else
            	{
            		Object o = getProperty(attField.getId());
            		if (o != null)
            		{
            		    return true;
            		}else{
            		    if (attField.isRequired())
            		    {
            		        return true;
            		    }else{

            		    }
            		}
            	}
            }

            Iterator childElements = element.getChildren().iterator();
            while(childElements.hasNext())
            {
            	XMLWrapperField xmlField = (XMLWrapperField)childElements.next();
            	if (xmlField.getExpose())
            	{
            	    hasXMLContent(xmlField,allowDBAccess);
            	}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return hascontent;
    }

    public boolean hasXMLContent(XMLWrapperField field,boolean allowDBAccess)
    {
        boolean hascontent =false;
        try {
            if (field.isReference())
            {
                if (field.isMultiple())
                {
                    if (allowDBAccess){
                        if (getChildItemCount(field,this.user)>0){
                            return true;
                        }
                    }else{
                        try {
                            if (getCurrentChildItems(field,user,false).size()>0){
                                return true;
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        }
                    }
                }else{
                    try {
                        if (getProperty(field.getId()) != null)
                        {
                        	if (getProperty(field.getId()) instanceof XFTItem)
                        	{
                        	    XFTItem child = (XFTItem)getProperty(field.getId());
                        	    return child.hasXMLContent(allowDBAccess);
                        	}
                        }

                        return false;
                    } catch (XFTInitException e) {
                        return false;
                    } catch (ElementNotFoundException e) {
                        return false;
                    } catch (FieldNotFoundException e) {
                        return false;
                    }
                }
            }else{
                Iterator attributes = field.getAttributes().iterator();
    			while (attributes.hasNext())
    			{
    				XMLWrapperField x = (XMLWrapperField)attributes.next();
    				if (x.getXMLType().getLocalType().equals("string"))
    				{
    				    try {
                            Object o = getProperty(x.getId());
                            if (o != null)
                            {
                        	    return true;
                            }else{
                                if (x.isRequired())
                                {
                            	    return true;
                                }else{
                                }
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        }
    				}else{
    				    try {
                            if(getProperty(x.getId())!=null)
                            {
                                Object o = getProperty(x.getId());
                            	if (o != null)
                            	{
                            	    return true;
                            	}else{
                            	    if (x.isRequired())
                            	    {
                                	    return true;
                            	    }else{
                            	    }
                            	}
                            }else{
                                if (x.isRequired()){
                            	    return true;
                                }
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        }
    				}
    			}

            	if (field.getChildren().size() > 0)
            	{
            	    Iterator childElements2 = field.getChildren().iterator();
            		while(childElements2.hasNext())
            		{
            			XMLWrapperField xwf = (XMLWrapperField)childElements2.next();
            			if (xwf.getExpose())
            			{
            			    if( hasXMLContent(xwf,allowDBAccess)){
            			        return true;
            			    }
            			}
            		}
            	}else
            	{
            	    try {

                        if (field.getXMLType()==null){
                        }else if((getProperty(field.getId()) != null))
                        {
                            return true;
                        }else if (field.isRequired()){
                            return true;
                        }
                    }catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
            	}
            }
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return hascontent;
    }

    /**
     * @param out
     * @throws java.lang.IllegalArgumentException
     * @throws org.xml.sax.SAXException
     */
    public void toXML(OutputStream out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException{
        try {
            SAXWriter writer = new SAXWriter(out,allowDBAccess);
            writer.write(this);
        } catch (TransformerConfigurationException e) {
            logger.error("",e);
        } catch (TransformerFactoryConfigurationError e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }

    /**
     * @param out
     * @throws java.lang.IllegalArgumentException
     * @throws org.xml.sax.SAXException
     */
    public void toXML(Writer out,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException{
        try {
            SAXWriter writer = new SAXWriter(out,allowDBAccess);
            writer.write(this);
        } catch (TransformerConfigurationException e) {
            logger.error("",e);
        } catch (TransformerFactoryConfigurationError e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }

    /**
     * @param out
     * @throws java.lang.IllegalArgumentException
     * @throws org.xml.sax.SAXException
     */
    public void toXML(OutputStream out,String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException{
        try {
            SAXWriter writer = new SAXWriter(out,allowDBAccess);
            if (schemaDir !=null){
                writer.setAllowSchemaLocation(true);
                writer.setLocation(schemaDir);
            }
            writer.write(this);
        } catch (TransformerConfigurationException e) {
            logger.error("",e);
        } catch (TransformerFactoryConfigurationError e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }

    /**
     * @param out
     * @throws java.lang.IllegalArgumentException
     * @throws org.xml.sax.SAXException
     */
    public void toXML(Writer out,String schemaDir,boolean allowDBAccess) throws java.lang.IllegalArgumentException, org.xml.sax.SAXException{
        try {
            SAXWriter writer = new SAXWriter(out,allowDBAccess);
            if (schemaDir !=null){
                writer.setAllowSchemaLocation(true);
                writer.setLocation(schemaDir);
            }
            writer.write(this);
        } catch (TransformerConfigurationException e) {
            logger.error("",e);
        } catch (TransformerFactoryConfigurationError e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
    }


    /**
	 * @deprcated Use {@link #getChildrenOfTypeWithPaths(String,boolean)} instead
	 */
	public List<XFTItem> getChildrenOfType(String xsiType){
		return getChildrenOfType(xsiType, true);
	}

	public List<XFTItem> getChildrenOfType(String xsiType, boolean preventLoop){
		List<XFTItem> _return = new ArrayList<XFTItem>();
        try {
            ArrayList<GenericWrapperField> fields = this.getGenericSchemaElement().getReferenceFields(true);

            for(GenericWrapperField gwf : fields){
                if (!gwf.isPossibleLoop() || !preventLoop){
                    boolean localLoop = preventLoop;
                    if (gwf.getPreventLoop())
                        localLoop = true;

                    if (gwf.isMultiple()){
                        if (gwf.getReferenceElement().getGenericXFTElement().instanceOf(xsiType)){
                            try {
                               ItemCollection items= this.getChildItemCollection(gwf);
                               _return.addAll(items.items());
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }else{
                            try {
                                ItemCollection items= this.getChildItemCollection(gwf);
                                if (items.size()>0){
                                    Iterator iter = items.iterator();
                                    while(iter.hasNext()){
                                        XFTItem temp = (XFTItem)iter.next();
                                        _return.addAll(temp.getChildrenOfType(xsiType,localLoop));
                                    }
                                }
                             } catch (FieldNotFoundException e) {
                                 logger.error("",e);
                             }
                        }
                    }else{
                        if (gwf.getReferenceElement().getGenericXFTElement().instanceOf(xsiType)){
                            try {
                               ItemCollection items= this.getChildItemCollection(gwf);
                               _return.addAll(items.items());
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }else{
                            try {
                                ItemCollection items= this.getChildItemCollection(gwf);
                                if (items.size()>0){
                                    Iterator iter = items.iterator();
                                    while(iter.hasNext()){
                                        XFTItem resource = (XFTItem)iter.next();
                                        _return.addAll(resource.getChildrenOfType(xsiType,localLoop));
                                    }
                                }
                             } catch (FieldNotFoundException e) {
                                 logger.error("",e);
                             }
                        }
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return _return;
    }


    /**
	 * @deprcated Use {@link #getChildrenOfTypeWithPaths(String,boolean)} instead
	 */
	public Hashtable<String,XFTItem> getChildrenOfTypeWithPaths(String xsiType){
		return getChildrenOfTypeWithPaths(xsiType, true);
    }

	public Hashtable<String,XFTItem> getChildrenOfTypeWithPaths(String xsiType, boolean preventLoop){
        Hashtable<String,XFTItem> _return = new Hashtable<String,XFTItem>();
        try {
            ArrayList<GenericWrapperField> fields = this.getGenericSchemaElement().getReferenceFields(true);

            for(GenericWrapperField gwf : fields){
                if (!gwf.isPossibleLoop() || !preventLoop){
                    boolean localLoop = preventLoop;
                    if (gwf.getPreventLoop())
                        localLoop = true;

                    if (gwf.isMultiple()){
                        if (gwf.getReferenceElement().getGenericXFTElement().instanceOf(xsiType)){
                            try {
                               ItemCollection items= this.getChildItemCollection(gwf);
                               if (items.size()>0){
                                   Iterator iter = items.iterator();
                                   String s = gwf.getXMLPathString();
                                   while(iter.hasNext()){
                                       XFTItem resource = (XFTItem)iter.next();

                                       _return.put(s + "[xnat_abstractresource_id=" + resource.getProperty("xnat_abstractresource_id") + "]", resource);
                                   }
                               }
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }else{
                            try {
                                ItemCollection items= this.getChildItemCollection(gwf);
                                if (items.size()>0){
                                    Iterator iter = items.iterator();
                                    String s = gwf.getXMLPathString();
                                    while(iter.hasNext()){
                                        XFTItem temp = (XFTItem)iter.next();

                                        Hashtable<String,XFTItem> hash = temp.getChildrenOfTypeWithPaths(xsiType,localLoop);
                                        if (hash.size()>0){
                                            for (String key : hash.keySet()){
                                                XFTItem resource = hash.get(key);
                                                _return.put(s + "[" + temp.getPKString() + "]/" + key, resource);
                                            }
                                        }

                                    }
                                }
                             } catch (FieldNotFoundException e) {
                                 logger.error("",e);
                             }
                        }
                    }else{
                        if (gwf.getReferenceElement().getGenericXFTElement().instanceOf(xsiType)){
                            try {
                               ItemCollection items= this.getChildItemCollection(gwf);
                               if (items.size()>0){
                                   Iterator iter = items.iterator();
                                   String s = gwf.getXMLPathString();
                                   while(iter.hasNext()){
                                       XFTItem resource = (XFTItem)iter.next();

                                       _return.put(s, resource);
                                   }
                               }
                            } catch (FieldNotFoundException e) {
                                logger.error("",e);
                            }
                        }else{
                            try {
                                ItemCollection items= this.getChildItemCollection(gwf);
                                if (items.size()>0){
                                    Iterator iter = items.iterator();
                                    String s = gwf.getXMLPathString();
                                    while(iter.hasNext()){
                                        XFTItem resource = (XFTItem)iter.next();

                                        Hashtable<String,XFTItem> hash = resource.getChildrenOfTypeWithPaths(xsiType,localLoop);
                                        if (hash.size()>0){
                                            for (String key : hash.keySet()){
                                                XFTItem temp = hash.get(key);
                                                _return.put(s + "/" + key, temp);
                                            }
                                        }

                                    }
                                }
                             } catch (FieldNotFoundException e) {
                                 logger.error("",e);
                             }
                        }
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return _return;
    }

    public String getPKString(){
        StringBuffer sb = new StringBuffer();
        try {
            Hashtable<String,Object> hash = (Hashtable<String,Object>)this.getPkValues();
            for (String key: hash.keySet()){
                sb.append(key).append(EQUALS).append(hash.get(key));
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        return sb.toString();
    }
    public String getPKValueString(){
        StringBuffer sb = new StringBuffer();
        try {
            Hashtable<String,Object> hash = (Hashtable<String,Object>)this.getPkValues();
            int count =0;
            for (String key: hash.keySet()){
                if (count++>0)
                    sb.append(",");
                sb.append(hash.get(key));
            }
        } catch (Exception e) {
            logger.error("",e);
        }
        return sb.toString();
    }
    
    public void internValues(){
    	for(Map.Entry<String, Object> e:this.props.entrySet()){
    		if(e.getValue() instanceof String){
    			props.put(e.getKey(),StringUtils.intern((String)e.getValue()));
    		}else if(e.getValue() instanceof ItemI){
    			((ItemI)e.getValue()).getItem().internValues();
    		}
    	}
    }
    
    public static String identity(XFTItem i){
    	StringBuilder sb=new StringBuilder();
    	
    	try {
        	GenericWrapperElement gwe = i.getGenericSchemaElement().ignoreHistory();
        	sb.append(gwe.getXSIType());
        	
        	Iterator iter = gwe.getPkNames().iterator();
    		while (iter.hasNext())
    		{
    			Object v = i.getProperty((String)iter.next());
    			if(v!=null){
        			sb.append(v);
    			}
    		}
		} catch (XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		} catch (FieldNotFoundException e) {
			logger.error("",e);
		}
    	return sb.toString();
    }
}

