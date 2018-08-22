/*
 * web: pluginManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Display list of installed plugins and view plugin info
 */

$(function(){

    console.log('pluginManager.js');

    // spawning this table in js for more flexibility
    XNAT.table.dataTable([], {
        id: 'plugin-list-table',
        load: '/xapi/plugins',
        columns: {
            id: '~data-id',
            beanName: '~data-bean',
            pluginInfo: {
                label: 'Plugin Info',
                filter: true,
                apply: function(){
                    var item      = this;
                    var itemTable = XNAT.table({
                        className: 'xnat-table clean compact borderless'
                    });
                    itemTable.tbody();
                    function itemRow(TH, TD){
                        var _TH = spawn('b', TH);
                        itemTable.tr().td({ className: 'text-right nowrap valign-top' }, _TH).td({}, TD);
                    }
                    itemRow('Plugin Name:', spawn('b', item.name));
                    itemRow('Plugin ID:', item.id);
                    itemRow('Plugin Version:', item.version);
                    itemRow('Plugin Class:', item.pluginClass);
                    itemRow('Description:', item.description);
                    return itemTable.table;
                }
            },
            viewPluginInfo: {
                label: 'Plugin JSON',
                value: "&ndash;", // this gets overwritten with 'html' below
                className: 'center',
                // function that accepts current item as sole argument and parent object as 'this'
                // returned value will be new value - if nothing's returned, value is passed through
                // apply: "{(  function(){ return formatJSON(this) }  )}"
                html: '<a href="#!" class="view-plugin-info link nowrap">View JSON</a>'
            }
        },
        messages: {
            noData: '' +
            'There are no plugins installed in this XNAT. Try searching for plugins on ' +
            '<a href="http://marketplace.xnat.org/plugins/" title="XNAT Marketplace" target="_blank">XNAT Marketplace</a>.',
            error: 'An error occurred retrieving information for installed plugins.'
        }
    }).render('div#plugin-table-container');


    // open dialog to view JSON
    $(document).on('click', 'a.view-plugin-info', function(e){

        e.preventDefault();

        var _id  = $(this).closest('tr').dataAttr('id');
        var _url = XNAT.url.restUrl('/xapi/plugins/' + _id);

        XNAT.xhr.getJSON(_url).done(function(data){

            var _source = spawn('textarea', JSON.stringify(data, null, 4));

            var _editor = XNAT.app.codeEditor.init(_source, {
                language: 'json'
            });

            _editor.openEditor({
                title: data.name,
                classes: 'plugin-json',
                footerContent: '(read-only)',
                buttons: {
                    close: { label: 'Close' }
                },
                afterShow: function(dialog, obj){
                    obj.aceEditor.setReadOnly(true);
                }
            });
        });
    });

});

