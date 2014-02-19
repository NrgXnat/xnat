/*
 * org.nrg.config.entities.Configuration
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 9/4/13 4:37 PM
 */
package org.nrg.config.entities;

import javax.persistence.*;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class Configuration extends AbstractHibernateEntity {

	public static final String ENABLED_STRING = "enabled";
	public static final String DISABLED_STRING = "disabled";
    private static final long serialVersionUID = -8112028990905366714L;

    private Long project;
	private String tool;
	private String path;
	private ConfigurationData configData;

	private String xnatUser;
	private String reason;
	private String status;
	private int version;
    private boolean unversioned;

	@Transient
	public String getContents(){
		if(configData != null) {
			return configData.getContents();
		} else {
			return null;
		}
	}

	public String getXnatUser() {
		return xnatUser;
	}

	public void setXnatUser(String xnatUser) {
		this.xnatUser = xnatUser;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getProject() {
		return project;
	}

	public void setProject(Long project) {
		this.project = project;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	@ManyToOne(cascade = CascadeType.PERSIST)
	public ConfigurationData getConfigData() {
		return configData;
	}

	public void setConfigData(ConfigurationData data) {
		this.configData = data;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

    @Column(columnDefinition="boolean default false")
    public boolean isUnversioned() {
        return unversioned;
    }

    public void setUnversioned(final boolean unversioned) {
        this.unversioned = unversioned;
    }

	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
}
