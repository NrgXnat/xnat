package org.nrg.xnat.entities;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

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

    public ArchiveProcessorInstance(String scope, List<String> projectIdsList, int order, Map<String, String> parameters, String processorClass) {
        this.scope = scope;
        this.projectIdsList = projectIdsList;
        this.priority = priority;
        this.parameters = parameters;
        this.processorClass = processorClass;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @ElementCollection
    public List<String> getProjectIdsList() {
        return projectIdsList;
    }

    public void setProjectIdsList(List<String> projectIdsList) {
        this.projectIdsList = projectIdsList;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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

    private String scope;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> projectIdsList;

    private int priority;
    private Map<String, String> parameters;
    private String processorClass;

    public static final String SITE_SCOPE="site";
}
