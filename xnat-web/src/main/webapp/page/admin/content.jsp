<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:if test="${empty hasInit}">
    <pg:init/>
</c:if>

<script src="${themeRoot}/scripts/xnat/ui/table.js"></script>
<script src="${themeRoot}/scripts/xnat/ui/panel.js"></script>
<script src="${themeRoot}/scripts/xnat/ui/tabs.js"></script>

        <div id="page-header">
            <div class="pad">
                <a href="${siteRoot}/"><img src="${siteRoot}/images/logo.png" style="vertical-align:baseline;"></a>
                <span style="position:relative;bottom:4px;left:20px;">
                    XNAT currently contains
                    <a href="#">16 projects</a>,
                    <a href="#">47 subjects</a>, and
                    <a href="#">15 imaging sessions</a>.
                </span>
            </div>
        </div><!-- /#page-header -->
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
                        (function () {

                            function fixSlashes(str){

                                var HTTP  = /^http:\/+/;
                                var HTTPS = /^https:\/+/;
                                var FTP   = /^ftp:\/+/;

                                // initially remove multiple slashes
                                str = str.replace(/\/+/g, '/');

                                // if there's a protocol specified,
                                // restore '//' to that
                                if (HTTP.test(str)){
                                    str = str.replace(HTTP, 'http://')
                                }
                                else if (HTTPS.test(str)){
                                    str = str.replace(HTTPS, 'https://')
                                }
                                else if (FTP.test(str)){
                                    str = str.replace(FTP, 'ftp://')
                                }

                                return str;

                            }
                            function setUrl(url){
                                return fixSlashes(PAGE.siteRoot + url);
                            }
                            function dataUrl(url){
                                return setUrl('/page/admin/data/' + url);
                            }


                            loadjs(setUrl('/scripts/lib/yamljs/dist/yaml.js'), function(){

                                // get the JSON and do the setup
                                $.getJSON(dataUrl('/config/siteAdmin.json')).done(function (data) {

                                    var adminTabs =
                                            XNAT.ui.tabs
                                                    .init(data.XNAT)
                                                    .render('#admin-config-tabs');

                                    console.log(adminTabs);
                                });

//                                    $.get({
//                                        url: dataUrl('config/siteAdminAlt.yaml'),
//                                        dataType: 'text',
//                                        success: function (data) {
//                                            var parsed = YAML.parse(data);
//                                            console.log(parsed);
//                                            extend(true, parsed.XNAT.siteAdmin.xnatSetup.siteSetup.siteInfo, {
//                                                _self: {
//                                                    foo: 'bar',
//                                                    fn: function () {
//                                                        alert('foo')
//                                                    }
//                                                },
//                                                anotherElement: {
//                                                    _self: {
//                                                        type: 'thing',
//                                                        bar: 'foo'
//                                                    }
//                                                }
//                                            });
//                                            console.log(parsed.XNAT.siteAdmin.xnatSetup.siteSetup.siteInfo._self);
//
//                                            parsed.XNAT.siteAdmin.xnatSetup.siteSetup.siteInfo._self.fn();
//
//                                        }
//                                    });

                            });

                        })();
                    </script>

                </div>

            </div>
        </div><!-- /#page-body -->

        <div id="page-footer">
            <div class="pad">
                <div id="mylogger"></div>
            </div>
        </div><!-- /#page-footer -->

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
