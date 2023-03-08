package org.nrg.xnat.services.archive.impl;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.event.methods.AbstractXftItemEventHandlerMethod;
import org.nrg.xft.event.methods.XftItemEventCriteria;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.archive.Rename;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceSurveyReport;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ResourceSurveyXftItemEventHandlerMethod extends AbstractXftItemEventHandlerMethod {
    private static final String PARAM_EXPERIMENT_ID                = "experimentId";
    private static final String QUERY_OPEN_REQUESTS_FOR_EXPERIMENT = "SELECT rsr.id "
                                                                     + "FROM xnat_abstractresource ar "
                                                                     + "         LEFT JOIN xnat_abstractresource_meta_data armd ON ar.abstractresource_info = armd.meta_data_id "
                                                                     + "         LEFT JOIN xhbm_resource_survey_request rsr ON ar.xnat_abstractresource_id = rsr.resource_id "
                                                                     + "         LEFT JOIN xnat_resource r ON ar.xnat_abstractresource_id = r.xnat_abstractresource_id "
                                                                     + "         LEFT JOIN xnat_imagescandata sc ON ar.xnat_imagescandata_xnat_imagescandata_id = sc.xnat_imagescandata_id "
                                                                     + "         LEFT JOIN xnat_experimentdata x ON sc.image_session_id = x.id "
                                                                     + "         LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id "
                                                                     + "         LEFT JOIN xnat_subjectassessordata sa ON x.id = sa.id "
                                                                     + "         LEFT JOIN xnat_subjectdata s ON sa.subject_id = s.id "
                                                                     + "WHERE r.format = 'DICOM' "
                                                                     + "  AND rsr.id IS NOT NULL "
                                                                     + "  AND rsr.closing_date IS NULL "
                                                                     + "  AND x.id = :" + PARAM_EXPERIMENT_ID;

    private final ResourceSurveyRequestEntityService _service;
    private final NamedParameterJdbcTemplate         _template;

    @Autowired
    public ResourceSurveyXftItemEventHandlerMethod(final ResourceSurveyRequestEntityService service, final NamedParameterJdbcTemplate template) {
        super(XftItemEventCriteria.builder().xsiType(XnatImagesessiondata.SCHEMA_ELEMENT_NAME).actions(XftItemEvent.MOVE, EventUtils.RENAME).build());
        _service  = service;
        _template = template;
    }

    @Override
    protected boolean handleEventImpl(final XftItemEventI event) {
        switch (event.getAction()) {
            case EventUtils.RENAME:
                return handleRename(event);
            case XftItemEvent.MOVE:
                return handleMove(event);
            default:
                log.warn("Unknown event action {} for object {} of type {}", event.getAction(), event.getId(), event.getXsiType());
        }
        return true;
    }

    private boolean handleRename(final XftItemEventI event) {
        final String         id             = event.getId();
        final String         xsiType        = event.getXsiType();
        final Map<String, ?> properties     = event.getProperties();
        final String         oldName        = (String) properties.get(Rename.OLD_LABEL);
        final Path           oldSessionPath = (Path) properties.get(Rename.OLD_PATH);
        final String         newName        = (String) properties.get(Rename.NEW_LABEL);
        final Path           newSessionPath = (Path) properties.get(Rename.NEW_PATH);
        log.debug("Got an event for instance {} of XSI type {}: renamed from {} to {}", id, xsiType, oldName, newName);

        return getOpenRequestsForExperiment(xsiType, id, requests -> {
            final Function<File, File> makeChildOfNewPath = FileUtils.fileRootMapper(oldSessionPath, newSessionPath);

            requests.forEach(request -> {
                request.setExperimentLabel(newName);
                request.setResourceUri(makeChildOfNewPath.apply(Paths.get(request.getResourceUri()).toFile()).getAbsolutePath());

                final ResourceSurveyReport existing = request.getSurveyReport();
                if (existing != null) {
                    request.setSurveyReport(ResourceSurveyReport.builder()
                                                                .resourceSurveyRequestId(existing.getResourceSurveyRequestId())
                                                                .surveyDate(existing.getSurveyDate())
                                                                .totalEntries(existing.getTotalEntries())
                                                                .uids(existing.getUids())
                                                                .badFiles(existing.getBadFiles().stream().map(makeChildOfNewPath).collect(Collectors.toList()))
                                                                .mismatchedFiles(existing.getMismatchedFiles().keySet().stream().collect(Collectors.toMap(makeChildOfNewPath, existing.getMismatchedFiles()::get)))
                                                                .duplicates(remapDuplicates(existing.getDuplicates(), makeChildOfNewPath))
                                                                .build());
                }

                _service.update(request);
            });
        });
    }

    private boolean handleMove(final XftItemEventI event) {
        final String         id         = event.getId();
        final String         xsiType    = event.getXsiType();
        final Map<String, ?> properties = event.getProperties();
        final String         origin     = (String) properties.get("origin");
        final String         target     = (String) properties.get("target");
        log.debug("Got an event for instance {} of XSI type {}: moved from project {} to {}", id, xsiType, origin, target);

        return getOpenRequestsForExperiment(xsiType, id, requests -> requests.forEach(request -> {
            request.setProjectId(target);
            request.setRsnStatus(ResourceSurveyRequest.Status.CANCELED);
            log.debug("Updated resource survey request {} on resource {} to set project to {} and status to CANCELED", request.getId(), request.getResourceId(), target);
            _service.update(request);
        }));
    }

    // TODO: This should be replaced by calls to ResourceSurveyRequestEntityService.getOpenRequestsBySessionId()
    private boolean getOpenRequestsForExperiment(final String xsiType, final String id, final Consumer<List<ResourceSurveyRequest>> handler) {
        final List<Long> requestIds = _template.queryForList(QUERY_OPEN_REQUESTS_FOR_EXPERIMENT, new MapSqlParameterSource(PARAM_EXPERIMENT_ID, id), Long.class);
        log.debug("Found {} IDs for outstanding resource survey requests with survey reports for {} instance {}", requestIds.size(), xsiType, id);

        final List<ResourceSurveyRequest> requests;
        try {
            requests = _service.getRequests(requestIds);
        } catch (NotFoundException e) {
            log.error("One or more request IDs not found", e);
            return false;
        }

        if (requests.isEmpty()) {
            log.info("Instance {} of XSI type {} was moved, but there don't appear to be any outstanding requests associated with it.", id, xsiType);
            return true;
        }

        log.debug("Instance {} of XSI type {} was moved and has {} outstanding requests associated with it.", id, xsiType, requests.size());
        handler.accept(requests);
        return true;
    }

    private static Map<String, Map<String, Map<File, String>>> remapDuplicates(final Map<String, Map<String, Map<File, String>>> existingDuplicates, final Function<File, File> makeChildOfNewPath) {
        final Map<String, Map<String, Map<File, String>>> newDuplicates = new HashMap<>();
        for (final String classUid : existingDuplicates.keySet()) {
            final Map<String, Map<File, String>> newInstances = new HashMap<>();
            newDuplicates.put(classUid, newInstances);
            final Map<String, Map<File, String>> existingInstances = existingDuplicates.get(classUid);
            for (final String instanceUid : existingInstances.keySet()) {
                final Map<File, String> existingInstanceDuplicates = existingInstances.get(instanceUid);
                newInstances.put(instanceUid, existingInstanceDuplicates.keySet().stream().collect(Collectors.toMap(makeChildOfNewPath, existingInstanceDuplicates::get)));
            }
        }
        return newDuplicates;
    }
}
