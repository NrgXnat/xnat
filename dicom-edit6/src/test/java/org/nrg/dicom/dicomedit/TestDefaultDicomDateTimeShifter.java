package org.nrg.dicom.dicomedit;

import junit.framework.TestCase;
import org.junit.Test;
import org.nrg.dicom.dicomedit.datetime.DicomDateTimeFactory;
import org.nrg.dicom.dicomedit.datetime.DefaultDicomDateTimeShifter;
import org.nrg.dicom.dicomedit.datetime.MidPointDateTimeFactory;

public class TestDefaultDicomDateTimeShifter extends TestCase {

    @Test
    public void test() {

        String year               = "2022";
        String year_plus_3days    = "2022";
        String year_minus_3days   = "2022";
        String year_plus_5months  = "2022";
        String year_minus_5months = "2022";
        String year_plus_6months  = "2022";  // month shift is approximated low.
        String year_minus_6months = "2022";
        String year_plus_9months  = "2023";
        String year_minus_9months = "2021";

        assertEquals( year_plus_3days,    shiftBySeconds( year, days(3)));
        assertEquals( year_minus_3days,   shiftBySeconds( year, - days(3)));
        assertEquals( year_plus_5months,  shiftBySeconds( year, months(5)));
        assertEquals( year_minus_5months, shiftBySeconds( year, - months(5)));
        assertEquals( year_plus_6months,  shiftBySeconds( year, months(6)));
        assertEquals( year_minus_6months, shiftBySeconds( year, - months(6)));
        assertEquals( year_plus_9months,  shiftBySeconds( year, months(9)));
        assertEquals( year_minus_9months, shiftBySeconds( year, - months(9)));

        String yearMonth               = "202205";
        String yearMonth_plus_3days    = "202205";
        String yearMonth_minus_3days   = "202205";
        String yearMonth_plus_5months  = "202210";
        String yearMonth_minus_5months = "202112";
        String yearMonth_plus_6months  = "202211";
        String yearMonth_minus_6months = "202111";
        String yearMonth_plus_9months  = "202302";
        String yearMonth_minus_9months = "202108";

        assertEquals( yearMonth_plus_3days,    shiftBySeconds( yearMonth, days(3)));
        assertEquals( yearMonth_minus_3days,   shiftBySeconds( yearMonth, - days(3)));
        assertEquals( yearMonth_plus_5months,  shiftBySeconds( yearMonth, months(5)));
        assertEquals( yearMonth_minus_5months, shiftBySeconds( yearMonth, - months(5)));
        assertEquals( yearMonth_plus_6months,  shiftBySeconds( yearMonth, months(6)));
        assertEquals( yearMonth_minus_6months, shiftBySeconds( yearMonth, - months(6)));
        assertEquals( yearMonth_plus_9months,  shiftBySeconds( yearMonth, months(9)));
        assertEquals( yearMonth_minus_9months, shiftBySeconds( yearMonth, - months(9)));

        String yearMonthDay               = "20220519";
        String yearMonthDay_plus_5hours   = "20220519";
        String yearMonthDay_minus_5hours  = "20220519";
        String yearMonthDay_plus_12hours  = "20220520";
        String yearMonthDay_minus_12hours = "20220519";
        String yearMonthDay_plus_13hours  = "20220520";
        String yearMonthDay_minus_13hours = "20220518";

        assertEquals( yearMonthDay_plus_5hours,   shiftBySeconds( yearMonthDay, hours(5)));
        assertEquals( yearMonthDay_minus_5hours,  shiftBySeconds( yearMonthDay, - hours(5)));
        assertEquals( yearMonthDay_plus_12hours,  shiftBySeconds( yearMonthDay, hours(12)));
        assertEquals( yearMonthDay_minus_12hours, shiftBySeconds( yearMonthDay, - hours(12)));
        assertEquals( yearMonthDay_plus_13hours,  shiftBySeconds( yearMonthDay, hours(13)));
        assertEquals( yearMonthDay_minus_13hours, shiftBySeconds( yearMonthDay, - hours(13)));

        String yMDH             = "2022051913";
        String yMDH_plus_20m    = "2022051913";
        String yMDH_minus_20m   = "2022051913";
        String yMDH_plus_30m    = "2022051914";
        String yMDH_minus_30m   = "2022051913";
        String yMDH_plus_90m    = "2022051915";  // +2h
        String yMDH_minus_90m   = "2022051912";
        String yMDH_plus_3d     = "2022052213";
        String yMDH_minus_3d    = "2022051613";

        assertEquals( yMDH_plus_20m,  shiftBySeconds( yMDH, minutes(20)));
        assertEquals( yMDH_minus_20m, shiftBySeconds( yMDH, -minutes(20)));
        assertEquals( yMDH_plus_30m,  shiftBySeconds( yMDH, minutes(30)));
        assertEquals( yMDH_minus_30m, shiftBySeconds( yMDH, -minutes(30)));
        assertEquals( yMDH_plus_90m,  shiftBySeconds( yMDH, minutes(90)));
        assertEquals( yMDH_minus_90m, shiftBySeconds( yMDH, -minutes(90)));
        assertEquals( yMDH_plus_3d,   shiftBySeconds( yMDH, days(3)));
        assertEquals( yMDH_minus_3d,  shiftBySeconds( yMDH, -days(3)));

        String yMDHM              = "202205191324";
        String yMDHM_plus_20s     = "202205191324";
        String yMDHM_minus_20s    = "202205191324";
        String yMDHM_plus_30s     = "202205191325";
        String yMDHM_minus_30s    = "202205191324";
        String yMDHM_plus_90s     = "202205191326";
        String yMDHM_minus_90s    = "202205191323";
        String yMDHM_plus_20h     = "202205200924";
        String yMDHM_minus_20h    = "202205181724";

        assertEquals( yMDHM_plus_20s,  shiftBySeconds( yMDHM, seconds(20)));
        assertEquals( yMDHM_minus_20s, shiftBySeconds( yMDHM, -seconds(20)));
        assertEquals( yMDHM_plus_30s,  shiftBySeconds( yMDHM, seconds(30)));
        assertEquals( yMDHM_minus_30s, shiftBySeconds( yMDHM, -seconds(30)));
        assertEquals( yMDHM_plus_90s,  shiftBySeconds( yMDHM, seconds(90)));
        assertEquals( yMDHM_minus_90s, shiftBySeconds( yMDHM, -seconds(90)));
        assertEquals( yMDHM_plus_20h,  shiftBySeconds( yMDHM, hours(20)));
        assertEquals( yMDHM_minus_20h, shiftBySeconds( yMDHM, -hours(20)));

        String yMDHMS             = "20220519132435";
        String yMDHMS_plus_20s    = "20220519132455";
        String yMDHMS_minus_20s   = "20220519132415";
        String yMDHMS_plus_30s    = "20220519132505";
        String yMDHMS_minus_30s   = "20220519132405";
        String yMDHMS_plus_90s    = "20220519132605";
        String yMDHMS_minus_90s   = "20220519132305";
        String yMDHMS_plus_20m    = "20220519134435";
        String yMDHMS_minus_20m   = "20220519130435";

        assertEquals( yMDHMS_plus_20s,  shiftBySeconds( yMDHMS, seconds(20)));
        assertEquals( yMDHMS_minus_20s, shiftBySeconds( yMDHMS, -seconds(20)));
        assertEquals( yMDHMS_plus_30s,  shiftBySeconds( yMDHMS, seconds(30)));
        assertEquals( yMDHMS_minus_30s, shiftBySeconds( yMDHMS, -seconds(30)));
        assertEquals( yMDHMS_plus_90s,  shiftBySeconds( yMDHMS, seconds(90)));
        assertEquals( yMDHMS_minus_90s, shiftBySeconds( yMDHMS, -seconds(90)));
        assertEquals( yMDHMS_plus_20m,  shiftBySeconds( yMDHMS, minutes(20)));
        assertEquals( yMDHMS_minus_20m, shiftBySeconds( yMDHMS, -minutes(20)));

        String yMDHMSF1            = "20220519132435.1";
        String yMDHMSF1_plus_20s   = "20220519132455.1";
        String yMDHMSF1_minus_20s  = "20220519132415.1";

        assertEquals( yMDHMSF1_plus_20s,  shiftBySeconds( yMDHMSF1, seconds(20)));
        assertEquals( yMDHMSF1_minus_20s, shiftBySeconds( yMDHMSF1, -seconds(20)));

        String yMDHMSF3            = "20220519132435.123";
        String yMDHMSF3_plus_20s   = "20220519132455.123";
        String yMDHMSF3_minus_20s  = "20220519132415.123";

        assertEquals( yMDHMSF3_plus_20s,  shiftBySeconds( yMDHMSF3, seconds(20)));
        assertEquals( yMDHMSF3_minus_20s, shiftBySeconds( yMDHMSF3, -seconds(20)));

        String yMDHMSF6            = "20220519132435.123456";
        String yMDHMSF6_plus_20s   = "20220519132455.123456";
        String yMDHMSF6_minus_20s  = "20220519132415.123456";

        assertEquals( yMDHMSF6_plus_20s,  shiftBySeconds( yMDHMSF6, seconds(20)));
        assertEquals( yMDHMSF6_minus_20s, shiftBySeconds( yMDHMSF6, -seconds(20)));

        String yMTZ       = "202205+1000";
        String yMDTZ      = "20220519-0500";
        String yMDHTZ     = "2022051913-0500";
        String yMDHMTZ    = "202205191324-0500";
        String yMDHMSTZ   = "20220519132435-0500";
        String yMDHMSF1TZ = "20220519132435.1-0500";
        String yMDHMSF3TZ = "20220519132435.123-0500";
        String yMDHMSF6TZ = "20220519132435.123456-0500";

        assertEquals( yMTZ,  shiftBySeconds( yMTZ, seconds(0)));
        assertEquals( yMDTZ,  shiftBySeconds( yMDTZ, seconds(0)));
        assertEquals( yMDHTZ,  shiftBySeconds( yMDHTZ, seconds(0)));
        assertEquals( yMDHMTZ,  shiftBySeconds( yMDHMTZ, seconds(0)));
        assertEquals( yMDHMSTZ,  shiftBySeconds( yMDHMSTZ, seconds(0)));
        assertEquals( yMDHMSF1TZ,  shiftBySeconds( yMDHMSF1TZ, seconds(0)));
        assertEquals( yMDHMSF3TZ,  shiftBySeconds( yMDHMSF3TZ, seconds(0)));
        assertEquals( yMDHMSF6TZ,  shiftBySeconds( yMDHMSF6TZ, seconds(0)));

    }

