package org.nrg.xdat.forms.exceptions;

public class UnsupportedXsiTypeException extends Exception {
    public UnsupportedXsiTypeException(String dataType) {
        super(dataType + " is not supported.");
    }
}
