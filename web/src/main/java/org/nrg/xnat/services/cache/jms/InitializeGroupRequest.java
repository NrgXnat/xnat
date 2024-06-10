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
        log.debug("Creating initialize request for group {}", groupId);
        _groupId = groupId;
    }

    @Override
    public String toString() {
        return "Initialize group request " + _groupId;
    }

    private final String _groupId;
}
