package org.nrg.xnat.initialization.tasks;

import org.nrg.xnat.initialization.InitializingTask;

import java.util.Date;

public abstract class AbstractInitializingTask implements InitializingTask {
    @Override
    public abstract String getTaskName();

    @Override
    public abstract void run();

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
    }

    private Date _completedAt;
}
