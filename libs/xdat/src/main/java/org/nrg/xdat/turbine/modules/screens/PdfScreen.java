/*
 * core: org.nrg.xdat.turbine.modules.screens.PdfScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;


@SuppressWarnings("unused")
public abstract class PdfScreen extends SecureReport
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
    }

    /**
    * Classes that implement this ADT must override this to build the pdf file.
    *
    * @param data RunData
    * @return ByteArrayOutputStream* @exception Exception When something goes wrong.
    */
    protected abstract ByteArrayOutputStream buildPdf (RunData data) throws Exception;

    /**
    * Overrides and finalizes {@link RawScreen#doOutput(RunData)}to serve the output stream created in buildPDF.
    *
    * @param data RunData
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