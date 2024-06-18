/*
 * core: org.nrg.xdat.services.StudyRoutingService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services;

import org.nrg.framework.orm.hibernate.BaseHibernateEntity;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xdat.entities.StudyRouting;

import java.util.List;
import java.util.Map;

/**
 * Manages study routing configurations. Routing configurations are stored and accessed by the study instance UID of the
 * relevant DICOM study. Routing configurations determine the project, subject, or label applied to the study. If no
 * routing configuration can be found for a particular study, it's up to the object identifier to determine a fall-back
 * or default strategy.
 */
public interface StudyRoutingService extends BaseHibernateService<StudyRouting> {
    String PROJECT  = "project";
    String SUBJECT  = "subject";
    String LABEL    = "label";
    String USER     = "user";
    String CREATED  = "created";
    String ACCESSED = "accessed";

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link
     * BaseHibernateService#update(BaseHibernateEntity)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param userId           The login name for the user who created the routing configurations.
     *
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    boolean assign(String studyInstanceUid, String projectId, String userId);

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link
     * BaseHibernateService#update(BaseHibernateEntity)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param subjectId        The ID of the destination subject.
     * @param userId           The login name for the user who created the routing configurations.
     *
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    boolean assign(String studyInstanceUid, String projectId, String subjectId, String userId);

    /**
     * Creates a new routing configuration for the indicated attributes.
     * <p>
     * <b>Note:</b> Once a routing configuration has been created for a particular study instance UID, further calls to
     * any of the <b>assign()</b> methods will not change that existing routing configuration. In that case, the return
     * value will be <b>false</b>. To change an existing routing configuration, use the {@link
     * BaseHibernateService#update(BaseHibernateEntity)} method.
     *
     * @param studyInstanceUid The study instance UID to be assigned.
     * @param projectId        The ID of the destination project.
     * @param subjectId        The ID of the destination subject.
     * @param label            The label for the session.
     * @param userId           The login name for the user who created the routing configurations.
     *
     * @return This method returns <b>true</b> when the configuration was successfully created, <b>false</b> otherwise.
     *         The <b>false</b> return value usually indicates that a routing configuration for the submitted study
     *         instance UID already exists.
     */
    boolean assign(String studyInstanceUid, String projectId, String subjectId, String label, String userId);

    /**
     * Finds the active routing configuration for the indicated study instance UID. If there is no active routing
     * configuration for that study instance UID, this method returns <b>null</b>.
     *
     * @param studyInstanceUid The study instance UID to query.
     *
     * @return The active routing configuration, if any, for the indicated study instance UID. Returns <b>null</b> if
     *         there is no active routing configuration for that study.
     */
    StudyRouting getStudyRouting(String studyInstanceUid);

    /**
     * Finds all active routing configurations for a particular project.
     *
     * @param projectId The ID of the destination project.
     *
     * @return A list of the routing configurations for the indicated project.
     */
    List<StudyRouting> getProjectRoutings(String projectId);

    /**
     * Gets all active routing configurations created by a particular user.
     *
     * @param userId The login name for the user who created the routing configurations.
     *
     * @return A list of the routing configurations for the indicated user.
     */
    List<StudyRouting> getUserRoutings(String userId);

    /**
     * Finds the active routing configuration for the indicated study instance UID. If there is no active routing
     * configuration for that study instance UID, this method returns <b>null</b>.
     *
     * @param studyInstanceUid The study instance UID to query.
     *
     * @return The active routing configuration, if any, for the indicated study instance UID. Returns <b>null</b> if
     *         there is no active routing configuration for that study.
     */
    Map<String, String> findStudyRouting(String studyInstanceUid);

    /**
     * Finds all active routing configurations for a particular project. This returns as a map of maps. The containing
     * map is keyed on the study instance UIDs for the study configurations. The contained maps include all the
     * attributes for each routing configuration. The project ID configured for all routing configurations wil be the
     * same as the requested project ID parameter.
     *
     * @param projectId The ID of the destination project.
     *
     * @return A map of the routing configurations for the indicated project.
     */
    Map<String, Map<String, String>> findProjectRoutings(String projectId);

    /**
     * Finds all active routing configurations created by a particular user. This returns as a map of maps. The
     * containing map is keyed on the study instance UIDs for the routing configurations. The contained maps include all
     * the attributes for each routing configuration. The user ID configured for all routing configurations will be the
     * same as the requested project ID parameter.
     *
     * @param userId The login name for the user who created the routing configurations.
     *
     * @return A map of the routing configurations for the indicated user.
     */
    Map<String, Map<String, String>> findUserRoutings(String userId);

    /**
     * Finds all active routing configurations.
     *
     * @return A map of the routing configurations currently in the system.
     */
    Map<String, Map<String, String>> findAllRoutings();

    /**
     * Closes the routing configuration for the indicated study instance UID. This should only be done when the study
     * has been fully received and archived. Any DICOM data for the study received later will not have the routing
     * configuration available anymore, which could lead to split sessions.
     *
     * @param studyInstanceUid The UID of the routing configuration to close.
     */
    boolean close(String studyInstanceUid);

    /**
     * Closes all routing configurations on the site. This is a pretty extreme step and can only be performed by site
     * administrators.
     */
    void closeAll();
}
