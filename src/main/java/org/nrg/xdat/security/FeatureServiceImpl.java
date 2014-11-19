package org.nrg.xdat.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventManager;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class FeatureServiceImpl implements FeatureServiceI{
    private static final String __ = "__";
	static Logger logger = Logger.getLogger(FeatureServiceImpl.class);
    

	@Override
	public Collection<String> getFeaturesForGroup(UserGroupI group) {
		if(group!=null){
			return ((UserGroup)group).getFeatures();
		}else{
			return null;
		}
	}

	@Override
	public void addFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
		if(!checkFeature(group,feature)){
			GroupFeature gr=XDAT.getContextService().getBean(GroupFeatureService.class).findGroupFeature(group.getId(), feature);
			if(gr==null){
				XDAT.getContextService().getBean(GroupFeatureService.class).addFeatureToGroup(group.getId(), group.getTag(), feature);
			}else{
				gr.setOnByDefault(true);
				XDAT.getContextService().getBean(GroupFeatureService.class).update(gr);
			}
			
			((UserGroup)group).getFeatures().add(feature);
			
			try {
				//group objects are cached by an old caching implementation which listened for events
				EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
			} catch (Exception e1) {
				logger.error("", e1);
			}
		}
	}

	@Override
	public void removeFeatureSettingFromGroup(UserGroupI group, String feature, UserI authenticatedUser) {
		if(checkFeature(group,feature)){
			XDAT.getContextService().getBean(GroupFeatureService.class).delete(group.getId(), feature);
			
			((UserGroup)group).getFeatures().remove(feature);
			((UserGroup)group).getBlockedFeatures().remove(feature);
			
			try {
				//group objects are cached by an old caching implementation which listened for events
				EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
			} catch (Exception e1) {
				logger.error("", e1);
			}
		}
	}

	@Override
	public void removeAllFeatureSettingsFromGroup(UserGroupI group, UserI authenticatedUser) {
		XDAT.getContextService().getBean(GroupFeatureService.class).deleteByGroup(group.getId());
		
		((UserGroup)group).getFeatures().clear();
		((UserGroup)group).getBlockedFeatures().clear();
		
		try {
			//group objects are cached by an old caching implementation which listened for events
			EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
		} catch (Exception e1) {
			logger.error("", e1);
		}
	}

	@Override
	public Collection<String> getFeaturesForUserByTag(UserI user, String tag) {
		return ((XDATUser)user).getFeaturesForUserByTag(tag);
	}

	@Override
	public Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags) {
		Collection<String> combined=Lists.newArrayList();
		for(String tag: tags){
			for(String feature: getFeaturesForUserByTag(user, tag)){
				if(!combined.contains(feature)){
					combined.add(feature);
				}
			}
		}
		return combined;
	}

	@Override
	public boolean checkFeature(UserGroupI group, String feature) {
		if(feature==null){
			return false;
		}
		if(Features.isBanned(feature)){
			return false;
		}
		
		if(group!=null && isOnByDefaultForGroup(group,feature)){
			//if feature configured to have access, then return true
			return true;
		}else if(group!=null && isOnByDefaultForGroupType(feature,group.getDisplayname())){
			//if not blocked return true, else false
			return !isBlockedByGroup(group, feature);
		}else if(Features.isOnByDefault(feature)){
			//if not blocked return true, else false
			return (!(group!=null && isBlockedByGroup(group, feature)) && !(group!=null && isBlockedByGroupType(feature, group.getDisplayname())));
		}else{
			return false;
		}
	}

	@Override
	public boolean checkFeature(UserI user, String tag, String feature) {
		return ((XDATUser)user).checkFeature(tag,feature);
	}

	@Override
	public boolean checkFeature(UserI user, Collection<String> tags, String feature) {
		return ((XDATUser)user).checkFeature(tags,feature);
	}

	@Override
	public boolean checkFeatureForAnyTag(UserI user, String feature) {
		for(UserGroupI group:((XDATUser)user).getGroups().values()){
			if(checkFeature(group,feature)){
				return true;
			}
		}
		
		return false;
	}

	public enum STATUS{ON,OFF,BLOCKED};

	/**
	 * Cached features by type 
	 */
	static Map<String,STATUS> byType=null;
	
	private static void initCacheByType(){
		if(byType==null){
			byType=Maps.newHashMap();
			synchronized(byType){
				List<GroupFeature> all=XDAT.getContextService().getBean(GroupFeatureService.class).findFeaturesForTag(Features.SITE_WIDE);
				for(GroupFeature gr: all){
					byType.put((gr.getGroupId()+"."+gr.getFeature()).intern(), (gr.isBlocked())?STATUS.BLOCKED:((gr.isOnByDefault())?STATUS.ON:STATUS.OFF));
				}
			}
		}
	}
	
	private static STATUS getCachedSettingByType(String type, String feature){
		initCacheByType();
		
		return byType.get(__ +type+"."+feature);
	}
	
	private static void setCachedSettingByType(String type, String feature, STATUS setting) {
		initCacheByType();
		
		synchronized(byType){
			byType.put((__ +type+"."+feature).intern(),setting);
			
			GroupFeature gr = XDAT.getContextService().getBean(GroupFeatureService.class).findGroupFeature(__ + type, feature);
			if(gr!=null){
				if(STATUS.BLOCKED.equals(setting) && !gr.isBlocked()){
					gr.setBlocked(true);
				}else if(STATUS.ON.equals(setting) && !gr.isOnByDefault()){
					gr.setOnByDefault(true);
					gr.setBlocked(false);
				}else{
					gr.setOnByDefault(false);
					gr.setBlocked(false);
				}
				XDAT.getContextService().getBean(GroupFeatureService.class).update(gr);
			}else{
				gr=new GroupFeature();
				gr.setGroupId(__ + type);
				gr.setTag(Features.SITE_WIDE);
				gr.setFeature(feature);
				
				if(STATUS.BLOCKED.equals(setting)){
					gr.setBlocked(true);
					gr.setOnByDefault(false);
				}else if(STATUS.ON.equals(setting)){
					gr.setOnByDefault(true);
					gr.setBlocked(false);
				}else{
					gr.setOnByDefault(false);
					gr.setBlocked(false);
				}
				XDAT.getContextService().getBean(GroupFeatureService.class).create(gr);
			}
		}
	}
	
	@Override
	public boolean isOnByDefaultForGroupType(String feature, String displayName) {
		STATUS cache=getCachedSettingByType(displayName,feature);
		
		if(cache!=null && STATUS.ON.equals(cache)){
			return true;
		}else{
			return false;
		}
		
	}
	
	@Override
	public boolean isBlockedByGroupType(String feature, String displayName) {
		STATUS cache=getCachedSettingByType(displayName,feature);
		
		if(cache!=null && STATUS.BLOCKED.equals(cache)){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public void blockByGroupType(String feature, String displayName, UserI authenticatedUser) {
		setCachedSettingByType(displayName, feature, STATUS.BLOCKED);
	}

	@Override
	public void unblockByGroupType(String feature, String displayName, UserI authenticatedUser) {
		setCachedSettingByType(displayName, feature, STATUS.OFF);
	}

	@Override
	public void enableIsOnByDefaultByGroupType(String feature, String displayName, UserI authenticatedUser) {
		setCachedSettingByType(displayName, feature, STATUS.ON);
	}

	@Override
	public void disableIsOnByDefaultByGroupType(String feature, String displayName, UserI authenticatedUser) {
		setCachedSettingByType(displayName, feature, STATUS.OFF);
	}

	@Override
	public boolean isBlockedByGroup(UserGroupI group, String feature) {
		return (((UserGroup)group).getBlockedFeatures().contains(feature));
	}

	@Override
	public boolean isOnByDefaultForGroup(UserGroupI group, String feature) {
		return (getFeaturesForGroup(group)).contains(feature);
	}

	@Override
	public void disableFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
		if((getFeaturesForGroup(group)).contains(feature)){
			GroupFeature gr=XDAT.getContextService().getBean(GroupFeatureService.class).findGroupFeature(group.getId(), feature);
			if(gr.isOnByDefault()){
				gr.setOnByDefault(false);
				XDAT.getContextService().getBean(GroupFeatureService.class).update(gr);
			}
			
			((UserGroup)group).getFeatures().remove(feature);
			
			try {
				//group objects are cached by an old caching implementation which listened for events
				EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
			} catch (Exception e1) {
				logger.error("", e1);
			}
		}
	}

	@Override
	public void blockFeatureForGroup(UserGroupI group, String feature,	UserI authenticatedUser) {
		if(!((UserGroup)group).getBlockedFeatures().contains(feature)){
			
			XDAT.getContextService().getBean(GroupFeatureService.class).blockFeatureForGroup(group.getId(), group.getTag(), feature);
			
			((UserGroup)group).getBlockedFeatures().add(feature);
			
			if(((UserGroup)group).getFeatures().contains(feature)){
				((UserGroup)group).getFeatures().remove(feature);
			}
			
			try {
				//group objects are cached by an old caching implementation which listened for events
				EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
			} catch (Exception e1) {
				logger.error("", e1);
			}
		}
	}

	@Override
	public void unblockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
		if(((UserGroup)group).getBlockedFeatures().contains(feature)){
			GroupFeature gr=XDAT.getContextService().getBean(GroupFeatureService.class).findGroupFeature(group.getId(), feature);
			if(gr.isBlocked()){
				gr.setBlocked(false);
				XDAT.getContextService().getBean(GroupFeatureService.class).update(gr);
			}
			
			if((getFeaturesForGroup(group)).contains(feature)){
				((UserGroup)group).getFeatures().remove(feature);
			}
			
			((UserGroup)group).getBlockedFeatures().add(feature);
			
			try {
				//group objects are cached by an old caching implementation which listened for events
				EventManager.Trigger(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), Event.UPDATE);
			} catch (Exception e1) {
				logger.error("", e1);
			}
		}
	}

	@Override
	public Collection<String> getEnabledFeaturesForGroupType(String type) {
		initCacheByType();
		List<String> features=Lists.newArrayList();
		for(Map.Entry<String, STATUS> entry: byType.entrySet()){
			if(STATUS.ON.equals(entry.getValue()) && entry.getKey().startsWith(__ + type + ".")){
				features.add(entry.getKey().substring((__ + type + ".").length()));
			}
		}
		return features;
	}

	@Override
	public Collection<String> getBannedFeaturesForGroupType(String type) {
		initCacheByType();
		List<String> features=Lists.newArrayList();
		for(Map.Entry<String, STATUS> entry: byType.entrySet()){
			if(STATUS.BLOCKED.equals(entry.getValue()) && entry.getKey().startsWith(__ + type + ".")){
				features.add(entry.getKey().substring((__ + type + ".").length()));
			}
		}
		return features;
	}

	@Override
	public List<String> getEnabledFeaturesByTag(String tag) {
		List<String> features=Lists.newArrayList();
		
		for(GroupFeature gr:XDAT.getContextService().getBean(GroupFeatureService.class).getEnabledByTag(tag)){
			features.add(gr.getFeature());
		}
		
		return features;
	}

	@Override
	public List<String> getBlockedFeaturesByTag(String tag) {
		List<String> features=Lists.newArrayList();
		
		for(GroupFeature gr:XDAT.getContextService().getBean(GroupFeatureService.class).getBannedByTag(tag)){
			features.add(gr.getFeature());
		}
		
		return features;
	}

	@Override
	public Collection<String> getBlockedFeaturesForGroup(UserGroupI group) {
		if(group!=null){
			return ((UserGroup)group).getBlockedFeatures();
		}else{
			return null;
		}
	}

}
