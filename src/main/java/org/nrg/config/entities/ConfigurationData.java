/*
 * org.nrg.config.entities.ConfigurationData
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 5:30 PM
 */
package org.nrg.config.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.framework.orm.hibernate.annotations.Auditable;

@Auditable
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrgConfig")
public class ConfigurationData  extends AbstractHibernateEntity{
	
	public static final int MAX_FILE_LENGTH = 10485760; //max size for postgres, see htup.h #define MaxAttrSize (10 * 1024 * 1024) 
	
	private Set<Configuration> configurations;

	private String contents;
	
	@Column(length=MAX_FILE_LENGTH)
	//@Column(columnDefinition="TEXT")//if you need > 10MB files uncomment this, comment out length and adjust MAX_FILE_LENGTH 
	public String getContents() {
		return contents;
	}
	
	public void setContents(String contents) {
		this.contents = contents;
	}
	
	@OneToMany
	public Set<Configuration> getConfigurations() {
		return configurations;
	}
	
	public void setConfigurations(Set<Configuration> configurations) {
		this.configurations = configurations;
	}
}
