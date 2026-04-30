/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.xdat.display.transport.services.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xdat.display.SQLFunction;
import org.nrg.xdat.display.SQLObjectI;
import org.nrg.xdat.display.SQLView;
import org.nrg.xdat.display.transport.dao.DisplayStoredViewDAO;
import org.nrg.xdat.display.transport.entities.DisplayStoredViewDB;
import org.nrg.xdat.display.transport.services.DisplayStoredViewService;
import org.python.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tim Olsen
 *
 * Service implementation for StoredViews stored via Hibernate.
 */

@Service
public class HibernateDisplayStoredViewService extends AbstractHibernateEntityService<DisplayStoredViewDB, DisplayStoredViewDAO> implements DisplayStoredViewService {
    @Autowired
    public HibernateDisplayStoredViewService(final DisplayStoredViewDAO dao) {
        _dao = dao;
    }


    /**
     * Finds the SQLObject by name
     *
     * @param name The name for the configuration
     *
     * @return
     */
    @Override
    @Transactional
    public SQLObjectI findSQLObjectByName(final String name) {
        final DisplayStoredViewDB view= getDao().findByName(name,false);
        return wrapSQLObject(view);
    }

    private SQLObjectI wrapSQLObject(final DisplayStoredViewDB view) {
        if(view.isFunction()){
            final SQLFunction sf = new SQLFunction();
            sf.setName(view.getName());
            sf.setContent(view.getSql());
            sf.setSortOrder(view.getSortOrder());
            return sf;
        }else{
            final SQLView sf = new SQLView();
            sf.setName(view.getName());
            sf.setSql(view.getSql());
            sf.setSortOrder(view.getSortOrder());
            return sf;
        }
    }

    @Override
    @Transactional
    public List<SQLView> findSortedViews() {
        final List<SQLView> _return = new ArrayList<>();

        if(getDao()==null){
            _log.error("DAO is NULL!!!!");
        }

        for(final DisplayStoredViewDB view: getDao().getSortedViews()){
            final SQLObjectI v= wrapSQLObject(view);
            if(v instanceof SQLView){
                _return.add((SQLView)v);
            }
        }
        return _return;
    }

    @Override
    @Transactional
    public List<SQLObjectI> findAllSQLObjects() {
        final List<SQLObjectI> _return = Lists.newArrayList();

        for(final DisplayStoredViewDB view: getDao().findAllEnabled()){
            _return.add(wrapSQLObject(view));
        }
        return _return;
    }

    /**
     * Finds and deactivate the view with the given name
     *
     * @param name The name of the view to deactivate
     */
    @Override
    @Transactional
    public void deactivateByName(final String name) {
        final DisplayStoredViewDB view = getDao().findByName(name,false);
        if (view != null) {
            view.setEnabled(false);
        }
    }

    /**
     * Finds and deactivate the view with the given name
     *
     * @param name The name of the view to deactivate
     */
    @Override
    @Transactional
    public void deleteByName(final String name) {
        final DisplayStoredViewDB view = this.getDao().findByName(name,true);
        if (view != null) {
            view.setEnabled(false);
            this.getDao().delete(view);
        }
    }

    @Override
    @Transactional
    public DisplayStoredViewDB createView(final String name, final String sql, final Integer sortOrder, final boolean isFunction){
        if(getDao().findByName(name,true)==null){
            final DisplayStoredViewDB view = new DisplayStoredViewDB();
            view.setName(name);
            view.setSql(sql);
            view.setFunction(isFunction);
            view.setSortOrder(sortOrder);

            _log.error("Creating DisplayStoredView: " + name);
            this.getDao().create(view);
        }else{
            _log.error("Found existing DisplayStoredView: " + name);
        }

        return getDao().findByName(name,false);
    }

    protected DisplayStoredViewDAO getDao() {
        return _dao;
    }

    private static final Log _log = LogFactory.getLog(HibernateDisplayStoredViewService.class);

    private final DisplayStoredViewDAO _dao;
}
