package org.nrg.xnat.customforms.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter

public class CollatedLegacyCustomVariable {


    public CollatedLegacyCustomVariable (LegacyCustomVariable legacyCustomVariable) {
        this.fieldDefinitionGroupId = legacyCustomVariable.getFieldDefinitionGroupId();
        this.id = legacyCustomVariable.getId();
        this.description = legacyCustomVariable.getDescription();
        this.dataType = legacyCustomVariable.getDataType();
        this.shareable = legacyCustomVariable.getShareable();
        this.project_specific = legacyCustomVariable.getProject_specific();

    }
    public CollatedLegacyCustomVariable(int fId, String id, String description, String dataType, String projectId, int shareable, int project_specific) {
        this.fieldDefinitionGroupId = fId;
        this.id = id;
        this.description = description;
        this.dataType = dataType;
        this.projectIds.add(projectId);
        this.shareable = shareable;
        this.project_specific = project_specific;
    }


    int fieldDefinitionGroupId;
    String id;
    String description;
    String dataType;
    List<String> projectIds = new ArrayList<String>();
    int shareable;
    int project_specific;
}

