/*
 * core: org.nrg.xdat.schema.SchemaElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.schema; 
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.XftStringUtils;

/**
 * @author Tim
 *
 */
public class SchemaElement implements SchemaElementI {
	static Logger logger = Logger.getLogger(SchemaElement.class);
	private GenericWrapperElement element = null;
	private ElementDisplay display = null;
	private Hashtable arcs = null;

	public SchemaElement(GenericWrapperElement e)
	{
		element = e;
	}


	public String getSQLName()
	{
		return element.getSQLName();
	}

	public String getFormattedName()
	{
		return element.getFormattedName();
	}

	@SuppressWarnings("unused")
	public String getSQLSingleViewName()
	{
	    return element.getSingleViewName();
	}

	@SuppressWarnings("unused")
	public String getSQLMultiViewName()
	{
	    return element.getMultiViewName();
	}

	public String toString()
	{
		return element.getFullXMLName();
	}

	public String getDisplayTable()
	{
		return DisplayManager.DISPLAY_FIELDS_VIEW + element.getSQLName();
	}

	public DisplayField getDisplayField(String id) throws DisplayFieldCollection.DisplayFieldNotFoundException
	{
		return getDisplay().getDisplayFieldWException(id);
	}

    @SuppressWarnings("unused")
    public boolean hasDisplayField(String id)
	{
		try {
			this.getDisplay().getDisplayFieldWException(id);
			return true;
		} catch (DisplayFieldCollection.DisplayFieldNotFoundException e) {
			return false;
		}
	}

	public String getFullXMLName()
	{
		return element.getFullXMLName();
	}

	public String getDbName()
	{
		return element.getDbName();
	}

    @SuppressWarnings("unused")
    public boolean isInSecure() throws Exception
	{
		return ElementSecurity.IsInSecureElement(getFullXMLName());
	}

	public static SchemaElement GetElement(String n) throws XFTInitException,ElementNotFoundException
	{
		return new SchemaElement(GenericWrapperElement.GetElement(n));
	}

	public static SchemaElementI GetElement(XMLType n) throws XFTInitException,ElementNotFoundException
	{
		return new SchemaElement(GenericWrapperElement.GetElement(n));
	}

//	public static SchemaElementI GetElementByCode(String code) throws XFTInitException,ElementNotFoundException
//	{
//		return new SchemaElement(GenericWrapperElement.GetElementByCode(code));
//	}

	public static SchemaElementI GetElement(String n,String uri) throws XFTInitException,ElementNotFoundException
	{
		return new SchemaElement(GenericWrapperElement.GetElement(n,uri));
	}

	public GenericWrapperElement getGenericXFTElement()
	{
		return element;
	}

