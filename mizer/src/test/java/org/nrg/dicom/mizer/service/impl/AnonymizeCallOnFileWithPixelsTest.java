package org.nrg.dicom.mizer.service.impl;

import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.impl.test.TestMizer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The file anonymization path reads pixel data by reference rather than onto the heap.
 * <p>
 * That one argument is what lets an object over 2 GB be anonymized at all:
 * {@code DicomInputStream.readValue()} reads a value into a {@code byte[]}, so past
 * {@link Integer#MAX_VALUE} it cannot represent one and fails with "tag value too large" however
 * much heap is available. {@code LargeDicomObjectTest} pins that for
 * {@code DicomObjectFactory}, and {@code BulkDataLoadingTest} pins what the factory does with each
 * setting, but neither reaches this call site -- changing it to
 * {@code IncludeBulkData.YES} broke no test at all, which is what this closes.
 * <p>
 * Asserted by watching what arrives, since nothing about the finished output distinguishes the two:
 * the bytes written are identical either way, and a pixel edit stages a scratch file from the heap
 * just as readily. What differs is only whether the value could have been represented, which cannot
 * be observed on an object small enough to be a fixture. So the mizer is a stub that records the
 * form the pixel data reached it in.
 */
public class AnonymizeCallOnFileWithPixelsTest {

    private static final String FIXTURE = "dicom/1.MR.head_DHead.4.1.20061214.091206.156000.1632817982.dcm";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void handsPixelDataToTheMizerAsAReferenceRatherThanOnTheHeap() throws Exception {
        final File source = temporaryFolder.newFile("source.dcm");
        Files.copy(fixture().toPath(), source.toPath(), StandardCopyOption.REPLACE_EXISTING);
        final File output = temporaryFolder.newFile("output.dcm");

        final AtomicReference<Object> pixelData = new AtomicReference<>();
        final TestMizer recording = new TestMizer() {
            @Override
            protected AnonymizationResult anonymizeImpl(final DicomObjectI dicomObject,
                                                        final MizerContextWithScript context)
                    throws MizerException {
                pixelData.set(dicomObject.getAttributes().getValue(Tag.PixelData));
                return super.anonymizeImpl(dicomObject, context);
            }
        };

        final AnonymizeCallOnFileWithPixels call =
                new AnonymizeCallOnFileWithPixels(source, recording, new MizerContextWithScript());
        call.setFile(output);
        final AnonymizationResult result = call.call();

        assertNotNull("the anonymization should have produced a result", result);
        assertTrue("the mizer should have been given the object", pixelData.get() != null);
        assertTrue("pixel data should reach the mizer as a reference into the source file, not as a "
                   + "copy on the heap, or an object larger than 2 GB could not be read at all; got "
                   + pixelData.get().getClass().getName(),
                   pixelData.get() instanceof BulkData);
        assertTrue("the anonymized object should have been written out", output.length() > 0);
    }

    private static File fixture() throws Exception {
        return new File(AnonymizeCallOnFileWithPixelsTest.class.getClassLoader().getResource(FIXTURE).toURI());
    }
}
