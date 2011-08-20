/**
 * DefaultNotificationServiceImpl
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 17, 2011
 */
package org.nrg.xdat.notifications.services.impl;

import org.nrg.xdat.notifications.api.Category;
import org.nrg.xdat.notifications.daos.CategoryDAO;
import org.nrg.xdat.notifications.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class DefaultNotificationServiceImpl.
 *
 * @author rherrick
 */
@Service
public class DefaultNotificationServiceImpl implements NotificationService {

    /**
     * @return
     * @see org.nrg.xdat.notifications.services.NotificationService#newCategory()
     */
    public Category newCategory() {
        return new Category();
    }

    /**
     * @param category
     * @see org.nrg.xdat.notifications.services.NotificationService#addCategory(org.nrg.xdat.notifications.api.Category)
     */
    @Transactional
    public void addCategory(Category category) {
        _categoryDAO.create(category);
    }
    
    @Autowired
    private CategoryDAO _categoryDAO;
}
