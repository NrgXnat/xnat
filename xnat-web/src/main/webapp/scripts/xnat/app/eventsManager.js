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
        handlersRendered = false,
        hasEvents = false;

    function initEventsTable(){

        // hide stuff
        $events_table.hide();
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
                    forEach(_handlers, function(eventHandler){
                        var _event_id = eventHandler['event'];
                        eventsManager.handlers.push(_event_id);
                        eventRows += '<tr class="highlight">' +
                            '<td class="event-id">' + _event_id + '</td>' +
                            '<td class="script-id">' + eventHandler.scriptId + '</td>' +
                            '<td class="description">' + eventHandler.description + '</td>' +
                            '<td style="text-align: center;">' +
                            '<a href="javascript:" class="delete-handler" ' +
                            'data-handler="' + eventHandler.triggerId + '" title="Delete handler for event ' + _event_id + '">delete</a>' +
                            '</td>' +
                            '</tr>';
                    });
                    $events_table.find('tbody').html(eventRows);
                    $events_table.show();
                }
                else {
                    if (!hasEvents) {
                        $add_event_handler.prop('disabled',true);
                        $no_events_defined.show();
                    }
                    else {
                        $no_event_handlers.show();
                    }
                }
            },
            error: function( request, status, error ){
                xmodal.message('Error', 'An error occurred retrieving event handlers for this project: [' + status + '] ' + error);
            },
            complete: function(){
                $('#accordion').accordion('refresh');
            }
        });
    }

    function initEventsMenu(){
        eventsManager.events = []; // reset array
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

                        if (eventsManager.handlers.indexOf(_id) === -1){
                            // only add unused events to the menu
                            options += '<option value="' + _id + '">' + _label +'</option>';
                            // store available (unused) events
                            availableEvents.push(_id);
                        }
                        // store list of all events
                        eventsManager.events.push(_id);
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
                if (!handlersRendered){
                    initEventsTable();
                }
            }
        });
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
                        options += '<option title="' + script['Description'] + '" value="' + script['Script ID'] + '">' + script['Script ID'] + '</option>';
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

    // initialize menus and table
    initEventsMenu();
    initScriptsMenu();

    function doAddEventHandler( xmodalObj ){

        var data = {
            event: xmodalObj.__modal.find('select.event').val(),
            scriptId: xmodalObj.__modal.find('select.scriptId').val(),
            description: xmodalObj.__modal.find('input.description').val()
        };

        if (!data.event || data.event === '!' || !data.scriptId){
            xmodal.message('Missing Information','Please select an <b>Event</b> <i>and</i> <b>Script ID</b> to create an <br>Event Handler.');
            return false;
        }

        xhr.put({
            url: XNAT.url.restUrl('/data/projects/' + window.projectScope + '/automation/handlers', null, false),
            data: data,
            dataType: "json",
            success: function(){
                xmodal.message('Success', 'Your event handler was successfully added.', 'OK', { 
                        action: function(){
                            initEventsTable();
                            xmodal.closeAll();
                        }  
                    }
                );
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

    function addEventHandler(){

        var getEvents = initEventsMenu();

        getEvents.done(function(){
            xmodal.open({
                title: 'Add Event Handler',
                template: $('#addEventHandler'),
                width: 500,
                height: 300,
                overflow: true,
                beforeShow: function(obj){
                    chosenInit(obj.$modal.find('select.event, select.scriptId'), null, 300);
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
        });
    }

    function doDeleteTrigger( triggerId ){
        var url = XNAT.url.restUrl('/data/automation/triggers/' + triggerId);
        if (window.jsdebug) console.log(url);
        jQuery.ajax({
            type: 'DELETE',
            url: url,
            cache: false,
            success: function(){
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

    function deleteEventHandler( triggerId ){
        xmodal.confirm({
            title: 'Delete Event Handler?',
            content: 'Are you sure you want to delete the handler: <br><br><b>' + triggerId + '</b>?<br><br>Only the Event Handler will be deleted. The associated Script will still be available for use.',
            width: 440,
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
    $add_event_handler.on('click', addEventHandler);

});
