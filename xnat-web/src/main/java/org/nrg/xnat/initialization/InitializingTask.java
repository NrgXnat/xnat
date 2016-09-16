package org.nrg.xnat.initialization;

import java.util.Date;
import java.util.concurrent.Callable;

public interface InitializingTask extends Callable<Boolean> {
    String getTaskName();

    boolean isCompleted();

    Date completedAt();

    void complete();

    void reset();

    boolean isMaxedOut();

    int executions();
}
