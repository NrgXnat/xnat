package org.nrg.xnat.services.messaging.archive;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;
import org.nrg.xnat.restlet.actions.importer.ImporterHandlerA;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

import static org.nrg.xnat.services.messaging.archive.DicomInboxImportRequest.Status.Queued;

@Slf4j
@Entity
@Table
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class DicomInboxImportRequest extends AbstractHibernateEntity {
    public static final String IMPORT_REQUEST_ID = "importRequestId";

    public enum Status {
        Queued,
        Trawling,
        Importing,
        Accepted,
        Processed,
        Failed,
        Completed
    }

    public DicomInboxImportRequest() {
        // Unfortunately have to do this explicitly or status will be null. This is a known Lombok issue:
        // https://github.com/rzwitserloot/lombok/issues/1347
        // https://github.com/rzwitserloot/lombok/pull/1429
        // Optimally you'd use the builder but in some cases (e.g. Hibernate and other bean factory frameworks)
        // how the object gets created is beyond your control.
        setStatus(Queued);
    }

    public DicomInboxImportRequest(final String username, final Map<String, String> parameters, final String sessionPath, final Status status, final boolean cleanupAfterImport, final String resolution) {
        this.username = username;
        this.parameters = parameters;
        this.sessionPath = sessionPath;
        this.status = status;
        this.cleanupAfterImport = cleanupAfterImport;
        this.resolution = resolution;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder sessionPath(String sessionPath) {
            this.sessionPath = sessionPath;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder cleanupAfterImport(final boolean cleanupAfterImport) {
            this.cleanupAfterImport = cleanupAfterImport;
            return this;
        }

        public Builder resolution(final String resolution) {
            this.resolution = resolution;
            return this;
        }

        public Builder parameters(final Map<String, String> parameters) {
            this.parameters.clear();
            this.parameters.putAll(parameters);
            return this;
        }

        public DicomInboxImportRequest build() {
            return new DicomInboxImportRequest(username, parameters, sessionPath, status, cleanupAfterImport, resolution);
        }

        private String              username;
        private String              sessionPath;
        private Status              status             = Queued;
        private boolean             cleanupAfterImport = true;
        private String              resolution;
        private Map<String, String> parameters         = new HashMap<>();
    }

    /**
     * Sets the {@link #setParameters(Map)} from a map of string keys and object values. This is necessary
     * because the Guava map transform utilities produce either unserializable or immutable maps, neither
     * of which is suitable for use with the JMS or JPA functions for which this class is intended. Values
     * are converted by calling the {@link String#valueOf(Object)} method.
     *
     * @param parameters The parameters to be set.
     */
    @SuppressWarnings("serial")
	@Transient
    public void setParametersFromObjectMap(final Map<String, Object> parameters) {
        setParameters(new HashMap<String, String>() {{
            for (final Map.Entry<String, Object> entry : parameters.entrySet()) {
                put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }});
    }

    /**
     * Gets the {@link #getParameters() parameters map of string keys and values} as a map of string keys
     * and object values. This is necessary because {@link ImporterHandlerA} assumes that importer constructors
     * take a <b>Map&ltString, Object&gt;</b> for their parameters. The string values are "converted" simply
     * by inserting them as the values.
     *
     * @return The parameters as a map of object values.
     */
    @SuppressWarnings("serial")
    @Transient
    public Map<String, Object> getObjectParametersMap() {
        return new HashMap<String, Object>() {{
            for (final Map.Entry<String, String> entry : getParameters().entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }};
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Must provide the public getParameters() method explicitly (i.e. without Lombok) in order for Hibernate
    // to properly determine the type of the parameters map.
    @ElementCollection(fetch = FetchType.EAGER)
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getSessionPath() {
        return sessionPath;
    }

    public void setSessionPath(String sessionPath) {
        this.sessionPath = sessionPath;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Boolean getCleanupAfterImport() {
        return cleanupAfterImport;
    }

    public void setCleanupAfterImport(Boolean cleanupAfterImport) {
        this.cleanupAfterImport = cleanupAfterImport;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return "DICOM inbox import requested by " + username + " on session " + sessionPath + ": " + parameters;
    }

    private String username;
    private String sessionPath;
    private Status status = Queued;
    private boolean cleanupAfterImport = true;
    @Column(length = 4096)
    private String resolution;
    private Map<String, String> parameters = new HashMap<>();
}
