/*
 * spawner: org.nrg.xnat.spawner.validation.Validated
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.spawner.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "_")
@AllArgsConstructor
public class Validated {
    public static final Validated SUCCESS = new Validated();

    private Validated() {
        _valid = true;
        _message = "";
    }

    private final boolean _valid;
    private final String  _message;
}
