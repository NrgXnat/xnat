/*
 * web: org.nrg.xnat.restlet.actions.SessionImportSiteAnonymization
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions;

import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Runs a session import's site anonymization script. The import unpacks the upload into the prearchive before the
 * script runs, and the script changes no instance unless it succeeds on all of them, so when it fails every instance
 * is still as it arrived. Left in the prearchive, those instances would be visible to project users, and archiving
 * the session by hand would skip the site script. The session is removed instead, as the receive path removes an
 * instance whose site script fails.
 * <p>
 * Kept apart from {@link SessionImporter}, whose superclass needs a Spring context to load, so this can be tested on
 * its own.
 */
final class SessionImportSiteAnonymization {
    private SessionImportSiteAnonymization() {
    }

    /** Removes the session from the prearchive, returning whether it did. */
    @FunctionalInterface
    interface Removal {
        boolean remove() throws Exception;
    }

    /** The site script failed and the session was removed from the prearchive. */
    static final class SessionRemovedException extends ServerException {
        SessionRemovedException(final Exception cause) {
            super("The site anonymization script failed, so the upload was removed from the prearchive: " + cause.getMessage(), cause);
        }
    }

    /**
     * @return The site script's results.
     *
     * @throws SessionRemovedException When the script failed and the session was removed.
     * @throws Exception               The script's own failure when the session couldn't be removed, with any failure of
     *                                 the removal attached as suppressed.
     */
    static List<AnonymizationResult> run(final Callable<List<AnonymizationResult>> siteScript, final Removal removal) throws Exception {
        try {
            return siteScript.call();
        } catch (Exception e) {
            boolean removed = false;
            try {
                removed = removal.remove();
            } catch (Exception removalFailure) {
                e.addSuppressed(removalFailure);
            }
            if (removed) {
                throw new SessionRemovedException(e);
            }
            throw e;
        }
    }
}
