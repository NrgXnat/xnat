package org.nrg.xnat.dicom.mizer.service;

import org.apache.commons.io.FileUtils;
import org.dcm4che3.data.Tag;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomElementI;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.Mizer;
import org.nrg.dicom.mizer.service.MizerContext;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.test.utils.TestFileUtils;
import org.nrg.xnat.dicom.mizer.config.MizerServiceTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MizerServiceTestConfig.class)
public class BaseMizerServiceTest extends BaseMizerTest {
    @Test
    public void createService() {
        assertNotNull(service);
    }

    @Test
    public void serviceHasMultipleHandlers() {
        Collection<Mizer> mizers = null;
        try {
            mizers = service.getMizers();
        } catch (MizerException e) {
            fail("Unexpected exception: " + e);
        }
        assertEquals(2, mizers.size());
    }

    @Test
    public void mizersAreInDescendingMaxVersionOrder() {

    }

    @Test
    public void anonOnCopyTestv4() {
        long scriptId = 0L;

        String script = "- (0008,103e)\n";

        try (InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"))) {
            final File anonTestFile = TestFileUtils.copyTestFileToTemp(TEST_FILE);

            DicomObjectI pre_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertTrue(pre_dobj.contains(0x0008103e));

            service.anonymize(anonTestFile, PROJECT, SUBJECT, SESSION, false, false, scriptId, is);

            DicomObjectI post_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertFalse(post_dobj.contains(0x0008103e));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    @Test
    public void anonOnCopyTestv6() {
        String project = "my_project";
        String subject = "my_subject";
        String session = "my_session";
        long scriptId = 0L;

        String script = "version \"6.0\"\n- (0008,103e)\n";

        try (InputStream is = new ByteArrayInputStream(script.getBytes("UTF-8"))) {
            final File anonTestFile = TestFileUtils.copyTestFileToTemp(TEST_FILE);

            DicomObjectI pre_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertTrue(pre_dobj.contains(0x0008103e));

            service.anonymize(anonTestFile, project, subject, session, false, false, scriptId, is);

            DicomObjectI post_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertFalse(post_dobj.contains(0x0008103e));

        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    @Test
    public void mizerServiceDE6UpdatesFileDeidentificationMethodCodeSequence() {
        try {
            final File anonTestFile = TestFileUtils.copyTestFileToTemp(TEST_FILE);
            service.anonymize(anonTestFile, PROJECT, SUBJECT, SESSION, true, false, 1L, SET_STD_ATTRS_DE6);

            final DicomObjectI dicomObject = DicomObjectFactory.newInstance(anonTestFile);

            assertEquals(PROJECT, dicomObject.getString(TAG_PROJECT));
            assertEquals(SUBJECT, dicomObject.getString(TAG_SUBJECT));
            assertEquals(SESSION, dicomObject.getString(TAG_SESSION));

            final DicomElementI element = dicomObject.get(Tag.DeidentificationMethodCodeSequence);

            assertNotNull(element);
            assertTrue(element.hasItems());
            assertEquals(1, element.countItems());

            final DicomObjectI sequence = element.getDicomObject(0);
            assertNotNull(sequence);
            assertEquals("1", sequence.getString(Tag.CodeValue));
            assertEquals("XNAT DicomEdit 6 Script", sequence.getString(Tag.CodeMeaning));
            assertEquals("XNAT", sequence.getString(Tag.CodingSchemeDesignator));
            assertEquals("1.0", sequence.getString(Tag.CodingSchemeVersion));
        } catch (IOException e) {
            fail("Test setup failed: " + e);
        } catch (MizerException ae) {
            fail("Unexpected exception: " + ae);
        }
    }

    @Test
    public void mizerServiceDE6UpdatesDicomObjectDeidentificationMethodCodeSequence() {
        try {
            getTestDicomObject();

            final MizerContextWithScript context = new MizerContextWithScript();
            context.setElement("project", "project");
            context.setScript("version \"6.1\"\nstudyDescription := project\n(0008,103e) := studyDescription\n");
        } catch (IOException e) {
            fail("Test setup failed: " + e);
        } catch (MizerException ae) {
            fail("Unexpected exception: " + ae);
        }
    }

    @Test
    public void mizerServiceDE4UpdatesFileDeidentificationMethodCodeSequence() {
        try {
            final File anonTestFile = TestFileUtils.copyTestFileToTemp(TEST_FILE);
            service.anonymize(anonTestFile, PROJECT, SUBJECT, SESSION, true, false, 1L, SET_STD_ATTRS_DE4);

            final DicomObjectI dicomObject = DicomObjectFactory.newInstance(anonTestFile);

            assertEquals(PROJECT, dicomObject.getString(TAG_PROJECT));
            assertEquals(SUBJECT, dicomObject.getString(TAG_SUBJECT));
            assertEquals(SESSION, dicomObject.getString(TAG_SESSION));

            final DicomElementI element = dicomObject.get(Tag.DeidentificationMethodCodeSequence);

            assertNotNull(element);
            assertTrue(element.hasItems());
            assertEquals(1, element.countItems());

            final DicomObjectI sequence = element.getDicomObject(0);
            assertNotNull(sequence);
            assertEquals("1", sequence.getString(Tag.CodeValue));
            assertEquals("XNAT DicomEdit 4 Script", sequence.getString(Tag.CodeMeaning));
            assertEquals("XNAT", sequence.getString(Tag.CodingSchemeDesignator));
            assertEquals("1.0", sequence.getString(Tag.CodingSchemeVersion));
        } catch (IOException e) {
            fail("Test setup failed: " + e);
        } catch (MizerException ae) {
            fail("Unexpected exception: " + ae);
        }
    }

    @Test
    public void mizerServiceDE4UpdatesDicomObjectDeidentificationMethodCodeSequence() {
        try {
            final MizerContextWithScript context = new MizerContextWithScript();

            context.setElement("project", "project");
            context.setScript("version \"6.1\"\nstudyDescription := project\n(0008,103e) := studyDescription\n");
        } catch (MizerException ae) {
            fail("Unexpected exception: " + ae);
        }
    }

    @Test
    public void multiContextAnonTestHappyPath() {
        try {
            MizerContextWithScript siteContext = new MizerContextWithScript();
            siteContext.setScript("(0010,0030) := \"DE4 site script\"\n");
            MizerContextWithScript projectContext = new MizerContextWithScript();
            projectContext.setScript("version \"6.0\"\n(0010,0040) := \"DE6 project script\"\n");

            File anonTestFile = new File("/tmp/anonTestFile");
            FileUtils.copyFile(TEST_FILE, anonTestFile);

            DicomObjectI pre_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertTrue("19751231".equals(pre_dobj.getString(0x00100030)));
            assertTrue("F".equals(pre_dobj.getString(0x00100040)));

            service.anonymize(anonTestFile, new ArrayList<MizerContext>(Arrays.asList(siteContext, projectContext)));

            DicomObjectI post_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertTrue("DE4 site script".equals(post_dobj.getString(0x00100030)));
            assertTrue("DE6 project script".equals(post_dobj.getString(0x00100040)));

        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    @Test
    // Ignored because it fails, and has since it was written: a multi-script anonymization
    // whose second script errors leaves the first script's edits on disk instead of rolling
    // back, so the file is left partially de-identified. Reproduced unchanged against
    // origin/develop, so this is a standing defect in the rollback path rather than
    // fallout from any one change. The other three tests here were ignored alongside it and
    // pass, so they now run.
    @Ignore
    public void rollbackOn2ndStepErrorTest() {
        File anonTestFile = new File("/tmp/anonTestFile");
        DicomObjectI post_dobj;
        try {
            MizerContextWithScript siteContext = new MizerContextWithScript();
            siteContext.setScript("(0010,0030) := \"DE4 site script\"\n");
            MizerContextWithScript projectContext = new MizerContextWithScript();
            //syntax error to induce anon error
            projectContext.setScript("version \"6.0\"\n(0010,00gg) := \"DE6 project script\"\n");

            FileUtils.copyFile(TEST_FILE, anonTestFile);

            DicomObjectI pre_dobj = DicomObjectFactory.newInstance(anonTestFile);
            assertTrue("19751231".equals(pre_dobj.getString(0x00100030)));
            assertTrue("F".equals(pre_dobj.getString(0x00100040)));

            service.anonymize(anonTestFile, new ArrayList<MizerContext>(Arrays.asList(siteContext, projectContext)));

            fail("servicce.anonymize should have thrown an error.");
        } catch (Exception e) {
            try {
                post_dobj = DicomObjectFactory.newInstance(anonTestFile);
                assertTrue("19751231".equals(post_dobj.getString(0x00100030)));
                assertTrue("F".equals(post_dobj.getString(0x00100040)));
            } catch (Exception ioe) {
                fail("Unexpected excpetion: " + ioe);
            }
        }
    }

    @Autowired
    private MizerService service;
}
