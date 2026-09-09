package org.nrg.dcm.xnat.entities;

import org.nrg.dcm.xnat.pojos.DicomMapping;
import org.nrg.dcm.xnat.pojos.FieldType;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"schemaElement", "dicomTag", "scope", "scopeObjectId"}))
public class DicomMappingEntity extends AbstractHibernateEntity {
    private static final long serialVersionUID = 7055450186733417335L;

    @Nonnull private Scope scope = Scope.Site;
    @Nullable private String scopeObjectId;
    @Nonnull private String fieldName;
    @Nonnull private FieldType fieldType;
    @Nonnull private String dicomTag;
    @Nonnull private String schemaElement;
    private boolean addParamAttribute = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Nonnull
    public Scope getScope() {
        return scope;
    }

    public void setScope(@Nonnull Scope scope) {
        this.scope = scope;
    }

    @Nullable
    public String getScopeObjectId() {
        return scopeObjectId;
    }

    public void setScopeObjectId(@Nullable String scopeObjectId) {
        this.scopeObjectId = scopeObjectId;
    }

    @Nonnull
    @Column(nullable = false)
    public String getSchemaElement() {
        return schemaElement;
    }

    public void setSchemaElement(@Nonnull String schemaElement) {
        this.schemaElement = schemaElement;
    }

    @Nonnull
    @Column(nullable = false)
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(@Nonnull String fieldName) {
        this.fieldName = fieldName;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Nonnull
    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(@Nonnull FieldType fieldType) {
        this.fieldType = fieldType;
    }

    @Nonnull
    public String getDicomTag() {
        return dicomTag;
    }

    public void setDicomTag(@Nonnull String dicomTag) {
        this.dicomTag = dicomTag;
    }

    @Column(columnDefinition = "boolean default true")
    public boolean isAddParamAttribute() {
        return addParamAttribute;
    }

    public void setAddParamAttribute(boolean addParamAttribute) {
        this.addParamAttribute = addParamAttribute;
    }

    @Override
    public String toString() {
        return "DICOMAddParamMappingEntity{" +
                "scope=" + scope +
                ", scopeObjectId='" + scopeObjectId + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", fieldType=" + fieldType +
                ", dicomTag=" + dicomTag +
                ", schemaElement='" + schemaElement + '\'' +
                '}';
    }

    public DicomMapping toPojo() {
        return new DicomMapping(getId(), scope, scopeObjectId, fieldName, fieldType, dicomTag, schemaElement);
    }
}
