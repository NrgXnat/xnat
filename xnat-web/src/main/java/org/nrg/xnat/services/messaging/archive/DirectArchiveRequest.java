package org.nrg.xnat.services.messaging.archive;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Getter
@Slf4j
public class DirectArchiveRequest implements Serializable {
    private final long id;

    public DirectArchiveRequest(long id) {
        this.id = id;
    }
}
