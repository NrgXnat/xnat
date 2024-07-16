package org.nrg.xnat.micrometer.api;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.StatelessApi;
import org.nrg.framework.annotations.XapiRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.nrg.framework.annotations.StatelessApi;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Api("Observability and Metrics API")
@XapiRestController
@RequestMapping(value = "/metrics")
@Slf4j
@StatelessApi
public class MetricsApi extends AbstractXapiRestController {

    @Autowired
    public MetricsApi(final UserManagementServiceI userManagementService, final RoleHolder roleHolder,
                         final MeterRegistry meterRegistry) {
        super(userManagementService, roleHolder);
        this.meterRegistry = meterRegistry;
    }

    @ApiOperation(value = "Get observable metrics")
    @ApiResponses({@ApiResponse(code = 200, message = "Metrics"),
            @ApiResponse(code = 500, message = "An unexpected error occurred."),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 403, message = "Cannot access data item."),
            @ApiResponse(code = 404, message = "Item not found.")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = GET)
    public ResponseEntity<String>   getMetrics() {
            if (meterRegistry instanceof CompositeMeterRegistry) {
                if (((CompositeMeterRegistry)meterRegistry).getRegistries().size() == 0) {
                    return new ResponseEntity<>(METRICS_NOT_ENABLED,HttpStatus.OK);
                }
            } else if (meterRegistry instanceof PrometheusMeterRegistry) {
              return new ResponseEntity<>(((PrometheusMeterRegistry)meterRegistry).scrape(), HttpStatus.OK);
            }
        return new ResponseEntity<>(METRICS_NOT_AVAILABLE,HttpStatus.OK);
    }

    private final String METRICS_NOT_AVAILABLE = "[\"Metrics are not available on this instance.\"]";
    private final String METRICS_NOT_ENABLED = "[\"Metrics gathering has not been enabled on this instance.\"]";
    private final MeterRegistry meterRegistry;
}