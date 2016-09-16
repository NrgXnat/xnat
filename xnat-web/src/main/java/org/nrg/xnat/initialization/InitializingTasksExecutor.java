package org.nrg.xnat.initialization;

import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        if (_log.isDebugEnabled()) {
            _log.debug("Handling context refreshed event at " + event.getTimestamp());
        }
        if (_future == null || _future.isCancelled()) {
            _future = _scheduler.scheduleWithFixedDelay(new CheckTasks(), 15000);
        }
    }

    private class CheckTasks implements Runnable {
        @Override
        public void run() {
            final Map<String, Boolean> results = new HashMap<>();
            for (final InitializingTask task : _tasks) {
                if (!task.isCompleted() && !task.isMaxedOut()) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Beginning execution {} for initializing task \"{}\".", task.executions() + 1, task.getTaskName());
                    }
                    try {
                        final boolean completed = task.call();
                        if (_log.isInfoEnabled()) {
                            _log.info("Task \"{}\" {}", task.getTaskName(), completed ? "completed at " + task.completedAt() : "did not complete.");
                        }
                        results.put(task.getTaskName(), completed);
                    } catch (Exception e) {
                        _log.error("An error occurred while running the task " + task.getTaskName(), e);
                        results.put(task.getTaskName(), false);
                    }
                } else if (_log.isDebugEnabled()){
                    if (task.isCompleted()) {
                        _log.debug("Found task {}, but it is marked as completed.", task.getTaskName());
                    } else {
                        _log.debug("Found task {}, but it is marked as maxed out: {} total executions completed.", task.getTaskName(), task.executions());
                    }
                }
            }
            final Set<Boolean> distinct = Sets.newHashSet(results.values());
            switch (distinct.size()) {
                case 2:
                    if (_log.isDebugEnabled()) {
                        _log.debug("There are {} incomplete initializing tasks. Will continue processing initializing tasks at regular intervals.");
                    }
                    break;
                case 1:
                    if (distinct.contains(true)) {
                        if (_log.isInfoEnabled()) {
                            _log.info("All initializing tasks completed. Cancelling further initializing tasks processing.");
                        }
                        _future.cancel(false);
                    } else if (_log.isDebugEnabled()) {
                        _log.debug("There are {} incomplete initializing tasks. Will continue processing initializing tasks at regular intervals.");
                    }
                    break;
                case 0:
                    if (_log.isInfoEnabled()) {
                        _log.info("No incomplete or non-maxed-out initializing tasks found, cancelling future executions of initializing tasks.");
                    }
                    _future.cancel(false);
                    break;
                default:
                    throw new RuntimeException("Somehow there are more than 2 or fewer than 0 values in a boolean set: " + distinct.size() + "\n" + distinct.toString());
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(InitializingTasksExecutor.class);

    private final TaskScheduler          _scheduler;
    private final List<InitializingTask> _tasks;

    private ScheduledFuture<?> _future;
}
