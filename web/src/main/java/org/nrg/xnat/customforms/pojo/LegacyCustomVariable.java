package org.nrg.xnat.customforms.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LegacyCustomVariable {

    public LegacyCustomVariable(int fId, String id, String description, String dataType, String projectId, int shareable, int project_specific) {
        this.fieldDefinitionGroupId = fId;
        this.id = id;
        this.description = description;
        this.dataType = dataType;
        this.projectId = projectId;
        this.shareable = shareable;
        this.project_specific = project_specific;
    }


    int fieldDefinitionGroupId;
    String id;
    String description;
    String dataType;
    String projectId;
    int shareable;
    int project_specific;
}
