package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.values.UIDValue;

public class UIDUtils {

    /**
     * Generate a new UID by appending the hash of the provided value to the prefix.
     *
     * @param prefix The prefix of the new UID.  (Handles trailing "." appropriately)
     * @param value The value to be hashed.
     * @return the new UID.
     * @throws ScriptEvaluationException
     */
    protected static String getHashedUID( String prefix, String value) throws ScriptEvaluationException {
        String hash = UIDValue.getHashedUIDValue( value).asString();
        hash = (hash.startsWith("2.25."))? hash.substring( "2.25.".length()): hash;
        String newUID;
        if( prefix.endsWith(".")) {
            newUID = prefix + hash;
        }
        else {
            newUID = prefix + "." + hash;
        }
        return newUID;
    }

    protected static String createUID() {
        return org.dcm4che3.util.UIDUtils.createUID();
    }

    protected static String createUID( String root) {
        return org.dcm4che3.util.UIDUtils.createUID(root);
    }

}
