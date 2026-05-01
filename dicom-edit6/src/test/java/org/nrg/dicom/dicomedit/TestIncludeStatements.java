package org.nrg.dicom.dicomedit;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIncludeStatements {

    /**
     * This is for manual inspection of the output to see what info is leaked from an included file.
     */
    @Test
    @Ignore
    public void testIncludedFileLeak() {
        try {

            final String script =
//                    "include file:///etc/passwd \n";
                    "include file:///tmp/foo \n";
            DE6Script de6Script = new DE6Script.Builder().statement(script).build();

        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test()
    @Ignore
    public void testIncludedMissingFile() {
        try {

            final String script =
                    "include file:///tmp/xyzzz \n";
            DE6Script de6Script = new DE6Script.Builder()
                    .statement(script)
                    .processor(new ScriptDirectiveProcessorInclude())
                    .build();

            fail("Missing fileNotFoundException .");

        }
        catch( Exception e) {
            assertTrue(e.getMessage().contains("NoSuchFile"));
        }
    }

}