/*
 * web: org.nrg.xnat.event.listeners.methods.InitializedHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.xapi.exceptions.InitializationException;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DateTimeFormatPreferencesHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    private static final String TIME_FORMAT              = "uiTimeFormat";
    private static final String DATE_FORMAT              = "uiDateFormat";
    private static final String DATE_TIME_FORMAT         = "uiDateTimeFormat";
    private static final String DATE_TIME_SECONDS_FORMAT = "uiDateTimeSecondsFormat";

    @Autowired
    public DateTimeFormatPreferencesHandlerMethod(final XnatUserProvider primaryAdminUserProvider) {
        super(primaryAdminUserProvider, TIME_FORMAT, DATE_FORMAT, DATE_TIME_FORMAT, DATE_TIME_SECONDS_FORMAT);
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        if (!StringUtils.equalsAny(preference, TIME_FORMAT, DATE_FORMAT, DATE_TIME_FORMAT, DATE_TIME_SECONDS_FORMAT)) {
            log.warn("Received preference update for unrecognized preference: {}", preference);
            return;
        }
        final String output;
        switch (preference) {
            case TIME_FORMAT:
                output = TurbineUtils.resetTimeFormat();
                break;
            case DATE_FORMAT:
                output = TurbineUtils.resetDateFormat();
                break;
            case DATE_TIME_FORMAT:
                output = TurbineUtils.resetDateTimeFormat();
                break;
            case DATE_TIME_SECONDS_FORMAT:
                output = TurbineUtils.resetDateTimeSecondsFormat();
                break;
            default:
                throw new NrgServiceRuntimeException(new InitializationException("Unrecognized preference: " + preference));
        }
        log.info("Preference {} updated to {}. Resetting date/time format to {}", preference, value, output);
    }
}
