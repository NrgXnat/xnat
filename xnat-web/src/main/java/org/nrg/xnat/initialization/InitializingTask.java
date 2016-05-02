package org.nrg.xnat.initialization;

import java.util.Date;

public interface InitializingTask extends Runnable {
    String getTaskName();

    boolean isCompleted();

    Date completedAt();

    void complete();

    void reset();
}
