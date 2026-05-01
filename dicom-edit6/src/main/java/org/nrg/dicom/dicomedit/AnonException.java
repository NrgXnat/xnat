package org.nrg.dicom.dicomedit;

/**
 * Created by davidmaffitt on 4/6/17.
 */
public class AnonException extends Exception {

    public AnonException(String msg) {
        super( msg);
    }

    public AnonException(String msg, Exception e) {
        super(msg, e);
    }

    public AnonException(Throwable e) {
        super( e);
    }
}
