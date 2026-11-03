/*
 * web: org.nrg.xnat.xft.ExperimentCustomFieldDateSuffixTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.xft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.XFTManager;

/**
 * Experiment custom fields ({@code xnat:experimentData/fields/field}) are typed {@code xdat:LONGVARCHAR} since
 * XNAT-6064. Forms built with the {@code #xdatTimeBox} macro post those fields as split
 * {@code .hours/.minutes/.seconds/.year/.month/.date} parameters, which {@link XFTItem} only accepted for
 * {@code date}, {@code dateTime} and {@code string} fields. The split parameters must assemble a date for
 * LONGVARCHAR fields exactly as they do for the (still {@code xs:string}) subject custom fields.
 */
public class ExperimentCustomFieldDateSuffixTest {
    private static final String EXPERIMENT_FIELD = "xnat:petSessionData/fields/field[name=injection_time]/field";
    private static final String SUBJECT_FIELD    = "xnat:subjectData/fields/field[name=injection_time]/field";

    @BeforeClass
    public static void loadSchema() throws Exception {
        ContextService.getInstance().setApplicationContext(null);
        final File schema = new File(ExperimentCustomFieldDateSuffixTest.class.getClassLoader().getResource("schemas/xnat/xnat.xsd").toURI());
        XFT.init(schema.getParentFile().getParentFile().getParent());
    }

    @AfterClass
    public static void unloadSchema() {
        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();
    }

    @Test
    public void splitDateAndTimeParametersPopulateExperimentCustomField() throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:petSessionData", null);

        setSplitDateTime(item, EXPERIMENT_FIELD, "2012", "1", "22", "13", "3", "15");

        assertDateTime(item.getDateProperty(EXPERIMENT_FIELD), 2012, Calendar.FEBRUARY, 22, 13, 3, 15);
    }

    @Test
    public void splitDateAndTimeParametersStillPopulateSubjectCustomField() throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:subjectData", null);

        setSplitDateTime(item, SUBJECT_FIELD, "2012", "1", "22", "13", "3", "15");

        assertDateTime(item.getDateProperty(SUBJECT_FIELD), 2012, Calendar.FEBRUARY, 22, 13, 3, 15);
    }

    @Test
    public void timeParametersUpdateExistingStoredDateString() throws Exception {
        final XFTItem item   = XFTItem.NewItem("xnat:petSessionData", null);
        final Date    stored = new GregorianCalendar(2012, Calendar.FEBRUARY, 22, 13, 3, 15).getTime();
        item.setProperty(EXPERIMENT_FIELD, stored.toString());

        item.setProperty(EXPERIMENT_FIELD + ".hours", "9");

        assertDateTime(item.getDateProperty(EXPERIMENT_FIELD), 2012, Calendar.FEBRUARY, 22, 9, 3, 15);
    }

    @Test
    public void timeParametersUpdateExistingStoredTimeString() throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:petSessionData", null);
        item.setProperty(EXPERIMENT_FIELD, "13:03:15");

        item.setProperty(EXPERIMENT_FIELD + ".hours", "9");

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getDateProperty(EXPERIMENT_FIELD));
        assertEquals(9, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(3, calendar.get(Calendar.MINUTE));
        assertEquals(15, calendar.get(Calendar.SECOND));
    }

    @Test
    public void timeParametersUpdateExistingStoredDateStringFromAnotherZone() throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:petSessionData", null);
        item.setProperty(EXPERIMENT_FIELD, "Wed Feb 22 13:03:15 PST 2012");

        item.setProperty(EXPERIMENT_FIELD + ".minutes", "9");

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(item.getDateProperty(EXPERIMENT_FIELD));
        assertEquals(2012, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.FEBRUARY, calendar.get(Calendar.MONTH));
        assertEquals(9, calendar.get(Calendar.MINUTE));
        assertEquals(15, calendar.get(Calendar.SECOND));
    }

    @Test
    public void storedValueThatIsNotADateOrTimeIsReplacedFromBaseDate() throws Exception {
        // A DICOM DA value, a date with trailing text and free text must not be read as a date or time
        // (lenient SimpleDateFormat would take "20120222" as 20:12:02); they are replaced from the base date.
        for (final String stored : new String[]{"20120222", "2012-02-22 notes", "120 MBq"}) {
            final XFTItem item = XFTItem.NewItem("xnat:petSessionData", null);
            item.setProperty(EXPERIMENT_FIELD, stored);

            item.setProperty(EXPERIMENT_FIELD + ".hours", "9");

            assertDateTime(item.getDateProperty(EXPERIMENT_FIELD), 2000, Calendar.JANUARY, 1, 9, 0, 0);
        }
    }

    @Test
    public void timeOnlyParametersUseYear2000BaseDate() throws Exception {
        final XFTItem item = XFTItem.NewItem("xnat:petSessionData", null);

        item.setProperty(EXPERIMENT_FIELD + ".hours", "13");
        item.setProperty(EXPERIMENT_FIELD + ".minutes", "3");
        item.setProperty(EXPERIMENT_FIELD + ".seconds", "15");

        assertDateTime(item.getDateProperty(EXPERIMENT_FIELD), 2000, Calendar.JANUARY, 1, 13, 3, 15);
    }

    private static void setSplitDateTime(final XFTItem item, final String field, final String year, final String month, final String day,
                                         final String hours, final String minutes, final String seconds) throws Exception {
        // Same order as the DCA PET form: time selects first, then addDateFields() appends year/date/month.
        item.setProperty(field + ".hours", hours);
        item.setProperty(field + ".minutes", minutes);
        item.setProperty(field + ".seconds", seconds);
        item.setProperty(field + ".year", year);
        item.setProperty(field + ".date", day);
        item.setProperty(field + ".month", month);
    }

    private static void assertDateTime(final Date date, final int year, final int month, final int day, final int hours, final int minutes, final int seconds) {
        assertNotNull(date);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(year, calendar.get(Calendar.YEAR));
        assertEquals(month, calendar.get(Calendar.MONTH));
        assertEquals(day, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(hours, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(minutes, calendar.get(Calendar.MINUTE));
        assertEquals(seconds, calendar.get(Calendar.SECOND));
    }
}
