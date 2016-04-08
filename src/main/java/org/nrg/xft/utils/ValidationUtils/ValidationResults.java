/*
 * org.nrg.xft.utils.ValidationUtils.ValidationResults
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.XftStringUtils;
public class ValidationResults implements ValidationResultsI{
	private ArrayList results = new ArrayList(); // FIELD (VWrapperField), MESSAGE (String)
	/* (non-Javadoc)
	 * @see org.nrg.xft.utils.ValidationUtils.ValidationResultsI#isValid()
	 */
	@Override
	public boolean isValid() {
		if (results.size() > 0)
		{
			return false;
		}else
		{
			return true;
		}
	}

	/**
	 * Gets results collection Object[VWrapperField,String(message)]
	 * @return
	 */
	public ArrayList<Object[]> getResults() {
		return results;
	}

	/**
	 * @param list
	 */
	public void setResults(ArrayList list) {
		results = list;
	}
	
	/**
	 * Adds error message to collection of results.
     * @param field           The field with the error.
     * @param briefMessage    A short display message.
     * @param xmlPath         The XML path of the field.
     * @param e               The element with the error result.
	 */
	public void addResult(XFTFieldWrapper field,String briefMessage,String xmlPath,GenericWrapperElement e)
	{
	    String s;
	    if (e != null && field != null)
	    {
		    s = "The content of element '" + e.getFullXMLName() + "' is not complete. '{\"" + e.getSchemaTargetNamespaceURI() + "\":" + field.getXPATH() +  "}' " + briefMessage;
			
	    }else{
	        s = xmlPath + " " + briefMessage;
	    }
	    Object [] result= {field,briefMessage,xmlPath,s};
	    
		results.add(result);
	}
	
	/**
	 * Adds error message to collection of results.
	 * @param field           The field with the error.
	 * @param briefMessage    A short display message.
	 * @param xmlPath         The XML path of the field.
     * @param fullMessage     The full message.
	 */
	public void addResult(XFTFieldWrapper field,String briefMessage,String xmlPath,String fullMessage)
	{

	    Object [] result= {field,briefMessage,xmlPath,fullMessage};
	    
		results.add(result);
	}
	
	
	/**
	 * basic iterator for the results collection.
	 * @return
	 */
	public Iterator getResultsIterator()
	{
		return results.iterator();
	}
	
	/**
	 * If there is a message for this field (sql_name) in the results, its message
	 * is returned.
	 * @param s
	 * @return
	 */
	public String getField(String s)
	{
        String original = s;
	    s = XftStringUtils.StandardizeXMLPath(s);
		Iterator iter = getResultsIterator();
		while(iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			if (messages[0]!=null)
			{
				if (((GenericWrapperField)messages[0]).getSQLName().equalsIgnoreCase(s))
				{
					return (String)messages[1];
				}
			}
			if(((String)messages[2]).equalsIgnoreCase(s))
			{
			    return (String)messages[1];
			}
            if(((String)messages[2]).equalsIgnoreCase(original))
            {
                return (String)messages[1];
            }
		}
		
		return null;
	}
	
	/**
	 * Outputs the results collection as an Unordered List with HTML Tags.
	 * @return
	 */
	public String toHTML()
	{
		StringBuffer sb = new StringBuffer();
		Iterator iter = getResultsIterator();
		sb.append("<UL>");
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			sb.append("<li>" + messages[2] + " : " + StringUtils.replace((String)messages[1], "'bad'", "") + "</li>");
		}
		sb.append("</UL>");
		return sb.toString();	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("\nValidationResults\n\n");
		sb.append("IsValid:" + isValid() + "\n");
		Iterator iter = getResultsIterator();
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			if (messages[0] != null)
			{
				GenericWrapperField field = ((GenericWrapperField)messages[0]);
				sb.append(messages[2] + " : " + messages[1] + "\n");
			}else
			{
				sb.append(messages[1] + "\n");
			}
		}
		return sb.toString();
	}

	public String toFullString()
	{
	    StringBuffer sb = new StringBuffer();
		Iterator iter = getResultsIterator();
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			sb.append(messages[3].toString() + "\n");
		}
		return sb.toString();
	}
    
    public static GenericWrapperField GetField(String xmlPath) throws XFTInitException,FieldNotFoundException,ElementNotFoundException{
        return GenericWrapperElement.GetFieldForXMLPath(xmlPath);
    }
}

