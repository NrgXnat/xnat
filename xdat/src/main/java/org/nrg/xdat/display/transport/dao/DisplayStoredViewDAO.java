/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.dao;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xdat.display.transport.entities.DisplayStoredViewDB;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * @author Tim Olsen
 *
 * DAO Object for management of DisplayStoredViews (sql views and functions configured for use by the search engine)
 */
@Repository
public class DisplayStoredViewDAO extends AbstractHibernateDAO<DisplayStoredViewDB> {
    /**
     * Find stored view/function by entity's name
     * @param name
     * @param includeDisabled
     * @return
     */
    public DisplayStoredViewDB findByName(final String name, final boolean includeDisabled) {
        return instance(includeDisabled
                        ? findByProperties(parameters("name", name))
                        : findByProperties(parameters("name", name, ENABLED_PROPERTY, true)));
    }

    /**
     * Get the stored view/function objects, sorted by sourceType, sourceOrder
     * @return
     */
    public List<DisplayStoredViewDB> getSortedViews() {
        return emptyToNull(findByProperties(Collections.emptyMap(), Arrays.asList(asc("sourceType"), asc("sortOrder"))));
    }
}
