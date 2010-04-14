//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 17, 2004
 */
package org.nrg.xft.schema;

import java.text.ParseException;

import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.utils.StringUtils;

/**
 * This class identifies a specific type of XML Element.  The XMLType
 * can specify a basic data type or a created data type.
 * 
 * <BR><BR>The local field specifies the name of the data type.  The
 * localPrefix identifies the prefix assigned within this document to 
 * this item.  If this is a basic data type, then the localPrefix will
 * match the XMLNS of the schema.  For example, with a type of xs:string
 * xs would become the localPrefix and string would become the local.  In
 * the case of basic data types, the foreignPrefix will be equal to the 
 * localPrefix.  For a generated type, if the data type was defined in the 
 * same schema then its localPrefix and foreignPrefix will be the XFTSchema's
 * targetNamespacePrefix.  If the generated type refers to a foreign data type 
 * (defined in a seperate namespace) then the localPrefix will define the local
 * schema's version of the data type prefix.  This prefix will be matched within
 * that schema to provide the URI of the referenced schema.  This URI can then
 * be used to find the original defined prefix for that data type through the
 * XFTMetaManager's URI-to-Prefix mapping.  The original prefix will have been used
 * to define the element throughout the application.  This orginal prefix will 
 * become the foreignPrefix of the XMLType.
 * 
 * <BR><BR>The fullLocalType will give the localPrefix:local.  The fullForeignType will
 * give the foreignPefix:local.
 * 
 * @author Tim
 */
public class XMLType {
	private static final String TIMESTAMP = "timestamp";
	private static final String DATE_TIME = "dateTime";
	private static final String DATE = "date";
	private static final String DOUBLE = "double";
	private static final String DECIMAL = "decimal";
	private static final String FLOAT = "float";
	private static final String INTEGER = "integer";
	private XFTSchema schema=null;
	
	private String localPrefix = null;
	private String foreignPrefix = null;
	private String local = null;
	
	private String fullLocalType= null;
	private String fullForeignType = null;
	private String foreignURI = null;
	
	/**
	 * if the xml string has a ':' then the characters before it will become the localPrefix, and 
	 * the characters after it will become the local Field.  Otherwise, the xml string will become
	 * the local Field and the XFTSchema's target namespace prefix will become the localPrefix.
	 * 
	 * @param xml
	 * @param s
	 */
	public XMLType(String xml,XFTSchema s)
	{
		int index = xml.indexOf(":");
		if (index != -1)
		{
			localPrefix = StringUtils.intern(xml.substring(0,index));
			setLocalType(xml.substring(index + 1).intern());
		}else
		{
			localPrefix = StringUtils.intern(s.getTargetNamespacePrefix());
            setLocalType(xml.intern());
		}
		if (local.indexOf(":")!=-1)
		{
            setLocalType(StringUtils.intern(local.substring(index + 1)));
		}
		if (localPrefix.equalsIgnoreCase(""))
		{
			fullLocalType = StringUtils.intern(local);
		}else{
			fullLocalType = StringUtils.intern(localPrefix + ":" + local);
		}
		schema = s;
	}
	

	/**
	 * Removes any characters before (and including) the ':' character.
	 * @param xml
	 * @return
	 */
	public static String CleanType(String xml)
	{
		int index = xml.indexOf(":");
		if (index != -1)
		{
			return xml.substring(index + 1);
		}else
		{
			return xml;
		}
	}
	
	public static boolean IsDate(String t)
	{
	    String clean = CleanType(t);
	    if (clean.equalsIgnoreCase(DATE) || clean.equalsIgnoreCase("datetime") || clean.equalsIgnoreCase("time") || clean.equalsIgnoreCase(TIMESTAMP))
	    {
	        return true;
	    }else{
	        return false;
	    }
	}
	

	/**
	 * Removes any characters before (and including) the ':' character.
	 * @param xml
	 * @return
	 */
	public static String GetPrefix(String xml)
	{
		int index = xml.indexOf(":");
		if (index != -1)
		{
			return xml.substring(0,index);
		}else
		{
			return "";
		}
	}

	/**
	 * Locally defined prefix for this dataType.
	 * @return
	 */
	public String getLocalPrefix() {
		return localPrefix;
	}

	/**
	 * Locally defined dataType
	 * @return
	 */
	public String getLocalType() {
		return local;
	}

	/**
	 * Locally defined prefix for this dataType.
	 * @param string
	 */
	public void setLocalPrefix(String string) {
		localPrefix = StringUtils.intern(string);
	}

