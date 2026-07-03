/*
 * web: org.nrg.xnat.web.filter.ApiIncludeContentTypeIsolationFilter
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * Isolates the Content-Type header during include dispatches to API endpoints.
 * <p>
 * Starting with Tomcat 9.0.116 (bug 69967), {@code response.getContentType()} and
 * {@code response.getHeader("Content-Type")} return consistent values during
 * {@link RequestDispatcher#include} calls. This means that when a JSP page (with
 * {@code contentType="text/html"}) uses {@code <c:import url="/xapi/...">}, the
 * included Spring REST controller now sees the outer response's {@code text/html}
 * content type as a "preset" value. Spring MVC then fails to find a message converter
 * for Java objects (ArrayList, HashMap) with {@code text/html}, throwing
 * {@link org.springframework.http.converter.HttpMessageNotWritableException}.
 * <p>
 * This filter wraps the response during include dispatches so that the included
 * controller does not inherit the outer response's Content-Type, allowing Spring's
 * normal content negotiation to select {@code application/json} based on the
 * controller's {@code produces} attribute.
 * <p>
 * This filter must be registered with {@link DispatcherType#INCLUDE} only.
 */
public class ApiIncludeContentTypeIsolationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            chain.doFilter(request, new ContentTypeIsolatingResponseWrapper((HttpServletResponse) response));
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }

    /**
     * Response wrapper that maintains its own Content-Type state, preventing the
     * outer response's Content-Type from leaking into the included request's
     * content negotiation.
     */
    private static class ContentTypeIsolatingResponseWrapper extends HttpServletResponseWrapper {
        private String contentType;

        ContentTypeIsolatingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setContentType(String type) {
            this.contentType = type;
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public void setHeader(String name, String value) {
            if ("Content-Type".equalsIgnoreCase(name)) {
                this.contentType = value;
            } else {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if ("Content-Type".equalsIgnoreCase(name)) {
                this.contentType = value;
            } else {
                super.addHeader(name, value);
            }
        }

        @Override
        public String getHeader(String name) {
            if ("Content-Type".equalsIgnoreCase(name)) {
                return this.contentType;
            }
            return super.getHeader(name);
        }
    }
}
