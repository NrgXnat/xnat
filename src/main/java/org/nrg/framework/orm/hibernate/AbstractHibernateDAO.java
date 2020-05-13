/*
 * framework: org.nrg.framework.orm.hibernate.AbstractHibernateDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.hibernate.*;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "WeakerAccess"})
abstract public class AbstractHibernateDAO<E extends BaseHibernateEntity> extends AbstractParameterizedWorker<E> implements BaseHibernateDAO<E> {

    public static final String DEFAULT_CACHE_REGION = "nrg";

    protected AbstractHibernateDAO() {
        this(null, null);
    }

    protected AbstractHibernateDAO(final Class<E> clazz) {
        this(clazz, null);
    }

    protected AbstractHibernateDAO(final SessionFactory factory) {
        this(null, factory);
    }

    private AbstractHibernateDAO(final Class<E> clazz, final SessionFactory factory) {
        super(clazz);

        if (factory != null) {
            _log.debug("Adding session factory in constructor: {}", factory.hashCode());
            _factory = factory;
        }

        final Class<E> parameterizedType = getParameterizedType();
        _isAuditable = HibernateUtils.isAuditable(parameterizedType);
        _cacheRegion = extractCacheRegion(parameterizedType);
        _addDistinctRootEntity = HibernateUtils.hasEagerlyFetchedCollection(parameterizedType);
        _hqlExistsBody = getHqlExistsComponent(HQL_EXISTS_BODY, "type", parameterizedType.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Autowired(required = false)
    @Override
    public void setSessionFactory(final SessionFactory factory) {
        _log.debug("Setting session factory in setter: {}", factory.hashCode());
        _factory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable create(final E entity) {
        return getSession().save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E retrieve(final long id) {
        if (_isAuditable) {
            return findEnabledById(id);
        }
        return (E) getSession().get(getParameterizedType(), id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final E entity) {
        // TODO: When JPA persistence lifecycle support is working, remove explicit timestamp update.
        entity.setTimestamp(new Date());
        getSession().update(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final E entity) {
        if (_isAuditable) {
            entity.setEnabled(false);
            entity.setDisabled(new Date());
            getSession().update(entity);
        } else {
            getSession().delete(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveOrUpdate(final E entity) {
        try {
            final Criteria criteria = getCriteriaForType();
            criteria.add(Restrictions.eq("id", entity.getId()));
            if (!criteria.list().isEmpty()) {
                update(entity);
            } else {
                create(entity);
            }
        } catch (NonUniqueObjectException e) {
            getSession().merge(entity);
        }
    }

    /**
     * @see BaseHibernateDAO#findAll()
     */
    @Override
    public List<E> findAll() {
        return findByCriteria();
    }

    /**
     * @see BaseHibernateDAO#findAllEnabled()
     */
    @Override
    public List<E> findAllEnabled() {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq("enabled", true));
        return criteria.list();
    }

    @Override
    public long countAll() {
        final Criteria criteria = getCriteriaForType();
        criteria.setProjection(Projections.rowCount());
        return (long) criteria.uniqueResult();
    }

    @Override
    public long countAllEnabled() {
        final Criteria criteria = getCriteriaForType();
        criteria.setProjection(Projections.rowCount());
        criteria.add(Restrictions.eq("enabled", true));
        return (long) criteria.uniqueResult();
    }

    /**
     * @see BaseHibernateDAO#findByExample(BaseHibernateEntity, String[])
     */
    @Override
    public List<E> findByExample(E exampleInstance, String[] excludeProperty) {
        final Criteria criteria = getCriteriaForType();
        if (_isAuditable) {
            exampleInstance.setEnabled(true);
        }
        final Example example = Example.create(exampleInstance);
        for (final String exclude : excludeProperty) {
            if (!_isAuditable || !exclude.equals("enabled")) {
                example.excludeProperty(exclude);
            }
        }
        criteria.add(example);
        return criteria.list();
    }

    /**
     * @see BaseHibernateDAO#findByExample(BaseHibernateEntity, String[])
     */
    @Override
    public List<E> findAllByExample(final E exampleInstance, final String[] excludeProperty) {
        final Criteria criteria = getCriteriaForType();
        final Example  example  = Example.create(exampleInstance);
        for (final String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        criteria.add(example);
        return criteria.list();
    }

    @Override
    public List<E> findByProperty(final String property, final Object value) {
        final Criteria criteria = getCriteriaForType();
        criteria.add(Restrictions.eq(property, value));
        if (_isAuditable) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        final List list = criteria.list();
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return (List<E>) list;
        }
    }

    @Override
    public List<E> findByProperties(final Map<String, Object> properties) {
        final Criteria criteria = getCriteriaForType();
        for (final String property : properties.keySet()) {
            final Object value = properties.get(property);
            criteria.add(Restrictions.eq(property, value));
        }
        if (_isAuditable) {
            criteria.add(Restrictions.eq("enabled", true));
        }
        final List list = criteria.list();
        if (list == null || list.size() == 0) {
            return null;
        }
        return (List<E>) list;
    }

    @Override
    public E findByUniqueProperty(final String property, final Object value) {
        List<E> matches = findByProperty(property, value);
        if (matches != null && matches.size() > 1) {
            throw new RuntimeException("The specified property " + property + " is not a unique constraint!");
        }
        return matches != null ? matches.get(0) : null;
    }

    /**
     * @see BaseHibernateDAO#findById(long)
     */
    @Override
    public E findById(final long id) {
        return findById(id, false);
    }

    /**
     * @see BaseHibernateDAO#findById(long, boolean)
     */
    @Override
    public E findById(final long id, final boolean lock) {
        return lock ? (E) getSession().load(getParameterizedType(), id, LockOptions.UPGRADE) : (E) getSession().load(getParameterizedType(), id);
    }

    /**
     * @see BaseHibernateDAO#findEnabledById(long)
     */
    @Override
    public E findEnabledById(final long id) {
        return findEnabledById(id, false);
    }

    /**
     * @see BaseHibernateDAO#findEnabledById(long, boolean)
     */
    @Override
    public E findEnabledById(final long id, final boolean lock) {
        final E entity = findById(id, lock);
        return entity != null && entity.isEnabled() ? entity : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final String property, final Object value) {
        return exists(parameters(property, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final Map<String, Object> criteria) {
        if (criteria.isEmpty()) {
            return false;
        }
        final StringBuilder       query      = new StringBuilder();
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", getParameterizedType().getSimpleName());
        if (!criteria.containsKey("enabled")) {
            query.append(_hqlExistsBody).append(getHqlExistsComponent(HQL_EXISTS_WHERE, "property", "enabled"));
            parameters.put("enabled", true);
        }
        for (final String name : criteria.keySet()) {
            query.append(query.length() == 0 ? HQL_EXISTS_BODY : " and ").append(getHqlExistsComponent(HQL_EXISTS_WHERE, "property", name));
            parameters.put(name, criteria.get(name));
        }
        _log.debug("Composed HQL query '{}' with parameters: {}", query, parameters);
        return getSession().createQuery(query.toString()).setProperties(parameters).uniqueResult() != null;
    }

    /**
     * @see BaseHibernateDAO#refresh(boolean, BaseHibernateEntity)
     */
    @Override
    public void refresh(final boolean initialize, final E entity) {
        getSession().refresh(entity);
        if (initialize) {
            initialize(entity);
        }
    }

    @Override
    public void flush() {
        getSession().flush();
    }

    /**
     * Method to initialize entity. By default, calls {@link org.hibernate.Hibernate#initialize(Object)}, but this
     * can be overridden.
     *
     * @param entity Entity object to initialize.
     */
    @Override
    public void initialize(final E entity) {
        Hibernate.initialize(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Number> getRevisions(final long id) {
        return getAuditReader().getRevisions(getParameterizedType(), id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getRevision(final long id, final Number revision) {
        return getAuditReader().find(getParameterizedType(), id, revision);
    }

    protected static Map<String, Object> parameters(final String property, final Object value) {
        return ImmutableMap.of(property, value);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2) {
        return ImmutableMap.of(property1, value1, property2, value2);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3) {
        return ImmutableMap.of(property1, value1, property2, value2, property3, value3);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4) {
        return ImmutableMap.of(property1, value1, property2, value2, property3, value3, property4, value4);
    }

    @SuppressWarnings("unused")
    protected E getEntityFromResult(final Object result) {
        if (result == null) {
            return null;
        }
        if (getParameterizedType().isAssignableFrom(result.getClass())) {
            return (E) result;
        } else if (result instanceof Object[]) {
            for (final Object object : (Object[]) result) {
                if (getParameterizedType().isAssignableFrom(object.getClass())) {
                    return (E) object;
                }
            }
        }
        return null;
    }

    /**
     * Returns the current Hibernate session object.
     *
     * @return The current Hibernate session object if available.
     */
    protected Session getSession() {
        try {
            return _factory.getCurrentSession();
        } catch (HibernateException exception) {
            _log.error("Trying to get session for parameterized type: {}", getParameterizedType(), exception);
            throw exception;
        }
    }

    /**
     * Use this inside subclasses as a convenience method.
     *
     * @param criterion The criteria on which you want to search.
     *
     * @return All entities matching the submitted criteria.
     */
    protected List<E> findByCriteria(final Criterion... criterion) {
        final Criteria criteria = getCriteriaForType();
        for (final Criterion c : criterion) {
            criteria.add(c);
        }
        return criteria.list();
    }

    /**
     * Gets a {@link Criteria Criteria object} for the parameterized type of the concrete definition. Default standard
     * values are set for the criteria object, including {@link Criteria#setCacheable(boolean)} set to <b>true</b>.
     *
     * @return An initialized {@link Criteria Criteria object}.
     */
    protected Criteria getCriteriaForType() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType(), StringUtils.uncapitalize(getParameterizedType().getSimpleName()));
        criteria.setCacheable(true);
        criteria.setCacheRegion(getCacheRegion());
        if (_addDistinctRootEntity) {
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        return criteria;
    }

    /**
     * Add a {@link Restrictions} {@link Criterion criterion} to the {@link Criteria} object for the name/value pair. If
     * the value is null, the criterion is set to {@link Restrictions#isNull(String)} for the indicated name, otherwise
     * it's set to the given value.
     *
     * @param criteria The {@link Criteria} object to which the restriction should be added.
     * @param name     The name of the property.
     * @param value    The value of the property. May be null.
     */
    @SuppressWarnings("unused")
    protected void addNullableCriteria(final Criteria criteria, final String name, final Object value) {
        if (value == null) {
            criteria.add(Restrictions.isNull(name));
        } else {
            criteria.add(Restrictions.eq(name, value));
        }
    }

    /**
     * Provides convenience method to return untyped list as list parameterized with same type as the DAO.
     *
     * @param list The list to be converted to a checked parameterized list.
     *
     * @return The parameterized list.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected List<E> checked(final List list) {
        return (List<E>) list;
    }

    /**
     * Provides convenience method to return list if it contains any items or null if not.
     *
     * @param list The list to be checked and returned if not empty.
     *
     * @return Returns the list if it's not null and contains at least one item, <b>null</b> otherwise.
     */
    protected List<E> emptyToNull(final List<E> list) {
        return isEmpty(list) ? null : list;
    }

    /**
     * Provides convenience method to return list if it contains any items or null if not.
     *
     * @param list The list to be checked and returned if not empty.
     *
     * @return Returns the list if it's not null and contains at least one item, <b>null</b> otherwise.
     */
    protected E instance(final List<E> list) {
        return isEmpty(list) ? null : list.get(0);
    }

    protected static boolean isEmpty(final List<?> list) {
        return list == null || list.isEmpty();
    }

    protected String getCacheRegion() {
        return _cacheRegion;
    }

    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(getSession());
    }

    private String extractCacheRegion(final Class<E> type) {
        return type.isAnnotationPresent(org.hibernate.annotations.Cache.class)
               ? type.getAnnotation(org.hibernate.annotations.Cache.class).region()
               : DEFAULT_CACHE_REGION;
    }

    private static String getHqlExistsComponent(final String hqlExistsBody, final String type, final String name) {
        return StringSubstitutor.replace(hqlExistsBody, parameters(type, name));
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractHibernateDAO.class);

    private static final String HQL_EXISTS_BODY  = "select 1 from ${type} where ";
    private static final String HQL_EXISTS_WHERE = "${property} = :${property}";

    private SessionFactory _factory;

    private final String  _hqlExistsBody;
    private final boolean _isAuditable;
    private final String  _cacheRegion;
    private final boolean _addDistinctRootEntity;
}
