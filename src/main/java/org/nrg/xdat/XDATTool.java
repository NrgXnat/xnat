/*
 * org.nrg.xdat.XDATTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:36 AM
 */


package org.nrg.xdat;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.presentation.HTMLPresenter;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.*;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.ValidationException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.SQLCreateGenerator;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xft.utils.XMLValidator;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * @author Tim
 *
 */
public class XDATTool {
	private static final Logger logger = LoggerFactory.getLogger(XDATTool.class);

    private UserI user = null;
    private boolean ignoreSecurity = false;

    public XDATTool() throws XFTInitException
    {
        location = XFTManager.GetInstance().getSourceDir();
    }

    /**
     *
     */
    public XDATTool(String instanceLocation) throws Exception {
        location = FileUtils.AppendSlash(instanceLocation);
        XDAT.init(location,false);
    }
    /**
     *
     */
    public XDATTool(String instanceLocation, UserI u) throws Exception {
        user=u;
        location= FileUtils.AppendSlash(instanceLocation);
        XDAT.init(location,false);
    }

	public XDATTool(String instanceLocation,String username, String password) throws Exception
    {
        location = new File(instanceLocation).toURI().toString();
        XDAT.init(location,true);
        login(username,password);
    }

	// MIGRATE: This is just a stand-in to see if this code is even necessary any more.
    private static String location = "SOME KIND OF BOGUS LOCATION I DONT THINK THIS IS EVEN CALLED";

    private void login(String username, String password) throws Exception
    {
        try {
            user = Authenticator.Authenticate(new Authenticator.Credentials(username,password));
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            logger.error("",e);
        }
    }

    /**
	 * Generate CREATE, ALTER, VIEW, and INSERT statements for each element in the
	 * defined schemas.
	 */
	public void generateSQL() throws Exception
	{
		SQLCreateGenerator.generateDoc(Paths.get(getWorkDirectory(), "xdat.sql").toString());
	}

    /**
	 * Generate CREATE, ALTER, VIEW, and INSERT statements for each element in the
	 * defined schemas.
	 */
	public void generateSQL(String s) throws Exception
	{
	    XDAT.GenerateCreateSQL(s);
	}

	public void storeXML(String fileLocation,Boolean quarantine, boolean allowItemRemoval) throws Exception
	{
	    storeXML(new File(fileLocation),quarantine,allowItemRemoval);
	}


    /**
     * @return Returns the ignoreSecurity.
     */
    public boolean isIgnoreSecurity() {
        return ignoreSecurity;
    }
    /**
     * @param ignoreSecurity The ignoreSecurity to set.
     */
    public void setIgnoreSecurity(boolean ignoreSecurity) {
        this.ignoreSecurity = ignoreSecurity;
    }

	public void storeXML(File fileLocation,Boolean quarantine, boolean allowItemRemoval) throws Exception
	{
        if (user == null && (!ignoreSecurity))
	    {
	        if(!fileLocation.getAbsolutePath().endsWith("security.xml"))
	        {
	            throw new Exception("Error: No username and password.");
	        }
	    }
	    if (! fileLocation.exists())
	    {
	        throw new Exception("File Not Found: " + fileLocation.getPath());
	    }

	    //Document doc = XMLUtils.GetDOM(fileLocation);
	    if (XFT.VERBOSE)
	        System.out.println("Found Document:" + fileLocation);
	    logger.info("Found Document:" + fileLocation);

	    XMLValidator validator = new XMLValidator();
	    validator.validateSchema(fileLocation.getAbsolutePath());

		//XFTItem item = XMLReader.TranslateDomToItem(doc,this.user);
	   SAXReader reader = new SAXReader(user);
	   XFTItem item = reader.parse(fileLocation);
		if (XFT.VERBOSE)
	        System.out.println("Loaded XML Item:" + item.getProperName());
	    logger.info("Loaded XML Item:" + item.getProperName());

		ValidationResults vr = XFTValidator.Validate(item);
		if (vr.isValid())
		{
		    if (XFT.VERBOSE)
		        System.out.println("Validation: PASSED");
		    logger.info("Validation: PASSED");

			boolean q;
			boolean override;
			if (quarantine!=null)
			{
			    q = quarantine;
			    override = true;
			}else{
			    q = item.getGenericSchemaElement().isQuarantine();
			    override = false;
			}
        	
			PersistentWorkflowI wrk=null;
			EventMetaI ci=null;
			try {
				if(item.getItem().instanceOf("xnat:experimentData") || item.getItem().instanceOf("xnat:subjectData")){
					wrk=PersistentWorkflowUtils.buildOpenWorkflow(user,item.getItem(),EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));
				}else{
					wrk=PersistentWorkflowUtils.buildAdminWorkflow(user,item.getXSIType(),item.getPKValueString(),EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.STORE_XML, "Stored XML", EventUtils.MODIFY_VIA_STORE_XML, null));
				}

				assert wrk != null;
				ci = wrk.buildEvent();
			} catch (Throwable e) {
				// THIS IS SO UGLY...  But it's a chicken and egg problem
				// if a StoreXML is called outside of the full xnat context (i.e. command line), XNAT may not have access to the WrkWorkflowdata, if those classes aren't present
				// in general we should try to avoid this problem, but until the generated classes are properly jar'd, it's a challenge.
				// for now we'll allow this to proceed with out the valid audit event (which uses WrkWorkflowdata)
				// this means that StoreXML events which are called from the command line and do not go through the webapp, will not have registered events describing the change (though the changes themselves are still audited).
				// I'm doing this here, rather then deeper in the code, because other save attempts should require audit events.  It's only because this one is for command line calls that we'll ignore the exceptions.
				// We should either move the audit event persistence to use hibernate, or jar up the generated classes from xft so that they can be easily added to the classpath for the storeXML.
			}
            
