package org.nrg.xnat.event.archive;

import org.apache.commons.lang.ObjectUtils;
import org.nrg.framework.status.StatusListenerI;
import org.nrg.framework.status.StatusMessage;
import org.nrg.framework.status.StatusProducer;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.archive.Operation;

import java.util.Random;

import static org.nrg.xdat.XDAT.getEventService;

public class ArchiveStatusProducer extends StatusProducer {
    public ArchiveStatusProducer(Object control) {
        super(control);
    }

    public ArchiveStatusProducer(Object control, UserI user) {
        super(ObjectUtils.defaultIfNull(control, makeControl(user)));
    }

    public ArchiveStatusProducer(Object control, StatusListenerI... listeners) {
        super(control, listeners);
    }

    public ArchiveStatusProducer(Object control, Iterable<StatusListenerI> listeners) {
        super(control, listeners);
    }

    @Override
    public void publish(final StatusMessage m) {
        super.publish(m);
        String key = getControlString();
        if (key == null || StatusMessage.DO_NOT_TRACK.equals(key)) {
            return;
        }
        if (m.isTerminal()) {
            switch (m.getStatus()) {
                case COMPLETED:
                    getEventService().triggerEvent(ArchiveEvent.completed(Operation.Import, "Unk",
                            key, m.getMessage()));
                    break;
                case FAILED:
                default:
                    getEventService().triggerEvent(ArchiveEvent.failed(Operation.Import, "Unk",
                            key, m.getMessage()));
                    break;
            }
        } else {
            switch (m.getStatus()) {
                case COMPLETED:
                case PROCESSING:
                    getEventService().triggerEvent(ArchiveEvent.progress(Operation.Import, 0, "Unk",
                            key, m.getMessage()));
                    break;
                case FAILED:
                case WARNING:
                default:
                    getEventService().triggerEvent(ArchiveEvent.warn(Operation.Import, "Unk",
                            key, m.getMessage()));
                    break;
            }
        }
    }

    private static String makeControl(UserI user) {
        return user.getUsername() + new Random().nextInt(1000) + System.currentTimeMillis();
    }
}
