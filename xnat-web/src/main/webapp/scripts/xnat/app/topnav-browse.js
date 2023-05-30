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
    var $myProjects = $('#my-projects');
    var $storedSearches = $('#stored-search-menu');
    var undef;

    function projectListingLink() {
        return spawn('li',[
                spawn('a',{
                    href: '#!',
                    onclick: function(){ $('#hiddenProjectForm').submit() },
                    style: {'border-top':'1px solid #dedede','padding-left':'12px'},
                    html: 'View Project Listing'
                })
            ]);
    }

    var displayProjectList = function($parent, projectData){
        if (!projectData.length) return;
        function projectListItem(val, len){
            var URL = XNAT.url.rootUrl('/data/projects/' + this.id);
            // var TEXT = truncateText(val || '<i><i>&ndash;</i></i>', len || 30);
            var TEXT = (val ? escapeHtml(val) : '<i><i>&ndash;</i></i>');
            var linkText = spawn('a.truncate', {
                title: unescapeAllHtml(val),
                // style: { width: len },
                href: URL
            }, TEXT);
            return linkText;
            // return [spawn('div.hidden', [val]), linkText];
        }
        var WIDTHS = {
            id: '180px',
            name: '360px',
            pi: '240px'
        };
        var shortList = projectData.length < 10;
        var _menuItem = spawn('li.table-list');
        XNAT.table.dataTable([], {
            container: _menuItem,
            sortable: false,
            filter: shortList ? false : 'secondary_id, name, pi',
            header: shortList,
            body: false,
            table: {
                style: { tableLayout: 'fixed' }
            },
            overflowY: shortList ? 'auto' : 'scroll',
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
            table: {
                style: { tableLayout: 'fixed' }
            },
            overflowY: shortList ? 'auto' : 'scroll',
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
        $parent.html('').append(_menuItem).parents('li').removeClass('hidden');
        $parent.append(projectListingLink);
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
        $container.html('').append(_menuItem).parents('li').removeClass('hidden');
    }

    var xnatJSON = XNAT.xhr.getJSON;
    var restUrl = XNAT.url.restUrl;

    // append hidden project search form
    function hiddenProjectSearch(){
        var hiddenProjectForm = spawn('form#hiddenProjectForm',
            {method:'POST',action:XNAT.url.rootUrl('/app/action/DisplaySearchAction')},
            [
                spawn('input',{type:'hidden',name:'ELEMENT_0',value:'xnat:projectData'}),
                spawn('input',{type:'hidden',name:'XNAT_CSRF',value:window.csrfToken })
            ]);
        $('#page_wrapper').append(hiddenProjectForm);
    }
    $(document).ready(function(){
        hiddenProjectSearch();
    });

    // populate project list
    xnatJSON({
        url: restUrl('/data/projects', ['format=json', 'accessible=true']),
        success: function(data){
            displayProjectList($browseProjects, data.ResultSet.Result)
        },
        error: function(){
            displayProjectNavFail();
        }
    });

    // look for my projects. If found, show that dropdown list.
    xnatJSON({
        url: restUrl('/data/projects', ['format=json', 'accessible=true', 'users=true']),
        success: function(data){
            displayProjectList($myProjects, data.ResultSet.Result);
        },
        error: function(){
            /* set My Projects nav item to hidden, if necessary */
        }
    });

    // look for favorite projects. If found, show that dropdown list.
    xnatJSON({
        url: restUrl('/data/projects', ['format=json', 'favorite=true']),
        success: function(data){
            var FAVORITES = data.ResultSet.Result.map(function(item){
                var URL = XNAT.url.restUrl('/data/projects/' + item.id);
                return {
                    // sorry for the confusing naming
                    name: item.secondary_id,
                    item: spawn('a.truncate', {
                        href: URL,
                        title: unescapeAllHtml(item.name),
                        style: { width: '100%' }
                    }, escapeHtml(item.secondary_id))
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
                title: unescapeAllHtml(type.element_name),
                style: { width: '100%' }
            }, escapeHtml(type.plural))
        }
    }

    function compareSearches(a,b) {
        // sort alphabetically by the brief_description field, accounting for accented characters if necessary.
        return a.brief_description.localeCompare(b.brief_description);
    }

    // populate data list
    XNAT.app.dataTypeAccess.getElements['browseable'].ready(
        // success
        function(data){

            var sortedElements = data.sortedElements;
            var elementMap = data.elementMap;

            if (!data || !sortedElements || !sortedElements.length) {
                $browseData.parent('li').addClass('disabled');
                return;
            }

            var DATATYPES = [];

            // use what's stored for 'Subjects' plural display
            if (elementMap && elementMap['xnat:subjectData']) {
                DATATYPES.push(dataTypeItem(elementMap['xnat:subjectData']));
            }
            else {
                DATATYPES.push(dataTypeItem({
                    element_name: 'xnat:subjectData',
                    plural: lookupObjectValue(XNAT, 'app.displayNames.plural.subject')
                }));
            }

            forEach(sortedElements, function(type){
                if (type.plural === undef) return;
                if (/workflowData|subjectData/i.test(type.element_name)) return;
                DATATYPES.push(dataTypeItem(type));
            });

            displaySimpleList($browseData, DATATYPES);

        },
        // failure
        function(e){
            console.warn(e);
            $browseData.parent('li').addClass('disabled');
        }
    );

    // if (window.available_elements !== undef && window.available_elements.length) {
    //     var DATATYPES = [dataTypeItem({
    //         element_name: 'xnat:subjectData',
    //         plural: 'Subjects'
    //     })];
    //     var sortedTypes = sortObjects(window.available_elements, 'plural');
    //     forEach(sortedTypes, function(type){
    //         if (type.plural === undef) return;
    //         if (/workflowData|subjectData/i.test(type.element_name)) return;
    //         DATATYPES.push(dataTypeItem(type));
    //     });
    //     displaySimpleList($browseData, DATATYPES);
    // }
    // else {
    //     $browseData.parent('li').addClass('disabled');
    // }

    // populate stored search list
    xnatJSON({
        url: restUrl('/data/search/saved', ['format=json']),
        success: function(data){
            if (data.ResultSet.Result.length){
                data.ResultSet.Result.sort(compareSearches);
                var STORED = data.ResultSet.Result.map(function(item){
                    var URL = XNAT.url.rootUrl('/app/template/Search.vm/node/ss.'+item.id);
                    return {
                        name: item.brief_description,
                        item: spawn('a',{
                            href: URL,
                            style: { width: '100%' }
                        }, escapeHtml(item.brief_description) )
                    }
                });
                displaySimpleList($storedSearches, STORED);
            }
        },
        error: function(e){
            console.log(e);
        }
    })

})();
