/*
 * core: org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.Wrappers.GenericWrapper;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.TypeConverter.PGSQLMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.MetaFieldCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.*;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@SuppressWarnings({"unchecked","rawtypes"})
public class GenericWrapperElement extends XFTElementWrapper implements SchemaElementI{	
	private static final Logger    logger                 = LoggerFactory.getLogger(GenericWrapperElement.class);
	private static       Hashtable HIDDEN_SUPERIOR_FIELDS = new Hashtable();
	
    
	private ArrayList allFields = null;
	private ArrayList<Object[]> allFieldNames = null;
	private ArrayList allFieldsWAddIns = null;
	private ArrayList<GenericWrapperField> directFields = null;
	private ArrayList superiorReferences = null;
	private ArrayList<GenericWrapperField> undefinedReferences = null;

	private ArrayList<Object[]> possibleParents = null;
	
	private TorqueElement te = null;
	
	private MetaFieldCollection metaFields = null;
	
	private ArrayList referencedElements = null;
	private ArrayList<ArrayList> extendedElements = null;
	
	private ArrayList<XFTFieldWrapper> directNoFilter = null;
	private ArrayList<SchemaElementI> _possibleExtenders = null;
	
	private final static Map<String,GenericWrapperElement> ALL_ELEMENTS_CACHE = new HashMap<>();
	private final static Map<String, String[]> XMLPATH_TABLES_CACHE = new HashMap<>();

	private String _finalSqlName=null;
	private String _finalFormattedName=null;
	/**
	 * Get GenericWrapperElement with a matching XMLType
	 * @param t
	 * @return Returns the GenericWrapperElement with a matching XMLType
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static GenericWrapperElement GetElement(XMLType t) throws XFTInitException,ElementNotFoundException
	{
		GenericWrapperElement gwe=ALL_ELEMENTS_CACHE.get(t.getFullForeignType().toLowerCase());
	    if (gwe == null)
	    {
			try {
	            gwe = (GenericWrapperElement) XFTMetaManager.GetWrappedElementByXMLType(GenericWrapperFactory.GetInstance(),t);
	            ALL_ELEMENTS_CACHE.put(XftStringUtils.intern(t.getFullForeignType().toLowerCase()), gwe);
	        } catch (RuntimeException e) {
		        logger.error("GetElement:" + t.getFullForeignType().toLowerCase());
	            logger.error("",e);
	            throw new ElementNotFoundException(t.getFullForeignType().toLowerCase());
	        }
	    }
	    
	    return gwe;
	}
    
    
	
	/**
	 * Gets the GenericWrapperElement with a matching name.  If the matching element is not
	 * found, then the URI is used to assign the correct XMLType prefix to the name, and the search
	 * is performed again.
	 * 
	 * @param name
	 * @param URI
	 * @return Returns the GenericWrapperElement with a matching name
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static GenericWrapperElement GetElement(String name,String URI) throws XFTInitException,ElementNotFoundException
	{
	    try{
	    	String xsiType=name;
	    	if(name.contains(":")){
	    		try {
					return GetElement(name);
				} catch (RuntimeException e) {
					xsiType=name.substring(name.indexOf(":")+1);
				}
	    	}
	    	
	    	if(StringUtils.isNotBlank(URI)){
		    	String abbr = XFTMetaManager.TranslateURIToPrefix(URI);
		    	
		    	if(StringUtils.isNotBlank(abbr)){
		    		xsiType=abbr+":"+name;
		    	}
	    	}
	        return GetElement(xsiType);
	    } catch (RuntimeException e) {
	        return GetElement(name);
        }
	}
	
	/**
	 * Gets the GenericWrapperElement with a matching name (Must have prefix, unless it is a proper name).
	 * @param name
	 * @return Returns the GenericWrapperElement with a matching name
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static GenericWrapperElement GetElement(String name) throws XFTInitException,ElementNotFoundException
	{
        if (name!=null)
        {
        	GenericWrapperElement gwe=(GenericWrapperElement)ALL_ELEMENTS_CACHE.get(name.toLowerCase());
            if (gwe == null)
            {
                try {
                    gwe = (GenericWrapperElement) XFTMetaManager.GetWrappedElementByName(GenericWrapperFactory.GetInstance(),name);
                    ALL_ELEMENTS_CACHE.put(XftStringUtils.intern(name.toLowerCase()), gwe);
                } catch (RuntimeException e) {
                    logger.error("GetElement:" + name);
                    logger.error("",e);
                    throw new ElementNotFoundException(name);
                }
            }
            
            return gwe;
        }else{
            throw new ElementNotFoundException("NULL");
        }
	}
    
    public static ArrayList<GenericWrapperElement> GetAllElements(boolean allowAddOns) throws XFTInitException,ElementNotFoundException{
        ArrayList<GenericWrapperElement> elements = new ArrayList<GenericWrapperElement>();
        Iterator iter = XFTMetaManager.GetElementNames().iterator();
        while (iter.hasNext())
        {
            String s = (String)iter.next();
            GenericWrapperElement gwe = GenericWrapperElement.GetElement(s);
            if (gwe.getAddin().equalsIgnoreCase("") || allowAddOns)
            {
                elements.add(gwe);
            }
        }
        
        return elements;
    }

	/**
	 * @return Returns whether the element contains the stated key
	 */
	public boolean containsStatedKey() {
		boolean hasKey = false;
		Iterator iter = this.getAllFields().iterator();
		while (iter.hasNext()) {
			GenericWrapperField gwf = (GenericWrapperField) iter.next();
			if (gwf.isPrimaryKey()) {
				hasKey = true;
				break;
			}
		}
		return hasKey;
	}

	/**
	 * Returns a collection of XFTFields which are not specifically defined in the schema element
	 * but are used behind the scenes.  These include a default primary key field (if one was not
	 * specified in the schema) and any undefined references.
	 * 
	 * @see org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement#getAllFieldsWAddIns()
	 * @see org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement#getDirectFields()
	 * 
	 * @return ArrayList of XFTFields.
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public ArrayList<XFTFieldWrapper> getAddIns()
		throws XFTInitException, ElementNotFoundException {
		ArrayList<XFTFieldWrapper> al = new ArrayList<XFTFieldWrapper>();
		if (!containsStatedKey()) {
			al.add(getDefaultKey());
		}

		al.addAll(getUndefinedReferences());
		al.trimToSize();
		return al;
	}

	/**
	 * Returns the Alias defined in the TorqueElement
	 * @return Returns the Alias defined in the TorqueElement
	 */
	public String getAlias() {
		return getTorqueElement().getAlias();
	}
    
    public boolean isSkipSQL()
    {
        return this.getWrapped().isSkipSQL();
    }

	/**
	 * Returns a collection of Object Arrays which specify the sql name, data type, xmlOnly values and
	 * the GenericWrapperField for every field except multiple reference fields.
	 * <BR>0: sql name
	 * <BR>1: type
	 * <BR>2: xmlOnly ('true'|'false')
	 * <BR>3: GenericWrapperField
	 * @return ArrayList of Object[4]
	 */
	public synchronized ArrayList<Object[]> getAllFieldNames()
		throws ElementNotFoundException, XFTInitException {
	    if (allFieldNames==null)
	    {
			allFieldNames = new ArrayList<Object[]>();
			Iterator fields = this.getAllFieldsWAddIns(true, true).iterator();
			while (fields.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) fields.next();
				if (field.isReference()) {
					if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(this.getFullXMLName())) || (!field.isMultiple()))
					{
					    Iterator refs = field.getLocalRefNames().iterator();
						while (refs.hasNext()) {
							ArrayList ref = (ArrayList) refs.next();
							Object[] sub =
								{
									(String) ref.get(0),
									((GenericWrapperField) ref.get(1))
										.getXMLType()
										.getFullLocalType(),
									"false",
									field };
							allFieldNames.add(sub);
						}
					}
				} else {
					if (field.isXMLOnly()) {
						Object[] sub =
							{
								field.getSQLName(),
								field.getXMLType().getFullLocalType(),
								"true",
								field};
						if (sub[1] != "") {
						    allFieldNames.add(sub);
						}
					} else {
						Object[] sub =
							{
								field.getSQLName(),
								field.getXMLType().getFullLocalType(),
								"false",
								field};
						if (sub[1] != "") {
						    allFieldNames.add(sub);
						}
					}
				}
			}
			allFieldNames.trimToSize();
	    }
		return allFieldNames;
		
	}
	
	/**
	 * Hashtable of all possible fieldIds (including extension fields) with (DATA,SINGLE,or MULTI) as the value
	 * @return Returns Hashtable of all possible fieldIds
	 */
	public Hashtable getAllFieldIDs()throws ElementNotFoundException, XFTInitException 
	{
		Hashtable hash = new Hashtable();
		
		Iterator fields = this.getAllFieldsWAddIns(true, true).iterator();
		while (fields.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) fields.next();
			if (field.isReference()) {
				if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(this.getFullXMLName())) || (!field.isMultiple()))
				{
					Iterator refs = field.getLocalRefNames().iterator();
					while (refs.hasNext()) {
						ArrayList ref = (ArrayList) refs.next();
						hash.put(((String) ref.get(0)).toLowerCase(),"DATA");
					}
					hash.put(field.getId(),"SINGLE");
				}else
				{
					hash.put(field.getId(),"MULTI");
				}
			}else{
				hash.put(field.getId(),"DATA");
			}
		}
		
