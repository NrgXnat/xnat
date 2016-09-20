/*
 * org.nrg.dicomtools.filters.SeriesImportFilterMode
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.filters;

import java.util.HashMap;
import java.util.Map;

public enum SeriesImportFilterMode {
    Blacklist("blacklist"),
    Whitelist("whitelist"),
    ModalityMap("modalityMap");

    private final String _value;
    private static final Map<String, SeriesImportFilterMode> _modes = new HashMap<>();

    SeriesImportFilterMode(String value) {
        _value = value;
    }

    public String getValue() {
        return _value;
    }

    public static SeriesImportFilterMode mode(String value) {
        if (_modes.isEmpty()) {
            synchronized (SeriesImportFilterMode.class) {
                for (SeriesImportFilterMode mode : values()) {
                    _modes.put(mode.getValue(), mode);
                }
            }
        }
        return _modes.get(value);
    }

    @Override
    public String toString() {
        return this.name();
    }
}
