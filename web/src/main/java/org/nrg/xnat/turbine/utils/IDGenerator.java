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
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.identifier.IDGeneratorFactory;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.services.XnatAppInfo;
import org.nrg.xnat.services.system.HostInfoService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
    public IDGenerator(final JdbcTemplate template, final SiteConfigPreferences preferences, final HostInfoService hostInfoService, final XnatAppInfo appInfo) {
        if (!ObjectUtils.allNotNull(template, preferences, hostInfoService, appInfo)) {
            throw new NrgServiceRuntimeException("The ID generator class must be created in an initialized application context, but at least one of JdbcTemplate, SiteConfigPreferences, or HostInfoService was null");
        }
        _template = template;
        _siteId = StringUtils.replaceChars(RegExUtils.removeAll(preferences.getSiteId(), "[ \"'^]"), '-', '_');
        _hostNumber = appInfo.hasMultipleActiveNodes() ? StringUtils.defaultIfBlank(hostInfoService.getHostNumber(), "") : "";
        if (StringUtils.isBlank(_hostNumber) && appInfo.hasMultipleActiveNodes()) {
            try {
                log.warn("The host number for this server isn't a number, but the application info indicates that this deployment has multiple active nodes. Check for an entry in xhbm_host_info where host_name is: {}", InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                log.error("The application info indicates that this deployment has multiple active nodes, but I couldn't get the host name due to an unexpected error", e);
            }
        }
        log.debug("Initializing ID generator with site ID {} and host number {}", _siteId, _hostNumber);
        setColumn(DEFAULT_COLUMN);
        setDigits(DEFAULT_DIGITS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String generateIdentifier() {
        final Set<String> offLimits = getClaimedIds();
        if (log.isTraceEnabled() && !offLimits.isEmpty()) {
            log.trace("Generating ID for site {} and table {} using template \"{}\" and {} off-limits IDs:\n * {}", getSiteId(), getTable(), getIdTemplate(), offLimits.size(), StringUtils.join(offLimits, "\n * "));
        } else {
            log.debug("Generating ID for site {} and table {} using template \"{}\" and {} off-limits IDs", getSiteId(), getTable(), getIdTemplate(), offLimits.size());
        }

        final AtomicInteger index      = new AtomicInteger(offLimits.size());
        final String        identifier = Stream.generate(index::incrementAndGet).map(this::format).filter(candidate -> !offLimits.contains(candidate)).findFirst().orElseThrow(() -> new NrgServiceRuntimeException("Found a candidate ID but then it didn't exist: this shouldn't happen"));
        log.debug("Found unused candidate {}, adding to claimed IDs", identifier);
        getClaimedIds().add(identifier);
        return identifier;
    }

    @Override
    public void setTable(final String table) {
        _table = StringUtils.lowerCase(table);
        _code = getDefaultCode(_table);
        log.debug("Set ID generator table to {} and code to {}", _table, _code);
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

    private String getQuery(final String template) {
        return String.format(QUERY_FORMAT, getColumn(), getTable(), template);
    }

    private String format(final int id) {
        return String.format(_format, getIdTemplate(), id);
    }

    private Set<String> getClaimedIds() {
        if (_claimedIds == null) {
            _claimedIds = new HashSet<>(_template.queryForList(getQuery(getIdTemplate()), String.class));
        }
        return _claimedIds;
    }

    private String getIdTemplate() {
        if (_idTemplate == null) {
            _idTemplate = _siteId + _hostNumber + "_" + getCode();
            log.debug("Set ID generator template to: {}", _idTemplate);
        }
        log.debug("Returning ID generator template to: {}", _idTemplate);
        return _idTemplate;
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

    private static final String QUERY_FORMAT = "SELECT DISTINCT %1$s FROM (SELECT %1$s FROM %2$s WHERE %1$s LIKE '%3$s%%' UNION SELECT DISTINCT %1$s FROM %2$s_history WHERE %1$s LIKE '%3$s%%') SEARCH";

    @Getter(PRIVATE)
    private final JdbcTemplate _template;
    @Getter(PRIVATE)
    private final String       _hostNumber;
    @Getter(PRIVATE)
    private final String       _siteId;

    private Set<String> _claimedIds = null;
    private String      _idTemplate = null;

    private String  _column;
    private String  _table;
    private Integer _digits;
    private String  _code;
    private String  _format;
}
