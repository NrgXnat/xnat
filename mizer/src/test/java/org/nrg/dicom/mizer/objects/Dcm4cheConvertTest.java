package org.nrg.dicom.mizer.objects;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Pins the documented contract of {@link Dcm4cheConvert#extractFmiFromDataset(Attributes)}: the method
 * strips the file meta information (group 0002) out of the dataset it is given and returns that same,
 * now FMI-free, instance. Callers that still need the FMI afterwards must add it back themselves,
 * which is exactly what the production callers do.
 */
public class Dcm4cheConvertTest {
    private static final String SOP_INSTANCE_UID = "1.2.826.0.1.3680043.8.498.1";

    private static Attributes datasetWithFmi() {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.TransferSyntaxUID, VR.UI, UID.ExplicitVRLittleEndian);
        dataset.setString(Tag.MediaStorageSOPClassUID, VR.UI, UID.MRImageStorage);
        dataset.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, SOP_INSTANCE_UID);
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, SOP_INSTANCE_UID);
        dataset.setString(Tag.PatientName, VR.PN, "Doe^Jane");
        return dataset;
    }

    private static Attributes datasetWithoutFmi() {
        final Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, UID.MRImageStorage);
        dataset.setString(Tag.SOPInstanceUID, VR.UI, SOP_INSTANCE_UID);
        dataset.setString(Tag.PatientName, VR.PN, "Doe^Jane");
        return dataset;
    }

    @Test
    public void extractFmiFromDatasetStripsGroup0002FromTheInputInstance() {
        final Attributes dataset = datasetWithFmi();

        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.extractFmiFromDataset(dataset);

        assertSame("onlyDataset must be the input instance, not a copy", dataset, split.onlyDataset);
        assertFalse(dataset.contains(Tag.TransferSyntaxUID));
        assertFalse(dataset.contains(Tag.MediaStorageSOPClassUID));
        assertFalse(dataset.contains(Tag.MediaStorageSOPInstanceUID));
        assertTrue(dataset.contains(Tag.SOPClassUID));
        assertEquals("Doe^Jane", dataset.getString(Tag.PatientName));
    }

    @Test
    public void extractFmiFromDatasetReturnsTheStrippedGroup0002AsFmi() {
        final Attributes dataset = datasetWithFmi();

        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.extractFmiFromDataset(dataset);

        assertEquals(UID.ExplicitVRLittleEndian, split.fmi.getString(Tag.TransferSyntaxUID));
        assertEquals(UID.MRImageStorage, split.fmi.getString(Tag.MediaStorageSOPClassUID));
        assertEquals(SOP_INSTANCE_UID, split.fmi.getString(Tag.MediaStorageSOPInstanceUID));
        assertFalse(split.fmi.contains(Tag.PatientName));
    }

    @Test
    public void extractFmiFromDatasetCreatesExplicitVrLittleEndianFmiWhenInputHasNone() {
        final Attributes dataset = datasetWithoutFmi();

        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.extractFmiFromDataset(dataset);

        assertSame(dataset, split.onlyDataset);
        assertEquals(UID.ExplicitVRLittleEndian, split.fmi.getString(Tag.TransferSyntaxUID));
        assertEquals(UID.MRImageStorage, split.fmi.getString(Tag.MediaStorageSOPClassUID));
        assertEquals(SOP_INSTANCE_UID, split.fmi.getString(Tag.MediaStorageSOPInstanceUID));
    }

    @Test
    public void addingTheFmiBackRestoresTheInputForCallersThatNeedIt() {
        final Attributes dataset = datasetWithFmi();

        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.extractFmiFromDataset(dataset);
        dataset.addAll(split.fmi);

        assertEquals(UID.ExplicitVRLittleEndian, dataset.getString(Tag.TransferSyntaxUID));
        assertEquals(SOP_INSTANCE_UID, dataset.getString(Tag.MediaStorageSOPInstanceUID));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedSplitFmiAndDatasetKeepsTheOldBehaviour() {
        final Attributes dataset = datasetWithFmi();

        final Dcm4cheConvert.SplitAttributes split = Dcm4cheConvert.splitFmiAndDataset(dataset);

        assertSame(dataset, split.onlyDataset);
        assertFalse(dataset.contains(Tag.TransferSyntaxUID));
        assertEquals(UID.ExplicitVRLittleEndian, split.fmi.getString(Tag.TransferSyntaxUID));
    }
}
