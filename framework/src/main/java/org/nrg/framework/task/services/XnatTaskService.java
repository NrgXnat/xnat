/*
 * framework: org.nrg.framework.task.services.XnatTaskService
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.task.services;

import java.util.List;

import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.framework.task.XnatTaskI;

/**
 * The Interface XnatTaskService.
 */
public interface XnatTaskService {
	
	/**
	 * Should this process run the task?.
	 *
	 * @param clazz the clazz
	 * @return true, if successful
	 */
	boolean shouldRunTask(Class<?> clazz);

	/**
	 * Notifies the task service that the submitted task has started.
	 *
	 * @param task The task that has started.
	 */
	void start(XnatTaskI task);

	/**
	 * Updates the status of the specified task.
	 *
	 * @param task       The task to update.
	 * @param message    A message containing the update status.
	 * @param parameters Parameters for the message.
	 */
	void update(XnatTaskI task, String message, Object... parameters);

	/**
	 * Notifies the task service that the submitted task has finished.
	 *
	 * @param task The task that has finished.
	 */
	void finish(XnatTaskI task);

	/**
	 * Record task run information.
	 *
	 * @param clazz the clazz
	 */
	void recordTaskRun(Class<?> clazz);
	
	/**
	 * Gets the resolver for task.
	 *
	 * @param clazz the clazz
	 * @return the resolver for task
	 */
	XnatTaskExecutionResolverI getResolverForTask(Class<?> clazz);
	
	/**
	 * Gets the configuration elements yaml.
	 *
	 * @param clazz the clazz
	 * @return the configuration elements yaml
	 */
	List<String> getConfigurationElementsYaml(Class<?> clazz);
}