	public ElementSecurity getElementSecurity()
	{
		try {
			return ElementSecurity.GetElementSecurity(getFullXMLName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

    public String getSingularDescription(){
        return getElementSecurity().getSingularDescription();
    }

    public String getPluralDescription(){
        return getElementSecurity().getPluralDescription();
    }

    public boolean hasDisplayValueOption() {
        ElementDisplay ed = getDisplay();
        if (ed == null) {
            return false;
        }
        DisplayField idField      = ed.getDisplayField(ed.getValueField());
        DisplayField displayField = ed.getDisplayField(ed.getDisplayField());
        return idField != null && displayField != null;
    }

	/**
	 * @return The available arcs.
	 */
	public Hashtable getArcs() {
		if (arcs == null)
		{
			if (getDisplay() != null)
			{
				arcs = getDisplay().getArcs();
			}else
			{
				arcs = new Hashtable();
			}
		}

		return arcs;
	}

	/**
	 * @return The display.
	 */
	public ElementDisplay getDisplay() {
		if (display == null)
		{
			display = DisplayManager.GetElementDisplay(getFullXMLName());
		}
		return display;
	}

	public boolean hasDisplay()
	{
		return getDisplay() != null;
	}
	
	public DisplayField getSQLQueryField(String id, String header, boolean visible, boolean searchable, String dataType, String sqlColName, String subQuery, String schemaField, String schemaQueryField){
		DisplayField df=this.getDisplay().getDisplayField(id);
		if(df==null){
			df = new SQLQueryField(this.getDisplay());
			df.setId(id);
			df.setHeader(header);
			df.setVisible(visible);
			df.setSearchable(searchable);
			df.setDataType(dataType);
			df.getContent().put("sql",sqlColName);
			((SQLQueryField)df).setSubQuery(subQuery);
			((SQLQueryField)df).addMappingColumn(schemaField, schemaQueryField);
			
			try {
				this.getDisplay().addDisplayFieldWException(df);
				return df;
			} catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
		        logger.error(df.getParentDisplay().getElementName() + "." + df.getId());
				logger.error("",e);
			}
		}
		
		return df;
	}

	public DisplayField createDisplayFieldForXMLPath(String s) throws Exception
	{
		ElementDisplay ed = this.getDisplay();
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(s);
		assert f != null;
		if (f.isReference())
		{
		 	if (! f.isMultiple())
		 	{
		 		GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
		 		GenericWrapperField pk = foreign.getAllPrimaryKeys().get(0);
		 		
		 		DisplayField df = new DisplayField(this.getDisplay());
				df.setId(DisplaySearch.cleanColumnName(s).toUpperCase());
				df.setHeader(pk.getName());
				df.setVisible(true);
				df.setSearchable(true);
				df.setDataType(pk.getXMLType().getLocalType());
				df.generatedFor=s;
				DisplayFieldElement dfe = new DisplayFieldElement();
				dfe.setName("Field1");
				dfe.setSchemaElementName(s + XFT.PATH_SEPARATOR + pk.getName());
				df.addDisplayFieldElement(dfe);
				try {
					ed.addDisplayFieldWException(df);
					return df;
				} catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
	                logger.error(df.getParentDisplay().getElementName() + "." + df.getId());
					logger.error("",e);
				}
		 	}
		}else{
			if(!GenericWrapperElement.IsMultipleReference(s)){
				DisplayField df = new DisplayField(this.getDisplay());
				df.setId(DisplaySearch.cleanColumnName(s).toUpperCase());
				df.setHeader(s.substring(s.lastIndexOf("/")+1));
				df.setVisible(true);
				df.setSearchable(true);
				df.setDataType(f.getXMLType().getLocalType());
				df.generatedFor=s;
				DisplayFieldElement dfe = new DisplayFieldElement();
				dfe.setName("Field1");
				dfe.setSchemaElementName(s);
				df.addDisplayFieldElement(dfe);
				try {
					ed.addDisplayFieldWException(df);
					return df;
				} catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
	                logger.error(df.getParentDisplay().getElementName() + "." + df.getId());
					logger.error("",e);
				}
			}
		}
		return null;
	}

	/**
	 * @param xmlPath    The XML path of the field to retrieve.
	 * @return The requested display field.
     * @throws Exception When an error occurs.
	 */
	public DisplayField getDisplayFieldForXMLPath(String xmlPath) throws Exception
	{
		DisplayField temp = null;
		ElementDisplay ed = this.getDisplay();
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
		if(f==null){
			throw new FieldNotFoundException(xmlPath);
		}
		if (f.isReference())
		{
		 	if (! f.isMultiple())
		 	{
		 		GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
		 		GenericWrapperField pk = foreign.getAllPrimaryKeys().get(0);
		 		xmlPath += XFT.PATH_SEPARATOR + pk.getName();
		 	}
		}
		String localName=f.getXMLPathString(f.getParentElement().getFullXMLName());
		Iterator dfs = ed.getDisplayFieldIterator();
		while (dfs.hasNext())
		{
			DisplayField df = (DisplayField)dfs.next();
			if(df.generatedFor.equalsIgnoreCase(xmlPath)){
				temp=df;
				break;
			}else if (df.getElements().size() == 1 && df.getContent().size()==0)
			{
				DisplayFieldElement dfe = df.getElements().get(0);
				if (dfe.getSchemaElementName().equalsIgnoreCase(xmlPath)
						|| dfe.getSchemaElementName().equals(localName))
				{
					temp=df;
					break;
				}
			}
		}
		
		if(temp==null){
			temp = this.createDisplayFieldForXMLPath(xmlPath);
		}
		return temp;
	}

