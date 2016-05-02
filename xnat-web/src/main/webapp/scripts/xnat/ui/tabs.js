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
        tmp     = {},
        element = XNAT.element;

    XNAT.ui = ui =
        getObject(XNAT.ui || {});

    XNAT.ui.tab = XNAT.tab = tab =
        getObject(XNAT.ui.tab || XNAT.tab || {});

    XNAT.ui.tabs = XNAT.tabs = tabs =
        getObject(XNAT.ui.tabs || XNAT.tabs || {});

    XNAT.page = page =
        getObject(XNAT.page || {});


    // ==================================================
    // MAIN FUNCTION
    tabs.init = function _tabs(obj){

        console.log('XNAT.ui.tabs.init()');
        console.log(obj);

        var spawned = spawn('div.tabs');

        function render(element){
            $$(element).append(spawned);
            return spawned;
        }

        function get(){
            console.log('XNAT.ui.tab.init().get()');
            return spawned;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: spawned,
            spawned: spawned,
            render: render,
            get: get
        };

        // var frag = document.createDocumentFragment();
        // var flippers, panes;
        //
        // // add content to pane
        // function paneContents(contents){
        //     var frag = document.createDocumentFragment();
        //     $.each(contents, function(item, obj){
        //         var widget = obj.kind||obj.type||'panel';
        //         frag.appendChild(ui[widget](item).get());
        //     });
        //     return frag;
        // }
        //
        // // setup the footer for the whole tab pane
        // function paneFooter(){
        //     var footer = spawn('footer.footer', [
        //         ['button', {
        //             type: 'button',
        //             html: 'Save All',
        //             classes: 'save-all btn btn-primary pull-right'
        //         }]
        //     ]);
        //     return footer;
        // }
        //
        // return frag;

    };
    // ==================================================


    // ==================================================
    // CREATE A SINGLE TAB
    tab.init = function _tab(obj){

        console.log('XNAT.ui.tab.init()');
        console.log(obj);

        var spawned = spawn('div.tab');

        function render(element){
            $$(element).append(spawned);
            return spawned;
        }

        function get(){
            console.log('XNAT.ui.tab.init().get()');
            return spawned;
        }

        return {
            // contents: obj.tabs||obj.contents||obj.content||'',
            element: spawned,
            spawned: spawned,
            render: render,
            get: get
        }
    };

// ==================================================

tabs.tab = tab;


return tabs;

}));


