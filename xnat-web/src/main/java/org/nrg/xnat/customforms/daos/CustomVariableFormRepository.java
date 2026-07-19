package org.nrg.xnat.customforms.daos;

import org.hibernate.Hibernate;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.entities.CustomVariableForm;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional
public class CustomVariableFormRepository extends AbstractHibernateDAO<CustomVariableForm> {
    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public CustomVariableForm findById(final long id) {
        List<CustomVariableForm> forms = findByProperty("id", id);
        return forms.isEmpty() ? null : forms.getFirst();
    }

    /**
     * Method to find by form UUID
     * @param id the ID to match
     * @return matched object - CustomVariableForm
     */
    @Nullable
    public CustomVariableForm findByUuid(final UUID id) {
        List<CustomVariableForm>  forms =  findByProperty("formUuid", id);
        return forms.isEmpty() ? null : forms.getFirst();
    }

    /**
     * Get all forms
     * @return - the rows - List of CustomVariableForm
     */
    public List<CustomVariableForm> getAllCustomVariableForm() {
        return initialize(emptyToNull(findAll()));
    }

    /**
     * Find a row identified by a given property and value
     * @param property - the name of the property
     * @param value - the value of the property
     * @return - matched rows - List of CustomVariableForm
     */
    @Nonnull
    @Override
    public List<CustomVariableForm> findByProperty(final String property, final Object value) {
        return initialize(super.findByProperty(property, value));
    }


    /**
     * Evict an object from session
     * @param form - the form to evict
     */
    public void evict(CustomVariableForm form) {
        getSession().evict(form);
    }

    private List<CustomVariableForm> initialize(final List<CustomVariableForm> forms) {
        if (forms != null) {
            forms.forEach(
                    form -> {
                        Hibernate.initialize(form.getCustomVariableFormAppliesTos());
                        form.getCustomVariableFormAppliesTos().forEach(Hibernate::initialize);
                        form.getCustomVariableFormAppliesTos().forEach(
                                customVariableFormAppliesTo -> {
                                    Hibernate.initialize(customVariableFormAppliesTo.getCustomVariableAppliesTo());
                                    Hibernate.initialize(customVariableFormAppliesTo.getCustomVariableForm());
                                });

                    });
        }
        return forms;
    }
}
