/*
 * core: org.nrg.xft.XFTTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.ValidationException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.XMLValidator;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.w3c.dom.Document;
public class XFTTool {
	
	/**
	 * Generate CREATE, ALTER, VIEW, and INSERT statements for each element in the 
	 * defined schemas.
	 * @param outputFile (location to save generated sql)
	 */
	public static void GenerateSQL(String outputFile) throws Exception
	{
		SQLCreateGenerator.generateDoc(outputFile);
	}
	
	/**
	 * Performs a select on the database for all rows in the given element's table.  
	 * As a 'simple' browse, this search does not join to any of the child elements 
	 * of this element.
	 * @param elementName (XML name of the element whose data will be returned)
	 * @return
	 * @throws XFTInitException
	 * @throws DBPoolException
	 * @throws SQLException
	 * @throws ElementNotFoundException
	 */
	public static XFTTable BrowseAllSimple(String elementName) throws XFTInitException, DBPoolException, SQLException, org.nrg.xft.exception.ElementNotFoundException,Exception
	{
		GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
		TableSearch search = new TableSearch();
		search.setElement(element);
		search.setJoined(false);
		return (XFTTable)search.execute(null);
	}
	
	/**
	 * Performs a select on the database for all rows in the given element's table.  
	 * As a 'Grand' browse, this search will join the primary table to all of its child 
	 * tables (Ref Elements).
	 * @param elementName
	 * @return
	 * @throws XFTInitException
	 * @throws DBPoolException
	 * @throws SQLException
	 * @throws org.nrg.xft.exception.ElementNotFoundException
	 */
	public static XFTTable BrowseAllGrand(String elementName) throws XFTInitException, DBPoolException, SQLException, org.nrg.xft.exception.ElementNotFoundException,Exception
	{
		GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
		TableSearch search = new TableSearch();
		search.setElement(element);
		search.setJoined(true);
		return (XFTTable)search.execute(null);
	}
