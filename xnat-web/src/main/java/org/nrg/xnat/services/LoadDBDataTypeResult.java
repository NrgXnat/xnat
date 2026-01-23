/*
 * web: org.nrg.xnat.services.LoadDBDataTypeResult
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

/**
 * Result object for LoadDBDataTypeCallable operations
 */
public class LoadDBDataTypeResult {
    public enum Status {
        SUCCESS,
        NOT_FOUND,
        INTERNAL_ERROR
    }

    private final Status status;
    private final String message;

    private LoadDBDataTypeResult(Status status, String message) {
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

    public static LoadDBDataTypeResult success(String message) {
        return new LoadDBDataTypeResult(Status.SUCCESS, message);
    }

    public static LoadDBDataTypeResult notFound(String message) {
        return new LoadDBDataTypeResult(Status.NOT_FOUND, message);
    }

    public static LoadDBDataTypeResult internalError(String message) {
        return new LoadDBDataTypeResult(Status.INTERNAL_ERROR, message);
    }
}
