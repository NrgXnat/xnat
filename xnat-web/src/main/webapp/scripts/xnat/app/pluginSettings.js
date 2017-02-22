/*
 * web: pluginSettings.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Plugin site- and project-level settings
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
    var adminMenuItem = false;
    var hasSiteSettings = false;
    var hasProjectSettings = false;
    var adminMenuElement = '#/view-plugin-settings';

    XNAT.app =
            getObject(XNAT.app);

    XNAT.app.pluginSettings = pluginSettings =
            getObject(XNAT.app.pluginSettings);

    XNAT.data =
            getObject(XNAT.data);


    // get the 'adminMenuElement', optionally setting it
    pluginSettings.adminMenuElement = function(element){
        adminMenuElement = element || adminMenuElement;
        return $$(adminMenuElement);
    };


    // are there ANY plugins with site-level settings?
    // set this to true to stop checking and
    // show the "Plugin Settings" menu item
    // in the top-level site "Administer" menu
    function showAdminMenuItem(menuItem){
        if (adminMenuItem) return false;
        pluginSettings.adminMenuElement(menuItem).removeClass(function(){
            adminMenuItem = true;
            return 'hidden';
        });
    };
    pluginSettings.showAdminMenuItem = showAdminMenuItem;



    // render settings tabs (for any context)
    // this MUST be called in the context a Spawner instance
    function renderPluginSettingsTabs(container){
        if (!container) return this;
        // these properties MUST be set before spawning 'tabs' widgets
        XNAT.tabs.container = $$(container).empty();
        XNAT.tabs.layout = 'left';
        // only render the tabs if there's a container for them
        if (XNAT.tabs.container.length) {
            this.render(XNAT.tabs.container).done(function(){
                XNAT.tabs.container.fadeIn(200);
            });
        }
        return this;
    }



    // return site or project settings Spawner object for specified plugin
    function getPluginSettings(name, type, callback){
        var spawnerResolver = XNAT.spawner.resolve(name + '/' + type);
        spawnerResolver.done(function(data, textStatus, xhrObj){
            // only fire callback for 200 response
            if (data && xhrObj.status === 200) {
                // spawnerResolver.render(tabsContainer)
                callback.call(spawnerResolver, data);
            }
        });
        return spawnerResolver;
    }



    // return site-level settings Spawner object for specified plugin
    getPluginSettings.siteSettings = function getPluginSiteSettings(name, tabs){
        if (hasSiteSettings) {
            // stop if there are already site settings
            return false;
        }
        return getPluginSettings(name, 'siteSettings', function(data){
            hasSiteSettings = true;
            if (tabs === false){
                showAdminMenuItem();
            }
            else {
                renderPluginSettingsTabs.call(this, pluginSettings.siteSettingsTabs || null);
            }
        })
    }



    // return project-level settings Spawner object for specified plugin
    getPluginSettings.projectSettings = function getPluginProjectSettings(name){
        return getPluginSettings(name, 'projectSettings', function(data){
            renderPluginSettingsTabs.call(this, pluginSettings.projectSettingsTabs || null);
        });
    }



    // get REST data and cache it, returning already cached data if available
    function getCacheData(url){
        var URL = XNAT.url.restUrl(url);
        var obj = null;
        if (XNAT.data[url]) {
            obj = {
                done: function(callback){
                    if (isFunction(callback)) {
                        callback(XNAT.data[url])
                    }
                    return obj;
                }
            }
        }
        else {
            obj = XNAT.xhr.get(URL).done(function(data){
                XNAT.data[url] = data;
            });
        }
        return obj;
    }



    // get Spawner element names for plugin in 'namespace'
    function getPluginElementNames(namespace){
        return getCacheData('/xapi/spawner/ids/' + namespace);
    }



    // retrieve list of installed plugins
    function getInstalledPlugins(){
        return getCacheData('/xapi/plugins');
    }



    // retrieve list of spawner namespaces
    function getSpawnerNamespaces(){
        return getCacheData('/xapi/spawner/namespaces');
    }



    // 1) get all Spawner namespaces
    // -- site-level settings --
    // 2a) does the namespace match a plugin name?
    // 2b) check namespaces for 'siteSettings' element
    // 2c) show "Plugin Settings" menu item
    // 2d) if on admin/plugins page, show the plugin's site settings tabs
    // -- project-level settings --
    // 3a) if on a project page, check for 'projectSettings' element
    // 3b) does the namespace match a plugin name?
    // 3c) render the plugin's project settings tab(s)


    /**
     * Render settings 'type' for each installed plugin
     * with a matching Spawner namespace and 'type' element
     * @param {Array|String} [types] - single 'type' string or array of multiple 'types'
     * @param {Boolean} [tabs] - render tabs? (set to false to show admin menu item)
     */
    pluginSettings.renderSettings = function renderSettings(types, tabs){

        types = types || ['siteSettings', 'projectSettings'];

        return getSpawnerNamespaces().done(function(namespaces){
            // --- CALLBACK --- //
            namespaces.forEach(function(namespace){
                getInstalledPlugins().done(function(plugins){
                    // --- CALLBACK --- //
                    var pluginsWithElements = [];
                    // 'plugins' will be an object map of ALL plugins
                    forOwn(plugins, function(name, obj){
                        if (namespace === name) {
                            pluginsWithElements.push(name);
                        }
                    })
                    // if any plugins have Spawner elements, check
                    // for element 'types'
                    if (pluginsWithElements.length) {
                        pluginsWithElements.forEach(function(plugin){
                            getPluginElementNames(namespace).done(function(ids){
                                // --- CALLBACK --- //
                                [].concat(types).forEach(function(type){
                                    if (ids.indexOf(type) > -1) {
                                        getPluginSettings[type](plugin, tabs);
                                    }
                                })
                            });
                        });
                    }
                });

            });
        });

    };



    // pluginSettings.renderSettings = function renderSettings(types, tabs){
    //
    //     types = types || ['siteSettings', 'projectSettings'];
    //
    //     return getSpawnerNamespaces().done(function(namespaces){
    //
    //         // --- CALLBACK --- //
    //         function getNamespaceData(i){
    //             if (i === namespaces.length) return;
    //
    //             var namespace = namespaces[i];
    //
    //             getInstalledPlugins().done(function(plugins){
    //
    //                 // --- CALLBACK --- //
    //                 var pluginsWithElements = [];
    //                 // 'plugins' will be an object map of ALL plugins
    //                 forOwn(plugins, function(name, obj){
    //                     if (namespace === name) {
    //                         pluginsWithElements.push(name);
    //                     }
    //                 })
    //
    //                 // if any plugins have Spawner elements,
    //                 // check for element 'types'
    //                 function getPluginData(i){
    //                     if (i === pluginsWithElements.length) return;
    //
    //                     var plugin = pluginsWithElements[i];
    //
    //                     getPluginElementNames(namespace).done(function(ids){
    //
    //                         // --- CALLBACK --- //
    //                         types = [].concat(types);
    //
    //                         function getPluginElement(i){
    //                             if (i === types.length) return;
    //                             var type = types[i];
    //                             if (ids.indexOf(type) > -1) {
    //                                 getPluginSettings[type](plugin).done(function(){
    //                                     getPluginElement(i++);
    //                                 });
    //                             }
    //                         };
    //
    //                         getPluginElement(0);
    //
    //                     });
    //
    //                     getPluginData(i++);
    //
    //                 }
    //
    //                 if (pluginsWithElements.length) {
    //                     // GET DATA FOR PLUGINS
    //                     getPluginData(0);
    //                 }
    //
    //                 getNamespaceData(i++);
    //
    //             });
    //         }
    //
    //         // START HERE
    //         getNamespaceData(0);
    //
    //     });
    //
    // };



    // do plugins have settings for 'elementName' element
    // and if so, fire 'callback' function
    // XNAT.admin.pluginSettings.check('siteSettings', siteSettingsTabs)
    // XNAT.admin.pluginSettings.check('projectSettings', projectSettingsTabs)
    pluginSettings.check = function(elementName, callback){
        // is this needed?
    };

    pluginSettings.siteSettingsMenuItem = function(elementId){
        if (elementId) {
            adminMenuElement = elementId;
        }
        pluginSettings.siteSettingsTabs = false;
        return pluginSettings.renderSettings('siteSettings', false);
    };

    // render site-level settings for installed plugins
    pluginSettings.siteSettings = function(tabContainer){
        pluginSettings.siteSettingsTabs =
                pluginSettings.siteSettingsTabs ||
                tabContainer ? $$(tabContainer) : $('#plugin-settings-tabs').find('div.content-tabs');
        return pluginSettings.renderSettings('siteSettings');
    };

    // render project-level settings for installed plugins
    pluginSettings.projectSettings = function(tabContainer){
        pluginSettings.projectSettingsTabs =
            tabContainer ?
                $$(tabContainer) :
                $('#plugin-project-settings-tabs').find('div.content-tabs');
        return pluginSettings.renderSettings('projectSettings');
    };

    // call it.
    //pluginSettings.check();

    XNAT.app.pluginSettings = pluginSettings;

}));
