/**
 * CategoryService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.mail.api.NotificationType;
import org.nrg.notify.api.CategoryScope;
import org.nrg.notify.entities.Category;


/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface CategoryService extends BaseHibernateService<Category> {
    public static String SERVICE_NAME = "CategoryService";

    /**
     * Finds a currently enabled {@link Category category} with the indicated {@link CategoryScope scope} and
     * event. If there is no currently enabled category that meets that criteria, this method returns <b>null</b>.
     * @param scope Indicates the category scope.
     * @param event Indicates the category event.
     * @return The matching category if it exists, otherwise <b>null</b>.
     */
    Category getCategoryByScopeAndEvent(CategoryScope scope, String event);
}
