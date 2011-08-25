/**
 * DefinitionService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 24, 2011
 */
package org.nrg.notify.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.notify.api.Definition;

/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface DefinitionService extends BaseHibernateService<Definition>  {
    public static String SERVICE_NAME = "DefinitionService";
}
