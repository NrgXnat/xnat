package org.nrg.xnat.restlet.resources.search;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit coverage for {@link SearchResource#extractSearchXml(String)} — recovering the stored-search XML
 * from a YUI-Connect POST body that prepends the URL query string to the (percent-encoded) XML.
 */
public class SearchResourceExtractXmlTest {

    private static final String XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<xdat:bundle ID=\"pr\"/>";

    /** Bare XML (e.g. a REST client posting text/xml) is returned unchanged. */
    @Test
    public void passesThroughBareXml() {
        assertEquals(XML, SearchResource.extractSearchXml(XML));
    }

    /** Bare XML with leading whitespace is still treated as XML. */
    @Test
    public void passesThroughBareXmlWithLeadingWhitespace() {
        final String padded = "  \n" + XML;
        assertEquals(padded, SearchResource.extractSearchXml(padded));
    }

    /** Form-wrapped body with a literal (unencoded) &lt;?xml is trimmed to the XML. */
    @Test
    public void stripsFormParamsBeforeLiteralXml() {
        final String body = "XNAT_CSRF=abc&format=json&cache=true&refresh=true&" + XML;
        assertEquals(XML, SearchResource.extractSearchXml(body));
    }

    /** The real case from the deploy: params + percent-encoded XML → decoded XML. */
    @Test
    public void stripsFormParamsAndDecodesEncodedXml() {
        final String encoded =
                "%3C%3Fxml%20version=%221.0%22%20encoding%3D%22UTF-8%22%20standalone%3D%22yes%22%3F%3E"
                + "%0A%3Cxdat%3Abundle%20ID%3D%22pr%22/%3E";
        final String body = "XNAT_CSRF=19e0c8d4-4a7e-4df2-8456-34e75717c4cb&format=json&cache=true&refresh=true&" + encoded;
        final String result = SearchResource.extractSearchXml(body);
        assertTrue("expected decoded XML prolog, got: " + result, result.startsWith("<?xml version=\"1.0\""));
        assertTrue("expected the bundle element, got: " + result, result.contains("<xdat:bundle ID=\"pr\"/>"));
    }

    @Test
    public void nullBodyBecomesEmpty() {
        assertEquals("", SearchResource.extractSearchXml(null));
    }
}
