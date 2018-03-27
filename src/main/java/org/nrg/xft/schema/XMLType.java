/*
 * core: org.nrg.xft.schema.XMLType
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.utils.XftStringUtils;

import java.text.ParseException;

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
			localPrefix = XftStringUtils.intern(xml.substring(0, index));
			setLocalType(xml.substring(index + 1).intern());
		}else
		{
			localPrefix = XftStringUtils.intern(s.getTargetNamespacePrefix());
            setLocalType(xml.intern());
		}
		if (local.contains(":"))
		{
            setLocalType(XftStringUtils.intern(local.substring(index + 1)));
		}
		if (localPrefix.equalsIgnoreCase(""))
		{
			fullLocalType = XftStringUtils.intern(local);
		}else{
			fullLocalType = XftStringUtils.intern(localPrefix + ":" + local);
		}
		schema = s;
	}
	

	/**
	 * Removes any characters before (and including) the ':' character.
	 * @param xml
	 * @return Returns the type as a String without its prefix and the ':' character
	 */
	public static String CleanType(String xml) {
		return StringUtils.contains(xml, ":") ? StringUtils.substringAfter(xml, ":") : xml;
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
	 * @return Returns the prefix String (including the ':')
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
	 * @return Returns the locally defined prefix for this dataType
	 */
	public String getLocalPrefix() {
		return localPrefix;
	}

	/**
	 * Locally defined dataType
	 * @return Returns the locally defined dataType
	 */
	public String getLocalType() {
		return local;
	}

	/**
	 * Locally defined prefix for this dataType.
	 * @param string
	 */
	public void setLocalPrefix(String string) {
		localPrefix = XftStringUtils.intern(string);
	}

	/**
	 * Locally defined dataType
	 * @param string
	 */
	public void setLocalType(String string) {
        if (string.equalsIgnoreCase("anyURI")){
            string = "string";
        }
		local = XftStringUtils.intern(string);
	}

	/**
	 * localPrefix + ':' + localType
	 * @return Returns the local type (including local prefix)
	 */
	public String getFullLocalType() {
		return fullLocalType;
	}

	/**
	 * foreignPrefix + ':' + localType
	 * @return Returns the local type (including foreign prefix)
	 */
	public String getFullForeignType() {
		if (! this.getForeignPrefix().equalsIgnoreCase(""))
		{
			if (fullForeignType == null)
			{
				fullForeignType = XftStringUtils.intern(this.getForeignPrefix() + ":" + this.local);
			}
			return fullForeignType;
		}else{
			return this.local;
		}
	}

	/**
	 * @return Returns the XFTSchema
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
		fullLocalType = XftStringUtils.intern(string);
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
	 * @return Returns the foreign prefix
	 */
	public String getForeignPrefix() {
		if (foreignPrefix == null) {
			if (getLocalPrefix().equalsIgnoreCase(schema.getTargetNamespacePrefix()) || getLocalPrefix().equalsIgnoreCase(schema.getXMLNS())) {
				foreignPrefix = localPrefix;
			} else {
				final String uri = schema.getURIForPrefix(localPrefix);
				if (uri == null) {
					foreignPrefix = localPrefix;
				} else {
					foreignPrefix = XFTMetaManager.TranslateURIToPrefix(uri);
					if (foreignPrefix == null) {
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
		               return org.nrg.xft.utils.DateUtils.parseDateTime(o.toString());
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

