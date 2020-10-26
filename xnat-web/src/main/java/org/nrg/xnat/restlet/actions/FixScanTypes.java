/*
 * web: org.nrg.xnat.restlet.actions.FixScanTypes
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;


/**
 * @author Timothy R. Olsen <olsent@wustl.edu>
 */
@Getter(AccessLevel.PRIVATE)
@Accessors(prefix = "_")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FixScanTypes {
    public Boolean call() throws Exception {
        if (_experiment instanceof XnatImagesessiondata) {
            ((XnatImagesessiondata) _experiment).fixScanTypes();
        }
        if (isAllowSave()) {
            if (!SaveItemHelper.authorizedSave(getExperiment(), getUser(), false, false, getEventMeta())) {
                return Boolean.FALSE;
            }
            MaterializedView.deleteByUser(getUser());
            final Integer quarantineCode = getProject().getArcSpecification().getQuarantineCode();
            if (quarantineCode != null && quarantineCode.equals(1)) {
                _experiment.quarantine(getUser());
            }
        }
        return Boolean.TRUE;
    }

    private final XnatExperimentdata _experiment;
    private final UserI              _user;
    private final XnatProjectdata    _project;
    private final boolean            _allowSave;
    private final EventMetaI         _eventMeta;
}
