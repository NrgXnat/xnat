/*
 * web: org.nrg.xnat.turbine.modules.screens.Index
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Index extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        // TODO: put all this in a method in the theme service with an optional requested page parameter
        final String themedRedirect = themeService.getThemePage("Landing");
        if (StringUtils.isNotBlank(themedRedirect)) {
            context.put("themedRedirect", themedRedirect);
            return;
        }

        final UserI user = XDAT.getUserDetails();
        assert user != null;

        final UserHelperServiceI userHelper = UserHelper.getUserHelperService(user);

        if (TurbineUtils.GetPassedParameter("node", data) != null) {
            context.put("node", TurbineUtils.GetPassedParameter("node", data));
        }

        ProjectAccessRequest.CreatePARTable();

        if (StringUtils.isBlank(user.getEmail())) {
            data.setMessage("WARNING: A valid email account is required for many features.  Please use the (edit) link at the top of the page to add a valid email address to your user account.");
        } else {
            context.put("par_count", PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(par_id)::int4 AS count FROM xs_par_table WHERE approval_date IS NULL AND LOWER(email)='" + user.getEmail().toLowerCase() + "'", "count", user.getDBName(), user.getLogin()));
        }

        final Date lastLogin = userHelper.getPreviousLogin();
        if (lastLogin != null) {
            context.put("last_login", lastLogin);
        }

        context.put("proj_count", XDAT.getTotalCounts().get(XnatProjectdata.SCHEMA_ELEMENT_NAME));
        context.put("sub_count", XDAT.getTotalCounts().get(XnatSubjectdata.SCHEMA_ELEMENT_NAME));
        context.put("isd_count", XDAT.getTotalCounts().get(XnatImagesessiondata.SCHEMA_ELEMENT_NAME));

        final Map<String, String> codeMap = XDAT.getSiteConfigPreferences().getMainPageSearchDatatypeOptions()
                .stream()
                .map(dataType -> Pair.of(dataType, getElementSecurityCode(dataType)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));

        context.put("codeMap", codeMap);
    }

    private String getElementSecurityCode(String dataType) {
        String elementSecurityCode = "";
        try {
            elementSecurityCode =  ElementSecurity.GetElementSecurity(dataType).getCode();
        } catch (Exception ignored) {

        }
        return elementSecurityCode;
    }
}
