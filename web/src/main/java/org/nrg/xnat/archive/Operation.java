package org.nrg.xnat.archive;

import org.nrg.xnat.helpers.prearchive.PrearcUtils;

import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_ARCHIVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_BUILDING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_DELETING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_MOVING;
import static org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus.QUEUED_SEPARATING;

public enum Operation {
    Archive(QUEUED_ARCHIVING),
    Delete(QUEUED_DELETING),
    Move(QUEUED_MOVING),
    Rebuild(QUEUED_BUILDING),
    Separate(QUEUED_SEPARATING),
    Import(null);

    private final PrearcUtils.PrearcStatus queuedStatus;

    Operation(PrearcUtils.PrearcStatus queuedStatus) {
        this.queuedStatus = queuedStatus;
    }

    public PrearcUtils.PrearcStatus getQueuedStatus() {
        return queuedStatus;
    }
}
