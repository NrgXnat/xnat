<%@ tag description="Head Element" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%@ attribute name="title" %>
<%@ attribute name="headTop" %>
<%@ attribute name="headBottom" %>

<%--
  ~ web: head.tag
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<head>

    ${headTop}

    <title>${title}</title>

        <c:if test="${empty requestScope.hasInit}">
            <pg:init>
                <c:if test="${empty requestScope.hasVars}">
                    <pg:jsvars/>
                </c:if>
            </pg:init>
        </c:if>

    <c:set var="rootUrl" value="${sessionScope.themeRoot}"/>

    <%--<link type="text/css" rel="stylesheet" href="${rootUrl}/scripts/lib/bootstrap/themes/xnat/bootstrap.css">--%>
    <link type="text/css" rel="stylesheet" href="${rootUrl}/style/app.css">

    <%--<script src="<c:url value="/scripts/polyfills.js"/>"></script>--%>
    <%--<script src="<c:url value="/scripts/globals.js"/>"></script>--%>
    <script src="${rootUrl}/scripts/polyfills.js"></script>
    <script src="${rootUrl}/scripts/globals.js"></script>

    <script type="text/javascript">

        var serverRoot = '';
        var siteRoot = serverRoot;
        var rootURL = serverRoot;
        var jsDir = serverRoot + '/scripts';
        var jsLib = jsDir + '/lib';

        //        var themeRoot = serverRoot + '/themes/admin';

        var themeRoot = '${rootUrl}';

        window.jsdebug = (function () {
            return getQueryStringValue('jsdebug') === 'true' ||
                    /(jsdebug|debug)/i.test(window.location.hash);
        })();

        // if ?jsdebug=true or #jsdebug,
        // don't add 'min' to script name
        function setMin(min) {
            return window.jsdebug ? '' : min;
        }

        // keep this simple
        function insertScripts(scripts) {
            scripts.forEach(function (script) {
                if (!script) return;
                document.write('<script type="text/javascript" src="' + script + '"><\/script>');
            })
        }

        // load minified script unless url contains
        // ?jsdebug=true or #jsdebug
        insertScripts([
            jsLib + '/jquery/jquery' + setMin('.min') + '.js',
            jsLib + '/bootstrap/js/bootstrap' + setMin('.min') + '.js',
            jsLib + '/loadjs/loadjs' + setMin('.min') + '.js',
//            jsLib + '/yaml.js',
//            jsLib + '/js-yaml/dist/js-yaml' + setMin('.min') + '.js',
//            jsLib + '/head/head' + setMin('.min') + '.js',
            jsDir + '/utils.js',
            ''
        ]);

    </script>
    <script id="insert-scripts-before"></script>
    <!-- ^^ scripts inserted above ^^ -->
    <script src="<c:url value="/scripts/lib/yamljs/dist/yaml.js"/>"></script>
    <script type="text/javascript">

        var XNAT = {
            load: loadjs,
            ready: loadjs.ready
        };

        // use 'jq' for jQuery functions in .vm templates
        // to avoid _possible_ conflicts with Velocity
        var jq = jQuery;

    </script>

    <!-- Other third-party scripts/libraries -->
    <script>
        insertScripts([
            jsLib + '/js.cookie.js',
            jsLib + '/spawn/spawn.js',
            jsLib + '/jquery-plugins/jquery.spawn.js',
            jsLib + '/jquery-plugins/jquery.dataAttr.js',
            jsDir + '/xmodal-v1/xmodal.js',
            null
        ]);
    </script>

    <!-- XNAT lib scripts -->
    <script>
        insertScripts([
            jsDir + '/xnat/misc/filler.js',
            jsDir + '/xnat/url.js',
            jsDir + '/xnat/xhr.js',
            jsDir + '/xnat/event.js',
            jsDir + '/xnat/element.js',
            jsDir + '/xnat/ui/table.js',
            jsDir + '/xnat/ui/panel.js',
            jsDir + '/xnat/ui/tabs.js',
            false
        ]);
    </script>

    <!-- sessionScope.themeName: ${sessionScope.themeName} -->
    <!-- sessionScope.siteRoot:  <c:url value="/"/> -->
    <!-- sessionScope.csrfToken: ${sessionScope.csrfToken} -->

    <jsp:doBody/>

    ${headBottom}

</head>
