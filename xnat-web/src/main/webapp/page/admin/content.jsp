<%@ page contentType="text/html" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: content.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<c:set var="redirect">
    <div class="error">Not authorized. Redirecting...</div>
    <script> window.location.href = '<c:url value="/"/>' </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page" class="settings-tabs">
                <header id="content-header">
                    <div class="pull-right xnat-wrapper">
                        <i class="fa fa-info-circle" title="Find a setting in the Admin UI by its name, XNAT config key, or description"></i>
                        &nbsp;
                        <input id="feature-finder" list="feature-list" type="text" placeholder="Find Setting" tabindex="1" />
                        <datalist id="feature-list">
                        </datalist>
                        <button class="btn btn-sm" id="feature-finder-submit" style="margin-left: 2px;" tabindex="2"><i class="fa fa-search"></i></button>
                    </div>
                    <h2 class="pull-left">Site Administration</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Admin tab container -->
                <div id="admin-config-tabs">

                    <div class="content-tabs xnat-tab-container">

                        <%--
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
                        --%>

                    </div>

                </div>

                <c:import url="/xapi/siteConfig" var="siteConfig"/>
                <c:import url="/xapi/notifications" var="notifications"/>

                <script>
                    (function(){

                        XNAT.data = extend(true, {
                            siteConfig: {},
                            notifications: {}
                        }, XNAT.data||{});

                        var featureDataList = document.getElementById('feature-list'),
                            featureFinder = document.getElementById('feature-finder'),
                            featureFinderSubmit = document.getElementById('feature-finder-submit');

                        <%-- safety check --%>
                        <c:if test="${not empty siteConfig}">
                            XNAT.data.siteConfig = ${siteConfig};
                            // get rid of the 'targetSource' property
                            delete XNAT.data.siteConfig.targetSource;
                            XNAT.data['/xapi/siteConfig'] = XNAT.data.siteConfig;
                            XNAT.data['${SITE_ROOT}/xapi/siteConfig'] = XNAT.data.siteConfig;

                            const configOptions = Object.keys(${siteConfig})
                                .sort(function(a,b){ return a<b ? -1 : 1 });
                            configOptions.forEach(function(configKey){
                                var opt = document.createElement('option');
                                opt.value=configKey;
                                featureDataList.appendChild(opt);
                            })
                        </c:if>

                        <%-- can't use empty/undefined object --%>
                        <c:if test="${not empty notifications}">
                            XNAT.data.notifications = ${notifications};
                            XNAT.data['/xapi/notifications'] = XNAT.data.notifications;
                            XNAT.data['${SITE_ROOT}/xapi/notifications'] = XNAT.data.notifications;
                        </c:if>

                        // these properties MUST be set before spawning 'tabs' widgets
                        XNAT.tabs.container = $('#admin-config-tabs').find('div.content-tabs');
                        XNAT.tabs.layout = 'left';

                        var gotTabs = false;
                        var spawnerIds = ['root', 'adminPage'];

                        function findAdminTabs(idIndex){
                            if (gotTabs || idIndex >= spawnerIds.length) return;
                            var siteAdminSettingsNamespace = XNAT.data.siteConfig.siteAdminSettingsNamespace || 'siteAdmin';
                            var spawnerNS = siteAdminSettingsNamespace + '/' + spawnerIds[idIndex];
                            XNAT.spawner
                                .resolve(spawnerNS)
                                .ok(function(){
                                    gotTabs = true;
                                    this.render(XNAT.tabs.container, 200, function(){
                                        //initInfoLinks();
                                    });
                                    this.done(function(){
                                        XNAT.tab.activate(XNAT.tab.active, XNAT.tabs.container);
                                    })
                                })
                                .fail(function(){
                                    findAdminTabs(idIndex += 1)
                                })
                        }

                        findAdminTabs(0);

                        function searchAdminTabs(configKey){
                            if (!configKey.length) return false;
                            var match = $(document).find('div[data-name='+configKey+']');

                            if (!match) {
                                // if the user fat-fingers an entry
                                XNAT.ui.dialog.message('Sorry, the <b>'+configKey+'</b> setting could not be found. PLease double-check your entry.');
                                return false;
                            }

                            var matchingTab = match.parents('div.tab-pane').data('tab');
                            console.log(configKey,matchingTab);

                            if (!matchingTab) {
                                // not every config setting has a UI element
                                XNAT.ui.dialog.message('Sorry, the <b>'+configKey+'</b> setting does not have a UI element. This can be set via Swagger.');
                                return false;
                            }

                            XNAT.ui.banner.top(2000,'Found ' + configKey + ' in the '+matchingTab+' tab.','info');
                            XNAT.tab.activate(matchingTab);
                            $(document).scrollTop(match.position()['top'] - 50);

                            match.find('.element-label:first-child').css('background-color','#e4efff');
                        }

                        featureFinder.onkeyup = function(event){
                            event.stopPropagation();
                            
                            if (event.keyCode === 13) {
                                searchAdminTabs(featureFinder.value);
                            }
                            if (event.keyCode === 27) {
                                featureFinder.value = '';
                            }
                        };
                        featureFinderSubmit.onmouseup = function(event){
                            event.preventDefault();
                            var selectedVal = document.getElementById('feature-finder').value;
                            if (selectedVal.length > 0) searchAdminTabs(selectedVal);
                        };


                    })();

                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="xnat-scripts">
        <script>
            //        $(window).load(function(){
            //            // any values that start with '@|' will be set to
            //            // the value of the element with matching selector
            //            $('[value^="@|"]').each(function(){
            //                var selector = $(this).val().split('@|')[1];
            //                var value = $$(selector).val();
            //                $(this).val(value).dataAttr('value',value);
            //            });
            //        });
        </script>
    </div>

</pg:restricted>

