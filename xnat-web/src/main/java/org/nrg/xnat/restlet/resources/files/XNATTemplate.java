/*
 * web: org.nrg.xnat.restlet.resources.files.XNATTemplate
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources.files;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatImageassessordataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.services.archive.CatalogService;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XNATTemplate extends SecureResource {
    XnatProjectdata proj = null;
    XnatSubjectdata sub  = null;

    ArrayList<XnatExperimentdata> expts = new ArrayList<>();

    ArrayList<XnatImagescandata> scans = new ArrayList<>();

    ArrayList<XnatReconstructedimagedata> recons = new ArrayList<>();

    ArrayList<XnatExperimentdata> assesseds = new ArrayList<>();

    String type = null;

    ItemI parent = null;

    ItemI security = null;

    String xmlPath = null;

    private final CatalogService _catalogService;

    public XNATTemplate(Context context, Request request, Response response) {
        super(context, request, response);

        _catalogService = XDAT.getContextService().getBean(CatalogService.class);

        String pID = (String) getParameter(request, "PROJECT_ID");
        final UserI user = getUser();
        if (pID != null) {
            proj = XnatProjectdata.getProjectByIDorAlias(pID, user, false);

            if (proj == null) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                                   "Unable to identify project");
                return;
            }
        }

        String subID = (String) getParameter(request, "SUBJECT_ID");
        if (subID != null) {
            if (this.proj != null) {
                sub = XnatSubjectdata.GetSubjectByProjectIdentifier(proj
                                                                            .getId(), subID, user, false);
            }

            if (sub == null) {
                sub = XnatSubjectdata.getXnatSubjectdatasById(subID, user,
                                                              false);
                if (sub != null && (proj != null && !sub.hasProject(proj.getId()))) {
                    sub = null;
                }
            }

            if (sub == null) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                                   "Unable to identify subject");
                return;
            }
        }

        String assessid = (String) getParameter(request,
                                                "ASSESSED_ID");
        if (assessid != null) {
            for (String s : XftStringUtils.CommaDelimitedStringToArrayList(assessid)) {
                XnatExperimentdata assessed = XnatImagesessiondata.getXnatImagesessiondatasById(
                        s, user, false);

                if (assessed != null && (proj != null && !assessed.hasProject(proj.getId()))) {
                    assessed = null;
                }

                if (assessed == null && proj != null) {
                    assessed = XnatImagesessiondata
                            .GetExptByProjectIdentifier(proj.getId(), s,
                                                        user, false);
                }

                if (assessed != null) {
                    try {
                        if (assessed.canRead(user)) {
                            assesseds.add(assessed);
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (assesseds.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
                    response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                                       "Unable to identify image session");
                    return;
                }
            }
        }

        type = (String) getParameter(request, "TYPE");

        String exptID = (String) getParameter(request, "EXPT_ID");
        if (exptID != null) {
            for (String s : XftStringUtils.CommaDelimitedStringToArrayList(exptID)) {
                XnatExperimentdata expt = XnatExperimentdata.getXnatExperimentdatasById(s,
                                                                                        user, false);

                if (expt == null && proj != null) {
                    expt = XnatExperimentdata
                            .GetExptByProjectIdentifier(proj.getId(), s,
                                                        user, false);
                }

                if (expt != null && assesseds.size() > 0) {
                    if (type == null) {
                        type = "out";
                    }
                }

                if (expt != null) {
                    try {
                        if (expt.canRead(user)) {
                            expts.add(expt);
                        }
                    } catch (Exception ignored) {
                    }
                } else if (assesseds.size() > 0) {
                    for (XnatExperimentdata assessed : assesseds) {
                        for (XnatImageassessordataI iad : ((XnatImagesessiondata) assessed).getMinimalLoadAssessors()) {
                            if (iad.getId().equals(s)
                                || (iad.getLabel() != null && iad.getLabel().equals(s))) {
                                try {
                                    if (((XnatImageassessordata) iad).canRead(user)) {
                                        expts.add(((XnatImageassessordata) iad));
                                    }
                                } catch (Exception ignored) {
                                }
                            } else if (s.equals("*") || s.equals("ALL")) {
                                try {
                                    if (((XnatImageassessordata) iad).canRead(user)) {
                                        expts.add(((XnatImageassessordata) iad));
                                    }
                                } catch (Exception ignored) {
                                }
                            } else {
                                try {
                                    GenericWrapperElement gwe = GenericWrapperElement.GetElement(s);

                                    if (((XnatImageassessordata) iad).getItem().instanceOf(gwe.getFullXMLName())) {
                                        if (((XnatImageassessordata) iad).canRead(user)) {
                                            expts.add(((XnatImageassessordata) iad));
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            }

            if (expts.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                                   "Unable to identify experiment");
                return;
            }
        }

        String scanID = getUrlEncodedParameter(request, "SCAN_ID");
        if (scanID != null && this.assesseds.size() > 0) {

            scanID = scanID.replace("[SLASH]", "/");//this is such an ugly hack.  If a slash is included in the scan type and thus in the URL, it breaks the GET command.  Even if it is properly escaped.  So, I'm adding this alternative encoding of slash to allow us to work around the issue.  Hopefully Spring MVC will eliminate it.

            CriteriaCollection cc = new CriteriaCollection("OR");
            for (XnatExperimentdata assessed : this.assesseds) {
                CriteriaCollection subcc = new CriteriaCollection("AND");
                subcc.addClause("xnat:imageScanData/image_session_ID", assessed
                        .getId());
                if (!(scanID.equals("*") || scanID.equals("ALL"))) {
                    if (!scanID.contains(",")) {
                        subcc.addClause("xnat:imageScanData/ID", scanID);
                    } else {
                        CriteriaCollection subsubcc = new CriteriaCollection("OR");
                        for (String s : XftStringUtils.CommaDelimitedStringToArrayList(scanID, true)) {
                            subsubcc.addClause("xnat:imageScanData/ID", s);
                        }
                        subcc.add(subsubcc);
                    }
                }
                cc.add(subcc);

                subcc = new CriteriaCollection("AND");
                subcc.addClause("xnat:imageScanData/image_session_ID", assessed
                        .getId());
                if (!(scanID.equals("*") || scanID.equals("ALL"))) {
                    if (!scanID.contains(",")) {
                        if (scanID.equals("NULL")) {
                            CriteriaCollection subsubcc = new CriteriaCollection("OR");
                            subsubcc.addClause("xnat:imageScanData/type", "", " IS NULL ", true);
                            subsubcc.addClause("xnat:imageScanData/type", "");
                            subcc.add(subsubcc);
                        } else {
                            subcc.addClause("xnat:imageScanData/type", scanID.replace("[COMMA]", ","));
                        }
                    } else {
                        CriteriaCollection subsubcc = new CriteriaCollection("OR");
                        for (String s : XftStringUtils.CommaDelimitedStringToArrayList(scanID, true)) {
                            if (s.equals("NULL")) {
                                subsubcc.addClause("xnat:imageScanData/type", "", " IS NULL ", true);
                                subsubcc.addClause("xnat:imageScanData/type", "");
                            } else {
                                subsubcc.addClause("xnat:imageScanData/type", s.replace("[COMMA]", ","));
                            }
                        }
                        subcc.add(subsubcc);
                    }
                }
                cc.add(subcc);
            }

            scans = XnatImagescandata
                    .getXnatImagescandatasByField(cc, user,
                                                  completeDocument);

            if (scans.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                                   "Unable to identify scan");
                return;
            }
        }

        String reconID = getUrlEncodedParameter(request, "RECON_ID");
        if (reconID != null && assesseds.size() > 0) {
            CriteriaCollection cc = new CriteriaCollection("OR");

            for (XnatExperimentdata assessed : this.assesseds) {
                CriteriaCollection subcc = new CriteriaCollection("AND");
                subcc.addClause("xnat:reconstructedImageData/image_session_ID",
                                assessed.getId());
                if (!(reconID.equals("*") || reconID.equals("ALL"))) {
                    if (!reconID.contains(",")) {
                        subcc.addClause("xnat:reconstructedImageData/ID", reconID);
                    } else {
                        CriteriaCollection subsubcc = new CriteriaCollection("OR");
                        for (String s : XftStringUtils.CommaDelimitedStringToArrayList(reconID, true)) {
                            subsubcc.addClause("xnat:reconstructedImageData/ID", s);
                        }
                        subcc.add(subsubcc);
                    }
                }
                cc.add(subcc);

                subcc = new CriteriaCollection("AND");
                subcc.addClause("xnat:reconstructedImageData/image_session_ID",
                                assessed.getId());
                if (!(reconID.equals("*") || reconID.equals("ALL"))) {
                    if (!reconID.contains(",")) {
                        if (reconID.equals("NULL")) {
                            CriteriaCollection subsubcc = new CriteriaCollection("OR");
                            subsubcc.addClause("xnat:reconstructedImageData/type", "", " IS NULL ", true);
                            subsubcc.addClause("xnat:reconstructedImageData/type", "");
                            subcc.add(subsubcc);
                        } else {
                            subcc.addClause("xnat:reconstructedImageData/type", reconID);
                        }
                    } else {
                        CriteriaCollection subsubcc = new CriteriaCollection("OR");
                        for (String s : XftStringUtils.CommaDelimitedStringToArrayList(reconID, true)) {
                            if (s.equals("NULL")) {
                                subsubcc.addClause("xnat:reconstructedImageData/type", "", " IS NULL ", true);
                                subsubcc.addClause("xnat:reconstructedImageData/type", "");
                            } else {
                                subsubcc.addClause("xnat:reconstructedImageData/type", s.replace("[COMMA]", ","));
                            }
                        }
                        subcc.add(subsubcc);
                    }
                }
                cc.add(subcc);
            }

            recons = XnatReconstructedimagedata
                    .getXnatReconstructedimagedatasByField(cc, user,
                                                           completeDocument);
            if (recons.size() > 0) {
                if (type == null) {
                    type = "out";
                }
            }

            if (recons.size() != 1 && !this.getRequest().getMethod().equals(Method.GET)) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to identify reconstruction");
            }
        }
    }

    public ItemI getSecurityItem() {
        if (this.security != null) {
            return security;
        }

        XnatExperimentdata assessed = null;
        if (this.assesseds.size() == 1) {
            assessed = assesseds.get(0);
        }

        if (recons.size() > 0) {
            return assessed;
        } else if (scans.size() > 0) {
            return assessed;
        } else if (expts.size() > 0) {
//			experiment
            return expts.get(0);
        } else if (sub != null) {
            return sub;
        } else if (proj != null) {
            return proj;
        } else {
            return null;
        }
    }

    public void insertCatalogWrap(XnatResourcecatalog catResource, PersistentWorkflowI wrk, UserI user) throws Exception {
        final boolean isNew;
        final Integer wrkId;
        if (wrk == null) {
            isNew = true;
            wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, getSecurityItem().getItem(), newEventInstance(EventUtils.CATEGORY.DATA, (getAction() != null) ? getAction() : EventUtils.CREATE_RESOURCE));
            if (wrk == null) {
                throw new Exception("Unable to build open workflow for inserting catalog " + catResource.getUri());
            }

            wrk.setStatus(PersistentWorkflowUtils.IN_PROGRESS);
            PersistentWorkflowUtils.save(wrk, wrk.buildEvent());
            wrkId=wrk.getWorkflowId();
        } else {
            wrkId=null;
            isNew = false;
        }

        insertCatalog(catResource, wrkId);

        if (isNew) {
            WorkflowUtils.complete(wrk, wrk.buildEvent());
        }
    }

    public boolean insertCatalog(XnatResourcecatalog catResource, Integer eventId) throws Exception {
        final XnatExperimentdata assessed = assesseds.size() == 1 ? assesseds.get(0) : null;

        final UserI user = getUser();
        if (recons.size() > 0) {
            if (assessed == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Invalid session id.");
                return false;
            }

            final XnatReconstructedimagedata reconstruction = recons.get(0);
            return _catalogService.insertResourceCatalog(user, UriParserUtils.getArchiveUri(assessed, reconstruction), catResource,eventId) != null;
        } else if (scans.size() > 0) {
            if (assessed == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_GONE, "Invalid session id.");
                return false;
            }
            final XnatImagescandata scan = scans.get(0);
            return _catalogService.insertResourceCatalog(user, UriParserUtils.getArchiveUri(assessed, scan), catResource, eventId) != null;
        } else if (expts.size() > 0) {
            final XnatExperimentdata experiment = expts.get(0);
            return _catalogService.insertResourceCatalog(user, UriParserUtils.getArchiveUri(experiment), catResource, eventId) != null;
        } else if (sub != null) {
            return _catalogService.insertResourceCatalog(user, UriParserUtils.getArchiveUri(sub), catResource, eventId) != null;
        } else if (proj != null) {
            return _catalogService.insertResourceCatalog(user, UriParserUtils.getArchiveUri(proj), catResource, eventId) != null;
        }
        return true;
    }

    public void checkResourceIDs(final List<String> resourceIds) throws Exception {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }
        if (Iterables.any(resourceIds, CONTAINS_QUOTE)) {
            throw new Exception("Possible SQL Injection attempt. The \"'\" character is not allowed in resource labels: " + StringUtils.join(Iterables.filter(resourceIds, CONTAINS_QUOTE), ", "));
        }
        if (Iterables.any(resourceIds, HACK_CHECK)) {
            throw new Exception("Possible SQL Injection attempt: " + StringUtils.join(Iterables.filter(resourceIds, CONTAINS_QUOTE), ", "));
        }
    }

    public XFTTable loadCatalogs(final List<String> resourceIds, final boolean includeURI, final boolean allowAll) throws Exception {
        checkResourceIDs(resourceIds);

        final StringBuilder query = new StringBuilder();
        final boolean hasResourceIds = resourceIds != null && !resourceIds.isEmpty();
        final boolean isInResource = StringUtils.equalsIgnoreCase(type, "in");

        final UserI user = getUser();
        if (!recons.isEmpty()) {
            security = assesseds.get(0);
            parent = recons.get(0);
            final List<Integer> reconIds = Lists.transform(recons, new Function<XnatReconstructedimagedata, Integer>() {
                @Override
                public Integer apply(final XnatReconstructedimagedata recon) {
                    return recon.getXnatReconstructedimagedataId();
                }
            });
            if (isInResource) {
                xmlPath = "xnat:reconstructedImageData/in/file";
                query.append(STARTER_FIELDS);
                query.append(", 'reconstructions'::TEXT AS category, recon.id::TEXT AS cat_id, recon.type::TEXT AS cat_desc");
                if (includeURI) {
                    query.append(",'/experiments/' || recon.image_session_id || '/reconstructions/' || recon.id || '/in' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM recon_in_resource map LEFT JOIN xnat_reconstructedimagedata recon ON map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=recon.xnat_reconstructedimagedata_id LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_reconstructedimagedata_xnat_reconstructedimagedata_id IN ('");
                query.append(StringUtils.join(reconIds, "', '"));
                query.append("') ");
                if (hasResourceIds) {
                    query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
                }
            } else {
                xmlPath = "xnat:reconstructedImageData/out/file";
                query.append(STARTER_FIELDS);
                query.append(", 'reconstructions'::TEXT AS category, recon.id::TEXT AS cat_id, recon.type::TEXT AS cat_desc");
                if (includeURI) {
                    query.append(",'/experiments/' || recon.image_session_id || '/reconstructions/' || recon.id || '/out' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM recon_out_resource map LEFT JOIN xnat_reconstructedimagedata recon ON map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id=recon.xnat_reconstructedimagedata_id LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_reconstructedimagedata_xnat_reconstructedimagedata_id IN ('");
                query.append(StringUtils.join(reconIds, "', '"));
                query.append("') ");
                if (hasResourceIds) {
                    query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
                }
            }
        } else if (!scans.isEmpty()) {
            security = assesseds.get(0);
            parent = scans.get(0);
            final List<Integer> scanIds = Lists.transform(scans, new Function<XnatImagescandata, Integer>() {
                @Override
                public Integer apply(final XnatImagescandata scan) {
                    return scan.getXnatImagescandataId();
                }
            });
            xmlPath = "xnat:imageScanData/file";
            query.append(STARTER_FIELDS);
            query.append(", 'scans'::TEXT AS category, scan.id::TEXT AS cat_id, scan.type::TEXT AS cat_desc");
            if (includeURI) {
                query.append(",'/experiments/' || scan.image_session_id || '/scans/' || scan.id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
            }
            query.append(" FROM xnat_abstractresource abst LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id LEFT JOIN xnat_imagescandata scan ON abst.xnat_imagescandata_xnat_imagescandata_id=scan.xnat_imagescandata_id WHERE xnat_imagescandata_xnat_imagescandata_id IN ('");
            query.append(StringUtils.join(scanIds, "', '"));
            query.append("') ");
            if (hasResourceIds) {
                query.append(" AND (").append(getResourceIdsWhereClause(resourceIds, "abst.xnat_abstractresource_id")).append(")");
            }
        } else if (!expts.isEmpty()) {
            security = expts.get(0);
            parent = expts.get(0);
            final List<String> experimentIds = Lists.transform(expts, new Function<XnatExperimentdata, String>() {
                @Override
                public String apply(final XnatExperimentdata experiment) {
                    return experiment.getId();
                }
            });
            if (!assesseds.isEmpty()) {
                security = assesseds.get(0);
                if (isInResource) {
                    xmlPath = "xnat:imageAssessorData/in/file";
                    query.append(STARTER_FIELDS);
                    query.append(", 'assessors'::TEXT AS category, expt.id::TEXT AS cat_id, COALESCE(xes.singular,xmeexpt.element_name)::TEXT AS cat_desc");
                    if (includeURI) {
                        query.append(",'/experiments/' || xiad.imagesession_id || '/assessors/' || expt.id || '/in' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                    }
                    query.append(" FROM img_assessor_in_resource map LEFT JOIN xnat_experimentdata expt ON map.xnat_imageassessordata_id=expt.id ");
                    if (includeURI) {
                        query.append(" LEFT JOIN xnat_imageassessordata xiad ON expt.id=xiad.id ");
                    }
                    query.append(" LEFT JOIN xdat_meta_element xmeexpt ON expt.extension=xmeexpt.xdat_meta_element_id LEFT JOIN xdat_element_security xes ON xmeexpt.element_name=xes.element_name LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE map.xnat_imageassessordata_id IN ('");
                    query.append(StringUtils.join(experimentIds, "', '"));
                    query.append("') ");
                    if (hasResourceIds) {
                        query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
                    }
                } else {
                    xmlPath = "xnat:imageAssessorData/out/file";
                    query.append(STARTER_FIELDS);
                    query.append(", 'assessors'::TEXT AS category, expt.id::TEXT AS cat_id, COALESCE(xes.singular,xmeexpt.element_name)::TEXT AS cat_desc");
                    if (includeURI) {
                        query.append(",'/experiments/' || xiad.imagesession_id || '/assessors/' || expt.id || '/out' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                    }
                    query.append(" FROM img_assessor_out_resource map LEFT JOIN xnat_experimentdata expt ON map.xnat_imageassessordata_id=expt.id ");
                    if (includeURI) {
                        query.append(" LEFT JOIN xnat_imageassessordata xiad ON expt.id=xiad.id ");
                    }
                    query.append(" LEFT JOIN xdat_meta_element xmeexpt ON expt.extension=xmeexpt.xdat_meta_element_id LEFT JOIN xdat_element_security xes ON xmeexpt.element_name=xes.element_name LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE map.xnat_imageassessordata_id IN ('");
                    query.append(StringUtils.join(experimentIds, "', '"));
                    query.append("') ");
                    if (hasResourceIds) {
                        query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
                    }
                }
            } else if (allowAll && (isQueryVariableTrue("all") || resourceIds != null)) {
                xmlPath = "xnat:experimentData/resources/resource";
                final Map<String, String> variables = new HashMap<>();
                variables.put("username", user.getUsername());
                variables.put("sessionIds", StringUtils.join(experimentIds, "', '"));
                final String userAccessibleAccessorIds = StringSubstitutor.replace(USER_ACCESSIBLE_ASSESSOR_IDS, variables);
                // resources

                query.append("SELECT * FROM (").append(STARTER_FIELDS).append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id,''::TEXT AS cat_desc");
                if (includeURI) {
                    query.append(",'/experiments/' || res_map.xnat_experimentdata_id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM xnat_experimentdata_resource res_map JOIN xnat_abstractresource abst ON res_map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE res_map.xnat_experimentdata_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("') ");
                query.append("  UNION ");
                query.append(STARTER_FIELDS);
                query.append(", 'scans'::TEXT,isd.id,isd.type");
                if (includeURI) {
                    query.append(",'/experiments/' || isd.image_session_id || '/scans/' || isd.id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM xnat_imagescanData isd JOIN xnat_abstractresource abst ON isd.xnat_imagescandata_id=abst.xnat_imagescandata_xnat_imagescandata_id JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE isd.image_session_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("') UNION ");
                query.append(STARTER_FIELDS);
                query.append(", 'reconstructions'::TEXT,recon.id,recon.type");
                if (includeURI) {
                    query.append(",'/experiments/' || recon.image_session_id || '/reconstructions/' || recon.id || '/out' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM xnat_reconstructedimagedata recon JOIN recon_out_resource map ON recon.xnat_reconstructedimagedata_id=map.xnat_reconstructedimagedata_xnat_reconstructedimagedata_id JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE image_session_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("') UNION ");
                query.append(STARTER_FIELDS);
                query.append(", 'assessors'::TEXT,iad.id,xes.singular");
                if (includeURI) {
                    query.append(",'/experiments/' || iad.imagesession_id || '/assessors/' || iad.id || '/out' || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM ").append(userAccessibleAccessorIds).append(" iad JOIN img_assessor_out_resource map ON iad.id=map.xnat_imageassessordata_id JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id LEFT JOIN xdat_element_security xes ON xme.element_name=xes.element_name WHERE iad.imagesession_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("') UNION ");
                query.append(STARTER_FIELDS);
                query.append(", 'assessors'::TEXT,iad.id,xes.singular");
                if (includeURI) {
                    query.append(",'/experiments/' || iad.imagesession_id || '/assessors/' || iad.id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM ").append(userAccessibleAccessorIds).append(" iad JOIN xnat_experimentdata_resource map ON iad.id=map.xnat_experimentdata_id JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id LEFT JOIN xdat_element_security xes ON xme.element_name=xes.element_name WHERE iad.imagesession_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("')) all_resources");

                if (hasResourceIds) {
                    query.append(" WHERE (").append(getResourceIdsWhereClause(resourceIds, "xnat_abstractresource_id", "label")).append(")");
                }
            } else {
                xmlPath = "xnat:experimentData/resources/resource";
                // resources
                query.append(STARTER_FIELDS);
                query.append(", 'resources'::TEXT AS category, expt.id::TEXT AS cat_id, ' '::TEXT AS cat_desc");
                if (includeURI) {
                    query.append(",'/experiments/' || map.xnat_experimentdata_id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
                }
                query.append(" FROM xnat_experimentdata_resource map LEFT JOIN xnat_experimentdata expt ON map.xnat_experimentdata_id=expt.id LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_experimentdata_id IN ('");
                query.append(StringUtils.join(experimentIds, "', '"));
                query.append("') ");
                if (hasResourceIds) {
                    query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
                }
            }
        } else if (sub != null) {
            security = sub;
            parent = sub;
            xmlPath = "xnat:subjectData/resources/resource";
            // resources
            query.append(STARTER_FIELDS);
            query.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id, ' '::TEXT AS cat_desc");
            if (includeURI) {
                query.append(",'/projects/' || sub.project || '/subjects/' || sub.id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
            }
            query.append(" FROM xnat_subjectdata_resource map LEFT JOIN xnat_subjectdata sub ON map.xnat_subjectdata_id=sub.id LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_subjectdata_id='");
            query.append(sub.getId());
            query.append("'");
            if (hasResourceIds) {
                query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
            }
        } else if (proj != null) {
            security = proj;
            parent = proj;
            xmlPath = "xnat:projectData/resources/resource";
            // resources
            query.append(STARTER_FIELDS);
            query.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id, ' '::TEXT AS cat_desc");
            if (includeURI) {
                query.append(",'/projects/' || map.xnat_projectdata_id || '/resources/' || abst.xnat_abstractresource_id AS resource_path");
            }
            query.append(" FROM xnat_projectdata_resource map LEFT JOIN xnat_abstractresource abst ON map.xnat_abstractresource_xnat_abstractresource_id=abst.xnat_abstractresource_id LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_projectdata_id='");
            query.append(proj.getId());
            query.append("'");
            if (hasResourceIds) {
                query.append(" AND (").append(getResourceIdsWhereClause(resourceIds)).append(")");
            }
        } else {
            query.append(STARTER_FIELDS);
            query.append(", 'resources'::TEXT AS category, NULL::TEXT AS cat_id, ' '::TEXT AS cat_desc FROM xnat_abstractresource abst LEFT JOIN xdat_meta_element xme ON abst.extension=xme.xdat_meta_element_id WHERE xnat_abstractresource_id IS NULL");
        }

        final String completedQuery = query.toString();
        log.debug("Loading catalog for user '{}' using query: {}", user.getUsername(), completedQuery);
        return XFTTable.Execute(completedQuery, user.getDBName(), userName);
    }

    protected String getResourceIdsWhereClause(final List<String> resourceIds) {
        return getResourceIdsWhereClause(resourceIds, "map.xnat_abstractresource_xnat_abstractresource_id", "abst.label");
    }

    protected String getResourceIdsWhereClause(final List<String> resourceIds, final String idKey) {
        return getResourceIdsWhereClause(resourceIds, idKey, "abst.label");
    }

    protected String getResourceIdsWhereClause(final List<String> resourceIds, final String idKey, final String labelKey) {
        // Numeric resource IDs are those that contain only digits.
        final List<String> numericIds = Lists.newArrayList(Iterables.filter(resourceIds, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String resourceId) {
                return StringUtils.isNumeric(resourceId);
            }
        }));
        // Text resource IDs are those that are not the literal value "NULL". This includes the numeric resource IDs.
        final List<String> textIds = Lists.newArrayList(Iterables.filter(resourceIds, new Predicate<String>() {
            @Override
            public boolean apply(@Nullable final String resourceId) {
                return !StringUtils.equalsIgnoreCase("NULL", resourceId);
            }
        }));
        // We can detect the literal value "NULL" implicitly, because it would have been filtered out of the text resource IDs.
        final boolean hasNull = resourceIds.size() > textIds.size();
        final boolean hasNumerics = !numericIds.isEmpty();
        final boolean hasTexts = !textIds.isEmpty();

        final StringBuilder whereClause = new StringBuilder();
        if (hasNumerics) {
            whereClause.append(idKey).append(" IN (").append(StringUtils.join(numericIds, ", ")).append(")");
        }
        if (hasNumerics && hasTexts) {
            whereClause.append(" OR ");
        }
        if (hasTexts) {
            whereClause.append(labelKey).append(" IN ('").append(StringUtils.join(textIds, "', '")).append("')");
        }
        if ((hasNumerics || hasTexts) && hasNull) {
            whereClause.append(" OR ");
        }
        if (hasNull) {
            whereClause.append(labelKey).append(" IS NULL");
        }
        return whereClause.toString();
    }

    protected void setCatalogAttributes(final UserI user, final XnatResourcecatalog catalog) throws Exception {
        if (StringUtils.isNotBlank(getQueryVariable("description"))) {
            catalog.setDescription(this.getQueryVariable("description"));
        }
        if (StringUtils.isNotBlank(getQueryVariable("format"))) {
            catalog.setFormat(this.getQueryVariable("format"));
        }
        if (StringUtils.isNotBlank(getQueryVariable("content"))) {
            catalog.setContent(this.getQueryVariable("content"));
        }

        final String[] tags = getQueryVariables("tags");
        if (tags != null) {
            for (final String variable : tags) {
                if (StringUtils.isNotBlank(variable)) {
                    for (final String instance : variable.split("\\s*,\\s*")) {
                        final XnatAbstractresourceTag tag = new XnatAbstractresourceTag(user);
                        if (instance.contains("=")) {
                            final String[] atoms = instance.split("=");
                            tag.setName(atoms[0]);
                            tag.setTag(atoms[1]);
                        } else if (instance.contains(":")) {
                            final String[] atoms = instance.split(":");
                            tag.setName(atoms[0]);
                            tag.setTag(atoms[1]);
                        } else {
                            tag.setTag(instance);
                        }
                        catalog.setTags_tag(tag);
                    }
                }
            }
        }
    }

    private static final Predicate<String> CONTAINS_QUOTE = new Predicate<String>() {
        @Override
        public boolean apply(final String resourceId) {
            return StringUtils.contains(resourceId, "'");
        }
    };

    private static final Predicate<String> HACK_CHECK = new Predicate<String>() {
        @Override
        public boolean apply(final String resourceId) {
            return PoolDBUtils.HackCheck(resourceId);
        }
    };
    private static final String STARTER_FIELDS = "SELECT xnat_abstractresource_id, abst.label, xme.element_name ";
    public static final  String            USER_ACCESSIBLE_ASSESSOR_IDS = "( SELECT * FROM xnat_imageassessordata WHERE id IN (SELECT id " +
                                                              " FROM   (SELECT xea.element_name, " +
                                                              "                xfm.field, " +
                                                              "                xfm.field_value " +
                                                              "         FROM   xdat_user u " +
                                                              "                JOIN xdat_user_groupid map " +
                                                              "                  ON u.xdat_user_id = map.groups_groupid_xdat_user_xdat_user_id " +
                                                              "                JOIN xdat_usergroup gp " +
                                                              "                  ON map.groupid = gp.id " +
                                                              "                JOIN xdat_element_access xea " +
                                                              "                  ON gp.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                              "                JOIN xdat_field_mapping_set xfms " +
                                                              "                  ON " +
                                                              " xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              " JOIN xdat_field_mapping xfm " +
                                                              "   ON " +
                                                              " xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              " AND read_element = 1 " +
                                                              " AND field_value != '' " +
                                                              " AND field != '' " +
                                                              " WHERE  u.login = 'guest' " +
                                                              "  UNION " +
                                                              "  SELECT xea.element_name, " +
                                                              "         xfm.field, " +
                                                              "         xfm.field_value " +
                                                              "  FROM   xdat_user_groupid map " +
                                                              "         JOIN xdat_user u ON map.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id " +
                                                              "         JOIN xdat_usergroup gp " +
                                                              "           ON map.groupid = gp.id " +
                                                              "         JOIN xdat_element_access xea " +
                                                              "           ON gp.xdat_usergroup_id = xea.xdat_usergroup_xdat_usergroup_id " +
                                                              "         JOIN xdat_field_mapping_set xfms " +
                                                              "           ON " +
                                                              " xea.xdat_element_access_id = xfms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              " JOIN xdat_field_mapping xfm " +
                                                              "   ON " +
                                                              " xfms.xdat_field_mapping_set_id = xfm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              " AND read_element = 1 " +
                                                              " AND field_value != '' " +
                                                              " AND field != '' " +
                                                              " WHERE u.login = '${username}' " +
                                                              " OR xfm.field_value IN (SELECT proj.id " +
                                                              "         FROM   xnat_projectdata proj " +
                                                              "         JOIN (SELECT field_value, " +
                                                              "                        read_element AS project_read " +
                                                              "                                FROM   xdat_element_access " +
                                                              "                                ea " +
                                                              "                                LEFT JOIN xdat_field_mapping_set fms " +
                                                              "                                ON ea.xdat_element_access_id = " +
                                                              "                                fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              "                                LEFT JOIN xdat_user u " +
                                                              "                                ON ea.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                              "                                LEFT JOIN xdat_field_mapping fm " +
                                                              "                                ON fms.xdat_field_mapping_set_id = " +
                                                              "                                fm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              "                                WHERE  login = 'guest' " +
                                                              "                                AND read_element = 1 " +
                                                              "                                AND element_name = 'xnat:projectData')project_read " +
                                                              " ON proj.id = project_read.field_value " +
                                                              " JOIN (SELECT field_value, " +
                                                              "       read_element AS subject_read " +
                                                              "               FROM   xdat_element_access ea " +
                                                              "               LEFT JOIN xdat_field_mapping_set fms " +
                                                              "               ON ea.xdat_element_access_id = " +
                                                              "               fms.permissions_allow_set_xdat_elem_xdat_element_access_id " +
                                                              "               LEFT JOIN xdat_user u " +
                                                              "               ON ea.xdat_user_xdat_user_id = u.xdat_user_id " +
                                                              "               LEFT JOIN xdat_field_mapping fm " +
                                                              "               ON fms.xdat_field_mapping_set_id = " +
                                                              "               fm.xdat_field_mapping_set_xdat_field_mapping_set_id " +
                                                              "               WHERE  login = 'guest' " +
                                                              "               AND read_element = 1 " +
                                                              "               AND field = 'xnat:subjectData/project')subject_read " +
                                                              " ON proj.id = subject_read.field_value)) perms " +
                                                              " INNER JOIN (SELECT iad.id, " +
                                                              "                    element_name " +
                                                              "                    || '/project' AS field, " +
                                                              "                    expt.project, " +
                                                              "                    expt.label " +
                                                              "             FROM   xnat_imageassessordata iad " +
                                                              "                    LEFT JOIN xnat_experimentdata expt " +
                                                              "                           ON iad.id = expt.id " +
                                                              "                    LEFT JOIN xdat_meta_element xme " +
                                                              "                           ON expt.extension = xme.xdat_meta_element_id " +
                                                              "             WHERE  iad.imagesession_id IN ('${sessionIds}') " +
                                                              "             UNION " +
                                                              "             SELECT expt.id, " +
                                                              "                    xme.element_name " +
                                                              "                    || '/sharing/share/project', " +
                                                              "                    shr.project, " +
                                                              "                    shr.label " +
                                                              "             FROM   xnat_experimentdata_share shr " +
                                                              "                    LEFT JOIN xnat_experimentdata expt " +
                                                              "                           ON expt.id = shr.sharing_share_xnat_experimentda_id " +
                                                              "                    LEFT JOIN xdat_meta_element xme " +
                                                              "                           ON expt.extension = xme.xdat_meta_element_id) expts " +
                                                              "         ON perms.field = expts.field " +
                                                              "            AND perms.field_value IN (expts.project, '*') " +
                                                              " ORDER  BY element_name) )";
}
