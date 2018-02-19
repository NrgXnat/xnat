package org.nrg.xnat.services.cache.jms;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Accessors(prefix = "_")
public class InitializeGroupRequest implements Serializable {
    public InitializeGroupRequest(final String groupId) {
        _groupId = groupId;
    }

    private final String _groupId;
}
