package org.nrg.xnat.entities;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"hostName"}))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class HostInfo extends AbstractHibernateEntity {

	private static final long serialVersionUID = -1264374836830855705L;
	
	private String hostName;
	
    public HostInfo() {
    	super();
    }
    
    public HostInfo(String hostName) {
    	super();
    	this.hostName = hostName;
    }

    /**
     * @param name Sets the hostname property.
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @return Returns the name property.
     */
    public String getHostName() {
        return this.hostName;
    }

}
