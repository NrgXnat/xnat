/*
 * web: org.nrg.xnat.restlet.resources.files.XNATCatalogTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.action.ClientException;
import org.nrg.xdat.om.*;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        final List<String> requestedResources  = StringUtils.isNotBlank(requestedResourceId) ? Arrays.asList(requestedResourceId.split("\\s*,\\s*")) : Collections.emptyList();
        if (!requestedResources.isEmpty()) {
            // Separate numeric and non-numeric IDs to start. Non-numeric IDs get qualified by project/experiment/etc later.
            final Map<Boolean, List<String>> separated = requestedResources.stream().collect(Collectors.partitioningBy(NumberUtils::isDigits));
            _resourceIds.addAll(separated.get(false));
            final List<Integer> requestedResourceIds = separated.get(true).stream().map(Integer::parseInt).collect(Collectors.toList());

            // Add all numeric IDs that are either permitted based on associated security ID or that have no security ID (in construction)
            if (!requestedResourceIds.isEmpty()) {
                final List<String> resourceIds = getTemplate().query(QUERY_FIND_RESOURCE_SECURE_OBJECTS, new MapSqlParameterSource("resourceIds", requestedResourceIds).addValue("username", getUser().getUsername()), RESOURCE_MAP_ROW_MAPPER).stream().map(ResourceMap::resourceIdAsString).collect(Collectors.toList());
                if (resourceIds.size() < requestedResourceIds.size()) {
                    final int requested = requestedResourceIds.size();
                    requestedResourceIds.removeAll(resourceIds.stream().map(Integer::parseInt).collect(Collectors.toList()));
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
            buffer.append(assesseds.stream().map(XnatExperimentdata::getId).collect(Collectors.joining(",")));
            buffer.append("/reconstructions/");
            buffer.append(recons.stream().map(XnatReconstructedimagedata::getId).collect(Collectors.joining(",")));
            if (StringUtils.isNotBlank(type)) {
                buffer.append("/").append(type);
            }
        } else if (!scans.isEmpty()) {
            buffer.append("/experiments/");
            buffer.append(assesseds.stream().map(XnatExperimentdata::getId).collect(Collectors.joining(",")));
            buffer.append("/scans/");
            buffer.append(scans.stream().map(XnatImagescandata::getId).collect(Collectors.joining(",")));
        } else if (!expts.isEmpty()) {
            if (!assesseds.isEmpty()) {
                buffer.append("/experiments/");
                buffer.append(assesseds.stream().map(XnatExperimentdata::getId).collect(Collectors.joining(",")));
                buffer.append("/assessors/");
                buffer.append(expts.stream().map(XnatExperimentdata::getId).collect(Collectors.joining(",")));
                if (type != null) {
                    buffer.append("/").append(type);
                }
            } else {
                buffer.append("/experiments/");
                buffer.append(expts.stream().map(XnatExperimentdata::getId).collect(Collectors.joining(",")));
            }
        } else if (sub == null && proj != null) {
            buffer.append("/projects/");
            buffer.append(proj.getId());
        }
        return buffer.toString();
    }

    protected XnatResourceInfo buildResourceInfo(EventMetaI ci) {
        final XnatResourceInfo.XnatResourceInfoBuilder builder = XnatResourceInfo.builder();
        if (StringUtils.isNotBlank(getQueryVariable("description"))) {
            builder.description(getQueryVariable("description"));
        }
        if (StringUtils.isNotBlank(getQueryVariable("format"))) {
            builder.format(getQueryVariable("format"));
        }
        if (StringUtils.isNotBlank(getQueryVariable("content"))) {
            builder.content(getQueryVariable("content"));
        }
        if (getQueryVariables("tags") != null) {
            builder.tags(Arrays.asList(getQueryVariables("tags")));
        }

        final Date now = EventUtils.getEventDate(ci, false);
        return builder.username(getUser().getUsername()).created(now).lastModified(now).eventId(EventUtils.getEventId(ci)).build();
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

    @Value
    @Accessors(prefix = "_")
    private static class ResourceMap {
        String resourceIdAsString() {
            return Long.toString(_resourceId);
        }

        long   _resourceId;
        String _xsiType;
        String _securityId;
        String _projectId;
    }

    private static final String QUERY_FIND_RESOURCE_SECURE_OBJECTS = "WITH " +
                                                                     "    resources AS ( " +
                                                                     "        SELECT " +
                                                                     "            a.xnat_abstractresource_id AS resourceId, " +
                                                                     "            r.uri                      AS uri, " +
                                                                     "            xme.element_name           AS xsiType, " +
                                                                     "            e.id                       AS securityId, " +
                                                                     "            e.project                  AS projectId " +
                                                                     "        FROM " +
                                                                     "            xnat_abstractresource a " +
                                                                     "                LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_imagescandata s ON a.xnat_imagescandata_xnat_imagescandata_id = s.xnat_imagescandata_id " +
                                                                     "                LEFT JOIN img_assessor_in_resource iain ON a.xnat_abstractresource_id = iain.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "                LEFT JOIN img_assessor_out_resource iaout ON a.xnat_abstractresource_id = iaout.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "                LEFT JOIN recon_in_resource rin ON a.xnat_abstractresource_id = rin.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "                LEFT JOIN recon_out_resource rout ON a.xnat_abstractresource_id = rout.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_reconstructedimagedata recon ON COALESCE(rin.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id, rout.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id) = recon.xnat_reconstructedimagedata_id " +
                                                                     "                LEFT JOIN xnat_experimentdata_resource eres ON a.xnat_abstractresource_id = eres.xnat_abstractresource_xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_experimentdata e ON COALESCE(s.image_session_id, eres.xnat_experimentdata_id, iaout.xnat_imageassessordata_id, iain.xnat_imageassessordata_id, recon.image_session_id) = e.id " +
                                                                     "                LEFT JOIN xdat_meta_element xme ON e.extension = xme.xdat_meta_element_id " +
                                                                     "        WHERE " +
                                                                     "            a.xnat_abstractresource_id IN (:resourceIds) AND " +
                                                                     "            e.id IS NOT NULL " +
                                                                     "        UNION " +
                                                                     "        SELECT " +
                                                                     "            a.xnat_abstractresource_id AS resourceId, " +
                                                                     "            r.uri                      AS uri, " +
                                                                     "            'xnat:subjectData'         AS xsiType, " +
                                                                     "            s.id                       AS securityId, " +
                                                                     "            s.project                  AS projectId " +
                                                                     "        FROM " +
                                                                     "            xnat_subjectdata_resource res " +
                                                                     "                LEFT JOIN xnat_abstractresource a ON res.xnat_abstractresource_xnat_abstractresource_id = a.xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_subjectdata S ON res.xnat_subjectdata_id = S.id " +
                                                                     "        WHERE " +
                                                                     "            a.xnat_abstractresource_id IN (:resourceIds) " +
                                                                     "        UNION " +
                                                                     "        SELECT " +
                                                                     "            a.xnat_abstractresource_id AS resourceId, " +
                                                                     "            r.uri                      AS uri, " +
                                                                     "            'xnat:projectData'         AS xsiType, " +
                                                                     "            p.id                       AS securityId, " +
                                                                     "            p.id                       AS projectId " +
                                                                     "        FROM " +
                                                                     "            xnat_projectdata_resource res " +
                                                                     "                LEFT JOIN xnat_abstractresource a ON res.xnat_abstractresource_xnat_abstractresource_id = a.xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_resource r ON a.xnat_abstractresource_id = r.xnat_abstractresource_id " +
                                                                     "                LEFT JOIN xnat_projectdata p ON res.xnat_projectdata_id = p.id " +
                                                                     "        WHERE " +
                                                                     "            a.xnat_abstractresource_id IN (:resourceIds)) " +
                                                                     "SELECT * " +
                                                                     "FROM " +
                                                                     "    resources " +
                                                                     "WHERE " +
                                                                     "    data_type_fns_can_action_entity(:username, 'read', securityId) = TRUE";

    private static final RowMapper<ResourceMap> RESOURCE_MAP_ROW_MAPPER = (resultSet, index) -> new ResourceMap(resultSet.getLong("resourceId"), resultSet.getString("xsiType"), resultSet.getString("securityId"), resultSet.getString("projectId"));
    private static final Predicate<String>      IS_A_NUMBER             = Pattern.compile("^\\d+$").asPredicate();
    private static final Predicate<String>      NOT_A_NUMBER            = IS_A_NUMBER.negate();

    private final List<String>               _resourceIds = new ArrayList<>();
    private final List<XnatAbstractresource> _resources   = new ArrayList<>();

    private XFTTable _catalogs = null;
}
