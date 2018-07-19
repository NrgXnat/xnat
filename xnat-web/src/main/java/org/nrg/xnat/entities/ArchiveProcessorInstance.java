package org.nrg.xnat.entities;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.Map;
import java.util.Set;

/**
 * Created by mike on 2/26/18.
 */
@Slf4j
@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class ArchiveProcessorInstance extends AbstractHibernateEntity {
    public ArchiveProcessorInstance(){

    }

    public ArchiveProcessorInstance(String label, String scope, Set<String> scpWhitelist, Set<String> scpBlacklist, int order, Map<String, String> parameters, String processorClass) {
        this.label = label;
        this.scope = scope;
        this.scpWhitelist = scpWhitelist;
        this.scpBlacklist = scpBlacklist;
        this.priority = priority;
        this.location = location;
        this.parameters = parameters;
        this.processorClass = processorClass;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name="parameterName")
    @Column(name="value")
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(String processorClass) {
        this.processorClass = processorClass;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<String> getScpWhitelist() {
        return scpWhitelist;
    }

    public void setScpWhitelist(Set<String> scpWhitelist) {
        this.scpWhitelist = scpWhitelist;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<String> getScpBlacklist() {
        return scpBlacklist;
    }

    public void setScpBlacklist(Set<String> scpBlacklist) {
        this.scpBlacklist = scpBlacklist;
    }

    private String label;

    private String scope;

    private Set<String> scpWhitelist;
    private Set<String> scpBlacklist;
    private int location;
    private int priority;
    private Map<String, String> parameters;
    private String processorClass;

    public static final String SITE_SCOPE="site";
}
