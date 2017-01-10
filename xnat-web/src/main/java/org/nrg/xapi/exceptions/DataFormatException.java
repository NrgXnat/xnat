/*
 * web: org.nrg.xapi.exceptions.DataFormatException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.exceptions;

import com.google.common.base.Joiner;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataFormatException extends ApiException {
    private final List<String>        _missing = new ArrayList<>();
    private final List<String>        _unknown = new ArrayList<>();
    private final Map<String, String> _invalid = new HashMap<>();

    public DataFormatException() {
        this("There was an error with the submitted data");
    }

    public DataFormatException(final String message) {
        super(HttpStatus.BAD_REQUEST.value(), message);
    }

    public List<String> getMissingFields() {
        return _missing;
    }

    public void setMissing(final List<String> missing) {
        _missing.clear();
        _missing.addAll(missing);
    }

    public void addMissing(final String missing) {
        _missing.add(missing);
    }

    public List<String> getUnknownFields() {
        return _unknown;
    }

    public void setUnknown(final List<String> unknown) {
        _unknown.clear();
        _unknown.addAll(unknown);
    }

    public void addUnknown(final String unknown) {
        _unknown.add(unknown);
    }

    public Map<String, String> getInvalidFields() {
        return _invalid;
    }

    public void setInvalid(final Map<String, String> invalid) {
        _invalid.clear();
        _invalid.putAll(invalid);
    }

    public void addInvalid(final String invalid) {
        _invalid.put(invalid, "Invalid " + invalid + " format");
    }

    public void addInvalid(final String invalid, final String message) {
        _invalid.put(invalid, message);
    }

    public boolean hasDataFormatErrors() {
        return !_missing.isEmpty() && !_unknown.isEmpty() && !_invalid.isEmpty();
    }

    @Override
    public String getMessage() {
        final StringBuilder buffer = new StringBuilder(super.getMessage());
        buffer.append("\n");
        if (_missing.size() > 0) {
            buffer.append(" * Missing fields: ").append(Joiner.on(", ").join(_missing)).append("\n");
        }
        if (_unknown.size() > 0) {
            buffer.append(" * Unknown fields: ").append(Joiner.on(", ").join(_unknown)).append("\n");
        }
        if (_invalid.size() > 0) {
            buffer.append(" * Invalid fields:\n");
            for (final String invalid : _invalid.keySet()) {
                buffer.append("    - ").append(invalid).append(": ").append(_invalid.get(invalid)).append("\n");
            }
        }
        return buffer.toString();
    }
}
