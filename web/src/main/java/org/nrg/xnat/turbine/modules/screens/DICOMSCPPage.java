/*
 * web: org.nrg.xnat.turbine.modules.screens.DICOMSCPPage
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DICOMSCPPage extends SecureScreen {
    @Override
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
        context.put("uploadID", FORMATTER.format(Calendar.getInstance().getTime()));
        context.put("host", XDAT.getSiteConfigPreferences().getSiteUrl());
        context.put("scps", XDAT.getContextService().getBean(DicomSCPManager.class).getDicomSCPInstances());
    }

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyyMMdd_hhmmss");
}
