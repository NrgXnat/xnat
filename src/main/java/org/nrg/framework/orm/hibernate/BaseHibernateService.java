/**
 * BaseService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 24, 2011
 */
package org.nrg.framework.orm.hibernate;

import org.nrg.framework.services.NrgService;

/**
 * Defines the basic operations for service classes in the framework. 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface BaseHibernateService<E extends BaseHibernateEntity> extends NrgService {

    /**
     * Gets a new empty entity object. There is no guarantee
     * as to the contents of the entity.
     * @return A new empty entity object.
     */
    public abstract E newEntity();

    /**
     * Adds the submitted entity object to the system. This will always create 
     * an entirely new entity, but if data validation constraints are violated
     * for the particular table or schema, an exception will be thrown.
     * @param entity The new entity to be created.
     */
    public abstract void create(E entity);

    /**
     * Retrieves the entity with the specified ID.
     * @param id The ID of the entity to be retrieved.
     */
    public abstract E retrieve(long id);

    /**
     * Updates the submitted entity.
     * @param entity The entity to update.
     */
    public abstract void update(E entity);

    /**
     * Deletes the entity with the specified ID from the system.
     * @param id The ID of the entity to be deleted.
     */
    public abstract void delete(long id);

    /**
     * Deletes the submitted entity from the system.
     * @param entity The entity to be deleted.
     */
    public abstract void delete(E entity);
}
