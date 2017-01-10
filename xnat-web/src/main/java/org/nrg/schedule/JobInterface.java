/*
 * web: org.nrg.schedule.JobInterface
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.schedule;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public interface JobInterface {
	/**
	 * Initializes the job bean. All parameters must be passed in here on bean creation.
	 * @param jobExecutionContext The execution context.
	 * @throws JobExecutionException When something goes wrong with job execution.
	 */
	public void init(JobExecutionContext jobExecutionContext) throws JobExecutionException;
	/**
	 * Executes the job bean.
	 * @throws JobExecutionException When something goes wrong with job execution.
	 */
	public void execute() throws JobExecutionException;
	/**
	 * Destroys the job bean. This can be used for task clean-up, etc.
	 */
	public void destroy();
}
