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
        
        $(document).find('textarea[name=htmlResourceRenderingWhitelist]').val(whitelist);

    });

    $(document).ready(function(){
        var enablePanel = $(document).find('input[name=allowHtmlResourceRendering]').val();
        toggleHtmlPanelVisibility(enablePanel);

        // hack in an info link since panel switchbox entries don't support them
        function showResourceInfo(){
            var infoMessage = spawn('!',[
                spawn('p','Setting this to "Allow" will mean that resources specified in the whitelist below will be rendered by your web browser. Rendering includes execution of any code that might be present. For example, that might include javascript within an html file. This setting should only be enabled if you trust all sources of the resources you include in the whitelist below. If set to "Do Not Allow", resources will be downloaded without being rendered in the page.'),
                spawn('p','For a more complete description, please read the article <a href="https://wiki.xnat.org/documentation/xnat-administration/how-to-restrict-rendering-of-xnat-resources" target="_new">How To Restrict Rendering of XNAT Resources</a>. This article also includes a list of all supported mime types.</p>')
            ]);

            XNAT.dialog.open({
                title: 'Resource Rendering',
                width: '400px',
                content: infoMessage,
                footer: false,
                buttons: []
            });
        }

        var infoLink = spawn('a.infoLink#render-resources-info', {
            style: {position: 'relative', top: '0px', right: '8px'},
            href: '#!',
            html: '<i class="fa fa-question-circle" style="font-size: 16px; color: rgb(26, 117, 194);"></i>',
            onclick: showResourceInfo
        });

        $(document).find('div[data-name=allowHtmlResourceRendering]').find('.element-label').prepend(infoLink);

    });

})();
