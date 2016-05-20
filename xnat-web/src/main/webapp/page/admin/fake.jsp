<%@ page session="true" contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pg" tagdir="/WEB-INF/tags/page" %>

<c:if test="${empty hasInit}">
    <pg:init>
        <c:if test="${empty hasVars}">
            <pg:jsvars/>
        </c:if>
    </pg:init>
</c:if>

<script src="${siteRoot}/scripts/lib/jquery-plugins/jquery.dataAttr.js"></script>

<pg:restricted>

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
            <ul class="nav tab-group" id="dashboard">
                <li class="label">Dashboard</li>
                <li class="tab"><a title="Site Dashboard" href="#site-dashboard">Site Dashboard</a></li>
            </ul>
            <ul class="nav tab-group" id="xnat-setup">
                <li class="label">XNAT Setup</li>
                <li class="tab active"><a title="Site Setup" href="#site-setup">Site Setup</a></li>
                <li class="tab"><a title="Security" href="#security">Security</a></li>
                <li class="tab"><a title="Email Server" href="#email-server">Email Server</a></li>
                <li class="tab"><a title="Notifications" href="#notifications">Notifications</a></li>
                <li class="tab"><a title="Themes &amp;amp; Features" href="#features">Themes &amp; Features</a></li>
            </ul>
            <ul class="nav tab-group" id="manage-access">
                <li class="label">Manage Access</li>
                <li class="tab"><a title="Authentication Methods" href="#authentication">Authentication Methods</a></li>
                <li class="tab"><a title="Users" href="#users">Users</a></li>
                <li class="tab"><a title="User Roles" href="#user-roles">User Roles</a></li>
                <li class="tab"><a title="Registration Options" href="#registration">Registration Options</a></li>
            </ul>
            <ul class="nav tab-group" id="manage-data">
                <li class="label">Manage Data</li>
            </ul>
            <ul class="nav tab-group" id="processing">
                <li class="label">Processing</li>
            </ul>
            <ul class="nav tab-group" id="project-customization">
                <li class="label">Project Customization</li>
            </ul>
            <ul class="nav tab-group" id="advanced">
                <li class="label">Advanced XNAT Settings</li>
                <li class="tab"><a title="DICOM SCP Receivers" href="#dicom-scp-receivers">DICOM SCP Receivers</a></li>
            </ul>
        </div>
        <div class="xnat-tab-content side pull-right">
            <!-- ================== -->
            <!-- Admin tab panes    -->
            <!-- ================== -->
            <div class="tab-pane" id="site-dashboard-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane active" id="site-setup-content" style="position: relative; padding-bottom: 60px; display: block;">
                <div class="pad">
                    <div class="tab-pane-content">
                        <form class="xnat-form-panel panel panel-default" id="site-information">
                            <div class="panel-heading"><h3 class="panel-title">Site Information</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="siteId">
                                    <label class="element-label" for="site-id">Site ID</label>
                                    <div class="element-wrapper">
                                        <input name="siteId" type="text" id="site-id" size="25" title="Site ID">
                                        <div class="description">The id used to refer to this site (also used to generate database ids). No spaces or non-alphanumeric characters. It should be a short, one-word name or acronym which describes your site.</div>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="siteURL">
                                    <label class="element-label" for="site-url">Site Url</label>
                                    <div class="element-wrapper">
                                        <input name="siteURL" type="text" id="site-url" size="25" title="Site Url">
                                    </div>
                                </div>
                                <div class="panel-element element-group" data-name="siteDescription">
                                    <label class="element-label" for="site-description">Site Description</label>
                                    <div class="element-wrapper">
                                        <div class="group-item" data-name="siteDescriptionType">
                                            <input name="siteDescriptionType" type="hidden" value="page"></div>
                                        <div class="group-item" data-name="siteDescription">
                                            <input name="siteDescription" type="radio" id="site-description-page" value="page" class="dirty"><label for="site-description-page-input"> Page</label><br><input id="site-description-page-input" type="text" name="siteDescriptionPage">
                                        </div>
                                        <div class="group-item" data-name="siteDescription">
                                            <input name="siteDescription" type="radio" id="site-description-text" value="text" class="dirty"><label for="site-description-text-input"> Text (Markdown)</label><br><textarea class="hidden" id="site-description-text-input" name="siteDescriptionText">foo.</textarea>
                                        </div>
                                    </div>
                                </div>
                                <div class="hidden" data-name="homePage">
                                    <div class="element-wrapper">
                                        <input name="homePage" type="hidden" id="home-page" value="/screens/QuickSearch.vm">
                                    </div>
                                </div>
                                <div class="panel-element" data-name="landingPage">
                                    <label class="element-label" for="landing-page">Landing Page</label>
                                    <div class="element-wrapper">
                                        <input name="landingPage" type="text" id="landing-page" size="25" title="Landing Page"><span class="after"><label class="small"><input id="toggle-home-page" type="checkbox" name="setHomePage" class="dirty"> Use This As My Home Page</label></span>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="homePageInput">
                                    <label class="element-label" for="home-page-input">Home Page</label>
                                    <div class="element-wrapper">
                                        <input name="homePageInput" type="text" id="home-page-input" size="25" title="Home Page" data-value="/screens/QuickSearch.vm">
                                        <script>XNAT.element.copyValue('?homePageInput', '?homePage')</script>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" class="save pull-right btn btn-sm btn-primary">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" class="revert pull-right btn btn-sm btn-default">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                        <form class="xnat-form-panel panel panel-default" id="admin-information">
                            <div class="panel-heading"><h3 class="panel-title">Admin Information</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="siteAdminEmail">
                                    <label class="element-label" for="site-admin-email">Site Admin Email</label>
                                    <div class="element-wrapper">
                                        <input name="siteAdminEmail" type="text" id="site-admin-email" size="25" title="Site Admin Email">
                                        <div class="description"><p>
                                            <a href="/path/to/notification-options">&gt;&gt; Set Email Notification Options</a>
                                        </p>
                                            <p>
                                                <a href="/path/to/email-server-config">&gt;&gt; Setup XNAT Email Server</a>
                                            </p></div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" disabled="" class="save pull-right btn btn-sm btn-primary disabled">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" disabled="" class="revert pull-right btn btn-sm btn-default disabled">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                    </div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="security-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content">
                        <form class="xnat-form-panel panel panel-default" id="site-security">
                            <div class="panel-heading"><h3 class="panel-title">General Site Security Settings</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="securityChannel">
                                    <label class="element-label" for="security-channel">Security Channel</label>
                                    <div class="element-wrapper">
                                        <select name="securityChannel" id="security-channel" title="Security Channel">
                                            <option value="http">http</option>
                                            <option value="https">https</option>
                                        </select></div>
                                </div>
                                <div class="panel-element" data-name="requireLogin">
                                    <label class="element-label" for="require-login">Require User Login?</label>
                                    <div class="element-wrapper">
                                        <input name="requireLogin" id="require-login" type="checkbox" title="Require User Login?" class="dirty">
                                        <div class="description">If checked, then only registered users will be able to access your site. If false, anyone visiting your site will automatically be logged in as 'guest' with access to public data.</div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" class="save pull-right btn btn-sm btn-primary">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" class="revert pull-right btn btn-sm btn-default">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                        <form class="xnat-form-panel panel panel-default" id="login-session-controls">
                            <div class="panel-heading"><h3 class="panel-title">User Logins / Session Controls</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="sessionTimeout">
                                    <label class="element-label" for="session-timeout-input">Session Timeout</label>
                                    <div class="element-wrapper">
                                        <input name="sessionTimeout" id="session-timeout-input" type="text" size="3" title="Session Timeout">
                                        <div class="description">Number of minutes of inactivity before users are locked out of the site. This will not affect users that are currently logged in.</div>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="sessionTimeoutMessage">
                                    <label class="element-label" for="session-timeout-message">Session Timeout Message</label>
                                    <div class="element-wrapper">
                                        <textarea name="sessionTimeoutMessage" id="session-timeout-message" title="Session Timeout Message"></textarea>
                                        <div class="description">Alert message provided to users after a session timeout and logout.</div>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="sessionAllowResume">
                                    <label class="element-label" for="session-allow-resume">Allow Resume on Next Login?</label>
                                    <div class="element-wrapper">
                                        <input name="sessionAllowResume" id="session-allow-resume" type="checkbox" title="Allow Resume on Next Login?" value="true">
                                        <div class="description">Allow user to resume where they left off, if logging back in after a session timeout?</div>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="maxSessions">
                                    <label class="element-label" for="max-sessions">Maximum Concurrent Sessions</label>
                                    <div class="element-wrapper">
                                        <input name="maxSessions" type="text" id="max-sessions" size="3" title="Maximum Concurrent Sessions">
                                        <div class="description">The maximum number of permitted sessions a user can have open simultaneously</div>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="loginFailureMessage">
                                    <label class="element-label" for="login-failure-message">Login Failure Message</label>
                                    <div class="element-wrapper">
                                        <textarea name="loginFailureMessage" id="login-failure-message" title="Login Failure Message"></textarea>
                                        <div class="description">Text to show when a user fails to login</div>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" disabled="" class="save pull-right btn btn-sm btn-primary disabled">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" disabled="" class="revert pull-right btn btn-sm btn-default disabled">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                    </div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="dicom-scp-receivers-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content">
                        <form class="xnat-form-panel panel panel-default">
                            <div class="panel-heading"><h3 class="panel-title">Receivers</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="receiverList">
                                    <label class="element-label" for="receiver-list">Receiver List</label>
                                    <div class="element-wrapper">
                                        <table class="input-table">
                                            <tbody>
                                            <tr>
                                                <td><input name="fooInput" data-value="Value 1"></td>
                                                <td>Text</td>
                                                <td>Foo</td>
                                                <td>Bar</td>
                                            </tr>
                                            <tr>
                                                <td><input name="barInput" data-value="1"></td>
                                                <td>2</td>
                                                <td>3</td>
                                                <td>4</td>
                                            </tr>
                                            <tr>
                                                <td><input name="bazInput" data-value="5"></td>
                                                <td>6</td>
                                                <td>7</td>
                                                <td>8</td>
                                            </tr>
                                            </tbody>
                                        </table>
                                        <script>$('#receiver-list').addClass('foo');</script>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" disabled="" class="save pull-right btn btn-sm btn-primary disabled">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" disabled="" class="revert pull-right btn btn-sm btn-default disabled">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                    </div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="email-server-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content">
                        <form class="xnat-form-panel panel panel-default" id="email-server-config">
                            <div class="panel-heading"><h3 class="panel-title">Email Server Configuration</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="xdat.mail.server">
                                    <label class="element-label">Mail Server Host</label>
                                    <div class="element-wrapper">
                                        <input name="xdat.mail.server" type="text" size="25" title="Mail Server Host">
                                    </div>
                                </div>
                                <div class="panel-element" data-name="xdat.mail.port">
                                    <label class="element-label">Mail Server Port</label>
                                    <div class="element-wrapper">
                                        <input name="xdat.mail.port" type="text" size="5" title="Mail Server Port">
                                    </div>
                                </div>
                                <div class="panel-element" data-name="xdat.mail.username">
                                    <label class="element-label">Mail Server Username</label>
                                    <div class="element-wrapper">
                                        <input name="xdat.mail.username" type="text" size="25" title="Mail Server Username">
                                    </div>
                                </div>
                                <div class="panel-element" data-name="xdat.mail.password">
                                    <label class="element-label">Mail Server Password</label>
                                    <div class="element-wrapper">
                                        <input name="xdat.mail.password" type="password" title="Mail Server Password">
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" disabled="" class="save pull-right btn btn-sm btn-primary disabled">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" disabled="" class="revert pull-right btn btn-sm btn-default disabled">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                    </div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="notifications-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="features-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content">
                        <form class="xnat-form-panel panel panel-default" id="theme-config">
                            <div class="panel-heading"><h3 class="panel-title">Theme Management</h3></div>
                            <div class="panel-body">
                                <div class="panel-element" data-name="undefined">
                                    <div class="element-wrapper">
                                        <script type="text/javascript" src="../../scripts/themeManagement.js"></script>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="xnat.theme.active">
                                    <label class="element-label">Current theme</label>
                                    <div class="element-wrapper"><span title="Current theme">None</span></div>
                                </div>
                                <div class="panel-element" data-name="xnat.theme.available">
                                    <label class="element-label" for="themeSelection">Select an existing theme</label>
                                    <div class="element-wrapper">
                                        <select name="xnat.theme.available" id="themeSelection" title="Select an existing theme">
                                            <option value="null">None</option>
                                            <option value="Modern UI">Modern UI</option>
                                            <option value="Green XNAT Theme">Green XNAT Theme</option>
                                            <option value="Red XNAT Theme">Red XNAT Theme</option>
                                        </select>&nbsp;<!--button id="submitThemeButton" onclick="setTheme();">Set Theme</button-->&nbsp;&nbsp;
                                        <button id="removeThemeButton" onclick="removeTheme();">Remove Theme</button>
                                    </div>
                                </div>
                                <div class="panel-element" data-name="xnat.theme.upload">
                                    <label class="element-label">Upload a theme package</label>
                                    <div class="element-wrapper">
                                        <file title="Upload a theme package"></file>
                                        <form id="uploadThemeForm" method="POST" class="optOutOfXnatDefaultFormValidation" action="/xapi/theme?XNAT_CSRF=undefined">
                                            <span class="label bold">Upload a theme package: </span><input type="file" id="themeFileUpload" name="themeFileUpload" multiple="" style="width: 270px; display: inline; float: left;">
                                            <button type="submit" id="submitThemeUploadButton" style="position: relative; top:-15px;">Upload</button>
                                        </form>
                                    </div>
                                </div>
                            </div>
                            <div class="panel-footer">
                                <button type="submit" disabled="" class="save pull-right btn btn-sm btn-primary disabled">Save</button>
                                <span class="pull-right">&nbsp;&nbsp;&nbsp;</span>
                                <button type="button" disabled="" class="revert pull-right btn btn-sm btn-default disabled">Discard Changes</button>
                                <button type="link" class="defaults pull-left btn btn-sm btn-link">Default Settings</button>
                                <div class="clear"></div>
                            </div>
                        </form>
                    </div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="authentication-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="users-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="user-roles-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
            <div class="tab-pane" id="registration-content" style="position: relative; padding-bottom: 60px; display: none;">
                <div class="pad">
                    <div class="tab-pane-content"></div>
                </div>
                <footer class="footer">
                    <button class="save-all btn btn-primary pull-right" type="button">Save All</button>
                </footer>
            </div>
        </div>
    </div>

    <script>
//        (function(){
//            // get the JSON and do the setup
//            $.getJSON('/page/admin/data/config/site-admin.json').done(function(data){
//                var adminTabs =
//                            XNAT.ui.tabs
//                                .init(data.Result)
//                                .render('#admin-config-tabs');
//
//                console.log(adminTabs);
//            });
//        })();
    </script>

</div>

</pg:restricted>