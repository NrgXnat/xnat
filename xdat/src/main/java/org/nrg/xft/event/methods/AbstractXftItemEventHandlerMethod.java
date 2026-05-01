package org.nrg.xft.event.methods;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.event.XftItemEventI;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

@Getter
@Accessors(prefix = "_")
@Slf4j
public abstract class AbstractXftItemEventHandlerMethod implements XftItemEventHandlerMethod {
    protected AbstractXftItemEventHandlerMethod(final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(null, first, criteria);
    }

    protected AbstractXftItemEventHandlerMethod(final String name, final XftItemEventCriteria first, final XftItemEventCriteria... criteria) {
        this(name, Arrays.asList(ArrayUtils.add(criteria, first)));
    }

    protected AbstractXftItemEventHandlerMethod(final List<XftItemEventCriteria> criteria) {
        this(null, criteria);
    }

    protected AbstractXftItemEventHandlerMethod(final String name, final List<XftItemEventCriteria> criteria) {
        _criteria = ImmutableList.copyOf(criteria);
        _name = StringUtils.defaultIfBlank(name, getClass().getSimpleName());
    }

    /**
     * Placeholder for implementing classes to handle events that match the method's criteria.
     *
     * @param event The event to be handled.
     *
     * @return Should return true if the event was handled successfully, false otherwise.
     */
    protected abstract boolean handleEventImpl(final XftItemEventI event);

    /**
     * Handles the event for this method.
     *
     * @param event The event to handle.
     *
     * @return A future that returns the final result of the handler.
     */
    @Async
    @Override
    public Future<Boolean> handleEvent(final XftItemEventI event) {
        final boolean handled = handleEventImpl(event);
        if (handled) {
            log.debug("The {} method handled an XFT item event: {}", getName(), event);
        } else {
            log.warn("The {} method failed to handle an XFT item event: {}. Look for logging info in the appropriate place.", getName(), event);
        }
        return new AsyncResult<>(handled);
    }

    /**
     * Tests the event to see if it matches the criteria for this handler method. This default implementation
     * tests against a list of {@link XftItemEventCriteria} objects, but this can be overridden as needed.
     *
     * @param event The event to test.
     *
     * @return Returns true if this method can handle the event, false otherwise.
     */
    @Override
    public boolean matches(final XftItemEventI event) {
        final Optional<XftItemEventCriteria> match = getCriteria().stream().filter(criteria -> criteria.matches(event)).findAny();
        if (!match.isPresent()) {
            log.info("No current XFT item event criteria matches event {}", event);
            return false;
        }
        log.info("XFT item event criteria {} matches event {}", match.get(), event);
        return true;
    }

    @Override
    public String toString() {
        return getName() + ": " + StringUtils.join(_criteria, ", ");
    }

    private final List<XftItemEventCriteria> _criteria;
    private final String                     _name;
}
