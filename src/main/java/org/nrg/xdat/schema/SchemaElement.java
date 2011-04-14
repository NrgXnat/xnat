//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 6, 2005
 *
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
import org.nrg.xft.utils.StringUtils;
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

	public String getSQLSingleViewName()
	{
	    return element.getSingleViewName();
	}

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
		return (DisplayField)this.getDisplay().getDisplayFieldWException(id);
	}

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

	public boolean hasDisplayValueOption()
	{
		ElementDisplay ed = getDisplay();
		if (ed ==null)
		{
		    return false;
		}else{
		    DisplayField idField = ed.getDisplayField(ed.getValueField());
			DisplayField displayField = ed.getDisplayField(ed.getDisplayField());
			if (idField != null && displayField != null)
			{
			    return true;
			}else{
			    return false;
			}
		}
	}

	/**
	 * @return
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
	 * @return
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
		if (getDisplay()==null)
		{
			return false;
		}else
		{
			return true;
		}
	}

	public DisplayField createDisplayFieldForXMLPath(String s) throws XFTInitException,ElementNotFoundException,Exception
	{
		DisplayField temp = null;
		ElementDisplay ed = this.getDisplay();
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(s);
		if (f.isReference())
		{
		 	if (! f.isMultiple())
		 	{
		 		GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
		 		GenericWrapperField pk = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
		 		
		 		DisplayField df = new DisplayField(this.getDisplay());
				df.setId(DisplaySearch.cleanColumnName(s).toUpperCase());
				df.setHeader(pk.getName());
				df.setVisible(true);
				df.setSearchable(true);
				df.setDataType(pk.getXMLType().getLocalType());
				df.generatedFor=s;
				DisplayFieldElement dfe = new DisplayFieldElement();
				dfe.setName("Field1");
				dfe.setSchemaElementName(s + XFT.PATH_SEPERATOR+ pk.getName());
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
			}else{
				
			}
		}
		return null;
	}

	/**
	 * @param s
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 * @throws Exception
	 */
	public DisplayField getDisplayFieldForXMLPath(String s) throws XFTInitException,ElementNotFoundException,Exception
	{
		DisplayField temp = null;
		ElementDisplay ed = this.getDisplay();
		GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(s);
		if(f==null){
			throw new FieldNotFoundException(s);
		}
		if (f.isReference())
		{
		 	if (! f.isMultiple())
		 	{
		 		GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
		 		GenericWrapperField pk = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
		 		s += XFT.PATH_SEPERATOR+ pk.getName();
		 	}
		}
		String localName=f.getXMLPathString(f.getParentElement().getFullXMLName());
		Iterator dfs = ed.getDisplayFieldIterator();
		while (dfs.hasNext())
		{
			DisplayField df = (DisplayField)dfs.next();
			if(df.generatedFor.equalsIgnoreCase(s)){
				temp=df;
				break;
			}else if (df.getElements().size() == 1 && df.getContent().size()==0)
			{
				DisplayFieldElement dfe = (DisplayFieldElement)df.getElements().get(0);
				if (dfe.getSchemaElementName().equalsIgnoreCase(s)
						|| dfe.getSchemaElementName().equals(localName))
				{
					temp=df;
					break;
				}
			}
		}
		
		if(temp==null){
			temp = this.createDisplayFieldForXMLPath(s);
		}
		return temp;
	}

	public static SchemaField GetSchemaField(String xmlPath)throws FieldNotFoundException
	{
		try {
			GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
			return new SchemaField(f);
		} catch (XFTInitException e) {
			e.printStackTrace();
			throw new FieldNotFoundException(xmlPath);
		} catch (ElementNotFoundException e) {
			e.printStackTrace();
			throw new FieldNotFoundException(xmlPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new FieldNotFoundException(xmlPath);
		}
	}
//
//	public ArrayList getSchemaFields()
//	{
//		ArrayList al = new ArrayList();
//		Iterator iter = this.element.getAllFields(false,false).iterator();
//		while (iter.hasNext())
//		{
//			GenericWrapperField f = (GenericWrapperField)iter.next();
//			SchemaField sf = new SchemaField(f);
//			al.add(sf);
//		}
//		al.trimToSize();
//		return al;
//	}
//
//	public ArrayList getSchemaFieldsInstances()
//	{
//		ArrayList al = new ArrayList();
//		Iterator iter = this.element.getMetaFields().getIterator();
//		while (iter.hasNext())
//		{
//			MetaField mf = (MetaField)iter.next();
//			SchemaFieldInstance esf = new SchemaFieldInstance(mf,false);
//			 al.add(esf);
//		}
//
//
//		al.trimToSize();
//		return al;
//	}
//
//	public ArrayList getSchemaFieldsInstances(ItemI item)
//	{
//		ArrayList al = getSchemaFieldsInstances();
//		Iterator iter = al.iterator();
//		while (iter.hasNext())
//		{
//			SchemaFieldInstance esf = (SchemaFieldInstance)iter.next();
//			try {
//				if (esf.isReference())
//				{
//					Object o = item.getProperty(esf.getName());
//					if (o != null)
//					{
//						esf.setValue(o);
//					}
//				}else{
//					Object o = item.getProperty(esf.getName());
//					if (o != null)
//					{
//						esf.setValue(o);
//					}
//				}
//			} catch (XFTInitException e) {
//				logger.error("",e);
//			} catch (ElementNotFoundException e) {
//				logger.error("",e);
//			} catch (FieldNotFoundException e) {
//				logger.error("",e);
//			}
//		}
//		return al;
//	}

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
	 * @return
	 */
	public ArrayList getAllPrimaryKeys()
	{
		return WrapFields(element.getAllPrimaryKeys());
	}


	public static ArrayList WrapFields(ArrayList old)
	{
		ArrayList al = new ArrayList();

		Iterator iter = old.iterator();
		while (iter.hasNext())
		{
			GenericWrapperField gwf = (GenericWrapperField)iter.next();
			al.add(new SchemaField(gwf));
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList GetUniqueValuesForField(String xmlPath) throws Exception
	{
	    return GenericWrapperElement.GetUniqueValuesForField(xmlPath);
	}

	public static ArrayList GetPossibleFieldValues(String xmlPath) throws Exception
	{
	    return GenericWrapperElement.GetPossibleValues(xmlPath);
	}

	public String getDefaultPrimarySecurityField()
	{
	    String psf = null;
	    int dotCount = 100;

	    List al = getDefinedFields(true);
	    if (al.size()>0)
	    {
		    Iterator iter = al.iterator();
		    while (iter.hasNext())
		    {
		        String s = (String)iter.next();
		        int count = StringUtils.CountStringOccurrences(s,String.valueOf(XFT.PATH_SEPERATOR));
		        if (count==0)
		        {
		            return this.getFullXMLName() + "/" + s;
		        }else{
		            if (count < dotCount)
		            {
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
		final List<String> _alldefinedfields =new ArrayList<String>();
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
	
	private static class DefinedFieldManager{
		private Map<String,List<String>> map=new Hashtable<String,List<String>>();
		
		public synchronized List<String> getDefinedFields(final SchemaElement se){
			final String xsiType=se.getFullXMLName();
			if(map.containsKey(xsiType)){
				return map.get(xsiType);
			}else{
				return map.put(xsiType,se.buildDefinedFields());
			}
		}
	}

	
	/**
	 * @param onlyDisplayValueOption
	 * @return
	 */
	//TODO: this method (and class) needs some refactoring.
	private List<String> getDefinedFields(boolean onlyDisplayValueOption)
	{
		List<String> al = new ArrayList<String>();
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
                s = StringUtils.StandardizeXMLPath(s);
                int dotCounter = 0;
                int lastIndex = 0;
                while (s.indexOf(XFT.PATH_SEPERATOR,lastIndex + 1)!=-1)
                {
                    lastIndex = s.indexOf(XFT.PATH_SEPERATOR,lastIndex + 1);
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
                Iterator ignoreIter = ignore.iterator();
                while (ignoreIter.hasNext())
                {
                    String ignoreString = (String)ignoreIter.next();
                    if (s.startsWith(ignoreString))
                    {
                        ignoreBool = true;
                        break;
                    }
                }

                if (!ignoreBool)
                {
                    if (! s.endsWith(XFT.PATH_SEPERATOR +"extension") && ! s.endsWith("_info"))
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
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
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
	    if (XFTManager.GetRootElementsHash().containsKey(this.getFullXMLName()))
	    {
	        return true;
	    }else{
	        return false;
	    }
	}
}

