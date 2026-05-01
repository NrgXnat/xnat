/*
 * framework: org.nrg.framework.orm.hibernate.AbstractHibernateDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.query.Query;
import org.nrg.framework.ajax.PaginatedRequest;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.framework.utilities.Reflection;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter(AccessLevel.PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractHibernateDAO<E extends BaseHibernateEntity> extends AbstractParameterizedWorker<E> implements BaseHibernateDAO<E> {
    public static final String DEFAULT_CACHE_REGION = "nrg";
    public static final String ENABLED_PROPERTY     = "enabled";
    public static final String ENABLED_ALL          = "all";

    private static final String HQL_EXISTS_BODY  = "select 1 from ${type} where ";
    private static final String HQL_EXISTS_WHERE = "${property} = :${property}";

    private final String                _hqlExistsBody;
    private final boolean               _isAuditable;
    private final String                _cacheRegion;
    private final boolean _addDistinctRootEntity;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Map<String, Class<?>> _propertyTypes;
    // TODO: We should be able to use the property types map to inform the
    //  column type(s) as in #distinct(), but how to do so with parameterization?

    private SessionFactory _factory;
    private EntityType<E>  _entityType;

    protected AbstractHibernateDAO() {
        this(null);
    }

    protected AbstractHibernateDAO(final SessionFactory factory) {
        if (factory != null) {
            setSessionFactory(factory);
        }
        final Class<E> parameterizedType = getParameterizedType();
        _isAuditable   = HibernateUtils.isAuditable(parameterizedType);
        _cacheRegion   = extractCacheRegion(parameterizedType);
        _addDistinctRootEntity = HibernateUtils.hasEagerlyFetchedCollection(parameterizedType);
        _hqlExistsBody = getHqlExistsComponent(HQL_EXISTS_BODY, "type", parameterizedType.getName());
        _propertyTypes = Reflection.getProperties(parameterizedType, method -> method.getAnnotation(Transient.class) == null);
    }

    /**
     * Sets the DAO's session factory. This is annotated as <pre>@Autowired</pre> and will be set to an instance from
     * the application context if available.
     *
     * @param factory The session factory to set
     */
    @Autowired
    public void setSessionFactory(final SessionFactory factory) {
        log.debug("Setting session factory in setter: {}", factory.hashCode());
        _factory = factory;
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return getSession().getCriteriaBuilder();
    }

    public <T> Query<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getSession().createQuery(criteriaQuery);
    }

    public Query<E> createQuery(String query) {
        return createQuery(query, getParameterizedType());
    }

    public <T> Query<T> createQuery(String query, final Class<T> type) {
        return getSession().createQuery(query, type);
    }

    public Query<E> createNamedQuery(String queryName) {
        return createNamedQuery(queryName, getParameterizedType());
    }

    public <T> Query<T> createNamedQuery(String queryName, Class<T> type) {
        return getSession().createNamedQuery(queryName, type);
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
        return getParameterizedType().cast(getSession().get(getParameterizedType(), id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final E entity) {
        try {
            getSession().update(entity);
        } catch (NonUniqueObjectException e) {
            getSession().merge(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final E entity) {
        if (_isAuditable) {
            entity.setEnabled(false);
            entity.setDisabled(new Date());
            try {
                getSession().update(entity);
            } catch (HibernateException e) {
                getSession().merge(entity);
            }
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
            if (exists("id", entity.getId())) {
                if (entity.getCreated() == null) {
                    entity.setCreated(new Date());
                }
                update(entity);
            } else {
                create(entity);
            }
        } catch (NonUniqueObjectException e) {
            getSession().merge(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAll() {
        return _isAuditable ? findByProperty(ENABLED_PROPERTY, ENABLED_ALL) : findByProperties(Collections.emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAllEnabled() {
        return findByProperty(ENABLED_PROPERTY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findPaginated(final HibernatePaginatedRequest request) {
        // CACHING: This should be converted to use QueryBuilder...
        CriteriaBuilder  criteriaBuilder = getSession().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery   = criteriaBuilder.createQuery(getParameterizedType());
        Root<E>          root            = criteriaQuery.from(getEntityType());

        criteriaQuery.select(root);

        List<Predicate> predicates = new ArrayList<>();
        if (request.hasFilters()) {
            Map<String, HibernateFilter> expressions = request.getCriterion();
            log.debug("Found {} filters", expressions.size());
            expressions.forEach((property, filter) -> predicates.add(filter.makeCriterion(criteriaBuilder, root, property)));
        }

        if (requiresEnabledCriterion(request)) {
            predicates.add(criteriaBuilder.equal(root.get(ENABLED_PROPERTY), true));
        }

        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));

        if (request.getSortBys().isEmpty()) {
            Pair<PaginatedRequest.SortDir, String> order = request.getOrder();
            if (order != null) {
                Expression<Object> sortColumn = root.get(order.getValue());
                criteriaQuery.orderBy(order.getKey() == PaginatedRequest.SortDir.ASC
                                      ? criteriaBuilder.asc(sortColumn)
                                      : criteriaBuilder.desc(sortColumn));
            }
        } else {
            criteriaQuery.orderBy(request.getSortBys()
                                         .stream()
                                         .map(pair -> pair.getKey() == PaginatedRequest.SortDir.ASC
                                                      ? criteriaBuilder.asc(root.get(pair.getValue()))
                                                      : criteriaBuilder.desc(root.get(pair.getValue())))
                                         .collect(Collectors.toList()));
        }

        final Query<E> query    = getSession().createQuery(criteriaQuery);
        final int      offset   = request.getOffset();
        final int      pageSize = request.getPageSize();
        if (offset > 0) {
            query.setFirstResult(offset);
        }
        if (pageSize > 0) {
            query.setMaxResults(pageSize);
        }
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countAll() {
        return countByProperties(Collections.emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countAllEnabled() {
        return countByProperty(ENABLED_PROPERTY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countByProperty(String property, Object value) {
        return countByProperties(parameters(property, value));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countByProperties(Map<String, Object> properties) {
        CriteriaBuilder     builder       = getSession().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<E>             root          = criteriaQuery.from(getEntityType());
        criteriaQuery.select(builder.count(root));
        if (!properties.isEmpty()) {
            criteriaQuery.where(builder.and(properties.entrySet()
                                                      .stream()
                                                      .map(entry -> builder.equal(root.get(entry.getKey()), entry.getValue()))
                                                      .toArray(Predicate[]::new)));
        }
        return getSession().createQuery(criteriaQuery).getSingleResult();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findByExample(E example, String[] excludeProperties) {
        if (_isAuditable) {
            example.setEnabled(true);
        }
        return findAllByExample(example, excludeProperties);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<E> findAllByExample(final E example, final String[] excludeProperties) {
        QueryBuilder<E> queryBuilder = newQueryBuilder();
        final Predicate predicate    = queryBuilder.example(example, Arrays.asList(excludeProperties));
        queryBuilder.where(predicate);
        return queryBuilder.getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(final String property, final Object value) {
        return findByProperties(parameters(property, value), 0, 0, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(String property, Object value, int offset, int pageSize) {
        return findByProperties(parameters(property, value), offset, pageSize, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(final String property, Object value, Pair<PaginatedRequest.SortDir, String> ordering) {
        return findByProperties(parameters(property, value), 0, 0, Collections.singletonList(ordering));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(final String property, Object value, List<Pair<PaginatedRequest.SortDir, String>> ordering) {
        return findByProperties(parameters(property, value), 0, 0, ordering);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(String property, Object value, int offset, int pageSize, Pair<PaginatedRequest.SortDir, String> ordering) {
        return findByProperties(parameters(property, value), offset, pageSize, Collections.singletonList(ordering));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperty(final String property, Object value, int offset, int pageSize, List<Pair<PaginatedRequest.SortDir, String>> ordering) {
        return findByProperties(parameters(property, value), offset, pageSize, ordering);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties) {
        return findByProperties(properties, 0, 0, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties, int offset, int pageSize) {
        return findByProperties(properties, offset, pageSize, Collections.emptyList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties, Pair<PaginatedRequest.SortDir, String> ordering) {
        return findByProperties(properties, 0, 0, Collections.singletonList(ordering));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties, int offset, int pageSize, Pair<PaginatedRequest.SortDir, String> ordering) {
        return findByProperties(properties, offset, pageSize, Collections.singletonList(ordering));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties, List<Pair<PaginatedRequest.SortDir, String>> ordering) {
        return findByProperties(properties, 0, 0, ordering);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<E> findByProperties(final Map<String, Object> properties, int offset, int pageSize, List<Pair<PaginatedRequest.SortDir, String>> ordering) {
        checkEnabledParameter(properties);
        final QueryBuilder<E> builder = newQueryBuilder();
        builder.where(builder.and(properties));
        builder.orderBy(ordering);
        return builder.firstResult(offset).pageSize(pageSize).getResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findByUniqueProperty(final String property, final Object value) {
        List<E> matches = findByProperty(property, value);
        if (matches.size() > 1) {
            throw new RuntimeException("The specified property " + property + " is not a unique constraint!");
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findByUniqueProperties(final Map<String, Object> properties) {
        List<E> matches = findByProperties(properties);
        if (matches.size() > 1) {
            throw new RuntimeException("The specified properties " + String.join(", ", properties.keySet()) + " are not a unique constraint!");
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findById(final long id) {
        return findById(id, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findById(final long id, final boolean lock) {
        return lock ? getParameterizedType().cast(getSession().load(getParameterizedType(), id, LockOptions.UPGRADE)) : getParameterizedType().cast(getSession().load(getParameterizedType(), id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E findEnabledById(final long id) {
        return findEnabledById(id, false);
    }

    /**
     * {@inheritDoc}
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
        if (_isAuditable && !criteria.containsKey(ENABLED_PROPERTY)) {
            query.append(_hqlExistsBody).append(getHqlExistsComponent(HQL_EXISTS_WHERE, "property", ENABLED_PROPERTY));
            parameters.put(ENABLED_PROPERTY, true);
        }
        for (final String name : criteria.keySet()) {
            query.append(query.length() == 0 ? _hqlExistsBody : " and ").append(getHqlExistsComponent(HQL_EXISTS_WHERE, "property", name));
            parameters.put(name, criteria.get(name));
        }
        log.debug("Composed HQL query '{}' with parameters: {}", query, parameters);
        return getSession().createQuery(query.toString()).setProperties(parameters).uniqueResult() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Set<T> distinct(final Class<T> columnType, final String columnName) {
        return newQueryBuilder().distinct(columnType, columnName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh(final boolean initialize, final E entity) {
        getSession().refresh(entity);
        if (initialize) {
            initialize(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() {
        getSession().flush();
    }

    /**
     * {@inheritDoc}
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
        return parameterMap(property, value);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2) {
        return parameterMap(property1, value1, property2, value2);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3) {
        return parameterMap(property1, value1, property2, value2, property3, value3);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4) {
        return parameterMap(property1, value1, property2, value2, property3, value3, property4, value4);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4, final String property5, final Object value5) {
        return parameterMap(property1, value1, property2, value2, property3, value3, property4, value4, property5, value5);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4, final String property5, final Object value5, final String property6, final Object value6) {
        return parameterMap(property1, value1, property2, value2, property3, value3, property4, value4, property5, value5, property6, value6);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4, final String property5, final Object value5, final String property6, final Object value6, final String property7, final Object value7) {
        return parameterMap(property1, value1, property2, value2, property3, value3, property4, value4, property5, value5, property6, value6, property7, value7);
    }

    protected static Map<String, Object> parameters(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4, final String property5, final Object value5, final String property6, final Object value6, final String property7, final Object value7, final String property8, final Object value8) {
        return parameterMap(property1, value1, property2, value2, property3, value3, property4, value4, property5, value5, property6, value6, property7, value7, property8, value8);
    }

    protected static Pair<PaginatedRequest.SortDir, String> asc(final String column) {
        return Pair.of(PaginatedRequest.SortDir.ASC, column);
    }

    protected static Pair<PaginatedRequest.SortDir, String> desc(final String column) {
        return Pair.of(PaginatedRequest.SortDir.DESC, column);
    }

    @SuppressWarnings("unused")
    protected E getEntityFromResult(final Object result) {
        if (result == null) {
            return null;
        }
        if (getParameterizedType().isAssignableFrom(result.getClass())) {
            return getParameterizedType().cast(result);
        } else if (result instanceof Object[]) {
            for (final Object object : (Object[]) result) {
                if (getParameterizedType().isAssignableFrom(object.getClass())) {
                    return getParameterizedType().cast(object);
                }
            }
        }
        return null;
    }

    /**
     * Creates a new query builder for an atomic operation.
     *
     * @return The new query builder instance.
     */
    @Nonnull
    protected QueryBuilder<E> newQueryBuilder() {
        return new QueryBuilder<>(getSession(), getParameterizedType());
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
            log.error("Trying to get session for parameterized type: {}", getParameterizedType(), exception);
            throw exception;
        }
    }

    /**
     * Use this inside subclasses as a convenience method.
     *
     * @param criterion The criteria on which you want to search.
     * @return All entities matching the submitted criteria.
     * @deprecated Use JPA query to replace Hibernate query.
     */
    @Deprecated
    protected List<E> findByCriteria(final Criterion... criterion) {
        final Criteria criteria = getCriteriaForType();
        for (final Criterion c : criterion) {
            criteria.add(c);
        }
        return GenericUtils.convertToTypedList(criteria.list(), getParameterizedType());
    }
    /**
     * Gets a {@link Criteria Criteria object} for the parameterized type of the concrete definition. Default standard
     * values are set for the criteria object, including {@link Criteria#setCacheable(boolean)} set to <b>true</b>.
     *
     * @return An initialized {@link Criteria Criteria object}.
     * @deprecated Use JPA query to replace Hibernate query.
     */
    @Deprecated
    protected Criteria getCriteriaForType() {
        final Criteria criteria = getSession().createCriteria(getParameterizedType(), StringUtils.uncapitalize(getParameterizedType().getSimpleName()));
//        criteria.setCacheable(true);
        criteria.setCacheRegion(getCacheRegion());
        if (_addDistinctRootEntity) {
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        return criteria;
    }

    /**
     * Returns the type of the configured entity class.
     *
     * @return The type of the configured entity class.
     */
    protected EntityType<E> getEntityType() {
        return ObjectUtils.getIfNull(_entityType, () -> {
            _entityType = getSession().getMetamodel().entity(getParameterizedType());
            return _entityType;
        });
    }

    /**
     * Provides convenience method to return untyped list as list parameterized with same type as the DAO.
     *
     * @param list The list to be converted to a checked parameterized list.
     *
     * @return The parameterized list.
     *
     * @deprecated Use {@link GenericUtils#convertToTypedList(Iterable, Class)} instead.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    @Deprecated
    protected List<E> checked(final List list) {
        return (List<E>) list;
    }

    /**
     * Provides convenience method to return untyped list as list parameterized with different type from the DAO. This
     * is useful when casting lists returned by, e.g., HQL queries.
     *
     * @param list       The list to be converted to a checked parameterized list.
     * @param returnType The return type for the list.
     *
     * @return The parameterized list.
     */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    protected <T> List<T> checked(final List list, final Class<T> returnType) {
        return (List<T>) list;
    }

    /**
     * Provides convenience method to return list if it contains any items or null if not.
     *
     * @param list The list to be checked and returned if not empty.
     *
     * @return Returns the list if it's not null and contains at least one item, <b>null</b> otherwise.
     */
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    protected String getCacheRegion() {
        return _cacheRegion;
    }

    /**
     * If the entity type is auditable, this checks whether the enabled parameter is already in the properties map.
     * If not, it's added with the value set to true. If so, and the value is the string {@link #ENABLED_ALL}, the
     * enabled property is removed from the map altogether.
     *
     * @param properties The query properties (parameters)
     */
    private void checkEnabledParameter(final Map<String, Object> properties) {
        if (!_isAuditable) {
            return;
        }
        Object enabled = properties.get(ENABLED_PROPERTY);
        if (enabled == null) {
            properties.put(ENABLED_PROPERTY, true);
        } else if (enabled instanceof String && StringUtils.equals(ENABLED_ALL, (String) enabled)) {
            properties.remove(ENABLED_PROPERTY);
        }
    }

    private boolean requiresEnabledCriterion(final HibernatePaginatedRequest request) {
        if (!_isAuditable) {
            return false;
        }
        return requiresEnabledCriterion(request.getCriterion());
    }

    private boolean requiresEnabledCriterion(final Map<String, HibernateFilter> filters) {
        // If not auditable, doesn't require enabled criterion
        if (!_isAuditable) {
            return false;
        }
        // If there is a filter mapped to enabled, doesn't require enabled criterion
        return !filters.containsKey(AbstractHibernateDAO.ENABLED_PROPERTY);
    }

    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(getSession());
    }

    private String extractCacheRegion(final Class<E> type) {
        return type.isAnnotationPresent(org.hibernate.annotations.Cache.class)
               ? type.getAnnotation(org.hibernate.annotations.Cache.class).region()
               : DEFAULT_CACHE_REGION;
    }

    private static Map<String, Object> parameterMap(Object... parameters) {
        if (parameters.length % 2 != 0) {
            throw new RuntimeException("The number of parameters must be even");
        }
        final Map<String, Object> parameterMap = new HashMap<>();
        for (int index = 0; index < parameters.length; index += 2) {
            parameterMap.put((String) parameters[index], parameters[index + 1]);
        }
        return parameterMap;
    }

    private static String getHqlExistsComponent(final String hqlExistsBody, final String type, final String name) {
        return StringSubstitutor.replace(hqlExistsBody, parameters(type, name));
    }
}
