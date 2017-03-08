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

<c:set var="redirect">
    <div class="error">Not authorized. Redirecting...</div>
    <script> window.location.href = '<c:url value="/"/>' </script>
</c:set>

<pg:restricted msg="${redirect}">

    <c:set var="SITE_ROOT" value="${sessionScope.siteRoot}"/>

    <c:import url="/xapi/users/profiles" var="userProfiles"/>
    <c:import url="/xapi/users/active" var="activeUsers"/>
    <c:import url="/xapi/siteConfig" var="siteConfig"/>

    <script>
        (function(){

            XNAT.data =
                getObject(XNAT.data);

            XNAT.xapi =
                    getObject(XNAT.xapi);

            XNAT.xapi.users =
                    getObject(XNAT.xapi.users);

            <c:if test="${not empty userProfiles}">
            XNAT.xapi.users.profiles = ${userProfiles};
            XNAT.data['/xapi/users/profiles'] = XNAT.xapi.users.profiles;
            </c:if>

            <c:if test="${not empty activeUsers}">
            XNAT.xapi.users.active = ${activeUsers};
            XNAT.data['/xapi/users/active'] = XNAT.xapi.users.active;
            </c:if>

            XNAT.data = extend(true, XNAT.data || {}, {
                userProfiles: XNAT.xapi.users.profiles,
                activeUsers: XNAT.xapi.users.active
            });

            <c:if test="${not empty siteConfig}">
            XNAT.data.siteConfig = ${siteConfig};
            XNAT.data['/xapi/siteConfig'] = XNAT.data.siteConfig;
            </c:if>

            // these properties MUST be set before spawning 'tabs' widgets
            //XNAT.tabs.container = $('#users-groups-tabs').find('div.content-tabs');
            XNAT.tabs.contaner = null;
            //XNAT.tabs.layout = 'top';

        })();
    </script>

    <script src="${SITE_ROOT}/scripts/xnat/admin/usersGroups.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/misc/filler.js"></script>
    <script src="${SITE_ROOT}/scripts/xnat/misc/namer.js"></script>
    <%--<script src="${SITE_ROOT}/scripts/xnat/admin/usersGroupsGenerator.js"></script>--%>

    <div id="page-body" style="width:600px;margin:20px auto;">
        <div class="pad">

            <div id="generate-users" class="pad10v">
                <!-- input #email-user -->
                <!-- input #user-count -->
                <!-- button #generate -->
                <!-- button #do-upload -->
            </div>

                <%--<div id="upload-users" class="pad10v"></div>--%>

            <div id="users-output" class="pad10v">
                <!-- textarea -->
            </div>

        </div>
    </div>
    <!-- /#page-body -->

    <script>
        (function(){

            var generateUsers$  = $('#generate-users');
            var uploadUsers$    = $('#upload-users');
            var usersOutput$    = $('#users-output');

            var addRandom       = spawn('input#random-string | type=checkbox | title=add a random string to the username');
            var emailUser       = spawn('input#email-user | type=text | size=20 | placeholder=username | title=use only gmail username');
            var defaultPassword = spawn('input#default-password | type=text | size=10 | placeholder=password | title=common password for all generated users');
            var userCount       = spawn('input#user-count | type=number | size=4');
            var verifyEvery     = spawn('input#verify-every | type=number | size=2');
            var enableEvery     = spawn('input#enable-every | type=number | size=2');
            var generateButton$ = $.spawn('button#generate | type=button', 'Generate Users');

            var outputTextarea  = spawn('textarea#user-list|rows=20|cols=80');

            generateButton$.on('click', function(e){
                e.preventDefault();
                var randomize = $('#random-string').prop('checked');
                outputTextarea.value = JSON.stringify(XNAT.namer.generate(userCount.value, null, null, randomize))
            });

            var uploadButton$ = $.spawn('button#do-upload|type=button', 'Upload Users');

            uploadButton$.on('click', function(e){
                e.preventDefault();
                var userList = JSON.parse(outputTextarea.value);
                var randomize = $('#random-string').prop('checked');
                xmodal.message(false, 'Creating users...', 'Close');
                XNAT.namer.postUniqueNames(null, userList, {
                    random: randomize,
                    email: (emailUser.value ? (emailUser.value + '+') : '') + 'USERNAME@gmail.com',
                    password: defaultPassword.value,
                    verified: verifyEvery.value + '',
                    enabled: enableEvery.value + ''
                }, function(){
                    xmodal.closeAll();
                    xmodal.message(false, '<div class="success">Users created.</div>', 'Close');
//                    XNAT.ui.banner.top(3000, 'Users created.', 'success');
                })
            });

            function spawnLabel(text, input){

                var labelText = spawn('b', {
                    style: {
                        display: 'table-cell',
                        padding: '5px 10px 5px 0',
                        textAlign: 'left'
                    }
                }, text);

                var textInput = spawn('div', {
                    style: {
                        display: 'table-cell',
                        padding: '5px 0 5px 10px'
                    }
                }, [input]);

                var labelRow = spawn('label', {
                    style: {
                        display: 'table-row',
                        whiteSpace: 'nowrap'
                    }
                });

                labelRow.append(labelText);
                labelRow.append(textInput);

                return labelRow;

            }

            generateUsers$.append([
                spawn('p', [addRandom, '<b>add a random string to the username</b>']),
                '<br>',
                spawnLabel('gmail username: ', emailUser),
                spawnLabel('default password: ', defaultPassword),
                spawnLabel('number of users: ', userCount),
                spawnLabel('verify every # users: ', verifyEvery),
                spawnLabel('enable every # users: ', enableEvery),
                '<br>',
                generateButton$, '&nbsp;&nbsp;', uploadButton$
            ]);
            //            uploadUsers$.append(uploadButton$);
            usersOutput$.append(outputTextarea);

        }());

    </script>

    <div id="xnat-scripts"></div>

</pg:restricted>

