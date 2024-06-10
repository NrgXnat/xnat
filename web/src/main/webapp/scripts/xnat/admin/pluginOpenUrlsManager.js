/*
 * web: pluginOpenUrlsManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

$(function(){

    var undef;

    console.log('pluginOpenUrlsManager.js');

    XNAT.xhr.get({
         url: XNAT.url.restUrl("/xapi/pluginOpenUrls/settings"),
         dataType: 'text'
     })
     .done(function(data, textStatus, jqXHR){
         var rObj = JSON.parse(data);
         populateDisplay(rObj, !(rObj === undef || Object.keys(rObj).length < 1));
     })
     .fail(function(data, textStatus, jqXHR){
         console.log("WARNING:  Could not retrieve plugin openurls configuration status");
         populateDisplay(undef, false);
     });

    function populateDisplay(settingsObj, anyDefined){

        var config = {
            urlPanel: {
                id: "openurls-panel",
                kind: "panel.form",
                label: "Open URLs Defined in Plugins",
                method: "POST",
                contentType: "json",
                action: "/xapi/pluginOpenUrls/settings",
                contents: {
                    message: {
                        tag: "div.message.bold",
                        content: "NOTE:  Open URLs defined in plugins must be enabled by an administrator before they are available in the" +
                        " application."
                    }
                }
            }
        };

        if (anyDefined) {
            config.urlPanel.contents["subHead"] = {
                kind: "panel.subhead",
                name: "openurls-subhead",
                label: "Plugin Open URLS"
            };
            forOwn(settingsObj, function(prop, val){
                config.urlPanel.contents[prop] = {
                    kind: "panel.input.switchbox",
                    id: prop,
                    label: prop,
                    name: prop,
                    title: prop,
                    value: true,
                    onText: "Authorized",
                    offText: "Not Authorized"
                };
            });
        }
        else {
            config.urlPanel.contents["subHead"] = {
                tag: "div",
                content: "<b>No open URLs have been defined in current plugins.</b>",
                element: { style: { marginTop: "20px" } }
            };
            config.urlPanel["footer"] = false;
        }

        // handle container id with or without '-panel' suffix
        var $container = $('#plugin-open-urls-config');
        if (!$container.length) {
            $container = $('#plugin-open-urls-config-panel');
        }

        XNAT.spawner.spawn(config).render($container);

    }

});

