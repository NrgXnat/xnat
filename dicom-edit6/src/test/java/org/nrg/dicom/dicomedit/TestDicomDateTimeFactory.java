package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.dicomedit.datetime.DicomDateTime;
import org.nrg.dicom.dicomedit.datetime.DicomDateTimeFactory;

import java.util.Optional;

import static org.junit.Assert.*;

public class TestDicomDateTimeFactory {
    @Test
    public void testDateTime() {
        // YYYYMMDDHHMMSS.FFFFFF&ZZXX
        Optional<DicomDateTime> dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.123456+1600");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( ".123456", dtString.get().getFractionalSeconds());
        assertEquals( "+1600", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.123456-1600");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( ".123456", dtString.get().getFractionalSeconds());
        assertEquals( "-1600", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.123456");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( ".123456", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.12345-0900");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( ".12345", dtString.get().getFractionalSeconds());
        assertEquals( "-0900", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.12");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( ".12", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.");
        assertFalse( dtString.isPresent());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345.-0900");
        assertFalse( dtString.isPresent());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111135345-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "45", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("202401111353-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("202401111353");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "53", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("2024011113-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("2024011113");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "13", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("20240111");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "11", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("202401-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("202401");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "01", dtString.get().getMonth());
        assertEquals( "", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("2024-0500");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "", dtString.get().getMonth());
        assertEquals( "", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "-0500", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("2024");
        assertTrue( dtString.isPresent());
        assertEquals( "2024", dtString.get().getYear());
        assertEquals( "", dtString.get().getMonth());
        assertEquals( "", dtString.get().getDay());
        assertEquals( "", dtString.get().getHour());
        assertEquals( "", dtString.get().getMinute());
        assertEquals( "", dtString.get().getSeconds());
        assertEquals( "", dtString.get().getFractionalSeconds());
        assertEquals( "", dtString.get().getTzOffset());

        dtString = DicomDateTimeFactory.fromDateTimeString("not a date");
        assertFalse( dtString.isPresent());
        dtString = DicomDateTimeFactory.fromDateTimeString("196");
        assertFalse( dtString.isPresent());
        dtString = DicomDateTimeFactory.fromDateTimeString("19601");
        assertFalse( dtString.isPresent());
        dtString = DicomDateTimeFactory.fromDateTimeString("1960051");
        assertFalse( dtString.isPresent());
    }
    @Test
    public void testDate() {
        Optional<DicomDateTime> dateString = DicomDateTimeFactory.fromDateString("20240111");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "01", dateString.get().getMonth());
        assertEquals( "11", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateString("202401");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "01", dateString.get().getMonth());
        assertEquals( "", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateString("2024");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "", dateString.get().getMonth());
        assertEquals( "", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateString("not a date");
        assertFalse( dateString.isPresent());
        dateString = DicomDateTimeFactory.fromDateString("202");
        assertFalse( dateString.isPresent());
    }
    @Test
    public void testDateAsDateTime() {
        Optional<DicomDateTime> dateString = DicomDateTimeFactory.fromDateTimeString("20240111");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "01", dateString.get().getMonth());
        assertEquals( "11", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateTimeString("202401");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "01", dateString.get().getMonth());
        assertEquals( "", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateTimeString("2024");
        assertTrue( dateString.isPresent());
        assertEquals( "2024", dateString.get().getYear());
        assertEquals( "", dateString.get().getMonth());
        assertEquals( "", dateString.get().getDay());

        dateString = DicomDateTimeFactory.fromDateTimeString("not a date");
        assertFalse( dateString.isPresent());
        dateString = DicomDateTimeFactory.fromDateTimeString("202");
        assertFalse( dateString.isPresent());
    }
    @Test
    public void testTime() {
        Optional<DicomDateTime> timeString = DicomDateTimeFactory.fromTimeString("202401.123456");
        assertTrue( timeString.isPresent());
        assertEquals( "20", timeString.get().getHour());
        assertEquals( "24", timeString.get().getMinute());
        assertEquals( "01", timeString.get().getSeconds());
        assertEquals( ".123456", timeString.get().getFractionalSeconds());

        timeString = DicomDateTimeFactory.fromTimeString("202401");
        assertTrue( timeString.isPresent());
        assertEquals( "20", timeString.get().getHour());
        assertEquals( "24", timeString.get().getMinute());
        assertEquals( "01", timeString.get().getSeconds());
        assertEquals( "", timeString.get().getFractionalSeconds());

        timeString = DicomDateTimeFactory.fromTimeString("2024");
        assertTrue( timeString.isPresent());
        assertEquals( "20", timeString.get().getHour());
        assertEquals( "24", timeString.get().getMinute());
        assertEquals( "", timeString.get().getSeconds());
        assertEquals( "", timeString.get().getFractionalSeconds());

        timeString = DicomDateTimeFactory.fromTimeString("20");
        assertTrue( timeString.isPresent());
        assertEquals( "20", timeString.get().getHour());
        assertEquals( "", timeString.get().getMinute());
        assertEquals( "", timeString.get().getSeconds());
        assertEquals( "", timeString.get().getFractionalSeconds());

        timeString = DicomDateTimeFactory.fromTimeString("not a time");
        assertFalse( timeString.isPresent());
        timeString = DicomDateTimeFactory.fromTimeString("202");
        assertFalse( timeString.isPresent());
    }
}
