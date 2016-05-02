<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<pg:restricted>

    <div id="page-body">
        <div class="pad">

            <div id="admin-page">
                <header id="content-header">
                    <h2 class="pull-left">Site Administration</h2>
                    <div class="clearfix"></div>
                </header>

                <!-- Admin tab container -->
                <div id="admin-config-tabs" class="content-tabs xnat-tab-container">

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

                </div>

                <script>
                    (function(){

                        function fixSlashes(str){

                            var HTTP = /^http:\/+/i;
                            var HTTPS = /^https:\/+/i;
                            var FTP = /^ftp:\/+/i;

                            // initially remove multiple slashes
                            str = str.replace(/\/+/g, '/');

                            // if there's a protocol specified,
                            // restore '//' to that
                            if (HTTP.test(str)) {
                                str = str.replace(HTTP, 'http://')
                            }
                            else if (HTTPS.test(str)) {
                                str = str.replace(HTTPS, 'https://')
                            }
                            else if (FTP.test(str)) {
                                str = str.replace(FTP, 'ftp://')
                            }

                            return str;

                        }

                        function setUrl(url){
                            return XNAT.url.rootUrl(url);
                            //return fixSlashes(PAGE.siteRoot + url);
                        }

                        function dataUrl(url){
                            return setUrl('/page/admin/data' + url);
                        }

                        var getTabs = $.getJSON({
                            url: dataUrl('/config/site-admin-sample-new.json'),
                            success: function(obj){
                                var adminTabs = XNAT.spawner.spawn(obj);
                                adminTabs.render('#admin-config-tabs', true);
                            }
                        });



                    })();
                </script>

            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <div id="page-footer">
        <div class="pad">
            <div id="mylogger"></div>
        </div>
    </div>
    <!-- /#page-footer -->

    <div id="sticky-footer">
        <div>
            <div class="pad">
                <a class="xnat-version" target="_blank" href="http://www.xnat.org/" style="">XNAT v1.7</a>
                <a id="xnat_power" class="pull-right" target="_blank" href="http://www.xnat.org/" style="">
                    <img src="${sessionScope.siteRoot}/images/xnat_power_small.jpg">
                </a>
            </div>
        </div>
    </div>

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
