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
    eventServicePanel.actions = {};

    function getProjectListUrl(){
        return rootUrl('/data/projects?format=json');
    }

    function getEventActionsUrl(projectId,xsiType){
        var path = (xsiType) ?
            '/xapi/events/actions?xnattype='+xsiType :
            '/xapi/events/allactions';

        if (xsiType && projectId) path += '&projectid='+projectId;
        return rootUrl(path);
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
        var project = (opts) ? opts.project : false;
        var xsiType = (opts) ? opts.xsiType : false;

        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: getEventActionsUrl(project,xsiType),
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


    /* -------------------------- *
     * Subscription Display Table *
     * -------------------------- */
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
            .th({ style: { width: '125px' }, html: '<b>Action</b>' });

        /* Formatted table cells */
        function subscriptionNiceLabel(label,id){
            return spawn('a',{
                href: '#!',
                style: { 'font-weight': 'bold' },
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription('Edit',id);
                }
            }, label);
        }
        function projectLinks(id){
            if (id) {
                var projectLink = spawn('a',{ href: XNAT.url.rootUrl('/data/projects/'+id+'?format=html'), style: { display: 'block' }}, id);
                return spawn( 'div.center', [ projectLink ]);
            }
            else {
                return 'N/A';
            }
        }
        function eventNiceName(eventId){
            return eventServicePanel.events[eventId]['display-name'];
        }
        function actionNiceName(actionKey){
            return (eventServicePanel.actions[actionKey]) ?
                eventServicePanel.actions[actionKey]['display-name'] :
                actionKey;
        }
        function subscriptionEnabledCheckbox(subscription){
            var enabled = !!subscription.active;
            var ckbox = spawn('input.subscription-enabled', {
                type: 'checkbox',
                checked: enabled,
                value: 'true',
                onchange: function(){
                    eventServicePanel.toggleSubscription(subscription.id, this);
                }
            });

            return spawn('div.center', [
                spawn('label.switchbox|title=' + subscription.name, [
                    ckbox,
                    ['span.switchbox-outer', [['span.switchbox-inner']]]
                ])
            ]);
        }
        function editSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription('Edit',subscription.id);
                },
                title: 'Edit'
            }, [ spawn('span.fa.fa-pencil') ]);
        }
        function cloneSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.editSubscription('Clone',subscription.id);
                },
                title: 'Duplicate'
            }, [ spawn('span.fa.fa-clone') ]);
        }
        function deleteSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    eventServicePanel.deleteSubscriptionConfirmation(subscription);
                },
                title: 'Delete'
            }, [ spawn('span.fa.fa-trash') ]);
        }

        eventServicePanel.getSubscriptions().done(function(data){
            if (data.length) {
                data.forEach(function(subscription){
                    subTable.tr({ addClass: (subscription.valid) ? 'valid' : 'invalid', id: 'event-subscription-'+subscription.id, data: { id: subscription.id } })
                        .td([ subscriptionNiceLabel(subscription.name,subscription.id) ])
                        .td([ projectLinks(subscription['project-id']) ])
                        .td([ eventNiceName(subscription['event-id']) ])
                        .td([ actionNiceName(subscription['action-key']) ])
                        .td(subscription['subscription-owner'])
                        .td([ subscriptionEnabledCheckbox(subscription) ])
                        .td({ addClass: 'center' },[ editSubscriptionButton(subscription), spacer(4), cloneSubscriptionButton(subscription), spacer(4), deleteSubscriptionButton(subscription) ])
                })
            }
            else {
                subTable.tr().td({ colSpan: '7', html: 'No event subscriptions have been created' })
            }

        });

        eventServicePanel.$table = $(subTable.table);

        return subTable.table;
    };

    /* ---------------------------------- *
     * Create, Edit, Delete Subscriptions *
     * ---------------------------------- */

    var emptyOptionObj = {
        html: '<option selected></option>'
    };
    var createFormObj = {
        kind: 'panel.form',
        id: 'edit-subscription-form',
        header: false,
        footer: false,
        element: {
            style: { border: 'none', margin: '0' }
        },
        contents: {
            subName: {
                kind: 'panel.input.text',
                name: 'name',
                label: 'Event Subscription Label',
                validation: 'not-empty',
                order: 10
            },
            subId: {
                kind: 'panel.input.hidden',
                name: 'id',
                element: {
                    disabled: 'disabled'
                }
            },
            subEventSelector: {
                kind: 'panel.select.single',
                name: 'event-id',
                label: 'Select Event',
                id: 'subscription-event-selector',
                element: emptyOptionObj,
                order: 20
            },
            subProjSelector: {
                kind: 'panel.select.single',
                name: 'project-id',
                label: 'Select Project',
                id: 'subscription-project-selector',
                element: {
                    html: '<option selected>Any Project</option>'
                },
                order: 30
            },
            subActionSelector: {
                kind: 'panel.select.single',
                name: 'action-key',
                label: 'Select Action',
                id: 'subscription-action-selector',
                element: emptyOptionObj,
                description: 'Available actions are dependent on your project and xsiType selections',
                order: 40
            },
            subActionPreview: {
                tag: 'div#subscription-action-preview.panel-element',
                element: {
                    style: {
                        display: 'none'
                    }
                },
                contents: {
                    subActionPreviewPane: {
                        tag: 'div.preview-pane.panel-element',
                        element: {
                            style: {
                                border: '1px dotted #ccc',
                                'margin-bottom': '2em',
                                padding: '1em 1em 0'
                            }
                        },
                        contents: {
                            subAttributeLabel: {
                                tag: 'label.element-label',
                                content: 'Attributes'
                            },
                            subAttributePreview: {
                                tag: 'div.element-wrapper',
                                contents: {
                                    subAttributePreviewTextarea: {
                                        tag: 'p',
                                        content: '<textarea id="sub-action-attribute-preview" class="disabled" disabled="disabled" readonly></textarea><br>'
                                    },
                                    subAttributeEditButton: {
                                        tag: 'p',
                                        content: '<button id="set-sub-action-attributes">Set Attributes</button>'
                                    }
                                }
                            },
                            subAttributeClear: {
                                tag: 'div.clear.clearfix'
                            }
                        }
                    }
                },
                order: 41
            },
            subActionAttributes: {
                kind: 'panel.input.hidden',
                name: 'attributes',
                id: 'subscription-action-attributes'
            },
            subActionInherited: {
                kind: 'panel.input.hidden',
                name: 'inherited-action',
                id: 'subscription-inherited-action'
            },
            subFilter: {
                kind: 'panel.input.text',
                name: 'event-filter',
                label: 'Event Filter',
                description: 'Optional. Enter filter in JSON path notation, e.g. <pre style="margin-top:0">$[?(@.xsiType == \\"xnat:mrScanData\\")]</pre>',
                order: 50
            },
            subUserProxy: {
                kind: 'panel.input.switchbox',
                name: 'act-as-event-user',
                label: 'Perform Action As:',
                onText: 'Action is performed as the user who initiates the event',
                offText: 'Action is performed as you (the subscription owner)',
                value: 'true',
                order: 60
            },
            subActive: {
                kind: 'panel.input.switchbox',
                name: 'active',
                label: 'Status',
                onText: 'Enabled',
                offText: 'Disabled',
                value: 'true',
                order: 70
            }
        }
    };

    // populate the Action Select menu based on selected project and event (which provides xsitype)
    function findActions($element){
        var $form = $element.parents('form');
        var project = $form.find('select[name=project-id]').find('option:selected').val();
        var xsiType = $form.find('select[name=event-id]').find('option:selected').data('xsitype');
        var inheritedAction = $form.find('input[name=inherited-action]').val(); // hack to stored value for edited subscription
        var actionSelector = $form.find('select[name=action-key]');
        var url = getEventActionsUrl();

        if (project && actionSelector) {
            url = getEventActionsUrl(project,xsiType)
        }
        else if (actionSelector) {
            url = getEventActionsUrl(false,xsiType)
        }

        XNAT.xhr.get({
            url: url,
            success: function(data){
                actionSelector
                    .empty()
                    .append(spawn('option', { selected: true }));
                if (data.length){
                    data.forEach(function(action){
                        var selected = false;
                        if (inheritedAction.length && action['action-key'] === inheritedAction) {
                            // if we're editing an existing subscription, we'll know the action before this select menu knows which actions exist.
                            // get the stored value and mark this option selected if it matches, then clear the stored value.
                            selected='selected';
                            $form.find('input[name=inherited-action]').val('');
                        }

                        actionSelector.append( spawn('option', { value: action['action-key'], selected: selected }, action['display-name'] ))
                        // if the action has attributes, add them to the global actions object
                        if (action['attributes']) {
                            eventServicePanel.actions[action['action-key']].attributes = action['attributes'];
                        }
                    });
                }
            }
        })
    }

    // populate or hide the Action Attributes selector depending on whether it is required by the selected action
    function getActionAttributes($element){
        var $form = $element.parents('form');
        var actionId = $element.find('option:selected').val();
        if (actionId) {
            if (eventServicePanel.actions[actionId].attributes && eventServicePanel.actions[actionId].attributes !== {}) {
                $form.find('#subscription-action-preview').slideDown(300);
            }
            else {
                $form.find('#subscription-action-preview').slideUp(300);
            }
        }
    }
    function renderAttributeInput(name,props){

        var obj = {
            label: name,
            name: name,
            description: props.description
        };
        if (props.required) obj.validation = 'required';
        obj.value = props['default-value'] || '';

        var el;

        if (eventServicePanel.subscriptionAttributes) {
            var presetAttributes = (typeof eventServicePanel.subscriptionAttributes === "string") ?
                JSON.parse(eventServicePanel.subscriptionAttributes) :
                eventServicePanel.subscriptionAttributes;
            obj.value = presetAttributes[name];
        }

        switch (props.type){
            case 'boolean':
                obj.onText = 'On';
                obj.offText = 'Off';
                obj.value = obj.value || 'true';
                el = XNAT.ui.panel.input.switchbox(obj);
                break;
            default:
                el = XNAT.ui.panel.input.text(obj);
        }

        return el;
    }
    eventServicePanel.enterAttributesDialog = function(attributesObj){
        var inputElements;
        if (Object.keys(attributesObj).length > 0) {
            inputElements = [];
            Object.keys(attributesObj).forEach(function(name){
                var props = attributesObj[name];
                if (props['user-settable']) {
                    inputElements.push( renderAttributeInput(name,props) );
                }
            });
            inputElements = spawn('!',inputElements);
        }
        else {
            return false
        }
        eventServicePanel.subscriptionAttributes = "";
        XNAT.ui.dialog.open({
            width: 450,
            title: 'Enter Attributes',
            content: '<form class="xnat-form-panel panel panel-default" id="attributes-form" style="border: none; margin: 0"><div class="panel-body" id="attributes-elements-container"></div></form>',
            beforeShow: function(obj){
                var $container = obj.$modal.find('#attributes-elements-container');
                $container.append( inputElements );
            },
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    action: function(obj){
                        var $form = obj.$modal.find('form');
                        eventServicePanel.subscriptionAttributes = JSON.stringify($form);
                        $(document).find('#sub-action-attribute-preview').html(eventServicePanel.subscriptionAttributes);
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    };

    eventServicePanel.modifySubscription = function(action,subscription){
        var projs = eventServicePanel.projects;
        subscription = subscription || false;
        action = action || 'Create';

        eventServicePanel.subscriptionAttributes = (subscription) ? subscription.attributes : false;
        // if (projs.length) {

            XNAT.ui.dialog.open({
                title: action + ' Subscription',
                width: 600,
                content: '<div id="subscription-form-container"></div>',
                beforeShow: function(obj){
                    var $container = obj.$modal.find('#subscription-form-container');
                    XNAT.spawner.spawn({ form: createFormObj }).render($container);

                    var $form = obj.$modal.find('form');

                    if (projs.length) {
                        projs.forEach(function(project){
                            $form.find('#subscription-project-selector')
                                .append(spawn(
                                    'option',
                                    { value: project.ID },
                                    project['secondary_ID']
                                ));
                        });
                    }
                    else {
                        $form.find('#subscription-project-selector').parents('.panel-element').hide();
                    }

                    Object.keys(eventServicePanel.events).forEach(function(event){
                        $form.find('#subscription-event-selector')
                            .append(spawn(
                                'option',
                                { value: event, data: { xsitype: eventServicePanel.events[event]['xnat-type'] }},
                                eventServicePanel.events[event]['display-name']
                            ));
                    });

                    if (subscription && subscription['event-filter']) subscription['event-filter'] = subscription['event-filter']['json-path-filter'];
                    if (action.toLowerCase() === "clone") {
                        delete subscription.name;
                        delete subscription.id;
                        delete subscription['registration-key'];
                    }

                    if (action.toLowerCase() !== "create") {
                        $form.find('#subscription-inherited-action').val(subscription['action-key']);

                        $form.setValues(subscription); // sets values in inputs and selectors, which triggers the onchange listeners below. Action has to be added again after the fact.
                        $form.addClass((subscription.valid) ? 'valid' : 'invalid');

                        if (Object.keys(subscription.attributes).length) {
                            $form.find('#subscription-action-preview').show();
                            $form.find('#sub-action-attribute-preview').html( JSON.stringify(subscription.attributes) );
                        }
                    }

                    if (action.toLowerCase() === "edit") {
                        $form.find('input[name=id]').prop('disabled',false);
                    }

                },
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true,
                        action: function(obj){
                            var formData = JSON.stringify(obj.$modal.find('form'));
                            var jsonFormData = JSON.parse(formData);
                            if (eventServicePanel.subscriptionAttributes) {
                                jsonFormData.attributes = (typeof eventServicePanel.subscriptionAttributes === 'object') ?
                                    eventServicePanel.subscriptionAttributes :
                                    JSON.parse(eventServicePanel.subscriptionAttributes);
                            }
                            else {
                                jsonFormData.attributes = {};
                            }

                            if (jsonFormData['event-filter']) {
                                var jsonpathfilter = jsonFormData['event-filter'];
                                jsonFormData['event-filter'] = {};
                                jsonFormData['event-filter']['json-path-filter'] = jsonpathfilter;
                            }
                            else {
                                delete jsonFormData['event-filter'];
                            }

                            formData = JSON.stringify(jsonFormData);

                            var url = (action.toLowerCase() === 'edit') ? setEventSubscriptionUrl(subscription.id) : setEventSubscriptionUrl();
                            var method = (action.toLowerCase() === 'edit') ? 'PUT' : 'POST';
                            var successMessages = {
                                'Create': 'Created new event subscription',
                                'Edit' : 'Edited event subscription',
                                'Clone' : 'Created new event subscription'
                            };

                            XNAT.xhr.ajax({
                                url: url,
                                data: formData,
                                method: method,
                                contentType: 'application/json',
                                success: function(){
                                    XNAT.ui.banner.top(2000,successMessages[action],'success');
                                    eventServicePanel.refreshSubscriptionList();
                                },
                                fail: function(e){
                                    errorHandler(e,'Could not create event subscription')
                                }
                            })
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true
                    }
                ]
            })
        // }
        // else {
        //     errorHandler({}, 'Could not load projects');
        // }
    };

    eventServicePanel.editSubscription = function(action,subscriptionId) {
        action = action || "Edit";
        if (!subscriptionId) return false;

        XNAT.xhr.getJSON({
            url: getEventSubscriptionUrl(subscriptionId),
            success: function(subscriptionData){
                eventServicePanel.modifySubscription(action,subscriptionData);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve event subscription details');
            }
        })
    };

    eventServicePanel.toggleSubscription = function(id,selector){
        // if underlying checkbox has just been checked, take action to enable this subscription
        var enableMe = $(selector).prop('checked');
        if (enableMe){
            eventServicePanel.enableSubscription(id);
        }
        else {
            eventServicePanel.disableSubscription(id);
        }
    };

    eventServicePanel.enableSubscription = function(id,refresh){
        refresh = refresh || false;
        XNAT.xhr.ajax({
            url: setEventSubscriptionUrl(id,'/activate'),
            method: 'POST',
            success: function(){
                XNAT.ui.banner.top(2000,'Event subscription enabled','success');
                if (refresh) eventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e,'Could not enable event subscription')
            }
        })
    };
    eventServicePanel.disableSubscription = function(id,refresh){
        refresh = refresh || false;
        XNAT.xhr.ajax({
            url: setEventSubscriptionUrl(id,'/deactivate'),
            method: 'POST',
            success: function(){
                XNAT.ui.banner.top(2000,'Event subscription disabled','success');
                if (refresh) eventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e,'Could not disable event subscription');
            }
        })
    };

    eventServicePanel.deleteSubscriptionConfirmation = function(subscription){


        XNAT.ui.dialog.open({
            title: 'Confirm Deletion',
            width: 350,
            content: '<p>Are you sure you want to permanently delete the <strong>'+ escapeHTML(subscription.name) +'</strong> event subscription? This operation cannot be undone. Alternately, you can just disable it.</p>',
            buttons: [
                {
                    label: 'Confirm Delete',
                    isDefault: true,
                    close: true,
                    action: function(){
                        eventServicePanel.deleteSubscription(subscription.id);
                    }
                },
                {
                    label: 'Disable',
                    close: true,
                    action: function(){
                        eventServicePanel.disableSubscription(subscription.id,true);
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    };
    eventServicePanel.deleteSubscription = function(id){
        if (!id) return false;
        XNAT.xhr.ajax({
            url: getEventSubscriptionUrl(id),
            method: 'DELETE',
            success: function(){
                XNAT.ui.banner.top(2000,'Permanently deleted event subscription', 'success');
                eventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e, 'Could not delete event subscription');
            }
        })
    };

    /* browser event listeners */

    $(document).off('click','#create-new-subscription').on('click', '#create-new-subscription', function(e){
        // console.log(e);
        XNAT.admin.eventServicePanel.modifySubscription('Create');
    });

    $(document).off('change','select[name=project-id]').on('change','select[name=project-id]', function(){
        findActions($(this));
    });
    $(document).off('change','select[name=event-id]').on('change','select[name=event-id]', function(){
        findActions($(this));
    });
    $(document).off('change','select[name=action-key]').on('change','select[name=action-key]', function(){
        getActionAttributes($(this));
    });
    $(document).off('click','#set-sub-action-attributes').on('click','#set-sub-action-attributes', function(e){
        e.preventDefault();
        var $form = $(this).parents('form');
        var actionKey = $form.find('select[name=action-key]').find('option:selected').val();
        var attributesObj = eventServicePanel.actions[actionKey].attributes;
        eventServicePanel.enterAttributesDialog(attributesObj);
    });

    /* ---------------------------------- *
     * Display Event Subscription History *
     * ---------------------------------- */

    var historyTable, historyData;

    XNAT.admin.eventServicePanel.historyTable = historyTable =
        getObject(XNAT.admin.eventServicePanel.historyTable || {});

    XNAT.admin.eventServicePanel.historyData = historyData =
        getObject(XNAT.admin.eventServicePanel.historyData || {});

    function viewHistoryDialog(e, onclose){
        e.preventDefault();
        var historyId = $(this).data('id') || $(this).closest('tr').prop('title');
        eventServicePanel.historyTable.viewHistory(historyId);
    }

    function getHistoryUrl(project,sub){
        var params = [];
        if (project) params.push('projectid='+project);
        if (sub) params.push('subscriptionid='+sub);
        var appended = (params.length) ? '?'+params.join('&') : '';

        return XNAT.url.rootUrl('/xapi/events/delivered'+appended);
    }

    historyTable.getHistory = function(opts,callback){
        var project = (opts) ? opts.project : false;
        var subscription = (opts) ? opts.subscription : false;

        return XNAT.xhr.getJSON({
            url: getHistoryUrl(project,subscription),
            success: function(data){
                if (data.length){
                    data.forEach(function(entry){
                        historyData[entry.id] = entry;
                    });

                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could Not Get History');
            }
        })
    };

    historyTable.table = function(data){
        var $datarows = [];

        return {
            kind: 'table.dataTable',
            name: 'event-subscription-history',
            id: 'event-subscription-history',
            data: data,
            table: {
                classes: 'highlight hidden',
                on: [
                    ['click', 'a.view-history', viewHistoryDialog ]
                ]
            },
            trs: function(tr,data){
                tr.id = data.id;
                addDataAttrs(tr, {filter: '0' });
            },
            sortable: '',
            filter: '',
            items: {
                // by convention, name 'custom' columns with ALL CAPS
                // 'custom' columns do not correspond directly with
                // a data item
                DATE: {
                    label: 'Date',
                    th: { className: 'left' },
                    td: { className: 'left mono'},
                    filter: function(table){
                        var MIN = 60*1000;
                        var HOUR = MIN*60;
                        var X8HRS = HOUR*8;
                        var X24HRS = HOUR*24;
                        var X7DAYS = X24HRS*7;
                        var X30DAYS = X24HRS*30;
                        return spawn('div.center', [XNAT.ui.select.menu({
                            value: 0,
                            options: {
                                all: {
                                    label: 'All',
                                    value: 0,
                                    selected: true
                                },
                                lastHour: {
                                    label: 'Last Hour',
                                    value: HOUR
                                },
                                last8hours: {
                                    label: 'Last 8 Hrs',
                                    value: X8HRS
                                },
                                last24hours: {
                                    label: 'Last 24 Hrs',
                                    value: X24HRS
                                },
                                lastWeek: {
                                    label: 'Last Week',
                                    value: X7DAYS
                                },
                                last30days: {
                                    label: 'Last 30 days',
                                    value: X30DAYS
                                }
                            },
                            element: {
                                id: 'filter-select-container-timestamp',
                                on: {
                                    change: function(){
                                        var FILTERCLASS = 'filter-timestamp';
                                        var selectedValue = parseInt(this.value, 10);
                                        var currentTime = Date.now();
                                        $dataRows = $dataRows.length ? $dataRows : $$(table).find('tbody').find('tr');
                                        if (selectedValue === 0) {
                                            $dataRows.removeClass(FILTERCLASS);
                                        }
                                        else {
                                            $dataRows.addClass(FILTERCLASS).filter(function(){
                                                var timestamp = this.querySelector('input.container-timestamp');
                                                var containerLaunch = +(timestamp.value);
                                                return selectedValue === containerLaunch-1 || selectedValue > (currentTime - containerLaunch);
                                            }).removeClass(FILTERCLASS);
                                        }
                                    }
                                }
                            }
                        }).element])
                    },
                    apply: function(){
                        var timestamp = 0, dateString;
                        if (this.status.length > 0){
                            timestamp = status[0]['timestamp'];
                            dateString = new Date(timestamp);
                            dateString = dateString.toISOString().replace('T',' ').replace('Z',' ').split('.')[0];

                        } else {
                            dateString = 'N/A';
                        }
                        return spawn('!',[
                            spawn('span', dateString ),
                            spawn('input.hidden.subscription-timestamp.filtering|type=hidden', { value: timestamp } )
                        ])
                    }
                },
                SUBSCRIPTION: {
                    label: 'Subscription',
                    th: { className: 'left' },
                    td: { className: 'left' },
                    apply: function(){
                        return this.subscription.name;
                    }
                },
                ACTION: {
                    label: 'Action',
                    th: { className: 'left' },
                    td: { className: 'left' },
                    apply: function(){
                        return eventServicePanel.actions[ this.subscription['action-key'] ];
                    }
                },
                user: {
                    label: 'Run As User',
                    th: { className: 'left' },
                    td: { className: 'left' },
                    apply: function(){
                        return this.user;
                    }
                },
                PROJECT: {
                    label: 'Project',
                    th: { className: 'left' },
                    td: { className: 'left' },
                    apply: function(){
                        return this.subscription['project-id'];
                    }
                }
            }

        }
    };

    historyTable.viewHistory = function(id){
        if (historyData[id]) {
            var historyEntry = XNAT.admin.historyData[id];
            var historyDialogButtons = [
                {
                    label: 'OK',
                    isDefault: true,
                    close: true
                }
            ];

            // build nice-looking history entry table
            var pheTable = XNAT.table({
                className: 'xnat-table compact',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            });

            // add table header row
            pheTable.tr()
                .th({ addClass: 'left', html: '<b>Key</b>' })
                .th({ addClass: 'left', html: '<b>Value</b>' });

            for (var key in historyEntry){
                var val = historyEntry[key], formattedVal = '';
                if (Array.isArray(val)) {
                    var items = [];
                    val.forEach(function(item){
                        if (typeof item === 'object') item = JSON.stringify(item);
                        items.push(spawn('li',[ spawn('code',item) ]));
                    });
                    formattedVal = spawn('ul',{ style: { 'list-style-type': 'none', 'padding-left': '0' }}, items);
                } else if (typeof val === 'object' ) {
                    formattedVal = spawn('code', JSON.stringify(val));
                } else if (!val) {
                    formattedVal = spawn('code','false');
                } else {
                    formattedVal = spawn('code',val);
                }

                pheTable.tr()
                    .td('<b>'+key+'</b>')
                    .td([ spawn('div',{ style: { 'word-break': 'break-all','max-width':'600px' }}, formattedVal) ]);

                // check logs and populate buttons at bottom of modal
                if (key === 'log-paths') {
                    // returns an array of log paths
                    historyEntry[key].forEach(function(logPath){
                        if (logPath.indexOf('stdout.log') > 0) {
                            historyDialogButtons.push({
                                label: 'View StdOut.log',
                                close: false,
                                action: function(){
                                    historyTable.viewLog(historyEntry['container-id'],'stdout')
                                }
                            });
                        }
                        if (logPath.indexOf('stderr.log') > 0) {
                            historyDialogButtons.push({
                                label: 'View StdErr.log',
                                close: false,
                                action: function(){
                                    historyTable.viewLog(historyEntry['container-id'],'stderr')
                                }
                            })
                        }
                    });
                }
            }

            // display history
            XNAT.ui.dialog.open({
                title: historyEntry['wrapper-name'],
                width: 800,
                scroll: true,
                content: pheTable.table,
                buttons: historyDialogButtons
            });
        } else {
            console.log(id);
            XNAT.ui.dialog.open({
                content: 'Sorry, could not display this history item.',
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true
                    }
                ]
            });
        }
    };

    /* ------------------------- *
     * Initialize tabs & Display *
     * ------------------------- */

   eventServicePanel.populateDisplay = function(rootDiv) {
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
                            tag: 'div#subscriptionFilters',
                            contents: {
                                addNewSubscription: {
                                    tag: 'button#create-new-subscription.pull-right.btn1',
                                    contents: 'Add New Event Subscription'
                                },
                                clearfix: {
                                    tag: 'div.clear.clearfix',
                                    contents: '<br>'
                                }
                            }
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

        eventServicePanel.showSubscriptionList();

        XNAT.ui.tab.activate('subscription-tab');
    };

   eventServicePanel.showSubscriptionList = eventServicePanel.refreshSubscriptionList = function(container){
       var $container = $(container || '#subscriptionTableContainer');
       $container
           .empty()
           .append( eventServicePanel.subscriptionTable() );
   };

    eventServicePanel.init = function(){

        // Prerequisite: Get known events
        // translate events array into an object driven by the event ID

        eventServicePanel.getEvents().done(function(events){
            events.forEach(function(event){
                eventServicePanel.events[event.id] = event;
            });

            eventServicePanel.getActions().done(function(actions){
                actions.forEach(function(action){
                    eventServicePanel.actions[action['action-key']] = action;
                });

                // Populate event subscription table
                eventServicePanel.populateDisplay();
            });

        });

        // initialize arrays of values that we'll need later
        eventServicePanel.getProjects().done(function(data){
            eventServicePanel.projects = data.ResultSet.Result;
        });

    };

}));