/*
 * framework: org.nrg.framework.orm.hibernate.BaseHibernateDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Hibernate;
import org.nrg.framework.ajax.PaginatedRequest.SortDir;
import org.nrg.framework.ajax.hibernate.HibernateFilter;
import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface BaseHibernateDAO<E extends BaseHibernateEntity> {
    /**
     * Creates (i.e. persists to the database) the submitted entity.
     *
     * @param entity The entity instance to create
     *
     * @return The newly created entity
     */
    Serializable create(final E entity);

    /**
     * Retrieves the entity instance with the specified ID.
     *
     * @param id The ID of the entity to retrieve
     *
     * @return The entity instance with the specified ID if it exists, null otherwise.
     */
    E retrieve(final long id);

    /**
     * Updates the submitted entity.
     *
     * @param entity The entity instance to update
     */
    void update(final E entity);

    /**
     * Deletes the submitted entity.
     *
     * @param entity The entity instance to delete
     */
    void delete(final E entity);

    /**
     * {@link #create(BaseHibernateEntity) Creates} the submitted entity if it doesn't already exist and {@link
     * #update(BaseHibernateEntity) updates} it if it does.
     *
     * @param entity The entity instance to create or update
     */
    void saveOrUpdate(final E entity);

    /**
     * Retrieves all entities of the parameterized type in the database.
     */
    List<E> findAll();

    /**
     * Retrieves all enabled entities of the parameterized type in the database.
     */
    List<E> findAllEnabled();

    /**
     * Retrieves all entities of the parameterized type matching the {@link HibernateFilter criteria} in the {@link
     * HibernatePaginatedRequest}.
     */
    List<E> findPaginated(HibernatePaginatedRequest listingRequest);

    /**
     * Returns the number of entities of the parameterized type stored in the database.
     *
     * @return The number of entities of the parameterized type.
     */
    long countAll();

    /**
     * Returns the number of enabled entities of the parameterized type stored in the database.
     *
     * @return The number of enabled entities of the parameterized type.
     */
    long countAllEnabled();

    /**
     * Returns the number of entities of the parameterized type stored in the database that have the specified value for
     * the indicated property.
     *
     * @return The number of entities of the parameterized type with the indicated property value.
     */
    long countByProperty(String property, Object value);

    /**
     * Returns the number of entities with properties matching the values in the submitted properties.
     *
     * @param properties A map of the property names and values on which to search
     *
     * @return The number of entities whose values for the specified properties matches the given values.
     */
    long countByProperties(Map<String, Object> properties);

    /**
     * Finds all entities with a value of <pre>value</pre> for the specified property.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     *
     * @return All entities whose value for the specified property matches the given value.
     */
    List<E> findByProperty(String property, Object value);

    /**
     * Finds up to <pre>pageSize</pre> entities with a value of <pre>value</pre> for the specified property, starting
     * with the <pre>offset</pre><sup>th</sup> result.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     * @param offset   The offset within the list of overall matches to start the returned list
     * @param pageSize The maximum number of results to include in the returned list
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose value for the
     * specified property matches the given value.
     */
    List<E> findByProperty(String property, Object value, int offset, int pageSize);

    /**
     * Finds all entities with a value of <pre>value</pre> for the specified property, ordered by the specified property
     * in the {@link SortDir direction} contained in the <pre>ordering</pre> parameter.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     * @param ordering The direction and property on which the results should be ordered
     *
     * @return All entities whose value for the specified property matches the given value, in the specified order.
     */
    List<E> findByProperty(final String property, Object value, Pair<SortDir, String> ordering);

    /**
     * Finds all entities with a value of <pre>value</pre> for the specified property, ordered by the specified properties
     * in the {@link SortDir directions} contained in the <pre>ordering</pre> parameters.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     * @param ordering The directions and properties on which the results should be ordered
     *
     * @return All entities whose value for the specified property matches the given value, in the specified order.
     */
    List<E> findByProperty(String property, Object value, List<Pair<SortDir, String>> ordering);

    /**
     * Finds up to <pre>pageSize</pre> entities with a value of <pre>value</pre> for the specified property, starting
     * with the <pre>offset</pre><sup>th</sup> result, ordered by the specified property in the {@link
     * SortDir direction} contained in the <pre>ordering</pre> parameter.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     * @param offset   The offset within the list of overall matches to start the returned list
     * @param pageSize The maximum number of results to include in the returned list
     * @param ordering The direction and property on which the results should be ordered
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose value for the
     * specified property matches the given value, in the specified order.
     */
    List<E> findByProperty(String property, Object value, int offset, int pageSize, Pair<SortDir, String> ordering);

    /**
     * Finds up to <pre>pageSize</pre> entities with a value of <pre>value</pre> for the specified property, starting
     * with the <pre>offset</pre><sup>th</sup> result, ordered by the specified properties in the {@link
     * SortDir directions} contained in the <pre>ordering</pre> parameters.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     * @param offset   The offset within the list of overall matches to start the returned list
     * @param pageSize The maximum number of results to include in the returned list
     * @param ordering The directions and properties on which the results should be ordered
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose value for the
     * specified property matches the given value, in the specified order.
     */
    List<E> findByProperty(String property, Object value, int offset, int pageSize, List<Pair<SortDir, String>> ordering);

    /**
     * Finds all entities with properties matching the values in the submitted properties.
     *
     * @param properties A map of the property names and values on which to search
     *
     * @return All entities whose values for the specified properties matches the given values.
     */
    List<E> findByProperties(Map<String, Object> properties);

    /**
     * Finds up to <pre>pageSize</pre> entities with properties matching the values in the submitted properties,
     * starting with the <pre>offset</pre><sup>th</sup> result.
     *
     * @param properties A map of the property names and values on which to search
     * @param offset     The offset within the list of overall matches to start the returned list
     * @param pageSize   The maximum number of results to include in the returned list
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose values for the
     * specified properties matches the given values.
     */
    List<E> findByProperties(Map<String, Object> properties, int offset, int pageSize);

    /**
     * Finds all entities with properties matching the values in the submitted properties, ordered by the specified
     * property in the {@link SortDir direction} contained in the <pre>ordering</pre> parameter.
     *
     * @param properties A map of the property names and values on which to search
     * @param ordering   The direction and property on which the results should be ordered
     *
     * @return All entities whose values for the specified properties matches the given values, in the specified order.
     */
    List<E> findByProperties(final Map<String, Object> properties, Pair<SortDir, String> ordering);

    /**
     * Finds all entities with properties matching the values in the submitted properties, ordered by the specified
     * properties in the {@link SortDir directions} contained in the <pre>ordering</pre> parameters.
     *
     * @param properties A map of the property names and values on which to search
     * @param ordering   The directions and properties on which the results should be ordered
     *
     * @return All entities whose values for the specified properties matches the given values, in the specified order.
     */
    List<E> findByProperties(final Map<String, Object> properties, List<Pair<SortDir, String>> ordering);

    /**
     * Finds up to <pre>pageSize</pre> entities with properties matching the values in the submitted properties,
     * starting with the <pre>offset</pre><sup>th</sup> result, ordered by the specified property in the {@link
     * SortDir direction} contained in the <pre>ordering</pre> parameter.
     *
     * @param properties A map of the property names and values on which to search
     * @param offset     The offset within the list of overall matches to start the returned list
     * @param pageSize   The maximum number of results to include in the returned list
     * @param ordering   The direction and property on which the results should be ordered
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose value for the
     * specified property matches the given value, in the specified order.
     */
    List<E> findByProperties(final Map<String, Object> properties, int offset, int pageSize, Pair<SortDir, String> ordering);

    /**
     * Finds up to <pre>pageSize</pre> entities with properties matching the values in the submitted properties,
     * starting with the <pre>offset</pre><sup>th</sup> result, ordered by the specified property in the {@link
     * SortDir direction} contained in the <pre>ordering</pre> parameters.
     *
     * @param properties A map of the property names and values on which to search
     * @param offset     The offset within the list of overall matches to start the returned list
     * @param pageSize   The maximum number of results to include in the returned list
     * @param ordering   The directions and properties on which the results should be ordered
     *
     * @return All entities, starting at <pre>offset</pre> and up to <pre>pageSize</pre> results, whose value for the
     * specified property matches the given value, in the specified order.
     */
    List<E> findByProperties(final Map<String, Object> properties, int offset, int pageSize, List<Pair<SortDir, String>> ordering);

    /**
     * Finds the entity instance with the specified value for <pre>property</pre>. Note that the specified property
     * must be a unique constraint on the data type.
     *
     * @param property The name of the property on which to search
     * @param value    The value on which to search
     *
     * @return The entity instance with the specified property value if it exists, null otherwise.
     */
    E findByUniqueProperty(String property, Object value);

    /**
     * Finds the entity instance with the specified values for the indicated properties. Note that the specified
     * properties must be a unique constraint on the data type.
     *
     * @param properties A map of the property names and values on which to search
     *
     * @return The entity instance with the specified property values if it exists, null otherwise.
     */
    E findByUniqueProperties(Map<String, Object> properties);

    /**
     * Finds the entity instance with the specified ID.
     *
     * @param id The ID of the entity to retrieve
     *
     * @return The entity instance with the specified ID if it exists, null otherwise.
     */
    E findById(long id);

    /**
     * Finds the entity instance with the specified ID, optionally locking the database row.
     *
     * @param id   The ID of the entity to retrieve
     * @param lock Whether the database row should be locked
     *
     * @return The entity instance with the specified ID if it exists, null otherwise.
     */
    E findById(long id, boolean lock);

    /**
     * Finds the enabled entity instance with the specified ID.
     *
     * @param id The ID of the entity to retrieve
     *
     * @return The entity instance with the specified ID if it exists and is enabled, null otherwise.
     */
    E findEnabledById(long id);

    /**
     * Finds the enabled entity instance with the specified ID, optionally locking the database row.
     *
     * @param id   The ID of the entity to retrieve
     * @param lock Whether the database row should be locked
     *
     * @return The entity instance with the specified ID if it exists and is enabled, null otherwise.
     */
    E findEnabledById(long id, boolean lock);

    /**
     * Finds entities that match the set properties.
     *
     * @param exampleInstance The example to use.
     * @param excludeProperty Properties of the example that should be ignored.
     *
     * @return All entities that match the set properties.
     */
    List<E> findByExample(E exampleInstance, String[] excludeProperty);

    /**
     * Works just like {@link #findByExample(BaseHibernateEntity, String[])}, except that it includes disabled instances
     * of {@link Auditable auditable} entity definitions.
     *
     * @param exampleInstance The example to use.
     * @param excludeProperty Properties of the example that should be ignored.
     *
     * @return All entities that match the set properties.
     *
     * @deprecated Auditable entities should be transitioned to use the Hibernate Envers @Audited annotation.
     */
    @Deprecated
    @SuppressWarnings({"unused"})
    List<E> findAllByExample(E exampleInstance, String[] excludeProperty);

    /**
     * Uses an efficient query to quickly determine whether an object exists on the system with the specified value
     * for the indicated property. The value <b>true</b> for the <b>enabled</b> is presumed unless <b>enabled</b>
     * is explicitly specified.
     *
     * @param property The property to check.
     * @param value    The value to check for.
     *
     * @return Returns true if an object with the value for the specified property exists.
     *
     * @see #exists(Map)
     */
    boolean exists(final String property, final Object value);

    /**
     * Uses an efficient query to quickly determine whether an object exists on the system with the specified values
     * for the indicated properties. The value <b>true</b> for the <b>enabled</b> is presumed unless <b>enabled</b>
     * is explicitly specified.
     *
     * @param parameters The properties and values to check.
     *
     * @return Returns true if an object with the value for the specified properties exists.
     *
     * @see #exists(String, Object)
     */
    boolean exists(final Map<String, Object> parameters);

    /**
     * Gets all distinct values for the specified property.
     *
     * @param columnName The name of the property
     * @param columnType The type of the property
     * @param <T>        The type of the property
     *
     * @return A set of distinct values for the specified property
     */
    <T> Set<T> distinct(final Class<T> columnType, final String columnName);

    /**
     * Refreshes and optionally {@link #initialize(BaseHibernateEntity) initializes} the submitted entity.
     *
     * @param initialize Whether the entity should be initialized after refresh
     * @param entity     The entity to refresh and optionally initialized
     */
    void refresh(boolean initialize, E entity);

    /**
     * Manually flushes the Hibernate session. You should know that you need to do this before you do it!
     */
    void flush();

    /**
     * Method to initialize entity. By default, calls {@link Hibernate#initialize(Object)}, but this
     * can be overridden.
     *
     * @param entity Entity object to initialize.
     */
    void initialize(E entity);

    /**
     * Gets a list of the available revisions for the entity with the specified ID. These
     * revision numbers can be used when calling {@link #getRevision(long, Number)}.
     *
     * @param id The ID of the entity to retrieve.
     *
     * @return The available revision numbers for the specified entity.
     */
    List<Number> getRevisions(final long id);

    /**
     * Gets the requested revision of the entity with the specified ID. You can get a
     * list of the available revision numbers by calling {@link #getRevisions(long)}.
     *
     * @param id       The ID of the entity to retrieve.
     * @param revision The revision of the entity to retrieve.
     *
     * @return The requested revision of the specified entity.
     */
    E getRevision(final long id, final Number revision);
}
