/*
 * web: org.nrg.xnat.event.listeners.methods.AbstractScheduledXnatPreferenceHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.task.AbstractXnatRunnable;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static lombok.AccessLevel.PRIVATE;

/**
 * This abstract class provides the base functionality for site configuration preference handler methods that handle preference values used
 * in scheduled tasks. The scheduler is used to schedule the task and trigger passed to the {@link #scheduleTask()} method.
 */
@Slf4j
@Getter(PRIVATE)
@Setter(PRIVATE)
@Accessors(prefix = "_")
public abstract class AbstractScheduledXnatPreferenceHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    /**
     * Creates an instance of the method with the specified scheduler. If you need to pass a user provider instance, you can use the
     * {@link #AbstractScheduledXnatPreferenceHandlerMethod(ThreadPoolTaskScheduler, XnatUserProvider, String...)} constructor
     * instead.
     *
     * @param scheduler          The task scheduler.
     * @param handledPreferences The preferences this method can handle.
     */
    protected AbstractScheduledXnatPreferenceHandlerMethod(final ThreadPoolTaskScheduler scheduler, final String... handledPreferences) {
        this(scheduler, null, handledPreferences);
    }

    /**
     * Creates an instance of the method with the specified scheduler. If you don't need to pass a user provider instance, you can use the
     * {@link #AbstractScheduledXnatPreferenceHandlerMethod(ThreadPoolTaskScheduler, String...)} constructor instead.
     *
     * @param scheduler          The task scheduler.
     * @param userProvider       The user provider for the primary admin user.
     * @param handledPreferences The preferences this method can handle.
     */
    protected AbstractScheduledXnatPreferenceHandlerMethod(final ThreadPoolTaskScheduler scheduler, final XnatUserProvider userProvider, final String... handledPreferences) {
        super(userProvider, handledPreferences);
        _scheduler = scheduler;
    }

    /**
     * Gets the task for the {@link #scheduleTask()} method.
     *
     * @return A runnable implementation with the handler method's implementation.
     */
    protected abstract AbstractXnatRunnable getTask();

    /**
     * Gets the trigger for the {@link #scheduleTask()} method.
     *
     * @return A trigger setting the schedule for the scheduled task.
     */
    protected abstract Trigger getTrigger();

    /**
     * Passes handling of the preference map to the {@link AbstractXnatPreferenceHandlerMethod#handlePreferences(Map) superclass method},
     * then calls {@link #scheduleTask()} to inject the scheduled job into the scheduler along with any modified properties.
     *
     * @param values The preference values that were modified, mapped by preference name.
     */
    @Override
    public void handlePreferences(final Map<String, String> values) {
        super.handlePreferences(values);
        scheduleTask();
    }

    /**
     * Passes handling of the preference map to the {@link AbstractXnatPreferenceHandlerMethod#handlePreference(String, String) superclass method},
     * then calls {@link #scheduleTask()} to inject the scheduled job into the scheduler along with any modified properties.
     *
     * @param preference The name of the preference that was modified.
     * @param value      The value of the preference that was modified.
     */
    @Override
    public void handlePreference(final String preference, final String value) {
        super.handlePreference(preference, value);
        scheduleTask();
    }

    /**
     * Starts the scheduled task configured from the handler method and returns true, unless the task has already been started, in which case it
     * won't be started again and returns false.
     *
     * @return Returns true if the task was started or false if the task was already started.
     */
    public boolean start() {
        if (_scheduledFuture != null) {
            return false;
        }
        scheduleTask();
        return true;
    }

    /**
     * Schedules the {@link #getTask() task} using the specified {@link #getTrigger() trigger}.
     *
     * @return The future object wrapping the task.
     */
    private ScheduledFuture<?> scheduleTask() {
        final Runnable task    = getTask();
        final Trigger  trigger = getTrigger();

        log.info("Scheduling scheduled handler method task with runnable of type {}", task.getClass().getName());

        if (getScheduledFuture() != null) {
            final ScheduledFuture future = getScheduledFuture();
            log.debug("Found an existing future in handler method {}, cancelling", getHandlerName());
            future.cancel(false);
        }

        final ScheduledFuture<?> future = getScheduler().schedule(task, trigger);
        setScheduledFuture(future);
        if (log.isDebugEnabled()) {
            log.debug("Created new future in handler method {} with parameter values:", getHandledPreferences());
            for (final String preference : getPreferences()) {
                log.debug(" **** Preference: {}", preference);
                final Method method = getPreferenceMethods().get(preference);
                try {
                    log.debug(" **** Value:      {}", method != null ? method.invoke(this) : "No getter found for preference");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("An error occurred trying to access the get method for preference {} on the {} handler method", preference, getHandlerName());
                }
            }
        } else {
            log.info("Created new future in handler method {}", getHandlerName());
        }
        return future;
    }

    private final ThreadPoolTaskScheduler _scheduler;

    private ScheduledFuture _scheduledFuture;
}
