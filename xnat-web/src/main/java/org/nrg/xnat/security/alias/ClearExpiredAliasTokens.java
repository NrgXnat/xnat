/*
 * org.nrg.xnat.security.alias.ClearExpiredAliasTokensJob
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.security.alias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

public class ClearExpiredAliasTokens implements Runnable {
    public ClearExpiredAliasTokens(final JdbcTemplate template, final String timeout) {
        if (_log.isDebugEnabled()) {
            _log.debug("Initializing the alias token sweeper job with an interval of: " + timeout);
        }

        _template = template;
        _timeout = timeout;
    }

    /**
     */
    @Override
    public void run() {
        if (_log.isDebugEnabled()) {
            _log.debug("Executing alias token sweep function");
        }
        for (final String format : ALIAS_TOKEN_QUERIES) {
            final String query = String.format(format, _timeout);
            if (_log.isDebugEnabled()) {
                _log.debug("Executing alias token sweep query: " + query);
            }
            _template.execute(query);
        }
    }

    private static final Logger       _log                            = LoggerFactory.getLogger(ClearExpiredAliasTokens.class);
    private static final String       QUERY_DELETE_TOKEN_IP_ADDRESSES = "DELETE FROM xhbm_alias_token_validipaddresses WHERE alias_token in (SELECT id FROM xhbm_alias_token WHERE created < NOW() - INTERVAL '%s')";
    private static final String       QUERY_DELETE_ALIAS_TOKENS       = "DELETE FROM xhbm_alias_token WHERE created < NOW() - INTERVAL '%s'";
    private static final List<String> ALIAS_TOKEN_QUERIES             = Arrays.asList(QUERY_DELETE_TOKEN_IP_ADDRESSES, QUERY_DELETE_ALIAS_TOKENS);

    private final JdbcTemplate _template;
    private final String       _timeout;
}
