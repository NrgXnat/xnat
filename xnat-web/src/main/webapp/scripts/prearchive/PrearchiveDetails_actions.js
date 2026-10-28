/*
 * web: PrearchiveDetails_actions.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
XNAT.app.prearchiveActions={
	requestDelete:function(){
        xmodal.confirm({
          content: "Are you sure you want to permanently delete this session?",
          okAction: function(){
        	  XNAT.app.prearchiveActions.doDelete();
          },
          cancelAction: function(){
          }
        });
	},
	doDelete:function(){
		this.delCallback={
            success:this.handleDelSuccess,
            failure:this.handleDelFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
		
		openModalPanel("delete_scan","Deleting session");
		
        YAHOO.util.Connect.asyncRequest('DELETE',serverRoot+"/REST" + this.url+"?XNAT_CSRF=" + csrfToken,this.delCallback,null,this);
	},
	handleDelSuccess:function(o){
		closeModalPanel("delete_scan");
		window.close();
	},
	handleDelFailure:function(o){
		closeModalPanel("delete_scan");
	    showMessage("page_body", "Error", "Failed to delete session. ("+ o.message + ")");
	},
	requestRebuild:function(){
        xmodal.confirm({
          content: "Are you sure you want to rebuild this session?",
          okAction: function(){
        	  XNAT.app.prearchiveActions.doRebuild();
          },
          cancelAction: function(){
          }
        });
	},
	doRebuild:function(){
		// The same request as Rebuild in the prearchive list. It queues the rebuild, and never archives the session.
		this.rebuildCallback={
            success:this.waitForRebuild,
            failure:function(o){
        		closeModalPanel("rebuild_session");
        	    showMessage("page_body", "Error", "Failed to rebuild session: " + escapeHTML(this.rebuildFailureReason(o)));
        	},
            cache:false, // Turn off caching for IE
            scope:this
        };

		openModalPanel("rebuild_session","Rebuilding session");
		this.rebuildChecks = 0;

        YAHOO.util.Connect.asyncRequest('POST',serverRoot+"/REST/services/prearchive/rebuild?src=" + encodeURIComponent(this.url) + "&XNAT_CSRF=" + csrfToken,this.rebuildCallback,null,this);
	},
	rebuildFailureReason:function(o){
		// A request refused before any session is looked at, for permissions for example, gets a status page that
		// gives the reason. Otherwise the body lists the sessions that were rebuilt, and only the status says why
		// this one wasn't.
		var text = o.responseText ? o.responseText.trim() : "";
		if (text && "<{".indexOf(text.charAt(0)) < 0) {
			return text;
		}
		var heading = new DOMParser().parseFromString(text, "text/html").querySelector("h3");
		if (heading && heading.textContent.trim()) {
			return heading.textContent.trim();
		}
		if (o.status == 403) {
			return "you can't rebuild this session. It may be locked by another operation, which only a site administrator can clear.";
		}
		if (o.status == 409) {
			return "another operation holds this session. Try again once it has finished.";
		}
		return "the server answered with status " + o.status + ".";
	},
	waitForRebuild:function(){
		// Reloads once the session has left the build, so the page shows what the build made of it. After 5 minutes
		// (150 checks, 2 seconds apart) it reloads anyway, so a stalled queue or build can't leave the modal up.
		var statusCallback={
            success:function(o){
        		var rows = JSON.parse(o.responseText).ResultSet.Result;
        		var status = rows.length > 0 ? rows[0].status : "";
        		if ((status == "QUEUED_BUILDING" || status == "BUILDING" || status == "_BUILDING") && ++this.rebuildChecks < 150) {
        			var actions = this;
        			setTimeout(function(){ actions.waitForRebuild(); }, 2000);
        		} else {
        			window.location.reload();
        		}
        	},
            failure:function(o){
        		window.location.reload();
        	},
            cache:false, // Turn off caching for IE
            scope:this
        };
        YAHOO.util.Connect.asyncRequest('GET',serverRoot+"/REST" + this.url+"?format=json",statusCallback,null,this);
	},
	requestMoveDialog:function(){
        if(this.projects==undefined){
			this.projCallback={
	            success:this.handleProjectsLoad,
	            failure:function(o){
	        		closeModalPanel("load_projects");
	        	    showMessage("page_body", "Error", "Failed to load projects. ("+ o.message + ")");},
	            cache:false, // Turn off caching for IE
	            scope:this
	        };
			
			openModalPanel("load_projects","Loading projects");
			
	        YAHOO.util.Connect.asyncRequest('GET',serverRoot+"/REST/projects?format=json&restrict=edit&columns=ID&XNAT_CSRF=" + csrfToken,this.projCallback,null,this);
		}else{
			this.showMoveDialog();
		}
	},
	handleProjectsLoad:function(o){
		this.projects=[];
		var projectResults= JSON.parse(o.responseText);
		for(var pC=0;pC<projectResults.ResultSet.Result.length;pC++){
			this.projects.push(projectResults.ResultSet.Result[pC]);
		}
		this.projects = this.projects.sort(function(a,b){ return (a.ID.toLowerCase() < b.ID.toLowerCase()) ? -1 : 1 });

		var options = $("#proj_move_select");
		$.each(this.projects, function() {
		    options.append($("<option />").val(this.ID).text(this.ID));
		});

		closeModalPanel("load_projects");
		this.showMoveDialog();
	},
	showMoveDialog:function(){
		XNAT.app.move_project_dialog.render(document.body);//need to pre-render it for the height change to take effect.
		XNAT.app.move_project_dialog.show();
	},
	move:function(proj){
		this.newProj=proj;
		this.moveCallback={
            success:function(o){
        		closeModalPanel("move_p");
        		window.location=o.responseText;
        	},
            failure:function(o){
        		closeModalPanel("move_p");
        		if(o.status==301 || o.status==0){
            		window.location=serverRoot+"/REST/prearchive/projects/"+ this.newProj + "/"+ this.timestamp +"/" + this.folder+"?format=html&screen=PrearchiveDetails.vm&popup=false";
        		}else{
        		    showMessage("page_body", "Error", "Failed to move session. ("+ o.message + ")");
                }
        	},
            cache:false, // Turn off caching for IE
            scope:this
        };
		
		openModalPanel("move_p","Moving session");
		
        YAHOO.util.Connect.asyncRequest('POST',serverRoot+"/REST" + this.url+"?action=move&newProject=" + proj +"&XNAT_CSRF=" + csrfToken,this.moveCallback,null,this);
	},
	loadLogs:function(){
		var logsCallback={
	        success:function(o){
				$('#prearcLogs').html("<h3>History</h3>"+o.responseText);
			},
	        failure:function(o){},
	        cache:false // Turn off caching for IE
	    };
		YAHOO.util.Connect.asyncRequest('GET',serverRoot+"/REST" + this.url+"/logs?template=details&format=html&requested_screen=PrearchiveDetailsLogs.vm&popup=true",logsCallback,null,this);
	},
    buildResourceTable(resources) {
		if (resources.length === 0) {
			return;
		}
		let trs = '';
		for (let i = 0; i < resources.length; i++) {
			trs += '<tr id="resourceTR' + resources[i] + '">' +
				'<td>' + resources[i] + '</td>' +
				'<td nowrap>' +
				'<select class="resourceActionSelect" data-resource-id="' + resources[i]+ '">' +
				'	<option value="">Actions</option>' +
				'	<option value="download">Download Files</option>' +
				'	<option value="review">Review File Details</option>' +
				'</td>' +
				'<td nowrap><div id="res' + resources[i] + 'Files" class="fileCountAndSize">' +
				'<a href="#" class="nolink" onclick="XNAT.app.fileCounter.load()" style="text-decoration:underline">Show Counts</a></div></td>' +
				'</tr>';
		}

		const resTable = $('<div class="resources-table"><br><hr><div class="edit_header1" style="margin-bottom:16px">Resources</div>' +
			'<table>' +
			'<tr><th>Label</th><th></th><th></th></tr>' +
			trs +
			'</table></div>');

		$('#scan_tbody').parents('table').after(resTable);
    }
};

//validator is used to simply validate if archiving would work (not to actually archive).
XNAT.app.validator={
	validate:function(){	//issues the REST call to see if this would be archivable
		var callback={
			success:function(o){ 
	    		this.handleValidation(o);
			},
			failure:function(o){
			},
	        cache:false, // Turn off caching for IE
			scope:this
		};
		var validate_service=serverRoot+"/REST/services/validate-archive?format=json&XNAT_CSRF=" + csrfToken;

		YAHOO.util.Connect.setForm(document.getElementById("form1"),false);
		YAHOO.util.Connect.asyncRequest('POST',validate_service,callback);
	},
	indexOf:function(_list,_obj){
		for (var i = 0;i < _list.length; i++) {
	         if (_list[i] === _obj) { return i; }
	     }
	     return -1;
	},
	handleValidation:function(o){
		var validation= JSON.parse(o.responseText);
		this.show=[];
		var matched=false;
		var failed=false;
		//iterate over the list of reasons why the archive might fail.
		for(var valC=0;valC<validation.ResultSet.Result.length;valC++){
			var val=validation.ResultSet.Result[valC];
			if(val.code=="1"){
				//this just means it matched an existing session, which would have been echoed to the page elsewhere
				matched=true;
			}else if(this.indexOf(this.fail_merge_on,val.code)>-1){
				failed=true;
				val.type="FAIL";//these events are standardly conflicts, but this server is configured for them to fail
				this.show.push(val);
			}else if(val.type=="FAIL"){
				failed=true;
				this.show.push(val);
			}else{
				this.show.push(val);
			}
		}
		
		if(this.show.length>0){
			//show conflicts, ask approval to override
			XNAT.app.validator.warnings="<h3>Current Warnings</h3>";
			for(var valC=0;valC<this.show.length;valC++){
				XNAT.app.validator.warnings+="<div>"+this.show[valC].type+"-"+this.show[valC].code+": "+this.show[valC].message+"</div>"
			}
			
			$("#validationAlerts").html(XNAT.app.validator.warnings);
			
			if(failed){
				$("#archiveLink").hide();
			}else{
				$("#archiveLink").show();
			}
			
			this.requiresOverwrite=true;
		}else{
			$("#validationAlerts").html("");
		}
	}
};

XNAT.app.prearchiveActions.downloadResFiles = function( url, resId ){
	window.location = serverRoot + '/REST' + url + '/resources/' + resId + '?format=zip';
	return false;
}

XNAT.app.prearchiveActions.reviewResFileDetails = function( url, resId, project ){
	var RESTurl = serverRoot +
		'/data' + url + '/resources/' + resId +
		'/files?format=html&requested_screen=PrearchiveFileList.vm&popup=true&prettyPrint=true' + (project ? '&project=' + project : '');
	XNAT.app.fileDialog.loadScan(RESTurl, 'Resource ' + resId + ' files');
}


XNAT.app.prearchiveActions.downloadAllFiles = function( url ){
	window.location = serverRoot + '/REST' + url + '?format=zip';
	return false;
};

XNAT.app.prearchiveActions.downloadFiles = function( url, scan_id ){
	window.location = serverRoot + '/REST' + url + '/scans/' + scan_id + '?format=zip';
	return false;
};

XNAT.app.prearchiveActions.reviewFileDetails = function( url, scan_id, label, project ){
	var RESTurl = serverRoot +
		'/data' + url + '/scans/' + scan_id + '/resources/' + label +
		'/files?format=html&requested_screen=PrearchiveFileList.vm&popup=true&prettyPrint=true' + (project ? '&project=' + project : '');
	XNAT.app.fileDialog.loadScan(RESTurl, 'Scan ' + scan_id + ' files');
};

XNAT.app.prearchiveActions.reviewDicomTags = function( url, scan_id ){
	var RESTurl = serverRoot +
		'/data/services/dicomdump?src=' + url + '/scans/' + scan_id +
		'&format=html&requested_screen=DicomScanTable.vm&popup=true';
	XNAT.app.headerDialog.load(RESTurl, 'Scan ' + scan_id + ' DICOM');
};

//project selector dialog
XNAT.app.move_project_dialog = new YAHOO.widget.Dialog("move_project_dialog", { fixedcenter:true, visible:false, width:"400px", height:"150px", modal:true, close:true, draggable:true,resizable:true});
XNAT.app.move_project_dialog.cfg.queueProperty("buttons", [
    { text:"Cancel", handler:{fn:function(){
    	XNAT.app.move_project_dialog.hide();
    }}},{ text:"Move",id:'move_project_continue', handler:{fn:function(){
    	XNAT.app.move_project_dialog.hide();
    	XNAT.app.prearchiveActions.move($("#proj_move_select").val());
    }, isDefault:true}}]);
