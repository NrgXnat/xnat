// Basic Application Level Confidentiality Profile
// Includes: Retain Device Ident. Option
// Adapted from DICOM Standard, PS 3.15-2011, Table E.1-1
// Copyright (c) 2012 Washington University
// Author: Kevin A. Archie <karchie@wustl.edu>
// NOTE: requires DicomEdit v3.0.0 or later
(0012,0062) := "YES"                 // Patient Identity Removed
(0008,0050) ~ ".+" : (0008,0050) := ""  // Accession Number
-(0018,4000)                         // Acquisition Comments
-(0040,0555)                         // Acquisition Context Sequence
-(0008,0022)                         // Acquisition Date
-(0008,002A)                         // Acquisition DateTime
-(0018,1400)                         // Acquisition Device Processing Description
-(0018,9424)                         // Acquisition Protocol Description
-(0008,0032)                         // Acquisition Time
-(0040,4035)                         // Actual Human Performers Sequence
-(0010,21B0)                         // Additional Patient's History
-(0038,0010)                         // Admission ID
-(0038,0020)                         // Admitting Date
-(0008,1084)                         // Admitting Diagnoses Code Sequence
-(0008,1080)                         // Admitting Diagnoses Description
-(0038,0021)                         // Admitting Time
-(0000,1000)                         // Affected SOP Instance UID
-(0010,2110)                         // Allergies
-(4000,0010)                         // Arbitrary
-(0040,A078)                         // Author Observer Sequence
-(0010,1081)                         // Branch of Service
-(0040,0280)                         // Comments on Performed Procedure Step
(0020,9161) ~ "\\d[\\d\\.]+" : (0020,9161) := hashUID[(0020,9161)]
-(0020,9161)                         // Concatenation UID
-(0040,3001)                         // Confidentiality Constraint on Patient Data Description
(0070,0084) := ""                    // Content Creator's Name
-(0070,0086)                         // Content Creator's Identification Code Sequence
(0008,0023) ~ ".+" : (0008,0023) := ""  // Content Date
-(0040,A730)                                      // Content Sequence
(0008,0033) ~ ".+" : (0008,0033) := ""    // Content Time
(0008,010D) ~ "\\d[\\d\\.]+" : (0008,010D) := hashUID[(0008,010D)]
-(0008,010D)                         // Context Group Extension Creator UID
(0018,0010) ~ ".+" : (0018,0010) := ""           // Contrast Bolus Agent
-(0018,A003)                         // Contribution Description
-(0010,2150)                         // Country of Residence
(0008,9123) ~ "\\d[\\d\\.]+" : (0008,9123) := hashUID[(0008,9123)]
-(0008,9123)                         // Creator Version UID
-(0038,0300)                         // Current Patient Location
-(50XX,XXXX)                         // Curve Data
-(0008,0025)                         // Curve Date
-(0008,0035)                         // Curve Time
-(0040,A07C)                         // Custodial Organization Sequence
-(FFFC,FFFC)                         // Data Set Trailing Padding
-(0008,2111)                         // Derivation Description
-(0400,0100)                         // Digital Signature UID
-(FFFA,FFFA)                         // Digital Signatures Sequence
(0020,9164) ~ "\\d[\\d\\.]+" : (0020,9164) := hashUID[(0020,9164)]
-(0020,9164)                         // Dimension Organization UID
-(0038,0040)                         // Discharge Diagnosis Description
-(4008,011A)                         // Distribution Address
-(4008,0119)                         // Distribution Name
(300A,0013) ~ "\\d[\\d\\.]+" : (300A,0013) := hashUID[(300A,0013)]
-(300A,0013)                         // Dose Reference UID
-(0010,2160)                         // Ethnic Group
(0008,0058) ~ "\\d[\\d\\.]+" : (0008,0058) := hashUID[(0008,0058)]
-(0008,0058)                         // Failed SOP Instance UID List
(0070,031A) ~ "\\d[\\d\\.]+" : (0070,031A) := hashUID[(0070,031A)]
-(0070,031A)                         // Fiducial UID
(0040,2017) ~ ".+" : (0040,2017) := ""  // Filler Order Number of Imaging Service Request
-(0020,9158)                         // Frame Comments
(0020,0052) ~ "\\d[\\d\\.]+" : (0020,0052) := hashUID[(0020,0052)]
-(0020,0052)                         // Frame of Reference UID
-(0070,0001)                         // Graphic Annotation Sequence (NOTE: should be dummy value)
-(0040,4037)                         // Human Performers Name
-(0040,4036)                         // Human Performers Organization
-(0088,0200)                         // Icon Image Sequence
-(0008,4000)                         // Identifying Comments
-(0020,4000)                         // Image Comments
-(0028,4000)                         // Image Presentation Comments
-(0040,2400)                         // Image Service Request Comments
-(4008,0300)                         // Impressions
-(0008,0012)			     // Instance Creation Date (NOTE: not in deident standard)
-(0008,0013)			     // Instance Creation Time (NOTE: not in deident standard)
(0008,0014) ~ "\\d[\\d\\.]+" : (0008,0014) := hashUID[(0008,0014)]
-(0008,0014)                         // Instance Creator UID
-(0008,0081)                         // Institution Address
-(0008,0082)                         // Institution Code Sequence
-(0008,0080)                         // Institution Name
-(0008,1040)                         // Institutional Department Name
-(0010,1050)                         // Insurance Plan Identification
-(0040,1011)                         // Intended Recipients of Results Identification Sequence
-(4008,0111)                         // Interpretation Approver Sequence
-(4008,010C)                         // Interpretation Author
-(4008,0115)                         // Interpretation Diagnosis Description
-(4008,0202)                         // Interpretation ID Issuer
-(4008,0102)                         // Interpretation Recorder
-(4008,010B)                         // Interpretation Text
-(4008,010A)                         // Interpretation Transcriber
(0008,3010) ~ "\\d[\\d\\.]+" : (0008,3010) := hashUID[(0008,3010)]
-(0008,3010)                         // Irradiation Event UID
-(0038,0011)                         // Issuer of Admission ID
-(0010,0021)                         // Issuer of Patient ID
-(0038,0061)                         // Issuer of Service Episode ID
(0028,1214) ~ "\\d[\\d\\.]+" : (0028,1213) := hashUID[(0028,1214)]
-(0028,1214)                         // Large Palette Color Lookup Table UID
-(0010,21D0)                         // Last Menstrual Date
-(0040,0404)                         // MAC
(0002,0003) ~ "\\d[\\d\\.]+" : (0002,0003) := hashUID[(0002,0003)]
-(0002,0003)                         // Media Storage SOP Instance UID
-(0010,2000)                         // Medical Alerts
-(0010,1090)                         // Medical Record Locator
-(0010,1080)                         // Military Rank
-(0400,0550)                         // Modified Attributes Sequence
-(0020,3406)                         // Modified Image Description
-(0020,3401)                         // Modifying Device ID
-(0020,3404)                         // Modifying Device Manufacturer
-(0008,1060)                         // Name of Physician(s) Reading Study
-(0040,1010)                         // Names of Intended Recipient of Results
-(0010,2180)                         // Occupation
-(0008,1072)                         // Operators' Identification Sequence
-(0008,1070)                         // Operators' Name
-(0400,0561)                         // Original Attributes Sequence
-(0040,2010)                         // Order Callback Phone Number
-(0040,2008)                         // Order Entered By
-(0040,2009)                         // Order Enterer Location
-(0010,1000)                         // Other Patient IDs
-(0010,1002)                         // Other Patient IDs Sequence
-(0010,1001)                         // Other Patient Names
-(60XX,4000)                         // Overlay Comments
-(60XX,3000)                         // Overlay Data
-(0008,0024)                         // Overlay Date
-(0008,0034)                         // Overlay Time
(0028,1199) ~ "\\d[\\d\\.]+" : (0028,1199) := hashUID[(0028,1199)]
-(0028,1199)                         // Palette Color Lookup Table UID
-(0040,A07A)                         // Participant Sequence
-(0010,1040)                         // Patient Address
-(0010,4000)                         // Patient Comments
(0010,0020) := session               // Patient ID
-(0010,2203)                         // Patient Sex Neutered
-(0038,0500)                         // Patient State
-(0040,1004)                         // Patient Transport Arrangements
-(0010,1010)                         // Patient's Age
(0010,0030) ~ ".+" : (0010,0030) := ""           // Patient's Birth Date
-(0010,1005)                         // Patient's Birth Name
-(0010,0032)                         // Patient's Birth Time
-(0038,0400)                         // Patient's Institution Residence
-(0010,0050)                         // Patient's Insurance Plan Code Sequence
-(0010,1060)                         // Patient's Mother's Birth Name
(0010,0010) := subject               // Patient's Name
-(0010,0101)                         // Patient's Primary Language Code Sequence
-(0010,0102)                         // Patient's Primary Language Modifier Code Sequence
-(0010,21F0)                         // Patient's Religious Preference
(0010,0040) ~ ".+" : (0010,0040) := ""   // Patient's Sex
-(0010,1020)                         // Patient's Size
-(0010,2154)                         // Patient's Telephone Number
-(0010,1030)                         // Patient's Weight
-(0040,0243)                         // Performed Location
-(0040,0254)                         // Performed Procedure Step Description
-(0040,0253)                         // Performed Procedure Step ID
-(0040,0244)                         // Performed Procedure Step Start Date
-(0040,0245)                         // Performed Procedure Step Start Time
-(0008,1052)                         // Performing Physicians' Identification Sequence
-(0008,1050)                         // Performing Physicians' Name
-(0040,1102)                         // Person Address
-(0040,1101)                         // Person Identification Code Sequence (NOTE: should be dummy value)
-(0040,A123)                         // Person Name (NOTE: should be dummy value)
-(0040,1103)                         // Person Telephone Numbers
-(4008,0114)                         // Physician Approving Interpretation
-(0008,1062)                         // Physican Reading Study Identification Sequence
-(0008,1048)                         // Physician(s) of Record
-(0008,1049)                         // Physician(s) of Record Identification Sequence
(0040,2016) ~ ".+" : (0040,2016) := ""  // Placer Order Number of Imaging Service Request
-(0040,0012)                         // Pre-Medication
-(0010,21C0)                         // Pregnancy Status
-(XXX#,XXXX)                         // Private Attributes
-(0018,1030)                         // Protocol Name
-(0040,2001)                         // Reason for Imaging Service Request
-(0032,1030)                         // Reason for Study
-(0400,0402)                         // Referenced Digital Signature Sequence
(3006,0024) ~ "\\d[\\d\\.]+" : (3006,0024) := hashUID[(3006,0024)]
-(3006,0024)                         // Referenced Frame of Reference UID
(0040,4023) ~ "\\d[\\d\\.]+" : (0040,4023) := hashUID[(0040,4023)]
-(0040,4023)                         // Referenced General Purpose Scheduled Procedure Step Transaction UID
-(0008,1140)                         // Referenced Image Sequence
-(0038,1234)                         // Referenced Patient Atlas Sequence
-(0008,1120)                         // Referenced Patient Sequence
-(0008,1111)                         // Referenced Performed Procedure Step Sequence
-(0400,0403)                         // Referenced SOP Instance MAC Sequence
(0008,1155) ~ "\\d[\\d\\.]+" : (0008,1155) := hashUID[(0008,1155)]                                     
-(0008,1155)                         // Referenced SOP Instance UID
(0004,1511) ~ "\\d[\\d\\.]+" : (0004,1511) := hashUID[(0004,1511)]                                     
-(0004,1511)                         // Referenced SOP Instance UID in File
-(0008,1110)                         // Referenced Study Sequence
-(0008,0092)                         // Referring Physician's Address
-(0008,0096)                         // Referring Physician's Identification Sequence
(0008,0090) ~ ".+" : (0008,0090) := ""  // Referring Physician's Name
-(0008,0094)                         // Referring Physician's Telephone Numbers
-(0010,2152)                         // Region of Residence
(3006,00C2) ~ "\\d[\\d\\.]+" : (3006,00C2) := hashUID[(3006,00C2)]
-(3006,00C2)                         // Related Frame of Reference UID
-(0040,0275)                         // Request Attributes Sequence
-(0032,1070)                         // Requested Contrast Agent
-(0040,1400)                         // Requested Procedure Comments
-(0032,1060)                         // Requested Procedure Description
-(0040,1001)                         // Requested Procedure ID
-(0040,1005)                         // Requested Procedure Location
(0000,1001) ~ "\\d[\\d\\.]+" : (0000,1001) := hashUID[(0000,1001)]
-(0000,1001)                         // Requested SOP Instance UID
-(0032,1032)                         // Requesting Physican
-(0032,1033)                         // Requesting Service
-(0010,2299)                         // Responsible Organization
-(0010,2297)                         // Responsible Person
-(4008,4000)                         // Results Comments
-(4008,0118)                         // Results Distribution List Sequence
-(4008,0042)                         // Results ID Issuer
-(300E,0008)                         // Reviewer Name
-(0040,4034)                         // Scheduled Human Performers Sequence
-(0038,001E)                         // Scheduled Patient Institution Reference
-(0040,000B)                         // Scheduled Performing Physician Identification Sequence
-(0040,0006)                         // Scheduled Performing Physician Name
-(0040,0004)                         // Scheduled Procedure Step End Date
-(0040,0005)                         // Scheduled Procedure Step End Time
-(0040,0007)                         // Scheduled Procedure Step Description
-(0040,0002)                         // Scheduled Procedure Step Start Date
-(0040,0003)                         // Scheduled Procedure Step Start Time
-(0008,0021)                         // Series Date
-(0008,103E)                         // Series Description
(0020,000E) ~ "\\d[\\d\\.]+" : (0020,000E) := hashUID[(0020,000E)]
-(0020,000E)                         // Series Instance UID
-(0008,0031)                         // Series Time
-(0038,0062)                         // Service Episode Description
-(0038,0060)                         // Service Episode ID
-(0010,21A0)                         // Smoking Status
(0008,0018) := hashUID[(0008,0018)]  // SOP Instance UID
-(0008,2112)                         // Source Image Sequence
-(0038,0050)                         // Special Needs
(0088,0140) ~ "\\d[\\d\\.]+" : (0088,0140) := hashUID[(0088,0140)]
-(0088,0140)                         // Storage Media Fileset UID
-(0032,4000)                         // Study Comments
(0008,0020) ~ ".+" : (0008,0020) := ""  // Study Date
(0008,1030) := project               // Study Description
(0020,0010) ~ ".+" : (0020,0010) := ""  // Study ID
-(0032,0012)                         // Study ID Issuer
(0020,000D) := hashUID[(0020,000D)]  // Study Instance UID
(0008,0030) ~ ".+" : (0008,0030) := ""  // Study Time
(0020,0200) ~ "\\d[\\d\\.]+" : (0020,0200) := hashUID[(0020,0200)]
-(0020,0200)                         // Synchronization Frame of Reference UID
(0040,DB0D) ~ "\\d[\\d\\.]+" : (0040,DB0D) := hashUID[(0040,DB0D)]
-(0040,DB0D)                         // Template Extension Creator UID
(0040,DB0C) ~ "\\d[\\d\\.]+" : (0040,DB0C) := hashUID[(0040,DB0C)]
-(0040,DB0C)                         // Template Extension Organization UID
-(4000,4000)                         // Text Comments
-(2030,0020)                         // Text String
-(0008,0201)                         // Timezone Offset From UTC
-(0088,0910)                         // Topic Author
-(0088,0912)                         // Topic Key Words
-(0088,0906)                         // Topic Title
(0008,1195) ~ "\\d[\\d\\.]+" : (0008,1195) := hashUID[(0008,1195)]
-(0008,1195)                         // Transaction UID
(0040,A124) ~ "\\d[\\d\\.]+" : (0040,A124) := hashUID[(0040,A124)]
-(0040,A124)                         // UID
-(0040,A088)                         // Verifying Observer Identification Code Sequence
-(0040,A075)                         // Verifying Observer Name (NOTE: should be dummy value)
-(0040,A073)                         // Verifying Observer Sequence (NOTE: should be dummy value)
-(0040,A027)                         // Verifying Organization
-(0038,4000)                         // Visit Comments
