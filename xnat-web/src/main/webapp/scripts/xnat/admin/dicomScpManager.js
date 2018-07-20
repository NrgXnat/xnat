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

    var dicomScpManager, undefined, undef,
        rootUrl = XNAT.url.rootUrl,
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

    // dialog to create/edit receivers
    dicomScpManager.dialog = function(item, isNew){

        var doWhat = !item ? 'New' : 'Edit';
        var oldPort = item && item.port ? item.port : null;
        var oldTitle = item && item.aeTitle ? item.aeTitle : null;
        var modalDimensions =
                Object.keys(dicomScpManager.identifiers).length > 1
                    ? { height: '320px', width: '600px' }
                    : { height: '250px', width: '500px' };

        isNew = firstDefined(isNew, doWhat === 'New');

        console.log(isNew);

        item = getObject(item);

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

                forOwn(identifiers, function(key, val){
                    var label = key;
                    label += (key === 'dicomObjectIdentifier') ? ' (Default)' : '';
                    var option = spawn('option', {
                        value: key,
                        html: label
                    });
                    if (key === 'dicomObjectIdentifier') {
                        option.setAttribute('selected', 'selected');
                        option.selected = true;
                    }
                    options.push(option)
                });

                if (options.length > 1) {

                    var identifierSelect = $form.find('#scp-identifier');

                    // un-hide the menu
                    identifierSelect.append(options)
                                    .disabled(false)
                                    .hidden(false);

                    // un-hide the menu element container
                    identifierSelect.closest('.panel-element')
                                    .hidden(false)

                }

                if (isNew) { item.enabled = true }

                if (isDefined(item.id)) {
                    // SET VALUES IN EDITOR DIALOG
                    // $form.setValues(item);
                    $form.find('[name="id"]').val(item.id);
                    $form.find('[name="aeTitle"]').val(item.aeTitle);
                    $form.find('[name="port"]').val(item.port);
                    $form.find('[name="enabled"]').val(item.enabled);
                }

                spawneri.render($container);

                var scpEditorDialog = XNAT.dialog.open({
                    title: doWhat + ' DICOM SCP Receiver',
                    content: $container,
                    width: modalDimensions.width,
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

                                // set the value for 'customProcessing' on-the-fly
                                var customProcessing$ = $formPanel.find('[name="customProcessing"]');
                                if (customProcessing$.length) {
                                    customProcessing$[0].value = customProcessing$[0].checked + '';
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

                                        var newTitle = $aeTitle.val();
                                        console.log(newTitle);

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

                                        errorMsg += '</ul>';

                                        if (errors > 0) {
                                            XNAT.dialog.message('Errors Found', errorMsg);
                                        }

                                        return errors === 0;

                                    },
                                    success: function(){
                                        refreshTable();
                                        scpEditorDialog.close();
                                        XNAT.ui.banner.top(2000, 'Saved.', 'success')
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

        dicomScpManager.getAll().done(function(data){
            data.forEach(function(item){
                // var identifierLabel = dicomScpManager.identifiers[item.identifier] || dicomScpManager.identifiers['dicomObjectIdentifier'];
                var identifierLabel = item.identifier || 'dicomObjectIdentifier';
                identifierLabel += (identifierLabel === 'dicomObjectIdentifier') ? ' (Default)' : '';
                scpTable.tr({ title: item.aeTitle, data: { id: item.id, port: item.port } })
                        .td([editLink(item, item.aeTitle)]).addClass('aeTitle')
                        .td([['div.mono.center', item.port]]).addClass('port')
                        .td(identifierLabel).addClass((Object.keys(dicomScpManager.identifiers).length > 1) ? '' : 'hidden') // only show this if there are multiple identifiers
                        .td([enabledCheckbox(item)]).addClass('status')
                        .td([['div.center', [editButton(item), spacer(10), deleteButton(item)]]]);
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
