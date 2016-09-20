/*
 * web: topnav-browse.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Manage visibility of items in the top nav bar
 */

(function(){

    var $browseProjects = $('#browse-projects');
    var $browseData = $('#browse-data');
    var $favoriteProjects = $('#favorite-projects');
    var undefined;

    var displayProjectList = function($parent, projectList){
        if (!projectList.length) return;
        // convert projectList to an array of <li> elements
        projectList = projectList.map(function(proj){
            if (!proj.name) return;
            return '<li><a href="' + serverRoot + '/data/projects/' + proj.id + '">' + proj.name + '</a></li>'
        });
        $parent.append(projectList).parents('li').removeClass('hidden');
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
