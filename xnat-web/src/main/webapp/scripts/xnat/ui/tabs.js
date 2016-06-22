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
    tab.group = function(obj){
        var id = toDashed(obj.id || obj.name);
        if (!id) return; // a tab group MUST have an id
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
    XNAT.ui.tab.active = '';


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

        var $group, groupId, tabId, _flipper, _pane;

        obj = cloneObject(obj);
        obj.config = cloneObject(obj.config);

        tabId = toDashed(obj.id || obj.name || '');

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

        obj.config.data =
            extend(true, {}, obj.config.data, {
                name: obj.name||'',
                tab: tabId
            });

        _pane = spawn('div.tab-pane', obj.config);

        if (obj.active) {
            $(_flipper).addClass('active');
            $(_pane).addClass('active');
            tab.active = _pane.id;
        }

        groupId = toDashed(obj.group||'other');

        // un-hide the group that this tab is in
        // (groups are hidden until there is a tab for them)
        $group = $('#' + groupId + '.tab-group');

        $group.show();
        
        // add all the flippers
        $group.append(_flipper);

        // console.log($group[0]);

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
    tabs.init = function tabsInit(obj){

        var layout, container, $container, 
            navTabs, tabContent;

        // set container and layout before spawning:
        // XNAT.tabs.container = 'div.foo';
        container = tabs.container || 'div.xnat-tab-container';

        layout = tabs.layout || 'left';

        navTabs = spawn('div.xnat-nav-tabs');
        tabContent = spawn('div.xnat-tab-content');

        if (layout === 'left') {
            navTabs.className += ' side pull-left';
            tabContent.className += ' side pull-right';
        }

        $container = $$(container);

        $container.append(navTabs);
        $container.append(tabContent);

        // set up the group elements
        $(navTabs).append(tab.groups(obj.meta.tabGroups));

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
            window.location.replace('#' + clicked);
        });

        function render(element){
            $$(element).append(tabContent);
            return tabContent;
        }

        function get(){
            return tabContent;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: tabContent,
            spawned: tabContent,
            render: render,
            get: get
        };

    };
    // ==================================================

    tabs.tab = tab;

    return tabs;

}));

