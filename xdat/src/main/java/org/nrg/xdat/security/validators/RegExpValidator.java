/*
 * core: org.nrg.xdat.security.validators.RegExpValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Component
public class RegExpValidator implements PasswordValidator {
    @Autowired
    public RegExpValidator(final SiteConfigPreferences preferences) {
        _preferences = preferences;
    }

    /**
     * Package-protected access level constructor is provided for "panic mode" instantiation when context can't be
     * found. Default values are then used for all preference settings.
     */
    RegExpValidator() {
        _preferences = null;
    }

    @Override
    public String isValid(final String password, final UserI user) {
        // bcrypt only hashes the first 72 bytes of a password, and CVE-2025-22228 means anything beyond that is
        // ignored on comparison as well: two passwords sharing a 72-byte prefix both authenticate. Reject rather
        // than silently truncate, so a user is never given credentials whose tail does not count.
        if (password != null && password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            return "Password must be " + MAX_PASSWORD_BYTES + " characters or fewer.";
        }

        final String regexp = getPasswordComplexity();
        return StringUtils.isBlank(regexp) || Pattern.matches(regexp, password)
               ? ""
               : StringUtils.defaultIfBlank(getPasswordComplexityMessage(), "Password is not sufficiently complex.");
    }

    private String getPasswordComplexity() {
        return _preferences != null ? _preferences.getPasswordComplexity() : "^.*$";
    }

    private String getPasswordComplexityMessage() {
        return _preferences != null ? _preferences.getPasswordComplexityMessage() : "Password is not sufficiently complex.";
    }

    /**
     * The maximum number of bytes bcrypt incorporates into a hash. Passwords longer than this are rejected: see
     * CVE-2025-22228.
     */
    private static final int MAX_PASSWORD_BYTES = 72;

    private final SiteConfigPreferences _preferences;
}
