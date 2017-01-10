<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<%--
  ~ web: index.jsp
  ~ XNAT http://www.xnat.org
  ~ Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
  ~ All Rights Reserved
  ~
  ~ Released under the Simplified BSD.
  --%>

<pg:wrapper>
    <pg:xnat page="setup" title="XNAT Setup">

        <div id="page-body">
            <div class="pad">

                <div id="setup-page">

                    <c:set var="message">
                        <header id="content-header">
                            <h2>XNAT Site Setup</h2>
                            <div class="message">
                                This XNAT system has not yet been configured for use.
                                Please contact your site administrator to have the system set up.
                            </div>
                        </header>
                    </c:set>

                    <pg:restricted msg="${message}">

                        <c:import url="/xapi/siteConfig" var="siteConfig"/>
                        <c:import url="/xapi/notifications" var="notifications"/>

                        <script>
                            var XNAT = getObject(XNAT);
                            XNAT.data = extend({}, XNAT.data, {
                                siteConfig: ${siteConfig},
                                notifications: ${notifications}
                            });
                            XNAT.data['/xapi/siteConfig'] = XNAT.data.siteConfig;
                            XNAT.data['/xapi/notifications'] = XNAT.data.notifications;
                            // get rid of the 'targetSource' property
                            delete XNAT.data.siteConfig.targetSource;
                        </script>

                        <header id="content-header">
                            <h2 class="pull-left">XNAT Site Setup</h2>
                            <div class="hidden message pull-left">
                                The settings below need to be configured before this XNAT system
                                can be used. Please set the properties below and submit the form to continue.
                            </div>
                            <div class="clearfix"></div>
                        </header>

                        <!-- Setup tab container -->
                        <div id="site-setup-panels">


                            <!-- ======================== -->
                            <!-- PANELS WILL SHOW UP HERE -->
                            <!-- ======================== -->


                        </div>
                        <!-- /#site-setup-panels -->

                        <script src="<c:url value="/scripts/xnat/app/siteSetup.js"/>"></script>

                        <script>

                            //XNAT.app.setupComplete = function(){
                            //    XNAT.xhr.form('#site-setup', {});
                            //};

                            XNAT.xhr.get({
                                url:     XNAT.url.rootUrl('/setup/site-setup.yaml'),
                                //url: XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/siteSetup'),
                                success: function (data) {
                                    if (typeof data === 'string') {
                                        data = YAML.parse(data);
                                    }

                                    console.log(data);

                                    XNAT.spawner
                                        .spawn(data)
                                        .render('#site-setup-panels');
                                }
                            });

                        </script>

                    </pg:restricted>

                </div>
                <!-- /#setup-page -->

            </div>
        </div>
        <!-- /#page-body -->

    </pg:xnat>
</pg:wrapper>
