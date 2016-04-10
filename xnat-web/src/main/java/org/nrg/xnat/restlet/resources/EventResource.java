package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.hibernate.HibernateException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.automation.entities.Event;
import org.nrg.automation.entities.ScriptTrigger;
import org.nrg.automation.services.EventService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.XDAT;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class EventResource extends AutomationResource {

    private static final String PROPERTY_EVENT_ID    = "event_id";
    private static final String PROPERTY_EVENT_LABEL = "event_label";

    public EventResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _service = XDAT.getContextService().getBean(EventService.class);
        _scriptTriggerService = XDAT.getContextService().getBean(ScriptTriggerService.class);
        _cascade = isQueryVariableTrue("cascade");

        final String eventId;
        try {
            if (getRequest().getAttributes().containsKey(EVENT_ID)) {
                eventId = URLDecoder.decode((String) getRequest().getAttributes().get(EVENT_ID), "UTF-8");
            } else {
                eventId = null;
            }
        } catch (UnsupportedEncodingException exception) {
            // This is the stupidest exception ever. From the docs:
            //
            // The supplied encoding is used to determine what characters are represented by any consecutive sequences
            // of the form "%xy". Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used.
            // Not doing so may introduce incompatibilities.
            //
            // So in other words you have to specify an encoding then handle the exception if you specify an unsupported
            // encoding, except that the only encoding you should use is "UTF-8" and you should specify that every time.
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Something stupid happened. Sorry about that.", exception);
        }

        getParameters();

        if (!_properties.containsKey(PROPERTY_EVENT_ID) && StringUtils.isNotBlank(eventId)) {
            _properties.setProperty(PROPERTY_EVENT_ID, eventId);
        }

        final Method method = request.getMethod();
        if (method.equals(Method.PUT) || method.equals(Method.DELETE)) {
            if (!Roles.isSiteAdmin(user)) {
                getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Only site administrators can create, update, or delete an event.");
            }
            if (!_properties.containsKey(PROPERTY_EVENT_ID)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "You must specify the event ID parameter on the REST URI when creating, updating, or deleting an event.");
            }
            if (method == Method.PUT && !_properties.containsKey(PROPERTY_EVENT_LABEL)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unable to find event label: no data sent?");
            }
            if (StringUtils.isNotBlank(eventId) && !StringUtils.equals(_properties.getProperty(PROPERTY_EVENT_ID), eventId)) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "The event ID found in the form data or querystring parameters doesn't match the REST URI. Event IDs are immutable and can not be changed.");
            }
        }

        final Status status = getResponse().getStatus();
        if (status.getCode() < Status.CLIENT_ERROR_BAD_REQUEST.getCode()) {
            _log.info("Got status " + status + ", won't process further: " + status.getDescription());
        } else if (_log.isDebugEnabled()) {
            if (StringUtils.isNotBlank(getResourceId())) {
                _log.debug("Servicing event request for event ID " + getResourceId() + " for user " + user.getLogin());
            } else {
                _log.debug("Retrieving available events for user " + user.getLogin());
            }
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    protected String getResourceType() {
        return "Event";
    }

    @Override
    protected String getResourceId() {
        if (_properties.containsKey(PROPERTY_EVENT_ID)) {
            return _properties.getProperty(PROPERTY_EVENT_ID);
        }
        return null;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = overrideVariant(variant);

        try {
            if (StringUtils.isNotBlank(getResourceId())) {
                // They're requesting a specific event, so return that to them.
                final XFTTable table = getEventsTable();
                final Map<Object, Object> eventMap = table.convertToHashtable(PROPERTY_EVENT_ID, PROPERTY_EVENT_LABEL);
                if (!eventMap.containsKey(getResourceId())) {
                    throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No event of ID " + getResourceId() + " was found.");
                } else {
                    final String label = (String) eventMap.get(getResourceId());
                    final Map<String, String> event = new HashMap<>();
                    event.put(PROPERTY_EVENT_ID, getResourceId());
                    event.put(PROPERTY_EVENT_LABEL, label);
                    return new StringRepresentation(getSerializer().toJson(event), mediaType);
                }
            } else {
                // They're asking for list of existing script events, so give them that.
                final XFTTable table = getEventsTable();
                return representTable(table, mediaType, null);
            }
        } catch (IOException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred marshalling the event data to JSON", e);
        } catch (SQLException | DBPoolException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred accessing the database.", e);
        }
    }

    @Override
    public void handlePut() {
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Preparing to PUT event: " + getResourceId());
            }
            putEvent();
        } catch (ClientException e) {
            _log.error("Client error occurred trying to store an event: " + getResourceId(), e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (ServerException e) {
            _log.error("Server error occurred trying to store an event: " + getResourceId(), e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public void handleDelete() {
        try {
            if (_log.isDebugEnabled()) {
                _log.debug("Preparing to DELETE event: " + getResourceId());
            }
            deleteEvent();
        } catch (ClientException e) {
            _log.error("Client error occurred trying to delete an event: " + getResourceId(), e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        } catch (ServerException e) {
            _log.error("Server error occurred trying to delete an event: " + getResourceId(), e);
            getResponse().setStatus(e.getStatus(), e.getMessage());
        }
    }

    private void getParameters() throws ResourceException {
        final Map<String, String> queryParameters = getQueryVariableMap();
        if (queryParameters.size() > 0) {
            _properties.putAll(queryParameters);
        }
        final Method method = getRequest().getMethod();
        if (method != Method.GET) {
            final Representation entity = getRequest().getEntity();
            if (entity.getSize() > 0) {
                final MediaType mediaType = entity.getMediaType();
                if (!mediaType.equals(MediaType.APPLICATION_WWW_FORM) && !mediaType.equals(MediaType.APPLICATION_JSON)) {
                    throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE, "This function currently only supports " + MediaType.APPLICATION_WWW_FORM + " and " + MediaType.APPLICATION_JSON);
                }
                if (mediaType.equals(MediaType.APPLICATION_WWW_FORM)) {
                    try {
                        final List<NameValuePair> formMap = URLEncodedUtils.parse(entity.getText(), DEFAULT_CHARSET);
                        for (final NameValuePair entry : formMap) {
                            _properties.setProperty(entry.getName(), entry.getValue());
                        }
                    } catch (IOException e) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to read the submitted form body.", e);
                    }
                } else {
                    try {
                        final String text = entity.getText();
                        _properties.putAll(getSerializer().deserializeJson(text, Properties.class));
                    } catch (IOException e) {
                        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred processing the script properties", e);
                    }
                }
            }
        }
    }

    private void putEvent() throws ClientException, ServerException {
        final String eventId = _properties.getProperty(PROPERTY_EVENT_ID);
        final String eventLabel = _properties.getProperty(PROPERTY_EVENT_LABEL);

        final Event event;
        final boolean created;
        if (_service.hasEvent(eventId)) {
            event = _service.getByEventId(eventId);
            boolean isDirty = false;
            if (!StringUtils.equals(eventLabel, event.getEventLabel())) {
                isDirty = true;
                event.setEventLabel(eventLabel);
            }
            if (isDirty) {
                _service.update(event);
            }
            created = false;
        } else {
            event = _service.create(getResourceId(), eventLabel);
            created = true;
        }

        recordAutomationEvent(event.getEventId(), SITE_SCOPE, created ? "Create" : "Update", Event.class);
    }

    private void deleteEvent() throws ClientException, ServerException {
        final String eventId = getResourceId();
        if (!_service.hasEvent(eventId)) {
            throw new ClientException(Status.CLIENT_ERROR_NOT_FOUND, "Couldn't find an event with the event ID " + eventId);
        }

        final List<ScriptTrigger> triggers = _scriptTriggerService.getByEvent(eventId);
        if (triggers != null && triggers.size() > 0) {
            if (!_cascade) {
                throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "There are " + triggers.size() + " event handlers that reference the indicated event ID " + eventId + ". Please delete these triggers directly or call this method with the \"cascade=true\" query parameter: " + getRequest().getResourceRef().toString() + "?cascade=true");
            }
            for (final ScriptTrigger trigger : triggers) {
                _log.info("Deleting script trigger: " + trigger.getTriggerId());
                _scriptTriggerService.delete(trigger);
            }
        }
        try {
            _service.delete(_service.getByEventId(eventId));
            recordAutomationEvent(eventId, SITE_SCOPE, "Delete", Event.class);
        } catch (HibernateException e) {
            throw new ServerException(Status.SERVER_ERROR_INTERNAL, "An error occurred trying to delete the event " + eventId);
        }
    }

    private XFTTable getEventsTable() throws SQLException, DBPoolException {
        final EventService eventService = XDAT.getContextService().getBean(EventService.class);
        final List<Event> events = eventService.getAll();
        final XFTTable table = new XFTTable();
        table.initTable(new String[] { PROPERTY_EVENT_LABEL, PROPERTY_EVENT_ID });

        for (final Event event : events) {
            table.insertRow(new String[] { event.getEventLabel(), event.getEventId() });
        }

        table.sort(PROPERTY_EVENT_LABEL, "ASC");
        return table;
    }

    private static final Logger _log = LoggerFactory.getLogger(EventResource.class);
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final String EVENT_ID = "EVENT_ID";

    private final EventService _service;
    private final ScriptTriggerService _scriptTriggerService;
    private final Properties _properties = new Properties();
    private final boolean _cascade;
}
