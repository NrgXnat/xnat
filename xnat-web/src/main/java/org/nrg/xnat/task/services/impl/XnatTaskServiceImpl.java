/*
 * web: org.nrg.xnat.task.services.impl.XnatTaskServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.services.impl;

import java.util.Arrays;
import java.util.List;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.XnatTaskExecutionResolver;
import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.prefs.entities.Preference;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.task.services.XnatTaskInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.common.collect.Lists;

/**
 * The Class XnatTaskServiceImpl.
 */
@Service
public class XnatTaskServiceImpl implements XnatTaskService {
	
	/**
	 * Instantiates a new xnat task service impl.
	 *
	 * @param xnatNode the xnat node
	 * @param preferences the preferences
	 * @param taskInfoService the task info service
	 * @param executionResolvers the execution resolvers
	 */
	@Autowired
	public XnatTaskServiceImpl(final XnatNode xnatNode,final  SiteConfigPreferences preferences,final  XnatTaskInfoService taskInfoService,final  XnatTaskExecutionResolverI[] executionResolvers) {
		_xnatNode = xnatNode;
		_preferences = preferences;
		_taskInfoService = taskInfoService;
		_executionResolvers = executionResolvers;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.task.services.XnatTaskService#runTaskOnCurrentNode()
	 */
	@Override
	public boolean shouldRunTask(final Class<?> clazz) {
		final XnatTaskExecutionResolverI resolver = getResolverForTask(clazz);
		if (resolver != null) {
			final XnatTask taskAnno = clazz.getAnnotation(XnatTask.class);
			if (taskAnno != null) {
				return resolver.shouldRunTask(taskAnno.taskId());
			}
		}
		// Default to run on node when not (correctly) configured.
		_log.warn("The shouldRunTask method for the task (CLASS=?) was not able to match any configuration.  The task will be run.", clazz.getName());
		return true;
	}


	/* (non-Javadoc)
	 * @see org.nrg.framework.task.services.XnatTaskService#recordTaskRun(java.lang.Class)
	 */
	@Override
	public void recordTaskRun(final Class<?> clazz) {
		final XnatTask taskAnno = clazz.getAnnotation(XnatTask.class);
		if (taskAnno != null) {
			final String taskId = taskAnno.taskId();
			_taskInfoService.recordTaskRun(_xnatNode, taskId);
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xnat.task.services.XnatTaskService#runTaskOnCurrentNode()
	 */
	@Override
	public XnatTaskExecutionResolverI getResolverForTask(final Class<?> clazz) {
		final XnatTask taskAnno = clazz.getAnnotation(XnatTask.class);
		if (taskAnno != null) {
			final String taskId = taskAnno.taskId();
			final String resolverPreferenceValue = getResolverPreferenceValue(taskId);
			final List<String> allowedResolvers = Arrays.asList(taskAnno.allowedExecutionResolvers());
			for (final XnatTaskExecutionResolverI resolver : Arrays.asList(_executionResolvers)) {
				final XnatTaskExecutionResolver resolverAnno =  resolver.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (resolverAnno == null) {
					continue;
				}
				final String resolverId = resolverAnno.resolverId();
				if (!taskAnno.executionResolverConfigurable() && taskAnno.defaultExecutionResolver().equals(resolverAnno.resolverId())) {
					return resolver;
				} else if (taskAnno.executionResolverConfigurable() && resolverPreferenceValue == null &&
						taskAnno.defaultExecutionResolver().equals(resolverAnno.resolverId())) {
					return resolver;
				} else if (taskAnno.executionResolverConfigurable() && resolverId.equals(resolverPreferenceValue) &&
						(allowedResolvers.isEmpty() || allowedResolvers.contains(resolverId))) {
					return resolver;
				}
			}
		}
		_log.warn("Invalid XnatTaskExecutionResolver configuration for task (CLASS=?).");
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.framework.task.services.XnatTaskService#getConfigurationElementsYaml(java.lang.Class)
	 */
	@Override
	public List<String> getConfigurationElementsYaml(final Class<?> clazz) {
		final XnatTask taskAnno = clazz.getAnnotation(XnatTask.class);
		if (taskAnno != null) {
			final String taskId = taskAnno.taskId();
			final String resolverPreferenceValue = getResolverPreferenceValue(taskId);
			final List<String> allowedResolvers = Arrays.asList(taskAnno.allowedExecutionResolvers());
			for (final XnatTaskExecutionResolverI resolver : Arrays.asList(_executionResolvers)) {
				final XnatTaskExecutionResolver resolverAnno = resolver.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (resolverAnno == null) {
					continue;
				}
				final String resolverId = resolverAnno.resolverId();
				if (!taskAnno.executionResolverConfigurable() && taskAnno.defaultExecutionResolver().equals(resolverAnno.resolverId())) {
					return resolver.getConfigurationElementsYaml(taskId);
				} else if (taskAnno.executionResolverConfigurable() && resolverPreferenceValue == null &&
						taskAnno.defaultExecutionResolver().equals(resolverAnno.resolverId())) {
					return getConfigurationYamlWithResolverConfig(taskAnno, _executionResolvers, resolver, resolver.getConfigurationElementsYaml(taskId));
				} else if (taskAnno.executionResolverConfigurable() && resolverId.equals(resolverPreferenceValue) &&
						(allowedResolvers.isEmpty() || allowedResolvers.contains(resolverId))) {
					return getConfigurationYamlWithResolverConfig(taskAnno, _executionResolvers, resolver, resolver.getConfigurationElementsYaml(taskId));
				}
			}
		}
		_log.warn("Invalid XnatTaskExecutionResolver configuration for task (CLASS=?).");
		return null;
	}
	
	/**
	 * Gets the configuration yaml with resolver config.
	 *
	 * @param taskAnno the task anno
	 * @param executionResolvers the execution resolvers
	 * @param resolver the resolver
	 * @param configurationElementsYaml the configuration elements yaml
	 * @return the configuration yaml with resolver config
	 */
	private List<String> getConfigurationYamlWithResolverConfig(final XnatTask taskAnno,
			final XnatTaskExecutionResolverI[] executionResolvers,final XnatTaskExecutionResolverI resolver,
			final List<String> configurationElementsYaml) {
		final List<String> eleList = Lists.newArrayList();
		final StringBuilder sb = new StringBuilder();
		sb.append("resolverSelSubhead:\n");
		sb.append("   kind: panel.subhead\n");
		sb.append("   name: resolverSelSubhead\n");
		sb.append("   label: Resolver Selector\n");
		sb.append("resolverSel:\n");
		sb.append("   kind: panel.select.single\n");
		sb.append("   id: task-" + taskAnno.taskId() + "-resolver\n");
		sb.append("   name: task-" + taskAnno.taskId() + "-resolver\n");
		sb.append("   label: Execution Resolver\n");
		sb.append("   description: Select an execution resolver for use with this task.\n");
		sb.append("   options:\n");
		if (executionResolvers.length>0) {
			for (final XnatTaskExecutionResolverI iResolv : Arrays.asList(executionResolvers)) {
				final XnatTaskExecutionResolver anno = iResolv.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (anno != null) {
					if (taskAnno.allowedExecutionResolvers().length<1 ||
							Arrays.asList(taskAnno.allowedExecutionResolvers()).contains(anno.resolverId())) {
						sb.append("      " + anno.resolverId() + ": " + anno.description()  + "\n");
					}
				}
			}
		}
		sb.append("resolverPropSubhead:\n");
		sb.append("   kind: panel.subhead\n");
		sb.append("   name: resolverPropSubhead\n");
		sb.append("   label: Resolver Properties\n");
		eleList.add(sb.toString());
		eleList.addAll(configurationElementsYaml);
		return eleList;
	}
	
	/**
	 * Gets the resolver preference value.
	 *
	 * @param taskId the task id
	 * @return the resolver preference value
	 */
	private String getResolverPreferenceValue(final String taskId) {
		final String prefKey = "task-" + taskId + "-resolver";
		if (_preferences.getPreferenceKeys().contains(prefKey)) {
			final Preference resolverPref = _preferences.get(prefKey);
			return resolverPref.getValue();
		}
		return null;
	}

	/** The _xnat node. */
	private final XnatNode _xnatNode;
	
	/** The _preferences. */
	private final SiteConfigPreferences _preferences;
	
	/** The _preferences. */
	private final XnatTaskInfoService _taskInfoService;
	
	/** The _preferences. */
	private final XnatTaskExecutionResolverI[] _executionResolvers;
	
	/** The Constant _log. */
	private static final Logger _log = LoggerFactory.getLogger(XnatTaskServiceImpl.class); 

}
