<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--@elvariable id="hibernateSpawnerService" type="org.nrg.xnat.spawner.services.SpawnerService"--%>

<c:set var="redirect">
    <div class="error">Not authorized. Redirecting...</div>
    <script> window.location.href = '<c:url value="/"/>' </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div id="page-body">
        <div class="pad">

            <div id="plugin-admin-page" class="settings-tabs">
                <header id="content-header">
                    <h2 class="pull-left">Plugin Settings</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Plugin Settings tab container -->
                <div id="plugin-settings-tabs">

                    <div class="content-tabs xnat-tab-container">

                        <div id="tabs-loading" class="message waiting">Loading...</div>

                            <%--&lt;%&ndash;--%>
                            <div class="xnat-nav-tabs side pull-left">
                                <!-- ================== -->
                                <!-- Admin tab flippers -->
                                <!-- ================== -->
                            </div>
                            <div class="xnat-tab-content side pull-right">
                                <!-- ================== -->
                                <!-- Admin tab panes    -->
                                <!-- ================== -->
                            </div>
                            <%--&ndash;%&gt;--%>

                    </div>

                </div>

                <script>
                    XNAT.app = getObject(XNAT.app || {});
                    XNAT.app.pluginSettings = getObject(XNAT.app.pluginSettings || {});
                    XNAT.app.pluginSettings.siteTabConfigs = [];
                    function returnValue(value){ return value }
                </script>

                <c:catch var="jspError">

                    <%-- don't worry about getting the list of plugins...
                         ...any Spawner namespace with :siteSettings will get processed --%>

                    <c:forEach items="${hibernateSpawnerService.namespaces}" var="namespace">
                        <%-- only get 'siteSettings' items --%>
                        <script>console.log('namespace: ${namespace}')</script>
                        <c:if test="${fn:endsWith(namespace, 'siteSettings')}">
                            <c:import url="/xapi/spawner/resolve/${namespace}/siteSettings" var="pluginTabsConfig"/>
                            <c:if test="${empty pluginTabsConfig}">
                                <%-- originally 'siteSettings' was the expected name of
                                     the root element, but now 'root' is preferred --%>
                                <script>console.log('(no "siteSettings" property; using "root")')</script>
                                <c:import url="/xapi/spawner/resolve/${namespace}/root" var="pluginTabsConfig"/>
                            </c:if>
                            <script>
                                (function(){
                                    var config = returnValue(${pluginTabsConfig});
                                    if (!config) return;
                                    if (config.hasOwnProperty('siteSettings')) {
                                        XNAT.app.pluginSettings.siteTabConfigs.push(config['siteSettings'])
                                    }
                                    else if (config.hasOwnProperty('root')) {
                                        XNAT.app.pluginSettings.siteTabConfigs.push(config['root'])
                                    }
                                })();
                            </script>
                        </c:if>
                    </c:forEach>

                </c:catch>

                <c:if test="${not empty jspError}">
                    <script>
                        console.error('JSP error:');
                        console.error('${jspError}');
                    </script>
                </c:if>

                <script>
                    (function(){
                        var siteSettingsTabs = {};
                        // alias for brevity
                        var tabConfigs = XNAT.app.pluginSettings.siteTabConfigs;
                        if (tabConfigs.length) {
                            // show the 'Plugin Settings' item in the 'Administer' menu
                            $('#view-plugin-settings').show().hidden(false);
                            forEach(tabConfigs, function(tabConfig){
                                // resolve top-level 'contains'/'contents' properties
                                if (tabConfig.hasOwnProperty('contains')) {
                                    tabConfig.contents = tabConfig[tabConfig.contains];
                                    delete tabConfig[tabConfig.contains];
                                    delete tabConfig.contains;
                                }
                                // resolve 'meta.tabGroups'/'tabGroups'/'groups' properties
                                if (tabConfig.meta && tabConfig.meta.tabGroups) {
                                    tabConfig.groups = tabConfig.meta.tabGroups;
                                    delete tabConfig.meta.tabGroups;
                                }
                                else if (tabConfig.tabGroups) {
                                    tabConfig.groups = tabConfig.tabGroups;
                                    delete tabConfig.tabGroups;
                                }
                                // merge all configs into a single config object
                                extend(true, siteSettingsTabs, tabConfig);
                            });
                            // make sure these properties are set correctly
                            siteSettingsTabs.kind = 'tabs';
                            siteSettingsTabs.name = 'siteSettingsTabs';
                            // render the tabs
                            XNAT.spawner
                                .spawn({ siteSettingsTabs: siteSettingsTabs })
                                .render($('#plugin-settings-tabs').find('.xnat-tab-container'))
                                .done(function(spawned){
                                    var selectTab = getUrlHashValue('tab=');
                                    var tabSelector = selectTab ? 'ul.nav > li[data-tab="' + selectTab + '"]' : 'ul.nav > li[data-tab]';
                                    // XNAT.ui.tab.activate(getUrlHashValue('tab='), spawned);
                                    waitForElement(1, tabSelector, function(){
                                        $('#tabs-loading').remove();
                                        $(tabSelector).first().trigger('click');
                                    });
                                })
                        }
                    })();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

</pg:restricted>
