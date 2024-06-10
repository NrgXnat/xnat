package org.nrg.xnat.customforms.service;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
public class ObjectSaverService {

    @Autowired
    public ObjectSaverService(final CustomVariableAppliesToService selectionService,
                              final CustomVariableFormService formService,
                              final CustomVariableFormAppliesToService customVariableFormAppliesToService) {
        this.appliesToService = selectionService;
        this.formService = formService;
        this.customVariableFormAppliesToService = customVariableFormAppliesToService;
    }

    @Transactional
    public boolean saveAll(final CustomVariableAppliesTo customVariableAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        boolean saved = false;
        try {
            appliesToService.saveOrUpdate(customVariableAppliesTo);
            formService.saveOrUpdate(form);
            CustomVariableFormAppliesTo customVariableFormAppliesTo = getCustomVariableFormAppliesTo(customVariableAppliesTo, form, user, status);
            //Relation Owner
            customVariableFormAppliesToService.saveOrUpdate(customVariableFormAppliesTo);
            saved = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
        }
        return saved;
    }

    public boolean saveOnlyFormAppliesTo(final CustomVariableFormAppliesTo customVariableFormAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        boolean saved = false;
        try {
            //Relation Owner
            customVariableFormAppliesTo.setCustomVariableForm(form);
            customVariableFormAppliesToService.saveOrUpdate(customVariableFormAppliesTo);
            saved = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
        }
        return saved;
    }

    public boolean saveOnlyFormAndAssign(final CustomVariableAppliesTo customVariableAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        boolean saved = false;
        try {
            formService.saveOrUpdate(form);
            //Is this a new association or the same form being updated?
            boolean existingRow = false;
            List<CustomVariableFormAppliesTo> customVariableFormAppliesTos = customVariableAppliesTo.getCustomVariableFormAppliesTos();
            if (customVariableFormAppliesTos != null && customVariableFormAppliesTos.size() > 0) {
                for (CustomVariableFormAppliesTo c : customVariableFormAppliesTos) {
                    if (c.getCustomVariableForm().getId() == form.getId() && c.getCustomVariableAppliesTo().getId() == customVariableAppliesTo.getId()) {
                        existingRow = true;
                        break;
                    }
                }
            }
            if (!existingRow) {
                CustomVariableFormAppliesTo customVariableFormAppliesTo = getCustomVariableFormAppliesTo(customVariableAppliesTo, form, user, status);
                //Relation Owner
                customVariableFormAppliesToService.saveOrUpdate(customVariableFormAppliesTo);
            }
            saved = true;

        } catch (Exception e) {
            log.debug("Could not save", e);
        }
        return saved;
    }

    public boolean saveOnlyAssign(final CustomVariableAppliesTo customVariableAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        boolean saved = false;
        try {
            //Is this a new association or the same form being updated?
            boolean existingRow = false;
            RowIdentifier rowId = new RowIdentifier();
            rowId.setFormId(form.getId());
            rowId.setAppliesToId(customVariableAppliesTo.getId());
            CustomVariableFormAppliesTo customVariableFormAppliesTos = customVariableFormAppliesToService.findByRowIdentifier(rowId);
            if (customVariableFormAppliesTos == null) {
                CustomVariableFormAppliesTo customVariableFormAppliesTo = getCustomVariableFormAppliesTo(customVariableAppliesTo, form, user, status);
                customVariableFormAppliesToService.saveOrUpdate(customVariableFormAppliesTo);
            }
            saved = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
        }
        return saved;
    }

    public boolean saveOnlyAppliesToAndAssign(final CustomVariableAppliesTo customVariableAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        boolean success = false;
        try {
            CustomVariableFormAppliesTo customVariableFormAppliesTo = getCustomVariableFormAppliesTo(customVariableAppliesTo, form, user, status);
            saveCustomVariableAppliesTo(customVariableAppliesTo);
            saveCustomVariableFormAppliesTo(customVariableFormAppliesTo);
            formService.saveOrUpdate(form);
            success = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
            return false;
        }
        return success;
    }

    public boolean saveCustomVariableFormAppliesTo(final CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        boolean saved = false;
        try {
            customVariableFormAppliesToService.saveOrUpdate(customVariableFormAppliesTo);
            saved = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
            throw e;
        }
        return saved;
    }

    public boolean saveCustomVariableAppliesTo(final CustomVariableAppliesTo customVariableAppliesTo) {
        boolean saved = false;
        try {
            appliesToService.saveOrUpdate(customVariableAppliesTo);
            saved = true;
        } catch (Exception e) {
            log.debug("Could not save", e);
            throw e;
        }
        return saved;
    }

    private CustomVariableFormAppliesTo getCustomVariableFormAppliesTo(final CustomVariableAppliesTo customVariableAppliesTo, final CustomVariableForm form, final UserI user, final String status) {
        CustomVariableFormAppliesTo customVariableFormAppliesTo = new CustomVariableFormAppliesTo();
        customVariableFormAppliesTo.setXnatUser(user.getUsername());
        customVariableFormAppliesTo.setStatus(status);
        customVariableFormAppliesTo.setCustomVariableForm(form);
        customVariableFormAppliesTo.setCustomVariableAppliesTo(customVariableAppliesTo);
        return customVariableFormAppliesTo;
    }

    final private CustomVariableAppliesToService appliesToService;
    final private CustomVariableFormService formService;
    final private CustomVariableFormAppliesToService customVariableFormAppliesToService;


}
