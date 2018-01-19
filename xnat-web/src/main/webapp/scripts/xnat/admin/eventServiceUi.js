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

console.log('xnat/admin/eventService.js');

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

    function errorHandler(e, title, closeAll){
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


    /* ====================== *
     * Site Admin UI Controls *
     * ====================== */

    var eventServicePanel,
        undefined,
        rootUrl = XNAT.url.rootUrl,
        csrfUrl = XNAT.url.csrfUrl;

    XNAT.admin =
        getObject(XNAT.admin || {});

    XNAT.admin.eventServicePanel = eventServicePanel =
        getObject(XNAT.admin.eventServicePanel || {});

    function getEventSubscriptionUrl(id){
        id = id || false;
        var path = (id) ? '/xapi/events/subscription/'+id : '/xapi/events/subscriptions';
        return rootUrl(path);
    }

    function setEventSubscriptionUrl(id,appended){
        id = id || false;
        var path = (id) ? '/xapi/events/subscription/'+id : '/xapi/events/subscription';
        appended = (appended) ? '/'+appended : '';
        return csrfUrl(path + appended);
    }

    XNAT.admin.eventServicePanel.populateDisplay = function(rootDiv) {
        var $container = $(rootDiv || '#event-service-admin-tabs');
        $container.empty().append(
            spawn('p','Tap tap, is this thing on?')
        );


    }

}));