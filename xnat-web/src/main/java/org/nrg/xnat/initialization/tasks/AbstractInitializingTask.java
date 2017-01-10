/*
 * web: org.nrg.xnat.initialization.tasks.AbstractInitializingTask
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.nrg.xnat.initialization.InitializingTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class AbstractInitializingTask implements InitializingTask {
    protected AbstractInitializingTask() {
        this(0);
    }

    protected AbstractInitializingTask(final int maxExecutions) {
        _maxExecutions = maxExecutions;
    }

    @Override
    public Boolean call() {
        try {
            _executions++;
            callImpl();
            complete();
            return true;
        } catch (InitializingTaskException e) {
            switch (e.getLevel()) {
                case RequiresInitialization:
                    manageSingleNotice("The system is not yet fully initialized. Delaying execution of the intializing task \"" + getTaskName() + "\" until initialization is completed.");
                    break;
                case SingleNotice:
                    manageSingleNotice(e.getMessage());
                    break;
                case Info:
                    _log.info(e.getMessage());
                    break;
                case Warn:
                    _log.warn(e.getMessage());
                    break;
                case Error:
                    if (e.getCause() == null) {
                        _log.error(e.getMessage(), e);
                    } else {
                        _log.error(e.getMessage(), e.getCause());
                    }
            }
            return false;
        }
    }

    @Override
    public abstract String getTaskName();

    @Override
    public boolean isCompleted() {
        return _completedAt != null;
    }

    @Override
    public Date completedAt() {
        return _completedAt;
    }

    @Override
    public void complete() {
        _completedAt = new Date();
    }

    @Override
    public void reset() {
        _completedAt = null;
        _executions = 0;
    }

    @Override
    public boolean isMaxedOut() {
        return _maxExecutions > 0 && _executions < _maxExecutions;
    }

    @Override
    public int executions() {
        return _executions;
    }

    protected abstract void callImpl() throws InitializingTaskException;

    private void manageSingleNotice(final String message) {
        if (_executions == 1) {
            _log.info(message);
        } else {
            _log.debug(message);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(AbstractInitializingTask.class);

    private final int _maxExecutions;

    private Date _completedAt;
    private int _executions;
}
