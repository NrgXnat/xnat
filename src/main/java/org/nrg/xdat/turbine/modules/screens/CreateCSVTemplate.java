/*
 * org.nrg.xdat.turbine.modules.screens.CreateCSVTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.File;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FieldMapping;

public class CreateCSVTemplate extends RawScreen {

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocitySecureScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    @Override
    protected void doOutput(RunData data) throws Exception {
        FieldMapping fm = (FieldMapping)TurbineUtils.GetPassedParameter("fm", data);
        String fm_id = (String)TurbineUtils.GetPassedParameter("fm_id", data);
        if (fm==null && fm_id!=null){
            File f = Users.getUserCacheFile(TurbineUtils.getUser(data),"csv/" + fm_id + ".xml");
            fm  = new FieldMapping(f);
        }

        HttpServletResponse response = data.getResponse();
         //We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
         //data.getResponse().setContentLength(baos.size());
         TurbineUtils.setContentDisposition(data.getResponse(), "template.csv", false);
         ServletOutputStream out = response.getOutputStream();

         StringBuffer sb = new StringBuffer();
         List fields = fm.getFields();
         for(int i=0;i<fields.size();i++){
             String xmlPath=(String)fields.get(i);
             if (i>0)sb.append(",");
             sb.append(xmlPath.substring(xmlPath.lastIndexOf("/")+1));
         }
         out.print(sb.toString());
         out.close();
    }
    public String getContentType(RunData data)
      {
        return "application/msexcel";
      }

}
