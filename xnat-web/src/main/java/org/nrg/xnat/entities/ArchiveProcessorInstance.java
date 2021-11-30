/*
 * web: org.nrg.xnat.entities.ArchiveProcessorInstance
 * XNAT http://www.xnat.org
 * Copyright (c) 2018-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xnat.archive.GradualDicomImporter;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mike on 2/26/18.
 */
@Slf4j
@Entity
@Table
@Audited
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveProcessorInstance extends AbstractHibernateEntity {
    private static final long serialVersionUID = 6376648522174162478L;

    public ArchiveProcessorInstance(final String label, final String scope, final Set<String> scpWhitelist, final Set<String> scpBlacklist, final int priority, final String location, final Map<String, String> parameters, final String processorClass) {
        this.label = label;
        this.scope = scope;
        this.scpWhitelist = new HashSet<>(scpWhitelist);
        this.scpBlacklist = new HashSet<>(scpBlacklist);
        this.priority = priority;
        this.location = location;
        this.parameters = new HashMap<>(parameters);
        this.processorClass = processorClass;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "parameterName")
    @Column(name = "value")
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(final String processorClass) {
        this.processorClass = processorClass;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<String> getScpWhitelist() {
        return scpWhitelist;
    }

    public void setScpWhitelist(final Set<String> scpWhitelist) {
        this.scpWhitelist = scpWhitelist;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public Set<String> getScpBlacklist() {
        return scpBlacklist;
    }

    public void setScpBlacklist(final Set<String> scpBlacklist) {
        this.scpBlacklist = scpBlacklist;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    public List<String> getProjectIdsList() {
        return projectIdsList;
    }

    public void setProjectIdsList(final List<String> projectIdsList) {
        this.projectIdsList = projectIdsList;
    }

    /**
     * Updates this archive processor instance from the values set in the submitted other archive processor instance.
     * Null values or values that are the same as this instance are ignored. If no changes are found, this method
     * returns <b>false</b>.
     *
     * @param other The archive processor instance from which property values should be copied.
     *
     * @return Returns <b>true</b> if any property values were changed in this instance, <b>false</b> otherwise.
     *
     * @throws DataFormatException When a project-based processor is being created before the project is set.
     */
    public boolean update(final ArchiveProcessorInstance other) throws DataFormatException {
        // Only update fields that are actually included in the submitted data and differ from the original source.
        final AtomicBoolean isDirty = new AtomicBoolean();
        if (StringUtils.isNotBlank(other.getLabel()) && !StringUtils.equals(other.getLabel(), getLabel())) {
            setLabel(other.getLabel());
            isDirty.set(true);
        }
        if (StringUtils.isNotBlank(other.getScope()) && !StringUtils.equals(other.getScope(), getScope())) {
            setScope(other.getScope());
            isDirty.set(true);
        }
        if (other.getProjectIdsList() != null && !other.getProjectIdsList().equals(getProjectIdsList())) {
            setProjectIdsList(other.getProjectIdsList());
            isDirty.set(true);
        }
        if (other.getScpWhitelist() != null && !other.getScpWhitelist().equals(getScpWhitelist())) {
            setScpWhitelist(other.getScpWhitelist());
            isDirty.set(true);
        }
        if (other.getScpBlacklist() != null && !other.getScpBlacklist().equals(getScpBlacklist())) {
            setScpBlacklist(other.getScpBlacklist());
            isDirty.set(true);
        }
        if (other.getPriority() != getPriority()) {
            setPriority(other.getPriority());
            isDirty.set(true);
        }
        if (StringUtils.isNotBlank(other.getLocation()) && !StringUtils.equals(other.getLocation(), getLocation())) {
            checkForValidProject(other.getLocation());
            setLocation(other.getLocation());
            isDirty.set(true);
        }
        if ((other.getParameters() == null && getParameters() != null) || (other.getParameters() != null && getParameters() == null) || ((other.getParameters() != null) && !other.getParameters().equals(getParameters()))) {
            setParameters(other.getParameters());
            isDirty.set(true);
        }
        if (StringUtils.isNotBlank(other.getProcessorClass()) && !StringUtils.equals(other.getProcessorClass(), getProcessorClass())) {
            setProcessorClass(other.getProcessorClass());
            isDirty.set(true);
        }
        if (other.isEnabled() != isEnabled()) {
            setEnabled(other.isEnabled());
            isDirty.set(true);
        }
        return isDirty.get();
    }

    /**
     * Checks that the location for this archive processor instance is valid.
     *
     * @throws DataFormatException Thrown when the location is invalid. The message contains a list of valid projects ID for the archive processor instance.
     */
    public void checkForValidProject() throws DataFormatException {
        checkForValidProject(getLocation());
    }

    /**
     * Checks that the specified location is valid for this archive processor instance.
     *
     * @throws DataFormatException Thrown when the location is invalid. The message contains a list of valid projects ID for the archive processor instance.
     */
    private void checkForValidProject(final String location) throws DataFormatException {
        if (StringUtils.equals(location, GradualDicomImporter.NAME_OF_LOCATION_AT_BEGINNING_AFTER_DICOM_OBJECT_IS_READ) && CollectionUtils.isNotEmpty(getProjectIdsList())) {
            throw new DataFormatException(String.join(", ", getProjectIdsList()));
        }
    }

    private String              label;
    private String              scope;
    private Set<String>         scpWhitelist;
    private Set<String>         scpBlacklist;
    private String              location;
    private int                 priority;
    private Map<String, String> parameters;
    private String              processorClass;
    private List<String>        projectIdsList;
}
