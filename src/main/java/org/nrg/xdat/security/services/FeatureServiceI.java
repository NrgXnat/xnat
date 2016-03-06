package org.nrg.xdat.security.services;

import java.util.Collection;
import java.util.List;

import org.nrg.xdat.security.UserGroupI;
import org.nrg.xft.security.UserI;

public interface FeatureServiceI {
	
	/**
	 * Retrieve a list of features for this user by tag
	 * @param user    The user to check.
     * @param tag     The tag to find.
	 * @return The features associated with the user with the indicated tag.
	 */
	Collection<String> getFeaturesForUserByTag(UserI user, String tag);
	
	/**
	 * Retrieve a list of features for this user by tags
	 * @param user    The user to check.
     * @param tags    The tags to find.
     * @return The features associated with the user with the indicated tags.
	 */
	Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags);
	
	/**
	 * Get features for this group
	 * @param group    The group to check.
     * @return The features associated with the indicated group.
	 */
	Collection<String> getFeaturesForGroup(UserGroupI group);
	
	/**
	 * Add feature for this group
	 * @param group                The group to add the feature to.
	 * @param feature              The feature to add.
     * @param authenticatedUser    The user requesting the change.
	 */
	void addFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception;
	
	/**
	 * Remove feature from this group
     * @param group                The group to remove the feature from.
     * @param feature              The feature to remote.
     * @param authenticatedUser    The user requesting the change.
	 */
	void removeFeatureSettingFromGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception;
	
	/**
	 * Remove all features from this group
	 * @param group                The group to remove the features from.
     * @param authenticatedUser    The user requesting the change.
	 */
	void removeAllFeatureSettingsFromGroup(UserGroupI group, UserI authenticatedUser) throws Exception;
	
	
	/**
	 * Check if group contains this feature
	 * @param group    The group to check.
     * @param feature  The feature to test.
     * @return Returns true if the group contains the feature, false otherwise.
	 */
	boolean checkFeature(UserGroupI group, String feature);

	/**
	 * Is this user a member of a group with the matching tag and feature
	 * @param user       The user to check.
     * @param tag        The tag to find.
     * @param feature    The feature to test.
     * @return Returns true if the user is in a group that contains the tag and feature, false otherwise.
     */
	boolean checkFeature(UserI user, String tag, String feature);
	
	/**
	 * Is this user a member of any groups with the matching tags and feature
     * @param user       The user to check.
     * @param tags       The tags to find.
     * @param feature    The feature to test.
     * @return Returns true if the user is in a group that contains the tag and feature, false otherwise.
	 */
	boolean checkFeature(UserI user, Collection<String> tags, String feature);

	/**
	 * returns default setting for this feature for a given tag
	 * @param feature    The feature to test.
     * @param tag        The tag to find.
	 * @return Whether the feature is on by default for groups with the indicated tag.
	 */
	boolean isOnByDefaultForGroupType(String feature, String tag);

	/**
	 * Block feature for this group
	 * @param group                The group to block from the feature.
	 * @param feature              The feature to block.
     * @param authenticatedUser    The user requesting the change.
	 */
	void blockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser);

	/**
	 * Unblocks feature for this group
     * @param group                The group to unblock from the feature.
     * @param feature              The feature to unblock.
     * @param authenticatedUser    The user requesting the change.
	 */
	void unblockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser);

	/**
	 * returns default blocked setting for this feature for a given tag
	 * @param feature    The feature to test.
	 * @param tag        The tag to find.
	 * @return Whether the feature is blocked for groups with the indicated tag.
	 */
	boolean isBlockedByGroupType(String feature, String tag);

	/**
	 * Is this user a member of any group with this feature
	 * @param user       The user to check.
	 * @param feature    The feature to test.
	 * @return Whether the user is a member of a group with the indicated feature.
	 */
	boolean checkFeatureForAnyTag(UserI user, String feature);

    /**
     * Blocks the feature by group type.
     * @param feature              The feature to block.
     * @param displayName          The display name.
     * @param authenticatedUser    The user requesting the change.
     */
	void blockByGroupType(String feature, String displayName, UserI authenticatedUser);

    /**
     * Unblocks the feature by group type.
     * @param feature              The feature to unblock.
     * @param displayName          The display name.
     * @param authenticatedUser    The user requesting the change.
     */
	void unblockByGroupType(String feature, String displayName,UserI authenticatedUser);

	void enableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser);

	void disableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser);

	boolean isBlockedByGroup(UserGroupI group, String feature);

	boolean isOnByDefaultForGroup(UserGroupI group, String feature);

	void disableFeatureForGroup(UserGroupI group, String feature,	UserI authenticatedUser);

	Collection<String> getEnabledFeaturesForGroupType(String type);

	Collection<String> getBannedFeaturesForGroupType(String type);

	List<String> getEnabledFeaturesByTag(String tag);

	List<String> getBlockedFeaturesByTag(String tag);

	Collection<String> getBlockedFeaturesForGroup(UserGroupI group);
}
