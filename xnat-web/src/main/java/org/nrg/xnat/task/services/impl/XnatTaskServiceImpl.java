/*
 * web: org.nrg.xnat.task.services.impl.XnatTaskServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.services.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTask;
import org.nrg.framework.task.XnatTaskExecutionResolver;
import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.framework.task.services.XnatTaskService;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.task.services.XnatTaskInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * The Class XnatTaskServiceImpl.
 */
@Service
@Slf4j
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldRunTask(final Class<?> clazz) {
		final XnatTaskExecutionResolverI resolver = getResolverForTask(clazz);
		if (resolver != null) {
			final XnatTask annotation = clazz.getAnnotation(XnatTask.class);
			if (annotation != null) {
				return resolver.shouldRunTask(annotation.taskId());
			}
		} else {
			final Class<?> sClazz = clazz.getSuperclass();
			if (sClazz!=null) {
				return shouldRunTask(sClazz);
			}
		}
		// Default to run on node when not (correctly) configured.
		log.warn("The shouldRunTask method for the task (CLASS=?) was not able to match any configuration.  The task will be run.", clazz.getName());
		return true;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void recordTaskRun(final Class<?> clazz) {
		final XnatTask annotation = clazz.getAnnotation(XnatTask.class);
		if (annotation != null) {
			final String taskId = annotation.taskId();
			_taskInfoService.recordTaskRun(_xnatNode, taskId);
		} else {
			final Class<?> sClazz = clazz.getSuperclass();
			if (sClazz != null ) {
				recordTaskRun(sClazz);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XnatTaskExecutionResolverI getResolverForTask(final Class<?> clazz) {
		final XnatTask taskAnnotation = clazz.getAnnotation(XnatTask.class);
		if (taskAnnotation != null) {
			final String taskId = taskAnnotation.taskId();
			final String resolverPreferenceValue = getResolverPreferenceValue(taskId);
			final List<String> allowedResolvers = Arrays.asList(taskAnnotation.allowedExecutionResolvers());
			for (final XnatTaskExecutionResolverI resolver : _executionResolvers) {
				final XnatTaskExecutionResolver resolverAnnotation =  resolver.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (resolverAnnotation == null) {
					continue;
				}
				final String resolverId = resolverAnnotation.resolverId();
				if (!taskAnnotation.executionResolverConfigurable() && taskAnnotation.defaultExecutionResolver().equals(resolverAnnotation.resolverId())) {
					return resolver;
				} else if (taskAnnotation.executionResolverConfigurable() && resolverPreferenceValue == null &&
						taskAnnotation.defaultExecutionResolver().equals(resolverAnnotation.resolverId())) {
					return resolver;
				} else if (taskAnnotation.executionResolverConfigurable() && resolverId.equals(resolverPreferenceValue) &&
						(allowedResolvers.isEmpty() || allowedResolvers.contains(resolverId))) {
					return resolver;
				}
			}
		}
		log.warn("Invalid XnatTaskExecutionResolver configuration for task (CLASS=?).");
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getConfigurationElementsYaml(final Class<?> clazz) {
		final XnatTask taskAnnotation = clazz.getAnnotation(XnatTask.class);
		if (taskAnnotation != null) {
			final String taskId = taskAnnotation.taskId();
			final String resolverPreferenceValue = getResolverPreferenceValue(taskId);
			final List<String> allowedResolvers = Arrays.asList(taskAnnotation.allowedExecutionResolvers());
			for (final XnatTaskExecutionResolverI resolver : _executionResolvers) {
				final XnatTaskExecutionResolver resolverAnnotation = resolver.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (resolverAnnotation == null) {
					continue;
				}
				final String resolverId = resolverAnnotation.resolverId();
				if (!taskAnnotation.executionResolverConfigurable() && taskAnnotation.defaultExecutionResolver().equals(resolverAnnotation.resolverId())) {
					return resolver.getConfigurationElementsYaml(taskId);
				} else if (taskAnnotation.executionResolverConfigurable() && resolverPreferenceValue == null &&
						taskAnnotation.defaultExecutionResolver().equals(resolverAnnotation.resolverId())) {
					return getConfigurationYamlWithResolverConfig(taskAnnotation, _executionResolvers, resolver.getConfigurationElementsYaml(taskId));
				} else if (taskAnnotation.executionResolverConfigurable() && resolverId.equals(resolverPreferenceValue) &&
						(allowedResolvers.isEmpty() || allowedResolvers.contains(resolverId))) {
					return getConfigurationYamlWithResolverConfig(taskAnnotation, _executionResolvers, resolver.getConfigurationElementsYaml(taskId));
				}
			}
		}
		log.warn("Invalid XnatTaskExecutionResolver configuration for task (CLASS=?).");
		return null;
	}

	/**
	 * Gets the configuration yaml with resolver config.
	 *
	 * @param taskAnnotation the task annotation
	 * @param executionResolvers the execution resolvers
	 * @param configurationElementsYaml the configuration elements yaml
	 * @return the configuration yaml with resolver config
	 */
	private List<String> getConfigurationYamlWithResolverConfig(final XnatTask taskAnnotation,
																final XnatTaskExecutionResolverI[] executionResolvers,
																final List<String> configurationElementsYaml) {
		final List<String> eleList = Lists.newArrayList();
		final StringBuilder sb = new StringBuilder();
		sb.append("resolverSelSubhead:\n");
		sb.append("   kind: panel.subhead\n");
		sb.append("   name: resolverSelSubhead\n");
		sb.append("   label: Resolver Selector\n");
		sb.append("resolverSel:\n");
		sb.append("   kind: panel.select.single\n");
		sb.append("   id: task-").append(taskAnnotation.taskId()).append("-resolver\n");
		sb.append("   name: task-").append(taskAnnotation.taskId()).append("-resolver\n");
		sb.append("   label: Execution Resolver\n");
		sb.append("   description: Select an execution resolver for use with this task.\n");
		sb.append("   options:\n");
		if (executionResolvers.length>0) {
			for (final XnatTaskExecutionResolverI iResolv : executionResolvers) {
				final XnatTaskExecutionResolver resolverAnnotation = iResolv.getClass().getAnnotation(XnatTaskExecutionResolver.class);
				if (resolverAnnotation != null) {
					if (taskAnnotation.allowedExecutionResolvers().length<1 ||
							Arrays.asList(taskAnnotation.allowedExecutionResolvers()).contains(resolverAnnotation.resolverId())) {
						sb.append("      ").append(resolverAnnotation.resolverId()).append(": ").append(resolverAnnotation.description()).append("\n");
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
		return _preferences.containsKey(prefKey) ? _preferences.getPreference(prefKey).getValue() : null;
	}

	private final XnatNode                     _xnatNode;
	private final SiteConfigPreferences        _preferences;
	private final XnatTaskInfoService          _taskInfoService;
	private final XnatTaskExecutionResolverI[] _executionResolvers;
}
