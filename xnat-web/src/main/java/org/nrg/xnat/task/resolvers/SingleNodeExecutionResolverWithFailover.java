/*
 * web: org.nrg.xnat.task.resolvers.SingleNodeExecutionResolverWithFailover
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.resolvers;

import java.util.Arrays;
import java.util.List;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTaskExecutionResolver;
import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.prefs.entities.Preference;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xnat.helpers.prearchive.SessionXMLRebuilder;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.nrg.xnat.task.entities.XnatTaskInfo;
import org.nrg.xnat.task.services.XnatTaskInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.google.common.collect.Lists;

/**
 * The Class SingleNodeExecutionResolverWithFailover.  
 */
@Component
@XnatTaskExecutionResolver(resolverId = "SingleNodeExecutionResolverWithFailover",
	description = "Single Node Execution Resolver With Failover")
public class SingleNodeExecutionResolverWithFailover implements XnatTaskExecutionResolverI {
	
	/** The _xnat node. */
	private final XnatNode _xnatNode;
	
	/** The _preferences. */
	private final SiteConfigPreferences _preferences;
	
	/** The _xnat task info service. */
	private final XnatTaskInfoService _xnatTaskInfoService;
	
	/** The _xnat node info service. */
	private final XnatNodeInfoService _xnatNodeInfoService;
	
	/** The Constant DEFAULT_WAIT_MIN. */
	private static final int DEFAULT_WAIT_MIN = 30;
	
    /** The Constant _log. */
    private static final Logger _log = LoggerFactory.getLogger(SessionXMLRebuilder.class);
	
