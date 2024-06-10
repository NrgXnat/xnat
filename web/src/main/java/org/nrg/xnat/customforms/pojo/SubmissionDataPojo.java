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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/*
 * POJO to represent the SubmissionData
 */

@Getter
@Setter

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionDataPojo {

    private SubmissionPojo data;

    public String validate() {
        return data == null ? "Submission missing required value \"data\"" : data.validate();
    }
}
