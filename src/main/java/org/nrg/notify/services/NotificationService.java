/**
 * NotificationService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.services;

import org.nrg.framework.services.NrgService;
import org.nrg.notify.api.Category;

/**
 * The Interface NotificationService.
 */
public interface NotificationService extends NrgService {
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
    public abstract void addCategory(Category category);
}
