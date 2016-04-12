package org.nrg.xdat.bean;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.nrg.framework.utilities.Reflection;

import com.google.common.collect.Maps;

public class ClassMapping implements ClassMappingI {
    private static final Logger logger = Logger.getLogger(ClassMapping.class);
    private static final String   DEFINITION_PACKAGE    = "org.nrg.xdat.bean";
    private static final Pattern  DEFINITION_PROPERTIES = Pattern.compile(".*-bean-definition\\.properties");
    
	private final Map<String,String> elements;
    
	public ClassMapping(){
		elements=Maps.newHashMap();
		final Set<String> propFiles = Reflection.findResources(DEFINITION_PACKAGE, DEFINITION_PROPERTIES);
        if (propFiles.size() > 0) {
            for (final String props : propFiles) {
            	try {
					final Configuration config = new PropertiesConfiguration(props);
					@SuppressWarnings("unchecked")
					Iterator<Object> keys=config.getKeys();
					while(keys.hasNext()){
						String key=(String)keys.next();
						String value=config.getString(key);
						elements.put(key,value);
					}
				} catch (ConfigurationException e) {
					logger.error("",e);
				}
            }
        }
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xdat.bean.ClassMappingI#getElements()
	 */
	@Override
	public Map<String,String> getElements(){
		return elements;
	}
}