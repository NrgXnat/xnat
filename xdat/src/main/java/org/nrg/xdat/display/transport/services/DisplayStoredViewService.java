/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.display.SQLObjectI;
import org.nrg.xdat.display.SQLView;
import org.nrg.xdat.display.transport.entities.DisplayStoredViewDB;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DisplayStoredViewService  extends BaseHibernateService<DisplayStoredViewDB> {
    /**
     * get the SQL view/function by name
     * @param name
     * @return
     */
    @Transactional
    SQLObjectI findSQLObjectByName(String name);

    /**
     * get all enabled SQL View objects sorted by sourceType (elementName), sourceOrder (order in original doc)
     * @return
     */
    @Transactional
    List<SQLView> findSortedViews();

    /**
     * @return
     */
    @Transactional
    List<SQLObjectI> findAllSQLObjects();

    /**
     * @param name
     */
    @Transactional
    void deactivateByName(String name);

    /**
     * @param name
     */
    @Transactional
    void deleteByName(String name);

    /**
     * @param name
     * @param sql
     * @param sortOrdwer
     * @param isFunction
     * @return
     */
    @Transactional
    DisplayStoredViewDB createView(String name, String sql, Integer sortOrdwer, boolean isFunction);
}
