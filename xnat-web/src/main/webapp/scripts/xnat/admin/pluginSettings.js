/*
 * web: pluginSettings.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Plugin site settings functions
 */

var XNAT = getObject(XNAT);

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

    var pluginSettings;

    XNAT.admin =
        getObject(XNAT.admin);

    XNAT.admin.pluginSettings = pluginSettings =
        getObject(XNAT.admin.pluginSettings);

    pluginSettings.check = function(){

        return XNAT.xhr.get(XNAT.url.restUrl('/xapi/plugins'), function(plugins){

            console.log('/xapi/plugins response:');
            console.log(plugins);

            var showMenuItem = false;

            if (!isEmpty(plugins)) {

                // calling forOwn() returns the key names
                var pluginNames = forOwn(plugins);

                console.log('plugin names:');
                console.log(pluginNames);

                var setPaths = function(names){
                    var paths = [];
                    [].concat(names).forEach(function(name){
                        paths.push(name + '/siteSettings');
                        // paths.push(name + '/admin');
                    });
                    return paths;
                };

                var pluginSettingsPaths = setPaths(pluginNames);

                function getPluginSpawnerElements(path){
                    var _url = XNAT.url.restUrl('/xapi/spawner/resolve/' + path);
                    return XNAT.xhr.get(_url);
                }

                function lookForSettings(i) {
                    if (showMenuItem || i === pluginSettingsPaths.length){
                        // console.log("couldn't do it");
                        return false;
                    }
                    // recursively try to get settings at different places
                    getPluginSpawnerElements(pluginSettingsPaths[i])
                        .done(function(data, textStatus, xhrObj){
                            // only show the menu for valid responses
                            if (xhrObj.status === 200) {
                                showMenuItem = true;
                                $('#view-plugin-settings').removeClass('hidden');
                            }
                        })
                        .fail(function(){
                            lookForSettings(++i)
                        });
                }

                // do the stuff
                lookForSettings(0);

            }

        });
    };

    // call it.
    pluginSettings.check();

    XNAT.admin.pluginSettings = pluginSettings;

}));
