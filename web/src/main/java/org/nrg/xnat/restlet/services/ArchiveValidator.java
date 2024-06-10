/*
 * web: org.nrg.xnat.restlet.services.ArchiveValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.services;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.XFTTable;
import org.nrg.xnat.archive.PrearcSessionValidator;
import org.nrg.xnat.archive.PrearcSessionValidator.Notice;
import org.nrg.xnat.helpers.prearchive.PrearcSession;
import org.nrg.xnat.helpers.prearchive.PrearcUtils;
import org.nrg.xnat.restlet.services.prearchive.BatchPrearchiveActionsA;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@Getter(PROTECTED)
@Setter(PROTECTED)
@Accessors(prefix = "_")
@Slf4j
public class ArchiveValidator extends BatchPrearchiveActionsA {
    public ArchiveValidator(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    public void handleParam(final String key, final Object value) {
        if (value != null) {
            switch (key) {
                case PROJECT:
                    getAdditionalValues().put("project", value);
                    break;
                case PrearcUtils.PREARC_TIMESTAMP:
                    setTimestamp((String) value);
                    break;
                case PrearcUtils.PREARC_SESSION_FOLDER:
                    getSessionFolder().add((String) value);
                    break;
                case DEST:
                    setDestination((String) value);
                    break;
                case SRC:
                    super.handleParam(key, value);
                    break;
                default:
                    getAdditionalValues().put(key, value);
                    break;
            }
        }
    }

    protected void finishSingleSessionArchive(final PrearcSession session) throws Exception {
        final PrearcSessionValidator validator  = new PrearcSessionValidator(session, getUser(), getAdditionalValues());
        final List<? extends Notice> validation = validator.validate();

        Collections.sort(validation);

        final XFTTable table = new XFTTable();
        table.initTable(COLUMNS);
        for (final Notice notice : validation) {
            table.rows().add(new Object[]{notice.getCode(), notice.getType(), notice.getMessage()});
        }

        getResponse().setEntity(representTable(table, overrideVariant(getPreferredVariant()), new Hashtable<String, Object>()));
    }

    protected void finishNonSingleSessionUpload(final List<PrearcSession> sessions) throws Exception {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot validate multiple sessions in one request.");
    }

    private static final String   PROJECT = "project";
    private static final String   DEST    = "dest";
    private static final String[] COLUMNS = {"code", "type", "message"};
}
