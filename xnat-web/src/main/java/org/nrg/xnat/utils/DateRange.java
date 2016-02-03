/*
 * org.nrg.xnat.utils.DateRange
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.utils;

import java.util.Date;

public class DateRange {

    private final static Date MIN_DATE = new Date(Long.MIN_VALUE);
    private final static Date MAX_DATE = new Date(Long.MAX_VALUE);

    private final Date start;

    private final Date end;

    /**
     * represents a date range that is unbounded/infinite on both ends.
     */
    public DateRange() {
	this(null, null);
    }

    public DateRange(Date start, Date end) {
	if (null == start) {
	    start = MIN_DATE;
	}
	if (null == end) {
	    end = MAX_DATE;
	}
	this.start = new Date(start.getTime());
	this.end = new Date(end.getTime());
    }

    public boolean isEmpty() {
	return start.after(end);
    }

    public boolean isBoundedAtStart() {
	return !start.equals(MIN_DATE);
    }

    public boolean isBoundedAtEnd() {
	return !end.equals(MAX_DATE);
    }

    public boolean isBounded() {
	return isBoundedAtStart() || isBoundedAtEnd();
    }

    public boolean includes(Date arg) {
	return !arg.before(start) && !arg.after(end);
    }

    public boolean includes(DateRange arg) {
	return this.includes(arg.start) && this.includes(arg.end);
    }

    public boolean overlaps(DateRange arg) {
	return arg.includes(start) || arg.includes(end) || this.includes(arg);
    }

    public Date getStart() {
	return start;
    }

    public Date getEnd() {
	return end;
    }

    @Override
    public String toString() {
	if (isEmpty())
	    return "Empty Date Range";
	return start.toString() + " - " + end.toString();
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((end == null) ? 0 : end.hashCode());
	result = prime * result + ((start == null) ? 0 : start.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof DateRange))
	    return false;
	DateRange other = (DateRange) obj;
	if (end == null) {
	    if (other.end != null)
		return false;
	} else if (!end.equals(other.end))
	    return false;
	if (start == null) {
	    if (other.start != null)
		return false;
	} else if (!start.equals(other.start))
	    return false;
	return true;
    }
}
