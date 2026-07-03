/*
 * web: org.nrg.xapi.model.Error
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;

@Schema(name = "XAPI Error", description = "Provides the description of an error that occurred within the XAPI functions.")
public class Error {
    private Integer code    = null;
    private String  message = null;
    private String  fields  = null;

    /**
     * The code for the error. The meaning of the code is dependent on the context.
     * @return The error code.
     */
    @Schema(title = "Error Code", description = "The code for the error.")
    @JsonProperty("code")
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * A message associated with the error.
     */
    @Schema(title = "Error Message", description = "A message indicating what the error was.")
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Indicates the fields of the data object that caused the error.
     */
    @Schema(title = "Error Fields", description = "Indicates the fields of the data object that caused the error.")
    @JsonProperty("fields")
    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "class Error {\n" +
               "  code: " + code + "\n" +
               "  message: " + message + "\n" +
               "  fields: " + fields + "\n" +
               "}\n";
    }
}