	public static SchemaField GetSchemaField(String xmlPath)throws FieldNotFoundException
	{
		try {
			GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
			return new SchemaField(f);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FieldNotFoundException(xmlPath);
		}
	}
	public Hashtable getDistinctIdentifierValues(String login)
	{
		Hashtable hash = new Hashtable();
		try {
			hash = ElementSecurity.GetDistinctIdValuesFor(getFullXMLName(),"default",login);
		} catch (Exception e) {
			logger.error("",e);
		}

		return hash;
	}

	/**
	 * returns SchemaFields in an ArrayList
	 * @return The primary keys for all schema fields.
	 */
	public ArrayList getAllPrimaryKeys()
	{
		return WrapFields(element.getAllPrimaryKeys());
	}


	public static ArrayList WrapFields(ArrayList old)
	{
		ArrayList al = new ArrayList();

		for (final Object anOld : old) {
			GenericWrapperField gwf = (GenericWrapperField) anOld;
			//noinspection unchecked
			al.add(new SchemaField(gwf));
		}
		return al;
	}

	@SuppressWarnings("unused")
	public static ArrayList GetUniqueValuesForField(String xmlPath) throws Exception
	{
	    return GenericWrapperElement.GetUniqueValuesForField(xmlPath);
	}

	@SuppressWarnings("unused")
	public static ArrayList GetPossibleFieldValues(String xmlPath) throws Exception
	{
	    return GenericWrapperElement.GetPossibleValues(xmlPath);
	}

	public String getDefaultPrimarySecurityField()
	{
	    String psf = null;
	    int dotCount = 100;

	    List<String> al = getDefinedFields(true);
	    if (al.size()>0)
	    {
			for (final Object anAl : al) {
				String s     = (String) anAl;
				int    count = XftStringUtils.CountStringOccurrences(s, String.valueOf(XFT.PATH_SEPARATOR));
				if (count == 0) {
					return this.getFullXMLName() + "/" + s;
				} else {
					if (count < dotCount) {
						psf = s;
					}
				}
			}
		    return this.getFullXMLName() + "/" + psf;
	    }else{
	        return null;
	    }
	}

	//this wasn't cached properly because the SchemaElement is being created new each time.  This should be pushed to a static representation to manage the persistence of the list... and use intern() too. Done...04/14/11 TO
	public List<String> getAllDefinedFields()
	{
		return getDefinedFieldManager().getDefinedFields(this);
	}
	
	public List<String> buildDefinedFields()
	{
		final List<String> _alldefinedfields = new ArrayList<>();
	    for (final String s:getDefinedFields(false))
	    {
	        _alldefinedfields.add((this.getFullXMLName() + "/" + s).intern());
	    }
	    return _alldefinedfields;
	}
		
	
	private static DefinedFieldManager dfm;
	private synchronized static DefinedFieldManager getDefinedFieldManager(){
		if(dfm==null){
			dfm=new DefinedFieldManager();
		}
		return dfm;
	}
	
	//Refactored 06/06/11 TO. Old code was returning directly from map.put (which returns the preexisting object).  Bad, bad, bad.
	private static class DefinedFieldManager{
		private Map<String,List<String>> map= new Hashtable<>();
		
