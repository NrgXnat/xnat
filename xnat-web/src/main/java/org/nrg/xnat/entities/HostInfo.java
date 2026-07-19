/*
 * web: org.nrg.xnat.entities.HostInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.entities;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serial;

/**
 * The Class HostInfo.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"hostName"}))
@Cacheable
public class HostInfo extends AbstractHibernateEntity {

    /** The Constant serialVersionUID. */
    @Serial
    private static final long serialVersionUID = -1264374836830855705L;
	
	/** The host name. */
	private String hostName;
	
    /**
     * Instantiates a new host info.
     */
    public HostInfo() {
    	super();
    }
    
    /**
     * Instantiates a new host info.
     *
     * @param hostName the host name
     */
    public HostInfo(String hostName) {
    	super();
    	this.hostName = hostName;
    }

    /**
     * Sets the host name.
     *
     * @param hostName the new host name
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostName() {
        return this.hostName;
    }

}
