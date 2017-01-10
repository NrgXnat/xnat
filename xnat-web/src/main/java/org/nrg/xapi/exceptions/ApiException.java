/*
 * web: org.nrg.xapi.exceptions.ApiException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

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
