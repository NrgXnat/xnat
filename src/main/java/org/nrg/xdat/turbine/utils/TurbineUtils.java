//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.utils.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
/**
 * @author Tim
 *
 */
public class TurbineUtils {
	public static final String EDIT_ITEM = "edit_item";
	static Logger logger = Logger.getLogger(TurbineUtils.class);
	private XdatSecurity _security = null;
	
	private static TurbineUtils INSTANCE = null;
	
	private TurbineUtils()
	{
	    init();
	}
	
	private void init(){
	    
	}
	
	public static TurbineUtils GetInstance()
	{
	    if (INSTANCE == null)
	    {
	        INSTANCE = new TurbineUtils();
	    }
	    
	    return INSTANCE;
	}
	
	private XdatSecurity getSecurityObject()
	{
	    if (_security==null)
	    {
	       final List<XdatSecurity> al =XdatSecurity.getAllXdatSecuritys(null,false);
	       if (al.size()>0)
	       {
	           _security=al.get(0);
	       }
	    }
	    
	    return _security;
	}
	
	public Integer getSecurityID(){
	    final XdatSecurity sec = this.getSecurityObject();
		if (sec!=null)
		{
			try {
                return sec.getIntegerProperty("xdat:security.xdat_security_id");
            } catch (FieldNotFoundException e) {
                logger.error("",e);
                return null;
            } catch (ElementNotFoundException e) {
                logger.error("",e);
                return null;
            }
		}else{
		    return null;
		}
	}
	
	public static Integer GetSystemID(){
	    return TurbineUtils.GetInstance().getSecurityID();
	}
	
	public static String GetSystemName()
	{
		final String site_id= XFT.GetSiteID();
		if(site_id==null || org.apache.commons.lang.StringUtils.isEmpty(site_id)){
			return "XNAT";
		}else{
			return site_id;
		}
	}
	
	public boolean loginRequired()
	{
		return XFT.GetRequireLogin();
	}
	
	public static boolean LoginRequired()
	{
        return XFT.GetRequireLogin();
	}
	
	public static ItemI GetItemBySearch(RunData data, boolean preLoad) throws Exception
	{
		//TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
		final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
		final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
		if (searchField != null && searchValue != null)
		{
			final ItemSearch search = new ItemSearch();
		    search.setUser(TurbineUtils.getUser(data));
		    
		    final String elementName = StringUtils.GetRootElementName(searchField);
		    
		    search.setElement(elementName);
		    search.addCriteria(searchField,searchValue);
		    search.setAllowMultiples(preLoad);
		    
		    final ItemCollection items = search.exec();
			if (items.size() > 0)
			{
				ItemI o = items.getFirst();
				//o.extend();
				return o;
			}else{
				return null;
			}
		}else
		{
			return null;
		}
	}
	
	public static SchemaElementI GetSchemaElementBySearch(RunData data)
	{
		//TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
		final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
		final String searchElement = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
		if (searchElement!=null)
		{
		    try {
                return GenericWrapperElement.GetElement(searchElement);
            } catch (XFTInitException e) {
            } catch (ElementNotFoundException e) {
            }
		}
		
		if (searchField !=null)
		{
			try {
                return StringUtils.GetRootElement(searchField);
            } catch (ElementNotFoundException e) {
            }
		}
		
		return null;
	}
	
	public static XFTItem GetItemBySearch(RunData data) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception
	{
		//TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
		final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
		final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
		if (searchField != null && searchValue != null)
		{
			final ItemSearch search = new ItemSearch();
		    search.setUser(TurbineUtils.getUser(data));
		    
		    final String elementName = StringUtils.GetRootElementName(searchField);
		    
		    final SchemaElementI gwe = SchemaElement.GetElement(elementName);
		    search.setElement(elementName);
		    search.addCriteria(searchField,searchValue);
		    
		    search.setAllowMultiples(gwe.isPreLoad());
		    
		    final ItemCollection items = search.exec();
			if (items.size() > 0)
			{
				ItemI o = items.getFirst();
				//o.extend();
				return (XFTItem)o;
			}else{
				return null;
			}
		}else
		{
			return null;
		}
	}
	
