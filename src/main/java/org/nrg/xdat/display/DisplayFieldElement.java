/*
 * org.nrg.xdat.display.DisplayFieldElement
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;

import org.apache.log4j.Logger;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.utils.StringUtils;

import java.util.Hashtable;

/**
 * @author Tim
 *
 */
public class DisplayFieldElement {
    static Logger logger = Logger.getLogger(DisplayFieldElement.class);
	private String name = "";
	private String schemaElementName = "";
	private String viewName = "";
	private String viewColumn = "";
	private String xdatType = "";
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public String getSchemaElementName() {
		return schemaElementName;
	}

	/**
	 * @return
	 */
	public String getViewColumn() {
		return viewColumn;
	}

	/**
	 * @return
	 */
	public String getViewName() {
		return viewName;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setSchemaElementName(String string) {
	    string  = StringUtils.StandardizeXMLPath(string);
		schemaElementName = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setViewColumn(String string) {
		viewColumn = StringUtils.intern(string);
	}

	/**
	 * @param string
	 */
	public void setViewName(String string) {
		viewName = StringUtils.intern(string);
	}

    boolean checked = false;
    private Hashtable elementMapping = new Hashtable();
	public String getSQLJoinedName(SchemaElementI e)
	{
		try {
            if (!elementMapping.containsKey(e.getFullXMLName()) && !checked)
            {
                checked=true;
            	String sqlJoinedName = ViewManager.GetViewColumnName(e.getGenericXFTElement(),getStandardizedPath(),ViewManager.DEFAULT_LEVEL,true,true);
                if (sqlJoinedName!=null)
                {
                    elementMapping.put(e.getFullXMLName(), sqlJoinedName);
                }
                return sqlJoinedName;
            }else if(elementMapping.containsKey(e.getFullXMLName())){
                return (String)elementMapping.get(e.getFullXMLName());
            }else{
                return null;
            }
        } catch (XFTInitException e1) {
            logger.error("",e1);
            return null;
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
            return null;
        }
	}

    String fieldType = null;
	public String getFieldType()
	{
        if (fieldType==null)
        {
            try {
                if (getSchemaElementName()!=null && !getSchemaElementName().equals(""))
                {
                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(getSchemaElementName());
                    fieldType= f.getXMLType().getLocalType();
                }else
                {
                    fieldType= "UNKNOWN";
                }
            } catch (Exception e) {
                fieldType= "UNKNOWN";
            }
        }

        return fieldType;
	}

    private String standardized_path=null;
    public String getStandardizedPath(){
        if(standardized_path==null){
            standardized_path = this.getSchemaElementName();
            if (standardized_path.startsWith("VIEW_"))
            {
                standardized_path = standardized_path.substring(5);
            }else{
                try {
                    SchemaFieldI f = SchemaElement.GetSchemaField(standardized_path);
                    if (f.isReference())
                    {
                        SchemaElementI foreign = f.getReferenceElement();
                        SchemaFieldI sf = (SchemaFieldI)foreign.getAllPrimaryKeys().get(0);
                        standardized_path = standardized_path + sf.getXMLPathString("");
                    }
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (Exception e) {
                    logger.error("",e);
                }
            }
        }

        return standardized_path;
    }

    SchemaField sf = null;
	public SchemaField getSchemaField() throws XFTInitException,ElementNotFoundException, Exception
	{
        if (sf==null)
        {
            GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(getSchemaElementName());
            sf= new SchemaField(f);
        }

        return sf;
	}

    /**
     * @return Returns the xdatType.
     */
    public String getXdatType() {
        return xdatType;
    }
    /**
     * @param xdatType The xdatType to set.
     */
    public void setXdatType(String xdatType) {
        this.xdatType = xdatType;
    }
}

