package org.nrg.xnat.restlet.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.StringUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EventResource extends AutomationResource {

    public EventResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _event = (String) getRequest().getAttributes().get(EVENT);

        if (!request.getMethod().equals(Method.GET)) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "XNAT events are currently read-only: you can only do GET requests.");
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "XNAT events are currently read-only: you can only do GET requests.");
        }

        if (_log.isDebugEnabled()) {
            if (StringUtils.isNotBlank(_event)) {
                _log.debug("Servicing event request for event ID " + _event + " for user " + user.getLogin());
            } else {
                _log.debug("Retrieving available events for user " + user.getLogin());
            }
        }
    }

    @Override
    protected String getResourceType() {
        return "Event";
    }

    @Override
    protected String getResourceId() {
        return _event;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        try {
            if (StringUtils.isNotBlank(_event)) {
                // They're requesting a specific event, so return that to them.
                final XFTTable table = getEventsTable();
                final Map<Object, Object> eventMap = table.convertToHashtable("event_id", "event_name");
                if (!eventMap.containsKey(_event)) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No event of ID " + _event + " was found.");
                } else {
                    final String label = (String) eventMap.get(_event);
                    final Map<String, String> event = new HashMap<>();
                    event.put("event_id", _event);
                    event.put("event_label", label);
                    return new StringRepresentation(MAPPER.writeValueAsString(event), mediaType);
                }
            } else {
                // They're asking for list of existing script events, so give them that.
                final XFTTable table = getEventsTable();
                return representTable(table, mediaType, null);
            }
        } catch (JsonProcessingException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred marshalling the event data to JSON", e);
        } catch (SQLException | DBPoolException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred accessing the database.", e);
        }
    }

    // TODO: Convert this to use a separate events registry service.
    private XFTTable getEventsTable() throws SQLException, DBPoolException {
        final XFTTable table = XFTTable.Execute("SELECT DISTINCT CASE pipeline_name\n" +
                "            WHEN 'Transfer'::text THEN 'Archive'::text\n" +
                "                    ELSE\n" +
                "                                CASE xs_lastposition('/'::text, pipeline_name::text) WHEN 0 THEN pipeline_name ELSE\n" +
                "                                substring(substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1), 1, xs_lastposition('.'::text, substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1)) - 1)\n" +
                "        END END AS event_label, pipeline_name AS event_id FROM wrk_workflowData  WHERE externalid !='ADMIN' AND externalid !='' AND externalid IS NOT NULL;\n", user.getDBName(), userName);

        table.sort("event_label", "ASC");
        return table;
    }

    private static final Logger _log = LoggerFactory.getLogger(EventResource.class);

    private static final String EVENT = "EVENT";
    private final String _event;
}
