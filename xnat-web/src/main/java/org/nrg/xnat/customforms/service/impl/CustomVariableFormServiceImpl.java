package org.nrg.xnat.customforms.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.customforms.daos.CustomVariableFormRepository;
import org.nrg.xnat.customforms.service.CustomVariableFormService;
import org.nrg.xnat.entities.CustomVariableForm;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@Transactional
public class CustomVariableFormServiceImpl extends AbstractHibernateEntityService<CustomVariableForm, CustomVariableFormRepository>
        implements CustomVariableFormService {

    public List<CustomVariableForm> getAllEagerly() {
        return getDao().getAllCustomVariableForm();
    }

    public CustomVariableForm findById(final long id) {
        return getDao().findById(id);
    }

    public CustomVariableForm findByUuid(UUID fId) {
        return getDao().findByUuid(fId);
    }

    public List getCustomVariableForms() {
        return getDao().getAllCustomVariableForm();
    }

    public void saveOrUpdate(CustomVariableForm customVariableForm) {
        getDao().saveOrUpdate(customVariableForm);
    }

    public void evict(CustomVariableForm form) {
        getDao().evict(form);
    }
}
