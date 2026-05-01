package org.nrg.dicom.dicomedit.mizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestMizerConfig.class)
public class TestPersistentContext {

    @Autowired
    DE6Mizer mizer;

    @Test
    public void testPersistentContext() {
        try {
            List<String> script = new ArrayList<>();
            script.add("version \"6.1\"");
            script.add("(0020,000E) := mapUID[(0020,000E), \"6.666\"]");

            MizerContextWithScript context = createContext( "ProjectA", "SubjectA", "SessionA", 0l, script, false);

            mizer.setContext( context);
        } catch (MizerException e) {
            fail( "Unexpected exception: " + e);
        }
    }

    public MizerContextWithScript createContext( final String project, final String subject, final String session, final long scriptId, final Object script, final boolean record) throws MizerException {
        final MizerContextWithScript mizerContext = new MizerContextWithScript();
        mizerContext.setScriptId(scriptId);
        mizerContext.setElement("project", project);
        mizerContext.setElement("subject", subject);
        mizerContext.setElement("session", session);
        if (script instanceof List) {
            //noinspection unchecked
            mizerContext.setScript((List<String>) script);
        } else if (script instanceof InputStream) {
            mizerContext.setScript((InputStream) script);
        } else {
            mizerContext.setScript(script.toString());
        }
        mizerContext.setRecord(record);
        return mizerContext;
    }

}
