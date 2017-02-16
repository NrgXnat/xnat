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
        // convert projectList to an array of <li> elements
        // projectList = projectList.map(function(proj){
        //     if (!proj.name) return;
        //     return '<li><a href="' + serverRoot + '/data/projects/' + proj.id + '">' + proj.name + '</a></li>'
        // });
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
        var projectListFilter = XNAT.table.dataTable([], {
            container: _menuItem,
            sortable: false,
            filter: 'secondary_id, name, pi',
            header: false,
            body: false,
            items: {
                secondary_id: {
                    th: { style: { width: WIDTHS.id } }
                },
                name: {
                    th: { style: { width: WIDTHS.name } }
                },
                pi: {
                    th: { style: { width: WIDTHS.pi } }
                }
            }
        });
        var projectList = XNAT.table.dataTable(projectData, {
            container: _menuItem,
            sortable: true,
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

    var displayProjectNavFail = function(){
        $browseProjects.find('.create-project').removeClass('hidden');
    };

    var displayDataNav = function(){
        var dataTypes = window.available_elements || [];
        if (!dataTypes.length) return;
        // convert dataTypes to an array of <li> elements
        dataTypes = dataTypes.map(function(type){
            if (type.plural === undefined || type.element_name === 'wrk:workflowData') return;
            return '<li><a href="' + serverRoot + '/app/template/Search.vm/node/d.' + type.element_name + '">' + type.plural + '</a></li>';
        });
        // dataTypes is now an array of <li> elements
        $browseData.append(dataTypes).parents('li').removeClass('hidden');
    };

    //$(document).ready(function(){

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
            displayProjectList($favoriteProjects, data.ResultSet.Result)
        },
        error: function(){
            /* set Favorite Projects nav item to hidden, if necessary */
        }
    });

    // populate data list
    if (window.available_elements !== undefined) {
        displayDataNav();
    }
    else {
        $browseData.parent('li').addClass('disabled');
    }

    //});

})();
