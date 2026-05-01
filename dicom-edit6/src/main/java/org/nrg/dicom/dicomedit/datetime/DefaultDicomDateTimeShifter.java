package org.nrg.dicom.dicomedit.datetime;

import org.nrg.dicom.dicomedit.DicomDateTimeShifter;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * DefaultDateTimeShifter
 *
 * Missing precision is filled in with the midpoint in the missing fields' range.
 * This sets the exact time from which shifts are computed.
 * The output has the same precision as the input.
 * The optional time-zone offset field is not considered in shifts but is carried through if present.
 * Fractional seconds and time-zone offset are not applied.
 *
 * OffsetDateTime is far superior to Calendar for computing time shifts.
 *
 */
public class DefaultDicomDateTimeShifter implements DicomDateTimeShifter {

    private final MidPointDateTimeFactory midPointDateTimeFactory;

    private static final DateTimeFormatter fracFormatter = new DateTimeFormatterBuilder()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true)
            .toFormatter();
    private static final DateTimeFormatter tzFormatter = new DateTimeFormatterBuilder()
            .appendPattern("x")
            .toFormatter();
    // This is a DateTimeFormatter for DICOM DT VR format. It is not used but is left here for possible future use.
    private static final DateTimeFormatter dtFormatter = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4)
            .appendPattern( "[MM[dd[HH[mm[ss]]]]]")
            .appendOptional( fracFormatter)
            .appendOptional( tzFormatter)
            .toFormatter();

    public DefaultDicomDateTimeShifter() {
        this.midPointDateTimeFactory = new MidPointDateTimeFactory();
    }

    @Override
    public String shiftTimeBySeconds(String dicomTimeString, long seconds) {
        DicomDateTime dicomDateTime =  DicomDateTimeFactory.fromTimeString( dicomTimeString)
                .orElseThrow( () -> new ScriptEvaluationRuntimeException( String.format( "Failed to parse DICOM time string '%s'", dicomTimeString)));
        OffsetTime ot = midPointDateTimeFactory.time(dicomDateTime);
        return dicomDateTime.toDicomTimeString( ot.plusSeconds(seconds));
    }

    @Override
    public String shiftDateTimeBySeconds(String dicomDateTimeString, long seconds) {
        DicomDateTime dicomDateTime =  DicomDateTimeFactory.fromDateTimeString( dicomDateTimeString)
                .orElseThrow( () -> new ScriptEvaluationRuntimeException( String.format( "Failed to parse DICOM time string '%s'", dicomDateTimeString)));
        OffsetDateTime odt = midPointDateTimeFactory.dateTime(dicomDateTime);
        return dicomDateTime.toDicomDateTimeString( odt.plusSeconds(seconds));
    }

}