		public synchronized List<String> getDefinedFields(final SchemaElement se){
			final String xsiType=se.getFullXMLName();
			if(!map.containsKey(xsiType)){
				map.put(xsiType,se.buildDefinedFields());
			}
			return map.get(xsiType);
		}
	}

	
	/**
	 * @param onlyDisplayValueOption    Indicates whether only the value should be displayed.
	 * @return A list of the fields defined for the schema element.
	 */
	//TODO: this method (and class) needs some refactoring.
	@SuppressWarnings("unchecked")
	private List<String> getDefinedFields(boolean onlyDisplayValueOption)
	{
		List<String> al = new ArrayList<>();
	    try {
            GenericWrapperElement gwe = this.getGenericXFTElement();
            ArrayList fields = ViewManager.GetFieldNames(gwe,ViewManager.QUARANTINE,true,true);
            Iterator iter = fields.iterator();

            ArrayList temp = new ArrayList();
            ArrayList temp2 = new ArrayList();
            ArrayList checked = new ArrayList();
            ArrayList ignore = new ArrayList();
            while (iter.hasNext())
            {
                String s =(String)iter.next();
                s = XftStringUtils.StandardizeXMLPath(s);
                int dotCounter = 0;
                int lastIndex = 0;
                while (s.indexOf(XFT.PATH_SEPARATOR, lastIndex + 1) != -1)
                {
                    lastIndex = s.indexOf(XFT.PATH_SEPARATOR, lastIndex + 1);
                    if (dotCounter != 0)
                    {
	                    String xmlPath = s.substring(0,lastIndex);

	                        GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
	                        if (f!=null)
	                        {
	                            if (f.isReference())
	                            {
                                    if (s.endsWith("/project") || s.endsWith("/protocol"))
                                        temp.add(s);
                                    else
                                        temp2.add(s);

	                                SchemaElement se = SchemaElement.GetElement(f.getReferenceElementName().getFullForeignType());
                                    if (!checked.contains(xmlPath)){
    	                                if (se.hasDisplayValueOption())
    	                                {
                                            checked.add(xmlPath);
                                            temp.add(xmlPath);
    	                                }
                                    }

	                                if (!se.getGenericXFTElement().getAddin().equalsIgnoreCase(""))
	                                {
	                                    ignore.add(xmlPath);
	                                }
	                            }else{
                                    temp2.add(xmlPath);
                                }
	                        }
                    }else{
                        if (s.endsWith("/project") || s.endsWith("/protocol"))
                            temp.add(s);
                        else
                            temp2.add(s);
                    }
                    dotCounter++;
                }
            }
            if (!onlyDisplayValueOption)
            {
                    temp.addAll(temp2);
            }

            ArrayList contains = new ArrayList();

            iter = temp.iterator();
            while(iter.hasNext())
            {
                String s = (String)iter.next();
                boolean ignoreBool = false;
				for (final Object anIgnore : ignore) {
					String ignoreString = (String) anIgnore;
					if (s.startsWith(ignoreString)) {
						ignoreBool = true;
						break;
					}
				}

                if (!ignoreBool)
                {
                    if (! s.endsWith(XFT.PATH_SEPARATOR + "extension") && ! s.endsWith("_info"))
                    {
                        String compact = GenericWrapperElement.GetCompactXMLPath(s);
                        if (compact!=null && !contains.contains(compact.toLowerCase()))
                        {
                            contains.add(compact.toLowerCase());
                            al.add(compact);
                        }
                    }
                }
            }
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            logger.error("",e);
        }
		return al;
	}

    /**
     * @return Returns the preLoad.
     */
    public boolean isPreLoad() {
        return element.isPreLoad();
    }
    /**
     * @param preLoad The preLoad to set.
     */
    public void setPreLoad(boolean preLoad) {
        element.setPreLoad(preLoad);
    }

    public boolean hasField(String field){
        try {
        	List al = this.getAllDefinedFields();

            return al.contains(field);
        } catch (Exception e) {
            logger.error("",e);
            return false;
        }
    }

    public SchemaElementI getOtherElement(String s)
    {
        try {
            return SchemaElement.GetElement(s);
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
    }

    public String getProperName()
    {
        return this.getGenericXFTElement().getProperName();
    }

	public boolean isRootElement()
	{
		return XFTManager.GetRootElementsHash().containsKey(this.getFullXMLName());
	}
	
	public boolean instanceOf(String s){
		return this.getGenericXFTElement().getExtendedXSITypes().contains(s);
	}
}

