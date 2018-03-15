/*
 * core: org.nrg.xft.utils.ValidationUtils.ValidationResults
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils.ValidationUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.XFTFieldWrapper;
import org.nrg.xft.utils.XftStringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValidationResults implements ValidationResultsI{
	public void addResults(final ValidationResults added) {
		results.addAll(added.getResults());
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.utils.ValidationUtils.ValidationResultsI#isValid()
	 */
	@Override
	public boolean isValid() {
		return results.isEmpty();
	}

	/**
	 * Gets results collection Object[VWrapperField,String(message)]
	 * @return Returns an ArrayList of the results
	 */
	public List<Object[]> getResults() {
		return results;
	}

	/**
	 * @param list The list of results to set.
	 */
	public void setResults(final List<Object[]> list) {
		results.clear();
		results.addAll(list);
	}
	
	/**
	 * Adds error message to collection of results.
     * @param field           The field with the error.
     * @param briefMessage    A short display message.
     * @param xmlPath         The XML path of the field.
     * @param e               The element with the error result.
	 */
	public void addResult(XFTFieldWrapper field, String briefMessage, String xmlPath, GenericWrapperElement e)
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
	public void addResult(XFTFieldWrapper field, String briefMessage, String xmlPath, String fullMessage)
	{

	    Object [] result= {field,briefMessage,xmlPath,fullMessage};
	    
		results.add(result);
	}

	/**
	 * Removes validation results for the indicated field name.
	 *
	 * @param fieldName The name of the field to check for.
	 *
	 * @return The validation result that was removed.
	 */
	public Object[] removeResult(final String fieldName) {
		final int index = getFieldIndex(fieldName);
		return index > -1 ? getResults().remove(index) : null;
	}
	
	/**
	 * basic iterator for the results collection.
	 * @return Returns the Iterator for the results collection
	 */
	public Iterator getResultsIterator()
	{
		return results.iterator();
	}
	
	/**
	 * Indicates whether there is a message for this field (sql_name) in the results.
	 *
	 * @param fieldName The name of the field to check for.
	 *
	 * @return Returns true if there's a validation failure for the indicated field.
	 */
	public boolean hasField(final String fieldName) {
		final int index = getFieldIndex(fieldName);
		return index > -1;
	}
	
	/**
	 * If there is a message for this field (sql_name) in the results, its message
	 * is returned.
	 * @param fieldName The name of the field to get.
	 * @return Returns the String message
	 */
	public String getField(final String fieldName) {
		final int index = getFieldIndex(fieldName);
		return index > -1 ? (String) getResults().get(index)[1] : null;
	}

	/**
	 * Outputs the results collection as an Unordered List with HTML Tags.
	 * @return Returns a String containing the unordered list HTML
	 */
	public String toHTML()
	{
		final StringBuilder sb   = new StringBuilder();
		Iterator      iter = getResultsIterator();
		sb.append("<UL>");
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			sb.append("<li>").append(messages[2]).append(" : ").append(StringUtils.replace((String) messages[1], "'bad'", "")).append("</li>");
		}
		sb.append("</UL>");
		return sb.toString();	
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("\nValidationResults\n\n");
		sb.append("IsValid:").append(isValid()).append("\n");
		Iterator iter = getResultsIterator();
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			if (messages[0] != null)
			{
				sb.append(messages[2]).append(" : ").append(messages[1]).append("\n");
			}else
			{
				sb.append(messages[1]).append("\n");
			}
		}
		return sb.toString();
	}

	public String toFullString()
	{
	    StringBuilder sb   = new StringBuilder();
		Iterator      iter = getResultsIterator();
		while (iter.hasNext())
		{
			Object [] messages = (Object [])iter.next();
			sb.append(messages[3].toString()).append("\n");
		}
		return sb.toString();
	}
    
    @SuppressWarnings("unused")
	public static GenericWrapperField GetField(String xmlPath) throws XFTInitException, FieldNotFoundException, ElementNotFoundException{
        return GenericWrapperElement.GetFieldForXMLPath(xmlPath);
    }

    private int getFieldIndex(final String fieldName) {
		final String standardized = XftStringUtils.StandardizeXMLPath(fieldName);
		for (int index = 0; index < results.size(); index++) {
			final Object[] messages = results.get(index);
			if ((messages[0] != null && StringUtils.equalsIgnoreCase(((GenericWrapperField) messages[0]).getSQLName(), standardized)) || StringUtils.equalsAnyIgnoreCase((String) messages[2], standardized, fieldName)) {
				return index;
			}
		}
		return -1;
	}

	private final List<Object[]> results = new ArrayList<>();
}

