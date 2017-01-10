/*
 * web: org.nrg.xnat.restlet.representations.TurbineScreenRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.representations;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.PageLoader;
import org.apache.turbine.services.template.TemplateService;
import org.apache.turbine.services.template.TurbineTemplate;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.ServerData;
import org.apache.turbine.util.TurbineException;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.restlet.rundata.RestletRunData;
import org.nrg.xnat.restlet.servlet.XNATRestletServlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.resource.OutputRepresentation;

import com.noelios.restlet.ext.servlet.ServletCall;
import com.noelios.restlet.http.HttpRequest;

public abstract class TurbineScreenRepresentation extends OutputRepresentation {
	static org.apache.log4j.Logger logger = Logger.getLogger(TurbineScreenRepresentation.class);
	final RunData data;
	final Request request;
	final UserI user;
	final Map<String,Object> params;

	public TurbineScreenRepresentation(MediaType mediaType,Request request, UserI _user,Map<String,Object> params) throws TurbineException{
		super(mediaType);
		this.request=request;
		user=_user;
		this.params=params;
		HttpServletRequest _request = ((ServletCall)((HttpRequest) request).getHttpCall()).getRequest(); 
		HttpServletResponse _response = ((ServletCall)((HttpRequest) request).getHttpCall()).getResponse(); 
		
		data = populateRunData(_request,_response,user,params);
	}

	public TurbineScreenRepresentation(MediaType mediaType,Request request, UserI _user,Map<String,Object> params,Map<String,Object> additionalObjects) throws TurbineException{
		super(mediaType);
		this.request=request;
		user=_user;
		this.params=params;
		HttpServletRequest _request = ((ServletCall)((HttpRequest) request).getHttpCall()).getRequest(); 
		HttpServletResponse _response = ((ServletCall)((HttpRequest) request).getHttpCall()).getResponse(); 
		
		data = populateRunData(_request,_response,user,params);
	}

	@SuppressWarnings("deprecation")
	public void turbineScreen(RunData data,OutputStream out)throws IOException,Exception{
		TemplateService templateService = TurbineTemplate.getService();
        String defaultPage = (templateService == null)
                ? null :templateService.getDefaultPageName(data);

        PrintWriter writer= new PrintWriter(out);

        if(data instanceof RestletRunData){
			((RestletRunData)data).hijackOutput(writer);
		}else{
			throw new RestletTurbineConfigurationException("Inproper Turbine configuration for RESTLET support.");
		}

        PageLoader.getInstance().exec(data, defaultPage);

		//COPIED FROM org.apache.turbine.Turbine.doGet
        if (data.isPageSet() && data.isOutSet() == false)
        {
            // Output the Page.
            data.getPage().output(out);
        }

        writer.flush();
        writer.close();
	}
	
	public void setRunDataParameter(String key, String value)
	{
		data.getParameters().setString(key, value);
	}
	
	public static RunData populateRunData(HttpServletRequest request, HttpServletResponse response,UserI user,final Map<String,Object> params) throws TurbineException{
//		RunDataService rundataService = null;
//		rundataService = TurbineRunDataFacade.getService();
//		if (rundataService == null)
//		{
//		    throw new TurbineException(
//		            "No RunData Service configured!");
//		}
//		RunData data = rundataService.getRunData("restlet",request, response, XNATRestletServlet.REST_CONFIG);

		RestletRunData data = new RestletRunData();
        data.setParameterParser(new org.apache.turbine.util.parser.DefaultParameterParser());
        data.setCookieParser(new org.apache.turbine.util.parser.DefaultCookieParser());

        // Set the request and response.
        data.setRequest(request);
        data.setResponse(response);

        // Set the servlet configuration.
        data.setServletConfig(XNATRestletServlet.REST_CONFIG);

        // Set the ServerData.
        data.setServerData(new ServerData(request));
		
		if(!XDAT.isAuthenticated()) {
			try {
				XDAT.setUserDetails(user);
			} catch (Exception e) {
				logger.error("",e);
			}
		}
		
		//RENAME script name /REST to /app
		data.getServerData().setScriptName("/app");
		
		if(params!=null){
			for(Map.Entry<String,Object> entry:params.entrySet()){
				if(entry.getValue()!=null){
					if(isPrimitiveWrapper(entry.getValue())){
						data.getParameters().add(entry.getKey(), entry.getValue().toString());
					}else{
						data.passObject(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		
						
		return data;
	}
	
	final static List<Class> types=Arrays.asList(new Class[]{Boolean.class,Character.class,Byte.class,Short.class,Integer.class,Long.class,Float.class,Double.class,String.class});
	
	public static boolean isPrimitiveWrapper(Object o){
		return types.contains(o.getClass());
	}
	
	public class RestletTurbineConfigurationException extends Exception{
		private static final long serialVersionUID = 1L;

		public RestletTurbineConfigurationException(String msg){
			super(msg);
		}
	}
	
	@Override
	public void write(OutputStream out) throws IOException {
		try {
	    	data.setScreenTemplate(getScreen());
			turbineScreen(data,out);
		} catch (TurbineException e) {
			logger.error("",e);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public abstract String getScreen();
}
