package org.nrg.framework.orm.hibernate;

import com.google.common.annotations.Beta;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.InvalidMappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.UnknownEntityTypeException;
import org.nrg.framework.ajax.PaginatedRequest;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(prefix = "_")
@Slf4j
public class QueryBuilder<E extends BaseHibernateEntity> {
    private final Map<String, Join<E, ?>> _joins = new HashMap<>();

    private final EntityManager    _entityManager;
    private final CriteriaBuilder  _builder;
    private final EntityType<E>    _entityType;
    private final CriteriaQuery<E> _query;
    private final Root<E>          _root;

    private int _firstResult;
    private int _pageSize;

    @Builder
    public QueryBuilder(final EntityManager entityManager, final Class<E> entityType) {
        Validate.notNull(entityManager, "EntityManager cannot be null");
        Validate.notNull(entityType, "Entity type cannot be null");
        _entityManager = entityManager;
        _builder       = _entityManager.getCriteriaBuilder();
        _entityType    = _entityManager.getMetamodel().entity(entityType);
        _query         = _builder.createQuery(_entityType.getJavaType());
        if (HibernateUtils.hasEagerlyFetchedCollection(_entityType.getJavaType())) {
            _query.distinct(true);
        }
        _root = _query.from(_entityType);
    }

    public Class<?> getJavaType(final String property) {
        final Attribute<? super E, ?> attribute = _entityType.getAttribute(property);
        if (attribute == null) {
            throw new PropertyNotFoundException("No attribute named \"" + property + "\" found on entity type " + _entityType.getName());
        }
        return attribute.getJavaType();
    }

    public Order getOrder(Pair<PaginatedRequest.SortDir, String> order) {
        return getOrder(order.getKey(), order.getValue());
    }

    public Order getOrder(PaginatedRequest.SortDir sortDir, String sortColumn) {
        return sortDir == PaginatedRequest.SortDir.ASC
               ? getBuilder().asc(getRoot().get(sortColumn))
               : getBuilder().desc(getRoot().get(sortColumn));
    }

    /**
     * Gets a single result from the query. You should only call this when you know your query will return a single
     * result. If a query returns more than one result, this method will throw <pre>NonUniqueResultException</pre>. If
     * there are no results for the query the return value is empty.
     *
     * @return An optional result or empty if no result was returned internally.
     *
     * @throws NonUniqueResultException When more than one result is returned for the query.
     */
    public Optional<E> getResult() {
        try {
            return Optional.of(createTypedQuery().getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<E> getResults() {
        return createTypedQuery().getResultList();
    }

    public QueryBuilder<E> firstResult(int firstResult) {
        _firstResult = firstResult;
        return this;
    }

    public QueryBuilder<E> pageSize(int pageSize) {
        _pageSize = pageSize;
        return this;
    }

    public <J> Join<E, J> join(final String joinProperty) {
        return join(joinProperty, joinProperty, null);
    }

    public <J> Join<E, J> join(final String joinProperty, final JoinType joinType) {
        return join(joinProperty, joinProperty, joinType);
    }

    public <J> Join<E, J> join(final String joinProperty, final String joinAlias) {
        return join(joinProperty, joinAlias, null);
    }

    public <J> Join<E, J> join(final String joinProperty, final String joinAlias, final JoinType joinType) {
        final Join<E, J> join = getRoot().join(joinProperty, ObjectUtils.getIfNull(joinType, () -> JoinType.LEFT));
        _joins.put(joinAlias, join);
        return join;
    }

    public <T> Set<T> distinct(final Class<T> columnType, final String columnName) {
        CriteriaQuery<T> query  = _builder.createQuery(columnType);
        Root<E>          root   = query.from(getEntityType());
        Path<T>          column = root.get(columnName);
        query.distinct(true).select(column);
        return getEntityManager().createQuery(query).getResultStream().collect(Collectors.toSet());
    }

    public CriteriaQuery<E> where(final Expression<Boolean> restriction) {
        return getQuery().where(restriction);
    }

    public CriteriaQuery<E> where(final Predicate... restrictions) {
        return getQuery().where(restrictions);
    }

    public Predicate isNull(String property) {
        return _builder.isNull(getExpression(property));
    }

    public Predicate isEmpty(String property) {
        return _builder.isEmpty(getExpression(property));
    }

    public Predicate isNotNull(String property) {
        return _builder.not(isNull(property));
    }

    public Predicate isMember(String property, Object value) {
        return _builder.isMember(value, getExpression(property));
    }

    public Predicate isNotMember(String property, Object value) {
        return _builder.isNotMember(value, getExpression(property));
    }

    public Predicate eq(String property, Object value) {
        return Objects.nonNull(value) ? _builder.equal(getExpression(property), value) : isNull(property);
    }

    public Predicate ne(String property, Object value) {
        return not(eq(property, value));
    }

    public Predicate lt(String property, Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return _builder.lt(_root.get(property), (Number) value);
        }
        if (Date.class.isAssignableFrom(value.getClass())) {
            return _builder.lessThan(_root.get(property), (Date) value);
        }
        // CACHING: What other types might fit in here?
        return null;
    }

    public Predicate gt(String property, Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return _builder.gt(_root.get(property), (Number) value);
        }
        if (Date.class.isAssignableFrom(value.getClass())) {
            return _builder.greaterThan(_root.get(property), (Date) value);
        }
        // CACHING: What other types might fit in here?
        return null;
    }

    public Predicate le(String property, Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return _builder.le(_root.get(property), (Number) value);
        }
        if (Date.class.isAssignableFrom(value.getClass())) {
            return _builder.lessThanOrEqualTo(_root.get(property), (Date) value);
        }
        // CACHING: What other types might fit in here?
        return null;
    }

