package org.nrg.xnat.tracking.xapi;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xnat.tracking.entities.EventTrackingDataPojo;
import org.nrg.xnat.tracking.services.EventTrackingDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api("The XNAT Event Tracking Data API")
@XapiRestController
@RequestMapping(value = "event_tracking")
@Slf4j
public class EventTrackingDataApi extends AbstractXapiRestController {
    private final EventTrackingDataService eventTrackingDataService;

    @Autowired
    public EventTrackingDataApi(final EventTrackingDataService eventTrackingDataService,
                                final UserManagementServiceI userManagementService,
                                final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        this.eventTrackingDataService = eventTrackingDataService;
    }

    @XapiRequestMapping(value = {"{key}/payload"}, produces = {MediaType.TEXT_PLAIN_VALUE}, method = RequestMethod.GET)
    public ResponseEntity<String> getPayload(@PathVariable final String key) {
        try {
            return new ResponseEntity<>(eventTrackingDataService.getPayloadByKey(key), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Throwable t) {
            log.error("Issue getting payload for key {}", key, t);
            return new ResponseEntity<>(t.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @XapiRequestMapping(value = {"{key}"}, produces = {MediaType.APPLICATION_JSON_VALUE}, method = RequestMethod.GET)
    public ResponseEntity<EventTrackingDataPojo> getData(@PathVariable final String key) {
        try {
            return new ResponseEntity<>(eventTrackingDataService.getPojoByKey(key), HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Throwable t) {
            log.error("Issue getting POJO for key {}", key, t);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
