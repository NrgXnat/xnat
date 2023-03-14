package org.nrg.xnat.customforms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.forms.models.pojo.FormFieldPojo;
import org.nrg.xdat.forms.services.FormIOJsonService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xnat.customforms.helpers.CustomFormDisplayFieldHelper;
import org.nrg.xnat.customforms.helpers.CustomFormHelper;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.customforms.service.FormDisplayFieldService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.customforms.utils.TypeConversionUtils;
import org.nrg.xnat.entities.CustomVariableForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class FormDisplayFieldServiceImpl implements FormDisplayFieldService {

    @Autowired
    public FormDisplayFieldServiceImpl(final FormIOJsonService formIOJsonService,
                                       final CustomVariableFormService formService) {
        this.displayManager = DisplayManager.GetInstance();
        this.formIOJsonService = formIOJsonService;
        this.displayHelper = new CustomFormDisplayFieldHelper();
        this.formService = formService;
    }

    /**
     * Navigate over all schema-elements, look for all forms associated with the xsiType
     * which are in enabled state. For each of these, generate the in-memory display fields.
     */
    public synchronized  void refreshDisplayFields() {
        getSchemaElements().forEach(schemaElement -> {
            final String dataType = schemaElement.getFullXMLName();
            formIOJsonService.getFormsForObject(dataType, null, null, null, null)
                    .forEach(field -> addDisplayField(schemaElement, field));
        });
    }

    /**
     * Reload the display fields for the form identified by the UUID string formUUID associated with the dataType.
     * All form related display fields begin with (in uppercase)
     * dataType _ CUSTOM-FORM _ formUUID _
     * Existing ones, searched by the ID pattern are removed and new ones are generated
     *
     * @see org.nrg.xnat.customforms.helpers.CustomFormDisplayFieldHelper
     *
     * @param dataType - the xsiType to which the form is associated
     * @param formUUID - the form UUID
     */
    public void reloadDisplayFieldsForForm(final String dataType,  final String formUUID, final boolean deleteExistingFormDisplayFields) {
        synchronized (this) {
            getSchemaElements().forEach(schemaElement -> {
                if (schemaElement.getFullXMLName().equals(dataType)) {
                    resetDisplayField(schemaElement, formUUID, deleteExistingFormDisplayFields);
                }
            });
        }
    }

    /**
     * Removes all display fields for a form identified by the formUUID associated with a datatype
     * @param dataType - the xsiType
     * @param formUUID - the form UUID
     */
    public void removeDisplayFieldsForForm(final String dataType,  final String formUUID) {
        synchronized (this) {
            getSchemaElements().forEach(schemaElement -> {
                if (schemaElement.getFullXMLName().equals(dataType)) {
                    removeDisplayFieldsThatBeginWith(schemaElement, formUUID);
                }
            });
        }
    }

    private Stream<SchemaElement> getSchemaElements() {
        final List<ElementSecurity> secureElements;
        try {
            secureElements = ElementSecurity.GetSecureElements();
        } catch (Exception e){
            log.error("Could not obtain Secure Elements", e);
            return Stream.empty();
        }
        return secureElements.stream()
                .map(this::getSchemaElement)
                .filter(Objects::nonNull);
    }

    private SchemaElement getSchemaElement(ElementSecurity elementSecurity){
        try {
            return elementSecurity.getSchemaElement();
        } catch (Exception e) {
            log.warn("Could not retrieve schema element for \"{}\"", elementSecurity);
            return null;
        }
    }

    private void resetDisplayField(final SchemaElement schemaElement, final String formUUID, final boolean deleteExistingFormDisplayFields) {
        CustomVariableForm form = formService.findByUuid(UUID.fromString(formUUID));
        if (form == null) {
            return;
        }
        if (deleteExistingFormDisplayFields) {
            removeDisplayFieldsThatBeginWith(schemaElement, formUUID);
        }
        CustomFormHelper.getFormObjects(form, true)
                .forEach(f -> addDisplayField(schemaElement, f));
    }

    private void removeDisplayFieldsThatBeginWith(final SchemaElement schemaElement, final String formUUID) {
        final String dataType = schemaElement.getFullXMLName();
        final String fieldIdRoot  = displayHelper.getFieldIdRoot(dataType, formUUID);
        final ElementDisplay elementDisplay  = schemaElement.getDisplay();
        Hashtable displayItemHash  = elementDisplay.getDisplayFieldHash();
        if (displayItemHash == null) {
            return;
        }
        final Collection<String> formFieldIds = (Collection<String>) displayItemHash.keySet().stream().filter(id -> ((String)id).startsWith(fieldIdRoot)).collect(Collectors.toSet());
        formFieldIds.forEach(f -> removeDisplayField(schemaElement, f));
    }


    private void removeDisplayField(final SchemaElement schemaElement, final String fieldId) {
       synchronized (this) {
           try {
               final ElementDisplay elementDisplay  = schemaElement.getDisplay();
               elementDisplay.removeDisplayField(fieldId);
               displayManager.addElement(elementDisplay);
           } catch (Exception e) {
              log.error("Could not remove display field " + fieldId, e);
           }
       }
    }

    private void addDisplayField(final SchemaElement schemaElement,  final FormFieldPojo field) {
        final String dataType = schemaElement.getFullXMLName();
        final String displayFieldId = displayHelper.getCleanFieldId(dataType, field);
        if (!schemaElement.hasDisplayField(displayFieldId)) {
            final ElementDisplay elementDisplay = schemaElement.getDisplay();
            final DisplayFieldElement element = new DisplayFieldElement();
            final DisplayField displayField = new DisplayField(elementDisplay);
            element.setSchemaElementName(dataType + "." + CUSTOM_FIELDS_COLUMN_NAME);
            element.setName("Field1");
            displayField.addDisplayFieldElement(element);
            displayField.setSearchable(true);
            final String formioType = field.getType();
            String type = TypeConversionUtils.mapFormioTypeToXnatType(formioType);
            displayField.setDataType(type);
            displayField.setDescription("Custom Field: " + field.getLabel());
            displayField.setId(displayFieldId);
            String fieldSql = displayHelper.buildSql("@Field1", field);
            if (!type.equalsIgnoreCase(CustomFormsConstants.DEFAULT_XNAT_TYPE)) {
                fieldSql = "CAST (" + fieldSql + " AS " + type + ") ";
            }
            displayField.setContent(Collections.singletonMap("sql", fieldSql));
            displayField.setHeader(displayHelper.getCleanFieldHeader(field));
            elementDisplay.setAllowReplacement(true);
            elementDisplay.addDisplayField(displayField);
            elementDisplay.setAllowReplacement(false);
            schemaElement.setElementDisplay(elementDisplay);
            displayManager.addElement(elementDisplay);
        }
    }


    private final DisplayManager displayManager;
    private final CustomVariableFormService formService;
    private final FormIOJsonService formIOJsonService;
    private final CustomFormDisplayFieldHelper displayHelper;
    private final static String CUSTOM_FIELDS_COLUMN_NAME = "custom_fields";
}
