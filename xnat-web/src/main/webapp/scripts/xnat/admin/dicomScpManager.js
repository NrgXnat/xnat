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

    var dicomScpManager, undefined,
        rootUrl = XNAT.url.rootUrl;

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

    function scpUrl(appended){
        appended = isDefined(appended) ? '/' + appended : '';
        return rootUrl('/xapi/dicomscp' + appended);
    }

    // keep track of used ports to help prevent port conflicts
    dicomScpManager.usedPorts = [];

    // keep track of scpIds to prevent id conflicts
    dicomScpManager.ids = [];

    // get the list of DICOM SCP Receivers
    dicomScpManager.getReceivers = dicomScpManager.getAll = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        dicomScpManager.usedPorts = [];
        dicomScpManager.ids = [];
        return XNAT.xhr.get({
            url: scpUrl(),
            dataType: 'json',
            success: function(data){
                dicomScpManager.receivers = data;
                // refresh the 'usedPorts' array every time this function is called
                data.forEach(function(item){
                    dicomScpManager.usedPorts.push(item.port);
                    dicomScpManager.ids.push(item.id);
                });
                callback.apply(this, arguments);
            }
        });
    };

    dicomScpManager.getIdentifiers = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: rootUrl('/xapi/dicomscp/identifiers'),
            dataType: 'json',
            success: callback,
            fail: function(e) { console.log (e.status, e.statusText);}
        })
    };

    dicomScpManager.getReceiver = dicomScpManager.getOne = function(id, callback){
        if (!id) return null;
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: scpUrl(id),
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
        var tmpl = $('#dicom-scp-editor-template');
        var doWhat = !item ? 'New' : 'Edit';
        var oldPort = item && item.port ? item.port : null;
        var modalDimensions = (Object.keys(dicomScpManager.identifiers).length > 1) ? { height: '320', width: '600'} : { height: '250', width: '350' };
        isNew = firstDefined(isNew, doWhat === 'New');
        console.log(isNew);
        item = item || {};
        xmodal.open({
            title: doWhat + ' DICOM SCP Receiver',
            template: tmpl.clone(),
            width: modalDimensions.width,
            height: modalDimensions.height,
            scroll: false,
            padding: '0',
            beforeShow: function(obj){
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                var identifiers = dicomScpManager.identifiers;
                var identifier_ids = Object.keys(identifiers);
                if (identifier_ids.length > 1) {
                    var identifierSelect = $form.find('#scp-identifier');
                    identifierSelect
                        .prop('disabled',false)
                        .removeClass('hidden')
                        .parents('.panel-element').removeClass('hidden');
                    identifier_ids.forEach(function(key,i){
                        var option = '<option value="'+key+'"';
                        option += (key=='dicomObjectIdentifier') ? ' selected' : ''; // preselect the default identifier.
                        option += '>'+identifiers[key]+'</option>';
                        identifierSelect.append(option);
                    });
                }
                if (item && isDefined(item.id)) {
                    $form.setValues(item);
                }
            },
            okClose: false,
            okLabel: 'Save',
            okAction: function(obj){
                // the form panel is 'dicomScpEditorTemplate' in site-admin-element.yaml
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                var $title = $form.find('#scp-title');
                var $port = $form.find('#scp-port');
                console.log(item.id);
                $form.submitJSON({
                    method: isNew ? 'POST' : 'PUT',
                    url: isNew ? scpUrl() : scpUrl(item.id),
                    validate: function(){

                        $form.find(':input').removeClass('invalid');

                        var errors = 0;
                        var errorMsg = 'Errors were found with the following fields: <ul>';

                        [$port, $title].forEach(function($el){
                            var el = $el[0];
                            if (!el.value) {
                                errors++;
                                errorMsg += '<li><b>' + el.title + '</b> is required.</li>';
                                $el.addClass('invalid');
                            }
                        });

                        var newPort = $port.val();

                        console.log(newPort);

                        // only check for port conflicts if we're changing the port
                        if (newPort+'' !== oldPort+''){
                            dicomScpManager.usedPorts.forEach(function(usedPort){
                                if (usedPort+'' === newPort+''){
                                    errors++;
                                    errorMsg += '<li>Port <b>' + newPort + '</b> is already in use. Please use another port number.</li>';
                                    $port.addClass('invalid');
                                    return false;
                                }
                            });
                        }

                        errorMsg += '</ul>';

                        if (errors > 0) {
                            xmodal.message('Errors Found', errorMsg, { height: 300 });
                        }

                        return errors === 0;

                    },
                    success: function(){
                        refreshTable();
                        xmodal.close(obj.$modal);
                        XNAT.ui.banner.top(2000, 'Saved.', 'success')
                    }
                });
            }
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
                spawn('label.switchbox|title=' + item.aeTitle, [
                    ckbox,
                    ['span.switchbox-outer', [['span.switchbox-inner']]]
                ])
            ]);
        }

        function editLink(item, text){
            return spawn('a.link|href=#!', {
                onclick: function(e){
                    e.preventDefault();
                    dicomScpManager.dialog(item, false);
                }
            }, [['b', text]]);
        }

        function editButton(item) {
            return spawn('button.btn.sm.edit', {
                onclick: function(e){
                    e.preventDefault();
                    dicomScpManager.dialog(item, false);
                }
            }, 'Edit');
        }

        function deleteButton(item){
            return spawn('button.btn.sm.delete', {
                onclick: function(){
                    xmodal.confirm({
                        height: 220,
                        scroll: false,
                        content: "" +
                        "<p>Are you sure you'd like to delete the '<b>" + item.aeTitle + "</b>' DICOM Receiver?</p>" +
                        "<p><b>This action cannot be undone.</b></p>",
                        okAction: function(){
                            console.log('delete id ' + item.id);
                            XNAT.xhr.delete({
                                url: scpUrl(item.id),
                                success: function(){
                                    console.log('"'+ item.aeTitle + '" deleted');
                                    XNAT.ui.banner.top(1000, '<b>"'+ item.aeTitle + '"</b> deleted.', 'success');
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
                var identifierLabel = dicomScpManager.identifiers[item.identifier] || dicomScpManager.identifiers['dicomObjectIdentifier'];
                scpTable.tr({ title: item.aeTitle, data: { id: item.id, port: item.port }})
                        .td([editLink(item, item.aeTitle)]).addClass('aeTitle')
                        .td([['div.mono.center', item.port]]).addClass('port')
                        .td(identifierLabel).addClass((Object.keys(dicomScpManager.identifiers).length > 1) ? '' : 'hidden') // only show this if there are multiple identifiers
                        .td([enabledCheckbox(item)]).addClass('status')
                        .td([['div.center', [editButton(item), spacer(10), deleteButton(item)]]]);
            });

            if (container){
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

            var $manager = $$(container||'div#dicom-scp-manager');

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
