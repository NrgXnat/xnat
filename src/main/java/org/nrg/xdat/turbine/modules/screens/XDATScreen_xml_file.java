/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_xml_file
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
@Slf4j
public class XDATScreen_xml_file extends RawScreen {
    public static final String XML_FILENAME_FORMAT = "%1$tm_%1$td_%1$ty_%1$tH_%1$tM_%1$tS.xml";

    /**
     * Set the content type to Xml. (see RawScreen)
     *
     * @param data Turbine information.
     * @return content type.
     */
    public String getContentType(final RunData data) {
        return "text/xml";
    }

    /**
     * Classes that implement this ADT must override this to build the pdf file.
     *
     * @param data RunData
     * @return ByteArrayOutputStream
     * @throws Exception When something goes wrong.
     */
    protected ByteArrayOutputStream buildXml(final RunData data) throws Exception {
        final ItemI item = TurbineUtils.GetItemBySearch(data);
        if (item != null) {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final SAXWriter writer = new SAXWriter(outputStream, true);
            writer.setAllowSchemaLocation(true);
            writer.setLocation(TurbineUtils.GetFullServerPath() + "/schemas/");
            writer.write(item.getItem());
            return outputStream;
        } else {
            log.error("No Item Found.");
            TurbineUtils.OutputDataParameters(data);
            data.setScreenTemplate("Index.vm");
            return null;
        }
    }

    /**
     * Overrides and finalizes doOutput in RawScreen to serve the output stream created in buildPDF.
     *
     * @param data RunData
     * @throws Exception When something goes wrong.
     */
    protected final void doOutput(final RunData data) throws Exception {
        try (final ByteArrayOutputStream outputStream = buildXml(data)) {
            if (outputStream != null) {
                final String fileName = StringUtils.defaultIfBlank((String) TurbineUtils.GetPassedParameter("fileName", data), String.format(XML_FILENAME_FORMAT, Calendar.getInstance().getTime()));
                log.debug("Writing output for XML to filename {}", fileName);

                //We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
                final HttpServletResponse response = data.getResponse();
                response.setContentLength(outputStream.size());
                TurbineUtils.setContentDisposition(response, fileName);
                outputStream.writeTo(response.getOutputStream());
            } else {
                throw new Exception("Output stream from buildXml() is null");
            }
        }
    }
}
