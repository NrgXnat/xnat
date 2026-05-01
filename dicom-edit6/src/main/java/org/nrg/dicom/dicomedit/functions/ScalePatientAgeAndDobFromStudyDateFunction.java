/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.LowercaseFunction
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.dicom.dicomedit.DicomDateTimeShifter;
import org.nrg.dicom.dicomedit.datetime.DicomDateTime;
import org.nrg.dicom.dicomedit.datetime.DicomDateTimeFactory;
import org.nrg.dicom.dicomedit.datetime.DefaultDicomDateTimeShifter;
import org.nrg.dicom.dicomedit.datetime.MidPointDateTimeFactory;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.objects.DicomObjectVisitor;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.AbstractMizerValue;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.Value;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.List;

/**
 * 1. All occurrences of PatientAge = 089Y if PatientAge is present and > 89 years.
 * 2. PatientDOB is shifted to be 89 years from the studyDate if studyDate is present and age is > 89 years.
 */
@Slf4j
public class ScalePatientAgeAndDobFromStudyDateFunction extends AbstractScriptFunction {
    private final MidPointDateTimeFactory midPointDateTimeFactory;

    private static final int PATIENT_AGE = 0x00101010;
    private static final int PATIENT_DOB = 0x00100030;
    private static final int STUDY_DATE = 0x00080020;
    private static final int STUDY_INSTANCE_UID = 0x0020000D;
    private static final int INSTANCE_UID = 0x00080018;
    private static final long APPROX_DAYS_IN_89_YEARS = Math.round( 365.24 * 89);

    public ScalePatientAgeAndDobFromStudyDateFunction() {
        super("scalePatientAgeAndDobFromStudyDate", AbstractScriptFunction.DEFAULT_NAMESPACE, "Usage: ", "Description: ");
        this.midPointDateTimeFactory = new MidPointDateTimeFactory();
    }

    @Override
    public Value apply(List<Value> values, DicomObjectI dicomObject) throws ScriptEvaluationException {
        OffsetDateTime dob = getDOB( dicomObject);
        if(dob != null) {
            OffsetDateTime studyDate = getStudyDate( dicomObject);
            if (studyDate == null) {
                log.warn("DOB is unchanged. Unable to determine DOB shift. StudyDate is not present in instance {}, studyInstanceUID {}.",dicomObject.getString(INSTANCE_UID),dicomObject.getString(STUDY_INSTANCE_UID));
                return new ConstantValue(AbstractMizerValue.VOID);
            }
            Duration ageFromDOB = Duration.between( dob, studyDate);
            long days = ageFromDOB.toDays();
            long dobShift = days - APPROX_DAYS_IN_89_YEARS;
            if( dobShift > 0) {
                DicomObjectVisitor dobShiftVisitor = new DOBShiftVisitor(dobShift);
                dobShiftVisitor.visit(dicomObject);
            }
        }

        Period age = dicomObject.getAge(PATIENT_AGE).orElse(Period.ZERO);
        if( age.getYears() > 89) {
            DicomObjectVisitor patientAgeVisitor = new PatientAgeVisitor();
            patientAgeVisitor.visit(dicomObject);
        }
        return new ConstantValue(AbstractMizerValue.VOID);
    }

    /**
     * Find the Patient's DOB as OffsetDateTime
     *
     * @param dobj The DICOM under scrutiny
     * @return patient's DOB as OffsetDateTime, null if not present.
     */
    private OffsetDateTime getDOB(DicomObjectI dobj) {
        String dobString = dobj.getString(PATIENT_DOB);
        if (!StringUtils.isEmpty( dobString)) {
            DicomDateTime dts = DicomDateTimeFactory.fromDateTimeString( dobString).orElse(null);
            if( dts != null) {
                return midPointDateTimeFactory.dateTime(dts);
            }
        }
        return null;
    }

    /**
     * Find the StudyDate as OffsetDateTime
     *
     * @param dobj The DICOM under scrutiny
     * @return studyDate as OffsetDateTime, null if not present.
     */
    private OffsetDateTime getStudyDate(DicomObjectI dobj) {
        String dateString = dobj.getString(STUDY_DATE);
        if (!StringUtils.isEmpty( dateString)) {
            DicomDateTime dts = DicomDateTimeFactory.fromDateTimeString( dateString).orElse(null);
            if( dts != null) {
                return midPointDateTimeFactory.dateTime(dts);
            }
        }
        return null;
    }

    /**
     * Set all PatientAge tags to "089Y"
     */
    private static class PatientAgeVisitor extends DicomObjectVisitor {
        public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if( dicomElement.tag() == PATIENT_AGE) {
                dicomObject.putString(PATIENT_AGE, "089Y");
            }
        }
    }

    /**
     * Shift all occurrences of Patient's Birth Date by specified number of days.
     *
     */
    private static class DOBShiftVisitor extends DicomObjectVisitor {
        private final long shiftInDays;
        public DOBShiftVisitor( long shiftInDays) {
            this.shiftInDays = shiftInDays;
        }
        @Override
        public void visitTag(TagPath tagPath, DicomElementI dicomElement, DicomObjectI dicomObject) {
            if( dicomElement.tag() == PATIENT_DOB) {
                DicomDateTimeShifter shifter = new DefaultDicomDateTimeShifter();
                String shiftedDate = shifter.shiftDateTime( dicomObject.getString(PATIENT_DOB), shiftInDays, "days");
                dicomObject.putString(PATIENT_DOB, shiftedDate);
            }
        }
    }
}
