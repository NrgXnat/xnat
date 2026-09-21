// QA ingest-io site-wide script: a realistic mix of the things site scripts do.
// Header edits only; the redaction lives in project QA_REDACT's script.
version "6.1"

// Identity, as the default XNAT script does it
project != "Unassigned" ? (0008,1030) := project
(0010,0010) := subject
(0010,0020) := session

// Constants that mark the object as having passed through this script
(0008,0080) := "XNAT QA ingest-io"
(0012,0062) := "YES"
(0012,0063) := "XNAT QA site script"

// Removals: the usual identifying elements
- (0008,0081)
- (0008,0090)
- (0008,1010)
- (0008,1040)
- (0008,1048)
- (0008,1050)
- (0008,1060)
- (0008,1070)
- (0010,0030)
- (0010,0032)
- (0010,1000)
- (0010,1001)
- (0010,1005)
- (0010,1010)
- (0010,1040)
- (0010,1060)
- (0010,2154)
- (0010,2160)
- (0010,2180)
- (0010,21B0)
- (0010,4000)
- (0020,4000)
- (0032,1032)
- (0038,0300)
- (0040,0006)
- (0040,0275)
- (0040,1001)
- (0040,1400)
- (4008,0114)

// NB: no date shifting here. shiftDateByIncrement throws on a present-but-empty or malformed date
// (which real vendor files in Group A carry), and a script error aborts the whole C-STORE
// association -- a pre-existing XNAT fragility, unrelated to the I/O change under test. Left out so
// the corpus anonymizes deterministically on both builds. It is a genuine finding for the report,
// not something to route around by editing the importer.

// Private tags go, wherever they are in the object, including after the pixel data
removeAllPrivateTags

// Pixel redaction, to exercise the streaming rectangle pixel-edit path on the receive/site path.
alterPixels["rectangle", "l=10, t=10, r=60, b=60", "solid", "v=0"]
