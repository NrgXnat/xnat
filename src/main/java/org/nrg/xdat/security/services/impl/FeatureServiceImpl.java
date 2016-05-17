package org.nrg.xdat.security.services.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class FeatureServiceImpl implements FeatureServiceI {
	
    /**
     * The _eventService.
     */
    @Autowired
 	private NrgEventService _eventService;

    @Override
    public Collection<String> getFeaturesForGroup(UserGroupI group) {
        if (group != null) {
            return ((UserGroup) group).getFeatures();
        } else {
            return null;
        }
    }

    @Override
    public void addFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if (!checkFeature(group, feature)) {
            GroupFeature gr = _service.findGroupFeature(group.getId(), feature);
            if (gr == null) {
                _service.addFeatureToGroup(group.getId(), group.getTag(), feature);
            } else {
                gr.setOnByDefault(true);
                _service.update(gr);
            }

            ((UserGroup) group).getFeatures().add(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
            	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }
    }

    @Override
    public void removeFeatureSettingFromGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if (checkFeature(group, feature)) {
            _service.delete(group.getId(), feature);

            ((UserGroup) group).getFeatures().remove(feature);
            ((UserGroup) group).getBlockedFeatures().remove(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
            	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }
    }

    @Override
    public void removeAllFeatureSettingsFromGroup(UserGroupI group, UserI authenticatedUser) {
        _service.deleteByGroup(group.getId());

        ((UserGroup) group).getFeatures().clear();
        ((UserGroup) group).getBlockedFeatures().clear();

        try {
            //group objects are cached by an old caching implementation which listened for events
        	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
        } catch (Exception e1) {
            logger.error("", e1);
        }
    }

    @Override
    public Collection<String> getFeaturesForUserByTag(UserI user, String tag) {
        return ((XDATUser) user).getFeaturesForUserByTag(tag);
    }

    @Override
    public Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags) {
        Collection<String> combined = Lists.newArrayList();
        for (String tag : tags) {
            for (String feature : getFeaturesForUserByTag(user, tag)) {
                if (!combined.contains(feature)) {
                    combined.add(feature);
                }
            }
        }
        return combined;
    }

    @Override
    public boolean checkFeature(UserGroupI group, String feature) {
        if (feature == null) {
            return false;
        }
        if (Features.isBanned(feature)) {
            return false;
        }

        if (group != null && isOnByDefaultForGroup(group, feature)) {
            //if feature configured to have access, then return true
            return true;
        } else if (group != null && isOnByDefaultForGroupType(feature, group.getDisplayname())) {
            //if not blocked return true, else false
            return !isBlockedByGroup(group, feature);
        } else
            //if not blocked return true, else false
            return Features.isOnByDefault(feature) && (!(group != null && isBlockedByGroup(group, feature)) && !(group != null && isBlockedByGroupType(feature, group.getDisplayname())));
    }

    @Override
    public boolean checkFeature(UserI user, String tag, String feature) {
        return ((XDATUser) user).checkFeature(tag, feature);
    }

    @Override
    public boolean checkFeature(UserI user, Collection<String> tags, String feature) {
        return ((XDATUser) user).checkFeature(tags, feature);
    }

    @Override
    public boolean checkFeatureForAnyTag(UserI user, String feature) {
        for (UserGroupI group : ((XDATUser) user).getGroups().values()) {
            if (checkFeature(group, feature)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isOnByDefaultForGroupType(String feature, String displayName) {
        STATUS cache = getCachedSettingByType(displayName, feature);

        return cache != null && STATUS.ON.equals(cache);

    }

    @Override
    public boolean isBlockedByGroupType(String feature, String displayName) {
        STATUS cache = getCachedSettingByType(displayName, feature);

        return cache != null && STATUS.BLOCKED.equals(cache);
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
        return (((UserGroup) group).getBlockedFeatures().contains(feature));
    }

    @Override
    public boolean isOnByDefaultForGroup(UserGroupI group, String feature) {
        return (getFeaturesForGroup(group)).contains(feature);
    }

    @Override
    public void disableFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if ((getFeaturesForGroup(group)).contains(feature)) {
            GroupFeature gr = _service.findGroupFeature(group.getId(), feature);
            if (gr.isOnByDefault()) {
                gr.setOnByDefault(false);
                _service.update(gr);
            }

            ((UserGroup) group).getFeatures().remove(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
            	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }
    }

    @Override
    public void blockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if (!((UserGroup) group).getBlockedFeatures().contains(feature)) {

            _service.blockFeatureForGroup(group.getId(), group.getTag(), feature);

            ((UserGroup) group).getBlockedFeatures().add(feature);

            if (((UserGroup) group).getFeatures().contains(feature)) {
                ((UserGroup) group).getFeatures().remove(feature);
            }

            try {
                //group objects are cached by an old caching implementation which listened for events
            	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }
    }

    @Override
    public void unblockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if (((UserGroup) group).getBlockedFeatures().contains(feature)) {
            GroupFeature gr = _service.findGroupFeature(group.getId(), feature);
            if (gr.isBlocked()) {
                gr.setBlocked(false);
                _service.update(gr);
            }

            if ((getFeaturesForGroup(group)).contains(feature)) {
                ((UserGroup) group).getFeatures().remove(feature);
            }

            ((UserGroup) group).getBlockedFeatures().add(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
            	_eventService.triggerEvent(new XftItemEvent(XdatUsergroup.SCHEMA_ELEMENT_NAME, group.getId(), XftItemEvent.UPDATE));
            } catch (Exception e1) {
                logger.error("", e1);
            }
        }
    }

    @Override
    public Collection<String> getEnabledFeaturesForGroupType(String type) {
        initCacheByType();
        List<String> features = Lists.newArrayList();
        for (Map.Entry<String, STATUS> entry : byType.entrySet()) {
            if (STATUS.ON.equals(entry.getValue()) && entry.getKey().startsWith(STATUS_PREFIX + type + ".")) {
                features.add(entry.getKey().substring((STATUS_PREFIX + type + ".").length()));
            }
        }
        return features;
    }

    @Override
    public Collection<String> getBannedFeaturesForGroupType(String type) {
        initCacheByType();
        List<String> features = Lists.newArrayList();
        for (Map.Entry<String, STATUS> entry : byType.entrySet()) {
            if (STATUS.BLOCKED.equals(entry.getValue()) && entry.getKey().startsWith(STATUS_PREFIX + type + ".")) {
                features.add(entry.getKey().substring((STATUS_PREFIX + type + ".").length()));
            }
        }
        return features;
    }

    @Override
    public List<String> getEnabledFeaturesByTag(String tag) {
        List<String> features = Lists.newArrayList();

        for (GroupFeature gr : _service.getEnabledByTag(tag)) {
            features.add(gr.getFeature());
        }

        return features;
    }

    @Override
    public List<String> getBlockedFeaturesByTag(String tag) {
        List<String> features = Lists.newArrayList();

        for (GroupFeature gr : _service.getBannedByTag(tag)) {
            features.add(gr.getFeature());
        }

        return features;
    }

    @Override
    public Collection<String> getBlockedFeaturesForGroup(UserGroupI group) {
        if (group != null) {
            return ((UserGroup) group).getBlockedFeatures();
        } else {
            return null;
        }
    }

    public enum STATUS {
        ON,
        OFF,
        BLOCKED
    }

    /**
     * Cached features by type
     */
    static Map<String, STATUS> byType = null;
    private static final Object MUTEX = new Object();

    private void initCacheByType() {
        if (byType == null) {
            byType = Maps.newHashMap();
            synchronized (MUTEX) {
                List<GroupFeature> all = _service.findFeaturesForTag(Features.SITE_WIDE);
                for (GroupFeature gr : all) {
                    byType.put((gr.getGroupId() + "." + gr.getFeature()).intern(), (gr.isBlocked()) ? STATUS.BLOCKED : ((gr.isOnByDefault()) ? STATUS.ON : STATUS.OFF));
                }
            }
        }
    }

    private STATUS getCachedSettingByType(String type, String feature) {
        initCacheByType();

        return byType.get(STATUS_PREFIX + type + "." + feature);
    }

    private void setCachedSettingByType(String type, String feature, STATUS setting) {
        initCacheByType();

        synchronized (MUTEX) {
            byType.put((STATUS_PREFIX + type + "." + feature).intern(), setting);

            GroupFeature gr = _service.findGroupFeature(STATUS_PREFIX + type, feature);
            if (gr != null) {
                if (STATUS.BLOCKED.equals(setting) && !gr.isBlocked()) {
                    gr.setBlocked(true);
                } else if (STATUS.ON.equals(setting) && !gr.isOnByDefault()) {
                    gr.setOnByDefault(true);
                    gr.setBlocked(false);
                } else {
                    gr.setOnByDefault(false);
                    gr.setBlocked(false);
                }
                _service.update(gr);
            } else {
                gr = new GroupFeature();
                gr.setGroupId(STATUS_PREFIX + type);
                gr.setTag(Features.SITE_WIDE);
                gr.setFeature(feature);

                if (STATUS.BLOCKED.equals(setting)) {
                    gr.setBlocked(true);
                    gr.setOnByDefault(false);
                } else if (STATUS.ON.equals(setting)) {
                    gr.setOnByDefault(true);
                    gr.setBlocked(false);
                } else {
                    gr.setOnByDefault(false);
                    gr.setBlocked(false);
                }
                _service.create(gr);
            }
        }
    }

    private static final String STATUS_PREFIX = "__";
    private static final Logger logger = Logger.getLogger(FeatureServiceImpl.class);

    @Autowired
    @Lazy
    private GroupFeatureService _service;
}
