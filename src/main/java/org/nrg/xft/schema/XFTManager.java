/*
 * org.nrg.xft.schema.XFTManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 8/28/13 3:19 PM
 */


package org.nrg.xft.schema;
import com.google.common.collect.Lists;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.XFTElementSorter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class XFTManager {
    private static final Logger logger  = LoggerFactory.getLogger(XFTManager.class);
    private static XFTManager MANAGER = null;

    private static XFTElement ELEMENT_TABLE = null;
    private static Hashtable DATA_MODELS = new Hashtable();

    private static Hashtable ROOT_LEVEL_ELEMENTS= new Hashtable();

    //private String packageName = "";
    private String sourceDir = "";

    /**
     * Gets singleton instance of the Manager
     * @return The manager instance.
     * @throws XFTInitException         When an error occurs in XFT.
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
     * @param schemaLocation    The schema location.
     * @return The manager instance.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public static XFTManager init(String schemaLocation) throws ElementNotFoundException
    {
        //XFT.LogCurrentTime("MANAGER INIT:1","ERROR");
        MANAGER = new XFTManager(schemaLocation);

        //XFT.LogCurrentTime("MANAGER INIT:2","ERROR");
        try {
            MANAGER.manageAddins();
        } catch (Exception e) {
            logger.error("An error occurred initializing XFT", e);
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
     * @return The XFT überelement.
     */
    public static XFTElement GetElementTable()
    {
        return ELEMENT_TABLE;
    }

    /**
     * sets the XFTElement which stores all of the element names.
     * @param xe    The XFT überelement to set.
     */
    public static void SetElementTable(XFTElement xe)
    {
        ELEMENT_TABLE = xe;
    }

    /**
     * Re-Initializes the XFTManager
     * @throws XFTInitException
     */
    @SuppressWarnings("unused")
    public static void Refresh(String sourceDirectory) throws XFTInitException, ElementNotFoundException
    {
        MANAGER = null;
        init(sourceDirectory);
    }

    /**
     * Gets the XFTSchemas contained in the XFTDataModels collection
     * @return The available schema objects.
     * @see XFTSchema
     */
    public static ArrayList GetSchemas()
    {
        ArrayList al = new ArrayList();
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
     * @return The source directory.
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * Source directory where the InstanceSettings.xml document can be found.
     * @param directory    The source directory to set.
     */
    @SuppressWarnings("unused")
    public void setSourceDir(String directory) {
        sourceDir = directory;
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
        if (source.contains("WEB-INF")){
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

        String lastDB=null;

        if (root.hasChildNodes())
        {
            for (int i=0;i<root.getChildNodes().getLength();i++)
            {
                Node child1 = root.getChildNodes().item(i);
                if (child1.getNodeName().equalsIgnoreCase("Models"))
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
                                	lastDB=NodeUtils.GetAttributeValue(child2,"DB","");
                                    model.setDb(lastDB);
                                }
                                if (NodeUtils.HasAttribute(child2,"package"))
                                {
                                    model.setDb(NodeUtils.GetAttributeValue(child2,"package",""));
                                }
                                try {
                                    model.setSchema();
                                } catch (XFTInitException | ElementNotFoundException e) {
                                    logger.error("An error occurred", e);
                                }
                                DATA_MODELS.put(model.getFileName(),model);
                            }
                        }
                    }
                }
            }
        }
        
        try {
			//retrieve schema from jars
			List<XFTDataModel> models=discoverSchema();
			for(XFTDataModel model:models){
				model.setDb(lastDB);
				DATA_MODELS.put(model.getFileName(), model);
			}
		} catch (XFTInitException e) {
			e.printStackTrace();
			logger.error("",e);
		}
    }

    private List<XFTDataModel> discoverSchema() throws XFTInitException, ElementNotFoundException {
		List<XFTDataModel> models=Lists.newArrayList();
		  	
		List<DataModelDefinition> defs=discoverDataModelDefs();
		for(DataModelDefinition annotation: defs){
            InputStream in=this.getClass().getClassLoader().getResourceAsStream(annotation.getSchemaPath());
            
            if(in!=null){
                XFTDataModel model=new XFTDataModel();
				model.setFileLocation(annotation.getSchemaPath());
				model.setFileName((annotation.getSchemaPath().contains("/"))?annotation.getSchemaPath().substring(annotation.getSchemaPath().lastIndexOf("/")):annotation.getSchemaPath());
				model.setSchema(new XFTSchema(XMLUtils.GetDOM(in),annotation.getSchemaPath(),model));
				models.add(model);
            }
        }
		
		return models;
	}
    
    public static List<DataModelDefinition> discoverDataModelDefs(){
    	List<DataModelDefinition> defs=Lists.newArrayList();
    	//look for defined schema extensions
        List<Class<?>> classes;
        try {
            classes = Reflection.getClassesForPackage("org.nrg.xft.schema.extensions");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        
        for (Class<?> clazz : classes) {
            if (DataModelDefinition.class.isAssignableFrom(clazz)) {//must be a data model definition
            	try {
					DataModelDefinition annotation = (DataModelDefinition)clazz.newInstance();
					String schemaPath=annotation.getSchemaPath();
					InputStream in=clazz.getClassLoader().getResourceAsStream(schemaPath);
					
					if(in!=null){
					    defs.add(annotation);
					}
				} catch (InstantiationException | IllegalAccessException e) {
					logger.error("",e);
				}
            }
        }
        return defs;
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
        for (final Object o : GetSchemas()) {
            XFTSchema schema = (XFTSchema) o;
            main.appendChild(schema.toXML(doc));
        }
        doc.appendChild(main);
        return doc;
    }

    private void manageAddins() throws Exception
    {
        //XFT.LogCurrentTime("MANAGER ADD_INS:1","ERROR");

        ArrayList histories = new ArrayList();
        for (final Object o : getAddInElements()) {
            XFTElement addIn = (XFTElement) o;
            if (addIn.getAddin().equalsIgnoreCase("global")) {
                //Every data element should have a reference to this element
                for (final Object o1 : GetSchemas()) {
                    XFTSchema schema   = (XFTSchema) o1;
                    for (final Object o2 : schema.getSortedElements()) {
                        XFTElement element = (XFTElement) o2;
                        if (element.getAddin() == null || element.getAddin().equalsIgnoreCase("")) {
                            XFTElement clone = addIn.clone(element, false);
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
                            String parentType = element.getType().getLocalType();
                            if (element.getSqlElement() != null && element.getSqlElement().getName() != null) {
                                parentType = element.getSqlElement().getName();
                            }
                            field.getSqlField().setSqlName(parentType + "_info");
                            element.addField(field);


                            schema.addElement(clone);
                        }
                    }
                }
            } else if (addIn.getAddin().equalsIgnoreCase("local")) {
//				Every data element should have an additional table of this type
                for (final Object o1 : GetSchemas()) {
                    XFTSchema schema   = (XFTSchema) o1;
                    for (final Object o2 : schema.getSortedElements()) {
                        XFTElement element = (XFTElement) o2;
                        if (element.getAddin() == null || element.getAddin().equalsIgnoreCase("")) {
                            XFTElement clone = addIn.clone(element, false);
                            schema.addElement(clone);
                        }
                    }
                }
            } else if (addIn.getAddin().equalsIgnoreCase("history")) {
                histories.add(addIn);
            } else if (addIn.getAddin().equalsIgnoreCase("extension")) {

            }
        }

        //XFT.LogCurrentTime("MANAGER ADD_INS:2","ERROR");
        XFTReferenceManager.init();
        //XFT.LogCurrentTime("MANAGER ADD_INS:3","ERROR");

        for (final Object history : histories) {
            XFTElement addIn = (XFTElement) history;
//			Every data element should have an additional table of this type with its rows included
            Iterator schemas = GetSchemas().iterator();
            while (schemas.hasNext()) {
                XFTSchema schema   = (XFTSchema) schemas.next();
                Iterator  elements = schema.getSortedElements().iterator();
                while (elements.hasNext()) {
                    XFTElement            element = (XFTElement) elements.next();
                    GenericWrapperElement wrapE   = (GenericWrapperElement) GenericWrapperFactory.GetInstance().wrapElement(element);

                    if (element.getAddin() == null || element.getAddin().equalsIgnoreCase("")) {
                        XFTElement clone = addIn.clone(element, true);
                        clone.setExtension(false);

                        clone.setSkipSQL(element.isSkipSQL());

                        final List<GenericWrapperField> fields = (List<GenericWrapperField>) wrapE.getAllFieldsWAddIns(false, false);
                        for (GenericWrapperField field : fields) {
                            if (field.isReference()) {
                                XFTReferenceI ref = field.getXFTReference();
                                try {
                                    if (!ref.isManyToMany()) {
                                        Iterator specs = ((XFTSuperiorReference) ref).getKeyRelations().iterator();
                                        while (specs.hasNext()) {
                                            XFTRelationSpecification spec       = (XFTRelationSpecification) specs.next();
                                            XFTField                 cloneField = XFTDataField.GetEmptyField();
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
                                    } else {
                                        System.out.println();
                                    }
                                } catch (RuntimeException e) {
                                    throw new RuntimeException("Error managing XDAT add-ins for element(" + wrapE.getFullXMLName() + ") field(" + field.getXMLPathString("") + ")");
                                }
                            } else {
                                if (GenericWrapperField.IsLeafNode(field.getWrapped())) {
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


                        XFTField cloneField = XFTDataField.GetEmptyField();
                        cloneField.setMinOccurs("0");
                        cloneField.setRequired("");
                        cloneField.setParent(clone);
                        cloneField.setUnique("false");
                        cloneField.setUniqueComposite("false");
                        cloneField.setSize("");
                        cloneField.setName("change_id");
                        cloneField.setFullName("xft_version");
                        cloneField.getSqlField().setSqlName("xft_version");
                        cloneField.setXMLType(new XMLType("xs:integer", schema));
                        clone.addField(cloneField);

                        schema.addElement(clone);
                    }
                }
            }
        }
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
     * @return The ordered elements.
     * @throws Exception When something goes wrong.
     */
    public ArrayList getOrderedElements() throws Exception {
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
     * @return All elements.
     * @throws XFTInitException         When an error occurs in XFT.
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

