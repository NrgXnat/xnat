/*
 * web: tabs.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Functions for creating XNAT tab UI elements
 */

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

    var ui, tab, tabs, page,
        urlHashValue = getUrlHashValue('#tab=');

    XNAT.ui = ui =
        getObject(XNAT.ui || {});

    XNAT.ui.tab = XNAT.tab = tab =
        getObject(XNAT.ui.tab || XNAT.tab || {});

    XNAT.ui.tabs = XNAT.tabs = tabs =
        getObject(XNAT.ui.tabs || XNAT.tabs || {});

    XNAT.page = page =
        getObject(XNAT.page || {});


    // by default there are no groups
    tabs.hasGroups = false;

    // ==================================================
    // SET UP ONE TAB GROUP
    // add a single tab group to the groups
    tab.group = function(obj){
        var id = toDashed(obj.id || obj.name);
        if (!id) return; // a tab group MUST have an id
        // return if the group already exists
        if ($('ul#' + id + '.nav.tab-group').length) return;
        return spawn('ul.nav.tab-group', { id: id }, [
            ['li.label', (obj.label || obj.title || obj.text || 'Tab Group')]
        ]);
    };
    // ==================================================


    // ==================================================
    // SET UP TAB GROUPS
    tab.groups = function(obj){
        var groups = [];
        $.each(obj, function(name, label){
            groups.push(tab.group({
                id: toDashed(name),
                label: label
            }));
        });
        // console.log(groups);
        return groups;
    };
    // ==================================================


    // save the id of the active tab
    tab.active = tabs.active = '';


    // ==================================================
    // SELECT A TAB
    tab.activate = tab.select = function(name, container){
        // console.log('tab.select / tab.activate');
        tab.active = tabs.active =
            name || tab.active || tabs.active;
        var $container = $$(container || tabs.container || 'body');
        var tabSelector =
                tab.active ?
                    '[data-tab="' + tab.active + '"]' :
                    '[data-tab]';
        if (!$(tabSelector).first().length) return;
        $container
            .find('li.tab')
            .removeClass('active')
            .filter(tabSelector)
            .addClass('active')
            .hidden(false);
        $container
            .find('div.tab-pane')
            .removeClass('active')
            .filter(tabSelector)
            .addClass('active')
            .hidden(false);
        // if a tab is being activated, make sure
        // the container is NOT hidden
        $container.hidden(false, 200);
        var newUrl = XNAT.url.updateHashQuery('', 'tab', tab.active);
        window.location.replace(newUrl);

        // trigger event for scrollable tables now visible
        $container.find(tabSelector).find('table.scrollable-table').trigger('nowVisible');
    };
    // ==================================================


    // ==================================================
    // CREATE A SINGLE TAB
    tab.init = function _tab(obj){

        var $group, groupId, tabId, tabIdHash, _flipper, _pane;

        obj = cloneObject(obj);
        obj.element = getObject(obj.element || obj.config);

        tabId = toDashed(obj.id || obj.name || randomID('t', false));

        _flipper = spawn('li.tab', {data: {tab: tabId }}, [
            ['a', {
                title: obj.label,
                // href: '#'+obj.config.id,
                href: '#' + tabId,
                html: obj.label
            }]
        ]);

        // setup the footer for the whole tab pane
        function paneFooter(){
            return spawn('footer.footer', [
                ['button', {
                    type: 'button',
                    html: 'Save All',
                    classes: 'save-all btn btn-primary pull-right'
                }]
            ]);
        }
        tab.paneFooter = paneFooter;

        obj.element.data =
            extend(true, {}, obj.element.data, {
                name: obj.name||'',
                tab: tabId
            });

        _pane = spawn('div.tab-pane', obj.element);

        // set the first spawned tab as the 'active' tab if not already set
        tabs.active = tab.active =
            tab.active || tabs.active || tabId;

        // if 'active' is explicitly set, use the tabId value
        // obj.active = (obj.active) ? tabId : '';

        // set active tab on page load if tabId matches url hash
        if (urlHashValue && urlHashValue === tabId) {
            tabIdHash = tabId;
            tabs.active = tab.active = tabIdHash;
        }

        // if ((tabIdHash||obj.active) === tabId) {
            //$(_flipper).addClass('active');
            //$(_pane).addClass('active');
        //     tabs.active = tab.active = tabId;
        // }

        if (tabs.hasGroups) {
            groupId = toDashed(obj.group||'other');
            // un-hide the group that this tab is in
            // (groups are hidden until there is a tab for them)
            $group = $$(tabs.navTabs).find('#' + groupId + '.tab-group');
        }
        else {
            $group = $$(tabs.navTabs).find('ul.tab-group').first();
        }

        // add all the flippers
        $group.append(_flipper).show();

        function load(){
            // console.log('render tab: ' + tabId)
        }

        function get(){
            return _pane;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            flipper: _flipper,
            pane: _pane,
            element: _pane,
            spawned: _pane,
            load: load,
            get: get,
            render: function(container){
                $$(container).append(_pane).hidden(false);
                return _pane;
            }
        }
    };
    // ==================================================


    // ==================================================
    // MAIN FUNCTION
    tabs.init = function tabsInit(obj){

        var layout, container, $container, $thisContainer,
            navTabs, $navTabs, tabContent, $tabContent,
            NAV_TABS = 'div.xnat-nav-tabs',
            TAB_CONTENT = 'div.xnat-tab-content';

        // set container and layout before spawning:
        // XNAT.tabs.container = 'div.foo';
        container = obj.container || tabs.container || 'div.xnat-tab-container';

        // the main container - contains tabs and content
        $container = $$(container).hidden();

        // if no container exists, spawn a new one
        $container =
            $container.length ? $container : $.spawn('div.xnat-tab-container');

        // fresh assignment
        container = $container[0];

        // use existing tabs if already present
        if ($container.find(NAV_TABS).length) {
            $navTabs = $container.find(NAV_TABS);
        }
        else {
            $navTabs = $.spawn(NAV_TABS);
            $container.append($navTabs);
        }
        navTabs = $navTabs[0];

        // use existing content container if already present
        if ($container.find(TAB_CONTENT).length) {
            $tabContent = $container.find(TAB_CONTENT);
        }
        else {
            $tabContent = $.spawn(TAB_CONTENT);
            $container.append($tabContent);
        }
        tabContent = $tabContent[0];

        layout = obj.layout || tabs.layout || 'left';

        if (layout === 'left') {
            $navTabs.addClass('side pull-left');
            $tabContent.addClass('side pull-right');
        }

        $navTabs.addClass(layout);

        // copy values to XNAT.tabs object for use elsewhere
        tabs.container = container;
        tabs.layout = layout;
        tabs.navTabs = navTabs;

        // set up the group elements, if present
        if (obj.groups || obj.tabGroups || (obj.meta && obj.meta.tabGroups)){
            tabs.hasGroups = true;
            obj.groups = obj.tabGroups =
                obj.groups || obj.tabGroups || obj.meta.tabGroups;
            $navTabs.append(tab.groups(obj.groups));
        }
        else {
            tabs.hasGroups = false;
            $navTabs.spawn('ul.tab-group');
        }

        $thisContainer = $container;

        function load(){
            // console.log('tabs load');
            // console.log($element);
            // $container.find('li.tab.active').first().trigger('click');
            tab.activate(tab.active, $thisContainer);
            $thisContainer.hidden(false, 200);
            return tabContent;
        }

        function get(){
            console.log('tabs get');
            load();
            return tabContent;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: tabContent,
            spawned: tabContent,
            load: function(){
                // console.log('tabs.load');
                return load.apply(this, arguments);
            },
            get: get,
            render: function(container){
                $container = container ? $$(container) : $container;
                $thisContainer = $container.append(tabContent);
                load();
                return tabContent;
            }
        };

    };
    // ==================================================

    // bind tab click events... ONCE
    $(document).on('click', 'li.tab', function(e){
        e.preventDefault();
        // console.log('tab click');
        var $thisTab = $(this);
        var clicked = $thisTab.data('tab');
        console.log('tab: ' + clicked);
        // find the parent container
        var $thisContainer = $thisTab.closest('div.xnat-nav-tabs').parent();
        // activate the clicked tab and pane
        tab.activate(clicked, $thisContainer);
    });

    // activate tab indicated in url hash
    $(function(){
        if (/#tab=/i.test(window.location.hash)) {
            tab.activate(getUrlHashValue('#tab='));
            return;
        }
        var tabs$ = $('li.tab');
        if (tabs$.length && tabs$.filter(':visible').length && !tabs$.filter('.active').length) {
            tab.activate(tabs$.first().data('tab'));
        }
    });

    tabs.tab = tab;

    return tabs;

}));

