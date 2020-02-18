/*
 * org.nrg.xnat.helpers.dicom.DicomSummary
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 5/11/2016
 * @author james@radiologics.com
 */
package org.nrg.xnat.helpers.dicom;

public class DicomSummary {
	String tag1;
	String tag2;
	String vr;
	String desc;
	String value;

	public DicomSummary(String tag1, String tag2, String vr, String value, String desc) {
		super();
		this.tag1 = tag1;
		this.tag2 = tag2;
		this.vr = vr;
		this.value = value;
		this.desc = desc;
	}

	public DicomSummary() {
		// TODO Auto-generated constructor stub
	}

	public String getTag1() {
		return tag1;
	}

	public void setTag1(String tag1) {
		this.tag1 = tag1;
	}

	public String getTag2() {
		return tag2;
	}

	public void setTag2(String tag2) {
		this.tag2 = tag2;
	}

	public String getVr() {
		return vr;
	}

	public void setVr(String vr) {
		this.vr = vr;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}