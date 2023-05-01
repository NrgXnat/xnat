package org.nrg.xnat.initialization.tasks;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnat.entities.ConditionalInitializationTaskStatus;
import org.nrg.xnat.services.system.ConditionalInitializationTaskStatusService;

@Slf4j
public abstract class AbstractConditionalInitializingTask extends AbstractInitializingTask {

    protected AbstractConditionalInitializingTask(final ConditionalInitializationTaskStatusService conditionalInitializationTaskStatusService) {
        super();
        this.conditionalInitializationTaskStatusService = conditionalInitializationTaskStatusService;
    }


    @Override
    public boolean isCompleted() {
        ConditionalInitializationTaskStatus conditionalInitializationTaskStatus = conditionalInitializationTaskStatusService.getTaskInitializationStatus(getTaskName());
        if (conditionalInitializationTaskStatus != null) {
            log.info(conditionalInitializationTaskStatus.getTaskName() + " conditional initialization  status = " + conditionalInitializationTaskStatus.getStatus());
            return conditionalInitializationTaskStatus.getStatus().equals(TASK_COMPLETE);
        }
        log.info("Initializing  " + getTaskName());
        return false;
    }

    @Override
    public void complete() {
        super.complete();
        ConditionalInitializationTaskStatus conditionalInitializationTaskStatus = new ConditionalInitializationTaskStatus();
        conditionalInitializationTaskStatus.setStatus(TASK_COMPLETE);
        conditionalInitializationTaskStatus.setTaskName(getTaskName());
        conditionalInitializationTaskStatusService.create(conditionalInitializationTaskStatus);
        log.info(getTaskName() + " initialization complete.");
    }

    private final ConditionalInitializationTaskStatusService conditionalInitializationTaskStatusService;

    public static final String TASK_COMPLETE = "TASK_COMPLETE";


}
