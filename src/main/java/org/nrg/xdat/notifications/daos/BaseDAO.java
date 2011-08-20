/**
 * BaseDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 19, 2011
 */
package org.nrg.xdat.notifications.daos;

import java.lang.reflect.ParameterizedType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Sets the base class for notification service DAOs. A basic DAO for any entity type can
 * be easily created just by extending this class, setting the entity class as the parameterized
 * type.
 *
 * @author rherrick
 */
public abstract class BaseDAO<T> {
    protected BaseDAO()
    {
    }

    protected BaseDAO(SessionFactory factory)
    {
        if (_log.isDebugEnabled())
        {
            _log.debug("Adding session factory in constructor: " + factory.hashCode());
        }
        _factory = factory;
    }

    public void setSessionFactory(SessionFactory factory)
    {
        if (_log.isDebugEnabled())
        {
            _log.debug("Setting session factory in setter: " + factory.hashCode());
        }
        _factory = factory;
    }

    public void create(T entity) {
        getSession().save(entity);
    }

    @SuppressWarnings("unchecked")
    public T retrieve(long id) {
        return (T) getSession().get(getParameterizedType(), id);
    }

    public void update(T entity) {
        getSession().update(entity);
    }

    public void delete(T entity) {
        getSession().delete(entity);
    }

    protected Session getSession() {
        try {
            return _factory.getCurrentSession();
        } catch (HibernateException exception) {
            _log.error("Trying to get session for parameterized type: " + getParameterizedType(), exception);
            throw exception;
        }
    }

    protected Class<?> getParameterizedType()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<?>) parameterizedType.getActualTypeArguments()[0];
    }

    private static final Log _log = LogFactory.getLog(BaseDAO.class);

    @Autowired
    private SessionFactory _factory;
}