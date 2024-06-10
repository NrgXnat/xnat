/*
 * web: eventsManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * functions for XNAT events (project)
 * xnat-templates/screens/xnat_projectData/xnat_projectData_summary_manage.vm
 */

XNAT.app.eventsManager = {};
XNAT.app.eventsManager.events = [];
XNAT.app.eventsManager.scripts = [];
XNAT.app.eventsManager.handlers = [];
XNAT.app.eventsManager.importhandlers = [];
 xhr = XNAT.xhr;

$(function() {

    var eventsManager = XNAT.app.eventsManager,
        xhr = XNAT.xhr;

    //var systemEvents = []; // ?
    //var systemScripts = []; // ?

    var $events_table = $('#events_table'),
        $no_events_defined = $('#no_events_defined'),
        $no_event_handlers = $('#no_event_handlers'),
        $add_event_handler = $('#add_event_handler'),
        $manage_event_handlers = $('#manage_event_handlers'),
        $import_event_handlers = $('#import_event_handlers'),
        handlersRendered = false,
        hasEvents = false;

    function initEventsTable() {
        $events_table.hide();
        $no_event_handlers.hide();

        // Now get all the data and stick it in the table.
        xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects/' + window.projectScope + '/automation/handlers'),
            success: function(response) {

                var eventRows = '',
                    _handlers = (response.ResultSet) ? response.ResultSet.Result || [] : [];

                // reset handlers array
                eventsManager.handlers = [];

                if (_handlers.length) {
                    handlersRendered = true;
                    eventRows +=
                        '<dl class="header">' +
                        '<dl>' +
                        '<dd class="col1">Event</dd>' +
                        '<dd class="col2">Script</dd>' +
                        '<dd class="col3">Description</dd>' +
                        '</dl>' +
                        '</dl>';
                    forEach(_handlers, function(eventHandler) {
                        var _event_id = XNAT.utils.escapeXML(eventHandler['event']);
                        eventsManager.handlers.push(_event_id);
                        eventRows += '<dl class="item">' +
                            '<dd class="col1">' + _event_id + '</dd>' +
                            '<dd class="col2">' + XNAT.utils.escapeXML(eventHandler.scriptId) + '</dd>' +
                            '<dd class="col3">' + XNAT.utils.escapeXML(eventHandler.description) + '</dd>' +
                            '<dd class="col4" style="text-align: right;">' +
                            '<button href="javascript:" class="edit-handler event-handler-button" ' +
                            'data-handler="' + eventHandler.triggerId + '" ' +
                            'data-event="' + _event_id + '" ' +
                            ' title="Edit handler for event ' + _event_id + '">edit</button>' +
                            '</dd>' +
                            '<dd class="col4" style="text-align: right;">' +
                            '<button href="javascript:" class="delete-handler event-handler-button" ' +
                            'data-handler="' + eventHandler.triggerId + '" ' +
                            'data-event="' + _event_id + '" ' +
                            ' title="Delete handler for event ' + _event_id + '">delete</button>' +
                            '</dd>' +
                            '<dd class="col5" style="text-align: center;">' +
                            '<button href="javascript:" class="configure-uploader-handler event-handler-button" ' +
                            'data-handler="' + eventHandler.triggerId + '" ' +
                            'data-event="' + _event_id + '" ' +
                            ' title="Configure uploader for event ' + _event_id + '">configure uploader</button>' +
                            '</dd>' +
                            '<dd class="colC">' + '<b>Event Class: </b> ' + getEventClassDisplayValueFromHandlers(_handlers, eventHandler) + '</dd>' +
                            '<dd class="colC">' + '<b>Event Filters: </b> ' + XNAT.utils.escapeXML(eventHandler.eventFilters) + '</dd>' +
                            '</dl>';
                    });
                    $events_table.html(eventRows);
                    $events_table.show();
                } else {
                    if (!hasEvents) {
                        $add_event_handler.prop('disabled', true);
                        $no_event_handlers.show();
                    } else {
                        $no_event_handlers.show();
                    }
                }
            },
            error: function(request, status, error) {
                if ($("#err_event_handlers").length < 1) {
                    $("#events_list").prepend('<p id="err_event_handlers" style="padding:20px;">' +
                        'An error occurred retrieving event handlers for this project: [' + status + '] ' + error +
                        '.  This may mean that your account does not have permission to edit event handlers for this project.</p>').show();
                    $("#manage_event_handlers").prop('disabled', true);
                }
            },
            complete: function() {
                //$('#accordion').accordion('refresh');
            }
        });
    }

    function initEventsMenu(isEdit, triggerId) {
        eventsManager.events = []; // reset array
        XNAT.app.eventsManager.triggerData = null;

        $("#select_event").prop('disabled', 'disabled');
        if (typeof XNAT.app.eventsManager.eventClasses === 'undefined') {
            var eventClassesAjax = $.ajax({
                type: "GET",
                url: serverRoot + "/xapi/projects/" + window.projectScope + '/eventHandlers/automationEventClasses?XNAT_CSRF=' + window.csrfToken,
                cache: false,
                async: false,
                context: this,
                dataType: 'json'
            });
            eventClassesAjax.done(function(data, textStatus, jqXHR) {
                if (typeof data !== 'undefined') {
                    XNAT.app.eventsManager.eventClasses = data;
                }
            });
            eventClassesAjax.fail(function(data, textStatus, jqXHR) {
                xmodal.message('Error', 'An error occurred retrieving system events: ' + textStatus);
            });
        }
        if (!isEdit) {
            //Add Mode: Populate Events Modal window with default values.
            populateEventsMenu();
        } else {
            //Edit Mode: Get the data from server for existing event.
            var eventHandlerAjax = $.ajax({
                type: "GET",
                url: serverRoot + '/xapi/eventHandlers/' + triggerId + '?XNAT_CSRF=' + window.csrfToken,
                cache: false,
                async: true,
                context: this,
                dataType: 'json'
            });
            eventHandlerAjax.done(function(data, textStatus, jqXHR) {
                if (typeof data !== 'undefined') {
                    XNAT.app.eventsManager.triggerData = data;
                    XNAT.app.eventsManager.triggerId = triggerId;
                    populateEventsMenu(data);
                }
            });
            eventHandlerAjax.fail(function(data, textStatus, jqXHR) {
                xmodal.message('Error', 'An error occurred retrieving system events: ' + textStatus);
            });
        }
    }



    function populateEventsMenu(triggerData) {

        $('#select_eventClass').empty().append('<option></option>');
        for (var i = 0; i < XNAT.app.eventsManager.eventClasses.length; i++) {
            if (typeof XNAT.app.eventsManager.eventClasses[i].class !== 'undefined') {
                $('#select_eventClass').append('<option value="' + XNAT.app.eventsManager.eventClasses[i].class + '">' + getEventClassDisplayValue(XNAT.app.eventsManager.eventClasses[i].class) + '</option>');
            }
        }
        //Add Mode: If trigger data is null or undefined.
        if (triggerData === null || typeof triggerData === 'undefined') {
            xmodal.open({
                title: 'Add Event Handler',
                template: $('#addEventHandler'),
                width: 600,
                height: 350,
                overflow: 'auto',
                beforeShow: function(obj) {},
                buttons: {
                    save: {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: doAddEventHandler
                    },
                    close: {
                        label: 'Cancel'
                    }
                }
            });

        } else {
            //Edit Mode: If trigger data is not null or undefined.
            xmodal.open({
                title: 'Edit Event Handler',
                template: $('#addEventHandler'),
                width: 600,
                height: 350,
                overflow: 'auto',
                beforeShow: function(obj) {},
                buttons: {
                    save: {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: doEditEventHandler
                    },
                    close: {
                        isDefault: false,
                        label: 'Cancel'
                    }

                }
            });
            $('#select_eventClass').val(XNAT.app.eventsManager.triggerData.srcEventClass);
            $('#description').val(XNAT.app.eventsManager.triggerData.description);
            $('#select_scriptId').val(XNAT.app.eventsManager.triggerData.scriptId);
            $('#select_event').val(XNAT.app.eventsManager.triggerData.event);
        }
        updateEventIdSelect();
        $('#select_eventClass').change(function() {
            updateEventIdSelect();
        });

    }

    function updateEventIdSelect() {
        $('#select_event').empty().append('<option></option>');
        $('#filterRow').css('display', 'none');
        $('#filterDiv').html(filterableHtml);
        for (var i = 0; i < XNAT.app.eventsManager.eventClasses.length; i++) {
            if ($('#select_eventClass').val() == XNAT.app.eventsManager.eventClasses[i].class) {
                var eventIds = XNAT.app.eventsManager.eventClasses[i].eventIds;
                eventFromDropdown = false;
                if (typeof eventIds !== 'undefined' && eventIds.length > 0) {
                    for (var j = 0; j < eventIds.length; j++) {
                        var eventId = eventIds[j];
                        $('#select_event').append('<option value="' + eventId + '">' + eventId + '</option>');
                        //If trigger data is not null and event is matching with one of the dropdown value then it is not a custom value.
                        if (XNAT.app.eventsManager.triggerData !== null && XNAT.app.eventsManager.triggerData.event === eventId) {
                            eventFromDropdown = true;
                        }
                    }
                }
                if (XNAT.app.eventsManager.triggerData != null) {
                    $('#select_input').css('display', 'none');
                    $('#select_event').css('display', 'none');
                    //If value from event Id dropdown.
                    if (eventFromDropdown) {
                        $('#select_event').val(XNAT.app.eventsManager.triggerData.event);
                        $('#select_event').css('display', 'inline');
                    }
                    //If custom value.
                    else {
                        $('#select_input').val(XNAT.app.eventsManager.triggerData.event);
                        $('#select_input').css('display', 'inline');
                    }
                }
                var filterableFields = XNAT.app.eventsManager.eventClasses[i].filterableFields;
                var filterableHtml = ""
                var selectOptionsHtml = ""
                $('#filterRow').css('display', 'none');
                if (typeof filterableFields !== 'undefined') {
                    for (var filterable in filterableFields) {
                        if (!filterableFields.hasOwnProperty(filterable)) continue;
                        var filterableInfo = filterableFields[filterable];
                        var filterableVals = filterableInfo["filterValues"];
                        var filterableRequired = filterableInfo["filterRequired"];
                        var filterableDefault = filterableInfo["defaultValue"];
                        if (XNAT.app.eventsManager.triggerData != null && XNAT.app.eventsManager.triggerData.eventFilters != null && XNAT.app.eventsManager.triggerData.eventFilters.length != 'undefined' && XNAT.app.eventsManager.triggerData.eventFilters.length > 0) {
                            filterableDefault = XNAT.app.eventsManager.triggerData.eventFilters[0].filterVals[0];
                        }
                        if (typeof filterableRequired !== 'undefined' && !filterableRequired) {
                            filterableHtml = filterableHtml + '<option value="">&lt;NONE&gt;</option>';
                        }
                        if (typeof filterableVals !== 'undefined' && filterableVals.length > 0) {

                            customValuePresent = true;
                            customValue = "";
                            for (var i = 0; i < filterableVals.length; i++) {
                                if (typeof filterableDefault !== 'undefined' && filterableDefault == filterableVals[i]) {
                                    customValuePresent = false;
                                    selectOptionsHtml += '<option value="' + filterableVals[i] + '" selected>' + filterableVals[i] + '</option>';
                                } else {
                                    selectOptionsHtml += '<option value="' + filterableVals[i] + '">' + filterableVals[i] + '</option>';
                                }
                            }
                            customValueStyle = 'style="display:none" ';
                            selectDropDownStyle = 'style="display:inline" ';
                            buttonDisplayName = 'Custom Value';
                            if (customValuePresent) {
                                customValue = filterableDefault;
                                customValueStyle = 'style="display:inline" ';
                                selectDropDownStyle = 'style="display:none" ';
                                buttonValue = 'Selection Menu';
                            }
                            filterableHtml = filterableHtml + '<div style="width:100%;margin-top:5px;margin-bottom:5px">' + filterable + ' &nbsp;<select id="filter_sel_' + filterable + '" name="' + filterable + '" class="filter" ' + selectDropDownStyle + ' >';
                            filterableHtml += selectOptionsHtml;
                            filterableHtml = filterableHtml + '</select> <input type="text" id="filter_input_' + filterable + '" name="' + filterable +
                                '" value="' + customValue + '" class="filter" ' + customValueStyle + ' style="display:none" size="15"/> <button class="customButton">' + buttonDisplayName + '</button></div>';
                        }
                    }
                }
                if (filterableHtml.length > 0) {
                    $('#filterRow').css('display', 'table-row');
                    $('#filterDiv').html(filterableHtml);
                } else {
                    $('#filterDiv').html("");
                }
                $("#select_event").prop('disabled', false);
                break;
            }
        }
        $(".customButton").each(function() {
            var eventObject = $._data(this, 'events');
            if (typeof eventObject == 'undefined' || typeof eventObject.click == 'undefined') {
                $(this).click(function(event) {
                    customInputToggle(event.target);
                });
            }
        });
        $(".customButton").css('margin-left', '5px');
    }

    function customInputToggle(ele) 
	{
		$(ele).parent().find("input, select").each(function() {
			if ($(this).css('display') == 'none') {
				$(this).css('display', 'inline');
			} else {
				$(this).css('display', 'none');
				//if ($(this).is("input")) {
				$(this).val("");
				//}
			}
		});
		if ($(ele).html() == "Selection Menu") {
			$(ele).html("Custom Value");
		} else {
			$(ele).html("Selection Menu");
		}
    }

    function initScriptsMenu() {
        //if (!hasEvents) { return; }
        eventsManager.scripts = []; // reset array
        xhr.getJSON({
            url: XNAT.url.restUrl('/data/automation/scripts'),
            success: function(response) {

                var scripts = response.ResultSet.Result || [],
                    $scriptsMenu = $('#select_scriptId'),
                    options = '<option></option>';

                if (!scripts.length) {
                    options += '<option value="!" disabled>(no scripts available)</option>';
                    $scriptsMenu.html(options).prop('disabled', true);
                } else {
                    forEach(scripts, function(script) {
                        options += '<option title="' + XNAT.utils.escapeXML(script['Description']) + '" value="' + XNAT.utils.escapeXML(script['Script ID']) + '">' + XNAT.utils.escapeXML(script['Script ID']) + ':' + XNAT.utils.escapeXML(script['Script Label']) + '</option>';
                        eventsManager.scripts.push(script['Script ID']);
                    });
                    $scriptsMenu.html(options);
                }

            },
            error: function(request, status, error) {
                xmodal.message('Error', 'An error occurred retrieving system events: [' + status + '] ' + error);
            }
        });
    }

    function doAddEventHandler(xmodalObj) {
        var filterVar = {};
        var filterEle = $("select.filter, input.filter").filter(function() {
            return $(this).val() != ""
        });
        for (var i = 0; i < filterEle.length; i++) {
            filterVar[filterEle[i].name] = [];
            filterVar[filterEle[i].name].push($(filterEle[i]).val());
        }

        var data = {
            eventClass: xmodalObj.__modal.find('select.eventClass').val(),
            event: xmodalObj.__modal.find('select.event, input.event').filter(function() {
                return $(this).val() != ""
            }).val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val(),
            filters: filterVar
        };

        if (!data.event || data.event === '!' || !data.scriptId) {
            xmodal.message('Missing Information', 'Please select an <b>Event</b> <i>and</i> <b>Script</b> to create an <br>Event Handler.');
            return false;
        }
        var createEvent = new Object();
        createEvent.import = false;
        var events = [];
        events[0] = data;
        createEvent.eventHandlers = events;
        XNAT.app.eventsManager.eventHandlerData = data;

        var eventHandlerAjax = $.ajax({
            type: "PUT",
            url: serverRoot + '/data/projects/' + window.projectScope + '/automation/handlers?XNAT_CSRF=' + window.csrfToken,
            cache: false,
            async: true,
            data: JSON.stringify(createEvent),
            contentType: 'application/json'
        });
        eventHandlerAjax.done(function(data, textStatus, jqXHR) {
            if (typeof data !== 'undefined') {
                xmodal.message('Success', 'Your event handler was successfully added.', 'OK', {
                    action: function() {
                        initEventsTable();
                        if ($("#events_manage_table").length > 0) {
                            initEventsTable();
                        }
                        xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                        // Trigger automation uploader to reload handlers
                        XNAT.app.abu.getAutomationHandlers();
                    }
                });
            }
        });
        eventHandlerAjax.fail(function(data, textStatus, jqXHR) {
            xmodal.message('Error', 'An error occurred: [' + data.statusText + '] ' + data.responseText, 'Close', {
                action: function() {
                    xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                }
            });
        });
    }

    function doEditEventHandler(xmodalObj) {
        var filterVar = {};
        var filterEle = $("select.filter, input.filter").filter(function() {
            return $(this).val() != ""
        });
        for (var i = 0; i < filterEle.length; i++) {
            filterVar[filterEle[i].name] = [];
            filterVar[filterEle[i].name].push($(filterEle[i]).val());
        }

        var data = {
            eventClass: xmodalObj.__modal.find('select.eventClass').val(),
            event: xmodalObj.__modal.find('select.event, input.event').filter(function() {
                return $(this).val() != ""
            }).val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val(),
            filters: filterVar
        };

        if (!data.event || data.event === '!' || !data.scriptId) {
            xmodal.message('Missing Information', 'Please select an <b>Event</b> <i>and</i> <b>Script</b> to create an <br>Event Handler.');
            return false;
        }
        var importEvent = new Object();
        importEvent.import = false;
        var events = [];
        events[0] = data;
        importEvent.eventHandlers = events;
        XNAT.app.eventsManager.eventHandlerData = data;
        var eventHandlerAjax = $.ajax({
            type: "PUT",
            url: serverRoot + '/data/automation/triggers/' + XNAT.app.eventsManager.triggerId + "?XNAT_CSRF=" + window.csrfToken,
            cache: false,
            async: true,
            data: JSON.stringify(importEvent),
            contentType: 'application/json'
        });
        eventHandlerAjax.done(function(data, textStatus, jqXHR) {
            if (typeof data !== 'undefined') {
                xmodal.message('Success', 'Your event handler was successfully updated.', 'OK', {
                    action: function() {
                        initEventsTable();
                        if ($("#events_manage_table").length > 0) {
                            initEventsTable();
                        }
                        xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                        // Trigger automation uploader to reload handlers
                        XNAT.app.abu.getAutomationHandlers();
                    }
                });
            }
        });
        eventHandlerAjax.fail(function(data, textStatus, jqXHR) {
            xmodal.message('Error', 'An error occurred: [' + data.statusText + '] ' + data.responseText, 'Close', {
                action: function() {
                    xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                }
            });
        });
    }

    function addEventHandler() {
        initEventsMenu();
    }

    function editEventHandler(triggerId) {
        initEventsMenu(true, triggerId);
    }

    function doDeleteTrigger(triggerId) {
        var url = serverRoot + '/data/automation/triggers/' + triggerId + "?XNAT_CSRF=" + window.csrfToken;
        if (window.jsdebug) console.log(url);
        jQuery.ajax({
            type: 'DELETE',
            url: url,
            cache: false,
            success: function() {
                var configScope;
                if (typeof XNAT.app.abu.uploaderConfig !== 'undefined') {
                    for (var i = XNAT.app.abu.uploaderConfig.length - 1; i >= 0; i--) {
                        var thisConfig = XNAT.app.abu.uploaderConfig[i];
                        if (typeof thisConfig == 'undefined') {
                            continue;
                        }
                        if (thisConfig.eventTriggerId == triggerId) {
                            configScope = thisConfig.eventScope;
                            XNAT.app.abu.uploaderConfig.splice(i, 1);
                        }
                    }
                    if (typeof configScope !== 'undefined') {
                        XNAT.app.abu.putUploaderConfiguration(configScope, false);
                    }
                }
                xmodal.message('Success', 'The event handler was successfully deleted.', 'OK', {
                    action: function() {
                        initEventsTable();
                        xmodal.closeAll()
                    }
                });
            },
            error: function(request, status, error) {
                xmodal.message('Error', 'An error occurred: [' + status + '] ' + error, 'Close', {
                    action: function() {
                        xmodal.closeAll()
                    }
                });
            }
        });
    }

    function deleteEventHandler(triggerId) {
        xmodal.confirm({
            title: 'Delete Event Handler?',
            content: 'Are you sure you want to delete the handler: <br><br><b>' + triggerId + '</b>?<br><br>Only the Event Handler will be deleted. The associated Script will still be available for use.',
            width: 560,
            height: 240,
            okLabel: 'Delete',
            okClose: false, // don't close yet
            cancelLabel: 'Cancel',
            okAction: function() {
                doDeleteTrigger(triggerId);
            },
            cancelAction: function() {
                xmodal.message('Delete event handler cancelled', 'The event handler was not deleted.', 'Close');
            }
        });
    }

    function getEventClassDisplayValueFromHandlers(_handlers, eventHandler) {
        var classPart = eventHandler.srcEventClass.substring(eventHandler.srcEventClass.lastIndexOf('.'));
        var matches = 0;
        for (var i = 0; i < _handlers.length; i++) {
            var classVal = _handlers[i].srcEventClass;
            if (typeof classVal !== 'undefined' && classVal.endsWith(classPart) && !(eventHandler.srcEventClass == _handlers[i].srcEventClass)) {
                matches++;
            }
        }
        return (matches < 1) ? classPart.substring(1) : eventHandler.srcEventClass;
    }

    function getEventClassDisplayValue(ins) {
        var classPart = ins.substring(ins.lastIndexOf('.'));
        var displayName;
        var matches = 0;
        for (var i = 0; i < XNAT.app.eventsManager.eventClasses.length; i++) {
            var classVal = XNAT.app.eventsManager.eventClasses[i].class;
            if (typeof classVal !== 'undefined' && classVal.endsWith(classPart)) {
                matches++;
                displayName = XNAT.app.eventsManager.eventClasses[i].displayName;
            }
        }
        return (typeof displayName !== 'undefined' && displayName !== ins) ? displayName : (matches <= 1) ? classPart.substring(1) : ins;
    }

    function getProjectsForUser() {
        var projects = $.ajax({
            type: "GET",
            url: serverRoot + '/xapi/users/projects',
            cache: false,
            async: true
        });
        projects.done(function(data, textStatus, jqXHR) {
            if (typeof data !== 'undefined') {
                var x = document.getElementById("prjOptions");
                var i = 0;
                while (i != data.length) {
					if(window.projectScope!==data[i])
					{
						var option = document.createElement("option");
						option.text = data[i];
						option.value = data[i];
						x.add(option);
					}
                    i++;
                }
            }
        });
    }
	

    function importEventHandler() {
        var importModalOpts = {
            width: 840,
            height: 480,
            id: 'xmodal-import-events',
            title: "Import Event Handlers",
            content: "<div id='projectList'/><div id='importModalDiv'></div>",
            buttons: {
				importEvents: {
                    classes: 'import-events',
                    label: 'Import Events',
                    isDefault: true,
                    disabled: true,
                    action: function(obj) {
                        importEventsHandler();
                    }
                },
                close: {
                    label: 'Cancel',
                    isDefault: false
                }                
            }
        };
        //getProjectsForUser();
        xmodal.open(importModalOpts);
        $('#projectList').html('<table><tr><td><b>Select Project to Import event Handlers:</b></td><td><select class="prjOptions" id="prjOptions" ></select></td></tr></table>');
        $('#importModalDiv').html(
            '<p id="no_import_event_handlers" style="display:none;padding:20px;">There are no event handlers currently configured for this project or events with same details already exists.</p>' +
            '<div id="import_events_manage_table" class="xnat-table" style="display:table;width:100%">' +
            '<dl class="header">' +
            '<dd></dd>' +
            '<dd></dd>' +
            '<dd class="col1">Event</dd>' +
            '<dd class="col2">Script</dd>' +
            '<dd class="col3">Description</dd>' +
            '</dl>' +
            '</div>'
        );
		
		var $selectProjectDropdown = $('#prjOptions');
		var option = new Option("Select", "Select");
		$selectProjectDropdown.append($(option));
		getProjectsForUser();
		$selectProjectDropdown.on('change', function(){
			initImportEventsTable($selectProjectDropdown.val());
		});
        $('#importModalDiv').hide();
    }
	
	
	
    function initImportEventsTable(projectName) {
        if (projectName != "Select") {
            events_manage_table = $('#import_events_manage_table');
            // Now get all the data and stick it in the table.
            XNAT.xhr.getJSON({
                url: XNAT.url.restUrl('/data/projects/' + projectName + '/automation/handlers?targetProjectName=' + window.projectScope),
                success: function(response) {
                    var eventRows = '',
                        _handlers = (response.ResultSet) ? response.ResultSet.Result || [] : [];

                    if (_handlers.length) {
                        $('#no_import_event_handlers').hide();
                        handlersRendered = true;
                        eventRows +=
                            '<dl class="header">' +
                            '<dl>' +
                            '<dd style="margin-left:0px;"></dd>' +
                            '<dd class="col0"><input id="master_check" type="checkbox" style="width:15px;" onclick="checkAll()"/></dd>' +
                            '<dd class="col1">Event</dd>' +
                            '<dd class="col2">Script</dd>' +
                            '<dd class="col3">Description</dd>' +
                            '</dl>' +
                            '</dl>';
                        forEach(_handlers, function(eventHandler) {
                            var _event_id = XNAT.utils.escapeXML(eventHandler['event']);
                            eventRows += '<dl class="item" id="event_' + eventHandler.id + '">' +
                                '<dd id="id" style="visibility:hidden;width:0px;margin-left:0px;">' + eventHandler.id + '</dd>' +
                                '<dd class="col0"><input type="checkbox" class="checkbox" name="event_' + eventHandler.id + '" style="width:15px;" onclick="onCheckBoxClick(this.checked)" /></dd>' +
                                '<dd class="col1" id="event">' + _event_id + '</dd>' +
                                '<dd class="col2" id="scriptId">' + XNAT.utils.escapeXML(eventHandler.scriptId) + '</dd>' +
                                '<dd class="col3" id="description">' + XNAT.utils.escapeXML(eventHandler.description) + '</dd>' +
                                '<dd class="colC" id="eventClass">' + '<b>Event Class: </b> ' + getEventClassDisplayValueFromHandlers(_handlers, eventHandler) + '</dd>' +
                                '<dd class="colC" id="filters" style="margin-left:40px;">' + '<b>Event Filters: </b> ' + XNAT.utils.escapeXML(eventHandler.eventFilters) + '</dd>' +
                                '</dl>';
                        });
                        events_manage_table.html(eventRows);
                        events_manage_table.show();
                        $('#importModalDiv').show();
                    } else {
                        $('#importModalDiv').show();
                        events_manage_table.hide();
                        $('#no_import_event_handlers').show();
                    }
                },
                error: function(request, status, error) {
                    if ($("#err_event_handlers").length < 1) {
                        $("#events_list").prepend('<p id="err_event_handlers" style="padding:20px;">' +
                            'An error occurred retrieving event handlers for this project: [' + status + '] ' + error +
                            '.  This may mean that your account does not have permission to edit event handlers for this project.</p>').show();
                        $("#manage_event_handlers").prop('disabled', true);
                    }
                },
                complete: function() {
                    //$('#accordion').accordion('refresh');
                }
            });
        } else {
            xmodal.message('Error', 'Please select valid project name.', 'Close');
        }
    }

    

    function importEventsHandler() {
        var importEvent = false;
        events = [];
        var checkboxes = $('.checkbox:checkbox:checked');
        if (checkboxes != null) {
            var i = 0;
            while (i < checkboxes.length) {
                if (checkboxes[i].checked) {
                    events[events.length] = document.getElementById(checkboxes[i].name);
                    importEvent = true;
                }
                i++;
            }
        }
        if (importEvent)
            initImportEventsMenu(events);
        else {
            var message = 'Please select events which needs to be imported.';
            if (document.getElementById('prjOptions').value == 'Select') {
                message = 'Please select a project from which you want to import events.';
            }
            xmodal.message('Error', message, 'OK');
        }
    }

    function initImportEventsMenu(events) {
        var projectName = document.getElementById('prjOptions').value;
        var eventHandlers = {};
        if (events.length > 0) {
            var eventObjs = [];
            for (var i = 0; i < events.length; i++) {
                if (events[i].className === 'item') {
                    var event = events[i];
                    var eventObj = {};
                    for (var j = 0; j < event.childNodes.length; j++) {
                        var rec = event.childNodes[j];
                        switch (rec.id) {
                            case 'id':
                                eventObj.id = rec.innerHTML;
                                break;

                            case 'event':
                                eventObj.event = rec.innerText.replace('Executed script ', '').trim();
                                break;

                            case 'scriptId':
                                eventObj.scriptId = rec.innerText;
                                break;

                            case 'eventClass':
                                var eventClass = rec.innerText.replace('Event Class: ', '').trim();
                                switch (eventClass) {
                                    case "ScriptLaunchRequestEvent":
                                        eventObj.eventClass = "org.nrg.xnat.event.entities." + eventClass;
                                        break;

                                    case "WorkflowStatusEvent":
                                        eventObj.eventClass = "org.nrg.xft.event.entities." + eventClass;
                                        break;
                                }
                                break;

                            case 'description':
                                eventObj.description = rec.innerText;
                                break;

                            case 'filters':
                                var obj = {};
                                var val = [];
                                if (rec.innerText.replace('Event Filters: ', '').trim() !== "{}") {
                                    var filt = rec.innerText.replace('Event Filters: ', '').replace('{', "").replace('[', "").replace(']', "").replace('}', "").split('=');
                                    val[val.length] = filt[1];
                                    obj.status = val;
                                }
                                eventObj.filters = obj;
                                break;
                        }
                    }
                    eventObjs[eventObjs.length] = eventObj;
                }
            }
            eventHandlers.import = true;
            eventHandlers.sourceProjectId = projectName;
            eventHandlers.eventHandlers = eventObjs;
        }


        var eventHandlerAjax = $.ajax({
            type: "PUT",
            url: serverRoot + '/data/projects/' + window.projectScope + '/automation/handlers?XNAT_CSRF=' + window.csrfToken,
            cache: false,
            async: true,
            data: JSON.stringify(eventHandlers),
            contentType: 'application/json'
        });
        eventHandlerAjax.done(function(data, textStatus, jqXHR) {
            if (typeof data !== 'undefined') {
                xmodal.message('Success', 'Event handlers successfully imported.', 'OK', {
                    action: function() {
                        initEventsTable();
                        xmodal.closeAll();
                        XNAT.app.abu.getAutomationHandlers();
                    }
                });
            }
        });
        eventHandlerAjax.fail(function(data, textStatus, jqXHR) {
            xmodal.message('Error', 'An error occurred: [' + data.statusText + '] ' + data.responseText, 'Close', {
                action: function() {
                    xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-import-events'));
                }
            });
        });

    }
	// initialize scripts menu and table
    initScriptsMenu();
    if (!handlersRendered) {
        initEventsTable();
    }
	
	// removed inline onclick attributes:
    $events_table.on('click', 'button.delete-handler', function() {
        deleteEventHandler($(this).data('handler'))
    });
    $events_table.on('click', 'button.edit-handler', function() {
        editEventHandler($(this).data('handler'))
    });
    $events_table.on('click', 'button.configure-uploader-handler', function() {
        XNAT.app.abu.configureUploaderForEventHandler($(this).data('handler'), $(this).data('event'), 'prj')
    });

    // *javascript* event handler for adding an XNAT event handler (got it?)
    $manage_event_handlers.on('click', addEventHandler);
    $import_event_handlers.on('click', importEventHandler);
});

function checkAll() {
	var masterChkbox = document.getElementById('master_check');
	var checkboxes = document.getElementsByClassName('checkbox');
	if (checkboxes != null) {
		if (masterChkbox.checked) {
			$('.import-events').prop('disabled', false);
			$('.import-events').removeClass('disabled');
		} else {
			$('.import-events').prop('disabled', true);
			$('.import-events').addClass('disabled');
		}

		var i = 0;
		while (i < checkboxes.length) {
			checkboxes[i].checked = masterChkbox.checked;
			i++;
		}
	}
}

function onCheckBoxClick(checked) {
	if (checked) {
		$('.import-events').prop('disabled', false);
		$('.import-events').removeClass('disabled');
	} else {
		var checkboxes = $('.checkbox:checkbox:checked');
		if (checkboxes.length == 0) {
			$('.import-events').prop('disabled', true);
			$('.import-events').addClass('disabled');
		}
	}
}