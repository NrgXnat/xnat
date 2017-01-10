/*
 * core: org.nrg.xft.utils.DateUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Tim
 */
public class DateUtils {
    List<SimpleDateFormat> dates = new ArrayList<>();
    List<SimpleDateFormat> dateTimes = new ArrayList<>();
    List<SimpleDateFormat> times = new ArrayList<>();

    private static List<DateUtils> FREE = new ArrayList<>();

    public synchronized static DateUtils GetInstance() {
        DateUtils d = null;
        if (FREE.size() > 0) {
            d = FREE.remove(0);
		}

        if (d != null) {
			d.open();
        } else {
            d = new DateUtils();
			d.open();
		}

		return d;
	}

    private void open() {
	}

    private void close() {
		FREE.add(this);
	}

    static Map<String, SimpleDateFormat> formatters = new Hashtable<>();

    public synchronized static String format(Date d, String format) {
        if (!formatters.containsKey(format)) {
            formatters.put(format, new SimpleDateFormat(format));
		}

		return formatters.get(format).format(d);
	}

    private DateUtils() {
		dates.add(new SimpleDateFormat("yyyy-MM-dd", Locale.US));
		dates.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US));
		dates.add(new SimpleDateFormat("MM/dd/yyyy", Locale.US));
		dates.add(new SimpleDateFormat("MM.dd.yyyy", Locale.US));
		dates.add(new SimpleDateFormat("yyyyMMdd", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss.S", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss z", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US));
		dateTimes.add(new SimpleDateFormat("MM/dd/yyyy", Locale.US));
		dateTimes.add(new SimpleDateFormat("yyyy-MM-dd", Locale.US));

		times.add(new SimpleDateFormat("HH:mm:ss", Locale.US));
		times.add(new SimpleDateFormat("HH:mm:ss z", Locale.US));
		times.add(new SimpleDateFormat("HHmmss", Locale.US));
	}


    private Date parse(List<SimpleDateFormat> al, String s) throws ParseException {
        if (s.contains("'")) {
            s = StringUtils.replace(s, "'", "");
        }
        if (s == null) {
            return null;
        } else {
            ParseException firstE;
            try {
                return DateFormat.getInstance().parse(s);
            } catch (ParseException e) {
                firstE = e;
            }

            for (SimpleDateFormat sdf : al) {
            	try {
                    return sdf.parse(s);
                } catch (ParseException ignored) {
                }
            }

            throw firstE;
        }
	}

    private synchronized Date parseD(String s) throws ParseException {
		return this.parse(dates, s);
	}

    private synchronized Date parseDT(String s) throws ParseException {
		return this.parse(dateTimes, s);
	}

    private synchronized Date parseT(String s) throws ParseException {
		return this.parse(times, s);
	}

    public static Date parseDate(String s) throws ParseException {
        DateUtils du = GetInstance();
        Date d = du.parseD(s);
    	du.close();
        return d;
    }


    public static Date parseDateTime(String s) throws ParseException {
        DateUtils du = GetInstance();
        Date d = du.parseDT(s);
    	du.close();
        return d;
    }

    public static Date parseTime(String s) throws ParseException {
        DateUtils du = GetInstance();
        Date d = du.parseT(s);
    	du.close();
        return d;
    }

    /**
     * Returns true if d1 is on or after d2.  If both are null, then they are equal.  Otherwise, null is considered to be after a valid date.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return Whether the first date is on or after the second date.
     */
    public static boolean isOnOrAfter(Date d1, Date d2) {
        int compare = (new TimestampSafeComparator()).compare(d1, d2);
        return (compare == 0 || compare == 1);
    }


    /**
     * Returns true if d1 is on or before d2.  If both are null, then they are equal.  Otherwise, null is considered to be after a valid date.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return Whether the first date is on or before the second date.
     */
    public static boolean isOnOrBefore(Date d1, Date d2) {
        int compare = (new TimestampSafeComparator()).compare(d1, d2);
        return (compare == 0 || compare == -1);
    }


    /**
     * Returns true if is = d2.  If both are null, then they are equal.  Otherwise, null is considered to be after a valid date.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return Whether the two dates are equal.
     */
    public static boolean isEqualTo(Date d1, Date d2) {
        int compare = (new TimestampSafeComparator()).compare(d1, d2);
        return (compare == 0);
    }


    /**
     * Returns 0 if d1 is = d2.  If both are null, then they are equal.  Otherwise, null is considered to be after a valid date.
     *
     * @param d1 The first date.
     * @param d2 The second date.
     * @return 0 if the first date is the samne as the second date.
     */
    public static int compare(Date d1, Date d2) {
        return (new TimestampSafeComparator()).compare(d1, d2);
    }

    public static class TimestampSafeComparator implements Comparator<Date> {

		public int compare(Date one, Date two) {
            if (one == null ^ two == null) {
                return (one == null) ? 1 : -1;
			}
            if (one == null) {
                return 0;
		}
            return ((Long) one.getTime()).compareTo(two.getTime());
    }

    }

    public static java.util.Date toDate(java.sql.Timestamp timestamp) {
        return new java.util.Date(timestamp.getTime());
    }
}

