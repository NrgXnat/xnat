/*
 * HibernateDBBackedSchemaServiceImpl
 * (C) 2016 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 */
package org.nrg.xft.schema.db.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.SQLView;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.JavaFileGenerator;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.schema.XFTDataModel;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.db.entities.DBBackedSchema;
import org.nrg.xft.schema.db.services.DBBackedSchemaService;
import org.nrg.xft.schema.db.dao.DBBackedSchemaDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class HibernateDBBackedSchemaServiceImpl extends AbstractHibernateEntityService<DBBackedSchema, DBBackedSchemaDAO> implements DBBackedSchemaService {
    /**
     * Finds the DBBackedSchemaDAO by path
     *
     * @param path The path the file was found at
     *
     * @return
     */
    @Override
    @Transactional
    public DBBackedSchema findConfigByPath(String path) {
        return getDao().findByPath(path,false);
    }

    @Override
    @Transactional
    public DBBackedSchema findConfigByName(String name) {
        return getDao().findByName(name,false);
    }

    @Override
    @Transactional
    public DBBackedSchema createConfig(String path, String name, String content) throws Exception{
        DBBackedSchema config = new DBBackedSchema();
        config.setContent(content);
        config.setPath(path);
        config.setName(name);

        this.getDao().create(config);

        return this.getDao().findByPath(path,false);
    }

    @Override
    public XFTDataModel initializeSchema(DBBackedSchema schema) throws Exception{
        return XFTManager.GetInstance().addSchema(schema.getName(), schema.getContent());
    }


    @Transactional
    @Override
    public XFTDataModel registerNewElement(String prefix, String complexType, String prettyPrintName, SchemaElement extended, String singular, String plural, boolean generateDisplayDoc) throws Exception {
        final String proposedType = String.format("%1$s:%2$s",prefix,complexType);
        final String proposedName = String.format("%1$s:%2$s",prefix,prettyPrintName);

        try {
            if(SchemaElement.GetElement(proposedType)!=null){
                //duplicate
                return null;
            }
        } catch (Exception e) {
            //safe
        }

        try {
            if(SchemaElement.GetElement(proposedName)!=null){
                //duplicate
                return null;
            }
        } catch (Exception e) {
            //safe
        }

        final String xsd = String.format(SCHEMA_TEMPLATE,prefix,complexType,prettyPrintName,extended);

        return registerNewSchema("/custom/".concat(complexType), proposedType, xsd, generateDisplayDoc);
    }

    @Transactional
    @Override
    public XFTDataModel registerNewSchema(String path, String name, String content, boolean generateDisplayDoc) throws Exception {
        final DBBackedSchema schema = this.createConfig(path, name, content);

        final XFTDataModel model = XFTManager.GetInstance().addSchema(schema.getName(), schema.getContent());

        //initialize meta_data, history and references/relationships
        XFTManager.GetInstance().manageAddins(model.getSchema().getTargetNamespacePrefix());

        GenericWrapperElement.ClearElementCache();

        //generate and run create sql
        final List<String> create = new ArrayList<>();
        final List<String> alter = new ArrayList<>();
        final List<String> functions = new ArrayList<>();
        final List<GenericWrapperElement> done = new ArrayList<>();

        model.getSchema().getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).forEach(e -> {
            final GenericWrapperElement se = (GenericWrapperElement) e;

            _log.debug("Creating SQL for element " + se.getFullXMLName());

            try {
                generateSql(se, create, alter, functions);
            } catch (Exception ex) {
                _log.error("",ex);
                throw new RuntimeException(ex);
            }
            done.add(se);
        });

        generateExtensions(done, functions);

        create.addAll(alter);
        create.addAll(functions);

        if (!create.isEmpty()) {
            PoolDBUtils.ExecuteBatch(create, null, null);
        }

        if(generateDisplayDoc){
            model.getSchema().getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).forEach(o -> {
                final GenericWrapperElement se = (GenericWrapperElement) o;
                if(se.canBeRoot()) {
                    _log.debug("Generating display XML for root element " + se.getFullXMLName());
                    final StringBuilder displayXML = (new JavaFileGenerator()).generateDisplayXml(se);
                    final ElementDisplay ed = DisplayManager.GetInstance().registerDisplay(displayXML);

                    final String projectsView = ed.getSQLName().toUpperCase().concat("_PROJECTS");
                    final SQLView v = DisplayManager.getSqlViews().get(projectsView);
                    if (v != null) {
                        final String viewCreationQuery = String.format(CREATE_VIEW_TEMPLATE, v.getName(), v.getSql());

                        try {
                            PoolDBUtils.ExecuteNonSelectQuery(viewCreationQuery, null, null);
                        } catch (Exception e) {
                            _log.error("", e);
                        }
                    } else {
                        RuntimeException e = new RuntimeException("Unable to find view " + projectsView + " for element " + se.getXSIType());
                        _log.error("", e);
                        throw e;
                    }
                }else{
                    _log.debug("Skipping display XML generation for non-root element " + se.getFullXMLName());
                }
            });
        }

        return model;
    }

    public static final Map<String,List<String>> elementsPerConfig = Maps.newHashMap();

    @Override
    public List<String> getElementNames(DBBackedSchema config) {
        if(!elementsPerConfig.containsKey(config.getName())){
            elementsPerConfig.put(config.getName(), Lists.newArrayList());

            //iterate active schema to identify elements for this schema
            for (final XFTSchema s : XFTManager.GetSchemas()) {
                if (StringUtils.equals(config.getName(), s.getDataModel().getFileName())) {
                    for (final Object o : s.getSortedElements()) {
                        final XFTElement e = (XFTElement) o;
                        final String xsiType= e.getType().getFullForeignType();
                        elementsPerConfig.get(config.getName()).add(xsiType);
                    }
                }
            }
        }

        return elementsPerConfig.get(config.getName());
    }

    private void generateSql(GenericWrapperElement gwe, List<String> create, List<String> alter, final List<String> functions) throws Exception {
        create.add(GenericWrapperUtils.GetCreateStatement(gwe).toString());

        GenericWrapperUtils.GetAlterTableStatements(gwe).forEach( object1 ->  {
            alter.add("\n" + object1);
        });

        Arrays.stream(GenericWrapperUtils.GetFunctionStatements(gwe)).forEach( object1 ->  {
            functions.addAll(object1);
        });
    }

    private void generateExtensions(List<GenericWrapperElement> done, final List<String> functions) throws Exception {
        for(GenericWrapperElement e : GenericWrapperElement.GetAllElements(false)){
            if(("xnat:experimentData".equals(e.getXSIType()) || e.instanceOf("xnat:experimentData")) && e.isExtended() && !done.contains(e)){
                // some functions for xnat:experimentData and its extensions reference extended data types
                // could limit it to these if we wanted this to be more efficient (i_xnat_experimentdata, a_xnat_experimentdata, update_ls_xnat_experimentdata)
                functions.addAll(Arrays.stream(GenericWrapperUtils.GetFunctionStatements(e))
                        .flatMap(Collection::stream)
                        .toList());

                //also effects history tables
                functions.addAll(Arrays.stream(GenericWrapperUtils.GetFunctionStatements(GenericWrapperElement.GetElement(e.getXSIType()+"_history")))
                        .flatMap(Collection::stream)
                        .toList());
            }
        }

    }

    @Override
    @Transactional
    public List<DBBackedSchema> findAllSchema() {
        return getDao().findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean isSchemaLoaded(final long id) throws NotFoundException {
        try {
            final DBBackedSchema schema = get(id);
            return isSchemaLoaded(schema);
        } catch (org.nrg.framework.exceptions.NotFoundException e) {
            throw new NotFoundException(DBBackedSchema.class.toString(), id);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public boolean isSchemaLoaded(final DBBackedSchema schema) {
        // Get element names from the schema
        List<String> elementNames = getElementNames(schema);
        if (_log.isDebugEnabled()) {
            _log.debug("Schema " + schema.getName() + " contains " + elementNames.size() + " elements: " + String.join(", ", elementNames));
        }

        // Check if any element from this schema is already loaded
        for (final String elementName : elementNames) {
            try {
                final SchemaElement schemaElement = SchemaElement.GetElement(elementName);
                _log.info("Element " + elementName + " is already loaded in the system, returning true");
                return true;
            } catch (XFTInitException e) {
                _log.error("XFT initialization error checking for element: " + elementName, e);
            } catch (ElementNotFoundException e) {
                // Ignore this, it just means that the element hasn't been defined, which
                // means we do need to load this schema.
            }
        }

        // If we made it here, no elements from the schema are loaded, so return false;
        return false;
    }

    private static final Log _log = LogFactory.getLog(HibernateDBBackedSchemaServiceImpl.class);

    private final static String SCHEMA_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema targetNamespace=\"http://xnat.org/%1$s\" xmlns:%1$s=\"http://xnat.org/%1$s\" xmlns:xnat=\"http://nrg.wustl.edu/xnat\" xmlns:xdat=\"http://nrg.wustl.edu/xdat\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n" +
            "\t<xs:import namespace=\"http://nrg.wustl.edu/xdat\" schemaLocation=\"../xdat/xdat.xsd\"/>\n" +
            "\t<xs:import namespace=\"http://nrg.wustl.edu/xnat\" schemaLocation=\"../xnat/xnat.xsd\"/>\n" +
            "\t<xs:element name=\"%3$s\" type=\"%1$s:%2$s\"/>" +
            "\t<xs:complexType name=\"%2$s\">\n" +
            "\t\t<xs:complexContent>\n" +
            "\t\t\t<xs:extension base=\"%4$s\">\n" +
            "\t\t\t\t<xs:sequence>\n" +
            "\t\t\t\t\t<xs:element name=\"data\" type=\"xdat:jsonb\"  minOccurs=\"0\"/>\n" +
            "\t\t\t\t</xs:sequence>\n" +
            "\t\t\t</xs:extension>\n" +
            "\t\t</xs:complexContent>\n" +
            "\t</xs:complexType>\n" +
            "</xs:schema>";

    private static final String CREATE_VIEW_TEMPLATE = "CREATE OR REPLACE VIEW %s AS %s;";
}
