package org.nrg.config.interfaces;

/**
 * Allows creators of custom properties to specify a callback that's invoked when a property it's interested in is changed.
 * @author ehaas01
 *
 */
public interface SiteConfigurationPropertyChangedListener {
	
	void siteConfigurationPropertyChanged(String propertyName, String newPropertyValue);
}
