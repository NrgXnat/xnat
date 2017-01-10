/*
 * web: org.nrg.xnat.initialization.InitializingTask
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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
