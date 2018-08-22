/*
 * web: org.nrg.xnat.services.investigators.InvestigatorService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.investigators.impl.xft;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xapi.model.investigators.Investigator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.exception.XftItemException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.investigators.InvestigatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages operations with {@link Investigator investigator proxy objects}. This is not a full-on Hibernate service,
 * since the "entities" managed are not Hibernate entities but instead are composite objects that represent XFT {@link
 * XnatInvestigatordataI} objects as well as metadata aggregated from other tables.
 */
@Service
@Slf4j
public class DefaultInvestigatorService implements InvestigatorService {
    @Autowired
    public DefaultInvestigatorService(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator createInvestigator(final Investigator investigator, final UserI user) throws XftItemException, ResourceAlreadyExistsException {
        try {
            getInvestigator(investigator.getFirstname(), investigator.getLastname());
            throw new ResourceAlreadyExistsException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, investigator.getFirstname() + " " + investigator.getLastname());
        } catch (NotFoundException ignored) {
            // Do nothing here: this is actually what we want.
        }
        try {
            final XFTItem item = XFTItem.NewItem(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, user);
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".title", investigator.getTitle());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".firstname", investigator.getFirstname());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".lastname", investigator.getLastname());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".department", investigator.getDepartment());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".institution", investigator.getInstitution());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".email", investigator.getEmail());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".phone", investigator.getPhone());
            if (!SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.CREATE_INVESTTGATOR))) {
                log.error("Failed to create a new investigator \"{}\" for user {}. Check the logs for possible errors or exceptions.", investigator, user.getUsername());
                return null;
            }
            return getInvestigator(investigator.getFirstname(), investigator.getLastname());
        } catch (Exception e) {
            throw new XftItemException("Failed to create the investigator: " + investigator.toString(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator getInvestigator(final int investigatorId) throws NotFoundException {
        try {
            return _template.queryForObject(INVESTIGATOR_QUERY + BY_ID_WHERE, new MapSqlParameterSource("investigatorId", investigatorId), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ":ID = " + investigatorId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator getInvestigator(final String firstName, final String lastName) throws NotFoundException {
        try {
            return _template.queryForObject(INVESTIGATOR_QUERY + BY_FIRST_LAST_WHERE, new MapSqlParameterSource("firstName", firstName).addValue("lastName", lastName), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ":firstName = " + firstName + ", :lastName = " + lastName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Investigator> getInvestigators() {
        return _template.query(INVESTIGATOR_QUERY + ORDER_BY_NAME, ROW_MAPPER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator updateInvestigator(final int investigatorId, final Investigator investigator, final UserI user) throws NotFoundException, InitializationException, XftItemException {
        final XnatInvestigatordata existing = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (existing == null) {
            throw new NotFoundException("No investigator found for ID " + investigatorId);
        }

        final AtomicBoolean isDirty = new AtomicBoolean(false);
        // Only update fields that are actually included in the submitted data and differ from the original source.
        if (!StringUtils.equals(investigator.getTitle(), existing.getTitle())) {
            existing.setTitle(investigator.getTitle());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getFirstname(), existing.getFirstname())) {
            existing.setFirstname(investigator.getFirstname());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getLastname(), existing.getLastname())) {
            existing.setLastname(investigator.getLastname());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getDepartment(), existing.getDepartment())) {
            existing.setDepartment(investigator.getDepartment());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getInstitution(), existing.getInstitution())) {
            existing.setInstitution(investigator.getInstitution());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getEmail(), existing.getEmail())) {
            existing.setEmail(investigator.getEmail());
            isDirty.set(true);
        }
        if (!StringUtils.equals(investigator.getPhone(), existing.getPhone())) {
            existing.setPhone(investigator.getPhone());
            isDirty.set(true);
        }

        if (!isDirty.get()) {
            return null;
        }

        final boolean saved;
        try {
            saved = SaveItemHelper.authorizedSave(existing, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.MODIFY_INVESTTGATOR));
        } catch (Exception e) {
            throw new XftItemException("Failed to save the investigator with ID " + investigatorId + ": " + investigator.toString(), e);
        }

        if (!saved) {
            log.error("Failed to save the investigator with ID {}. Check the logs for possible errors or exceptions.");
            throw new InitializationException("Failed to save the investigator with ID {}. Check the logs for possible errors or exceptions.");
        }

        XDAT.triggerXftItemEvent(existing, XftItemEventI.UPDATE, getInvestigatorEventProperties(investigatorId));
        return getInvestigator(investigator.getFirstname(), investigator.getLastname());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteInvestigator(final int investigatorId, final UserI user) throws InsufficientPrivilegesException, NotFoundException, XftItemException {
        if (!Roles.isSiteAdmin(user)) {
            throw new InsufficientPrivilegesException(user.getUsername(), XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ":ID = " + investigatorId);
        }
        final XnatInvestigatordata investigator = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (investigator == null) {
            throw new NotFoundException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ":ID = " + investigatorId);
        }
        try {
            final Map<String, Object> properties = getInvestigatorEventProperties(investigatorId);
            SaveItemHelper.authorizedDelete(investigator.getItem(), user, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.REMOVE_INVESTTGATOR));
            XDAT.triggerXftItemEvent(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, Integer.toString(investigatorId), XftItemEvent.DELETE, properties);
        } catch (Exception e) {
            throw new XftItemException("Failed to delete the investigator with ID " + investigatorId, e);
        }
    }

    final Map<String, Object> getInvestigatorEventProperties(final int investigatorId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("projects", _template.queryForList(QUERY_INVESTIGATOR_PROJECTS, new MapSqlParameterSource("investigatorId", investigatorId), String.class));
        return properties;
    }

    private static final String INVESTIGATOR_QUERY          = "SELECT " +
                                                              "  inv.xnat_investigatordata_id                                                                             AS xnat_investigatordata_id, " +
                                                              "  inv.id                                                                                                   AS id, " +
                                                              "  inv.title                                                                                                AS title, " +
                                                              "  inv.firstname                                                                                            AS firstname, " +
                                                              "  inv.lastname                                                                                             AS lastname, " +
                                                              "  inv.institution                                                                                          AS institution, " +
                                                              "  inv.department                                                                                           AS department, " +
                                                              "  inv.email                                                                                                AS email, " +
                                                              "  inv.phone                                                                                                AS phone, " +
                                                              "  (SELECT array(SELECT p.id " +
                                                              "                FROM xnat_projectdata p " +
                                                              "                WHERE p.pi_xnat_investigatordata_id = inv.xnat_investigatordata_id))                       AS primary_inv, " +
                                                              "  (SELECT array(SELECT pinv.xnat_projectdata_id " +
                                                              "                FROM xnat_projectdata_investigator pinv " +
                                                              "                WHERE pinv.xnat_investigatordata_xnat_investigatordata_id = inv.xnat_investigatordata_id)) AS inv " +
                                                              "FROM xnat_investigatordata inv";
    private static final String BY_ID_WHERE                 = " WHERE inv.xnat_investigatordata_id = :investigatorId";
    private static final String BY_FIRST_LAST_WHERE         = " WHERE inv.firstname = :firstName AND inv.lastname = :lastName";
    private static final String ORDER_BY_NAME               = " ORDER BY lastname, firstname";
    private static final String QUERY_INVESTIGATOR_PROJECTS =  "SELECT id AS project_id " +
                                                               "FROM xnat_projectdata " +
                                                               "WHERE pi_xnat_investigatordata_id = :investigatorId " +
                                                               "UNION " +
                                                               "SELECT xnat_projectdata_id AS project_id " +
                                                               "FROM xnat_projectdata_investigator " +
                                                               "WHERE xnat_investigatordata_xnat_investigatordata_id = :investigatorId";

    private static final RowMapper<Investigator> ROW_MAPPER = new RowMapper<Investigator>() {
        @Override
        public Investigator mapRow(final ResultSet resultSet, final int i) throws SQLException {
            return new Investigator(resultSet);
        }
    };

    private final NamedParameterJdbcTemplate _template;
}
