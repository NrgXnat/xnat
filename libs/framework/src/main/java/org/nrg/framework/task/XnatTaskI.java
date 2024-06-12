/*
 * framework: org.nrg.framework.task.XnatTaskI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.task;

/**
 * The Interface XnatTaskI.
 */
public interface XnatTaskI extends Runnable {
	/**
	 * Should run task.
	 *
	 * @return true, if successful
	 */
	boolean shouldRunTask();
	
	/**
	 * Record task run.
	 */
	void recordTaskRun();
}
