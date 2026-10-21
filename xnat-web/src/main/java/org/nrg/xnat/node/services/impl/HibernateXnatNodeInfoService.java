/*
 * web: org.nrg.xnat.node.services.impl.HibernateXnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.node.dao.XnatNodeInfoDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The Class HibernateXnatNodeInfoService.
 */
@Service
@Transactional
@Slf4j
public class HibernateXnatNodeInfoService extends AbstractHibernateEntityService<XnatNodeInfo, XnatNodeInfoDAO> implements XnatNodeInfoService {
    private final XnatNode _xnatNode;
    private final JdbcTemplate _jdbcTemplate;

    /**
     * Instantiates a new hibernate xnat node info service.
     *
     * @param xnatNode     the xnat node
     * @param jdbcTemplate the jdbc template
     */
    @Autowired
    public HibernateXnatNodeInfoService(final XnatNode xnatNode, final JdbcTemplate jdbcTemplate) {
        _xnatNode     = xnatNode;
        _jdbcTemplate = jdbcTemplate;
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeInfo(org.nrg.xnat.node.XnatNode)
     */
    @Override
    public void recordNodeInitialization() {
        getLocalHost().ifPresent(localHost -> {
            final String             nodeId           = _xnatNode.getNodeId();
            final List<XnatNodeInfo> nodeInfoList     = getDao().getXnatNodeInfoListByNodeId(nodeId);
            final String             localHostName    = localHost.getHostName();
            final String             localHostAddress = localHost.getHostAddress();
            final Date               initialized      = new Date(System.currentTimeMillis());
            for (final XnatNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo.getHostName().equals(localHostName)) {
                    nodeInfo.setLastIpAddress(localHostAddress);
                    nodeInfo.setLastInitialized(initialized);
                    nodeInfo.setIsActive(true);
                    getDao().saveOrUpdate(nodeInfo);
                    return;
                }
            }
            final XnatNodeInfo nodeInfo = new XnatNodeInfo(nodeId, localHostName, localHostAddress, initialized);
            getDao().saveOrUpdate(nodeInfo);
        });
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#checkIn(org.nrg.framework.node.XnatNode)
     */
    @Override
    public void recordNodeCheckIn() {
        getLocalHost().ifPresent(localHost -> {
            final String nodeId           = _xnatNode.getNodeId();
            final String localHostName    = localHost.getHostName();
            final String localHostAddress = localHost.getHostAddress();
            final Date   checkIn          = new Date(System.currentTimeMillis());

            final XnatNodeInfo nodeInfo = ObjectUtils.getIfNull(getDao().getXnatNodeInfoByNodeIdAndHostname(nodeId, localHostName),
                                                                () -> new XnatNodeInfo(nodeId, localHostName));
            nodeInfo.setLastIpAddress(localHostAddress);
            nodeInfo.setLastCheckIn(checkIn);
            nodeInfo.setIsActive(true);
            getDao().saveOrUpdate(nodeInfo);
        });
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeShutdown()
     */
    @Override
    @PreDestroy
    public void recordNodeShutdown() {
        getLocalHost().ifPresent(localHost -> {
            final String             nodeId        = _xnatNode.getNodeId();
            final List<XnatNodeInfo> nodeInfoList  = getDao().getXnatNodeInfoListByNodeId(nodeId);
            final String             localHostName = localHost.getHostName();
            for (final XnatNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo.getHostName().equals(localHostName)) {
                    // Use JDBC here.  The Hibernate session doen't seem to be available in the PreDestroy context.
                    _jdbcTemplate.update("UPDATE xhbm_xnat_node_info SET is_active = FALSE WHERE node_id=? and host_name=?", nodeId, localHostName);
                    return;
                }
            }
        });
    }

    @Override
    public List<XnatNodeInfo> getXnatNodeInfoByNodeId(final String nodeId) {
        return getDao().getXnatNodeInfoListByNodeId(nodeId);
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#getXnatNodeInfoByNodeIdAndHostname(java.lang.String, java.lang.String)
     */
    @Override
    public XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(String nodeId, String hostName) {
        return getDao().getXnatNodeInfoByNodeIdAndHostname(nodeId, hostName);
    }

    private static Optional<InetAddress> getLocalHost() {
        try {
            return Optional.of(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log.warn("WARNING:  Unable to obtain host information.  Cannot record node information", e);
            return Optional.empty();
        }
    }
}
