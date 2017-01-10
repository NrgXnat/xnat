/*
 * web: org.nrg.xnat.restlet.services.RefreshCatalog
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.services.archive.CatalogService;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import java.util.Arrays;
import java.util.List;

public class RefreshCatalog extends SecureResource {

    public RefreshCatalog(Context context, Request request, Response response) {
        super(context, request, response);
        _catalogService = XDAT.getContextService().getBean(CatalogService.class);
    }

    @Override
    public boolean allowGet() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    public void handleParam(final String key, final Object value) throws ClientException {
        if (value != null) {
            switch (key) {
                case "resource":
                    _resources.add((String) value);
                    break;
                case "append":
                    if (isQueryVariableTrueHelper(value)) {
                        _operations.add(CatalogService.Operation.Append);
                    }
                    break;
                case "checksum":
                    if (isQueryVariableTrueHelper(value)) {
                        _operations.add(CatalogService.Operation.Checksum);
                    }
                    break;
                case "delete":
                    if (isQueryVariableTrueHelper(value)) {
                        _operations.add(CatalogService.Operation.Delete);
                    }
                    break;
                case "populateStats":
                    if (isQueryVariableTrueHelper(value)) {
                        _operations.add(CatalogService.Operation.PopulateStats);
                    }
                    break;
                case "options":
                    List<String> options = Arrays.asList(((String) value).split(","));
                    if (options.contains("append")) {
                        _operations.add(CatalogService.Operation.Append);
                    }
                    if (options.contains("checksum")) {
                        _operations.add(CatalogService.Operation.Checksum);
                    }
                    if (options.contains("delete")) {
                        _operations.add(CatalogService.Operation.Delete);
                    }
                    if (options.contains("populateStats")) {
                        _operations.add(CatalogService.Operation.PopulateStats);
                    }
                    break;
                default:
                    _otherParams.put(key, value);
                    break;
            }
        }
        if (_operations.contains(CatalogService.Operation.All)) {
            _operations.clear();
            _operations.addAll(CatalogService.Operation.ALL);
        }
    }

    @Override
    public void handlePost() {
        try {
            final Representation entity = this.getRequest().getEntity();

            //parse body to identify resources if its multi-part form data
            //TODO: Handle JSON body.
            if (entity.isAvailable() && RequestUtil.isMultiPartFormData(entity)) {
                loadParams(new Form(entity));
            }
            loadQueryVariables();//parse query string to identify resources

            _catalogService.refreshResourceCatalogs(getUser(), _resources, _operations.toArray(new CatalogService.Operation[_operations.size()]));

            this.getResponse().setStatus(Status.SUCCESS_OK);
        } catch (ActionException e) {
            this.getResponse().setStatus(e.getStatus(), e.getMessage());
            logger.error("", e);
        } catch (Exception e) {
            this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            logger.error("", e);
        }
    }

    private final CatalogService _catalogService;

    private final List<String>                   _resources   = Lists.newArrayList();
    private final ListMultimap<String, Object>   _otherParams = ArrayListMultimap.create();
    private final List<CatalogService.Operation> _operations  = Lists.newArrayList();
}
