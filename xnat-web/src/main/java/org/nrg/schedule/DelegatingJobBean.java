/*
 * org.nrg.schedule.DelegatingJobBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.schedule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class DelegatingJobBean extends QuartzJobBean {
	@Override
	protected final void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		SchedulerContext schedulerContext = null;
		try {
			schedulerContext = jobExecutionContext.getScheduler().getContext();
		} catch (SchedulerException e) {
			throw new JobExecutionException("Failure accessing scheduler context", e);
		}

		String contextKey = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(APPLICATION_CONTEXT_KEY_NAME);
		if (contextKey != null && !contextKey.isEmpty()) {
			_contextKey = contextKey;
		}

		ApplicationContext appContext = (ApplicationContext) schedulerContext.get(_contextKey);

		if (appContext == null) {
			throw new JobExecutionException("No application context found. Verify that the applicationContextSchedulerContextKey is set in the scheduler properties. This should be set to either applicationContext (the default) or to the value configured with the " + APPLICATION_CONTEXT_KEY_NAME + " setting.");
		}

		String jobBeanName = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(JOB_BEAN_NAME_KEY);
		if (jobBeanName == null || jobBeanName.isEmpty()) {
			throw new JobExecutionException("You must specify the " + JOB_BEAN_NAME_KEY + " setting to identify the job to be executed.");
		}

		if (_log.isInfoEnabled()) {
			_log.info("Starting job: " + jobBeanName);
		}

		JobInterface jobBean = (JobInterface) appContext.getBean(jobBeanName);
		try {
			jobBean.init(jobExecutionContext);
			jobBean.execute();
		} finally {
			jobBean.destroy();
		}
	}

	private static final String APPLICATION_CONTEXT_KEY_NAME = "job.context.name";
	private static final String JOB_BEAN_NAME_KEY = "job.bean.name";

	private static Log _log = LogFactory.getLog(DelegatingJobBean.class);
	private String _contextKey = "applicationContext";
}