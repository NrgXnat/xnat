/*
 * web: org.nrg.xapi.configuration.ContextPathStrippingPathProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xapi.configuration;

import org.apache.commons.lang3.StringUtils;
import springfox.documentation.spring.web.paths.DefaultPathProvider;

/**
 * Removes the servlet context path that springfox 2.10 prepends to every operation path. The spec's basePath
 * already includes the context path and the XAPI servlet path, so without this a non-ROOT deployment documents
 * each operation as {context}/xapi/{context}/... and Swagger UI requests return 404.
 */
public class ContextPathStrippingPathProvider extends DefaultPathProvider {
    private final String contextPath;

    public ContextPathStrippingPathProvider(final String contextPath) {
        this.contextPath = StringUtils.removeEnd(StringUtils.defaultString(contextPath), "/");
    }

    @Override
    public String getOperationPath(final String operationPath) {
        return super.getOperationPath(stripContextPath(operationPath));
    }

    private String stripContextPath(final String operationPath) {
        if (contextPath.isEmpty()) {
            return operationPath;
        }
        if (operationPath.equals(contextPath)) {
            return "/";
        }
        return operationPath.startsWith(contextPath + "/") ? operationPath.substring(contextPath.length()) : operationPath;
    }
}
