/*
 * web: org.nrg.xnat.restlet.extensions.DicomSCPRestlet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.extensions;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dcm.DicomSCPManager;
import org.nrg.dcm.exceptions.EnabledDICOMReceiverWithDuplicatePortException;
import org.nrg.dcm.preferences.DicomSCPInstance;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XnatRestlet({"/services/dicomscp", "/services/dicomscp/instance/{SCP_ID}", "/services/dicomscp/instance/{SCP_ID}/{ACTION}", "/services/dicomscp/{ACTION}"})
public class DicomSCPRestlet extends SecureResource {
    private static final String       PARAM_SCP_ID    = "SCP_ID";
    private static final String       PARAM_ACTION    = "ACTION";
    private static final List<String> ALLOWED_ACTIONS = new ArrayList<>(Arrays.asList("status", "start", "stop", "enable", "disable"));
    private static final Logger       _log            = LoggerFactory.getLogger(DicomSCPRestlet.class);

    private final DicomSCPManager _dicomSCPManager;
    private final Integer         _scpId;
    private final String          _action;

    public DicomSCPRestlet(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.ALL));

        _dicomSCPManager = XDAT.getContextService().getBean(DicomSCPManager.class);
        if (null == _dicomSCPManager) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "DicomSCP manager was not properly initialized.");
            throw new ResourceException(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "ERROR: DicomSCP manager was not properly initialized.");
        }

        final String scpId = (String) getRequest().getAttributes().get(PARAM_SCP_ID);
        _scpId = StringUtils.isBlank(scpId) ? null : Integer.parseInt(scpId);
        if (_scpId != null && !_dicomSCPManager.hasDicomSCP(_scpId)) {
            final String message = String.format("DICOM SCP instance '%s' not found in configuration.", _scpId);
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, message);
        }

        _action = (String) getRequest().getAttributes().get(PARAM_ACTION);
        if (StringUtils.isNotBlank(_action) && !ALLOWED_ACTIONS.contains(_action)) {
            final String message = String.format("Action '%s' is not supported by this resource.  Valid actions are: %s", _action, Joiner.on(",").join(ALLOWED_ACTIONS));
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, message);
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (_log.isDebugEnabled()) {
            _log.debug("Getting the status of {}.", (_scpId == null ? "all configured DICOM SCP receivers" : "the DICOM SCP receiver with ID " + _scpId));
        }

        // If this is a GET represent and not just a return from a PUT or other call, the only action we support is status.
        if (Method.GET.equals(getRequest().getMethod()) && StringUtils.isNotBlank(_action) && !_action.equalsIgnoreCase("status")) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "You can't specify any action other than status with a GET request.");
        }

        try {
            if (Method.DELETE.equals(getRequest().getMethod()) || (StringUtils.isNotBlank(_action) && _action.equalsIgnoreCase("status"))) {
                return new StringRepresentation(getSerializer().toJson(_dicomSCPManager.areDicomSCPsStarted()));
            } else if (_scpId == null) {
                return new StringRepresentation(getSerializer().toJson(_dicomSCPManager.getDicomSCPInstances()));
            } else {
                return new StringRepresentation(getSerializer().toJson(_dicomSCPManager.getDicomSCPInstance(_scpId)));
            }
        } catch (IOException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred marshaling the DICOM SCP statuses", e);
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public void handlePost() {
        if (StringUtils.isNotBlank(_action)) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You should only POST to this URL to create a new DICOM SCP instance. The action " + _action + " is invalid in this context.");
        } else if (_scpId != null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You should only POST to this URL to create a new DICOM SCP instance. The DICOM SCP ID " + _scpId + " is invalid in this context.");
        }
        try {
            final String           serialized = getRequest().getEntity().getText();
            final DicomSCPInstance instance   = getSerializer().deserializeJson(serialized, DicomSCPInstance.class);
            _dicomSCPManager.create(instance);
        } catch (IOException e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "An error occurred trying to retrieve the body text of the PUT request.");
        } catch (NrgServiceException e) {
            if (e.getServiceError() == NrgServiceError.ConfigurationError) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e, "There was an error creating the DICOM SCP instance, probably an existing ID");
            } else {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "An unknown service error occurred trying to create the DICOM SCP instance.");
            }
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void handlePut() {
        if (StringUtils.isBlank(_action)) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify an action to perform.");
        } else if (_action.equalsIgnoreCase("start")) {
            if (_scpId != null) {
                _dicomSCPManager.startDicomSCP(_scpId);
            } else {
                _dicomSCPManager.startDicomSCPs();
            }
            returnDefaultRepresentation();
        } else if (_action.equalsIgnoreCase("stop")) {
            if (_scpId != null) {
                _dicomSCPManager.stopDicomSCP(_scpId);
            } else {
                _dicomSCPManager.stopDicomSCPs();
            }
            returnDefaultRepresentation();
        } else if (_action.equalsIgnoreCase("enable")) {
            if (_scpId == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a specific DICOM SCP instance to enable.");
            } else {
                try {
                    _dicomSCPManager.enableDicomSCP(_scpId);
                } catch (EnabledDICOMReceiverWithDuplicatePortException e) {
                    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "There is already another DICOM SCP instance enabled with the same port: " + e.getExisting().toString());
                }
                returnDefaultRepresentation();
            }
        } else if (_action.equalsIgnoreCase("disable")) {
            if (_scpId == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a specific DICOM SCP instance to disable.");
            } else {
                _dicomSCPManager.disableDicomSCP(_scpId);
                returnDefaultRepresentation();
            }
        } else {
            _dicomSCPManager.startDicomSCPs();
            returnDefaultRepresentation();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public void handleDelete() {
        if (_scpId == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify a specific DICOM SCP instance to delete.");
        } else {
            try {
                _dicomSCPManager.delete(_scpId);
                returnDefaultRepresentation();
            } catch (NrgServiceException e) {
                if (e.getServiceError() == NrgServiceError.UnknownEntity) {
                    getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "There is no DICOM SCP instance with the ID " + _scpId);
                } else {
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "An unknown service error occurred trying to delete the DICOM SCP instance.");
                }
            }
        }
    }
}
