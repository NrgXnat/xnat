package org.nrg.xnat.customforms.interfaces.impl;

import org.nrg.xdat.forms.models.pojo.FormFieldPojo;
import org.nrg.xdat.forms.services.FormIOJsonService;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.customforms.helpers.CustomFormDisplayFieldHelper;
import org.nrg.xnat.customforms.interfaces.CustomFormDisplayFieldsI;
import org.nrg.xnat.customforms.interfaces.annotations.CustomFormFetcherAnnotation;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.customforms.utils.TypeConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@CustomFormFetcherAnnotation(type = CustomFormsConstants.PROTOCOL_UNAWARE)
public class DefaultCustomFormDisplayFieldImpl implements CustomFormDisplayFieldsI {


    @Autowired
    public DefaultCustomFormDisplayFieldImpl(final FormIOJsonService jsonService) {
        this.jsonService = jsonService;
    }

    public void addDisplayFields(final SchemaElement se, final Object o, List<String> addedJsonFields, XFTTable fields) {
        List<FormFieldPojo> formFields =  jsonService.getFormsForObject(se.getFullXMLName(),(String)o, null, null, null);
        if (null == formFields || formFields.isEmpty()) {
            return;
        }
        CustomFormDisplayFieldHelper displayHelper = new CustomFormDisplayFieldHelper();
        for(FormFieldPojo formJsonPojo : formFields){
            final String   displayFieldId = displayHelper.getCleanFieldId(se.getFullXMLName(), formJsonPojo);
            if (addedJsonFields.contains(displayFieldId)) {
                continue;
            }
            final String fieldDisplayLabel = formJsonPojo.getLabel() +"["+formJsonPojo.getFormUUID()+"]";
            final String formioType = formJsonPojo.getType();
            String type = TypeConversionUtils.mapFormioTypeToXnatType(formioType);
            Object[] availableField = new Object[8];
            availableField[0] = displayFieldId; //Id
            availableField[1] = displayHelper.getCleanFieldHeader(formJsonPojo); //Header
            availableField[2] = fieldDisplayLabel; //Summary
            availableField[3] = type;
            availableField[4] = false;
            availableField[5] = fieldDisplayLabel; //Description
            availableField[6] = se.getFullXMLName();
            availableField[7] = 1;
            fields.insertRow(availableField);
            addedJsonFields.add(displayFieldId);
        }

    }

    private final  FormIOJsonService jsonService;
}
