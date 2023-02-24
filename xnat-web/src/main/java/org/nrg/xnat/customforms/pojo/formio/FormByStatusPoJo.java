package org.nrg.xnat.customforms.pojo.formio;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;

import java.util.Objects;

@Getter
@Setter

public class FormByStatusPoJo {
    private long formId;
    private String formUUID;
    private String status;
    private Integer zIndex;


    public FormByStatusPoJo(final CustomVariableFormAppliesTo c) {
        this.formId = c.getCustomVariableForm().getId();
        this.formUUID = c.getCustomVariableForm().getFormUuid().toString();
        this.status = c.getStatus();
        this.zIndex = c.getCustomVariableForm().getzIndex();
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FormByStatusPoJo that = (FormByStatusPoJo) o;
        return (formId == that.formId && formUUID == that.formUUID && compareProperties(status, that.status));
    }

    private boolean compareProperties(final String first, final String second) {
        // If they're both not null, we can just compare the times.
        if (ObjectUtils.allNotNull(first, second)) {
            return first.equals(second);
        }
        // If they're not both null, then they're not equal.
        return !ObjectUtils.anyNotNull(first, second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormId(), getFormUUID(), getStatus());
    }


}
