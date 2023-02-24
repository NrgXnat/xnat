package org.nrg.xnat.customforms.helpers;

import org.nrg.xdat.forms.models.pojo.FormFieldPojo;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;

public class CustomFormDisplayFieldHelper {

    public String getCleanFieldId(final String dataType, final FormFieldPojo field){
        final String formUUID = field.getFormUUID().toString();
        final String fieldKey = field.getKey();
        return String.format("%s_%s",getFieldIdRoot(dataType, formUUID), fieldKey.replaceAll("[^A-Za-z0-9_]", "")).toUpperCase();
    }


    public String getFieldIdRoot(final String dataType) {
        return String.format("%s_%s_",dataType, CUSTOM_FORM);
    }

    public String getFieldIdRoot(final String dataType, final String formUUID) {
        return String.format("%s%s",getFieldIdRoot(dataType), formUUID);
    }

    public boolean isCustomFieldDisplayField(final String fieldId, final String dataType) {
        return fieldId.startsWith(getFieldIdRoot(dataType.toUpperCase()));
    }

    public String getCleanFieldHeader(final FormFieldPojo field){
        final String fieldKey = field.getFormUUID() + CustomFormsConstants.DOT_SEPARATOR + field.getKey();
        return fieldKey;
    }

    public String buildSql(final String column,  final FormFieldPojo field) {
        final String formUUID = field.getFormUUID().toString();
        final String fieldKey = field.getKey();
        return column + " -> '" + formUUID + "' ->> '" + fieldKey + "'";
    }

    private final String CUSTOM_FORM = "CUSTOM-FORM";

}