	/**
	 * Instantiates a new single node execution resolver with failover.
	 *
	 * @param xnatNode the xnat node
	 * @param preferences the preferences
	 * @param xnatTaskInfoService the xnat task info service
	 * @param xnatNodeInfoService the xnat node info service
	 */
	@Autowired
	public SingleNodeExecutionResolverWithFailover(final XnatNode xnatNode, final SiteConfigPreferences preferences,
			final XnatTaskInfoService xnatTaskInfoService, final XnatNodeInfoService xnatNodeInfoService) {
		_xnatNode = xnatNode;
		_preferences = preferences;
		_xnatTaskInfoService = xnatTaskInfoService;
		_xnatNodeInfoService = xnatNodeInfoService;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskExecutionResolverI#shouldRunTask(java.lang.String)
	 */
	@Override
	public boolean shouldRunTask(final String taskId) {
		final String prefKeyNodeList = "task-" + taskId + "-nodelist";
		final String prefKeyWait = "task-" + taskId + "-wait";
		Preference nodeListPref = null;
		Preference nodeWaitPref = null;
		if (_preferences.getPreferenceKeys().contains(prefKeyNodeList)) {
			nodeListPref = _preferences.get(prefKeyNodeList);
		}	
		if (_preferences.getPreferenceKeys().contains(prefKeyWait)) {
			nodeWaitPref = _preferences.get(prefKeyWait);
		}	
		if (nodeListPref == null) {
			// If task not configured, we'll run.
			_log.trace("ExecutionResolver:  No configuration found.  This node will run the task.");
			return true;
		}	
		final List<String> nodeList = Arrays.asList(nodeListPref.getValue().toString().replaceAll(" ","").split(","));
		final int waitMin = (nodeWaitPref.getValue()!=null) ? Integer.valueOf(nodeWaitPref.getValue()) : DEFAULT_WAIT_MIN;
		final List<XnatNodeInfo> nodeInfoList = _xnatNodeInfoService.getAll();
		final List<XnatTaskInfo> taskInfoList = _xnatTaskInfoService.getXnatTaskInfoListByTaskIdAndNode(taskId);
		final long currentTime = System.currentTimeMillis();
		final List<String> upNodes = Lists.newArrayList();
		final List<String> prefNodes = Lists.newArrayList();
		for (final String node : nodeList) {
			if (node.equals(_xnatNode.getNodeId())) {
				if (upNodes.size()>0) {
					_log.trace("ExecutionResolver:  Preferred node is up.  This node will not run task.");
					return false;
				} else if (!hasBeenRunRecentlyByAPreferredNode(prefNodes, taskInfoList, currentTime, waitMin)) {
					if (prefNodes.size() > 0) {
						_log.trace("ExecutionResolver:  No preferred node is up, and the task hasn't recently been run by a preferred node.  This node will run the task.");
					} else {
						_log.trace("ExecutionResolver:  Preferred node.  This node will run the task.");
					}
					return true;
				} else {
					_log.trace("ExecutionResolver:  Preferred nodes are not up, but one of them has recently run this task.  Skipping for now.");
					return false;
				}
			} else {
				prefNodes.add(node);
				if (isUp(node, nodeInfoList, currentTime, waitMin)) {
					upNodes.add(node);
				}
			}
		}
		_log.trace("ExecutionResolver:  Resolver didn't match any criteria to run this task.  This node will not run task.");
		return false;
	}

	/**
	 * Checks if is up.
	 *
	 * @param node the node
	 * @param nodeInfoList the node info list
	 * @param currentTime the current time
	 * @param waitMin the wait min
	 * @return true, if is up
	 */
	private boolean isUp(final String node,final  List<XnatNodeInfo> nodeInfoList, long currentTime, int waitMin) {
		for (final XnatNodeInfo nodeInfo : nodeInfoList) {
			if (node.equals(nodeInfo.getNodeId())) {
				if (nodeInfo.getIsActive() && withinWait(nodeInfo.getLastCheckIn().getTime(), currentTime, waitMin)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Within wait.
	 *
	 * @param time the time
	 * @param currentTime the current time
	 * @param waitMin the wait min
	 * @return true, if successful
	 */
	private boolean withinWait(final long time, final long currentTime, final int waitMin) {
		return (currentTime-time) < (waitMin*60*1000); 
	}

	/**
	 * Checks if has been run recently by a preferred node.
	 *
	 * @param prefNodes the pref nodes
	 * @param taskInfoList the task info list
	 * @param currentTime the current time
	 * @param waitMin the wait min
	 * @return true, if successful
	 */
	private boolean hasBeenRunRecentlyByAPreferredNode(final List<String> prefNodes, final List<XnatTaskInfo> taskInfoList,
			final long currentTime, final int waitMin) {
		for (final XnatTaskInfo taskInfo : taskInfoList) {
			if (prefNodes.contains(taskInfo.getXnatNodeInfo().getNodeId()) && withinWait(taskInfo.getLastRun().getTime(), currentTime,waitMin)) {
				return true;
				
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.nrg.framework.task.XnatTaskExecutionResolverI#getConfigurationElementsYaml(java.lang.String)
	 */
	@Override
	public List<String> getConfigurationElementsYaml(final String taskId) {
		// NOTE:  These configuration elements will be returned to the UI to build the configuration page used by the tasks that use this execution resolver. 
		final List<String> eleList = Lists.newArrayList();
		final String ele = 
				"nodeList:\n" +
				"   kind: panel.input.text\n" +
				"   label: Execution Node List\n" +
				"   name: task-" + taskId + "-nodelist\n" +
				
				"   placeholder: nodeID1,nodeId2\n" + 
				"   description: Comma-separated list of nodes ID the system should use from which to choose a single node on which to run this process.  The " +
					"system tries the first node.  If that node reposrts that it's down or hasn't checked in more than 10 minutes, " +
					"the second, and then subsequent, nodes will be tried.  This configuration is not required on a single-node XNAT installation, " +
					"however in a multi-node environment, where multiple XNAT instances point to the same database, this configuration " + 
					"should be in place in order to avoid conflicts when the task tries to run simultaneously on multiple nodes. " +
					"Nodes are defined by setting a <em>node.id</em> property value in a properies file called <em>node-conf.properties</em> located " +
					"in the same directory as your <em>xnat-conf.properties</em> file.\n" +
				"waitTime:\n" +
				"   kind: panel.input.text\n" +
				"   label: Wait before failover(minutes)\n" +
				"   name: task-" + taskId + "-wait\n" +
				"   placeholder: " + DEFAULT_WAIT_MIN + "\n" + 
				"   size: 5\n" +
				"   validation: allow-empty number\n" + 
				"   description: Minutes since last indication of server live time to wait before failing over to next node.  Make sure this value " + 
						"is significantly greater than the interval between task runs, as this resolver currently doesn't account for " +
						"differences in time between servers.\n";
		eleList.add(ele);
		return eleList;
	}

}
