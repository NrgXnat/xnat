// Common DIAN anonymization script v. 001, 2009-08-21
// by Kevin Archie, karchie@npg.wustl.edu
- (0008,0050)							// Accession Number
(0008,0080) = "Columbia University" : (0008,0080) := "DIAN_010" // Institution Name
(0008,0080) = "Hatch Ctr 1.5T @ Columbia" : (0008,0080) := "DIAN_010" 
(0008,0080) = "CCIR 3T" : (0008,0080) := "DIAN_011" 
(0008,0080) = "Center for Clinical Imaging Research" : (0008,0080) := "DIAN_011" 
(0008,0080) = "Washington University" : (0008,0080) := "DIAN_011" 
(0008,0080) = "MRRC" : (0008,0080) := "DIAN_024"
(0008,0080) = "UCLA BRAIN MAPPING CENTER" : (0008,0080) := "DIAN_035" 
(0008,0080) = "Brain Mapping" : (0008,0080) := "DIAN_035" 
(0008,0080) = "Ser.Nr. 58013" : (0008,0080) := "DIAN_035"
(0008,0080) = "Indiana University Radiology Associates" : (0008,0080) := "DIAN_037" 
(0008,0080) = "INDIANA UNIVERSITY RESEARCH" : (0008,0080) := "DIAN_037" 
(0008,0080) = "Martinos Center Bay 4" : (0008,0080) := "DIAN_094" 
(0008,0080) = "Massachusetts General Hospital" : (0008,0080) := "DIAN_094"
(0008,0080) = "BROWN UNIVERSITY MRF" : (0008,0080) := "DIAN_941" 
(0008,0080) = "POW Medical Research Institute" : (0008,0080) := "DIAN_950"
(0008,0080) = "Diagnostic MRI Services" : (0008,0080) := "DIAN_950"
(0008,0080) = "Austin Health, Centre for PET" : (0008,0080) := project 
(0008,0080) = "<Full Institution Name>" : (0008,0080) := "DIAN_951"
(0008,0080) = "Brain Research Institute" : (0008,0080) := "DIAN_951"
(0008,0080) = "SKG Radiology Subiaco" : (0008,0080) := "DIAN_952"
(0008,0080) = "0ECU 0952" : (0008,0080) := "DIAN_952"
(0008,0080) = "0ECU0952" : (0008,0080) := "DIAN_952"
(0008,0080) = "The WA PET/Cyclotron Service" : (0008,0080) := "DIAN_952"
(0008,0080) = "National Hospital for Neurology & Neurosurgery" : (0008,0080) := "DIAN_953"
(0008,0080) = "nhnn" : (0008,0080) := "DIAN_953"
(0008,0080) = "NHNN" : (0008,0080) := "DIAN_953"
(0008,0080) = "Addenbrookes Cambridge" : (0008,0080) := "DIAN_953"
(0008,0080) = "National Hospital for Neurology and Neurosurgery" : (0008,0080) := "DIAN_953"
(0008,0080) = "Hospital" : (0008,0080) := "DIAN_Test"
- (0010,0021)							// Issuer of Patient ID
- (0010,0030)							// Patient's Birth Date
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
(0012,0063) := "DIAN common deidentification v001"
- (0038,0500)							// Patient State
- (0038,0100)							// Pertinent Documents SQ

session := makeSessionLabel[format["{0}_v##_{1}", subject, lowercase[modalityLabel]]]

visit := match[session,".*_(v[0-9]+)_.*",1]                     // Uses a regular expression to pull the visit ID out of thesession label
describe visit hidden                                           // Tells the UI not to show the visit field in the assign sesion variables page
export visit "xnat:experimentData/visit_id"                     // Tells the uploader to use visit to set the named XNAT fied

// Set XNAT identifying fields
(0008,1030) := project
(0010,0010) := subject
(0010,0020) := session
