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

    var $body = $('body');

    // open dialog to view JSON
    $body.on('click', 'a.view-plugin-info', function(){

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

