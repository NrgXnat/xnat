package org.nrg.xnat.services.cache.jms;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;

@Component
@Slf4j
public class InitializeGroupRequestListener implements GroupsAndPermissionsCache.Listener {
    @Autowired
    public InitializeGroupRequestListener(final GroupsAndPermissionsCache cache) {
        _cache = cache;
        _groupIds = new HashSet<>();
        _processed = Sets.newConcurrentHashSet();
        _start = new Date();

        if (cache instanceof GroupsAndPermissionsCache.Provider) {
            ((GroupsAndPermissionsCache.Provider) _cache).registerListener(this);
            log.info("Registered with cache provider of type: {}", _cache.getClass().getName());
        }
    }

    @JmsListener(destination = "initializeGroup", containerFactory = "listenerContainerFactory")
    public void onRequest(final InitializeGroupRequest request) {
        final LapStopWatch stopWatch = LapStopWatch.createStarted(log, Level.INFO);
        final String       groupId   = request.getGroupId();

        final int totalNumberOfGroups = _groupIds.size();
        try {
            stopWatch.lap("Starting to process group '{}', this is #{} of {} total", groupId, _processed.size() + 1, totalNumberOfGroups);
            final UserGroupI group = _cache.cacheGroup(groupId);
            if (group == null) {
                stopWatch.lap("Group '{}' not found on initialization, nothing cached", groupId);
            } else {
                stopWatch.lap("Retrieved group '{}' successfully", groupId);
            }
            _processed.add(groupId);
        } finally {
            final int remaining = totalNumberOfGroups - _processed.size();
            if (remaining > 0) {
                stopWatch.stop("Completed processing group '{}', there are {} groups remaining out of {}", groupId, remaining, totalNumberOfGroups);
            } else {
                stopWatch.stop("Completed processing group '{}', there are NO groups remaining of the original {}! Total time elapsed {} ms", groupId, totalNumberOfGroups, FORMATTER.format(new Date().getTime() - _start.getTime()));
            }
        }
    }

    /**
     * Returns a set containing the IDs of the groups that have not yet been cached.
     *
     * @return A set of group IDs for groups that have not yet been cached.
     */
    @Override
    public Set<String> getUnprocessed() {
        return Sets.difference(_groupIds, _processed);
    }

    /**
     * Returns a set containing the IDs of the groups that have been cached.
     *
     * @return A set of group IDs for groups that have been cached.
     */
    @Override
    public Set<String> getProcessed() {
        return ImmutableSet.copyOf(_processed);
    }

    /**
     * Accessor for time/date cache initialization was started.
     *
     * @return The start time/date.
     */
    @Override
    public Date getStart() {
        return new Date(_start.getTime());
    }

    /**
     * Accessor for time/date cache initialization was completed.
     *
     * @return The completion time/date.
     */
    @Override
    public Date getCompleted() {
        return _completed == null ? null : new Date(_completed.getTime());
    }

    public void setGroupIds(final List<String> groupIds) {
        _groupIds.addAll(groupIds);
        log.info("Added {} group IDs to the listener: {}", _groupIds.size(), StringUtils.join(_groupIds, ", "));
    }

    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.getDefault());

    private final Date                      _start;
    private final GroupsAndPermissionsCache _cache;
    private final Set<String>               _groupIds;
    private final Set<String>               _processed;

    private Date _completed;
}
