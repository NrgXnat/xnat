/*
 * DicomDB: org.nrg.dcm.DirectoryRecordTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import org.dcm4che2.data.Tag;

import static org.junit.Assert.*;

import org.junit.Test;

import org.nrg.dcm.DirectoryRecord;

public class DirectoryRecordTest {
  private final static SimpleDateFormat dicomDateFormat = new SimpleDateFormat("yyyyMMdd");
  private final static SimpleDateFormat dicomTimeFormat = new SimpleDateFormat("HHmmss.SSS");

  @Test
  public final void testDirectoryRecordType() {
    assertEquals("Patient", DirectoryRecord.Type.PATIENT.toString());
    assertEquals("Study", DirectoryRecord.Type.STUDY.toString());
    assertEquals("Series", DirectoryRecord.Type.SERIES.toString());
    assertEquals("Image", DirectoryRecord.Type.INSTANCE.toString());

    assertTrue(DirectoryRecord.Type.PATIENT.matches("PATIENT"));
    assertTrue(DirectoryRecord.Type.STUDY.matches("STUDY"));
    assertTrue(DirectoryRecord.Type.SERIES.matches("SERIES"));
    assertTrue(DirectoryRecord.Type.INSTANCE.matches("IMAGE"));
  }

  @Test
  public final void testDirectoryRecord() {
    assertNotNull(new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, new HashMap<Integer,String>()));
    assertNotNull(new DirectoryRecord(DirectoryRecord.Template.STUDY, null, new HashMap<Integer,String>()));
    assertNotNull(new DirectoryRecord(DirectoryRecord.Template.SERIES, null, new HashMap<Integer,String>()));
    assertNotNull(new DirectoryRecord(DirectoryRecord.Template.INSTANCE, null, new HashMap<Integer,String>()));
  }

  @Test
  public final void testGetUpper() {
    DirectoryRecord patient = new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, new HashMap<Integer,String>());
    DirectoryRecord study = new DirectoryRecord(DirectoryRecord.Template.STUDY, patient, new HashMap<Integer,String>());
    assertNull(patient.getUpper());
    assertEquals(patient, study.getUpper());
  }

  @Test
  public final void testGetLower() {
    final DirectoryRecord patient = new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, new HashMap<Integer,String>());
    final DirectoryRecord study = new DirectoryRecord(DirectoryRecord.Template.STUDY, patient, new HashMap<Integer,String>());
    final DirectoryRecord series = new DirectoryRecord(DirectoryRecord.Template.SERIES, study, new HashMap<Integer,String>());
    assertEquals(1, patient.getLower().size());
    assertTrue(patient.getLower().contains(study));
    assertEquals(1, study.getLower().size());
    assertTrue(study.getLower().contains(series));
    assertEquals(0, series.getLower().size());
  }

  @Test
  public final void testGetSelectionKeys() {
    assertEquals(1, new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, new HashMap<Integer,String>()).getSelectionKeys().length);
    assertEquals(3, new DirectoryRecord(DirectoryRecord.Template.STUDY, null, new HashMap<Integer,String>()).getSelectionKeys().length);
    assertEquals(3, new DirectoryRecord(DirectoryRecord.Template.SERIES, null, new HashMap<Integer,String>()).getSelectionKeys().length);
    assertEquals(2, new DirectoryRecord(DirectoryRecord.Template.INSTANCE, null, new HashMap<Integer,String>()).getSelectionKeys().length);
  }

  @Test
  public final void testGetType() {
    assertTrue(new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, new HashMap<Integer,String>()).getType().matches("PATIENT"));
    assertTrue(new DirectoryRecord(DirectoryRecord.Template.STUDY, null, new HashMap<Integer,String>()).getType().matches("STUDY"));
    assertTrue(new DirectoryRecord(DirectoryRecord.Template.SERIES, null, new HashMap<Integer,String>()).getType().matches("SERIES"));
    assertTrue(new DirectoryRecord(DirectoryRecord.Template.INSTANCE, null, new HashMap<Integer,String>()).getType().matches("IMAGE"));
  }

  @Test
  public final void testGetValue() {
    final Map<Integer,String> values = new HashMap<Integer,String>();
    values.put(0, "zero");
    values.put(1, "one");
    DirectoryRecord newr = new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, values);
    assertEquals("zero", newr.getValue(0));
    assertEquals("one", newr.getValue(1));
    assertEquals(null, newr.getValue(2));
  }

  @Test
  public final void testTypeGetInstance() {
    assertEquals(DirectoryRecord.Type.PATIENT, DirectoryRecord.Type.getInstance("PATIENT"));
    assertEquals(DirectoryRecord.Type.STUDY, DirectoryRecord.Type.getInstance("STUDY"));
    assertEquals(DirectoryRecord.Type.SERIES, DirectoryRecord.Type.getInstance("SERIES"));
    assertEquals(DirectoryRecord.Type.INSTANCE, DirectoryRecord.Type.getInstance("IMAGE"));
    assertNull(DirectoryRecord.Type.getInstance("no such type"));
  }

  @Test
  public final void testSetUpper() {
    final Map<Integer,String> patientValues = new HashMap<Integer,String>();
    patientValues.put(Tag.PatientID, "patientID");
    final DirectoryRecord p = new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, patientValues);
    final DirectoryRecord st = new DirectoryRecord(DirectoryRecord.Template.STUDY, null, new HashMap<Integer,String>());
    assertEquals(0, p.getLower().size());
    st.setUpper(p);
    assertEquals(1, p.getLower().size());
    st.setUpper(p);
    assertEquals(1, p.getLower().size());
  }

  @Test
  public final void testPurge() {
    final String[] studyIDs = {"A", "B", "C", "D"};
    final int NUM_SERIES = 4;
    final int NUM_IMAGES = 5;
    final DirectoryRecord[] study = new DirectoryRecord[studyIDs.length];
    final DirectoryRecord[][] series = new DirectoryRecord[studyIDs.length][NUM_SERIES];
    final DirectoryRecord[][][] image = new DirectoryRecord[studyIDs.length][NUM_SERIES][NUM_IMAGES];

    // Build a directory record tree
    final String patientID = "Foo_Barr";
    final Map<Integer,String> patientValues = new HashMap<Integer,String>();
    patientValues.put(Tag.PatientID, patientID);
    final DirectoryRecord p = new DirectoryRecord(DirectoryRecord.Template.PATIENT, null, patientValues);

    for (int studyi = 0; studyi < studyIDs.length; studyi++) {
      final String studyID = studyIDs[studyi];
      final Date studyDate = Calendar.getInstance().getTime();
      final Map<Integer,String> studyValues = new HashMap<Integer,String>();
      studyValues.put(Tag.StudyDate, dicomDateFormat.format(studyDate));
      studyValues.put(Tag.StudyTime, dicomTimeFormat.format(studyDate));
      studyValues.put(Tag.StudyID, studyID);

      study[studyi] = new DirectoryRecord(DirectoryRecord.Template.STUDY, p, studyValues);
      assertTrue(p.getLower().contains(study[studyi]));

      for (int seriesi = 0; seriesi < NUM_SERIES; seriesi++) {
	final Map<Integer,String> seriesValues = new HashMap<Integer,String>();
	seriesValues.put(Tag.Modality, "MR");
	seriesValues.put(Tag.SeriesNumber, String.valueOf(seriesi+1));
	seriesValues.put(Tag.SeriesInstanceUID, "1.1." + studyi + "." + (seriesi+1));

	series[studyi][seriesi] = new DirectoryRecord(DirectoryRecord.Template.SERIES, study[studyi], seriesValues);
	assertTrue(study[studyi].getLower().contains(series[studyi][seriesi]));

	for (int i = 0; i < NUM_IMAGES; i++) {
	  final Map<Integer,String> instanceValues = new HashMap<Integer,String>();
	  instanceValues.put(Tag.InstanceNumber, String.valueOf(i));
	  instanceValues.put(Tag.ReferencedFileID, patientID + studyID + seriesi + "_" + i + ".dcm");

	  image[studyi][seriesi][i] = new DirectoryRecord(DirectoryRecord.Template.INSTANCE, series[studyi][seriesi], instanceValues);
	  assertTrue(series[studyi][seriesi].getLower().contains(image[studyi][seriesi][i]));
	}
      }
    }

    // Now purge some records and verify the results.
    int nRemoved;

    nRemoved = p.purge(image[1][2][1]).size();
    assertEquals(1, nRemoved);

    nRemoved = p.purge(image[1][2][1]).size();
    assertEquals(0, nRemoved);	// already purged

    nRemoved = p.purge(series[1][2]).size();
    assertEquals(4, nRemoved);		// 3 images left

    nRemoved = p.purge(series[1][2]).size();
    assertEquals(0, nRemoved);	// already purged

    // Removing all images removes the series too.
    assertTrue(study[0].getLower().contains(series[0][1]));
    nRemoved = p.purge(image[0][1][0], image[0][1][1], image[0][1][2], image[0][1][3], image[0][1][4]).size();
    assertFalse(study[0].getLower().contains(series[0][1]));
  }
}
