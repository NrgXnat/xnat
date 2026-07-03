/*
 * web: swagger-ui-overrides/swagger-initializer.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Overrides the swagger-ui webjar's default initializer (which points at the petstore demo). Served in place of
 * the webjar copy via the resource handler in WebConfig#addResourceHandlers. The spec URL is derived from the
 * current location so it works behind any servlet mapping prefix (/xapi, /admin, ...) and context path.
 */
window.onload = function() {
    const specUrl = window.location.pathname.replace(/\/swagger-ui\/.*$/, "/v3/api-docs");
    window.ui = SwaggerUIBundle({
        url: specUrl,
        dom_id: "#swagger-ui",
        deepLinking: true,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout",
        validatorUrl: ""
    });
};
