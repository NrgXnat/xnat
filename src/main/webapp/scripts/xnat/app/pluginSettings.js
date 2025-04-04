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
    }
    pluginSettings.showAdminMenuItem = showAdminMenuItem;

    // render settings tabs (for any context)
    // this MUST be called in the context of a Spawner instance
    function renderPluginSettingsTabs(container){
        if (!container || pluginSettings.showTabs === false) return this;
        var container$ = $$(container).hide();
        // only render the tabs if there's a container for them
        if (container$.length) {
            this.render(container$, 200);
            this.done(function(){
                //container$.removeClass('hidden').fadeIn(200);
                XNAT.tab.activate(XNAT.tab.active, container$);
            });
        }
        return this;
    }



    // return site or project settings Spawner object for specified plugin
    function getPluginSettings(name, type){
        return XNAT.spawner.resolve(name + '/' + type);
    }



    // return site-level settings Spawner object for specified plugin
    getPluginSettings.siteSettings = function getPluginSiteSettings(name, id){
        if (hasSiteSettings) {
            // stop if there are already site settings
            return false;
        }
        return getPluginSettings(name, id || 'siteSettings').ok(function(){
            hasSiteSettings = true;
            renderPluginSettingsTabs.call(this, pluginSettings.siteSettingsTabs || null);
        })
    };



    // return project-level settings Spawner object for specified plugin
    getPluginSettings.projectSettings = function getPluginProjectSettings(name, id){
        if (hasProjectSettings){
            return false;
        }
        return getPluginSettings(name, id || 'projectSettings').ok(function(){
            hasProjectSettings = true;
            renderPluginSettingsTabs.call(this, pluginSettings.projectSettingsTabs || null);
        });
    };

    // get REST data and cache it, returning already cached data if available
    function getCacheData(url, force){
        var URL = XNAT.url.restUrl(url);
        var obj = null;
        if (XNAT.data[url] && !force) {
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
            obj = XNAT.xhr.get({
                url: URL,
                async: false // necessary evil to prevent redundant calls
            }).done(function(data){
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
    function getSpawnerNamespaces(append){
        return getCacheData('/xapi/spawner/namespaces' + append);
    }



    // 1) get all Spawner namespaces
    // -- site-level settings --
    // 2a) does the namespace root match a plugin name?
    // 2b) check namespaces for 'siteSettings' element
    // 2c) show "Plugin Settings" admin menu item
    // 2d) if on admin/plugins page, show the plugin's site settings tabs
    // -- project-level settings --
    // 3a) if on a project page, check for 'projectSettings' element
    // 3b) does the namespace root match a plugin name?
    // 3c) render the plugin's project settings tab(s)

    /**
     * Render settings 'type' for each installed plugin
     * with a matching Spawner namespace and 'type' element
     * @param {Array|String} [types] - single 'type' string or array of multiple 'types'
     * @param {Function} [callback] - function to call after rendering settings elements
     */
    pluginSettings.renderSettings = function renderSettings(types, callback){

        types = types || ['siteSettings', 'projectSettings'];

        return getSpawnerNamespaces('').done(function(namespaces){
            // --- CALLBACK --- //
            namespaces.forEach(function(namespace){
                getInstalledPlugins().done(function(plugins){
                    // --- CALLBACK --- //
                    var pluginsWithElements = [];
                    // 'plugins' will be an object map of ALL plugins
                    // we're adding 'xnat' as a default plugin namespace
                    // without actually installing an 'xnat' plugin
                    extend(plugins, { xnat: { id: 'xnat', name: 'XNAT Default'} });
                    forOwn(plugins, function(name, obj){
                        // plugins with namespaced elements
                        // will have plugin name first
                        if (namespace.split(/[:.]/)[0] === name) {
                            pluginsWithElements.push(name);
                        }
                    });
                    // if any plugins have Spawner elements, check
                    // for element 'types'
                    if (pluginsWithElements.length) {
                        pluginsWithElements.forEach(function(plugin){
                            getPluginElementNames(namespace).done(function(ids){
                                // --- CALLBACK --- //
                                var allIds = ids.join(';') + ';';
                                console.log(allIds);
                                [].concat(types).forEach(function(type){
                                    var pluginNamespace = plugin;
                                    var hasElementType = ids.indexOf(type) > -1;
                                    var hasRootElement = ids.indexOf('root') > -1;
                                    var elementId = hasElementType ? type : 'root';
                                    if (hasRootElement || hasElementType) {
                                        // a file named 'plugin-name.site-settings.yaml' will work
                                        if (plugin + '.' + type === namespace){
                                            pluginNamespace = plugin + '.' + type
                                        }
                                        // a file in a folder at 'plugin-name/site-settings.yaml' will also work
                                        if (plugin + ':' + type === namespace){
                                            pluginNamespace = plugin + ':' + type
                                        }
                                        getPluginSettings[type](pluginNamespace, elementId, callback);
                                    }
                                })
                            });
                        });
                    }
                });

            });
        });

    };


    // do plugins have settings for 'elementName' element
    // and if so, fire 'callback' function
    // XNAT.admin.pluginSettings.check('siteSettings', siteSettingsTabs)
    // XNAT.admin.pluginSettings.check('projectSettings', projectSettingsTabs)
    pluginSettings.check = function(elementName, callback){
        // is this needed?
    };

    pluginSettings.siteSettingsMenuItem = function(elementId){
        if (pluginSettings.hasAdminMenuItem === true) {
            return;
        }
        getSpawnerNamespaces('/restricted').done(function(namespaces){
            // plugins with site-level settings
            // MUST have a site-settings.yaml file
            var nsSiteSettings = namespaces.filter(function(ns){
                return /([:.]sitesettings)$/i.test(ns);
            });
            if (nsSiteSettings.length) {
                $$(elementId).hidden(false);
                pluginSettings.hasAdminMenuItem = true;
            }
        });
    };

    pluginSettings.projectSettingsActionItem = function(elementId){
        if (pluginSettings.hasProjectActionItem === true) {
            return;
        }
        getSpawnerNamespaces('').done(function(namespaces){
            // plugins with project-level settings
            // MUST have a project-settings.yaml file
            var nsProjectSettings = namespaces.filter(function(ns){
                return /([:.]projectSettings)$/i.test(ns);
            });
            if (nsProjectSettings.length) {
                $$(elementId).hidden(false);
                pluginSettings.hasProjectActionItem = true;
            }
        });
    };

    // render site-level settings for installed plugins
    pluginSettings.siteSettings = function(tabContainer, callback){
        pluginSettings.siteSettingsTabs =
            pluginSettings.siteSettingsTabs ||
            tabContainer ?
                $$(tabContainer) :
                $('#plugin-settings-tabs').find('div.content-tabs');
        // these properties MUST be set before spawning 'tabs' widgets
        XNAT.tabs.container = $$(XNAT.tabs.container || pluginSettings.siteSettingsTabs);
        XNAT.tabs.layout = XNAT.tabs.layout || 'left';
        pluginSettings.showTabs = true;
        return pluginSettings.renderSettings('siteSettings', callback);
    };

    // render project-level settings for installed plugins
    pluginSettings.projectSettings = function(tabContainer, callback){
        pluginSettings.projectSettingsTabs =
            pluginSettings.projectSettingsTabs ||
            tabContainer ?
                $$(tabContainer) :
                $('#project-settings-tabs').find('div.content-tabs');
        XNAT.tabs.container = $$(XNAT.tabs.container || pluginSettings.projectSettingsTabs);
        XNAT.tabs.layout = XNAT.tabs.layout || 'left';
        pluginSettings.showTabs = true;
        return pluginSettings.renderSettings('projectSettings', callback);
    };

    // call it.
    //pluginSettings.check();

    XNAT.app.pluginSettings = pluginSettings;

}));
