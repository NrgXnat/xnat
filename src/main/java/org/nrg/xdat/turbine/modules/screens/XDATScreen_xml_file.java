/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_xml_file
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class XDATScreen_xml_file extends RawScreen {
    static Logger logger = Logger.getLogger(XDATScreen_xml.class);

    /**
     * Set the content type to Xml. (see RawScreen)
     *
     * @param data Turbine information.
     * @return content type.
     */
    public String getContentType(RunData data) {
        return "text/xml";
    }

    /**
     * Classes that implement this ADT must override this to build the pdf file.
     *
     * @param data RunData
     * @return ByteArrayOutputStream
     * @throws Exception When something goes wrong.
     */
    protected ByteArrayOutputStream buildXml(RunData data) throws Exception {

        //ByteArrayOutputStream bos = XMLUtils.BeanToByteArrayOutputStream(data.getUser().getTemp("outputObject"),XMLUtils.DEFAULT_MAPPING_FILE_PATH);
        ByteArrayOutputStream bos = null;

        ItemI o = TurbineUtils.GetItemBySearch(data);
        if (o != null) {
            bos = new ByteArrayOutputStream();
            //bos = o.toXML_BOS("http://" + data.getServerName() + ":" + data.getServerPort() + data.getContextPath() + "/" + "schemas/");
            SAXWriter writer = new SAXWriter(bos, true);
            writer.setAllowSchemaLocation(true);
            writer.setLocation(TurbineUtils.GetFullServerPath() + "/schemas/");
            writer.write(o.getItem());
        } else {
            logger.error("No Item Found.");
            TurbineUtils.OutputDataParameters(data);
            data.setScreenTemplate("Index.vm");
        }

        return bos;
    }

    /**
     * Overrides and finalizes doOutput in RawScreen to serve the output stream created in buildPDF.
     *
     * @param data RunData
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("deprecation")
    protected final void doOutput(RunData data) throws Exception {
        ByteArrayOutputStream baos = buildXml(data);
        if (baos != null) {
            String fileName = ((String) TurbineUtils.GetPassedParameter("fileName", data));
            if (fileName == null) {
                final String format = "%1$tm_%1$td_%1$ty_%1$tH_%1$tM_%1$tS.xml";
                System.out.println(format);
                fileName = String.format(format, Calendar.getInstance().getTime());
            }
            HttpServletResponse response = data.getResponse();
            //We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
            response.setContentLength(baos.size());
            TurbineUtils.setContentDisposition(response, fileName);
            ServletOutputStream out = response.getOutputStream();
            baos.writeTo(out);
        } else {
            throw new Exception("output stream from buildPDF is null");
        }
    }
}
