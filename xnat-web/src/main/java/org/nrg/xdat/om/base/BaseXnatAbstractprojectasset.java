/*
 * xnat-data-models: org.nrg.xdat.om.base.BaseXnatAbstractprojectasset
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om.base;

import java.io.Serial;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatAbstractprojectasset;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * This class and the clara:abstractProjectAsset data type are based on an implementation in the
 * <b>features/proj-assessors</b> branch of <b>xnat-data-models</b>. Once that's been merged into
 * the main <b>develop</b> branch, this should be removed and the <b>clara:trainConfig</b> data
 * type should be migrated to extend the <b>xnat:abstractProjectAsset</b> data type instead.
 */
@SuppressWarnings("unused")
public abstract class BaseXnatAbstractprojectasset extends AutoXnatAbstractprojectasset {
    @Serial
    private static final long serialVersionUID = 1;
    public BaseXnatAbstractprojectasset(final ItemI item) {
        super(item);
    }

    public BaseXnatAbstractprojectasset(final UserI user) {
        super(user);
    }

    /**
     * @deprecated Use BaseAbstractProjectAsset(UserI user)
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public BaseXnatAbstractprojectasset() {
    }

    public BaseXnatAbstractprojectasset(final Hashtable properties, final UserI user) {
        super(properties, user);
    }

    /**
     * Returns a list of the {@link XnatSubjectdata#getId() subject IDs} associated with this project assessor, if any. This provides
     * a faster way to get the list of subjects compared to {@link #getSubjects_subject()}, as this method doesn't retrieve the full
     * {@link XnatSubjectdata subject object} but just the IDs instead.
     *
     * @return A list of IDs of subjects associated with this assessor or an empty list if no subjects are associated with the assessor.
     */
    public List<String> getSubjectIds() {
        final NamedParameterJdbcTemplate template = XDAT.getNamedParameterJdbcTemplate();
        if (template == null || StringUtils.isBlank(getId())) {
            final List<XnatSubjectdataI> subjects = ObjectUtils.defaultIfNull(getSubjects_subject(), Collections.emptyList());
            return subjects.stream().map(XnatSubjectdataI::getId).collect(Collectors.toList());
        }
        return template.queryForList(QUERY_ASSET_SUBJECTS, new MapSqlParameterSource("experiment", getId()), String.class);
    }

    /**
     * Returns a list of the {@link XnatExperimentdata#getId() subject IDs} associated with this project assessor, if any. This provides
     * a faster way to get the list of subjects compared to {@link #getExperiments_experiment()}, as this method doesn't retrieve the full
     * {@link XnatExperimentdata subject object} but just the IDs instead.
     *
     * @return A list of IDs of experiments associated with this assessor or an empty list if no experiments are associated with the assessor.
     */
    public List<String> getExperimentIds() {
        final NamedParameterJdbcTemplate template = XDAT.getNamedParameterJdbcTemplate();
        if (StringUtils.isBlank(getId()) || template == null) {
            final List<XnatExperimentdataI> experiments = ObjectUtils.defaultIfNull(getExperiments_experiment(), Collections.emptyList());
            return experiments.stream().map(XnatExperimentdataI::getId).collect(Collectors.toList());
        }
        return template.queryForList(QUERY_ASSET_EXPERIMENTS, new MapSqlParameterSource("experiment", getId()), String.class);
    }

    private static final String QUERY_ASSET_SUBJECTS    = "SELECT s.id FROM xnat_abstractprojectasset apa LEFT JOIN xnat_projectasset_subjectdata apas ON apa.id = apas.xnat_abstractprojectasset_id LEFT JOIN xnat_subjectdata s ON apas.xnat_subjectdata_id = s.id WHERE apa.id = :experiment";
    private static final String QUERY_ASSET_EXPERIMENTS = "SELECT e.id FROM xnat_abstractprojectasset apa LEFT JOIN xnat_projectasset_experimentdata apae ON apa.id = apae.xnat_abstractprojectasset_id LEFT JOIN xnat_experimentdata e ON apae.xnat_experimentdata_id = e.id WHERE apa.id = :experiment";
}
