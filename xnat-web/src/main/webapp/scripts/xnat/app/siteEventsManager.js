/*
 * web: siteEventsManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * functions for XNAT site Event Handlers
 */

XNAT.app.siteEventsManager = {};
XNAT.app.siteEventsManager.events = [];
XNAT.app.siteEventsManager.scripts = [];
XNAT.app.siteEventsManager.handlers = [];

$(function(){

    var siteEventsManager = XNAT.app.siteEventsManager,
        xhr = XNAT.xhr;

    //var systemEvents = []; // ?
    //var systemScripts = []; // ?

    var $events_table = $('#events_table'),
        $no_events_defined = $('#no_events_defined'),
        $no_event_handlers = $('#no_event_handlers'),
        $add_event_handler = $('#add_event_handler'),
        $manage_event_handlers = $('#manage_event_handlers'),
        handlersRendered = false,
        hasEvents = false;


    function initHandlersTable(doEdit){

        if (doEdit) {
            var events_manage_table = $('#events_table');
        }

        // hide stuff
        $((doEdit) ? events_manage_table : $events_table).hide();
        $no_event_handlers.hide();

        // Now get all the data and stick it in the table.
        xhr.getJSON({
            url: XNAT.url.restUrl('/data/automation/handlers'),
            success: function( response ){

                var eventRows = '',
                    _handlers = (response.ResultSet) ? response.ResultSet.Result || [] : [];

                // reset handlers array
                siteEventsManager.handlers = [];

                if(_handlers.length) {
                    handlersRendered = true;
                    eventRows +=
                    '<dl class="header">' +
                         '<dl>' +
                              '<dd class="col1">Event</dd>' +
                              '<dd class="col2">Script</dd>' +
                              '<dd class="col3">Description</dd>' +
                            //((doEdit) ?
                              '<dd class="col4"></dd>' +
                            //  '<dd class="col5"></dd>' +
			    // : '') +
                         '</dl>' +
                    '</dl>';
                    forEach(_handlers, function(eventHandler){
                        var _event_id = eventHandler['event'];
                        siteEventsManager.handlers.push(_event_id);
                        eventRows += '<dl class="item">' +
                            '<dd class="col1">' + _event_id + '</dd>' +
                            '<dd class="col2">' + eventHandler.scriptId + '</dd>' +
                            '<dd class="col3">' + eventHandler.description + '</dd>' +
                            //((doEdit) ?
                                '<dd class="col4" style="text-align: center;">' +
                                '<button href="javascript:" class="delete-handler event-handler-button" ' +
                                'data-event="' + _event_id + '" ' +
                                'data-handler="' + eventHandler.triggerId + '" title="Delete handler for event ' + _event_id + '">delete</button>' +
                                '</dd>' +
                                '<dd class="col5" style="text-align: center;">' +
                                '<button href="javascript:" class="configure-uploader-handler event-handler-button" ' +
                                'data-event="' + _event_id + '" ' +
                                'data-handler=' + eventHandler.triggerId + ' title="Configure uploader for event ' + _event_id + '">configure uploader</button>' +
                                '</dd>' +
                            //: '') +
                            '<dd class="colC">' + '<b>Event Class: </b> ' + getEventClassDisplayValueFromHandlers(_handlers, eventHandler) + '</dd>' +
                            '<dd class="colC">' + '<b>Event Filters: </b> ' + eventHandler.eventFilters + '</dd>' +
                            '</dl>';

                    });
                    //$((doEdit) ? events_manage_table : $events_table).find('tbody').html(eventRows);
                    $((doEdit) ? events_manage_table : $events_table).html(eventRows);
                    $((doEdit) ? events_manage_table : $events_table).show();
                }
                else {
                    $no_event_handlers.show();
                }
            },
            error: function( request, status, error ){
                if ($("#err_event_handlers").length<1) {
                        $("#events_list").prepend('<p id="err_event_handlers" style="padding:20px;">' +
                                        'An error occurred retrieving event handlers for this project: [' + status + '] ' + error +  
                                        '.  This may mean that your account does not have permission to edit site event handlers.</p>').show();
                        $("#add_event_handler").prop('disabled', true);
                }
            },
            complete: function(){
                //$('#accordion').accordion('refresh');
            }
        });
    }
    initHandlersTable(false);
    siteEventsManager.initHandlersTable = initHandlersTable;

    function manageEventHandlers(){

			var manageModalOpts = {
				width: 840,
				height: 480,
				id: 'xmodal-manage-events',
				title: "Manage Event Handlers",
				content: '' +
                    '<p id="no_events_defined" style="display:none;padding:20px;">There are no events currently defined for this site.</p>' +
                    '<p id="no_event_handlers" style="display:none;padding:20px;">There are no event handlers currently configured for this project.</p>' +
                    '<div id="events_manage_table" class="xnat-table" style="display:table;width:100%">' +
                    '<dl class="header">' +
                        '<dl>' +
                            '<dd class="col1">Event</dd>' +
                            '<dd class="col2">Script</dd>' +
                            '<dd class="col3">Description</dd>' +
                            '<dd class="col4"></dd>' +
                            '<dd class="col5"></dd>' +
                            '</dl>' +
                        '</dl>' +
                    '</div>',
                buttons: {
                    close: {
                        label: 'Done',
                        isDefault: true
                    },
                    addEvents: {
                        label: 'Add Event Handler',
                        action: function( obj ){
                            addEventHandler();
                        }
                    }
                }
			};
			xmodal.open(manageModalOpts);
           initHandlersTable(true);
           $("#events_manage_table").on('click', 'button.delete-handler', function(){
               deleteEventHandler($(this).data('handler'), $(this).data('event'))
           });
           $("#events_manage_table").on('click', 'button.configure-uploader-handler', function(){
               XNAT.app.abu.configureUploaderForEventHandler($(this).data('handler'), $(this).data('event'), 'site')
           });

    }

    function initEvents(){
        siteEventsManager.events = []; // reset array
        if (typeof siteEventsManager.eventClasses === 'undefined') {
            var eventClassesAjax = xhr.getJSON(XNAT.url.restUrl('/xapi/eventHandlers/automationEventClasses'));
            eventClassesAjax.done(function(data, textStatus, jqXHR){
                if (typeof data !== 'undefined') {
                    siteEventsManager.eventClasses = data;
                    addEventHandlerDialog();
                }
            });
            eventClassesAjax.fail(function(data, textStatus, jqXHR){
                xmodal.message('Error', 'An error occurred retrieving system events: ' + textStatus);
            });
        }
        else {
            addEventHandlerDialog();
        }
    }


    function getEventClassDisplayValueFromHandlers(_handlers, eventHandler){
        var classPart = eventHandler.srcEventClass.substring(eventHandler.srcEventClass.lastIndexOf('.'));
        var matches=0;
        for (var i=0; i<_handlers.length; i++) {
            var classVal = _handlers[i].srcEventClass;
            if (typeof classVal !== 'undefined' && classVal.endsWith(classPart) && !(eventHandler.srcEventClass == _handlers[i].srcEventClass)) {
                matches++;
            }
        }
        return (matches<1) ? classPart.substring(1) : eventHandler.srcEventClass;
    }

    function getEventClassDisplayValue(ins){
        var classPart = ins.substring(ins.lastIndexOf('.'));
        var displayName;
        var matches = 0;
        siteEventsManager.eventClasses.forEach(function(evtCls){
            var classVal = evtCls.class;
            if (typeof classVal !== 'undefined' && classVal.endsWith(classPart)) {
                matches++;
                displayName = evtCls.displayName;
            }
        });
        return (typeof displayName !== 'undefined' && displayName !== ins) ? displayName : (matches <= 1) ? classPart.substring(1) : ins;
    }

    function addEventHandlerDialog(){

        return xmodal.open({
            title: 'Add Event Handler',
            template: $('#addEventHandler'),
            width: 600,
            height: 350,
            overflow: 'auto',
            beforeShow: function(obj){
                // initialize the menus INSIDE the dialog

                // 'Event Type' menu
                var $eventTypeMenu = obj.$modal.find('#select_eventClass');
                $eventTypeMenu.empty().append('<option></option>');

                // 'Event ID' menu
                var $eventIdMenu = obj.$modal.find('#select_event');

                // nice clean Array.forEach()
                siteEventsManager.eventClasses.forEach(function(evtCls){
                    $eventTypeMenu.append('<option value="' + evtCls.class + '">' + getEventClassDisplayValue(evtCls.class) + '</option>');
                });

                $eventTypeMenu.on('change', function(){
                    updateEventIdSelect($eventIdMenu);
                });

                updateEventIdSelect($eventIdMenu);

                var $scriptsMenu = obj.$modal.find('#select_scriptId');
                initScriptsMenu($scriptsMenu);

            },
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

    }

    function updateEventIdSelect($eventIdMenu){

        var filterableHtml = '';
        var $filterRow = $('#filterRow').hide();
        var $filterDiv = $('#filterDiv').html(filterableHtml);

        $eventIdMenu.empty()
                    .append('<option></option>')
                    .addClass('disabled')
                    .prop('disabled', true);

        for (var i = 0; i < siteEventsManager.eventClasses.length; i++) {
            if ($('#select_eventClass').val() == siteEventsManager.eventClasses[i].class) {
                var eventIds = siteEventsManager.eventClasses[i].eventIds;
                if (eventIds && eventIds.length) {
                    eventIds.forEach(function(eventId){
                        $eventIdMenu.append('<option value="' + eventId + '">' + eventId + '</option>');
                    });
                }
                var filterableFields = siteEventsManager.eventClasses[i].filterableFields;
                $filterRow.hide();
                if (typeof filterableFields !== 'undefined') {
                    for (var filterable in filterableFields) {
                        if (!filterableFields.hasOwnProperty(filterable)) continue;
                        var filterableInfo = filterableFields[filterable];
                        var filterableVals = filterableInfo["filterValues"];
                        var filterableRequired = filterableInfo["filterRequired"];
                        var filterableDefault = filterableInfo["defaultValue"];
                        if (typeof filterableRequired !== 'undefined' && !filterableRequired) {
                            filterableHtml += ('<option value="">&lt;NONE&gt;</option>');
                        }
                        if (typeof filterableVals !== 'undefined' && filterableVals.length > 0) {
                            filterableHtml += ('<div style="width:100%;margin-top:5px;margin-bottom:5px">' + filterable + ' &nbsp;<select id="filter_sel_' + filterable + '" name="' + filterable + '" class="filter">');
                            for (var i = 0; i < filterableVals.length; i++) {
                                if (typeof filterableDefault !== 'undefined' && filterableDefault == filterableVals[i]) {
                                    filterableHtml += ('<option value="' + filterableVals[i] + '" selected>' + filterableVals[i] + '</option>');
                                }
                                else {
                                    filterableHtml += ('<option value="' + filterableVals[i] + '">' + filterableVals[i] + '</option>');
                                }
                            }
                            filterableHtml += ('</select> ' +
                                '<input type="text" id="filter_input_' + filterable + '" name="' + filterable + '" class="filter" style="display:none" size="15"/>' +
                                '<button class="customButton">Custom Value</button>' +
                                '</div>');
                        }
                    }
                }
                if (filterableHtml.length > 0) {
                    $filterRow.css('display', 'table-row');
                    $filterDiv.html(filterableHtml);
                } else {
                    $filterDiv.html("");
		}
                $eventIdMenu.removeClass('disabled').prop('disabled', false);
                break;
            }
        }
        $(".customButton").each(function(){
            // TODO: ???????
            var eventObject = $._data(this, 'events');
            if (typeof eventObject == 'undefined' || typeof eventObject.click == 'undefined') {
                $(this).click(function(event){
                    customInputToggle(event.target);
                });
            }
        });
        $(".customButton").css('margin-left', '5px');

    }

    function customInputToggle(ele){
         $(ele).parent().find("input, select").each(function() {
    		if ($(this).css('display') == 'none') {
                $(this).css('display','inline');
    		} else {
                $(this).css('display','none');
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

    function initScriptsMenu(menu){
        //if (!hasEvents) { return; }
        siteEventsManager.scripts = []; // reset array
        return xhr.getJSON({
            url: XNAT.url.restUrl('/data/automation/scripts'),
            success: function( response ){

                var scripts = response.ResultSet.Result || [],
                    $scriptsMenu = menu ? $$(menu) : $('#select_scriptId'),
                    options = '<option></option>';

                if (!scripts.length){
                    options += '<option value="!" disabled>(no scripts available)</option>';
                    $scriptsMenu.html(options).prop('disabled',true);
                }
                else {
                    forEach(scripts, function(script){
                        options += '<option title="' + script['Description'] + '" value="' + script['Script ID'] + '">' + script['Script ID'] + ':' + script['Script Label'] + '</option>';
                        siteEventsManager.scripts.push(script['Script ID']);
                    });
                }
                $scriptsMenu.html(options);
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving system events: [' + status + '] ' + error);
            }
        });
    }

    // initialize menus and table
    // initScriptsMenu();
    if (!handlersRendered) {
        initHandlersTable(false);
    }

    function doAddEventHandler(xmodalObj){

        var filterVar = {};
        var filterEle = $("select.filter, input.filter").filter(function(){ return $(this).val() != "" });
        for (var i = 0; i < filterEle.length; i++) {
            filterVar[filterEle[i].name] = [];
            filterVar[filterEle[i].name].push($(filterEle[i]).val());
        }

        var data = {
            eventClass: xmodalObj.__modal.find('select.eventClass').val(),
            event: xmodalObj.__modal.find('select.event, input.event').filter(function(){ return $(this).val() != "" })
                            .val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val(),
            filters: filterVar
        };

        // TODO: Should we let them name the trigger? Is that worthwhile? (yes)
        // var url = serverRoot + "/data/projects/" + window.projectScope + "/automation/events/" + triggerId + "?XNAT_CSRF=$!XNAT_CSRF";
        //var url = serverRoot + "/data/projects/" + window.projectScope + "/automation/events?XNAT_CSRF=$!XNAT_CSRF";

        if (!data.event || data.event === '!' || !data.scriptId) {
            xmodal.message('Missing Information', 'Please select an <b>Event</b> <i>and</i> <b>Script</b> to create an <br>Event Handler.');
            return false;
        }
        siteEventsManager.eventHandlerData = data;

        var eventHandlerAjax = xhr.putJSON({
            url: XNAT.url.csrfUrl('/data/automation/handlers'),
            data: JSON.stringify(data)
        });
        eventHandlerAjax.done(function(data, textStatus, jqXHR){
            if (typeof data !== 'undefined') {
                xmodal.message('Success', 'Your event handler was successfully added.', 'OK', {
                        action: function(){
                            initHandlersTable(false);
                            if ($("#events_manage_table").length > 0) {
                                initHandlersTable(true);
                            }
                            xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                            // Trigger automation uploader to reload handlers
                            XNAT.app.abu.getAutomationHandlers();
                        }
                    }
                );
            }
        });
        eventHandlerAjax.fail(function(data, textStatus, jqXHR){
            xmodal.message('Error', 'An error occurred: [' + data.statusText + '] ' + data.responseText, 'Close', {
                action: function(){
                    xmodal.closeAll($(xmodal.dialog.open), $('#xmodal-manage-events'));
                }
            });
        });

    }

    function addEventHandler(){
        // initScriptsMenu();
        initEvents();
    }

    function doDeleteHandler( triggerId ){
        var url = XNAT.url.csrfUrl('/data/automation/triggers/'+triggerId);
        if (window.jsdebug) console.log(url);
        //xhr.delete({
        $.ajax({
            type: 'DELETE',
            url: url,
            success: function(){
               var configScope;
                if (typeof XNAT.app.abu.uploaderConfig !== 'undefined') {
                    for (var i=XNAT.app.abu.uploaderConfig.length -1; i >= 0; i--) {
                        var thisConfig = XNAT.app.abu.uploaderConfig[i];
                        if (typeof thisConfig == 'undefined') {
                            continue;
                        }
                        if (thisConfig.eventTriggerId == triggerId) {
                            configScope = thisConfig.eventScope;
                            XNAT.app.abu.uploaderConfig.splice(i,1);
                        }
                    }
                    if (typeof configScope !== 'undefined') {
                        XNAT.app.abu.putUploaderConfiguration(configScope,false);
                    }
                }
                xmodal.message('Success', 'The event handler was successfully deleted.', 'OK', {
                    action: function(){
                        initHandlersTable();
                        xmodal.closeAll()
                    }
                });
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred: [' + status + '] ' + error, 'Close', {
                    action: function(){
                        xmodal.closeAll()
                    }
                });
            }
        });
    }

    function deleteEventHandler( triggerId, event ){
        xmodal.confirm({
            title: 'Delete Event Handler?',
            content: 'Are you sure you want to delete the handler: <br><br><b>' + triggerId + '</b>?<br><br>Only the Event Handler will be deleted. The associated Script will still be available for use.',
            width: 560,
            height: 220,
            okLabel: 'Delete',
            okClose: false, // don't close yet
            cancelLabel: 'Cancel',
            okAction: function(){
                doDeleteHandler(triggerId);
            },
            cancelAction: function(){
                xmodal.message('Delete event handler cancelled', 'The event handler was not deleted.', 'Close');
            }
        });
    }

    // removed inline onclick attributes:
    $events_table.on('click', 'button.delete-handler', function(){
        deleteEventHandler($(this).data('handler'), $(this).data('event'));
    });
    $events_table.on('click', 'button.configure-uploader-handler', function(){
        XNAT.app.abu.configureUploaderForEventHandler($(this).data('handler'), $(this).data('event'), 'site')
    });

    // *javascript* event handler for adding an XNAT event handler (got it?)
    $add_event_handler.on('click', addEventHandler);
    $manage_event_handlers.on('click', manageEventHandlers);

    XNAT.app.siteEventsManager = siteEventsManager;

});
