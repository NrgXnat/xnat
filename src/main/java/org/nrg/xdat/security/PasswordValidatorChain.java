/*
 * org.nrg.xdat.security.PasswordValidatorChain
 * XNAT http://www.xnat.org
 * Copyright (c) 2015, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */
package org.nrg.xdat.security;

import java.util.List;

import org.nrg.xft.security.UserI;

public class PasswordValidatorChain implements PasswordValidator {
    public PasswordValidatorChain(final List<PasswordValidator> validators) {
        _validators = validators;
    }

    @Override
    public boolean isValid(String password, UserI user) {
        boolean ret = true;
        final StringBuilder buffer = new StringBuilder();
        if (_validators != null) {
            for (final PasswordValidator validator : _validators) {
                if (!validator.isValid(password, user)) {
                    buffer.append(validator.getMessage()).append(" \n");
                    ret = false;
                }
            }
        }
        message = buffer.toString();
        return ret;

    }

    public String getMessage() {
        return message;
    }

    public List<PasswordValidator> getValidators() {
        return _validators;
    }

    private final List<PasswordValidator> _validators;
    private String message;
}
