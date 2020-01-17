/*
 * web: org.nrg.xnat.turbine.utils.IDGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.services.system.HostInfoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static lombok.AccessLevel.PRIVATE;
import static org.nrg.xft.identifier.IDGeneratorFactory.DEFAULT_COLUMN;
import static org.nrg.xft.identifier.IDGeneratorFactory.DEFAULT_DIGITS;

/**
 * Standard XNAT implementation of the {@link IDGeneratorI} service.
 */
@Component
@Getter
@Setter
@Accessors(prefix = "_")
@Slf4j
public class IDGenerator implements IDGeneratorI {
    @SuppressWarnings("unused")
    public IDGenerator() {
        this(XDAT.getJdbcTemplate(), XDAT.getSiteConfigPreferences(), XDAT.getContextService().getBean(HostInfoService.class));
    }

    public IDGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService) {
        if (!ObjectUtils.allNotNull(template, preferences, hostInfoService)) {
            throw new NrgServiceRuntimeException("The ID generator class must be created in an initialized application context, but at least one of JdbcTemplate, SiteConfigPreferences, or HostInfoService was null");
        }
        _template = template;
        _siteId = StringUtils.replaceChars(RegExUtils.removeAll(preferences.getSiteId(), "[ \"'^]"), '-', '_');
        final String hostNumberValue = hostInfoService.getHostNumber();
        final int hostNumber = NumberUtils.isCreatable(hostNumberValue) ? NumberUtils.createInteger(hostNumberValue) : 0;
        _hostInfo = hostNumber > 1 ? hostNumber : null;
        setColumn(DEFAULT_COLUMN);
        setDigits(DEFAULT_DIGITS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String generateIdentifier() {
        final String template = _siteId + ObjectUtils.defaultIfNull(_hostInfo, "") + "_" + getCode();

        final Set<String> offLimits = new HashSet<>(getClaimedIds());
        offLimits.addAll(_template.queryForList(getQuery(template), String.class));

        if (log.isTraceEnabled() && !offLimits.isEmpty()) {
            log.debug("Generating ID for site {} and table {} using template \"{}\" and {} off-limits IDs:\n * {}", getSiteId(), getTable(), template, offLimits.size(), StringUtils.join(offLimits, "\n * "));
        } else {
            log.debug("Generating ID for site {} and table {} using template \"{}\" and {} off-limits IDs", getSiteId(), getTable(), template, offLimits.size());
        }

        final AtomicInteger count = new AtomicInteger(offLimits.size() + 1);
        String              candidate;
        do {
            candidate = format(template, count.getAndIncrement());
            log.trace("Generated candidate ID {}", candidate);
        } while (offLimits.contains(candidate));

        log.debug("Found unused candidate {}, adding to claimed IDs", candidate);
        getClaimedIds().add(candidate);
        return candidate;
    }

    @Override
    public void setTable(final String table) {
        _table = StringUtils.lowerCase(table);
        if (StringUtils.isBlank(_code)) {
            setCode(getDefaultCode(_table));
        }
    }

    @Override
    public void setColumn(final String column) {
        _column = StringUtils.lowerCase(column);
    }

    @Override
    public void setDigits(final Integer digits) {
        _digits = digits;
        _format = "%s%0" + _digits + "d";
    }

    private static String getDefaultCode(final String tableName) {
        switch (StringUtils.lowerCase(tableName)) {
            case IDGeneratorFactory.KEY_SUBJECTS:
                return "S";
            case IDGeneratorFactory.KEY_VISITS:
                return "V";
            case IDGeneratorFactory.KEY_EXPERIMENTS:
                return "E";
            default:
                log.warn("I don't have a default code for the table {}, returning \"E\" for experiment but this may not be what you want.", tableName);
                return "E";
        }
    }

    private String getQuery(final String template) {
        return String.format(QUERY_FORMAT, getColumn(), getTable(), template);
    }

    private String format(final String template, final int id) {
        return String.format(_format, template, id);
    }

    private static final String    QUERY_FORMAT = "SELECT DISTINCT %1$s FROM (SELECT %1$s FROM %2$s WHERE %1$s LIKE '%3$s%%' UNION SELECT DISTINCT %1$s FROM %2$s_history WHERE %1$s LIKE '%3$s%%') SEARCH";

    @Getter(PRIVATE)
    private final JdbcTemplate _template;
    @Getter(PRIVATE)
    private final Integer      _hostInfo;
    @Getter(PRIVATE)
    private final String       _siteId;

    @Getter(PRIVATE)
    private final List<String> _claimedIds = new ArrayList<>();

    private String  _column;
    private String  _table;
    private Integer _digits;
    private String  _code;

    private String _format;
}
