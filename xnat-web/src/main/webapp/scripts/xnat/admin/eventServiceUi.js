/*
 * web: eventServiceUi.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Site-wide Admin UI functions for the Event Service
 */

console.log('xnat/admin/eventService.js');

var XNAT = getObject(XNAT || {});

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function(){

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    function spacer(width){
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function errorHandler(e, title, closeAll){
        console.log(e);
        title = (title) ? 'Error Found: '+ title : 'Error';
        closeAll = (closeAll === undefined) ? true : closeAll;
        var errormsg = (e.statusText) ? '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>' : e;
        XNAT.dialog.open({
            width: 450,
            title: title,
            content: errormsg,
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(){
                        if (closeAll) {
                            xmodal.closeAll();

                        }
                    }
                }
            ]
        });
    }


    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var eventServicePanel,
        undefined,
        rootUrl = XNAT.url.rootUrl,
        csrfUrl = XNAT.url.csrfUrl;

    XNAT.admin =
        getObject(XNAT.admin || {});

    XNAT.admin.eventServicePanel = eventServicePanel =
        getObject(XNAT.admin.eventServicePanel || {});

    eventServicePanel.xsiTypes = [
        'xnat:projectData',
        'xnat:subjectData',
        'xnat:imageSessionData',
        'xnat:imageScanData'
    ];

    eventServicePanel.events = {};

    function getProjectListUrl(){
        return rootUrl('/data/projects?format=json');
    }

    function getEventActionsUrl(projectId,xsiType){
        return rootUrl('/xapi/events/actions?projectid='+projectId+'&xnattype='+xsiType);
    }

    function getEventSubscriptionUrl(id){
        id = id || false;
        var path = (id) ? '/xapi/events/subscription/'+id : '/xapi/events/subscriptions';
        return rootUrl(path);
    }

    function setEventSubscriptionUrl(id,appended){
        id = id || false;
        var path = (id) ? '/xapi/events/subscription/'+id : '/xapi/events/subscription';
        appended = (appended) ? '/'+appended : '';
        return csrfUrl(path + appended);
    }

    eventServicePanel.getProjects = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.getJSON({
            url: getProjectListUrl(),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve projects');
            }
        })
    };

    eventServicePanel.getEvents = function(callback){
        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/xapi/events/events'),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve events');
            }
        })
    };

    eventServicePanel.getSubscriptions = function(callback){
        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: getEventSubscriptionUrl(),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve event subscriptions');
            }
        })
    };

    eventServicePanel.getActions = function(opts,callback){
        if (!opts.project || !opts.xsiType) return false;

        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: getEventActionsUrl(opts.project,opts.xsiType),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve event actions');
            }
        })
    };
    
    eventServicePanel.subscriptionTable = function(){
        // initialize the table
        var subTable = XNAT.table({
            addClass: 'xnat-table compact',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        // add table header row
        subTable.tr()
            .th({ addClass: 'left', html: '<b>Name</b>' })
            .th('<b>Project</b>')
            .th('<b>Trigger Event</b>')
            .th('<b>Action</b>')
            .th('<b>Created By</b>')
            .th('<b>Enabled</b>')
            .th({ style: { width: '90px' }, html: '<b>Action</b>' });

        function subscriptionNiceLabel(label){
            return spawn('a',{
                href: '#!',
                style: { 'font-weight': 'bold' },
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription(subscription.id, false);
                }
            }, label);
        }
        function projectLinks(ids){
            var projectLinks = [];
            if (ids) {
                ids.forEach(function(id){
                    projectLinks.push( spawn('a',{ href: XNAT.url.rootUrl('/data/projects/'+id+'?format=html'), style: { display: 'block' }}, id) );
                });
                return spawn( 'div.center', projectLinks );
            }
            else {
                return 'N/A';
            }
        }
        function eventNiceName(eventId){
            return eventServicePanel.events[eventId]['display-name'];
        }
        function actionNiceName(subscription){
            return spawn('span.truncate.truncate200', subscription['action-key']);
        }

        function enabledCheckbox(item){
            var enabled = !!item.active;
            var ckbox = spawn('input.subscription-enabled', {
                type: 'checkbox',
                checked: enabled,
                value: enabled,
                onchange: function(){

                }
            });

            return spawn('div.center', [
                spawn('label.switchbox|title=' + item.name, [
                    ckbox,
                    ['span.switchbox-outer', [['span.switchbox-inner']]]
                ])
            ]);
        }

        function editSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription(subscription.id, false);
                }
            }, [ spawn('span.fa.fa-pencil') ]);
        }
        function deleteSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription(subscription.id, false);
                }
            }, [ spawn('span.fa.fa-trash') ]);
        }

        eventServicePanel.getSubscriptions().done(function(data){
            if (data.length) {
                data.forEach(function(subscription){
                    subTable.tr({ addClass: (subscription.valid) ? 'valid' : 'invalid', id: 'event-subscription-'+subscription.id, data: { id: subscription.id } })
                        .td([ subscriptionNiceLabel(subscription.name) ])
                        .td([ projectLinks(subscription.projects) ])
                        .td([ eventNiceName(subscription['event-id']) ])
                        .td([ actionNiceName(subscription) ])
                        .td(subscription['subscription-owner'])
                        .td([ enabledCheckbox(subscription) ])
                        .td([ editSubscriptionButton(subscription), spacer(6), deleteSubscriptionButton(subscription) ])
                })
            }
            else {
                subTable.tr().td({ colSpan: '7', html: 'No event subscriptions have been created' })
            }

        });

        eventServicePanel.$table = $(subTable.table);

        return subTable.table;
    };

    XNAT.admin.eventServicePanel.populateDisplay = function(rootDiv) {
        var $container = $(rootDiv || '#event-service-admin-tabs');
        $container.empty();

        var subscriptionTab =  {
            kind: 'tab',
            label: 'Event Subscriptions',
            group: 'General',
            active: true,
            contents: {
                subscriptionPanel: {
                    kind: 'panel',
                    label: 'Event Subscriptions',
                    contents: {
                        subscriptionFilterBar: {
                            tag: 'div#subscriptionFilters'
                        },
                        subscriptionTableContainer: {
                            tag: 'div#subscriptionTableContainer'
                        }
                    }
                }
            }
        };
        var historyTab = {
            kind: 'tab',
            label: 'Event Service History',
            group: 'General',
            contents: {
                eventHistoryContainer: {
                    tag: 'div#historyTable'
                }
            }
        };
        var eventTabSet = {
            kind: 'tabs',
            name: 'eventSettings',
            label: 'Event Service Administration',
            contents: {
                subscriptionTab: subscriptionTab,
                historyTab: historyTab
            }
        };

        eventServicePanel.tabSet = XNAT.spawner.spawn({ eventSettings: eventTabSet });
        eventServicePanel.tabSet.render($container);

        $('#subscriptionTableContainer').append( eventServicePanel.subscriptionTable() );

        XNAT.ui.tab.activate('subscription-tab');

    };

    eventServicePanel.init = function(){

        // Prerequisite: Get known events
        // translate events array into an object driven by the event ID

        eventServicePanel.getEvents().done(function(events){
            events.forEach(function(event){
                eventServicePanel.events[event.id] = event;
            });

            // Populate event subscription table
            eventServicePanel.populateDisplay();
        });


    };

}));