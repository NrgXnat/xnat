/*
 * core: org.nrg.xdat.display.DisplayManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.DirectoryScanner;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.display.transport.services.ArcDefService;
import org.nrg.xdat.display.transport.services.DisplayStoredViewService;
import org.nrg.xdat.display.transport.services.ElementDisplayStorageService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.JavaFileGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.QueryOrganizer.CachedRootQuery;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.XMLUtils;
import org.nrg.xft.utils.XftStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Tim
 */
public class DisplayManager {
    public static final  String                            DISPLAY_FIELDS_VIEW = "displayfields_";
    public static final  String                            ARC_MAP             = "arc_map_";
    private static final Logger                            logger              = LoggerFactory.getLogger(DisplayManager.class);
    private final        Hashtable<String, ElementDisplay> elements            = new Hashtable<>();
    private static       DisplayManager                    instance            = null;
    private final        List<Object[]>                    schemaLinks         = new ArrayList<>();
    private final        Map<String, ArcDefinition>        arcDefinitions      = new HashMap<>();
    private static final Map<String, SQLFunction>          SQL_FUNCTIONS       = new HashMap<>();
    private static final Map<String, SQLView>          SQL_VIEWS       = new HashMap<>();

    private ElementDisplayStorageService _elementDisplayService;
    private static DisplayStoredViewService _displayStoredViewService;
    private ArcDefService _arcDefService;

    private ElementDisplayStorageService getElementDisplayService() {
        if (_elementDisplayService == null) {
            _elementDisplayService = XDAT.getContextService().getBean(ElementDisplayStorageService.class);
        }
        return _elementDisplayService;
    }

    private static DisplayStoredViewService getDisplayStoredViewervice() {
        if (_displayStoredViewService == null) {
            _displayStoredViewService = XDAT.getContextService().getBean(DisplayStoredViewService.class);
        }
        return _displayStoredViewService;
    }

    private ArcDefService getArcDefService() {
        if (_arcDefService == null) {
            _arcDefService = XDAT.getContextService().getBean(ArcDefService.class);
        }
        return _arcDefService;
    }


    /**
     * Gets the elements set in the display manager.
     *
     * @return The elements set in the display manager.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Hashtable getElements() {
        return elements;
    }

    /**
     * Sets the elements for the display manager.
     *
     * @param elements The elements to set for the display manager.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void setElements(Hashtable elements) {
        this.elements.clear();
        this.elements.putAll(elements);
    }

    /**
     * Adds the indicated element to the manager's list of elements.
     *
     * @param element The element to add.
     */
    public void addElement(ElementDisplay element) {
        elements.put(element.getElementName(), element);
    }

    /**
     * Gets an instance of the display manager.
     *
     * @return An instance of the display manager.
     */
    public static DisplayManager GetInstance() {
        if (instance == null) {
            XFTManager.waitForInit();

            instance = new DisplayManager();
            instance.init();
        }
        return instance;
    }

    /**
     * Gets an instance of the display manager.
     *
     * @param location The location of the plugin schemas directory when getting an instance while building a plugin.
     * @return An instance of the display manager.
     */
    public static DisplayManager GetInstance(final String location) {
        if (instance == null) {
            XFTManager.waitForInit();

            instance = new DisplayManager();
            instance.init(location);
        }
        return instance;
    }

    /**
     * Gets the {@link ElementDisplay display element} indicated by the element name.
     *
     * @param name The name of the display element to retrieve.
     * @return The display element.
     */
    public static ElementDisplay GetElementDisplay(final String name) {
        return (ElementDisplay) GetInstance().getElements().get(name);
    }

    /**
     * Assigns displays based on the contents of the submitted document.
     *
     * @param doc The document to be parsed and processed.
     */
    public ElementDisplay assignDisplays(final Document doc, final boolean postInitialization){
       return this.assignDisplays(doc,false, postInitialization);

    }

    /**
     * Assigns displays based on the contents of the submitted xml.
     *
     */
    public ElementDisplay assignDisplays(final StringBuilder buffer, final boolean postInitialization){
        try(final InputStream in=new ByteArrayInputStream(buffer.toString().getBytes(StandardCharsets.UTF_8))){
            if (in != null) {
                final Document doc = XMLUtils.GetDOM(in);
                return assignDisplays(doc,postInitialization);
            }
            return null;
        } catch (IOException e) {
            logger.error("Error reading xml",e);
            return null;
        }
    }

    public ElementDisplay registerDisplay(final StringBuilder buffer){
        final ElementDisplay ed = assignDisplays(buffer,true);
        if(ed!=null){
            getElementDisplayService().saveElementDisplay(ed);
        }
        return ed;
    }

