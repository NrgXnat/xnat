/*
 * org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/20/13 2:28 PM
 */
package org.nrg.xdat.velocity.loaders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.nrg.xdat.servlet.XDATServlet;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.google.common.collect.Lists;

/**
 * @author Tim Olsen <tim@deck5consulting.com>
 *
 * This custom implementation of the Velocity ResourceLoader will manage the loading of VM files from the file system OR the classpath.
 * 
 * This allows VMs to be loaded from within JAR files but is still backwards compatible with the old file system structure.  Also, the loading enforces 
 * the templates/xnat-templates/xdat-templates/base-templates hierarchy.
 */
public class CustomClasspathResourceLoader extends ResourceLoader {
	static Logger logger = Logger.getLogger(CustomClasspathResourceLoader.class);

	public static final String META_INF_RESOURCES = "META-INF/resources/";
	
	@SuppressWarnings("unchecked")
	@Override
	public void init(ExtendedProperties arg0) {
	}

    private static final List<String> paths = Arrays.asList("templates","module-templates","xnat-templates","xdat-templates","base-templates");
    private static Map<String,String> templatePaths = Collections.synchronizedMap(new HashMap<String,String>());
    
	/* (non-Javadoc)
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
	 * 
	 * Expected paths are like: 
	 */
	@Override
	public InputStream getResourceStream(String name)
			throws ResourceNotFoundException {
		InputStream result = null;
        
        if (StringUtils.isEmpty(name))
        {
            throw new ResourceNotFoundException ("No template name provided");
        }
        
        if(name.contains("//")){
        	name=name.replace("//", "/");
        }
        
        try 
        {
            final ClassLoader classLoader = this.getClass().getClassLoader();
            
            //VMs can be located in alot of places.  file system vs classpath, xnat-templates vs templates
            //for improved efficiency, we cache the location were we last found a template, that way the loader doesn't have to look for it each time.
            final String known=templatePaths.get(name);
            
            if(known!=null){
            	try {
            		if(known.startsWith("f:")){
            			//VMs found on the file system, will have f: on the start of their path.
    					try {
							result=new BufferedInputStream(new FileInputStream(new File(XDATServlet.WEBAPP_ROOT,known.substring(2))));
							if(result!=null){
								return result;
							}else{
								throw new ResourceNotFoundException(known);
							}
						} catch (Exception e) {
    						throw new ResourceNotFoundException(known);
						}
            		}else if(known.startsWith("c:")){
            			//VMs fond on the classpath, will have c: on the start of their path.
    					result=classLoader.getResourceAsStream(safeJoin("/",META_INF_RESOURCES,known.substring(2)));
    					if(result!=null){
    						return result;
    					}else{
    						throw new ResourceNotFoundException(known);
    					}
            		}else{
						throw new ResourceNotFoundException(name);
            		}
				} catch (Exception e) {
					//ignore
				}
            }
            
            for(final String path:paths){
            	//iterate through potential sub-directories (templates, xnat-templates, etc) looking for a match.
            	//ordering of the paths is significant as it enforces the VM overwriting model
                try {
                	result=findMatch(name,safeJoin("/", path,name));
					if(result!=null){
						return result;
					}
				} catch (Exception e) {
					//ignore this
				}
            }
            
            //check root (without sub directories... allowing one last location to plug in matches)
        	result=findMatch(name,name);
        	if(result!=null){
        		return result;
        	}
        }
        catch( Exception fnfe )
        {
            /*
             *  log and convert to a general Velocity ResourceNotFoundException
             */
            
            throw new ResourceNotFoundException( fnfe.getMessage() );
        }
        
        throw new ResourceNotFoundException(String.format("CustomClasspathResourceLoader: cannot find resource {}", name));
	}
	
	/**
	 * Looks for matching resource at the give possible path.  If its found, it caches the path and returns an InputStream.
	 * 
	 * Returns null if no match is found.
	 * 
	 * @param name
	 * @param possible
	 * @return
	 */
	private InputStream findMatch(String name, String possible){
		try {
			File f = new File(XDATServlet.WEBAPP_ROOT,possible);
			if(f.exists()){
				InputStream result= new BufferedInputStream(new FileInputStream(f));
				templatePaths.put(name.intern(), ("f:"+possible).intern());
				return result;
			}
		} catch (FileNotFoundException e) {
			//ignore.  shouldn't happen because we check if it exists first.
		}
		
		final InputStream result= this.getClass().getClassLoader().getResourceAsStream(safeJoin("/", META_INF_RESOURCES,possible));
		if(result!=null){
			//once we find a match, lets cache the name of it
			templatePaths.put(name.intern(), ("c:"+possible).intern());
		}else{
			//check for file system file
			
		}
		return result;
	}
	
	/**
	 * Joins together multiple strings using the referenced separator.  However, it will not duplicate the separator if the joined strings already include them.
	 * @param <T>
	 * @param sep
	 * @param elements
	 * @return
	 */
	public static <T extends String> String safeJoin(final String sep, T... elements){
		final StringBuilder sb=new StringBuilder();
		for(int i=0;i<elements.length;i++){
			sb.append(elements[i]);
			if((i + 1)<elements.length){
				if(!elements[i].endsWith(sep) && !elements[(i+1)].startsWith(sep)){
					sb.append(sep);
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Identifies all of the VM files in the specified directory
	 * Adds META-INF/resources to the package.
	 * Looks in templates, xnat-templates, xdat-templates, and base-templates
	 * @param dir
	 * @return
	 */
	public static List<URL> findVMsByClasspathDirectory(String dir){
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        assert loader != null;

        final List<URL> matches=Lists.newArrayList();
        try {
        	for(String folder: paths){
        		final org.springframework.core.io.Resource[] resources=new PathMatchingResourcePatternResolver((ClassLoader)null).getResources("classpath*:"+safeJoin("/",META_INF_RESOURCES,folder,dir,"*.vm"));
				for(org.springframework.core.io.Resource r:resources){
					matches.add(r.getURL());
				} 
        	}
		} catch (IOException e) {
			//not sure if we care about this, I don't think so
			logger.error("",e);
		}
		return matches;
	}
	
	/**
	 * Static convenience method for retrieving an InputStream outside of the normal Turbine context.
	 * @param resource
	 * @return
	 * @throws ResourceNotFoundException
	 */
	public static InputStream getInputStream(String resource) throws ResourceNotFoundException{
		CustomClasspathResourceLoader loader= new CustomClasspathResourceLoader();
		return loader.getResourceStream(resource);
	}
	
	@Override
	public boolean isSourceModified(Resource arg0) {
		return false;
	}

	@Override
	public long getLastModified(Resource arg0) {
		return 0;
	}	
}
