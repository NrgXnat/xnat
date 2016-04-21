package org.nrg.schedule;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.concurrent.TimeUnit;

public class TriggerTaskProxy extends TriggerTask {
    public TriggerTaskProxy(final Runnable runnable, final long period) {
        this(runnable, period, null);
    }

    public TriggerTaskProxy(final Runnable runnable, final long period, final TimeUnit timeUnit) {
        this(runnable, new PeriodicTrigger(period, timeUnit));
    }

    public TriggerTaskProxy(final Runnable runnable, final long period, final int initialDelay) {
        this(runnable, period, null, initialDelay);
    }

    public TriggerTaskProxy(final Runnable runnable, final long period, final TimeUnit timeUnit, final int initialDelay) {
        this(runnable, new PeriodicTrigger(period, timeUnit) {{
            setInitialDelay(initialDelay);
        }});
    }

    public TriggerTaskProxy(final Runnable runnable, final String expression) {
        this(runnable, new CronTrigger(expression));
    }

    public TriggerTaskProxy(final Runnable runnable, final Trigger trigger) {
        super(runnable, trigger);
    }
}
