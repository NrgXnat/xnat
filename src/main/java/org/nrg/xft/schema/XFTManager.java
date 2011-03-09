//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 18, 2004
 */
package org.nrg.xft.schema;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.XFTElementSorter;
import org.nrg.xft.db.DBConfig;
import org.nrg.xft.db.DBPool;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This singleton class manages the creation and manipulation of XFTDataModels.
 *
 * <BR><BR>This class is in charge of loading and maintaining all of the Schemas used
 * used in the application.  Upon a call to the init() method, the XFTManager reads
 * the configuration from the InstanceSettings.xml document.  Using these specifications,
 * it finds the Schema files and creates XFTDataModels accordingly.
 *
 * <BR><BR>The singleton format of the class mandates that only one instance of the
 * XFTManager will be running at any given time.  It is loaded up init() and exists
 * until the application process ends.  After initialization, the XFTManager instance
 * is available through the GetInstance() method.  From the instance, one can access
 * the source Directory (where the InstanceSettings.xml was located).  Otherwise, all
 * access is done through the static methods.
 *
 * <BR><BR>This class also maintains a static reference to the XFTElement table (if it exists).
 * This Element maintains a list of all elements in the application.  Extended elements
 * use this Reference to create an Extended Element Reference Field which defines the type of
 * element which extended it.
 *
 * @author Tim
 */
public class XFTManager {
    static org.apache.log4j.Logger logger = Logger.getLogger(XFTManager.class);
    private static XFTManager MANAGER = null;

    private static XFTElement ELEMENT_TABLE = null;
    private static Hashtable DATA_MODELS = new Hashtable();

    private static Hashtable ROOT_LEVEL_ELEMENTS= new Hashtable();

    //private String packageName = "";
    private String sourceDir = "";

    /**
     * Gets singleton instance of the Manager
     * @return
     * @throws XFTInitException
     */
    public static XFTManager GetInstance() throws XFTInitException
    {
        if (MANAGER == null)
        {
            throw new XFTInitException();
        }
        return MANAGER;
    }

