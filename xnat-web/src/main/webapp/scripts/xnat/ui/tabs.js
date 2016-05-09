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

    var ui, tab, tabs, page;
    
    XNAT.ui = ui =
        getObject(XNAT.ui || {});

    XNAT.ui.tab = XNAT.tab = tab =
        getObject(XNAT.ui.tab || XNAT.tab || {});

    XNAT.ui.tabs = XNAT.tabs = tabs =
        getObject(XNAT.ui.tabs || XNAT.tabs || {});

    XNAT.page = page =
        getObject(XNAT.page || {});


    // ==================================================
    // SET UP ONE TAB GROUP
    // add a single tab group to the groups
    tab.group = function(obj, container){
        var id = toDashed(obj.id || obj.name);
        if (!id) return; // a tab group MUST have an id
        var group = spawn('ul.nav.tab-group', { id: id }, [
            ['li.label', (obj.label || obj.title || obj.text || 'Tab Group')]
        ]);
        if (container) {
            $$(container).append(group);
        }
        return group;
    };
    // ==================================================


    // ==================================================
    // SET UP TAB GROUPS
    tab.groups = function(obj, container, empty){
        var groups = [],
            $container = $$(container);
        $.each(obj, function(name, label){
            groups.push(tab.group({
                id: toDashed(name),
                label: label
            }));
        });
        if (empty) {
            $container.empty();
        }
        $container.append(groups);
        return groups;
    };
    // ==================================================


    // save the id of the active tab
    XNAT.ui.tab.active = '';

    function activateTab(tab, id){

        var $tab  = $(tab),
            $tabs = $(tab).closest('div.xnat-tab-container');

        // first deactivate ALL tabs and panes
        $tabs
            .find('div.tab-pane')
            .hide()
            .removeClass('active');

        $tabs
            .find('li.tab')
            .removeClass('active');

        // then activate THIS tab and pane

        $tab.addClass('active');

        $('#' + id)
            .show()
            .addClass('active');

        XNAT.ui.tab.active = id;

    }
    

    // ==================================================
    // CREATE A SINGLE TAB
    tab.init = function _tab(obj){

        var $group, _flipper, _pane;

        obj = getObject(obj);
        obj.config = getObject(obj.config);
        obj.config.id = obj.config.id || obj.id || (toDashed(obj.name) + '-content');
        obj.config.data = extend({ name: obj.name }, obj.config.data);

        _flipper = spawn('li.tab', {
            // onclick event handler attached
            // directly to tab flipper
            onclick: function(){
                activateTab(this, obj.config.id)
            }
        }, [
            ['a', {
                title: obj.label,
                // href: '#'+obj.config.id,
                href: '#!',
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

        _pane = spawn('div.tab-pane', obj.config);

        if (obj.active) {
            $(_flipper).addClass('active');
            $(_pane).addClass('active');
            tab.active = _pane.id;
        }

        // un-hide the group that this tab is in
        // (groups are hidden until there is a tab for them)
        $group = $('#' + (toDashed(obj.group || 'other')) + '.tab-group');
        
        $group.show();
        
        // add all the flippers
        $group.append(_flipper);

        function render(element){
            $$(element).append(_pane);
            $$(element).append(paneFooter());
            return _pane;
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
            render: render,
            get: get
        }
    };
    // ==================================================


    // ==================================================
    // MAIN FUNCTION
    tabs.init = function _tabs(obj){

        var spawned = spawn('div.tabs');

        // set up the group elements
        tab.groups(obj.meta.tabGroups, '#admin-config-tabs > .xnat-nav-tabs');

        function render(element){
            $$(element).append(spawned);
            return spawned;
        }

        function get(){
            return spawned;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: spawned,
            spawned: spawned,
            render: render,
            get: get
        };

    };
    // ==================================================

    tabs.tab = tab;

    return tabs;

}));
