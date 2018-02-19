package org.nrg.xnat.services.cache.jms;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.utilities.LapStopWatch;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xnat.services.cache.DefaultGroupsAndPermissionsCache;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.*;

@Component
@Slf4j
public class InitializeGroupRequestListener {
    @Autowired
    public InitializeGroupRequestListener(final GroupsAndPermissionsCache cache) {
        _cache = cache;
        _groupIds = new HashSet<>();
        _groupTimings = ArrayListMultimap.create();
        _start = new Date();

        if (_cache instanceof DefaultGroupsAndPermissionsCache) {
            ((DefaultGroupsAndPermissionsCache) _cache).registerListener(this);
        }
    }

    @JmsListener(destination = "initializeGroup", containerFactory = "listenerContainerFactory")
    public void onRequest(final InitializeGroupRequest request) {
        final LapStopWatch stopWatch = LapStopWatch.createStarted(log, Level.INFO);
        final String       groupId   = request.getGroupId();

        final UserGroupI group;
        try {
            stopWatch.lap("Starting to process group '{}'", groupId);
            group = _cache.get(groupId);
            if (group == null) {
                stopWatch.lap("Skipping group '{}', no value found on initialization", groupId);
            } else {
                stopWatch.lap("Retrieved group '{}' successfully", groupId);
            }
        } finally {
            _groupTimings.putAll(groupId, stopWatch.getLaps());
            final int remaining = _groupIds.size() - _groupTimings.keySet().size();
            if (remaining > 0) {
                stopWatch.stop("Completed processing group '{}', there are {} groups remaining", groupId, remaining);
            } else {
                stopWatch.stop("Completed processing group '{}', there are NO groups remaining! Total time elapsed {} ms", groupId, FORMATTER.format(new Date().getTime() - _start.getTime()));
            }
        }
    }

    @SuppressWarnings("unused")
    public Set<String> getUnprocessedGroupIds() {
        return Sets.difference(_groupIds, _groupTimings.keySet());
    }

    @SuppressWarnings("unused")
    public Multimap<String, LapStopWatch.Lap> getGroupTimings() {
        return _groupTimings;
    }

    public void setGroupIds(final List<String> groupIds) {
        _groupIds.addAll(groupIds);
    }

    private static final NumberFormat FORMATTER = NumberFormat.getNumberInstance(Locale.getDefault());

    private final Date                               _start;
    private final GroupsAndPermissionsCache          _cache;
    private final Set<String>                        _groupIds;
    private final Multimap<String, LapStopWatch.Lap> _groupTimings;
}
