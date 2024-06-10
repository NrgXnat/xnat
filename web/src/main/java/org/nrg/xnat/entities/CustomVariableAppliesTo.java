package org.nrg.xnat.entities;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import javax.validation.constraints.NotNull;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.*;

import java.util.*;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.xapi.model.users.User;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;

@Slf4j
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints =
        @UniqueConstraint(columnNames = {"scope", "dataType","entityId","visit","protocol", "subtype"}))
public class CustomVariableAppliesTo extends AbstractHibernateEntity {

    private static final long serialVersionUID = -1264374836830855705L;

    @Nonnull
    private Scope scope;


    private String entityId;

    @Nonnull
    private String dataType;

    private String visit;

    private String protocol;

    private String subType;

    //For Future use
    private String scanType = null;

    private List<CustomVariableFormAppliesTo> customVariableFormAppliesTos =  new ArrayList<>(0);


    public @NotNull Scope getScope() {
        return scope;
    }

    public void setScope(@NotNull Scope scope) {
        this.scope = scope;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public @NotNull String getDataType() {
        return dataType;
    }

    public void setDataType(@NotNull String dataType) {
        this.dataType = dataType;
    }

    public String getVisit() {
        return visit;
    }

    public void setVisit(String visit) {
        this.visit = visit;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subtype) {
        this.subType = subtype;
    }

    @Nullable
    public String getScanType() {
        return scanType;
    }

    public void setScanType(String scanType) {
        this.scanType = scanType;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "customVariableAppliesTo", cascade=CascadeType.ALL)
    public List<CustomVariableFormAppliesTo> getCustomVariableFormAppliesTos() {
        return this.customVariableFormAppliesTos;
    }

    public void setCustomVariableFormAppliesTos(List<CustomVariableFormAppliesTo> forms) {
        this.customVariableFormAppliesTos = forms;
    }


    public String pathAsString() {
        String path = "datatype/" + dataType;
        if (protocol != null) {
            path += "/protoocol/" + protocol;
            if (visit != null) {
                path += "/visit/" + visit;
            }
            if (subType != null) {
                path += "/subtype/" + subType;
            }
            if (scanType != null) {
                path += "/scanType/" + scanType;
            }
        }

        return path;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustomVariableAppliesTo that = (CustomVariableAppliesTo) o;
        boolean eq = super.equals(o);
        if (eq) {
            return getDataType().equals(that.getDataType()) &&
                   getScope().equals(that.getScope()) &&
                    compareProperties(getEntityId(), that.getEntityId()) &&
                            compareProperties(getProtocol(),that.getProtocol()) &&
                            compareProperties(getVisit(),that.getVisit()) &&
                            compareProperties(getSubType(),that.getSubType());


        }
        return eq;
    }

    private   boolean compareProperties(final String first, final String second) {
        // If they're both not null, we can just compare the times.
        if (ObjectUtils.allNotNull(first, second)) {
            return first.equals(second);
        }
        // If they're not both null, then they're not equal.
        return !ObjectUtils.anyNotNull(first, second);
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDataType(), getScope(),getEntityId(), getCreated(), getTimestamp());
    }

    @Override
    public String toString() {
        if (this == null) {
            //Consistent with String.toString() on null
            return "null";
        }
        return pathAsString();
    }



}
