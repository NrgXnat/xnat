version "6.6"

// "injected"
patientID := "new patientID"
accessionNumber := "new accession"
dateOffset := -1

tagsToRemove := {
    (0040,0555),   // AcquisitionContextSequence
    (0010,21B0),   // AdditionalPatientHistory
    (0038,0010),   // AdmissionID
    (0008,1080)    // AdmittingDiagnosesDescription
}

 tagsToHashUID := {
    (0008,0014),    // InstanceCreatorUID
    (0008,0018),    // SOPInstanceUID
    (0008,1155),    // ReferencedSOPInstanceUID
    (0020,000E),    // SeriesInstanceUID
    (0020,000D)     // StudyInstanceUID
}

tagsToOffsetDateTime := {
    (0008,0020),    // StudyDate
    (0008,0012),    // InstanceCreationDate
    (0008,0021),    // SeriesDate
    (0008,0022),    // AcquisitionDate
    (0008,0023),    // ContentDate
    (0008,002A),    // AcquisitionDateTime
    (0010,0030)     // PatientBirthDate
}

// tagsWithValuesToHunt are tags whose values contain PHI.
tagsWithValuesToHunt := {
    (0008,0050),  // AccessionNumber
    (0008,0080),  // InstitutionName
    (0008,0090),  // ReferringPhysicianName
    (0010,0010),  // PatientName
    (0010,0020),  // PatientID
    (0010,0021)   // IssuerOfPatientID
}

knownPHI := collectValues[ tagsToRemove, tagsWithValuesToHunt]
// blankKnownPHI[ tagsToRemove, tagsWithValuesToHunt]

// accessionNumber is injected
(0008,0050) := accessionNumber

// patientName is patientID.
(0010,0010) := patientID

// patientID is injected
(0010,0020) := patientID

// IssuerOfPatientID
(0010,0021) ?= "PIDIssuer"

// timezoneOffsetFromUTC ?= "-0500"
(0008,0201)  ?= "-0500"

// ReferringPhysicianName
(0008,0090) ?= ""

// PatientIdentityRemoved
(0012,0062) := "YES"

// StudyID
(0020,0010) ?= accessionNumber

removeTags[ tagsToRemove]
hashUIDList[ tagsToHashUID]
shiftDateTimeListByIncrement[ tagsToOffsetDateTime, dateOffset, "days"]

(0008,1030) := normalizeString[(0008,1030)]
(0008,103E) := normalizeString[(0008,103E)]

// velocity encoding private tags to be preserved for CVI42 Flow analysis
// GE private tags for CVI42 "Flow 2D" application: models "GENESIS_SIGNA", "Signa HDxt"
privateTagsToRetain := {
    (0019, {GE_19}CC),   //  velocity_encoding
    (0019, {GE_19}E2),   //  velocity_encode_scale
    (0043, {GE_43}30),   //  vas_collapse_flag
    (0043, {GE_43}32)    //  vas_flag
}

retainPrivateTags[ privateTagsToRetain]

scalePatientAgeAndDobFromStudyDate[]

blankValues[ knownPHI]
