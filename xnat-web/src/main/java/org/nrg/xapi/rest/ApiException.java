package org.nrg.xapi.rest;

public class ApiException extends Exception {
    public ApiException(int code, String msg) {
        super(msg);
        _code = code;
    }

    public int getCode() {
        return _code;
    }

    private int _code;
}
