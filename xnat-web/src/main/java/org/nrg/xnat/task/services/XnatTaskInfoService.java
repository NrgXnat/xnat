package org.nrg.xnat.task.services;

import java.util.List;

import org.nrg.framework.node.XnatNode;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.task.entities.XnatTaskInfo;

/**
 * The Interface XnatTaskInfoService.
 */
public interface XnatTaskInfoService extends BaseHibernateService<XnatTaskInfo> {

	/**
	 * Record task run.
	 *
	 * @param _xnat the _xnat
	 * @param taskId the task id
	 */
	void recordTaskRun(XnatNode _xnat, String taskId);

	/**
	 * Gets the xnat task info list by task id and node.
	 *
	 * @param taskId the task id
	 * @return the xnat task info list by task id and node
	 */
	List<XnatTaskInfo> getXnatTaskInfoListByTaskIdAndNode(String taskId);

}
