package org.nrg.xnat.initialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitializingTasksExecutor {
    @EventListener
    public void executeOnContextRefresh(final ContextRefreshedEvent event) {
        if (_log.isDebugEnabled()) {
            _log.debug("Handling context refreshed event at " + event.getTimestamp());
        }
        for (final InitializingTask task : _tasks) {
            if (!task.isCompleted()) {
                if (_log.isInfoEnabled()) {
                    _log.info("Executing type: " + task.getTaskName());
                }
                task.run();
                if (_log.isInfoEnabled()) {
                    _log.info("Task \"" + task.getTaskName() + "\" " + (task.isCompleted() ? "completed at " + task.completedAt() : "did not complete."));
                }
            }
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(InitializingTasksExecutor.class);

    @Autowired
    @Lazy
    private List<InitializingTask> _tasks;
}
