/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.customforms.pojo.FormIOJsonToXnatCustomField;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableForm;

import java.util.*;

import static org.nrg.xnat.customforms.utils.CustomFormsConstants.NON_SEARCHABLE_FORMIO_TYPES;

/**
 * A Helper class to manage the Custom Form JSONs
 */

@Slf4j
public class CustomFormHelper {

    public static List<FormIOJsonToXnatCustomField> GetFormObj(CustomVariableForm form, final boolean skipNonSearchable) {
        if (null == form) {
            return Collections.emptyList();
        }
        final UUID formUUID = form.getFormUuid();
        final JsonNode rootComponent = form.getFormIOJsonDefinition().get(CustomFormsConstants.COMPONENTS_KEY);
        //This is the root component. Underneath which lies a container with key formUUID.
        //This container has all the form elements
        try {
            final JsonNode formComponents = rootComponent.get(0).get(CustomFormsConstants.COMPONENTS_KEY);
            return GetFormObj(formUUID, formComponents, skipNonSearchable);
        } catch(Exception e) {
            log.error("Could not extract form components", e);
        }
        return Collections.emptyList();
    }

    public static List<FormIOJsonToXnatCustomField> GetFormObj(final UUID formUUID, final JsonNode components, final boolean skipNonSearchable) {
        if (components == null || !components.isArray()) {
            return Collections.emptyList();
        }
        final List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields = new ArrayList<>();
        components.forEach(component -> getFormIOJsonToXnatCustomField(formUUID, component, formIOJsonToXnatCustomFields, skipNonSearchable));
        return formIOJsonToXnatCustomFields;
    }

    private static void getFormIOJsonToXnatCustomField(final UUID formUUID, final JsonNode compNode, final List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields,  final boolean skipNonSearchable) {
        final String formType = compNode.get(CustomFormsConstants.COMPONENTS_TYPE_FIELD).asText();
        if (skipNonSearchable && NON_SEARCHABLE_FORMIO_TYPES.contains(formType)) {
            return;
        }
        traverse(formUUID, compNode, formIOJsonToXnatCustomFields, skipNonSearchable, new ArrayList<>());
    }

    /**
     * Recursively traverse the FormIO JSON structure to find input components and skip layout components
     * Based off of FormIO Utils eachComponent method:
     * https://github.com/formio/formio-utils/blob/master/src/index.js
     *
     * @param components                   - FormIO components JSON
     * @param formIOJsonToXnatCustomFields - Converted input components are added to this list
     */
    private static void traverse(final UUID formUUID, JsonNode components, List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields, final boolean skipNonSearchable, List<String> parentJsonPath) {
        try {
            final String formType = components.get(CustomFormsConstants.COMPONENTS_TYPE_FIELD).asText();
            if (components.has(CustomFormsConstants.COMPONENTS_INPUT_FIELD) && components.get(CustomFormsConstants.COMPONENTS_INPUT_FIELD).asBoolean()) {
                if (skipNonSearchable && NON_SEARCHABLE_FORMIO_TYPES.contains(formType)) {
                    return;
                }
            }
        } catch(Exception ignored) {}
        boolean isArray = components.isArray();
        boolean hasColumns = components.has(CustomFormsConstants.COMPONENTS_COLUMNS_TYPE) && components.get(CustomFormsConstants.COMPONENTS_COLUMNS_TYPE).isArray();
        boolean hasRows = components.has(CustomFormsConstants.COMPONENTS_ROWS_TYPE) && components.get(CustomFormsConstants.COMPONENTS_ROWS_TYPE).isArray();
        boolean hasComponents = components.has(CustomFormsConstants.COMPONENTS_KEY) && components.get(CustomFormsConstants.COMPONENTS_KEY).isArray();

        if (!isArray && !hasColumns && !hasRows && !hasComponents) {
            FormIOJsonToXnatCustomField f = getFormsIOJsonToXnatCustomField(formUUID, components, parentJsonPath);
            if (null != f) {
                formIOJsonToXnatCustomFields.add(f);
            }
            return;
        }

        if (hasColumns) {
            components.get(CustomFormsConstants.COMPONENTS_COLUMNS_TYPE).forEach(column -> traverse(formUUID, column.get(CustomFormsConstants.COMPONENTS_KEY), formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPath));
        }

        if (hasRows) {
            components.get(CustomFormsConstants.COMPONENTS_ROWS_TYPE).forEach(row -> row.forEach(rowComponent -> traverse(formUUID, rowComponent.get(CustomFormsConstants.COMPONENTS_KEY), formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPath)));
        }

        if (hasComponents) {
            final String type = components.get(CustomFormsConstants.COMPONENTS_TYPE_FIELD).asText();
            final String key = components.get(CustomFormsConstants.COMPONENTS_KEY_FIELD).asText();
            List<String> jsonPath = new ArrayList<>();
            if (type.equalsIgnoreCase(CustomFormsConstants.CONTAINER_KEY)) {
                if (!parentJsonPath.isEmpty()) {
                    jsonPath.addAll(parentJsonPath);
                }
                jsonPath.add(key);
            }
            traverse(formUUID, components.get(CustomFormsConstants.COMPONENTS_KEY), formIOJsonToXnatCustomFields,  skipNonSearchable, jsonPath);
        }

        if (isArray) {
            components.forEach(component -> traverse(formUUID, component, formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPath));
        }
    }

    private static FormIOJsonToXnatCustomField getFormsIOJsonToXnatCustomField(final UUID formUUID, final JsonNode compNode, List<String> parentPath) {
        FormIOJsonToXnatCustomField formIOJsonToXnatCustomField = null;
        if (compNode.has(CustomFormsConstants.COMPONENTS_INPUT_FIELD) && compNode.get(CustomFormsConstants.COMPONENTS_INPUT_FIELD).asBoolean()) {
                String key = compNode.get(CustomFormsConstants.COMPONENTS_KEY_FIELD).asText();
                String label = compNode.get(CustomFormsConstants.LABEL_KEY).asText();
                String type = compNode.get(CustomFormsConstants.COMPONENTS_TYPE_FIELD).asText();
                formIOJsonToXnatCustomField = new FormIOJsonToXnatCustomField(formUUID, label, key, key, type, parentPath);
        }
        return formIOJsonToXnatCustomField;
    }



}
