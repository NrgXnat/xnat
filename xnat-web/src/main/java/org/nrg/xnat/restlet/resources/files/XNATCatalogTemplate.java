/*
 * web: org.nrg.xnat.restlet.resources.files.XNATCatalogTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.DirectResourceModifierBuilder;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierBuilderI;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@SuppressWarnings({"SqlNoDataSourceInspection", "SqlResolve"})
@Getter
@Setter
@Accessors(prefix = "_")
@Slf4j
public class XNATCatalogTemplate extends XNATTemplate {
    public static final String RESOURCE_ID = "RESOURCE_ID";

    protected XNATCatalogTemplate(final Context context, final Request request, final Response response, final boolean allowAll) throws ClientException {
        super(context, request, response);

        final String       requestedResourceId = (String) getParameter(request, RESOURCE_ID);
        final List<String> requestedResources  = StringUtils.isNotBlank(requestedResourceId) ? Arrays.asList(requestedResourceId.split("\\s*,\\s*")) : Collections.<String>emptyList();
        if (!requestedResources.isEmpty()) {
            // Separate numeric and non-numeric IDs to start. Non-numeric IDs get qualified by project/experiment/etc later.
            _resourceIds.addAll(Lists.newArrayList(Iterables.filter(requestedResources, Predicates.not(Predicates.containsPattern("^\\d+$")))));
            final List<String> requestedResourceIds = Lists.newArrayList(Iterables.filter(requestedResources, Predicates.containsPattern("^\\d+$")));

            // Add all numeric IDs that are either permitted based on associated security ID or that have no security ID (in construction)
            if (!requestedResourceIds.isEmpty()) {
                final List<String> resourceIds = Lists.transform(Lists.newArrayList(Iterables.filter(getTemplate().query(QUERY_FIND_RESOURCE_SECURE_OBJECTS, new MapSqlParameterSource("resourceIds", requestedResourceIds), RESOURCE_MAP_ROW_MAPPER), new PermittedResourcePredicate(getUser()))), ResourceMap.FUNCTION_RESOURCE_MAP);
                if (resourceIds.size() < requestedResourceIds.size()) {
                    final int requested = requestedResourceIds.size();
                    requestedResourceIds.removeAll(resourceIds);
                    throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "The user " + getUser().getUsername() + " requested " + requested + " resources by ID (i.e. not by qualified resource label) but was denied access to " + (resourceIds.isEmpty() ? "all" : requestedResourceIds.size()) + " of them: " + StringUtils.join(requestedResourceIds, ", "));
                }
                _resourceIds.addAll(resourceIds);
            }
        }
        try {
            setCatalogs(loadCatalogs(_resourceIds, true, allowAll));
        } catch (Exception e) {
            log.error("An error occurred trying to load catalogs for user {} from the resource IDs: {}", getUser().getUsername(), _resourceIds, e);
        }
    }

    protected boolean hasCatalogs() {
        return _catalogs != null && _catalogs.size() > 0;
    }

    protected String getBaseURI() {
        final StringBuilder buffer = new StringBuilder("/data");
        if (proj != null && sub != null) {
            buffer.append("/projects/");
            buffer.append(proj.getId());
            buffer.append("/subjects/");
            buffer.append(sub.getId());
        }
        if (!recons.isEmpty()) {
            buffer.append("/experiments/");
            buffer.append(StringUtils.join(Lists.transform(assesseds, FUNCTION_EXPERIMENT_IDS), ","));
            buffer.append("/reconstructions/");
            buffer.append(StringUtils.join(Lists.transform(recons, new Function<XnatReconstructedimagedata, String>() {
                @Override
                public String apply(final XnatReconstructedimagedata recon) {
                    return recon.getId();
                }
            }), ","));
            if (type != null) {
                buffer.append("/").append(type);
            }
        } else if (!scans.isEmpty()) {
            buffer.append("/experiments/");
            buffer.append(StringUtils.join(Lists.transform(assesseds, FUNCTION_EXPERIMENT_IDS), ","));
            buffer.append("/scans/");
            buffer.append(StringUtils.join(Lists.transform(scans, new Function<XnatImagescandata, String>() {
                @Override
                public String apply(final XnatImagescandata scan) {
                    return scan.getId();
                }
            }), ","));
        } else if (!expts.isEmpty()) {
            if (!assesseds.isEmpty()) {
                buffer.append("/experiments/");
                buffer.append(StringUtils.join(Lists.transform(assesseds, FUNCTION_EXPERIMENT_IDS), ","));
                buffer.append("/assessors/");
                buffer.append(StringUtils.join(Lists.transform(expts, FUNCTION_EXPERIMENT_IDS), ","));
                if (type != null) {
                    buffer.append("/").append(type);
                }
            } else {
                buffer.append("/experiments/");
                buffer.append(StringUtils.join(Lists.transform(expts, FUNCTION_EXPERIMENT_IDS), ","));
            }
        } else if (sub == null && proj != null) {
            buffer.append("/projects/");
            buffer.append(proj.getId());
        }
        return buffer.toString();
    }

    protected XnatResourceInfo buildResourceInfo(EventMetaI ci) {
        final String description;
        if (this.getQueryVariable("description") != null) {
            description = this.getQueryVariable("description");
        } else {
            description = null;
        }

        final String format;
        if (this.getQueryVariable("format") != null) {
            format = this.getQueryVariable("format");
        } else {
            format = null;
        }

        final String content;
        if (this.getQueryVariable("content") != null) {
            content = this.getQueryVariable("content");
        } else {
            content = null;
        }

        String[] tags;
        if (this.getQueryVariables("tags") != null) {
            tags = this.getQueryVariables("tags");
        } else {
            tags = null;
        }

        Date d = EventUtils.getEventDate(ci, false);
        return XnatResourceInfo.buildResourceInfo(description, format, content, tags, getUser(), d, d, EventUtils.getEventId(ci));
    }

    protected ResourceModifierA buildResourceModifier(final boolean overwrite, final EventMetaI ci) throws Exception {
        final XnatImagesessiondata assessed = assesseds.size() == 1 ? (XnatImagesessiondata) assesseds.get(0) : null;

        //this should allow dependency injection - TO
        final ResourceModifierBuilderI builder = new DirectResourceModifierBuilder();

        if (!recons.isEmpty()) {
            builder.setRecon(assessed, recons.get(0), type);
        } else if (!scans.isEmpty()) {
            builder.setScan(assessed, scans.get(0));
        } else if (!expts.isEmpty()) {
            final XnatExperimentdata expt = expts.get(0);
            if (expt.getItem().instanceOf("xnat:imageAssessorData")) {
                builder.setAssess(ObjectUtils.defaultIfNull(assessed, ((XnatImageassessordata) expt).getImageSessionData()), (XnatImageassessordata) expt, type);
            } else {
                builder.setExpt(ObjectUtils.defaultIfNull(proj, expt.getProjectData()), expt);
            }
        } else if (sub != null) {
            builder.setSubject(proj, sub);
        } else if (proj != null) {
            builder.setProject(proj);
        } else {
            throw new Exception("Unknown resource");
        }

        return builder.buildResourceModifier(overwrite, getUser(), ci);
    }

    @Data
    @Accessors(prefix = "_")
    private static class ResourceMap {
        private static final Function<ResourceMap, String> FUNCTION_RESOURCE_MAP = new Function<ResourceMap, String>() {
            @Override
            public String apply(final ResourceMap resourceMap) {
                return Long.toString(resourceMap.getResourceId());
            }
        };
        private final        long                          _resourceId;
        private final        String                        _xsiType;
        private final        String                        _securityId;
        private final        String                        _projectId;
    }

    @Data
    @Accessors(prefix = "_")
    private static class PermittedResourcePredicate implements Predicate<ResourceMap> {
        public PermittedResourcePredicate(final UserI user) {
            _user = user;
        }

        @Override
        public boolean apply(final ResourceMap resourceMap) {
            final String xsiType   = resourceMap.getXsiType();
            final String projectId = resourceMap.getProjectId();

            final List<String> xmlPaths = StringUtils.equalsIgnoreCase(xsiType, XnatProjectdata.SCHEMA_ELEMENT_NAME)
                                          ? Collections.singletonList(XnatProjectdata.SCHEMA_ELEMENT_NAME + "/ID")
                                          : Arrays.asList(xsiType + "/project", xsiType + "/sharing/share/project");
            try {
                if (Iterables.any(xmlPaths, new Predicate<String>() {
                    @Override
                    public boolean apply(final String xmlPath) {
                        try {
                            return Permissions.canRead(getUser(), xmlPath, projectId);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                })) {
                    return true;
                }
                log.error("The user {} requested the resource with ID {} on the object {}/ID[{}] but was denied access", getUser().getUsername(), resourceMap.getResourceId(), xsiType, resourceMap.getSecurityId());
                return false;
            } catch (Exception e) {
                log.error("An error occurred trying to check permissions for user {} on the object {}/ID[{}], denying by default", getUser().getUsername(), xsiType, resourceMap.getSecurityId(), e);
                return false;
            }
        }

        private final UserI _user;
    }

    private static final String QUERY_FIND_RESOURCE_SECURE_OBJECTS = "SELECT " +
                                                                     "  a.xnat_abstractresource_id AS resourceId, " +
                                                                     "  r.uri AS uri, " +
                                                                     "  xme.element_name AS xsiType, " +
                                                                     "  e.id AS securityId, " +
                                                                     "  e.project AS projectId " +
                                                                     "FROM " +
                                                                     "  xnat_abstractresource a " +
                                                                     "  LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_imagescandata s ON a.xnat_imagescandata_xnat_imagescandata_id = s.xnat_imagescandata_id " +
                                                                     "  LEFT JOIN img_assessor_in_resource iain ON a.xnat_abstractresource_id = iain.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "  LEFT JOIN img_assessor_out_resource iaout ON a.xnat_abstractresource_id = iaout.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "  LEFT JOIN recon_in_resource rin ON a.xnat_abstractresource_id = rin.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "  LEFT JOIN recon_out_resource rout ON a.xnat_abstractresource_id = rout.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_reconstructedimagedata recon ON COALESCE(rin.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id, rout.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id) = recon.xnat_reconstructedimagedata_id " +
                                                                     "  LEFT JOIN xnat_experimentdata_resource eres ON a.xnat_abstractresource_id = eres.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_experimentdata e ON COALESCE(s.image_session_id, eres.xnat_experimentdata_id, iaout.xnat_imageassessordata_id, iain.xnat_imageassessordata_id, recon.image_session_id) = e.id " +
                                                                     "  LEFT JOIN xdat_meta_element xme ON e.extension = xme.xdat_meta_element_id " +
                                                                     "WHERE " +
                                                                     "  a.xnat_abstractresource_id::VARCHAR(64) IN (:resourceIds) AND " +
                                                                     "  e.id IS NOT NULL " +
                                                                     "UNION " +
                                                                     "SELECT " +
                                                                     "  a.xnat_abstractresource_id AS resourceId, " +
                                                                     "  r.uri AS uri, " +
                                                                     "  'xnat:subjectData' AS xsiType, " +
                                                                     "  s.id AS securityId, " +
                                                                     "  s.project AS projectId " +
                                                                     "FROM " +
                                                                     "  xnat_subjectdata_resource res " +
                                                                     "  LEFT JOIN xnat_abstractresource a ON res.xnat_abstractresource_xnat_abstractresource_id = a.xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_subjectdata S ON res.xnat_subjectdata_id = S.id " +
                                                                     "WHERE " +
                                                                     "  a.xnat_abstractresource_id::VARCHAR(64) IN (:resourceIds) " +
                                                                     "UNION " +
                                                                     "SELECT " +
                                                                     "  a.xnat_abstractresource_id AS resourceId, " +
                                                                     "  r.uri AS uri, " +
                                                                     "  'xnat:projectData' AS xsiType, " +
                                                                     "  p.id AS securityId, " +
                                                                     "  p.id AS projectId " +
                                                                     "FROM " +
                                                                     "  xnat_projectdata_resource res " +
                                                                     "  LEFT JOIN xnat_abstractresource a ON res.xnat_abstractresource_xnat_abstractresource_id = a.xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "  LEFT JOIN xnat_projectdata p ON res.xnat_projectdata_id = p.id " +
                                                                     "WHERE " +
                                                                     "  a.xnat_abstractresource_id::VARCHAR(64) IN (:resourceIds)";

    private static final RowMapper<ResourceMap>               RESOURCE_MAP_ROW_MAPPER = new RowMapper<ResourceMap>() {
        @Override
        public ResourceMap mapRow(final ResultSet resultSet, final int index) throws SQLException {
            return new ResourceMap(resultSet.getLong("resourceId"), resultSet.getString("xsiType"), resultSet.getString("securityId"), resultSet.getString("projectId"));
        }
    };
    private static final Function<XnatExperimentdata, String> FUNCTION_EXPERIMENT_IDS = new Function<XnatExperimentdata, String>() {
        @Override
        public String apply(final XnatExperimentdata experiment) {
            return experiment.getId();
        }
    };

    @Nonnull
    private final List<String>               _resourceIds = new ArrayList<>();
    @Nonnull
    private final List<XnatAbstractresource> _resources   = new ArrayList<>();

    private XFTTable _catalogs = null;
}
