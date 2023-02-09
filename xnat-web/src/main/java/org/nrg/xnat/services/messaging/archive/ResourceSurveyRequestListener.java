package org.nrg.xnat.services.messaging.archive;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.messaging.JmsRequestListener;
import org.nrg.xapi.exceptions.ConflictedStateException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.ResourceSurveyRequestEntityService;
import org.nrg.xnat.services.archive.ResourceSurveyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ResourceSurveyRequestListener implements JmsRequestListener<ResourceSurveyRequest> {
    private final ResourceSurveyService              _surveyService;
    private final ResourceSurveyRequestEntityService _entityService;

    @Autowired
    public ResourceSurveyRequestListener(final ResourceSurveyService surveyService, final ResourceSurveyRequestEntityService entityService) {
        log.debug("Starting the resource survey request JMS listener");
        _surveyService = surveyService;
        _entityService = entityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JmsListener(id = ResourceSurveyRequest.BEAN_NAME, destination = ResourceSurveyRequest.BEAN_NAME)
    public void onRequest(final ResourceSurveyRequest request) {
        log.info("Now handling resource survey request {} for resource {} for user {}, current status is: {}", request.getId(), request.getResourceId(), request.getRequester(), request.getRsnStatus());
        try {
            switch (request.getRsnStatus()) {
                case QUEUED_FOR_SURVEY:
                    log.debug("Requesting service to survey for resource survey request {} for resource {} for user {}", request.getId(), request.getResourceId(), request.getRequester());
                    final ResourceSurveyRequest processed = _surveyService.surveyResource(request);
                    log.debug("Completed survey with resource survey request {} for resource {} for user {}, status is now {}", processed.getId(), processed.getResourceId(), processed.getRequester(), processed.getRsnStatus());
                    break;
                case QUEUED_FOR_MITIGATION:
                    log.debug("Requesting service to mitigate for resource survey request {} for resource {} for user {}", request.getId(), request.getResourceId(), request.getRequester());
                    _surveyService.mitigateResource(request);
                    break;
                default:
                    log.warn("User {} requested action on resource survey request {} for resource {} with status {} but I don't know what to do with a request in that status.", request.getRequester(), request.getId(), request.getResourceId(), request.getRsnStatus());
            }
        } catch (InsufficientPrivilegesException | NotFoundException | ConflictedStateException |
                 InitializationException e) {
            handleError(e, request);
        }
    }

    private void handleError(final Exception exception, final ResourceSurveyRequest request) {
        if (exception instanceof InsufficientPrivilegesException) {
            log.error("Tried to handle resource survey request {} for resource {} with status {} but the referenced user \"{}\" doesn't have sufficient privileges on the project {}", request.getId(), request.getResourceId(), request.getRsnStatus(), request.getRequester(), request.getProjectId());
        } else if (exception instanceof NotFoundException) {
            log.error("Tried to handle resource survey request {} for resource {} with status {} but couldn't find something: {}", request.getId(), request.getResourceId(), request.getRsnStatus(), exception.getMessage());
        } else if (exception instanceof ConflictedStateException) {
            log.error("Tried to handle resource survey request {} for resource {} with status {} but found a conflicted state: {}", request.getId(), request.getResourceId(), request.getRsnStatus(), exception.getMessage());
        } else if (exception instanceof InitializationException) {
            log.error("Tried to handle resource survey request {} for resource {} with status {} but encountered an error trying to initialize the task: {}", request.getId(), request.getResourceId(), request.getRsnStatus(), exception.getMessage());
        }
        request.setRsnStatus(ResourceSurveyRequest.Status.ERROR);
        _entityService.update(request);
    }
}
