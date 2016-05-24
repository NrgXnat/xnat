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

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidatorChain implements PasswordValidator {
    public PasswordValidatorChain() {
    }

    @Override
    public boolean isValid(String password, UserI user) {
        boolean             ret    = true;
        final StringBuilder buffer = new StringBuilder();
        List<PasswordValidator> validators = getValidators();
        if (validators != null) {
            for (final PasswordValidator validator : validators) {
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
        ArrayList<PasswordValidator> validators = new ArrayList<PasswordValidator>();
        validators.add(XDAT.getContextService().getBean("regexValidator", RegExpValidator.class));
        if(StringUtils.equals(XDAT.getSiteConfigPreferences().getPasswordReuseRestriction(),"Historical")){
            validators.add(XDAT.getContextService().getBean("historicPasswordValidator", HistoricPasswordValidator.class));
        }
        return validators;
    }

    private       String                  message;
}
