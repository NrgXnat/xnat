/*
 * framework: org.nrg.framework.orm.hibernate.AbstractHibernateEntityService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import com.google.common.base.Joiner;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.generics.AbstractParameterizedWorker;
import org.nrg.framework.utilities.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Properties;

abstract public class AbstractHibernateEntityService<E extends BaseHibernateEntity, DAO extends BaseHibernateDAO<E>> extends AbstractParameterizedWorker<E> implements BaseHibernateService<E>, ApplicationContextAware, InitializingBean {
    public AbstractHibernateEntityService() {
        super();
        _isAuditable = HibernateUtils.isAuditable(getParameterizedType());
    }

    /**
     * Gets a new entity object, using the entity constructor matching the submitted parameters. If the entity class has
     * the method <b>setService()</b>, this will set the service instance on the entity. The service instance should
     * always be declared as {@link javax.persistence.Transient}.
     *
     * <p><b>Note:</b> Calling this method with parameters actually calls the {@link #create(BaseHibernateEntity)} method
     * inline. That means the object has already been persisted to the database when it's returned. Calling this method
     * without parameters calls the default constructor for the entity. Since the entity is then uninitialized, this
     * method does not attempt to persist it.
     *
     * @param parameters    The parameters to be passed to the entity constructor. Note that the corresponding
     *                      constructor must already exist on the entity class!
     *
     * @return A new entity object.
     */
    @Override
    @Transactional
    public E newEntity(Object... parameters) {
        try {
            Constructor<E> constructor = getConstructor(parameters);
            E instance = constructor.newInstance(parameters);
            try {
                Method method = getParameterizedType().getMethod("setService", AbstractHibernateEntityService.class);
                method.invoke(instance, this);
            } catch (NoSuchMethodException ignored) {
                // Ignore this here, it just may not have the method.
            }
            if (parameters != null && parameters.length > 0) {
                create(instance);
            }
            return postProcessNewEntity(instance);
        } catch (NrgServiceException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.Instantiation, e);
        }
    }

    /**
     * Adds the submitted entity to the system.
     * @param entity The entity to be added to the system.
     * @see BaseHibernateService#create(BaseHibernateEntity)
     */
    @Override
    @Transactional
    public E create(E entity) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating a new entity: " + entity.toString());
        }
        getDao().create(entity);
        return postProcessNewEntity(entity);
    }

    /**
     * Creates a new entity similar to the {@link #newEntity(Object...)} method, then adds the submitted
     * entity object to the system. This will always create an entirely new entity, but if data validation
     * constraints are violated for the particular table or schema, an exception will be thrown.
     *
     * This method is a convenience method that combines the functions of the {@link #newEntity(Object...)} and
     * {@link #create(BaseHibernateEntity)} methods.
     *
     * @param parameters    The parameters passed to the entity constructor
     * @return A new entity object.
     */
    @Override
    @Transactional
    public E create(Object... parameters) {
        final E entity = newEntity(parameters);
        return postProcessNewEntity(entity);
    }

    /**
     *
     * @see BaseHibernateService#retrieve(long)
     */
    @Override
    @Transactional
    public E retrieve(long id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Retrieving entity for ID: " + id);
        }
        final E entity;
        if (_isAuditable) {
            entity = getDao().findEnabledById(id);
        } else {
            entity = getDao().retrieve(id);
        }
        if (_initialize) {
            initialize(entity);
        }
        return entity;
    }

    /**
     * @see BaseHibernateService#get(long)
     */
    @Override
    @Transactional
    public E get(long id) throws NotFoundException {
        final E entity = retrieve(id);
        if (entity == null) {
            throw new NotFoundException("Could not find entity with ID " + String.valueOf(id));
        }
        return entity;
    }

    /**
     * @see BaseHibernateService#update(BaseHibernateEntity)
     */
    @Override
    @Transactional
    public void update(E entity) {
        if (_log.isDebugEnabled()) {
            _log.debug("Updating entity for ID: " + entity.getId());
        }
        getDao().update(entity);
    }

    /**
     * @see BaseHibernateService#delete(BaseHibernateEntity)
     */
    @Override
    @Transactional
    public void delete(E entity) {
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting entity for ID: " + entity.getId());
        }
        if (_isAuditable) {
            entity.setEnabled(false);
            entity.setDisabled(new Date());
            getDao().update(entity);
        } else {
            getDao().delete(entity);
        }
    }

    /**
     * @see BaseHibernateService#delete(long)
     */
    @Override
    @Transactional
    public void delete(long id) {
        if (_log.isDebugEnabled()) {
            _log.debug("Deleting entity for ID: " + id);
        }
        delete(getDao().retrieve(id));
    }

    @Override
    @Transactional
    public List<E> getAll() {
        _log.debug("Getting all enabled entities");
        final List<E> list = getDao().findAllEnabled();
        if (_initialize) {
            for (final E entity : list) {
                initialize(entity);
            }
        }
        return list;
    }

    @Override
    @Transactional
    public List<E> getAllWithDisabled() {
        _log.debug("Getting all enabled and disabled entities");
        final List<E> list = getDao().findAll();
        if (_initialize) {
            for (final E entity : list) {
                initialize(entity);
            }
        }
        return list;
    }

    @Override
    @Transactional
    public long getCount() {
        return getDao().countAllEnabled();
    }

    @Override
    @Transactional
    public long getCountWithDisabled() {
        return getDao().countAll();
    }

    @Override
    @Transactional
    public void refresh(E entity) {
        refresh(true, entity);
    }

    @Override
    @Transactional
    public void refresh(List<E> entities) {
        refresh(true, entities);
    }

    @SuppressWarnings("ImplicitSubclassInspection")
    @SafeVarargs
    @Override
    @Transactional
    public final void refresh(E... entities) {
        refresh(true, entities);
    }

    @Override
    @Transactional
    public void refresh(boolean initialize, E entity) {
        getDao().refresh(initialize, entity);
    }

    @Override
    @Transactional
    public void refresh(boolean initialize, List<E> entities) {
        for (E entity : entities) {
            getDao().refresh(initialize, entity);
        }
    }

    @SuppressWarnings("ImplicitSubclassInspection")
    @SafeVarargs
    @Override
    @Transactional
    public final void refresh(boolean initialize, E... entities) {
        for (E entity : entities) {
            getDao().refresh(initialize, entity);
        }
    }

    @Override
    @Transactional
    public void flush() {
        getDao().flush();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("ImplicitSubclassInspection")
    @Override
    @Transactional
    public List<Number> getRevisions(final long id) {
        return getDao().getRevisions(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public E getRevision(final long id, @Nonnull final Number revision) {
        if (revision.longValue() < 1) {
            throw new IllegalArgumentException("Revision numbers for entities start at 1, anything less than that is invalid.");
        }
        return getDao().getRevision(id, revision);
    }

    /**
     * Provides a default validation method that can be overridden in specific implementations. This implementation
     * always returns <b>null</b>, i.e. entities are always considered to be in a valid state. Overriding
     * implementations should return a non-null string message for entities that are in an invalid state, but otherwise
     * return null.
     *
     * Note, though, that Hibernate will automatically validate entities that are annotated with validation criteria if
     * it finds a validation provider on the classpath (it is in XNAT builder, so all XNAT entities will be validated on
     * save). Given that, there may not be much need for this method, but we'll leave it here for now. Someday there may
     * be need for validation in the business layer (here) as well.
     */
    @Override
    public String validate(E entity) {
        return null;
    }

    /**
     * Indicates whether entities should be initialized before being returned from transactional service methods.
     * If <b>true</b>, {@link #initialize(BaseHibernateEntity)} is called before returning entities. This
     * deals with the problem of lazily initialized data members being unavailable in the web tier once the Hibernate
     * session is no longer accessible. For performance benefits, you should set this to <b>false</b> when working with
     * a service with the "open session in view" pattern available.
     * @see org.nrg.framework.orm.hibernate.BaseHibernateService#setInitialize(boolean)
     * @return Whether the service is set to initialize entities prior to returning them.
     */
    @Override
    public boolean getInitialize() {
        return _initialize;
    }

    /**
     * Sets whether entities should be initialized before being returned from transactional service methods.
     * If <b>true</b>, {@link #initialize(BaseHibernateEntity)} is called before returning entities. This
     * deals with the problem of lazily initialized data members being unavailable in the web tier once the Hibernate
     * session is no longer accessible. For performance benefits, you should set this to <b>false</b> when working with
     * a service with the "open session in view" pattern available.
     * @param initialize    Indicates whether the service should initialize entities prior to returning them.
     * @see BaseHibernateService#getInitialize()
     */
    public void setInitialize(final boolean initialize) {
        _initialize = initialize;
    }

    /**
     * Method to initialize entity. By default, calls {@link BaseHibernateDAO#initialize(BaseHibernateEntity)}, but this
     * can be overridden.
     *
     * @param entity Entity object to initialize.
     *
     * @see #getInitialize()
     */
    public void initialize(final E entity) {
        getDao().initialize(entity);
    }

    @Override
    public void setApplicationContext(@Nonnull final ApplicationContext context) throws BeansException {
        _context = context;
    }

    /**
     * Checks to see if entities should be initialized before being returned from transactional service methods. See the
     * {@link #getInitialize()} method for more information.
     */
    @Override
    public void afterPropertiesSet() {
        final Properties properties = getContext().getBean("hibernateProperties", Properties.class);
        if (properties.containsKey("xnat.initialize_entities")) {
            setInitialize(Boolean.parseBoolean(properties.getProperty("xnat.initialize_entities")));
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected E postProcessNewEntity(final E entity) {
        return entity;
    }

    /**
     * Gets the DAO configured for the service instance.
     * @return The DAO object.
     */
    protected DAO getDao() {
        return _dao;
    }

    protected ApplicationContext getContext() {
        return _context;
    }

    private Constructor<E> getConstructor(final Object[] parameters) throws NrgServiceException {
        if (parameters == null || parameters.length == 0) {
            return Reflection.getConstructorForParameters(getParameterizedType());
        }
        final Class<?>[] types = Reflection.getClassTypes(parameters);
        Constructor<E> constructor = Reflection.getConstructorForParameters(getParameterizedType(), types);
        if (constructor == null) {
            throw new NrgServiceException(NrgServiceError.Instantiation, "No constructor available for the class " + getParameterizedType().getName() + " that matches the submitted signature: (" + displayTypes(types) + ")");
        }
        return constructor;
    }

    private String displayTypes(final Class<?>[] types) {
        if (types == null) {
            return "Default constructor";
        }
        return Joiner.on(", ").join(types);
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractHibernateEntityService.class);

    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringAutowiredFieldsWarningInspection"})
    @Autowired
    private DAO _dao;

    private ApplicationContext _context;
    private boolean _isAuditable;
    private boolean _initialize = true;
}
