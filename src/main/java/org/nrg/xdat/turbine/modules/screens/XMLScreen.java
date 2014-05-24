//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 19, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.util.RunData;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXWriter;
/**
 * @author Tim
 *
 */
public class XMLScreen extends
org.apache.turbine.modules.screens.RawScreen
{
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
		
		ItemI item = TurbineUtils.getDataItem(data);
		
		if (item==null)
		{
		    item = TurbineUtils.GetItemBySearch(data);
		}
		
		if (item == null)
		{
			data.setMessage("No Item found for XML display.");
			data.setScreenTemplate("Index.vm");
		}else{

            HttpServletResponse response = data.getResponse();
            response.setContentType("text/xml");
            ServletOutputStream out = response.getOutputStream();
            
		    SAXWriter writer = new SAXWriter(out,true);
			writer.setAllowSchemaLocation(true);
			writer.setLocation(TurbineUtils.GetFullServerPath() + "/" + "schemas/");
			writer.write(item.getItem());
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