	public static ItemI GetItemBySearch(RunData data,Boolean preload) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception
	{
		//TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
		final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
		final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
		if (searchField != null && searchValue != null)
		{
			final ItemSearch search = new ItemSearch();
		    search.setUser(TurbineUtils.getUser(data));
		    
		    final String elementName = StringUtils.GetRootElementName(searchField);
		    
		    final SchemaElementI gwe = SchemaElement.GetElement(elementName);
		    search.setElement(elementName);
		    search.addCriteria(searchField,searchValue);
		    
		    boolean b = false;
		    if (preload==null)
		    {
		        b= gwe.isPreLoad();
		    }else{
		        b = preload.booleanValue();
		    }
		    search.setAllowMultiples(b);
		    
		    final ItemCollection items = search.exec();
			if (items.size() > 0)
			{
				ItemI o = items.getFirst();
				//o.extend();
				return o;
			}else{
				return null;
			}
		}else
		{
			return null;
		}
	}
    
	
	public static void SetEditItem(ItemI item,RunData data)
	{
	    data.getSession().setAttribute(EDIT_ITEM,item);
	}
	
	public static ItemI GetEditItem(RunData data)
	{
		final ItemI edit_item = (ItemI)data.getSession().getAttribute(EDIT_ITEM);
	    data.getSession().removeAttribute(EDIT_ITEM);
	    return edit_item;
	}
	
	public static void SetParticipantItem(ItemI item,RunData data)
	{
	    data.getSession().setAttribute("participant",item);
	}
	
	public static ItemI GetParticipantItem(RunData data)
	{
		final ItemI edit_item = (ItemI)data.getSession().getAttribute("participant");
	    if (edit_item==null)
	    {
	       String s = TurbineUtils.escapeParam(data.getParameters().getString("part_id"));
	       if (s != null)
	       {
		       try {
		    	   final ItemCollection items = ItemSearch.GetItems("xnat:subjectData.ID",s,TurbineUtils.getUser(data),false);
	               if (items.size()>0)
	               {
	                   return items.getFirst();
	               }
            } catch (Exception e) {
                logger.error("",e);
            }
	       }else{
	           s = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
	           if (s != null)
	           {
	               if (s.equalsIgnoreCase("xnat:subjectData.ID"))
	               {
	                   try {
	                	   final ItemI part = TurbineUtils.GetItemBySearch(data);
                        return part;
                    } catch (Exception e) {
                        logger.error("",e);
                    }
	               }
	           }
	       }
	    }
	    if (edit_item!=null)
	    {
	    	data.getSession().removeAttribute("participant");
	    }
	    return edit_item;
	}
	
	public static String GetSearchElement(RunData data)
	{
		String s =  TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
		if (s==null)
		{
			s = TurbineUtils.escapeParam(data.getParameters().getString("element"));
		}
		return s;
	}

    
    /**
     * Returns server & context as specified in the Turbine object model (taken from the first login url).
     * @return
     */
	public static String GetFullServerPath()
	{
		if (XFT.GetSiteURL()==null|| XFT.GetSiteURL().equals("")){
			String s = "";
			s = Turbine.getServerScheme() + "://" + Turbine.getServerName();
			if(!Turbine.getServerPort().equals("80"))
				s+= ":" + Turbine.getServerPort();
			s+=Turbine.getContextPath();
			return s;
		}else{
			return XFT.GetSiteURL();
		}
	}
    
    /**
     * Returns server & context as specified in user request object.
     * @param data
     * @return
     */
    public static String GetRelativeServerPath(RunData data)
    {
    	return GetRelativePath(data.getRequest());
    }
    
    public static String GetRelativePath(HttpServletRequest req){
        if (req.getContextPath()!=null && !req.getContextPath().equals(""))
        {
            return req.getContextPath();
        }else{
        	return "";
        }
    }
    
    /**
     * Returns server & context as specified in user request object.
     * @param req    Servlet request
     * @return
     */
    public static String GetFullServerPath(HttpServletRequest req){
    	if (XFT.GetSiteURL()==null|| XFT.GetSiteURL().equals("")){
            String s = null;
            s= req.getRequestURL().toString();
            String server = null;
            
            if (req.getContextPath()!=null && !req.getContextPath().equals(""))
            {
            	final String path = req.getContextPath() +"/";
                if (s.indexOf(path)!=-1)
                {
                	final int breakIndex = s.indexOf(path) + (path.length());
                    server = s.substring(0,breakIndex);
                }
            }
            
            if (server==null){
                if (s.indexOf((new Integer(req.getServerPort())).toString())!=-1)
                {
                	final int breakIndex = s.indexOf((new Integer(req.getServerPort())).toString()) + (new Integer(req.getServerPort())).toString().length();
                    server = s.substring(0,breakIndex);
                }
            }
            
            if (server==null){
                server = req.getScheme() + "://" + req.getServerName();
            }
            
            return server;
		}else{
			return XFT.GetSiteURL();
		}
    }
        
    public static String GetContext()
    {
        final String s = Turbine.getContextPath();

        return s;
    }
	
