package org.nrg.xnat.customforms.pojo.formio;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class FormAppliesToPoJo implements Serializable {

    private static final long serialVersionUID = 1L;
    private String entityId;
    private String idCustomVariableFormAppliesTo;
    private String status;

    public FormAppliesToPoJo(@NotNull final CustomVariableFormAppliesTo formAppliesTo, final String status) {
        CustomVariableAppliesTo appliesTo = formAppliesTo.getCustomVariableAppliesTo();
        this.entityId = appliesTo.getEntityId();
        this.idCustomVariableFormAppliesTo = RowIdentifier.Marshall(formAppliesTo.getRowIdentifier());
        this.status = status;
    }

}
