/*
 * spawner: org.nrg.xnat.spawner.validation.standard.Interval
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.validation.standard;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.spawner.validation.SpawnerValidationService;
import org.nrg.xnat.spawner.validation.Validated;
import org.nrg.xnat.spawner.validation.Validation;
import org.nrg.xnat.spawner.validation.Validator;
import org.postgresql.util.PGInterval;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Validation(id = "interval", set = SpawnerValidationService.STANDARD_SET)
public class Interval implements Validator {
    @Override
    public Validated validate(final String value) {
        try {
            if (StringUtils.isBlank(value)) {
                return new Validated(false, "No valid interval was specified.");
            }

            // Now convert to an interval.
            final PGInterval interval = new PGInterval(value);

            // If everything's 0...
            if (interval.getYears() == 0 &&
                interval.getMonths() == 0 &&
                interval.getDays() == 0 &&
                interval.getHours() == 0 &&
                interval.getMinutes() == 0 &&
                interval.getSeconds() == 0) {
                final String normalized = value.toLowerCase().trim();
                final Matcher matcher = PATTERN_ZERO_INTERVAL.matcher(normalized);
                if (matcher.matches()) {
                    return Validated.SUCCESS;
                }

                return new Validated(false, "The submitted value doesn't contain a valid interval: " + value + ". An interval should be in the form \"A years B months C days D hours E minutes F seconds\".");
            }
        } catch (SQLException ignored) {
            // This is ignored because it doesn't actually happen.
        }

        return Validated.SUCCESS;
    }

    private static final String ZERO_INTERVAL = "0 (years|months|days|hours|minutes|seconds)";
    private static final Pattern PATTERN_ZERO_INTERVAL = Pattern.compile(Interval.ZERO_INTERVAL);
}
