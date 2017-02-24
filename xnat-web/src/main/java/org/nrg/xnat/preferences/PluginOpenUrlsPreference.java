package org.nrg.xnat.preferences;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.beans.AbstractPreferenceBean;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.exceptions.UnknownToolId;
import org.nrg.prefs.services.NrgPreferenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.google.common.collect.Lists;

/**
 * The Class PluginOpenUrlsPreference.
 */
@NrgPreferenceBean(toolId = "pluginOpenUrls", toolName = "Plugin Open URL Preferences", 
					description = "Manages plugin open URL authorization", strict = true)
public class PluginOpenUrlsPreference extends AbstractPreferenceBean {
	
	static {
		
	}
	
    /**
     * Instantiates a new plugin open urls preference.
     *
     * @param preferenceService the preference service
     */
    @Autowired
    public PluginOpenUrlsPreference(final NrgPreferenceService preferenceService) {
        super(preferenceService);
        try {
			_pluginResources.addAll(BasicXnatResourceLocator.getResources("classpath*:META-INF/xnat/**/*-plugin.properties"));
		} catch (IOException e) {
			_logger.warn("Could not access plugin resources", e);
		}
    }
    
    /**
     * Gets the allowed plugin open urls.
     *
     * @return the allowed plugin open urls
     */
    @NrgPreference
    public List<String> getAllowedPluginOpenUrls() {
    	return getListValue(PREF_ID);
    }
    
    /**
     * Sets the allowed plugin open urls.
     *
     * @param urls the new allowed plugin open urls
     */
    @NrgPreference
    public void setAllowedPluginOpenUrls(List<String> urls) {
    	try {
			setListValue(PREF_ID, urls);
		} catch (UnknownToolId e) {
			_logger.error("UnknownToolId exception setting allowedPluginOpenUrls", e);
		} catch (InvalidPreferenceName e) {
			_logger.error("InvalidPreferenceName exception setting allowedPluginOpenUrls", e);
		}
    }

	/**
	 * Gets the plugin open urls.
	 *
	 * @return the plugin open urls
	 */
	public List<String> getPluginOpenUrls() {
		return getUrlList(XnatPlugin.PLUGIN_OPEN_URLS);
	}
	
    /**
     * Gets the url list.
     *
     * @param urlType the url type
     * @return the url list
     */
    public List<String> getUrlList(String urlType) {
		final List<String> returnList = Lists.newArrayList();
        for (final Resource resource : _pluginResources) {
			try {
				final Properties properties = PropertiesLoaderUtils.loadProperties(resource);
				if (!properties.containsKey(urlType)) {
					continue;
				}
				returnList.addAll(Arrays.asList(properties.getProperty(urlType).replaceAll(", *",",").split(",")));
			} catch (IOException e) {
				_logger.warn("Could not pull " + urlType + " from plugin resource file (" + resource.getFilename() + ")", e);
			}
        }
		return returnList;
	}
	
    /** The Constant _logger. */
    private static final Logger _logger    = LoggerFactory.getLogger(PluginOpenUrlsPreference.class);
    
    /** The Constant PREF_ID. */
    public static final String PREF_ID = "allowedPluginOpenUrls";
    
    /** The _plugin resources. */
    private List<Resource> _pluginResources = Lists.newArrayList();
    

}
