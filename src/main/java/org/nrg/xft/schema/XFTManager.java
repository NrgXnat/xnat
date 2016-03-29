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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XFTManager {
    private static final Logger     logger  = LoggerFactory.getLogger(XFTManager.class);
    private static       XFTManager MANAGER = null;

    private static XFTElement                      ELEMENT_TABLE = null;
    private static final Hashtable<String, XFTDataModel> DATA_MODELS   = new Hashtable<>();

    private static Hashtable ROOT_LEVEL_ELEMENTS = new Hashtable();

    //private String packageName = "";
    private String sourceDir = "";

    /**
     * Gets singleton instance of the Manager
     *
     * @return The manager instance.
     *
     * @throws XFTInitException When an error occurs in XFT.
     */
    public static XFTManager GetInstance() throws XFTInitException {
        if (MANAGER == null) {
            throw new XFTInitException();
        }
        return MANAGER;
    }

    /**
     * Initializes the XFTManager (if it hasn't been already)
     *
     * @param schemaLocation The schema location.
     *
     * @return The manager instance.
     *
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public static XFTManager init(String schemaLocation) throws ElementNotFoundException {
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

    public static void clean() {
        MANAGER = null;
        ELEMENT_TABLE = null;
        DATA_MODELS.clear();
    }

    /**
     * returns the XFTElement which stores all of the element names.
     *
     * @return The XFT ￼berelement.
     */
    public static XFTElement GetElementTable() {
        return ELEMENT_TABLE;
    }

    /**
     * sets the XFTElement which stores all of the element names.
     *
     * @param xe The XFT ￼berelement to set.
     */
    public static void SetElementTable(XFTElement xe) {
        ELEMENT_TABLE = xe;
    }

    /**
     * Re-Initializes the XFTManager
     *
     * @throws XFTInitException
     */
    @SuppressWarnings("unused")
    public static void Refresh(String sourceDirectory) throws XFTInitException, ElementNotFoundException {
        MANAGER = null;
        init(sourceDirectory);
    }

    /**
     * Gets the XFTSchemas contained in the XFTDataModels collection
     *
     * @return The available schema objects.
     *
     * @see XFTSchema
     */
    public static ArrayList GetSchemas() {
        ArrayList al = new ArrayList();
        Enumeration enumer = DATA_MODELS.keys();
        while (enumer.hasMoreElements()) {
            al.add(((XFTDataModel) DATA_MODELS.get(enumer.nextElement())).getSchema());
        }
        return al;
    }

    /**
     * Gets the XFTDataModels
     *
     * @return hash of XFTDataModels
     *
     * @see XFTDataModel
     */
    public static Hashtable GetDataModels() {
        return DATA_MODELS;
    }

    /**
     * Gets the Root Elements
     *
     * @return hash of String properName,String complexType
     */
    public static Hashtable GetRootElementsHash() {
        return ROOT_LEVEL_ELEMENTS;
    }

    /**
     * Add a Root Element
     */
    public static void AddRootElement(String name, String elementName) {
        ROOT_LEVEL_ELEMENTS.put(name, elementName);
    }

    /**
     * Source directory where the InstanceSettings.xml document can be found.
     *
     * @return The source directory.
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * Source directory where the InstanceSettings.xml document can be found.
     *
     * @param directory The source directory to set.
     */
    @SuppressWarnings("unused")
    public void setSourceDir(String directory) {
        sourceDir = directory;
    }

    /**
     * Access the InstanceSettings.xml document, and parses it to create a
     * collection of DB Connections in the DBPool and a collection of XFTDataModels (local).
     *
     * @param source location where InstanceSettings.xml can be found.
     *
     * @throws ElementNotFoundException
     */
    private XFTManager(String source) throws ElementNotFoundException {
        logger.debug("Java Version is: " + System.getProperty("java.version"));
        if (!source.endsWith(File.separator)) {
            source = source + File.separator;
        }
        if (source.contains("WEB-INF")) {
            sourceDir = source.substring(0, source.indexOf("WEB-INF"));
            System.out.println("SOURCE: " + sourceDir);
        } else {
            sourceDir = source;
        }
        String lastDB = null;
        File file = new File(source + "InstanceSettings.xml");
        if (file.exists()) {
            Document doc = XMLUtils.GetDOM(file);
            Element root = doc.getDocumentElement();

            ViewManager.PRE_LOAD_HISTORY = NodeUtils.GetBooleanAttributeValue(root, "Pre_Load_History", false);

            String admin_email = (NodeUtils.GetAttributeValue(root, "admin_email", ""));
            if (!admin_email.equals("")) {
                XFT.SetAdminEmail(admin_email);
            }
            String site_url = (NodeUtils.GetAttributeValue(root, "site_url", ""));
            if (!site_url.equals("")) {
                XFT.SetSiteURL(site_url);
            }

            String archive_root_path = (NodeUtils.GetAttributeValue(root, "archive_root_path", ""));
            if (!archive_root_path.equals("")) {
                XFT.SetArchiveRootPath(archive_root_path);
            }

            String prearchive_path = (NodeUtils.GetAttributeValue(root, "prearchive_path", ""));
            if (!prearchive_path.equals("")) {
                XFT.SetPrearchivePath(prearchive_path);
            }

            String cache_path = (NodeUtils.GetAttributeValue(root, "cache_path", ""));
            if (!cache_path.equals("")) {
                XFT.SetCachePath(cache_path);
            }

            String smtp_server = (NodeUtils.GetAttributeValue(root, "smtp_server", ""));
            if (!smtp_server.equals("")) {
                XFT.SetAdminEmailHost(smtp_server);
            }


            String pipeline_path = (NodeUtils.GetAttributeValue(root, "pipeline_path", ""));
            if (!pipeline_path.equals("")) {
                XFT.SetPipelinePath(pipeline_path);
            }

            String ftp_path = (NodeUtils.GetAttributeValue(root, "ftp_path", ""));
            if (!ftp_path.equals("")) {
                XFT.setFtpPath(ftp_path);
            }

            String build_path = (NodeUtils.GetAttributeValue(root, "build_path", ""));
            if (!build_path.equals("")) {
                XFT.setBuildPath(build_path);
            }

            String require_login = (NodeUtils.GetAttributeValue(root, "require_login", ""));
            if (!require_login.equals("")) {
                XFT.SetRequireLogin(require_login);
            }

            String user_registration = (NodeUtils.GetAttributeValue(root, "user_registration", ""));
            if (!user_registration.equals("")) {
                XFT.SetUserRegistration(user_registration);
            }

            if (root.hasChildNodes()) {
                for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                    Node child1 = root.getChildNodes().item(i);
                    if (child1.getNodeName().equalsIgnoreCase("Models")) {
                        if (child1.hasChildNodes()) {
                            for (int j = 0; j < child1.getChildNodes().getLength(); j++) {
                                Node child2 = child1.getChildNodes().item(j);
                                if (child2.getNodeName().equalsIgnoreCase("Data_Model")) {
                                    XFTDataModel model = new XFTDataModel();
                                    if (NodeUtils.HasAttribute(child2, "File_Name")) {
                                        model.setFileName(NodeUtils.GetAttributeValue(child2, "File_Name", ""));
                                    }
                                    if (NodeUtils.HasAttribute(child2, "File_Location")) {
                                        String file_location = NodeUtils.GetAttributeValue(child2, "File_Location", "");
                                        if (!FileUtils.IsAbsolutePath(file_location)) {
                                            file_location = sourceDir + file_location;
                                        }
                                        model.setFileLocation(file_location);
                                    }
                                    if (NodeUtils.HasAttribute(child2, "DB")) {
                                        lastDB = NodeUtils.GetAttributeValue(child2, "DB", "");
                                        model.setDb(lastDB);
                                    }
                                    if (NodeUtils.HasAttribute(child2, "package")) {
                                        model.setDb(NodeUtils.GetAttributeValue(child2, "package", ""));
                                    }
                                    try {
                                        model.setSchema();
                                    } catch (XFTInitException | ElementNotFoundException e) {
                                        logger.error("An error occurred", e);
                                    }
                                    DATA_MODELS.put(model.getFileName(), model);
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("Missing InstanceSettings.xml, skipping InstanceSettings.xml initialization");
                logger.error("Missing InstanceSettings.xml, skipping InstanceSettings.xml initialization");
            }
        } else {
            System.out.println("Missing SOURCE parameter.  Unable to identify WEB-INF/conf location.");
            logger.error("Missing SOURCE parameter.  Unable to identify WEB-INF/conf location.");
        }

        try {
            List<String> schemaParsed = Lists.newArrayList(DATA_MODELS.keySet());
            List<SchemaWrapper> toLoad = discoverSchema(source);
            //iterate over the list of schema until they've all been loaded
            int registered = 1;
            while (toLoad.size() > 0 && registered > 0) {
                registered = 0;

                //iterate on copy of toLoad so we can remove schema as we register them
                List<SchemaWrapper> toLoadCopy = toLoad;
                toLoad = Lists.newArrayList();

                //look for schema that are ok to parse in this pass
                for (SchemaWrapper schema : toLoadCopy) {
                    try (final InputStream inputStream = schema.getStream()) {
                        if (inputStream != null && !schemaParsed.contains(schema.getName())) {
                            //check if dependent schema have been registered yet.
                            if (schema.getDependencies().size() == 0 || schemaParsed.containsAll(schema.getDependencies())) {
                                System.out.println("Importing schema: " + schema.toString());
                                XFTDataModel model = new XFTDataModel();
                                model.setFileLocation(schema.getLocation());
                                model.setFileName(schema.getName());
                                model.setSchema(new XFTSchema(XMLUtils.GetDOM(inputStream), schema.getLocation(), model));
                                model.setDb(lastDB);

                                DATA_MODELS.put(model.getFileName(), model);
                                schemaParsed.add(schema.getName());
                                registered++;
                            } else {
                                toLoad.add(schema);
                            }
                        } else {
                            //dead reference
                            registered++;
                        }
                    } catch (IOException e) {
                        logger.warn("An error occurred trying to close a stream", e);
                    }
                }
            }

            //if there are still some to be loaded AFTER all loadable ones have been loaded, then there is a dependency problem.
            if (toLoad.size() > 0) {
                throw new XFTInitException("Unable to startup due to missing or cyclical schema dependency! " + listToString(toLoad));
            }
        } catch (XFTInitException e)

        {
            e.printStackTrace();
            logger.error("", e);
        }
    }

    //TODO: Make this more generic so that you can dynamically ignore schema
    private final List<String> ignoredSchemaNames = Lists.newArrayList("xdat.xsd", "build.xsd", "display.xsd", "instance.xsd", "PlexiViewer.xsd");

    private List<SchemaWrapper> discoverSchema(String source) throws XFTInitException, ElementNotFoundException {
        List<String> schemaLoaded = Lists.newArrayList();

        List<SchemaWrapper> toLoad = Lists.newArrayList();

        if (source != null) {
            //load schema from file system
            final File schemasFolder = new File(source, "schemas");
            if (schemasFolder.exists()) {
                final File[] files = schemasFolder.listFiles();
                if (files != null) {
                    for (final File level1 : files) {
                        if (level1.isDirectory()) {
                            final File[] subfiles = level1.listFiles();
                            if (subfiles != null) {
                                for (final File level2 : subfiles) {
                                    if (!schemaLoaded.contains(level2.getName()) && !ignoredSchemaNames.contains(level2.getName())) {
                                        if (level2.getName().endsWith(".xsd")) {
                                            schemaLoaded.add(level2.getName());

                                            try {
                                                List<String> dependencies = getDependentSchema(level2.getName(), new FileInputStream(level2));
                                                SchemaWrapper schema = new SchemaWrapper("file", level2.getParentFile().getAbsolutePath(), level2.getName(), new FileInputStream(level2), dependencies);

                                                toLoad.add(schema);
                                            } catch (Exception e) {
                                                logger.error("", e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //retrieve schema from jars registered via DataModelDefinitions
        List<DataModelDefinition> defs = discoverDataModelDefs();
        for (DataModelDefinition annotation : defs) {
            String name = FilenameUtils.getName(annotation.getSchemaPath());
            if (!schemaLoaded.contains(name)) {
                schemaLoaded.add(name);

                try {
                    List<String> dependencies = getDependentSchema(name, this.getClass().getClassLoader().getResourceAsStream(annotation.getSchemaPath()));
                    SchemaWrapper schema = new SchemaWrapper("def", annotation.getSchemaPath(), name, this.getClass().getClassLoader().getResourceAsStream(annotation.getSchemaPath()), dependencies);

                    toLoad.add(schema);
                } catch (Exception e) {
                    logger.error("", e);
                }

            }
        }

        //retrieve schema from classpath through discovery
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources = resolver.getResources("classpath*:schemas/**/*.xsd");
            for (final Resource resource : resources) {
                final String name = resource.getFilename();
                if (!schemaLoaded.contains(resource.getFilename()) && !ignoredSchemaNames.contains(resource.getFilename())) {
                    schemaLoaded.add(name);

                    try {
                        List<String> dependencies = getDependentSchema(name, resource.getInputStream());
                        SchemaWrapper schema = new SchemaWrapper("cp", resource.getURL().getPath(), name, resource.getInputStream(), dependencies);

                        toLoad.add(schema);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        } catch (IOException e1) {
            logger.error("Unable to discover XSD's from classpath.", e1);
        }

        return toLoad;
    }

    private static String listToString(List<SchemaWrapper> toLoad) {
        StringBuilder sb = new StringBuilder();
        for (SchemaWrapper schema : toLoad) {
            sb.append("\n").append(schema.toString());
        }
        return sb.toString();
    }

    /**
     * Uses a SAX Parser to retrieve the names of any schema which this one is dependent on (imports)
     *
     * @param in Schema to parse
     *
     * @return A list of the discovered dependent schema.
     *
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    private static List<String> getDependentSchema(String filename, InputStream in) throws ParserConfigurationException, SAXException, IOException {
        try (final InputStream inputStream = in) {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            DependencyParser parser = new DependencyParser();

            spf.setNamespaceAware(true);

            SAXParser sp = spf.newSAXParser();
            sp.setProperty("http://xml.org/sax/properties/lexical-handler", parser);
            sp.parse(inputStream, parser);

            return parser.dependencies;
        }
    }

    private static class SchemaWrapper {
        private final String       location;
        private final String       name;
        private final String       src;
        private final List<String> dependencies;
        private final InputStream  in;

        public SchemaWrapper(String src, String location, String name, InputStream in, List<String> dependencies) {
            super();
            this.src = src;
            this.location = location;
            this.name = name;
            this.in = in;
            this.dependencies = dependencies;
        }


        public String getLocation() {
            return location;
        }

        public String getName() {
            return name;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public InputStream getStream() {
            return in;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(src).append("-").append(getName()).append(" (");
            for (String dependency : getDependencies()) {
                sb.append(" ").append(dependency);
            }
            sb.append(")");
            return sb.toString();
        }
    }

    private static class DependencyParser extends org.xml.sax.ext.DefaultHandler2 {
        public List<String> dependencies = Lists.newArrayList();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (StringUtils.equals("xs:import", qName)) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String local = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    if (StringUtils.isNotEmpty(value) && StringUtils.equals("schemaLocation", local)) {
                        dependencies.add(FilenameUtils.getName(value));
                    }
                }
            }
        }

    }

    public static List<DataModelDefinition> discoverDataModelDefs() {
        List<DataModelDefinition> defs = Lists.newArrayList();
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
                    DataModelDefinition annotation = (DataModelDefinition) clazz.newInstance();
                    String schemaPath = annotation.getSchemaPath();
                    InputStream in = clazz.getClassLoader().getResourceAsStream(schemaPath);

                    if (in != null) {
                        defs.add(annotation);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("", e);
                }
            }
        }
        return defs;
    }

    @Override
    public String toString() {
        Document doc = toXML();
        return XMLUtils.DOMToString(doc);
    }

    public Document toXML() {
        XMLWriter writer = new XMLWriter();
        Document doc = writer.getDocument();
        Node main = doc.createElement("xdat-manager");
        for (final Object o : GetSchemas()) {
            XFTSchema schema = (XFTSchema) o;
            main.appendChild(schema.toXML(doc));
        }
        doc.appendChild(main);
        return doc;
    }

    private void manageAddins() throws Exception {
        //XFT.LogCurrentTime("MANAGER ADD_INS:1","ERROR");

        ArrayList histories = new ArrayList();
        for (final Object o : getAddInElements()) {
            XFTElement addIn = (XFTElement) o;
            if (addIn.getAddin().equalsIgnoreCase("global")) {
                //Every data element should have a reference to this element
                for (final Object o1 : GetSchemas()) {
                    XFTSchema schema = (XFTSchema) o1;
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
                    XFTSchema schema = (XFTSchema) o1;
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
                XFTSchema schema = (XFTSchema) schemas.next();
                Iterator elements = schema.getSortedElements().iterator();
                while (elements.hasNext()) {
                    XFTElement element = (XFTElement) elements.next();
                    GenericWrapperElement wrapE = (GenericWrapperElement) GenericWrapperFactory.GetInstance().wrapElement(element);

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
                                            XFTRelationSpecification spec = (XFTRelationSpecification) specs.next();
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

    private ArrayList getAddInElements() {
        ArrayList al = new ArrayList();
        Iterator schemas = GetSchemas().iterator();
        while (schemas.hasNext()) {
            XFTSchema schema = (XFTSchema) schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext()) {
                XFTElement element = (XFTElement) elements.next();
                if (element.getAddin() != null && !element.getAddin().equalsIgnoreCase("")) {
                    al.add(element);
                }
            }
        }
        al.trimToSize();
        return al;
    }

    /**
     * ArrayList of GenericWrapperElements
     *
     * @return The ordered elements.
     *
     * @throws Exception When something goes wrong.
     */
    public ArrayList getOrderedElements() throws Exception {
        ArrayList al = new ArrayList();
        Iterator schemas = GetSchemas().iterator();
        XFTElementSorter sorter = new XFTElementSorter();
        while (schemas.hasNext()) {
            XFTSchema schema = (XFTSchema) schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext()) {
                XFTElement element = (XFTElement) elements.next();
                sorter.addElement((GenericWrapperElement) GenericWrapperFactory.GetInstance().wrapElement(element));
            }
        }

        al.addAll(sorter.getElements());

        al.trimToSize();
        return al;
    }

    /**
     * ArrayList of GenericWrapperElements
     *
     * @return All elements.
     *
     * @throws XFTInitException When an error occurs in XFT.
     */
    public ArrayList getAllElements() throws XFTInitException {
        ArrayList al = new ArrayList();
        Iterator schemas = GetSchemas().iterator();
        while (schemas.hasNext()) {
            XFTSchema schema = (XFTSchema) schemas.next();
            Iterator elements = schema.getSortedElements().iterator();
            while (elements.hasNext()) {
                XFTElement element = (XFTElement) elements.next();
                al.add(GenericWrapperFactory.GetInstance().wrapElement(element));
            }
        }

        al.trimToSize();
        return al;
    }
}

