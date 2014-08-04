/*
 * org.nrg.xdat.turbine.modules.screens.CSVScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Tim
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class CSVScreen extends RawScreen {
    private static final Logger logger = LoggerFactory.getLogger(CSVScreen.class);

    public String getContentType(RunData data) {
        return "application/msexcel";
    }

    public DisplaySearch getSearch(RunData data) {
        if (TurbineUtils.GetPassedParameter("search_xml", data) != null) {
            String searchXml = ((String) TurbineUtils.GetPassedParameter("search_xml", data));
            if (StringUtils.isBlank(searchXml)) {
                logger.warn("Requested search XML, but found nothing in the run data.");
                return null;
            }

            // Sometimes the XML is escaped.
            if (searchXml.startsWith("&lt;")) {
                searchXml = StringEscapeUtils.unescapeXml(searchXml);
            }

            XDATUser user = TurbineUtils.getUser(data);

            if (user != null) {
                StringReader sr = new StringReader(searchXml);
                InputSource is = new InputSource(sr);
                SAXReader reader = new SAXReader(user);
                try {
                    XFTItem item = reader.parse(is);
                    XdatStoredSearch search = new XdatStoredSearch(item);
                    DisplaySearch ds = search.getCSVDisplaySearch(user);
                    if (logger.isDebugEnabled()) {
                        if (ds != null) {
                            logger.debug("Found display search " + ds.getTitle() + ": " + ds.getDescription());
                        }
                    }
                    return ds;
                } catch (IOException e) {
                    logger.error("", e);
                } catch (SAXException e) {
                    logger.error("", e);
                } catch (XFTInitException e) {
                    logger.error("", e);
                } catch (ElementNotFoundException e) {
                    logger.error("", e);
                } catch (FieldNotFoundException e) {
                    logger.error("", e);
                } catch (Throwable e) {
                    logger.error("", e);
                }
            }
        }
        return TurbineUtils.getSearch(data);
    }

    /**
     * Overrides & finalizes doOutput in RawScreen to serve the output stream
     * created in buildPDF.
     *
     * @param data RunData
     * @throws Exception, any old generic exception.
     */
    @SuppressWarnings("deprecation")
    protected final void doOutput(RunData data) throws Exception {
        DisplaySearch search = getSearch(data);
        search.setPagingOn(false);
        XFTTableI table = search.execute(new CSVPresenter(), TurbineUtils.getUser(data).getLogin());
        search.setPagingOn(true);
        String sb = table.toString(",");
        if (sb != null) {
            Date today = Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
            String fileName = TurbineUtils.getUser(data).getUsername() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".csv";

            try {
                File f = new File(AccessLogger.getAccessLogDirectory() + "history");
                if (!f.exists()) {
                    f.mkdir();
                }
                FileUtils.OutputToFile(sb, AccessLogger.getAccessLogDirectory() + "history" + File.separator + fileName);
            } catch (RuntimeException e) {
                logger.error("Something went wrong trying to write out the data", e);
            }

            HttpServletResponse response = data.getResponse();
            TurbineUtils.setContentDisposition(response, fileName, false);
            ServletOutputStream out = response.getOutputStream();
            out.print(sb);
            out.close();
        } else {
            throw new Exception("output stream from FileScreen::doOutput is null");
        }
    }
}
