package org.nrg.xnat.customforms.service;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.entities.CustomVariableForm;

import java.util.List;
import java.util.UUID;

public interface CustomVariableFormService extends BaseHibernateService<CustomVariableForm> {

    List<CustomVariableForm> getAllEagerly();

    CustomVariableForm findById(final long id);

    CustomVariableForm findByUuid(UUID id);

    void saveOrUpdate(CustomVariableForm customVariableForm);

    void evict(CustomVariableForm form);
}
