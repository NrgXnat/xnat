/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_pdf
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.MessageHandler;
import org.apache.turbine.Turbine;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.w3c.dom.Document;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class XDATScreen_pdf extends SecureScreen {
    XFTItem item = null;

    /**
     * Set the content type to Pdf. (see RawScreen)
     *
     * @param data Turbine information.
     * @return content type.
     */
    public String getContentType(RunData data) {
        return "application/pdf";
    }

    /**
     * Overrides and finalizes doOutput in RawScreen to serve the output stream created in buildPDF.
     *
     * @param data       RunData
     * @param context    The context.
     */
    public final void finalProcessing(RunData data, Context context) {
        try {
            ByteArrayOutputStream outputStream = buildPdf(data);
            if (outputStream != null) {
                HttpServletResponse response = data.getResponse();
                //We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
                data.getResponse().setContentLength(outputStream.size());
                data.getResponse().setContentType(getContentType(data));
                ServletOutputStream out = response.getOutputStream();
                outputStream.writeTo(out);
                out.close();

            } else {
                throw new Exception("output stream from buildPDF is null");
            }
        } catch (IOException e) {
            logger.error("", e);
            try {
                doRedirect(data, "/screens/Index.vm");
            } catch (Exception e1) {
                logger.error("", e1);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.xdat.turbine.modules.screens.PdfScreen#buildPdf(org.apache.turbine.util.RunData)
     */
    protected ByteArrayOutputStream buildPdf(RunData data) throws Exception {

        ByteArrayOutputStream out = new ByteArrayOutputStream();


        try {
            if (XFT.VERBOSE) {
                System.out.println("PDF: " + item.getXSIType() + " ID: " + item.getStringProperty("ID"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ItemI om = BaseElement.GetGeneratedItem(item);
        // Marshal the person object
        Document doc = om.toJoinedXML();
        //Setup FOP
        Driver driver = new Driver();

        //INSERTED BY TIM 1/23 PER WEBSITE http://xml.apache.org/fop/embedding.html
        Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        MessageHandler.setScreenLogger(logger);
        driver.setLogger(logger);

        driver.setRenderer(Driver.RENDER_PDF);

        //Setup a buffer to obtain the content length
        driver.setOutputStream(out);

        //Setup Transformer
        File f = new File(Turbine.getRealPath("pdf" + File.separator + item.getGenericSchemaElement().getFormattedName() + "_fo.xsl"));
        if (XFT.VERBOSE) {
            System.out.println("Using " + f.getAbsolutePath());
        }
        Source                                 xsltSrc     = new StreamSource(f);
        javax.xml.transform.TransformerFactory tFactory    = javax.xml.transform.TransformerFactory.newInstance();
        Transformer                            transformer = tFactory.newTransformer(xsltSrc);

        //Make sure the XSL transformation's result is piped through to FOP
        Result res = new SAXResult(driver.getContentHandler());
        //Result res = new StreamResult(out);

        //Start the transformation and rendering process
        transformer.transform(new DOMSource(doc), res);


        return out;
    }

    public void doBuildTemplate(RunData data, Context context) {
        try {
            item = TurbineUtils.GetItemBySearch(data);
        } catch (Exception ignored) {
        }

        if (item == null) {
            data.setMessage("Error: No item found.");
            TurbineUtils.OutputPassedParameters(data, context, this.getClass().getName());
        } else {
            try {
                context.put("item", item);

                context.put("element", org.nrg.xdat.schema.SchemaElement.GetElement(item.getXSIType()));
                context.put("search_element", TurbineUtils.GetPassedParameter("search_element", data));
                context.put("search_field", TurbineUtils.GetPassedParameter("search_field", data));
                context.put("search_value", TurbineUtils.GetPassedParameter("search_value", data));

                finalProcessing(data, context);
            } catch (Exception e) {
                data.setMessage(e.toString());
            }
        }

    }
}
