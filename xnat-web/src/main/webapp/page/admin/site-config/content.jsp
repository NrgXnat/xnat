<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
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

<c:set var="MSG">
    Nope.
</c:set>

<pg:restricted msg="${MSG}">

    <c:import url="/xapi/siteConfig" var="siteConfig"/>
    <c:import url="/xapi/notifications" var="notifications"/>

    <script>
        XNAT.data = extend({}, XNAT.data, {
            siteConfig: ${siteConfig},
            notifications: ${notifications}
        });
        // get rid of the 'targetSource' property
        delete XNAT.data.siteConfig.targetSource;
    </script>

    <div id="page-body">
        <div class="pad" style="padding:20px;">

            <header id="content-header">
                <h2 class="pull-left">XNAT Site Config Output</h2>
                <div class="clear"></div>
            </header>

            <hr class="light">

            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">View Site Config Items</h3>
                </div>
                <div class="panel-body">
                    <div class="panel-element">
                        <label for="select-site-config-item" class="element-label">Select a property</label>
                        <div class="element-wrapper">

                            <select id="select-site-config-item">
                                <%--<option value="!"> &nbsp; </option>--%>
                            </select>

                            <br><br>

                            <textarea id="site-config-item" class="mono" style="width:400px;height:200px;"></textarea>

                            <hr class="light">

                            <button type="button" id="show-site-config-json">Show Site Config JSON</button>

                            <form method="get" action="#!"></form>
                            <div class="description"></div>

                        </div>
                    </div>
                </div>
                <div class="hidden"></div>
            </div>


        </div>
    </div>

    <script>
        (function(){

            var siteConfigObj = XNAT.data.siteConfig;
            var $siteConfigSelect = $('#select-site-config-item');
            var siteConfigItems = '';

            // populate the menu
            forOwn(siteConfigObj, function(prop, val){
                // having trouble with objects right now
                // if (isPlainObject(val)) return;
                siteConfigItems += ('<option value="' + prop + '">' + prop + '</div> \n');
            });

            $siteConfigSelect.append(siteConfigItems);

            // select an item to show in the textarea
            $siteConfigSelect.on('change', function(){
                if (this.value === '!') return false;
                var val = siteConfigObj[this.value];
                if (isPlainObject(val) || Array.isArray(val)) {
                    val = JSON.stringify(val, null, 4);
                }
                $('#site-config-item').text(val);
                // don't need to do a REST call because we already have the object
//                $.get(XNAT.url.restUrl('/xapi/siteConfig/' + this.value), function(data){
//                    $('#site-config-item').val(data)
//                });
            });

            // show the whole siteConfig object in an xmodal dialog
            $('#show-site-config-json').click(function(){
                xmodal.open({
                    size: 'large',
                    minWidth: 800,
                    minHeight: '90%',
                    //width: 600,
                    //height: 500,
                    maximize: true,
                    title: 'Site Config JSON',
                    content: prettifyJSON(siteConfigObj)
                });
            });

            $(function(){
                $siteConfigSelect.selectize();
            })

        })();
    </script>

</pg:restricted>