    @Test
    public void testMidPoints() {
        String y       = "2022",               y_mid     = "20220701000000";
        String y31M    = "202205",             y31M_mid  = "20220516120000";
        String y30M    = "202206",             y30M_mid  = "20220616000000";
        String yMD     = "20220519",           yMD_mid   = "20220519120000";
        String yMDH    = "2022051913",         yMDH_mid  = "20220519133000";
        String yMDHM   = "202205191324",       yMDHM_mid = "20220519132430";
        String yMDHMS  = "20220519132435",     yHDMS_mid = "20220519132435";

        MidPointDateTimeFactory odtFactory = new MidPointDateTimeFactory();

        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y_mid).get()),     odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y31M_mid).get()),  odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y31M).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y30M_mid).get()),  odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( y30M).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMD_mid).get()),   odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMD).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMDH_mid).get()),  odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMDH).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMDHM_mid).get()), odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMDHM).get()));
        assertEquals( odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yHDMS_mid).get()), odtFactory.dateTime( DicomDateTimeFactory.fromDateTimeString( yMDHMS).get()));
    }

    @Test
    public void testInputFormat() {
        String badYear = "20";
        try {
            String s = shiftBySeconds( badYear, seconds(20));
            fail( String.format("Expected exception for bad dateTime string: '%s'", badYear));
        }
        catch (Exception e) {
            assertTrue( e.getMessage().contains("Failed to parse"));
        }

        String badFractionalSecs = "20220519123344.";
        try {
            String s = shiftBySeconds( badFractionalSecs, seconds(20));
            fail( String.format("Expected exception for bad dateTime string: '%s'", badFractionalSecs));
        }
        catch (Exception e) {
            assertTrue( e.getMessage().contains("Failed to parse"));
        }
    }

    public String shiftBySeconds( String dateTimeString, long seconds) {
        return new DefaultDicomDateTimeShifter().shiftDateTime( dateTimeString, seconds, "seconds");
    }

    // close enough
    private long years( int y) { return y * days(365);}
    private long months( int m) { return m * days(30);}
    private long days( int d) { return d * hours(24);}
    private long hours( int i) { return i * minutes(60);}
    private long minutes( int m) { return m * seconds(60);}
    private long seconds( int s) { return s;}

}