/**
 * CategoryDAO
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 19, 2011
 */
package org.nrg.xdat.notifications.daos;

import org.nrg.xdat.notifications.api.Category;
import org.springframework.stereotype.Repository;

/**
 * Implements the DAO class for the {@link Category} entity type.
 * 
 * @see BaseDAO
 * @author rherrick
 */
@Repository
public class CategoryDAO extends BaseDAO<Category> {
}
