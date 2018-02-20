package org.nrg.xnat.services.cache.jms;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Getter
@Accessors(prefix = "_")
@Slf4j
public class InitializeGroupRequest implements Serializable {
    public InitializeGroupRequest(final String groupId) {
        log.info("Creating initialize request for group {}", groupId);
        _groupId = groupId;
    }

    private final String _groupId;
}
