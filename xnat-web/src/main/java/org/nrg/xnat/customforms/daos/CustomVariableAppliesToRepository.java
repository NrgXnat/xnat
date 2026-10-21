package org.nrg.xnat.customforms.daos;

import org.hibernate.Hibernate;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.framework.orm.hibernate.QueryBuilder;
import org.nrg.xnat.customforms.pojo.UserOptionsPojo;
import org.nrg.xnat.entities.CustomVariableAppliesTo;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Transactional
public class CustomVariableAppliesToRepository extends AbstractHibernateDAO<CustomVariableAppliesTo> {

    public static final String SCOPE     = "scope";
    public static final String ENTITY_ID = "entityId";
    public static final String DATA_TYPE = "dataType";
    public static final String PROTOCOL  = "protocol";
    public static final String VISIT = "visit";
    public static final String SUB_TYPE = "subType";

    /**
     * Overridden method to find rows by a given id
     *
     * @param id - the id to match
     * @return - the matched row - CustomVariableAppliesTo
     */
    @Override
    public CustomVariableAppliesTo findById(final long id) {
        return initializeChild(super.findById(id));
    }

    /**
     * Get all rows of the AppliesTo table
     *
     * @return - all rows or Null if empty
     */
    @SuppressWarnings("unused")
    public List<CustomVariableAppliesTo> getAllCustomVariableAppliesTo() {
        return initializeChildren(findAll());
    }

    /**
     * Find rows by a property
     *
     * @param property - the property name
     * @param value    - the value of the property
     * @return - matched rows - List of CustomVariableAppliesTo
     */
    @Nonnull
    @Override
    public List<CustomVariableAppliesTo> findByProperty(final String property, final Object value) {
        return initializeChildren(super.findByProperty(property, value));
    }

    /**
     * Find rows by a properties
     *
     * @param properties - Map of property name and value
     * @return - matched rows - List of CustomVariableAppliesTo
     */
    @Nonnull
    @Override
    public List<CustomVariableAppliesTo> findByProperties(final Map<String, Object> properties) {
        return initializeChildren(super.findByProperties(properties));
    }

    /**
     * Convenience method to evict from session; since we Lazy load
     *
     * @param appliesTo - the object to evict
     */
    public void evict(CustomVariableAppliesTo appliesTo) {
        getSession().evict(appliesTo);
    }

    @Nullable
    public List<CustomVariableAppliesTo> findByOptions(final UserOptionsPojo userOptionsPojo, final String entityId, final boolean imposeIsNull) {
        return initializeChildren(getCriteria(userOptionsPojo, entityId, imposeIsNull).getResults());
    }

    private List<CustomVariableAppliesTo> initializeChildren(List<CustomVariableAppliesTo> results) {
        if (results == null) {
            return null;
        }
        return results.stream().map(this::initializeChild).collect(Collectors.toList());
    }

    private CustomVariableAppliesTo initializeChild(CustomVariableAppliesTo appliesTo) {
        if (appliesTo != null) {
            Hibernate.initialize(appliesTo.getCustomVariableFormAppliesTos());
            appliesTo.getCustomVariableFormAppliesTos().forEach(cvfa-> {
                Hibernate.initialize(cvfa);
                Hibernate.initialize(cvfa.getCustomVariableForm());
            });
        }
        return appliesTo;
    }

    private QueryBuilder<CustomVariableAppliesTo> getCriteria(final UserOptionsPojo userOptionsPojo, final String entityId, final boolean imposeIsNull) {
        final QueryBuilder<CustomVariableAppliesTo> builder    = newQueryBuilder();
        final List<Predicate>                       predicates = new ArrayList<>();
        if (null == entityId) {
            predicates.add(builder.eq(SCOPE, Scope.Site));
        } else {
            predicates.add(builder.eq(SCOPE, Scope.Project));
            predicates.add(builder.eq(ENTITY_ID, entityId));
        }

        composePredicate(builder, predicates, imposeIsNull, DATA_TYPE, userOptionsPojo.getDataType());
        composePredicate(builder, predicates, imposeIsNull, PROTOCOL, userOptionsPojo.getProtocol());
        composePredicate(builder, predicates, imposeIsNull, VISIT, userOptionsPojo.getVisit());
        composePredicate(builder, predicates, imposeIsNull, SUB_TYPE, userOptionsPojo.getSubType());

        builder.where(builder.and(predicates));
        return builder;
    }

    private void composePredicate(QueryBuilder<CustomVariableAppliesTo> builder, List<Predicate> predicates, boolean imposeIsNull, String property, Object value) {
        if (imposeIsNull && value == null) {
            predicates.add(builder.isNull(property));
        } else if (value != null) {
            predicates.add(builder.eq(property, value));
        }
    }
}
