package org.nrg.xnat.contrast.parser;


import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.nrg.xnat.contrast.model.ContrastBolus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ContrastParserTest extends TestCase {

    @Test
    public void testCall() throws Exception {
        Path dcmFileEnhanced = Paths.get("src", "test", "resources","contrastParser_enhanced.dcm");
        ContrastParser contrastParser;
        contrastParser = new ContrastParser(dcmFileEnhanced);
        List<ContrastBolus> result = contrastParser.call();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Gadodiamide", result.get(0).getAgent());
        Assert.assertEquals("1", result.get(0).getAgentNumber());
        Assert.assertEquals("4.01", result.get(0).getConcentration());
        Assert.assertEquals(1, result.get(0).getIngredients().size());
        Assert.assertEquals("Xenon", result.get(0).getIngredients().get(0).getName());
        Assert.assertEquals(1, result.get(0).getAdministrations().size());
        Assert.assertEquals("203.75", result.get(0).getAdministrations().get(0).getVolume());
        Path dcmFilePlain = Paths.get("src", "test", "resources","contrastParser_plain.dcm");
        contrastParser = new ContrastParser(dcmFilePlain);
        result = contrastParser.call();
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("Gadolinium", result.get(0).getAgent());
        Assert.assertNull(result.get(0).getAgentNumber());
        Assert.assertNull(result.get(0).getConcentration());
        Assert.assertNull(result.get(0).getIngredients());
        Assert.assertNull(result.get(0).getAdministrations());
    }
}
