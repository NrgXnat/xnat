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
        urlHashValue = getUrlHashValue('#tab=','/#');
    
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
    tab.active = '';


    // ==================================================
    // SELECT A TAB
    tab.select = tab.activate = function(name, container){
        container = container || tabs.container || 'body';
        $$(container).find('li.tab[data-tab="' + name + '"]').trigger('click');
    };
    // ==================================================


    // ==================================================
    // CREATE A SINGLE TAB
    tab.init = function _tab(obj){

        var $group, groupId, tabId, tabIdHash, _flipper, _pane;

        obj = cloneObject(obj);
        obj.element = getObject(obj.element || obj.config);

        tabId = toDashed(obj.id || obj.name || randomID('t', false));

        _flipper = spawn('li.tab', {
            data: { tab: tabId }
        }, [
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

        // if 'active' is explicitly set, use the tabId value
        obj.active = (obj.active) ? tabId : '';

        // set active tab on page load if tabId matches url hash
        if (urlHashValue && urlHashValue === tabId) {
            tabIdHash = tabId;
        }

        if ((tabIdHash||obj.active) === tabId) {
            //$(_flipper).addClass('active');
            //$(_pane).addClass('active');
            tabs.active = tab.active = tabId;
        }

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

        function onRender(){
            console.log('tab: ' + tabId)
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
            onRender: onRender,
            get: get
        }
    };
    // ==================================================

    
    // ==================================================
    // MAIN FUNCTION
    tabs.init = function tabsInit(obj){

        var layout, container, $container, 
            navTabs, $navTabs, tabContent, $tabContent,
            NAV_TABS = 'div.xnat-nav-tabs',
            TAB_CONTENT = 'div.xnat-tab-content';

        // set container and layout before spawning:
        // XNAT.tabs.container = 'div.foo';
        container = obj.container || tabs.container || 'div.xnat-tab-container';

        // the main container - contains tabs and content
        $container = $$(container).hide();

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
        if (obj.meta && obj.meta.tabGroups){
            tabs.hasGroups = true;
            $navTabs.append(tab.groups(obj.meta.tabGroups));
        }
        else {
            tabs.hasGroups = false;
            $navTabs.spawn('ul.tab-group');
        }

        // bind tab click events
        $container.on('click', 'li.tab', function(e){
            e.preventDefault();
            var clicked = $(this).data('tab');
            // de-activate all tabs and panes
            $container.find('[data-tab]').removeClass('active');
            // activate the clicked tab and pane
            $container.find('[data-tab="' + clicked + '"]').addClass('active');
            // set the url hash
            //var baseUrl = window.location.href.split('#')[0];
            var newUrl = XNAT.url.updateHashQuery('', 'tab', clicked);
            window.location.replace(newUrl);
        });

        function onRender($element){
            $container.find('li.tab, div.tab-pane').removeClass('active');
            $container.find('[data-tab="' + tabs.active + '"]').addClass('active');
            // console.log($element);
            // $container.find('li.tab.active').last().trigger('click');
        }

        function get(){
            return tabContent;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: tabContent,
            spawned: tabContent,
            onRender: onRender,
            get: get
        };

    };
    // ==================================================

    // activate tab indicated in url hash
    // $(function(){
    //     if (window.location.hash) {
    //         tab.activate(getUrlHashValue())
    //     }
    // });
    
    tabs.tab = tab;

    return tabs;

}));

