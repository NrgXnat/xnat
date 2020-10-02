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

console.log('xnat/admin/eventServiceUi.js');

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

    function titleCase(string){
        var words = string.split(' ');
        words.forEach(function(word,i){
            words[i] = word[0].toUpperCase() + word.slice(1).toLowerCase();
        });
        return words.join(' ');
    }


    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var eventServicePanel,
        undefined,
        rootUrl = XNAT.url.rootUrl,
        restUrl = XNAT.url.restUrl,
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
        return restUrl('/data/projects?format=json');
    }

    function getEventActionsUrl(projectId,eventType){
        var path = (eventType) ?
            '/xapi/events/actionsbyevent?event-type='+eventType :
            '/xapi/events/allactions';

        if (eventType && projectId) path += '&project='+projectId;
        return restUrl(path);
    }

    function getEventSubscriptionUrl(id){
        id = id || false;
        var path = (id) ? '/xapi/events/subscription/'+id : '/xapi/events/subscriptions';
        return restUrl(path);
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
                errorHandler(e,'Could not retrieve projects','silent');
            }
        })
    };

    eventServicePanel.getStatus = function(callback){
        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: restUrl('/xapi/events/prefs'),
            success: function(data){
                if (data) return data;
                callback.apply(this,arguments);
            },
            fail: function(e){
                errorHandler(e,'Could not retrieve Event Service status','silent');
            }
        })
    };

    eventServicePanel.getEvents = function(callback){
        callback = isFunction(callback) ? callback : function(){};

        return XNAT.xhr.getJSON({
            url: XNAT.url.restUrl('/xapi/events/events'),
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
                errorHandler(e,'Could not retrieve event subscriptions','silent');
            }
        })
    };

    eventServicePanel.getActions = function(opts,callback){
        var project = (opts) ? opts.project : false;
        var xsiType = (opts) ? opts.xsiType : false;

        callback = isFunction(callback) ? callback : function(){};

        if (project) xmodal.loading.open('Getting available actions');

        return XNAT.xhr.getJSON({
            url: getEventActionsUrl(project,xsiType),
            done: xmodal.loading.close(),
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
            .th('<b>ID</b>')
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
        function displayProjects(projects){
            if (isArray(projects) && projects.length) {
                return projects.join(', ');
            }
            else {
                return 'All Projects';
            }
        }
        function eventNiceName(subscription){
            var eventId = subscription['event-filter']['event-type'];
            if (eventServicePanel.events[eventId]) {
                return eventServicePanel.events[eventId]['display-name'] + ': ' + titleCase(subscription['event-filter']['status']);
            }
            else return 'Unknown event: '+eventId;
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
                data = data.sort(function(a,b){ return (a.id > b.id) ? 1 : -1 });

                data.forEach(function(subscription){
                    subTable.tr({ addClass: (subscription.valid) ? 'valid' : 'invalid', id: 'event-subscription-'+subscription.id, data: { id: subscription.id } })
                        .td(subscription['id'])
                        .td([ subscriptionNiceLabel(subscription.name,subscription.id) ])
                        .td([ displayProjects(subscription['event-filter']['project-ids']) ])
                        .td([ eventNiceName(subscription) ])
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
                kind: 'panel.select.multiple',
                name: 'project-id',
                label: 'Select Project',
                id: 'subscription-project-selector',
                element: {
                    // html: '<option selected value="">Any Project</option>'
                },
                order: 30,
                onchange: XNAT.admin.eventServicePanel.projectSubscriptionCheck
            },
            subProjGlobalSelect: {
                kind: 'panel.element',
                html: '<label class="disabled"><input type="checkbox" id="subscription-anyproject-selector" disabled /> Apply to All Projects</label>',
                order: 31
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
        // var project = $form.find('select[name=project-id]').find('option:selected').val();
        var project, projectArray = [], projects = $form.find('select[name=project-id]').find('option:selected');
        projects.each(function(selectedProject) {
            projectArray.push($(this).val())
        });
        project = projectArray.join(',');
        var xsiType = $form.find('select[name=event-selector]').find('option:selected').data('xsitype');
        var eventType = $form.find('select[name=event-selector]').find('option:selected').data('event-type');
        var inheritedAction = $form.find('input[name=inherited-action]').val(); // hack to stored value for edited subscription
        var actionSelector = $form.find('select[name=action-key]');
        var url;

        if (project && actionSelector) {
            url = getEventActionsUrl(project,eventType);
        }
        else if (actionSelector) {
            url = getEventActionsUrl(false,eventType);
        }
        else {
            url = getEventActionsUrl();
        }

        if (project) xmodal.loading.open('Getting Available Actions...');

        XNAT.xhr.get({
            url: url,
            done: xmodal.loading.close(),
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

    // display Action Attributes after editing
    function setActionAttributes($element){
        var $form = $element.parents('form');
        var actionKey = $form.find('select[name=action-key]').find('option:selected').val();
        var storedAttributes = $form.find('#sub-action-attribute-preview').html();
        var genericAttributes = eventServicePanel.actions[actionKey].attributes;

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

        eventServicePanel.enterAttributesDialog(attributesObj,genericAttributes,eventServicePanel.actions[actionKey]['display-name']);
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

    eventServicePanel.enterAttributesDialog = function(attributesObj,genericAttributes,actionLabel){
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
        eventServicePanel.subscriptionAttributes = "";
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

        XNAT.ui.dialog.open({
            title: action + ' Event Subscription',
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

                // when editing an existing event subscription, always show the attributes preview panel
                $form.find('#subscription-action-preview').show();

                Object.keys(eventServicePanel.events).forEach(function(event){
                    var thisEvent = eventServicePanel.events[event];

                    var optGroup = [];
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
                });

                if (subscription){
                    // Prepopulate / preselect form fields if we are editing an existing subscription.
                    // This involves a bit of manipulation of object properties between the subscription and the form elements

                    var subscriptionData = subscription;

                    eventServicePanel.subscriptionAttributes = subscription.attributes;

                    subscriptionData['project-id'] = subscription['event-filter']['project-ids'][0];
                    // subscriptionData['project-id'] = subscription['event-filter']['project-ids'];
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
                    // special case for collapsing project-ids into the multiselect input
                    subscription['event-filter']['project-ids'].forEach(function(project){
                        $form.find('select[name=project-id]').find('option[value='+project+']').prop('selected','selected');
                    });

                    findActions($form.find('#subscription-event-selector'));
                    $form.addClass((subscription.valid) ? 'valid' : 'invalid');

                    // custom set event selector

                    if (Object.keys(subscription.attributes).length) {
                        $form.find('#subscription-action-preview').show();
                        $form.find('#sub-action-attribute-preview').html( JSON.stringify(subscription.attributes) );
                    }
                }
                else delete eventServicePanel.subscriptionAttributes;

                if (!subscription || !subscription['event-filter']['project-ids'].length) {
                    $form.find('#subscription-anyproject-selector').prop('checked','checked');
                }

                // Create form-specific event handlers, enable them after setValues() has run
                $form.off('change','select[name=project-id]').on('change','select[name=project-id]', function(e){
                    findActions($(this));
                    eventServicePanel.projectSubscriptionCheck(e);
                });
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
            buttons: [
                {
                    label: 'OK',
                    isDefault: true,
                    close: false,
                    action: function(obj){
                        // Convert form inputs to a parseable JSON object
                        // This also involves a conversion into the accepted JSON attribute hierarchy
                        var formData, jsonFormData = {}, projectArray = [];
                        var formArrayData = obj.dialog$.find('form').serializeArray();
                        formArrayData.map(function(x){jsonFormData[x.name] = x.value;});

                        // accommodate multiple selected projects
                        formArrayData.filter(function(item){
                            if (item.name === 'project-id') projectArray.push(item.value);
                            return;
                        });
                        jsonFormData['project-id'] = projectArray.join(',');

                        if (eventServicePanel.subscriptionAttributes) {
                            jsonFormData.attributes = (typeof eventServicePanel.subscriptionAttributes === 'object') ?
                                eventServicePanel.subscriptionAttributes :
                                JSON.parse(eventServicePanel.subscriptionAttributes);
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
                            // projectArray.push(jsonFormData['project-id']);
                            jsonFormData['event-filter']['project-ids'] = projectArray;
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
                                eventServicePanel.refreshSubscriptionList();
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
            ]
        })
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

    eventServicePanel.projectSubscriptionCheck = function(e){
        e.preventDefault();
        var element = $(e.target),
            selected = element.find('option:selected');
        if (selected.length) {
            $('#subscription-anyproject-selector').prop('checked',false)
        }
        else $('#subscription-anyproject-selector').prop('checked','checked');
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
            content: '<p>Are you sure you want to permanently delete the <strong>'+ escapeHTML(subscription.name) +'</strong> event subscription? This will also delete all event history items associated with this event. This operation cannot be undone. Alternatively, you can just disable it.</p>',
            buttons: [
                {
                    label: 'Confirm Delete',
                    isDefault: true,
                    close: true,
                    action: function(){
                        eventServicePanel.deleteSubscription(subscription.id);
                        eventServicePanel.historyTable.refresh();
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


    /* ------------------------- *
     * Initialize tabs & Display *
     * ------------------------- */

   eventServicePanel.populateDisplay = function(status,rootDiv) {
        var $container = $(rootDiv || '#event-service-admin-tabs');
        $container.empty();

        var eventSetupTab =  {
            kind: 'tab',
            label: 'Event Setup',
            group: 'General',
            active: true,
            contents: {
                enablePanel: {
                    kind: 'panel',
                    label: 'Enable Event Service',
                    contents: {
                        enableEsSetting: {
                            tag: 'div#enableEventService'
                        }
                    }
                },
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
                eventSetupTab: eventSetupTab,
                historyTab: historyTab
            }
        };

        eventServicePanel.tabSet = XNAT.spawner.spawn({ eventSettings: eventTabSet });
        eventServicePanel.tabSet.render($container);

        eventServicePanel.showSubscriptionList(false,status);

        XNAT.ui.tab.activate('event-setup-tab');
    };

   eventServicePanel.showSubscriptionList = eventServicePanel.refreshSubscriptionList = function(container,status){
       var $container = $(container || '#subscriptionTableContainer');

       if (status === undefined || status.toString() === 'true') {
           $container
               .empty()
               .append( eventServicePanel.subscriptionTable() );
           return;
       }
       $container
           .empty()
           .append(spawn('p','Event Service subscriptions are disabled.'));
       $('#subscriptionFilters').empty();
   };


    /* ****************************** */
    /* Enable / Disable Event Service */
    /* ****************************** */

    var prefs = eventServicePanel.prefs = {};

    eventServicePanel.changeEventServiceStatus = function(prefs,enable){
        var msg = (enable) ? 'Enabling Event Service' : 'Disabling Event Service';
        eventServicePanel.prefs['enabled'] = enable;

        xmodal.loading.open(msg);
        XNAT.xhr.putJSON({
            url: csrfUrl('/xapi/events/prefs'),
            data: JSON.stringify(eventServicePanel.prefs),
            processData: false,
            fail: function(e){
                errorHandler(e,'Could not store Event Service preferences');
            },
            success: function(prefs){
                console.log(prefs);
                eventServicePanel.prefs = prefs;
                eventServicePanel.init(prefs);
                xmodal.loading.close();
                XNAT.ui.banner.top(3000,'Event Service status updated','success');
            }
        })
    };

    eventServicePanel.displayStatus = function(prefs,container){

        function switchbox(status){
            var enabled = (status === 'true'),
                value = enabled;
            return XNAT.ui.panel.switchbox({
                name: 'eventServiceStatus',
                label: 'Enable Event Service',
                description: 'Enables or Disables all event subscriptions in the XNAT Event Service. (Note: Does not affect automations defined elsewhere.)',
                onText: 'Enabled',
                offText: 'Disabled',
                checked: enabled,
                value: value,
                onchange: XNAT.admin.eventServicePanel.handleStatusSwitchbox
            })
        }

        var $container = $(container || '#enableEventService'),
           status = prefs.enabled.toString() || 'true';
        $container
           .empty()
           .append(switchbox(status));
    };

    // $(document).on('change','input[name=eventServiceStatus]',function(e){
    eventServicePanel.handleStatusSwitchbox = function(e){
        e.preventDefault();
        // at the moment of change being recorded, the input still has its value prior to change
        var originalVal = ($(this).val().toString() === 'true'),
           intendedVal = !originalVal;

        var prefs = eventServicePanel.prefs;
        prefs.enabled = intendedVal;

        if (originalVal.toString === 'true') {
           XNAT.ui.dialog.confirm({
               title: 'Confirm Disabling of Event Service',
               content: 'Are you sure you want to disable the Event Service for this XNAT site?',
               buttons: [
                   {
                       label: 'Disable Event Service',
                       isDefault: true,
                       close: true,
                       action: function(){
                           XNAT.admin.eventServicePanel.changeEventServiceStatus(prefs,intendedVal)
                       }
                   },
                   {
                       label: 'Cancel',
                       close: true,
                       action: function(){

                       }
                   }
               ]
           })
        }
        else {
            XNAT.admin.eventServicePanel.changeEventServiceStatus(prefs,intendedVal)
        }
    };

    eventServicePanel.init = function(){

        eventServicePanel.getStatus().done(function(prefs){
            eventServicePanel.prefs = prefs;
            var status = eventServicePanel.prefs.enabled;

            // Prerequisite: Get known events
            // translate events array into an object driven by the event ID

            eventServicePanel.getEvents().done(function(events){
                events.forEach(function(event){
                    eventServicePanel.events[event.type] = event;
                });

                eventServicePanel.getActions().done(function(actions){
                    actions.forEach(function(action){
                        eventServicePanel.actions[action['action-key']] = action;
                    });

                    // Populate event subscription table
                    eventServicePanel.populateDisplay(status);

                    // Display status
                    eventServicePanel.displayStatus(prefs);

                    // initialize history table
                    eventServicePanel.historyTable.init(status);
                });

            });

        });

        // initialize arrays of values that we'll need later
        eventServicePanel.getProjects().done(function(data){
            data = data.ResultSet.Result;
            data = data.sort(function(a,b){ return (a['secondary_ID'].toLowerCase() < b['secondary_ID'].toLowerCase()) ? -1 : 1 });
            eventServicePanel.projects = data;
        });
    };

}));