    public Predicate ge(String property, Object value) {
        if (Number.class.isAssignableFrom(value.getClass())) {
            return _builder.ge(_root.get(property), (Number) value);
        }
        if (Date.class.isAssignableFrom(value.getClass())) {
            return _builder.greaterThanOrEqualTo(_root.get(property), (Date) value);
        }
        // CACHING: What other types might fit in here?
        return null;
    }

    public Predicate in(String property, Collection<?> values) {
        // Do this below and NOT _builder.in(_root.get(property)).in(values): this caused an extra "in ()" that broke stuff.
        return getExpression(property).in(values);
    }

    public Predicate in(String property, Object... values) {
        return _builder.in(getExpression(property)).in(values);
    }

    public Predicate between(String property, Object lo, Object hi) {
        Validate.notBlank(property, "The property value can't be blank");
        Validate.noNullElements(Arrays.asList(lo, hi), "Can't do between when one or both of the boundaries is null");
        if (lo instanceof Integer) {
            return betweenImpl(property, (int) lo, (int) hi);
        }
        if (lo instanceof Long) {
            return betweenImpl(property, (long) lo, (long) hi);
        }
        if (lo instanceof Float) {
            return betweenImpl(property, (float) lo, (float) hi);
        }
        if (lo instanceof Double) {
            return betweenImpl(property, (double) lo, (double) hi);
        }
        if (lo instanceof Date) {
            return betweenImpl(property, (Date) lo, (Date) hi);
        }
        throw new InvalidMappingException("The property \"" + property + "\"'s type " + _root.get(property).getJavaType().getName() + " does not match the type (" + lo.getClass().getName() + ") of the lower and upper bounds", _entityType.getName(), property);
    }

    public Predicate like(String property, String value) {
        return _builder.like(getExpression(property, String.class), wrapLikeValue(value));
    }

    public Predicate ilike(String property, String value) {
        return _builder.like(_builder.upper(getExpression(property, String.class)), wrapLikeValue(value));
    }

    public Predicate not(Predicate predicate) {
        return _builder.not(predicate);
    }

    public Predicate or(Predicate... predicates) {
        return _builder.or(predicates);
    }

    public Predicate or(List<Predicate> predicates) {
        return or(predicates.toArray(new Predicate[0]));
    }

    public Predicate or(Map<String, Object> properties) {
        return or(mapToPredicates(properties));
    }

    public Predicate and(Predicate... subCriteria) {
        return _builder.and(subCriteria);
    }

    public Predicate and(List<Predicate> predicates) {
        return and(predicates.toArray(new Predicate[0]));
    }

    public Predicate and(Map<String, Object> properties) {
        return and(mapToPredicates(properties));
    }

    public long count() {
        CriteriaQuery<Long> query = _builder.createQuery(Long.class);
        Root<E> root = query.from(_entityType.getJavaType());
        query.select(_builder.count(root));
        query.where(_builder.equal(root.get("enabled"), true));
        return getEntityManager().createQuery(query).getSingleResult();
    }

