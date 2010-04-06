//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 18, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.presentation.CSVPresenter;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.utils.FileUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
/**
 * @author Tim
 *
 */
public class CSVScreen extends org.apache.turbine.modules.screens.RawScreen
{
    static org.apache.log4j.Logger logger = Logger.getLogger(CSVScreen.class);

	public String getContentType(RunData data)
	  {
		return "application/msexcel";
		 // return "application/octet-stream";
	  };

      
      public DisplaySearch getSearch(RunData data){

          if (data.getParameters().get("search_xml") !=null)
          {
              String search_xml = data.getParameters().get("search_xml");

              XDATUser user = TurbineUtils.getUser(data);
              
              if (user!=null){
                  StringReader sr = new StringReader(search_xml);
                  InputSource is = new InputSource(sr);
                  SAXReader reader = new SAXReader(user);
                  try {
                      XFTItem item = reader.parse(is);
                      XdatStoredSearch search = new XdatStoredSearch(item);
                      if (search!=null){
                          DisplaySearch ds=search.getCSVDisplaySearch(user);
                          return ds;
                      }
                } catch (IOException e) {
                    logger.error("",e);
                } catch (SAXException e) {
                    logger.error("",e);
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                } catch (Throwable e) {
                    logger.error("",e);
                }
              }
          }
          return TurbineUtils.getSearch(data);
      }
	   /**
	   * Overrides & finalizes doOutput in RawScreen to serve the output stream
   created in buildPDF.
	   *
	   * @param data RunData
	   * @exception Exception, any old generic exception.
	   */
      @SuppressWarnings("deprecation")
	   protected final void doOutput(RunData data) throws Exception
	   {
	   	 	DisplaySearch search = getSearch(data);
	   	 	search.setPagingOn(false);
	   	 	XFTTableI table = search.execute(new CSVPresenter(),TurbineUtils.getUser(data).getLogin());
	   	 	search.setPagingOn(true);
			String sb = table.toString(",");
			 if (sb != null)
			 {
				 String fileName = (String) data.getUser().getTemp("fileName");
				 if (fileName == null)
				 {
				   fileName = "results.csv";
				 }else
				 {
				   data.getUser().removeTemp("fileName");
				 }

				java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
				 fileName=TurbineUtils.getUser(data).getUsername() + "_" + (today.getMonth() + 1) + "_" + today.getDate() + "_" + (today.getYear() + 1900) + "_" + today.getHours() + "_" + today.getMinutes() + "_" + today.getSeconds() + ".csv";
				 
				try {
				    File f = new File(org.nrg.xdat.turbine.utils.AccessLogger.getAccessLogDirectory() + "history");
				    if (!f.exists())
				    {
				        f.mkdir();
				    }
					 FileUtils.OutputToFile(sb.toString(),org.nrg.xdat.turbine.utils.AccessLogger.getAccessLogDirectory() + "history" + File.separator + fileName);	
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
				
				 HttpServletResponse response = data.getResponse();
				 //data.getResponse().setContentLength(baos.size());
				 data.getResponse().setHeader("Content-Disposition","inline;filename=" + fileName);
				 ServletOutputStream out = response.getOutputStream();
				 out.print(sb.toString());
				 out.close();
			 }
			 else
			 {
				 throw new Exception("output stream from FileScreen::doOutput is null");
			 }

	   }
}

