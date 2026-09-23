import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * Run inside the XNAT pod (source-file mode, dcm4che-core and slf4j-api from the webapp on the classpath):
 * reads file paths from stdin, one per line, and prints "&lt;sha256&gt; &lt;path&gt;" per file, where the digest is
 * over the dataset re-encoded as Explicit VR Little Endian with DeidentificationMethodCodeSequence (0012,0064)
 * removed. That sequence is where the anonymizer records the ID of the script it applied, and the ID moves every
 * time a script is (re)installed, so raw file bytes differ from run to run for an anonymized object whose content
 * did not. Re-encoding also makes Big Endian and Deflated objects comparable with their Explicit LE equivalents.
 */
public class ObjectDigest {
    public static void main(final String[] args) throws Exception {
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String path;
        while ((path = in.readLine()) != null) {
            if (path.isEmpty()) {
                continue;
            }
            final Attributes dataset;
            try (DicomInputStream dis = new DicomInputStream(new File(path))) {
                dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.YES);
                dataset = dis.readDataset();
            }
            dataset.remove(Tag.DeidentificationMethodCodeSequence);
            final MessageDigest sha = MessageDigest.getInstance("SHA-256");
            try (DicomOutputStream dos = new DicomOutputStream(new DigestOutputStream(OutputStream.nullOutputStream(), sha),
                                                               UID.ExplicitVRLittleEndian)) {
                dos.writeDataset(null, dataset);
            }
            final StringBuilder hex = new StringBuilder();
            for (final byte b : sha.digest()) {
                hex.append(String.format("%02x", b));
            }
            System.out.println(hex + " " + path);
        }
    }
}
