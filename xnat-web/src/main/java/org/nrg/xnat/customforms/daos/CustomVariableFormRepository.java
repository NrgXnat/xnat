package org.nrg.xnat.customforms.daos;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.customforms.utils.CustomFormHibernateUtils;
import org.nrg.xnat.entities.CustomVariableForm;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;


@Repository
@Transactional
public class CustomVariableFormRepository extends AbstractHibernateDAO<CustomVariableForm> {

    /**
     * Overridden method to find by Id
     * @param id - the Id to match
     * @return - matched object - CustomVariableForm
     */

    @Override
    @Nullable
    public CustomVariableForm findById(final long id) {
        List<CustomVariableForm>  forms =  findByProperty("id", id);
        if (forms != null) {
            return forms.get(0);
        }
        return null;
    }


    /**
     * Method to find by form UUID
     * @param id - the Id to match
     * @return - matched object - CustomVariableForm
     */
    @Nullable
    public CustomVariableForm findByUuid(final UUID id) {
        List<CustomVariableForm>  forms =  findByProperty("formUuid", id);
        if (forms != null) {
            return forms.get(0);
        }
        return null;
    }

    /**
     * Get all forms
     * @return - the rows - List of CustomVariableForm
     */


    public List<CustomVariableForm> getAllCustomVariableForm() {
        Criteria criteria = getCriteriaForType();
        List<CustomVariableForm> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        if (results != null) {
            for (CustomVariableForm a : results) {
                Hibernate.initialize(a.getCustomVariableFormAppliesTos());
            }
        }
        return results;
    }

    /**
     * Find a row identified by a given property and value
     * @param property - the name of the property
     * @param value - the value of the property
     * @return - matched rows - List of CustomVariableForm
     */

    @Override
    public List<CustomVariableForm> findByProperty(final String property, final Object value) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq(property, value));
        List<CustomVariableForm> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        if (results != null) {
            for (CustomVariableForm a : results) {
                Hibernate.initialize(a.getCustomVariableFormAppliesTos());
            }
        }
        return results;
    }


    /**
     * Evict an object from session
     * @param form - the form to evict
     */
    public void evict(CustomVariableForm form) {
        getSession().evict(form);
    }
}
