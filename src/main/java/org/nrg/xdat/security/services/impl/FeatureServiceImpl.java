/*
 * core: org.nrg.xdat.security.services.impl.FeatureServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.NrgEventService;
import org.nrg.xdat.entities.GroupFeature;
import org.nrg.xdat.security.UserGroup;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.services.FeatureServiceI;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.GroupFeatureService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.xdat.om.base.auto.AutoXdatUsergroup.SCHEMA_ELEMENT_NAME;
import static org.nrg.xft.event.XftItemEvent.builder;
import static org.nrg.xft.event.XftItemEventI.UPDATE;

@Service
@Getter(PRIVATE)
@Accessors(prefix = "_")
@Slf4j
public class FeatureServiceImpl implements FeatureServiceI {

    @Autowired
    public void setCache(final GroupsAndPermissionsCache cache) {
        _cache = cache;
    }

    @Autowired
    public void setGroupFeatureService(final GroupFeatureService groupFeatureService) {
        _groupFeatureService = groupFeatureService;
    }

    @Autowired
    public void setEventService(final NrgEventService eventService) {
        _eventService = eventService;
    }

    @Autowired
    public void setFeatureService(final FeatureServiceI featureService) {
        _featureService = featureService;
    }

    @Override
    public Collection<String> getFeaturesForGroup(final UserGroupI group) {
        return group != null ? ((UserGroup) group).getFeatures() : null;
    }

    @Override
    public void addFeatureForGroup(final UserGroupI group, final String feature, final UserI authenticatedUser) {
        if (!checkFeature(group, feature)) {
            final GroupFeature groupFeature = _groupFeatureService.findGroupFeature(group.getId(), feature);
            if (groupFeature == null) {
                _groupFeatureService.addFeatureToGroup(group.getId(), group.getTag(), feature);
            } else {
                if(groupFeature.isBlocked()) {
                   groupFeature.setBlocked(false);
                }
                groupFeature.setOnByDefault(true);
                _groupFeatureService.update(groupFeature);
            }
            
            ((UserGroup) group).removeBlockedFeature(feature);
            ((UserGroup) group).addFeature(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
                _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
            } catch (Exception e1) {
                log.error("", e1);
            }
        }
    }

    @Override
    public void removeFeatureSettingFromGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        if (checkFeature(group, feature)) {
            _groupFeatureService.delete(group.getId(), feature);

            ((UserGroup) group).removeFeature(feature);
            ((UserGroup) group).removeBlockedFeature(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
                _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
            } catch (Exception e1) {
                log.error("", e1);
            }
        }
    }

    @Override
    public void removeAllFeatureSettingsFromGroup(UserGroupI group, UserI authenticatedUser) {
        _groupFeatureService.deleteByGroup(group.getId());

        final UserGroup userGroup = (UserGroup) group;
        userGroup.clearFeatures();
        userGroup.clearBlockedFeatures();

        try {
            //group objects are cached by an old caching implementation which listened for events
            _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
        } catch (Exception e1) {
            log.error("", e1);
        }
    }

    @Override
    public Collection<String> getFeaturesForUserByTag(final UserI user, final String tag) {
        try {
            final UserGroupI group = getCache().getGroupForUserAndTag(user.getUsername(), tag);
            return getFeatureService().getFeaturesForGroup(group);
        } catch (UserNotFoundException ignored) {
            // We have the UserI so we know it exists already.
            return Collections.emptyList();
        }
    }

    @Override
    public Collection<String> getFeaturesForUserByTags(UserI user, Collection<String> tags) {
        final Collection<String> combined = new HashSet<>();
        for (final String tag : tags) {
            combined.addAll(getFeaturesForUserByTag(user, tag));
        }
        return combined;
    }

    @Override
    public boolean checkFeature(UserGroupI group, String feature) {
        if (StringUtils.isBlank(feature) || Features.isBanned(feature)) {
            return false;
        }
        if (group != null && isOnByDefaultForGroup(group, feature)) {
            //if feature configured to have access, then return true
            return true;
        }
        if (group != null && isOnByDefaultForGroupType(feature, group.getDisplayname())) {
            //if not blocked return true, else false
            return !isBlockedByGroup(group, feature);
        }
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
        try {
            final Map<String, UserGroupI> groups = getCache().getGroupsForUser(user.getUsername());
            for (final UserGroupI group : groups.values()) {
                if (checkFeature(group, feature)) {
                    return true;
                }
            }
        } catch (UserNotFoundException ignored) {
            // We have the UserI so we know it exists already.
        }
        return false;
    }

    @Override
    public boolean isOnByDefaultForGroupType(String feature, String displayName) {
        return STATUS.ON.equals(getCachedSettingByType(displayName, feature));
    }

    @Override
    public boolean isBlockedByGroupType(String feature, String displayName) {
        return STATUS.BLOCKED.equals(getCachedSettingByType(displayName, feature));
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
        final UserGroup userGroup = (UserGroup) group;
        if (userGroup.hasFeature(feature)) {
            final GroupFeature groupFeature = _groupFeatureService.findGroupFeature(group.getId(), feature);
            if (groupFeature.isOnByDefault()) {
                groupFeature.setOnByDefault(false);
                _groupFeatureService.update(groupFeature);
            }

            userGroup.removeFeature(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
                _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
            } catch (Exception e1) {
                log.error("", e1);
            }
        }
    }

    @Override
    public void blockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        final UserGroup userGroup = (UserGroup) group;
        if (!userGroup.hasBlockedFeature(feature)) {
            _groupFeatureService.blockFeatureForGroup(group.getId(), group.getTag(), feature);
            userGroup.addBlockedFeature(feature);
            userGroup.removeFeature(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
                _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
            } catch (Exception e1) {
                log.error("", e1);
            }
        }
    }

    @Override
    public void unblockFeatureForGroup(UserGroupI group, String feature, UserI authenticatedUser) {
        final UserGroup userGroup = (UserGroup) group;
        if (userGroup.hasBlockedFeature(feature)) {
            final GroupFeature groupFeature = _groupFeatureService.findGroupFeature(group.getId(), feature);
            if (groupFeature.isBlocked()) {
                groupFeature.setBlocked(false);
                _groupFeatureService.update(groupFeature);
            }
            userGroup.removeBlockedFeature(feature);
            userGroup.addFeature(feature);

            try {
                //group objects are cached by an old caching implementation which listened for events
                _eventService.triggerEvent(builder().xsiType(SCHEMA_ELEMENT_NAME).id(group.getId()).action(UPDATE).build());
            } catch (Exception e1) {
                log.error("", e1);
            }
        }
    }

    @Override
    public Collection<String> getEnabledFeaturesForGroupType(final String type) {
        initCacheByType();
        final List<String> features = new ArrayList<>();
        final String       prefix   = STATUS_PREFIX + type + ".";
        for (final Map.Entry<String, STATUS> entry : byType.entrySet()) {
            final String key = entry.getKey();
            if (STATUS.ON.equals(entry.getValue()) && key.startsWith(prefix)) {
                features.add(key.substring((prefix).length()));
            }
        }
        return features;
    }

    @Override
    public Collection<String> getBannedFeaturesForGroupType(final String type) {
        initCacheByType();
        final List<String> features = new ArrayList<>();
        final String       prefix   = STATUS_PREFIX + type + ".";
        for (final Map.Entry<String, STATUS> entry : byType.entrySet()) {
            final String key = entry.getKey();
            if (STATUS.BLOCKED.equals(entry.getValue()) && key.startsWith(prefix)) {
                features.add(key.substring((prefix).length()));
            }
        }
        return features;
    }

    @Override
    public List<String> getEnabledFeaturesByTag(final String tag) {
        return Lists.transform(_groupFeatureService.getEnabledByTag(tag), GROUP_FEATURE_STRING_FUNCTION);
    }

    @Override
    public List<String> getBlockedFeaturesByTag(String tag) {
        return Lists.transform(_groupFeatureService.getBannedByTag(tag), GROUP_FEATURE_STRING_FUNCTION);
    }

    @Override
    public Collection<String> getBlockedFeaturesForGroup(UserGroupI group) {
        return group != null ? ((UserGroup) group).getBlockedFeatures() : null;
    }

    public enum STATUS {
        ON,
        OFF,
        BLOCKED
    }

    /**
     * Cached features by type
     */
    static               Map<String, STATUS> byType = null;
    private static final Object              MUTEX  = new Object();

    private void initCacheByType() {
        if (byType == null) {
            byType = Maps.newHashMap();
            synchronized (MUTEX) {
                List<GroupFeature> all = _groupFeatureService.findFeaturesForTag(Features.SITE_WIDE);
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

            GroupFeature gr = _groupFeatureService.findGroupFeature(STATUS_PREFIX + type, feature);
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
                _groupFeatureService.update(gr);
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
                _groupFeatureService.create(gr);
            }
        }
    }

    private static final Function<GroupFeature, String> GROUP_FEATURE_STRING_FUNCTION = new Function<GroupFeature, String>() {
        @Override
        public String apply(final GroupFeature groupFeature) {
            return groupFeature.getFeature();
        }
    };

    private static final String STATUS_PREFIX = "__";

    private GroupsAndPermissionsCache _cache;
    private GroupFeatureService       _groupFeatureService;
    private NrgEventService           _eventService;
    private FeatureServiceI           _featureService;
}
