/*
 * web: org.nrg.xnat.restlet.transaction.monitor.SQListenerRepresentation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.transaction.monitor;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.action.InvalidParamsException;
import org.nrg.framework.status.StatusMessage;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xnat.helpers.transactions.HTTPSessionStatusManagerQueue;
import org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI;
import org.nrg.xnat.restlet.representations.JSONObjectRepresentation;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.status.StatusList;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import static org.restlet.data.MediaType.*;

@Slf4j
public class SQListenerRepresentation extends SecureResource {
    public SQListenerRepresentation(Context context, Request request, Response response) {
        super(context, request, response);

        _transactionId = (String) getParameter(request, "TRANSACTION_ID");

        getVariants().addAll(Arrays.asList(new Variant(TEXT_PLAIN), new Variant(MediaType.TEXT_HTML)));
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(final Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        try {
            final StatusList statusList = retrieveStatusQueue();
            log.trace("Status: {}", statusList);

            if (mediaType.equals(APPLICATION_JSON)) {
                return new JSONObjectRepresentation(APPLICATION_JSON, buildJSONObject(statusList));
            }
            if (mediaType.equals(TEXT_PLAIN)) {
                return new StringRepresentation(statusList.toString(), TEXT_PLAIN);
            }
            return new HTMLStatusListRepresentation(TEXT_XML, statusList);
        } catch (JSONException e) {
            throw new ResourceException(e);
        }
    }

    @Override
    public void acceptRepresentation(final Representation entity) throws ResourceException {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") final Form form = new Form(entity);

        try {
            final StatusMessage message = buildMessage(buildStatus(TurbineUtils.escapeParam(form.getFirstValue("status"))), TurbineUtils.escapeParam(form.getFirstValue("message")));
            retrieveStatusQueue().notify(message);
        } catch (InvalidParamsException e) {
            log.error("Invalid params passed", e);
            throw new ResourceException(e);
        }
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        try {
            retrieveSQManager().deleteStatusQueue(_transactionId);
        } catch (IllegalArgumentException e) {
            throw new ResourceException(e);
        }
    }

    private StatusList retrieveStatusQueue() {
        final PersistentStatusQueueManagerI manager    = retrieveSQManager();
        final StatusList                    statusList = manager.retrieveStatusQueue(_transactionId);
        if (statusList != null) {
            return statusList;
        }
        return manager.storeStatusQueue(_transactionId, new StatusList());
    }

    private StatusMessage.Status buildStatus(final String value) throws InvalidParamsException {
        try {
            return Enum.valueOf(StatusMessage.Status.class, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidParamsException("Status", value + " is not a valid Status.");
        }
    }

    private StatusMessage buildMessage(final StatusMessage.Status status, final String message) {
        return new StatusMessage(_transactionId, status, message);
    }

    private JSONObject buildJSONObject(final StatusList statusList) throws JSONException {
        final JSONObject json = new JSONObject();

        final JSONArray messages = new JSONArray();
        json.append("msgs", messages);

        for (final StatusMessage message : statusList.getMessages()) {
            final JSONObject element = new JSONObject();
            element.put("status", message.getStatus());
            element.put("msg", message.getMessage());
            messages.put(element);
        }

        return json;
    }

    private PersistentStatusQueueManagerI retrieveSQManager() {
        return new HTTPSessionStatusManagerQueue(this.getHttpSession());
    }

    class HTMLStatusListRepresentation extends OutputRepresentation {
        HTMLStatusListRepresentation(final MediaType mediaType, final StatusList statusList) {
            super(mediaType);
            _statusList = statusList;
        }

        @Override
        public void write(final OutputStream outputStream) throws IOException {
            try (final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
                writer.write("<html><body><table>");

                for (final StatusMessage message : _statusList.getMessages()) {
                    writer.write("<tr><td class='s" + message.getStatus().toString() + "'>");
                    writer.write(message.getStatus().toString());
                    writer.write("</td><td>");
                    writer.write(message.getMessage());
                    writer.write("</td></tr>");
                    writer.newLine();
                }

                writer.write("</table></body></html>");
            }
        }

        private final StatusList _statusList;
    }

    private final String _transactionId;
}
