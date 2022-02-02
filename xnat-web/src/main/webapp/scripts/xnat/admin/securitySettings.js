/*
 * web: securitySettings.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// interactions with 'Security Settings' section of admin ui

console.log('securitySettings.js');

(function(){

    var $container = $('#html-resource-options');

    function toggleHtmlPanelVisibility(enabled){
        if (enabled === 'true'){
            $container.find('.panel-element').removeClass('disabled')
                .find('textarea').prop('disabled',false);
        }
        else {
            $container.find('.panel-element').addClass('disabled')
                .find('textarea').prop('disabled','disabled');
        }
    }

    $(document).on('change','input[name=allowHtmlResourceRendering]', function(){
        toggleHtmlPanelVisibility($(this).val())
    });

    $(document).on('blur','textarea[name=htmlResourceRenderingWhitelist]', function(){
        let whitelist = $(this).val().trim().toLowerCase();
        if (whitelist.length === 0){
            XNAT.ui.dialog.open({
                title: 'Validation error',
                width: 480,
                content: 'Leaving this whitelist blank is the same as disabling all HTML rendering. If you intend to enable all HTML resource types, use <code>"*"</code>.',
                buttons: [
                    {
                        label: 'Use <code>"*"</code>',
                        isDefault: true,
                        close: true,
                        action: function (obj) {
                            $(document).find('textarea[name=htmlResourceRenderingWhitelist]').val('*');
                        }
                    },
                    {
                        label: 'Leave empty',
                        isDefault: false,
                        close: true
                    }
                ]
            })
        }

        // convert whitespace to commas if no other separators exist
        if (whitelist.match(/\s/) && !whitelist.match(/[\r?\n?,]+/)) whitelist = whitelist.replace(/\s/g,',');

        // convert newlines to commas
        if (whitelist.match(/[\r?\n]+/)) whitelist = whitelist.split(/\r?\n/).join(',');

        // strip out "*." or "." from file extensions
        if (whitelist.match(/\*\./)) whitelist = whitelist.replace(/\*\./g,'');
        if (whitelist.match(/\./)) whitelist = whitelist.replace(/\./g,'');

        // if the user intends to use the allow-all "*" character, remove the list processing action on the textarea
        if (whitelist === "*") {
            $(document).find('textarea[name=htmlResourceRenderingWhitelist]').removeClass('array-list')
        }
        else {
            $(document).find('textarea[name=htmlResourceRenderingWhitelist]').addClass('array-list');
        }

        $(document).find('textarea[name=htmlResourceRenderingWhitelist]').val(whitelist);

    });

    $(document).ready(function(){
        var enablePanel = $(document).find('input[name=allowHtmlResourceRendering]').val();
        toggleHtmlPanelVisibility(enablePanel);
    });

})();