	/**
	 * Locally defined dataType
	 * @param string
	 */
	public void setLocalType(String string) {
        if (string.equalsIgnoreCase("anyURI")){
            string = "string";
        }
		local = StringUtils.intern(string);
	}

	/**
	 * localPrefix + ':' + localType
	 * @return
	 */
	public String getFullLocalType() {
		return fullLocalType;
	}

	/**
	 * foreignPrefix + ':' + localType
	 * @return
	 */
	public String getFullForeignType() {
		if (! this.getForeignPrefix().equalsIgnoreCase(""))
		{
			if (fullForeignType == null)
			{
				fullForeignType = StringUtils.intern(this.getForeignPrefix() + ":" + this.local);
			}
			return fullForeignType;
		}else{
			return this.local;
		}
	}

	/**
	 * @return
	 */
	public XFTSchema getSchema() {
		return schema;
	}

	/**
	 * localPrefix + ':' + localType
	 * @param string
	 */
    @SuppressWarnings("unused")
	private void setFullXMLType(String string) {
		fullLocalType = StringUtils.intern(string);
	}

	/**
	 * @param schema
	 */
	public void setSchema(XFTSchema schema) {
		this.schema = schema;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return this.getFullLocalType();
	}
	
	/**
	 * If the XMLType refers to a basic data type or a locally defined data type (based on
	 * the schema's target namespace prefix), then the localPrefix is returned, else the 
	 * valid original prefix is found and returned.
	 * @return
	 */
	public String getForeignPrefix()
	{
		if (foreignPrefix == null){
			if (this.getLocalPrefix().equalsIgnoreCase(schema.getTargetNamespacePrefix()) || getLocalPrefix().equalsIgnoreCase(schema.getXMLNS()))
			{
				foreignPrefix = localPrefix;
			}else
			{
				String uri = schema.getURIForPrefix(localPrefix);
				if (uri==null)
				{
                    foreignPrefix = localPrefix;
				}else{
					try {
	                    foreignPrefix = XFTMetaManager.TranslateURIToPrefix(uri);
	                } catch (RuntimeException e) {
	                    foreignPrefix = localPrefix;
	                }
				}
			}
		}
		
		return foreignPrefix;
	}
	
	public String getLocalXMLNS()
	{
	    return this.schema.getTargetNamespaceURI();
	}
	
	public String getForeignXMLNS()
	{
	    if (foreignURI ==null)
		{
	        foreignURI = schema.getURIForPrefix(localPrefix);
	        if (foreignURI==null)
	        {
	            foreignURI= this.schema.getTargetNamespaceURI();
	        }
		}
	    return foreignURI;
	}
    
    public String getText(boolean withPrefix){
        if (withPrefix){
            return this.getFullForeignType();
        }else{
            return this.getLocalType();
            
        }
    }
	
	public Object parseValue(Object o)
	{
	    return ParseValue(o,this.local);
	}
	
	public static Object ParseValue(Object o, String type)
	{
	    if (o instanceof String)
	    {
	        if (type.equalsIgnoreCase(INTEGER))
	        {
	            try {
                    return Integer.valueOf(o.toString());
                } catch (NumberFormatException e) {
                    return o;
                }
	        }else if (type.equalsIgnoreCase(FLOAT))
	        {
	            try {
                    return Float.valueOf(o.toString());
                } catch (NumberFormatException e) {
                    return o;
                }
	        }else if (type.equalsIgnoreCase(DECIMAL))
	        {
	            try {
                    return Float.valueOf(o.toString());
                } catch (NumberFormatException e) {
                    return o;
                }
	        }else if (type.equalsIgnoreCase(DOUBLE))
	        {
	            try {
                    return Double.valueOf(o.toString());
                } catch (NumberFormatException e) {
                    return o;
                }
	        }else if (type.equalsIgnoreCase(DATE))
	        {
	           try {
	               return org.nrg.xft.utils.DateUtils.parseDate(o.toString());
	            } catch (ParseException e) {
	                return o;
	            }
	        }else if (type.equalsIgnoreCase(DATE_TIME))
	        {
	            try {
		               return org.nrg.xft.utils.DateUtils.parseDate(o.toString());
		            } catch (ParseException e) {
		                return o;
		            }
	        }else if (type.equalsIgnoreCase(TIMESTAMP))
	        {
	            try {
		               return org.nrg.xft.utils.DateUtils.parseDate(o.toString());
		            } catch (ParseException e) {
		                return o;
		            }
	        }else{
	            return o;
	        }
	    }else{
	        return o;
	    }
	}
}

