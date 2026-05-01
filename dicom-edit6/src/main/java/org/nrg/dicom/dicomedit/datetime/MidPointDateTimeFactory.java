package org.nrg.dicom.dicomedit.datetime;

import java.time.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MidPointDateTimeFactory implements OffsetDateTimeFactory {
    private static final String zoneOffsetRegex = "[+-]?(\\d{2})(\\d{2})";
    private static final Pattern zoneOffsetPattern = Pattern.compile( zoneOffsetRegex);

    @Override
    public OffsetTime time(DicomDateTime timeString) {
        int hour = 12;
        if( timeString.hasHour()) {
            hour = Integer.parseInt( timeString.getHour());
        }
        return time( hour, timeString);
    }

    private OffsetTime time(int hour, DicomDateTime timeString) {
        int minutes = 30;
        if (timeString.hasMinute()) {
            minutes = Integer.parseInt(timeString.getMinute());
        }
        return time(hour, minutes, timeString);
    }

    private OffsetTime time(int hour, int minutes, DicomDateTime timeString) {
        int seconds = 30;
        if (timeString.hasSeconds()) {
            seconds = Integer.parseInt(timeString.getSeconds());
        }
        return time(hour, minutes, seconds, timeString);
    }

    private OffsetTime time(int hour, int minute, int seconds, DicomDateTime dts) {
        // Not tracking fractional seconds in OffsetTime
        int fractionalSeconds = 0;
        return OffsetTime.of(hour, minute, seconds, 0, ZoneOffset.ofHours(0));
    }

    /**
     * Set the year if present, otherwise mid-year of 1900. 1900-07-01 00:00:00
     */
    @Override
    public OffsetDateTime dateTime( DicomDateTime dts) {
        int year = 1900;
        if( dts.hasYear()) {
            year = Integer.parseInt( dts.getYear());
        }
        return dateTime( year, dts);
    }

    private boolean isEven( int i) {
        return i % 2 == 0;
    }

    /**
     * Set the month if present, otherwise set time to the middle of the year: July 1, midnight.
     */
    private OffsetDateTime dateTime( int year, DicomDateTime dts) {
        int month = 0;
        if( dts.hasMonth()) {
            month = Integer.parseInt( dts.getMonth());
        }
        else {
            month = Month.JULY.getValue();
        }
        return dateTime( year,month, dts);
    }

    /**
     * Set the day if present, otherwise, if month is present, set day to the middle of the month. yyyy mm
     */
    private OffsetDateTime dateTime( int year, int month, DicomDateTime dts) {
        int day = 1;
        if( dts.hasDay()) {
            day = Integer.parseInt( dts.getDay());
        }
        else if( dts.hasMonth()){
            int daysInMonth = YearMonth.of( year, month).lengthOfMonth();
            day = (daysInMonth / 2) + 1;
        }
        return dateTime( year, month, day, dts);
    }

    /**
     * Set the hour if present, otherwise, if day is present, set hour to the middle of the day.
     */
    private OffsetDateTime dateTime( int year, int month, int day, DicomDateTime dts) {
        int hour = 0;
        if( dts.hasHour()) {
            hour = Integer.parseInt( dts.getHour());
        }
        else if( dts.hasDay()) {
            hour = 12;
        }
        else if( dts.hasMonth()) {
            int daysInMonth = YearMonth.of( year, month).lengthOfMonth();
            hour = isEven( daysInMonth)? 0: 12;
        }
        return dateTime( year, month, day, hour, dts);
    }

    /**
     * Set the minute if present, otherwise, if hour is present, set time to half-passed the hour.
     */
    private OffsetDateTime dateTime( int year, int month, int day, int hour, DicomDateTime dts) {
        int minutes = 0;
        if( dts.hasMinute()) {
            minutes = Integer.parseInt( dts.getMinute());
        }
        else if( dts.hasHour()) {
            minutes = 30;
        }
        return dateTime( year, month, day, hour, minutes, dts);
    }

    /**
     * Set the seconds if present, otherwise, if minutes is present, set seconds to mid-minute.
     */
    private OffsetDateTime dateTime( int year, int month, int day, int hour, int minute, DicomDateTime dts) {
        int seconds = 0;
        if( dts.hasSeconds()) {
            seconds = Integer.parseInt( dts.getSeconds());
        }
        else if( dts.hasMinute()) {
            seconds = 30;
        }
        return dateTime( year, month, day, hour, minute, seconds, dts);
    }

    /**
     * Set the fractional seconds.
     * Not using this in OffsetDateTime in favor of just passing the original fractional-seconds string field through.
     */
    private OffsetDateTime dateTime( int year, int month, int day, int hour, int minute, int seconds, DicomDateTime dts) {
        // Not tracking fractional seconds in OffsetDateTime
        int fractionalSeconds = 0;
        return dateTime( year, month, day, hour, minute, seconds, fractionalSeconds, dts);
    }

    /**
     * Set the zoneOffset.
     * This is needed to create the OffsetDateTime object but the ZoneOffset is not used in favor of just passing
     * the original zone-offset field through.
     */
    private OffsetDateTime dateTime( int year, int month, int day, int hour, int minute, int seconds, int fractionalSeconds, DicomDateTime dts) {
        ZoneOffset timeZoneOffset;
        if( dts.hasTzOffset()) {
            Matcher m = zoneOffsetPattern.matcher( dts.getTzOffset());
            timeZoneOffset = (m.matches())? ZoneOffset.ofHoursMinutes( Integer.parseInt( m.group(1)), Integer.parseInt( m.group(2))): ZoneOffset.ofHours(0);
        } else {
            timeZoneOffset = ZoneOffset.ofHours( 0);
        }
        return dateTime( year, month, day, hour, minute, seconds, fractionalSeconds, timeZoneOffset);
    }

    private OffsetDateTime dateTime( int year, int month, int day, int hour, int minute, int seconds, int fractionalSeconds, ZoneOffset tzOffset) {
        return OffsetDateTime.of( year, month, day, hour, minute, seconds, fractionalSeconds, tzOffset);
    }
}
