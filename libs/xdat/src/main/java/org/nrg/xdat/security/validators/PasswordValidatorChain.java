/*
 * core: org.nrg.xdat.security.validators.PasswordValidatorChain
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.validators;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class PasswordValidatorChain implements PasswordValidator {
    public static PasswordValidatorChain getTestInstance() {
        return new PasswordValidatorChain();
    }

    @Autowired
    public PasswordValidatorChain(final List<PasswordValidator> validators) {
        for (final PasswordValidator validator : validators) {
            if (!(validator instanceof PasswordValidatorChain)) {
                _validators.add(validator);
            }
        }
    }

    @Override
    public String isValid(String password, UserI user) {
        final StringBuilder buffer = new StringBuilder();
        for (final PasswordValidator validator : _validators) {
            final String message = validator.isValid(password, user);
            if (StringUtils.isNotBlank(message)) {
                buffer.append(message).append(" \n");
            }
        }
        return buffer.toString().trim();
    }

    /**
     * Private access level constructor is provided for "panic mode" instantiation when context can't be
     * found. Default values are then used for all preference settings.
     */
    private PasswordValidatorChain() {
        _validators.add(new RegExpValidator());
        _validators.add(new HistoricPasswordValidator());
    }

    private final List<PasswordValidator> _validators = Lists.newArrayList();
}
