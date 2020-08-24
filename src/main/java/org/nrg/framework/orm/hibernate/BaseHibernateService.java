/*
 * framework: org.nrg.framework.orm.hibernate.BaseHibernateService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import org.nrg.framework.ajax.hibernate.HibernatePaginatedRequest;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.services.NrgService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Represents the basic functionality for a CRUD service in XNAT supporting {@link BaseHibernateEntity basic XNAT
 * data entity} implementations.
 *
 * @param <E> An entity class that implements the {@link BaseHibernateEntity} interface.
 */
public interface BaseHibernateService<E extends BaseHibernateEntity> extends NrgService {
    /**
     * Gets a new entity object, using the entity constructor matching the submitted parameters. Note
     * that the new entity is not yet created and added to the system!
     *
     * @param parameters The parameters passed to the entity constructor
     *
     * @return A new entity object.
     */
    E newEntity(Object... parameters);

    /**
     * Adds the submitted entity object to the system. This will always create
     * an entirely new entity, but if data validation constraints are violated
     * for the particular table or schema, an exception will be thrown.
     *
     * @param entity The new entity to be created.
     *
     * @return The newly created entity.
     */
    E create(E entity);

    /**
     * Creates a new entity similar to the {@link #newEntity(Object...)} method, then adds the submitted
     * entity object to the system. This will always create an entirely new entity, but if data validation
     * constraints are violated for the particular table or schema, an exception will be thrown.
     * <p>
     * This method is a convenience method that combines the functions of the {@link #newEntity(Object...)} and
     * {@link #create(BaseHibernateEntity)} methods.
     *
     * @param parameters The parameters passed to the entity constructor
     *
     * @return A new entity object.
     */
    E create(Object... parameters);

    /**
     * Retrieves the entity with the specified ID.
     *
     * @param id The ID of the entity to be retrieved.
     *
     * @return The requested entity.
     */
    E retrieve(long id);

    /**
     * Retrieves the entity with the specified ID.
     *
     * @param id The ID of the entity to be retrieved.
     *
     * @return The requested entity.
     *
     * @throws NotFoundException If entity is not found.
     */
    E get(long id) throws NotFoundException;

    /**
     * Updates the submitted entity.
     *
     * @param entity The entity to update.
     */
    void update(E entity);

    /**
     * Deletes the entity with the specified ID from the system.
     *
     * @param id The ID of the entity to be deleted.
     */
    void delete(long id);

    /**
     * Deletes the submitted entity from the system.
     *
     * @param entity The entity to be deleted.
     */
    void delete(E entity);

    /**
     * Gets all active enabled objects of the service's parameterized type.
     *
     * @return A list of all of the active enabled objects of the service's parameterized type.
     */
    List<E> getAll();

    /**
     * Get all objects of the service's parameterized type per parameters in paginatedRequest
     * @param paginatedRequest request including filter, sort, page, and size params
     * @return a list of all objects of the service's parameterized type per parameters in paginatedRequest
     */
    List<E> getPaginated(HibernatePaginatedRequest paginatedRequest);

    /**
     * Gets all objects of the service's parameterized type, including those that are inactive or disabled.
     *
     * @return A list of all of the objects of the service's parameterized type, including those that are inactive or disabled.
     */
    List<E> getAllWithDisabled();

    /**
     * Gives a count of the total number of enabled objects of the service's parameterized type.
     *
     * @return A count of all of the active enabled objects of the service's parameterized type.
     */
    long getCount();

