package org.nrg.xdat.security.helpers;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.services.FeatureRepositoryServiceI;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Lists;

public class Features {
    static Logger logger = Logger.getLogger(Features.class);
        
    private static FeatureServiceI singleton=null;
    private static FeatureRepositoryServiceI repository=null;
    
    /**
     * Returns the currently configured features service
     * 
     * You can change the default implementation returned via the security.featureService.default configuration parameter
     * @return
     */
    public static FeatureServiceI getFeatureService(){
    	if(singleton==null){
	       	//default to FeatureServiceImpl implementation (unless a different default is configured)
    		//we can swap in other ones later by setting a default 
    		//we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
       		try {
				String className=XDAT.getSiteConfigurationProperty("security.featureService.default", "org.nrg.xdat.security.FeatureServiceImpl");
				singleton=(FeatureServiceI)Class.forName(className).newInstance();
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			} catch (ConfigServiceException e) {
				logger.error("",e);
			}
    	}
    	return singleton;
    }
    
    /**
     * Returns the currently configured features service
     * 
     * You can change the default implementation returned via the security.featureService.default configuration parameter
     * @return
     */
    public static FeatureRepositoryServiceI getFeatureRepositoryService(){
    	if(repository==null){
	       	//default to FeatureRepositoryServiceImpl implementation (unless a different default is configured)
    		//we can swap in other ones later by setting a default 
    		//we can even have a config tab in the admin ui which allows sites to select their configuration of choice.
       		try {
				String className=XDAT.getSiteConfigurationProperty("security.featureRepositoryService.default", "org.nrg.xdat.security.FeatureRepositoryServiceImpl");
				repository=(FeatureRepositoryServiceI)Class.forName(className).newInstance();
			} catch (ClassNotFoundException e) {
				logger.error("",e);
			} catch (InstantiationException e) {
				logger.error("",e);
			} catch (IllegalAccessException e) {
				logger.error("",e);
			} catch (ConfigServiceException e) {
				logger.error("",e);
			}
    	}
    	return repository;
    }
    
    public static Collection<? extends FeatureDefinitionI> getAllFeatures(){
    	return getFeatureRepositoryService().getAllFeatures();
    }
    
    public static Collection<String> getEnabledFeatures(){
    	List<String> features=Lists.newArrayList();
    	for(FeatureDefinitionI def: getAllFeatures()){
    		if(def.isOnByDefault()){
    			features.add(def.getKey());
    		}
    	}
    	return features;
    }
    
    public static Collection<String> getBannedFeatures(){
    	List<String> features=Lists.newArrayList();
    	for(FeatureDefinitionI def: getAllFeatures()){
    		if(def.isBanned()){
    			features.add(def.getKey());
    		}
    	}
    	return features;
    }

    
    public static Collection<String> getEnabledFeaturesForGroupType(String type){
    	return getFeatureService().getEnabledFeaturesForGroupType(type);
    }
    
    public static Collection<String> getBannedFeaturesForGroupType(String type){
    	return getFeatureService().getBannedFeaturesForGroupType(type);
    }
		
	/**
	 * Retrieve a list of features for this user by tag
	 * @param user
	 * @return
	 */
	public static Collection<String> getFeaturesForUserByTag(UserI user, String tag){
		return getFeatureService().getFeaturesForUserByTag(user, tag);
	}
	
