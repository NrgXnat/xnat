/*
 * web: org.nrg.xnat.node.services.impl.HibernateXnatNodeInfoService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.services.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.node.XnatNode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.node.dao.XnatNodeInfoDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.node.services.XnatNodeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

/**
 * The Class HibernateXnatNodeInfoService.
 */
@Service
@Slf4j
public class HibernateXnatNodeInfoService extends AbstractHibernateEntityService<XnatNodeInfo, XnatNodeInfoDAO> implements XnatNodeInfoService {
    /**
     * Instantiates a new hibernate xnat node info service.
     *
     * @param xnatNode     the xnat node
     * @param jdbcTemplate the jdbc template
     */
    @Autowired
    public HibernateXnatNodeInfoService(final XnatNode xnatNode, final JdbcTemplate jdbcTemplate) {
        _xnatNode = xnatNode;
        _jdbcTemplate = jdbcTemplate;
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeInfo(org.nrg.xnat.node.XnatNode)
     */
    @Override
    @Transactional
    public void recordNodeInitialization() {
        recordNodeEvent(NodeInfoEvent.initialization);
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#checkIn(org.nrg.framework.node.XnatNode)
     */
    @Override
    @Transactional
    public void recordNodeCheckIn() {
        recordNodeEvent(NodeInfoEvent.checkin);
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#recordNodeShutdown()
     */
    @Override
    @Transactional
    @PreDestroy
    public void recordNodeShutdown() {
        recordNodeEvent(NodeInfoEvent.shutdown);
    }

    @Override
    public List<XnatNodeInfo> getXnatNodeInfoByNodeId(final String nodeId) {
        return getDao().getXnatNodeInfoListByNodeId(nodeId);
    }

    /* (non-Javadoc)
     * @see org.nrg.xnat.node.services.XnatNodeInfoService#getXnatNodeInfoByNodeIdAndHostname(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional
    public XnatNodeInfo getXnatNodeInfoByNodeIdAndHostname(String nodeId, String hostName) {
        return getDao().getXnatNodeInfoByNodeIdAndHostname(nodeId, hostName);
    }

    @Override
    public List<XnatNodeInfo> getAllXnatNodeInfos() {
        return getDao().getAllXnatNodeInfos();
    }

    @Transactional
    @Override
    public List<XnatNodeInfo> getActiveXnatNodeInfos() {
        return getDao().getActiveXnatNodeInfos();
    }

    private void recordNodeEvent(final NodeInfoEvent event) {
        final InetAddress localHost;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.warn("WARNING:  Unable to obtain host information.  Cannot record node information", e);
            return;
        }
        final String nodeId           = _xnatNode.getNodeId();
        final String localHostName    = localHost.getHostName();
        final String localHostAddress = localHost.getHostAddress();

        final Optional<XnatNodeInfo> nodeInfoTry = Iterables.tryFind(getDao().getXnatNodeInfoListByNodeId(nodeId), new Predicate<XnatNodeInfo>() {
            @Override
            public boolean apply(final XnatNodeInfo nodeInfo) {
                return StringUtils.equals(nodeInfo.getHostName(), localHostName);
            }
        });

        final Date now = new Date(System.currentTimeMillis());

        final XnatNodeInfo nodeInfo;
        if (nodeInfoTry.isPresent()) {
            if (event == NodeInfoEvent.shutdown) {
                // Use JDBC here.  The Hibernate session doesn't seem to be available in the PreDestroy context.
                _jdbcTemplate.update("UPDATE xhbm_xnat_node_info SET is_active = FALSE WHERE node_id=? and host_name=?", nodeId, localHostName);
                return;
            }
            nodeInfo = nodeInfoTry.get();
            nodeInfo.setLastIpAddress(localHostAddress);
            nodeInfo.setIsActive(true);
            switch (event) {
                case initialization:
                    nodeInfo.setLastInitialized(now);
                    break;
                case checkin:
                    nodeInfo.setLastCheckIn(now);
                    break;
            }
        } else {
            nodeInfo = new XnatNodeInfo(nodeId, localHostName, localHostAddress, now);
            if (event == NodeInfoEvent.checkin) {
                nodeInfo.setLastCheckIn(now);
            }
        }
        getDao().saveOrUpdate(nodeInfo);
    }

    private enum NodeInfoEvent {
        initialization,
        checkin,
        shutdown
    }

    private final XnatNode     _xnatNode;
    private final JdbcTemplate _jdbcTemplate;
}
