/*
 * framework: org.nrg.framework.task.XnatTaskExecutionResolverI
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.task;

import java.util.List;

/**
 * The Interface XnatTaskExecutionResolverI.
 */
public interface XnatTaskExecutionResolverI {
	
	/**
	 * Should run task.
	 *
	 * @param taskId the task id
	 * @return true, if successful
	 */
	public boolean shouldRunTask(String taskId);

	/**
	 * Gets the configuration elements yaml.
	 *
	 * @param taskId the task id
	 * @return the configuration elements yaml
	 */
	List<String> getConfigurationElementsYaml(String taskId);
	
}
