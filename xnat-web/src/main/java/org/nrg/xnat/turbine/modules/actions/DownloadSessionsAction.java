/*
 * web: org.nrg.xnat.turbine.modules.actions.DownloadSessionsAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.modules.actions.ListingAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class DownloadSessionsAction extends ListingAction {
    @Override
    public String getDestinationScreenName(RunData data) {
        return _destination;
    }

    public void finalProcessing(RunData data, Context context) throws Exception {
        final DisplaySearch search = TurbineUtils.getSearch(data);
        if (search == null) {
            data.setMessage("No search criteria were specified. <a href=\"" + ((TemplateLink) context.get("link")).getLink() + "\">Try your search again.</a>");
            data.setScreenTemplate(_destination = "Error.vm");
            return;
        }

        UserI user = XDAT.getUserDetails();
        if (user == null) {
            throw new Exception("Invalid User.");
        }

        search.setPagingOn(false);

        // Load search results into a table
        final XFTTable table = (XFTTable) search.execute(null, user.getLogin());
        search.setPagingOn(true);

        // Acceptable display field ids for the session ID
        String sessionIDHeader = null;
        for (String key : HEADERS) {
            if (table.getColumnIndex(key) != null) {
                sessionIDHeader = key;
                break;
            }
        }

        if (sessionIDHeader == null) {
            logger.error("Missing expected display field for " + search.getRootElement().getFullXMLName() + " download feature (SESSION_ID, EXPT_ID, or ID)");
            throw new Exception("Missing expected ID display field.");
        }

        table.resetRowCursor();
        while (table.hasMoreRows()) {
            data.getParameters().add("sessions", (String) table.nextRowHash().get(sessionIDHeader));
        }
    }

    private static final Logger       logger  = LoggerFactory.getLogger(DownloadSessionsAction.class);
    private static final List<String> HEADERS = Arrays.asList("session_id", "expt_id", "id");

    private String _destination = "XDATScreen_download_sessions.vm";
}
