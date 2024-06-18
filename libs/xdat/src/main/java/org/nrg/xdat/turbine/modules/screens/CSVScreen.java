

/*
 * core: org.nrg.xdat.turbine.modules.screens.CSVScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.turbine.modules.screens.RawScreen;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.xml.sax.InputSource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Date;

@Slf4j
public class CSVScreen extends RawScreen {
    public String getContentType(RunData data) {
        return "application/msexcel";
    }

    public DisplaySearch getSearch(RunData data) {
        if (TurbineUtils.GetPassedParameter("search_xml", data) != null) {
            String searchXml = ((String) TurbineUtils.GetPassedParameter("search_xml", data));
            if (StringUtils.isBlank(searchXml)) {
                log.warn("Requested search XML, but found nothing in the run data.");
                return null;
            }

            // Sometimes the XML is escaped.
            if (searchXml.startsWith("&lt;")) {
                searchXml = StringEscapeUtils.unescapeXml(searchXml);
            }

            UserI user = XDAT.getUserDetails();

            if (user != null) {
                StringReader sr = new StringReader(searchXml);
                InputSource is = new InputSource(sr);
                SAXReader reader = new SAXReader(user);
                try {
                    XFTItem item = reader.parse(is);
                    XdatStoredSearch search = new XdatStoredSearch(item);
                    DisplaySearch ds = search.getCSVDisplaySearch(user);
                    if (log.isDebugEnabled()) {
                        if (ds != null) {
                            log.debug("Found display search " + ds.getTitle() + ": " + ds.getDescription());
                        }
                    }
                    return ds;
                } catch (Throwable e) {
                    log.error("", e);
                }
            }
        }
        return TurbineUtils.getSearch(data);
    }

    /**
     * Overrides and finalizes {@link RawScreen#doOutput(RunData)} to serve the output stream.
     *
     * @param data RunData
     * @throws Exception When something goes wrong.
     */
    @SuppressWarnings("deprecation")
    protected final void doOutput(RunData data) throws Exception {
        DisplaySearch search = getSearch(data);
        search.setPagingOn(false);
        XFTTableI table = search.execute(new CSVPresenter(), TurbineUtils.getUser(data).getLogin());
        search.setPagingOn(true);
        final String sb = table.toString(",");
        if (StringUtils.isNotBlank(sb)) {
            Date   today    = Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
            String fileName = TurbineUtils.getUser(data).getUsername() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".csv";

            try {
                final Path xnatHome = XDAT.getContextService().getBean("xnatHome", Path.class);
                if (xnatHome != null) {
                    final Path history = xnatHome.resolve("logs").resolve("history");
                    Files.createDirectories(history);
                    FileUtils.OutputToFile(sb, history.resolve(fileName).toAbsolutePath().toString());
                } else {
                    log.error("Couldn't find the access log directory! Message is orphaned: " + sb);
                }
            } catch (RuntimeException e) {
                log.error("Something went wrong trying to write out the data", e);
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
