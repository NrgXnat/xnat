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

/**
 * The Interface NotificationService.
 */
public interface NotificationService extends NrgService {

    public abstract CategoryService getCategoryService();

    public abstract void setCategoryService(CategoryService categoryService);
}
