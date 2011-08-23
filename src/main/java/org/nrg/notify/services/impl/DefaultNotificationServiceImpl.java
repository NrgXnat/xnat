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

import org.nrg.notify.api.Category;
import org.nrg.notify.daos.CategoryDAO;
import org.nrg.notify.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides a default implementation for the notification service.
 *
 * @author rherrick
 */
@Service
public class DefaultNotificationServiceImpl implements NotificationService {

    /**
     * @return A new empty {@link Category} object.
     * @see NotificationService#newCategory()
     */
    public Category newCategory() {
        return new Category();
    }

    /**
     * @param category Adds the submitted {@link Category object} to the system.
     * @see NotificationService#addCategory(Category)
     */
    @Transactional
    public void addCategory(Category category) {
        _categoryDAO.create(category);
    }
    
    @Autowired
    private CategoryDAO _categoryDAO;
}
