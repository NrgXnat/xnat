package org.nrg.xnat.customforms.customvariable.migration.reviewer;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatFielddefinitiongroupFieldI;
import org.nrg.xdat.model.XnatFielddefinitiongroupI;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xnat.customforms.customvariable.migration.model.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MigrationDataReviewer {

    public MigrationDataReviewer(final JdbcTemplate template,
                                 final String dataType,
                                 final String projectId, final XnatFielddefinitiongroupI fieldDefinitiongroup) {

        this.template = template;
        this.dataType = dataType;
        this.projectId = projectId;
        this.fieldDefinitiongroup = fieldDefinitiongroup;
        this.distinctEntityIds = new ArrayList<String>();
        this.fieldDefinitions = new ArrayList<FieldDefinition>();
        this.fieldNames = new ArrayList<String>();
        fieldNameDatatype = new Hashtable<String, String>();
    }

    private void fetchRequiredData() {
        final String entityColumnName = getEntityColumnName();
        final String tableName = getTableNane();
        final String joinClause = getJoinClause();
        distinctEntityIds =  template.queryForList("select distinct " + entityColumnName + " from " + tableName + " " + joinClause, String.class);
    }

    public void init() throws DataAccessException {
        setFieldDefinitions();
        extractDataTypeOfField();
        setFieldNames();
        fetchRequiredData();
    }

    private void setFieldDefinitions() {
        List<XnatFielddefinitiongroupFieldI> fields = fieldDefinitiongroup.getFields_field();
        for(XnatFielddefinitiongroupFieldI field : fields) {
            fieldDefinitions.add(new FieldDefinition(field.getName(), field.getDatatype()));
        }
    }

    private void extractDataTypeOfField() {
       for (FieldDefinition fieldDefinition : fieldDefinitions) {
            fieldNameDatatype.put(fieldDefinition.getName().toLowerCase(), fieldDefinition.getDatatype());
        }

    }
    public String getEntityColumnName() {
        String entityColumnName  = "fields_field_xnat_experimentdat_id";
        if (dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            entityColumnName = "fields_field_xnat_subjectdata_id";
        }
        return entityColumnName;
    }

    private String getJoinClause() {
        String joinClause = " left join xnat_experimentdata j ";
        String entityColumnName = getEntityColumnName();
        if (dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            joinClause = " left join xnat_subjectdata j ";
        }
        joinClause += " on j.id = " + entityColumnName;
        if (!dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            joinClause += " left join xdat_meta_element m on j.extension = m.xdat_meta_element_id ";
        }
        joinClause += " where j.project ='" + projectId + "'";
        if (!dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            joinClause += " and m.element_name='" + dataType + "'" ;
        }
        return joinClause;
    }

    public String getTableNane() {
        String tableName = "xnat_experimentdata_field";
        if (dataType.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
            tableName = "xnat_subjectdata_field";
        }
        return tableName;
    }

    private void setFieldNames() {
        //XNAT will save the field names in lower case even if the definition has them in mixed case
        for(FieldDefinition fieldDefinition : fieldDefinitions) {
            fieldNames.add(fieldDefinition.getName().toLowerCase());
        }
    }

    public DataIntegrityFailureReport reviewData(Hashtable<String, List<CustomVariable>> entityCustomVariableHash) {
        DataIntegrityFailureReport dataIntegrityFailureReport = new DataIntegrityFailureReport();
        dataIntegrityFailureReport.setProjectId(projectId);
        for (String entityId : distinctEntityIds) {
            List<CustomVariable> customVariables = entityCustomVariableHash.get(entityId);
            DataIntegrityFailureReportItem dataIntegrityFailureReportItem = reviewEntityData(entityId, customVariables);
            if (!dataIntegrityFailureReportItem.getDataIntegrityItems().isEmpty()) {
                dataIntegrityFailureReport.addDataIntegrityReportItem(dataIntegrityFailureReportItem);
            }
        }
        return dataIntegrityFailureReport;
    }

    public Hashtable<String, List<CustomVariable>> getEntityCustomVariableData() throws DataAccessException {
        final String entityColumnName = getEntityColumnName();
        final String tableName = getTableNane();
        Hashtable<String, List<CustomVariable>> entityCustomVariableHash = new Hashtable<String, List<CustomVariable>>();
        for (String entityId : distinctEntityIds) {
            String inSql = String.join(",", Collections.nCopies(fieldDefinitions.size(), "?"));
            List<CustomVariable> customVariables = template.query(
                    String.format("select f.field, f.name  from " + tableName +" f where " +
                            "f."+ entityColumnName + "='" + entityId+ "'" +
                            " and f.name in (%s)", inSql),
                    fieldNames.toArray(),
                    (rs, rowNum) -> new CustomVariable(rs.getString("name"),
                            rs.getString("field")));
            entityCustomVariableHash.put(entityId, customVariables);
        }
        return entityCustomVariableHash;
    }

    private DataIntegrityFailureReportItem reviewEntityData(final String entityId, final List<CustomVariable> customVariables ) {
        DataIntegrityFailureReportItem dataIntegrityFailureReportItem = new DataIntegrityFailureReportItem(entityId);
        for (CustomVariable customVariable : customVariables) {
            final String variableDataType = fieldNameDatatype.get(customVariable.getName());
            if (null != customVariable.getField()) {
                if (variableDataType.equalsIgnoreCase("INTEGER")) {
                    //Legacy Custom Variable Accepts float into integer values
                    try {
                        Integer.parseInt(customVariable.getField());
                    } catch (NumberFormatException e) {
                        dataIntegrityFailureReportItem.addDataIntegrityItem(customVariable.getName(), variableDataType, customVariable.getField());
                    }
                } else if (variableDataType.equalsIgnoreCase("FLOAT")) {
                    try {
                        Double.parseDouble(customVariable.getField());
                    } catch (NumberFormatException e) {
                        dataIntegrityFailureReportItem.addDataIntegrityItem(customVariable.getName(), variableDataType, customVariable.getField());
                    }
                } else if (variableDataType.equalsIgnoreCase("BOOLEAN")) {
                    //Null value will be set to false
                    if (!customVariable.getField().equalsIgnoreCase("TRUE") && !customVariable.getField().equalsIgnoreCase("FALSE")) {
                        dataIntegrityFailureReportItem.addDataIntegrityItem(customVariable.getName(), variableDataType, customVariable.getField());
                    }
                } else if (variableDataType.equalsIgnoreCase("DATE")) { //date
                    //Date format expected in legacy custom variable is  MM/DD/YYYY
                    String regex = "^(1[0-2]|0[1-9])/(3[01]|[12][0-9]|0[1-9])/[0-9]{4}$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(customVariable.getField());
                    boolean bool = matcher.matches();
                    if (!bool) {
                        dataIntegrityFailureReportItem.addDataIntegrityItem(customVariable.getName(), variableDataType, customVariable.getField());
                    }
                }
            }
        }
        return dataIntegrityFailureReportItem;
    }

    public String getProjectId() {
        return projectId;
    }

    public List<String> getDistinctEntityIds() {
        return distinctEntityIds;
    }

    public List<String> getFieldNames() {
        return fieldNames;
    }


    public List<FieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }
        private List<String> distinctEntityIds;
        private List<FieldDefinition> fieldDefinitions;
        private List<String> fieldNames;
        private Hashtable<String, String> fieldNameDatatype;

        private final JdbcTemplate template;
        private final String dataType;
        private final String projectId;
        private  final XnatFielddefinitiongroupI fieldDefinitiongroup;


}
