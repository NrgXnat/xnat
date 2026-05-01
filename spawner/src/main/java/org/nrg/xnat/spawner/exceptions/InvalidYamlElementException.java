/*
 * spawner: org.nrg.xnat.spawner.exceptions.InvalidYamlElementException
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.exceptions;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "An invalid YAML element was submitted.")
@Getter
@Accessors(prefix = "_")
public class InvalidYamlElementException extends Exception {
    public InvalidYamlElementException(final String invalidElement) {
        super("An invalid YAML element was submitted: " + invalidElement);
        _invalidElement = invalidElement;
    }

    private final String _invalidElement;
}
