/*
 * core: org.nrg.xft.search.QueryOrganizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import java.util.*;

/**
 * @author Tim
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
public class QueryOrganizer implements QueryOrganizerI{

    public static final String XNAT_SUBJECT_DATA = "xnat:subjectData";
    public static final String XNAT_EXPERIMENT_DATA = "xnat:experimentData";
    public static final String XNAT_PROJECT_DATA = "xnat:projectData";
    public static final String XNAT_IMAGE_SCAN_DATA = "xnat:imageScanData";
    protected SchemaElementI       rootElement = null;
    protected UserI                user;

    protected String level;
    protected ArrayList<String> fields = new ArrayList<>();
	  protected Map<String,String> tables = new Hashtable();

    protected StringBuffer joins = new StringBuffer();
    protected Map<String,CachedRootQuery> _rootQueries;

    protected Hashtable<String,String> fieldAliases= new Hashtable();
    protected Hashtable tableAliases= new Hashtable();

    private Hashtable tableColumnAlias = new Hashtable();
    protected Integer joinCounter=0;

    private Hashtable<String,List<String>> externalFields = new Hashtable();
    private Hashtable externalFieldAliases = new Hashtable();
    protected boolean isMappingTable = false;

    private static final boolean PREVENT_DATA_ADMINS = false;//used for testing users with lots of projects

    CriteriaCollection where = null;

    private boolean skipSecurity=false;

    static final String PROJECT_QUERY = "SELECT xnat_projectData.* "
        + " FROM xnat_projectdata "
        + "              LEFT JOIN xnat_projectData_meta_data meta ON xnat_projectdata.projectData_info = meta.meta_data_id \n"
        + "WHERE ID IN (\n"
        + "                 SELECT xfm.field_value \n"
        + "                FROM xdat_element_access xea \n"
        + "\t\tLEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id=grp.xdat_usergroup_id\n"
        + "\t\tLEFT JOIN xdat_user_groupid gid ON grp.id=gid.groupid\n"
        + "\t\tLEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id\n"
        + "\t\tLEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element=1 AND 'xnat:projectData/ID'=xfm.field\n"
        + "\t\tWHERE gid.groups_groupid_xdat_user_xdat_user_id IN (%1$s) OR xea.xdat_user_xdat_user_id IN (%2$s) \n"
        + "                GROUP BY xfm.field_value)";

    static final String PERMISSIONS_QUERY = "SELECT \n"
        + "                  xfm.field_value \n"
        + "                FROM xdat_element_access xea \n"
        + "\t\tLEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id=grp.xdat_usergroup_id\n"
        + "\t\tLEFT JOIN xdat_user_groupid gid ON grp.id=gid.groupid\n"
        + "\t\tLEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id=fms.permissions_allow_set_xdat_elem_xdat_element_access_id\n"
        + "\t\tLEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id=xfm.xdat_field_mapping_set_xdat_field_mapping_set_id AND xfm.read_element=1 AND '%1$s/project'=xfm.field\n"
        + "\t\tWHERE  (field_value IS NOT NULL AND field_value NOT IN ('','*')) AND (gid.groups_groupid_xdat_user_xdat_user_id IN (%2$s) OR xea.xdat_user_xdat_user_id IN (%3$s)) \n"
        + "                GROUP BY xfm.field_value";

    static final String SCAN_QUERY = " SELECT root.*\n"
        + "\tFROM xnat_imageScanData table20 \n"
        + "              LEFT JOIN xnat_imageScanData_meta_data meta ON table20.imageScanData_info = meta.meta_data_id \n"
        + "              LEFT JOIN xnat_imageScanData_share table40 ON table20.xnat_imagescandata_id = table40.sharing_share_xnat_imagescandat_xnat_imagescandata_id \n"
        + "              RIGHT JOIN %1$s root ON table20.xnat_imagescandata_id=root.xnat_imagescandata_id\n"
        + "              WHERE (table20.project IN (SELECT field_value FROM P_%1$s) OR table40.project IN (SELECT field_value FROM P_%1$s))";

    static final String EXPERIMENT_QUERY = " SELECT root.*\n"
        + "\tFROM xnat_experimentData table20 \n"
        + "              LEFT JOIN xnat_experimentData_meta_data meta ON table20.experimentData_info=meta.meta_data_id \n"
        + "              LEFT JOIN xnat_experimentData_share table40 ON table20.id = table40.sharing_share_xnat_experimentDa_id \n"
        + "              RIGHT JOIN %1$s root ON table20.id=root.id\n"
        + "              WHERE (table20.project IN (SELECT field_value FROM P_%1$s) OR table40.project IN (SELECT field_value FROM P_%1$s))";

    static final String SUBJECT_QUERY="SELECT xnat_subjectData.* \n"
        + "  \tFROM xnat_subjectData xnat_subjectData \n"
        + "        LEFT JOIN xnat_subjectData_meta_data meta ON xnat_subjectData.subjectData_info = meta.meta_data_id \n"
        + "        LEFT JOIN xnat_projectParticipant xpp ON xnat_subjectData.id = xpp.subject_id \n"
        + "        WHERE (xnat_subjectData.project IN (SELECT field_value FROM P_XNAT_SUBJECTDATA) OR xpp.project IN (SELECT field_value FROM P_XNAT_SUBJECTDATA))";
    static final String SUBJECT_QUERY1="SELECT xnat_subjectData.* \n"
        + "  \tFROM xnat_subjectData xnat_subjectData \n"
        + "        LEFT JOIN xnat_subjectData_meta_data meta ON xnat_subjectData.subjectData_info = meta.meta_data_id \n"
        + "        WHERE (xnat_subjectData.project IN (SELECT field_value FROM P_XNAT_SUBJECTDATA) )";
    static final String SUBJECT_QUERY2="SELECT xnat_subjectData.* \n"
        + "  \tFROM xnat_projectParticipant xpp\n"
        + "  \t INNER JOIN xnat_subjectData ON xpp.subject_id=xnat_subjectData.id\n"
        + "     LEFT JOIN xnat_subjectData_meta_data meta ON xnat_subjectData.subjectData_info = meta.meta_data_id \n"
        + "     WHERE (xpp.project IN (SELECT field_value FROM P_XNAT_SUBJECTDATA))";

    static final String PROJ_SUBJ_QUERY="SELECT DISTINCT ON (id) xnat_subjectData.* \n"
        + "\tFROM xnat_subjectData\n"
        + "        LEFT JOIN xnat_subjectData_meta_data meta ON xnat_subjectData.subjectData_info = meta.meta_data_id \n"
        + "\tLEFT JOIN xnat_projectParticipant xpp ON xnat_subjectData.id=xpp.subject_id\n"
        + "\tWHERE (xnat_subjectData.project='%1$s' OR xpp.project='%1$s')";
    private static final String PROJ_SUBJ_QUERY1 ="SELECT xnat_subjectData.* FROM xnat_subjectData WHERE project = '%1$s'";
    private static final String PROJ_SUBJ_QUERY2 ="SELECT xnat_subjectData.* FROM xnat_projectParticipant xpp LEFT JOIN xnat_subjectdata ON xpp.subject_id=xnat_subjectdata.id WHERE xpp.project = '%1$s'";

    static  final String PROJ_EXPT_QUERY="SELECT DISTINCT ON (id) root.* \n"
        + "\tFROM xnat_experimentData expt\n"
        + "              LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "\tLEFT JOIN xnat_experimentData_share xpp ON expt.id=xpp.sharing_share_xnat_experimentda_id\n"
        + " RIGHT JOIN %2$s root ON expt.id=root.id"
        + "\tWHERE (expt.project='%1$s' OR xpp.project='%1$s')";

    static final String PROJ_SCAN_QUERY="SELECT DISTINCT ON (id) root.* \n"
        + "\tFROM xnat_imageScanData\n"
        + "              LEFT JOIN xnat_imageScanData_meta_data meta ON xnat_imageScanData.imageScanData_info = meta.meta_data_id \n"
        + "\tLEFT JOIN xnat_imageScanData_share xpp ON xnat_imageScanData.xnat_imagescandata_id=xpp.sharing_share_xnat_imagescandat_xnat_imagescandata_id\n"
        + " RIGHT JOIN %2$s root ON xnat_imageScanData.xnat_imagescandata_id=root.xnat_imagescandata_id"
        + "\tWHERE (xnat_imageScanData.project='%1$s' OR xpp.project='%1$s')";

    static final String EXPT_NON_ROOT_PERMS="SELECT field_value, xme.xdat_meta_element_id FROM xdat_element_access xea \n"
        + "    LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id \n"
        + "    LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid \n"
        + "    LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id \n"
        + "    LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id \n"
        + "                  AND xfm.read_element = 1 \n"
        + "    LEFT JOIN xdat_meta_element xme ON xea.element_name = xme.element_name \n"
        + "    WHERE  (xfm.field LIKE '%%/sharing/share/project' AND field_value IS NOT NULL AND field_value NOT IN ('','*')) AND (gid.groups_groupid_xdat_user_xdat_user_id IN (%1$s) OR xea.xdat_user_xdat_user_id IN (%2$s))\n"
        + "    GROUP BY field_value, xme.xdat_meta_element_id\n"
        + "    ORDER BY field_value, xme.xdat_meta_element_id";

    static final String EXPT_NON_ROOT_QUERY="SELECT \n"
        + "    %1$s.* \n"
        + "  FROM P_%1$s \n"
        + "    LEFT JOIN xnat_experimentData_share ON P_%1$s.field_value=xnat_experimentData_share.project\n"
        + "    INNER JOIN xnat_experimentData expt ON (xnat_experimentData_share.sharing_share_xnat_experimentda_id=expt.id OR P_%1$s.field_value=expt.project) AND P_%1$s.xdat_meta_element_id=expt.extension"
        + "    LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "    INNER JOIN %1$s ON expt.id=%1$s.id";
    static final String EXPT_NON_ROOT_QUERY1="SELECT %1$s.*\n"
        + "    FROM P_%1$s \n"
        + "    INNER JOIN xnat_experimentData expt ON P_%1$s.field_value=expt.project AND P_%1$s.xdat_meta_element_id=expt.extension"
        + "    LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "    INNER JOIN %1$s ON expt.id=%1$s.id";
    static final String EXPT_NON_ROOT_QUERY2="SELECT %1$s.*\n"
        + "    FROM P_%1$s \n"
        + "    INNER JOIN xnat_experimentData_share ON P_%1$s.field_value=xnat_experimentData_share.project\n"
        + "    INNER JOIN xnat_experimentData expt ON xnat_experimentData_share.sharing_share_xnat_experimentda_id=expt.id AND P_%1$s.xdat_meta_element_id=expt.extension"
        + "    LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "    INNER JOIN %1$s ON expt.id=%1$s.id";

    static final String PROJ_EXPT_NON_ROOT_PERMS="SELECT xme.xdat_meta_element_id FROM xdat_element_access xea "
        + "      LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id "
        + "      LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid "
        + "      LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id "
        + "      LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id "
        + "                    AND xfm.read_element = 1 "
        + "      LEFT JOIN xdat_meta_element xme ON xea.element_name = xme.element_name "
        + "      WHERE xfm.field_value = '%1$s' AND (gid.groups_groupid_xdat_user_xdat_user_id IN (%2$s) OR xea.xdat_user_xdat_user_id IN (%3$s))"
        + "      GROUP BY xme.xdat_meta_element_id";
    static final String PROJ_EXPT_NON_ROOT_PERMS_ALL_ACCESS="SELECT xme.xdat_meta_element_id FROM xdat_element_access xea "
        + "      LEFT JOIN xdat_usergroup grp ON xea.xdat_usergroup_xdat_usergroup_id = grp.xdat_usergroup_id "
        + "      LEFT JOIN xdat_user_groupid gid ON grp.id = gid.groupid "
        + "      LEFT JOIN xdat_field_mapping_set fms ON xea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id "
        + "      LEFT JOIN xdat_field_mapping xfm ON fms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id "
        + "                    AND xfm.read_element = 1 "
        + "      LEFT JOIN xdat_meta_element xme ON xea.element_name = xme.element_name "
        + "      WHERE xfm.field_value = '%1$s'"
        + "      GROUP BY xme.xdat_meta_element_id";

    static final String PROJ_EXPT_NON_ROOT_QUERY="SELECT DISTINCT ON (id) root.* "
        + "        FROM xnat_experimentData expt"
        + "        LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id "
        + "        LEFT JOIN xnat_experimentData_share xpp ON expt.id=xpp.sharing_share_xnat_experimentda_id"
        + "        JOIN %2$s root ON expt.id=root.id"
        + "        WHERE (expt.project='%1$s' OR xpp.project='%1$s') AND expt.extension IN (SELECT xdat_meta_element_id FROM P_%2$s) ";
    static final String PROJ_EXPT_NON_ROOT_QUERY1="SELECT DISTINCT ON (id) root.* "
        + "        FROM xnat_experimentData expt"
        + "        LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "        INNER JOIN %2$s root ON expt.id=root.id"
        + "        WHERE (expt.project='%1$s') AND expt.extension IN (SELECT xdat_meta_element_id FROM P_%2$s) ";
    static final String PROJ_EXPT_NON_ROOT_QUERY2="SELECT DISTINCT ON (id) root.* "
        + "        FROM xnat_experimentData_share"
        + "        INNER JOIN xnat_experimentData expt ON xnat_experimentData_share.sharing_share_xnat_experimentda_id=expt.id"
        + "        LEFT JOIN xnat_experimentData_meta_data meta ON expt.experimentData_info=meta.meta_data_id \n"
        + "        INNER JOIN %2$s root ON expt.id=root.id"
        + "        WHERE (xnat_experimentData_share.project='%1$s') AND expt.extension IN (SELECT xdat_meta_element_id FROM P_%2$s) ";

    public QueryOrganizer(String elementName, UserI u, String level, Map<String,CachedRootQuery> rootQueries) throws ElementNotFoundException
    {
        try {
            rootElement = GenericWrapperElement.GetElement(elementName);
        } catch (XFTInitException e) {
            log.error("", e);
        }
        this.level=level;
        user = u;
        setPKField();
        _rootQueries=rootQueries;
    }

    public QueryOrganizer(String elementName, UserI u, String level) throws ElementNotFoundException
    {
        this(elementName,u,level,null);
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level, Map<String,CachedRootQuery> rootQueries)
    {
        rootElement=se;
        user = u;
        this.level=level;
        setPKField();
        _rootQueries=rootQueries;
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level) {
        this(se,u,level,null);
    }

    public static QueryOrganizer buildXFTQueryOrganizerWithClause(SchemaElementI se, UserI u){
        return new QueryOrganizer(se, u, ViewManager.ALL, new LinkedHashMap<>());
    }

    public static QueryOrganizer buildXFTQueryOrganizerWithClause(String se, UserI u) throws ElementNotFoundException {
        return new QueryOrganizer(se, u, ViewManager.ALL, new LinkedHashMap<>());
    }

    public void setJoinCounter(final Integer index){
        joinCounter=index;
    }

    private List<String> keys= new ArrayList<>();
    
    public List<String> getKeys(){
    	return keys;
    }
    
    private void setPKField()
    {
        for (final GenericWrapperField sf : rootElement.getGenericXFTElement().getAllPrimaryKeys()) {
            try {
            	String key=sf.getXMLPathString(rootElement.getFullXMLName());
                addField(key);
            	this.keys.add(key);
            } catch (ElementNotFoundException e) {
                log.error("", e);
            }
        }
    }

    public void setSkipSecurity(boolean b){
        this.skipSecurity=b;
    }

    public Map<String,String> getTableDefinitions(){
        return tables;
    }

    public Hashtable getTableAliases(){
        return tableAliases;
    }

    public void setIsMappingTable(boolean isMap)
    {
        isMappingTable=isMap;
    }

    protected void addDirectField(String xmlPath)
    {
        fields.add(xmlPath);
    }

    public boolean matchesRootType(String xmlPath){
        return XftStringUtils.IsRootElementNameEqualTo(xmlPath,getRootElement().getFullXMLName());
    }
    /**
     * @param xmlPath    The XML path to add.
     * @throws ElementNotFoundException If the indicated element isn't found.
     */
    public void addField(String xmlPath) throws ElementNotFoundException
    {
        xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);

        if ( matchesRootType(xmlPath) ) {
            //add to 'fields' variable which is local fields (simple joins)
            XftStringUtils.addIfMissingIgnoreCase(fields,xmlPath);
        }else{
            //add to sub queries by datatype
            final SchemaElementI foreign = XftStringUtils.GetRootElement(xmlPath);

            assert foreign != null;
            final String foreignFullXMLName = foreign.getFullXMLName();
            //retrieve fields for this data type in this search already.
            final List<String> fieldsForType = Optional.ofNullable(externalFields.get(foreignFullXMLName)).orElse(new ArrayList<>());

            if(!XftStringUtils.containsIgnoreCase(fieldsForType,xmlPath)){
                fieldsForType.add(xmlPath);
                externalFields.put(foreignFullXMLName,fieldsForType);
            }
        }

    }

    /**
     * @param elementName    The element name to retrieve.
     * @return The indicated filter field.
     * @throws ElementNotFoundException If the indicated element isn't found.
     */
    public String getFilterField(String elementName) throws ElementNotFoundException
    {
        try {
            return GenericWrapperElement.GetElement(elementName).getFilterField();
        } catch (XFTInitException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * @param xmlPath    The XML path to retrieve SQL for.
     * @return The SQL for the requested XML path.
     * @throws FieldNotFoundException If the field indicated by xmlPath isn't found.
     */
    public String getTableAndFieldSQL(String xmlPath) throws FieldNotFoundException {
        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
        assert layers != null;
        String s         = layers[1];
        String tableName = s.substring(s.lastIndexOf(".") + 1);
        if (tableAliases.get(tableName) != null) {
            tableName = (String) tableAliases.get(tableName);
        } else {
            String tableNamePath = layers[0];
            if (tables.get(tableNamePath) != null) {
                tableName = (String) tables.get(tableNamePath);
            }
        }

        return tableName + "." + layers[2];
    }

    /**
     * @param tableAlias    The alias for the table.
     * @param xmlPath       The XML path to retrieve SQL for.
     * @return The SQL for the requested XML path.
     * @throws FieldNotFoundException If the field indicated by xmlPath isn't found.
     */
    public static String GetTableAndFieldSQL(String tableAlias, String xmlPath) throws FieldNotFoundException
    {
        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
	    
        String tableName;
        assert layers != null;
        if (layers[1].contains(".")){
            tableName= layers[1].substring(layers[1].lastIndexOf(".") + 1);
        }else{
            tableName = layers[1];
        }
        String rootElement = XftStringUtils.GetRootElementName(xmlPath);
        String viewColumnName="";
        try {
            SchemaElement se = SchemaElement.GetElement(rootElement);
            viewColumnName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),xmlPath,ViewManager.ACTIVE,true,true);
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("", e);
        }
        return tableAlias + "." + XftStringUtils.CreateAlias(tableName, viewColumnName);
    }

    /**
     * Builds query which gets all necessary fields from the db for the generation of the Display Fields
     * (Secured).
     * @return The join query.
     * @throws Exception When an error occurs.
     */
    public String buildJoin() throws Exception
    {
        joins = new StringBuffer();
        tables = new Hashtable();

        //ADD FIELDS
        for (String s:fields)
        {
            try {
                String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                addFieldToJoin(layers);
            } catch (FieldNotFoundException e) {
            	throw e;
            } catch (Exception e) {
                log.error("",e);
                throw new FieldNotFoundException(s);
            }
        }

        Enumeration keys = externalFields.keys();
        while (keys.hasMoreElements())
        {
            String k = (String)keys.nextElement();
            SchemaElement foreign = SchemaElement.GetElement(k);

            ArrayList al = (ArrayList)externalFields.get(k);
            
            QueryOrganizer qo = new QueryOrganizer(foreign,null,ViewManager.ALL, _rootQueries);

            for (final Object anAl : al) {
                String eXmlPath = (String) anAl;
                qo.addField(eXmlPath);
            }
            
            
//          CHECK ARCS
            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
            if (arcDefine!=null)
            {
                joins.append(getArcJoin(arcDefine,qo,foreign));
                tables.put(k,foreign.getSQLName());

                for (final Object anAl : al) {
                    String eXmlPath = (String) anAl;
                    this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
                }
                continue;
            }

            String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

            if (connection != null)
            {
                String localSyntax = connection[0];
                String xmlPath = connection[1];

                log.info("JOINING: " + localSyntax + " to " + xmlPath);
                SchemaFieldI gwf;
                SchemaElementI extension;
                if (rootElement.getGenericXFTElement().instanceOf(foreign.getFullXMLName()) || localSyntax.indexOf(XFT.PATH_SEPARATOR) == -1)
                {
                    extension = rootElement;

                    //MAPS DIRECTLY TO THE ROOT TABLE
                    Iterator pks = extension.getAllPrimaryKeys().iterator();
                    while (pks.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)pks.next();
                        qo.addField(xmlPath + sf.getXMLPathString(""));

                        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(localSyntax + sf.getXMLPathString(""));
                        addFieldToJoin(layers);
                    }


                    //BUILD CONNECTION FROM FOREIGN TO EXTENSION
                    String        query = qo.buildQuery();
                    StringBuilder sb    = new StringBuilder();
                    sb.append(" LEFT JOIN (").append(query);
                    sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");

                    pks = extension.getAllPrimaryKeys().iterator();
                    int pkCount=0;
                    while (pks.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)pks.next();
                        if (pkCount++ != 0)
                        {
                            sb.append(" AND ");
                        }
                        String localCol = this.getTableAndFieldSQL(localSyntax + sf.getXMLPathString(""));
                        String foreignCol = qo.translateXMLPath(xmlPath + sf.getXMLPathString(""),foreign.getSQLName());

                        sb.append(localCol).append("=");
                        sb.append(foreignCol);
                    }
                    joins.append(sb.toString());

                    for (final Object anAl : al) {
                        String eXmlPath = (String) anAl;
                        this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
                    }
                }else{
                    gwf = SchemaElement.GetSchemaField(localSyntax);
                    extension = gwf.getReferenceElement();

                    QueryOrganizer mappingQO = new QueryOrganizer(this.rootElement,null,this.level, _rootQueries);
                    if (this.where!=null){
                        mappingQO.setWhere(where);
                    }
                    mappingQO.setIsMappingTable(true);

                    Iterator pks = extension.getAllPrimaryKeys().iterator();
                    while (pks.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)pks.next();
                        qo.addField(xmlPath + sf.getXMLPathString(""));

                        mappingQO.addField(localSyntax + sf.getXMLPathString(""));
                    }

                    Iterator pKeys = rootElement.getAllPrimaryKeys().iterator();
                    while (pKeys.hasNext())
                    {
                        SchemaFieldI pKey = (SchemaFieldI)pKeys.next();
                        mappingQO.addField(pKey.getXMLPathString(rootElement.getFullXMLName()));
                    }

                    StringBuilder sb = new StringBuilder();

              //BUILD MAPPING TABLE
                    String mappingQuery = mappingQO.buildQuery();
                    sb.append(" LEFT JOIN (").append(mappingQuery);
                    sb.append(") AS ").append("map_").append(foreign.getSQLName()).append(" ON ");
                    pKeys = rootElement.getAllPrimaryKeys().iterator();
                    int pkCount=0;
                    while (pKeys.hasNext())
                    {
                        SchemaFieldI pKey = (SchemaFieldI)pKeys.next();
                        if (pkCount++ != 0)
                        {
                            sb.append(" AND ");
                        }

                        String localCol = this.getTableAndFieldSQL(pKey.getXMLPathString(rootElement.getFullXMLName()));
                        String foreignCol = mappingQO.translateXMLPath(pKey.getXMLPathString(rootElement.getFullXMLName()),"map_" + foreign.getSQLName());

                        sb.append(localCol).append("=");
                        sb.append(foreignCol);
                    }


                    //BUILD CONNECTION FROM FOREIGN TO EXTENSION
                    String query = qo.buildQuery();
                    sb.append(" LEFT JOIN (").append(query);
                    sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");

                    pks = extension.getAllPrimaryKeys().iterator();
                    pkCount=0;
                    while (pks.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)pks.next();
                        if (pkCount++ != 0)
                        {
                            sb.append(" AND ");
                        }
                        String localCol = mappingQO.translateXMLPath(localSyntax + sf.getXMLPathString(""),"map_" + foreign.getSQLName());
                        String foreignCol = qo.translateXMLPath(xmlPath + sf.getXMLPathString(""),foreign.getSQLName());

                        sb.append(localCol).append("=");
                        sb.append(foreignCol);
                    }
                    joins.append(sb.toString());

                    //SET EXTERNAL COLUMN ALIASES
                    for (final Object anAl : al) {
                        String eXmlPath = (String) anAl;
                        this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
                    }
                }
            }else{
                throw new Exception("Unable to join " + rootElement.getSQLName() + " AND " + foreign.getFullXMLName());
            }

        }

        return joins.toString();
    }

    /***************************
     * Builds an SQL statement that is ready for execution, including any WITH clauses.
     * @return
     * @throws Exception
     */
    public String buildFullQuery() throws Exception{
        final String project = getProjectForSpecificQuery();

        final String query = this.buildQuery();
        if ((this.getRootQueries() == null || this.getRootQueries().size() == 0)) {
            return query;
        }

        int clauses = 0;
        final String rootDatatype=getRootElement().getXSIType();
        final String origROOTName="S_"+getRootElement().getSQLName();
        final String permROOTName="P_"+getRootElement().getSQLName();

        if(getRootQueries().containsKey(rootDatatype) && getRootQueries().containsKey(permROOTName) && project!=null  && this.getWhere().numClauses()==2) {
            //only project filter
            boolean modified = false;
            if(query.indexOf(origROOTName)>-1){
                if(getRootElement().instanceOf(XNAT_SUBJECT_DATA)){
                    getRootQueries().remove(rootDatatype);
                    getRootQueries().put(origROOTName+"1",new CachedRootQuery(origROOTName+"1",String.format(PROJ_SUBJ_QUERY1,project)));
                    getRootQueries().put(origROOTName+"2",new CachedRootQuery(origROOTName+"2",String.format(PROJ_SUBJ_QUERY2,project)));
                    modified=true;
                }else if(getRootElement().instanceOf(XNAT_EXPERIMENT_DATA)){
                    getRootQueries().remove(rootDatatype);
                    getRootQueries().put(origROOTName+"1",new CachedRootQuery(origROOTName+"1",String.format(PROJ_EXPT_NON_ROOT_QUERY1,project,getRootElement().getSQLName())));
                    getRootQueries().put(origROOTName+"2",new CachedRootQuery(origROOTName+"2",String.format(PROJ_EXPT_NON_ROOT_QUERY2,project,getRootElement().getSQLName())));
                    modified=true;
                }

                if(modified){
                    final StringBuilder withClause = new StringBuilder();
                    withClause.append("WITH ");
                    for (CachedRootQuery cachedQuery : this.getRootQueries().values()) {
                        if (clauses++ > 0) {
                            withClause.append(", ");
                        }

                        withClause.append(cachedQuery.getAlias()).append(" AS (").append(cachedQuery.getQuery()).append(") ");
                    }

                    withClause.append(query.replace(origROOTName,origROOTName+"1"));
                    withClause.append(" UNION ");
                    withClause.append(query.replace(origROOTName,origROOTName+"2"));
                    return withClause.toString();
                }
            }
        }else if (getRootQueries().containsKey(permROOTName) && project==null){
            boolean modified = false;
            if(getRootQueries().containsKey(rootDatatype) && query.indexOf(origROOTName)>-1){
                if(getRootElement().instanceOf(XNAT_SUBJECT_DATA)){
                    getRootQueries().remove(rootDatatype);
                    getRootQueries().put(origROOTName+"1",new CachedRootQuery(origROOTName+"1",String.format(SUBJECT_QUERY1)));
                    getRootQueries().put(origROOTName+"2",new CachedRootQuery(origROOTName+"2",String.format(SUBJECT_QUERY2)));
                    modified=true;
                }else if(getRootElement().instanceOf(XNAT_EXPERIMENT_DATA)){
                    getRootQueries().remove(rootDatatype);
                    getRootQueries().put(origROOTName+"1",new CachedRootQuery(origROOTName+"1",String.format(EXPT_NON_ROOT_QUERY1,getRootElement().getSQLName())));
                    getRootQueries().put(origROOTName+"2",new CachedRootQuery(origROOTName+"2",String.format(EXPT_NON_ROOT_QUERY2,getRootElement().getSQLName())));
                    modified=true;
                }

                if(modified){
                    //the site wide search query needs to do a DISTINCT on the UNION results due to possible duplicate rows
                    final StringBuilder withClause = new StringBuilder();
                    withClause.append("WITH ");
                    for (CachedRootQuery cachedQuery : this.getRootQueries().values()) {
                        if (clauses++ > 0) {
                            withClause.append(", ");
                        }

                        withClause.append(cachedQuery.getAlias()).append(" AS (").append(cachedQuery.getQuery()).append(") ");
                    }
                    withClause.append("SELECT DISTINCT * FROM (");
                    withClause.append(query.replace(origROOTName,origROOTName+"1"));
                    withClause.append(" UNION ");
                    withClause.append(query.replace(origROOTName,origROOTName+"2"));
                    withClause.append(" ) SRCH");
                    return withClause.toString();
                }
            }
        }

        final StringBuilder withClause = new StringBuilder();
        withClause.append("WITH ");
        for (CachedRootQuery cachedQuery : this.getRootQueries().values()) {
            if (clauses++ > 0) {
                withClause.append(", ");
            }

            withClause.append(cachedQuery.getAlias()).append(" AS (").append(cachedQuery.getQuery()).append(") ");
        }

        withClause.append(query);
        return withClause.toString();
    }


    public String buildQuery() throws Exception
    {
        StringBuilder sb = new StringBuilder();

        String join = buildJoin();
        fieldAliases = new Hashtable<>();
        sb.append("SELECT ");
        Iterator fieldIter = getAllFields().iterator();
        int counter=0;
        ArrayList selected = new ArrayList();
        while (fieldIter.hasNext())
        {
            String s = (String) fieldIter.next();
            String lowerCase = s.toLowerCase();
            if (!selected.contains(lowerCase))
            {
                selected.add(lowerCase);
                String element = XftStringUtils.GetRootElementName(s);
                GenericWrapperElement se = GenericWrapperElement.GetElement(element);
                if (rootElement.getFullXMLName().equalsIgnoreCase(se.getFullXMLName()))
                {
                    log.info("Found matching root and element names: {}", rootElement.getFullXMLName());
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias;
                    if (viewColName==null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }else{
                        alias = XftStringUtils.Last62Chars(viewColName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    final String tableColumnReference = tableNameLC + "_" + colNameLC;
                    if (!selected.contains(tableColumnReference))
                    {
                        selected.add(tableColumnReference);
                        if (counter++==0)
                        {
                            sb.append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }else{
                            sb.append(", ").append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }

                        fieldAliases.put(lowerCase, alias);
                        tableColumnAlias.put(tableColumnReference, alias);
                        log.info("Matching root and element: Stored table '{}' column '{}' and table-column reference '{}' as field alias '{}'", tableName, colName, tableColumnReference, alias);
                    }else{
                        if (tableColumnAlias.get(tableColumnReference) != null)
                        {
                            log.info("Matching root and element: Found existing table-column reference '{}' for table '{}' column '{}', calculated alias is '{}', cached alias is '{}'", tableColumnReference, tableName, colName, alias, tableColumnAlias.get(tableColumnReference));
                            alias = (String)tableColumnAlias.get(tableColumnReference);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }else{
                    log.info("Found non-matching root and element names: '{}' vs '{}'", rootElement.getFullXMLName(), se.getFullXMLName());
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias;
                    if (viewColName!=null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, viewColName);
                    }else{
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    final String tableColumnReference = tableNameLC + "_" + colNameLC;
                    if (!selected.contains(tableColumnReference))
                    {
                        selected.add(tableColumnReference);
	                    if (counter++==0)
	                    {
	                        sb.append(se.getSQLName()).append(".").append(tableName).append("_").append(colName).append(" AS ").append(alias);
	                    }else{
	                        sb.append(", ").append(se.getSQLName()).append(".").append(tableName).append("_").append(colName).append(" AS ").append(alias);
	                    }

	                    fieldAliases.put(lowerCase,alias);
                        tableColumnAlias.put(tableColumnReference, alias);
                        log.info("Non-matching root and element: Stored table '{}' column '{}' and table-column reference '{}' as field alias '{}'", tableName, colName, tableColumnReference, alias);
                    }else{
                        if (tableColumnAlias.get(tableColumnReference) != null)
                        {
                            log.info("Matching root and element: Found existing table-column reference '{}' for table '{}' column '{}', calculated alias is '{}', cached alias is '{}'", tableColumnReference, tableName, colName, alias, tableColumnAlias.get(tableColumnReference));
                            alias = (String)tableColumnAlias.get(tableColumnReference);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }
            }
        }

        for (final Object o : this.getExternalFieldXMLPaths()) {
            String s   = (String) o;
            String sLC = s.toLowerCase();

            String alias      = (String) this.externalFieldAliases.get(sLC);
            String localAlias = alias.substring(alias.indexOf(".") + 1);

            sb.append(", ").append(alias).append(" AS ").append(localAlias);

            fieldAliases.put(sLC, localAlias);
        }

        sb.append(join);

        final String query = sb.toString();
        log.trace("Composed query: {}", query);
        return query;
    }

    public ArrayList getAllFields()
    {
        return new ArrayList<>(fields);
    }

    protected void addFieldToJoin(String s) throws Exception
    {
		String[] layer = GenericWrapperElement.TranslateXMLPathToTables(s);
		addFieldToJoin(layer);
    }

    protected String getRootQuery(String elementName,String sql_name,String level)throws IllegalAccessException{
        try {
            GenericWrapperElement e = GenericWrapperElement.GetElement(elementName);
            //check for queries already built
            if(_rootQueries!=null && _rootQueries.containsKey(e.getXSIType())){
                return _rootQueries.get(e.getXSIType()).getAlias();
            }

            if(_rootQueries==null || user==null || user.isGuest()){
                return getLegacyRootQuery(e,sql_name,level);
            }else if( !(e.instanceOf(XNAT_SUBJECT_DATA) || StringUtils.equals(XNAT_PROJECT_DATA,e.getXSIType()) || StringUtils.equals(XNAT_SUBJECT_DATA,e.getXSIType()) || e.instanceOf(XNAT_EXPERIMENT_DATA) || e.instanceOf(XNAT_IMAGE_SCAN_DATA))) {
                return getLegacyRootQuery(e,sql_name,level);
            }else {
                if(this.skipSecurity){
                    return rootElement.getSQLName();
                }else{
                    CachedRootQuery cachedQ = new CachedRootQuery("S_"+e.getSQLName(),getRootQuery(e,sql_name,level));
                    _rootQueries.put(e.getXSIType(),cachedQ);
                    return cachedQ.getAlias();
                }
            }
        } catch (XFTInitException e) {
            log.error("An error occurred accessing XFT", e);
            return null;
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element " + e.ELEMENT, e);
            return null;
        } catch (Exception e) {
            log.error("Error parsing element " + elementName, e);
            return null;
        }
    }

    private String getLegacyRootQuery(GenericWrapperElement e,String sql_name,String level)throws IllegalAccessException{
        QueryOrganizer securityQO;
        SQLClause coll = null;

        try {
            try {
                if (user==null){
                    if (where!=null && where.numClauses()>0){
                        coll =where;
                    }
                }else{
                    coll = Permissions.getCriteriaForXDATRead(user,new SchemaElement(e));
                }


                if (coll ==null || coll.numClauses()==0){
                    if (where!=null && where.numClauses()>0){
                        coll =where;
                    }
                }
            } catch (IllegalAccessException e1) {
                log.error("", e1);
                coll = new org.nrg.xdat.search.CriteriaCollection("AND");
            }

            if (coll != null)
            {
                if (coll.numClauses() > 0)
                {
                    if (where!=null && where.numClauses()>0){
                        CriteriaCollection newColl = new CriteriaCollection("AND");
                        newColl.add(where);
                        newColl.add(coll);

                        //add support for limited status reflected in search results
                        if (StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                            CriteriaCollection inner = new CriteriaCollection("OR");
                            for(String l: XftStringUtils.CommaDelimitedStringToArrayList(level)){
                                inner.addClause(e.getFullXMLName()+"/meta/status", l);
                            }
                            newColl.add(inner);
                        }

                        coll = newColl;
                    }else if (StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                        CriteriaCollection newColl = new CriteriaCollection("AND");
                        newColl.add(coll);

                        //add support for limited status reflected in search results
                        CriteriaCollection inner = new CriteriaCollection("OR");
                        for(String l: XftStringUtils.CommaDelimitedStringToArrayList(level)){
                            inner.addClause(e.getFullXMLName()+"/meta/status", l);
                        }
                        newColl.add(inner);

                        coll = newColl;
                    }

                    securityQO = new QueryOrganizer(rootElement,null,ViewManager.ALL, _rootQueries);
                    for (final Object o1 : coll.getSchemaFields()) {
                        Object[]     o = (Object[]) o1;
                        String       path = (String) o[0];
                        SchemaFieldI field = (SchemaFieldI) o[1];
                        final boolean hasSchemaField;
                        if (field == null) {
                            field = GenericWrapperElement.GetFieldForXMLPath(path);
                            hasSchemaField = false;
                        } else {
                            hasSchemaField = true;
                        }
                        String fieldName = path.substring(path.lastIndexOf("/") + 1);
                        assert field != null;
                        String id = field.getId();
                        if (log.isTraceEnabled()) {
                            if (hasSchemaField) {
                                log.trace("There was a schema field for type '{}' with ID '{}' and parent type '{}'", path, id, figureItOut(field));
                            } else {
                                log.trace("There was no schema field for type '{}', retrieved the field by XMLPath with ID '{}' and name '{}'", path, id, figureItOut(field));
                            }
                        }
                        if (!id.equalsIgnoreCase(fieldName)) {
                            log.debug("The ID '{}' does not match the name '{}', checking for reference", id, fieldName);
                            if (field.isReference()) {
                                final GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
                                final GenericWrapperField   foreignPK      = foreign.getAllPrimaryKeys().get(0);
                                log.debug("Field '{}' is a reference! The element is '{}', with primary key field '{}' and a parent '{}'", id, foreign.getXMLName(), foreignPK.getXMLPathString(), foreignPK.getParentElement().getFullXMLName());
                                path = foreignPK.getXMLPathString(path);
                            }
                        }
                        log.info("Adding field '{}' to the query", path);
                        securityQO.addField(path);
                    }

                    Iterator keys = rootElement.getAllPrimaryKeys().iterator();
                    ArrayList keyXMLFields = new ArrayList();
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();
                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
                        keyXMLFields.add(key);
                        securityQO.addField(key);
                    }
                    String        subQuery      = securityQO.buildQuery();
                    StringBuilder securityQuery = new StringBuilder("SELECT DISTINCT ON (");

                    for (int i=0;i<keyXMLFields.size();i++){
                        if (i>0) securityQuery.append(", ");
                        securityQuery.append(securityQO.getFieldAlias((String) keyXMLFields.get(i)));
                    }

                    securityQuery.append(") * FROM (").append(subQuery).append(") SECURITY WHERE ");
                    securityQuery.append(coll.getSQLClause(securityQO));

                    StringBuilder query = new StringBuilder("SELECT SEARCH.* FROM (" + securityQuery + ") SECURITY LEFT JOIN " + e.getSQLName() + " SEARCH ON ");

                    keys = rootElement.getAllPrimaryKeys().iterator();
                    int keyCounter=0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();
                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
                        if (keyCounter++>0){
                            query.append(" AND ");
                        }
                        query.append("SECURITY.").append(securityQO.getFieldAlias(key)).append("=SEARCH.").append(sf.getSQLName());

                    }
                    return "(" +query + ")";
                }else{
                    StringBuilder query    = new StringBuilder("SELECT * FROM " + sql_name);
                    Iterator      keys     = rootElement.getAllPrimaryKeys().iterator();
                    int           keyCount =0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();

                        if (keyCount++>0){
                            query.append(" AND ");
                        }else{
                            query.append(" WHERE ");
                        }

                        query.append(sf.getSQLName()).append(" IS NULL ");
                    }
                    return "(" +query + ")";
                    //throw new IllegalAccessException("No defined read privileges for " + rootElement.getFullXMLName());
                }
            }
        } catch (IllegalAccessException e1) {
            log.error("", e1);
            throw e1;
        } catch (Exception e1) {
            log.error("", e1);
        }

        return sql_name;
    }

    private String getRootQuery(GenericWrapperElement e,String sql_name,String level)throws IllegalAccessException{
        final QueryOrganizer securityQO = new QueryOrganizer(rootElement,null,ViewManager.ALL, _rootQueries);
        SQLClause coll = where;

        try {
             if (StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                 //if the data access level is set, then restrict the data by the corresponding status
                 CriteriaCollection newColl = new CriteriaCollection("AND");

                 if(coll!=null && coll.numClauses()>0){
                     newColl.add(coll);
                 }

                 //add support for limited status reflected in search results
                 CriteriaCollection inner = new CriteriaCollection("OR");
                 for (String l : XftStringUtils.CommaDelimitedStringToArrayList(level)) {
                     inner.addClause(e.getFullXMLName() + "/meta/status", l);
                 }
                 newColl.add(inner);

                 coll = newColl;
             }

             //object used to build a query of the root element
            if(coll!=null) {
                for (final Object o1 : coll.getSchemaFields()) {
                    //iterate all the fields in the criteria and add them to the root query
                    final Object[] o = (Object[]) o1;
                    String path = (String) o[0];
                    SchemaFieldI field = (SchemaFieldI) o[1];
                    final boolean hasSchemaField;
                    if (field == null) {
                        field = GenericWrapperElement.GetFieldForXMLPath(path);
                        hasSchemaField = false;
                    } else {
                        hasSchemaField = true;
                    }
                    String fieldName = path.substring(path.lastIndexOf("/") + 1);
                    assert field != null;
                    String id = field.getId();
                    if (log.isTraceEnabled()) {
                        if (hasSchemaField) {
                            log.trace(
                                "There was a schema field for type '{}' with ID '{}' and parent type '{}'",
                                path, id, figureItOut(field));
                        } else {
                            log.trace(
                                "There was no schema field for type '{}', retrieved the field by XMLPath with ID '{}' and name '{}'",
                                path, id, figureItOut(field));
                        }
                    }

                    if (!id.equalsIgnoreCase(fieldName)) {
                        log.debug(
                            "The ID '{}' does not match the name '{}', checking for reference", id,
                            fieldName);
                        if (field.isReference()) {
                            final GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
                            final GenericWrapperField foreignPK = foreign.getAllPrimaryKeys()
                                .get(0);
                            log.debug(
                                "Field '{}' is a reference! The element is '{}', with primary key field '{}' and a parent '{}'",
                                id, foreign.getXMLName(), foreignPK.getXMLPathString(),
                                foreignPK.getParentElement().getFullXMLName());
                            path = foreignPK.getXMLPathString(path);
                        }
                    }
                    log.info("Adding field '{}' to the query", path);
                    securityQO.addField(path);
                }
            }

             ArrayList keyXMLFields = new ArrayList();
             Iterator keys = rootElement.getAllPrimaryKeys().iterator();
             while (keys.hasNext())
             {
                 SchemaFieldI sf = (SchemaFieldI)keys.next();
                 String key =sf.getXMLPathString(rootElement.getFullXMLName());
                 keyXMLFields.add(key);
                 securityQO.addField(key);
             }

            StringBuilder securityQuery = new StringBuilder();
             if(coll!=null && coll.numClauses()>0) {
                 StringBuilder subQuery = this.getRootInnerQuery(securityQO, keyXMLFields);
                 securityQuery.append("SELECT DISTINCT ON (");

                 for (int i = 0; i < keyXMLFields.size(); i++) {
                     if (i > 0)
                         securityQuery.append(", ");
                     securityQuery.append(securityQO.getFieldAlias((String) keyXMLFields.get(i)));
                 }

                 securityQuery.append(") * FROM (").append(subQuery).append(") SECURITY WHERE ");
                 securityQuery.append(coll.getSQLClause(securityQO));
             }else{
                 securityQuery.append(getRootInnerQuery(securityQO, keyXMLFields));
             }

             StringBuilder query = new StringBuilder();
             query.append("SELECT SEARCH.* FROM (").append(securityQuery).append(") SECURITY LEFT JOIN ")
                 .append(e.getSQLName()).append(" SEARCH ON ");

             keys = rootElement.getAllPrimaryKeys().iterator();
             int keyCounter=0;
             while (keys.hasNext())
             {
                 SchemaFieldI sf = (SchemaFieldI)keys.next();
                 String key =sf.getXMLPathString(rootElement.getFullXMLName());
                 if (keyCounter++>0){
                     query.append(" AND ");
                 }
                 query.append("SECURITY.").append(securityQO.getFieldAlias(key)).append("=SEARCH.").append(sf.getSQLName());

             }

             return "(" +query + ")";
        } catch (IllegalAccessException e1) {
            log.error("", e1);
            throw e1;
        } catch (Exception e1) {
            log.error("", e1);
        }

        return sql_name;
    }

    private Boolean hasPublicData(){
        try {
            return Permissions.canReadAny(Users.getGuest(), XNAT_SUBJECT_DATA);
        } catch (UserNotFoundException|UserInitException e) {
            log.error("",e);
            return false;
        }
    }

    private String getProjectQuery(Integer userId){
        return String.format(PROJECT_QUERY,userId,Users.getUserId(Users.DEFAULT_GUEST_USERNAME));
    }

    private String getSubjectQuery(Integer userId){
        CachedRootQuery crq = new CachedRootQuery("P_XNAT_SUBJECTDATA",String.format(PERMISSIONS_QUERY, XNAT_SUBJECT_DATA,userId,Users.getUserId(Users.DEFAULT_GUEST_USERNAME)));
        getRootQueries().put(crq.getAlias(),crq);

        return SUBJECT_QUERY;
    }

    private String getExperimentQuery(Integer userId, SchemaElementI se, String sqlName) throws Exception {
        if(ElementSecurity.IsSecureElement(se.getFullXMLName())){
            CachedRootQuery crq = new CachedRootQuery("P_"+sqlName,String.format(PERMISSIONS_QUERY,se.getFullXMLName(),userId,Users.getUserId(Users.DEFAULT_GUEST_USERNAME)));
            getRootQueries().put(crq.getAlias(),crq);

            return String.format(EXPERIMENT_QUERY,sqlName);
        }else{
            CachedRootQuery crq = new CachedRootQuery("P_"+sqlName,String.format(EXPT_NON_ROOT_PERMS,userId,Users.getUserId(Users.DEFAULT_GUEST_USERNAME)));
            getRootQueries().put(crq.getAlias(),crq);

            return String.format(EXPT_NON_ROOT_QUERY,se.getSQLName());
        }
    }

    private String getScanQuery(Integer userId, String fullXMLName, String sqlName){
        CachedRootQuery crq = new CachedRootQuery("P_"+sqlName,String.format(PERMISSIONS_QUERY,fullXMLName,userId,Users.getUserId(Users.DEFAULT_GUEST_USERNAME)));
        getRootQueries().put(crq.getAlias(),crq);

        return String.format(SCAN_QUERY,sqlName);
    }

    private String getProjectForSpecificQuery(){
        CriteriaCollection where = getWhere();
        //expecting this to look like:
        //collection:AND
        //collection[0]:collection:OR
        //collection[0]:collection[0]:datatype/project=
        //collection[0]:collection[1]:datatype/sharing/share/project=

        if(where==null || where.size()==0){
            return null;
        }

        if(where instanceof CriteriaCollection){
            return getProjectSpecificClause(where);
        }

        return null;
    }

    @Nullable
    private String getProjectSpecificClause(CriteriaCollection clause){
        String project = null;

        if(clause==null || clause.getCriteriaCollection().size()==0){
            return null;
        }

        if((clause.getCriteriaCollection().size()!=2) || clause.containsNestedQuery()){
            if(StringUtils.equals(clause.getJoinType(),"OR") && clause.getCriteriaCollection().size()!=1){
                //or's negate the effect of the project specification clause here
                return null;
            }

            for(SQLClause child: clause.getCriteriaCollection()){
                if(child instanceof CriteriaCollection){
                    project = getProjectSpecificClause((CriteriaCollection) child);
                    if (project != null){
                       return project;
                    }
                }
            }
            return null;
        }

        final String rootElement = this.getRootElement().getFullXMLName();

        final String owned = rootElement+"/project";
        final String shared = rootElement+"/sharing/share/project";
        int matched = 0;

        for(final SQLClause child: clause.getCriteriaCollection()){
            if(StringUtils.equals(getComparisonType(child),"=")){
                if(StringUtils.equalsAnyIgnoreCase(owned,getXMLPath(child)) ||
                    StringUtils.equalsAnyIgnoreCase(shared,getXMLPath(child))){
                    //is expected owned or shared
                    if(project!=null && !StringUtils.equals(project,(String)getValue(child))){
                        //mis-matched project ids
                        break;
                    }
                    project=(String)getValue(child);
                    matched++;

                }
            }
        }
        if(matched==2){
            return project;
        }

        return null;
    }

    private static String getComparisonType(final SQLClause child) {
        if (child instanceof ElementCriteria) {
            return ((ElementCriteria) child).getComparison_type();
        }else if (child instanceof SearchCriteria) {
            return ((SearchCriteria) child).getComparison_type();
        }else{
            return null;
        }
    }

    private static String getXMLPath(final SQLClause child) {
        if (child instanceof ElementCriteria) {
            return ((ElementCriteria) child).getXMLPath();
        }else if (child instanceof SearchCriteria) {
            return ((SearchCriteria) child).getXMLPath();
        }else{
            return null;
        }
    }

    private static Object getValue(final SQLClause child) {
        if (child instanceof ElementCriteria) {
            return ((ElementCriteria) child).getValue();
        }else if (child instanceof SearchCriteria) {
            return ((SearchCriteria) child).getValue();
        }else{
            return null;
        }
    }

    private StringBuilder getRootInnerQuery(QueryOrganizer securityQO, List keyXMLFields) throws Exception {

        //subQuery has security clauses and the primary key fields necessary to join the data
        String        subQuery      = securityQO.buildQuery();
        if(user !=null) {
            final String project = getProjectForSpecificQuery();
            String securedQuery = null;

            boolean modified = false;
            if(project!=null){
                //check security, otherwise this could skip security.
                checkAccess(user,rootElement,project);

                //project specific query
                if (rootElement.getGenericXFTElement().instanceOf(XNAT_SUBJECT_DATA)) {
                    securedQuery=String.format(PROJ_SUBJ_QUERY,project);
                    modified=true;
                } else if (rootElement.getGenericXFTElement().instanceOf(XNAT_EXPERIMENT_DATA)) {
                    if(ElementSecurity.IsSecureElement(rootElement.getFullXMLName())){
                        securedQuery=String.format(PROJ_EXPT_QUERY,project,rootElement.getSQLName());
                    }else{
                        final String permissionQueryTemplate;
                        if(Groups.isDataAccess(user) || Groups.isDataAdmin(user)){
                            permissionQueryTemplate=PROJ_EXPT_NON_ROOT_PERMS_ALL_ACCESS;
                        }else{
                            permissionQueryTemplate=PROJ_EXPT_NON_ROOT_PERMS;
                        }
                        CachedRootQuery crq = new CachedRootQuery("P_"+rootElement.getSQLName(),String.format(permissionQueryTemplate,project,user.getID(),Users.getUserId(Users.DEFAULT_GUEST_USERNAME)));
                        getRootQueries().put(crq.getAlias(),crq);

                        securedQuery=String.format(PROJ_EXPT_NON_ROOT_QUERY,project,rootElement.getSQLName());
                    }
                    modified=true;
                } else if (rootElement.getGenericXFTElement().instanceOf(XNAT_IMAGE_SCAN_DATA)) {
                    securedQuery=String.format(PROJ_SCAN_QUERY,project,rootElement.getSQLName());
                    modified=true;
                }
            }else{
                if(PREVENT_DATA_ADMINS || (!Groups.isDataAdmin(user) && !Groups.isDataAccess(user))) {
                    if (rootElement.getGenericXFTElement().instanceOf(XNAT_SUBJECT_DATA)) {
                        securedQuery = getSubjectQuery(user.getID());
                        modified = true;
                    } else if (rootElement.getGenericXFTElement().instanceOf(XNAT_EXPERIMENT_DATA)) {
                        securedQuery = getExperimentQuery(user.getID(), rootElement, rootElement.getSQLName());
                        modified = true;
                    } else if (rootElement.getGenericXFTElement().instanceOf(XNAT_PROJECT_DATA)) {
                        securedQuery = getProjectQuery(user.getID());
                        modified = true;
                    } else if (rootElement.getGenericXFTElement().instanceOf(XNAT_IMAGE_SCAN_DATA)) {
                        securedQuery = getScanQuery(user.getID(), rootElement.getFullXMLName(), rootElement.getSQLName());
                        modified = true;
                    }
                }
            }

            if (modified && StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                securedQuery+= " AND (meta.status IN (" ;
                securedQuery+= XftStringUtils.CommaDelimitedStringToArrayList(level).stream()
                    .map(s -> "'" + s + "'")
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
                securedQuery+= "))";
            }

            if(securedQuery!=null){
                subQuery = StringUtils.replace(subQuery, "FROM " + rootElement.getSQLName(),"FROM (" + securedQuery + ")");
            }
        }

        //now we need to wrap query for distinct rows
        StringBuilder securityQuery = new StringBuilder("SELECT DISTINCT ON (");
        //distinct on each primary key field
        for (int i=0;i<keyXMLFields.size();i++){
            if (i>0) securityQuery.append(", ");
            securityQuery.append(securityQO.getFieldAlias((String) keyXMLFields.get(i)));
        }

        securityQuery.append(") * FROM (").append(subQuery).append(") SECURITY");
        return securityQuery;
    }

    private void checkAccess(final UserI user, final SchemaElementI rootElement, final String project) throws Exception{
        if(!Permissions.canRead(user,rootElement.getFullXMLName()+"/project",project)){
            throw new InvalidPermissionException(user.getUsername(),rootElement.getFullXMLName(),project);
        }
    }
	
    private String figureItOut(final SchemaFieldI field) {
        if (field instanceof GenericWrapperField) {
            return ((GenericWrapperField) field).getWrapped().getParentElement().getName();
        }
        if (field instanceof SchemaField) {
            return ((SchemaField) field).getWrapped().getParentElement().getFullXMLName();
        }
        return "";
    }

    protected void addFieldToJoin(String[] layers) throws Exception
    {
	    String tableString = layers[0];
	    if (tables.get(tableString)==null)
	    {
		    if (!tableString.contains("."))
		    {
		        String elementName = tableString.substring(tableString.lastIndexOf("]")+1);
		        if (rootElement.getFullXMLName().equalsIgnoreCase(elementName))
		        {
		            tables.put(tableString,layers[1]);
		            joins.append(" FROM ").append(getRootQuery(elementName,layers[1],this.getLevel())).append(" ").append(layers[1]);
		        }else{
		            throw new Exception("Improper initialization of QueryOrganizer");
		        }
		    }else{

                    String foreignAlias = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String subString = tableString.substring(0,tableString.lastIndexOf("."));
                    String joinTable = tableString.substring(tableString.lastIndexOf(".") + 1);

                    String [] layers1 = new String[4];
                    layers1[0]= subString;
                    if (layers[1].contains("."))
                    {
                        layers1[1]= layers[1].substring(0,layers[1].lastIndexOf("."));
                    }
                    if (!layers[3].contains("."))
                    {
                        layers1[3] = "";
                    }else{
                        layers1[3]= layers[3].substring(0,layers[3].lastIndexOf("."));
                    }

                    addFieldToJoin(layers1);

                    //JOIN TO joinTable
                    String rootTable = (String)tables.get(subString);
                    if (tableAliases.get(rootTable)!=null)
                    {
                        rootTable = (String)tableAliases.get(rootTable);
                    }

                    String rootElementName;

                    if (!subString.contains("]"))
                    {
                        rootElementName = subString;
                    }else{
                        rootElementName = subString.substring(subString.lastIndexOf("]")+1);
                    }

                    GenericWrapperElement rootElement = getGenericWrapperElement(rootElementName);

                    String foreignFieldName=null;
                    String foreignElementName;

                    if (!joinTable.contains("]"))
                    {
                        foreignElementName = joinTable;
                    }else{
                        foreignElementName = joinTable.substring(joinTable.lastIndexOf("]")+1);
                        foreignFieldName=joinTable.substring(joinTable.lastIndexOf("[")+1,joinTable.lastIndexOf("]"));
                    }

                    GenericWrapperElement joinElement = getGenericWrapperElement(foreignElementName);

                    String join = getJoinClause(rootElement,joinElement,rootTable,foreignAlias,layers,foreignFieldName);
                    joins.append(" ").append(join);
                    if (tableAliases.get(foreignAlias)!=null)
                    {
                        tables.put(tableString, (String)tableAliases.get(foreignAlias));
                    }else{
                        tables.put(tableString,foreignAlias);
                    }
		    }
	    }else{
	        String tableAlias = layers[1];
	        if (tableAlias.contains("."))
	        {
	            tableAlias = tableAlias.substring(tableAlias.lastIndexOf(".") + 1);
	        }
	        if (tableAliases.get(tableAlias)==null)
	        {
	            tableAliases.put(tableAlias,tables.get(tableString));
	        }
	    }
    }

    /**
     * Returns String []{0:SQL JOIN CLAUSE,1:FOREIGN ALIAS}
     * @param root         The root element.
     * @param foreign      The foreign element.
     * @param rootAlias    The alias to use for the root element.
     * @return The join clause generated from the parameters.
     * @throws Exception When an error occurs.
     */
    private String getJoinClause(GenericWrapperElement root, GenericWrapperElement foreign, String rootAlias,String foreignAlias, String[] layer, String relativeFieldRef) throws Exception
    {
    	if(!tableAliases.containsKey(foreignAlias))
    		tableAliases.put(foreignAlias,"table"+ tableAliases.size() + ""+this.joinCounter);
        foreignAlias = (String)tableAliases.get(foreignAlias);

        String last;
        if (!layer[3].contains("."))
        {
            last = layer[3];
        }else{
            last = layer[3].substring(layer[3].lastIndexOf(".") + 1);
        }
        GenericWrapperField f;
        if (relativeFieldRef==null)
        {
            if(last.endsWith("EXT"))
            {
                f = root.getExtensionField();
            }else{
                f = root.getField(last);
            }
        }else{
            f = root.getField(relativeFieldRef);
            if (f==null)
            {
                if(last.endsWith("EXT"))
                {
                    f = root.getExtensionField();
                }else{
                    f = root.getField(last);
                }
            }
        }

        if(f==null && foreign.isExtensionOf(root)){
        	f= foreign.getExtensionField();
        }
        
        if (f==null)
        {
            f=root.getField(foreign.getXSIType());
        }
        
        if (f == null)
        {
            throw new FieldNotFoundException("");
        }

        StringBuilder j = new StringBuilder(" ");

        XFTReferenceI ref = f.getXFTReference();
        if (ref.isManyToMany())
        {
            XFTManyToManyReference many = (XFTManyToManyReference)ref;
            String mapTableName = many.getMappingTable() + tableAliases.size();
            j.append(" LEFT JOIN ").append(many.getMappingTable()).append(" AS ").append(mapTableName);
            j.append(" ON ");

            Iterator iter =many.getMappingColumnsForElement(root).iterator();
		    int counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j.append(" ").append(rootAlias).append(".").append(map.getForeignKey().getSQLName()).append("=").append(mapTableName).append(".").append(map.getLocalSqlName());
	            }else{
	                j.append(" AND ").append(rootAlias).append(".").append(map.getForeignKey().getSQLName()).append("=").append(mapTableName).append(".").append(map.getLocalSqlName());
	            }
		    }
            j.append(" LEFT JOIN ").append(foreign.getSQLName()).append(" ").append(foreignAlias);
            j.append(" ON ");
		    iter =many.getMappingColumnsForElement(foreign).iterator();
		    counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j.append(" ").append(mapTableName).append(".").append(map.getLocalSqlName()).append("=").append(foreignAlias).append(".").append(map.getForeignKey().getSQLName());
	            }else{
	                j.append(" AND ").append(mapTableName).append(".").append(map.getLocalSqlName()).append("=").append(foreignAlias).append(".").append(map.getForeignKey().getSQLName());
	            }
		    }
        }else{
            j.append(" LEFT JOIN ").append(foreign.getSQLName()).append(" ").append(foreignAlias);
            j.append(" ON ");
            XFTSuperiorReference sup = (XFTSuperiorReference)ref;
            Iterator iter = sup.getKeyRelations().iterator();
            int counter=0;
            while (iter.hasNext())
            {
                XFTRelationSpecification spec = (XFTRelationSpecification)iter.next();
                if (spec.getLocalTable().equalsIgnoreCase(root.getSQLName()))
                {
                    if (counter!=0)
                        j.append(" AND ");
                    j.append(rootAlias).append(".").append(spec.getLocalCol());
                    j.append("=").append(foreignAlias).append(".").append(spec.getForeignCol());
                }else{
                    if (counter!=0)
                        j.append(" AND ");
                    j.append(rootAlias).append(".").append(spec.getForeignCol());
                    j.append("=").append(foreignAlias).append(".").append(spec.getLocalCol());
                }
                counter++;
            }
        }
        return j.toString();
    }

    private GenericWrapperElement getGenericWrapperElement(String s) throws Exception
    {
        if (s.contains("."))
        {
            s= s.substring(s.lastIndexOf(".")+1);
        }
        return GenericWrapperElement.GetElement(s);
    }

    /**
     * @return Returns the fields.
     */
    public ArrayList getFields() {
        return fields;
    }
    /**
     * @param fields The fields to set.
     */
    public void setFields(ArrayList fields) {
        this.fields = fields;
    }
    /**
     * @return Returns the rootElement.
     */
    public GenericWrapperElement getRootElement() {
        return rootElement.getGenericXFTElement();
    }
    /**
     * @param rootElement The rootElement to set.
     */
    public void setRootElement(SchemaElementI rootElement) {
        this.rootElement = rootElement;
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

    @SuppressWarnings("unused")
    public Hashtable<String,String> getFieldAliass()
    {
        return fieldAliases;
    }

    public String getFieldAlias(String xmlPath)
    {
        return fieldAliases.get(xmlPath.toLowerCase());
    }

    public String getFieldAlias(String xmlPath,String tableAlias) throws FieldNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
        }else{
            return tableAlias + "." + fieldAliases.get(xmlPath.toLowerCase());
        }
    }
    
    public String getXPATHforAlias(String alias){
    		for(Map.Entry<String, String> entry: this.fieldAliases.entrySet()){
    			if(entry.getValue().equalsIgnoreCase(alias)){
    				return entry.getKey();
    			}
    		}
    	
    	return null;
    }

    public String translateXMLPath(String xmlPath) throws FieldNotFoundException
    {
        //  org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::1");
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }else{
            try {
                //          org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::2");
                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                String fieldName = xmlPath.substring(xmlPath.lastIndexOf("/") + 1);
                assert f != null;
                String id = f.getId();
                if (!id.equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        //    org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::3");
		try {
            String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
            if (temp !=null)
            {
                //           org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::4");
                return temp;
            }
        } catch (XFTInitException | ElementNotFoundException e1) {
            log.error("", e1);
        }

        //     org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::5");
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return fieldAliases.get(xmlPath.toLowerCase());
        }
    }

    public String translateStandardizedPath(String xmlPath) throws FieldNotFoundException{

        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return fieldAliases.get(xmlPath.toLowerCase());
        }
    }

    public String translateXMLPath(String xmlPath,String tableAlias) throws FieldNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }else{
            try {
                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                String fieldName = xmlPath.substring(xmlPath.lastIndexOf("/") + 1);
                assert f != null;
                if (!f.getId().equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
        if (tableAlias==null || tableAlias.equalsIgnoreCase(""))
        {
            return translateXMLPath(xmlPath);
        }
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
        }else{
            return tableAlias + "." + fieldAliases.get(xmlPath.toLowerCase());
        }
    }

    /**
     * @return Returns the level.
     */
    public String getLevel() {
        return level;
    }
    /**
     * @param level The level to set.
     */
    public void setLevel(String level) {
        this.level = level;
    }

    public ArrayList getExternalFieldXMLPaths()
    {
        ArrayList _return = new ArrayList();
        Enumeration keys = externalFields.keys();
        while (keys.hasMoreElements())
        {
            String k = (String)keys.nextElement();

            ArrayList al = (ArrayList)externalFields.get(k);
            _return.addAll(al);
        }

        return _return;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getRootElement().getFullXMLName()).append("\n");

        for (final Object o : this.getAllFields()) {
            String s = (String) o;
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    /**
     * @return the where
     */
    public CriteriaCollection getWhere() {
        return where;
    }

    /**
     * @param where the where to set
     */
    public void setWhere(CriteriaCollection where) {
        this.where = where;
    }

    public void addWhere(SQLClause clause){
        if(this.where==null){
            this.where= new CriteriaCollection("AND");
        }
        where.addClause(clause);
    }


    public String getArcJoin(ArcDefinition arcDefine,QueryOrganizerI qo,SchemaElement foreign) throws Exception
    {
        StringBuilder sb = new StringBuilder();
        if (arcDefine.getBridgeElement().equalsIgnoreCase(rootElement.getFullXMLName()))
		{
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());
			String rootField = arcDefine.getBridgeField();

			String foreignField = (String)foreignArc.getCommonFields().get(arcDefine.getEqualsField());

			foreign.getDisplay().getDisplayField(foreignField);

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe = df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (XftStringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL, _rootQueries);

					foreignMap.addField(foreignFieldElement);

					Iterator pks = foreign.getAllPrimaryKeys().iterator();
	    			int pkCount;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}else{
			    SchemaElementI middle = XftStringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL, _rootQueries);
			    localMap.setIsMappingTable(true);
				localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
                assert middle != null;
                String localMapName = "map_" + rootElement.getSQLName() + "_" + middle.getSQLName();
				sb.append(" LEFT JOIN (").append(localMapQuery);
				sb.append(") AS ").append(localMapName).append(" ON ");

				pks = rootElement.getAllPrimaryKeys().iterator();
    			pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    if (pkCount++ != 0)
                    {
                        sb.append(" AND ");
                    }
                    String localCol = getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                    String foreignC = localMap.translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),localMapName);

        			sb.append(localCol).append("=");
        			sb.append(foreignC);
                }

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = localMap.translateXMLPath(localFieldElement,localMapName);

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL, _rootQueries);
					foreignMap.addField(foreignFieldElement);

					pks = foreign.getAllPrimaryKeys().iterator();
                    while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}
		}else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()))
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());

			String foreignField = arcDefine.getBridgeField();
			String rootField = (String)rootArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe = df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (XftStringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL, _rootQueries);
					foreignMap.addField(foreignFieldElement);

					Iterator pks = foreign.getAllPrimaryKeys().iterator();
	    			int pkCount;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}else{
			    SchemaElementI middle = XftStringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL, _rootQueries);
			    localMap.setIsMappingTable(true);
			    localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
                assert middle != null;
                String localMapName = "map_" + rootElement.getSQLName() + "_" + middle.getSQLName();
				sb.append(" LEFT JOIN (").append(localMapQuery);
				sb.append(") AS ").append(localMapName).append(" ON ");

				pks = rootElement.getAllPrimaryKeys().iterator();
    			pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    if (pkCount++ != 0)
                    {
                        sb.append(" AND ");
                    }
                    String localCol = getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                    String foreignC = localMap.translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),localMapName);

        			sb.append(localCol).append("=");
        			sb.append(foreignC);
                }

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = localMap.translateXMLPath(localFieldElement,localMapName);

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL, _rootQueries);
					foreignMap.addField(foreignFieldElement);

					pks = foreign.getAllPrimaryKeys().iterator();
                    while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}
		}else
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());

			String distinctField = arcDefine.getDistinctField();

			String foreignField = (String)foreignArc.getCommonFields().get(distinctField);
			String rootField = (String)rootArc.getCommonFields().get(distinctField);

			String arcMapQuery = DisplayManager.GetArcDefinitionQuery(arcDefine,(new SchemaElement(rootElement.getGenericXFTElement())),foreign,user, _rootQueries);
			String arcTableName = DisplayManager.ARC_MAP + rootElement.getSQLName()+"_"+foreign.getSQLName();

			DisplayField rDF = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayField fDF = foreign.getDisplayField(foreignField);
			this.addField(rDF.getPrimarySchemaField());
			//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(rDF.getPrimarySchemaField());
			//this.addFieldToJoin(layers);
			qo.addField(fDF.getPrimarySchemaField());

			sb.append(" LEFT JOIN (").append(arcMapQuery);
			sb.append(") ").append(arcTableName).append(" ON ").append(getTableAndFieldSQL(rDF.getPrimarySchemaField())).append("=").append(arcTableName);
			sb.append(".").append(rootElement.getSQLName()).append("_").append(distinctField);

			String query = qo.buildQuery();
			sb.append(" LEFT JOIN (").append(query);
			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ").append(arcTableName);
			sb.append(".").append(foreign.getSQLName()).append("_");
			sb.append(distinctField).append("=").append(qo.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName()));
		}
        return sb.toString();
    }

    /*************************
     * retrieves stored root queries, to be added via a WITH clause
     *
     * @return
     */
    public Map<String,CachedRootQuery> getRootQueries(){
        return _rootQueries;
    }

    public class CachedRootQuery {
        private String _alias;
        private String _query;

        public CachedRootQuery(String alias, String query){
            _alias=alias;
            _query=query;
        }

        public String getAlias() {
            return _alias;
        }

        public String getQuery() {
            return _query;
        }
    }

}
