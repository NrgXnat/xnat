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

    // get the list of DICOM SCP Receivers
    dicomScpManager.getReceivers = dicomScpManager.getAll = function(callback){
        callback = isFunction(callback) ? callback : function(){};
        return XNAT.xhr.get({
            url: rootUrl('/xapi/dicomscp'),
            dataType: 'json',
            success: function(data){
                // refresh the 'usedPorts' array every time this function is called
                dicomScpManager.usedPorts = data.map(function(item){
                    return item.port;
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
        item = item || {};
        xmodal.open({
            title: doWhat + ' DICOM SCP Receiver',
            template: tmpl.clone(),
            height: 500,
            padding: '0',
            beforeShow: function(obj){
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                if (item && item.scpId) {
                    forOwn(item, function(prop, val){
                        $form.find('[name="'+prop+'"]').val(val);
                    });
                }
            },
            okClose: false,
            okLabel: 'Save',
            okAction: function(obj){
                var $form = obj.$modal.find('#dicom-scp-editor-panel');
                var id = $form.find('#scp-id').val();
                XNAT.xhr.form($form, {
                    method: 'PUT',
                    url: '/xapi/dicomscp/' + id,
                    contentType: 'application/json'
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
                .th({ addClass: 'left', html: '<b>ID</b>' })
                .th({ addClass: 'left', html: '<b>AE Title</b>' })
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

        function editButton(item){
            return spawn('button.btn.sm.edit', {
                onclick: function(){
                    dicomScpManager.dialog(item);
                    //alert('(feature not yet enabled)')
                }
            }, 'Edit');
        }

        function deleteButton(item){
            return spawn('button.btn.sm.delete', {
                onclick: function(){
                    xmodal.confirm({
                        content: "" +
                        "Are you sure you'd like to delete the '" + item.aeTitle + "' DICOM Receiver? " +
                        "<b>This action cannot be undone.</b>",
                        okAction: function(){
                            XNAT.xhr.delete({
                                url: rootUrl('/xapi/dicomscp/' + item.scpId),
                                success: function(){
                                    console.log('"'+ item.scpId + '" deleted')
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
                        .td(item.scpId)
                        .td(item.aeTitle)
                        .td([['div.mono.center', item.port]])
                        .td([enabledCheckbox(item)])
                        .td([['div.center', [editButton(item), spacer(10), deleteButton(item)]]]);
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
                callback();
            }

        });
        
        return scpTable.table;

    };

    dicomScpManager.init = function(container){
        
        var $manager = $$(container||'div#dicom-scp-manager');

        $manager.append(dicomScpManager.table());
        // dicomScpManager.table($manager);
        
        var newReceiver = spawn('button.new-dicomscp-receiver.btn.btn-sm.submit.pull-right', {
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
            startAll, spacer(10), stopAll, newReceiver, ['div.clear.clearfix']
        ]));
        
        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function(){
                return $manager[0]
            }
        };
    };

    dicomScpManager.init();

    return XNAT.admin.dicomScpManager = dicomScpManager;

}));
