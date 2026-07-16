package org.nrg.xnat.restlet.resources;

import org.junit.Test;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Restlet 1.1 answered entity-less success with 200 (OK); Restlet 2.x ServerResource.handle()
 * rewrites an entity-less 200 to 204 (No Content) after the handler returns. XNAT's REST clients
 * and the xnat_rest_tests harness assert the 1.1 behavior (e.g. {@code XnatInterface.logout()} on
 * {@code DELETE /data/JSESSION/...} expects 200), so the SecureResource bridge marks handlers that
 * finish with the default bodyless OK and restores the 200 after super.handle().
 */
public class SecureResourceOkParityTest {

    @Test
    public void marksHandlerThatFinishedWithDefaultBodylessOk() {
        assertTrue(SecureResource.isBodylessOk(null, Status.SUCCESS_OK));
    }

    @Test
    public void doesNotMarkWhenHandlerSetAnEntity() {
        assertFalse(SecureResource.isBodylessOk(new StringRepresentation("body"), Status.SUCCESS_OK));
    }

    @Test
    public void doesNotMarkExplicitNonOkStatuses() {
        assertFalse(SecureResource.isBodylessOk(null, Status.SUCCESS_NO_CONTENT));
        assertFalse(SecureResource.isBodylessOk(null, Status.SUCCESS_CREATED));
        assertFalse(SecureResource.isBodylessOk(null, Status.CLIENT_ERROR_NOT_FOUND));
        assertFalse(SecureResource.isBodylessOk(null, null));
    }

    @Test
    public void restoresRewritten204To200WhenMarked() {
        assertEquals(Status.SUCCESS_OK, SecureResource.okParity(true, Status.SUCCESS_NO_CONTENT));
    }

    @Test
    public void leavesStatusAloneWhenNotMarked() {
        assertEquals(Status.SUCCESS_NO_CONTENT, SecureResource.okParity(false, Status.SUCCESS_NO_CONTENT));
    }

    @Test
    public void leavesNon204StatusesAloneEvenWhenMarked() {
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, SecureResource.okParity(true, Status.CLIENT_ERROR_FORBIDDEN));
        assertEquals(Status.SUCCESS_OK, SecureResource.okParity(true, Status.SUCCESS_OK));
    }
}
