/*
 * web: org.nrg.xnat.helpers.resource.XnatResourceInfo
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.resource;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.services.archive.CatalogService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Provides a package for passing resources to methods on the {@link CatalogService} that insert resources into a
 * catalog. This descriptor includes a file or resource stream and optional path, content, and format specifiers.
 */
@Data
@Builder
public class XnatResourceInfo implements Serializable {
    public static class XnatResourceInfoBuilder {
        public XnatResourceInfo.XnatResourceInfoBuilder file(final File file) {
            if (StringUtils.isBlank(name)) {
                name(file.getName());
            }
            source(new FileSystemResource(file));
            return this;
        }
    }

    private final String              name;
    private final String              description;
    private final String              format;
    private final String              content;
    private final Number              eventId;
    private final Date                lastModified;
    private final Date                created;
    private final String              username;
    private final InputStreamSource   source;
    @Singular
    private final List<String>        tags;
    @Singular("data")
    private final Map<String, String> metadata;
}
