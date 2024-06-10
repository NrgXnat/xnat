/*
 * web: org.nrg.xnat.configuration.SchedulerConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.event.listeners.methods.AbstractScheduledXnatPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class SchedulerConfig implements SchedulingConfigurer {
    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setErrorHandler(new ErrorHandler() {
            @Override
            public void handleError(final Throwable throwable) {
                log.error("Something happened while handling a scheduled task", throwable);
            }
        });

        log.debug("Setting scheduler thread pool size to {}", _threadPoolSize);
        scheduler.setPoolSize(_threadPoolSize);
        if (_waitForTerminationSeconds > 0) {
            log.debug("Found non-zero value for scheduling.wait.for.termination.seconds, setting wait for tasks to complete on shutdown to true and await termination seconds to {}", _waitForTerminationSeconds);
            scheduler.setWaitForTasksToCompleteOnShutdown(true);
            scheduler.setAwaitTerminationSeconds(_waitForTerminationSeconds);
        } else {
            log.debug("Found zero value for scheduling.wait.for.termination.seconds, setting wait for tasks to complete on shutdown to false, no setting for await termination seconds");
            scheduler.setWaitForTasksToCompleteOnShutdown(false);
        }
        return scheduler;
    }

    @Autowired
    public void setTriggerTasks(final List<? extends TriggerTask> tasks) {
        _tasks.addAll(tasks);
    }

    @Autowired
    public void setScheduledXnatPreferenceHandlerMethods(final List<? extends AbstractScheduledXnatPreferenceHandlerMethod> handlerMethods) {
        _handlerMethods.addAll(handlerMethods);
    }

    @Override
    public void configureTasks(final ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());

        log.debug("Found {} trigger tasks, scheduling now", _tasks.size());
        for (final TriggerTask triggerTask : _tasks) {
            log.info("Scheduling stand-alone trigger task with runnable of type {}", triggerTask.getRunnable().getClass().getName());
            taskRegistrar.addTriggerTask(triggerTask);
        }

        log.debug("Found {} scheduled handler methods, scheduling now", _handlerMethods.size());
        for (final AbstractScheduledXnatPreferenceHandlerMethod method : _handlerMethods) {
            try {
                final boolean started = method.start();
                if (started) {
                    log.info("Started instance of {}", method.getClass().getName());
                } else {
                    log.warn("Didn't start instance of {}. Check for relevant errors in XNAT logs.", method.getClass().getName());
                }
            } catch (Throwable e) {
                log.error("An error occurred trying to start the preference handler method " + method.getClass().getName(), e);
            }
        }
    }

    private final List<AbstractScheduledXnatPreferenceHandlerMethod> _handlerMethods = new ArrayList<>();
    private final List<TriggerTask>                                  _tasks          = new ArrayList<>();

    @Value("${scheduling.thread.pool.size:4}")
    private int _threadPoolSize;

    @Value("${scheduling.wait.for.termination.seconds:0}")
    private int _waitForTerminationSeconds;
}
