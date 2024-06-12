/*
 * framework: org.nrg.framework.pinto.ParameterData
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class ParameterData {

public ParameterData(final Method method, Parameter parameter) {
        if (_log.isDebugEnabled()) {
            _log.debug("Creating new parameter data object:");
            _log.debug(" *** Short option:  " + parameter.value());
            _log.debug(" *** Long option:   " + parameter.longOption());
            _log.debug(" *** Help text:     " + parameter.help());
            _log.debug(" *** Expected type: " + parameter.type());
        }

        _method = method;
        _shortOption = parameter.value();
        _longOption = parameter.longOption();
        _defaultValue = parameter.defaultValue();
        _argCount = parameter.argCount();
        _exactArgCount = parameter.exactArgCount();
        _multiplesAllowed = parameter.multiplesAllowed();
        _help = parameter.help();
    }

    public Method getMethod() {
        return _method;
    }

    public String getShortOption() {
        return _shortOption;
    }

    public boolean hasLongOption() {
        return !StringUtils.isBlank(_longOption);
    }

    public String getLongOption() {
        return _longOption;
    }

    public boolean hasDefaultValue() {
        return !StringUtils.isBlank(_defaultValue);
    }

    public String getDefaultValue() {
        return _defaultValue;
    }

    public ArgCount getArgCount() {
        return _argCount;
    }

    public int getExactArgCount() {
        return _exactArgCount;
    }

    public boolean getMultiplesAllowed() {
        return _multiplesAllowed;
    }

    public String getHelp() {
        return _help;
    }

    private static final Logger _log = LoggerFactory.getLogger(ParameterData.class);

    private final Method _method;
    private final String _shortOption;
    private final String _longOption;
    private final String _defaultValue;
    private final ArgCount _argCount;
    private final int _exactArgCount;
    private final boolean _multiplesAllowed;
    private final String _help;
}

