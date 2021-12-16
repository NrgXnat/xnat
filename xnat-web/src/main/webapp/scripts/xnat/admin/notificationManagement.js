/*
 * web: notificationManagement.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

var XNAT = getObject(XNAT);

(function($, console){

    var notifyMgr;

    console.log('notificationManagement.js');

    XNAT.admin =
        getObject(XNAT.admin||{});

    XNAT.admin.notificationManager = notifyMgr =
        getObject(XNAT.admin.notificationManager||{});

    notifyMgr.templates = [];

    XNAT.admin.notificationManager.showTemplate = function(templateName){
        var emailTemplates = $(document).find('#notification-email-container').find('.panel-element').toArray();

        emailTemplates.filter(function(template){ return $(template).css('display') !== 'none' })
            .forEach(function(visibleTemplate){ $(visibleTemplate).hide() });
        emailTemplates.filter(function(template){ return $(template).data('name') === templateName })
            .forEach(function(selectedTemplate){ $(selectedTemplate).show(); });
    };

    function populateNotificationSelector(){
        notifyMgr.templates.length=0;

        var container = $(document).find('#notification-email-container');
        container.css({
            'background':'#f0f0f0',
            'border':'1px solid #e0e0e0',
            'padding':'15px 15px 0'
        });

        var selector = $(document).find('#notification-email-selector');

        var emailTemplates = $(container).find('.panel-element').toArray();
        emailTemplates.forEach(function(template,i){
            // index each of the email template editor panels, add a corresponding option to the selector, then hide them except for the first.
            var name = $(template).data('name'),
                label = $(template).find('.element-label').html();

            notifyMgr.templates.push({
                name: name,
                label: label
            });

            selector.append(spawn('option',{ value: name },label));

            if (i>0) $(template).hide();

        });

        // refactor this to be a callable function
        selector.off('change').on('change',function(){
            var name = $(this).find('option:selected').val();
            XNAT.admin.notificationManager.showTemplate(name);
        })
    }

    $(populateNotificationSelector);

})(window.jQuery, window.console);
