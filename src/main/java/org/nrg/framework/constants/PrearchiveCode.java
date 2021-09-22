/*
 * framework: org.nrg.framework.constants.PrearchiveCode
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.constants;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum PrearchiveCode {
    Manual(0),
    AutoArchive(4),
    AutoArchiveOverwrite(5);

    private static final Map<Integer, PrearchiveCode> _codes = new HashMap<>();

    private final int _code;

    PrearchiveCode(final int code) {
        _code = code;
    }

    public int getCode() {
        return _code;
    }

    public static PrearchiveCode code(final String code) {
        return code(Integer.parseInt(code));
    }

    public static PrearchiveCode code(final int code) {
        if (_codes.isEmpty()) {
            synchronized (PrearchiveCode.class) {
                for (PrearchiveCode prearchiveCode : values()) {
                    _codes.put(prearchiveCode.getCode(), prearchiveCode);
                }
            }
        }
        return _codes.get(code);
    }

    public static PrearchiveCode normalize(final String incoming) {
        return StringUtils.isNotBlank(incoming) ? Arrays.stream(values()).filter(value -> incoming.equalsIgnoreCase(value.toString())).findFirst().orElse(null) : null;
    }

    @Override
    public String toString() {
        return name();
    }
}
