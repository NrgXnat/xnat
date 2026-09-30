package org.nrg.dicom.mizer.service.impl;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * How the file anonymization path reads an object, which decides whether its pixel data reaches the mizer on the
 * heap or as a reference into the source file.
 * <p>
 * A reference is what lets an object over 2 GB be anonymized at all: {@code DicomInputStream.readValue()} reads a
 * value into a {@code byte[]}, so past {@link Integer#MAX_VALUE} it cannot represent one and fails with "tag value
 * too large" however much heap is available. {@code LargeDicomObjectTest} pins that for {@code DicomObjectFactory},
 * and {@code BulkDataLoadingTest} pins what the factory does with each setting. A reference also costs a reopen of
 * the source for every value when the object is written, which is why an ordinary object is read whole.
 * <p>
 * Asserted by watching what arrives, since the bytes written are the same either way: the mizer is a stub that
 * records the form the pixel data reached it in, and edits the header as a script would.
 */
public class AnonymizeCallOnFileWithPixelsTest {

    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final AtomicReference<Object> pixelData = new AtomicReference<>();

    private final TestMizer recording = new TestMizer() {
        @Override
        protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject,
                                                    final MizerContextWithScript context)
                throws MizerException {
            pixelData.set(dicomObject.getAttributes().getValue(Tag.PixelData));
            dicomObject.getAttributes().setString(Tag.PatientName, VR.PN, "ANON^SUBJECT");
            dicomObject.getAttributes().remove(Tag.PatientBirthDate);
            return super.anonymizeImpl(dicomObject, context);
        }
    };

    @Test
    public void readsAnOrdinaryObjectOntoTheHeapInOnePass() throws Exception {
        final File output = anonymize(copyOfFixture());

        assertNotNull("the mizer should have been given the object", pixelData.get());
        assertTrue("an object within the limit should reach the mizer read whole, not as references the write "
                   + "has to reopen the file for; got " + pixelData.get().getClass().getName(),
                   pixelData.get() instanceof byte[]);
        assertTrue("the anonymized object should have been written out", output.length() > 0);
    }

    @Test
    public void handsAnObjectOverTheLimitToTheMizerAsAReference() throws Exception {
        final File source = temporaryFolder.newFile("large.dcm");
        writeObjectWithPixelData(source, (int) AnonymizeCallOnFileWithPixels.WHOLE_READ_LIMIT);
        assertTrue(source.length() > AnonymizeCallOnFileWithPixels.WHOLE_READ_LIMIT);

        anonymize(source);

        assertNotNull("the mizer should have been given the object", pixelData.get());
        assertTrue("pixel data of an object over the limit should reach the mizer as a reference into the source "
                   + "file, not as a copy on the heap, or an object larger than 2 GB could not be read at all; got "
                   + pixelData.get().getClass().getName(),
                   pixelData.get() instanceof BulkData);
    }

    @Test
    public void writesTheSameBytesWhicheverWayTheObjectIsRead() throws Exception {
        final File   source = copyOfFixture();
        final byte[] whole  = Files.readAllBytes(anonymize(source).toPath());

        final DicomObjectI          byReference = DicomObjectFactory.newInstance(source, DicomInputStream.IncludeBulkData.URI);
        final ByteArrayOutputStream written     = new ByteArrayOutputStream();
        try {
            recording.anonymize(byReference, new MizerContextWithScript()).getDicomObject().write(written);
        } finally {
            byReference.releaseScratchFiles();
        }
        assertTrue("the reference read should have kept the pixel data in the file", pixelData.get() instanceof BulkData);
        assertArrayEquals("reading whole must write exactly what reading by reference did", whole, written.toByteArray());
    }

    @Test
    public void readsWholeUpToTheLimitAndByReferenceBeyondIt() {
        assertEquals(DicomInputStream.IncludeBulkData.YES,
                     AnonymizeCallOnFileWithPixels.bulkDataHandlingFor(AnonymizeCallOnFileWithPixels.WHOLE_READ_LIMIT));
        assertEquals(DicomInputStream.IncludeBulkData.URI,
                     AnonymizeCallOnFileWithPixels.bulkDataHandlingFor(AnonymizeCallOnFileWithPixels.WHOLE_READ_LIMIT + 1));
    }

    private File anonymize(final File source) throws Exception {
        final File output = temporaryFolder.newFile();
        final AnonymizeCallOnFileWithPixels call =
                new AnonymizeCallOnFileWithPixels(source, recording, new MizerContextWithScript());
        call.setFile(output);
        assertNotNull("the anonymization should have produced a result", call.call());
        return output;
    }

    private File copyOfFixture() throws Exception {
        final File source = temporaryFolder.newFile("source.dcm");
        Files.copy(new File(AnonymizeCallOnFileWithPixelsTest.class.getClassLoader().getResource(FIXTURE).toURI()).toPath(),
                   source.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return source;
    }

    /** An object whose pixel data alone is the given length, which puts the file just past it. */
    private static void writeObjectWithPixelData(final File file, final int length) throws IOException {
        final int rows = 4096, columns = 4096;
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.SecondaryCaptureImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, "1.2.826.0.1.3680043.8.498.201");
        dataset.setString(Tag.PatientName, VR.PN, "REAL^PATIENT^NAME");
        dataset.setString(Tag.PatientBirthDate, VR.DA, "19700101");
        dataset.setInt(Tag.Rows, VR.US, rows);
        dataset.setInt(Tag.Columns, VR.US, columns);
        dataset.setInt(Tag.BitsAllocated, VR.US, 16);
        dataset.setInt(Tag.BitsStored, VR.US, 16);
        dataset.setInt(Tag.HighBit, VR.US, 15);
        dataset.setInt(Tag.SamplesPerPixel, VR.US, 1);
        dataset.setInt(Tag.PixelRepresentation, VR.US, 0);
        dataset.setString(Tag.PhotometricInterpretation, VR.CS, "MONOCHROME2");
        dataset.setInt(Tag.NumberOfFrames, VR.IS, length / (rows * columns * 2));
        dataset.setBytes(Tag.PixelData, VR.OW, new byte[length]);
        try (DicomOutputStream out = new DicomOutputStream(new BufferedOutputStream(new FileOutputStream(file), 1 << 20),
                                                           UID.ExplicitVRLittleEndian)) {
            out.writeDataset(dataset.createFileMetaInformation(UID.ExplicitVRLittleEndian), dataset);
        }
    }
}
