<!-- BEGIN views/spawner/elements.jsp -->
<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="sp" tagdir="/WEB-INF/tags/spawner" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="elements" scope="request" type="java.util.List"/>
<jsp:useBean id="element" scope="request" type="org.nrg.xnat.spawner.entities.SpawnerElement"/>

<sp:layout title="Spawner Elements">
    <h3 style="margin:0 0 15px 0;">Spawner Elements</h3>

    <div style="margin:15px 0;max-width:696px;min-width:400px;">
        The Spawner is XNAT's system for managing dynamically configurable user interface elements.
    </div>

    <div class="yui-skin-sam">

        <style type="text/css" scoped="scoped">
            #automation-events-scripts .yui-content > div {
                padding: 2px;
            }

            td.edit:hover {
                cursor: pointer
            }

            td.edit:hover a,
            td.actions a:hover {
                text-decoration: underline
            }

            td.edit a,
            td.actions a {
                display: inline-block;
                padding: 2px 8px;
            }

            table.edit-script {
                margin-bottom: -4px;
            }

            table.edit-script td {
                padding: 4px 2px;
                vertical-align: baseline;
            }
        </style>

        <script type="text/javascript" src="/scripts/xnat/app/siteEventsManager.js"></script>
        <script type="text/javascript" src="scripts/uploaders/AutomationBasedUploader.js"></script>

        <div id="addEventHandler" class="html-template">
            <table>
                <tr>
                    <td><label for="select_event" class="required"><strong>Event:</strong><i>*</i></label></td>
                    <td><select id="select_event" name="event" class="event"></select></td>
                </tr>
                <tr>
                    <td><label for="select_scriptId" class="required"><strong>Script ID:</strong><i>*</i></label></td>
                    <td><select id="select_scriptId" name="scriptId" class="scriptId"></select></td>
                </tr>
                <tr>
                    <td><label for="description"><strong>Description:</strong></label></td>
                    <td><input id="description" name="description" class="description" type="text" size="40"></td>
                </tr>
            </table>
        </div>

        <!-- SITE EVENTS TABLE ROW TEMPLATE -->
        <table id="event-row-template" class="html-template">
            <tbody>
            <tr class="highlight events-list">
                <td class="site-event-id">
                </td>
                <td class="site-event-label">
                </td>
                <td class="workflow-label"></td>
                <td class="actions" style="text-align:center;white-space:nowrap;">
                </td>
            </tr>
            </tbody>
        </table>

        <table id="script-row-template" class="html-template">
            <tbody>
            <tr class="highlight script-list" data-script-id="__SCRIPT_ID__">
                <td class="edit">
                </td>
                <td class="edit" data-action="editScript"></td>
                <td class="actions" style="text-align:center;white-space:nowrap;">
                </td>
            </tr>
            </tbody>
        </table>

        <div class="yui-content">

            <table id="elements-table" class="xnat-table" style="width: 100%;">

                <thead>
                <tr><th width="40%">Element</th>
                    <th width="40%">Description</th>
                    <th width="20%">&nbsp;</th>
                </tr></thead>

                <tbody>
                <c:forEach var="element" items="${elements}">
                    <tr class="highlight events-list">
                        <td class="site-event-id">
                                ${element.label}
                        </td>
                        <td class="site-event-label">
                                ${element.description}
                        </td>
                        <td class="actions" style="text-align:center;white-space:nowrap;">
                            <a href="#!" class="edit-event" data-action="editEvent"
                               title="edit existing event">edit</a>
                            <a href="#!" class="delete-event" data-action="deleteEvent"
                               title="delete existing event">delete</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>

            </table>

        </div>

        <script type="text/javascript">

            // YUI tab simulator using jQuery for event handling for '.bogus-tabs'
            // using 'on' to handle any tabs that may be added dynamically
            jq('.yui-navset.bogus-tabs').each(function () {

                var __navset = jq(this),
                        TABS = '> ul.yui-nav > li',
                        CONTENTS = '> .yui-content > div',
                        selected = null;

                __navset.on('click', TABS + '> a', function (e) {

                    e.preventDefault();

                    var __link = jq(this),
                            __tab = __link.closest('li');

                    selected = __link.attr('href'); // safer to use jQuery to get 'href' value

                    __navset.find(TABS).removeClass('selected');
                    __tab.addClass('selected');

                    __navset.find(CONTENTS).addClass('yui-hidden');
                    __navset.find(selected).removeClass('yui-hidden');

                });

            });

        </script>

        <!-- SITE EVENTS DIALOG TEMPLATE -->
        <div id="site-events-template" class="html-template">
            <p>Select a Workflow or enter a new Event name to define an Event to use with project and site Event
                Handlers:</p>
            <table>
                <tr>
                    <td><label for="workflow-event-id-menu" class="required"><strong>Workflow:</strong><i>*</i></label>
                    </td>
                    <td>

                        <div id="workflow-event-id-container">
                            ## workflow events menu
                        </div>
                        ## <select id="workflow-event-id-menu" name="event_id" class="event_id active">
                        ##
                        <option data-label="__WORKFLOW_EVENT_LABEL__" data-id="__WORKFLOW_EVENT_ID__"
                                value="__WORKFLOW_EVENT_ID__">__WORKFLOW_EVENT_LABEL__
                        </option>
                        ## </select>

                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <div id="custom-event-id" style="padding:10px 0;">
                            <input type="checkbox" id="custom-event-checkbox">
                            <label for="custom-event-checkbox">Or enter a custom Event id:</label>
                            <br>
                            <input type="text" id="custom-event-input" class="event_id"
                                   placeholder="(enter custom Event id)" disabled>
                        </div>
                    </td>
                </tr>
                ##
                <tr>
                    ##
                    <td><label for="scriptId" class="required"><strong>Script ID:</strong><i>*</i></label></td>
                    ##
                    <td><select id="scriptId" name="scriptId" class="scriptId"></select></td>
                    ##
                </tr>
                <tr>
                    <td><label for="events-event-label"><strong>Label:</strong></label></td>
                    <td>
                        <input id="events-event-label" name="event_label" class="event_label" type="text" size="40">
                    </td>
                </tr>
                <tr>
                    <td></td>
                    <td>
                        <small>Label not required. If no label is entered, it will be the same as the ID.</small>
                    </td>
                </tr>
            </table>
        </div>

        <!-- SCRIPT EDITOR TEMPLATE -->
        <div id="script-editor-template" class="html-template">
            <input type="hidden" name="id" class="id" value="">
            <input type="hidden" name="scriptId" class="scriptId" value="">
            <input type="hidden" name="language" class="language" value="">
            <input type="hidden" name="languageVersion" class="languageVersion" value="">
            <input type="hidden" name="timestamp" class="timestamp" value="">
            <table class="edit-script" border="0" cellspacing="0">
                <tr>
                    <td><b>Script ID: </b>&nbsp;</td>
                    <td>
                        <b class="script-id-text"></b>
                        <input type="text" name="script-id-input" class="script-id-input" size="30" value="">
                    </td>
                </tr>
                <tr>
                    <td><b>Description: </b>&nbsp;</td>
                    <td><input type="text" name="script-description" class="script-description" size="80" value=""></td>
                </tr>
                <tr>
                    <td><b>Script Version: </b>&nbsp;</td>
                    <td><select id="script-version" class="script-version">
                        <option value="!">Select a Version</option>
                    </select></td>
                </tr>
            </table>
            <br>
            <div class="editor-wrapper" style="width:840px;height:482px;position:relative;">
                <!-- the '.editor-content' div gets replaced when the script content is loaded -->
                <div class="editor-content"
                     style="position:absolute;top:0;right:0;bottom:0;left:0;border:1px solid #ccc;"></div>
            </div>
        </div><!-- /#script-editor-template -->

        <script type="text/javascript" src="/scripts/lib/ace/ace.js"></script>
        <script type="text/javascript" src="/scripts/xnat/app/automation.js"></script>
        <script type="text/javascript" src="/scripts/xnat/app/scriptEditor.js"></script>

    </div>

</sp:layout>
<!-- END views/spawner/elements.jsp -->