//	
//	/**
//	 * Returns an xml document with a list element as a collection of the elements 
//	 * which match the values in the supplied Hashtable.
//	 * @param element
//	 * @param values (Hashtable: key (sql_table.sql_field),value(sql_value))
//	 * @return
//	 * @throws XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws java.sql.SQLException
//	 * @throws DBPoolException
//	 */
//	public static Document FindXML(String elementName,CriteriaCollection criteria) throws XFTInitException,ElementNotFoundException, java.sql.SQLException,DBPoolException,FieldNotFoundException,Exception
//	{
//		GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
//		ItemSearch search = new ItemSearch();
//		search.setElement(element);
//		search.setCriteriaCollection(criteria);
//		ItemCollection al = search.exec(true);
//		if (XFT.VERBOSE)
//            System.out.println(al.size() + " results found.");
//		return XMLWriter.XFTItemListToDOM(al.getItems());
//	}
//
//	
//	/**
//	 * Returns an xml document with a list element as a collection of the elements 
//	 * which match the values in the supplied Hashtable.
//	 * @param element
//	 * @param values (Hashtable: key (sql_table.sql_field),value(sql_value))
//	 * @return
//	 * @throws XFTInitException
//	 * @throws ElementNotFoundException
//	 * @throws java.sql.SQLException
//	 * @throws DBPoolException
//	 */
//	public static Document FindXML(String xmlPath,Object value, UserI user) throws XFTInitException,ElementNotFoundException, java.sql.SQLException,DBPoolException,FieldNotFoundException,Exception
//	{
//		ItemCollection items = ItemSearch.GetItems(xmlPath,value,user,true);
//		if (XFT.VERBOSE)
//            System.out.println(items.size() + " results found.");
//		return XMLWriter.XFTItemListToDOM(items.getItems());
//	}
	/**
	 * Returns a table with the results of an SQL select based on the elements which 
	 * match the SearchCriteria in the supplied ArrayList.
	 * @param elementName    The element name to search on.
	 * @param values         The values to search on.
	 * @return The table of search results.
     * @throws Exception When something goes wrong.
	 */
	public static XFTTable Search(String elementName,CriteriaCollection values) throws Exception
	{
		GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
		TableSearch search = new TableSearch();
		search.setCriteriaCollection(values);
		search.setElement(element);
		search.setJoined(true);
		return (XFTTable)search.execute(null);
	}
	
	/**
	 * Returns the names of all available schema elements known to XFT.
	 * @return ArrayList of Strings
	 * @throws XFTInitException
	 */
	public static ArrayList GetPossibleElements() throws XFTInitException
	{
		return org.nrg.xft.meta.XFTMetaManager.GetElementNames();
	}
	
	
	/**
	 * Checks if this elementName is known to XFT.
	 * @return ArrayList of Strings
	 * @throws XFTInitException
	 */
	public static boolean ValidateElementName(String elementName) throws XFTInitException
	{
		try {
			GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
		} catch (ElementNotFoundException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Checks if this elementName is known to XFT.
	 * @return ArrayList of Strings
	 * @throws XFTInitException
	 */
	public static String GetValidElementName(String elementName) throws XFTInitException
	{
		try {
			GenericWrapperElement element = GenericWrapperElement.GetElement(elementName);
			return element.getFullXMLName();
		} catch (ElementNotFoundException e) {
			return elementName;
		}
	}
//	
//	/**
//	 * Stores the data from the table into XML DOM files using the elementName in the 
//	 * given destinationDir.
//	 * @param elementName
//	 * @param table
//	 * @param destinationDir
//	 * @throws ElementNotFoundException
//	 * @throws XFTInitException
//	 * @throws Exception
//	 */
//	public static void StoreTableAsXMLFiles(String elementName,XFTTable table,String destinationDir, boolean withChildren) throws ElementNotFoundException,XFTInitException,Exception
//	{
//		ItemSearch search = new ItemSearch();
//		ItemCollection items = search.populateItems(elementName,table,withChildren,false,true);
//		XMLWriter.StoreXFTItemListToXMLFile(items.getItems(),destinationDir);
//	}
	
	/**
	 * Saves this item and all of its children (refs) to the database, and returns the updated xml.
	 * @param f                   The file to retrieve.
     * @param user                The user.
     * @param quarantine          Whether the data object should be quarantined.
     * @param allowItemRemoval    Whether the item can be removed.
	 * @return The document.
     * @throws XFTInitException When an error occurs in XFT.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
	 */
	public static Document StoreXMLToDB(File f, UserI user,Boolean quarantine,boolean allowItemRemoval) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
	{
	    boolean overrideSecurity = false;
	    if (user == null)
	    {
	        throw new Exception("Error: No username and password.");
	    }
		//XFTItem item = XMLReader.TranslateDomToItem(doc,user);
		SAXReader reader = new SAXReader(user);
		org.nrg.xft.XFTItem item = reader.parse(f);
		boolean q;
		boolean override;
		if (quarantine!=null)
		{
		    q = quarantine.booleanValue();
		    override = true;
		}else{
		    q = item.getGenericSchemaElement().isQuarantine();
		    override = false;
		}
    	SaveItemHelper.authorizedSave(item,user,false,q,override,allowItemRemoval,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));
    	
		return XMLWriter.ItemToDOM(item,true,false);
	}
	
	/**
	 * Accesses the supplied XML File, and saves the included item and all of its children (refs)
	 * to the database, and returns the updated xml.
	 * @param location            The location of the file.
	 * @param user                The user.
	 * @param quarantine          Whether the data object should be quarantined.
	 * @param allowItemRemoval    Whether the item can be removed.
	 * @throws XFTInitException When an error occurs in XFT.
	 * @throws ElementNotFoundException When a specified element isn't found on the object.
	 */
	public static void StoreXMLFileToDB(String location, UserI user, Boolean quarantine, boolean allowItemRemoval) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,ValidationException,Exception
	{
		StoreXMLFileToDB(new File(location),user,quarantine,allowItemRemoval);
	}
	
	public static void StoreXMLFileToDB(File location, UserI user, Boolean quarantine,boolean allowItemRemoval) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,ValidationException,Exception
	{
	    boolean overrideSecurity = false;
	    if (user == null)
	    {
	        if(!location.getAbsolutePath().endsWith("security.xml"))
	        {
	            throw new Exception("Error: No username and password.");
	        }else{
	            overrideSecurity=true;
	        }
	    }
	    XMLValidator validator = new XMLValidator();
	    validator.validateSchema(location.getAbsolutePath());
	    
		//Document doc = XMLUtils.GetDOM(location);
		if (XFT.VERBOSE)
            System.out.println("Found Document:" + location);
		//XFTItem item = XMLReader.TranslateDomToItem(doc,user);
		SAXReader reader = new SAXReader(user);
		org.nrg.xft.XFTItem item = reader.parse(location);
		if (XFT.VERBOSE)
            System.out.println("Loaded XML Item:" + item.getXSIType());
		ValidationResults vr = XFTValidator.Validate(item);
		if (vr.isValid())
		{
		    if (XFT.VERBOSE)
                System.out.println("Initial Validation: PASSED");
			boolean q;
			boolean override;
			if (quarantine!=null)
			{
			    q = quarantine.booleanValue();
			    override = true;
			}else{
			    q = item.getGenericSchemaElement().isQuarantine();
			    override = false;
			}
			
						
        	SaveItemHelper.authorizedSave(item,user,overrideSecurity,q,override,allowItemRemoval,EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));

			//XFTItem temp = item.getCurrentDBVersion(true);
			//XMLWriter.StoreXFTItemToXMLFile(temp,location.getAbsolutePath()+".stored.xml");
			
			System.out.println("Item Successfully Stored.");
		}else
		{
			throw new ValidationException(vr);
		}
	}
	
	public static String GetSettingsLocation() throws XFTInitException
	{
		return XFTManager.GetInstance().getSourceDir();
	}
}

