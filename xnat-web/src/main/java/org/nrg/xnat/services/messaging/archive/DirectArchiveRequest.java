package org.nrg.xnat.services.messaging.archive;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Slf4j
public class DirectArchiveRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private final long id;

    public DirectArchiveRequest(long id) {
        this.id = id;
    }
}
