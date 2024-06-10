package org.nrg.xnat.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;


@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CustomVariableFormAppliesToId.class)
public class CustomVariableFormAppliesTo extends AbstractHibernateEntity  {

    private static final long serialVersionUID = -1264374836830855705L;

    private CustomVariableAppliesTo customVariableAppliesTo;

    private CustomVariableForm customVariableForm;

    @Id
    @ManyToOne(fetch=FetchType.LAZY)
    public CustomVariableAppliesTo getCustomVariableAppliesTo() {
        return customVariableAppliesTo;
    }

    public void setCustomVariableAppliesTo(CustomVariableAppliesTo customVariableAppliesTo) {
       this.customVariableAppliesTo = customVariableAppliesTo;
    }

    @Id
    @ManyToOne(fetch=FetchType.LAZY)
    public CustomVariableForm getCustomVariableForm() {
        return customVariableForm;
    }

    public void setCustomVariableForm(CustomVariableForm customVariableForm) {
        this.customVariableForm = customVariableForm;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustomVariableFormAppliesTo that = (CustomVariableFormAppliesTo) o;
        boolean eq = super.equals(o);
        if (eq) {
            return that.getCustomVariableAppliesTo().equals(getCustomVariableAppliesTo()) &&
                    that.getCustomVariableForm().equals(getCustomVariableForm()) &&
                    that.getXnatUser().equals(getXnatUser()) &&
                    that.getStatus().equals(getStatus());
        }
        return eq;
    }

    public int hashCode() {
       return  Objects.hash(getId(), getXnatUser(), getStatus(), getCustomVariableAppliesTo().hashCode(), getCustomVariableForm().hashCode());
    }

    @Override
    public String toString() {
        //Consistent with String.toString() on null
        String str = "null";
        if (getCustomVariableAppliesTo() != null && getCustomVariableForm() != null) {
            return getCustomVariableAppliesTo().toString() + " " + getCustomVariableForm().toString();
        }
        return str;
    }

    public String getXnatUser() {
        return xnatUser;
    }

    public void setXnatUser(String xnatUser) {
        this.xnatUser = xnatUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Transient
    public @NotNull RowIdentifier getRowIdentifier() {
        RowIdentifier id = new RowIdentifier();
        id.setAppliesToId((customVariableAppliesTo == null)? -1: customVariableAppliesTo.getId());
        id.setFormId((customVariableForm == null )?-1:customVariableForm.getId());
        return id;
    }

    @Transient
    public boolean doProjectsShareForm() {
        boolean isShared = false;
        if (getCustomVariableForm() != null && getCustomVariableAppliesTo() != null && getCustomVariableAppliesTo().getScope().equals(Scope.Project) && getCustomVariableForm().getCustomVariableFormAppliesTos().size() > 1) {
            isShared = true;
        }
        return isShared;
    }


    private String            xnatUser;
    private String            status;

}
