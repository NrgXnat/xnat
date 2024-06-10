/*
 * web: org.nrg.xnat.turbine.modules.actions.FindStudy
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;

/**
 * @author Mike McKay
 */
public class FindStudy extends SecureAction {
    private static final Logger logger = LoggerFactory.getLogger(FindStudy.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache
     * .turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(RunData data, Context context) throws Exception {

        String patientName = data.getParameters().getString("patientName");
        String patientID = data.getParameters().getString("patientID");
        Date studyDate = data.getParameters().getDate("studyDate");
        //XFTTable table = null;
        try {
            //table = PrearcUtils.convertArrayLtoTable(PrearcDatabase.findMyStudy(patientName, patientID, studyDate));
            ArrayList<ArrayList<Object>> results = PrearcDatabase.findMyStudy(patientName, patientID, studyDate);
            context.put("results",results);
            context.put("resultsSize",results.size());
            context.put("displayManager", DisplayManager.GetInstance());
        } catch (Exception e) {
            logger.error(""+e);
        }
        data.setScreenTemplate("FindStudyMatch.vm");
    }
}
