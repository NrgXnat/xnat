package org.nrg.dcm.xnat.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.nrg.framework.constants.Scope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@JsonInclude
public class DicomMapping {
    long id;
    @Nonnull private Scope scope = Scope.Site;
    @Nullable private String scopeObjectId;
    @Nonnull private String fieldName;
    @Nonnull private FieldType fieldType;
    @Nonnull private String dicomTag;
    @Nonnull private String schemaElement;

    public DicomMapping(){}

    public DicomMapping(long id,
                        @Nonnull Scope scope,
                        @Nullable String scopeObjectId,
                        @Nonnull String fieldName,
                        @Nonnull FieldType fieldType,
                        @Nonnull String dicomTag,
                        @Nonnull String schemaElement) {
        this.id = id;
        this.scope = scope;
        this.scopeObjectId = scopeObjectId;
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.dicomTag = dicomTag;
        this.schemaElement = schemaElement;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(@Nonnull String fieldName) {
        this.fieldName = fieldName;
    }

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

    @Nonnull
    public String getSchemaElement() {
        return schemaElement;
    }

    public void setSchemaElement(@Nonnull String schemaElement) {
        this.schemaElement = schemaElement;
    }

}