	public static XDATUser getUser(RunData data)
	{
		XDATUser user;
		if (data.getSession().getAttribute("user") == null) {
			user = XDAT.getUserDetails();
			data.getSession().setAttribute("user", user);
		} else {
			user = (XDATUser) data.getSession().getAttribute("user");
		}
		return user;
	}
	
	public static void setUser(RunData data, XDATUser user) throws Exception
	{
		XDAT.setUserDetails(new XDATUserDetails(user));
	}
	
	public static void setNewUser(RunData data, XDATUser user, Context context) throws Exception
	{
		XDAT.setNewUserDetails(new XDATUserDetails(user), data, context);
	}
	
	/**
	 * @param data
	 * @return
	 */
	public static DisplaySearch getSearch(RunData data)
	{
        if (data.getParameters().get("search_xml")!=null || data.getParameters().get("search_id") !=null){
            return TurbineUtils.getDSFromSearchXML(data);
        }else{
            DisplaySearch ds =  (DisplaySearch)data.getSession().getAttribute("search");
            if (ds == null)
            {
                String displayElement = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
                if (displayElement == null)
                {
                    displayElement = TurbineUtils.escapeParam(data.getParameters().getString("element"));
                }
                
                if (displayElement == null)
                {
                    return null;
                }
                
                try {
                    ds = TurbineUtils.getUser(data).getSearch(displayElement,"listing");
                    
                    final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
                    final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
                    if (searchField!= null && searchValue != null)
                    {
                        SearchCriteria criteria = new SearchCriteria();
                        criteria.setFieldWXMLPath(searchField);
                        criteria.setValue(searchValue);
                        ds.addCriteria(criteria);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return ds;
        }
	}
	
	
	/**
	 * Findbugs says DisplaySearch should be serializable.  That is a good idea, but this code is only used 
	 * in legacy code and should be remove at some point.  Unable to suppress warnings to support 1.5. 
	 * 
	 * @param data
	 * @param search
	 */
	public static void setSearch(RunData data, DisplaySearch search)
	{
		data.getSession().setAttribute("search",search);
	}
    
    public static DisplaySearch getDSFromSearchXML(RunData data){
    	final XDATUser user = TurbineUtils.getUser(data);
        
        if (user!=null){
	        if (data.getParameters().get("search_xml") !=null)
	        {
	            try {
	                String search_xml = data.getParameters().getString("search_xml");
	                search_xml = search_xml.replaceAll("%", "%25");
	                search_xml=URLDecoder.decode(search_xml,"UTF-8");
	                search_xml=StringUtils.ReplaceStr(search_xml, ".close.", "/");
	                
	                final StringReader sr = new StringReader(search_xml);
	                final InputSource is = new InputSource(sr);
	                final SAXReader reader = new SAXReader(user);
	                final XFTItem item = reader.parse(is);
	                final XdatStoredSearch search = new XdatStoredSearch(item);
	                        if (search!=null){
	                        	final DisplaySearch ds=search.getCSVDisplaySearch(user);
	                            data.getParameters().remove("search_xml");
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
	        }else if (data.getParameters().get("search_id") !=null)
	        {
	            try {
	            	final String search_id = data.getParameters().get("search_id");
	                    
	            	final String search_xml = PoolDBUtils.RetrieveLoggedCustomSearch(user.getLogin(), user.getDBName(), search_id);
	                    
	                    if (search_xml!=null){
	                    	final StringReader sr = new StringReader(search_xml);
	                    	final InputSource is = new InputSource(sr);
	                    	final SAXReader reader = new SAXReader(user);
	                    	final XFTItem item = reader.parse(is);
	                    	final XdatStoredSearch search = new XdatStoredSearch(item);
	                        if (search!=null){
	                        	final DisplaySearch ds=search.getDisplaySearch(user);
	                            data.getParameters().remove("search_id");
	                            return ds;
	                        }
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
	        }else if (data.getRequest().getAttribute("xss") !=null)
	        {
	            try {
	            	final XdatStoredSearch search = (XdatStoredSearch)data.getRequest().getAttribute("xss");
                    if (search!=null){
                    	final DisplaySearch ds=search.getDisplaySearch(user);
                        data.getParameters().remove("search_id");
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
        return null;
    }
	
	public static RunData SetSearchProperties(RunData data, ItemI item)
	{
		data.getParameters().setString("search_element",item.getXSIType());
		try {
			final SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
			final SchemaField sf = (SchemaField)se.getAllPrimaryKeys().get(0);
			data.getParameters().setString("search_field",StringUtils.ReplaceStr(StringUtils.ReplaceStr(sf.getXMLPathString(se.getFullXMLName()),"/","."),"@","."));
			final Object o = item.getProperty(sf.getId());
			data.getParameters().setString("search_value",o.toString());
		} catch (Exception e) {
			logger.error("",e);
		}
		return data;
	}
    
    public static void SetSearchProperties(Context context, ItemI item)
    {
        context.put("search_element",item.getXSIType());
        try {
        	final SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
        	final SchemaField sf = (SchemaField)se.getAllPrimaryKeys().get(0);
            context.put("search_field",StringUtils.ReplaceStr(StringUtils.ReplaceStr(sf.getXMLPathString(se.getFullXMLName()),"/","."),"@","."));
            final Object o = item.getProperty(sf.getId());
            context.put("search_value",o.toString());
        } catch (Exception e) {
            logger.error("",e);
        }
    }
	
	public static ItemI getDataItem(RunData data)
	{
		final ItemI item = (ItemI)data.getSession().getAttribute("data_item");
		data.getSession().removeAttribute("data_item");
		return item;
	}
	
	public static RunData setDataItem(RunData data, ItemI item)
	{
		data.getSession().setAttribute("data_item",item);
		return data;
	}
	
	public static String getStoredElementName(RunData data)
	{
		final String s= (String)data.getSession().getAttribute("stored_element");
		data.getSession().removeAttribute("stored_element");
		return s;
	}
	
	public static void setStoredElementName(RunData data, String elementName)
	{
		data.getSession().setAttribute("stored_element",elementName);
	}
	
	public static void OutputDataParameters(RunData data)
	{
		if (data!= null)
		{
			logger.debug("\n\nData Parameters");
			final List<String> al = GetDataParameterList(data);
			for(int i=0; i < al.size(); i++)
			{
				logger.debug("KEY: "+ al.get(i).toString() + " VALUE: " + data.getParameters().get(al.get(i).toString().toLowerCase()));
			}
		}
	}
	
		public static List<String> GetDataParameterList(RunData data)
		{
			final List<String> al = new ArrayList<String>();
			for(int i=0; i < data.getParameters().getKeys().length; i++)
			{
				al.add(escapeParam(data.getParameters().getKeys()[i].toString()));
			}
			Collections.sort(al);
			return al;
		}

        @SuppressWarnings({ "deprecation", "unchecked" })
		public static Map<String,String> GetDataParameterHash(RunData data)
		{
		    //TurbineUtils.OutputDataParameters(data);
        	final Map<String,String> hash = new Hashtable<String,String>();
            ParameterParser pp = data.getParameters();
            Enumeration<Object> penum = pp.keys();
            while (penum.hasMoreElements()){
            	final String key = penum.nextElement().toString();
            	final  Object value = TurbineUtils.escapeParam(data.getParameters().get(key));
                if (value!=null && !value.equals(""))
                    hash.put(TurbineUtils.escapeParam(key),value.toString());
            }
			return hash;
		}
		
		public static Map<String,String> GetContextParameterHash(Context context)
		{
			final Map<String,String> hash = new Hashtable<String,String>();
			final Object[] keys = context.getKeys();
            for (int i =0;i<keys.length;i++){
            	final String key = (String)keys[i];
            	final Object value = context.get(key);
                if (value!=null && !value.equals(""))
                    hash.put(key,value.toString());
            }
			return hash;
		}
		
		public static Map<String,String> GetTurbineParameters(RunData data, Context context)
		{
			final Map<String,String> hash;
			if (data != null){
				hash = GetDataParameterHash(data);
			}else{
				hash=new Hashtable<String,String>();
			}
			if (context != null)
			{
				hash.putAll(GetContextParameterHash(context));
			}
			return hash;
		}
	
		/**
		 * Debugging method used in actions to display all fields in an Intake Group.
		 * @param group
		 */
		public static void OutputGroupFields(Group group)
		{
			logger.debug("\n\nGroup Parameters");
			for(int i=0; i < group.getFieldNames().length; i++)
			{
				try{
					logger.debug("FIELD: "+ group.getFieldNames()[i] + " VALUE: " + group.get(group.getFieldNames()[i]).getValue() + " DISPLAY NAME: " + group.get(group.getFieldNames()[i]).getDisplayName() + " KEY: " + group.get(group.getFieldNames()[i]).getKey() + " Initial: " + group.get(group.getFieldNames()[i]).getInitialValue() + " DEFAULT: " + group.get(group.getFieldNames()[i]).getDefaultValue() + " TEST: " + group.get(group.getFieldNames()[i]).getTestValue());
				}catch(Exception ex)
				{
				}
			}
		}
	
		/**
		 * Debugging method used in actions to display all parameters in the Context object
		 * @param context
		 */
		public static void OutputContextParameters(Context context)
		{
			if (context != null)
			{
				logger.debug("\n\nContext Parameters");
				for(int i=0; i < context.getKeys().length; i++)
				{
					logger.debug("KEY: "+ context.getKeys()[i].toString() + " VALUE: " + context.get(context.getKeys()[i].toString()));
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		public static void OutputSessionParameters(RunData data)
		{
			if (data != null)
			{
				logger.debug("\n\nSession Parameters");
				final Enumeration<String> enumer = data.getSession().getAttributeNames();
				while (enumer.hasMoreElements())
				{
					final String key = enumer.nextElement();
					final Object o = data.getSession().getAttribute(key);
				    logger.debug("KEY: "+ key + " VALUE: " + o.getClass());
				}
			}
		}
	
	public static void OutputPassedParameters(RunData data,Context context, String name)
	{
		logger.debug("\n\n" + name);
		TurbineUtils.OutputDataParameters(data);
		TurbineUtils.OutputContextParameters(context);
		TurbineUtils.OutputSessionParameters(data);
	}
	
	
	public static boolean HasPassedParameter(String s, RunData data)
	{
	    if (data.getParameters().get(s.toLowerCase())!=null)
	    {
	    	final Object o = TurbineUtils.escapeParam(data.getParameters().get(s.toLowerCase()));
	        if(o.toString().equalsIgnoreCase(""))
	        {
	            return false;
	        }else{
		        return true;
	        }
	    }else{
	        return false;
	    }
	}
	
	public static Object GetPassedParameter(String s, RunData data)
	{
	    return GetPassedParameter(s.toLowerCase(),data,null);
	}
	
	public static Boolean GetPassedBoolean(String s, RunData data)
	{
		return data.getParameters().getBool(s);
	}
	
	public static Integer GetPassedInteger(String s, RunData data)
	{
		return TurbineUtils.GetPassedInteger(s, data,null);
	}
	
	public static Integer GetPassedInteger(String s, RunData data,Integer defualt) 
	{
		if (data.getParameters().get(s.toLowerCase())!=null)
	    {
	    	final Object o = TurbineUtils.escapeParam(data.getParameters().getInteger(s.toLowerCase()));
	        if(o.toString().equalsIgnoreCase(""))
	        {
	            return defualt;
	        }else{
		        return (Integer)o;
	        }
	    }else{
	        return defualt;
	    }
	}
	
	public static Object[] GetPassedObjects(String s, RunData data)
	{
		final Object[] v=data.getParameters().getObjects(s);
		if(v!=null){
			for(int i=0;i<v.length;i++){
				v[i]=TurbineUtils.escapeParam(v[i]);
			}
		}
		return v;
	}

	public static Collection<String> GetPassedStrings(String s, RunData data)
	{
		final Collection<String> _ret = Lists.newArrayList();
		final String[] v=data.getParameters().getStrings(s);
		if(v!=null){
			for(int i=0;i<v.length;i++){
				if(!StringUtils.IsEmpty(v[i]) && !StringUtils.IsEmpty(TurbineUtils.escapeParam(v[i]))){
					_ret.add(TurbineUtils.escapeParam(v[i]));
				}
			}
		}
		return _ret;
	}
	
	public static Object GetPassedParameter(String s, RunData data, Object defualt)
	{
	    if (data.getParameters().get(s.toLowerCase())!=null)
	    {
	    	final Object o = TurbineUtils.escapeParam(data.getParameters().get(s.toLowerCase()));
	        if(o.toString().equalsIgnoreCase(""))
	        {
	            return defualt;
	        }else{
		        return o;
	        }
	    }else{
	        return defualt;
	    }
	}
	
	
	public static void InstanciatePassedItemForScreenUse(RunData data, Context context)
	{
	    try {
	    	final ItemI o = TurbineUtils.GetItemBySearch(data);
            
            if (o != null)
            {
            	TurbineUtils.setDataItem(data,o);
            	
            	context.put("item",o);
            	context.put("element",org.nrg.xdat.schema.SchemaElement.GetElement(o.getXSIType()));
            	context.put("search_element",TurbineUtils.escapeParam(data.getParameters().getString("search_element")));
            	context.put("search_field",TurbineUtils.escapeParam(data.getParameters().getString("search_field")));
            	context.put("search_value",TurbineUtils.escapeParam(data.getParameters().getString("search_value")));
            	
            }else{
            	logger.error("No Item Found.");
            	data.setScreenTemplate("DefaultReport.vm");
            }
        } catch (Exception e) {
            logger.error("",e);
            data.setMessage(e.getMessage());
        	data.setScreenTemplate("DefaultReport.vm");
        }
	}
	
	public Boolean toBoolean(String s){
		return Boolean.valueOf(s);
	}
    
    public String formatDate(Date d, String pattern){
	    	final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat (pattern);
	        return formatter.format(d);
    }
    
    public String formatDate(Date d){
    	synchronized (getDateFormatter()){
    		return getDateFormatter().format(d);
    	}
    }
    
    private static java.text.SimpleDateFormat default_date_format=null;
    public static java.text.SimpleDateFormat getDateFormatter(){
    	if(default_date_format==null){
    		try {
				default_date_format= new java.text.SimpleDateFormat (XDAT.getSiteConfigurationProperty("UI.date-format","MM/dd/yyyy"));
			} catch (ConfigServiceException e) {
				default_date_format= new java.text.SimpleDateFormat ("MM/dd/yyyy");
			}
    	}
    	return default_date_format;
    }
    
    public String formatDateTime(Date d){
    	synchronized (getDateTimeFormatter()){
    		return getDateTimeFormatter().format(d);
    	}
    }
    
    private static java.text.SimpleDateFormat default_date_time_format=null;
    public static java.text.SimpleDateFormat getDateTimeFormatter(){
    	if(default_date_time_format==null){
    		try {
    			default_date_time_format= new java.text.SimpleDateFormat (XDAT.getSiteConfigurationProperty("UI.date-time-format","MM/dd/yyyy HH:mm:ss"));
			} catch (ConfigServiceException e) {
				default_date_time_format= new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
			}
    	}
    	return default_date_time_format;
    }
    
    public String formatDateTimeSeconds(Date d){
    	synchronized (getDateTimeSecondsFormatter()){
    		return getDateTimeSecondsFormatter().format(d);
    	}
    }
    
    private static java.text.SimpleDateFormat default_date_time_seconds_format=null;
    public static java.text.SimpleDateFormat getDateTimeSecondsFormatter(){
    	if(default_date_time_seconds_format==null){
    		try {
    			default_date_time_seconds_format= new java.text.SimpleDateFormat (XDAT.getSiteConfigurationProperty("UI.date-time-seconds-format","MM/dd/yyyy HH:mm:ss.SSS"));
			} catch (ConfigServiceException e) {
				default_date_time_seconds_format= new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss.SSS");
			}
    	}
    	return default_date_time_seconds_format;
    }
    
    public String formatTime(Date d){
    	synchronized (getTimeFormatter()){
    		return getTimeFormatter().format(d);
    	}
    }
    
    private static java.text.SimpleDateFormat default_time_format=null;
    public static java.text.SimpleDateFormat getTimeFormatter(){
    	if(default_time_format==null){
    		try {
    			default_time_format= new java.text.SimpleDateFormat (XDAT.getSiteConfigurationProperty("UI.time-format","HH:mm:ss"));
			} catch (ConfigServiceException e) {
				default_time_format= new java.text.SimpleDateFormat ("HH:mm:ss");
			}
    	}
    	return default_time_format;
    }
    
    public boolean templateExists(String screen){
    	return Velocity.templateExists(screen);
    }
    
    public String validateTemplate(String screen, String project){
    	if(screen.endsWith(".vm")){
    		screen=screen.substring(0,screen.length()-3);
    	}
    	
    	if(project!=null && Velocity.templateExists(screen+"_" + project + ".vm")){
    		return screen + "_" + project + ".vm";
    	}else{
    		if(Velocity.templateExists(screen + ".vm")){
        		return screen + ".vm";
        	}else{
        		return null;
        	}
    	}
    }
    
    public String validateTemplate(String[] screens, String project){
    	for(final String screen : screens){
    		final String s = validateTemplate(screen,project);
    		if(s!=null){
    			return s;
    		}
    	}
    	
    	return null;
    }
    
    public String getTemplateName(String module,String dataType,String project){
    	try {
    		final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
			String temp = validateTemplate("/screens/"+ root.getSQLName()+ "/" + root.getSQLName() + module,project);
			if (temp!=null){
				return temp;
			}
			
			temp = validateTemplate("/screens/"+ root.getSQLName()+ "/" + module,project);
			if (temp!=null){
				return temp;
			}
			
			for(List<Object> primary: root.getExtendedElements()){
				GenericWrapperElement p= ((SchemaElementI)primary.get(0)).getGenericXFTElement();
				temp = validateTemplate("/screens/"+ p.getSQLName()+ "/" + p.getSQLName() + module,project);
				if (temp!=null){
					return temp;
				}
				
				temp = validateTemplate("/screens/"+ p.getSQLName()+ "/" + module,project);
				if (temp!=null){
					return temp;
				}
			}
		} catch (XFTInitException e) {
            logger.error("",e);
		} catch (ElementNotFoundException e) {
            logger.error("",e);
		}
    	
    	return null;
    }
    
    protected final Map<String,List<Properties>> cachedVMS=new Hashtable<String,List<Properties>>();
    
    
    /**
     * Note: much of this was copied from SecureScreen.  This version looks at the other templates directories (not just templates).  We may want to merge the two impls.
     * @param subFolder like topBar/admin
     * @return The properties from the located templates.
     */
    public List<Properties> getTemplates(String subFolder){
    	//first see if the props have been cached.
    	List<Properties> screens=cachedVMS.get(subFolder);
    	List<String> _defaultScreens=new ArrayList<String>();
    	if(screens==null){
    		synchronized (this){
    			//synchronized so that two calls don't overwrite each other.  I only synchronized this chunk in hopes that when the screens list is cached, the block wouldn't occur.
	    		//need to build the list of props.
	    		screens=new ArrayList<Properties>();
	        	List<String> exists=new ArrayList<String>();
	    		List<File> screensFolders = XDAT.getScreenTemplateFolders();
	    		for(File screensFolder: screensFolders){
	    	        if (screensFolder.exists()) {
	    	        	File subFile=new File(screensFolder,subFolder);
	    	        	if(subFile.exists()){
	        	            File[] files = subFile.listFiles(new FilenameFilter() {
	        	                @Override
	        	                public boolean accept(File folder, String name) {
	        	                    return name.endsWith(".vm");
	        	                }
	        	            });
	        	            
	        	            if(files!=null){
	        	            	for(File f:files){
	        	            		String path=subFolder+"/"+f.getName();
	        	            		if(!exists.contains(path)){
	            	            		try {
											SecureScreen.addProps(f, screens, _defaultScreens,path);
											exists.add(path);
										} catch (FileNotFoundException e) {
											//this shouldn't happen
										}
	        	            		}
	        	            	}
	        	            }
	    	        	}
	    	        }
	    		}
	    		
	    		Collections.sort(screens, new Comparator<Properties>() {
					@Override
					public int compare(Properties arg0, Properties arg1) {
						if(arg0.containsKey("Sequence") && arg1.containsKey("Sequence")){
							try {
								Integer sequence1=Integer.parseInt(arg0.getProperty("Sequence"));
								Integer sequence2=Integer.parseInt(arg1.getProperty("Sequence"));
								return sequence1.compareTo(sequence2);
							} catch (NumberFormatException e) {
								logger.error("Illegal sequence format.",e);
								return 0;
							}
						}else if(arg0.containsKey("Sequence")){
							return -1;
						}else if(arg1.containsKey("Sequence")){
							return 1;
						}else{
							return 0;
						}
					}
				});
	    		
	    		cachedVMS.put(subFolder,screens);
    		}
    	}
    	return screens;
    }
    
    private boolean containsPropByProperty(final List<Properties> props, final Properties prop, final String property){
    	if(!prop.containsKey(property)){
    		return false;
    	}
		return (CollectionUtils.find(props, new Predicate(){
			@Override
			public boolean evaluate(Object arg0) {
				if(((Properties)arg0).getProperty(property)!=null){
					return ObjectUtils.equals(prop.getProperty(property), ((Properties)arg0).getProperty(property));
				}else{
					return false;
				}
			}})!=null);
    }
    
    private void mergePropsNoOverwrite(final List<Properties> props, final List<Properties> add, final String property){
    	for(final Properties p:add){
			if(!containsPropByProperty(props,p,property)){
				props.add(p);
			}
		}
    }

    /**
     * Looks for templates in the give subFolder underneath the give dataType in the xdat-templatea, xnat-templates, or templates.
     * dataType/subFolder
     * 
     * @param dataType
     * @param subFolder
     * @return
     */
    public List<Properties> getTemplates(String dataType, String subFolder){
    	List<Properties> props= Lists.newArrayList();
		try {
			final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
			
			props.addAll(getTemplates(root.getSQLName()+"/"+subFolder));
			mergePropsNoOverwrite(props,getTemplates(root.getSQLName()+"/"+subFolder),"fileName");
			
			for(List<Object> primary: root.getExtendedElements()){
				final GenericWrapperElement p= ((SchemaElementI)primary.get(0)).getGenericXFTElement();
				mergePropsNoOverwrite(props,getTemplates(p.getSQLName()+"/"+subFolder),"fileName");
			}
		} catch (XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		}
		return props;
    }
    
    public String getTemplateName(String module,String dataType,String project,String subFolder){
    	try {
    		final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
			String temp = validateTemplate("/screens/"+ root.getSQLName()+ "/" + subFolder + "/" + root.getSQLName() + module,project);
			if (temp!=null){
				return temp;
			}
			
			temp = validateTemplate("/screens/"+ root.getSQLName()+ "/" + subFolder + "/" + module,project);
			if (temp!=null){
				return temp;
			}
			
			for(List<Object> primary: root.getExtendedElements()){
				final GenericWrapperElement p= ((SchemaElementI)primary.get(0)).getGenericXFTElement();
				temp = validateTemplate("/screens/"+ p.getSQLName()+ "/" + subFolder + "/" + p.getSQLName() + module,project);
				if (temp!=null){
					return temp;
				}
				
				temp = validateTemplate("/screens/"+ p.getSQLName()+ "/" + subFolder + "/" + module,project);
				if (temp!=null){
					return temp;
				}
			}
		} catch (XFTInitException e) {
            logger.error("",e);
		} catch (ElementNotFoundException e) {
            logger.error("",e);
		}
    	
    	return null;
    }
    
    public String formatDate(long d, String pattern){
    	return formatDate(new Date(d),pattern);
    }
    
    public String formatNumber(Object o, int roundTo){
    	final NumberFormat formatter = java.text.NumberFormat.getInstance();
        if (o==null){
            return "";
        }
        if (o instanceof String){
            try {
                o = formatter.parse((String)o);
            } catch (ParseException e) {
                logger.error("",e);
                return o.toString();
            }
        }
        
        if (o instanceof Number){
        	final Number n = (Number)o;
            formatter.setGroupingUsed(false);
            formatter.setMaximumFractionDigits(roundTo);
            formatter.setMinimumFractionDigits(roundTo);
            return formatter.format(n);
        }else{
            return o.toString();
        }
    }
    
    public Object getArrayIndex(Object[] array, int index){
        return array[index];
    }
    
    public static String escapeParam(String o){
    	return (o==null)?null:StringEscapeUtils.escapeXml(o);
    }
    
    public static Object escapeParam(Object o){
    	if(o instanceof String)
    		return escapeParam((String) o);
    	else
    		return o;
    }
    
    public static String unescapeParam(String o){
    	return (o==null)?null:StringEscapeUtils.unescapeXml(o);
    }
    
    /**
     * If a value is placed into a form field via JavaScript, it must be unescaped first, 
     * otherwise the value will be XML-encoded, and it will be double-encoded on re-tranmission to the server.
     * (e.g. "&quot;" will become "&amp;quot;"). 
     * This is not necessary for form fields populated via HTML, as the browser will automatically decode the entities.
     * @param o
     * @return The input string, with any XML entities decoded.
     */
    public static Object unescapeParam(Object o){
    	if(o instanceof String)
    		return unescapeParam((String) o);
    	else
    		return o;
    }
    
    public String escapeHTML(String o){
    	return (o==null)?null:StringEscapeUtils.escapeHtml(o);
    }
    
    public String escapeJS(String o){
    	return (o==null)?null:StringEscapeUtils.escapeJavaScript(o);
    }
    
    public int getYear(){
    	return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment. If you need to specify inline content (e.g. for MIME
     * content in email or embedded content situations), use {@link #setContentDisposition(javax.servlet.http.HttpServletResponse, String, boolean)}.
     * @param response    The servlet response on which the header should be set.
     * @param filename    The suggested filename for downloaded content.
     */
    public static void setContentDisposition(HttpServletResponse response, String filename){
        setContentDisposition(response, filename, true);
}

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment when the <b>isAttachment</b> parameter is set to true,
     * and as inline content when the <b>isAttachment</b> parameter is set to false. You can specify the content
     * as an attachment by default by calling {@link #setContentDisposition(HttpServletResponse, String)}.
     * @param response    The servlet response on which the header should be set.
     * @param filename    The suggested filename for downloaded content.
     * @param isAttachment    Indicates whether the content is an attachment or inline.
     */
    @SuppressWarnings("unchecked")
    public static void setContentDisposition(HttpServletResponse response, String filename, boolean isAttachment) {
        if (response.containsHeader(CONTENT_DISPOSITION)) {
            throw new IllegalStateException("A content disposition header has already been added to this response.");
        }
        response.addHeader(CONTENT_DISPOSITION, createContentDispositionValue(filename, isAttachment));
    }

    /**
     * Creates the value to be set for a content disposition header.
     * @param filename        The filename for the header.
     * @param isAttachment    Whether the content is an attachment or inline.
     * @return The value to be set for the content disposition header.
     */
    public static String createContentDispositionValue(final String filename, final boolean isAttachment) {
        return String.format("%s; filename=\"%s\";", isAttachment ? "attachment" : "inline", filename);
}

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
}
