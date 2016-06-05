var displayProjectNav = function(projectList) {
    if (projectList.length > 0) {
        for (p in projectList) {
            var pLink = '<li><a href="'+serverRoot+'/data/projects/'+projectList[p].id+'">'+projectList[p].name+'</a></li>';
            $('#browse-projects').append(pLink);
        };
    } else {
        $('#browse-projects').find('.create-project').removeClass('hidden');
    }
};
var displayFavoriteProjects = function(projectList) {
    if (projectList.length > 0) {
        for (p in projectList) {
            if (projectList[p].name !== undefined) {
                var pLink = '<li><a href="'+serverRoot+'/data/projects/'+projectList[p].id+'">'+projectList[p].name+'</a></li>';
                $('#favorite-projects').append(pLink);
            }
        };
        $('#favorite-projects').parent('li').removeClass('hidden');
    }
}

var displayProjectNavFail = function() {
    $('#browse-projects').find('.create-project').removeClass('hidden');
};

var displayDataNav = function() {
    var dataTypes = window.available_elements;
    if (dataTypes.length > 0) {
        for (d in dataTypes) {
            if (dataTypes[d].plural !== undefined && dataTypes[d].element_name !== 'wrk:workflowData') {
                var dLink = '<li><a href="'+serverRoot+'/app/template/Search.vm/node/d.'+dataTypes[d].element_name+'">'+dataTypes[d].plural+'</a></li>';
                $('#browse-data').append(dLink);
            }
        };
        $('#browse-data').parent('li').removeClass('hidden');
    }
};
$(document).ready(function(){
    // populate project list
    $.getJSON(serverRoot+'/data/projects?accessible=true&XNAT_CSRF='+csrfToken)
        .success(function(data){ displayProjectNav(data.ResultSet.Result) })
        .fail(function() { displayProjectNavFail(); });

    // look for favorite projects. If found, show that dropdown list.
    $.getJSON(serverRoot+'/data/projects?favorite=true&format=json&XNAT_CSRF='+csrfToken)
        .success(function(data) { displayFavoriteProjects(data.ResultSet.Result) })
        .fail(function() { /* set Favorite Projects nav item to hidden, if necessary */ });

    // populate data list
    if (window.available_elements !== undefined) {
        displayDataNav();
    } else {
        $('#browse-data').parent('li').addClass('disabled');
    }
});
