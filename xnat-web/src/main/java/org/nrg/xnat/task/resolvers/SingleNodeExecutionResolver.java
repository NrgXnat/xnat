/*
 * web: org.nrg.xnat.task.resolvers.SingleNodeExecutionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.resolvers;

import java.util.List;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTaskExecutionResolver;
import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.prefs.entities.Preference;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

/**
 * The Class SingleNodeExecutionResolver.
 */
@Component
@XnatTaskExecutionResolver(resolverId = "SingleNodeExecutionResolver",
	description = "Single Node Execution Resolver")
public class SingleNodeExecutionResolver implements XnatTaskExecutionResolverI {
	
	/** The _xnat node. */
	private final XnatNode _xnatNode;
	
	/** The _preferences. */
	private final SiteConfigPreferences _preferences;
	
	/**
	 * Instantiates a new single node execution resolver.
	 *
	 * @param xnatNode the xnat node
	 * @param preferences the preferences
	 */
	@Autowired
	public SingleNodeExecutionResolver(final XnatNode xnatNode, final SiteConfigPreferences preferences) {
		_xnatNode = xnatNode;
		_preferences = preferences;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskExecutionResolverI#shouldRunTask(java.lang.String)
	 */
	@Override
	public boolean shouldRunTask(final String taskId) {
		final String prefKey = "task-" + taskId + "-node";
		if (_preferences.getPreferenceKeys().contains(prefKey)) {
			final Preference taskNodePref = _preferences.get(prefKey);
			if (taskNodePref.getValue() != null && taskNodePref.getValue().trim().length()>0) {
				return _xnatNode.getNodeId().equals(taskNodePref.getValue()); 
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskExecutionResolverI#getConfigurationElementsYaml(java.lang.String)
	 */
	@Override
	public List<String> getConfigurationElementsYaml(final String taskId) {
		final List<String> eleList = Lists.newArrayList();
		final String ele = 
				"kind: panel.input.text\n" +
				"label: " + taskId + " Execution Node\n" +
				"name: task-" + taskId + "-node\n" +
				"size: 45\n" + 
				"placeholder: nodeID\n" +
				"description: Node on which to run this process. This configuration is not required on a single-node XNAT installation, " +
					"however in a multi-node environment, where multiple XNAT instances point to the same database, this configuration " + 
					"should be in place in order to avoid conflicts when the task tries to run simultaneously on multiple nodes. " +
					"Nodes are defined by setting a <em>node.id</em> property value in a properies file called <em>node-conf.properties</em> located " +
					"in the same directory as your <em>xnat-conf.properties</em> file.\n";
		eleList.add(ele);
		return eleList;
	}

}
