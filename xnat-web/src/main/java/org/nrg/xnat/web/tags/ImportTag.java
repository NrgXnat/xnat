/*
 * web: org.nrg.xnat.web.tags.ImportTag
 * XNAT http://www.xnat.org
 *
 * Replacement for JSTL's <c:import url=".." var=".."/> for context-relative URLs. The glassfish
 * jakarta.servlet.jsp.jstl implementations (2.0.0-3.0.1) have a defective capturing response
 * wrapper: its ServletOutputStream.flush() writes the captured bytes to the PAGE output and
 * resets the buffer, so any included servlet that flushes (Spring MVC, Restlet — i.e. every
 * /xapi and /data URL) spills its body into the page and leaves the var empty. This tag does
 * the same include with a wrapper whose flush is a no-op.
 */

package org.nrg.xnat.web.tags;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class ImportTag extends SimpleTagSupport {
    private String url;
    private String var;
    private String scope = "page";

    public void setUrl(final String url) {
        this.url = url;
    }

    public void setVar(final String var) {
        this.var = var;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    @Override
    public void doTag() throws JspException, IOException {
        final PageContext pageContext = (PageContext) getJspContext();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        final CapturingResponseWrapper wrapper = new CapturingResponseWrapper(response);
        try {
            request.getRequestDispatcher(url).include(request, wrapper);
        } catch (ServletException e) {
            throw new JspException("Failed to include " + url, e);
        }
        if (var != null) {
            pageContext.setAttribute(var, wrapper.getCaptured(), toScopeConstant(scope));
        } else {
            // No var: emit inline. Also the replacement for <jsp:include> inside tag-file bodies,
            // where Tomcat 10.1's include writes into a fragment buffer that never reaches the page.
            pageContext.getOut().write(wrapper.getCaptured());
        }
    }

    private static int toScopeConstant(final String scope) {
        switch (scope) {
            case "request":
                return PageContext.REQUEST_SCOPE;
            case "session":
                return PageContext.SESSION_SCOPE;
            case "application":
                return PageContext.APPLICATION_SCOPE;
            default:
                return PageContext.PAGE_SCOPE;
        }
    }

    private static final class CapturingResponseWrapper extends HttpServletResponseWrapper {
        private final StringWriter          writer  = new StringWriter();
        private final PrintWriter           printer = new PrintWriter(writer);
        private final ByteArrayOutputStream bytes   = new ByteArrayOutputStream();
        private boolean                     writerUsed;
        private boolean                     streamUsed;

        private final ServletOutputStream stream = new ServletOutputStream() {
            @Override
            public void write(final int b) {
                bytes.write(b);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(final WriteListener listener) {
            }

            @Override
            public void flush() {
                // no-op: the whole point — never spill the capture into the real response
            }

            @Override
            public void close() {
                // no-op: keep the capture intact
            }
        };

        CapturingResponseWrapper(final HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            writerUsed = true;
            return printer;
        }

        @Override
        public ServletOutputStream getOutputStream() {
            streamUsed = true;
            return stream;
        }

        @Override
        public void flushBuffer() {
            // no-op: the include target must not commit the real response
        }

        String getCaptured() {
            if (writerUsed) {
                printer.flush();
                return writer.toString();
            }
            if (streamUsed) {
                final String encoding = getCharacterEncoding();
                try {
                    return bytes.toString(encoding != null ? encoding : "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return bytes.toString();
                }
            }
            return "";
        }
    }
}
