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
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@ResponseStatus(HttpStatus.BAD_REQUEST)
@Getter
@Accessors(prefix = "_")
public class DataFormatException extends XapiException {
    public DataFormatException() {
        this("There was an error with the submitted data");
    }

    public DataFormatException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public void addMissingField(final String missing) {
        _missingFields.add(missing);
    }

    @SuppressWarnings("unused")
    public void addUnknownField(final String unknown) {
        _unknownFields.add(unknown);
    }

    public void addInvalidField(final String invalid) {
        _invalidFields.put(invalid, "Invalid " + invalid + " format");
    }

    @SuppressWarnings("unused")
    public void addInvalidField(final String invalid, final String message) {
        _invalidFields.put(invalid, message);
    }

    public boolean hasDataFormatErrors() {
        return !_missingFields.isEmpty() || !_unknownFields.isEmpty() || !_invalidFields.isEmpty();
    }

    public void validateBlankAndRegex(final String property, final String value, final Pattern regex) {
        if (StringUtils.isBlank(value)) {
            addMissingField(property);
        } else if (!regex.matcher(value).matches()) {
            addInvalidField(property);
        }
    }

    @Override
    public String getMessage() {
        final StringBuilder buffer = new StringBuilder(super.getMessage());
        buffer.append("\n");
        if (_missingFields.size() > 0) {
            buffer.append(" * Missing fields: ").append(Joiner.on(", ").join(_missingFields)).append("\n");
        }
        if (_unknownFields.size() > 0) {
            buffer.append(" * Unknown fields: ").append(Joiner.on(", ").join(_unknownFields)).append("\n");
        }
        if (_invalidFields.size() > 0) {
            buffer.append(" * Invalid fields:\n");
            for (final String invalid : _invalidFields.keySet()) {
                buffer.append("    - ").append(invalid).append(": ").append(_invalidFields.get(invalid)).append("\n");
            }
        }
        return buffer.toString();
    }

    private final Set<String>         _missingFields = new HashSet<>();
    private final Set<String>         _unknownFields = new HashSet<>();
    private final Map<String, String> _invalidFields = new HashMap<>();
}
