/*
 * web: org.nrg.xnat.turbine.modules.actions.DeleteXdatElementSecurity
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.actions.DeleteAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.security.UserI;

public class DeleteXdatElementSecurity extends DeleteAction {

    @Override
    protected void postDelete(RunData data, Context context) {
        final String dataType = TurbineUtils.GetPassedParameter("search_value", data).toString();
        final String[] queries = new String[] { "DELETE FROM xdat_element_access WHERE xdat_element_access_id IN ( select xdat_element_access_id from xdat_element_access xea LEFT JOIN xdat_element_security xes ON xea.element_name=xes.element_name WHERE xes.element_name IS NULL)",
                "DELETE FROM xdat_meta_element WHERE element_name LIKE '%s'",
                "DELETE FROM xdat_meta_element WHERE element_name LIKE '%s_history'",
                "DELETE FROM xdat_meta_element WHERE element_name LIKE '%s_meta_data'",
                "DELETE FROM xs_item_cache WHERE contents LIKE '%%%s%%'" };
        final UserI user = TurbineUtils.getUser(data);
        String login = user.getLogin();
        for (String query : queries) {
        try {
                if (query.contains("%")) {
                    query = String.format(query, dataType);
                }
                PoolDBUtils.ExecuteNonSelectQuery(query, user.getDBName(), login);
        } catch (SQLException exception) {
                _log.error("There was a SQL exception trying to remove the element access data for data type: " + dataType, exception);
            throw new RuntimeException(exception);
        } catch (Exception exception) {
                _log.error("There was an unknown exception trying to remove the element access data for data type: " + dataType, exception);
            throw new RuntimeException(exception);
        }
        }
		Users.clearCache(user);
    }

    private static final Log _log = LogFactory.getLog(DeleteXdatElementSecurity.class);
}