//		if (isExtension()) {
//			XMLType extended = getExtensionType();
//			GenericWrapperElement childElement =GenericWrapperElement.GetElement(extended);
//			hash.putAll(childElement.getAllFieldIDs());
//		}
		
		return hash;
	}
	
	/**
	 * Returns Hashtable of all possible root xml-field names.
	 * <BR>Value is the GenericWrapperField;
	 * @return Returns Hashtable of all possible root xml-field names
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public Hashtable getAllPossibleXMLFieldNames()throws ElementNotFoundException, XFTInitException 
	{
		Hashtable hash = new Hashtable();
		Iterator dF = this.getDirectFieldsNoFilter().iterator();
		while (dF.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)dF.next();
			hash.put(f.getName().toLowerCase(),f);
		}
		
		if (this.getStatedPrimaryKeyFields().size()== 0)
		{
			GenericWrapperField f = (GenericWrapperField)getDefaultKey();
			hash.put(f.getName().toLowerCase(),f);
		}
		
		
		return hash;
	}

	/**
	 * Returns all defined 'Leaf Node' Fields.
	 * @return arrayList of GenericWrapperFields
	 */
	public synchronized ArrayList getAllFields() {
		if (this.allFields == null) {
			allFields = new ArrayList();
			Iterator iter = wrapped.getSortedFields().iterator();
			while (iter.hasNext()) {
				XFTField xf = (XFTField) iter.next();
				GenericWrapperField gwf =
					(GenericWrapperField) getFactory().wrapField(xf);

				if (GenericWrapperField.IsLeafNode(xf))
					allFields.add(gwf);
				if (!gwf.isXMLOnly())
					allFields.addAll(gwf.getAllFields());
			}
		}
		return allFields;
	}

	/**
	 * Returns the collection of fields including XMLOnly fields (if allowXmlOnly) and
	 * multi-reference fields (if allowMultiples).
	 * @param allowXmlOnly
	 * @param allowMultiples
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getAllFields(
		boolean allowXmlOnly,
		boolean allowMultiples) {
		ArrayList _return = new ArrayList();
		Iterator al = getAllFields().iterator();
		while (al.hasNext()) {
			GenericWrapperField gwf = (GenericWrapperField) al.next();
			if ((gwf.isXMLOnly() == allowXmlOnly) || (allowXmlOnly)) {
				if ((allowMultiples == gwf.isMultiple()) || (allowMultiples))
					_return.add(gwf);
			}
		}
		return _return;
	}

	/**
	 * Returns all 'leaf nodes' fields including addIn fields.
	 * @return Returns all 'leaf nodes' fields including addIn fields
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private synchronized ArrayList getAllFieldsWAddIns()
		throws XFTInitException, ElementNotFoundException {
		if (this.allFieldsWAddIns == null) {
			allFieldsWAddIns = new ArrayList();
			allFieldsWAddIns.addAll(getAllFields());

			allFieldsWAddIns.addAll(getAddIns());
		}
		return this.allFieldsWAddIns;
	}
	
	/**
	 * Returns all 'leaf nodes' fields including addIn fields including 
	 * xmlOnly fields (if allowXmlOnly) and multi-references (if allowMultiples)
	 * @param allowXmlOnly
	 * @param allowMultiples
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getAllFieldsWAddIns(
		boolean allowXmlOnly,
		boolean allowMultiples)
		throws ElementNotFoundException, XFTInitException {
		ArrayList _return = new ArrayList();
		Iterator al = getAllFieldsWAddIns().iterator();
		while (al.hasNext()) {
			GenericWrapperField gwf = (GenericWrapperField) al.next();
			if (gwf.isXMLOnly() == allowXmlOnly || allowXmlOnly) {
				if ((allowMultiples) || (allowMultiples == gwf.isMultiple()))
					_return.add(gwf);
			}
		}
		return _return;
	}

	/**
	 * Gets all primary key fields including stated PKs and default PKs.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList<GenericWrapperField> getAllPrimaryKeys(){
		ArrayList al = new ArrayList();

		try {
            al.addAll(getStatedPrimaryKeyFields());

            if (al.size() == 0) {
            	al.add(getDefaultKey());
            }

            al.trimToSize();
        } catch (XFTInitException e) {
            logger.error("",e);
        }
		return al;
	}
	
	/**
	 * Returns the sql names of the pk fields for this item.
	 * @return ArrayList of strings
	 */
	public List<String> getPkNames() throws org.nrg.xft.exception.XFTInitException
	{
		List<String> keyNames = new ArrayList<String>();

		Iterator keys = getAllPrimaryKeys().iterator();

		while (keys.hasNext())
		{
			keyNames.add(((GenericWrapperField)keys.next()).getId());
		}

		return keyNames;
	}
	
	/**
	 * Returns period-delimited string of data types which this element extends.
	 * @return Returns period-delimited string of data types which this element extends
	 */
	private String getPrimaryElements()
	{
	    try {
            ArrayList al = getAllPrimaryKeys();
            GenericWrapperField field = (GenericWrapperField)al.get(0);
            String [] layers = GenericWrapperElement.TranslateXMLPathToTables(field.getXMLPathString(getFullXMLName()));
            return layers[0];
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            return getFullXMLName();
        }
	}
	
	/**
	 * @return Returns the base class String
	 */
	public String getBaseClass() {
		return getTorqueElement().getBaseClass();
	}

	/**
	 * @return Returns the base peer String
	 */
	public String getBasePeer() {
		return getTorqueElement().getBasePeer();
	}

	/**
	 * Gets the DB name from the elements parent XFTDataModel.
	 * @return	Returns the DB name from the elements parent XFTDataModel
	 */
	public String getDbName() {
		return this.wrapped.getDataModel().getDb();
	}

	/**
	 * Get default key for this element.  (Defaults to (getSQLName() + "_id")
	 * @return Returns the default key for this element wrapped in a XFTFieldWrapper
	 */
	public XFTFieldWrapper getDefaultKey() {
		XFTDataField data =
			XFTDataField.GetEmptyKey(
				this.wrapped.getSchemaPrefix(),
				this.wrapped.getAbsoluteParent().getSchema());
		data.setName(getSQLName() + "_id");
		data.setFullName(getSQLName() + "_id");
		data.setParent(this.wrapped);
		data.setMinOccurs("0");
		return getFactory().wrapField(data);
	}

	/**
	 * If there is a level 1 (direct child) field with the matching xml name, then
	 * that GenericWrapperField is returned.
	 * @param name (XML name)
	 * @return GenericWrapperField
	 */
	private Hashtable _directFieldMatchers = null;
	private Hashtable _addInFieldMatchers = null;
	
	private Map<String,GenericWrapperField> _returnedDirectFields=new Hashtable<String,GenericWrapperField>();
	
	public synchronized GenericWrapperField getDirectField(String name)
		throws FieldNotFoundException, ElementNotFoundException, XFTInitException {
		
		GenericWrapperField gwf=_returnedDirectFields.get(name);
		if(gwf!=null)return gwf;
		
	    String lower = name.toLowerCase();
	    
	    if (_directFieldMatchers == null)
	    {
	        _directFieldMatchers = new Hashtable();
	        
	        Iterator iter = this.getDirectFieldsNoFilter().iterator();
			while (iter.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) iter.next();

				if (field.getName()!=null)
				{
				    String lowerCaseName = field.getName().toLowerCase();
		            if (!_directFieldMatchers.containsKey(lowerCaseName))
		            {
						_directFieldMatchers.put(lowerCaseName,field);
		            }
				}
	            
				if (field.isReference())
				{
				    String referenceName = field.getReferenceElementName().getLocalType();
				    String extensionName = this.getExtensionFieldName();
			    
				    if (this.getExtensionType()==null || !referenceName.equalsIgnoreCase(extensionName))
				    {
				        if (!field.isMultiple())
					    {
						    XFTReferenceI ref = field.getXFTReference();
						    if (! ref.isManyToMany())
						    {
						        XFTSuperiorReference sup = (XFTSuperiorReference)ref;
						        Iterator specs = sup.getKeyRelations().iterator();
						        while (specs.hasNext())
						        {
							        XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
							        if (spec.getLocalCol()!=null && spec.getLocalCol()!="")
							        {
							            XFTDataField data = XFTDataField.GetEmptyField();
							            data.setName(spec.getLocalCol());
							            data.setFullName(spec.getLocalCol());
							            data.setExpose("false");
							            data.setXmlDisplay("false");
							            data.setXMLType(spec.getSchemaType());
							            data.setParent(this.wrapped);
							            
							            String lowerCaseName = spec.getLocalCol().toLowerCase();
							            if (!_directFieldMatchers.containsKey(lowerCaseName))
							            {
											_directFieldMatchers.put(lowerCaseName,GenericWrapperFactory.GetInstance().wrapField(data));
							            }
							        }
						        }
						    }
					    }else if (field.getRelationType().equalsIgnoreCase("single"))
					    {
					        XFTReferenceI ref = field.getXFTReference();
					        XFTSuperiorReference sup = (XFTSuperiorReference)ref;
					        Iterator specs = sup.getKeyRelations().iterator();
					        while (specs.hasNext())
					        {
						        XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
						        if (spec.getLocalCol()!=null && spec.getLocalCol()!="")
						        {
						            XFTDataField data = XFTDataField.GetEmptyField();
						            data.setName(spec.getLocalCol());
						            data.setFullName(spec.getLocalCol());
						            data.setExpose("false");
						            data.setXmlDisplay("false");
						            data.setXMLType(spec.getSchemaType());
						            data.setParent(this.wrapped);

						            String lowerCaseName = spec.getLocalCol().toLowerCase();
						            if (!_directFieldMatchers.containsKey(lowerCaseName))
						            {
										_directFieldMatchers.put(lowerCaseName,GenericWrapperFactory.GetInstance().wrapField(data));
						            }
						        }
					        }
					    }
				    }
				}
			}
	    }
	    
	    if (_directFieldMatchers.containsKey(lower))
	    {
	        gwf= (GenericWrapperField)_directFieldMatchers.get(lower);
	        _returnedDirectFields.put(name,gwf);
	        return gwf;
	    }

	    if (_addInFieldMatchers == null)
	    {
	        _addInFieldMatchers=new Hashtable();

			Iterator addIns = getAddIns().iterator();
			while (addIns.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) addIns.next();

				if (field.getName()!=null)
				{
				    String lowerCaseName = field.getName().toLowerCase();
		            if (!_addInFieldMatchers.containsKey(lowerCaseName))
		            {
		                _addInFieldMatchers.put(lowerCaseName,field);
		            }
				}
				
				if (field.isReference())
				{
				    if (!field.isMultiple())
				    {
					    XFTReferenceI ref = field.getXFTReference();
					    if (! ref.isManyToMany())
					    {
					        XFTSuperiorReference sup = (XFTSuperiorReference)ref;
					        Iterator specs = sup.getKeyRelations().iterator();
					        while (specs.hasNext())
					        {
						        XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
						        if (spec.getLocalCol()!=null && spec.getLocalCol()!="")
						        {
						            XFTDataField data = XFTDataField.GetEmptyField();
						            data.setName(spec.getLocalCol());
						            data.setFullName(spec.getLocalCol());
						            data.setExpose("false");
						            data.setXmlDisplay("false");
						            data.setXMLType(spec.getSchemaType());
						            data.setParent(this.getWrapped());
						            String lowerCaseName = spec.getLocalCol().toLowerCase();
						            if (!_addInFieldMatchers.containsKey(lowerCaseName))
						            {
						                _addInFieldMatchers.put(lowerCaseName,GenericWrapperFactory.GetInstance().wrapField(data));
						            }
						        }
					        }
					    }
				    }
				}
			}
	    }

	    if (_addInFieldMatchers.containsKey(lower))
	    {
	    	 gwf= (GenericWrapperField)_addInFieldMatchers.get(lower);
		     _returnedDirectFields.put(name,gwf);
		     return gwf;
	    }

		
		throw new FieldNotFoundException(name);
	}

	/**
	 * Get level 1 (direct child) 'leaf node' fields for this element
	 * @return ArrayList of GenericWrapperFields
	 */
	public synchronized ArrayList<GenericWrapperField> getDirectFields() {
		if (this.directFields == null) {
			directFields = new ArrayList<GenericWrapperField>();
			Iterator iter = wrapped.getSortedFields().iterator();
			while (iter.hasNext()) {
				XFTField xf = (XFTField) iter.next();
				if (GenericWrapperField.IsLeafNode(xf))
					directFields.add((GenericWrapperField)getFactory().wrapField(xf));
			}
		}
		return directFields;
	}

	/**
	 * Get level 1 (direct child) fields for this element
	 * @return Returns a list of this element's children (but not their children's children)
	 */
	
	public synchronized ArrayList<XFTFieldWrapper> getDirectFieldsNoFilter() {
	    if (directNoFilter==null)
	    {
			directNoFilter = new ArrayList<XFTFieldWrapper>();
			Iterator iter = wrapped.getSortedFields().iterator();
			while (iter.hasNext()) {
				XFTField xf = (XFTField) iter.next();
				directNoFilter.add(getFactory().wrapField(xf));
			}
			directNoFilter.trimToSize();
	    }
	    return directNoFilter;
	}
	
	public GenericWrapperField getExtensionField() throws ElementNotFoundException,FieldNotFoundException,XFTInitException
	{
		GenericWrapperField f = getDirectField(this.getExtensionFieldName());
		return f;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.schema.design.XFTElementWrapper#getFactory()
	 */
	public XFTFactoryI getFactory() {
		return GenericWrapperFactory.GetInstance();
	}

	/**
	 * Get GenericWrapperField (of any fields inluding AddIns) where the name 
	 * matches the xmlName, sqlName, xmlType local type, or xmlType full foreign type.
	 * @param name (XML name)
	 * @return GenericWrapperField
	 */
	public GenericWrapperField getField(String name)
		throws ElementNotFoundException, XFTInitException {
		Iterator iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.getName().equalsIgnoreCase(name)
				|| field.getXMLType().getLocalType().equalsIgnoreCase(name)
				|| field.getSQLName().equalsIgnoreCase(name)
				|| field.getXMLType().getFullForeignType().equalsIgnoreCase(name)
				|| name.equalsIgnoreCase(field.getName() + "_" + field.getSQLName())
				|| name.equalsIgnoreCase(field.getId())) {
				return field;
			}
		}
		return null;
	}
    
    public boolean instanceOf(String xsiType){
        if(this.getPrimaryElements().indexOf(xsiType)>-1){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * Get GenericWrapperField (of any fields inluding AddIns) where the name 
     * matches the xmlName, sqlName, xmlType local type, or xmlType full foreign type.
     * @param name (XML name)
     * @return GenericWrapperField
     */
    public GenericWrapperField getNonMultipleDataField(String name)
        throws ElementNotFoundException, XFTInitException {
    
        //CHECK NON REFERENCE FIELDS
        Iterator iter = this.getAllFieldsWAddIns().iterator();
        while (iter.hasNext()) {
            GenericWrapperField field = (GenericWrapperField) iter.next();
            if (!field.isReference())
            { 
                if (field.getName().equalsIgnoreCase(name)
                    || field.getXMLType().getLocalType().equalsIgnoreCase(name)
                    || field.getSQLName().equalsIgnoreCase(name)
                    || field.getXMLType().getFullForeignType().equalsIgnoreCase(name)
                    || name.equalsIgnoreCase(field.getName() + "_" + field.getSQLName())) {
                    return field;
                }
            }
        }
        
        //CHECK REFERENCE FIELDS WITH INSERTED SQL NAME
        iter = this.getAllFieldsWAddIns().iterator();
        while (iter.hasNext()) {
            GenericWrapperField field = (GenericWrapperField) iter.next();
            if (! field.isMultiple() || (field.isMultiple() && field.getRelationType().equalsIgnoreCase("single")))
            { 
                if (field.isReference())
                {
                    XFTReferenceI ref = field.getXFTReference();
                    if (!ref.isManyToMany()) {
                        XFTSuperiorReference sub = (XFTSuperiorReference) ref;
                        Iterator keys = sub.getKeyRelations().iterator();
                        while (keys.hasNext()) {
                            XFTRelationSpecification spec =
                                (XFTRelationSpecification) keys.next();
                            if (spec.getLocalCol().equalsIgnoreCase(name)) {
                                return field;
                            }
                        }
                    }
                }
            }
        }
        
        return null;
    }
	
	/**
	 * Get GenericWrapperField (of any fields inluding AddIns) where the name 
	 * matches the xmlName, sqlName, xmlType local type, or xmlType full foreign type.
	 * @param name (XML name)
	 * @return GenericWrapperField
	 */
	public GenericWrapperField getNonMultipleField(String name)
		throws ElementNotFoundException, XFTInitException {
	
		//CHECK NON REFERENCE FIELDS
		Iterator iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
            if (! field.isMultiple())
    			if (field.getName().equalsIgnoreCase(name)
    					|| field.getXMLType().getLocalType().equalsIgnoreCase(name)
    					|| field.getSQLName().equalsIgnoreCase(name)
    					|| field.getXMLType().getFullForeignType().equalsIgnoreCase(name)
    					|| name.equalsIgnoreCase(field.getName() + "_" + field.getSQLName())) {
    					return field;
    			}
		}
		
		//CHECK REFERENCE FIELDS WITH INSERTED SQL NAME
		iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (! field.isMultiple() || (field.isMultiple() && field.getRelationType().equalsIgnoreCase("single")))
			{ 
                if (field.getName().equalsIgnoreCase(name)
                        || field.getXMLType().getLocalType().equalsIgnoreCase(name)
                        || field.getSQLName().equalsIgnoreCase(name)
                        || field.getXMLType().getFullForeignType().equalsIgnoreCase(name)
                        || name.equalsIgnoreCase(field.getName() + "_" + field.getSQLName())) {
                        return field;
                }
				if (field.isReference())
				{
                    
					XFTReferenceI ref = field.getXFTReference();
					if (!ref.isManyToMany()) {
						XFTSuperiorReference sub = (XFTSuperiorReference) ref;
						Iterator keys = sub.getKeyRelations().iterator();
						while (keys.hasNext()) {
							XFTRelationSpecification spec =
								(XFTRelationSpecification) keys.next();
							if (spec.getLocalCol().equalsIgnoreCase(name)) {
								return field;
							}
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * Returns GenericWrapperField where the Field's sql name matches the submited string.
	* @param name (XML name)
	* @return GenericWrapperField
	*/
	public GenericWrapperField getFieldBySQLName(String name)
		throws ElementNotFoundException, XFTInitException, FieldNotFoundException {
		Iterator iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.getSQLName().equalsIgnoreCase(name)) {
				return field;
			}
		}
		
		iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference()) {
				XFTReferenceI ref = field.getXFTReference();
				if (!ref.isManyToMany()) {
					XFTSuperiorReference sub = (XFTSuperiorReference) ref;
					Iterator keys = sub.getKeyRelations().iterator();
					while (keys.hasNext()) {
						XFTRelationSpecification spec =
							(XFTRelationSpecification) keys.next();
						if (spec.getLocalCol().equalsIgnoreCase(name)) {
							return field;
						}
					}
				}
			}
		}
		throw new FieldNotFoundException(name);
	}
	
	public boolean validateID(String name) throws ElementNotFoundException, XFTInitException {
		Iterator iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.getId().equalsIgnoreCase(name)) {
				return true;
			}
		}
		
		iter = this.getAllFieldsWAddIns().iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference()) {
				XFTReferenceI ref = field.getXFTReference();
				if (!ref.isManyToMany()) {
					XFTSuperiorReference sub = (XFTSuperiorReference) ref;
					Iterator keys = sub.getKeyRelations().iterator();
					while (keys.hasNext()) {
						XFTRelationSpecification spec =
							(XFTRelationSpecification) keys.next();
						if (spec.getLocalCol().equalsIgnoreCase(name)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get XFTSuperiorReference for references that are not defined by Reference Fields in
	 * this element.
	 * @return ArrayList of XFTSuperiorReference
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public ArrayList getHiddenSuperiorReferences()
		throws ElementNotFoundException, XFTInitException {
		ArrayList hidden = new ArrayList();

		ArrayList temp = getSuperiorReferences();
		if (temp != null) {
			Iterator tempIter = temp.iterator();
			while (tempIter.hasNext()) {
				XFTSuperiorReference ref =(XFTSuperiorReference) tempIter.next();
				if (ref.getSubordinateField() == null) {
					hidden.add(ref);
				}
			}
		}

		hidden.trimToSize();
		return hidden;
	}
	
	/**
	 * Returns a collection of Object arrays representing other data types which can be parents of this type. ArrayList of Object[3] [0:GenericWrapperElement,1:xmlPath (String),2:GenericWrapperField]
	 * @return ArrayList of Object[3] [0:GenericWrapperElement,1:xmlPath (String),2:GenericWrapperField]
	 */
	public synchronized ArrayList<Object[]> getPossibleParents(boolean withExtensions)
	{
	    if (possibleParents==null)
	    {
	        possibleParents = new ArrayList<Object[]>();
	        
	        try {
                Iterator iter = XFTManager.GetInstance().getAllElements().iterator();
                while (iter.hasNext())
                {
                	GenericWrapperElement gwe = (GenericWrapperElement)iter.next();
                	if ((! gwe.getLocalXMLName().equalsIgnoreCase(this.getLocalXMLName())) && (gwe.getAddin().equals("")))
                	{
                		Iterator fields = gwe.getReferenceFields(true).iterator();
                		while (fields.hasNext())
                		{
                			GenericWrapperField gwf = (GenericWrapperField)fields.next();
                			if (gwf.isReference() && !gwe.getExtensionFieldName().equals(gwf.getName()))
                			{
                				if (gwf.getXMLType().getFullForeignType().equalsIgnoreCase(this.getType().getFullForeignType()))
                				{
                				    Object[] pp = new Object[3];
    								pp[0] = gwe;
    								pp[1] = gwf.getXMLPathString(gwe.getFullXMLName());
    								pp[2] = gwf;
    								possibleParents.add(pp);
                				}
                			}
                		}
                	}
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            }
	    }
        
        if (withExtensions)
        {
            if (this.isExtension())
            {
                try {
                    ArrayList<Object[]> temp = new ArrayList<Object[]>();
                    temp.addAll(possibleParents);
                    temp.addAll(this.getExtensionField().getReferenceElement().getGenericXFTElement().getPossibleParents(true));
                    return temp;
                } catch (Exception e1) {
                    logger.error("",e1);
                }
            }
        }
	    
	    return possibleParents;
	}
	
	/**
	 * ArrayList of GenericWrapperFields (DOES NOT CALL XFTReferenceManager)
	 * @return Returns an ArrayList of hidden superior elements
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public synchronized ArrayList getHiddenSuperiorElements()	throws ElementNotFoundException, XFTInitException,Exception {
		if (HIDDEN_SUPERIOR_FIELDS.get(this.getFullXMLName()) == null)
		{

			ArrayList hidden = new ArrayList();
		
			Iterator iter = XFTManager.GetInstance().getAllElements().iterator();
			while (iter.hasNext())
			{
				GenericWrapperElement gwe = (GenericWrapperElement)iter.next();
				if (! gwe.getLocalXMLName().equalsIgnoreCase(this.getLocalXMLName()))
				{
					Iterator fields = gwe.getReferenceFields(true).iterator();
					while (fields.hasNext())
					{
						GenericWrapperField gwf = (GenericWrapperField)fields.next();
						if (gwf.isReference() && gwf.isMultiple())
						{
							if (gwf.getXMLType().getFullForeignType().equalsIgnoreCase(this.getType().getFullForeignType()))
							{
								Iterator localRefs = this.getAllFields(false, true).iterator();
								boolean foundLocal = false;
								while (localRefs.hasNext())
								{
									GenericWrapperField localRef = (GenericWrapperField)localRefs.next();
									if (localRef.isReference())
									{
										if (localRef.getXMLType().getLocalType().equalsIgnoreCase(gwe.getLocalXMLName()))
										{
											foundLocal = true;
											break;
										}
									}else{
									    if (localRef.getBaseElement().equalsIgnoreCase(gwe.getFullXMLName()))
									    {
											foundLocal = true;
											break;
									    }
									}
								}
								if (! foundLocal)
								{
									hidden.add(gwf);
								}
							}
						}
					}
				}
			}
		
			hidden.trimToSize();
			HIDDEN_SUPERIOR_FIELDS.put(this.getFullXMLName(),hidden);
		}
		
		return (ArrayList)HIDDEN_SUPERIOR_FIELDS.get(this.getFullXMLName());
	}

	public String getIdMethod() {
		return this.getTorqueElement().getIdMethod();
	}
	
	public boolean isAutoIncrement() throws XFTInitException
	{
		Iterator keys = this.getAllPrimaryKeys().iterator();
		while (keys.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)keys.next();
			if (f.getAutoIncrement().equalsIgnoreCase("true"))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * if the element had a defined javaName in its WebAppElement, then that is returned.
	 * Otherwise, the localXMLName is returned (formated to remove '_' characters).
	 * @return Returns the element's javaName in its WebAppElement, or the localXMLName if no javaName is defined
	 */
	public String getJAVAName() {
		if (this.getXMLJavaNameValue() != "") {
			return getXMLJavaNameValue();
		}

		if (getLocalXMLName().indexOf("_") != -1) {
			return XftStringUtils.FormatStringToMethodName("", getLocalXMLName());
		} else {
			return getLocalXMLName();
		}

	}

	/**
	 * Return all multi-reference fields (foreign-keys)
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getMultiReferenceFields() {
		ArrayList al = new ArrayList();
		Iterator iter = this.getAllFields(false, true).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference() && field.isMultiple()) {
				al.add(field);
			}
		}

		al.trimToSize();
		return al;
	}

	/**
	 * Returns all non-multi-reference fields
	* @return ArrayList of GenericWrapperFields 
	*/
	public ArrayList getNonMultipleFields()
		throws ElementNotFoundException, XFTInitException {
		ArrayList al = new ArrayList();
		Iterator fields = this.getAllFieldsWAddIns(false, false).iterator();
		while (fields.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) fields.next();
			if (field.isReference()) {
				if (!field.isMultiple()) {
					al.add(field);
				}
			} else {
				al.add(field);
			}
		}
		al.trimToSize();
		return al;
	}
	
	/**
	 * returns true if this element directly references the foreign element.
	 * @param foreign
	 * @return Returns whether this element directly references the foreign element
	 */
	public boolean hasReferenceTo(GenericWrapperElement foreign)
	{
	    Iterator rFields= this.getReferenceFields(true).iterator();
	    while (rFields.hasNext())
	    {
	        GenericWrapperField f = (GenericWrapperField)rFields.next();
	        if (f.getReferenceElementName().getFullForeignType().equals(foreign.getFullXMLName()))
	        {
	            return true;
	        }
	    }
	    
	    return false;
	}

	/**
	 * Return all reference fields (foreign-keys) with or without multi-references
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList<GenericWrapperField> getReferenceFields(boolean allowMultiples) {
		ArrayList<GenericWrapperField> al = new ArrayList<GenericWrapperField>();
		Iterator iter = this.getAllFields(false, allowMultiples).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference()) {
				al.add(field);
			}
		}

		al.trimToSize();
		return al;
	}

	/**
	 * Return all reference fields (foreign-keys) including addIns without xmlOnly fields 
	 * or multi-reference fields.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getReferenceFieldsWAddIns()
		throws XFTInitException, ElementNotFoundException {
		ArrayList al = new ArrayList();
		ArrayList all = this.getAllFields(false, true);
		Iterator iter = all.iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference()) {
				if ((field.isMultiple() && field.getRelationType().equalsIgnoreCase("single") && field.getXMLType().getFullForeignType().equalsIgnoreCase(this.getFullXMLName())) || (!field.isMultiple()))
				{
					al.add(field);
				}
			}
		}

		al.addAll(getUndefinedReferences());

		al.trimToSize();
		return al;
	}

	/**
	 * This method is used to remove ReferenceFields which are not shown unless it is at the root level.
	 * Return all reference fields (foreign-keys) without xmlOnly fields where xmlDisplay='always'
	 * unless isRootElement=true.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getReferenceFieldsWXMLDisplay(
		boolean allowMultiples,
		boolean isRootElement) {
		ArrayList al = new ArrayList();
		Iterator iter = this.getAllFields(false, allowMultiples).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (isRootElement || field.getXMLDisplay().equalsIgnoreCase("always")) {
				if (field.isReference()) {
					al.add(field);
				}
			}
		}

		al.trimToSize();
		return al;
	}

	/**
	 * Gets fields which have XFTRelations but are not references.
	 * @return ArrayList of GenericWrapperFields
	 */
	public ArrayList getRelationFields() {
		ArrayList al = new ArrayList();

		Iterator iter = this.getAllFields(false, false).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.hasRelation()) {
				if (!field.isReference()) {
					al.add(field);
				}
			}
		}

		al.trimToSize();
		return al;
	}

	/**
	 * Gets validation rules for each field.
	 * @return ArrayList of ObjectArray[GenericWrapperField,ArrayList of String[]]
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public ArrayList getRules()
		throws ElementNotFoundException, XFTInitException {
		ArrayList rules = new ArrayList();
		Iterator leafs = this.getAllFieldsWAddIns(true, true).iterator();
		while (leafs.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) leafs.next();
			ArrayList inner = field.getRules();
			if (inner.size() > 0) {
				Object[] rule = { field, inner };
				rules.add(rule);
			}
		}

		return rules;
	}

	/**
	 * parent schema
	 * @return Returns schema
	 */
	public XFTSchema getSchema() {
		return this.wrapped.getSchema();
	}

	/**
	 * @return Returns the schema's target namespace prefix
	 */
	public String getSchemaTargetNamespacePrefix() {
		return this.wrapped.getSchema().getTargetNamespacePrefix();
	}

	/**
	 * @return Returns the schema's target namespace URI
	 */
	public String getSchemaTargetNamespaceURI() {
		return this.wrapped.getSchema().getTargetNamespaceURI();
	}
//
//	/**
//	 * Get SQL Select statement including select-grand fields and select-grand joins
//	 * @param allowMultiples
//	 * @return
//	 * @throws ElementNotFoundException
//	 * @throws XFTInitException
//	 */
//	public String getSelectFrom(boolean allowMultiples)
//		throws ElementNotFoundException, XFTInitException {
//		return "SELECT "
//			+ getSelectGrandFields("",getSQLName(),allowMultiples,new ArrayList(),true)
//			+ " FROM "
//			+ getSelectGrandFrom(allowMultiples);
//	}

	/**
	 * Get SQL Select statement as SELECT * FROM view_name.  allowMultiples 
	 * determines if the view contains all multi-reference field joins.
	 * @param allowMultiples
	 * @return Returns an SQL select statement to get the contents of the view
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public String getSelectFromView(boolean allowMultiples,boolean loadHistory)
		throws ElementNotFoundException, XFTInitException {
		if (loadHistory)
			return "SELECT * FROM " + ViewManager.GetViewName(this,ViewManager.ALL,allowMultiples,true);
		else
			return "SELECT * FROM " + ViewManager.GetViewName(this,ViewManager.QUARANTINE,allowMultiples,true);
	}
//	
	public String getMultiViewName()
	{
		return ViewManager.GetViewName(this,ViewManager.QUARANTINE,true,true);
	}
	
	public String getSingleViewName()
	{
		return ViewManager.GetViewName(this,ViewManager.QUARANTINE,false,true);
	}

	/**
	 * valid sql name for this element.
	 * @return Returns the element's SQL name
	 */
	public synchronized String getSQLName() {
	    if (_finalSqlName==null)
	    {
			String temp = "";
			if (this.getXMLSqlNameValue() != "") {
				if (getSchemaTargetNamespacePrefix().equalsIgnoreCase(""))
					temp =getXMLSqlNameValue();
				else
					temp = getSchemaTargetNamespacePrefix() + "_" + getXMLSqlNameValue();
			}
			
			if (temp.equalsIgnoreCase(""))
			{
				if (this.wrapped.hasParent()) {
					if (this.getXMLJavaNameValue() != "") {
						if (getSchemaTargetNamespacePrefix().equalsIgnoreCase(""))
							temp= getXMLJavaNameValue();
						else
							temp= getSchemaTargetNamespacePrefix() + "_" + getXMLJavaNameValue();
					}
				}
			}
			
			if (temp.equalsIgnoreCase(""))
			{
				if (getSchemaTargetNamespacePrefix().equalsIgnoreCase(""))
					temp= getXMLName();
				else
					temp= getSchemaTargetNamespacePrefix() + "_" + getXMLName();
			}

			_finalSqlName= XftStringUtils.CleanForSQLTableName(temp);
	    }
	    
	    return _finalSqlName;
	}

	/**
	 * valid sql name for this element.
	 * @return Returns the formatted SQL name for this element
	 */
	public synchronized String getFormattedName() {
	    if (_finalFormattedName==null)
	    {
			String temp = "";
			
			if (this.wrapped.hasParent()) {
				if (this.getXMLJavaNameValue() != "") {
					if (getSchemaTargetNamespacePrefix().equalsIgnoreCase(""))
						temp= getXMLJavaNameValue();
					else
						temp= getSchemaTargetNamespacePrefix() + "_" + getXMLJavaNameValue();
				}
			}
			
			if (temp.equalsIgnoreCase(""))
			{
				if (getSchemaTargetNamespacePrefix().equalsIgnoreCase(""))
					temp= getXMLName();
				else
					temp= getSchemaTargetNamespacePrefix() + "_" + getXMLName();
			}

			_finalFormattedName= XftStringUtils.CleanForSQLTableName(temp);
	    }
	    
	    return _finalFormattedName;
	}

	//	/**
	//	 * @return
	//	 */
	//	public ArrayList getImpliedParents() {
	//		
	//		return wrapped.getImpliedParents();
	//	}

	/**
	 * Gets stated Primary Key fields (Does not insert a default key if none are found)
	 * @return ArrayList of SQLFields
	 */
	public ArrayList getStatedPrimaryKeyFields()
		throws org.nrg.xft.exception.XFTInitException {
		ArrayList al = new ArrayList();

		Iterator iter = this.getAllFields().iterator();
		while (iter.hasNext()) {
			GenericWrapperField gwf = (GenericWrapperField) iter.next();
			if (gwf.isPrimaryKey()) {
				if (gwf.isReference()) {
					try {

						al.addAll(gwf.getReferenceKeys(this.wrapped));
					} catch (Exception e) {
						logger.error("ELEMENT:'" + this.getFullXMLName() + "'",e);
					}
				} else {
					al.add(gwf);
				}
			}
		}

		al.trimToSize();
		return al;
	}

	public ArrayList getUniqueIdentifierPrimaryKeyFields()
	throws org.nrg.xft.exception.XFTInitException {
		ArrayList al = new ArrayList();
	
		Iterator iter = this.getAllFields().iterator();
		while (iter.hasNext()) {
			GenericWrapperField gwf = (GenericWrapperField) iter.next();
			if (gwf.isPrimaryKey()) {
				if (gwf.isReference()) {
					try {
					    GenericWrapperElement foreign = (GenericWrapperElement)gwf.getReferenceElement();
					    if (foreign.hasUniqueIdentifiers())
					    {
							al.addAll(gwf.getReferenceKeys(this.wrapped));
					    }
					} catch (Exception e) {
						logger.error("ELEMENT:'" + this.getFullXMLName() + "'",e);
					}
				} else {
					al.add(gwf);
				}
			}
		}
	
		al.trimToSize();
		return al;
	}
	/**
	 * Finds XFTSuperiorReferences where this element is superior.
	 * @return ArrayList of XFTSuperiorReference
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public ArrayList getSuperiorReferences()
		throws ElementNotFoundException, XFTInitException {
		if (superiorReferences == null) {
			superiorReferences = XFTReferenceManager.FindSuperiorsFor(this);
		}
		return superiorReferences;
	}

	/**
	 * Finds XFTSuperiorReferences where this element is subordinate.
	 * @return ArrayList of XFTSuperiorReference
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public ArrayList getSubordinateReferences()
		throws ElementNotFoundException, XFTInitException {
		if (superiorReferences == null) {
			superiorReferences = XFTReferenceManager.FindSubordinatesFor(this);
		}
		return superiorReferences;
	}
	
	/**
	 * @return ArrayList of XFTManyToManyReference
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public ArrayList getManyToManyReferences() throws ElementNotFoundException, XFTInitException
	{
		return XFTReferenceManager.FindManyToManysFor(this);
	}

	/**
	 * Gets the SQL select-grand name for the referenced field as a sub item of 
	 * this element.
	 * @param subElementName
	 * @param sqlName
	 * @return Returns the SQL select-grand name for the referenced field as a sub item of this element
	 * @throws FieldNotFoundException
	 * @throws ElementNotFoundException
	 * @throws XFTInitException
	 */
	public String getTableAndFieldGrandSQLForExtendedFieldSQLName(
		String subElementName,
		String sqlName)
		throws FieldNotFoundException, ElementNotFoundException, XFTInitException {
		String s = getExtensionXMLPathName(subElementName,sqlName);
		
		s = ViewManager.GetViewColumnName(this,s);
		
		if (s==null)
		{
			throw new FieldNotFoundException(subElementName + XFT.PATH_SEPARATOR + sqlName);
		}
		
		return s;
	}
		
	public String getExtensionXMLPathName(String subElementName,String fieldName) throws FieldNotFoundException, ElementNotFoundException, XFTInitException
	{
		String xmlPath = this.getFullXMLName();
		if (subElementName.equalsIgnoreCase(this.getFullXMLName())) {
			GenericWrapperField f=this.getNonMultipleField(fieldName);
			if (f!=null)
			{
				if (f.isReference())
				{
					XFTReferenceI ref = f.getXFTReference();
					if (ref.isManyToMany())
					{
						
					}else{
						XFTSuperiorReference sup = (XFTSuperiorReference)ref;
						XFTRelationSpecification spec = (XFTRelationSpecification)sup.getKeyRelations().get(0);
						return xmlPath + XFT.PATH_SEPARATOR + spec.getLocalCol();
					}
				}
				return f.getXMLPathString(xmlPath);
			}
		}
		if (isExtension()) {
			XMLType extended = getExtensionType();
			GenericWrapperElement childElement =GenericWrapperElement.GetElement(extended);
			return childElement.getExtensionXMLPathName(subElementName,fieldName, xmlPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
		}
		throw new FieldNotFoundException(subElementName + XFT.PATH_SEPARATOR + fieldName);
	}
	
	public String getExtensionXMLPathName(String subElementName,String fieldName,String xmlPath) throws FieldNotFoundException, ElementNotFoundException, XFTInitException
	{
		if (subElementName.equalsIgnoreCase(this.getFullXMLName())) {
			GenericWrapperField f=this.getNonMultipleField(fieldName);
			if (f!=null)
			{
				if (f.isReference())
				{
					XFTReferenceI ref = f.getXFTReference();
					if (ref.isManyToMany())
					{
						
					}else{
						XFTSuperiorReference sup = (XFTSuperiorReference)ref;
						XFTRelationSpecification spec = (XFTRelationSpecification)sup.getKeyRelations().get(0);
						return xmlPath + XFT.PATH_SEPARATOR + spec.getLocalCol();
					}
				}
				return f.getXMLPathString(xmlPath);
			}
		}
		if (isExtension()) {
			XMLType extended = getExtensionType();
			GenericWrapperElement childElement =GenericWrapperElement.GetElement(extended);
			return childElement.getExtensionXMLPathName(subElementName,fieldName, xmlPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
		}
		throw new FieldNotFoundException(subElementName + XFT.PATH_SEPARATOR + fieldName);
	}
	
	/**
	 * Adding caching here because of frequent access. 11/10/09 TO
	 */
	HashMap<String, Object[]> cachedPathfields=new HashMap<String, Object[]>();
	
	/**
	 * Parses XML Dot Syntax to find field's select-grand name.
     * @param fieldXMLPath    The XML path to translate.
     * @return The translated XML path values.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 */
	public Object[] getTableAndFieldGrandSQLForXMLPath(String fieldXMLPath)throws ElementNotFoundException,XFTInitException,FieldNotFoundException 
	{
		Object[] match=this.cachedPathfields.get(fieldXMLPath);
		if(match==null){
			match=getTableAndFieldGrandSQLForXMLPath(fieldXMLPath,"","");
			this.cachedPathfields.put(fieldXMLPath, match);
		}
		return match;
	}

	/**
	 * Parses XML Dot Syntax to find field's select-grand name.
	 * @param path                The XML path to translate.
	 * @param header    	      The header.
	 * @param correctedXMLPath    The corrected XML path.
	 * @return The translated XML path values.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws ElementNotFoundException When a specified element isn't found on the object.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 */
	public Object[] getTableAndFieldGrandSQLForXMLPath(String path, String header, String correctedXMLPath) throws ElementNotFoundException,XFTInitException,FieldNotFoundException
	{
//		if (tableName == null || tableName.equalsIgnoreCase("")) {
//			tableName = getSQLName();
//		}
	    path = XftStringUtils.StandardizeXMLPath(path);
	    if (correctedXMLPath==null || correctedXMLPath.equalsIgnoreCase(""))
	    {
	        correctedXMLPath = this.getFullXMLName();
	    }
		GenericWrapperField lastField = null;
		while (path.indexOf(XFT.PATH_SEPARATOR) != -1) {
			String last = path;
			String current = path.substring(0, path.indexOf(XFT.PATH_SEPARATOR));
			path = path.substring(path.indexOf(XFT.PATH_SEPARATOR) + 1);
 
            String expectedXSIType = null;
                if (XFTItem.EndsWithFilter(current))
                {
                    Map map = XFTItem.GetFilterOptions(current);
                    if (map.get("@xsi:type")!=null){
                        expectedXSIType = (String)map.get("@xsi:type");
                    }
                    current = XFTItem.CleanFilter(current);
                }
			//last was a field
			if (lastField == null) {
				try {
					lastField = getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
                        if (expectedXSIType!=null){
                            child = GenericWrapperElement.GetElement(expectedXSIType);
                        }
						return child.getTableAndFieldGrandSQLForXMLPath(
								path,
								header + XftStringUtils.MinCharsAbbr(lastField.getSQLName()) + "_",
								correctedXMLPath + XFT.PATH_SEPARATOR + current);
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						return extendedE
								.getTableAndFieldGrandSQLForXMLPath(
								last,
								header + XftStringUtils.MinCharsAbbr(getExtensionFieldName()) + "_",
								correctedXMLPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
					}
					throw new FieldNotFoundException(current);
				}

			} else {
				try {
					lastField = lastField.getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						return child.getTableAndFieldGrandSQLForXMLPath(
									path,
									header + XftStringUtils.MinCharsAbbr(lastField.getSQLName()) + "_",
									correctedXMLPath + XFT.PATH_SEPARATOR + current);
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						return extendedE
							.getTableAndFieldGrandSQLForXMLPath(
							last,
							header + XftStringUtils.MinCharsAbbr(getExtensionFieldName()) + "_",
							correctedXMLPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
						
					}
					throw new FieldNotFoundException(current);
				}
			}
			correctedXMLPath += XFT.PATH_SEPARATOR + lastField.getXMLName();
		}

		if (lastField == null) {
			try {
				lastField = getDirectField(path);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					return extendedE.getTableAndFieldGrandSQLForXMLPath(
							path,
							header + XftStringUtils.MinCharsAbbr(getExtensionFieldName()) + "_",
							correctedXMLPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
					
				}
				throw new FieldNotFoundException(path + ":" + correctedXMLPath + ":" + this.getFullXMLName());
			}
			if (lastField == null) {
				throw new FieldNotFoundException(path);
			}
		}else{
			try {
				lastField = lastField.getDirectField(path);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					return extendedE.getTableAndFieldGrandSQLForXMLPath(
								path,
								header + XftStringUtils.MinCharsAbbr(getExtensionFieldName()) + "_",
								correctedXMLPath + XFT.PATH_SEPARATOR + getExtensionFieldName());
				}
				throw new FieldNotFoundException(path);
			}
			if (lastField == null) {
				throw new FieldNotFoundException(path);
			}
		}

		Object[] _return = new Object[3];
		if (header.equalsIgnoreCase(""))
			_return[0] =
					XftStringUtils.RegCharsAbbr(this.getSQLName())
					+ "_"
					+ lastField.getSQLName();
		else
			_return[0] =
					header
					+ XftStringUtils.RegCharsAbbr(this.getSQLName())
					+ "_"
					+ lastField.getSQLName();
		_return[2]= correctedXMLPath + XFT.PATH_SEPARATOR + path;
		_return[1] = lastField;
		return _return;
	}

	/**
	 * @deprecated
	 * @return Returns torque element
	 */
	private TorqueElement getTorqueElement() {
		if (te == null) {
			if (wrapped.getWebAppElement() != null) {
				te = (TorqueElement) wrapped.getWebAppElement();
			} else {
				te = new TorqueElement();
			}
		}
		return te;
	}
	/**
	 * Gets the value of the wrapped element's code.
	 * @return Returns the value of the wrapped element's code
	 */
	public String getTypeCode() {
		return wrapped.getCode();
	}
	
	public boolean isHiddenFK(String name)
	{
	    boolean isHidden = false;
	    try {
            Iterator iter = this.getUndefinedReferences().iterator();
            while (iter.hasNext())
            {
                GenericWrapperField f = (GenericWrapperField)iter.next();
                try {
                    XFTReferenceI ref= f.getXFTReference();
                    if (!ref.isManyToMany())
                    {
                        XFTSuperiorReference sup =  (XFTSuperiorReference)ref;
                        Iterator specs = sup.getKeyRelations().iterator();
                        while (specs.hasNext())
                        {
                            XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
                            if (spec.getLocalCol().equalsIgnoreCase(name))
                            {
                                return true;
                            }
                        }
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
	    
	    return isHidden;
	}

	/**
	 * Gets referenced items which are not specifically defined in the schema.
	 * @return ArrayList of GenericWrapperFields
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public synchronized ArrayList<GenericWrapperField> getUndefinedReferences()	throws XFTInitException, ElementNotFoundException 
	{
		if (undefinedReferences == null) {
			undefinedReferences = new ArrayList<GenericWrapperField>();

			try {
				ArrayList hiddenSuperiorElements =
					this.getHiddenSuperiorElements();

				if (this.wrapped.hasParent()) {
					//CREATE REF TO PARENT
					XFTReferenceField field = XFTReferenceField.GetEmptyRef();
					field.setName(wrapped.getParentElement().getName());
					field.setXMLType(new XMLType(this.wrapped.getSchema().getTargetNamespacePrefix()
								+ ":"
								+ wrapped.getParentElement().getName(),
							wrapped.getSchema()));
					field.setFullName(wrapped.getParentElement().getName());
					field.setParent(wrapped);
					GenericWrapperField gwf =((GenericWrapperField) getFactory().wrapField(field));
					undefinedReferences.add(gwf);
				} else if (hiddenSuperiorElements.size() > 0) {
					Iterator hiddenSuperiors = hiddenSuperiorElements.iterator();
					while (hiddenSuperiors.hasNext()) {
						GenericWrapperField foreignRef = (GenericWrapperField)hiddenSuperiors.next();
						XFTReferenceI refI = foreignRef.getXFTReference();
						if (! refI.isManyToMany())
						{
							XFTSuperiorReference xftRef = (XFTSuperiorReference)foreignRef.getXFTReference();

							XFTReferenceField ref = XFTReferenceField.GetEmptyRef();
							ref.setName(foreignRef.getWrapped().getFullName());
							ref.setXMLType(
								new XMLType(xftRef.getSuperiorElementName(),
									wrapped.getSchema()));
							ref.setFullName(foreignRef.getWrapped().getFullName());
							ref.setParent(this.wrapped);
							GenericWrapperField gwf =
								((GenericWrapperField) getFactory().wrapField(ref));
							try {
								GenericWrapperField foreignCol =
									xftRef.getSuperiorField();
								ref.setRequired("true");
								if (!foreignCol
									.getXMLSqlNameValue()
									.equalsIgnoreCase("")) {
									XFTSqlField xsf = new XFTSqlField();
									xsf.setSqlName(foreignCol.getXMLSqlNameValue());
									ref.setSqlField(xsf);
								}
							} catch (Exception e) {
								logger.error(
									"ELEMENT:'" + this.getFullXMLName() + "'",
									e);
							}

							undefinedReferences.add(gwf);
						}
						
					}
				}
			} catch (Exception e) {
			    logger.error("",e);
			}

			undefinedReferences.trimToSize();
		}
		return undefinedReferences;
	}

	/**
	 * Return Unique Composite fields.
	 * @return ArrayList of GenericWrapperFields
	 */
	public Hashtable getUniqueCompositeFields() {
	    Hashtable hash = new Hashtable();
		
	    if (!this.getAddin().equalsIgnoreCase("history"))
	    {
			Iterator iter = this.getAllFields(false, true).iterator();
			while (iter.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) iter.next();
				if (! field.isMultiple())
				{
					if (field.getUniqueComposite() != "" || field.getUniqueComposite() != null)
					{
					    Iterator ids = XftStringUtils.CommaDelimitedStringToArrayList(field.getUniqueComposite()).iterator();
					    while(ids.hasNext())
					    {
					        String s = (String)ids.next();
					        ArrayList al = null;
					        if (hash.get(s.toLowerCase())==null)
					        {
					            al = new ArrayList();
					        }else{
					            al = (ArrayList)hash.get(s.toLowerCase());
					        }
					        
					        al.add(field);
					        hash.put(s.toLowerCase(),al);
					    }
					}
				}
			}
			
			try {
				iter = this.getHiddenSuperiorElements().iterator();
				while (iter.hasNext())
				{
					GenericWrapperField f = (GenericWrapperField)iter.next();
					if (f.getRelationUniqueComposite()!=null || f.getRelationUniqueComposite()!="")
					{
						XFTReferenceI ref = f.getXFTReference();
						if (ref.isManyToMany())
						{

						}else{
							XFTSuperiorReference sup = (XFTSuperiorReference)f.getXFTReference();
							Iterator specs = sup.getKeyRelations().iterator();
							while (specs.hasNext())
							{
								XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
								XFTDataField temp = XFTDataField.GetEmptyField();
								temp.setXMLType(spec.getSchemaType());
								temp.setName(spec.getLocalCol());
								temp.setFullName(temp.getName());
								temp.setParent(this.wrapped);
								GenericWrapperField gwf = (GenericWrapperField)GenericWrapperFactory.GetInstance().wrapField(temp);
								
								Iterator ids = XftStringUtils.CommaDelimitedStringToArrayList(f.getRelationUniqueComposite()).iterator();
							    while(ids.hasNext())
							    {
							        String s = (String)ids.next();
							        ArrayList al = null;
							        if (hash.get(s.toLowerCase())==null)
							        {
							            al = new ArrayList();
							        }else{
							            al = (ArrayList)hash.get(s.toLowerCase());
							        }
							        
							        al.add(gwf);
							        hash.put(s.toLowerCase(),al);
							    }
							}
						}
						
					}
				}
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			} catch (XFTInitException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
		
		return hash;
	}

	/**
	 * Returns all unique fields
	 * @return ArrayList of SQLFields
	 */
	public ArrayList getUniqueFields() {
		ArrayList al = new ArrayList();

		Iterator iter = this.getAllFields(false, false).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isUnique())
				al.add(field);
		}
		
		try {
			iter = this.getHiddenSuperiorElements().iterator();
			while (iter.hasNext())
			{
				GenericWrapperField f = (GenericWrapperField)iter.next();
				if (!f.getXFTReference().isManyToMany())
				{
					if (f.getRelationUnique())
					{
						XFTSuperiorReference sup = (XFTSuperiorReference)f.getXFTReference();
						Iterator specs = sup.getKeyRelations().iterator();
						while (specs.hasNext())
						{
							XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
							XFTDataField temp = XFTDataField.GetEmptyField();
							temp.setXMLType(spec.getSchemaType());
							temp.setName(spec.getLocalCol());
							temp.setFullName(temp.getName());
							al.add(GenericWrapperFactory.GetInstance().wrapField(temp));
						}
					}
				}
			}
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
		} catch (XFTInitException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		al.trimToSize();
		return al;
	}

	public boolean hasUniques()
	{
		if (getUniqueFields().size() > 0)
		{
			return true;
		}
		if (getUniqueCompositeFields().size() > 0)
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns name and level of all xml data fields.
	 * @return ArrayList of XMLFieldData
	 */
	public ArrayList getXMLFields(Hashtable uriToPrefixMapping) {
		ArrayList al = new ArrayList();

		Iterator fields = this.getDirectFieldsNoFilter().iterator();
		while (fields.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) fields.next();
			ArrayList layers = new ArrayList();
			
			if (uriToPrefixMapping.get(getSchemaTargetNamespaceURI())==null)
			    layers.add(getType().getLocalType());
			else
			{
			    String prefix = (String)uriToPrefixMapping.get(getSchemaTargetNamespaceURI());
			    layers.add(prefix + ":" + getType().getLocalType());
			}
			
			layers.trimToSize();
			al.addAll(field.getXMLFields(1, layers,uriToPrefixMapping));
		}
		al.trimToSize();
		return al;
	}

	/**
	 * Gets defined javaName or an empty string
	 * @return Returns wrapped element's defined javaName
	 */
	public String getXMLJavaNameValue() {
		String _return = "";
		if (wrapped.getWebAppElement() != null) {
			_return = wrapped.getWebAppElement().getJavaName();
		}
		return _return;
	}

	/**
	 * Gets name property of the wrapped element.
	 * @return Returns wrapped element's name property
	 */
	public String getXMLName() {
		return wrapped.getName();
	}

	/**
	 * Gets the defined sqlName or an empty string.
	 * @return Returns wrapped element's name property
	 */
	public String getXMLSqlNameValue() {
		String _return = "";
		if (wrapped.getSqlElement() != null) {
			_return = wrapped.getSqlElement().getName();
		}
		return _return;
	}

	/**
	 * If this element does not correspond to a unique XML node then its isANoChildElement will be true.
	 * @return Returns whether the wrapped element is a child element
	 */
	public boolean isANoChildElement() {
		return this.wrapped.isANoChildElement();
	}

	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns whether the wrapped element is an extension of another element
	 */
	public boolean isExtension() {
		return wrapped.isExtension();
	}
	
	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns whether the wrapped element is extended
	 */
	public boolean isExtended()
	{
		return wrapped.isExtended();
	}
	
	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * @return Returns whether the wrapped element has an extension element
	 */
	public boolean hasExtendedField()
	{
		return wrapped.hasExtensionElement();
	}
	
	/**
	 * If the element is an extension of another element, then its extension is true and the
	 * extended elements XMLType is stored in the exstensionType element.  If this element is extended
	 * by another element then its isExtended field is true.  If it is extended and is not an extension
	 * itself, then it will contain an additional field (reference to XFT_Element) and its hasExtensionElement
	 * will be true.
	 * 
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public void initializeExtendedField()throws XFTInitException, ElementNotFoundException
	{
		if (! isExtended())
		{
			if (isExtension())
			{
				GenericWrapperElement ex = GenericWrapperElement.GetElement(getExtensionType());
				ex.initializeExtendedField();
				getWrapped().setExtended(true);
				GenericWrapperElement.ClearElementCache();
			}else{
				getWrapped().initializeExtensionField();		
				logger.debug("FOUND EXTENDED ELEMENT: '" + getName() + "'");	
				
				//EXTEND HISTORY
				try {
					GenericWrapperElement e = GenericWrapperElement.GetElement(getFullXMLName() + "_history");
					e.getWrapped().initializeExtensionField();
				} catch (ElementNotFoundException e) {
					logger.error("", e);
				}

				GenericWrapperElement.ClearElementCache();
			}	
		}
	}
	
	/**
	 * The name of the element, and its schema's target namespace prefix are used to generate a XMLType which
	 * is used to uniquely identify an element.
	 * @return Returns whether the wrapped element has storeHistory set to true
	 */
	public boolean storeHistory()
	{
		return wrapped.storeHistory();
	}
	
	/**
	 * The name of the element, and its schema's target namespace prefix are used to generate a XMLType which
	 * is used to uniquely identify an element.
	 * @return Returns the XMLType used to uniquely identify the element
	 */
	public XMLType getType()
	{
		return wrapped.getType();
	}
	
	/**
	 * localType of the XMLType
	 * @return Returns this element's local XML name
	 */
	public String getName()
	{
		return this.getLocalXMLName();
	}
	
	
	/**
	 * @param header
	 * @return ArrayList of Object[xmlPathName,GenericWrapperField]
	 */
	public ArrayList getAllFieldsWXMLPath(String header) throws XFTInitException,ElementNotFoundException
	{
		if (header == null || header.equalsIgnoreCase(""))
			header = this.getXMLName();
		ArrayList al = new ArrayList();
		Iterator dF = this.getDirectFieldsNoFilter().iterator();
		while (dF.hasNext())
		{
			GenericWrapperField f = (GenericWrapperField)dF.next();
			if (f.getExpose())
			{
				if (f.isReference())
				{
					if (! f.isMultiple())
					{
						GenericWrapperElement ref = (GenericWrapperElement)f.getReferenceElement();
						if (isExtension() && this.getExtensionType().getLocalType().equalsIgnoreCase(ref.getType().getLocalType()))
						{
							al.addAll(ref.getAllFieldsWXMLPath(header));
						}else
						{
							al.addAll(ref.getAllFieldsWXMLPath(header + XFT.PATH_SEPARATOR + f.getName()));
						}
					}
				}else{
					al.addAll(f.getAllFieldsWXMLPath(header + XFT.PATH_SEPARATOR + f.getName()));
				}
			}
		}
		al.trimToSize();
		return al;
	}
	
	/**
	 * Parses XML Dot Syntax to find field's select-grand name. Object[2] 0:grand sql name 1:GenericWrapperField
	 * @param s
	 * @return Returns the field's select-grand name
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws Exception
	 */
	public static Object[] GetViewBasedGrandColumnNameForXMLPath(String s) throws XFTInitException,ElementNotFoundException,Exception
	{
	    s = XftStringUtils.StandardizeXMLPath(s);
		String rootElement = XftStringUtils.GetRootElementName(s);
		GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
		try {

		String fieldXMLPath = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
		
			Object [] fieldInfo = root.getTableAndFieldGrandSQLForXMLPath(fieldXMLPath);
			
			String t = ViewManager.GetViewColumnName(root,s);
			
			fieldInfo[0] = t;
			return fieldInfo;
		} catch (FieldNotFoundException e) {
			e.FIELD=s;
			throw e;
		}
	}
	
	/**
	 * Parses XML Dot Syntax to find field's select-grand name. Object[2] 0:grand sql name 1:GenericWrapperField
	 * @param s
	 * @return Returns the field's select-grand name
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws Exception
	 */
	public static Object[] GetGrandColumnNameForXMLPath(String s) throws XFTInitException,ElementNotFoundException,Exception
	{
	    s = XftStringUtils.StandardizeXMLPath(s);
		String rootElement = XftStringUtils.GetRootElementName(s);
		GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
		try {
		    String fieldXMLPath = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
		
			Object [] fieldInfo = root.getTableAndFieldGrandSQLForXMLPath(fieldXMLPath);
			return fieldInfo;
		} catch (FieldNotFoundException e) {
			e.FIELD=s;
			throw e;
		}
	}
	
	/**
	 * Returns the validated dot-syntax reference.  (Inserts extension elements)
	 * @param s
	 * @return Returns the validated dot-syntax reference
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws Exception
	 */
	public static String GetVerifiedXMLPath(String s) throws XFTInitException,ElementNotFoundException,Exception
	{
	    s = XftStringUtils.StandardizeXMLPath(s);
		String rootElement = XftStringUtils.GetRootElementName(s);
		GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
		String fieldXMLPath = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
		Object [] fieldInfo = root.getTableAndFieldGrandSQLForXMLPath(fieldXMLPath);
		return (String)fieldInfo[2];
	}
	
    
	/**
	 * Parses XML Dot Syntax to find field
	 * @param path    The XML path to translate.
	 * @return The translated XML path values.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws ElementNotFoundException When a specified element isn't found on the object.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 */
	public static GenericWrapperField GetFieldForXMLPath(String path) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    path= XftStringUtils.StandardizeXMLPath(path);
            String rootElement = path.substring(0,path.indexOf(XFT.PATH_SEPARATOR));
            path = path.substring(path.indexOf(XFT.PATH_SEPARATOR) + 1);
            GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
            try {
                Object [] fieldInfo = root.getTableAndFieldGrandSQLForXMLPath(path);
                if (fieldInfo != null)
                {
				return (GenericWrapperField) fieldInfo[1];
                }else{
                    return null;
                }
            } catch (FieldNotFoundException e) {
                e.FIELD=path;
                throw e;
            }
	}
	
//	public String getGrandColumnNameForField(GenericWrapperField f) throws XFTInitException,ElementNotFoundException,Exception
//	{
//		return XftStringUtils.MaxCharsAbbr(this.getSQLName()) + "_" + f.getSQLName();
//	}
	
	/**
	 * Finds the type of relationship between this element and the foreign element
	 * @param foreign XFTReferenceI
	 * @return Returns the type of relationship between this element and the foreign element
	 */
	public XFTReferenceI getRelationType(GenericWrapperElement foreign) throws XFTInitException,ElementNotFoundException
	{
		Iterator superiors = XFTReferenceManager.FindSuperiorsFor(this).iterator();
		while (superiors.hasNext())
		{
			XFTSuperiorReference sup = (XFTSuperiorReference)superiors.next();
			if (sup.getSuperiorElement().getType().getFullLocalType().equalsIgnoreCase(foreign.getType().getFullLocalType()))
			{
				return sup;
			}
		}
		
		superiors = XFTReferenceManager.FindHiddenSuperiorsFor(this).iterator();
		while (superiors.hasNext())
		{
			XFTSuperiorReference sup = (XFTSuperiorReference)superiors.next();
			if (sup.getSuperiorElement().getType().getFullLocalType().equalsIgnoreCase(foreign.getType().getFullLocalType()))
			{
				return sup;
			}
		}
		
		Iterator subordinates = XFTReferenceManager.FindSubordinatesFor(this).iterator();
		while (subordinates.hasNext())
		{
			XFTSuperiorReference sup = (XFTSuperiorReference)subordinates.next();
			if (sup.getSubordinateElement().getType().getFullLocalType().equalsIgnoreCase(foreign.getType().getFullLocalType()))
			{
				return sup;
			}
		}
		
		Iterator manyToManys = XFTReferenceManager.FindManyToManysFor(this).iterator();
		while(manyToManys.hasNext())
		{
			XFTManyToManyReference many = (XFTManyToManyReference)manyToManys.next();
			if (many.getElement1().getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName())||
			many.getElement2().getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName()))
			{
				return many;
			}
		}
		
		return null;
	}
	
	public String toString()
	{
		return this.getFullXMLName();
	}
	
	
	public String getAddin()
	{
		return this.wrapped.getAddin();
	}
	
	/**
	 * ArrayList of sql-names
	 * @return Returns an ArrayList of the dependency names
	 */
	public ArrayList getDependencies() throws XFTInitException, ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		Iterator refs = getReferenceFieldsWXMLDisplay(true, false).iterator();
		while (refs.hasNext())
		{
			GenericWrapperField field = (GenericWrapperField)refs.next();
			if (field.isReference())
			{
				GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
				if (this.getAddin().equalsIgnoreCase("") || (!foreign.getAddin().equalsIgnoreCase("")))
				{
					al.add(foreign.getFormattedName());
				}
			}
		}
		
		al.trimToSize();
		return al;
	}
	
//	public ArrayList getAllPossibleChildRefs()
//	{
//		return getAllPossibleChildRefs(new ArrayList());
//	}
//	
//	private ArrayList getAllPossibleChildRefs(ArrayList history)
//	{
//		Iterator iter = getReferenceFieldsWXMLDisplay(true,true).iterator();
//		while (iter.hasNext())
//		{
//			
//		}
//	}
	
	public String getSequenceName() throws XFTInitException,ElementNotFoundException
	{
		return DBAction.getSequenceName(this);
	}
	
	/**
	 * @return Returns the metaFields.
	 */
	public synchronized MetaFieldCollection getMetaFields() {
		if (metaFields == null)
		{
			metaFields = new MetaFieldCollection();
			try {
				Iterator iter = ViewManager.GetFieldNames(this,ViewManager.QUARANTINE,false,true).iterator();
				while (iter.hasNext())
				{
					
					String xmlPath = (String)iter.next();
					MetaField mf = new MetaField();
					mf.setXmlPathName(xmlPath);
					
					try {
						GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
						if (f!= null)
						{
							mf.setLocalType(f.getXMLType().getLocalType());
							mf.setSqlName(f.getSQLName());
							mf.setField(f);
						}
					} catch (Exception e) {
						mf.setLocalType("string");
						mf.setReferenceIdField(true);
					}
					
					metaFields.addField(mf);
				}
			} catch (XFTInitException e) {
				logger.error("",e);
			} catch (ElementNotFoundException e) {
				logger.error("",e);
			}
			
		}
		
		return metaFields;
	}
	
	public static ArrayList GetUniqueValuesForField(String xmlPath) throws Exception
	{
	    return TableSearch.GetUniqueValuesForField(xmlPath,null);
	}
	
	public static ArrayList GetPossibleValues(String xmlPath) throws Exception
	{
	    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
	    return f.getPossibleValues();
	}
	
	private static boolean ENABLE_XPATH_TABLE_CACHING=true;
	
	/**
	 * Translates a XML Dot Syntax name into the matching table element names (separated by periods).
	 * String[]{
	 * 	0:string of element names, separated by .
	 * 	1:string of field/element_names for SQL alias
	 *  2:field's SQL name
	 *  3:string of field names.
	 * }
	 * 
	 * It's hard to understate the importance of this ugly mess of code.  Its at the heart of XFT's search engine, which is at the heart of XFT.
	 * The functionality really isn't that complicated, but the frankenstein mess of modifications over the years has turned it into a rabbit hole of complicated code.  Enter at your own risk, and leave a trail of breadcrumbs to get back out.
	 * This would be significantly refactored in the next round of search engine improvements, if it ends up happening.
	 * 
	 * This is used to see how elements are connected to eachother and build a connection strategy between database tables.  
	 * 
	 * @param path    The XML path to translate.
	 * @return The translated XML path values.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 */
	public static String[] TranslateXMLPathToTables(String path) throws FieldNotFoundException
	{
	    try {
	        path = XftStringUtils.StandardizeXMLPath(path);
	        if (XMLPATH_TABLES_CACHE.get(path)==null || (!ENABLE_XPATH_TABLE_CACHING))
	        {
	            String rootElement = XftStringUtils.GetRootElementName(path);
	            GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
	            String fieldXMLPath = path.substring(path.indexOf(XFT.PATH_SEPARATOR) + 1);
	            String[] tables = root.translateXMLPathToTables(fieldXMLPath);
	            if (tables != null)
	            {
	                XMLPATH_TABLES_CACHE.put(path,tables);
	            }
	        }
        } catch (XFTInitException e) {
            logger.error("",e);
            return null;            
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return null;            
        } catch (FieldNotFoundException e) {
            throw new FieldNotFoundException(path);
        }
        return (String[])XMLPATH_TABLES_CACHE.get(path);
	}

	public static String GetCompactXMLPath(String s)
	{
	    try {
	        s = XftStringUtils.StandardizeXMLPath(s);
            String rootElement = XftStringUtils.GetRootElementName(s);
            GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
            String fieldXMLPath = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
            return root.getCompactXMLPath(fieldXMLPath);
        } catch (XFTInitException e) {
            return null;            
        } catch (ElementNotFoundException e) {
            return null;            
        } catch (Exception e) {
            return null;            
        }
	}
	
	public String getCompactXMLPath(String s) throws Exception
	{
		GenericWrapperField lastField = null;
		while (s.indexOf(XFT.PATH_SEPARATOR) != -1) {
			String current = s.substring(0, s.indexOf(XFT.PATH_SEPARATOR));
			s = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);

			//last was a field
			if (lastField == null) {
				try {
					lastField = getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						String layer = child.getCompactXMLPath(s);
						if (lastField.getName().equalsIgnoreCase(this.getExtensionFieldName()))
						{
						    return layer;
						}else{
						    return lastField.getXMLPathString() + XFT.PATH_SEPARATOR + layer;
						}
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						String layer = extendedE.getCompactXMLPath(s);
						return layer;
					}
					throw new FieldNotFoundException(current);
				}

			} else {
				try {
					lastField = lastField.getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						String layer = child.getCompactXMLPath(s);
						if (lastField.getName().equalsIgnoreCase(this.getExtensionFieldName()))
						{
						    return layer;
						}else{
						    return lastField.getXMLPathString() + XFT.PATH_SEPARATOR + layer;
						}
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						String layer = extendedE.getCompactXMLPath(s);
						return layer;
						
					}
					throw new FieldNotFoundException(current);
				}
			}
		}

		if (lastField == null) {
			try {
				lastField = getDirectField(s);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					String layer = extendedE.getCompactXMLPath(s);
					return layer;
				}
				throw new FieldNotFoundException(s + ":" + this.getFullXMLName());
			}
			if (lastField == null) {
				throw new FieldNotFoundException(s);
			}
		}else{
			try {
				lastField = lastField.getDirectField(s);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					String layer = extendedE.getCompactXMLPath(s);
					return layer;
				}
				throw new FieldNotFoundException(s);
			}
			if (lastField == null) {
				throw new FieldNotFoundException(s);
			}
		}

		return lastField.getXMLPathString();
	}

    /**
     * Get the XMLType of the Element which this one extends.
     * @return Returns the XMLType of the Element which this one extends
     */
    public XMLType getExtensionType() {
        return wrapped.getExtensionType();
    }
	
	public String getExtensionFieldName()
	{
	    if (this.getExtensionType() !=null)
	        return this.getExtensionType().getLocalType();
	    else
	        return "";
	}

    /**
     * Get the XMLType of the Element which this one extends.
     * @return Returns the meta-data field
     */
    public GenericWrapperField getMetaDataField() {
    	GenericWrapperField meta = null;
    	try {
			for(GenericWrapperField temp : this.getDirectFields()){
				if(temp.isReference()){
					GenericWrapperElement gwe= (GenericWrapperElement) temp.getReferenceElement();
					if (gwe.getAddin()!=null && gwe.getLocalXMLName().endsWith("_meta_data")){
						meta= temp;
						break;
					}
				}
			}
		} catch (XFTInitException e) {
            logger.error("",e);
		} catch (ElementNotFoundException e) {
            logger.error("",e);
		}
        return meta;
    }
	
	public String getMetaDataFieldName()
	{
	    if (this.getMetaDataField() !=null){
	        try {
				XFTSuperiorReference sup = (XFTSuperiorReference)this.getMetaDataField().getXFTReference();
				XFTRelationSpecification spec =sup.getKeyRelations().get(0);
				return spec.getLocalCol();
			} catch (XFTInitException e) {
                logger.error("",e);
			} catch (ElementNotFoundException e) {
                logger.error("",e);
			}	        
	    }
    	return this.getLocalXMLName() + "_info";
	}
	
	public boolean matchByValues()
	{
	    if (this.hasUniqueIdentifiers())
	    {
	        return false;
	    }else{
	        boolean b = getWrapped().isMatchByValues();
	        if (b)
	        {
	            return true;
	        }else
	        {
	            try {
                    if (this.isExtension())
                    {
                        return ((GenericWrapperElement)this.getExtensionField().getReferenceElement()).matchByValues();
                    }else{
                        return false;
                    }
                } catch (Exception e) {
                    logger.error("",e);
                    return false;
                }
	        }
	    }
	}
	
	/**
	 * Translates a XML Dot Syntax name into the matching table element names (separated by periods).
	 * String[]{
	 * 	0:string of element names, separated by .
	 * 	1:string of field/element_names for SQL alias
	 *  2:field's SQL name
	 *  3:string of field names.
	 * }
	 * @param path    The XML path to translate.
	 * @return The translated XML path values.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws ElementNotFoundException When a specified element isn't found on the object.
	 * @throws FieldNotFoundException When one of the requested fields can't be found in the data object.
	 */
	public String[] translateXMLPathToTables(String path) throws XFTInitException, ElementNotFoundException,FieldNotFoundException
    {
		GenericWrapperField lastField = null;
		while (path.indexOf(XFT.PATH_SEPARATOR) != -1) {
			String last = path;
			String current = path.substring(0, path.indexOf(XFT.PATH_SEPARATOR));
			path = path.substring(path.indexOf(XFT.PATH_SEPARATOR) + 1);

			//last was a field
			if (lastField == null) {
				String expectedXSIType = null;
                if (XFTItem.EndsWithFilter(current))
                {
                    Map map = XFTItem.GetFilterOptions(current);
                    if (map.get("@xsi:type")!=null){
                        expectedXSIType = (String)map.get("@xsi:type");
                    }
                    current = XFTItem.CleanFilter(current);
                }
				try {
					lastField = getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						String[] layer = child.translateXMLPathToTables(path);
						layer[0] = this.getFullXMLName() + "." + "[" + lastField.getWrapped().getFullName() +"]" + layer[0];
						if (lastField.getName().equalsIgnoreCase(this.getExtensionFieldName()))
						{
							layer[1] = this.getSQLName() + "." + child.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], child.getName().toLowerCase() + "_EXT_");
						}else{
							layer[1] = this.getSQLName() + "." + lastField.getSQLName() + "_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], lastField.getSQLName() + "_" + getSQLName() + "_" + lastField.getXMLType().getLocalType() + "_");
						}
						if (layer[3]==null)
						{
							layer[3] = lastField.getXMLType().getLocalType();
						}else{
							layer[3] = lastField.getXMLType().getLocalType() + "." + layer[3];
						}
						return layer;
					}else{
					    
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						String[] layer = extendedE.translateXMLPathToTables(last);
						layer[0] = this.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT_");
						if (layer[3]==null)
						{
							layer[3] = getExtensionFieldName();
						}else{
							layer[3] = getExtensionFieldName() + "." + layer[3];
						}
						return layer;
					}
					if(expectedXSIType!=null){
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(expectedXSIType);
						String[] layer = extendedE.translateXMLPathToTables(path);
						//xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						layer[0] = this.getFullXMLName() + "." + "[" + lastField.getWrapped().getFullName() +"]" +child.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + lastField.getSQLName() + "_" + getSQLName() + "_" + child.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT_" + "." + child.getName().toLowerCase() + "_EXT_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], child.getName().toLowerCase() + "_EXT_"));
						if (layer[3]==null)
						{
							layer[3] = lastField.getXMLType().getLocalType() + "." + extendedE.getType().getLocalType();
						}else{
							layer[3] = lastField.getXMLType().getLocalType() + "." + extendedE.getType().getLocalType() + "." + layer[3];
						}
						return layer;
					}
					throw new FieldNotFoundException(current);
				}

			} else {
				String expectedXSIType = null;
                if (XFTItem.EndsWithFilter(current))
                {
                    Map map = XFTItem.GetFilterOptions(current);
                    if (map.get("@xsi:type")!=null){
                        expectedXSIType = (String)map.get("@xsi:type");
                    }
                    current = XFTItem.CleanFilter(current);
                }
				try {
					lastField = lastField.getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						//xnat:subjectData/experiments/experiment[@xsi:type=xnat:ctSessionData]/date
						String[] layer = child.translateXMLPathToTables(path);
						layer[0] = this.getFullXMLName() + "." + "[" + lastField.getWrapped().getFullName() +"]" + layer[0];
						if (lastField.getName().equalsIgnoreCase(this.getExtensionFieldName()))
						{
							layer[1] = this.getSQLName() + "." + lastField.getSQLName() + "_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], lastField.getSQLName() + "_" + getSQLName() + "_EXT" + "_");
						}else{
							layer[1] = this.getSQLName() + "." + lastField.getSQLName() + "_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], lastField.getSQLName() + "_" + getSQLName() + "_" + lastField.getXMLType().getLocalType() + "_");
						}
						if (layer[3]==null)
						{
							layer[3] = lastField.getXMLType().getLocalType();
						}else{
							layer[3] = lastField.getXMLType().getLocalType() + "." + layer[3];
						}
						return layer;
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						String[] layer = extendedE.translateXMLPathToTables(last);
						layer[0] = this.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_");
						if (layer[3]==null)
						{
							layer[3] = getExtensionFieldName();
						}else{
							layer[3] = getExtensionFieldName() + "." + layer[3];
						}
						return layer;

					}
					if(expectedXSIType!=null){
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(expectedXSIType);
						String[] layer = extendedE.translateXMLPathToTables(path);
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						layer[0] = this.getFullXMLName() + "." + "[" + lastField.getWrapped().getFullName() +"]" +child.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_" + "." + child.getName().toLowerCase() + "_EXT_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], child.getName().toLowerCase() + "_EXT_"));
						if (layer[3]==null)
						{
							layer[3] = getExtensionFieldName();
						}else{
							layer[3] = getExtensionFieldName() + "." + layer[3];
						}
						return layer;

					}
					throw new FieldNotFoundException(current);
				}
			}
		}

		if (lastField == null) {
			String expectedXSIType = null;
            if (XFTItem.EndsWithFilter(path))
            {
                Map map = XFTItem.GetFilterOptions(path);
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                path = XFTItem.CleanFilter(path);
            }
			try {
				lastField = getDirectField(path);
			} catch (FieldNotFoundException e) {
			    
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					try {
                        String[] layer = extendedE.translateXMLPathToTables(path);
                        layer[0] = this.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_");
                        if (layer[3]==null)
                        {
                        	layer[3] = getExtensionFieldName();
                        }else{
                        	layer[3] = getExtensionFieldName() + "." + layer[3];
                        }
                        return layer;
                    } catch (Exception e1) {
//                        e1.printStackTrace();
                    }
				}
				if(expectedXSIType!=null){
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(expectedXSIType);
					try {
                        String[] layer = extendedE.translateXMLPathToTables(path);
						GenericWrapperElement child =(GenericWrapperElement) lastField.getReferenceElement();
                        layer[0] = this.getFullXMLName() + "." + "[" + lastField.getWrapped().getFullName() +"]" +child.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
						layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_" + "." + child.getName().toLowerCase() + "_EXT_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], child.getName().toLowerCase() + "_EXT_"));
                        if (layer[3]==null)
                        {
                        	layer[3] = getExtensionFieldName();
                        }else{
                        	layer[3] = getExtensionFieldName() + "." + layer[3];
                        }
                        return layer;
                    } catch (Exception e1) {
//                        e1.printStackTrace();
                    }
				}
				
				Iterator iter = getAllFieldNames().iterator();
				while(iter.hasNext())
				{
				    Object[] al = (Object[])iter.next();
				    String tempName = (String) al[0];
				    if (tempName.equalsIgnoreCase(path))
				    {
				        XFTDataField data =
							XFTDataField.GetEmptyKey(
								this.wrapped.getSchemaPrefix(),
								this.wrapped.getAbsoluteParent().getSchema());
						data.setName(tempName);
						data.setFullName(tempName);
						data.setParent(this.wrapped);
						data.setMinOccurs("0");
				        lastField = (GenericWrapperField)GenericWrapperFactory.GetInstance().wrapField(data);
				        break;
				    }
				}
				
				if (lastField == null) {
					throw new FieldNotFoundException(path);
				}
			}
			if (lastField == null) {
				throw new FieldNotFoundException(path);
			}
		}else{
			String expectedXSIType = null;
            if (XFTItem.EndsWithFilter(path))
            {
                Map map = XFTItem.GetFilterOptions(path);
                if (map.get("@xsi:type")!=null){
                    expectedXSIType = (String)map.get("@xsi:type");
                }
                path = XFTItem.CleanFilter(path);
            }
			try {
				lastField = lastField.getDirectField(path);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					String[] layer = extendedE.translateXMLPathToTables(path);
					layer[0] = this.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
					layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_");
					if (layer[3]==null)
					{
						layer[3] = getExtensionFieldName();
					}else{
						layer[3] = getExtensionFieldName() + "." + layer[3];
					}
					return layer;
				}
				if (expectedXSIType!=null) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					String[] layer = extendedE.translateXMLPathToTables(path);
					layer[0] = this.getFullXMLName() + "." + "[" + extendedE.getName() +"]" + layer[0];
					layer[1] = this.getSQLName() + "." + extendedE.getName().toLowerCase() + "_EXT_" + getSQLName() + "_" + XftStringUtils.InsertCharsIntoDelimitedString(layer[1], extendedE.getName().toLowerCase() + "_EXT" + "_");
					if (layer[3]==null)
					{
						layer[3] = getExtensionFieldName();
					}else{
						layer[3] = getExtensionFieldName() + "." + layer[3];
					}
					return layer;
				}
				throw new FieldNotFoundException(path);
			}
			if (lastField == null) {
				throw new FieldNotFoundException(path);
			}
		}

		String[] layer = new String[4];
		layer[0]=this.getFullXMLName();
		layer[1]=this.getSQLName();
		layer[2]= lastField.getSQLName();
		return layer;
    }
	
	/**
	 * Returns the dot-syntax for the first reference field which references 
	 * the element.
	 * @param e
	 * @return Returns the dot-syntax for the first reference field which references the element
	 */
	public String findReferencedField(GenericWrapperElement e, ArrayList searched, boolean allowExtension, boolean isRoot)
	{
	    ArrayList localSearched = new ArrayList();
	    localSearched.addAll(searched);
        String s = null;
	    try {
	        
	        Iterator rels = this.getAllFields().iterator();
            while (rels.hasNext())
            {
                GenericWrapperField gwf = (GenericWrapperField)rels.next();
                if (gwf.isReference())
                {
                    if (allowExtension || !this.getExtensionFieldName().equals(gwf.getName()))
                    {
	                    GenericWrapperElement foreign = (GenericWrapperElement)gwf.getReferenceElement();
	                    if (foreign.hasUniqueIdentifiers())
	                    {
	                        if (foreign.getFullXMLName().equalsIgnoreCase(e.getFullXMLName()))
	                        {
	                           s= gwf.getXMLPathString();
	                           break;
	                        } 
	                    }
                    }
                }
            }
            
            int joinCount = 100;
            if (s ==null)
            {
                rels = this.getAllFields().iterator();
                while (rels.hasNext())
                {
                    GenericWrapperField gwf = (GenericWrapperField)rels.next();
					if (gwf.isReference())
					{
					    if (isRoot || !gwf.getXMLDisplay().equalsIgnoreCase("root"))
					    {
		                    GenericWrapperElement foreign = (GenericWrapperElement)gwf.getReferenceElement();
		                    if (foreign.getAddin().equalsIgnoreCase(""))
		                    {
		                        if (! localSearched.contains(foreign.getFullXMLName()))
		                        {
		                            localSearched.add(foreign.getFullXMLName());
		                            if (foreign.hasUniqueIdentifiers())
		                            {
				                        String temp = foreign.findReferencedField(e,localSearched,allowExtension,false);
				                        if (temp!=null)
				                        {
				                            temp = gwf.getXMLPathString() + XFT.PATH_SEPARATOR + temp.substring(temp.indexOf(XFT.PATH_SEPARATOR) + 1);
				                            try {
                                                String[] layers = GenericWrapperElement.TranslateXMLPathToTables(this.getFullXMLName() + XFT.PATH_SEPARATOR + temp);
                                                int tempJoinCount = XftStringUtils.DelimitedStringToArrayList(layers[1], ".").size();
                                                if (tempJoinCount<joinCount)
                                                {
                                                    s = temp;
                                                    joinCount = tempJoinCount;
                                                }
                                            } catch (FieldNotFoundException e2) {
                                                logger.error("",e2);
                                            }
				                        }
		                            }
		                        }
		                    }
					    }
					}
                }
            }
            
            if (s != null)
            {
                s= this.getFullXMLName() + XFT.PATH_SEPARATOR + s;
            }
        } catch (XFTInitException e1) {
            logger.error("",e1);
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }
        return s;
	}
	

	
	/**
	 * Returns the dot-syntax for the first reference field which references 
	 * 0: foreign dot-syntax
	 * 1: local dot-syntax
	 * the element.
	 * @param e
	 * @return Returns the dot-syntax for the first reference field which references
	 */
	public String[] findExtensionReferencedField(GenericWrapperElement e, ArrayList searched, boolean isRoot)
	{
        String[] s = null;
	    try {
	        String connection = e.findReferencedField(this,new ArrayList(),true,isRoot);
	        if (connection == null)
	        {
		        if (isExtension())
		        {
			        GenericWrapperElement extension = GenericWrapperElement.GetElement(getExtensionType());
		            connection = e.findReferencedField(extension,new ArrayList(),false,isRoot);
		            while (connection == null && extension.isExtension())
		            {
		                extension = GenericWrapperElement.GetElement(extension.getExtensionType());
		                connection = e.findReferencedField(extension,new ArrayList(),false,isRoot); 
		            }
		            
		            if (connection!=null)
		            {
		                String local = findReferencedField(extension,new ArrayList(),true,false);
		                s = new String[2];
		                s[0] = connection;
		                s[1] = local; 
		            }
		        }
	        }else{
	            String local = this.getFullXMLName();
                s = new String[2];
                s[0] = connection;
                s[1] = local; 
	        }
        } catch (XFTInitException e1) {
            logger.error("",e1);
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }
        return s;
	}
	
	/**
	 * Returns the dot syntax reference to a field which is defined as a filter field (as defined using an XDAT tag in the XML Schema).
	 * @return Returns the dot syntax reference to a field which is defined as a filter field
	 */
	public String getFilter()
	{
	    String s = null;
	    try {
            Iterator rels = this.getAllFieldsWAddIns().iterator();
            //CHECK LOCAL FIELDS FIRST
            while (rels.hasNext())
            {
                GenericWrapperField gwf = (GenericWrapperField)rels.next();
            	if ( !gwf.isReference())
            	{
                    if (gwf.isFilter())
                    {
                        s =  gwf.getXMLPathString(getFullXMLName());
                        break;
                    }
            	}
            }
            
            if (s != null)
            {
                return s;
            }
            
            if (this.isExtension())
            {
                GenericWrapperElement ext = GenericWrapperElement.GetElement(this.getExtensionType());
                s = ext.getFilter();
                if (s != null)
                {
                    s = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
                    s = this.getFullXMLName() + XFT.PATH_SEPARATOR + getExtensionFieldName() + XFT.PATH_SEPARATOR + s;
                }
            }
            
            if (s != null)
            {
                return s;
            }
            
            //CHECK SINGLE-REFERENCES
            while (rels.hasNext())
            {
                GenericWrapperField gwf = (GenericWrapperField)rels.next();
            	if ( gwf.isReference())
            	{
                    try {
                        GenericWrapperElement foreign = (GenericWrapperElement)gwf.getReferenceElement();
                        if (foreign.getAddin().equalsIgnoreCase(""))
                        {
                            s = foreign.getFilter();
                            if (s != null)
                            {
                                s = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
                                s = gwf.getXMLPathString(getFullXMLName()) + XFT.PATH_SEPARATOR + s;
                                break;
                            }
                        }
                    } catch (XFTInitException e) {
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
            	}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        return s;
	}
	
	public String getFilterField()
	{
	    String s= getFilter();
	    if (s==null&& !this.getName().endsWith("_history"))
	    {
	        s = getFullXMLName() + XFT.PATH_SEPARATOR + "meta" + XFT.PATH_SEPARATOR + "insert_date";
	    }
	    return s;
	}
	
	public static boolean IsMultipleReference(String s)
	{
	    try {
		    s = XftStringUtils.StandardizeXMLPath(s);
	        if (s.indexOf(XFT.PATH_SEPARATOR) == -1)
	        {
	            return false;
	        }
			String rootElement = XftStringUtils.GetRootElementName(s);
            GenericWrapperElement root = GenericWrapperElement.GetElement(rootElement);
            String fieldXMLPath = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);
            return root.isMultipleReference(fieldXMLPath);
        } catch (XFTInitException e) {
            logger.error("",e);
            return false;            
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return false;            
        } catch (Exception e) {
            logger.error("",e);
            return false;            
        }
	}
	
	public boolean isMultipleReference(String s) throws Exception
    {
		GenericWrapperField lastField = null;
		while (s.indexOf(XFT.PATH_SEPARATOR) != -1) {
			String current = s.substring(0, s.indexOf(XFT.PATH_SEPARATOR));
			s = s.substring(s.indexOf(XFT.PATH_SEPARATOR) + 1);

			//last was a field
			if (lastField == null) {
				try {
					lastField = getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						if (lastField.isMultiple())
						{
						    return true;
						}else{
						    return child.isMultipleReference(s);
						}
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						return extendedE.isMultipleReference(s);
					}
					throw new FieldNotFoundException(current);
				}

			} else {
				try {
					lastField = lastField.getDirectField(current);
					if (lastField.isReference()) {
						GenericWrapperElement child =
							(GenericWrapperElement) lastField
								.getReferenceElement();
						if (lastField.isMultiple())
						{
						    return true;
						}else{
						    return child.isMultipleReference(s);
						}
					}
				} catch (FieldNotFoundException e) {
					if (isExtension()) {
						GenericWrapperElement extendedE =
							GenericWrapperElement.GetElement(getExtensionType());
						return extendedE.isMultipleReference(s);
						
					}
					throw new FieldNotFoundException(current);
				}
			}
		}

		if (lastField == null) {
			try {
				lastField = getDirectField(s);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					return extendedE.isMultipleReference(s);
				}
				throw new FieldNotFoundException(s + ":" + this.getFullXMLName());
			}
			if (lastField == null) {
				throw new FieldNotFoundException(s);
			}else{
			    return lastField.isMultiple();
			}
		}else{
			try {
				lastField = lastField.getDirectField(s);
			} catch (FieldNotFoundException e) {
				if (isExtension()) {
					GenericWrapperElement extendedE =
						GenericWrapperElement.GetElement(getExtensionType());
					return extendedE.isMultipleReference(s);
				}
				throw new FieldNotFoundException(s);
			}
			if (lastField == null) {
				throw new FieldNotFoundException(s);
			}else{
			    return lastField.isMultiple();
			}
		}
    }
		
	public static String[] FindExtensionReferenceField(GenericWrapperElement rootElement, GenericWrapperElement foreign)
	{
	    String[] connection = new String[2];
	    
	    String[] temp = foreign.findExtensionReferencedField(rootElement,new ArrayList(),true);
        if (temp!=null)
        {
            //ROOT IS A CHILD OF THE FOREIGN
            return temp;
        }else{
            String xmlPath = rootElement.findReferencedField(foreign,new ArrayList(),true,true);
            if (xmlPath!=null)
            {
                //FOREIGN IS A CHILD OF ROOT
                connection[1]=foreign.getFullXMLName();
                connection[2]=xmlPath;
            }else
            {
                //CHECK TO SEE IF THE FOREIGN ELEMENT RELATES TO THE ROOT's EXTENDED 
                if (rootElement.isExtension())
                {
                    connection = rootElement.findExtensionReferencedField(foreign,new ArrayList(),true);
                    
                    if (connection != null)
                    {
                        //BUILD CONNECTION FROM ROOT TO EXTENSION
                        String localSyntax = connection[1];
                        xmlPath = connection[0];
                        connection[0]= localSyntax;
                        connection[1]=xmlPath;
                    }
                }
            }
            
        }
	      
	    return connection;
	}
	
	public boolean hasUniqueIdentifiers()
	{
	    try {
            if (this.getUniqueIdentifierPrimaryKeyFields().size()> 0)
            {
                return true;
            }else{
                if (this.getUniqueFields().size() > 0)
                {
                    return true;
                }else{
                    if (this.getUniqueCompositeFields().size()>0)
                    {
                        return true;
                    }
                }
            }
            
            if (this.isExtension())
            {
                return ((GenericWrapperElement)this.getExtensionField().getReferenceElement()).hasUniqueIdentifiers();
            }else{
                return false;
            }
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
	
	public boolean ignoreWarnings()
	{
	    return wrapped.isIgnoreWarnings();
	}

    /**
     * @return Returns the quarantine.
     */
    public boolean isQuarantine() {
        return wrapped.isQuarantine();
    }

    /**
     * @return Returns the quarantine.
     */
    public boolean isQuarantine(boolean defaultValue) {
        return wrapped.isQuarantine(defaultValue);
    }

    /**
     * @return Returns the quarantine.
     */
    public boolean hasQuarantineSetting() {
        return wrapped.hasQurantineSetting();
    }

    /**
     * @return Returns the quarantine.
     */
    public boolean isAutoActivate() {
        return wrapped.isAutoActivate();
    }
    /**
     * @param quarantine The quarantine to set.
     */
    public void setQuarantineSetting(boolean quarantine) {
        this.getWrapped().setQuarantineSetting(quarantine);

		GenericWrapperElement.ClearElementCache();
    }

    /**
     * @return Returns the preLoad.
     */
    public boolean isPreLoad() {
        return wrapped.isPreLoad();
    }
    /**
     * @param preLoad The preLoad to set.
     */
    public void setPreLoad(boolean preLoad) {
        getWrapped().setPreLoad(preLoad);

		GenericWrapperElement.ClearElementCache();
    }
    
    public GenericWrapperElement getGenericXFTElement()
    {
        return this;
    }
    
    private static Hashtable<String,Hashtable<String,String[]>> _schemaConnections=new Hashtable<String,Hashtable<String,String[]>>();
    
    private static String[] GetSchemaConnection(GenericWrapperElement local, GenericWrapperElement foreign){
    	Hashtable<String,String[]> cons=_schemaConnections.get(local.getXSIType());
    	if(cons!=null){
    		return cons.get(foreign.getXSIType());
    	}
    	
    	return null;
    }
    
    private synchronized static void SetSchemaConnection(GenericWrapperElement local, GenericWrapperElement foreign,String[] con){
    	Hashtable<String,String[]> cons=_schemaConnections.get(local.getXSIType());
    	if(cons==null){
    		cons = new Hashtable<String,String[]>();
    		_schemaConnections.put(local.getXSIType(), cons);
    	}
    	cons.put(foreign.getXSIType(), con);
    }
    
    public String[] findSchemaConnection(GenericWrapperElement foreign)
    {
        if (GetSchemaConnection(this, foreign)==null)
        {
            String[] s = null;

            int joinCount = 100;
            String localConnection = null;
            String foreignConnection = null;
            
            Iterator extensions = this.getExtendedElements().iterator();
            while (extensions.hasNext())
            {
                ArrayList al = (ArrayList)extensions.next();
                if (((SchemaElementI)al.get(0)).getFullXMLName().equals(foreign.getFullXMLName()))
                {
                    localConnection = (String)al.get(1);
                    foreignConnection = foreign.getXSIType();
                }
            }

            if (localConnection==null)
            {
	            extensions = foreign.getExtendedElements().iterator();
	            while (extensions.hasNext())
	            {
	                ArrayList al = (ArrayList)extensions.next();
	                if (((SchemaElementI)al.get(0)).getFullXMLName().equals(this.getFullXMLName()))
	                {
	                    localConnection = this.getFullXMLName();
	                    foreignConnection = (String)al.get(1);
	                }
	            }
            }
            
            if (localConnection==null)
            {
                Iterator localRefs = getReferencedElements().iterator();
                while(localRefs.hasNext())
                {
                    ArrayList link = (ArrayList)localRefs.next();
                    if (((SchemaElementI)link.get(0)).getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName()))
                    {
                        String temp = (String)link.get(1);
                       
                        int tempJoinCount = XftStringUtils.DelimitedStringToArrayList(temp, String.valueOf(XFT.PATH_SEPARATOR)).size();
                        if (tempJoinCount < joinCount)
                        {
                            joinCount = tempJoinCount;
                            localConnection = temp;
                            foreignConnection = foreign.getFullXMLName();
                        }
                    }
                    
                    Iterator foreignExtensions = foreign.getExtendedElements().iterator();
                    while(foreignExtensions.hasNext())
                    {
                        ArrayList extensionLink = (ArrayList)foreignExtensions.next();
                        SchemaElementI foreignExtension = (SchemaElementI)extensionLink.get(0);
                       
                        if (((SchemaElementI)link.get(0)).getFullXMLName().equalsIgnoreCase(foreignExtension.getFullXMLName()))
                        {
                            String temp = (String)link.get(1);
                            String extensionTemp = (String)extensionLink.get(1);
                           
                            int tempJoinCount = XftStringUtils.DelimitedStringToArrayList(temp, String.valueOf(XFT.PATH_SEPARATOR)).size() + 1;
                            if (tempJoinCount < joinCount)
                            {
                                joinCount = tempJoinCount;
                                localConnection = temp;
                                foreignConnection = extensionTemp;
                            }
                        }
                    }
                }
            }
            
            if (localConnection!=null && foreignConnection !=null)
            {
                s = new String[3];
                s[0]=localConnection;
                s[1]=foreignConnection;
                s[2]="root";
                SetSchemaConnection(this, foreign,s);
            }else{
	            Iterator foreignRefs = foreign.getReferencedElements().iterator();
	            while(foreignRefs.hasNext())
	            {
	                ArrayList link = (ArrayList)foreignRefs.next();
	                if (((SchemaElementI)link.get(0)).getFullXMLName().equalsIgnoreCase(this.getFullXMLName()))
	                {
	                    String temp = (String)link.get(1);
	                   
	                    int tempJoinCount = XftStringUtils.DelimitedStringToArrayList(temp, String.valueOf(XFT.PATH_SEPARATOR)).size();
	                    if (tempJoinCount < joinCount)
	                    {
	                        joinCount = tempJoinCount;
	                        localConnection = this.getFullXMLName();
	                        foreignConnection = temp;
	                    }
	                }
	                
	                Iterator localExtensions = getExtendedElements().iterator();
	                while(localExtensions.hasNext())
	                {
	                    ArrayList extensionLink = (ArrayList)localExtensions.next();
	                    SchemaElementI localExtension = (SchemaElementI)extensionLink.get(0);
	                   
	                    if (((SchemaElementI)link.get(0)).getFullXMLName().equalsIgnoreCase(localExtension.getFullXMLName()))
	                    {
	                        String temp = (String)link.get(1);
	                        String extensionTemp = (String)extensionLink.get(1);
	                       
	                        int tempJoinCount = XftStringUtils.DelimitedStringToArrayList(temp, String.valueOf(XFT.PATH_SEPARATOR)).size() + 1;
	                        if (tempJoinCount < joinCount)
	                        {
	                            joinCount = tempJoinCount;
	                            localConnection = extensionTemp;
	                            foreignConnection = temp;
	                        }
	                    }
	                }
	            }
	            
	            if (localConnection!=null && foreignConnection !=null)
	            {
	                s = new String[3];
	                s[0]=localConnection;
	                s[1]=foreignConnection;
	                s[2]="root";
	                SetSchemaConnection(this, foreign,s);
	            }else{
	                s = new String[3];
	                s[0]="NONE";
	                s[1]="NONE";
	                SetSchemaConnection(this, foreign,s);
                }
            }
        }

        String[] s = GetSchemaConnection(this, foreign);
        if (s[0]=="NONE")
        {
            return null;
        }else{
            return s;
        }
    }

	/**
	 * Returns SchemaElementI and XMLPath for each referenced element
	 * @return ArrayList of ArrayLists(SchemaElementI,Sting)
	 */
	public synchronized ArrayList getReferencedElements()
	{
	    if (this.referencedElements==null)
	    {
	        this.referencedElements = new ArrayList();
	        
	        ArrayList checked = new ArrayList();
	        try {
                Iterator iter = ViewManager.GetFieldNames(this,ViewManager.ACTIVE,true,true).iterator();
                while (iter.hasNext())
                {
                    String s = (String)iter.next();
                    boolean extension = false;
                    try {
                        //REMOVE EXTENSIONS
                        String temp = this.getFullXMLName() + XFT.PATH_SEPARATOR + GenericWrapperElement.GetCompactXMLPath(s);
                        temp = temp.substring(0,temp.lastIndexOf(XFT.PATH_SEPARATOR));
                        if (checked.contains(temp.toLowerCase()))
                        {
                            extension = true;
                        }
                    } catch (RuntimeException e2) {
                    }
                    
                    s = s.substring(0,s.lastIndexOf(XFT.PATH_SEPARATOR));
                    if (! checked.contains(s.toLowerCase()) && !extension)
                    {
                        checked.add(s.toLowerCase());
                        if (s.indexOf(XFT.PATH_SEPARATOR) != -1)
                        {
                            try {
                                GenericWrapperField field = GenericWrapperElement.GetFieldForXMLPath(s);
                                
                                if (field.isReference())
                                {
                                    if (!field.getName().equals(this.getExtensionFieldName()))
                                    {
                                        SchemaElementI e = field.getReferenceElement();
                                        ArrayList sub = new ArrayList();
                                        sub.add(e);
                                        sub.add(s);
                                        referencedElements.add(sub);
                                    }
                                }
                            } catch (FieldNotFoundException e1) {
                                logger.error("",e1);
                            }
                        }
                    }
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
	        referencedElements.trimToSize();
	        
	        
	    }
	    return referencedElements;
	}
	
	public List<String> getExtendedXSITypes(){
		List<String> s=new ArrayList<String>();
		for(ArrayList a: getExtendedElements()){
			s.add(((GenericWrapperElement)a.get(0)).getXSIType());
		}
		return s;
	}

	/**
	 * Returns SchemaElementI and XMLPath for each extended element
	 * @return ArrayList of ArrayLists(SchemaElementI,Sting)
	 */
	public synchronized ArrayList<ArrayList> getExtendedElements()
	{
	    if (this.extendedElements==null)
	    {
	        this.extendedElements = new ArrayList<ArrayList>();
	        
	        try {
	            SchemaElementI e = this;
	            String dotSyntax = e.getFullXMLName();
	            while (e.getGenericXFTElement().isExtension())
	            {
	                dotSyntax += XFT.PATH_SEPARATOR + e.getGenericXFTElement().getExtensionFieldName();
	                e = GenericWrapperElement.GetElement(e.getGenericXFTElement().getExtensionType());
	                
	                ArrayList sub = new ArrayList();
                    sub.add(e);
                    sub.add(dotSyntax);
                    extendedElements.add(sub);
	            }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
	        
	    }
	    return extendedElements;
	}
	
	public GenericWrapperElement getExtendedElement() throws XFTInitException, ElementNotFoundException{
		return GenericWrapperElement.GetElement(this.getExtensionType());
	}
	

	public List<String> getDisplayIdentifiers() {
		try {
			if(wrapped.getDisplayIdentifiers()!=null && wrapped.getDisplayIdentifiers().size()>0){
				return wrapped.getDisplayIdentifiers();
			}else{
				if(wrapped.isExtension()){
					return this.getExtendedElement().getDisplayIdentifiers();
				}
			}
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
		
		return new ArrayList<String>();
	}
	
	public String getProperName()
	{
	    String properName = XFTReferenceManager.GetProperName(getFullXMLName());
        if (properName==null)
        {
            properName = getFullXMLName();
        }
        return properName;
	}
	
	public static void ClearElementCache() {
		GenericWrapperElement.ALL_ELEMENTS_CACHE.clear();
	}
	
	public SchemaElementI getOtherElement(String s)
	{
	    try {
            return GenericWrapperElement.GetElement(s);
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
	}
	
	public boolean equals(Object e) {
		return e instanceof GenericWrapperElement && ((GenericWrapperElement) e).getFullXMLName().equals(this.getFullXMLName());
	}
	
	public int hashCode(){
		return this.getFullXMLName().hashCode();
	}
	
	public boolean canBeRootWithBase() {
		return this.getWrapped().canBeRoot() && hasBase();
	}
    
    public boolean canBeRoot()
    {
        return wrapped.canBeRoot();
        
    }
	
	public boolean hasBase()
	{
	    if (this.getWrapped().hasBase())
	    {
	        return true;
	    }else{
	        try {
				return this.isExtension() && ((GenericWrapperElement) this.getExtensionField().getReferenceElement()).hasBase();
			} catch (Exception e) {
                return false;
            }
	    }
	}
	
	/**
	 * Gets the SQL keys.
	 * @return An array of arrays containing the SQL keys.
	 */
	public Object[][] getSQLKeys()
	{
	    TypeConverter converter = new TypeConverter(new PGSQLMapping(getWrapped().getSchemaPrefix()));

	    ArrayList keys = getAllPrimaryKeys();
		Object[][] keyArray= new Object[keys.size()][4];
		int counter = 0;
		for (final Object key : keys) {
			GenericWrapperField gwf = (GenericWrapperField) key;
			keyArray[counter][0] = gwf.getSQLName();
			keyArray[counter][1] = gwf.getType(converter);
			keyArray[counter][2] = "$" + (counter + 1);
			keyArray[counter][3] = gwf;

			counter++;
		}
		return keyArray;
	}
	
	/**
	 * @return ArrayList of SchemaElementI(s)
	 */
	public synchronized ArrayList<SchemaElementI> getPossibleExtenders()
	{
	    if (_possibleExtenders==null)
	    {
	        _possibleExtenders = new ArrayList<>();
	        
	        try {
                Iterator iter= XFTMetaManager.GetElementNames().iterator();
                while (iter.hasNext())
                {
                    String _name = (String)iter.next();
                    if (!(_name.endsWith("_meta_data") || _name.endsWith("_history") || _name.equalsIgnoreCase(this.getFullXMLName())))
                    {
                        try {
                            GenericWrapperElement gwe = GenericWrapperElement.GetElement(_name);
                            if (gwe.isExtension())
                            {
                                Iterator iter2 = gwe.getExtendedElements().iterator();
                                while (iter2.hasNext())
                                {
                                    ArrayList child = (ArrayList)iter2.next();
                                    SchemaElementI e = (SchemaElementI)child.get(0);
                                    if (e.getFullXMLName().equals(this.getFullXMLName()))
                                    {
                                        _possibleExtenders.add(gwe);
                                        break;
                                    }
                                }
                            }
                        } catch (XFTInitException e) {
                            logger.error("",e);
                        } catch (ElementNotFoundException e) {
                            logger.error("",e);
                        }
                    }
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            }
	    }
	    return _possibleExtenders;
	}
    
    public boolean isExtensionOf(GenericWrapperElement foreign){
        if (isExtension())
        {
            Iterator iter2 = getExtendedElements().iterator();
            while (iter2.hasNext())
            {
                ArrayList child = (ArrayList)iter2.next();
                SchemaElementI e = (SchemaElementI)child.get(0);
                if (e.getFullXMLName().equals(foreign.getFullXMLName()))
                {
                   return true;
                }
            }
        }
        
        return false;
    }
    

    
    public String getExtensionXMLPath(GenericWrapperElement foreign){
        if (isExtension())
        {
            Iterator iter2 = getExtendedElements().iterator();
            while (iter2.hasNext())
            {
                ArrayList child = (ArrayList)iter2.next();
                SchemaElementI e = (SchemaElementI)child.get(0);
                String path = (String)child.get(1);
                if (e.getFullXMLName().equals(foreign.getFullXMLName()))
                {
                   return path;
                }
            }
        }
        return null;
    }
    
    public boolean isAbstract(){
        return this.getWrapped().isAbstract();        
    }
    
    public String getTextFunctionName(){
        return GenericWrapperUtils.TXT_FUNCTION + this.getFormattedName();
    }


	public GenericWrapperElement ignoreHistory() throws XFTInitException, ElementNotFoundException
	{
		GenericWrapperElement gwe=this;
		if(gwe.getFullXMLName().endsWith("_history")){
			gwe=GetElement(gwe.getFullXMLName().substring(0,gwe.getFullXMLName().indexOf("_history")));
		}

		return gwe;
	}
}

