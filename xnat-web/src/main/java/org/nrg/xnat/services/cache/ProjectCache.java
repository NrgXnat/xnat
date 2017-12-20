package org.nrg.xnat.services.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XdatUsergroup;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.fn.Consumer;
import reactor.fn.Predicate;

import java.util.*;
import java.util.regex.Pattern;

import static reactor.bus.selector.Selectors.predicate;

@SuppressWarnings("Duplicates")
@Service
@Slf4j
public class ProjectCache implements Consumer<Event<XftItemEvent>> {
    @Autowired
    public ProjectCache(final CacheManager cacheManager, final EventBus eventBus) {
        _cache = cacheManager.getCache("UserProjectCacheManagerCache");
        _aliasMapping = new HashMap<>();

        eventBus.on(predicate(new Predicate<Object>() {
            @Override
            public boolean test(final Object object) {
                return object != null && object instanceof XftItemEvent && isGroupItem((XftItemEvent) object);
            }
        }), this);
    }

    @Override
    public void accept(final Event<XftItemEvent> xftItemEventEvent) {

    }

    public boolean has(final String idOrAlias) {
        return _aliasMapping.containsKey(idOrAlias);
    }

    public boolean has(final String userId, final String idOrAlias) {
        if (!has(idOrAlias)) {
            return false;
        }

        final Pair<XnatProjectdata, Map<String, Boolean>> projectCache = getProjectCache(idOrAlias);
        final Map<String, Boolean>                        userCache    = projectCache.getRight();
        return userCache.containsKey(userId);
    }

    final boolean isWritable(final String userId, final String idOrAlias) {
        if (isCachedNonexistentProject(idOrAlias)) {
            return false;
        }
        final Pair<XnatProjectdata, Map<String, Boolean>> projectCache = getProjectCache(idOrAlias);
        final Map<String, Boolean>                        userCache    = projectCache.getRight();
        return userCache.containsKey(userId) && userCache.get(userId);
    }

    public XnatProjectdata get(final UserI user, final String idOrAlias) {
        final String userId = user.getUsername();

        // added caching here to prevent duplicate project queries in every file transaction
        // the cache is shared with the one in gradual dicom importer, which does a similar query.
        if (has(userId, idOrAlias)) {
            log.debug("Found cached entry for project ID or alias {} for user {}", idOrAlias, userId);
            if (isCachedNonWritableProject(userId, idOrAlias)) {
                log.debug("Found a cached project for {} that is non-writable for the user {}.", idOrAlias, userId);
            } else if (isCachedNonexistentProject(idOrAlias)) {
                log.debug("Found a cached alias {} for the user {} that is not a project ID or alias.", idOrAlias, userId);
            } else {
                log.debug("Found a cached project for {} that is writable for the user {}. This took {} ms for a total of {} ms for the operation.", idOrAlias, userId);
                final XnatProjectdata project = doGet(user, idOrAlias);
                if (project != null) {
                    log.debug("Found a cached project for ID or alias {} for user {}.", idOrAlias, userId);
                    return project;
                } else {
                    log.warn("A weird condition occurred in the user project cache: has({}, {}) returned true, no non-writable or nonexistent project was found, but doGet({}, {}) returned null. This method will return null, but this isn't a case that should arise.", userId, idOrAlias, userId, idOrAlias);
                }
            }
        } else {
            log.debug("Didn't find a cached entry for project ID or alias {} for user {}", idOrAlias, userId);
            // no cached value, look in the db
            final XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(idOrAlias, user, false);

            // We found a project...
            if (project != null) {
                doPut(user, project);
            } else {
                // This value is not a project ID or alias.
                log.debug("Search for project with ID or alias of {} for the user {} but that doesn't seem to exist.", idOrAlias, userId);
                cacheNonexistentProject(idOrAlias);
            }
        }

        return null;
    }

    private Pair<XnatProjectdata, Map<String, Boolean>> getProjectCache(final String idOrAlias) {
        final String projectId = _aliasMapping.get(idOrAlias);
        //noinspection unchecked
        return StringUtils.isNotBlank(projectId) ? (Pair<XnatProjectdata, Map<String, Boolean>>) _cache.get(projectId, Pair.class) : null;
    }

