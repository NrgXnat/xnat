/*
 * web: projResourceMgmt.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/* 
 * javascript for ResourceManagement.  Used to configure the expected resources for a project
 */
XNAT.app.pResources={
	configs:new Array(),
	settingsDialog:new YAHOO.widget.Dialog("pResource_settings_dialog", { fixedcenter:true, visible:false, width:"950px", modal:true, close:true, draggable:true,resizable:true}),
	begin:function(){
		this.load();

		this.settingsDialog.render(document.body);
		this.settingsDialog.show();
	},
	reset:function(){
		$("#pResource_form").html("");
		$("#pResource_form").hide();
		$("#pResource_exist").html("");
		$("#pResource_exist").height(430);
	},
	menu:function(level){
		$("#pResource_form").html("");
		var temp_html =
			"<div class='colA'>" +
			"	<div class='info simple'>What resource are you requiring?</div>" +
			"	<div class='row'>" +
			"		<div class='rowTitle' for='pResource.name'>Title</div> " +
			"		<input class='pResourceField' required='true' data-required-msg='<b>Title</b> field is required.' data-prop-name='name' type='text' id='pResource.name' value='' placeholder='Natural Language Title'/>" +
			"	</div>" +
			"	<div class='row'>" +
			"		<div class='rowTitle' for='pResource.desc'>Description (optional)</div> " +
			"		<textarea class='pResourceField' data-prop-name='description' id='pResource.desc' placeholder='' ></textarea>" +
			"	</div>" +
			"	<div " + (XNAT.app.pResources.allowAutomationScripts == false ? "style='display:none' " : "") + "class='row script-select-row' id='script-select-row'>" +
			"		<div class='rowTitle'>Script to run</div> " +
			"		<select id='pScriptSelect'><option value=''>NONE</option></select>" +
			"	</div>" +
			"</div>";
		temp_html+="<div class='colB'><div class='info simple'>Where will it be stored?</div>";
		if(level=="proj"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:projectData'/>";
		}else if(level=="subj"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:subjectData'/>";
		}else if(level=="sa"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
					"<option value='xnat:subjectAssessorData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isSubjectAssessor && !value.isImageSession){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
		}else if(level=="is"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
			"<option value='xnat:imageSessionData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isImageSession){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
		}else if(level=="scan"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:imageScanData'/>";
			temp_html+="<div class='row'><div class='rowTitle' for='pResource.filter'>Types (optional)</div> <input type='text' id='pResource.filter' class='pResourceField' data-prop-name='filter' placeholder='TYPE1,TYPE3,TYPE4'>";
			temp_html+="</div>";
		}else if(level=="ia"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
					"<option value='xnat:imageAssessorData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isImageAssessor){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
			temp_html+="<div class='row'><div class='rowTitle' for='pResource.level'>Level: </div> <select id='pResource.level' class='pResourceField' data-prop-name='level'>";
			temp_html+="<option value='default'>DEFAULT (resources)</option>";
			temp_html+="<option value='out'>outputs dir (out)</option>";
			temp_html+="<option value='in'>inputs dir (in)</option>";
			temp_html+="</select></div>";
		}
		temp_html+=" <div class='row'><div class='rowTitle' for='pResource.label'>Resource Folder</div> <input class='pResourceField' required='true' data-required-msg='<b>Resource Folder</b> is required.' data-prop-name='label' size='10' type='text' id='pResource.label' required=true placeholder='ex. DICOM' data-regex='^[a-zA-Z0-9_-]+$' /></div>";
		temp_html+=" <div id='subdir-row' class='row'><div class='rowTitle' for='pResource.subdir'>Sub-folder (optional)</div> <input class='pResourceField' data-prop-name='subdir' type='text' id='pResource.subdir' placeholder='(optional) ex. data/sub/dir' size='24' data-regex='^[a-zA-Z0-9_\\-\\/]+$'/></div>";
		temp_html+=" <div class='row'><div class='rowTitle'>&nbsp;</div><input class='pResourceField' style='width:10px;' data-prop-name='overwrite' type='checkbox' id='pResource.overwrite'/> <label for='pResource.overwrite'>Allow overwrite</label></div>";
		temp_html+=" <div class='row'><div class='rowTitle'>&nbsp;</div><input class='pResourceField' style='width:10px;' data-prop-name='triage' type='checkbox' id='pResource.triage'/> <label for='pResource.triage'>Force Quarantine</label></div>";
		temp_html+=" <div class='row'><div class='rowTitle'>&nbsp;</div><input class='pResourceField' style='width:10px;' data-prop-name='format' type='checkbox' id='pResource.format'/> <label for='pResource.format'>Show Format & Content</label></div>";
		temp_html+=" <div class='row'><div class='rowTitle'>&nbsp;</div><input class='pResourceField' style='width:10px;' data-prop-name='unzip' type='checkbox' id='pResource.unzip' checked/> <label for='pResource.unzip'>Extract compressed files by default</label></div>";
		temp_html+=" </div>";
		temp_html+=" <div style='clear:both;'></div>";
		temp_html+=" <div class='row3'><button id='cruCancelBut' onclick='$(\"#pResource_form\").html(\"\");$(\"#pResource_form\").hide();$(\"#pResource_exist\").height(430)'>Cancel</button><button class='default' id='cruAddBut' onclick='XNAT.app.pResources.add();'>Add</button></div>";
		temp_html+=" <div class='row4' style=''></div>";
		$("#pResource_form").html(temp_html);
		$("#pResource_form").show();
		$("#pResource_exist").height(430-$("#pResource_form").height());
		
		$("#pResource\\.triage").bind("change", function(){
			if(this.checked){ // Quarantine ignores these options so hide them. 
				$("#subdir-row").hide();
				$("#script-select-row").hide();
			}else{
				$("#subdir-row").show();
				$("#script-select-row").show();
			}
		});
		
		var automationScriptsAjax = $.ajax({
			type : "GET",
			url:serverRoot+"/data/automation/scripts?format=json&XNAT_CSRF=" + window.csrfToken,
			cache: false,
			async: true,
			context: this,
			dataType: 'json'
		});
		automationScriptsAjax.done( function( data, textStatus, jqXHR ) {
			if (typeof data.ResultSet !== "undefined" && typeof data.ResultSet.Result !== "undefined") {
				XNAT.app.pResources.scripts = data.ResultSet.Result;
				for (var i=0;i<XNAT.app.pResources.scripts.length;i++) {
					$("#pScriptSelect").append('<option value="' + XNAT.app.pResources.scripts[i]["Script ID"] +
					 '">' + XNAT.app.pResources.scripts[i]["Script ID"] + '</option>'); 
				}
			}
		});

	},
	add:function(){		
		var valid=true;
		
		var props=new Object();
		
		//iterate over the fields in the form
		$(".pResourceField").each(function(){
			var tmpValue=$(this).val();
			if($(this).attr('required')=='required' && tmpValue==""){
				if($(this).attr('data-required-msg'))
				{
					xmodal.message("Required field",$(this).attr('data-required-msg'));
				}else{
					xmodal.message("Required field", "<b>" + $(this).attr('data-prop-name') + "</b> is required.");
				}
				valid=false;
			}
			
			//check if this form field has a regex defined
			if($(this).attr('data-regex') !=undefined && tmpValue!=""){
				if(! (new RegExp($(this).attr('data-regex'))).test(tmpValue)){
					if($(this).attr('data-regex-msg'))
					{
						xmodal.message("Invalid value",$(this).attr('data-regex-msg'));
					}else{
						xmodal.message("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> has an invalid character.");
					}
					valid=false;
				}

			}
			
			if(tmpValue!="" && (tmpValue.indexOf("'")>-1 || tmpValue.indexOf("\"")>-1)){
				xmodal.message("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> has an invalid character (quote).");
				valid=false;
			}
			
			if(tmpValue!="" && (tmpValue.length>255)){
				xmodal.message("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> exceeds size limits.");
				valid=false;
			}
			
			if($(this).attr('type')=="checkbox"){
				props[$(this).attr('data-prop-name')]=$(this).is(':checked');
			}else{
				props[$(this).attr('data-prop-name')]=tmpValue;
			}
		});
		
		if(valid){
			this.configs.push(props);
			this.save();
			var scriptToRun = $("#pScriptSelect").find(":selected").text();
			for (var i=0;i<XNAT.app.pResources.scripts.length;i++) {
				if (scriptToRun == XNAT.app.pResources.scripts[i]["Script ID"]) {
					var eventData = { event: ("Uploaded " + props.name),
					      			scriptId: scriptToRun,
					      			eventClass: "org.nrg.xft.event.entities.WorkflowStatusEvent",
					      			filters: { "status":["Complete"] },
					      			//description: "Run " + scriptToRun + " upon " + props.name + " upload." };
					      			description: props.name + " --> " + scriptToRun };
					var eventHandlerAjax = $.ajax({
						type : "PUT",
						url:serverRoot+"/data/projects/" + this.id + "/automation/handlers?XNAT_CSRF=" + window.csrfToken,
						cache: false,
						async: true,
						context: this,
						data: JSON.stringify(eventData),
						contentType: "application/json; charset=utf-8"
					});
					eventHandlerAjax.done( function( data, textStatus, jqXHR ) {
						console.log("NOTE:  Event handler added for " + props.name + " upload");
						props.triggerId = jqXHR.responseText;
						// Configure uploader
						var getUploadConfigAjax = $.ajax({
							type : "GET",
							url:serverRoot+"/data/projects/" + this.id + "/config/automation_uploader/configuration?contents=true&XNAT_CSRF=" + window.csrfToken,
							cache: false,
							async: true,
							context: this,
							dataType: 'json'
						});
						getUploadConfigAjax.done( function( data, textStatus, jqXHR ) {
							if (typeof(data)!=undefined && $.isArray(data)) {
								var uploaderConfig = data; 
								var alreadyConfig = false;
								var NEW_HANDLER = "Uploaded " + props.name;
								var thisConfig;
								var dataPos;
								for (j=0; j<uploaderConfig.length; j++) {
									var currConfig = uploaderConfig[j];
									if (currConfig.event==NEW_HANDLER && currConfig.scope=="prj") {
										thisConfig = currConfig;
										dataPos = j;
										// NOTE:  We expect that the uploader configuration will have been created when the handler was
										// created, so we'll modify it.
										alreadyConfig = true;
									}
								}
								var newHandlerObj = {
									event:NEW_HANDLER,
									eventTriggerId:props.triggerId,
									eventScope:"prj",
									launchFromResourceUploads:true,
									launchFromCacheUploads:false,
									launchWithoutUploads:false,
									doNotUseUploader:false,
									contexts:[props.type],
									resourceConfigs:[props.name]
								};
								var hasGlobalUploaderConfig = (typeof XNAT.app.abu.uploaderConfig !== 'undefined' && XNAT.app.abu.uploaderConfig.constructor === Array);
								if (alreadyConfig) {
									thisConfig = newHandlerObj;
									uploaderConfig[dataPos]=thisConfig;
									if (hasGlobalUploaderConfig) {
										for (var i=0;i<XNAT.app.abu.uploaderConfig.length;i++) {
											if (XNAT.app.abu.uploaderConfig[i].eventScope ==" prj" && XNAT.app.abu.uploaderConfig[i].event==NEW_HANDLER) {
												XNAT.app.abu.uploaderConfig[i]=newHandlerObj;
											}
										}
									}
								} else {
									uploaderConfig.push(newHandlerObj);
									if (hasGlobalUploaderConfig) {
										XNAT.app.abu.uploaderConfig.push(newHandlerObj);
									}
								}
								// Configure uploader
								var putUploadConfigAjax = $.ajax({
									type : "PUT",
									url:serverRoot+"/data/projects/" + this.id + "/config/automation_uploader/configuration?inbody=true&XNAT_CSRF=" + window.csrfToken,
									cache: false,
									async: true,
									context: this,
									data: JSON.stringify(uploaderConfig),
									contentType: "application/text; charset=utf-8"
								});
								putUploadConfigAjax.done( function( data, textStatus, jqXHR ) {
									// Do nothing for now.
								});
								putUploadConfigAjax.fail( function( data, textStatus, jqXHR ) {
        	 							xmodal.message("ERROR","The event handler to launch the automation script for this resource configuration " + 
											"could not be added.  It must be added manually.");
								});
							}
						});
					});
					eventHandlerAjax.fail( function( data, textStatus, jqXHR ) {
						console.log("ERROR:  Event handler could not be added for " + props.name + " upload");
					});
					break;
				}
			}
		}
	},
	save:function(){
		openModalPanel("saveResource","Saving resource configurations.");
		YAHOO.util.Connect.asyncRequest('PUT',serverRoot+"/data/projects/" + this.id +"/config/resource_config/script?inbody=true&XNAT_CSRF=" + window.csrfToken,
        {	
        	success : function()
        	{
        		closeModalPanel('saveResource');
        		XNAT.app.pResources.load();
	                // Trigger automation uploader to reload handlers
			XNAT.app.abu.loadResourceConfigs(); 
        	},
        	 failure: function(){
        	 	closeModalPanel('saveResource');
        	 	xmodal.message("Exception","Failed to store configuration.");
        	 },
             cache:false, // Turn off caching for IE
        	 scope: this
        },
         YAHOO.lang.JSON.stringify(this.configs));
	},
	load:function(){
		this.reset();
		
		YAHOO.util.Connect.asyncRequest('GET', serverRoot+'/data/projects/' + this.id +'/config/resource_config/script?format=json', {success : this.handleLoad, failure : this.render, cache : false, scope : this});
	},
	handleLoad:function(obj){
		var parsedResponse = YAHOO.lang.JSON.parse(obj.responseText);
	    var script = "";
	    if (parsedResponse.ResultSet.Result.length !== 0) {
	    	script = parsedResponse.ResultSet.Result[0].script;
			//sort of a hack to get this code to work with generic nrg_config return values
			if(script == undefined ){
				script = parsedResponse.ResultSet.Result[0].contents;
			}
			
			this.configs=YAHOO.lang.JSON.parse(script);
		}
		//
		// Load event handlers for finding event handlers linked to this project resource
		//
		var automationHandlerAjax = $.ajax({
			type : "GET",
			url:serverRoot+"/data/projects/" + this.id +  "/automation/handlers?format=json&XNAT_CSRF=" + window.csrfToken,
			cache: false,
			async: true,
			context: this,
			dataType: 'json'
		});
		automationHandlerAjax.done( function( data, textStatus, jqXHR ) {
			if (typeof data.ResultSet !== "undefined" && typeof data.ResultSet.Result !== "undefined") {
				this.eventHandlers = data.ResultSet.Result;
			}
	    		this.render(this.eventHandlers);
		});
		automationHandlerAjax.fail( function( data, textStatus, jqXHR ) {
	    		this.render();
		});
	},
	render:function(eventHandlers){
		//identify columns
		if(this.configs!=undefined && this.configs.length>0){
			var tmpHtml="<dl class='header'><dl><dd class='col1'>&nbsp;</dd><dd class='colL col2'>Type</dd><dd class='colM col3'>Name</dd><dd class='colM col4'>Label</dd><dd class='colL col5'>Sub-dir</dd><dd class='colM col6'>Overwrite?</dd><dd class='colN col5'>Quarantine</dd><dd class='colS col5'>Format/Content</dd><dd class='unzip'>Extract?</dd><dd class='colT col6'>Options</dd></dl></dl>	";
			jq.each(this.configs,function(i1,v1){
				var elementName=window.available_elements_getByName(v1.type);
				if(elementName!=undefined && elementName.singular!=undefined){
					elementName=elementName.singular;
				}else{
					elementName=v1.type;
				}
				tmpHtml+="<dl class='item'><dd class='col1'><button onclick='XNAT.app.pResources.remove(\"" + i1 +"\");'>Remove</button></dd><dd class='colL col2'>"+ elementName +"</dd><dd class='colM col3'>"+v1.name +"</dd><dd class='colM col4'>"+v1.label +"</dd><dd class='colL col5'>"+v1.subdir +"&nbsp;</dd><dd class='colM col6'>"+((v1.overwrite)?v1.overwrite:"false") +"</dd><dd class='colN col5'>"+((v1.triage)?v1.triage:"false") +"</dd><dd class='colS col5'>"+((v1.format)?v1.format:"false") +
					"</dd><dd class='unzip'>" +((v1.unzip == false)?"false":"true") +
					"</dd><dd class='colT col6'>";
				if(v1.level){
					tmpHtml+="Level:"+v1.level;
				}
				if(v1.filter){
					tmpHtml+=v1.filter;
				}
				tmpHtml+="</dd>";
				if(v1.description){
					tmpHtml+="<dd class='colX'><b>Description:</b> "+v1.description +"</dd>";
				}
				if (typeof eventHandlers !== "undefined") {
					for (var j=0;j<eventHandlers.length;j++) {
						if (("Uploaded " + v1.name) == eventHandlers[j].event) {
							tmpHtml+="<dd class='colX'><b>Script to run upon upload completion:</b> "+ eventHandlers[j].scriptId +"</dd>";
							break;
			
						}
					} 
				}
				tmpHtml+="</dl>";
			});
		}else{
			var tmpHtml="<div style='color:grey;font-style:italic;'>None</div>";
		}
		$("#pResource_exist").html(tmpHtml);
	},
	remove:function(index){
		this.configs.splice(index,1);
		this.save();
	}
}
//implements button functionaly
XNAT.app.pResources.settingsDialog.cfg.queueProperty("buttons", [
   { text:"Close", handler:{fn:function(){
	   	XNAT.app.pResources.reset();
	   	XNAT.app.pResources.settingsDialog.hide();
   }},isDefault:true}
]);