    /**
     * Gives a count of the total number of enabled objects of the service's parameterized type, including those that are inactive or disabled.
     *
     * @return A count of all of the objects of the service's parameterized type, including those that are inactive or disabled.
     */
    @SuppressWarnings("unused")
    long getCountWithDisabled();

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
     * Convenience version of {@link #exists(Map)} that creates parameter map on the fly.
     *
     * @param property1 First property name.
     * @param value1    First property value
     * @param property2 Second property name.
     * @param value2    Second property value
     *
     * @return Returns true if an object with the values for the specified properties exists.
     *
     * @see #exists(Map)
     */
    boolean exists(final String property1, final Object value1, final String property2, final Object value2);

    /**
     * Convenience version of {@link #exists(Map)} that creates parameter map on the fly.
     *
     * @param property1 First property name.
     * @param value1    First property value
     * @param property2 Second property name.
     * @param value2    Second property value
     * @param property3 Third property name.
     * @param value3    Third property value
     *
     * @return Returns true if an object with the values for the specified properties exists.
     *
     * @see #exists(Map)
     */
    boolean exists(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3);

    /**
     * Convenience version of {@link #exists(Map)} that creates parameter map on the fly.
     *
     * @param property1 First property name.
     * @param value1    First property value
     * @param property2 Second property name.
     * @param value2    Second property value
     * @param property3 Third property name.
     * @param value3    Third property value
     * @param property4 Fourth property name.
     * @param value4    Fourth property value
     *
     * @return Returns true if an object with the values for the specified properties exists.
     *
     * @see #exists(Map)
     */
    boolean exists(final String property1, final Object value1, final String property2, final Object value2, final String property3, final Object value3, final String property4, final Object value4);

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
     * Refreshes the submitted entity. If the entity's properties have been changed elsewhere, the submitted instance
     * is updated appropriately.
     *
     * @param entity The entity to be refreshed.
     */
    void refresh(E entity);

    /**
     * Refreshes the submitted list of entities. If any of the entities' properties have been changed elsewhere, the
     * instances in the list are updated appropriately.
     *
     * @param entities The entities to be refreshed.
     */
    void refresh(List<E> entities);

    /**
     * Refreshes the submitted list of entities. If any of the entities' properties have been changed elsewhere, the
     * instances in the list are updated appropriately.
     *
     * @param entities The entities to be refreshed.
     */
    @SuppressWarnings({"unchecked", "varargs"})
    void refresh(E... entities);

    /**
     * Refreshes the submitted entity. If the entity's properties have been changed elsewhere, the submitted instance
     * is updated appropriately. The initialize parameter indicates whether the entity should be initialized prior to
     * being returned. Initialization populates any lazily initialized properties of the entity so that those properties
     * can be referenced after the Hibernate session has gone out of scope.
     *
     * @param initialize Indicates whether the entity should be initialized prior to being returned.
     * @param entity     The entity to be refreshed.
     */
    void refresh(boolean initialize, E entity);

    /**
     * Refreshes the submitted list of entities. If any of the entities' properties have been changed elsewhere, the
     * instances in the list are updated appropriately. The initialize parameter indicates whether the entities should
     * be initialized prior to being returned. Initialization populates any lazily initialized properties of the entity
     * so that those properties can be referenced after the Hibernate session has gone out of scope.
     *
     * @param initialize Indicates whether the entity should be initialized prior to being returned.
     * @param entities   The entities to be refreshed.
     */
    void refresh(boolean initialize, List<E> entities);

    /**
     * Refreshes the submitted list of entities. If any of the entities' properties have been changed elsewhere, the
     * instances in the list are updated appropriately. The initialize parameter indicates whether the entities should
     * be initialized prior to being returned. Initialization populates any lazily initialized properties of the entity
     * so that those properties can be referenced after the Hibernate session has gone out of scope.
     *
     * @param initialize Indicates whether the entity should be initialized prior to being returned.
     * @param entities   The entities to be refreshed.
     */
    @SuppressWarnings({"unchecked", "varargs"})
    void refresh(boolean initialize, E... entities);

    void flush();

    /**
     * Gets a list of the available revisions for the entity with the specified ID. These
     * revision numbers can be used when calling {@link #getRevision(long, Number)}.
     *
     * @param id The ID of the entity to retrieve.
     *
     * @return The available revision numbers for the specified entity.
     */
    @Transactional
    List<Number> getRevisions(long id);

    /**
     * Gets the requested revision of the entity with the specified ID. You can get a
     * list of the available revision numbers by calling {@link #getRevisions(long)}.
     *
     * @param id       The ID of the entity to retrieve.
     * @param revision The revision of the entity to retrieve.
     *
     * @return The requested revision of the specified entity.
     */
    @Transactional
    E getRevision(final long id, final Number revision);

    /**
     * Provides a hook for programmatically validating entities before committing them
     * to the database.
     *
     * @param entity The entity to be validated.
     *
     * @return A non-null string with a message if the entity has invalid state, null otherwise.
     */
    String validate(E entity);

    /**
     * Indicates whether entities should be initialized before being returned from transactional service methods.
     * If <b>true</b>, {@link AbstractHibernateEntityService#initialize(BaseHibernateEntity)} is called before returning entities. This
     * deals with the problem of lazily initialized data members being unavailable in the web tier once the Hibernate
     * session is no longer accessible. For performance benefits, you should set this to <b>false</b> when working with
     * a service with the "open session in view" pattern available.
     *
     * @return Whether the service is set to initialize entities prior to returning them.
     *
     * @see org.nrg.framework.orm.hibernate.BaseHibernateService#setInitialize(boolean)
     */
    boolean getInitialize();

    /**
     * Sets whether entities should be initialized before being returned from transactional service methods.
     * If <b>true</b>, {@link AbstractHibernateEntityService#initialize(BaseHibernateEntity)} is called before returning entities. This
     * deals with the problem of lazily initialized data members being unavailable in the web tier once the Hibernate
     * session is no longer accessible. For performance benefits, you should set this to <b>false</b> when working with
     * a service with the "open session in view" pattern available.
     *
     * @param initialize Indicates whether the service should initialize entities prior to returning them.
     *
     * @see BaseHibernateService#getInitialize()
     */
    void setInitialize(final boolean initialize);
}
