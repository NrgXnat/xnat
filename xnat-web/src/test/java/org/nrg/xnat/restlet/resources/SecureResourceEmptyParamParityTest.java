package org.nrg.xnat.restlet.resources;

import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Restlet 1.1 parsed an empty-valued query parameter ({@code ?a=}) to a null value; Restlet 2.x
 * parses it to the empty string. XNAT's REST layer was written against the 1.1 semantics: a null
 * value means "parameter absent" and PopulateItem skips the field, while an empty string is set
 * verbatim on the item — which for typed columns dies in the database (e.g. the REST test
 * harness's PET session PUT with {@code xnat:petSessionData/tracer/startTime=} → PSQLException
 * "invalid input syntax for type timestamp: ''"). The readers normalize empty to null.
 */
public class SecureResourceEmptyParamParityTest {

    private static Request request() {
        return new Request(Method.PUT, "http://localhost/data/test?a=&b=1&c");
    }

    @Test
    public void emptyValuedParameterReadsAsNull() {
        assertNull(SecureResource.getQueryVariable("a", request()));
    }

    @Test
    public void normalValueUnaffected() {
        assertEquals("1", SecureResource.getQueryVariable("b", request()));
    }

    @Test
    public void valuelessParameterReadsAsNull() {
        assertNull(SecureResource.getQueryVariable("c", request()));
    }

    @Test
    public void absentParameterReadsAsNull() {
        assertNull(SecureResource.getQueryVariable("missing", request()));
    }
}
