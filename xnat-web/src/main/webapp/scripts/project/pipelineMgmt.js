/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/pipelineMgmt.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
/*******************************
 * Set of javascript functions to facilitate project-pipeline access via AJAX
 */


var editbuttonFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/action/ManagePipeline?task=projectpipeline&template=PipelineScreen_add_project_pipeline.vm&edit=true&project="+window.pipelineManager.pID + "&pipeline_path=" + oRecord.getData("Path") +"&datatype="+oRecord.getData("Datatype")+"\','_blank','width=950,height=850,scrollbars=yes') \">Edit</a>"  ; 
 }; 


var addLinkFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/action/ManagePipeline?task=projectpipeline&template=PipelineScreen_add_project_pipeline.vm&project="+window.pipelineManager.pID + "&pipeline_path=" + oRecord.getData("Path") +"&datatype="+oRecord.getData("Datatype")+"\','_blank','width=950,height=850,scrollbars=yes') \">Add</a>"  ; 
 }; 


var detailsLinkFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/template/PipelineScreen_details.vm?search_element=pipe:pipelineDetails&search_field=pipe:pipelineDetails.path&search_value=" + oRecord.getData("Path") +"\','_blank','width=850,height=450,scrollbars=yes') \">Details</a>"  ; 
 }; 
     
//var myLogReader = new YAHOO.widget.LogReader("myLogger");
 




