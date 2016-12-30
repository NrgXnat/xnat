/*
 * core: org.nrg.xdat.security.RegExpValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
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
    public boolean isValid(String password, UserI user) {
        final String regexp = getPasswordComplexity();
        return StringUtils.isBlank(regexp) || Pattern.matches(regexp, password);
    }

    @Override
    public String getMessage() {
        return StringUtils.defaultIfBlank(getPasswordComplexityMessage(), "Password is not sufficiently complex.");
    }

    private String getPasswordComplexity() {
        return _preferences != null ? _preferences.getPasswordComplexity() : "^.*$";
    }

    private String getPasswordComplexityMessage() {
        return _preferences != null ? _preferences.getPasswordComplexityMessage() : "Password is not sufficiently complex.";
    }

    private final SiteConfigPreferences _preferences;
}
