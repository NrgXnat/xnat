package org.nrg.xnat.customforms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.customforms.daos.CustomVariableFormAppliesToRepository;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.customforms.service.CustomVariableFormAppliesToService;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
@Transactional
public class CustomVariableFormAppliesToServiceImpl extends AbstractHibernateEntityService<CustomVariableFormAppliesTo, CustomVariableFormAppliesToRepository>
        implements CustomVariableFormAppliesToService {


    public void saveOrUpdate(CustomVariableFormAppliesTo customVariableFormAppliesTo) {
        getDao().saveOrUpdate(customVariableFormAppliesTo);

    }



    public CustomVariableFormAppliesTo findByRowIdentifier(RowIdentifier rowId) {
        return getDao().findByRowIdentifier(rowId);
    }


    public CustomVariableFormAppliesTo findForProject(final String projectId, final long formId) {
        return getDao().findForProject(projectId, formId);
    }


    public List<CustomVariableFormAppliesTo> findAllFormsByExclusion(final UserOptionsPojo userOptionsPojo, final long excludedFormId) {
        return getDao().findAllFormsByExclusion(userOptionsPojo, excludedFormId);
    }


    public List<CustomVariableFormAppliesTo> findAllSpecificProjectForm(final UserOptionsPojo userOptionsPojo, final List<String> entityIds, final long formId) {
        return getDao().findAllSpecificProjectForm(userOptionsPojo, entityIds, formId);
    }

    public List<CustomVariableFormAppliesTo> findByFormId(final long formId) {
        return getDao().findByFormId(formId);
    }

    public List<CustomVariableFormAppliesTo> findOptedOutByFormId(final long formId) {
        return getDao().findByFormIdAndStatus(formId, CustomFormsConstants.OPTED_OUT_STATUS_STRING);
    }


    public List<CustomVariableFormAppliesTo> findByFormIdByExclusion(final long formId, final RowIdentifier exclude) {
        return getDao().findByFormIdByExclusion(formId, exclude);
    }

    public List<CustomVariableFormAppliesTo> findByAppliesToId(final long appliesToId) {
        return getDao().findByAppliesToId(appliesToId);
    }

    public List<CustomVariableForm> findAllDistinctFormsByDatatype(final String dataType, final String status) {
        return getDao().findAllDistinctFormsByDatatype(dataType, status);
    }


}