    /**
     * Adds criteria to the criteria builder for every attribute on the example instance that is not set to null for
     * object fields or the default value for primitive fields.
     *
     * @param example  An instance of the entity type class with one or more properties to search on set.
     * @param excluded Any attributes on the instance that should be ignored.
     */
    @Beta
    // This isn't "beta" so much as it doesn't really work. This is intended to replace the functionality in the
    // org.hibernate.criterion.Example feature. There is no JPA criteria equivalent of this, so we have to do it
    // ourselves.
    public Predicate example(E example, List<String> excluded) {
        return and(getEntityType().getAttributes().stream()
                                  .filter(attribute -> !excluded.contains(attribute.getName()))
                                  .map(attribute -> {
                                      String name   = attribute.getName();
                                      Member member = attribute.getJavaMember();
                                      if (!(member instanceof Method)) {
                                          log.warn("Got a member for attribute {} but it's not a method, what to do?", name);
                                          return null;
                                      }
                                      Method       method = (Method) member;
                                      final Object value;
                                      try {
                                          value = method.invoke(example);
                                      } catch (IllegalAccessException |
                                               InvocationTargetException e) {
                                          log.error("An error occurred trying to invoke the method for attribute {}", name, e);
                                          return null;
                                      }
                                      // This is fine: the method returned null so the value wasn't initialized.
                                      if (value == null) {
                                          log.debug("The attribute {} is null, discarding", name);
                                          return null;
                                      }
                                      // If the return type is an object, and it's not null, then it's an example value.
                                      final Class<?> returnType = method.getReturnType();
                                      if (returnType.isPrimitive()) {// Boolean is special because false is a valid value to search for, so we can't distinguish that.
                                          if (returnType.equals(boolean.class)) {
                                              log.warn("Primitive boolean type found for attribute {}, I'll return it if it's set to true but can't tell if it's meant to be false or not", name);
                                          }
                                          if (HibernateUtils.isDefaultValue(returnType, value)) {
                                              return null;
                                          }
                                      }
                                      return Pair.of(name, returnType.isEnum() ? ((Enum<?>) value).ordinal() : value);
                                  })
                                  .filter(Objects::nonNull)
                                  .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
    }

    protected <T> Expression<T> getExpression(final String property, final Class<? extends T> propertyType) {
        Expression<T> expression = getExpression(property);
        if (!expression.getJavaType().isAssignableFrom(propertyType)) {
            throw new InvalidMappingException("The property \"" + property + "\"'s type " + expression.getJavaType().getName() + " does not match the type (" + propertyType.getName() + ") of the lower and upper bounds", _entityType.getName(), property);
        }
        return expression;
    }

    protected <T> Expression<T> getExpression(final String property) {
        final String[] atoms = StringUtils.split(property, ".", 2);
        if (atoms.length == 1) {
            return _root.get(property);
        }
        final String joinId = atoms[0];
        final String joinOn = atoms[1];
        Join<E, ?>   join   = _joins.get(joinId);
        if (join == null) {
            throw new UnknownEntityTypeException("A join was requested for the property " + joinOn + " but the specified join ID " + joinId + " doesn't exist.");
        }
        return join.get(atoms[1]);
    }

    private TypedQuery<E> createTypedQuery() {
        TypedQuery<E> query = getEntityManager().createQuery(_query);
        if (_firstResult != 0) {
            query.setFirstResult(_firstResult);
        }
        if (_pageSize != 0) {
            query.setMaxResults(_pageSize);
        }
        return query;
    }

    private Predicate betweenImpl(String property, int lo, int hi) {
        return _builder.between(getExpression(property, Integer.class), lo, hi);
    }

    private Predicate betweenImpl(String property, long lo, long hi) {
        return _builder.between(getExpression(property, Long.class), lo, hi);
    }

    private Predicate betweenImpl(String property, float lo, float hi) {
        return _builder.between(getExpression(property, Float.class), lo, hi);
    }

    private Predicate betweenImpl(String property, double lo, double hi) {
        return _builder.between(getExpression(property, Double.class), lo, hi);
    }

    private Predicate betweenImpl(String property, Date lo, Date hi) {
        return _builder.between(getExpression(property, Date.class), lo, hi);
    }

    private Predicate[] mapToPredicates(Map<String, Object> properties) {
        return properties.entrySet().stream()
                         .map(entry -> {
                             Object value = entry.getValue();
                             if (value == null) {
                                 return isNull(entry.getKey());
                             }
                             if (Collection.class.isAssignableFrom(value.getClass())) {
                                 return in(entry.getKey(), (Collection<?>) value);
                             }
                             return eq(entry.getKey(), entry.getValue());
                         })
                         .toArray(Predicate[]::new);
    }

    private static String wrapLikeValue(final String value) {
        return StringUtils.startsWith(value, "%") || StringUtils.endsWith(value, "%")
               ? value
               : StringUtils.wrapIfMissing(value, "%");
    }

    public CriteriaQuery<E> orderBy(final Pair<PaginatedRequest.SortDir, String> ordering) {
        return orderBy(getOrder(ordering));
    }

    public CriteriaQuery<E> orderBy(final List<Pair<PaginatedRequest.SortDir, String>> ordering) {
        return orderBy(ordering.stream().map(this::getOrder).toArray(Order[]::new));
    }

    public CriteriaQuery<E> orderBy(final Order... ordering) {
        return getQuery().orderBy(ordering);
    }
}
