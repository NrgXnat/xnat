/*
 * web: org.nrg.xapi.model.Error
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.model;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@ApiModel(value = "XAPI Error", description = "Provides the description of an error that occurred within the XAPI functions.")
public class Error {
    private Integer code    = null;
    private String  message = null;
    private String  fields  = null;

    /**
     * The code for the error. The meaning of the code is dependent on the context.
     * @return The error code.
     */
    @ApiModelProperty(name = "Error Code", value = "The code for the error.", dataType = "java.lang.Integer", notes = "The meaning of this error code is dependent on the function where the error occurred.")
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
    @ApiModelProperty(name = "Error Message", value = "A message indicating what the error was.", dataType = "java.lang.String")
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
    @ApiModelProperty(name = "Error Fields", value = "Indicates the fields of the data object that caused the error.", dataType = "java.lang.String")
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
