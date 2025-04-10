<%@ page contentType="application/json" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--@elvariable id="hibernateSpawnerService" type="org.nrg.xnat.spawner.services.SpawnerService"--%>

    <c:set var="loggedInUser" value="${sessionScope.userHelper.user.username}"/>
    <c:import url="/xapi/users/${loggedInUser}/roles" var="loggedInUserRoles"/>

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
                    XNAT.app.pluginSettings.restrictedSiteTabConfigs = [];
                    function returnValue(value){ return value }
                    var isPermittedToView = false;
                </script>

                   <c:catch var="jspError">


                    <%-- don't worry about getting the list of plugins...
                         ...any Spawner namespace with :siteSettings will get processed --%>

                    <c:forEach items="${hibernateSpawnerService.namespaces}" var="namespace">
                        <%-- only get 'siteSettings' items --%>
                        <c:if test="${fn:endsWith(namespace, 'siteSettings')}">
                             <c:catch var="jspResponseError1">
                                <c:import url="/xapi/spawner/resolve/${namespace}/siteSettings" var="pluginTabsConfig"/>
                            </c:catch>
                            <c:if test="${not empty jspError1 && empty pluginTabsConfig}">
                                <%-- originally 'siteSettings' was the expected name of
                                     the root element, but now 'root' is preferred --%>
                                <c:catch var="jspResponseError2">
                                <c:import url="/xapi/spawner/resolve/${namespace}/root" var="pluginTabsConfig"/>
                                </c:catch>
                            </c:if>
                            <c:if test="${empty jspError1 && empty jspError2}">
                                <script>
                                    (function(){
                                        var config = returnValue(${pluginTabsConfig});
                                        var userRoles = returnValue(${loggedInUserRoles});
                                        if (!config) return;
                                        if (config.hasOwnProperty('siteSettings')) {
                                           if (config['siteSettings'].hasOwnProperty('meta') && config['siteSettings']['meta'].hasOwnProperty('restricted')) {
                                             var restrictedTo = config['siteSettings']['meta']['restricted'];
                                             var restrictedToArray = restrictedTo.split(",");
                                             if (userRoles.some(role => restrictedToArray.includes(role))) {
                                                isPermittedToView = true;
                                                XNAT.app.pluginSettings.siteTabConfigs.push(config['siteSettings']);
                                             } else {
                                                if (userRoles.includes('Administrator')) {
                                                  isPermittedToView = true;
                                                }
                                                XNAT.app.pluginSettings.restrictedSiteTabConfigs.push(config['siteSettings']);
                                             }
                                           } else if (userRoles.includes('Administrator')){
                                              isPermittedToView = true;
                                              XNAT.app.pluginSettings.siteTabConfigs.push(config['siteSettings']);
                                           }
                                        } else if (config.hasOwnProperty('root')) {
                                            XNAT.app.pluginSettings.siteTabConfigs.push(config['root'])
                                        }
                                    })();
                                </script>
                            </c:if>
                        </c:if>
                    </c:forEach>

                </c:catch>

                <c:if test="${not empty jspError || not empty jspError1 || not empty jspError2}">
                    <script>
                        console.error('JSP error:');
                        console.error('${jspError} ${jspError1} ${jspError2}');
                    </script>
                </c:if>

                <script>
                    (function(){
                        if (!isPermittedToView) {
                            window.location.href = '<c:url value="/"/>'
                        }
                        var siteSettingsTabs = {};
                        // alias for brevity
                        var tabConfigs = XNAT.app.pluginSettings.siteTabConfigs;
                        var restrictedTabConfigs = XNAT.app.pluginSettings.restrictedSiteTabConfigs;
                        if (tabConfigs.length != 0) {
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
                        }
                        if (restrictedTabConfigs.length != 0) {
                            if (isPermittedToView) {
                              $('#view-plugin-settings').show().hidden(false);
                                  forEach(restrictedTabConfigs, function(restrictedTabConfig){
                                      var restrictedTabConfigModified = "{" +
                                                                             "\"kind\": \"tabs\"," +
                                                                             "\"groups\": {" +
                                                                               "\"restrictedTabs\": \"" + restrictedTabConfig['label'] + "\"" +
                                                                             "}," +
                                                                             "\"contents\": {" +
                                                                               "\"restrictedTab\": {" +
                                                                                 "\"kind\": \"tab\"," +
                                                                                 "\"group\": \"restrictedTabs\"," +
                                                                                 "\"label\": \"Restricted Access\"," +
                                                                                 "\"contents\": { " +
                                                                                   "\"accessSettingInfo\": {" +
                                                                                     "\"kind\": \"panel\"," +
                                                                                     "\"name\": \"accessSettingInfo\"," +
                                                                                     "\"label\": \"Restricted Access\"," +
                                                                                     "\"contents\": {" +
                                                                                       "\"info\": {" +
                                                                                         "\"tag\": \"p\"," +
                                                                                         "\"element\": {" +
                                                                                           "\"style\": \"margin: 15px 0 30px\"" +
                                                                                         "}," +
                                                                                         "\"contents\": \"Access is restricted to role(s):  <b>"  + restrictedTabConfig['meta']['restricted'] + "</b> <br><br> <a href='" + XNAT.app.siteRoot + "app/template/Page.vm?view=admin/users'>Go to User Administration</a> to grant this role to your user profile to administer this feature.\""+
                                                                                       "}" +
                                                                                     "}" +
                                                                                   "}" +
                                                                                 "}" +
                                                                               "}" +
                                                                             "}" +
                                                                         "}";
                                      extend(true, siteSettingsTabs, JSON.parse(restrictedTabConfigModified));
                                  });
                            }
                        }
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
                    })();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts"></div>

