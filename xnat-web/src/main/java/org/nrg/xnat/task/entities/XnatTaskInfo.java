/*
 * web: org.nrg.xnat.task.entities.XnatTaskInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.task.entities;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.node.entities.XnatNodeInfo;

/**
 * The Class XnatNodeInfo.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"taskId", "xnatNodeInfoId"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class XnatTaskInfo extends AbstractHibernateEntity {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7040159962774846865L;

	/** The task id. */
	private String taskId;
	
	/** The xnat node info. */
	private XnatNodeInfo xnatNodeInfo;
	
	/** The last run. */
	private Date lastRun;

	/**
	 * Instantiates a new xnat task info.
	 */
	public XnatTaskInfo() {
		super();
	}
	
	/**
	 * Instantiates a new xnat task info.
	 *
	 * @param taskId the task id
	 * @param xnatNodeInfo the xnat node info
	 * @param lastRun the last run
	 */
	public XnatTaskInfo(final String taskId, final XnatNodeInfo xnatNodeInfo, final Date lastRun) {
		super();
		this.taskId = taskId;
		this.xnatNodeInfo = xnatNodeInfo;
		this.lastRun = lastRun;
	}
	
	/**
	 * Gets the task id.
	 *
	 * @return the task id
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * Sets the task id.
	 *
	 * @param taskId the new task id
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	/**
	 * Gets the xnat node info.
	 *
	 * @return the xnat node info
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "xnatNodeInfoId")
	public XnatNodeInfo getXnatNodeInfo() {
		return xnatNodeInfo;
	}

	/**
	 * Sets the xnat node info.
	 *
	 * @param xnatNodeInfo the new xnat node info
	 */
	public void setXnatNodeInfo(XnatNodeInfo xnatNodeInfo) {
		this.xnatNodeInfo = xnatNodeInfo;
	}

	/**
	 * Gets the last run.
	 *
	 * @return the last run
	 */
	public Date getLastRun() {
		return lastRun;
	}

	/**
	 * Sets the last run.
	 *
	 * @param lastRun the new last run
	 */
	public void setLastRun(Date lastRun) {
		this.lastRun = lastRun;
	}

}
