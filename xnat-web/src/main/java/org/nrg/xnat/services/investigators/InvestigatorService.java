/*
 * web: org.nrg.xnat.services.investigators.InvestigatorService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.investigators;

import org.nrg.xapi.model.investigators.Investigator;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Manages operations with {@link Investigator investigator proxy objects}. This is not a full-on Hibernate service,
 * since the "entities" managed are not Hibernate entities but instead are composite objects that represent XFT {@link
 * XnatInvestigatordataI} objects as well as metadata aggregated from other tables.
 */
@Service
public class InvestigatorService {
    @Autowired
    public InvestigatorService(final JdbcTemplate template) {
        _template = template;
    }

    public Investigator getInvestigator(final int xnatInvestigatordataId) {
        return _template.queryForObject(INVESTIGATOR_QUERY + BY_ID_WHERE, new Object[]{xnatInvestigatordataId}, ROW_MAPPER);
    }

    public Investigator getInvestigator(final String firstname, final String lastname) {
        return _template.queryForObject(INVESTIGATOR_QUERY + BY_FIRST_LAST_WHERE, new Object[]{firstname, lastname}, ROW_MAPPER);
    }

    public List<Investigator> getInvestigators() {
        return _template.query(INVESTIGATOR_QUERY, ROW_MAPPER);
    }

    private static final String INVESTIGATOR_QUERY  = "SELECT investigator.xnat_investigatordata_id AS xnat_investigatordata_id, investigator.id AS id, investigator.title AS title, investigator.firstname AS firstname, investigator.lastname AS lastname, investigator.institution AS institution, investigator.department AS department, investigator.email AS email, investigator.phone AS phone, (SELECT array(SELECT project.id FROM xnat_projectdata project WHERE project.pi_xnat_investigatordata_id = investigator.xnat_investigatordata_id)) AS primary_inv, (SELECT array(SELECT project_inv.xnat_projectdata_id FROM xnat_projectdata_investigator project_inv WHERE project_inv.xnat_investigatordata_xnat_investigatordata_id = investigator.xnat_investigatordata_id)) AS inv FROM xnat_investigatordata investigator";
    private static final String BY_ID_WHERE         = " WHERE investigator.xnat_investigatordata_id = ?";
    private static final String BY_FIRST_LAST_WHERE = " WHERE investigator.firstname = ? AND investigator.lastname = ?";

    private static final RowMapper<Investigator> ROW_MAPPER = new RowMapper<Investigator>() {
        @Override
        public Investigator mapRow(final ResultSet resultSet, final int i) throws SQLException {
            return new Investigator(resultSet);
        }
    };
    private final JdbcTemplate _template;
}
