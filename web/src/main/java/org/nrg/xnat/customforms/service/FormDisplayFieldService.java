package org.nrg.xnat.customforms.service;


public interface FormDisplayFieldService {
    void refreshDisplayFields();
    void reloadDisplayFieldsForForm(final String dataType, final String formUUID, final boolean deleteExistingFormDisplayFields);
    void removeDisplayFieldsForForm(final String dataType, final String formUUID);
}
