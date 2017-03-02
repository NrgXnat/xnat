/*
 * web: org.nrg.xnat.initialization.InitializingTasksExecutor
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import org.nrg.xft.schema.XFTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Component
public class InitializingTasksExecutor {

    @Autowired
    @Lazy
    public InitializingTasksExecutor(final TaskScheduler scheduler, final List<InitializingTask> tasks) {
        _tasks = tasks;
        _scheduler = scheduler;
    }

    @EventListener
    public void executeOnContextRefresh(final ContextRefreshedEvent event) {
        _log.debug("Handling context refreshed event at {}", event.getTimestamp());
        if (_future == null || _future.isCancelled()) {
            _future = _scheduler.scheduleWithFixedDelay(new CheckTasks(), 15000);
        }
    }

    private class CheckTasks implements Runnable {
        @Override
        public void run() {
            if (!XFTManager.isInitialized()) {
                _log.info("XFTManager not yet initialized. Delaying check for initializing tasks.");
                return;
            }
            if (_tasks.size() == 0) {
                _log.info("No initializing tasks found, cancelling future executions of initializing tasks.");
                _future.cancel(false);
            }
            int completedTasks = 0, incompleteTasks = 0;
            for (final InitializingTask task : _tasks) {
                if (!task.isCompleted() && !task.isMaxedOut()) {
                    _log.debug("Beginning execution {} for initializing task \"{}\".", task.executions() + 1, task.getTaskName());
                    try {
                        final boolean completed = task.call();
                        if (completed) {
                            _log.info("Task \"{}\" completed at {}, {} completed tasks found.", task.getTaskName(), task.completedAt(), ++completedTasks);
                        } else {
                            _log.debug("Task \"{}\" not yet completed, {} executions attempted, {} incomplete tasks found.", task.getTaskName(), task.executions(), ++incompleteTasks);
                        }
                    } catch (Exception e) {
                        _log.error("An error occurred while running the task " + task.getTaskName() + ", " + ++incompleteTasks + " incomplete tasks found.", e);
                    }
                } else if (task.isCompleted()) {
                    _log.debug("Found task {}, but it is marked as already completed, {} completed tasks found.", task.getTaskName(), ++completedTasks);
                } else {
                    _log.debug("Found task {}, but it is marked as maxed out: {} total executions completed, {} completed tasks found.", task.getTaskName(), task.executions(), ++completedTasks);
                }
            }
            if (incompleteTasks > 0) {
                _log.debug("There are {} completed initializing tasks, with {} incomplete initializing tasks remaining. Will continue processing initializing tasks at regular intervals.", completedTasks, incompleteTasks);
            } else {
                _log.info("{} initializing tasks completed, no incomplete tasks remaining, terminating further initializing tasks processing.", completedTasks);
                _future.cancel(false);
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(InitializingTasksExecutor.class);

    private final TaskScheduler          _scheduler;
    private final List<InitializingTask> _tasks;

    private ScheduledFuture<?> _future;
}
