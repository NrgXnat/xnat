/*
 * org.nrg.xnat.turbine.modules.actions.PlexiViewerSpec
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.plexiViewer.manager.PlexiSpecDocReader;
import org.nrg.xdat.turbine.modules.actions.SecureAction;

public class PlexiViewerSpec extends SecureAction {

    static Logger logger = Logger.getLogger(PlexiViewerSpec.class);
    
    public void doPerform(RunData data, Context context){
        boolean rtn = PlexiSpecDocReader.GetInstance().refresh();
        String msg = "PlexiViewerSpec file was refreshed with " + (rtn?"success":"failure");
        logger.info(msg);
        data.setMessage(msg);
        //data.getParameters().add("popup", "true");
        //data.setScreenTemplate("ClosePage.vm");
    }
}