    /**
     * Initializes the XFTManager (if it hasn't been already)
     * @param schemaLocation
     * @return
     * @throws ElementNotFoundException
     */
    public static XFTManager init(String schemaLocation) throws ElementNotFoundException
    {
        //XFT.LogCurrentTime("MANAGER INIT:1","ERROR");
        MANAGER = new XFTManager(schemaLocation);

        //XFT.LogCurrentTime("MANAGER INIT:2","ERROR");
        try {
            MANAGER.manageAddins();
        }catch (XFTInitException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        //XFT.LogCurrentTime("MANAGER INIT:3","ERROR");
        //FileUtils.OutputToFile(MANAGER.toString(),MANAGER.getSourceDir() +"xdat.xml");
        return MANAGER;
    }

    public static void clean()
    {
        MANAGER = null;
        ELEMENT_TABLE = null;
        DATA_MODELS = new Hashtable();
    }

    /**
     * returns the XFTElement which stores all of the element names.
     * @return
     */
    public static XFTElement GetElementTable()
    {
        return ELEMENT_TABLE;
    }

    /**
     * sets the XFTElement which stores all of the element names.
     * @param xe
     */
    public static void SetElementTable(XFTElement xe)
    {
        ELEMENT_TABLE = xe;
    }

    /**
     * Re-Initializes the XFTManager
     * @throws XFTInitException
     */
    public static void Refresh(String sourceDirectory) throws XFTInitException,ElementNotFoundException
    {
        MANAGER = null;
        init(sourceDirectory);
    }

    /**
     * Gets the XFTSchemas contained in the XFTDataModels collection
     * @return
     * @see XFTSchema
     */
    public static ArrayList GetSchemas()
    {
        ArrayList al = new ArrayList();
        XFTElement xe = null;
        Enumeration enumer = DATA_MODELS.keys();
        while(enumer.hasMoreElements())
        {
            al.add(((XFTDataModel)DATA_MODELS.get(enumer.nextElement())).getSchema());
        }
        return al;
    }
    /**
     * Gets the XFTDataModels
     * @return hash of XFTDataModels
     * @see XFTDataModel
     */
    public static Hashtable GetDataModels() {
        return DATA_MODELS;
    }
    /**
     * Gets the Root Elements
     * @return hash of String properName,String complexType
     */
    public static Hashtable GetRootElementsHash() {
        return ROOT_LEVEL_ELEMENTS;
    }
    /**
     * Add a Root Element
     */
    public static void AddRootElement(String name, String elementName) {
        ROOT_LEVEL_ELEMENTS.put(name,elementName);
    }
    /**
     * Source directory where the InstanceSettings.xml document can be found.
     * @return
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * Source directory where the InstanceSettings.xml document can be found.
     * @param string
     */
    public void setSourceDir(String string) {
        sourceDir = string;
    }

    /**
     * Access the InstanceSettings.xml document, and parses it to create a
     * collection of DB Connections in the DBPool and a collection of XFTDataModels (local).
     * @param source location where InstanceSettings.xml can be found.
     * @throws ElementNotFoundException
     */
    private XFTManager(String source) throws ElementNotFoundException
    {
        logger.debug("Java Version is: " + System.getProperty("java.version"));
        if (! source.endsWith(File.separator))
        {
            source = source + File.separator;
        }
        if (source.indexOf("WEB-INF")!=-1){
            sourceDir = source.substring(0,source.indexOf("WEB-INF"));
            System.out.println("SOURCE: " + sourceDir);
        }else{
            sourceDir = source;
        }
        File file = new File(source + "InstanceSettings.xml");
        Document doc = XMLUtils.GetDOM(file);
        Element root = doc.getDocumentElement();

        ViewManager.PRE_LOAD_HISTORY = NodeUtils.GetBooleanAttributeValue(root,"Pre_Load_History",false);

        String admin_email = (NodeUtils.GetAttributeValue(root,"admin_email",""));
        if (!admin_email.equals(""))
        {
            XFT.SetAdminEmail(admin_email);
        }
        String site_url = (NodeUtils.GetAttributeValue(root,"site_url",""));
        if (!site_url.equals(""))
        {
            XFT.SetSiteURL(site_url);
        }

        String archive_root_path = (NodeUtils.GetAttributeValue(root,"archive_root_path",""));
        if (!archive_root_path.equals(""))
        {
            XFT.SetArchiveRootPath(archive_root_path);
        }

        String prearchive_path = (NodeUtils.GetAttributeValue(root,"prearchive_path",""));
        if (!prearchive_path.equals(""))
        {
            XFT.SetPrearchivePath(prearchive_path);
        }

        String cache_path = (NodeUtils.GetAttributeValue(root,"cache_path",""));
        if (!cache_path.equals(""))
        {
            XFT.SetCachePath(cache_path);
        }

        String smtp_server = (NodeUtils.GetAttributeValue(root,"smtp_server",""));
        if (!smtp_server.equals(""))
        {
            XFT.SetAdminEmailHost(smtp_server);
        }



        String pipeline_path = (NodeUtils.GetAttributeValue(root,"pipeline_path",""));
        if (!pipeline_path.equals(""))
        {
            XFT.SetPipelinePath(pipeline_path);
        }

        String ftp_path = (NodeUtils.GetAttributeValue(root,"ftp_path",""));
        if (!ftp_path.equals(""))
        {
            XFT.setFtpPath(ftp_path);
        }

        String build_path = (NodeUtils.GetAttributeValue(root,"build_path",""));
        if (!build_path.equals(""))
        {
            XFT.setBuildPath(build_path);
        }

        String require_login = (NodeUtils.GetAttributeValue(root,"require_login",""));
        if (!require_login.equals(""))
        {
            XFT.SetRequireLogin(require_login);
        }

        String user_registration = (NodeUtils.GetAttributeValue(root,"user_registration",""));
        if (!user_registration.equals(""))
        {
            XFT.SetUserRegistration(user_registration);
        }

        if (root.hasChildNodes())
        {
            for (int i=0;i<root.getChildNodes().getLength();i++)
            {
                Node child1 = root.getChildNodes().item(i);
                if (child1.getNodeName().equalsIgnoreCase("Databases"))
               {
                   if (child1.hasChildNodes())
                   {
                       for (int j=0;j<child1.getChildNodes().getLength();j++)
                       {
                           Node child2 = child1.getChildNodes().item(j);
                           if (child2.getNodeName().equalsIgnoreCase("Database"))
                           {
                                DBConfig db = new DBConfig();
                                if (NodeUtils.HasAttribute(child2,"Type"))
                                {
                                    db.setType(NodeUtils.GetAttributeValue(child2,"Type",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"Id"))
                                {
                                    db.setDbIdentifier(NodeUtils.GetAttributeValue(child2,"Id",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"Url"))
                                {
                                    db.setUrl(NodeUtils.GetAttributeValue(child2,"Url",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"User"))
                                {
                                    db.setUser(NodeUtils.GetAttributeValue(child2,"User",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"Pass"))
                                {
                                    db.setPass(NodeUtils.GetAttributeValue(child2,"Pass",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"Driver"))
                                {
                                    db.setDriver(NodeUtils.GetAttributeValue(child2,"Driver",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"MaxConnections"))
                                {
                                    db.setMaxConnections(new Integer(NodeUtils.GetAttributeValue(child2,"MaxConnections","")).intValue());
                                }
                                DBPool.AddDBConfig(db);
                           }
                       }
                   }
               }else if (child1.getNodeName().equalsIgnoreCase("Package"))
                {
//					if (NodeUtils.HasAttribute(child1,"Name"))
//					{
//						this.setPackageName(NodeUtils.GetAttributeValue(child1,"Name",""));
//					}
                }else if (child1.getNodeName().equalsIgnoreCase("Models"))
                {
                    if (child1.hasChildNodes())
                    {
                        for (int j=0;j<child1.getChildNodes().getLength();j++)
                        {
                            Node child2 = child1.getChildNodes().item(j);
                            if (child2.getNodeName().equalsIgnoreCase("Data_Model"))
                            {
                                XFTDataModel model = new XFTDataModel();
                                if (NodeUtils.HasAttribute(child2,"File_Name"))
                                {
                                    model.setFileName(NodeUtils.GetAttributeValue(child2,"File_Name",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"File_Location"))
                                {
                                    String file_location = NodeUtils.GetAttributeValue(child2,"File_Location","");
                                    if (!FileUtils.IsAbsolutePath(file_location))
                                    {
                                        file_location = sourceDir + file_location;
                                    }
                                    model.setFileLocation(file_location);
                                }
                                if (NodeUtils.HasAttribute(child2,"DB"))
                                {
                                    model.setDb(NodeUtils.GetAttributeValue(child2,"DB",""));
                                }
                                if (NodeUtils.HasAttribute(child2,"package"))
                                {
                                    model.setDb(NodeUtils.GetAttributeValue(child2,"package",""));
                                }
                                try {
                                    model.setSchema();
                                } catch (XFTInitException e) {
                                    e.printStackTrace();
                                } catch (ElementNotFoundException e) {
                                    e.printStackTrace();
                                }
                                DATA_MODELS.put(model.getFileName(),model);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString()
    {
        Document doc = toXML();
        return XMLUtils.DOMToString(doc);
    }

    public Document toXML()
    {
        XMLWriter writer = new XMLWriter();
        Document doc =writer.getDocument();
        Node main = doc.createElement("xdat-manager");
        Iterator schemas =  GetSchemas().iterator();
        while (schemas.hasNext())
        {
            XFTSchema schema = (XFTSchema)schemas.next();
            main.appendChild(schema.toXML(doc));
        }
        doc.appendChild(main);
        return doc;
    }

    private void manageAddins() throws ElementNotFoundException,XFTInitException,Exception
    {
        //XFT.LogCurrentTime("MANAGER ADD_INS:1","ERROR");
        ArrayList histories = new ArrayList();
        Iterator addIns = getAddInElements().iterator();
        while (addIns.hasNext())
        {
            XFTElement addIn = (XFTElement)addIns.next();
            if (addIn.getAddin().equalsIgnoreCase("global"))
            {
                //Every data element should have a reference to this element
                Iterator schemas =  GetSchemas().iterator();
                while (schemas.hasNext())
                {
                    XFTSchema schema = (XFTSchema)schemas.next();
                    Iterator elements = schema.getSortedElements().iterator();
                    while (elements.hasNext())
                    {
                        XFTElement element = (XFTElement)elements.next();
                        if (element.getAddin() == null || element.getAddin().equalsIgnoreCase(""))
                        {
                            XFTElement clone = addIn.clone(element);
                            clone.setExtensionType(null);
                            clone.setExtension(false);
                            clone.setAddin("meta");
                            clone.setSkipSQL(element.isSkipSQL());

                            XFTReferenceField field = XFTReferenceField.GetEmptyRef();
                            field.setName("meta");
                            field.setXMLType(clone.getType());
                            field.setFullName("meta");
                            field.setMinOccurs("0");
                            field.setExpose("false");
                            field.getRelation().setOnDelete("SET NULL");
                            field.setParent(element);
                            field.setSequence(1001);
                            String parentType= element.getType().getLocalType();
                            if (element.getSqlElement()!=null && element.getSqlElement().getName()!=null){
                                parentType=element.getSqlElement().getName();
                            }
                            field.getSqlField().setSqlName(parentType +"_info");
                            element.addField(field);


                            schema.addElement(clone);
                        }
                    }
                }
            }else if (addIn.getAddin().equalsIgnoreCase("local"))
            {
//				Every data element should have an additional table of this type
                Iterator schemas =  GetSchemas().iterator();
                while (schemas.hasNext())
                {
                    XFTSchema schema = (XFTSchema)schemas.next();
                    Iterator elements = schema.getSortedElements().iterator();
                    while (elements.hasNext())
                    {
                        XFTElement element = (XFTElement)elements.next();
                        if (element.getAddin() == null || element.getAddin().equalsIgnoreCase(""))
                        {
                            XFTElement clone = addIn.clone(element);
                            schema.addElement(clone);
                        }
                    }
                }
            }else if (addIn.getAddin().equalsIgnoreCase("history"))
            {
                histories.add(addIn);
            }else if (addIn.getAddin().equalsIgnoreCase("extension"))
            {

            }
        }

        //XFT.LogCurrentTime("MANAGER ADD_INS:2","ERROR");
        XFTReferenceManager.init();
        //XFT.LogCurrentTime("MANAGER ADD_INS:3","ERROR");

        Iterator hist = histories.iterator();
        while (hist.hasNext())
        {
            XFTElement addIn = (XFTElement)hist.next();
//			Every data element should have an additional table of this type with its rows included
            Iterator schemas =  GetSchemas().iterator();
            while (schemas.hasNext())
            {
                XFTSchema schema = (XFTSchema)schemas.next();
                Iterator elements = schema.getSortedElements().iterator();
                while (elements.hasNext())
                {
                    XFTElement element = (XFTElement)elements.next();
                    GenericWrapperElement wrapE = (GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(element);

                    if (element.getAddin() == null || element.getAddin().equalsIgnoreCase(""))
                    {
                        XFTElement clone = addIn.clone(element);
                        clone.setAddin("history");
                        clone.setExtension(false);
                        clone.setSkipSQL(element.isSkipSQL());

                        Iterator iter = wrapE.getAllFieldsWAddIns(false,false).iterator();
                        while (iter.hasNext())
                        {
                            GenericWrapperField field = (GenericWrapperField)iter.next();
                            if (field.isReference())
                            {
                                XFTReferenceI ref = field.getXFTReference();
                                try {
                                    if (! ref.isManyToMany())
                                    {
                                        Iterator specs = ((XFTSuperiorReference)ref).getKeyRelations().iterator();
                                        while (specs.hasNext())
                                        {
                                            XFTRelationSpecification spec = (XFTRelationSpecification)specs.next();
                                            XFTField cloneField = XFTDataField.GetEmptyField();
                                            cloneField.setMinOccurs("0");
                                            cloneField.setRequired("");
                                            cloneField.setParent(clone);
                                            cloneField.setUnique("false");
                                            cloneField.setUniqueComposite("false");
                                            cloneField.setName(spec.getLocalCol());
                                            cloneField.setFullName(spec.getLocalCol());
                                            cloneField.setXMLType(spec.getSchemaType());
                                            clone.addField(cloneField);
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    throw new RuntimeException("Error managing XDAT add-ins for element(" + wrapE.getFullXMLName() + ") field(" + field.getXMLPathString("") + ")");
                                }
                            }else
                            {
                                if (GenericWrapperField.IsLeafNode(field.getWrapped()))
                                {
                                    XFTField cloneField = XFTDataField.GetEmptyField();
                                    cloneField.setMinOccurs("0");
                                    cloneField.setRequired("");
                                    cloneField.setParent(clone);
                                    cloneField.setUnique("false");
                                    cloneField.setUniqueComposite("false");
                                    cloneField.setSize(field.getSize());
                                    cloneField.setName(field.getSQLName());
                                    cloneField.setFullName(field.getSQLName());
                                    cloneField.getSqlField().setSqlName(field.getSQLName());
                                    cloneField.setXMLType(field.getXMLType());
                                    clone.addField(cloneField);
                                }
                            }
                        }


//						Iterator fields = wrapE.getAllFields(false,false).iterator();
//						while (fields.hasNext())
//						{
//							GenericWrapperField gwf = (GenericWrapperField)fields.next();
//							XFTField field = gwf.getWrapped();
//							XFTField cloneField = field.clone(element,false);
//							cloneField.getSqlField().setPrimaryKey("");
//							cloneField.getSqlField().setAutoIncrement("");
//							cloneField.setMinOccurs("0");
//							cloneField.setRequired("");
//							cloneField.setParent(clone);
//							cloneField.setUnique("false");
//							cloneField.setUniqueComposite("false");
//							clone.addField(cloneField);
//						}
//
//						if (wrapE.getStatedPrimaryKeyFields().size() == 0)
//						{
//							GenericWrapperField gwf = (GenericWrapperField)wrapE.getDefaultKey();
//							XFTField field = gwf.getWrapped();
//							XFTField cloneField = field.clone(element,false);
//							cloneField.setMinOccurs("0");
//							cloneField.setRequired("");
//							cloneField.getSqlField().setPrimaryKey("");
//							cloneField.getSqlField().setAutoIncrement("");
//							cloneField.setParent(clone);
//							cloneField.setUnique("false");
//							cloneField.setUniqueComposite("false");
//							clone.addField(cloneField);
//						}
//
//						Iterator iter = wrapE.getHiddenSuperiorElements().iterator();
//						while (iter.hasNext())
//						{
//							GenericWrapperField refF = (GenericWrapperField)iter.next();
//							GenericWrapperElement ref = refF.getParentElement();
//
//							XFTReferenceField field = XFTReferenceField.GetEmptyRef();
//							field.setName(ref.getLocalXMLName());
//							field.setXMLType(ref.getType());
//							field.setFullName(ref.getLocalXMLName());
//							field.setMinOccurs("0");
//							field.setExpose("false");
//							field.getRelation().setOnDelete("SET NULL");
//							field.setParent(element);
//							field.setSequence(1001);
//							if (! refF.getXMLSqlNameValue().equalsIgnoreCase(""))
//							{
//								field.getSqlField().setSqlName(refF.getXMLSqlNameValue());
//							}else{
//								ArrayList primaryKeys =ref.getAllPrimaryKeys();
//								if (primaryKeys.size() == 1)
//								{
//									GenericWrapperField pk = (GenericWrapperField) primaryKeys.get(0);
//									field.getSqlField().setSqlName(pk.getSQLName());
//								}
//							}
//							clone.addField(field);
//						}

                        schema.addElement(clone);
                    }
                }
            }
        }
        //XFT.LogCurrentTime("MANAGER ADD_INS:4","ERROR");

        //XFTReferenceManager.clean();
    }

    private ArrayList getAddInElements()
    {
        ArrayList al = new ArrayList();
        Iterator schemas =  GetSchemas().iterator();
        while (schemas.hasNext())
        {
            XFTSchema schema = (XFTSchema)schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext())
            {
                XFTElement element = (XFTElement)elements.next();
                if (element.getAddin() != null && !element.getAddin().equalsIgnoreCase(""))
                {
                    al.add(element);
                }
            }
        }
        al.trimToSize();
        return al;
    }

    /**
     * ArrayList of GenericWrapperElements
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws Exception
     */
    public ArrayList getOrderedElements() throws XFTInitException,ElementNotFoundException,Exception
    {
        ArrayList al = new ArrayList();
        Iterator schemas =  GetSchemas().iterator();
        XFTElementSorter sorter = new XFTElementSorter();
        while (schemas.hasNext())
        {
            XFTSchema schema = (XFTSchema)schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext())
            {
                XFTElement element = (XFTElement)elements.next();
                sorter.addElement((GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(element));
            }
        }

        al.addAll(sorter.getElements());

        al.trimToSize();
        return al;
    }

    /**
     * ArrayList of GenericWrapperElements
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     * @throws Exception
     */
    public ArrayList getAllElements() throws XFTInitException
    {
        ArrayList al = new ArrayList();
        Iterator schemas =  GetSchemas().iterator();
        while (schemas.hasNext())
        {
            XFTSchema schema = (XFTSchema)schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext())
            {
                XFTElement element = (XFTElement)elements.next();
                al.add(GenericWrapperFactory.GetInstance().wrapElement(element));
            }
        }

        al.trimToSize();
        return al;
    }
}

