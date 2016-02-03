/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/minProjectsList.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function MinProjectsList(_div, _options){

    var projList = this,
        xhr = XNAT.xhr,
        undefined;
    
    projList.options = _options;
    projList.div     = _div;

    if ( projList.options == undefined ){
        projList.options = { accessible: true };
    }

	projList.init = function(){

        projList.initLoader = prependLoader(projList.div, "Loading " + XNAT.app.displayNames.plural.project.toLowerCase());
		projList.initLoader.render();
		
		//load from search xml from server
        projList.initCallback = {
            success: projList.completeInit,
            failure: projList.initFailure
        };
		
		var params=[];

        if (projList.options.recent != undefined) {
            params.push("recent=true");
        }

        if (projList.options.owner != undefined) {
            params.push("owner=true");
        }

        if (projList.options.member != undefined) {
            params.push("member=true");
        }

        if (projList.options.collaborator != undefined) {
            params.push("collaborator=true");
        }

        if (projList.options.accessible != undefined) {
            params.push("accessible=true");
        }

        var url = XNAT.url.restUrl('/data/projects', params);

        xhr.getJSON(url, projList.initCallback);

	};

        projList.initFailure = function (o) {
            projList.initLoader.close();
        };

        projList.completeInit = function (json, status, jqXHR) {
            try {
                projList.projectResultSet = json;
            }
            catch (e) {
            }

            projList.initLoader.close();

            try {
                projList.render();
            }
            catch (e) {
            }
        };

	projList.render=function(){

        var display = document.getElementById(projList.div);
		display.innerHTML = "";

        //var items = [];
        var projects = projList.projectResultSet.ResultSet.Result;
		
		window.sort_field = "last_accessed_" + projList.projectResultSet.ResultSet.xdat_user_id;

        projects = projects.sort(function(a,b){
			if( a[window.sort_field] > b[window.sort_field] ) return -1;
			if( b[window.sort_field] > a[window.sort_field] ) return 1;
			return 0;
		});

		var projectsLength = projects.length;

        for ( var pC = 0; pC < projectsLength; pC++ ){

			var p = projects[pC];

            var project_name = p.name;

            // if there are no spaces in the first 42 characters, then chop it off
            if (project_name.length > 42 && project_name.substring(0,41).indexOf(' ') === -1){
                project_name = project_name.substring(0,39) + "&hellip;";
            }

			var newDisplay = document.createElement("div");
            newDisplay.title = p.name;
            newDisplay.className = ( pC%2 === 0 ) ? 'even' : 'odd';

			var row = document.createElement("div");
			row.innerHTML =
                '<h3>' +
                '<a href="' + serverRoot +
                    '/app/template/XDATScreen_report_xnat_projectData.vm' +
                    '/search_element/xnat:projectData' +
                    '/search_field/xnat:projectData.ID' +
                    '/search_value/' + p.id + '">' + project_name + '</a>' +
                '</h3>';

            newDisplay.appendChild(row);
			
			row = document.createElement("div");
			row.innerHTML =
                "<b>" + XNAT.app.displayNames.singular.project + " ID: " + p.id +"</b>";

            if ( p.pi != undefined && p.pi != ""){
				row.innerHTML+="&nbsp;&nbsp;&nbsp;<b>PI: "+ p.pi +"</b>";
			}

            newDisplay.appendChild(row);
			
			row = document.createElement("div");
			if (p.description.length > 160){
				row.innerHTML = p.description.substring(0,157) + "&nbsp;&hellip;";
			}
            else {
				row.innerHTML = p.description;
			}
			newDisplay.appendChild(row);
			
			row=document.createElement("div");

            if(p["user_role_"+projList.projectResultSet.ResultSet.xdat_user_id]==""){
				if(p.project_access && p.project_access.toLowerCase()=="public"){
					row.innerHTML="This is a <b>public</b> " + XNAT.app.displayNames.singular.project.toLowerCase() + "." + "<br/>"
					+ "<a href='" + serverRoot + "/app/template/RequestProjectAccess.vm/project/" + p.id + "'>Request write access</a> to this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".";
				}else{
					row.innerHTML="<a href='" + serverRoot + "/app/template/RequestProjectAccess.vm/project/" + p.id + "'>Request access</a> to this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".";
				}
			}else{
				if(p["user_role_"+projList.projectResultSet.ResultSet.xdat_user_id]=="Owners"){
					row.innerHTML="You are an <b>owner</b> for this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".";
				}else if(p["user_role_"+projList.projectResultSet.ResultSet.xdat_user_id]=="Members"){
					row.innerHTML="You are a <b>member</b> for this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".";
				}else if(p["user_role_"+projList.projectResultSet.ResultSet.xdat_user_id]=="Collaborators"){
					row.innerHTML="You are a <b>collaborator</b> for this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".";
				}
			}
			newDisplay.appendChild(row);
			
			display.appendChild(newDisplay);
		}
		//projList.menu=new YAHOO.widget.Menu(projList.div_id,{itemdata:items,visible:true, scrollincrement:5,position:"static"});
	};
}

function prependLoader(div_id,msg){
    if(div_id.id==undefined){
        var div=document.getElementById(div_id);
    }else{
        var div=div_id;
    }
    var loader_div = document.createElement("div");
    loader_div.innerHTML=msg;
    div.parentNode.insertBefore(loader_div,div);
    return new XNATLoadingGIF(loader_div);
}