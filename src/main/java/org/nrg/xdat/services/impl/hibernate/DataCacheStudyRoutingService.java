/*
 * org.nrg.xdat.services.impl.hibernate.DataCacheStudyRoutingService
 * TIP is developed by the Neuroinformatics Research Group
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 6/13/14 12:35 PM
 */

package org.nrg.xdat.services.impl.hibernate;

import org.apache.commons.lang.StringUtils;
import org.nrg.framework.datacache.DataCacheService;
import org.nrg.xdat.services.StudyRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataCacheStudyRoutingService implements StudyRoutingService {

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p/>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link #update(String,
     * java.util.Map)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param userId           The login name for the user who created the routing configurations.
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     * The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     * instance UID already exists.
     */
    @Override
    public boolean assign(final String studyInstanceUid, final String projectId, final String userId) {
        return assign(studyInstanceUid, projectId, null, null, userId);
    }

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p/>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link #update(String,
     * java.util.Map)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param subjectId        The ID of the destination subject.
     * @param userId           The login name for the user who created the routing configurations.
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     * The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     * instance UID already exists.
     */
    @Override
    public boolean assign(final String studyInstanceUid, final String projectId, final String subjectId, final String userId) {
        return assign(studyInstanceUid, projectId, subjectId, null, userId);
    }

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p/>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link #update(String,
     * java.util.Map)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param subjectId        The ID of the destination subject.
     * @param label            The label for the session.
     * @param userId           The login name for the user who created the routing configurations.
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     * The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     * instance UID already exists.
     */
    @Override
    public boolean assign(final String studyInstanceUid, final String projectId, final String subjectId, final String label, final String userId) {
        if (getConfigurations().containsKey(studyInstanceUid)) {
            if (_log.isWarnEnabled()) {
                _log.warn("Attempt to assign new routing configuration for study instance UID: " + studyInstanceUid + " (configuration already exists!)");
            }
            return false;
        }
        if (StringUtils.isBlank(studyInstanceUid)) {
            throw new RuntimeException("You must specify a study instance UID to create a new routing configuration.");
        }
        if (StringUtils.isBlank(userId)) {
            throw new RuntimeException("You must specify a user ID to create a new routing configuration.");
        }
        if (StringUtils.isBlank(projectId) && StringUtils.isBlank(subjectId) && StringUtils.isBlank(label)) {
            throw new RuntimeException("You must specify at least one of a project ID, subject ID, or label to create a new routing configuration.");
        }

        HashMap<String, String> configuration = new HashMap<String, String>();
        String now = FORMATTER.format(new Date());

        configuration.put(USER, userId);
        configuration.put(CREATED, now);
        configuration.put(ACCESSED, now);

        if (!StringUtils.isBlank(projectId)) {
            configuration.put(PROJECT, projectId);
        }
        if (!StringUtils.isBlank(subjectId)) {
            configuration.put(SUBJECT, subjectId);
        }
        if (!StringUtils.isBlank(label)) {
            configuration.put(LABEL, label);
        }

        if (_log.isDebugEnabled()) {
            _log.debug("Updated routing configuration for study instance UID: " + studyInstanceUid + "\n" + configuration.toString());
        }

        return commit(studyInstanceUid, configuration);
    }

    /**
     * Updates the configuration for the indicated study instance UID. Only the attributes specified in the submitted
     * configuration will be updated; all other existing attributes will remain the same (i.e. they will not be cleared
     * or deleted). To clear an existing attribute, submit the attribute key with a null value.
     * <p/>
     * <b>Note:</b> This method will not create a new routing configuration if one has not already been created for the
     * submitted study instance UID. In that case, the return value will be <b>false</b>. To create a new routing
     * configuration, use the {@link #assign(String, String, String)}, {@link #assign(String, String, String, String)},
     * or {@link #assign(String, String, String, String, String)} method.
     *
     * @param studyInstanceUid The study instance UID of the configuration to be updated.
     * @param configuration    The attribute keys and values to be updated.
     * @return This method returns <b>true</b> when the configuration was successfully updated, <b>false</b> otherwise.
     * The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     * instance UID doesn't exist to be updated.
     */
    @Override
    public boolean update(final String studyInstanceUid, final Map<String, String> configuration) {
        if (!getConfigurations().containsKey(studyInstanceUid)) {
            if (_log.isWarnEnabled()) {
                _log.warn("Attempt to update non-existent routing configuration for study instance UID: " + studyInstanceUid);
            }
            return false;
        }

        HashMap<String, String> existing = getConfigurations().get(studyInstanceUid);

        for (final String key : configuration.keySet()) {
            final String value = configuration.get(key);
            if (value == null && existing.containsKey(key)) {
                if (_log.isDebugEnabled()) {
                    _log.debug("Removing key " + key + " from configuration for study instance UID: " + studyInstanceUid);
                }
                existing.remove(key);
            } else {
                if (_log.isDebugEnabled()) {
                    if (existing.containsKey(key)) {
                        _log.debug("Updating existing key " + key + " in configuration for study instance UID: " + studyInstanceUid);
                    } else {
                        _log.debug("Creating new key " + key + " in configuration for study instance UID: " + studyInstanceUid);
                    }
                }
                existing.put(key, value);
            }
        }

        configuration.put(ACCESSED, FORMATTER.format(new Date()));

        return commit(studyInstanceUid, existing);
    }

    /**
     * Finds the active routing configuration for the indicated study instance UID. If there is no active routing
     * configuration for that study instance UID, this method returns <b>null</b>.
     *
     * @param studyInstanceUid The study instance UID to query.
     * @return The active routing configuration, if any, for the indicated study instance UID. Returns <b>null</b> if
     * there is no active routing configuration for that study.
     */
    @Override
    public Map<String, String> findStudyRouting(final String studyInstanceUid) {
        return getConfigurations().get(studyInstanceUid);
    }

    /**
     * Finds all active routing configurations for a particular project. This returns as a map of maps. The containing
     * map is keyed on the study instance UIDs for the study configurations. The contained maps include all of the
     * attributes for each routing configuration. The project ID configured for all routing configurations wil be the
     * same as the requested project ID parameter.
     *
     * @param projectId The ID of the destination project.
     * @return A list of the routing configurations for the indicated project.
     */
    @Override
    public Map<String, Map<String, String>> findProjectRoutings(final String projectId) {
        return findRoutingsByAttribute(PROJECT, projectId);
    }

    /**
     * Finds all active routing configurations created by a particular user. This returns as a map of maps. The
     * containing map is keyed on the study instance UIDs for the routing configurations. The contained maps include all
     * of the attributes for each routing configuration. The user ID configured for all routing configurations wil be
     * the same as the requested project ID parameter.
     *
     * @param userId The login name for the user who created the routing configurations.
     * @return A list of the routing configurations for the indicated user.
     */
    @Override
    public Map<String, Map<String, String>> findUserRoutings(final String userId) {
        return findRoutingsByAttribute(USER, userId);
    }

    /**
     * Finds all active routing configurations for a particular value of a particular attribute. This returns as a map
     * of maps. The containing map is keyed on the study instance UIDs for the study configurations. The contained maps
     * include all of the attributes for each routing configuration. The value for the requested attribute configured
     * for all routing configurations wil be the same as the requested attribute value.
     * @param attribute    The key for the attribute on which you wish to search.
     * @param value        The value of the attribute on which you wish to search.
     * @return A list of the routing configurations for the indicated attribute key and value.
     */
    @Override
    public Map<String, Map<String, String>> findRoutingsByAttribute(final String attribute, final String value) {
        Map<String, Map<String, String>> found = new HashMap<String, Map<String, String>>();
        for (final Map.Entry<String, HashMap<String, String>> entry : getConfigurations().entrySet()) {
            if (entry.getValue().containsKey(attribute) && entry.getValue().get(attribute).equals(value)) {
                found.put(entry.getKey(), entry.getValue());
            }
        }
        return found;
    }

    /**
     * Finds all active routing configurations.
     *
     * @return A list of the routing configurations currently in the system.
     */
    @Override
    public Map<String, Map<String, String>> findAllRoutings() {
        Map<String, Map<String, String>> found = new HashMap<String, Map<String, String>>();
        for (final Map.Entry<String, HashMap<String, String>> entry : getConfigurations().entrySet()) {
            found.put(entry.getKey(), entry.getValue());
        }
        return found;
    }

    /**
     * Closes the routing configuration for the indicated study instance UID. This should only be done when the study
     * has been fully received and archived. Any DICOM data for the study received later will not have the routing
     * configuration available any more, which could lead to split sessions. This method returns <b>true</b> if the
     * specified study instance UID exists in the routing configuration map and is closed successfully, and <b>false</b>
     * otherwise.
     *
     * @param studyInstanceUid The UID of the routing configuration to close.
     * @return <b>true</b> if the specified study instance UID exists in the routing configuration map and is closed
     * successfully, and <b>false</b> otherwise.
     */
    @Override
    public boolean close(final String studyInstanceUid) {
        if (!getConfigurations().containsKey(studyInstanceUid)) {
            return false;
        }
        getConfigurations().remove(studyInstanceUid);

        // The return value just checks for a non-zero object ID for the updated record.
        return _service.put(SERVICE_KEY, getConfigurations()) > 0;
    }

    /**
     * Closes all routing configurations on the site. This is a pretty extreme step and can only be performed by site
     * administrators. This returns all existing routings to allow for restoring configurations later.
     *
     * @return The existing routings.
     */
    @Override
    public Map<String, Map<String, String>> closeAll() {
        final Map<String, Map<String, String>> found = new HashMap<String, Map<String, String>>();
        for (final Map.Entry<String, HashMap<String, String>> entry : getConfigurations().entrySet()) {
            found.put(entry.getKey(), entry.getValue());
        }
        getConfigurations().clear();
        _service.put(SERVICE_KEY, getConfigurations());
        return found;
    }

    /**
     * Commits the routing configuration for the indicated study instance UID. Returns <b>true</b> if the commit
     * completed successfully, <b>false</b> otherwise.
     * @param studyInstanceUid    The study instance UID of the configuration to be created or updated.
     * @param configuration       The configuration itself.
     * @return <b>true</b> if the commit completed successfully, <b>false</b> otherwise.
     */
    private boolean commit(final String studyInstanceUid, final HashMap<String, String> configuration) {
        if (_log.isDebugEnabled()) {
            _log.debug(getConfigurations().containsKey(studyInstanceUid) ? "Updated existing" : "Created new" + " routing configuration for study instance UID: " + studyInstanceUid + "\n" + configuration.toString());
        }

        getConfigurations().put(studyInstanceUid, configuration);

        // The return value just checks for a non-zero object ID for the updated record.
        return _service.put(SERVICE_KEY, getConfigurations()) > 0;
    }

    private HashMap<String, HashMap<String, String>> getConfigurations() throws RuntimeException {
        if (_configuration == null) {
            _configuration = _service.get(SERVICE_KEY);
            if (_configuration == null) {
                _configuration = new HashMap<String, HashMap<String, String>>();
                _service.put(SERVICE_KEY, _configuration);
            }
        }
        return _configuration;
    }

    private static final String SERVICE_KEY = DataCacheStudyRoutingService.class.getName();
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss a");
    private static final Logger _log = LoggerFactory.getLogger(DataCacheStudyRoutingService.class);

    @Inject
    private DataCacheService _service;

    private HashMap<String, HashMap<String, String>> _configuration;
}
