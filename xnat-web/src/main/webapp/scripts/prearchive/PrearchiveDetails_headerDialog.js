/*
 * web: PrearchiveDetails_headerDialog.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

XNAT.app.headerDialog = XNAT.app.headerDialog || {};

XNAT.app.headerDialog.load = function( url, title ){

    //openModalPanel("resource_loading","Loading");
    xmodal.loading.open();

    //var csvURL = url.split('format=html')[0] + 'format=csv';

    var modalOpts={};
    modalOpts.width = '75%';
    // modalOpts.height = '75%';
    modalOpts.padding = '0px';
    modalOpts.maxBtn = true;
    modalOpts.nuke = true;
    modalOpts.buttons = [
        {
            label: 'Close',
            close: true,
            isDefault: true
        }
        // ,
        // letting this linger in case it can be used in the future
        // {
        //     label: 'Download CSV',
        //     close: false,
        //     // link: true, // this would make it look like a regular link
        //     action: function(){
        //         window.location.href = csvURL;
        //     }
        // }
    ];
    modalOpts.beforeShow = function(){
        xmodal.loading.close();
    };

    var getData = $.ajax({
        type: 'GET',
        url: url,
        cache: false ,
        dataType: 'html'
    });

    getData.done(function(data){
        modalOpts.title = title;
        modalOpts.content = data;
        XNAT.ui.dialog.open(modalOpts);
    });

    getData.fail(function(jqXHR, textStatus, errorThrown){
        modalOpts.title = 'Error - ' + title;
        modalOpts.content = 'Error: ' + textStatus;
        XNAT.ui.dialog.open(modalOpts);
    });

};
