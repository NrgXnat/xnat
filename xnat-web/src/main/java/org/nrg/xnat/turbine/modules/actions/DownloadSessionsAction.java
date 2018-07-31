/*
 * web: org.nrg.xnat.turbine.modules.actions.DownloadSessionsAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.services.pull.tools.TemplateLink;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplayFieldWrapper;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.modules.actions.ListingAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTable;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@SuppressWarnings("unused")
public class DownloadSessionsAction extends ListingAction {
    public DownloadSessionsAction() {
        _template = XDAT.getContextService().getBeanSafely(NamedParameterJdbcTemplate.class);
    }

    @Override
    public String getDestinationScreenName(final RunData data) {
        return "XDATScreen_download_sessions.vm";
    }

    public void finalProcessing(final RunData data, final Context context) throws Exception {
        final DisplaySearch search = TurbineUtils.getSearch(data);
        if (search == null) {
            data.setMessage("No search criteria were specified. <a href=\"" + ((TemplateLink) context.get("link")).getLink() + "\">Try your search again.</a>");
            data.setScreenTemplate("Error.vm");
            return;
        }

        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            throw new Exception("Invalid User.");
        }

        search.setPagingOn(false);

        // Load search results into a table
        final XFTTable table = (XFTTable) search.execute(null, user.getUsername());
        search.setPagingOn(true);

        // Acceptable display field ids for the session ID
        final String sessionIdHeader = getSessionIdHeader(table);

        if (StringUtils.isBlank(sessionIdHeader)) {
            log.error("Missing expected display field for " + search.getRootElement().getFullXMLName() + " download feature (SESSION_ID, EXPT_ID, or ID)");
            throw new Exception("Missing expected ID display field.");
        }

        table.resetRowCursor();
        while (table.hasMoreRows()) {
            data.getParameters().add("sessions", (String) table.nextRowHash().get(sessionIdHeader));
        }

        final String projectId = getProjectId(data, search);
        if (StringUtils.isNotBlank(projectId)) {
            data.getParameters().add("project", projectId);
            return;
        }

        final ListMultimap<String, String> projectIds = getProjectIds(user, data, search);

        if (projectIds.isEmpty()) {
            data.setMessage("No sessions in accessible projects were found. <a href=\"" + ((TemplateLink) context.get("link")).getLink() + "\">Try your search again.</a>");
            data.setScreenTemplate("Error.vm");
            return;
        }

        if (projectIds.size() == 1) {
            data.getParameters().add("project", projectIds.keySet().iterator().next());
        } else {
            final SerializerService serializer = XDAT.getContextService().getBeanSafely(SerializerService.class);
            if (serializer != null) {
                data.getParameters().add("projects", serializer.toJson(projectIds.asMap()));
            }
        }
    }

    private String getProjectId(final RunData data, final DisplaySearch search) {
        if (TurbineUtils.HasPassedParameter("project", data)) {
            final String projectId = (String) TurbineUtils.GetPassedParameter("project", data);
            if (StringUtils.isNotBlank(projectId)) {
                return projectId;
            }
        }

        final String  rootElement = search.getRootElement().getFullXMLName();
        final Pattern pattern     = Pattern.compile(rootElement + ".(?!SUB)[A-z_]+_PROJECT_IDENTIFIER\\.[A-z0-9_]+");
        for (final Object object : search.getFields().getSortedFields()) {
            final DisplayFieldWrapper field   = (DisplayFieldWrapper) object;
            final Matcher             matcher = pattern.matcher(field.getId());
            if (matcher.matches()) {
                return (String) field.getValue();
            }
        }

        return null;
    }

    @Nonnull
    protected ListMultimap<String, String> getProjectIds(final UserI user, final RunData data, final DisplaySearch search) {
        final String[] sessions = data.getParameters().getStrings("sessions");
        if (ArrayUtils.isEmpty(sessions)) {
            return ArrayListMultimap.create();
        }

        final List<String> accessibleProjects = Permissions.getReadableProjects(user);
        return Multimaps.filterKeys(Permissions.getProjectsForSessions(_template, new HashSet<>(Arrays.asList(sessions))), new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String projectId) {
                return accessibleProjects.contains(projectId);
            }
        });
    }

    private String getSessionIdHeader(final XFTTable table) {
        return Iterables.find(HEADERS, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String key) {
                return key != null && table.getColumnIndex(key) != null;
            }
        }, null);
    }

    private static final List<String> HEADERS = Arrays.asList("session_id", "expt_id", "id");

    private final NamedParameterJdbcTemplate _template;
}
