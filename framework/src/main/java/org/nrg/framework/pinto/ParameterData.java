/*
 * framework: org.nrg.framework.pinto.ParameterData
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
@Slf4j
public class ParameterData {
    public ParameterData(final Method method, Parameter parameter) {
        log.debug("Creating new parameter data object:\n *** Short option:  {}\n *** Long option:   {}\n *** Help text:     {}\n *** Expected type: {}", parameter.value(), parameter.longOption(), parameter.help(), parameter.type());
        _method           = method;
        _shortOption      = parameter.value();
        _longOption       = parameter.longOption();
        _defaultValue     = parameter.defaultValue();
        _argCount         = parameter.argCount();
        _exactArgCount    = parameter.exactArgCount();
        _multiplesAllowed = parameter.multiplesAllowed();
        _help             = parameter.help();
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getMultiplesAllowed() {
        return _multiplesAllowed;
    }

    public String getHelp() {
        return _help;
    }

    private final Method   _method;
    private final String   _shortOption;
    private final String   _longOption;
    private final String   _defaultValue;
    private final ArgCount _argCount;
    private final int      _exactArgCount;
    private final boolean  _multiplesAllowed;
    private final String   _help;
}
