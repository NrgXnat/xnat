package org.nrg.xnat.restlet;

import org.junit.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * XNAT resources routinely reject requests from their 1.1-style (Context, Request, Response)
 * constructor by throwing ResourceException with a meaningful status (e.g. AliasTokenRestlet
 * throws 403 when a non-admin requests a token for another user). Restlet 1.1 honored that
 * status; the 2.x Finder maps a null find() result to 404, so swallowing the constructor
 * exception rewrote every such rejection to 404. The finder must rethrow the ResourceException
 * and let the status service answer with the embedded status.
 */
public class XnatServerResourceFinderTest {

    public static class Throwing403Resource extends ServerResource {
        public Throwing403Resource(final Context context, final Request request, final Response response) {
            throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, "constructor rejection");
        }
    }

    @Test
    public void constructorResourceExceptionPropagatesWithItsStatus() {
        final XnatServerResourceFinder finder = new XnatServerResourceFinder(new Context(), Throwing403Resource.class);
        try {
            finder.find(new Request(Method.GET, "http://localhost/data/test"), new Response(null));
            fail("expected the constructor's ResourceException to propagate");
        } catch (ResourceException e) {
            assertEquals(Status.CLIENT_ERROR_FORBIDDEN, e.getStatus());
        }
    }
}
