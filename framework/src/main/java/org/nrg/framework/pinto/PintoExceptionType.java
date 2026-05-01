/*
 * framework: org.nrg.framework.pinto.PintoExceptionType
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import java.util.HashMap;
import java.util.Map;

public enum PintoExceptionType {
    UnknownParameter(0),
    SyntaxFormat(1),
    UnsupportedFeature(2),
    DuplicateParameter(3),
    Configuration(4),
    UnknownParameterTypes(5),
    InvalidOutputStreamAdapter(6);

    PintoExceptionType(int code) {
        _code = code;
    }

    public int getCode() {
        return _code;
    }

    public static PintoExceptionType code(String code) {
        return code(Integer.parseInt(code));
    }

    public static PintoExceptionType code(int code) {
        if (_codes.isEmpty()) {
            synchronized (PintoExceptionType.class) {
                for (PintoExceptionType typeCode : values()) {
                    _codes.put(typeCode.getCode(), typeCode);
                }
            }
        }
        return _codes.get(code);
    }

    @Override
    public String toString() {
        return this.name();
    }

    private static final Map<Integer, PintoExceptionType> _codes = new HashMap<Integer, PintoExceptionType>();
    private final int _code;
}