    /**
     * Assigns displays based on the contents of the submitted document.
     *
     * @param doc The document to be parsed and processed.
     * @param allowReplacement - Allow replacement of display fields that already exist
     */
    @SuppressWarnings({"unchecked"})
    public ElementDisplay assignDisplays(Document doc, boolean allowReplacement, boolean postInitialization) {
        final Element root = doc.getDocumentElement();

        final String name = NodeUtils.GetAttributeValue(root, "schema-element", "");

        ElementDisplay ed = GetElementDisplay(name);
        if (ed == null) {
            ed = new ElementDisplay();
            ed.setElementName(name);
        }

        ed.setAllowReplacement(allowReplacement);

        String temp = NodeUtils.GetAttributeValue(root, "value_field", "");
        if (!temp.equalsIgnoreCase("")) {
            ed.setValueField(temp);
        }

        temp = NodeUtils.GetAttributeValue(root, "display_field", "");
        if (!temp.equalsIgnoreCase("")) {
            ed.setDisplayField(temp);
        }

        temp = NodeUtils.GetAttributeValue(root, "display_label", "");
        if (!temp.equalsIgnoreCase("")) {
            ed.setDisplayLabel(temp);
        }

        temp = NodeUtils.GetAttributeValue(root, "brief-description", "");
        if (!temp.equalsIgnoreCase("")) {
            ed.setBriefDescription(temp);
        }

        temp = NodeUtils.GetAttributeValue(root, "full-description", "");
        if (!temp.equalsIgnoreCase("")) {
            ed.setFullDescription(temp);
        }

        int views = 0;
        int functions = 0;

        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeName().equalsIgnoreCase("DisplayField")) {
                Node child1 = nodes.item(i);
                final DisplayField df;
                if (NodeUtils.GetAttributeValue(child1, "xsi:type", "").equals("SubQueryField")) {
                    df = new SQLQueryField(ed);
                } else {
                    df = new DisplayField(ed);
                }

                df.setId(NodeUtils.GetAttributeValue(child1, "id", ""));

                df.setHeader(NodeUtils.GetAttributeValue(child1, "header", ""));
                df.setImage(NodeUtils.GetAttributeValue(child1, "image", "false"));
                df.setVisible(NodeUtils.GetAttributeValue(child1, "visible", "true"));
                df.setSearchable(NodeUtils.GetAttributeValue(child1, "searchable", "false"));
                df.setDataType(NodeUtils.GetAttributeValue(child1, "data-type", null));
                df.setSortBy(NodeUtils.GetAttributeValue(child1, "sort-by", ""));
                df.setSortOrder(NodeUtils.GetAttributeValue(child1, "sort-order", ""));
                df.setHtmlContent(NodeUtils.GetBooleanAttributeValue(child1, "html-content", false));

                for (int k = 0; k < child1.getChildNodes().getLength(); k++) {
                    Node child2 = child1.getChildNodes().item(k);
                    if (child2.getNodeName().equalsIgnoreCase("DisplayFieldElement")) {
                        DisplayFieldElement dfe = new DisplayFieldElement();

                        dfe.setName(NodeUtils.GetAttributeValue(child2, "name", ""));
                        dfe.setSchemaElementName(NodeUtils.GetAttributeValue(child2, "schema-element", ""));
                        dfe.setViewColumn(NodeUtils.GetAttributeValue(child2, "viewColumn", ""));
                        dfe.setViewName(NodeUtils.GetAttributeValue(child2, "viewName", ""));
                        dfe.setXdatType(NodeUtils.GetAttributeValue(child2, "xdat-type", ""));

                        df.addDisplayFieldElement(dfe);
                    } else if (child2.getNodeName().equalsIgnoreCase("Content")) {
                        String type = NodeUtils.GetAttributeValue(child2, "type", "sql");
                        String value = child2.getFirstChild().getNodeValue();
                        df.getContent().put(type, value);
                    } else if (child2.getNodeName().equalsIgnoreCase("description")) {
                        String value = child2.getFirstChild().getNodeValue();
                        df.setDescription(value);
                    } else if (child2.getNodeName().equalsIgnoreCase("HTML-Link")) {
                        HTMLLink htmlLink = new HTMLLink();
                        for (int l = 0; l < child2.getChildNodes().getLength(); l++) {
                            Node child3 = child2.getChildNodes().item(l);
                            if (child3.getNodeName().equalsIgnoreCase("Property")) {
                                HTMLLinkProperty prop = new HTMLLinkProperty();
                                prop.setName(NodeUtils.GetAttributeValue(child3, "name", ""));
                                prop.setValue(NodeUtils.GetAttributeValue(child3, "value", ""));

                                for (int m = 0; m < child3.getChildNodes().getLength(); m++) {
                                    Node child4 = child3.getChildNodes().item(m);
                                    if (child4.getNodeName().equalsIgnoreCase("InsertValue")) {
                                        String id = NodeUtils.GetAttributeValue(child4, "id", "");
                                        String field = NodeUtils.GetAttributeValue(child4, "field", "");
                                        if (!id.equalsIgnoreCase("") && !field.equalsIgnoreCase("")) {
                                            prop.addInsertedValue(id, field);
                                        }
                                    }
                                }

                                htmlLink.addProperty(prop);
                            } else if (child3.getNodeName().equalsIgnoreCase("SecureLink")) {
                                htmlLink.setSecureLinkTo(NodeUtils.GetAttributeValue(child3, "elementName", ""));
                                for (int m = 0; m < child3.getChildNodes().getLength(); m++) {
                                    Node child4 = child3.getChildNodes().item(m);
                                    if (child4.getNodeName().equalsIgnoreCase("securityMappingValue")) {
                                        String id = NodeUtils.GetAttributeValue(child4, "displayFieldId", "");
                                        String field = XftStringUtils.StandardizeXMLPath(NodeUtils.GetAttributeValue(child4, "schemaElementMap", ""));
                                        if (!id.equalsIgnoreCase("") && !field.equalsIgnoreCase("")) {
                                            htmlLink.getSecureProps().put(id, field);
                                        }
                                    }
                                }
                            }
                        }

                        df.setHtmlLink(htmlLink);
                    } else if (child2.getNodeName().equalsIgnoreCase("HTML-Cell")) {
                        df.getHtmlCell().setWidth(NodeUtils.GetAttributeValue(child2, "width", ""));
                        df.getHtmlCell().setHeight(NodeUtils.GetAttributeValue(child2, "height", ""));
                        df.getHtmlCell().setValign(NodeUtils.GetAttributeValue(child2, "valign", null));
                        df.getHtmlCell().setAlign(NodeUtils.GetAttributeValue(child2, "align", null));
                        df.getHtmlCell().setServerLink(NodeUtils.GetAttributeValue(child2, "serverLink", null));
                    } else if (child2.getNodeName().equalsIgnoreCase("HTML-Image")) {
                        df.getHtmlImage().setWidth(NodeUtils.GetAttributeValue(child2, "width", ""));
                        df.getHtmlImage().setHeight(NodeUtils.GetAttributeValue(child2, "height", ""));
                    } else if (child2.getNodeName().equalsIgnoreCase("SubQuery")) {
                        if (df instanceof SQLQueryField field) {
                            String value = child2.getFirstChild().getNodeValue();
                            field.setSubQuery(value);
                        }
                    } else if (child2.getNodeName().equalsIgnoreCase("MappingColumns")) {
                        if (df instanceof SQLQueryField sqf) {
                            for (int l = 0; l < child2.getChildNodes().getLength(); l++) {
                                Node child3 = child2.getChildNodes().item(l);
                                if (child3.getNodeName().equalsIgnoreCase("MappingColumn")) {
                                    String schemaField = NodeUtils.GetAttributeValue(child3, "schemaField", "");
                                    String queryField = NodeUtils.GetAttributeValue(child3, "queryField", "");

                                    sqf.addMappingColumn(schemaField, queryField);
                                }
                            }
                        }
                    }
                }

                try {
                    ed.addDisplayFieldWException(df);
                } catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
                    logger.error("DisplayManager.init: DUPLICATE DisplayField {} {}.{} ",ed.getElementName(), df.getParentDisplay().getElementName(),df.getId());
                }
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("DisplayVersion")) {
                Node displayVersion = nodes.item(i);
                DisplayVersion dv = new DisplayVersion();

                dv.setVersionName(NodeUtils.GetAttributeValue(displayVersion, "versionName", "default"));
                dv.setDefaultOrderBy(NodeUtils.GetAttributeValue(displayVersion, "default-order-by", ""));
                dv.setDefaultSortOrder(NodeUtils.GetAttributeValue(displayVersion, "default-sort-order", "ASC"));
                dv.setBriefDescription(NodeUtils.GetAttributeValue(displayVersion, "brief-description", ""));
                dv.setDarkColor(NodeUtils.GetAttributeValue(displayVersion, "dark-color", ""));
                dv.setLightColor(NodeUtils.GetAttributeValue(displayVersion, "light-color", ""));
                dv.setAllowDiffs(NodeUtils.GetBooleanAttributeValue(displayVersion, "allow-diff-columns", true));

                ed.addVersion(dv);

                for (int j = 0; j < displayVersion.getChildNodes().getLength(); j++) {
                    Node child1 = displayVersion.getChildNodes().item(j);
                    if (child1.getNodeName().equalsIgnoreCase("DisplayFieldRef")) {
                        DisplayFieldRef df = new DisplayFieldRef(dv);
                        df.setElementName(NodeUtils.GetAttributeValue(child1, "element_name", ""));
                        df.setId(NodeUtils.GetAttributeValue(child1, "id", ""));
                        df.setType(NodeUtils.GetAttributeValue(child1, "type", null));
                        df.setValue(NodeUtils.GetAttributeValue(child1, "value", null));
                        df.setHeader(NodeUtils.GetAttributeValue(child1, "header", null));
                        df.setVisible(NodeUtils.GetAttributeValue(child1, "visible", null));
                        try {
                            if (df.getElementName().equals("")) {
                                ed.getDisplayFieldWException(df.getId());
                            }
                            dv.addDisplayField(df);
                        } catch (DisplayFieldRefCollection.DuplicateDisplayFieldRefException e) {
                            logger.error("Duplicate display field", e);
                        } catch (DisplayFieldCollection.DisplayFieldNotFoundException e) {
                            logger.error("Display field not found", e);
                        }
                    } else if (child1.getNodeName().equalsIgnoreCase("HTML-Header")) {
                        dv.getHeaderCell().setWidth(NodeUtils.GetAttributeValue(child1, "width", ""));
                        dv.getHeaderCell().setHeight(NodeUtils.GetAttributeValue(child1, "height", ""));
                        dv.getHeaderCell().setValign(NodeUtils.GetAttributeValue(child1, "valign", null));
                        dv.getHeaderCell().setAlign(NodeUtils.GetAttributeValue(child1, "align", null));
                        dv.getHeaderCell().setServerLink(NodeUtils.GetAttributeValue(child1, "serverLink", null));
                    }
                }
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("SQLView")) {
                SQLView sql = new SQLView();
                sql.setName(NodeUtils.GetAttributeValue(nodes.item(i), "name", ""));
                sql.setSql(NodeUtils.GetAttributeValue(nodes.item(i), "sql", ""));
                sql.setSortOrder(views++);

                if(postInitialization){
                    getDisplayStoredViewervice().createView(sql.getName(),sql.getSql(), sql.getSortOrder(), false);
                }

                AddSqlView(sql);
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("SQLFunction")) {
                SQLFunction sql = new SQLFunction();
                sql.setName(NodeUtils.GetAttributeValue(nodes.item(i), "name", ""));
                sql.setContent(NodeUtils.GetAttributeValue(nodes.item(i), "content", ""));
                sql.setSortOrder(functions++);

                if(postInitialization){
                    getDisplayStoredViewervice().createView(sql.getName(),sql.getContent(), sql.getSortOrder(), true);
                }

                AddSqlFunction(sql);
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("Arc-Definition")) {
                Node child = nodes.item(i);
                ArcDefinition arcDefine = new ArcDefinition();
                arcDefine.setName(NodeUtils.GetAttributeValue(child, "Id", ""));
                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    Node child1 = child.getChildNodes().item(j);
                    if (child1.getNodeName().equalsIgnoreCase("CommonField")) {
                        String id = NodeUtils.GetAttributeValue(child1, "id", "");
                        String type = NodeUtils.GetAttributeValue(child1, "type", "");
                        arcDefine.addCommonField(id, type);
                    } else if (child1.getNodeName().equalsIgnoreCase("Bridge-Element")) {
                        arcDefine.setBridgeElement(NodeUtils.GetAttributeValue(child1, "name", null));
                        arcDefine.setBridgeField(NodeUtils.GetAttributeValue(child1, "field", null));
                    } else if (child1.getNodeName().equalsIgnoreCase("Filter")) {
                        String field = NodeUtils.GetAttributeValue(child1, "field", null);
                        String filter = NodeUtils.GetAttributeValue(child1, "filterType", null);
                        arcDefine.addFilter(field, filter);
                    }
                }

                this.addArcDefinition(arcDefine);
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("Arc")) {
                Node child = nodes.item(i);
                Arc arc = new Arc();
                arc.setName(NodeUtils.GetAttributeValue(child, "name", ""));
                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    Node child1 = child.getChildNodes().item(j);
                    if (child1.getNodeName().equalsIgnoreCase("CommonField")) {
                        String id = NodeUtils.GetAttributeValue(child1, "id", "");
                        String type = NodeUtils.GetAttributeValue(child1, "local-field", "");
                        arc.addCommonField(id, type);
                    }
                }
                ed.addArc(arc);
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("SchemaLink")) {
                Node child1 = nodes.item(i);
                SchemaLink link = new SchemaLink(ed.getElementName());
                link.setElement(NodeUtils.GetAttributeValue(child1, "element", ""));
                link.setType(NodeUtils.GetAttributeValue(child1, "type", ""));
                link.setAlias(NodeUtils.GetAttributeValue(child1, "alias", ""));

                for (int k = 0; k < child1.getChildNodes().getLength(); k++) {
                    Node child2 = child1.getChildNodes().item(k);

                    if (child2.getNodeName().equalsIgnoreCase("Mapping")) {
                        Mapping m = new Mapping();
                        m.setTableName(NodeUtils.GetAttributeValue(child2, "TableName", ""));

                        for (int l = 0; l < child2.getChildNodes().getLength(); l++) {
                            Node child3 = child2.getChildNodes().item(l);
                            if (child3.getNodeName().equalsIgnoreCase("MappingColumn")) {
                                MappingColumn mc = new MappingColumn();
                                mc.setFieldElementXMLPath(NodeUtils.GetAttributeValue(child3, "fieldElement", ""));
                                mc.setMapsTo(NodeUtils.GetAttributeValue(child3, "mapsTo", ""));
                                mc.setRootElement(NodeUtils.GetAttributeValue(child3, "rootElement", ""));
                                m.addColumn(mc);
                            }
                        }
                        link.setMapping(m);
                    }
                }

                ed.addSchemaLink(link);
                addSchemaLink(link);
            } else if (nodes.item(i).getNodeName().equalsIgnoreCase("ViewLink")) {
                Node child1 = nodes.item(i);
                ViewLink link = new ViewLink();
                link.setAlias(NodeUtils.GetAttributeValue(child1, "alias", ""));
                for (int k = 0; k < child1.getChildNodes().getLength(); k++) {
                    Node child2 = child1.getChildNodes().item(k);

                    if (child2.getNodeName().equalsIgnoreCase("Mapping")) {
                        Mapping m = new Mapping();
                        m.setTableName(NodeUtils.GetAttributeValue(child2, "TableName", ""));

                        for (int l = 0; l < child2.getChildNodes().getLength(); l++) {
                            Node child3 = child2.getChildNodes().item(l);
                            if (child3.getNodeName().equalsIgnoreCase("MappingColumn")) {
                                MappingColumn mc = new MappingColumn();
                                mc.setFieldElementXMLPath(NodeUtils.GetAttributeValue(child3, "fieldElement", ""));
                                mc.setMapsTo(NodeUtils.GetAttributeValue(child3, "mapsTo", ""));
                                mc.setRootElement(NodeUtils.GetAttributeValue(child3, "rootElement", ""));
                                m.addColumn(mc);
                            }
                        }
                        link.setMapping(m);
                    }
                }

                ed.addViewLink(link);
            }
        }
        this.addElement(ed);

        return ed;
    }

    public static void clean() {
        instance = null;
    }

    public void init() {
        init(null);
    }

    /**
     * Loads display documents from the specified location.
     * Plugins can call this method from an initialization task to ensure their version of a display document is loaded
     * after xnats version
     * @param pattern - location pattern
     * @param allowReplacement - Allow replacement of display fields that already exist
     */
    public void loadDisplayDocumentsFromLocation(String pattern, boolean allowReplacement) {
        this.loadDisplayDocumentsFromLocation(pattern, new HashMap<String,Exception>(), new ArrayList<String>(), allowReplacement);
    }

    /**
     * Loads display documents from the specified location.
     * @param pattern - location pattern
     * @param errors - Errors are stored here
     * @param loaded - Files that have already been loaded
     * @param allowReplacement - Allow replacement of display fields that already exist
     */
    private void loadDisplayDocumentsFromLocation(String pattern, Map<String,Exception> errors, List<String> loaded, boolean allowReplacement){
        final PathMatchingResourcePatternResolver resolver  = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources = resolver.getResources(pattern);
            for (final Resource resource : resources) {
                if(!loaded.contains(resource.getFilename())){
                    try {
                        InputStream in=resource.getInputStream();
                        if (in != null) {
                            Document doc = XMLUtils.GetDOM(in);
                            ElementDisplay ed = assignDisplays(doc, allowReplacement);
                            loaded.add(resource.getFilename());
                        }
                    } catch (IOException | RuntimeException e) {
                        logger.error("Failed to load display document: " + resource.getFilename(),e);
                        errors.put(resource.getURI().toString(), e);
                    }
                }
            }
        } catch (IOException e1) {
            logger.error("Unable to discover display.xml's from classpath.",e1);
        }
    }

    public void init(String location) {
        List<String> loaded                 = Lists.newArrayList();
        final Map<String, Exception> errors = Maps.newHashMap();

        loadDisplayDocumentsFromLocation("classpath*:schemas/*/display/*_display.xml", errors, loaded, false);

        if(!StringUtils.isEmpty(location)) {
            try {
                ArrayList<String> buildPathDisplayXml = new ArrayList<>();
                try {
                    DirectoryScanner scanner = new DirectoryScanner();
                    scanner.setBasedir(location);
                    scanner.setIncludes(new String[]{"*/display/*_display.xml"});
                    scanner.setCaseSensitive(false);
                    scanner.scan();
                    String[] displayFilePaths = scanner.getIncludedFiles();
                    for (String displayFilePath : displayFilePaths) {
                        buildPathDisplayXml.add(String.valueOf(Path.of(location, displayFilePath)));
                    }
                } catch (Throwable e) {
                    logger.error("Unable to locate display xml files in build location.", e);
                }

                for (final String displayXmlFilePath : buildPathDisplayXml) {
                    File displayXmlFile = new File(displayXmlFilePath);
                    if (!loaded.contains(displayXmlFile.getName())) {
                        try {
                            Document doc = XMLUtils.GetDOM(displayXmlFile);
                            ElementDisplay ed = assignDisplays(doc, false);
                            loaded.add(displayXmlFile.getName());
                        } catch (RuntimeException e) {
                            //noinspection ThrowableResultOfMethodCallIgnored
                            errors.put(displayXmlFile.getPath(), e);
                        }
                    }
                }

            } catch (Throwable e1) {
                logger.error("Unable to discover display.xml's from build path.",e1);
            }
        }

        final ElementDisplayStorageService displayService = this.getElementDisplayService();
        if(displayService != null){
            List<ElementDisplay> eds=displayService.findAllActive();
            for(ElementDisplay ed: eds){
                ElementDisplay existing= DisplayManager.GetElementDisplay(ed.getElementName());
                if(existing!=null){
                    existing.merge(ed);
                }else{
                    addElement(ed);
                }
            }
        }

        //check for missing display docs
        final DBBackedSchemaService dbService=XFTManager.getDBBackedSchemaService();
        if(dbService != null){
            for (final DBBackedSchema schema: dbService.getAll()) {
                for (final String xsiType : dbService.getElementNames(schema)) {
                    if(!this.getElements().containsKey(xsiType)) {//isn't there already
                        try {
                            if (StringUtils.isBlank(GenericWrapperElement.GetElement(xsiType).getAddin())) {//not an addon
                                if (GenericWrapperElement.GetFieldForXMLPath(xsiType + "/project") != null) {// is a securable element
                                    logger.error("Generating DisplayDoc for data type: {}", xsiType);
                                    GenericWrapperElement se = GenericWrapperElement.GetElement(xsiType);
                                    final StringBuilder displayXML = (new JavaFileGenerator()).generateDisplayXml(se);
                                    DisplayManager.GetInstance().registerDisplay(displayXML);
                                }
                            }
                        } catch (XFTInitException e) {
                            logger.error("", e);
                        } catch (ElementNotFoundException e) {
                            logger.error("", e);
                        } catch (Throwable e) {
                            //ignore
                        }
                    }
                }
            }
        }

        final DisplayStoredViewService viewService = getDisplayStoredViewervice();
        if(viewService != null) {
            for (SQLObjectI sqlObj : viewService.findAllSQLObjects()) {
                if (sqlObj instanceof SQLView) {
                    AddSqlView((SQLView) sqlObj);
                } else {
                    AddSqlFunction((SQLFunction) sqlObj);
                }
            }
        }

        final ArcDefService arcDefService = getArcDefService();
        if(arcDefService != null) {
            for(ArcDefinition arcDef: arcDefService.findAllArcDefs()){
                addArcDefinition(arcDef);
            }
        }

        if (errors.size() > 0) {
            logger.error("{} errors occurred while processing the following display documents:", errors.size());
            for (final String filename : errors.keySet()) {
                //noinspection ThrowableResultOfMethodCallIgnored
                final String message = errors.get(filename).getMessage();
                logger.error(" * {}:\n{}", filename, message.replaceAll("(?m)^", "\t"));
            }
        }

        try {
            initArcs();
        } catch (Exception e) {
            logger.error("",e);
        }

    }

    @SuppressWarnings("rawtypes")
	public void initArcs() throws Exception {
        final Enumeration enumer = elements.keys();
        while (enumer.hasMoreElements()) {
            final String elementName = (String) enumer.nextElement();
            final ElementDisplay ed = elements.get(elementName);
            ed.getArcs().forEach((arcName, value) -> {
                final ArcDefinition arcDefine = getArcDefinition(arcName);
                if (arcDefine == null) {
                    logger.warn("WARNING:  INVALID ARC:" + arcName);
                } else {
                    arcDefine.addMember(elementName);
                }
            });
        }
    }

    public static List<String> GetCreateViewsSQL() {
        final List<String> views = new ArrayList<>();

        views.add("GRANT ALL ON TABLE xdat_search.xs_item_access TO public;");

        Object[] col = GetInstance().getElements().values().toArray();

        //CREATE FUNCTIONS
        for (final Object o : GetSortedFunctions()) {
            SQLFunction function = (SQLFunction) o;
            String content = function.getContent();
            if (content.contains("CREATE TYPE ")) {
                try {
                    if (PoolDBUtils.checkIfTypeExists(function.getName().trim())) {
                        continue;
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
                if (content.endsWith(";")) {
                    content = content.substring(0, content.length() - 1);
                }
            } else {

                if (!content.endsWith(";")) {
                    content += ";";
                }
            }
            views.add("--DEFINED FUNCTION\n" + content + "\n\n");
        }

        final List<String> createdAlias = new ArrayList<>();

        for (final SQLView view : getSqlViews().values()) {
            views.add("--DEFINED VIEW\nCREATE OR REPLACE VIEW " + view.getName() + " AS " + view.getSql() + ";\n\n");
        }


        for (final Object aCol : col) {
            ElementDisplay ed = (ElementDisplay) aCol;

            try {
                SchemaElementI root = SchemaElement.GetElement(ed.getElementName());

                try {
                    DisplaySearch ds = new DisplaySearch();
                    ds.setRootElement(ed.getElementName());
                    for (final DisplayField df : ed.getSortedFields()) {
                        if (!(df instanceof SQLQueryField)) {
                            ds.addDisplayField((DisplayField) df);
                        }
                    }



                    String query = ds.getSQLQuery(null);

                    String viewName = DISPLAY_FIELDS_VIEW + root.getGenericXFTElement().getSQLName();
                    if (!createdAlias.contains(viewName)) {
                        createdAlias.add(viewName);
                        views.add("--DISPLAY LINK\nDROP VIEW IF EXISTS " + viewName + ";\nCREATE OR REPLACE VIEW " + viewName + " AS " + query + ";\n\n");
                    }
                } catch (Exception e1) {
                    logger.error("Error in Display Document for '" + root.getFullXMLName() + "'.\n" + e1.getMessage());
                }
            } catch (XFTInitException e) {
                logger.error("Error initializing XFT", e);
            } catch (ElementNotFoundException e) {
                logger.error("Error in Display Document.  \nNo such schema-element '" + ed.getElementName() + "'.", e);
            }
        }

        views.add("""
                CREATE OR REPLACE FUNCTION xdat_search_create("varchar","varchar")
                  RETURNS "varchar" AS
                '
                    declare
                        search_query_name alias for $1;
                        search_query alias for $2;
                	entry xdat_searches%ROWTYPE;
                    begin
                	SELECT * INTO entry FROM xdat_searches WHERE search_name = search_query_name;
                
                	    IF FOUND THEN
                		RAISE NOTICE ''Search Table % exists.'',
                		  search_query_name;
                		UPDATE xdat_searches SET last_access=NOW() WHERE search_name = search_query_name;
                	    ELSE
                		RAISE NOTICE ''Creating Search Table %.'',
                		  search_query_name;
                		EXECUTE ''CREATE TABLE '' || search_query_name || '' AS '' || search_query;
                		INSERT INTO xdat_searches (search_name) VALUES (search_query_name);
                     EXECUTE ''GRANT ALL ON TABLE '' || search_query_name || '' TO public'';
                	    END IF;
                
                	PERFORM xdat_search_drop_unused();
                
                	RETURN ''DONE'';
                    end;
                '
                  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION xdat_search_create("varchar", "varchar", "varchar")
                  RETURNS "varchar" AS
                '
                    declare
                        search_query_name alias for $1;
                        search_query alias for $2;
                        search_owner alias for $3;
                	entry xdat_searches%ROWTYPE;
                    begin
                	SELECT * INTO entry FROM xdat_searches WHERE search_name = search_query_name;
                
                	    IF FOUND THEN
                		RAISE NOTICE ''Search Table % exists.'',
                		  search_query_name;
                		UPDATE xdat_searches SET last_access=NOW() WHERE search_name = search_query_name;
                	    ELSE
                		RAISE NOTICE ''Creating Search Table %.'',
                		  search_query_name;
                		EXECUTE ''CREATE TABLE '' || search_query_name || '' AS '' || search_query;
                		INSERT INTO xdat_searches (search_name,owner) VALUES (search_query_name,search_owner);
                	    END IF;
                
                	PERFORM xdat_search_drop_unused(search_owner);
                
                	RETURN ''DONE'';
                    end;
                '
                  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION xdat_search_drop("varchar")
                  RETURNS "varchar" AS
                '
                    declare
                        search_query_name alias for $1;
                    begin
                	EXECUTE ''DROP TABLE '' || search_query_name;
                	DELETE FROM xdat_searches WHERE search_name = search_query_name;
                	
                	RETURN ''DONE'';
                    end;
                '
                  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION xdat_search_drop_unused()
                  RETURNS "varchar" AS
                '
                    declare
                	entry xdat_searches%ROWTYPE;
                    begin
                	FOR entry IN SELECT * FROM xdat_searches WHERE last_access + INTERVAL ''1 hour'' / int ''2'' < NOW()
                	LOOP
                		PERFORM xdat_search_drop(entry.search_name);
                
                		RAISE NOTICE ''Dropped Expired Search Table %. (Last Access: %)'',
                		  entry.search_name,entry.last_access;
                	END LOOP;
                
                	RETURN ''DONE'';
                    end;
                '
                  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION xdat_search_drop_unused("varchar")
                  RETURNS "varchar" AS
                '
                    declare
                	entry xdat_searches%ROWTYPE;
                        search_owner alias for $1;
                    begin
                	FOR entry IN SELECT * FROM xdat_searches WHERE owner=search_owner AND last_access + INTERVAL ''1 hour'' / int ''2'' < NOW()
                	LOOP
                		PERFORM xdat_search_drop(entry.search_name);
                
                		RAISE NOTICE ''Dropped Expired Search Table %. (Last Access: %)'',
                		  entry.search_name,entry.last_access;
                	END LOOP;
                
                	RETURN ''DONE'';
                    end;
                '
                  LANGUAGE 'plpgsql' VOLATILE;\
                """);

//		try {
//			if(!PoolDBUtils.checkIfTypeExists("sortedstrings")){
//				views.add("CREATE TYPE sortedstrings AS (strings \"varchar\",sort_order int4);");
//			}
//		} catch (Exception e) {
//			logger.error("",e);
//		}

        views.add("""
                CREATE OR REPLACE FUNCTION getnextview()   RETURNS name AS\s
                ' DECLARE   my_record RECORD;  viewName name;\s
                BEGIN  FOR my_record IN SELECT c.relname FROM pg_catalog.pg_class AS c LEFT JOIN pg_catalog.pg_namespace AS n ON n.oid = c.relnamespace
                WHERE     c.relkind IN (''v'') AND n.nspname NOT IN (''pg_catalog'', ''pg_toast'') AND pg_catalog.pg_table_is_visible(c.oid) LIMIT 1
                LOOP   viewName := my_record.relname;  END LOOP;  RETURN (viewName); END; '  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION viewcount()   RETURNS int8 AS ' DECLARE   my_record RECORD;  counter int8;
                BEGIN  FOR my_record IN SELECT * FROM (SELECT COUNT (c.relname) AS view_count FROM pg_catalog.pg_class AS c\s
                LEFT JOIN pg_catalog.pg_namespace AS n ON n.oid = c.relnamespace WHERE     c.relkind IN (''v'') AND n.nspname\s
                NOT IN (''pg_catalog'', ''pg_toast'') AND pg_catalog.pg_table_is_visible(c.oid) LIMIT 1) AS COUNT_TABLE  LOOP   counter := my_record.view_count; \s
                END LOOP;  RETURN (counter); END; '  LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION getsortedstring("varchar", int4)   RETURNS sortedstrings AS 'DECLARE  sorted_strings sortedStrings%ROWTYPE;\s
                BEGIN  sorted_strings.strings:=$1;  sorted_strings.sort_order:=$2;  return sorted_strings; END;'   LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION removeviews()   RETURNS varchar AS ' DECLARE  viewName name;  viewCounter int8;\s
                BEGIN  SELECT INTO viewName getnextview();  SELECT INTO viewCounter viewCount();  WHILE (viewCounter > 0)   LOOP
                EXECUTE ''DROP VIEW ''|| viewName || '' CASCADE'';   RAISE NOTICE ''DROPPED %. % more.'',viewName,viewCounter;   SELECT INTO viewName getnextview();
                SELECT INTO viewCounter viewCount();  END LOOP;   RETURN (''DONE''); END; '   LANGUAGE 'plpgsql' VOLATILE;\
                """);

        views.add("""
                CREATE OR REPLACE FUNCTION stringstosortedtable(varchar[])
                RETURNS SETOF sortedstrings AS
                'DECLARE \s
                ss sortedstrings%ROWTYPE;\s
                i int4; \s
                BEGIN \s
                i :=1 ;
                WHILE ($1[i] IS NOT NULL)\s
                LOOP  \s
                		FOR ss IN\s
                			SELECT * FROM getSortedString($1[i],i)\s
                		LOOP
                			RAISE NOTICE ''SORTED STRING: %,%'',ss.strings,ss.sort_order;
                			RETURN NEXT ss;
                		END LOOP;
                		i:=i+1;\s
                	END LOOP;\s
                	RETURN;\s
                END;'
                   LANGUAGE 'plpgsql' VOLATILE;\
                """);
        return views;
    }

    public static String GetArcDefinitionQuery(ArcDefinition arcD, SchemaElement root, SchemaElement foreign, UserI user, Map<String, CachedRootQuery> rootQueries) throws Exception {
        StringBuilder select = new StringBuilder("");
        StringBuilder join = new StringBuilder(" FROM ");
        int joinCounter = 0;
        StringBuilder where = new StringBuilder("");
        int whereCounter = 0;
        StringBuilder orderBy = new StringBuilder("");
        int orderByCounter = 0;
        Arc rootArc = (Arc) root.getDisplay().getArcs().get(arcD.getName());
        Arc foreignArc = (Arc) foreign.getDisplay().getArcs().get(arcD.getName());

        QueryOrganizer rootQuery = new QueryOrganizer(root, user, ViewManager.DEFAULT_LEVEL, rootQueries);
        QueryOrganizer foreignQuery = new QueryOrganizer(foreign, user, ViewManager.DEFAULT_LEVEL, rootQueries);

        for (Map.Entry<String, String> cf : arcD.getCommonFields().entrySet()) {
            String id = cf.getKey();

            String rootField = (String) rootArc.getCommonFields().get(id);
            String foreignField = (String) foreignArc.getCommonFields().get(id);

            DisplayField rDF = root.getDisplayField(rootField);
            DisplayField fDF = foreign.getDisplayField(foreignField);

            rootQuery.addField(rDF.getPrimarySchemaField());
            foreignQuery.addField(fDF.getPrimarySchemaField());
        }

        String rootString = rootQuery.buildQuery();
        String foreignString = foreignQuery.buildQuery();

        join.append("(").append(rootString).append(") ").append(root.getGenericXFTElement().getSQLName());
        join.append(" LEFT JOIN ").append("(").append(foreignString).append(") ").append(foreign.getGenericXFTElement().getSQLName());

        int counter = 0;

        for (Map.Entry<String, String> cf : arcD.getCommonFields().entrySet()) {
            String id = cf.getKey();

            if (counter++ != 0) {
                select.append(", ");
            }

            String rootField = (String) rootArc.getCommonFields().get(id);
            String foreignField = (String) foreignArc.getCommonFields().get(id);
            DisplayField rDF = root.getDisplayField(rootField);
            DisplayField fDF = foreign.getDisplayField(foreignField);

            select.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName())).append(" AS ");
            select.append(root.getGenericXFTElement().getSQLName()).append("_").append(id);

            select.append(",").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(), foreign.getSQLName())).append(" AS ");
            select.append(foreign.getGenericXFTElement().getSQLName()).append("_").append(id);

        }

        for (final String[] filter : arcD.getFilters()) {
            String filterID = filter[0];
            String filterType = filter[1];

            if (filterType.equalsIgnoreCase("equals")) {
                if (joinCounter++ == 0) {
                    join.append(" ON ");
                } else {
                    join.append(" AND ");
                }

                String rootField = (String) rootArc.getCommonFields().get(filterID);
                String foreignField = (String) foreignArc.getCommonFields().get(filterID);
                DisplayField rDF = root.getDisplayField(rootField);
                DisplayField fDF = foreign.getDisplayField(foreignField);

                join.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName()));
                join.append("=").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(), foreign.getSQLName()));
            } else if (filterType.equalsIgnoreCase("distinct")) {
                String rootField = (String) rootArc.getCommonFields().get(filterID);
                DisplayField rDF = root.getDisplayField(rootField);

                select.insert(0, "SELECT DISTINCT ON (" + rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName()) + ") ");

                if (orderByCounter++ == 0) {
                    orderBy.append(" ORDER BY ");
                } else {
                    orderBy.append(", ");
                }
                orderBy.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName()));
            } else if (filterType.equalsIgnoreCase("closest")) {
                String fieldType = arcD.getCommonFields().get(filterID);
                if (fieldType.equalsIgnoreCase("DATE")) {
                    String rootField = (String) rootArc.getCommonFields().get(filterID);
                    String foreignField = (String) foreignArc.getCommonFields().get(filterID);
                    DisplayField rDF = root.getDisplayField(rootField);
                    DisplayField fDF = foreign.getDisplayField(foreignField);

                    select.append(", ").append("(").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName()));
                    select.append("-").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(), foreign.getSQLName())).append(")");
                    select.append(" AS ").append(filterID).append("_DIFF");

                    if (orderByCounter++ == 0) {
                        orderBy.append(" ORDER BY ");
                    } else {
                        orderBy.append(", ");
                    }
                    orderBy.append("abs(").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName()));
                    orderBy.append("-").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(), foreign.getSQLName())).append(")");
                }
            } else if (filterType.equalsIgnoreCase("before")) {
                String fieldType = arcD.getCommonFields().get(filterID);
                if (fieldType.equalsIgnoreCase("DATE")) {
                    String rootField = (String) rootArc.getCommonFields().get(filterID);
                    String foreignField = (String) foreignArc.getCommonFields().get(filterID);
                    DisplayField rDF = root.getDisplayField(rootField);
                    DisplayField fDF = foreign.getDisplayField(foreignField);

                    if (whereCounter++ == 0) {
                        where.append(" WHERE ");
                    } else {
                        where.append(" AND ");
                    }
                    where.append(" ").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(), root.getSQLName())).append("<=").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(), foreign.getSQLName()));
                }
            }
        }


        return select.toString() + join.toString() + where.toString() + orderBy.toString();
    }

    /**
     * @return Object[String elementName, SchemaLink link]
     */
    public List<Object[]> getSchemaLinks() {
        return new ArrayList<>(schemaLinks);
    }

    /**
     * Sets a list of schema links for the display manager.
     *
     * @param list The list of schema links to set for the display manager.
     */
    public void setSchemaLinks(final List<Object[]> list) {
        schemaLinks.clear();
        schemaLinks.addAll(list);
    }

    public void addSchemaLink(final SchemaLink link) {
        schemaLinks.add(new Object[]{link.getRootElement(), link});
        schemaLinks.add(new Object[]{link.getElement(), link});
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList getSchemaLinksFor(String elementName) {
        ArrayList al = new ArrayList();
        Iterator iter = schemaLinks.iterator();
        while (iter.hasNext()) {
            Object[] o = (Object[]) iter.next();
            if (((String) o[0]).equalsIgnoreCase(elementName)) {
                al.add((SchemaLink) o[1]);
            }
        }
        return al;
    }

    /**
     * Gets the arc definitions for the system.
     * @return A map of the available arc definitions, stored by their IDs.
     */
    public Map<String, ArcDefinition> getArcDefinitions() {
        return new HashMap<>(arcDefinitions);
    }


    /**
     * Sets the available arc definitions for the display manager.
     *
     * @param definitions The definitions to set for the display manager.
     */
    public void setArcDefinitions(final Map<String, ArcDefinition> definitions) {
        arcDefinitions.clear();
        arcDefinitions.putAll(definitions);
    }

    public void addArcDefinition(final ArcDefinition arc) {
        arcDefinitions.put(arc.getName(), arc);
    }

    public ArcDefinition getArcDefinition(String name) {
        return arcDefinitions.get(name);
    }

    /**
     * Get the ArcDefinition which relates these two elements.
     *
     * @param root    The root schema element.
     * @param foreign The foreign-key schema element.
     * @return The related {@link ArcDefinition} object.
     */
    public ArcDefinition getArcDefinition(SchemaElementI root, SchemaElementI foreign) {
        ArcDefinition temp = null;
        for (final ArcDefinition arcDefine : arcDefinitions.values()) {
            if (arcDefine.getBridgeElement().equalsIgnoreCase(root.getFullXMLName()) &&
                    arcDefine.isMember(foreign.getFullXMLName())) {
                temp = arcDefine;
                break;
            } else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()) &&
                    arcDefine.isMember(root.getFullXMLName())) {
                temp = arcDefine;
                break;
            }
        }

        if (temp == null) {
            for (final ArcDefinition arcDefine : arcDefinitions.values()) {
                if (arcDefine.isMember(root.getFullXMLName()) && arcDefine.isMember(foreign.getFullXMLName())) {
                    temp = arcDefine;
                    break;
                }
            }
        }

        return temp;
    }

    /**
     * Get all {@link ArcDefinition archive definitions} associated with the specified {@link SchemaElementI schema
     * element instance}.
     *
     * @param root The root schema element requested.
     * @return The {@link ArcDefinition archive definitions} associated with the specified {@link SchemaElementI schema
     * element instance}.
     */
    public List<ArcDefinition> getArcDefinitions(SchemaElementI root) {
        final List<ArcDefinition> definitions = new ArrayList<>();
        for (final ArcDefinition arcDefine : arcDefinitions.values()) {
            if (arcDefine.getBridgeElement().equalsIgnoreCase(root.getFullXMLName())) {
                definitions.add(arcDefine);
            } else if (arcDefine.isMember(root.getFullXMLName())) {
                definitions.add(arcDefine);
            }
        }
        return definitions;
    }

    public static void AddSqlFunction(SQLFunction function) {
        SQL_FUNCTIONS.put(function.getName(), function);
    }

    /**
     * @return Returns the sqlFunctions.
     */
    public static Map<String, SQLFunction> GetSqlFunctions() {
        return SQL_FUNCTIONS;
    }

    public static List<SQLFunction> GetSortedFunctions() {
        final List<SQLFunction> functions = new ArrayList<>();
        functions.addAll(GetSqlFunctions().values());
        Collections.sort(functions, SQLFunction.SequenceComparator);
        return functions;
    }

    public static void AddSqlView(SQLView function) {
        SQL_VIEWS.put(function.getName(), function);
    }

    /**
     * @return Returns the sqlFunctions.
     */
    public static Map<String, SQLView> getSqlViews() {
        return SQL_VIEWS;
    }

    public static List<SQLView> GetSortedViews() {
        final List<SQLView> views = Lists.newArrayList();
        views.addAll(getSqlViews().values());
        Collections.sort(views, SQLView.SequenceComparator);
        return views;
    }

    public String getSingularDisplayNameForElement(String elementName) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            return se.getSingularDescription();
        } catch (Exception e) {
            return elementName;
        }
    }

    public String getPluralDisplayNameForElement(String elementName) {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            return se.getPluralDescription();
        } catch (Exception e) {
            return elementName;
        }
    }

    public String getSingularDisplayNameForProject() {
        return getSingularDisplayNameForElement("xnat:projectData");
    }

    public String getPluralDisplayNameForProject() {
        return getPluralDisplayNameForElement("xnat:projectData");
    }

    public String getSingularDisplayNameForSubject() {
        return getSingularDisplayNameForElement("xnat:subjectData");
    }

    public String getPluralDisplayNameForSubject() {
        return getPluralDisplayNameForElement("xnat:subjectData");
    }

    /**
     * xnat:imageSessionData is not an instantiable data type, and it seems silly to make it so just to reference the
     * singular/plural display names, so we'll just use a site config property for this one.
     * @return The display name for the data type.
     * @throws ConfigServiceException When the name can't be located.
     */
    public String getSingularDisplayNameForImageSession() throws ConfigServiceException {
        return XDAT.getSiteConfigurationProperty("imageSessionDisplayNameSingular", "Session");
    }

    /**
     * xnat:imageSessionData is not an instantiable data type, and it seems silly to make it so just to reference the
     * singular/plural display names, so we'll just use a site config property for this one.
     * @return The display name for the data type.
     * @throws ConfigServiceException When the name can't be located.
     */
    public String getPluralDisplayNameForImageSession() throws ConfigServiceException {
        return XDAT.getSiteConfigurationProperty("imageSessionDisplayNamePlural", "Sessions");
    }

    /**
     * xnat:imageSessionData is not an instantiable data type, and it seems silly to make it so just to reference the
     * singular/plural display names, so we'll just use a site config property for this one.
     * @return The display name for the data type.
     * @throws ConfigServiceException When the name can't be located.
     */
    public String getSingularDisplayNameForMRSession() {
        return getSingularDisplayNameForElement("xnat:mrSessionData");
    }

    /**
     * xnat:imageSessionData is not an instantiable data type, and it seems silly to make it so just to reference the
     * singular/plural display names, so we'll just use a site config property for this one.
     * @return The display name for the data type.
     * @throws ConfigServiceException When the name can't be located.
     */
    public String getPluralDisplayNameForMRSession() {
        return getPluralDisplayNameForElement("xnat:mrSessionData");
    }
}

