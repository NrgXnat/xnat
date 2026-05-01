package org.nrg.dicom.dicomedit.datetime;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DicomDateTimeFactory {
    private static final String dateTimeRegex = "(?<year>\\d{4})(?<month>\\d{2})?(?<day>\\d{2})?(?<hour>\\d{2})?(?<minute>\\d{2})?(?<second>\\d{2})?(?<fraction>\\.\\d{1,6})?(?<timezone>[+-]\\d{4})?";

    private static final String dateRegex = "(?<year>\\d{4})(?<month>\\d{2})?(?<day>\\d{2})?";
    private static final String timeRegex = "(?<hour>\\d{2})?(?<minute>\\d{2})?(?<second>\\d{2})?(?<fraction>\\.\\d{1,6})?";
    private static final Pattern dateTimePattern = Pattern.compile( dateTimeRegex);
    private static final Pattern datePattern = Pattern.compile( dateRegex);
    private static final Pattern timePattern = Pattern.compile( timeRegex);

    public static Optional<DicomDateTime> fromDateTimeString(String dateTimeString) {
        if(StringUtils.isNotEmpty( dateTimeString)) {
            Matcher m = dateTimePattern.matcher( dateTimeString);
            if( m.matches()) {
                return Optional.of( DicomDateTime.builder()
                        .year( m.group("year"))
                        .month( ObjectUtils.defaultIfNull(m.group("month"), ""))
                        .day( ObjectUtils.defaultIfNull(m.group("day"), ""))
                        .hour( ObjectUtils.defaultIfNull(m.group("hour"), ""))
                        .minute( ObjectUtils.defaultIfNull(m.group("minute"), ""))
                        .seconds( ObjectUtils.defaultIfNull(m.group("second"), ""))
                        // This includes the decimal point.
                        .fractionalSeconds( ObjectUtils.defaultIfNull(m.group("fraction"), ""))
                        .tzOffset( ObjectUtils.defaultIfNull(m.group("timezone"), ""))
                        .build());
            }
        }
        return Optional.empty();
    }
    public static Optional<DicomDateTime> fromDateString(String dateString) {
        if(StringUtils.isNotEmpty( dateString)) {
            Matcher m = datePattern.matcher( dateString);
            if( m.matches()) {
                return Optional.of( DicomDateTime.builder()
                        .year( m.group("year"))
                        .month( ObjectUtils.defaultIfNull(m.group("month"), ""))
                        .day( ObjectUtils.defaultIfNull(m.group("day"), ""))
                        .build());
            }
        }
        return Optional.empty();
    }
    public static Optional<DicomDateTime> fromTimeString(String timeString) {
        if(StringUtils.isNotEmpty( timeString)) {
            Matcher m = timePattern.matcher( timeString);
            if( m.matches()) {
                return Optional.of( DicomDateTime.builder()
                        .hour( m.group("hour"))
                        .minute( ObjectUtils.defaultIfNull(m.group("minute"), ""))
                        .seconds( ObjectUtils.defaultIfNull(m.group("second"), ""))
                        // This includes the decimal point.
                        .fractionalSeconds( ObjectUtils.defaultIfNull(m.group("fraction"), ""))
                        .build());
            }
        }
        return Optional.empty();
    }
}
