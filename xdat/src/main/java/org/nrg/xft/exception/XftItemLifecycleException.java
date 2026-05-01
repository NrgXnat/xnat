package org.nrg.xft.exception;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.xft.event.XftItemLifecyclePhase;

@Getter
@Accessors(prefix = "_")
public class XftItemLifecycleException extends XftItemException {
    public XftItemLifecycleException(final XftItemLifecyclePhase phase, final String message) {
        super(message);
        _phase = phase;
    }

    public XftItemLifecycleException(final XftItemLifecyclePhase phase, final String message, final Throwable cause) {
        super(message, cause);
        _phase = phase;
    }

    private final XftItemLifecyclePhase _phase;
}
