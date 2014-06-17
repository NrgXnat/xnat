/**
 * AliasTokenService
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 4/17/12 by rherri01
 */
package org.nrg.xdat.services;

import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.services.NrgService;

import java.util.Map;

/**
 * Manages study routing configurations. Routing configurations are stored and accessed by the study instance UID of the
 * relevant DICOM study. Routing configurations determine the project, subject, or label applied to the study. If no
 * routing configuration can be found for a particular study, it's up to the object identifier to determine a fall-back
 * or default strategy.
 */
public interface StudyRoutingService extends NrgService {
    public static final String PROJECT  = "project";
    public static final String SUBJECT  = "subject";
    public static final String LABEL    = "label";
    public static final String USER     = "user";
    public static final String CREATED  = "created";
    public static final String ACCESSED = "accessed";

    /**
     * Creates a new routing configuration for the indicated attributes.
     *
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link #update(String,
     * java.util.Map)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param userId           The login name for the user who created the routing configurations.
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    abstract public boolean assign(String studyInstanceUid, String projectId, String userId);
    /**
     * Creates a new routing configuration for the indicated attributes.
     *
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
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    abstract public boolean assign(String studyInstanceUid, String projectId, String subjectId, String userId);
    /**
     * Creates a new routing configuration for the indicated attributes.
     *
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
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    abstract public boolean assign(String studyInstanceUid, String projectId, String subjectId, String label, String userId);
    /**
     * Updates the configuration for the indicated study instance UID. Only the attributes specified in the submitted
     * configuration will be updated; all other existing attributes will remain the same (i.e. they will not be cleared
     * or deleted). To clear an existing attribute, submit the attribute key with a null value.
     *
     * <b>Note:</b> This method will not create a new routing configuration if one has not already been created for the
     * submitted study instance UID. In that case, the return value will be <b>false</b>. To create a new routing
     * configuration, use the {@link #assign(String, String, String)}, {@link #assign(String, String, String, String)},
     * or {@link #assign(String, String, String, String, String)} method.
     * @param studyInstanceUid    The study instance UID of the configuration to be updated.
     * @param configuration       The attribute keys and values to be updated.
     * @return This method returns <b>true</b> when the configuration was successfully updated, <b>false</b> otherwise.
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID doesn't exist to be updated.
     */
    abstract public boolean update(String studyInstanceUid, Map<String, String> configuration);
    /**
     * Finds the active routing configuration for the indicated study instance UID. If there is no active routing
     * configuration for that study instance UID, this method returns <b>null</b>.
     * @param studyInstanceUid    The study instance UID to query.
     * @return The active routing configuration, if any, for the indicated study instance UID. Returns <b>null</b> if
     * there is no active routing configuration for that study.
     */
    abstract public Map<String, String> findStudyRouting(String studyInstanceUid);
    /**
     * Finds all active routing configurations for a particular project. This returns as a map of maps. The containing
     * map is keyed on the study instance UIDs for the study configurations. The contained maps include all of the
     * attributes for each routing configuration. The project ID configured for all routing configurations wil be the
     * same as the requested project ID parameter.
     * @param projectId    The ID of the destination project.
     * @return A list of the routing configurations for the indicated project.
     */
    abstract public Map<String, Map<String, String>> findProjectRoutings(String projectId);
    /**
     * Finds all active routing configurations created by a particular user. This returns as a map of maps. The
     * containing map is keyed on the study instance UIDs for the routing configurations. The contained maps include all
     * of the attributes for each routing configuration. The user ID configured for all routing configurations wil be
     * the same as the requested project ID parameter.
     * @param userId    The login name for the user who created the routing configurations.
     * @return A list of the routing configurations for the indicated user.
     */
    abstract public Map<String, Map<String, String>> findUserRoutings(String userId);
    /**
     * Finds all active routing configurations for a particular value of a particular attribute. This returns as a map
     * of maps. The containing map is keyed on the study instance UIDs for the study configurations. The contained maps
     * include all of the attributes for each routing configuration. The value for the requested attribute configured
     * for all routing configurations wil be the same as the requested attribute value.
     * @param attribute    The key for the attribute on which you wish to search.
     * @param value        The value of the attribute on which you wish to search.
     * @return A list of the routing configurations for the indicated attribute key and value.
     */
    abstract public Map<String, Map<String, String>> findRoutingsByAttribute(String attribute, String value);
    /**
     * Closes the routing configuration for the indicated study instance UID. This should only be done when the study
     * has been fully received and archived. Any DICOM data for the study received later will not have the routing
     * configuration available any more, which could lead to split sessions.
     * @param studyInstanceUid    The UID of the routing configuration to close.
     */
    abstract public boolean close(String studyInstanceUid);
}
