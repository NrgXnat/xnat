package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xdat.security.helpers.FeatureDefinitionI;

public interface FeatureRepositoryServiceI {
	/**
	 * Get all (including disabled)
	 * @return
	 */
	public Collection<? extends FeatureDefinitionI> getAllFeatures();
	
	/**
	 * Get by key
	 * @param key
	 * @return
	 */
	public FeatureDefinitionI getByKey(String key);

	/**
	 * Prevent this feature from being used on this server
	 * @param feature
	 */
	public void banFeature(String feature);

	/**
	 * Allow this feature to be used on this server
	 * @param feature
	 */
	public void unBanFeature(String feature);

	/**
	 * Turn on this feature by default for all user groups
	 * @param feature
	 */
	public void enableByDefault(String feature);

	/**
	 * Turn off this feature by default for all user groups
	 * @param feature
	 */
	public void disableByDefault(String feature);
}
