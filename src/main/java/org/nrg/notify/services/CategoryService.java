/**
 * CategoryService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 24, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.services.NrgService;
import org.nrg.notify.api.Category;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface CategoryService extends NrgService {

    public static String SERVICE_NAME = "CategoryService";
    
    /**
     * Gets a new empty {@link Category} object. There is no guarantee
     * as to the contents of the category object.
     * @return A new empty {@link Category} object.
     */
    public abstract Category newCategory();

    /**
     * Adds the submitted {@link Category} category object to the system.
     * This will always create an entirely new category, but if the {@link
     * Category#getScope()} and {@link Category#getEvent()} duplicate an
     * existing category, an exception will be thrown.
     * @param category The new {@link Category} to be created.
     */
    public abstract void create(Category category);

    /**
     * Retrieves the category with the specified ID.
     * @param id The ID of the category to be retrieved.
     */
    public abstract Category retrieveCategory(long id);

    /**
     * Updates the submitted category.
     * @param category The category to update.
     */
    public abstract void update(Category category);

    /**
     * Deletes the category with the specified ID from the system.
     * @param id The ID of the category to be deleted.
     */
    public abstract void deleteCategory(long id);

    /**
     * Deletes the submitted category from the system.
     * @param category
     */
    public abstract void deleteCategory(Category category);
}
