package org.nrg.xnat.entities;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

public class CustomVariableFormAppliesToId implements java.io.Serializable {

    private static final long serialVersionUID = -1264374836830855705L;

    private CustomVariableAppliesTo customVariableAppliesTo;
    private CustomVariableForm customVariableForm;

    public CustomVariableAppliesTo getCustomVariableAppliesTo() {
        return customVariableAppliesTo;
    }

    public void setCustomVariableAppliesTo(CustomVariableAppliesTo customVariableAppliesTo) {
        this.customVariableAppliesTo = customVariableAppliesTo;
    }

    public CustomVariableForm getCustomVariableForm() {
        return customVariableForm;
    }

    public void setCustomVariableForm(CustomVariableForm customVariableForm) {
        this.customVariableForm = customVariableForm;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
    CustomVariableFormAppliesToId other = (CustomVariableFormAppliesToId) obj;
        if (customVariableAppliesTo == null) {
            if (other.customVariableAppliesTo != null) {
                return false;
            }
        } else if (!customVariableAppliesTo.equals(other.customVariableAppliesTo)) {
            return false;
        }
        if (customVariableForm == null) {
            if (other.customVariableForm != null) {
                return false;
            }
        } else if (!customVariableForm.equals(other.customVariableForm)) {
            return false;
        }
        return true;
    }


    public int hashCode() {
        int result;
        result = (customVariableAppliesTo != null ? customVariableAppliesTo.hashCode() : 0);
        result = 31 * result + (customVariableForm != null ? customVariableForm.hashCode() : 0);
        return result;
    }

}
