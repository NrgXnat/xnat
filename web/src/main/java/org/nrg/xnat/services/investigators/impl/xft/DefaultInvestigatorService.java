/*
 * web: org.nrg.xnat.services.investigators.InvestigatorService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.investigators.impl.xft;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.*;
import org.nrg.xapi.model.xft.Investigator;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.om.XnatInvestigatordata;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.exception.*;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.XFTValidator;
import org.nrg.xnat.services.investigators.InvestigatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Manages operations with {@link Investigator investigator proxy objects}. This is not a full-on Hibernate service,
 * since the "entities" managed are not Hibernate entities but instead are composite objects that represent XFT {@link
 * XnatInvestigatordataI} objects as well as metadata aggregated from other tables.
 */
@Service
@Slf4j
public class DefaultInvestigatorService implements InvestigatorService {
    public static final String COL_XNAT_INVESTIGATORDATA_ID = "xnat_investigatordata_id";
    public static final String COL_ID                       = "id";
    public static final String COL_INSERT_DATE              = "insert_date";
    public static final String COL_INSERT_USERNAME          = "insert_username";
    public static final String COL_TITLE                    = "title";
    public static final String COL_FIRSTNAME                = "firstname";
    public static final String COL_LASTNAME                 = "lastname";
    public static final String COL_INSTITUTION              = "institution";
    public static final String COL_DEPARTMENT               = "department";
    public static final String COL_EMAIL                    = "email";
    public static final String COL_PHONE                    = "phone";
    public static final String COL_PRIMARY_PROJECTS         = "primary_projects";
    public static final String COL_PROJECTS                 = "projects";

