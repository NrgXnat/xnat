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
import org.nrg.xnat.entities.CustomVariableForm;

import java.util.*;

import static org.nrg.xnat.customforms.utils.CustomFormsConstants.*;

/**
 * A Helper class to manage the Custom Form JSONs
 */

@Slf4j
public class CustomFormHelper {

    public static List<FormIOJsonToXnatCustomField> getFormObjects(CustomVariableForm form, final boolean skipNonSearchable) {
        if (null == form) {
            return Collections.emptyList();
        }
        final UUID formUUID = form.getFormUuid();
        // This is the root component, underneath which lies all the form elements
        final JsonNode rootComponent = form.getFormIOJsonDefinition().get(COMPONENTS_KEY);
        try {
            return getFormObjects(formUUID, rootComponent, skipNonSearchable);
        } catch(Exception e) {
            log.error("Could not extract form components", e);
        }
        return Collections.emptyList();
    }

    public static List<FormIOJsonToXnatCustomField> getFormObjects(final UUID formUUID, final JsonNode components, final boolean skipNonSearchable) {
        if (null == components || !components.isArray()) {
            return Collections.emptyList();
        }
        final List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields = new ArrayList<>();
        components.forEach(component -> getFormIOJsonToXnatCustomField(formUUID, component, formIOJsonToXnatCustomFields, skipNonSearchable));
        return formIOJsonToXnatCustomFields;
    }

    private static void getFormIOJsonToXnatCustomField(final UUID formUUID, final JsonNode compNode, final List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields,  final boolean skipNonSearchable) {
        if (compNode.has(COMPONENTS_TYPE_FIELD)
            && skipNonSearchable
            && NON_SEARCHABLE_FORMIO_TYPES.contains(compNode.get(COMPONENTS_TYPE_FIELD).asText())) {
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
    private static void traverse(final UUID formUUID, JsonNode components, List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields, final boolean skipNonSearchable, List<String> parentJsonPaths) {
        if (components.has(COMPONENTS_TYPE_FIELD)
                && components.has(COMPONENTS_INPUT_FIELD)
                && components.get(COMPONENTS_INPUT_FIELD).asBoolean()
                && skipNonSearchable
                && NON_SEARCHABLE_FORMIO_TYPES.contains(components.get(COMPONENTS_TYPE_FIELD).asText())) {
                return;
        }
        boolean isArray = components.isArray();
        boolean hasColumns = components.has(COMPONENTS_COLUMNS_TYPE) && components.get(COMPONENTS_COLUMNS_TYPE).isArray();
        boolean hasRows = components.has(COMPONENTS_ROWS_TYPE) && components.get(COMPONENTS_ROWS_TYPE).isArray();
        boolean hasComponents = components.has(COMPONENTS_KEY) && components.get(COMPONENTS_KEY).isArray();

        if (!isArray && !hasColumns && !hasRows && !hasComponents) {
            FormIOJsonToXnatCustomField f = getFormsIOJsonToXnatCustomField(formUUID, components, parentJsonPaths);
            if (null != f) {
                formIOJsonToXnatCustomFields.add(f);
            }
            return;
        }

        if (hasColumns) {
            components.get(COMPONENTS_COLUMNS_TYPE).forEach(column -> traverse(formUUID, column.get(COMPONENTS_KEY), formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPaths));
        }

        if (hasRows) {
            components.get(COMPONENTS_ROWS_TYPE).forEach(row -> row.forEach(rowComponent -> traverse(formUUID, rowComponent.get(COMPONENTS_KEY), formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPaths)));
        }

        if (hasComponents) {
            List<String> jsonPaths = new ArrayList<>();
            try {
                final String type = components.get(COMPONENTS_TYPE_FIELD).asText();
                final String key = components.get(COMPONENTS_KEY_FIELD).asText();
                if (type.equalsIgnoreCase(CONTAINER_KEY)) {
                    if (!parentJsonPaths.isEmpty()) {
                        jsonPaths.addAll(parentJsonPaths);
                    }
                    jsonPaths.add(key);
                }
            } catch(Exception ignored) {}
            traverse(formUUID, components.get(COMPONENTS_KEY), formIOJsonToXnatCustomFields,  skipNonSearchable, jsonPaths);
        }

        if (isArray) {
            components.forEach(component -> traverse(formUUID, component, formIOJsonToXnatCustomFields, skipNonSearchable, parentJsonPaths));
        }
    }

    private static FormIOJsonToXnatCustomField getFormsIOJsonToXnatCustomField(final UUID formUUID, final JsonNode compNode, List<String> parentPaths) {
        FormIOJsonToXnatCustomField formIOJsonToXnatCustomField = null;
        if (compNode.has(COMPONENTS_INPUT_FIELD) && compNode.get(COMPONENTS_INPUT_FIELD).asBoolean()) {
                String key = compNode.get(COMPONENTS_KEY_FIELD).asText();
                String label = compNode.get(LABEL_KEY).asText();
                String type = compNode.get(COMPONENTS_TYPE_FIELD).asText();
                formIOJsonToXnatCustomField = new FormIOJsonToXnatCustomField(formUUID, label, key, key, type, parentPaths);
        }
        return formIOJsonToXnatCustomField;
    }



}