(function(XNAT, $, window, undefined){

    return;

    /**
     * Initialize the tabs
     * @param [tabItems] {Array} array of tab config objects
     * @param [container] {Element} parent element for tabs
     * @returns {{}}
     */
    function init(tabItems, container){

        // a place to store things locally
        var __ = {};

        // keep tabs on ALL tabs
        __.tabs = {};

        // keep tabs on _each_ tab
        __.tab = {};


        // __.tab['tabName'].name    = 'name';
        // __.tab['tabName'].label   = 'Label';
        // __.tab['tabName'].id      = 'id'; // is this redundant?
        // __.tab['tabName'].flipper = spawn('li.tab');
        // __.tab['tabName'].content = spawn('div.tab-pane-content', content);
        // __.tab['tabName'].pane    = spawn('div.tab-pane', [['div.pad', __.tab['tabName'].content]]);


        function activateTab(name){

            var tab = __.tab[name];

            // first deactivate ALL tabs and panes

            __.tabs.$panes
              .find('.tab-pane')
              .hide()
              .removeClass('active');

            __.tabs.$flippers
              .find('.tab')
              .removeClass('active');

            // then activate THIS tab and pane

            $(tab.flipper.wrapper)
                .addClass('active');

            $(tab.pane.wrapper)
                .show()
                .addClass('active');

            __.tabs.activeTab = name;

        }


        function refreshData(form, url){

        }


        function paneFooter(){

            var footer = spawn('footer.footer', [
                ['button', {
                    type: 'button',
                    html: 'Save All',
                    classes: 'save-all btn btn-primary pull-right'
                }]
            ]);

            return footer;

        }


        // spawn sample elements for a pane
        function sampleContent(name, content){
            var pane = __.tab[name].pane.content;
            var list = [];
            [].concat(content).forEach(function(item){
                list.push(spawn('li', {
                    innerHTML: item.label + ': ',
                    id: item.id,
                    data: {
                        name: item.name,
                        kind: item.kind
                    },
                    append: element.p(item.description || '')
                }))
            });
            pane.appendChild(spawn('ul', list));
        }


        // add content to pane
        function paneContents(contents){
            return [].concat(contents).map(function(item){
                // "kind" property defines which
                // kind of UI widget to render
                // default is "panel"
                var widget = item.kind || 'panel';
                return ui[widget](item).element;
            });
        }


        // set up a single pane
        function setupPane(name){
            var tab = __.tab[name];
            tab.pane = {};
            tab.pane.content = spawn('div.tab-pane-content');
            tab.pane.wrapper = spawn('div.tab-pane', {
                id: tab.id + '-content'
            }, [
                ['div.pad', [tab.pane.content]]
            ]);

            // add pane header
            // or don't...
            //tab.pane.content.appendChild(element.h3(tab.label));

            // add contents
            if (tab.contents) {
                $(tab.pane.content).append(paneContents(tab.contents));
            }

            //sampleContent(tab.name, tab.contents||[]);

            // add styles so footer is pinned at the bottom
            // move these to CSS eventually
            tab.pane.wrapper.style.position = 'relative';
            tab.pane.wrapper.style.paddingBottom = '60px';

            // append footer last
            tab.pane.wrapper.append(paneFooter());

            return tab.pane;
        }


        // create <ul> elements for tab groups
        function setupGroups(groups){

            var flippers = __.tabs.flippers.container;

            __.tabs.groups = {};

            $.each(groups, function(name, label){

                var id = toDashed(name) + '-tabs';

                var container = spawn('ul.nav.tab-group', {
                    id: id,
                    html: '<li class="label">' + label + '</li>'
                });

                flippers.appendChild(container);

                __.tabs.groups[name] = {
                    name: name,
                    label: label,
                    id: id,
                    container: container,
                    children: []
                };

            });
        }


        // set up a single flipper
        function setupFlipper(name){
            var tab = __.tab[name];
            tab.flipper = {};
            tab.flipper.wrapper = spawn('li.tab');
            tab.flipper.label =
                tab.flipper.a =
                    spawn('a', {
                        innerHTML: tab.label,
                        title: tab.label,
                        href: '#' + tab.id,
                        onclick: function(){
                            activateTab(name);
                        }
                    });
            if (tab.isDefault && !__.tabs.activeTab) {
                activateTab(name);
            }
            tab.flipper.wrapper.appendChild(tab.flipper.label);
            return tab.flipper;
        }


        /**
         * Process JSON and setup flippers and panes to render
         * @param config {Array} array of tabs
         */
        function setupTabs(config){

            var frag = spawn.fragment();
            var panes = $$('!div.xnat-tab-content'); // get existing element
            //var panes = spawn('div.xnat-tab-content.xnat-tab-panes');
            //var panes = element.div({className:'xnat-tab-content xnat-tab-panes'});
            var flippers = $$('!div.xnat-nav-tabs'); // get existing element
            //var flippers = spawn('ul.nav.xnat-nav-tabs');
            //var flippers = element.ul({className:'nav xnat-nav-tabs'});

            // expose to outer scope
            __.tabs.frag = frag;
            __.tabs.panes = {
                container: panes
            };
            __.tabs.flippers = {
                container: flippers
            };
            __.tabs.config = config;

            __.tabs.$panes = $(panes);
            __.tabs.$flippers = $(flippers);

            function setLayout(side){
                // only 'left' or 'right'
                if (!/left|right/.test(side)) return;
                var other = side === 'left' ? 'right' : 'left';
                __.tabs.$flippers.addClass('side pull-' + side);
                __.tabs.$panes.addClass('side pull-' + other);
            }

            $.each(config, function(name, item){

                if (item.kind !== 'tab') {
                    if (item.kind === 'meta') {
                        if (item.groups) {
                            // setup tab groups
                            setupGroups(item.groups);
                        }
                        if (item.layout) {
                            setLayout(item.layout);
                        }
                    }
                    return;
                }

                var _tab = extend(true, {}, item);
                var _name = item.name || randomID('tab-', false);

                _tab.name = _name;
                _tab.label = item.label || titleCase(_name);
                _tab.id = item.id || toDashed(_name);

                // save the first tab to activate it if there's no 'active' tab
                if (!__.tabs.firstTab) {
                    __.tabs.firstTab = _name;
                }

                // add THIS tab to the collection
                __.tab[_name] = _tab;

                _tab.pane = setupPane(_name);
                _tab.flipper = setupFlipper(_name);

                __.tabs.panes.container.appendChild(_tab.pane.wrapper);
                __.tabs.groups[item.group].container.appendChild(_tab.flipper.wrapper);

            });

            // set the first tab to active if no 'default' tab is set
            if (!__.tabs.activeTab) {
                activateTab(__.tabs.firstTab)
            }

            frag.appendChild(flippers);
            frag.appendChild(panes);

            return __;

        }

        // expose globally
        __.setup = setupTabs;

        // run setup on init() if 'tabItems' is present
        if (tabItems) {
            setupTabs(tabItems);
        }

        __.render = function(container){
            $$(container).append(__.tabs.frag);
            // clone values
            //$('[value^="@|"]').each(function(){
            //    var selector = $(this).val().split('@|')[1];
            //    var value = $$(selector).val();
            //    $(this).val(value).dataAttr('value',value);
            //    $(this).change();
            //});
            return __;
        };

        // render immediately if 'container' is specified
        if (container) {
            __.render(container);
        }

        // object to cache tab elements and data for quicker access
        XNAT.page.tabs = __.tab;

        return __;

    }

    tabs.init = init;

})(XNAT, jQuery, window);
