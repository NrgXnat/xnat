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
import lombok.Getter;
import lombok.Setter;

/*
 * A POJO to represent the submission from Client
 * The submission is generated as part of the Add New task on the Manage Data Form Page
 *
 */

@Getter
@Setter
public class ClientPojo {

    private SubmissionDataPojo submission;

    private Object builder;

    @JsonRawValue
    public String getBuilder() {
        // default raw value: null or "{}"
        return builder == null ? null : builder.toString();
    }

    public void setBuilder(JsonNode node) {
        this.builder = node;
    }

    /**
     * Validates the submitted data from the UI
     *
     * @return - An error string, if any problems are found. Null value means form is valid.
     */
    public String validate() {
        return submission == null ? "Missing required value \"submission\"" : submission.validate();
    }
}
