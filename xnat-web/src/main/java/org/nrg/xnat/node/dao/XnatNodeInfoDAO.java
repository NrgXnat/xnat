/*
 * web: org.nrg.xnat.node.dao.XnatNodeInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.dao;

import java.util.List;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class XnatNodeInfoDAO.
 */
@Repository
public class XnatNodeInfoDAO extends AbstractHibernateDAO<XnatNodeInfo> {
    public static final String NODE_ID   = "nodeId";
    public static final String HOST_NAME = "hostName";

    /**
     * Gets the xnat node info list by node id.
     *
     * @param nodeId the node id
     *
     * @return the xnat node info list by node id
     */
    @Transactional
    public List<XnatNodeInfo> getXnatNodeInfoListByNodeId(final String nodeId) {
        return findByProperty(NODE_ID, nodeId);
    }

    /**
     * Gets the xnat node info by node id and hostname.
     *
     * @param nodeId   the node id
     * @param hostName the host name
     *
     * @return the xnat node info by node id and hostname
     */
    @Transactional
    public XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(final String nodeId, final String hostName) {
        return findByUniqueProperties(parameters(NODE_ID, nodeId, HOST_NAME, hostName));
    }
}
