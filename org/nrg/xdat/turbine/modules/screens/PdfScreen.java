//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;


public abstract class PdfScreen extends
SecureReport
{
   /**
    * Set the content type to Pdf. (see RawScreen)
    *
    * @param data Turbine information.
    * @return content type.
    */
    public String getContentType(RunData data)
    {
        return "application/pdf";
    };

    /**
    * Classes that implement this ADT must override this to build the pdf file.
    *
    * @param data RunData
    * @return ByteArrayOutputStream
    * @exception Exception, any old exception.
    */
    protected abstract ByteArrayOutputStream buildPdf (RunData data) throws
Exception;

    /**
    * Overrides & finalizes doOutput in RawScreen to serve the output stream
created in buildPDF.
    *
    * @param data RunData
    * @exception Exception, any old generic exception.
    */
    public final void finalProcessing(RunData data, Context context)
    {
        try {
            ByteArrayOutputStream baos = buildPdf(data);
            if (baos != null)
            {
                HttpServletResponse response = data.getResponse();
                //We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
                data.getResponse().setContentLength(baos.size());
                ServletOutputStream out = response.getOutputStream();
                baos.writeTo(out);
                out.close();

            }
            else
            {
                throw new Exception("output stream from buildPDF is null");
            }
        } catch (IOException e) {
            logger.error("",e);
            try {
                doRedirect(data,"/screens/Index.vm");
            } catch (Exception e1) {
                logger.error("",e1);
            }
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}
