/*
 * org.nrg.xdat.security.RegExpValidator
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class RegExpValidator implements PasswordValidator {
    @Override
    public boolean isValid(String password, UserI user) {
        final String regexp = _preferences.getPasswordComplexity();
        return StringUtils.isBlank(regexp) || Pattern.matches(regexp, password);
    }

    @Override
    public String getMessage() {
        return StringUtils.defaultIfBlank(_preferences.getPasswordComplexityMessage(), "Password is not sufficiently complex.");
    }

    @Autowired
    @Lazy
    private SiteConfigPreferences _preferences;
}
