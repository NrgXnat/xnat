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
        //handlersRendered = false,
        hasEvents = false;
    

    function initHandlersTable(doEdit){

        if (doEdit) {
            var events_manage_table = $('#events_manage_table');
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
                    //handlersRendered = true;
                    forEach(_handlers, function(eventHandler){
                        var _event_id = eventHandler['event'];
                        siteEventsManager.handlers.push(_event_id);
                        eventRows += '<tr class="highlight">' +
                            '<td class="event-id">' + _event_id + '</td>' +
                            '<td class="script-id">' + eventHandler.scriptId + '</td>' +
                            '<td class="description">' + eventHandler.description + '</td>' +
                            ((doEdit) ?
                            '<td style="text-align: center;">' +
                            '<a href="javascript:" class="delete-handler" ' +
                            'data-event="' + _event_id + '" ' +
                            'data-handler="' + eventHandler.triggerId + '" title="Delete handler for event ' + _event_id + '">delete</a>' +
                            '</td>' + 
                            '<td style="text-align: center;">' +
                            '<a href="javascript:" class="configure-uploader-handler" ' +
                            'data-event="' + _event_id + '" ' +
                            'data-handler="' + eventHandler.event + '" title="Configure uploader for event handler ' + _event_id + '">configure uploader</a>' +
                            '</td>' 
                             : '' ) +
                            '</tr>';
                    });
                    $((doEdit) ? events_manage_table : $events_table).find('tbody').html(eventRows);
                    $((doEdit) ? events_manage_table : $events_table).show();
                }
                else {
                    $no_event_handlers.show();
                }
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving event handlers: [' + status + '] ' + error);
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
			xModalOpenNew(manageModalOpts);
			$('#manageModalDiv').html(
               '<p id="no_events_defined" style="display:none;padding:20px;">There are no events currently defined for this site.</p>' +
                '<p id="no_event_handlers" style="display:none;padding:20px;">There are no event handlers currently configured for this project.</p>' +
                '<table id="events_manage_table" class="xnat-table" style="display:table;width:100%">' +
                    '<thead>' +
                    '<th>Event</th>' +
                    '<th>Script ID</th>' +
                    '<th>Description</th>' +
                    '<th></th>' +
                    '<th></th>' +
                    '</thead>' +
                    '<tbody>' +
                    '</tbody>' +
                '</table>' 
           ); 
           initHandlersTable(true);
           $("#events_manage_table").on('click', 'a.delete-handler', function(){
               deleteEventHandler($(this).data('handler'), $(this).data('event'))
           });
           $("#events_manage_table").on('click', 'a.configure-uploader-handler', function(){
               XNAT.app.abu.configureUploaderForEventHandler($(this).data('handler'),'site')
           });

    }

    function initEventsMenu(){
        siteEventsManager.events = []; // reset array
        return xhr.getJSON({
            url: XNAT.url.restUrl('/data/automation/events'),
            success: function( response ){

                var _events = (response.ResultSet) ? response.ResultSet.Result || [] : [],
                    availableEvents = [],
                    $eventsMenu = $('#select_event'),
                    options = '<option></option>';

                if (!_events.length){
                    options += '<option value="!" disabled>(no events defined)</option>';
                    //$eventsMenu.prop('disabled',true);
                    hasEvents = false;
                }
                else {
                    forEach(_events, function(event){

                        var _id = event['event_id'],
                            _label = event['event_label'];

                        if (siteEventsManager.handlers.indexOf(_id) === -1){
                            // only add unused events to the menu
                            options += '<option value="' + _id + '">' + _label +'</option>';
                            // store available (unused) events
                            availableEvents.push(_id);
                        }
                        // store list of all events
                        siteEventsManager.events.push(_id);
                    });

                    if (!availableEvents.length){
                        options = '<option value="!" disabled>(no available events)</option>';
                    }
                    hasEvents = true;
                }

                $eventsMenu.html(options);

            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving system events: [' + status + '] ' + error);
            },
            complete: function(){
                // render the events table stuff after the
                // request to get the events
                //if (!handlersRendered){
                //    initHandlersTable();
                //}
            }
        });
    }

    function initScriptsMenu(){
        //if (!hasEvents) { return; }
        siteEventsManager.scripts = []; // reset array
        return xhr.getJSON({
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
                        options += '<option title="' + script['Description'] + '" value="' + script['Script ID'] + '">' + script['Script ID'] + '</option>';
                        siteEventsManager.scripts.push(script['Script ID']);
                    });
                    $scriptsMenu.html(options);
                }

            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving system events: [' + status + '] ' + error);
            }
        });
    }

    // initialize menus and table
    initEventsMenu();
    initScriptsMenu();

    function doAddEventHandler( xmodalObj ){

        var data = {
            event: xmodalObj.__modal.find('select.event').val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val()
        };

        // TODO: Should we let them name the trigger? Is that worthwhile? (yes)
        // var url = serverRoot + "/data/projects/" + window.projectScope + "/automation/events/" + triggerId + "?XNAT_CSRF=$!XNAT_CSRF";
        //var url = serverRoot + "/data/projects/" + window.projectScope + "/automation/events?XNAT_CSRF=$!XNAT_CSRF";

        if (!data.event || data.event === '!' || !data.scriptId){
            xmodal.message('Missing Information','Please select an <b>Event</b> <i>and</i> <b>Script ID</b> to create an <br>Event Handler.');
            return false;
        }

        xhr.put({
            url: XNAT.url.restUrl('/data/automation/handlers?XNAT_CSRF=' + window.csrfToken, null, false),
            data: data,
            dataType: "json",
            success: function(e){
                initHandlersTable(false);
                if ($("#events_manage_table").length>0) {
                    initHandlersTable(true);
                }
                xmodal.message('Success', 'The event handler was successfully added.', 'OK', {
                    action: function(){
                        xmodal.closeAll($(xmodal.dialog.open),$('#xmodal-manage-events'));
                    }
                });
                // Trigger automation uploader to reload handlers
                if (typeof(XNAT.app.abu.uploaderConfig)==='undefined') {
			XNAT.app.abu.initUploaderConfig();
		}
		XNAT.app.abu.removeUploaderConfiguration(this.event,'site');
		XNAT.app.abu.getAutomationHandlers();
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred: [' + status + '] ' + error, 'Close', {
                    action: function(){
                        xmodal.closeAll($(xmodal.dialog.open),$('#xmodal-manage-events'));
                    }
                });
            }
        });
    }

    function addEventHandler(){
        xmodal.loading.open();
        initScriptsMenu().
            done(initEventsMenu().
                done(function(){
                    xmodal.loading.close();
                    xmodal.open({
                        title: 'Add Event Handler',
                        template: $('#addEventHandler'),
                        width: 500,
                        height: 300,
                        overflow: true,
                        esc: false,
                        enter: false,
                        beforeShow: function(obj){
                            var $menus = obj.$modal.find('select.event, select.scriptId');
                            $menus.trigger('chosen:updated');
                            //chosenInit($menus, null, 300);
                            $menus.chosen({
                                width: '300px',
                                disable_search_threshold: 6
                            });
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
            )
        );
    }

    function doDeleteHandler( handlerId ){
        var url = XNAT.url.restUrl('/data/automation/triggers/' + handlerId + "?XNAT_CSRF=" + window.csrfToken, null, false);
        if (window.jsdebug) console.log(url);
        xhr.delete({
            //type: 'DELETE',
            url: url,
            //cache: false,
            success: function(){
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

    function deleteEventHandler( handlerId, event ){
        xmodal.confirm({
            title: 'Delete Event Handler?',
            content: 'Are you sure you want to delete the handler for the <b>"' + event + '"</b> event? ' +
            'Only the Event Handler will be deleted. The associated Script will still be available for use.',
            width: 440,
            height: 220,
            okLabel: 'Delete',
            okClose: false, // don't close yet
            cancelLabel: 'Cancel',
            okAction: function(){
                doDeleteHandler(handlerId);
            },
            cancelAction: function(){
                xmodal.message('Delete event handler cancelled', 'The event handler was not deleted.', 'Close');
            }
        });
    }

    // removed inline onclick attributes:
    $events_table.on('click', 'a.delete-handler', function(){
        deleteEventHandler($(this).data('handler'), $(this).data('event'));
    });

    // *javascript* event handler for adding an XNAT event handler (got it?)
    $add_event_handler.on('click', addEventHandler);
    $manage_event_handlers.on('click', manageEventHandlers);

});
