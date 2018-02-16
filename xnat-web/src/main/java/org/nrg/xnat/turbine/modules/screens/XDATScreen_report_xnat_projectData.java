/*
 * web: org.nrg.xnat.turbine.modules.screens.XDATScreen_report_xnat_projectData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.exceptions.IllegalAccessException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.ItemAccessHistory;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;

import java.sql.SQLException;

import static org.nrg.xdat.XDAT.getUserDetails;

/**
 * @author XDAT
 */
@Slf4j
public class XDATScreen_report_xnat_projectData extends SecureReport {
    public void finalProcessing(RunData data, Context context) {
        final XnatProjectdata project = (XnatProjectdata) om;

        final UserI user = getUserDetails();
        assert user != null;

        try {
            if (Permissions.canRead(user, "xnat:subjectData/project", project.getId())) {
                ItemAccessHistory.LogAccess(user, item, "report");
            }

            if (ProjectAccessRequest.CREATED_PAR_TABLE) {
                context.put("par_count", PoolDBUtils.ReturnStatisticQuery("SELECT COUNT(par_id)::int4 AS count FROM xs_par_table WHERE proj_id='" + project.getId() + "'", "count", user.getDBName(), user.getLogin()));
            }

            if (TurbineUtils.GetPassedParameter("topTab", data) != null) {
                context.put("topTab", TurbineUtils.GetPassedParameter("topTab", data));
            }

            if (TurbineUtils.GetPassedParameter("bottomTab", data) != null) {
                context.put("bottomTab", TurbineUtils.GetPassedParameter("bottomTab", data));
            }

            context.put("showImportEventHandlers", XDAT.getBoolSiteConfigurationProperty("showImportEventHandlers", false));

            setDefaultTabs("xnat_projectData_summary_details", "xnat_projectData_summary_management", "xnat_projectData_summary_manage", "xnat_projectData_summary_pipeline", "xnat_projectData_summary_history");
            cacheTabs(context, "xnat_projectData/tabs");
        } catch (XFTInitException e) {
            log.error("An error occurred initializing XFT", e);
        } catch (ElementNotFoundException e) {
            log.error("Did not find the requested element on the item", e);
        } catch (FieldNotFoundException e) {
            log.error("Field not found {}: {}", e.FIELD, e.MESSAGE, e);
        } catch (DBPoolException e) {
            log.error("An error occurred querying with PoolDBUtils", e);
        } catch (SQLException e) {
            log.error("An SQL exception occurred", e);
        } catch (IllegalAccessException e) {
            log.error("An attempt to access an illegal field or method occurred", e);
        } catch (Exception e) {
            log.error("An unknown exception occurred", e);
        }
    }
}
