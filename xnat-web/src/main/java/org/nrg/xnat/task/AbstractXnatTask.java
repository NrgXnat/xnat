package org.nrg.xnat.task;

import org.nrg.framework.task.XnatTaskI;
import org.nrg.framework.task.services.XnatTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractXnatTask.
 */
public abstract class AbstractXnatTask implements XnatTaskI {
    
    /** The task service. */
    private final XnatTaskService _taskService;
    
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(AbstractXnatTask.class);
    
	/**
	 * Instantiates a new abstract xnat task.
	 *
	 * @param _taskService the _task service
	 */
	public AbstractXnatTask(XnatTaskService _taskService) {
		super();
		this._taskService = _taskService;
	}

	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskI#shouldRunTask()
	 */
	@Override
	public boolean shouldRunTask() {
        if (_taskService == null) {
        	logger.warn("XnatTaskService not initialized.  Could not run task.");
            return false;
        }    
        final boolean shouldRun = _taskService.shouldRunTask(this.getClass());
        if (shouldRun) {
            this.recordTaskRun();
        }
        return shouldRun;
	}

	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskI#recordTaskRun()
	 */
	@Override
	public void recordTaskRun() {
        if (_taskService == null) {
        	logger.warn("XnatTaskService not initialized.  Could not record task run.");
            return;
        }    
        _taskService.recordTaskRun(this.getClass());
	}
	
}
