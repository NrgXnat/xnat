/*
 * web: org.nrg.xnat.task.dao.XnatTaskInfoDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.dao;

import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.node.entities.XnatNodeInfo;
import org.nrg.xnat.task.entities.XnatTaskInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class XnatTaskInfoDAO.
 */
@Repository
public class XnatTaskInfoDAO extends AbstractHibernateDAO<XnatTaskInfo> {
    public static final String PROPERTY_TASK_ID        = "taskId";
    public static final String PROPERTY_XNAT_NODE_INFO = "xnatNodeInfo";

    /**
     * Gets the xnat task info by task id and node.
     *
     * @param taskId       the task id
     * @param xnatNodeInfo the xnat node info
     * @return the xnat task info by task id and node
     */
    public XnatTaskInfo getXnatTaskInfoByTaskIdAndNode(final String taskId, final XnatNodeInfo xnatNodeInfo) {
        return findByUniqueProperties(parameters(PROPERTY_TASK_ID, taskId, PROPERTY_XNAT_NODE_INFO, xnatNodeInfo));
    }

    /**
     * Gets the xnat task info list by task id and node.
     *
     * @param taskId the task id
     * @return the xnat task info list by task id and node
     */
    public List<XnatTaskInfo> getXnatTaskInfoListByTaskIdAndNode(final String taskId) {
        return findByProperty(PROPERTY_TASK_ID, taskId);
    }
}
