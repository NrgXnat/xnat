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

$(function(){

    var eventsManager = XNAT.app.eventsManager,
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

    function initEventsTable(doEdit){

        if (doEdit) {
            var events_manage_table = $('#events_manage_table');
        }

        // hide stuff
        $((doEdit) ? events_manage_table : $events_table).hide();
        $no_event_handlers.hide();

        // Now get all the data and stick it in the table.
        xhr.getJSON({
            url: XNAT.url.restUrl('/data/projects/' + window.projectScope + '/automation/handlers'),
            success: function( response ){

                var eventRows = '',
                    _handlers = (response.ResultSet) ? response.ResultSet.Result || [] : [];

                // reset handlers array
                eventsManager.handlers = [];

                if(_handlers.length) {
                    handlersRendered = true;
                    eventRows += 
                    '<dl class="header">' + 
                         '<dl>' +
                              '<dd class="col1">Event</dd>' +
                              '<dd class="col2">Script</dd>' +
                              '<dd class="col3">Description</dd>' +
                            ((doEdit) ?
                              '<dd class="col4"></dd>' +
                              '<dd class="col5"></dd>' 
                            : '') +
                         '</dl>' +
                    '</dl>';
                    forEach(_handlers, function(eventHandler){
                        var _event_id = eventHandler['event'];
                        eventsManager.handlers.push(_event_id);
                        /*
                        eventRows += '<tr class="highlight">' +
                            '<td class="event-id">' + _event_id + '</td>' +
                            '<td class="script-id">' + eventHandler.scriptId + '</td>' +
                            '<td class="description">' + eventHandler.description + '</td>' +
                            ((doEdit) ?
                                '<td style="text-align: center;">' +
                                '<a href="javascript:" class="delete-handler" ' +
                                'data-handler="' + eventHandler.triggerId + '" title="Delete handler for event ' + _event_id + '">delete</a>' +
                                '</td>' + 
                                '<td style="text-align: center;">' +
                                '<a href="javascript:" class="configure-uploader-handler" ' +
                                'data-handler="' + eventHandler.event + '" title="Delete handler for event ' + _event_id + '">configure uploader</a>' +
                                '</td>' 
                            : '') +
                            '</tr>';
                        */
                        eventRows += '<dl class="item">' +
                            '<dd class="col1">' + _event_id + '</dd>' +
                            '<dd class="col2">' + eventHandler.scriptId + '</dd>' +
                            '<dd class="col3">' + eventHandler.description + '</dd>' +
                            ((doEdit) ?
                                '<dd class="col4" style="text-align: center;">' +
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
                                '</dd>' 
                            : '') +
                            '<dd class="colC">' + '<b>Event Class: </b> ' + getEventClassDisplayValueFromHandlers(_handlers, eventHandler) + '</dd>' +
                            '<dd class="colC">' + '<b>Event Filters: </b> ' + eventHandler.eventFilters + '</dd>' +
                            '</dl>';
                    });
                    //$((doEdit) ? events_manage_table : $events_table).find('tbody').html(eventRows);
                    $((doEdit) ? events_manage_table : $events_table).html(eventRows);
                    $((doEdit) ? events_manage_table : $events_table).show();
                }
                else {
                    if (!hasEvents) {
                        $add_event_handler.prop('disabled',true);
                        $no_event_handlers.show();
                    }
                    else {
                        $no_event_handlers.show();
                    }
                }
            },
            error: function( request, status, error ){
                if ($("#err_event_handlers").length<1) {
                    $("#events_list").prepend('<p id="err_event_handlers" style="padding:20px;">' +
                        'An error occurred retrieving event handlers for this project: [' + status + '] ' + error +  
                        '.  This may mean that your account does not have permission to edit event handlers for this project.</p>').show();
                   $("#manage_event_handlers").prop('disabled', true);
                }
            },
            complete: function(){
                //$('#accordion').accordion('refresh');
            }
        });
    }

    function initEventsMenu(){
        eventsManager.events = []; // reset array

        $("#select_event").prop('disabled','disabled');
		if (typeof XNAT.app.eventsManager.eventClasses === 'undefined') {
			var eventClassesAjax = $.ajax({
				type : "GET",
		  		url:serverRoot+"/xapi/projects/" + window.projectScope + '/eventHandlers/automationEventClasses?XNAT_CSRF=' + window.csrfToken,
				cache: false,
				async: true,
				context: this,
				dataType: 'json'
			});
			eventClassesAjax.done( function( data, textStatus, jqXHR ) {
				if (typeof data !== 'undefined') {
					XNAT.app.eventsManager.eventClasses = data;
                    populateEventsMenu();
				}
			});
			eventClassesAjax.fail( function( data, textStatus, jqXHR ) {
                xmodal.message('Error', 'An error occurred retrieving system events: ' + textStatus);
			});
		} else {
            populateEventsMenu();
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
        var matches=0;
        for (var i=0; i<XNAT.app.eventsManager.eventClasses.length; i++) {
            var classVal = XNAT.app.eventsManager.eventClasses[i].class;
            if (typeof classVal !== 'undefined' && classVal.endsWith(classPart)) {
                matches++;
                displayName = XNAT.app.eventsManager.eventClasses[i].displayName;
            }
        }
        return (typeof displayName !== 'undefined' && displayName !== ins) ? displayName : (matches<=1) ? classPart.substring(1) : ins;
    }

    function populateEventsMenu(){

        $('#select_eventClass').empty().append('<option></option>');
        for (var i=0; i<XNAT.app.eventsManager.eventClasses.length; i++) {
            if (typeof XNAT.app.eventsManager.eventClasses[i].class !== 'undefined') {
                $('#select_eventClass').append('<option value="' + XNAT.app.eventsManager.eventClasses[i].class + '">' + getEventClassDisplayValue(XNAT.app.eventsManager.eventClasses[i].class) + '</option>');
            }
        }
        xmodal.open({
            title: 'Add Event Handler',
            template: $('#addEventHandler'),
            width: 600,
            height: 350,
            overflow: 'auto',
            beforeShow: function(obj){
                //menuInit(obj.$modal.find('select.event, select.scriptId'), null, 300);
                //obj.$modal.find('select.event, select.scriptId').chosen({
                //    width: '300px',
                //    disable_search_threshold: 6
                //});
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
        updateEventIdSelect();
        $('#select_eventClass').change(function(){
            updateEventIdSelect();
        });

    }

    function updateEventIdSelect(){
        $('#select_event').empty().append('<option></option>');
        $('#filterRow').css('display','none');
        $('#filterDiv').html(filterableHtml);
        for (var i=0; i<XNAT.app.eventsManager.eventClasses.length; i++) {
            if ($('#select_eventClass').val() == XNAT.app.eventsManager.eventClasses[i].class) {
                var eventIds = XNAT.app.eventsManager.eventClasses[i].eventIds;
                if (typeof eventIds !== 'undefined' && eventIds.length>0) {
                    for (var j=0; j<eventIds.length; j++) {
                        var eventId = eventIds[j];
                        $('#select_event').append('<option value="' + eventId + '">' + eventId + '</option>');
                    }
                }
                var filterableFields = XNAT.app.eventsManager.eventClasses[i].filterableFields;
                var filterableHtml = ""
                $('#filterRow').css('display','none');
                if (typeof filterableFields !== 'undefined') {
                    for (var filterable in filterableFields) {
                        if (!filterableFields.hasOwnProperty(filterable)) continue;
                        var filterableInfo = filterableFields[filterable];
                        var filterableVals = filterableInfo["filterValues"];
			var filterableRequired = filterableInfo["filterRequired"];
			var filterableDefault = filterableInfo["defaultValue"];
			if (typeof filterableRequired !== 'undefined' && !filterableRequired) {
                           filterableHtml = filterableHtml + '<option value="">&lt;NONE&gt;</option>';
			}
                        if (typeof filterableVals !== 'undefined' && filterableVals.length>0) {
                            filterableHtml = filterableHtml + '<div style="width:100%;margin-top:5px;margin-bottom:5px">' + filterable + ' &nbsp;<select id="filter_sel_' + filterable + '" name="' + filterable + '" class="filter">';
                            for (var i=0; i<filterableVals.length; i++) {
				if (typeof filterableDefault !== 'undefined' && filterableDefault == filterableVals[i]) {
                               		filterableHtml = filterableHtml + '<option value="' + filterableVals[i] + '" selected>' + filterableVals[i] + '</option>';
				} else {
                               		filterableHtml = filterableHtml + '<option value="' + filterableVals[i] + '">' + filterableVals[i] + '</option>';
				}
                            }
                            filterableHtml = filterableHtml + '</select> <input type="text" id="filter_input_' + filterable + '" name="' + filterable +
                                  '" class="filter" style="display:none" size="15"/> <button class="customButton">Custom Value</button></div>';
                        }
                    }
                }
                if (filterableHtml.length>0) {
                    $('#filterRow').css('display','table-row');
                    $('#filterDiv').html(filterableHtml);
                } else {
                    $('#filterDiv').html("");
		}
                $("#select_event").prop('disabled',false);
                break;
            }
        }
        $(".customButton").each(function(){
            var eventObject = $._data(this, 'events');
            if (typeof eventObject == 'undefined' || typeof eventObject.click == 'undefined') {
                $(this).click(function(event){
                    customInputToggle(event.target);
                });
            }
        });
        $(".customButton").css('margin-left','5px');
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

    function initScriptsMenu(){
        //if (!hasEvents) { return; }
        eventsManager.scripts = []; // reset array
        xhr.getJSON({
            url: XNAT.url.restUrl('/data/automation/scripts'),
            success: function( response ){

                var scripts = response.ResultSet.Result || [],
                    $scriptsMenu = $('#select_scriptId'),
                    options = '<option></option>';

                if (!scripts.length){
                    options += '<option value="!" disabled>(no scripts available)</option>';
                    $scriptsMenu.html(options).prop('disabled',true);
                }
                else {
                    forEach(scripts, function(script){
                        options += '<option title="' + script['Description'] + '" value="' + script['Script ID'] + '">' + script['Script ID'] + ':' + script['Script Label'] + '</option>';
                        eventsManager.scripts.push(script['Script ID']);
                    });
                    $scriptsMenu.html(options);
                }

            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving system events: [' + status + '] ' + error);
            }
        });
    }

    // initialize scripts menu and table
    initScriptsMenu();
    if (!handlersRendered){
        initEventsTable(false);
    }

    function doAddEventHandler( xmodalObj ){

	var filterVar = {};
	var filterEle = $("select.filter, input.filter").filter(function() { return $(this).val() != "" });
	for (var i=0; i<filterEle.length; i++) {
		filterVar[filterEle[i].name]=[];
		filterVar[filterEle[i].name].push($(filterEle[i]).val());
	}

        var data = {
            eventClass: xmodalObj.__modal.find('select.eventClass').val(),
            event: xmodalObj.__modal.find('select.event, input.event').filter(function() { return $(this).val() != "" }).val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val(),
            filters: filterVar
        };

        if (!data.event || data.event === '!' || !data.scriptId){
            xmodal.message('Missing Information','Please select an <b>Event</b> <i>and</i> <b>Script</b> to create an <br>Event Handler.');
            return false;
        }
	XNAT.app.eventsManager.eventHandlerData = data;

	var eventHandlerAjax = $.ajax({
		type : "PUT",
  		url:serverRoot+'/data/projects/' + window.projectScope + '/automation/handlers?XNAT_CSRF=' + window.csrfToken,
		cache: false,
		async: true,
		data: JSON.stringify(data),
		contentType: 'application/json'
	});
	eventHandlerAjax.done( function( data, textStatus, jqXHR ) {
		if (typeof data !== 'undefined') {
	                xmodal.message('Success', 'Your event handler was successfully added.', 'OK', { 
	                        action: function(){
	                            initEventsTable(false);
	                            if ($("#events_manage_table").length>0) {
	                                initEventsTable(true);
	                            }
	                            xmodal.closeAll($(xmodal.dialog.open),$('#xmodal-manage-events'));
	                            // Trigger automation uploader to reload handlers
	                            XNAT.app.abu.getAutomationHandlers();
	                        }  
	                    }
	                );
		}
	});
	eventHandlerAjax.fail( function( data, textStatus, jqXHR ) {
                xmodal.message('Error', 'An error occurred: [' + data.statusText + '] ' + data.responseText, 'Close', {
                    action: function(){
                        xmodal.closeAll($(xmodal.dialog.open),$('#xmodal-manage-events'));
                    }
                });
	});
    }

 
    function manageEventHandlers(){

			var manageModalOpts = {
				width: 840,  
				height: 480,  
				id: 'xmodal-manage-events',  
				title: "Manage Event Handlers",
				content: "<div id='manageModalDiv'></div>",
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
			$('#manageModalDiv').html(
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
                '</div>' 
           ); 
           initEventsTable(true);
           $("#events_manage_table").on('click', 'button.delete-handler', function(){
               deleteEventHandler($(this).data('handler'))
           });
           $("#events_manage_table").on('click', 'button.configure-uploader-handler', function(){
               XNAT.app.abu.configureUploaderForEventHandler($(this).data('handler'), $(this).data('event'), 'prj')
           });

    }


    function addEventHandler(){

        initEventsMenu();

    }

    function doDeleteTrigger(triggerId){
        var url = serverRoot+'/data/automation/triggers/' + triggerId + "?XNAT_CSRF=" + window.csrfToken;
        if (window.jsdebug) console.log(url);
        jQuery.ajax({
            type: 'DELETE',
            url: url,
            cache: false,
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
                        initEventsTable();
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

    function deleteEventHandler(triggerId){
        xmodal.confirm({
            title: 'Delete Event Handler?',
            content: 'Are you sure you want to delete the handler: <br><br><b>' + triggerId + '</b>?<br><br>Only the Event Handler will be deleted. The associated Script will still be available for use.',
            width: 560,
            height: 240,
            okLabel: 'Delete',
            okClose: false, // don't close yet
            cancelLabel: 'Cancel',
            okAction: function(){
                doDeleteTrigger(triggerId);
            },
            cancelAction: function(){
                xmodal.message('Delete event handler cancelled', 'The event handler was not deleted.', 'Close');
            }
        });
    }

    // removed inline onclick attributes:
    $events_table.on('click', 'a.delete-handler', function(){
        deleteEventHandler($(this).data('handler'))
    });

    // *javascript* event handler for adding an XNAT event handler (got it?)
    $manage_event_handlers.on('click', manageEventHandlers);

});

