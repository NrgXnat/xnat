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

/**
 * A Helper class to manage the Custom Form JSONs
 */

@Slf4j
public class CustomFormHelper {

    public static List<FormIOJsonToXnatCustomField> GetFormObj(CustomVariableForm form) {
        if (null == form) {
            return Collections.emptyList();
        }
        final JsonNode components = form.getFormIOJsonDefinition().get("components");
        final UUID formUUID = form.getFormUuid();
        return GetFormObj(formUUID, components);
    }

    public static List<FormIOJsonToXnatCustomField> GetFormObj(final UUID formUUID, final JsonNode components) {
        // Convert the configuration into a new FormJson Pojo
        if (components == null || !components.isArray()) {
            return Collections.emptyList();
        }
        final List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields = new ArrayList<>();
        traverse(formUUID, components, formIOJsonToXnatCustomFields);
        return formIOJsonToXnatCustomFields;
    }


    /**
     * Recursively traverse the FormIO JSON structure to find input components and skip layout components
     * Based off of FormIO Utils eachComponent method:
     * https://github.com/formio/formio-utils/blob/master/src/index.js
     *
     * @param components                   - FormIO components JSON
     * @param formIOJsonToXnatCustomFields - Converted input components are added to this list
     */
    private static void traverse(final UUID formUUID, JsonNode components, List<FormIOJsonToXnatCustomField> formIOJsonToXnatCustomFields) {

        boolean isArray = components.isArray();
        boolean hasColumns = components.has("columns") && components.get("columns").isArray();
        boolean hasRows = components.has("rows") && components.get("rows").isArray();
        boolean hasComponents = components.has("components") && components.get("components").isArray();

        if (!isArray && !hasColumns && !hasRows && !hasComponents) {
            FormIOJsonToXnatCustomField f = getFormsIOJsonToXnatCustomField(formUUID, components);
            if (null != f) {
                formIOJsonToXnatCustomFields.add(f);
            }
            return;
        }

        if (hasColumns) {
            components.get("columns").forEach(column -> traverse(formUUID, column.get("components"), formIOJsonToXnatCustomFields));
        }

        if (hasRows) {
            components.get("rows").forEach(row -> {
                row.forEach(rowComponent -> traverse(formUUID, rowComponent.get("components"), formIOJsonToXnatCustomFields));
            });
        }

        if (hasComponents) {
            traverse(formUUID, components.get("components"), formIOJsonToXnatCustomFields);
        }

        if (isArray) {
            components.forEach(component -> traverse(formUUID, component, formIOJsonToXnatCustomFields));
        }
    }

    private static FormIOJsonToXnatCustomField getFormsIOJsonToXnatCustomField(final UUID formUUID, JsonNode compNode) {
        FormIOJsonToXnatCustomField formIOJsonToXnatCustomField = null;
        if (compNode.has("input")) {
            boolean isInput = compNode.get("input").asBoolean();
            if (isInput) {
                String key = compNode.get("key").asText();
                String label = compNode.get("label").asText();
                String type = compNode.get("type").asText();
                String fieldName = key;
                formIOJsonToXnatCustomField = new FormIOJsonToXnatCustomField(formUUID, label, key, fieldName, type);
            }
        }
        return formIOJsonToXnatCustomField;
    }


}
