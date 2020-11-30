/*
 * web: projEventService.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Project-specific functions for the Event Service
 */

console.log('xnat/admin/projEventService.js');

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

    function errorHandler(e, title, silent, closeAll){
        title = (title) ? 'Error Found: '+ title : 'Error';
        console.log(title);
        console.warn(e);
        if (closeAll) {
            xmodal.closeAll();
        }
        if (!silent) {
            // closeAll = (closeAll === undefined) ? true : closeAll;
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
    }

    function titleCase(string){
        var words = string.split(' ');
        words.forEach(function(word,i){
            words[i] = word[0].toUpperCase() + word.slice(1).toLowerCase();
        });
        return words.join(' ');
    }

    function getUrlParams(){
        var paramObj = {};

        // get the querystring param, redacting the '?', then convert to an array separating on '&'
        var urlParams = window.location.search.substr(1,window.location.search.length);
        urlParams = urlParams.split('&');

        urlParams.forEach(function(param){
            // iterate over every key=value pair, and add to the param object
            param = param.split('=');
            paramObj[param[0]] = param[1];
        });

        return paramObj;
    }

    function getProjectId(){
        if (XNAT.data.context.projectID.length > 0) return XNAT.data.context.projectID;
        return getUrlParams().id;
    }


    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var projEventServicePanel,
        undefined,
        projectId = getProjectId(),
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
        csrfUrl = XNAT.url.csrfUrl;



    XNAT.admin =
        getObject(XNAT.admin || {});

    XNAT.admin.projEventServicePanel = projEventServicePanel =
        getObject(XNAT.admin.projEventServicePanel || {});

    projEventServicePanel.xsiTypes = [
        'xnat:projectData',
        'xnat:subjectData',
        'xnat:imageSessionData',
        'xnat:imageScanData'
    ];

    projEventServicePanel.events = {};
    projEventServicePanel.actions = {};

    function getEventActionsUrl(eventType){
        var path = (eventType) ?
            '/xapi/projects/'+projectId+'/events/actionsbyevent?event-type='+eventType :
            '/xapi/projects/'+projectId+'/events/actions';

        return restUrl(path);
    }

    function getAllActionsUrl(){
        var path = '/xapi/events/allactions';
        return restUrl(path);
    }

    function getEventSubscriptionUrl(id){
        id = id || false;
        var path = (id) ?
            '/xapi/projects/'+projectId+'/events/subscription/'+id :
            '/xapi/projects/'+projectId+'/events/subscriptions';
        return restUrl(path);
    }

    function setEventSubscriptionUrl(id,appended){
        id = id || false;
        var path = (id) ?
            '/xapi/projects/'+projectId+'/events/subscription/'+id :
            '/xapi/projects/'+projectId+'/events/subscription';
        appended = (appended) ? '/'+appended : '';
        return csrfUrl(path + appended);
    }

    projEventServicePanel.getEvents = function(callback){
        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/xapi/events/events'),
            // url: XNAT.url.restUrl('/xapi/projects/'+projectId+'/events/events'),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve events','silent');
            }
        })
    };

    projEventServicePanel.getSubscriptions = function(callback){
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
                errorHandler(e,'Could not retrieve event subscriptions','silent');
            }
        })
    };

    projEventServicePanel.getActions = function(opts,callback){

        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: getAllActionsUrl(),
            success: function(data){
                if (data) {
                    return data;
                }
                callback.apply(this, arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve event actions','silent');
            }
        })
    };


    /* -------------------------- *
     * Subscription Display Table *
     * -------------------------- */

    var subTable = function(subscriptions){
        /* Formatted table cells */
        function subscriptionNiceLabel(subscription){
            var action = (subscription.editable) ? 'Edit' : 'View';
            var appended = (subscription['event-filter']['project-ids'].length !== 1) ?
                spawn('em.small', { style: { 'font-size': '11px', display: 'block' }}, 'Multi-project Subscription') :
                '';
            return spawn('!', [
                spawn('a',{
                    href: '#!',
                    style: { 'font-weight': 'bold' },
                    onclick: function(e){
                        e.preventDefault();
                        projEventServicePanel.editSubscription(action,subscription.id);
                    }
                }, subscription.name),
                appended
            ]);
        }
        function eventNiceName(subscription){
            var eventId = subscription['event-filter']['event-type'];
            if (projEventServicePanel.events[eventId]) {
                return projEventServicePanel.events[eventId]['display-name'] + ': ' + titleCase(subscription['event-filter']['status']);
            }
            else return 'Unknown event: '+eventId;
        }
        function actionNiceName(actionKey){
            return (projEventServicePanel.actions[actionKey]) ?
                projEventServicePanel.actions[actionKey]['display-name'] :
                spawn('!',[
                    spawn('i',{ title: actionKey },'Unknown'),
                    spacer(4),
                    spawn('i.fa.fa-info-circle',{title: actionKey })
                ]);
        }
        function subscriptionEnabledCheckbox(subscription){
            var enabled = !!subscription.active;
            // Do not return a user-settable switchbox if it is not considered editable by a project owner
            if (!subscription.editable) return spawn('i',{title: 'Subscription set by Admin'}, enabled);

            var ckbox = spawn('input.subscription-enabled', {
                type: 'checkbox',
                checked: enabled,
                value: 'true',
                onchange: function(){
                    projEventServicePanel.toggleSubscription(subscription.id, this);
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
            var disabled = (!subscription.editable);
            var classes = (disabled) ?
                'btn btn-sm disabled' :
                'btn btn-sm';
            return spawn('button', {
                disabled: disabled,
                addClass: classes,
                onclick: function(e){
                    e.preventDefault();
                    projEventServicePanel.editSubscription('Edit',subscription.id);
                },
                title: 'Edit'
            }, [ spawn('span.fa.fa-pencil') ]);
        }
        function cloneSubscriptionButton(subscription){
            return spawn('button.btn.sm', {
                onclick: function(e){
                    e.preventDefault();
                    projEventServicePanel.editSubscription('Clone',subscription.id);
                },
                title: 'Duplicate'
            }, [ spawn('span.fa.fa-clone') ]);
        }
        function deleteSubscriptionButton(subscription){
            var disabled = (!subscription.editable);
            var classes = (disabled) ?
                'btn btn-sm disabled' :
                'btn btn-sm';
            return spawn('button', {
                disabled: disabled,
                addClass: classes,
                onclick: function(e){
                    e.preventDefault();
                    projEventServicePanel.deleteSubscriptionConfirmation(subscription);
                },
                title: 'Delete'
            }, [ spawn('span.fa.fa-trash') ]);
        }

        return {
            kind: 'table.dataTable',
            name: 'adminEventSubscriptionList',
            id: 'adminEventSubscriptionList',
            data: subscriptions,
            table: {},
            before: {
                filterCss: {
                    tag: 'style|type=text/css',
                    content: '\n' +
                        'td.align-top { vertical-align: top } \n'
                }
            },
            trs: function(tr, data){
                tr.id = "tr-" + data.id;
                addDataAttrs(tr, { filter: '0', data: data.id });
                tr.classList += (data.valid) ? ' valid' : ' invalid';
            },
            sortable: 'name, event, action, owner',
            items: {
                name: {
                    label: 'Name',
                    filter: true,
                    td: { className: 'name word-wrapped align-top' },
                    apply: function(){
                        return subscriptionNiceLabel(this)
                    }
                },
                event: {
                    label: 'Trigger Event',
                    filter: true,
                    td: { className: 'event word-wrapped align-top' },
                    apply: function(){
                        return eventNiceName(this)
                    }
                },
                action: {
                    label: 'Action',
                    filter: true,
                    td: { className: 'action word-wrapped align-top' },
                    apply: function(){
                        return actionNiceName(this['action-key'])
                    }
                },
                owner: {
                    label: 'Owner',
                    filter: true,
                    td: { className: 'owner' },
                    apply: function(){
                        return this['subscription-owner']
                    }
                },
                enabled: {
                    label: 'Enabled',
                    filter: false,
                    td: { className: 'enabled' },
                    apply: function(){
                        return subscriptionEnabledCheckbox(this)
                    }
                },
                ACTIONS: {
                    label: 'Actions',
                    filter: false,
                    td: { className: 'ACTIONS nowrap' },
                    apply: function(){
                        return spawn('div.center',[
                            editSubscriptionButton(this),
                            spacer(4),
                            cloneSubscriptionButton(this),
                            spacer(4),
                            deleteSubscriptionButton(this)
                        ]);
                    }
                }
            }
        }

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
                name: 'event-selector',
                label: 'Select Event',
                id: 'subscription-event-selector',
                element: emptyOptionObj,
                order: 20
            },
            subEventType: {
                kind: 'panel.input.hidden',
                name: 'event-type',
                id: 'subscription-event-type'
            },
            subEventStatus: {
                kind: 'panel.input.hidden',
                name: 'status',
                id: 'subscription-event-status'
            },
            subProjSelector: {
                kind: 'panel.input.hidden',
                name: 'project-id',
                id: 'subscription-project-selector',
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
                name: 'payload-filter',
                label: 'Event Payload Filter',
                description: 'Optional. Enter filter in JSON path notation without enclosing brackets, e.g. <pre style="margin-top:0">(@.xsiType == "xnat:mrScanData")</pre>',
                order: 50
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

    // populate the hidden Status field based on selected event
    function setEventStatus($element){
        var $form = $element.parents('form');
        var status = $element.find('option:selected').data('status');
        var eventType = $element.find('option:selected').data('event-type');
        $form.find('input[name=status]').val(status);
        $form.find('input[name=event-type]').val(eventType);
    }

    // populate the Action Select menu based on selected project and event (which provides xsitype)
    function findActions($element){
        var $form = $element.parents('form');
        var project = $form.find('input[name=project-id]').val();
        var xsiType = $form.find('select[name=event-selector]').find('option:selected').data('xsitype');
        var eventType = $form.find('select[name=event-selector]').find('option:selected').data('event-type');
        var inheritedAction = $form.find('input[name=inherited-action]').val(); // hack to stored value for edited subscription
        var actionSelector = $form.find('select[name=action-key]');
        var url;

        if (project && actionSelector) {
            url = getEventActionsUrl(eventType);
        }
        else if (actionSelector) {
            url = getEventActionsUrl(eventType);
        }
        else {
            url = getEventActionsUrl();
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
                            // $form.find('input[name=inherited-action]').val('');
                        }

                        actionSelector.append( spawn('option', { value: action['action-key'], selected: selected }, action['display-name'] ))
                        // if the action has attributes, add them to the global actions object
                        if (action['attributes']) {
                            projEventServicePanel.actions[action['action-key']].attributes = action['attributes'];
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
            if (projEventServicePanel.actions[actionId].attributes && projEventServicePanel.actions[actionId].attributes !== {}) {
                $form.find('#subscription-action-preview').slideDown(300);
            }
            else {
                $form.find('#subscription-action-preview').slideUp(300);
            }
        }
    }

    // display Action Attributes after editing
    function setActionAttributes($element){
        var $form = $element.parents('form');
        var actionKey = $form.find('select[name=action-key]').find('option:selected').val();
        var storedAttributes = $form.find('#sub-action-attribute-preview').html();
        var genericAttributes = projEventServicePanel.actions[actionKey].attributes;

        var attributesObj = Object.assign({}, genericAttributes);

        if (storedAttributes.length) {
            storedAttributes = JSON.parse(storedAttributes);

            // overwrite any generic values with saved values
            Object.keys(storedAttributes).forEach(function(key,val){
                attributesObj[key] = val;
            });

            // if any generic values were ignored, zero them out
            Object.keys(genericAttributes).forEach(function(key){
                if (storedAttributes[key] === undefined || storedAttributes[key].length === 0) {
                    attributesObj[key] = '';
                }
            })
        }

        projEventServicePanel.enterAttributesDialog(attributesObj,genericAttributes,projEventServicePanel.actions[actionKey]['display-name']);
    }
    function renderAttributeInput(name,props,opts){

        var obj = {
            label: name,
            name: name,
            description: props.description
        };

        if (props.required) obj.validation = 'required';
        obj.value = props['default-value'] || '';

        var el;

        if (projEventServicePanel.subscriptionAttributes) {
            var presetAttributes = (typeof projEventServicePanel.subscriptionAttributes === "string") ?
                JSON.parse(projEventServicePanel.subscriptionAttributes) :
                projEventServicePanel.subscriptionAttributes;
            obj.value = presetAttributes[name];
        }

        switch (props.type){
            case 'boolean':
                obj.onText = 'On';
                obj.offText = 'Off';
                obj.value = obj.value || 'true';

                // allow provided opts to override these defaults
                if (opts) obj = Object.assign( obj, opts );
                el = XNAT.ui.panel.input.switchbox(obj);
                break;

            default:
                if (opts) obj = Object.assign( obj, opts );
                el = XNAT.ui.panel.input.text(obj);
        }

        return el;
    }

    projEventServicePanel.enterAttributesDialog = function(attributesObj,genericAttributes,actionLabel){
        var inputElements;
        if (Object.keys(attributesObj).length > 0) {
            inputElements = [];
            Object.keys(attributesObj).forEach(function(name){
                var opts = {};
                var props = attributesObj[name];

                // check to see if supplied attribute is a part of the basic set of supported attributes
                if (Object.keys(genericAttributes).indexOf(name) < 0) opts = { addClass: 'invalid', description: 'This parameter is not natively supported by this action and may be ignored' };

                inputElements.push( renderAttributeInput(name,props,opts) );
            });
            inputElements = spawn('!',inputElements);
        }
        else {
            return false
        }
        projEventServicePanel.subscriptionAttributes = "";
        XNAT.ui.dialog.open({
            width: 450,
            title: false,
            content: '<h3 style="font-weight: normal;">Enter Attributes for '+ titleCase(actionLabel) +'</h3>' +
                '<form class="xnat-form-panel panel panel-default" id="attributes-form" style="border: none; padding-top: 1em;">' +
                '<div id="attributes-elements-container"></div></form>',
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
                        projEventServicePanel.subscriptionAttributes = JSON.stringify($form);
                        $(document).find('#sub-action-attribute-preview').html(projEventServicePanel.subscriptionAttributes);
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    };

    projEventServicePanel.modifySubscription = function(action,subscription){
        // var projs = projEventServicePanel.projects;
        subscription = subscription || false;
        action = action || 'Create';

        var saveButtons = [
            {
                label: 'OK',
                isDefault: true,
                close: false,
                action: function(obj){
                    // Convert form inputs to a parseable JSON object
                    // This also involves a conversion into the accepted JSON attribute hierarchy
                    var formData, jsonFormData = {}, projectArray = [];
                    obj.dialog$.find('form').serializeArray().map(function(x){jsonFormData[x.name] = x.value;});

                    if (projEventServicePanel.subscriptionAttributes) {
                        jsonFormData.attributes = (typeof projEventServicePanel.subscriptionAttributes === 'object') ?
                            projEventServicePanel.subscriptionAttributes :
                            JSON.parse(projEventServicePanel.subscriptionAttributes);
                    }
                    else {
                        jsonFormData.attributes = {};
                    }

                    jsonFormData['event-filter'] = {};

                    jsonFormData['event-filter']['event-type'] = jsonFormData['event-type'];
                    delete jsonFormData['event-type'];

                    jsonFormData['event-filter']['status'] = jsonFormData['status'];
                    delete jsonFormData['status'];

                    delete jsonFormData['inherited-action'];

                    if (jsonFormData['project-id']) {
                        jsonFormData['event-filter']['project-ids'] = [jsonFormData['project-id']];
                        delete jsonFormData['project-id'];
                    }
                    if (jsonFormData['payload-filter']) {
                        jsonFormData['event-filter']['payload-filter'] = jsonFormData['payload-filter'];
                        delete jsonFormData['payload-filter'];
                    } else {
                        jsonFormData['event-filter']['payload-filter'] = '';
                    }
                    if (!jsonFormData['active']) jsonFormData['active'] = false;
                    if (!jsonFormData['act-as-event-user']) jsonFormData['act-as-event-user'] = false;

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
                            projEventServicePanel.refreshSubscriptionList();
                            XNAT.dialog.closeAll();
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
        ];

        var viewButtons = [
            {
                label: 'OK',
                isDefault: true,
                close: true
            }
        ];

        XNAT.ui.dialog.open({
            title: action + ' Event Subscription',
            width: 600,
            content: '<div id="subscription-form-container"></div>',
            beforeShow: function(obj){
                var $container = obj.$modal.find('#subscription-form-container');
                XNAT.spawner.spawn({ form: createFormObj }).render($container);

                var $form = obj.$modal.find('form');

                if (action.toLowerCase() === "view") {
                    $form.find('div[data-name=active]').remove();
                }

                $form.find('#subscription-project-selector').val(getProjectId());

                // when editing an existing event subscription, always show the attributes preview panel
                $form.find('#subscription-action-preview').show();

                Object.keys(projEventServicePanel.events).forEach(function(event){
                    var thisEvent = projEventServicePanel.events[event];

                    var optGroup = [];
                    if (thisEvent['event-scope'].indexOf('PROJECT') >= 0) {
                        thisEvent.statuses.forEach(function(status){
                            optGroup.push(spawn(
                                'option',
                                { value: event+':'+status, data: {
                                    xsitype: thisEvent['xnat-type'],
                                    status: status,
                                    'event-type': event
                                }},
                                thisEvent['display-name'] + ' -- ' + titleCase(status)
                            ));
                        });
                        $form.find('#subscription-event-selector')
                            .append(spawn('optgroup', optGroup));
                    }
                });

                if (subscription){
                    // Prepopulate / preselect form fields if we are editing an existing subscription.
                    // This involves a bit of manipulation of object properties between the subscription and the form elements

                    var subscriptionData = subscription;

                    projEventServicePanel.subscriptionAttributes = subscription.attributes;

                    // subscriptionData['project-id'] = subscription['event-filter']['project-ids'][0];
                    subscriptionData['project-id'] = projectId;
                    subscriptionData['event-type'] = subscription['event-filter']['event-type'];
                    subscriptionData['status'] = subscription['event-filter']['status'];
                    subscriptionData['event-selector'] = subscription['event-filter']['event-type'] + ':' + subscription['event-filter']['status'];
                    subscriptionData['payload-filter'] = subscription['event-filter']['payload-filter'];
                    subscriptionData['inherited-action'] = subscription['action-key'];

                    if (action.toLowerCase() === "clone") {
                        delete subscriptionData.name;
                        delete subscriptionData.id;
                        delete subscriptionData['registration-key'];
                    }
                    if (action.toLowerCase() === "edit") {
                        $form.find('input[name=id]').prop('disabled',false);
                    }

                    $form.setValues(subscriptionData); // sets values in inputs and selectors, which triggers the onchange listeners below. Action has to be added again after the fact.
                    findActions($form.find('#subscription-event-selector'));
                    $form.addClass((subscription.valid) ? 'valid' : 'invalid');

                    // custom set event selector

                    if (Object.keys(subscription.attributes).length) {
                        $form.find('#subscription-action-preview').show();
                        $form.find('#sub-action-attribute-preview').html( JSON.stringify(subscription.attributes) );
                    }
                }
                else delete projEventServicePanel.subscriptionAttributes;

                if (action.toLowerCase() === "view") {
                    $form.find('input').addClass('disabled').prop('disabled','disabled');
                    $form.find('select').addClass('disabled').prop('disabled','disabled');
                    $form.find('textarea').addClass('disabled').prop('disabled','disabled');
                    $form.find('#set-sub-action-attributes').parents('p').remove();
                }

                $form.off('change','select[name=event-selector]').on('change','select[name=event-selector]', function(){
                    findActions($(this));
                    setEventStatus($(this));
                });
                $form.off('change','select[name=action-key]').on('change','select[name=action-key]', function(){
                    getActionAttributes($(this));
                });
                $form.off('click','#set-sub-action-attributes').on('click','#set-sub-action-attributes', function(e){
                    e.preventDefault();
                    setActionAttributes($(this));
                });
            },
            buttons: (action.toLowerCase() === "view") ?
                viewButtons :
                saveButtons
        })
    };

    projEventServicePanel.editSubscription = function(action,subscriptionId) {
        action = action || "Edit";
        if (!subscriptionId) return false;

        XNAT.xhr.getJSON({
            url: getEventSubscriptionUrl(subscriptionId),
            success: function(subscriptionData){
                projEventServicePanel.modifySubscription(action,subscriptionData);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve event subscription details');
            }
        })
    };

    projEventServicePanel.toggleSubscription = function(id,selector){
        // if underlying checkbox has just been checked, take action to enable this subscription
        var enableMe = $(selector).prop('checked');
        if (enableMe){
            projEventServicePanel.enableSubscription(id);
        }
        else {
            projEventServicePanel.disableSubscription(id);
        }
    };

    projEventServicePanel.enableSubscription = function(id,refresh){
        refresh = refresh || false;
        XNAT.xhr.ajax({
            url: setEventSubscriptionUrl(id,'/activate'),
            method: 'POST',
            success: function(){
                XNAT.ui.banner.top(2000,'Event subscription enabled','success');
                if (refresh) projEventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e,'Could not enable event subscription')
            }
        })
    };
    projEventServicePanel.disableSubscription = function(id,refresh){
        refresh = refresh || false;
        XNAT.xhr.ajax({
            url: setEventSubscriptionUrl(id,'/deactivate'),
            method: 'POST',
            success: function(){
                XNAT.ui.banner.top(2000,'Event subscription disabled','success');
                if (refresh) projEventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e,'Could not disable event subscription');
            }
        })
    };

    projEventServicePanel.deleteSubscriptionConfirmation = function(subscription){


        XNAT.ui.dialog.open({
            title: 'Confirm Deletion',
            width: 350,
            content: '<p>Are you sure you want to permanently delete the <strong>'+ escapeHTML(subscription.name) +'</strong> event subscription? This will also delete all event history items associated with this event. This operation cannot be undone. Alternatively, you can just disable it.</p>',
            buttons: [
                {
                    label: 'Confirm Delete',
                    isDefault: true,
                    close: true,
                    action: function(){
                        projEventServicePanel.deleteSubscription(subscription.id);
                        projEventServicePanel.projHistoryTable.refresh();
                    }
                },
                {
                    label: 'Disable',
                    close: true,
                    action: function(){
                        projEventServicePanel.disableSubscription(subscription.id,true);
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        })
    };
    projEventServicePanel.deleteSubscription = function(id){
        if (!id) return false;
        XNAT.xhr.ajax({
            url: getEventSubscriptionUrl(id),
            method: 'DELETE',
            success: function(){
                XNAT.ui.banner.top(2000,'Permanently deleted event subscription', 'success');
                projEventServicePanel.refreshSubscriptionList();
            },
            fail: function(e){
                errorHandler(e, 'Could not delete event subscription');
            }
        })
    };

    /* browser event listeners */

    $(document).off('click','#create-new-subscription').on('click', '#create-new-subscription', function(e){
        // console.log(e);
        XNAT.admin.projEventServicePanel.modifySubscription('Create');
    });

    /* ------------------------- *
     * Initialize tabs & Display *
     * ------------------------- */

    projEventServicePanel.populateDisplay = function(rootDiv) {
        var $container = $(rootDiv || '#project-eventservice-tabs');
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
                        subscriptionVerticalSpacer: {
                            tag: 'br'
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
                eventHistoryPanel: {
                    kind: 'panel',
                    label: 'Event Subscription History',
                    footer: false,
                    contents: {
                        eventHistoryContainer: {
                            tag: 'div#history-table-container'
                        }
                    }
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

        projEventServicePanel.tabSet = XNAT.spawner.spawn({ eventSettings: eventTabSet });
        projEventServicePanel.tabSet.render($container);

        projEventServicePanel.showSubscriptionList();

        XNAT.ui.tab.activate('subscription-tab');
    };

    projEventServicePanel.showSubscriptionList = projEventServicePanel.refreshSubscriptionList = function(container){
        var $container = $(container || '#subscriptionTableContainer');

        projEventServicePanel.getSubscriptions().done(function(data) {
            var subscriptionTable;

            if (data.length) {
                data = data.sort(function (a, b) {
                    return (a.id > b.id) ? 1 : -1
                });
                subscriptionTable = XNAT.spawner.spawn({
                    sTable: subTable(data)
                });
                subscriptionTable.done(function(){
                    $container.empty();
                    this.render($container)
                });
            }
            else {
                $container.empty().append('<p>No event subscriptions have been created.</p>');
            }

            return;
        })

        // $container
        //     .empty()
        //     .append( projEventServicePanel.subscriptionTable() );
    };

    projEventServicePanel.init = function(){

        function projectLink(cfg, txt){
            return spawn('a.last', extend(true, {
                href: rootUrl('/data/projects/' + projectId + '?format=html'),
                style: { textDecoration: 'underline' },
                data: { projectId: projectId }
            }, cfg), txt || projectId );
        }

        $('.project-link').each(function(){
            this.appendChild(projectLink())
        });

        // Prerequisite: Get known events
        // translate events array into an object driven by the event ID

        projEventServicePanel.getEvents().done(function(events){
            events.forEach(function(event){
                projEventServicePanel.events[event.type] = event;
            });

            projEventServicePanel.getActions().done(function(actions){
                actions.forEach(function(action){
                    projEventServicePanel.actions[action['action-key']] = action;
                });

                // Populate event subscription table
                projEventServicePanel.populateDisplay();

                // initialize history table
                projEventServicePanel.projHistoryTable.init();
            });

        });
    };

}));
