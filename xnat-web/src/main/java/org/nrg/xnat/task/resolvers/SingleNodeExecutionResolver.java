/*
 * web: org.nrg.xnat.task.resolvers.SingleNodeExecutionResolver
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.resolvers;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.task.XnatTaskExecutionResolver;
import org.nrg.framework.task.XnatTaskExecutionResolverI;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * The Class SingleNodeExecutionResolver.
 */
@Component
@XnatTaskExecutionResolver(resolverId = "SingleNodeExecutionResolver", description = "Single Node Execution Resolver")
@Slf4j
public class SingleNodeExecutionResolver implements XnatTaskExecutionResolverI {

    /**
     * The _xnat node.
     */
    private final XnatNode _xnatNode;

    /**
     * The _preferences.
     */
    private final SiteConfigPreferences _preferences;

    /**
     * Instantiates a new single node execution resolver.
     *
     * @param xnatNode    the xnat node
     * @param preferences the preferences
     */
    @Autowired
    public SingleNodeExecutionResolver(final XnatNode xnatNode, final SiteConfigPreferences preferences) {
        log.debug("Configuring the single-node execution resolver for the node {}", xnatNode.getNodeId());
        _xnatNode = xnatNode;
        _preferences = preferences;
    }

    /* (non-Javadoc)
     * @see org.nrg.framework.task.XnatTaskExecutionResolverI#shouldRunTask(java.lang.String)
     */
    @Override
    public boolean shouldRunTask(final String taskId) {
        final String  prefKey     = "task-" + taskId + "-node";
        final boolean containsKey = _preferences.containsKey(prefKey);
        if (containsKey) {
            log.debug("Found preference setting for {}", prefKey);
            final String value = _preferences.getPreference(prefKey).getValue();
            if (StringUtils.isNotBlank(value)) {
                log.info("Located pref setting for \"{}\", found value \"{}\" and comparing to node ID \"{}\"", prefKey, value, _xnatNode.getNodeId());
                return _xnatNode.getNodeId().equals(value);
            }
        }
        log.debug("Didn't find a preference setting for {}, this node will run the task", prefKey);
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
                "Nodes are defined by setting a <em>node.id</em> property value in a properties file called <em>node-conf.properties</em> located " +
                "in the same directory as your <em>xnat-conf.properties</em> file.\n";
        eleList.add(ele);
        return eleList;
    }
}
