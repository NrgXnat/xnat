/*
 * org.nrg.xdat.turbine.modules.screens.XMLSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
import org.xml.sax.SAXException;

/**
 * @author timo
 *
 */
public class XMLSearch extends
org.apache.turbine.modules.screens.RawScreen
{
    static org.apache.log4j.Logger logger = Logger.getLogger(XMLSearch.class);
    
   /**
	* Set the content type to Xml. (see RawScreen)
	*
	* @param data Turbine information.
	* @return content type.
	*/
	public String getContentType(RunData data)
	{
		return "text/xml";
	};

	/**
	* Overrides & finalizes doOutput in RawScreen to serve the output stream
created in buildPDF.
	*
	* @param data RunData
	* @exception Exception, any old generic exception.
	*/
	protected final void doOutput(RunData data) throws Exception
	{
	    String username = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("username",data));
	    String password = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("password",data));
	    XDATUser user = TurbineUtils.getUser(data);
        if(user==null){
            if (username != null && password !=null)
            {
                user = Authenticator.Authenticate(new Authenticator.Credentials(username,password));
                data.getSession().invalidate();
            }
        }
        if (user != null)
        {
    	    try {
                String dataType = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("data_type",data));
                String id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("id",data));
                
                GenericWrapperElement element = GenericWrapperElement.GetElement(dataType);
                String functionName= element.getTextFunctionName();

                String query = "SELECT " + functionName + "(";
                int count =0;
                Object[][] keyArray = element.getSQLKeys();

                for (int i = 0; i < keyArray.length; i++) {
                    if (i > 0)
                        query += ",";
                    query+=DBAction.ValueParser(id,((GenericWrapperField)keyArray[i][3]),true);
                }
                query+=",0,FALSE,TRUE,FALSE)";

                String s =(String)PoolDBUtils.ReturnStatisticQuery(query,functionName,element.getDbName(),username);
                XFTItem item = XFTItem.PopulateItemFromFlatString(s,user);
                
                HttpServletResponse response = data.getResponse();
                response.setContentType("text/xml");
                
                ServletOutputStream out = response.getOutputStream();
                
                if (item == null)
                {
                	data.setMessage("No Item found for XML display.");
                	data.setScreenTemplate("Index.vm");
                }else{
                    SAXWriter writer = new SAXWriter(out,true);
                	writer.setAllowSchemaLocation(true);
                	writer.setLocation(TurbineUtils.GetFullServerPath() + "/" + "schemas/");
                	writer.write(item.getItem());
                }
            } catch (TransformerConfigurationException e) {
                logger.error("",e);
            } catch (IllegalArgumentException e) {
                logger.error("",e);
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (SQLException e) {
                logger.error("",e);
            } catch (IOException e) {
                logger.error("",e);
            } catch (TransformerFactoryConfigurationError e) {
                logger.error("",e);
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (SAXException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
		    
	    
		
//		ByteArrayOutputStream baos = buildXml(data);
//		if (baos != null)
//		{
//			HttpServletResponse response = data.getResponse();
//			//We have to set the size to workaround a bug in IE (see com.lowagie iText FAQ)
//			data.getResponse().setContentLength(baos.size());
//			ServletOutputStream out = response.getOutputStream();
//			baos.writeTo(out);
//		}
//		else
//		{
//			throw new Exception("output stream from buildPDF is null");
//		}
	}
}
