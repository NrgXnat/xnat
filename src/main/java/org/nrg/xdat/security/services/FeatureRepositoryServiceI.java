/*
 * core: org.nrg.xdat.security.services.FeatureRepositoryServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import java.util.Collection;

import org.nrg.xdat.security.helpers.FeatureDefinitionI;

public interface FeatureRepositoryServiceI {
	String DEFAULT_FEATURE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.FeatureRepositoryServiceImpl";

	/**
	 * Get all (including disabled)
	 * @return All features in the system.
	 */
	Collection<? extends FeatureDefinitionI> getAllFeatures();
	
	/**
	 * Get by key
	 * @param key    The key of the feature to retrieve.
	 * @return The requested feature.
	 */
    FeatureDefinitionI getByKey(String key);

	/**
	 * Prevent this feature from being used on this server
	 * @param feature    The key for the feature to be banned.
	 */
    void banFeature(String feature);

	/**
	 * Allow this feature to be used on this server
     * @param feature    The key for the feature to be unbanned.
	 */
    void unBanFeature(String feature);

	/**
	 * Turn on this feature by default for all user groups
     * @param feature    The key for the feature to be enabled by default.
	 */
    void enableByDefault(String feature);

	/**
	 * Turn off this feature by default for all user groups
     * @param feature    The key for the feature to be disabled.
	 */
    void disableByDefault(String feature);

	/**
	 * Updates secure definitions.
	 */
    void updateNewSecureDefinitions();
}

