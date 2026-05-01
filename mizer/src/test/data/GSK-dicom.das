// Common GHF112670 anonymization script v. 001, 2009-08-21
// by Kevin Archie, karchie@npg.wustl.edu, Jenny Gurney gurneyj@wustl.edu
(0002,0003) := newUID[]					// Media Storage SOP Instance UID
- (0008,0014)							// Instance Creator UID
- (0008,0050)							// Accession Number
- (0010,0021)							// Issuer of Patient ID
(0008,0018) := newUID[]					// SOP Instance UID
- (0008,1111)							// Referenced Performed Procedure Step SQ
- (0008,1120)							// Referenced Patient SQ
- (0008,1140)							// Referenced Image SQ
- (0008,0090)							// Referring Physician's Name
- (0008,0096)							// Referring Physician Identification SQ
- (0008,1032)							// Procedure Code SQ
- (0008,1048)							// Physician(s) of Record
- (0008,1049)							// Physician(s) of Record Identification SQ
- (0008,1050)							// Performing Physicians' Name
- (0008,1052)							// Performing Physician Identification SQ
- (0008,1060)							// Name of Physician(s) Reading Study
- (0008,1062)							// Physician(s) Reading Study Identification SQ
- (0008,1110)							// Referenced Study SQ
- (0008,1111)							// Referenced Performed Procedure Step SQ
- (0008,1250)							// Related Series SQ
- (0008,9092)							// Referenced Image Evidence SQ
- (0010,0021)							// Issuer of Patient ID
//- (0010,0030)							// Patient's Birth Date
- (0010,0032)							// Patient's Birth Time
- (0010,0050)							// Patient's Insurance Plan Code SQ
- (0010,0101)							// Patient's Primary Language Code SQ
- (0010,1000)							// Other Patient IDs
- (0010,1001)							// Other Patient Names
- (0010,1002)							// Other Patient IDs SQ
- (0010,1005)							// Patient's Birth Name
- (0010,1010)							// Patient's Age
- (0010,1040)							// Patient's Address
- (0010,1060)							// Patient's Mother's Birth Name
- (0010,1080)							// Military Rank
- (0010,1081)							// Branch of Service
- (0010,1090)							// Medical Record Locator
- (0010,2000)							// Medical Alerts
- (0010,2110)							// Allergies
- (0010,2150)							// Country of Residence
- (0010,2152)							// Region of Residence
- (0010,2154)							// Patient's Telephone Numbers
- (0010,2160)							// Ethnic Group
- (0010,2180)							// Occupation
- (0010,21A0)							// Smoking Status
- (0010,21B0)							// Additional Patient History
- (0010,21C0)							// Pregnancy Status
- (0010,21D0)							// Last Menstrual Date
- (0010,21F0)							// Patient's Religious Preference
- (0010,2203)							// Patient's Sex Neutered
- (0010,2297)							// Responsible Person
- (0010,2298)							// Responsible Person Role
- (0010,2299)							// Responsible Organization
(0012,0063) := "CNDA GHF112670 deidentification v001"
(0020,000E) := newUID[]					// Series Instance UID
(0020,0052) := newUID[]					// Frame of Reference UID
(0020,0200) := newUID[]					// Synchronization FofR UID
(0020,9161) := newUID[]					// Concatenation UID
- (0020,9221)							// Dimension Organization SQ
- (0020,9222)							// Dimension Index SQ
- (0038,0010)							// Admission ID
- (0038,0011)							// Issuer of Admission ID
- (0038,0060)							// Service Episode ID
- (0038,0061)							// Issuer of Service Episode ID
- (0038,0062)							// Service Episode Description
- (0038,0500)							// Patient State
- (0038,0100)							// Pertinent Documents SQ
- (0040,0260)							// Performed Protocol Code SQ
- (0088,0130)							// Storage Media File-Set ID
- (0088,0140)							// Storage Media File-Set UID
- (5200,9229)							// Shared Functional Groups SQ

describe visit "visit (PRE or POST)"
export visit "xnat:experimentData/visit_id"        // Tells the uploader to use visit to set the named XNAT field (PRE/POST)
modalityLabel := "null"
//visit := "null"
session := format["{0}_{1}_{2}_{3}", subject,modalityLabel,(0008,0020),visit]

// Set XNAT identifying fields
(0008,1030) := project
(0010,0010) := subject
(0010,0020) := session
