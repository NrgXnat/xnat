/*!
 * Manage DICOM SCP Receivers
 */

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
            url: rootUrl('/xapi/dicomscp'),
            dataType: 'json',
            success: function(data){
                dicomScpManager.receivers = data;
                // refresh the 'usedPorts' array every time this function is called
                data.forEach(function(item){
                    dicomScpManager.usedPorts.push(item.port);
                    dicomScpManager.ids.push(item.scpId);
                });
                callback.apply(this, arguments);
            }
        });
    };

    dicomScpManager.getReceiver = dicomScpManager.getOne = function(id, callback){
        if (!id) return null;
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: rootUrl('/xapi/dicomscp/' + id),
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
    dicomScpManager.dialog = function(item, opts){
        var tmpl = $('#dicom-scp-editor-template');
        var doWhat = !item ? 'New' : 'Edit';
        var isNew = doWhat === 'New';
        item = item || {};
        xmodal.open({
            title: doWhat + ' DICOM SCP Receiver',
            template: tmpl.clone(),
            height: 400,
            padding: '0',
            beforeShow: function(obj){
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                if (item && item.scpId) {
                    // check the 'enabled' checkbox for new items
                    $form.setValues(item);
                    // forOwn(item, function(prop, val){
                    //     $form.find('[name="'+prop+'"]').val(val);
                    // });
                }
                if (isNew) {
                    $form.find('#scp-enabled').prop('checked', true);
                    // $$('?enabled')[0].checked = true;
                }
            },
            okClose: false,
            okLabel: 'Save',
            okAction: function(obj){
                // the form panel is 'dicomScpEditorTemplate' in site-admin-element.yaml
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                var id = $form.find('#scp-id').val();
                if (!id) {
                    xmodal.message('SCP ID is required');
                    return false;
                }
                $form.submitJSON({
                    method: 'PUT',
                    url: '/xapi/dicomscp/' + id,
                    validate: function(){
                        $form.find(':input').removeClass('invalid');
                        var $id = $form.find('#scp-id');
                        var $port = $form.find('#scp-port');
                        var $title = $form.find('#scp-title');
                        var errors = 0;
                        var errorMsg = 'Errors were found with the following fields: <ul>';

                        [$id, $port, $title].forEach(function($el){
                            var el = $el[0];
                            if (!el.value) {
                                errors++;
                                errorMsg += '<li><b>' + el.title + '</b> is required.</li>';
                                $el.addClass('invalid');
                            }
                        });

                        if (isNew) {
                            if (dicomScpManager.ids.indexOf($id.val()) > -1) {
                                errors++;
                                errorMsg += '<li><b>SCP ID</b> already exists. Please use a different ID value.</li>';
                                $id.addClass('invalid');
                            }
                            if (dicomScpManager.usedPorts.indexOf($port.val()) > -1) {
                                errors++;
                                errorMsg += '<li><b>Port</b> is already in use. Please use another port number.</li>';
                                $port.addClass('invalid');
                            }
                        }

                        errorMsg += '</ul>';

                        if (errors > 0) {
                            xmodal.message('Errors Found', errorMsg);
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
                .th({ addClass: 'left', html: '<b>SCP ID</b>' })
                .th('<b>Port</b>')
                .th('<b>Enabled</b>')
                //.th('<b>Default?</b>')  // if this is enabled, enable the radio button(s) too (below)
                .th('<b>Actions</b>');

        // TODO: move event listeners to parent elements - events will bubble up
        // ^-- this will reduce the number of event listeners
        function enabledCheckbox(item){
            return spawn('div.center', [
                ['input.enabled', {
                    type: 'checkbox',
                    checked: !!item.enabled,
                    onclick: function(){
                        // save the status when clicked
                        var enabled = this.checked;
                        XNAT.xhr.put({
                            url: rootUrl('/xapi/dicomscp/' + item.scpId + '/enabled/' + enabled),
                            success: function(){
                                console.log(item.scpId + (enabled ? ' enabled' : ' disabled'))
                            }
                        });
                    }
                }]
            ]);
        }

        function editLink(item, text){
            return spawn('a.link|href=#!', {
                onclick: function(e){
                    e.preventDefault();
                    dicomScpManager.dialog(item);
                }
            }, [['b', text]]);
        }

        function deleteButton(item){
            return spawn('button.btn.sm.delete', {
                onclick: function(){
                    xmodal.confirm({
                        content: "" +
                        "<p>Are you sure you'd like to delete the '" + item.aeTitle + "' DICOM Receiver?</p>" +
                        "<p><b>This action cannot be undone.</b></p>",
                        okAction: function(){
                            XNAT.xhr.delete({
                                url: rootUrl('/xapi/dicomscp/' + item.scpId),
                                success: function(){
                                    console.log('"'+ item.scpId + '" deleted');
                                    XNAT.ui.banner.top(2000, '<b>"'+ item.scpId + '"</b> deleted.', 'success');
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
                scpTable.tr({title:item.scpId})
                        .td([editLink(item, item.aeTitle)])
                        .td(item.scpId)
                        .td([['div.mono.center', item.port]])
                        .td([enabledCheckbox(item)])
                        .td([['div.center', [deleteButton(item)]]]);
                // scpTable.row([
                //     item.aeTitle,
                //     [['div.mono.center', item.port]],
                //     [enabledCheckbox(item)],
                //     //[['div.center', [['input|type=radio;name=defaultReceiver']] ]], // how do we know which one is 'default'
                //     [['div.center', [editButton(item), spacer(10), deleteButton(item)]]]
                // ]);
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
        
        var $manager = $$(container||'div#dicom-scp-manager');

        dicomScpManager.$container = $manager;

        $manager.append(dicomScpManager.table());
        // dicomScpManager.table($manager);
        
        var newReceiver = spawn('button.new-dicomscp-receiver.btn.btn-sm.submit', {
            html: 'New DICOM SCP Receiver',
            onclick: function(){
                dicomScpManager.dialog();
            }
        });

        var startAll = spawn('button.start-receivers.btn.btn-sm', {
            html: 'Start All',
            onclick: function(){
                XNAT.xhr.put({
                    url: XNAT.url.rootUrl('/xapi/dicomscp/start'),
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
                    url: XNAT.url.rootUrl('/xapi/dicomscp/stop'),
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
