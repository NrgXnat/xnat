package org.nrg.xnat.restlet.resources;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class WorkflowEventResource extends AutomationResource {

    public WorkflowEventResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        _spec = (String) getRequest().getAttributes().get(SPEC);

        if (_log.isDebugEnabled()) {
            if (StringUtils.isNotBlank(_spec)) {
                _log.debug("Servicing event request for workflow event " + _spec + " for user " + user.getLogin());
            } else {
                _log.debug("Retrieving available workflow events for user " + user.getLogin());
            }
        }
    }

    @Override
    protected String getResourceType() {
        return "Workflow";
    }

    @Override
    protected String getResourceId() {
        return _spec;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        try {
            // They're asking for list of existing script events, so give them that.
            final XFTTable table = getEventsTable();
            return representTable(table, overrideVariant(variant), null);
        } catch (SQLException | DBPoolException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "An error occurred accessing the database.", e);
        }
    }

    private XFTTable getEventsTable() throws SQLException, DBPoolException {
        final String workflowQuery = XDAT.getContextService().getBean("populateEventsQuery", String.class);
        final String eventSpecCriteria = XDAT.getContextService().getBean("eventSpecCriteria", String.class);
        final String query = workflowQuery + (StringUtils.isBlank(_spec) ? "" : String.format(eventSpecCriteria, _spec));
        final XFTTable table = XFTTable.Execute(query, user.getDBName(), userName);
        table.sort("event_label", "ASC");
        return table;
    }

    private static final Logger _log = LoggerFactory.getLogger(WorkflowEventResource.class);
    private static final String SPEC = "SPEC";
    private final String _spec;
}
