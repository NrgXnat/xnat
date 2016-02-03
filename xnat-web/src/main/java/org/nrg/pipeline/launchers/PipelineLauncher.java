/*
 * org.nrg.pipeline.launchers.PipelineLauncher
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.pipeline.launchers;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.utils.TurbineUtils;

import java.io.File;
import java.util.ArrayList;

public abstract class PipelineLauncher {
    public abstract boolean launch(RunData data, Context context);

    protected ArrayList<String> getCheckBoxSelections(RunData data, XnatImagesessiondata imageSession, String type) {
        // Included for backwards compatibility.
        return getCheckBoxSelections(data, type);
    }

    protected ArrayList<String> getCheckBoxSelections(RunData data, XnatImagesessiondata imageSession, String type, int totalCount) {
        // Included for backwards compatibility.
        return getCheckBoxSelections(data, type, totalCount);
    }

    protected ArrayList<String> getCheckBoxSelections(RunData data, String type) {
        ArrayList<String> rtn = new ArrayList<String>();
        String typeStr = type+"_rowcount";
        Integer totalCount = ((Integer)TurbineUtils.GetPassedInteger(typeStr,data));
        if(totalCount != null) {
            for (int i = 0; i < totalCount.intValue(); i++) {
                if (TurbineUtils.HasPassedParameter(type+"_"+i, data)) {
                    rtn.add(((String)TurbineUtils.GetPassedParameter(type+"_"+i,data)));
                }
            }
        }
        return rtn;
    }

    protected ArrayList<String> getCheckBoxSelections(RunData data, String type, int totalCount) {
        ArrayList<String> rtn = new ArrayList<String>();
        for (int i = 0; i < totalCount; i++) {
            if (TurbineUtils.HasPassedParameter(type+"_"+i, data)) {
                rtn.add(((String)TurbineUtils.GetPassedParameter(type+"_"+i,data)));
            }
        }
        return rtn;
    }


    protected String saveParameters(String rootpath, String fileName, Parameters parameters) throws Exception{
        File dir = new File(rootpath);
        if (!dir.exists()) dir.mkdirs();
        File paramFile = new File(rootpath + File.separator + fileName);
        ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
        paramDoc.addNewParameters().set(parameters);
        paramDoc.save(paramFile,new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
        return paramFile.getAbsolutePath();
    }

    protected String getName(String name) {
        String rtn = name;
        int i = name.lastIndexOf(File.separator);
        if (i != -1)
            rtn = name.substring(i+1);
        i = rtn.lastIndexOf(".");
        if (i != -1)
            rtn = rtn.substring(0, i);
        return rtn;
    }

}
