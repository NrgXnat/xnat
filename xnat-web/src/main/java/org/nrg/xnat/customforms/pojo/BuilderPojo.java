/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * @author: Mohana Ramaratnam (mohana@radiologics.com)
 * @since: 07-03-2021
 */
package org.nrg.xnat.customforms.pojo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.JsonNode;

/*
 * POJO for the FormIO component JSON
 */

public class BuilderPojo {
    Object builder;

    @JsonRawValue
    public String getBuilder() {
        // default raw value: null or "{}"
        return builder == null ? null : builder.toString();
    }

    public void setBuilder(JsonNode node) {
        this.builder = node;
    }
}