            SaveItemHelper.unauthorizedSave(item, user, false,q,override,allowItemRemoval,ci);
            
            if(wrk!=null)
            	PersistentWorkflowUtils.complete(wrk,ci);
			if(XFT.VERBOSE)System.out.println("Item Successfully Stored.");
		    logger.info("Item Successfully Stored.");
		}else
		{
			throw new ValidationException(vr);
		}
	}

	public void XMLSearch(String elementName, boolean isBackup, String dir,boolean limited,boolean pp) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }

	    SchemaElement se = SchemaElement.GetElement(elementName);

	    String query = "SELECT ";
	    ArrayList pks = se.getAllPrimaryKeys();

        String proper =  XFTReferenceManager.GetProperName(se.getFullXMLName());
		if (proper == null || proper.equalsIgnoreCase(""))
		{
			proper = se.getSQLName();
		}

		for (final Object pk1 : pks) {
			SchemaField sf = (SchemaField) pk1;
			query += sf.getSQLName();
		}

	    query += " FROM " + se.getSQLName() + ";";

	    String login="";
	    if(user != null)
	    {
	        login = user.getLogin();
	    }

	    XFTTable table = TableSearch.Execute(query,se.getDbName(),login);

	    table.resetRowCursor();
	    while (table.hasMoreRows())
	    {
	        Hashtable row = table.nextRowHash();

	        ItemSearch search = ItemSearch.GetItemSearch(elementName,user);

	        String fileName = proper;

			for (final Object pk1 : pks) {
				SchemaField sf = (SchemaField) pk1;
				Object      pk = row.get(sf.getSQLName().toLowerCase());
				search.addCriteria(sf.getGenericXFTField().getXMLPathString(se.getFullXMLName()), pk);

				fileName += "_" + pk;
			}

			    fileName += ".xml";


		    File f = new File(dir + fileName);
		    if ((!f.exists()) || (!isBackup))
		    {
			    ItemCollection items= search.exec();
			    XMLWriter.StoreXFTItemListToXMLFile(items.items(),dir,limited,pp);
		    }else{
		        if (XFT.VERBOSE)
			        System.out.println(f.getAbsolutePath() + " already exists.");
		    }
	    }
	}

	public void XMLSearch(String elementName,String xmlPath, Object value, String dir, boolean limited,boolean pp) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }
	    ItemCollection items= ItemSearch.GetItems(xmlPath,value,user,false);
	    XMLWriter.StoreXFTItemListToXMLFile(items.items(),dir,limited,pp);
	}

	public int XMLSearch(String elementName,String xmlPath, String comparisonType, Object value, String dir,boolean limited,boolean pp) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }

	    xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
	    ItemSearch search = ItemSearch.GetItemSearch(elementName,user);
	    search.setAllowMultiples(false);
	    search.addCriteria(xmlPath,value,comparisonType);

	    ItemCollection items= search.exec(false);

	    if (items.size()>0)
	    {
		    return XMLWriter.StoreXFTItemListToXMLFile(items.items(),dir,limited,pp);
	    }else{
	    	if(XFT.VERBOSE) System.out.println("No Matches Found.");
	        return 1;
	    }
	}

	public void HTMLSearch(String xmlPath, String comparisonType, Object value) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }
	    String rootElement = XftStringUtils.GetRootElementName(xmlPath);
	    DisplaySearch ds = UserHelper.getSearchHelperService().getSearchForUser(user,rootElement,"listing");
	    ds.addCriteria(xmlPath,comparisonType,value);
	    XFTTableI table =ds.execute(new HTMLPresenter(""),user.getLogin());
	    FileUtils.OutputToFile(table.toHTML(true,"FFFFFF","FFFFCC",new java.util.Hashtable(),0),getSearchFileName("html"));
	}

	public int VelocitySearch(String elementName,String xmlPath, String comparisonType, Object value) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }

	    xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
	    ItemSearch search = ItemSearch.GetItemSearch(elementName,user);
	    search.setAllowMultiples(false);
	    search.addCriteria(xmlPath,value,comparisonType);

	    ItemCollection items= search.exec(false);

	    if (items.size()>0)
	    {
		    Iterator iter = items.getItemIterator();
		    if(XFT.VERBOSE)System.out.println(items.size()+ " Matching Items Found.\n\n");
		    while (iter.hasNext())
		    {
		        XFTItem item =(XFTItem)iter.next();
		        ItemI bo = BaseElement.GetGeneratedItem(item);
		        System.out.println(bo.output());

		        System.out.println("\n");
		    }
		    return 0;
	    }else{
	    	if(XFT.VERBOSE)System.out.println("No Matches Found.");
	        return 1;
	    }
	}

	public void CSVSearch(String xmlPath, String comparisonType, Object value) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }
	    String rootElement = XftStringUtils.GetRootElementName(xmlPath);
	    DisplaySearch ds = UserHelper.getSearchHelperService().getSearchForUser(user, rootElement, "listing");
	    ds.addCriteria(xmlPath,comparisonType,value);
	    XFTTableI table =ds.execute(new CSVPresenter(),user.getLogin());
	    FileUtils.OutputToFile(table.toString(","),getSearchFileName("csv"));
	}

	public void HTMLSearch(String elementName) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }
	    DisplaySearch ds = UserHelper.getSearchHelperService().getSearchForUser(user, elementName, "listing");
	    XFTTableI table =ds.execute(new HTMLPresenter(""),user.getLogin());
	    FileUtils.OutputToFile(table.toHTML(true,"FFFFFF","FFFFCC",new java.util.Hashtable(),0),getSearchFileName("html"));
	}

	public void CSVSearch(String elementName) throws Exception
	{
	    if (user == null && (!ignoreSecurity))
	    {
	        throw new Exception("Error: No username and password.");
	    }
	    DisplaySearch ds = UserHelper.getSearchHelperService().getSearchForUser(user, elementName, "listing");
	    XFTTableI table =ds.execute(new CSVPresenter(),user.getLogin());
	    FileUtils.OutputToFile(table.toString(","),getSearchFileName("csv"));
	}

	public String getWorkDirectory()
	{
		final Path directory = Paths.get(location, "work");
		final File f         = directory.toFile();
		if (!f.exists()) {
			f.mkdir();
		}
		return directory.toString();
	}

	public String getSearchFileName(String extension)
	{
	    int counter =0;

	    File f = Paths.get(getWorkDirectory(), "search" + counter + "." + extension).toFile();

	    while (f.exists())
	    {
	        f = Paths.get(getWorkDirectory(), "search" + (++counter) + "." + extension).toFile();
	    }
	    return f.getAbsolutePath();
	}

	/**
     * @return Returns the user.
     */
    public UserI getUser() {
        return user;
    }
    /**
     * @param user The user to set.
     */
    public void setUser(UserI user) {
        this.user = user;
    }

    public void info(String message)
    {
        logger.info(message);
    }

    public void debug(String message)
    {
        logger.debug(message);
    }

    public void error(String message)
    {
        logger.error(message);
    }

    public void info(Exception e)
    {
        logger.info("", e);
    }

    public void debug(Exception e)
    {
        logger.debug("", e);
    }

    public void error(Exception e)
    {
        logger.error("", e);
    }
}
