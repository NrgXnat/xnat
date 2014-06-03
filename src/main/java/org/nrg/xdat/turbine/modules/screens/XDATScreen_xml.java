/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_xml
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;
/**
 * @author Tim
 *
 */
public class XDATScreen_xml extends XMLScreen
{
    // REMOVED CONTENT ... DUPLICATED XMLScreen
//	static Logger logger = Logger.getLogger(XDATScreen_xml.class);
//   /**
//	* Set the content type to Xml. (see RawScreen)
//	*
//	* @param data Turbine information.
//	* @return content type.
//	*/
//	public String getContentType(RunData data)
//	{
//		return "text/xml";
//	};
//
//	/**
//	* Classes that implement this ADT must override this to build the pdf file.
//	*
//	* @param data RunData
//	* @return ByteArrayOutputStream
//	* @exception Exception, any old exception.
//	*/
//	protected ByteArrayOutputStream buildXml (RunData data) throws Exception{
//
//	  	//ByteArrayOutputStream bos = XMLUtils.BeanToByteArrayOutputStream(data.getUser().getTemp("outputObject"),XMLUtils.DEFAULT_MAPPING_FILE_PATH);  
//	  	ByteArrayOutputStream bos = null;
//
//	 	ItemI o = TurbineUtils.GetItemBySearch(data);
//		if (o != null)
//	  	{		  
//			bos = o.toXML_BOS("http://" + data.getServerName() + ":" + data.getServerPort() + data.getContextPath() + "/" + "schemas/");
//	  	}else{
//		  	logger.error("No Item Found.");
//		  	TurbineUtils.OutputDataParameters(data);
//		  	data.setScreenTemplate("Index.vm");
//	  	}
//  
//	  	return bos;
//	}
//
//	/**
//	* Overrides & finalizes doOutput in RawScreen to serve the output stream
//created in buildPDF.
//	*
//	* @param data RunData
//	* @exception Exception, any old generic exception.
//	*/
//	protected final void doOutput(RunData data) throws Exception
//	{
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
//	}
}

