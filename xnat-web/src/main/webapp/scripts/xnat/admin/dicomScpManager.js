/*
 * web: dicomScpManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Manage DICOM SCP Receivers
 */

console.log('dicomScpManager.js');

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

    // Element identifiers.
    const CUSTOM_ROUTING_WARNING_ID   = 'customRoutingWarningID';
    const DICOM_OBJECT_IDENTIFIER     = 'dicomObjectIdentifier';
    const PROJECT_ROUTING_EXPRESSION  = 'projectRoutingExpression';
    const ROUTING_EXPRESSIONS_ENABLED = 'routingExpressionsEnabled';
    const SESSION_ROUTING_EXPRESSION  = 'sessionRoutingExpression';
    const SUBJECT_ROUTING_EXPRESSION  = 'subjectRoutingExpression';
    const EXPRESSIONS                 = [PROJECT_ROUTING_EXPRESSION, SUBJECT_ROUTING_EXPRESSION, SESSION_ROUTING_EXPRESSION];

    let dicomScpManager;
    restUrl = XNAT.url.restUrl;

    XNAT.admin =
        getObject(XNAT.admin || {});

    XNAT.admin.dicomScpManager = dicomScpManager =
        getObject(XNAT.admin.dicomScpManager || {});

    dicomScpManager.samples = [
        {
            "aeTitle": "Bogus",
            "enabled": true,
            "fileNamer": "string",
            "identifier": "string",
            "port": 0,
            "scpId": "BOGUS"
        },
        {
            "enabled": true,
            "fileNamer": "string",
            "identifier": "string",
            "port": 8104,
            "scpId": "XNAT",
            "aeTitle": "XNAT"
        }
    ];

    function spacer(width){
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    function scpUrl(appended, cacheParam){
        appended = appended ? '/' + appended : '';
        return restUrl('/xapi/dicomscp' + appended, '', cacheParam || false);
    }

    function formatAeTitleAndPort(aeTitle, port){
        return aeTitle + ':' + port;
    }

    // keep track of used ports to help prevent port conflicts
    dicomScpManager.usedAeTitlesAndPorts = [];

    // keep track of scpIds to prevent id conflicts
    dicomScpManager.ids = [];

    // get the list of DICOM SCP Receivers
    dicomScpManager.getReceivers = dicomScpManager.getAll = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        dicomScpManager.usedAeTitlesAndPorts = [];
        dicomScpManager.ids = [];
        return XNAT.xhr.get({
            url: scpUrl(null, true),
            dataType: 'json',
            success: function(data){
                dicomScpManager.receivers = data;
                // refresh the 'usedAeTitlesAndPorts' array every time this function is called
                data.forEach(function(item){
                    dicomScpManager.usedAeTitlesAndPorts.push(formatAeTitleAndPort(item.aeTitle, item.port));
                    dicomScpManager.ids.push(item.id);
                });
                callback.apply(this, arguments);
            }
        });
    };

    dicomScpManager.getIdentifiers = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: restUrl('/xapi/dicomscp/identifiers'),
            dataType: 'json',
            success: callback,
            fail: function(e){ console.log(e.status, e.statusText);}
        })
    };

    dicomScpManager.getReceiver = dicomScpManager.getOne = function(id, callback){
        if (!id) return null;
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: scpUrl(id, true),
            dataType: 'json',
            success: callback
        });
    };

    dicomScpManager.get = function(id){
        if (!id) {
            return dicomScpManager.getAll();
        }
        return dicomScpManager.getOne(id);
    };

    dicomScpManager.isCustomRoutingCapable = function( identityString) {
        return DICOM_OBJECT_IDENTIFIER === identityString;
    }

    // dialog to create/edit receivers
    dicomScpManager.dialog = function(item, isNew){

        var doWhat = !item ? 'New' : 'Edit';
        var oldPort = item && item.port ? item.port : null;
        var oldTitle = item && item.aeTitle ? item.aeTitle : null;
        var modalDimensions = { height: '320px', width: '650px' }

        isNew = firstDefined(isNew, doWhat === 'New');

        console.log(isNew);

        item = getObject(item);

        if (item['identifier'] === undefined) item['identifier'] = DICOM_OBJECT_IDENTIFIER;

        var $container = spawn('div.dicom-scp-editor-container');

        // spawn the editor form directly into the dialog (no template)
        XNAT.spawner
            .resolve('siteAdmin/dicomScpEditor')
            .ok(function(){

                var spawneri = this;

                var $form = spawneri.get$().find('form[name="dicomScpEditor"]');

                var identifiers = dicomScpManager.identifiers || {};

                // collect <option> elements
                var options = [];

                Object.keys(identifiers).forEach(function(identifier){
                    var label = (identifier === DICOM_OBJECT_IDENTIFIER) ? identifier+ ' (Default)' : identifier;
                    var option = spawn('option', {
                        value: identifier,
                        html: label
                    });
                    if (item.identifier !== undefined && item.identifier === identifier) {
                        option.setAttribute('selected','selected');
                        option.selected = true;
                    }
                    options.push(option)
                });

                let $identifierSelect = null;
                if (Object.keys(identifiers).length > 1) {

                    $identifierSelect = $form.find('#scp-identifier');

                    // un-hide the menu
                    $identifierSelect.append(options)
                                    .disabled(false)
                                    .hidden(false);

                    // un-hide the menu element container
                    $identifierSelect.closest('.panel-element')
                                    .hidden(false)

                } else {
                    // explicitly store the default XNAT identifier value with the SCP receiver     definition
                    $form.find('#scp-identifier').parents('.panel-element').empty().append(
                        `<input type="hidden" name="identifier" value="${DICOM_OBJECT_IDENTIFIER}" />`
                    );
                }

                if (isNew) { item.enabled = true }

                if (isDefined(item.id)) {
                    // SET VALUES IN EDITOR DIALOG
                    // $form.setValues(item);
                    $form.find('[name="id"]').val(item.id);
                    $form.find('[name="aeTitle"]').val(item.aeTitle);
                    $form.find('[name="port"]').val(item.port);
                    $form.find('[name="enabled"]').val(item.enabled);
                    $form.find('[name="customProcessing"]').prop('checked', item.customProcessing).val(item.customProcessing);
                    $form.find('[name="directArchive"]').prop('checked', item.directArchive).val(item.directArchive);
                    $form.find('[name="anonymizationEnabled"]').prop('checked', item.anonymizationEnabled).val(item.anonymizationEnabled);
                    $form.find('[name="whitelistEnabled"]').prop('checked', item.whitelistEnabled).val(item.whitelistEnabled);
                    $form.find('[name="whitelistText"]').val(item.whitelist.join('\r\n'));
                    $form.find(`[name="${ROUTING_EXPRESSIONS_ENABLED}"]`).prop('checked', item.routingExpressionsEnabled).val(item.routingExpressionsEnabled);
                    $form.find(`[name="${PROJECT_ROUTING_EXPRESSION}"]`).val(item.projectRoutingExpression);
                    $form.find(`[name="${SUBJECT_ROUTING_EXPRESSION}"]`).val(item.subjectRoutingExpression);
                    $form.find(`[name="${SESSION_ROUTING_EXPRESSION}"]`).val(item.sessionRoutingExpression);
                }else{
                    // Set default value for anonymization to true if this is a new receiver
                    $form.find('[name="anonymizationEnabled"]').prop('checked', true).val(true);
                }

                spawneri.render($container);

                var scpEditorDialog = XNAT.dialog.open({
                    title: doWhat + ' DICOM SCP Receiver',
                    content: $container,
                    width: modalDimensions.width,
                    beforeShow: function(obj){
                        var $whitelistEnabled = obj.dialog$.find('[name="whitelistEnabled"]');
                        if($whitelistEnabled[0].checked){
                            obj.dialog$.find('[data-name="whitelistText"]').show();
                        }else{
                            obj.dialog$.find('[data-name="whitelistText"]').hide();
                        }

                        $whitelistEnabled.on('change', function(){
                            if(this.checked){
                                obj.dialog$.find('[data-name="whitelistText"]').show();
                            }else{
                                obj.dialog$.find('[data-name="whitelistText"]').hide();
                            }

                        });
                        let identifier = item['identifier'];

                        dicomScpManager.setIdentifierDescription(identifier, obj.dialog$);
                        dicomScpManager.setCustomRoutingVisibility(identifier, obj.dialog$);

                        let $customRoutingSwitch = obj.dialog$.find(`[name="${ROUTING_EXPRESSIONS_ENABLED}"]`);
                        if( $identifierSelect !== null) {

                            let previousIdentifier;
                            $identifierSelect.on('focus', function (){
                                previousIdentifier = this.value
                            });
                            $identifierSelect.on('change', function (){
                                dicomScpManager.confirmIdentifierChange( this, previousIdentifier, $customRoutingSwitch[0], obj.dialog$);

                                previousIdentifier = $identifierSelect[0].value
                            });
                        }

                        $customRoutingSwitch.on('change', function(){
                            if(this.checked){
                                obj.dialog$.find(`[data-name="${PROJECT_ROUTING_EXPRESSION}"]`).show();
                                obj.dialog$.find(`[data-name="${SUBJECT_ROUTING_EXPRESSION}"]`).show();
                                obj.dialog$.find(`[data-name="${SESSION_ROUTING_EXPRESSION}"]`).show();
                            }else{
                                obj.dialog$.find(`[data-name="${PROJECT_ROUTING_EXPRESSION}"]`).hide();
                                obj.dialog$.find(`[data-name="${SUBJECT_ROUTING_EXPRESSION}"]`).hide();
                                obj.dialog$.find(`[data-name="${SESSION_ROUTING_EXPRESSION}"]`).hide();
                            }
                        });
                    },
                    // height: modalDimensions.height,
                    scroll: false,
                    padding: 0,
                    buttons: [
                        {
                            label: 'Save',
                            close: false,
                            isDefault: true,
                            action: function(obj){

                                // the form panel is 'dicomScpEditor' in site-admin-elements.yaml

                                var $formPanel = obj.dialog$.find('form[name="dicomScpEditor"]');
                                var $aeTitle = $formPanel.find('[name="aeTitle"]');
                                var $port = $formPanel.find('[name="port"]');

                                dicomScpManager.setCheckBoxVal("customProcessing");
                                dicomScpManager.setCheckBoxVal("directArchive");
                                dicomScpManager.setCheckBoxVal("anonymizationEnabled");
                                dicomScpManager.setCheckBoxVal("whitelistEnabled");
                                dicomScpManager.setCheckBoxVal(ROUTING_EXPRESSIONS_ENABLED);

                                // set the value for 'whitelist' on-the-fly
                                var whitelistText$ = $formPanel.find('[name="whitelistText"]');
                                if (whitelistText$.length) {
                                    new Set(whitelistText$[0].value.split(/\r?\n/).map(i => i.trim()).filter(i => i)).forEach(i => {
                                        var input$ = spawn('input|type=text', { name: "whitelist[]", className: 'hidden' });
                                        input$.value = i;
                                        $formPanel.append(input$);
                                    });
                                }

                                console.log(item.id);

                                if (isNew) {
                                    // make sure new receivers are enabled by default
                                    $formPanel.find('[name="enabled"]').val(true);
                                }

                                $formPanel.submitJSON({
                                    method: isNew ? 'POST' : 'PUT',
                                    url: isNew ? scpUrl() : scpUrl(item.id),
                                    validate: function(){

                                        $formPanel.find(':input').removeClass('invalid');

                                        var errors = 0;
                                        var errorMsg = 'Errors were found with the following fields: <ul>';

                                        var portVal = $port.val() * 1;

                                        // port must be less than 65535
                                        if (portVal < 1 || portVal >= 65535) {
                                            errors++;
                                            errorMsg += '<li><b>Port</b> value must be between <b>1</b> and <b>65535</b></li>';
                                        }
                                        else {
                                            [$port, $aeTitle].forEach(function($el){
                                                var el = $el[0];
                                                if (!el.value) {
                                                    errors++;
                                                    errorMsg += '<li><b>' + el.title + '</b> is required.</li>';
                                                    $el.addClass('invalid');
                                                }
                                            });
                                        }

                                        var newPort = portVal;
                                        console.log(newPort);

                                        var newTitle = $aeTitle.val().trim();
                                        $aeTitle.val(newTitle);
                                        console.log(newTitle);

                                        if(!XNAT.validation.regex.aeTitle.test(newTitle)){
                                            errors++;
                                            errorMsg += "<li>Invalid AE-title: " + (newTitle == "" ? "EMPTY" : newTitle) + "</li>";
                                            $aeTitle.addClass('invalid');
                                        }

                                        var newAeTitleAndPort = formatAeTitleAndPort(newTitle, newPort);

                                        // only check for port conflicts if we're changing the port
                                        if (newTitle + '' !== oldTitle + '' || newPort + '' !== oldPort + '') {
                                            dicomScpManager.usedAeTitlesAndPorts.forEach(function(usedAeTitleAndPort){
                                                if (usedAeTitleAndPort + '' === newAeTitleAndPort + '') {
                                                    errors++;
                                                    errorMsg += '<li>The AE title and port <b>' + newAeTitleAndPort + '</b> is already in use. Please use another AE title or port number.</li>';
                                                    $port.addClass('invalid');
                                                    return false;
                                                }
                                            });
                                        }

                                         var whitelistErr = 0;
                                         $formPanel.find('[name="whitelist[]"]').each(function(){
                                              var splitElement = this.value.split("@");
                                              if(splitElement.length == 2) {
                                                  if(!XNAT.validation.regex.aeTitle.test(splitElement[0]) || !XNAT.validation.regex.cidr.test(splitElement[1])){
                                                     whitelistErr++;
                                                     errorMsg += "<li>Invalid whitelist element: " + this.value + "</li>";
                                                  }
                                              } else if(splitElement.length == 1) {
                                                  if(!XNAT.validation.regex.aeTitle.test(splitElement[0]) && !XNAT.validation.regex.cidr.test(splitElement[0])){
                                                     whitelistErr++;
                                                     errorMsg += "<li>Invalid whitelist element: " + this.value + "</li>";
                                                  }
                                              } else{
                                                  whitelistErr++;
                                                  errorMsg += "<li>Invalid whitelist element: " + this.value + "</li>";
                                              }
                                          });

                                        errorMsg += '</ul>';

                                         if(whitelistErr > 0){
                                            $formPanel.find('[name="whitelistText"]').addClass('invalid');
                                            errors += whitelistErr;
                                            errorMsg += "Note: AE-titles must be no more than 16 characters and must not contain backslash or control characters.  IP Addresses must be a valid IP and can end with an optional CIDR subnet mask suffix. "
                                         }

                                        if (errors > 0) {
                                            XNAT.dialog.message('Errors Found', errorMsg);
                                            $formPanel.find('[name="whitelist[]"]').each(function(){ this.remove(); });
                                        }

                                        return errors === 0;

                                    },
                                    success: function(){
                                        refreshTable();
                                        scpEditorDialog.close();
                                        XNAT.ui.banner.top(2000, 'Saved.', 'success');
                                    },
                                    fail: function(o){
                                        XNAT.dialog.message('Failed to Save DICOM Receiver. ', o.responseText);
                                        $formPanel.find('[name="whitelist[]"]').each(function(){ this.remove(); });
                                    }
                                });
                            }
                        },
                        {
                            label: 'Cancel',
                            close: true
                        }
                    ]
                });

            });

    };

    dicomScpManager.setIdentifierDescription = function( identifier, $mainDialog) {
        let description = $mainDialog.find('[data-name="identifier"]').find('[class="description"]')[0];
        let isCapable = dicomScpManager.isCustomRoutingCapable( identifier);
        let $span = $(description).find(`#${CUSTOM_ROUTING_WARNING_ID}`);
        if( $span.length > 0) {
            if( $span[0].parentNode) {
                $span[0].parentNode.removeChild( $span[0]);
            }
        }

        const span = document.createElement("span");
        span.setAttribute("id",`${CUSTOM_ROUTING_WARNING_ID}`);
        if( ! isCapable) {
            span.innerHTML = "<br>The selected identifier does not support custom routing.";
            description.appendChild( span);
        }
    }

    dicomScpManager.confirmIdentifierChange = function( identifierSelect, previousIdentifier, isRoutingEnabledSwitch, $mainDialog) {
        let isConflict = isRoutingEnabledSwitch.checked && !dicomScpManager.isCustomRoutingCapable( identifierSelect.value);
        if( isConflict) {
            XNAT.dialog.open({
                title: 'Unsupported Per-Receiver Routing.',
                content: ` <p>
                          The selected identifier '${identifierSelect.value}' does not support per-receiver routing but per-receiver routing is enabled. 
                          Select OK to disable per-receiver routing or Cancel to restore previously selected identifier.
                       </p>`,
                beforeShow: function (obj) {
                },
                scroll: false,
                mask: true,
                buttons: [
                    {
                        label: 'OK',
                        default: true,
                        close: true,
                        action: function( obj) {
                            isRoutingEnabledSwitch.checked = false;

                            dicomScpManager.setIdentifierDescription(identifierSelect.value, $mainDialog);
                            dicomScpManager.setCustomRoutingVisibility(identifierSelect.value, $mainDialog);
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true,
                        action: function( obj) {
                            identifierSelect.value = previousIdentifier;
                            isRoutingEnabledSwitch.checked = true;

                            dicomScpManager.setIdentifierDescription(previousIdentifier, $mainDialog);
                            dicomScpManager.setCustomRoutingVisibility(previousIdentifier, $mainDialog);
                        }
                    }
                ]
            });
        } else {

            dicomScpManager.setIdentifierDescription(identifierSelect.value, $mainDialog);
            dicomScpManager.setCustomRoutingVisibility(identifierSelect.value, $mainDialog);
        }
    }

    dicomScpManager.setCustomRoutingVisibility = function( identifier, $dialog) {
        let isCapable = dicomScpManager.isCustomRoutingCapable( identifier);
        let isRoutingEnabledSwitch = $dialog.find(`[name="${ROUTING_EXPRESSIONS_ENABLED}"]`)[0];
        let $swtchPanel = $dialog.find(`[data-name="${ROUTING_EXPRESSIONS_ENABLED}"]`).filter('.panel-switchbox');

        const shouldShow = isCapable && isRoutingEnabledSwitch.checked;

        if( isCapable) {
            $swtchPanel.show();
        } else {
            $swtchPanel.hide();
        }

        EXPRESSIONS.forEach(expression => {
            const locator = $dialog.find(`[data-name="${expression}"]`)
            if (shouldShow) {
                locator.show()
            } else {
                locator.hide()
            }
        })
    }

    dicomScpManager.setCheckBoxVal = function(name){
        let $formPanel = jq('form[name="dicomScpEditor"]');
        let $container = $formPanel.find('[name="' + name + '"]');
        if ($container.length) {
            $container[0].value = $container[0].checked + '';
        }
    }

    // create table for DICOM SCP receivers
    dicomScpManager.table = function(container, callback){

        // initialize the table - we'll add to it below
        var scpTable = XNAT.table({
            className: 'dicom-scp-receivers xnat-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        // add table header row
        scpTable.tr()
                .th({ addClass: 'left', html: '<b>AE Title</b>' })
                .th('<b>Port</b>')
                .th('<b>Identifier</b>').addClass((Object.keys(dicomScpManager.identifiers).length > 1) ? '' : 'hidden') // only show this if there are multiple identifiers
                .th('<b>Archive Behavior</b>')
                .th('<b>Enabled</b>')
                .th('<b>Actions</b>');

        // TODO: move event listeners to parent elements - events will bubble up
        // ^-- this will reduce the number of event listeners
        function enabledCheckbox(item){
            var enabled = !!item.enabled;
            var ckbox = spawn('input.dicom-scp-enabled', {
                type: 'checkbox',
                checked: enabled,
                value: enabled,
                data: { id: item.id, name: item.aeTitle },
                onchange: function(){
                    // save the status when clicked
                    var checkbox = this;
                    enabled = checkbox.checked;
                    XNAT.xhr.put({
                        url: scpUrl(item.id + '/enabled/' + enabled),
                        success: function(){
                            var status = (enabled ? ' enabled' : ' disabled');
                            checkbox.value = enabled;
                            XNAT.ui.banner.top(1000, '<b>' + item.aeTitle + '</b> ' + status, 'success');
                            console.log(item.aeTitle + status)
                        }
                    });
                }
            });
            return spawn('div.center', [
                ['label.switchbox|title=' + item.aeTitle, [
                    ckbox,
                    ['span.switchbox-outer', [['span.switchbox-inner']]]
                ]]
            ]);
        }

        function editLink(item, text){
            return spawn('a.link|href=#!', {
                onclick: function(e){
                    e.preventDefault();
                    if (item && item.id) {
                        dicomScpManager.getReceiver(item.id, function(data){
                            dicomScpManager.dialog(data, false);
                        });
                    }
                    else {
                        dicomScpManager.dialog({}, false);
                    }
                }
            }, [['b', text]]);
        }

        function editButton(item){
            return spawn('button.btn.sm.edit', {
                onclick: function(e){
                    e.preventDefault();
                    if (item && item.id) {
                        dicomScpManager.getReceiver(item.id, function(data){
                            dicomScpManager.dialog(data, false);
                        });
                    }
                    else {
                        dicomScpManager.dialog({}, false);
                    }
                }
            }, 'Edit');
        }

        function deleteButton(item){
            return spawn('button.btn.sm.delete', {
                onclick: function(){
                    XNAT.dialog.confirm({
                        // height: 220,
                        title: 'Delete receiver?',
                        scroll: false,
                        content: '' +
                        "<p>Are you sure you'd like to delete the '<b>" + item.aeTitle + "</b>' DICOM Receiver?</p>" +
                        '<p><b><i class="fa fa-exclamation-circle"></i> This action cannot be undone.</b></p>' +
                        "",
                        okLabel: "Delete",
                        okAction: function(){
                            console.log('delete id ' + item.id);
                            XNAT.xhr.delete({
                                url: scpUrl(item.id),
                                success: function(){
                                    console.log('"' + item.aeTitle + '" deleted');
                                    XNAT.ui.banner.top(1000, '<b>"' + item.aeTitle + '"</b> deleted.', 'success');
                                    refreshTable();
                                }
                            });
                        }
                    })
                }
            }, 'Delete');
        }

        function displayBehavior(item){
            item.identifier = item.identifier || DICOM_OBJECT_IDENTIFIER;
            var archiveBehavior = (item.directArchive) ? 'Direct Archive Behavior Enabled' : 'Uses Standard Prearchive Behavior';
            var customRemapping = (item.customProcessing) ? 'Custom Remapping Enabled' : 'Uses Standard Anonymization';
            var whitelist = (item.whitelistEnabled) ? 'AE-Title Whitelist Enabled' : 'AE-Title Whitelist Disabled';
            var routingExpressionsEnabled = (item.routingExpressionsEnabled) ? 'Receiver-Specific Routing Enabled' : 'Receiver-Specific Routing Disabled';
            var anonymizationEnabled = (item.anonymizationEnabled) ? 'Anonymization Enabled' : 'Anonymization Disabled';
            var projectRouting = (item.identifier === DICOM_OBJECT_IDENTIFIER) ? 'Uses Standard Project Routing' :
                (item.identifier.slice(0,3) === 'dqr') ? 'DQR Routing Enabled' : 'Uses Custom Project Routing';
            return spawn('ul', {
                style: { 'margin': '0', 'padding-left': '1.5em' }
            },[
                [ 'li', archiveBehavior ],
                [ 'li', customRemapping ],
                [ 'li', projectRouting ],
                [ 'li', whitelist ],
                [ 'li', anonymizationEnabled ],
                [ 'li', routingExpressionsEnabled ]
            ]);
        }

        dicomScpManager.getAll().done(function(data){
            data.forEach(function(item){
                var identifierLabel = item.identifier || DICOM_OBJECT_IDENTIFIER;
                identifierLabel += (identifierLabel === DICOM_OBJECT_IDENTIFIER) ? ' (Default)' : '';
                scpTable.tr({ title: item.aeTitle, data: { id: item.id, port: item.port } })
                        .td({ style: 'max-width: 180px' },[editLink(item, item.aeTitle)]).addClass('aeTitle word-wrapped')
                        .td([['div.mono.center', item.port]]).addClass('port')
                        .td(identifierLabel).addClass((Object.keys(dicomScpManager.identifiers).length > 1) ? '' : 'hidden') // only show this if there are multiple identifiers
                        .td({ style: 'min-width: 150px' },[displayBehavior(item)]).addClass('behavior')
                        .td([enabledCheckbox(item)]).addClass('status')
                        .td([['div.center', [editButton(item), spacer(4), deleteButton(item)]]]).addClass('nowrap');
            });

            if (container) {
                $$(container).append(scpTable.table);
            }

            if (isFunction(callback)) {
                callback(scpTable.table);
            }

        });

        dicomScpManager.$table = $(scpTable.table);

        return scpTable.table;
    };

    dicomScpManager.init = function(container){

        dicomScpManager.getIdentifiers().done(function(data){

            dicomScpManager.identifiers = data;

            var $manager = $$(container || 'div#dicom-scp-manager');

            dicomScpManager.$container = $manager;

            $manager.append(dicomScpManager.table());
            // dicomScpManager.table($manager);

            var newReceiver = spawn('button.new-dicomscp-receiver.btn.btn-sm.submit', {
                html: 'New DICOM SCP Receiver',
                onclick: function(){
                    dicomScpManager.dialog(null, true);
                }
            });

            var startAll = spawn('button.start-receivers.btn.btn-sm', {
                html: 'Start All',
                onclick: function(){
                    XNAT.xhr.put({
                        url: scpUrl('start'),
                        success: function(){
                            console.log('DICOM SCP Receivers started')
                        }
                    })
                }
            });

            var stopAll = spawn('button.stop-receivers.btn.btn-sm', {
                html: 'Stop All',
                onclick: function(){
                    XNAT.xhr.put({
                        url: scpUrl('stop'),
                        success: function(){
                            console.log('DICOM SCP Receivers stopped')
                        }
                    })
                }
            });

            // add the start, stop, and 'add new' buttons at the bottom
            $manager.append(spawn('div', [
                // startAll,
                // spacer(10),
                // stopAll,
                newReceiver,
                ['div.clear.clearfix']
            ]));

            return {
                element: $manager[0],
                spawned: $manager[0],
                get: function(){
                    return $manager[0]
                }
            };

        });
    };

    function refreshTable(){
        dicomScpManager.$table.remove();
        dicomScpManager.table(null, function(table){
            dicomScpManager.$container.prepend(table);
        });
    }

    dicomScpManager.refresh = refreshTable;

    dicomScpManager.init();

    return XNAT.admin.dicomScpManager = dicomScpManager;

}));
