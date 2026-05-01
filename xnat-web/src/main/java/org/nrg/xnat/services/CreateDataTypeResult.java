/*
 * web: org.nrg.xnat.services.CreateDataTypeResult
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

/**
 * Result object for CreateDataTypeCallable operations
 */
public class CreateDataTypeResult {
    public enum Status {
        SUCCESS,
        BAD_REQUEST,
        FORBIDDEN,
        CONFLICT,
        INTERNAL_ERROR
    }

    private final Status status;
    private final String message;

    private CreateDataTypeResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public static CreateDataTypeResult success(String message) {
        return new CreateDataTypeResult(Status.SUCCESS, message);
    }

    public static CreateDataTypeResult badRequest(String message) {
        return new CreateDataTypeResult(Status.BAD_REQUEST, message);
    }

    public static CreateDataTypeResult forbidden(String message) {
        return new CreateDataTypeResult(Status.FORBIDDEN, message);
    }

    public static CreateDataTypeResult conflict(String message) {
        return new CreateDataTypeResult(Status.CONFLICT, message);
    }

    public static CreateDataTypeResult internalError(String message) {
        return new CreateDataTypeResult(Status.INTERNAL_ERROR, message);
    }
}
