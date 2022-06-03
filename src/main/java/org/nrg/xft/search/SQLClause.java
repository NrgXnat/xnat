/*
 * core: org.nrg.xft.search.SQLClause
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import org.apache.commons.lang3.RegExUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.db.PoolDBUtils;

import java.util.ArrayList;

/**
 * @author Tim
 */
public interface SQLClause {
    String getElementName();

    //public String getSQLClause() throws Exception;
    String getSQLClause(QueryOrganizerI qo) throws Exception;

    @SuppressWarnings("rawtypes")
    ArrayList getSchemaFields() throws Exception;

    ArrayList<DisplayCriteria> getSubQueries() throws Exception;

    int numClauses();

    default void hackCheck(final String value) throws Exception {
        hackCheck(value, true);
    }

    default void hackCheck(final String value, final boolean hackCheck) throws Exception {
        if (hackCheck && PoolDBUtils.HackCheck(value)) {
            if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                final String type = "VALUE: " + value;
                final String body = RegExUtils.replaceAll(RegExUtils.replaceAll(XDAT.getNotificationsPreferences().getEmailMessageUnauthorizedDataAttempt(),
                                                                                "TYPE",
                                                                                type),
                                                          "USER_DETAILS",
                                                          "");
                AdminUtils.sendAdminEmail("Possible SQL Injection Attempt", body);
            }
            throw new Exception("Invalid search value (" + value + ")");
        }
    }
}

