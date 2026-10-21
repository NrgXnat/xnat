/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.display.transport.dao;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.display.transport.entities.DisplayArcDefinitionDB;
import org.springframework.stereotype.Repository;

/**
 * @author Tim Olsen
 *
 * DAO Object for management of ArcDefinitions (settings for the join logic in the Search Engine - Display docs)
 */
@Repository
public class ArcDefinitionDAO extends AbstractHibernateDAO<DisplayArcDefinitionDB> {
    public DisplayArcDefinitionDB findByName(final String name, final boolean includeDisabled) {
        return instance(includeDisabled
                        ? findByProperties(parameters("name", name))
                        : findByProperties(parameters("name", name, ENABLED_PROPERTY, true)));
    }
}
