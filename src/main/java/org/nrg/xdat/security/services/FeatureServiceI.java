package org.nrg.xdat.security.services;

import java.util.Collection;
import java.util.List;

import org.nrg.xdat.security.UserGroup;
import org.nrg.xft.security.UserI;

public interface FeatureServiceI {
	
	/**
	 * Retrieve a list of features for this user by tag
	 * @param user
	 * @return
	 */
	public Collection<String> getFeaturesForUserByTag(UserI user, String tag);
	
	/**
	 * Retrieve a list of features for this user by tags
	 * @param user
	 * @return
	 */
	public Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags);
	
	/**
	 * Get features for this group
	 * @param group
	 * @return
	 */
	public Collection<String> getFeaturesForGroup(UserGroup group);
	
	/**
	 * Add feature for this group
	 * @param group
	 * @param feature
	 */
	public void addFeatureForGroup(UserGroup group, String feature, UserI authenticatedUser) throws Exception;
	
	/**
	 * Remove feature from this group
	 * @param group
	 * @param feature
	 */
	public void removeFeatureSettingFromGroup(UserGroup group, String feature, UserI authenticatedUser) throws Exception;
	
	/**
	 * Remove all features from this group
	 * @param group
	 * @param feature
	 */
	public void removeAllFeatureSettingsFromGroup(UserGroup group, UserI authenticatedUser) throws Exception;
	
	
	/**
	 * Check if group contains this feature
	 * @param group
	 */
	public boolean checkFeature(UserGroup group,String feature);

	/**
	 * Is this user a member of a group with the matching tag and feature
	 * @param group
	 */
	public boolean checkFeature(UserI user, String tag, String feature);
	
	/**
	 * Is this user a member of any groups with the matching tagand feature
	 * @param group
	 */
	public boolean checkFeature(UserI user, Collection<String> tags, String feature);

	/**
	 * returns default setting for this feature for a given tag
	 * @param feature
	 * @return
	 */
	public boolean isOnByDefaultForGroupType(String feature, String tag);

	/**
	 * Block feature for this group
	 * @param group
	 * @param feature
	 */
	public void blockFeatureForGroup(UserGroup group, String feature, UserI authenticatedUser);


	/**
	 * Un-Block feature for this group
	 * @param group
	 * @param feature
	 */
	public void unblockFeatureForGroup(UserGroup group, String feature, UserI authenticatedUser);

	/**
	 * returns default blocked setting for this feature for a given tag
	 * @param feature
	 * @param tag
	 * @return
	 */
	public boolean isBlockedByGroupType(String feature, String tag);


	/**
	 * Is this user a member of any group with this feature
	 * @param user
	 * @param feature
	 * @return
	 */
	public boolean checkFeatureForAnyTag(UserI user, String feature);

	public void blockByGroupType(String feature, String displayName,UserI authenticatedUser);

	public void unblockByGroupType(String feature, String displayName,UserI authenticatedUser);

	public void enableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser);

	public void disableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser);

	public boolean isBlockedByGroup(UserGroup group, String feature);

	public boolean isOnByDefaultForGroup(UserGroup group, String feature);

	public void disableFeatureForGroup(UserGroup group, String feature,	UserI authenticatedUser);

	public Collection<String> getEnabledFeaturesForGroupType(String type);

	public Collection<String> getBannedFeaturesForGroupType(String type);

	public List<String> getEnabledFeaturesByTag(String tag);

	public List<String> getBlockedFeaturesByTag(String tag);
}
