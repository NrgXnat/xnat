/*
 * core: org.nrg.xdat.security.helpers.Features
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

import com.google.common.collect.Lists;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.Collection;
import java.util.List;

public class Features {

    private static final Logger logger = LoggerFactory.getLogger(Features.class);

    private static FeatureServiceI singleton = null;
    private static FeatureRepositoryServiceI repository = null;

    /**
     * Returns the currently configured features service. You can change the default implementation returned via the
     * security.featureService.default configuration parameter
     *
     * @return An instance of the {@link FeatureServiceI feature service}.
     */
    public static FeatureServiceI getFeatureService() {
        if (singleton == null) {
            singleton = getServiceInstance(FeatureServiceI.class, "security.featureService.default", "org.nrg.xdat.security.services.impl.FeatureServiceImpl");
        }
        return singleton;
    }

    /**
     * Sets a new features service.
     */
    public static void setFeatureServiceToSiteConfigPreference() {
        try {
            singleton = Class.forName(XDAT.getSiteConfigPreferences().getFeatureService()).asSubclass(FeatureServiceI.class).newInstance();
        }
        catch(Exception e){
            logger.error("Failed to set Feature Service.",e);
        }
    }

    /**
     * Returns the currently configured feature repository service. You can change the default implementation returned
     * via the security.featureService.default configuration parameter.
     *
     * @return An instance of the {@link FeatureRepositoryServiceI feature repository service}.
     */
    public static FeatureRepositoryServiceI getFeatureRepositoryService() {
        if (repository == null) {
            repository = getServiceInstance(FeatureRepositoryServiceI.class, "security.featureRepositoryService.default", "org.nrg.xdat.security.services.impl.FeatureRepositoryServiceImpl");
        }
        return repository;
    }

    /**
     * Sets a new features repository service.
     */
    public static void setFeatureRepositoryServiceToSiteConfigPreference() {
        try {
            repository = Class.forName(XDAT.getSiteConfigPreferences().getFeatureRepositoryService()).asSubclass(FeatureRepositoryServiceI.class).newInstance();
        }
        catch(Exception e){
            logger.error("Failed to set Feature Service.",e);
        }
    }

    public static Collection<? extends FeatureDefinitionI> getAllFeatures() {
        return getFeatureRepositoryService().getAllFeatures();
    }

    public static Collection<String> getEnabledFeatures() {
        List<String> features = Lists.newArrayList();
        for (FeatureDefinitionI def : getAllFeatures()) {
            if (def.isOnByDefault()) {
                features.add(def.getKey());
            }
        }
        return features;
    }

    public static Collection<String> getBannedFeatures() {
        List<String> features = Lists.newArrayList();
        for (FeatureDefinitionI def : getAllFeatures()) {
            if (def.isBanned()) {
                features.add(def.getKey());
            }
        }
        return features;
    }

    public static Collection<String> getEnabledFeaturesForGroupType(String type) {
        return getFeatureService().getEnabledFeaturesForGroupType(type);
    }


    public static Collection<String> getBannedFeaturesForGroupType(String type) {
        return getFeatureService().getBannedFeaturesForGroupType(type);
    }

    /**
     * Retrieve a list of features for this user by tag.
     *
     * @param user The user to search on.
     * @param tag  The tag to search for.
     * @return The features available to the user with the indicated tag.
     */
    @SuppressWarnings("unused")
    public static Collection<String> getFeaturesForUserByTag(UserI user, String tag) {
        return getFeatureService().getFeaturesForUserByTag(user, tag);
    }

    /**
     * Retrieve a list of features for this user by tags.
     *
     * @param user The user to search on.
     * @param tags The tag sto search for.
     * @return The features available to the user with the indicated tags.
     */
    @SuppressWarnings("unused")
    public static Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags) {
        return getFeatureService().getFeaturesForUserByTags(user, tags);
    }

    /**
     * Get features for this group
     *
     * @param group The group to search on.
     * @return The features associated with the indicated group.
     */
    public static Collection<String> getFeaturesForGroup(UserGroupI group) {
        return getFeatureService().getFeaturesForGroup(group);
    }

    /**
     * Add feature for this group
     *
     * @param group   The group to which the feature should be added.
     * @param feature The feature to add.
     * @param user    The user performing the operation.
     */
    @SuppressWarnings("unused")
    public static void addFeatureForGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().addFeatureForGroup(group, feature, user);
    }

    /**
     * Enable feature for this group.
     *
     * @param group   The group for which the feature should be enabled.
     * @param feature The feature to be enabled.
     * @param user    The user performing the operation.
     */
    public static void enableFeatureForGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().addFeatureForGroup(group, feature, user);
    }

    /**
     * Disable feature for this group.
     *
     * @param group   The group for which the feature should be disabled.
     * @param feature The feature to be disabled.
     * @param user    The user performing the operation.
     */
    public static void disableFeatureForGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().disableFeatureForGroup(group, feature, user);
    }

    /**
     * Block feature for this group.
     *
     * @param group   The group for which the feature should be blocked.
     * @param feature The feature to be blocked.
     * @param user    The user performing the operation.
     */
    public static void blockFeatureForGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().blockFeatureForGroup(group, feature, user);
    }

    /**
     * Unblock feature for this group.
     *
     * @param group   The group for which the feature should be unblocked.
     * @param feature The feature to be unblocked.
     * @param user    The user performing the operation.
     */
    public static void unblockFeatureForGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().unblockFeatureForGroup(group, feature, user);
    }

    /**
     * Remove feature from this group.
     *
     * @param group   The group for which the feature should be deleted.
     * @param feature The feature to be delete.
     * @param user    The user performing the operation.
     */
    @SuppressWarnings("unused")
    public static void deleteFeatureSettingFromGroup(UserGroupI group, String feature, UserI user) throws Exception {
        getFeatureService().removeFeatureSettingFromGroup(group, feature, user);
    }

    /**
     * Remove all features from this group
     *
     * @param group The group for which all features should be deleted.
     * @param user  The user performing the operation.
     */
    @SuppressWarnings("unused")
    public static void deleteAllFeaturesFromGroup(UserGroupI group, UserI user) throws Exception {
        getFeatureService().removeAllFeatureSettingsFromGroup(group, user);
    }

    /**
     * Check if group contains this feature
     *
     * @param group   The group to check for a feature.
     * @param feature The feature to check for.
     */
    public static boolean checkFeature(UserGroupI group, String feature) {
        return getFeatureService().checkFeature(group, feature);
    }

    /**
     * Checks whether the indicated user is a member of any group with this feature.
     *
     * @param user    The user to check.
     * @param feature The feature to check for.
     * @return Returns true if the user is a member of a group with access to the indicated feature, false otherwise.
     */
    public static boolean checkFeatureForAnyTag(UserI user, String feature) {
        return getFeatureService().checkFeatureForAnyTag(user, feature);
    }

    /**
     * Checks whether the indicated user is a member of a group with the matching tag and feature.
     *
     * @param user    The user to check.
     * @param tag     The tag to search for.
     * @param feature The feature to check for.
     * @return Returns true if the user is a member of a group with access to the indicated feature and tag, false
     * otherwise.
     */
    @SuppressWarnings("unused")
    public static boolean checkFeature(UserI user, String tag, String feature) {
        return getFeatureService().checkFeature(user, tag, feature);
    }

    /**
     * Checks whether the user is a member of any groups with the matching tags and feature.
     *
     * @param user    The user to check.
     * @param tags    The tags to search for.
     * @param feature The feature to check for.
     * @return Returns true if the user is a member of a group with access to the indicated feature and tags, false
     * otherwise.
     */
    @SuppressWarnings("unused")
    public static boolean checkFeature(UserI user, Collection<String> tags, String feature) {
        return getFeatureService().checkFeature(user, tags, feature);
    }

    //TODO: Probably want some caching (memoization) for the banned and on by defaults

    /**
     * Checks whether the indicated feature is banned on the server.
     *
     * @param feature The feature to check for.
     * @return Returns true if the feature is banned, false otherwise.
     */
    public static Boolean isBanned(final String feature) {
        final FeatureDefinitionI def = getFeatureRepositoryService().getByKey(feature);
        return def == null || def.isBanned();
    }

    /**
     * Returns default blocked setting for this feature for a given tag.
     *
     * @param feature The feature to check for.
     * @param tag     The tag to search for.
     * @return Returns true if the feature and tag are blocked by default, false otherwise.
     */
    public static boolean isBlockedByGroupType(String feature, String tag) {
        return getFeatureService().isBlockedByGroupType(feature, tag);
    }

    /**
     * Returns blocked setting for this feature for a group.
     *
     * @param group   The group to check for a feature.
     * @param feature The feature to check for.
     * @return Returns true if the feature is banned by the group, false otherwise.
     */
    public static boolean isBannedByGroup(UserGroupI group, String feature) {
        return getFeatureService().isBlockedByGroup(group, feature);
    }

    /**
     * Checks whether the indicated feature is on by default for all users and groups.
     *
     * @param feature The feature to check for.
     * @return Returns true if the feature is on by default, false otherwise.
     */
    public static Boolean isOnByDefault(String feature) {
        FeatureDefinitionI def = getFeatureRepositoryService().getByKey(feature);
        return def != null && def.isOnByDefault();
    }


    /**
     * Checks whether the indicated feature is on by default for groups with a given tag.
     *
     * @param feature The feature to check for.
     * @param tag     The tag to search for.
     * @return Returns true if the feature is on by default for matching groups, false otherwise.
     */
    public static boolean isOnByDefaultByGroupType(String feature, String tag) {
        return getFeatureService().isOnByDefaultForGroupType(feature, tag);
    }

    /**
     * Checks whether the indicated feature is on by default for the indicated group
     *
     * @param group   The group to search on.
     * @param feature The feature to search on.
     * @return Returns true if the feature is on by default for the indicated group, false otherwise.
     */
    public static boolean isOnByDefaultByGroup(UserGroupI group, String feature) {
        return getFeatureService().isOnByDefaultForGroup(group, feature);
    }

    /**
     * Prevent this feature from being used on this server.
     *
     * @param feature The feature to ban.
     */
    public static void banFeature(String feature) {
        getFeatureRepositoryService().banFeature(feature);
    }

    /**
     * Allow this feature to be used on this server
     *
     * @param feature The feature to unban.
     */
    public static void unBanFeature(String feature) {
        getFeatureRepositoryService().unBanFeature(feature);
    }

    /**
     * Turn on this feature by default for all user groups
     *
     * @param feature The feature to turn on for all user groups.
     */
    public static void enableByDefault(String feature) {
        getFeatureRepositoryService().enableByDefault(feature);
    }

    /**
     * Turn off this feature by default for all user groups
     *
     * @param feature The feature to turn off for all user groups.
     */
    public static void disableByDefault(String feature) {
        getFeatureRepositoryService().disableByDefault(feature);
    }

    /**
     * Blocks the indicated feature for all groups with the indicated tag.
     *
     * @param feature The feature to block.
     * @param tag     The tag to search for.
     * @param user    The user performing the operation.
     */
    public static void blockByGroupType(String feature, String tag, UserI user) {
        getFeatureService().blockByGroupType(feature, tag, user);
    }

    /**
     * Unblocks the indicated feature for all groups with the indicated tag.
     *
     * @param feature The feature to block.
     * @param tag     The tag to search for.
     * @param user    The user performing the operation.
     */
    public static void unblockByGroupType(String feature, String tag, UserI user) {
        getFeatureService().unblockByGroupType(feature, tag, user);
    }

    /**
     * Sets the indicated feature's default to on for all groups with the indicated tag.
     *
     * @param feature The feature to set to on by default.
     * @param tag     The tag to search for.
     * @param user    The user performing the operation.
     */
    public static void enableIsOnByDefaultByGroupType(String feature, String tag, UserI user) {
        getFeatureService().enableIsOnByDefaultByGroupType(feature, tag, user);
    }

    /**
     * Sets the indicated feature's default to off for all groups with the indicated tag.
     *
     * @param feature The feature to set to off by default.
     * @param tag     The tag to search for.
     * @param user    The user performing the operation.
     */
    public static void disableIsOnByDefaultByGroupType(String feature, String tag, UserI user) {
        getFeatureService().disableIsOnByDefaultByGroupType(feature, tag, user);
    }

    /**
     * Gets a list of all blocked features for groups that have the indicated tag.
     *
     * @param tag The tag to search for.
     * @return A list of the blocked features for groups with the indicated tag.
     */
    @SuppressWarnings("unused")
    public static List<String> getBlockedFeaturesByTag(String tag) {
        return getFeatureService().getBlockedFeaturesByTag(tag);
    }

    /**
     * Gets a list of all blocked features for the indicated group.
     *
     * @param group The group to search on.
     * @return A list of the blocked features for the indicated group.
     */
    public static Collection<String> getBlockedFeaturesForGroup(UserGroupI group) {
        return getFeatureService().getBlockedFeaturesForGroup(group);
    }

    /**
     * Gets a list of all enabled features for groups that have the indicated tag.
     *
     * @param tag The tag to search for.
     * @return A list of the enabled features for groups with the indicated tag.
     */
    @SuppressWarnings("unused")
    public static List<String> getEnabledFeaturesByTag(String tag) {
        return getFeatureService().getEnabledFeaturesByTag(tag);
    }


    /**
     * Returns the default feature setting for the indicated site-wide role.
     *
     * @param feature The feature to check for.
     * @param role    The role to check for.
     * @return Returns true if the feature is on by default for the indicated role, false otherwise.
     */
    public static boolean isOnByDefaultBySiteRole(String feature, String role) {
        return getFeatureService().isOnByDefaultForGroupType(feature, "role:" + role);
    }

    /**
     * Returns the default blocked setting for this feature for a given role.
     *
     * @param feature The feature to check for.
     * @param role    The role to check for.
     * @return Returns true if the feature is blocked for the indicated role, false otherwise.
     */
    @SuppressWarnings("unused")
    public static boolean isBlockedBySiteRole(String feature, String role) {
        return getFeatureService().isBlockedByGroupType(feature, "role" + role);
    }

    public static final String SITE_WIDE = "_SITE_WIDE";

    private static <T> T getServiceInstance(Class<T> clazz, final String preference, final String aDefault) {
        // MIGRATION: All of these services need to switch from having the implementation in the prefs service to autowiring from the context.
        // First find out if it exists in the application context.
        final ContextService contextService = XDAT.getContextService();
        if (contextService != null) {
            try {
                return contextService.getBean(clazz);
            } catch (NoSuchBeanDefinitionException ignored) {
                // This is OK, we'll just create it from the indicated class.
            }
        }
        // If the application context didn't have an instance...
        //default to FeatureServiceImpl implementation (unless a different default is configured)
        //we can swap in other ones later by setting a default
        //we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
        try {
            final String className = XDAT.safeSiteConfigProperty(preference, aDefault);
            return Class.forName(className).asSubclass(clazz).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error("", e);
        }
        return null;
    }
}
