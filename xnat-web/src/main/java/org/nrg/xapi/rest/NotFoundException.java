package org.nrg.xapi.rest;

public class NotFoundException extends ApiException {
    public NotFoundException(int code, String msg) {
        super(code, msg);
    }
}
