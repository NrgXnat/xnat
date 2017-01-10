/*
 * web: org.nrg.xnat.node.entities.XnatNodeInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.node.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.task.entities.XnatTaskInfo;

/**
 * The Class XnatNodeInfo.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"nodeId","hostName"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class XnatNodeInfo extends AbstractHibernateEntity {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1629813101201706014L;
	
	/** The node id. */
	private String nodeId;
	
	/** The host name. */
	private String hostName;
	
	/** The last ip address. */
	private String lastIpAddress;
	
	/** The last initialized. */
	private Date lastInitialized;
	
	/** The last check-in. */
	private Date lastCheckIn;
	
	/** The is active. */
	private Boolean isActive;

	/** The xnat task info. */
	private List<XnatTaskInfo> xnatTaskInfo;

	/**
	 * Instantiates a new xnat node info.
	 */
	public XnatNodeInfo() {
		super();
	}
	
	/**
	 * Instantiates a new xnat node info.
	 *
	 * @param nodeId the node id
	 * @param hostName the host name
	 * @param lastIpAddress the last ip address
	 * @param lastInitialized the last initialized
	 */
	public XnatNodeInfo(final String nodeId, final String hostName, final String lastIpAddress, final Date lastInitialized) {
		super();
		this.nodeId = nodeId;
		this.hostName = hostName;
		this.lastIpAddress = lastIpAddress;
		this.lastInitialized = lastInitialized;
	}
	
	/**
	 * Instantiates a new xnat node info.
	 *
	 * @param nodeId the node id
	 */
	public XnatNodeInfo(final String nodeId) {
		super();
		this.nodeId = nodeId;
	}
	
	/**
	 * Gets the node id.
	 *
	 * @return the node id
	 */
	public String getNodeId() {
		return nodeId;
	}
	
	/**
	 * Sets the node id.
	 *
	 * @param nodeId the new node id
	 */
	public void setNodeId(final String nodeId) {
		this.nodeId = nodeId;
	}
	
	/**
	 * Gets the host name.
	 *
	 * @return the host name
	 */
	public String getHostName() {
		return hostName;
	}
	
	/**
	 * Sets the host name.
	 *
	 * @param hostName the new host name
	 */
	public void sethostName(final String hostName) {
		this.hostName = hostName;
	}
	
	/**
	 * Gets the last ip address.
	 *
	 * @return the last ip address
	 */
	public String getLastIpAddress() {
		return lastIpAddress;
	}
	
	/**
	 * Sets the last ip address.
	 *
	 * @param lastIpAddress the new last ip address
	 */
	public void setLastIpAddress(final String lastIpAddress) {
		this.lastIpAddress = lastIpAddress;
	}
	
	/**
	 * Gets the last initialized.
	 *
	 * @return the last initialized
	 */
	public Date getLastInitialized() {
		return lastInitialized;
	}
	
	/**
	 * Sets the last initialized.
	 *
	 * @param lastInitialized the new last initialized
	 */
	public void setLastInitialized(final Date lastInitialized) {
		this.lastInitialized = lastInitialized;
	}
	
	/**
	 * Gets the _last check in.
	 *
	 * @return the _last check in
	 */
	public Date getLastCheckIn() {
		return lastCheckIn;
	}

	/**
	 * Sets the last check in.
	 *
	 * @param lastCheckIn the new last check in
	 */
	public void setLastCheckIn(final Date lastCheckIn) {
		this.lastCheckIn = lastCheckIn;
	}
	
	/**
	 * Gets the checks if is active.
	 *
	 * @return the checks if is active
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * Sets the checks if is active.
	 *
	 * @param isActive the new checks if is active
	 */
	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Gets the xnat task info.
	 *
	 * @return the xnat task info
	 */
	@OneToMany(targetEntity = XnatTaskInfo.class, mappedBy = "xnatNodeInfo")
	public List<XnatTaskInfo> getXnatTaskInfo() {
		return xnatTaskInfo;
	}

	/**
	 * Sets the xnat task info.
	 *
	 * @param xnatTaskInfo the new xnat task info
	 */
	public void setXnatTaskInfo(final List<XnatTaskInfo> xnatTaskInfo) {
		this.xnatTaskInfo = xnatTaskInfo;
	}

}
