/*
 * web: topnav-browse.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Manage visibility of items in the top nav bar
 */

(function(){

    var $browseProjectsMenuItem = $('#browse-projects-menu-item');
    var $browseProjects = $('#browse-projects');
    var $browseData = $('#browse-data');
    var $favoriteProjects = $('#favorite-projects');
    var undefined;

    var displayProjectList = function($parent, projectData){
        if (!projectData.length) return;
        function projectListItem(val, len){
            var URL = XNAT.url.rootUrl('/data/projects/' + this.id);
            // var TEXT = truncateText(val || '<i><i>&ndash;</i></i>', len || 30);
            var TEXT = (val || '<i><i>&ndash;</i></i>');
            var linkText = spawn('a.truncate', {
                href: URL,
                title: val,
                style: { width: len }
            }, TEXT);
            return linkText;
            // return [spawn('div.hidden', [val]), linkText];
        }
        var WIDTHS = {
            id: '180px',
            name: '360px',
            pi: '240px'
        };
        var _menuItem = spawn('li.table-list');
        XNAT.table.dataTable([], {
            container: _menuItem,
            sortable: false,
            filter: 'secondary_id, name, pi',
            header: false,
            body: false,
            items: {
                secondary_id: {
                    label: 'Running Title',
                    th: { style: { width: WIDTHS.id } }
                },
                name: {
                    label: 'Project Name',
                    th: { style: { width: WIDTHS.name } }
                },
                pi: {
                    label: 'Investigator',
                    th: { style: { width: WIDTHS.pi } }
                }
            }
        });
        XNAT.table.dataTable(projectData, {
            container: _menuItem,
            // sortable: true,
            header: false,
            items: {
                _id: '~data-id',
                secondary_id: {
                    label: 'Running Title',
                    td: { style: { width: WIDTHS.id } },
                    apply: function(name){
                        return projectListItem.call(this, name, WIDTHS.id);
                    }
                },
                name: {
                    label: 'Project Name',
                    td: { style: { width: WIDTHS.name } },
                    apply: function(name){
                        return projectListItem.call(this, name, WIDTHS.name);
                    }
                },
                pi: {
                    label: 'Project PI',
                    td: { style: { width: WIDTHS.pi } },
                    apply: function(pi){
                        return projectListItem.call(this, pi, WIDTHS.pi);
                    }
                }
            }
        });
        $parent.append(_menuItem).parents('li').removeClass('hidden');
    };

    function displayProjectNavFail(){
        $browseProjects.find('.create-project').removeClass('hidden');
    }

    function displaySimpleList($container, items){
        if (!items.length) return;
        // add a filter row if there are more than 10 items
        var _menuItem = spawn('li.table-list');
        if (items.length > 10) {
            XNAT.table.dataTable([], {
                container: _menuItem,
                sortable: false,
                filter: 'item',
                header: false,
                body: false,
                items: {
                    item: 'list-item'
                }
            });
        }
        XNAT.table.dataTable(items, {
            container: _menuItem,
            sortable: false,
            header: false,
            items: {
                _name: '~data-name',
                item: 'list-item'
            }
        });
        $container.append(_menuItem).parents('li').removeClass('hidden');
    }

    var xnatJSON = XNAT.xhr.getJSON;
    var restUrl = XNAT.url.restUrl;

    // populate project list
    xnatJSON({
        url: restUrl('/data/projects', ['accessible=true']),
        success: function(data){
            displayProjectList($browseProjects, data.ResultSet.Result)
        },
        error: function(){
            displayProjectNavFail();
        }
    });

    // look for favorite projects. If found, show that dropdown list.
    xnatJSON({
        url: restUrl('/data/projects', ['favorite=true']),
        success: function(data){
            var FAVORITES = data.ResultSet.Result.map(function(item){
                var URL = XNAT.url.rootUrl('/data/projects/' + item.id);
                return {
                    // sorry for the confusing naming
                    name: item.secondary_id,
                    item: spawn('a.truncate', {
                        href: URL,
                        title: item.name,
                        style: { width: '100%' }
                    }, item.secondary_id)
                }
            });
            displaySimpleList($favoriteProjects, FAVORITES)
        },
        error: function(){
            /* set Favorite Projects nav item to hidden, if necessary */
        }
    });

    function dataTypeUrl(name){
        return XNAT.url.rootUrl('/app/template/Search.vm/node/d.' + name);
    }

    function dataTypeItem(type){
        return {
            name:  type.element_name,
            item: spawn('a.truncate', {
                href: dataTypeUrl(type.element_name),
                title: type.element_name,
                style: { width: '100%' }
            }, type.plural)
        }
    }

    // populate data list
    if (window.available_elements !== undefined && window.available_elements.length) {
        var DATATYPES = [dataTypeItem({
            element_name: 'xnat:subjectData',
            plural: 'Subjects'
        })];
        var sortedTypes = sortObjects(window.available_elements, 'plural');
        forEach(sortedTypes, function(type){
            if (type.plural === undefined) return;
            if (/workflowData|subjectData/i.test(type.element_name)) return;
            DATATYPES.push(dataTypeItem(type));
        });
        displaySimpleList($browseData, DATATYPES);
    }
    else {
        $browseData.parent('li').addClass('disabled');
    }

})();
