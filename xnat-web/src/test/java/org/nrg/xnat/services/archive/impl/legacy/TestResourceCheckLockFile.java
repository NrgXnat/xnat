package org.nrg.xnat.services.archive.impl.legacy;

import org.junit.Test;
import org.mockito.Mockito;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.nrg.xnat.helpers.uri.archive.impl.ExptReconURI;
import org.nrg.xnat.helpers.uri.archive.impl.ExptScanURI;
import org.nrg.xnat.helpers.uri.archive.impl.ExptURI;
import org.nrg.xnat.helpers.uri.archive.impl.ProjSubjAssScanURI;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestResourceCheckLockFile {
    private static final File SESSION_DIR = new File("/data/xnat/archive/PROJ/arc001/SESSION");

    @Test
    public void scansInTheSameSessionDoNotShareALock() {
        assertNotEquals(lockFor(scanUri(ExptScanURI.class, "1")), lockFor(scanUri(ExptScanURI.class, "2")));
    }

    @Test
    public void theSameScanSharesALockWhicheverUriNamesIt() {
        final File lock = lockFor(scanUri(ExptScanURI.class, "3"));
        assertEquals(new File(SESSION_DIR, "SCANS/3/.resourcecheckNIFTI"), lock);
        assertEquals(lock, lockFor(scanUri(ProjSubjAssScanURI.class, "3")));
    }

    @Test
    public void aReconstructionIsLockedOnItsOwnFolder() {
        final ExptReconURI uri = Mockito.mock(ExptReconURI.class);
        final XnatReconstructedimagedata recon = Mockito.mock(XnatReconstructedimagedata.class);
        Mockito.when(recon.getId()).thenReturn("RECON1");
        Mockito.when(uri.getRecon()).thenReturn(recon);
        assertEquals(new File(SESSION_DIR, "ASSESSORS/PROCESSED/RECON1/.resourcecheckNIFTI"), lockFor(uri));
    }

    @Test
    public void otherItemsKeepTheirLock() {
        assertEquals(new File(SESSION_DIR, ".resourcecheckNIFTI"), lockFor(Mockito.mock(ExptURI.class)));
    }

    private static File lockFor(final URIManager.ArchiveItemURI uri) {
        return DefaultCatalogService.getResourceCheckLockFile(uri, SESSION_DIR, "NIFTI");
    }

    private static <T extends URIManager.ArchiveItemURI> T scanUri(final Class<T> type, final String scanId) {
        final XnatImagescandata scan = Mockito.mock(XnatImagescandata.class);
        Mockito.when(scan.getId()).thenReturn(scanId);
        final T uri = Mockito.mock(type);
        Mockito.when(((ScanURII) uri).getScan()).thenReturn(scan);
        return uri;
    }
}
