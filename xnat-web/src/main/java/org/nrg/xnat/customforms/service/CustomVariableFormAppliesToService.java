package org.nrg.xnat.customforms.service;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.pojo.formio.RowIdentifier;
import org.nrg.xnat.entities.CustomVariableForm;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;

import javax.annotation.Nullable;
import java.util.List;

public interface CustomVariableFormAppliesToService extends BaseHibernateService<CustomVariableFormAppliesTo> {

    void saveOrUpdate(CustomVariableFormAppliesTo customVariableFormAppliesTo);

    @Nullable
    CustomVariableFormAppliesTo findByRowIdentifier(RowIdentifier rowId);

    CustomVariableFormAppliesTo findForProject(final String projectId, final long formId);

    List<CustomVariableFormAppliesTo> findByFormId(final long formId);

    List<CustomVariableFormAppliesTo> findOptedOutByFormId(final long formId);

    List<CustomVariableFormAppliesTo> findByFormIdByExclusion(final long formId, final RowIdentifier exclude);

    List<CustomVariableFormAppliesTo> findByAppliesToId(final long formId);

    List<CustomVariableFormAppliesTo> findAllFormsByExclusion(final UserOptionsPojo userOptionsPojo,  final long excludedFormId);

    List<CustomVariableFormAppliesTo> findAllSpecificProjectForm( final UserOptionsPojo userOptionsPojo, final List<String> entityIds, final long formId);

    List<CustomVariableForm> findAllDistinctFormsByDatatype(final String dataType, final String status);


    void delete(final CustomVariableFormAppliesTo entity);

}
