/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.dao;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.display.transport.entities.ElementDisplayDB;
import org.springframework.stereotype.Repository;

/**
 * @author Tim Olsen
 *
 * DAO Object for management of Element Displays (Search Engine - Display docs)
 */
@Repository
public class ElementDisplayDAO extends AbstractHibernateDAO<ElementDisplayDB> {
    /**
     * Find stored ElementDisplay object by elementName
     * @param name
     * @param includeDisabled
     * @return
     */
    public ElementDisplayDB findByElementName(final String name, final boolean includeDisabled) {
        return instance(includeDisabled
                        ? findByProperties(parameters("elementName", name))
                        : findByProperties(parameters("elementName", name, ENABLED_PROPERTY, true)));
    }
}