    private XnatProjectdata doGet(final UserI user, final String idOrAlias) {
        final Pair<XnatProjectdata, Map<String, Boolean>> projectCache = getProjectCache(idOrAlias);
        final XnatProjectdata                             project      = projectCache.getLeft();
        final Map<String, Boolean>                        users        = projectCache.getRight();
        final String                                      userId       = user.getUsername();

        final boolean writable;
        if (!users.containsKey(userId)) {
            writable = Permissions.canEditProject(user, project.getId());
            users.put(userId, writable);
        } else {
            writable = users.get(userId);
        }
        return writable ? projectCache.getLeft() : null;
    }

    private void doPut(final UserI user, final XnatProjectdata project) {
        final Pair<XnatProjectdata, Map<String, Boolean>> projectCache = getProjectCache(project.getId());
        if (projectCache == null) {
            final String projectId = project.getId();
            final Set<String> ids = getAliasIds(project);
            for (final String id : ids) {
                _aliasMapping.put(id, projectId);
            }
        }
    }

    private boolean isCachedNonWritableProject(final String userId, final String idOrAlias) {
        if (!has(idOrAlias) || isCachedNonexistentProject(idOrAlias)) {
            return false;
        }
        final Pair<XnatProjectdata, Map<String, Boolean>> projectCache = getProjectCache(idOrAlias);
        final Map<String, Boolean>                        users        = projectCache.getRight();
        return users.containsKey(userId) && !users.get(userId);
    }

    private boolean isCachedNonexistentProject(final String idOrAlias) {
        return StringUtils.equals(NOT_A_PROJECT, _aliasMapping.get(idOrAlias));
    }

    private void cacheNonexistentProject(final String idOrAlias) {
        _aliasMapping.put(idOrAlias, NOT_A_PROJECT);
    }

    private Pair<XnatProjectdata, Map<String, Boolean>> getProject(final String idOrAlias) {
        final String projectId = _cache.get(idOrAlias, String.class);
        //noinspection unchecked
        return StringUtils.isNotBlank(projectId) ? (Pair<XnatProjectdata, Map<String, Boolean>>) _cache.get(getProjectKey(projectId)) : null;
    }

    private Pair<XnatProjectdata, Map<String, Boolean>> putProject(final XnatProjectdata project) {
        final String projectId  = project.getId();

        final List<String> allProjectIds = new ArrayList<>();
        allProjectIds.addAll(getAliasIds(project));

        for (final String alias : allProjectIds) {
            _aliasMapping.put(alias, projectId);
        }

        final Pair<XnatProjectdata, Map<String, Boolean>> entry = new ImmutablePair<XnatProjectdata, Map<String, Boolean>>(project, new HashMap<String, Boolean>());
        _cache.put(projectKey, entry);

        return entry;
    }

    private static Set<String> getAliasIds(final XnatProjectdata project) {
        final Set<String> aliasIds = new HashSet<>();
        aliasIds.add(project.getId());
        aliasIds.addAll(Lists.transform(project.getAliases_alias(), new Function<XnatProjectdataAliasI, String>() {
            @Override
            public String apply(final XnatProjectdataAliasI alias) {
                return alias.getAlias();
            }
        }));
        return aliasIds;
    }

    private static boolean isGroupItem(final XftItemEvent event) {
        final Object item = event.getItem();
        try {
            return (item instanceof XFTItem && ((XFTItem) item).instanceOf(XdatUsergroup.SCHEMA_ELEMENT_NAME)) || XdatUsergroup.class.isAssignableFrom(item.getClass());
        } catch (ElementNotFoundException ignored) {
            return false;
        }
    }

    private static final String  NOT_A_WRITABLE_PROJECT = "NOT_A_WRITABLE_PROJECT";
    private static final String  NOT_A_PROJECT          = "NOT_A_PROJECT";
    private static final Pattern PROJECT_GROUP          = Pattern.compile("(?<project>.*)_(owner|member|collaborator)?");

    private final Cache               _cache;
    private final Map<String, String> _aliasMapping;
}
