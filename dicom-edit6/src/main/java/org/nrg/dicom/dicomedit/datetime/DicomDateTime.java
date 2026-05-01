package org.nrg.dicom.dicomedit.datetime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.time.OffsetTime;

@Getter
@Setter
@Builder
public class DicomDateTime {
    private String year;
    private String month;
    private String day;
    private String hour;
    private String minute;
    private String seconds;
    private String fractionalSeconds;
    private String tzOffset;

    public boolean hasYear() { return StringUtils.isNotBlank(year);}
    public boolean hasMonth() { return StringUtils.isNotBlank(month);}
    public boolean hasDay() { return StringUtils.isNotBlank(day);}
    public boolean hasHour() { return StringUtils.isNotBlank(hour);}
    public boolean hasMinute() { return StringUtils.isNotBlank(minute);}
    public boolean hasSeconds() { return StringUtils.isNotBlank(seconds);}
    public boolean hasFractionalSeconds() { return StringUtils.isNotBlank(fractionalSeconds);}
    public boolean hasTzOffset() { return StringUtils.isNotBlank(tzOffset);}

    /**
     * Return the OffsetDateTime as a DICOM-date string with the same precision as this DicomDateTime.
     *
     * @param odt OffsetDateTime
     * @return DICOM-DA formatted string
     */
    public String toDicomDateString(OffsetDateTime odt) {
        StringBuilder sb = new StringBuilder();
        if( hasYear()) {
            sb.append(String.format("%04d", odt.getYear()));
            if (hasMonth()) {
                sb.append(String.format("%02d", odt.getMonthValue()));
                if (hasDay()) {
                    sb.append(String.format("%02d", odt.getDayOfMonth()));
                }
            }
        }
        return sb.toString();
    }

    /**
     * Return the OffseTime as a DICOM-time string with the same precision as this DicomDateTime.
     *
     * @param ot OffsetTime
     * @return DICOM-TM formatted string.
     */
    public String toDicomTimeString(OffsetTime ot) {
        StringBuilder sb = new StringBuilder();
        if( hasHour()) {
            sb.append( String.format("%02d", ot.getHour()));
            if( hasMinute()) {
                sb.append( String.format("%02d", ot.getMinute()));
                if( hasSeconds()) {
                    sb.append( String.format("%02d", ot.getSecond()));
                    if( hasFractionalSeconds()) {
                        sb.append( getFractionalSeconds());
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Format the OffsetDateTime as a DICOM DateTime String with the same precision as this DicomDateTime.
     *
     * The optional time-zone offset field is passed through.
     *
     * @return DICOM DT-formatted string.
     */
    public String toDicomDateTimeString( OffsetDateTime odt) {
        StringBuilder sb = new StringBuilder();
        if( hasYear()) {
            sb.append( String.format("%04d", odt.getYear()));
            if( hasMonth()) {
                sb.append( String.format("%02d", odt.getMonthValue()));
                if( hasDay()) {
                    sb.append( String.format("%02d", odt.getDayOfMonth()));
                    if( hasHour()) {
                        sb.append( String.format("%02d", odt.getHour()));
                        if( hasMinute()) {
                            sb.append( String.format("%02d", odt.getMinute()));
                            if( hasSeconds()) {
                                sb.append( String.format("%02d", odt.getSecond()));
                                if( hasFractionalSeconds()) {
                                    sb.append( getFractionalSeconds());
                                }
                            }
                        }
                    }
                }
            }
            if( hasTzOffset()) {
                sb.append( getTzOffset());
            }
        }
        return sb.toString();
    }

}
