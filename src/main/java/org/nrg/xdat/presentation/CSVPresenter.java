/*
 * org.nrg.xdat.presentation.CSVPresenter
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.presentation;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.utils.XftStringUtils;
/**
 * @author Tim
 *
 */
public class CSVPresenter extends PresentationA {
	static org.apache.log4j.Logger logger = Logger.getLogger(CSVPresenter.class);
	
	public String getVersionExtension(){return "csv";}

	public XFTTableI formatTable(XFTTableI table,DisplaySearch search) throws XFTInitException,ElementNotFoundException
	{
	    return formatTable(table,search,true);
	}
	
	public XFTTableI formatTable(XFTTableI table,DisplaySearch search,boolean allowDiffs) throws XFTInitException,ElementNotFoundException
	{
		logger.debug("BEGIN CVS FORMAT");
		XFTTable csv = new XFTTable();
		ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
		ArrayList visibleFields = this.getVisibleFields(ed,search);
		
		//int fieldCount = visibleFields.size() + search.getInClauses().size();

		ArrayList columnHeaders = new ArrayList();
		ArrayList diffs = new ArrayList();
		
		if (search.getInClauses().size()>0)
		{
		    for(int i=0;i<search.getInClauses().size();i++)
		    {
		        columnHeaders.add("");
		    }
		}
		
		//POPULATE HEADERS
		
		Iterator fields = visibleFields.iterator();
		int counter = search.getInClauses().size();
		while (fields.hasNext())
		{
			DisplayFieldReferenceI df = (DisplayFieldReferenceI)fields.next();
			if (allowDiffs)
			{
				if (!diffs.contains(df.getElementName()))
				{
				    diffs.add(df.getElementName());
				    SchemaElementI foreign = SchemaElement.GetElement(df.getElementName());
				    if (search.isMultipleRelationship(foreign))
				    {
					    String temp = XftStringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
					    Integer index = ((XFTTable)table).getColumnIndex(temp);
					    if (index!=null)
					    {
						    columnHeaders.add("Diff");
					    }
				    }
				}
			}
			
			if (!df.isHtmlContent())
			{
				String value = df.getHeader();
				if ("ID".equals(value)){
					// Workaround for MS SYLK issue:
					// http://support.microsoft.com/kb/215591
					// http://nrg.wustl.edu/fogbugz/default.php?418
					value = value.toLowerCase();
				}
				columnHeaders.add(value);
			}
		}
		csv.initTable(columnHeaders);
		
		//POPULATE DATA
		table.resetRowCursor();
		
		while (table.hasMoreRows())
		{
			Hashtable row = table.nextRowHash();
			Object[] newRow = new Object[columnHeaders.size()];
			fields = visibleFields.iterator();

			diffs = new ArrayList();
			if (search.getInClauses().size()>0)
			{
			    for(int i=0;i<search.getInClauses().size();i++)
			    {
			        Object v = row.get("search_field"+i);
			        if (v!=null)
			        {
				        newRow[i] = v;
			        }else{
				        newRow[i] = "";
			        }
			    }
			}
			
			counter = search.getInClauses().size();
			while (fields.hasNext())
			{
			    DisplayFieldReferenceI dfr = (DisplayFieldReferenceI)fields.next();
				if(!dfr.isHtmlContent())
				{

					try {
					    if (allowDiffs)
					    {
		                    if (!diffs.contains(dfr.getElementName()))
		                    {
		                        diffs.add(dfr.getElementName());
		                        SchemaElementI foreign = SchemaElement.GetElement(dfr.getElementName());
		                        if (search.isMultipleRelationship(foreign))
		                        {
		                    	    String temp = XftStringUtils.SQLMaxCharsAbbr(search.getRootElement().getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
		                    	    Integer index = ((XFTTable)table).getColumnIndex(temp);
		                    	    if (index!=null)
		                    	    {
		                    		    String diff = "";
		                    		    Object d = row.get(temp.toLowerCase());
		                    	        if (d!=null)
		                    	        {
		                    		        diff=  (String)d.toString();
		                    	        }else{
		                    	            diff="";
		                    	        }
		                    		    newRow[counter++]=diff;
		                    	    }
		                        }
		                    }
					    }
	                    
	                    Object v = null;
	                    if (dfr.getElementName().equalsIgnoreCase(search.getRootElement().getFullXMLName()))
	                    {
	                    	v = row.get(dfr.getRowID().toLowerCase());
	                    }else{
	                    	v = row.get(dfr.getElementSQLName().toLowerCase() + "_" + dfr.getRowID().toLowerCase());
	                    }
	                    if (v != null)
	                    {
	                    	newRow[counter] = v;
	                    }
	                } catch (XFTInitException e) {
	                    logger.error("",e);
	                } catch (ElementNotFoundException e) {
	                    logger.error("",e);
	                }

					counter++;
				}
			}
			csv.insertRow(newRow);
		}
		

		logger.debug("END CVS FORMAT");
		return csv;
	}
}

