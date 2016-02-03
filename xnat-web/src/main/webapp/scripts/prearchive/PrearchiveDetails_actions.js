/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/prearchive/PrearchiveDetails_actions.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */
XNAT.app.prearchiveActions={
	requestDelete:function(){
        xModalConfirm({
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
		var projectResults= eval("(" + o.responseText +")");
		for(var pC=0;pC<projectResults.ResultSet.Result.length;pC++){
			this.projects.push(projectResults.ResultSet.Result[pC]);
		}

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
		var validation= eval("(" + o.responseText +")");
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

//project selector dialog
XNAT.app.move_project_dialog = new YAHOO.widget.Dialog("move_project_dialog", { fixedcenter:true, visible:false, width:"400px", height:"150px", modal:true, close:true, draggable:true,resizable:true});
XNAT.app.move_project_dialog.cfg.queueProperty("buttons", [
    { text:"Cancel", handler:{fn:function(){
    	XNAT.app.move_project_dialog.hide();
    }}},{ text:"Move",id:'move_project_continue', handler:{fn:function(){
    	XNAT.app.move_project_dialog.hide();
    	XNAT.app.prearchiveActions.move($("#proj_move_select").val());
    }, isDefault:true}}]);
