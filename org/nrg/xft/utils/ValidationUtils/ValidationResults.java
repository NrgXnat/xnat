//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on May 21, 2004
 */
package org.nrg.xft.utils.ValidationUtils;
import java.util.ArrayList;
import java.util.Iterator;

import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.StringUtils;
/**
 * Contains all of the validation errors found in a validation process.  If the 
 * collection of errors is empty then the results are valid, otherwise they are
 * invalid.
 * 
 * <BR><BR>The object stores a collection of validation errors.  The collection stores
 * a series of Object[VWrapperField,String(message)].
 * 
 * @author Tim
 */
public class ValidationResults {
	private ArrayList results = new ArrayList(); // FIELD (VWrapperField), MESSAGE (String)
	/**
	 * If there were any erros then false, else true.
	 * @return
	 */
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
	 * @param field
	 * @param message
	 */
	public void addResult(XFTFieldWrapper field,String briefMessage,String xmlPath,GenericWrapperElement e)
	{
	    String s = "";
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
	 * @param field
	 * @param message
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
	    s = StringUtils.StandardizeXMLPath(s);
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
			sb.append("<li>" + messages[2] + " : " + StringUtils.ReplaceStr((String)messages[1],"'bad'","") + "</li>");
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

