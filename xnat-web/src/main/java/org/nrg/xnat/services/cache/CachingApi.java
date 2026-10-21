package org.nrg.xnat.services.cache;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import jakarta.validation.constraints.NotNull;
import org.nrg.framework.annotations.XapiRestController;
import org.nrg.framework.jcache.JCacheHelper;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.AbstractXapiRestController;
import org.nrg.xapi.rest.XapiRequestMapping;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.UserManagementServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Api("XNAT Caching Management API")
@XapiRestController
@RequestMapping(value = "/caching")
@Slf4j
public class CachingApi extends AbstractXapiRestController {
    private final JCacheHelper _jCacheHelper;

    @Autowired
    public CachingApi(final JCacheHelper jCacheHelper, final UserManagementServiceI userManagementService, final RoleHolder roleHolder) {
        super(userManagementService, roleHolder);
        _jCacheHelper = jCacheHelper;
    }

    @ApiOperation(value = "Returns a list of all of the existing caches on the system.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT cache names successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Must be an administrator to view XNAT caches."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Admin)
    public List<String> getAllCacheNames() {
        return _jCacheHelper.getCacheNames();
    }

    @ApiOperation(value = "Returns the configuration for the specified cache.", notes = "Throws NotFoundException if a cache with the specified name doesn't exist.", response = CompleteConfiguration.class)
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT cache configuration successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Must be an administrator to view XNAT caches."),
                   @ApiResponse(code = 404, message = "No cache with the specified name exists."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{cacheName}/info", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Admin)
    public CompleteConfiguration<?, ?> getCacheInfo(final @PathVariable String cacheName) throws NotFoundException {
        final CompleteConfiguration<?, ?> configuration = _jCacheHelper.getCacheConfiguration(cacheName);
        if (configuration == null) {
            throw new NotFoundException("No cache with name " + cacheName + " exists");
        }
        return configuration;
    }

    @ApiOperation(value = "Returns a list of the keys in the specified cache.", notes = "Throws NotFoundException if a cache with the specified name doesn't exist.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT cache keys successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Must be an administrator to view XNAT caches."),
                   @ApiResponse(code = 404, message = "No cache with the specified name exists."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{cacheName}/keys", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Admin)
    public List<String> getCacheKeys(final @PathVariable String cacheName) throws NotFoundException {
        return StreamSupport.stream(getCache(cacheName).spliterator(), false).map(entry -> entry.getKey().toString()).collect(Collectors.toList());
    }

    @ApiOperation(value = "Returns a list of the keys in the specified cache.", notes = "Throws NotFoundException if a cache with the specified name doesn't exist.", response = String.class, responseContainer = "List")
    @ApiResponses({@ApiResponse(code = 200, message = "XNAT cache keys successfully retrieved."),
                   @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
                   @ApiResponse(code = 403, message = "Must be an administrator to view XNAT caches."),
                   @ApiResponse(code = 404, message = "No cache with the specified name exists."),
                   @ApiResponse(code = 500, message = "Unexpected error")})
    @XapiRequestMapping(value = "{cacheName}/keys/{key}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET, restrictTo = AccessLevel.Admin)
    public Object getCacheValue(final @PathVariable String cacheName, final @PathVariable String key) throws NotFoundException, DataFormatException {
        final Pair<Class<?>, Class<?>> types = _jCacheHelper.getCacheTypes(cacheName).orElseThrow(() -> new NotFoundException("No cache with name " + cacheName + " exists"));
        if (!String.class.isAssignableFrom(types.getKey())) {
            throw new DataFormatException("This method only works with caches that use strings for keys, not " + types.getKey().getName());
        }
        final Cache<String, ?> cache = _jCacheHelper.getCache(cacheName, String.class, types.getValue());
        return cache.get(key);
    }

    @NotNull
    private Cache<?, ?> getCache(final String cacheName) throws NotFoundException {
        final Cache<?, ?> cache = _jCacheHelper.getCache(cacheName);
        if (cache == null) {
            throw new NotFoundException("No cache with name " + cacheName + " exists");
        }
        return cache;
    }
}