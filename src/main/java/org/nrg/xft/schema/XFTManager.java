/*
 * core: org.nrg.xft.schema.XFTManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.schema;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xft.collections.XFTElementSorter;
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
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.utils.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XFTManager {
    private static final Logger     logger  = LoggerFactory.getLogger(XFTManager.class);
    private static       XFTManager MANAGER = null;
    private static       boolean    complete = false;
    private static       boolean    error = false;

    private static XFTElement                      ELEMENT_TABLE = null;
    private static final Map<String, XFTDataModel> DATA_MODELS   = new Hashtable<>();

    //TODO: Make this more generic so that you can dynamically ignore schema
    private static final List<String> IGNORED_SCHEMA_NAMES = Lists.newArrayList("xdat.xsd", "build.xsd", "display.xsd", "instance.xsd", "PlexiViewer.xsd");

    private static Map<String,String> ROOT_LEVEL_ELEMENTS = new Hashtable<>();

    private String sourceDir = "";

    /**
     * Indicates whether the manager instance has been initialized.
     * @return Returns true if the manager is initialized and ready for use, false otherwise.
     */
    public static boolean isInitialized() {
        return isComplete();
    }

    /**
     * Indicates whether the manager instance initialization has been completed.
     * @return Returns true if the manager has completed all initialization and data load operations, false otherwise.
     */
    public static boolean isComplete() {
        return complete;
    }

    /**
     * Gets singleton instance of the Manager
     *
     * @return The manager instance.
     *
     * @throws XFTInitException When an error occurs in XFT.
     */
    public static XFTManager GetInstance() throws XFTInitException {
        if (MANAGER == null || !isComplete() || error) {
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
        complete=false;
        final LapStopWatch stopWatch = LapStopWatch.createStarted(logger, Level.INFO);
        MANAGER = new XFTManager(schemaLocation);
        stopWatch.lap("Created XFTManager instance");

        try {
            MANAGER.manageAddins();
        } catch (Exception e) {
            logger.error("An error occurred initializing XFT", e);
            error=true;
            return null;
        }
        stopWatch.stop("Completed loading XFTManager add-ins");
        logger.info(stopWatch.toTable());
        complete=true;
        return MANAGER;
    }

    public static void clean() {
        MANAGER = null;
        complete = false;
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
    public static List<XFTSchema> GetSchemas() {
        final List<XFTSchema> al = Lists.newArrayList();
        for(final XFTDataModel model:DATA_MODELS.values()){
        	al.add(model.getSchema());
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
    public static  Map<String,XFTDataModel> GetDataModels() {
        return DATA_MODELS;
    }

    /**
     * Gets the Root Elements
     *
     * @return hash of String properName,String complexType
     */
    public static Map<String,String> GetRootElementsHash() {
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
     * @throws ElementNotFoundException When a specified element can't be found.
     */
    private XFTManager(final String source) throws ElementNotFoundException {
        logger.debug("Java Version is: " + System.getProperty("java.version"));
        sourceDir = (source.contains("WEB-INF") ? source.substring(0, source.indexOf("WEB-INF")) : source) + (source.endsWith(File.separator) ? "" : File.separator);
        System.out.println("SOURCE: " + sourceDir);

        final List<String> schemaParsed = Lists.newArrayList(DATA_MODELS.keySet());
        final List<SchemaWrapper> toLoad = discoverSchema(source);

        //iterate over the list of schema until they've all been loaded
        int registered = 1;
        while (toLoad.size() > 0 && registered > 0) {
            registered = 0;

            //iterate on copy of toLoad so we can remove schema as we register them
            final List<SchemaWrapper> toLoadCopy = Lists.newArrayList(toLoad);
            toLoad.clear();

            //look for schema that are ok to parse in this pass
            for (final SchemaWrapper schema : toLoadCopy) {
                try (final InputStream inputStream = schema.getResource().getInputStream()) {
                    if (!schemaParsed.contains(schema.getName())) {
                        //check if dependent schema have been registered yet, excepting those that are ignored.
                        final List<String> dependencies = Lists.newArrayList(schema.getDependencies());
                        dependencies.removeAll(IGNORED_SCHEMA_NAMES);
                        if (dependencies.size() == 0 || schemaParsed.containsAll(dependencies)) {
                            logger.info("Importing schema: " + schema.toString());
                            final XFTDataModel model = new XFTDataModel();
                            model.setResource(schema.getResource());
                            model.setFileName(schema.getName());
                            model.setSchema(new XFTSchema(XMLUtils.GetDOM(inputStream), model));

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
                    logger.warn("An error occurred trying to close the stream when processing the schema " + schema.getName(), e);
                } catch (XFTInitException e) {
                    logger.error("An error occurred trying to process the schema " + schema.getName(), e);
                }
            }
        }

        //if there are still some to be loaded AFTER all loadable ones have been loaded, then there is a dependency problem.
        if (toLoad.size() > 0) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "Unable to startup due to missing or cyclical schema dependency in the following schemas: " + Joiner.on(", ").join(toLoad));
        }
    }

    private List<SchemaWrapper> discoverSchema(final String source) {
        final List<String> schemaLoaded = Lists.newArrayList();
        final List<SchemaWrapper> toLoad = Lists.newArrayList();

        if (StringUtils.isNotBlank(source)) {
            //load schema from file system
            final File sourceFile = new File(source);
            final File schemasFolder = sourceFile.getName().equals("schemas") ? sourceFile : new File(sourceFile, "schemas");
            if (schemasFolder.exists()) {
                final File[] files = schemasFolder.listFiles();
                if (files != null) {
                    for (final File level1 : files) {
                        if (level1.isDirectory()) {
                            final File[] subfiles = level1.listFiles();
                            if (subfiles != null) {
                                for (final File level2 : subfiles) {
                                    if (!schemaLoaded.contains(level2.getName()) && !IGNORED_SCHEMA_NAMES.contains(level2.getName())) {
                                        if (level2.getName().endsWith(".xsd")) {
                                            schemaLoaded.add(level2.getName());
                                            try {
                                                final List<String> dependencies = getDependentSchema(new FileInputStream(level2));
                                                toLoad.add(new SchemaWrapper("file", level2.getParentFile().getAbsolutePath(), level2.getName(), new FileSystemResource(level2), dependencies));
                                            } catch (Exception e) {
                                                logger.error("An error occurred processing the schema " + level2.getAbsolutePath(), e);
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

        //retrieve schema from classpath through discovery
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources = resolver.getResources("classpath*:schemas/**/*.xsd");
            for (final Resource resource : resources) {
                final String name = resource.getFilename();
                if (!schemaLoaded.contains(name) && !IGNORED_SCHEMA_NAMES.contains(name)) {
                    schemaLoaded.add(name);
                    try {
                        final List<String> dependencies = getDependentSchema(resource.getInputStream());
                        toLoad.add(new SchemaWrapper("cp", resource.getURL().getPath(), name, resource, dependencies));
                    } catch (Exception e) {
                        logger.error("An error occurred while loading resource " + name, e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Unable to discover XSD's from classpath.", e);
        }

        return toLoad;
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
    private static List<String> getDependentSchema(final InputStream in) throws ParserConfigurationException, SAXException, IOException {
        try (final InputStream inputStream = in) {
            final DependencyParser parser = new DependencyParser();
            XDAT.getSerializerService().parse(inputStream, parser, "http://xml.org/sax/properties/lexical-handler", parser);
            return parser.getDependencies();
        }
    }

    private static class SchemaWrapper {
        private final String       location;
        private final String       name;
        private final String       src;
        private final List<String> dependencies;
        private final Resource  in;

        public SchemaWrapper(String src, String location, String name, Resource fileSystemResource, List<String> dependencies) {
            super();
            this.src = src;
            this.location = location;
            this.name = name;
            this.in = fileSystemResource;
            this.dependencies = dependencies;
        }


        public Resource getResource() {
			return in;
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

    private static class DependencyParser extends DefaultHandler2 {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (StringUtils.equals("xs:import", qName)) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    String local = attributes.getLocalName(i);
                    String value = attributes.getValue(i);
                    if (StringUtils.isNotEmpty(value) && StringUtils.equals("schemaLocation", local)) {
                        _dependencies.add(FilenameUtils.getName(value));
                    }
                }
            }
        }

        public List<String> getDependencies() {
            return _dependencies;
        }

        private final List<String> _dependencies = new ArrayList<>();
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
        XFTElementSorter sorter = new XFTElementSorter();
        final XFTFactoryI factory = GenericWrapperFactory.GetInstance();
        for (final XFTSchema schema : GetSchemas()) {
            for (final Object object : schema.getSortedElements()) {
                final XFTElement element = (XFTElement) object;
                final GenericWrapperElement gwe = (GenericWrapperElement) factory.wrapElement(element);
                try {
                    sorter.addElement(gwe);
                } catch (ElementNotFoundException e) {
                    logger.warn("Tried to add the element {}, but couldn't find it, so skipping it.", e.ELEMENT);
                }
            }
        }

        final Vector elements = sorter.getElements();
        ArrayList al = new ArrayList(elements.size());
        al.addAll(elements);
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