/*
var editbuttonFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/template/PipelineScreen_add_project_pipeline.vm?edit=true&project="+window.pipelineManager.pID + "&pipeline_path=" + oRecord.getData("Path") +"&datatype="+oRecord.getData("Datatype")+"\','_blank','width=950,height=850,scrollbars=yes') \">Edit</a>"  ; 
 }; 


var addLinkFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/template/PipelineScreen_add_project_pipeline.vm?project="+window.pipelineManager.pID + "&pipeline_path=" + oRecord.getData("Path") +"&datatype="+oRecord.getData("Datatype")+"\','_blank','width=950,height=850,scrollbars=yes') \">Add</a>"  ; 
 }; 


var detailsLinkFormatter = function(elCell, oRecord, oColumn, oData) {  
 	elCell.innerHTML="<a href=\"#\" onclick=\"window.open('" + serverRoot + "/app/template/PipelineScreen_details.vm?search_element=pipe:pipelineDetails&search_field=pipe:pipelineDetails.path&search_value=" + oRecord.getData("Path") +"\','_blank','width=850,height=450,scrollbars=yes') \">Details</a>"  ; 
 }; 
*/     
//var myLogReader = new YAHOO.widget.LogReader("myLogger");
 

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

     
function PipelineManager(pipeline_mgmt_div_id, pID){
		this.pID=pID;
		this.pipeline_mgmt_div = document.getElementById(pipeline_mgmt_div_id);

		var table_div=document.createElement("DIV");
		table_div.id="pipeline_table";
		this.pipeline_mgmt_div.appendChild(table_div);
		this.initLoader=prependLoader("waitPanel","Loading pipelines");


	this.confirmPipelineDelete = function(oTarget, oTable) {
	   		var handleOk=function() {
			   confirmDlg.hide();	
			   var record = oTable.getRecord(oTarget);
//			   window.waitPanel.show();
		           window.pipelineManager.initLoader.render();

			   var post_url = serverRoot + "/REST/projects/" + window.pipelineManager.pID + "/pipelines?" + "format=json&path="+ record.getData('Path')+"&datatype=" +  record.getData('Datatype') + '&XNAT_CSRF='+csrfToken;
			   YAHOO.util.Connect.asyncRequest(
			   	    'DELETE',
			   	     post_url ,
				    {
				      success: function (o) {
					    window.pipelineManager.completeInit(o);
				      },
				      failure: function (o) {
//					window.waitPanel.hide();      
					initLoader.close();
                          xModalMessage('Pipeline Validation', 'Could not delete pipeline.');
				      },
                      cache:false, // Turn off caching for IE
				      scope:this
				   }
			   
			     );
			};
	   		var handleCancel = function() {
	   		   confirmDlg.hide();	
	   		};
	   
	   
	   		var confirmDlg = new YAHOO.widget.SimpleDialog('widget_confirm', { 
	   				    visible:false, 
	   				    width: '20em', 
	   				    zIndex: 9998, 
	   				    close: false, 
	   				    fixedcenter: true, 
	   				    modal: false, 
	   				    draggable: true, 
	   				    constraintoviewport: true,  
	   				    icon: YAHOO.widget.SimpleDialog.ICON_WARN, 
	   				    buttons: [ 
	   					{ text: 'Yes', handler: handleOk },
	   					{ text: 'Cancel', handler: handleCancel, isDefault: true } 
	   					] 
	   		}); 
	   
	   		confirmDlg.setBody('Are you sure you want to remove the pipeline?'); 
	   		confirmDlg.cfg.queueProperty('icon', YAHOO.widget.SimpleDialog.ICON_HELP); 
	   		confirmDlg.cfg.queueProperty('zIndex', 9998); 
	   		confirmDlg.render(document.body);
	   		confirmDlg.show();

	};

	
	this.getAdditionalPipelines=function() {
        this.initLoader=prependLoader("waitPanel","Loading pipelines");
//		window.waitPanel.show();
		this.initLoader.render();
		this.initCallback={
			cache:false,
			success:this.completeAdditionalInit,
			failure:this.initFailure,
			scope:this
		}
		
		YAHOO.util.Connect.asyncRequest('GET',serverRoot + '/REST/projects/'+ pID + '/pipelines?format=json&additional=true',this.initCallback,null,this);
	}
	
	this.getPipelines=function() {
        this.initLoader=prependLoader("waitPanel","Loading Project Pipelines");
//		window.waitPanel.show();
		this.initLoader.render();
		this.initCallback={
			cache:false,
			success:this.completeInit,
			failure:this.initFailure,
			scope:this
		}
		
		YAHOO.util.Connect.asyncRequest('GET',serverRoot + '/REST/projects/'+ pID + '/pipelines?format=json',this.initCallback,null,this);
	};
	
	this.initFailure=function(o){
        if (!window.leaving) {
            this.initLoader.close();
            xModalMessage('Pipeline Validation', 'Failed to load pipeline list for ' + XNAT.app.displayNames.singular.project.toLowerCase() + ' ' + pID + '.');
        }
	};
	
	this.completeInit=function(oResponse){
//		       window.waitPanel.hide();
	   	this.initLoader.close();
        try{
            this.pipelineResultSet= eval("(" + oResponse.responseText +")");
            this.render();
        }catch(e){
            xModalMessage('Pipeline Validation', 'Invalid pipeline list.<br/><br/>' + e.toString());
        }
	};
	
	this.completeAdditionalInit=function(oResponse){
//		window.waitPanel.hide();
		this.initLoader.close();
		try{
            this.pipelineResultSet= eval("(" + oResponse.responseText +")");
            this.renderAdditional();
        }catch(e){
            xModalMessage('Pipeline Validation', 'Invalid pipeline list.<br/><br/>' + e.toString());
        }
	};


	this.render=function() {
		var pipelineDataSource  = new YAHOO.util.DataSource(this.pipelineResultSet.ResultSet.Result);   
		pipelineDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;   
		pipelineDataSource.responseSchema = {
			fields: [
			    "Applies To", 
			    "Generates",
			    "Name",
			    "Description",
			    "Path",
			    "Datatype"
			    ]
		};

		var imgUrl = serverRoot + "/images/delete.gif";

		var projectPipelineColumnDefs=[

			  {key:"delete",label:"", formatter:function(elCell) {
						elCell.innerHTML = '<img src=' + imgUrl + ' title="Disable pipeline" />';
						elCell.style.cursor = 'pointer';
					}
			  },
			  {key:"edit",label:"",formatter:editbuttonFormatter},
			  {key:"details",label:"",formatter:detailsLinkFormatter},
			  {key:"Applies To", label:"Applies To", sortable:true},
			  {key:"Generates"},
			  {key:"Name"},
			  {key:"Description"}
		];
		var pipelineDataTable = new YAHOO.widget.DataTable("pipeline_table", projectPipelineColumnDefs, pipelineDataSource, {caption:"Pipelines for " + this.pID, summary:"Pipelines for " + this.pID});  

		pipelineDataTable.subscribe('cellClickEvent',function(ev) {
		    var target = ev.target;
		    var column = this.getColumn(target);
		    if (column.key == 'delete') {
			window.pipelineManager.confirmPipelineDelete(target,this);    
		    } 
		});

	};
	


	this.renderAdditional=function() {
		var pipelineDataSource  = new YAHOO.util.DataSource(this.pipelineResultSet.ResultSet.Result);   
		pipelineDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;   
		pipelineDataSource.responseSchema = {
			fields: [
			    "Applies To", 
			    "Generates",
			    "Name",
			    "Description",
			    "Path",
			    "Datatype"
			    ]
		};

		var projectPipelineColumnDefs=[
			  {key:"add",label:"",formatter:addLinkFormatter},
			  {key:"details",label:"",formatter:detailsLinkFormatter},
			  {key:"Applies To", label:"Applies To", sortable:true},
			  {key:"Generates"},
			  {key:"Name"},
			  {key:"Description"}
		];
		var pipelineDataTable = new YAHOO.widget.DataTable("pipeline_table", projectPipelineColumnDefs,pipelineDataSource, {caption:"Additional Pipelines for " + this.pID, summary:"Additional Pipelines for " + this.pID});  
	};


}