    @Autowired
    public DefaultInvestigatorService(final NamedParameterJdbcTemplate template) {
        _template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator createInvestigator(final Investigator investigator, final UserI user) throws ResourceAlreadyExistsException, DataFormatException, XftItemException {
        try {
            getInvestigator(investigator.getFirstname(), investigator.getLastname());
            throw new ResourceAlreadyExistsException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, investigator.getFirstname() + " " + investigator.getLastname());
        } catch (NotFoundException ignored) {
            // Do nothing here: this is actually what we want.
        }

        final XFTItem item;
        try {
            item = XFTItem.NewItem(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, user);
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".title", investigator.getTitle());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".firstname", investigator.getFirstname());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".lastname", investigator.getLastname());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".department", investigator.getDepartment());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".institution", investigator.getInstitution());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".email", investigator.getEmail());
            item.setProperty(XnatInvestigatordata.SCHEMA_ELEMENT_NAME + ".phone", investigator.getPhone());
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException | InvalidValueException e) {
            throw createServiceException(investigator, "create", e);
        }

        final ValidationResults results;
        try {
            results = XFTValidator.Validate(item);
        } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
            throw createServiceException(investigator, "create", e);
        }

        if (!results.isValid()) {
            throw new DataFormatException("Failed to create the investigator " + investigator.toString() + " due to the following errors:\n" + results.toFullString());
        }

        try {
            if (!SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.CREATE_INVESTTGATOR, EventUtils.CREATE_INVESTTGATOR, EventUtils.CREATE_INVESTTGATOR))) {
                log.error("Failed to create a new investigator \"{}\" for user {}. Check the logs for possible errors or exceptions.", investigator, user.getUsername());
                return null;
            }

            return getInvestigator(investigator.getFirstname(), investigator.getLastname());
        } catch (Exception e) {
            throw createServiceException(investigator, "create", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final int investigatorId) {
        return _template.queryForObject(QUERY_INVESTIGATOR_EXISTS_BY_ID, new MapSqlParameterSource(PARAM_INVESTIGATOR_ID, investigatorId), Boolean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean exists(final String firstName, final String lastName) {
        return _template.queryForObject(QUERY_INVESTIGATOR_EXISTS_BY_FIRST_AND_LAST, new MapSqlParameterSource(PARAM_FIRST_NAME, firstName).addValue(PARAM_LAST_NAME, lastName), Boolean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator getInvestigator(final int investigatorId) throws NotFoundException {
        try {
            return _template.queryForObject(INVESTIGATOR_QUERY_BY_ID, new MapSqlParameterSource(PARAM_INVESTIGATOR_ID, investigatorId), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw createNotFoundException(investigatorId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator getInvestigator(final String firstName, final String lastName) throws NotFoundException {
        try {
            return _template.queryForObject(INVESTIGATOR_QUERY_BY_FIRST_LAST, new MapSqlParameterSource(PARAM_FIRST_NAME, firstName).addValue(PARAM_LAST_NAME, lastName), ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw createNotFoundException(ImmutableMap.<String, Object>builder().put(PARAM_FIRST_NAME, firstName).put(PARAM_LAST_NAME, lastName).build());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Investigator> getInvestigators() {
        return _template.query(INVESTIGATOR_QUERY_ORDER_BY_LAST_FIRST, ROW_MAPPER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Investigator updateInvestigator(final int investigatorId, final Investigator investigator, final UserI user) throws NotFoundException, InitializationException, XftItemException {
        final XnatInvestigatordata existing = XnatInvestigatordata.getXnatInvestigatordatasByXnatInvestigatordataId(investigatorId, user, false);
        if (existing == null) {
            throw createNotFoundException(investigatorId);
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
            saved = SaveItemHelper.authorizedSave(existing, user, false, false, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.MODIFY_INVESTTGATOR, EventUtils.MODIFY_INVESTTGATOR, EventUtils.MODIFY_INVESTTGATOR));
        } catch (Exception e) {
            throw createServiceException(investigator, "save", e);
        }

        if (!saved) {
            throw new InitializationException("Failed to save the investigator with ID " + investigatorId + ". Check the logs for possible errors or exceptions.");
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
            throw createNotFoundException(investigatorId);
        }
        try {
            final Map<String, Object> properties = getInvestigatorEventProperties(investigatorId);
            SaveItemHelper.authorizedDelete(investigator.getItem(), user, EventUtils.newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.TYPE.REST, EventUtils.REMOVE_INVESTTGATOR));
            XDAT.triggerXftItemEvent(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, Integer.toString(investigatorId), XftItemEvent.DELETE, properties);
        } catch (Exception e) {
            throw createServiceException(new Investigator(investigator), "delete", e);
        }
    }

    private Map<String, Object> getInvestigatorEventProperties(final int investigatorId) {
        final Map<String, Object> properties = new HashMap<>();
        properties.put("projects", _template.queryForList(QUERY_INVESTIGATOR_PROJECTS, new MapSqlParameterSource(PARAM_INVESTIGATOR_ID, investigatorId), String.class));
        return properties;
    }

    private static NotFoundException createNotFoundException(final int id) {
        return new NotFoundException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, "ID = " + id);
    }

    private static NotFoundException createNotFoundException(final Map<String, Object> parameters) {
        return new NotFoundException(XnatInvestigatordata.SCHEMA_ELEMENT_NAME, parameters.entrySet().stream().map(entry -> entry.getKey() + " = " + entry.getValue()).collect(Collectors.joining(", ")));
    }

    private static XftItemException createServiceException(final Investigator investigator, final String operation, final Throwable e) {
        return new XftItemException("Failed to " + operation + " the investigator: " + investigator.toString(), e);
    }

    private static final RowMapper<Investigator> ROW_MAPPER                                  = (resultSet, i) -> new Investigator(resultSet);
    private static final String                  PARAM_INVESTIGATOR_ID                       = "investigatorId";
    private static final String                  PARAM_FIRST_NAME                            = "firstName";
    private static final String                  PARAM_LAST_NAME                             = "lastName";
    private static final String                  INVESTIGATOR_QUERY                          = "SELECT " +
                                                                                               "  inv.xnat_investigatordata_id                                                                             AS " + COL_XNAT_INVESTIGATORDATA_ID + ", " +
                                                                                               "  inv.id                                                                                                   AS " + COL_ID + ", " +
                                                                                               "  inv.title                                                                                                AS " + COL_TITLE + ", " +
                                                                                               "  inv.firstname                                                                                            AS " + COL_FIRSTNAME + ", " +
                                                                                               "  inv.lastname                                                                                             AS " + COL_LASTNAME + ", " +
                                                                                               "  inv.institution                                                                                          AS " + COL_INSTITUTION + ", " +
                                                                                               "  inv.department                                                                                           AS " + COL_DEPARTMENT + ", " +
                                                                                               "  inv.email                                                                                                AS " + COL_EMAIL + ", " +
                                                                                               "  inv.phone                                                                                                AS " + COL_PHONE + ", " +
                                                                                               "  (SELECT array(SELECT p.id " +
                                                                                               "                FROM xnat_projectdata p " +
                                                                                               "                WHERE p.pi_xnat_investigatordata_id = inv.xnat_investigatordata_id))                       AS " + COL_PRIMARY_PROJECTS + ", " +
                                                                                               "  (SELECT array(SELECT pinv.xnat_projectdata_id " +
                                                                                               "                FROM xnat_projectdata_investigator pinv " +
                                                                                               "                WHERE pinv.xnat_investigatordata_xnat_investigatordata_id = inv.xnat_investigatordata_id)) AS " + COL_PROJECTS + ", " +
                                                                                               "  m.insert_date                                                                                            AS " + COL_INSERT_DATE + ", " +
                                                                                               "  u.login                                                                                                  AS " + COL_INSERT_USERNAME + " " +
                                                                                               "FROM xnat_investigatordata inv " +
                                                                                               "        LEFT JOIN xnat_investigatordata_meta_data m ON inv.investigatordata_info = m.meta_data_id " +
                                                                                               "        LEFT JOIN xdat_user u ON m.insert_user_xdat_user_id = u.xdat_user_id";
    private static final String                  BY_ID_WHERE                                 = "WHERE inv.xnat_investigatordata_id = :" + PARAM_INVESTIGATOR_ID;
    private static final String                  BY_FIRST_LAST_WHERE                         = "WHERE inv.firstname = :" + PARAM_FIRST_NAME + " AND inv.lastname = :" + PARAM_LAST_NAME;
    private static final String                  ORDER_BY_NAME                               = "ORDER BY lastname, firstname";
    private static final String                  INVESTIGATOR_QUERY_BY_ID                    = INVESTIGATOR_QUERY + " " + BY_ID_WHERE;
    private static final String                  INVESTIGATOR_QUERY_BY_FIRST_LAST            = INVESTIGATOR_QUERY + " " + BY_FIRST_LAST_WHERE;
    private static final String                  INVESTIGATOR_QUERY_ORDER_BY_LAST_FIRST      = INVESTIGATOR_QUERY + " " + ORDER_BY_NAME;
    private static final String                  QUERY_INVESTIGATOR_EXISTS_BY_FIRST_AND_LAST = "SELECT EXISTS(SELECT inv.xnat_investigatordata_id FROM xnat_investigatordata inv " + BY_FIRST_LAST_WHERE + ")";
    private static final String                  QUERY_INVESTIGATOR_EXISTS_BY_ID             = "SELECT EXISTS(SELECT inv.xnat_investigatordata_id FROM xnat_investigatordata inv " + BY_ID_WHERE + ")";
    private static final String                  QUERY_INVESTIGATOR_PROJECTS                 = "SELECT id AS project_id " +
                                                                                               "FROM xnat_projectdata " +
                                                                                               "WHERE pi_xnat_investigatordata_id = :" + PARAM_INVESTIGATOR_ID + " " +
                                                                                               "UNION " +
                                                                                               "SELECT xnat_projectdata_id AS project_id " +
                                                                                               "FROM xnat_projectdata_investigator " +
                                                                                               "WHERE xnat_investigatordata_xnat_investigatordata_id = :" + PARAM_INVESTIGATOR_ID;

    private final NamedParameterJdbcTemplate _template;
}
