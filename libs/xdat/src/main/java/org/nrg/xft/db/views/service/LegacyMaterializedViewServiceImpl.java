/*
 * core: org.nrg.xft.db.views.service.LegacyMaterializedViewServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db.views.service;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.db.views.LegacyMaterializedViewImpl;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

@Slf4j
public class LegacyMaterializedViewServiceImpl implements MaterializedViewServiceI {
    @Override
    public void deleteViewsByUser(final UserI user) throws Exception {
        for (final MaterializedViewI view : MaterializedViewManager.getMaterializedViewManager().getViewsByUser(user, this)) {
            view.delete();
        }
    }

    @Override
    public MaterializedViewI getViewByTablename(final String tablename, final UserI user) throws Exception {
        return MaterializedViewManager.getMaterializedViewManager().getViewByTablename(tablename, user, this);
    }

    @Override
    public MaterializedViewI getViewBySearchID(final String searchId, final UserI user) throws Exception {
        return MaterializedViewManager.getMaterializedViewManager().getViewBySearchID(searchId, user, this);
    }

    @Override
    public MaterializedViewI createView(final UserI user) {
        return new LegacyMaterializedViewImpl(user);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public MaterializedViewI populateView(final Hashtable table, final UserI user) {
        return new LegacyMaterializedViewImpl(table, user);
    }

    @Override
    public void save(final MaterializedViewI view) throws Exception {
        view.save();
    }

    @Override
    public void delete(final MaterializedViewI view) throws Exception {
        view.delete();
    }
}
