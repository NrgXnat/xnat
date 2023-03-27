package org.nrg.xnat.entities;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import javax.persistence.*;
import java.util.*;

import org.hibernate.annotations.Type;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"formUuid"})})
@NoArgsConstructor
@AllArgsConstructor
public class CustomVariableForm extends AbstractHibernateEntity{

    private static final long serialVersionUID = -1264374836830855705L;

    private JsonNode formIOJsonDefinition;
    private int zIndex = -1;
    private String formCreator;

    @Type(type="pg-uuid")
    @Column(nullable=false)
    private java.util.UUID formUuid;


    private List<CustomVariableFormAppliesTo> customVariableFormAppliesTos = new ArrayList<CustomVariableFormAppliesTo>(0);


    @Type(type="pg-uuid")
    public UUID getFormUuid() {return formUuid;}
    public void setFormUuid(UUID fId) { formUuid = fId;}

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    public JsonNode getFormIOJsonDefinition() {
        return formIOJsonDefinition;
    }

    public void setFormIOJsonDefinition(JsonNode def) {
        formIOJsonDefinition = def;
    }

    @OneToMany(mappedBy = "customVariableForm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    public List<CustomVariableFormAppliesTo> getCustomVariableFormAppliesTos() {
        return customVariableFormAppliesTos;
    }

    public void setCustomVariableFormAppliesTos(List<CustomVariableFormAppliesTo> f) {
        customVariableFormAppliesTos = f;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public String getFormCreator() {
        return formCreator;
    }

    public void setFormCreator(String formCreator) {
        this.formCreator = formCreator;
    }

    public String title() {
       String myTitle = "";
       try {
           myTitle = formIOJsonDefinition.get(CustomFormsConstants.TITLE_KEY).asText();
       }catch(Exception e) {}
       return myTitle;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        boolean eq = super.equals(o);
        final CustomVariableForm that = (CustomVariableForm) o;
        if (eq) {
            return this.formUuid.equals(that.getFormUuid());
        }
        return eq;
    }

    @Override
    public int hashCode() {
       return formUuid.hashCode();
    }

    @Override
    public String toString() {
        //Consistent with String.toString() on null
        String str = "null";
        if (getFormIOJsonDefinition() != null) {
            str = getFormIOJsonDefinition().toString();
        }
        return str;
    }
}
