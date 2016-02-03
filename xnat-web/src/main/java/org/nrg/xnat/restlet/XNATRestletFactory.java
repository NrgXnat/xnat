/*
 * org.nrg.xnat.restlet.XNATRestletFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.restlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xdat.turbine.utils.PropertiesHelper;
import org.restlet.Application;
import org.restlet.VirtualHost;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
public class XNATRestletFactory {
	   private static final String DEFAULT = "DEFAULT";

	private static final String XNAT_RESTLET_PROPERTIES = "xnat-restlet.properties";

	static Logger logger = Logger.getLogger(XNATRestletFactory.class);

	private static final String CLASS_NAME = "className";
	
	private static final String RESTLET_VIRTUAL_HOST_IDENTIFIER = "org.nrg.VirtualHost.impl";
	private static final String[] RESTLET_VIRTUAL_HOST_PROP_OBJECT_FIELDS = new String[]{CLASS_NAME};
	
	private static final Class[] RESTLET_VIRTUAL_HOST_PARAMETER_TYPES=new Class[]{org.restlet.Context.class};
	
	private static final String RESTLET_APP_IDENTIFIER = "org.nrg.Application.impl";
	private static final String[] RESTLET_APP_PROP_OBJECT_FIELDS = new String[]{CLASS_NAME};
	private static final Class[] RESTLET_APP_PARAMETER_TYPES=new Class[]{org.restlet.Context.class};
	 
	//EXAMPLE PROPERTIES FILE conf/xnat-restlet.properties
	//# add additional VirtualHosts using this (If you customize it, you'll need to manually register the STANDARD implementation as well as your custom one.
	//org.nrg.VirtualHost.impl=STANDARD
	//org.nrg.VirtualHost.impl.STANDARD.className=org.nrg.xnat.restlet.XNATVirtualHost
	//
	//org.nrg.VirtualHost.impl=CUSTOM1
	//org.nrg.VirtualHost.impl.CUSTOM1.className=org.some.path.CustomVirtualHost
	//
	//# replace the default Application using this.
	//org.nrg.Application.impl=DEFAULT
	//org.nrg.Application.impl.DEFAULT.className=org.nrg.xnat.restlet.XNATApplication
	
	   private static Configuration config =null;
	   
	   static{
		    try {
			   config=PropertiesHelper.RetrieveConfiguration(new File(XFT.GetConfDir(),XNAT_RESTLET_PROPERTIES));
			} catch (ConfigurationException e) {
				logger.error("",e);
			}
	   }
	   
	public synchronized static Collection<VirtualHost> buildVirtualHosts(final org.restlet.Context context){
		  List<VirtualHost> virtualHosts=new ArrayList<VirtualHost>();
		   
		   if(config!=null){
			   virtualHosts.addAll(((new PropertiesHelper<VirtualHost>()).buildObjectsFromProps(config, RESTLET_VIRTUAL_HOST_IDENTIFIER, RESTLET_VIRTUAL_HOST_PROP_OBJECT_FIELDS, CLASS_NAME, RESTLET_VIRTUAL_HOST_PARAMETER_TYPES, new Object[]{context})).values());
		   }

		   if(virtualHosts.size()==0){
			   virtualHosts.add(new XNATVirtualHost(context));
		   }
	   
	   return virtualHosts;
    }
	
	public synchronized static Application buildDefaultApplication(final org.restlet.Context context) {
		Application app= ((new PropertiesHelper<Application>()).buildObjectsFromProps(config, RESTLET_APP_IDENTIFIER, RESTLET_APP_PROP_OBJECT_FIELDS, CLASS_NAME, RESTLET_APP_PARAMETER_TYPES, new Object[]{context}).get(DEFAULT));
		if(app==null){
			return new XNATApplication(context);
		}else{
			return app;
		}
	}
	   
}
