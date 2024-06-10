package org.nrg.xnat.customforms.daos;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xapi.model.users.User;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.customforms.utils.CustomFormsConstants;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.nrg.xnat.entities.CustomVariableFormAppliesTo;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Repository
@Transactional
public class CustomVariableAppliesToRepository extends AbstractHibernateDAO<CustomVariableAppliesTo> {

    /**
     * Overriden method to find rows by a given id
     * @param id - the id to match
     * @return - the matched row - CustomVaribleAppliesTo
     */

    @Override
    public CustomVariableAppliesTo findById(final long id) {
        CustomVariableAppliesTo found = super.findById(id);
        if (null != found) {
            initializeChild(found);
        }
        return found;
    }

    /**
     * Get all rows of the AppliesTo table
     * @return - all rows or Null if empty
     */
    public List<CustomVariableAppliesTo> getAllCustomVariableAppliesTo() {
        Criteria criteria = getCriteriaForType();
        List<CustomVariableAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeChildren(results);
        return results;
    }

    /**
     * Find rows by a property
     * @param property - the property name
     * @param value - the value of the property
     * @return - matched rows - List of CustomVariableAppliesTo
     */
    @Override
    public List<CustomVariableAppliesTo> findByProperty(final String property, final Object value) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq(property, value));
        List<CustomVariableAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeChildren(results);
        return results;
    }

    /**
     * Find rows by a properties
     * @param properties - Map of property name and value
     * @return - matched rows - List of CustomVaribleAppliesTo
     */

    @Override
    public List<CustomVariableAppliesTo> findByProperties(final Map<String, Object> properties) {
        final Criteria criteria = getCriteriaForType();
        for (final String property : properties.keySet()) {
            final Object value = properties.get(property);
            criteria.add(Restrictions.eq(property, value));
        }
        List<CustomVariableAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeChildren(results);
        return results;
    }


    private void initializeChildren(List<CustomVariableAppliesTo> results) {
        if (results != null) {
            for (CustomVariableAppliesTo a : results) {
                initializeChild(a);
            }
        }
    }



    private void initializeChild(CustomVariableAppliesTo obj) {
        Hibernate.initialize(obj.getCustomVariableFormAppliesTos());
    }

    /**
     * Convenience method to evict from session; since we Lazy load
     * @param appliesTo - the object to evict
     */
    public void evict(CustomVariableAppliesTo appliesTo) {
        getSession().evict(appliesTo);
    }

    @Nullable
    public List<CustomVariableAppliesTo> findByOptions(final UserOptionsPojo userOptionsPojo, final String entityId, final boolean imposeIsNull) {
        final Criteria criteria = getCriteria(userOptionsPojo, entityId, imposeIsNull);
        List<CustomVariableAppliesTo> results = super.emptyToNull(GenericUtils.convertToTypedList(criteria.list(), getParameterizedType()));
        initializeChildren(results);
        return results;
    }

    private Criteria getCriteria(final UserOptionsPojo userOptionsPojo, final String entityId, final boolean imposeIsNull) {
        Criteria criteria  = getSession().createCriteria(CustomVariableAppliesTo.class);
        if (null == entityId) {
            criteria.add(Restrictions.eq("scope", Scope.Site));
        }else {
            criteria.add(Restrictions.eq("scope", Scope.Project));
            criteria.add(Restrictions.eq("entityId", entityId));
        }
        addToCriteria(criteria,userOptionsPojo, imposeIsNull);
        return criteria;
    }

    private void addToCriteria(Criteria criteria, final UserOptionsPojo userOptionsPojo, boolean imposeIsNull) {
        final String dataType = userOptionsPojo.getDataType();
        final String protocol = userOptionsPojo.getProtocol();
        final String visit = userOptionsPojo.getVisit();
        final String subType = userOptionsPojo.getSubType();
        if (imposeIsNull && (dataType == null)) {
            criteria.add(Restrictions.isNull("dataType"));
        }else if (dataType != null) {
            criteria.add(Restrictions.eq("dataType", dataType));
        }
        if (imposeIsNull && (protocol == null)) {
            criteria.add(Restrictions.isNull("protocol"));
        }else if (protocol != null) {
            criteria.add(Restrictions.eq("protocol", protocol));
        }
        if (imposeIsNull && (visit == null)) {
            criteria.add(Restrictions.isNull("visit"));
        }else if (visit != null) {
            criteria.add(Restrictions.eq("visit", visit));
        }
        if (imposeIsNull && (subType == null)) {
            criteria.add(Restrictions.isNull("subType"));
        }else if (subType != null) {
            criteria.add(Restrictions.eq("subType", subType));
        }

    }

}