	/**
	 * Retrieve a list of features for this user by tags
	 * @param user
	 * @return
	 */
	public static Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags){
		return getFeatureService().getFeaturesForUserByTags(user, tags);
	}
	
	/**
	 * Get features for this group
	 * @param group
	 * @return
	 */
	public static Collection<String> getFeaturesForGroup(UserGroupI group){
		return getFeatureService().getFeaturesForGroup(group);
	}
	
	/**
	 * Add feature for this group
	 * @param group
	 * @param feature
	 */
	public static void addFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().addFeatureForGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * Enable feature for this group
	 * @param group
	 * @param feature
	 */
	public static void enableFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().addFeatureForGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * Enable feature for this group
	 * @param group
	 * @param feature
	 */
	public static void disableFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().disableFeatureForGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * Block feature for this group
	 * @param group
	 * @param feature
	 */
	public static void blockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().blockFeatureForGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * UnBlock feature for this group
	 * @param group
	 * @param feature
	 */
	public static void unblockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().unblockFeatureForGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * Remove feature from this group
	 * @param group
	 * @param feature
	 */
	public static void deleteFeatureSettingFromGroup(UserGroupI group, String feature, UserI authenticatedUser) throws Exception{
		getFeatureService().removeFeatureSettingFromGroup(group,feature,authenticatedUser);
	}
	
	/**
	 * Remove all features from this group
	 * @param group
	 * @param feature
	 */
	public static void deleteAllFeaturesFromGroup(UserGroupI group, UserI authenticatedUser) throws Exception{
		getFeatureService().removeAllFeatureSettingsFromGroup(group,authenticatedUser);
	}
	
	/**
	 * Check if group contains this feature
	 * @param group
	 */
	public static boolean checkFeature(UserGroupI group,String feature){
		return getFeatureService().checkFeature(group,feature);
	}

	/**
	 * Is this user a member of any group with this feature
	 * @param user
	 * @param feature
	 * @return
	 */
	public static boolean checkFeatureForAnyTag(UserI user, String feature){
		return getFeatureService().checkFeatureForAnyTag(user, feature);
	}

	/**
	 * Is this user a member of a group with the matching tag and feature
	 * @param group
	 */
	public static boolean checkFeature(UserI user, String tag, String feature){
		return getFeatureService().checkFeature(user, tag, feature);
	}
	
	/**
	 * Is this user a member of any groups with the matching tag and feature
	 * @param group
	 */
	public static boolean checkFeature(UserI user, Collection<String> tags, String feature){
		return getFeatureService().checkFeature(user, tags, feature);
	}
	
	//TODO: Probably want some caching (memoization) for the banned and on by defaults
	
	/**
	 * Is this feature banned on the server
	 * @param feature
	 * @return
	 */
	public static Boolean isBanned(String feature){
		FeatureDefinitionI def=getFeatureRepositoryService().getByKey(feature);
		if(def==null){
			return true;
		}
		return def.isBanned();
	}

	/**
	 * returns default blocked setting for this feature for a given tag
	 * @param feature
	 * @return
	 */
	public static boolean isBlockedByGroupType(String feature, String displayName){
		return getFeatureService().isBlockedByGroupType(feature, displayName);
	}

	/**
	 * returns blocked setting for this feature for a group
	 * @param feature
	 * @return
	 */
	public static boolean isBannedByGroup(UserGroupI group, String feature){
		return getFeatureService().isBlockedByGroup(group,feature);
	}
	
	
	/**
	 * Is this feature on by default for all users and groups
	 * @param feature
	 * @return
	 */
	public static Boolean isOnByDefault(String feature){
		FeatureDefinitionI def=getFeatureRepositoryService().getByKey(feature);
		if(def==null){
			return false;
		}
		return def.isOnByDefault();
	}
	
	/**
	 * returns default setting for this feature for a given tag
	 * @param feature
	 * @return
	 */
	public static boolean isOnByDefaultByGroupType(String feature, String displayName){
		return getFeatureService().isOnByDefaultForGroupType(feature, displayName);
	}
	
	/**
	 * returns setting for this feature for a given group
	 * @param feature
	 * @return
	 */
	public static boolean isOnByDefaultByGroup(UserGroupI group, String feature){
		return getFeatureService().isOnByDefaultForGroup(group,feature);
	}
	
	/**
	 * Prevent this feature from being used on this server
	 * @param feature
	 */
	public static void banFeature(String feature){
		getFeatureRepositoryService().banFeature(feature);
	}

	/**
	 * Allow this feature to be used on this server
	 * @param feature
	 */
	public static void unBanFeature(String feature){
		getFeatureRepositoryService().unBanFeature(feature);
	}

	/**
	 * Turn on this feature by default for all user groups
	 * @param feature
	 */
	public static void enableByDefault(String feature){
		getFeatureRepositoryService().enableByDefault(feature);
	}

	/**
	 * Turn off this feature by default for all user groups
	 * @param feature
	 */
	public static void disableByDefault(String feature){
		getFeatureRepositoryService().disableByDefault(feature);
	}
	
	public static void blockByGroupType(String feature, String displayName,UserI authenticatedUser){
		getFeatureService().blockByGroupType(feature,displayName,authenticatedUser);
	}

	public static void unblockByGroupType(String feature, String displayName,UserI authenticatedUser){
		getFeatureService().unblockByGroupType(feature,displayName,authenticatedUser);
	}

	public static void enableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser){
		getFeatureService().enableIsOnByDefaultByGroupType(feature,displayName,authenticatedUser);
	}

	public static void disableIsOnByDefaultByGroupType(String feature, String displayName,UserI authenticatedUser){
		getFeatureService().disableIsOnByDefaultByGroupType(feature,displayName,authenticatedUser);
	}

	public static List<String> getBlockedFeaturesByTag(String tag) {
		return getFeatureService().getBlockedFeaturesByTag(tag);
	}

	public static Collection<String> getBlockedFeaturesForGroup(UserGroupI group) {
		return getFeatureService().getBlockedFeaturesForGroup(group);
	}

	public static List<String> getEnabledFeaturesByTag(String tag) {
		return getFeatureService().getEnabledFeaturesByTag(tag);
	}

	public static final String SITE_WIDE="_SITE_WIDE";

}
