/*
 * web: org.nrg.xnat.services.validation.DateValidation
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.validation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.time.DateUtils;
import org.nrg.config.exceptions.SiteConfigurationException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.nrg.framework.services.SerializerService.TYPE_REF_MAP_STRING_LIST_STRING;

@Service
public class DateValidation {
    @Autowired
    public DateValidation(final SerializerService serializer) throws IOException {
        final Resource resource = BasicXnatResourceLocator.getResource("classpath:META-INF/xnat/validation/date-time-elements.yaml");
        try (final InputStream input = resource.getInputStream()) {
            final HashMap<String, ArrayList<String>> formats = serializer.deserializeYaml(input, TYPE_REF_MAP_STRING_LIST_STRING);
            DATE_FORMATS = ImmutableList.copyOf(formats.get("dateFormats"));
            DATE_TIME_FORMATS = ImmutableList.copyOf(formats.get("dateTimeFormats"));
        }
    }

    public Date parseDate(final String date) throws ParseException {
        try {
            return DateUtils.parseDateStrictly(date, DATE_FORMATS.toArray(new String[DATE_FORMATS.size()]));
        } catch (ParseException e) {
            return DateUtils.parseDateStrictly(date, DATE_TIME_FORMATS.toArray(new String[DATE_TIME_FORMATS.size()]));
        }
    }

    public String convertDateToLongString(final String date) throws SiteConfigurationException {
        try {
            return Long.toString(parseDate(date).getTime());
        } catch (ParseException e) {
            throw new SiteConfigurationException(NrgServiceError.ConfigurationError, "The specified date \"" + date + "\" is in an invalid format. This should use one of the formats: " + Joiner.on(", ").join(DATE_FORMATS), e);
        }
    }

    private final List<String> DATE_FORMATS;
    private final List<String> DATE_TIME_FORMATS;
}
