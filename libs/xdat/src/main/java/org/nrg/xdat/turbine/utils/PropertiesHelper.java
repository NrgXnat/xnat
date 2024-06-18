/*
 * core: org.nrg.xdat.turbine.utils.PropertiesHelper
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import com.google.common.collect.Iterables;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class PropertiesHelper<T>  {
    static org.apache.log4j.Logger logger = Logger.getLogger(PropertiesHelper.class);

    public static Map<String,Map<String,Object>> RetrievePropertyObjects(final String props,final String identifier,final String[] fields){
        try {
            return RetrievePropertyObjects(ResourceUtils.getURL("classpath:" + props), identifier, fields);
        } catch (FileNotFoundException e) {
            logger.error("", e);
        }
        return new HashMap<>();
    }

    public static Map<String,Map<String,Object>> RetrievePropertyObjects(final File props,final String identifier,final String[] fields){
        try {
            if(props.exists()) {
                return RetrievePropertyObjects(props.toURI().toURL(), identifier, fields);
            }
        } catch (MalformedURLException e) {
            logger.error("", e);
        }

        return new HashMap<>();
    }

    public static Map<String, Map<String, Object>> RetrievePropertyObjects(final URL props, final String identifier, final String[] fields) {
        try {
            final Configuration config = new PropertiesConfiguration(props);
            return ParseStandardizedConfig(config, identifier, fields);
        } catch (ConfigurationException e) {
            logger.error("", e);
        }

        return new HashMap<>();
    }

	public static Configuration RetrieveConfiguration(final File props) throws ConfigurationException{
		if(props.exists()){
			return new PropertiesConfiguration(props);
		}
		
		return null;
	}
	
	public static Map<String,Map<String,Object>> ParseStandardizedConfig(final Configuration config,final String identifier,final String[] fields){
		final Map<String,Map<String,Object>> objects=new Hashtable<>();
		
				final String[] sA=config.getStringArray(identifier);
				if(sA!=null){
					for(final String objectName: sA){
						if(!objects.containsKey(objectName))objects.put(objectName, new Hashtable<String,Object>());
						
						final String fieldID=identifier+"."+objectName;
						for(final String field:fields){
							Object o=config.getProperty(fieldID+"."+field);
							if(o!=null)objects.get(objectName).put(field, o);
						}
					}
				}
		
		return objects;
	}
	
	
	@SuppressWarnings("unused")
	public static String GetStringProperty(final File props,final String identifier){
		try {
			if(props.exists()){
				final Configuration config=new PropertiesConfiguration(props);
				
				return config.getString(identifier);
			}
		} catch (ConfigurationException e) {
			logger.error("",e);
		}
		
		return null;
	}
	
	public static Integer GetIntegerProperty(final String fileName,final String identifier, final Integer defaultValue){
		return GetIntegerProperty(new File(XFT.GetConfDir(),fileName),identifier,defaultValue);
	}
	
	public static Integer GetIntegerProperty(final File props,final String identifier, final Integer defaultValue){
		try {
			if(props.exists()){
				final Configuration config=new PropertiesConfiguration(props);
				
				return config.getInteger(identifier,defaultValue);
			}
		} catch (ConfigurationException e) {
			logger.error("",e);
		}
		
		return defaultValue;
	}
	
	@SuppressWarnings({ "rawtypes", "unused" })
	public Map<String,T> buildObjectsFromProps(final String fileName, final String identifier,final String[] propFields, final String classNameProp, final Class[] contructorArgT, final Object[] contructorArgs) throws ConfigurationException{
		return buildObjectsFromProps(new File(XFT.GetConfDir(),fileName), identifier, propFields, classNameProp, contructorArgT, contructorArgs);
	}
	
	@SuppressWarnings({ "rawtypes" })
	public Map<String,T> buildObjectsFromProps(final File f, final String identifier,final String[] propFields, final String classNameProp, final Class[] contructorArgT, final Object[] contructorArgs) throws ConfigurationException{
		return buildObjectsFromProps(RetrieveConfiguration(f), identifier, propFields, classNameProp, contructorArgT, contructorArgs);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String,T> buildObjectsFromProps(final Configuration config, final String identifier,final String[] propFields, final String classNameProp, final Class[] contructorArgT, final Object[] contructorArgs){
		final Map<String,T> objs=new HashMap<>();
		   
	    if(config!=null){
		   final Map<String,Map<String,Object>> confBuilders=PropertiesHelper.ParseStandardizedConfig(config, identifier, propFields);
			for(final String key:confBuilders.keySet()){
				final String className=(String)confBuilders.get(key).get(classNameProp);
				try {
					if(className!=null)
						objs.put(key,(T)Class.forName(className).getConstructor(contructorArgT).newInstance(contructorArgs));
				} catch (Exception e) {
					logger.error("",e);
				}
			}
	    }
	    
	    return objs;
	}
	
	public Map<String,Class<? extends T>> buildClassesFromProps(final String fileName, final String identifier,final String[] propFields, final String classNameProp) throws ConfigurationException{
		return buildClassesFromProps(new File(XFT.GetConfDir(), fileName), identifier, propFields, classNameProp);
	}
	
	public Map<String,Class<? extends T>> buildClassesFromProps(final File f, final String identifier,final String[] propFields, final String classNameProp) throws ConfigurationException{
		return buildClassesFromProps(RetrieveConfiguration(f), identifier, propFields, classNameProp);
	}
	
	@SuppressWarnings({ "unchecked"})
	public Map<String,Class<? extends T>> buildClassesFromProps(final Configuration config, final String identifier,final String[] propFields, final String classNameProp){
		final Map<String,Class<? extends T>> objs=new HashMap<>();
		   
	    if(config!=null){
		   final Map<String,Map<String,Object>> confBuilders=PropertiesHelper.ParseStandardizedConfig(config, identifier, propFields);
			for(final String key:confBuilders.keySet()){
				final String className=(String)confBuilders.get(key).get(classNameProp);
				try {
					if(className!=null)
						objs.put(key,(Class<? extends T>)Class.forName(className));
				} catch (Exception e) {
					logger.error("",e);
				}
			}
	    }
	    
	    return objs;
	}
		
	public static class ImplLoader<T>{
		final String fileName, identifier;
		public ImplLoader(final String fileName,final String identifier){
			this.fileName=fileName;
			this.identifier=identifier;
		}
		
		public Class<? extends T> getClazz() throws ConfigurationException{
			Map<String,Class<? extends T>> impls=(new PropertiesHelper<T>()).buildClassesFromProps(fileName, identifier, new String[]{"className"}, "className");
			if(impls.size()>0){
				return Iterables.get(impls.values(), 0);
			}else{
				return null;
			}
		}
		
		public T buildNoArgs(final T _default) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ConfigurationException{
			return build(new Class[]{},new Object[]{},_default);
		}
		
		public T build(final Class[] contructorArgT, final Object[] contructorArgs, final T _default) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ConfigurationException{
			Class<? extends T> clazz=this.getClazz();
			if(clazz==null){
				return _default;
			}else{
				return clazz.getConstructor(contructorArgT).newInstance(contructorArgs);
			}
		}
	}
}
