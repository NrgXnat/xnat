/*
 * web: automation.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * functions for XNAT automation workflow events
 * xnat-templates/screens/xnat_projectData/xnat_projectData_summary_manage.vm
 */

var XNAT = getObject(XNAT||{});

XNAT.app.automation = {};

(function(XNAT){

    var app, automation, handlers,
        events, scripts, workflows,
        xhr = XNAT.xhr,
        xurl = XNAT.url;

    XNAT.app = app =
        getObject(XNAT.app||{});

    XNAT.app.automation = automation =
        getObject(XNAT.app.automation||{});

    XNAT.app.automation.events = events = [];
    XNAT.app.automation.scripts = scripts = [];
    XNAT.app.automation.workflows = workflows = {};

    //var systemEvents = []; // ?
    //var systemScripts = []; // ?

    // TODO: WRITE ALL JAVASCRIPT

    var $defineEvent = $('#define-event-button'),
        $eventsTable = $('#events-table'),
        $eventsBody = $eventsTable.find('> tbody'),
        $noEvents = $('#no-handlers'),
        eventRowTemplate = $('#event-row-template').find('> tbody').html(),
        $defineEventDialog,
        SEP = '|:'; // workflow id/label separator

    function getUsers(callback){
        var _url = XNAT.url.restUrl('/data/users');
        return xhr.getJSON({
            url: _url,
            success: function(json){
                var results = json.ResultSet ? json.ResultSet.Result || [] : [];
                callback(results);
            },
            error: function(){
                callback([]);
            }
        });
    }

    function workflowsURL(part){
        part = isDefined(part) ? part : '';
        // return workflows URL with CSRF and unique cache-busting paramater
        return XNAT.url.restUrl('/data/automation/workflows' + part);
        // URL below is used for testing only
        //return XNAT.url.restUrl('/scripts/xnat/app/automation-workflows.json');
    }

    /*
    function eventsURL(part, params, cacheit){
        part = isDefined(part) ? part : '';
        // return events URL with CSRF and unique cache-busting paramater
        return XNAT.url.restUrl('/data/automation/events' + part, params, cacheit);
    }

    function getSiteEvents(callback){
        automation.events = [];
        return xhr.getJSON(eventsURL(), callback)
    }
    */

    function createEventRows(events){
        var rows = '';
        forEach(events, function(event){
            rows += eventRowTemplate.
                replace(/__SITE_EVENT_ID__/g, event.event_id).
                replace(/__SITE_EVENT_LABEL__/g, event.event_label);
            // save an array of event ids.
            automation.events.push(event.event_id);
        });
        return rows;
    }


    // render the site events list
    // call this after creating/updating events
    automation.renderEvents = function(data){

        var results = data.ResultSet ? data.ResultSet.Result || [] : [];

        if (results.length){
            $noEvents.hide();
            $eventsBody.empty().html(createEventRows(results));
            $eventsTable.show();
        }
        else {
            $noEvents.show();
            $eventsTable.hide();
        }

    };


    // handle REST error
    automation.eventsError = function(req, status, error){
        $noEvents.hide();
        $eventsTable.hide();
        $noEvents.html('An error occurred while retrieving Event list: [' + status + '] ' + error).show();
    };


    // initialize the Events display
    /*
    automation.initEvents = function(){
        // make sure things are hidden first
        $eventsTable.hide();
        $noEvents.hide();

        getSiteEvents({
            success: automation.renderEvents,
            failure: automation.eventsError
        });

    };
    */

    function getWorkflows(callback){
        //automation.workflows = {}; // reset workflows
        return xhr.getJSON(workflowsURL(), callback);
    }

    function createWorkflowsMenu($container, json){

        var menu = document.createElement('select'),
            $menu = $(menu).addClass('event_id active'),
            options = '';

        menu.id = 'workflow-event-id-menu';
        menu.name = 'event_id';

        getUsers(function(users){

            var usernames = users.map(function(user){
                return user.login
            });

            //XNAT.app.usernames = usernames;

            forEach(json, function(workflow){

                var _id = (workflow.event_id),
                    _label = workflow.event_label,
                    //_workflow = _id + SEP + _label, // put id and label together, separated by SEP ( |: )
                    _parenStr = '';

                // check ids with parentheses
                if (_id.indexOf('user') > -1 && _id.match(/\(([^\)]+)\)/)){

                    _parenStr = _id.split('(')[1].split(')')[0];

                    // skip workflows with email addresses in them
                    if (_id.indexOf('@') > -1 && validEmailFormat(_parenStr)){
                        return;
                    }

                    else if (usernames.length && usernames.indexOf(_parenStr) > -1){
                        return;
                    }

                    else {
                        workflows[_id] = _label;
                    }

                }
                else {
                    workflows[_id] = _label;
                }

                // save an array of workflows
                //automation.workflows.push(_workflow);

                // only add unused workflow events as options
                if (automation.events.indexOf(_id) === -1){
                    options += '<option' +
                        ' value="' + _id + '"' +
                        ' title="' + _label + '"' +
                        '>' + _label + '</option>';
                }

            });

            if (!options){
                options = '<option value="!" title="no available workflows" disabled selected>no available workflows</option>';
                $menu.prop('disabled',true);
            }
            else {
                options = ('<option value=""></option>' + options);
            }

            $menu.html(options);

            $container.empty().append($menu);

            menuInit($menu, null, 370);

            initCustomEventNameToggler($defineEventDialog);

        });

    }

    /*
    automation.saveEvent = function(data){
        if (!data || !data.event_id){
            xmodal.loading.closeAll();
            xmodal.message('Error', 'Event ID not specified.', 'Close');
            return;
        }
        // if no label specified, use the workflow label or id
        if (!data.event_label){
            data.event_label = data.event_id;
        }
        xhr.put({
            url: eventsURL('', '?XNAT_CSRF=' + window.csrfToken, false),
            //url: eventsURL(),
            data: data,
            success: function(){
                automation.initEvents();
                xmodal.message('Success', 'Event saved.', 'Close', {
                    action: function(){
                        xmodal.closeAll();
                    }
                })
            },
            failure: function(){
                xmodal.message('Error', 'Error saving Event.', 'Close', {
                    action: function(){
                        xmodal.loading.closeAll();
                    }
            });
            }
        });
    };
    */

    function toggleDisabled($els, disabled){

        $$($els).each(function(){

            var $el = $(this);
            // disabled === true for disabled
            // otherwise the opposite of current disabled status
            disabled = disabled || !$el.is(':disabled');

            if (disabled){
                $el.prop('disabled', true).addClass('disabled');
            }
            else {
                $el.prop('disabled', false).removeClass('disabled');
            }
        });

    }

    // initialize controls for entering custom Event name
    function initCustomEventNameToggler($dialog){

        var $workflowMenu = $dialog.find('#workflow-event-id-menu'),
            $checkbox = $dialog.find('#custom-event-checkbox'),
            $input = $dialog.find('#custom-event-input');

        // if workflows menu is disabled, it's empty
        // so the only option is to manually enter
        // a new Event name
        if ($workflowMenu.is(':disabled')){
            $checkbox.prop('checked',true).prop('disabled',true);
            $input.prop('disabled',false).focus().select();
            return;
        }

        $checkbox.on('change', function(){
            var checked = $(this).prop('checked');
            toggleDisabled($workflowMenu, checked);
            toggleDisabled($input, !checked);
            $workflowMenu.trigger('chosen:updated');
            //$input.focus().select();
        });

    }

    // open a dialog to create a site-wide Event
    automation.defineEvent = function(){
        var $dialog; // a reference to the dialog
        getWorkflows({
            success: function(data){
                var results = data.ResultSet ? data.ResultSet.Result || [] : [];
                xmodal.open({
                    title: 'Define an Event',
                    template: $('#site-events-template'),
                    width: 500,
                    height: 400,
                    scroll: false,
                    overflow: true,
                    esc: false,
                    enter: false,
                    beforeShow: function(modal){
                        $defineEventDialog = $dialog = modal.$modal;
                        var $workflowsContainer = $dialog.find('#workflow-event-id-container');
                        createWorkflowsMenu($workflowsContainer, results);
                    },
                    okLabel: 'Save',
                    okAction: function(){

                        var _id = $dialog.find('.event_id').not(':disabled').val(),
                            _label = $dialog.find('.event_label').val();

                        // if no value for 'Label' is entered, get it
                        // from the XNAT.app.automation.workflows object
                        // or just use the ID
                        if (!_label){
                            _label = workflows[_id] || _id;
                        }

                        xmodal.loading.open('Saving...');

                        automation.saveEvent({
                            event_id: _id,
                            event_label:  _label
                        });

                    },
                    okClose: false
                });
            } 
        })
    };

    function deleteRequestMessage(title, msg){
        return xmodal.message({
            title: title || ' ',
            content: msg || 'Something happened.',
            action: function(){
                xmodal.closeAll();
            }
        })
    }

    /*
    automation.deleteEvent = function(id, label){
        var _url =  eventsURL('', '?XNAT_CSRF=' + window.csrfToken + '&cascade=true', false);
        xmodal.confirm({
            title: 'Delete?',
            content: 'Are you sure you want to delete the Event' +
                (label ? ': <b>"' + label + '"</b>' : '') + '? ' +
                'Any Event Handlers associated with this Event ' +
                'will also be deleted.',
            okLabel: 'Delete',
            okAction: function(){
                xhr.delete({
                    url: _url,
                    data: {
                        "event_id": id
                    },
                    success: function(){
                        automation.initEvents();
                        XNAT.app.siteEventsManager.initHandlersTable();
                        deleteRequestMessage('Success', 'Event deleted.');
                    },
                    failure: function(){
                        deleteRequestMessage('Error', 'There was an error deleting the Event.');
                    }
                });
            }
        });
    };
    */


    // INITIALIZE
    //automation.initEvents();


    // click the "Define Event" button
    $defineEvent.click(automation.defineEvent);


    // (javascript) event handler for 'action' (delete) links
    $eventsBody.on('click', 'a.delete-event', function(e){
        e.preventDefault();

        var action = $(this).data('action'),
            event_id = $(this).closest('tr').data('event-id'),
            event_label = $(this).closest('tr').data('event-label');

        automation.deleteEvent(event_id, event_label);

    });

})(XNAT);

