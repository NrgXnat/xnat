/**
 * DefaultNotificationServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.notify.services.impl;

import org.nrg.notify.services.CategoryService;
import org.nrg.notify.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides a default implementation for the notification service.
 *
 * @author rherrick
 */
@Service
public class DefaultNotificationServiceImpl implements NotificationService {

    /**
     * Sets the category service for this notification service.
     * @param categoryService The category service to set.
     */
    @Override
    public void setCategoryService(CategoryService categoryService) {
        _categoryService = categoryService;
    }

    /**
     * Gets the current category service instance.
     * @return Returns the category service.
     */
    @Override
    public CategoryService getCategoryService() {
        return _categoryService;
    }

    @Autowired
    private CategoryService _categoryService;
}
