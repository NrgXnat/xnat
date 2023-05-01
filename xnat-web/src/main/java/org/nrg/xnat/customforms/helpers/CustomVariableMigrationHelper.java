package org.nrg.xnat.customforms.helpers;

import org.nrg.xnat.customforms.pojo.CollatedLegacyCustomVariable;
import org.nrg.xnat.customforms.pojo.LegacyCustomVariable;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class CustomVariableMigrationHelper {

    public CustomVariableMigrationHelper(final JdbcTemplate template) {
        this.template = template;
    }

    public List<LegacyCustomVariable> doQuery(final String queryStr) {
        List<LegacyCustomVariable> legacyCustomVariables =  template.query(queryStr,
                (rs, rowNum) ->
                        new LegacyCustomVariable(
                                rs.getInt("xnat_fielddefinitiongroup_id"),
                                rs.getString("id"),
                                rs.getString("description"),
                                rs.getString("data_type"),
                                rs.getString("xnat_projectdata_id"),
                                rs.getInt("shareable"),
                                rs.getInt("project_specific")
                        )
        );
        return legacyCustomVariables;
    }

    public List<CollatedLegacyCustomVariable> collate(List<LegacyCustomVariable> legacyCustomVariables) {
        List<CollatedLegacyCustomVariable> collatedLegacyCustomVariables = new ArrayList<CollatedLegacyCustomVariable>();
        Map<Integer, List<String>> fieldDefinitionsAppliesToProjects = legacyCustomVariables.stream().collect(
                groupingBy(LegacyCustomVariable::getFieldDefinitionGroupId,
                        mapping(LegacyCustomVariable::getProjectId, toList())));
        Set<Integer> fieldDefinitionGroupIds = fieldDefinitionsAppliesToProjects.keySet();
        for (Integer id: fieldDefinitionGroupIds) {
            LegacyCustomVariable l  = getByFieldDefinitionGroupId(id, legacyCustomVariables);
            if (l != null) {
                CollatedLegacyCustomVariable collatedLegacyCustomVariable = getBasicInfo(l);
                collatedLegacyCustomVariable.setProjectIds(fieldDefinitionsAppliesToProjects.get(id));
                collatedLegacyCustomVariables.add(collatedLegacyCustomVariable);
            }
        }
        return collatedLegacyCustomVariables;
    }

    @Nullable
    private LegacyCustomVariable getByFieldDefinitionGroupId(int fieldDefinitionGroupId, final List<LegacyCustomVariable> legacyCustomVariables ) {
        if (legacyCustomVariables == null ) {
            return null;
        }
        LegacyCustomVariable found = null;
        for (LegacyCustomVariable l: legacyCustomVariables) {
            if (l.getFieldDefinitionGroupId() == fieldDefinitionGroupId) {
                found = l;
                break;
            }
        }
        return found;
    }

    private CollatedLegacyCustomVariable getBasicInfo(LegacyCustomVariable legacyCustomVariable) {
        CollatedLegacyCustomVariable collatedLegacyCustomVariable = new CollatedLegacyCustomVariable(legacyCustomVariable);
        return  collatedLegacyCustomVariable;
    }




    private final JdbcTemplate template;


}
