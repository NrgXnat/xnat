/*
 * web: org.nrg.xnat.initialization.NodeConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization;

import org.nrg.framework.beans.Beans;
import org.nrg.framework.node.XnatNode;
import org.nrg.xnat.node.NodeCheckInRunner;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Properties;

/**
 * Sets up the database configuration for XNAT.
 */
@Configuration
public class NodeConfig {
	
    /**
     * Xnat node.
     *
     * @param environment the environment
     * @return the xnat node
     */
    @Bean
    public XnatNode xnatNode(final Environment environment) {
        final Properties properties = Beans.getNamespacedProperties(environment, NODE_PROPERTIES_NAMESPACE, true);
        return new XnatNode(properties.getProperty(NODE_PROPERTY_ID, XnatNode.NODE_ID_NOT_CONFIGURED));
    }
    
    /**
     * Perform node checkin.
     *
     * @param xnatNode        The node checking in
     * @param nodeInfoService the node info service
     *
     * @return the trigger task
     */
    @Bean
    public TriggerTask performNodeCheckin(final XnatNode xnatNode, XnatNodeInfoService nodeInfoService) {
      	return new TriggerTask(new NodeCheckInRunner(xnatNode, nodeInfoService), new CronTrigger("0 * * * * *"));
    }
    
    /** The Constant NODE_PROPERTIES_NAMESPACE. */
    private static final String NODE_PROPERTIES_NAMESPACE  = "node";
    
    /** The Constant NODE_PROPERTY_ID. */
    private static final String NODE_PROPERTY_ID           = "id";
    
}
