//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Nov 10, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;

import java.io.BufferedInputStream;
import java.io.File;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.ecs.ConcreteElement;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xft.XFT;
import org.nrg.xft.utils.FileUtils;

/**
 * @author Tim
 *
 */
public abstract class FileScreen extends SecureScreen {

    public String getContentType(RunData data)
    {
 	   return "application/octet-stream";
    }

    public String getContentDisposition(RunData data){
        return null;
    }

    public abstract File getDownloadFile(RunData data, Context context);

    /* (non-Javadoc)
     * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    protected void doBuildTemplate(RunData data, Context context)
            throws Exception {
        File f = getDownloadFile(data,context);
        if (f==null || !f.exists())
        {
        	//if file doesn't exist return this image... Throwing an exception should skip this.
            String path = XFT.GetSettingsDirectory();
            if (!(path.endsWith("/") || path.endsWith("\\")))
            {
                path += "/";
            }
            f = new File(path + "/images/rc.gif");
        }
        
          HttpServletResponse resp =data.getResponse();
          
          //set file name
          String contentDisposition = getContentDisposition(data);
          if (contentDisposition==null){
              data.getResponse().setHeader("Content-Disposition","attachment;filename=" + f.getName());
          }else{
              data.getResponse().setHeader("Content-Disposition",contentDisposition);
          }

          ServletOutputStream sos = null;
          BufferedInputStream bis = null;

          try {
          sos = resp.getOutputStream();

          } catch (Exception e) {
          System.out.println("exception !!!!");
          logger.error(e);
          }

          //write file
          try {
	          bis = new BufferedInputStream(new java.io.FileInputStream(f));
	
	          int bytesRead;
	          final byte[] buffer = new byte[FileUtils.SMALL_DOWNLOAD];
	          while ((bytesRead = bis.read(buffer)) > 0) {
	        	  sos.write(buffer, 0, bytesRead);
	          }
	          bis.close();
          } catch (Exception e) {
              logger.error(e);
          }

    }

    public String getLayout(RunData data)
    {
        // the FileScreen has already retrieved the outputStream and written the response, 
        // so we don't want the layout rendered either
        return null;
    }
    
    public ConcreteElement buildTemplate(RunData data) {
        // the FileScreen has already retrieved the outputStream and written the response, 
        // so we don't want the DefaultPage to try to render the
        // response page and retrieve the output stream again
        data.declareDirectResponse();
        return null;
    }

}
