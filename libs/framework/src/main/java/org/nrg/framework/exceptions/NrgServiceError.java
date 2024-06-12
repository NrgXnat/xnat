/*
 * framework: org.nrg.framework.exceptions.NrgServiceError
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.exceptions;

public enum NrgServiceError {
    AlreadyInitialized, 
    ChannelRendererNotFound, 
    ChannelRendererProcessingError,
    ConfigurationError,
    DuplicateCategory,
    DuplicateDefinition,
    DuplicateSubscriber,
    GenericMailError,
    Instantiation,
    InvalidChannelRenderer,
    InvalidDirectParameterizedClassUsage,
    InvalidMailAttachment,
    InvalidRestServiceParameters,
    InvalidScript,
    NoMatchingCategory,
    NoMatchingDefinition,
    NoMatchingRepositoryForService,
    /** attempted access to a resource exceeding caller's permissions */
    PermissionsViolation,
    /** attempted forbidden action **/
    SecurityViolation,
    Uninitialized,
    Unknown,
    UnknownChannelRendererError,
    UnknownEntity,
    UnknownScriptRunner,
    UnsupportedFeature,
    UserNotFoundError,
    UserServiceError;

    public static final NrgServiceError Default = Unknown;
}
