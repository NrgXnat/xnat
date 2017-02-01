/*
 * web: AutomationBasedUploader.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/* 
 * resource dialog is used to upload resources at any level
 */

if(typeof XNAT.app.abu === 'undefined'){
	XNAT.app.abu={ cacheConstant:'_CACHE_',importHandler:'automation' }
}

if(typeof XNAT.app.abu.abuConfigs === 'undefined'){
	XNAT.app.abu.abuConfigs={
		load:function(){
			XNAT.app.abu.loadResourceConfigs();
			$('.uploadLink').click(function(e){ 
				XNAT.app.abu.currentLink = e.target;
	 		});
		},
		modalOpts:{
			width: 840,
			height: 580,
			id: 'xmodal-abu',
			title: "Automation-Based Launcher/Uploader",
			content: "<div id='modalUploadDiv'></div>",
			closeBtn: "hide",
			buttons: {
				done: {
					label: 'OK',
					isDefault: true,
					close: true,
					action: function() {}
				},
				process: {
					label: 'Process Uploaded Data',
					isDefault: true,
					close: false,
					action: function() {}
				},
				cancel: {
					label: 'Cancel',
					close: true
				}
			},
			beforeShow: function(obj) {
				obj.$modal.find('#xmodal-abu-process-button').prop('disabled','disabled');
				obj.$modal.find('#xmodal-abu-done-button').hide();
			}
		},

		// Much of the remainder of the options code originates from ConfiguredResourceUploader.js
		showUploadLink:function(){
			$("a.abuLink").css("display","block");
			$("a.abuLink").each(function(value){
				$(this).click(function(){
					XNAT.app.abu.initializeAbuUploader("upload");
					return false;
				});
			});
		},
		showLaunchLink:function(){
			$("a.ablLink").each(function(value){
				$(this).click(function(){
					XNAT.app.abu.initializeAbuUploader("launch");
					return false;
				});
			});
		},
		hideUploadLink:function(){
			$("a.abuLink").css("display","none");
		},
		hideLaunchLink:function(){
			$("a.ablLink").css("display","none");
		},
		hideLinks:function(){
			this.hideUploadLink();
			this.hideLaunchLink();
		},
		getConfigsByType:function(type){
			var configs=XNAT.app.abu.allResourceConfigs;
			var temp=new Array();
			jq.each(configs,function(i1,v1){
				if(v1.type==type){
					temp.push(v1);
				}
			});
			return temp;
		},
		getAllConfigsByType:function(type, props){
			var tmpConfigs=this.getConfigsByType(type);
			var typeInfo=XNAT.data.context;
			if(typeof typeInfo !== 'undefined' && !(typeof type !== 'undefined' && type.indexOf("ScanData")>=0)){
				if(typeInfo.isSubjectAssessor){
					tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:subjectAssessorData"));
				}
				if(typeInfo.isImageAssessor){
					tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageAssessorData"));
				}
				if(typeInfo.isImageSession){
					tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageSessionData"));
				}
				if(typeInfo.isImageScan){
					tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageScanData"));
				}
				if(typeInfo.isSubject){
					tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:subjectData"));
				}
			}
			
			// NOTE: $.inArray didn't work here
			var alreadyContains = function(array, val){
				outer:
				for (var i=0; i<array.length; i++) {
					if (Object.keys(val).length !== Object.keys(array[i]).length) {
						continue;
					}
					for (var j=0; j<Object.keys(val).length; j++) {
						var key = (Object.keys(val))[j];
						var tval = val[key];
						var cval = (array[i])[key];
						if (tval !== cval) {
							continue outer;
						}
					}
					return true;
				}
				return false;
			};
			var tempConfigs2=new Array();
			//allow filtering of links
			jq.each(tmpConfigs,function(i1,v1){
				if(typeof props!=='undefined' && v1.filter){
					var filters=v1.filter.split(",");
					var matched=false;
					jq.each(filters,function (i2,v2){
						if(!matched){
							if((v2.trim()==props.trim())){
								matched=true;
							}
						}
					});
					if(matched && (!alreadyContains(tempConfigs2,v1))){
						tempConfigs2.push(v1);
					}
				}else if (!alreadyContains(tempConfigs2,v1)){
					tempConfigs2.push(v1);
				}
			});
			return tempConfigs2;
		},


	}
}

XNAT.app.abu.showScanLinks = function(){
			$('.uploadLink[data-type*="ScanData"]').each(function(value){
				var type=$(this).attr('data-type');
				var tempConfigs=new Array();
	
				var props=$(this).attr('data-props');
				
				var tempConfigs=XNAT.app.abu.abuConfigs.getAllConfigsByType(type,props)			
				if(tempConfigs.length>0 || XNAT.app.abu.hasContextEvents("Upload",type)){
					if(value.dontHide){
						$(value).color(value.defaultColor);
						$(value).css('cursor:pointer');
					}
					
					$(this).click(function(){
						XNAT.app.abu.initializeAbuUploader("upload");
						return false;
					});
					$(this).show();
				}else{
					if(!value.dontHide){
						$(this).hide();
					}
				}
			});
}

XNAT.app.abu.loadResourceConfigs = function(){

	var resourceConfigAjax = $.ajax({
		type : "GET",
		url:serverRoot+'/data/projects/' + XNAT.data.context.project +'/config/resource_config/script?contents=true&format=json',
		cache: false,
		async: true,
		context: this,
		dataType: 'json'
	 });
	resourceConfigAjax.done( function( data, textStatus, jqXHR ) {

		XNAT.app.abu.allResourceConfigs = data;
		XNAT.app.abu.getAutomationHandlers();

	});
	resourceConfigAjax.fail( function( data, textStatus, error ) {

		// Setting resourceConfigsObject to empty array.  This is usually hit because project has no resources configured.
		XNAT.app.abu.allResourceConfigs = [];
		XNAT.app.abu.getAutomationHandlers();

	});

}

XNAT.app.abu.getAutomationHandlers = function(){

	var initializeBuildAjax = $.ajax({
		type : "GET",
		url:serverRoot+"/data/projects/" + XNAT.data.context.projectID +  "/automation/handlers?format=json&XNAT_CSRF=" + window.csrfToken,
		cache: false,
		async: true,
		context: this,
		dataType: 'json'
	});
	initializeBuildAjax.done( function( data, textStatus, jqXHR ) {

		if (!(typeof data.ResultSet !== 'undefined' && typeof data.ResultSet.Result !== 'undefined')) {
			return;
		}

		XNAT.app.abu.automationEvents = data.ResultSet.Result;
		var sitewideHandlerAjax = $.ajax({
			type : "GET",
	  		url:serverRoot+"/data/automation/handlers?format=json&XNAT_CSRF=" + window.csrfToken,
			cache: false,
			async: true,
			context: this,
			dataType: 'json'
		});
		sitewideHandlerAjax.done( function( data, textStatus, jqXHR ) {

			if (typeof data.ResultSet !== 'undefined' && typeof data.ResultSet.Result !== 'undefined') {
				XNAT.app.abu.automationEvents = XNAT.app.abu.automationEvents.concat(data.ResultSet.Result);
			}
			var events = XNAT.app.abu.automationEvents;
			var resources = XNAT.app.abu.allResourceConfigs;
			var type = $('.uploadLink').not('[data-type*="ScanData"]').attr("data-type");
			var props = $('.uploadLink').not('[data-type*="ScanData"]').attr("data-props");
			XNAT.app.abu.contextResourceConfigs = XNAT.app.abu.abuConfigs.getAllConfigsByType(type,props);		
			// Determine whether or not to display links
			if (events.length>0) {
				XNAT.app.abu.initUploaderConfig();
				if (XNAT.app.abu.hasContextEvents("Upload",type) || XNAT.app.abu.contextResourceConfigs.length>0) {
					XNAT.app.abu.abuConfigs.showUploadLink();
				} else {
					XNAT.app.abu.abuConfigs.hideUploadLink();
				}
				if (XNAT.app.abu.hasContextEvents("Launch",type)) {
					XNAT.app.abu.abuConfigs.showLaunchLink();
				} else {
					XNAT.app.abu.abuConfigs.hideLaunchLink();
				}
			} else {
				XNAT.app.abu.uploaderConfig = [];
				XNAT.app.abu.abuConfigs.hideLaunchLink();	
				if (XNAT.app.abu.contextResourceConfigs.length>0) {
					XNAT.app.abu.abuConfigs.showUploadLink();	
				} else {
					XNAT.app.abu.abuConfigs.hideUploadLink();	
				}
			}
			XNAT.app.abu.showScanLinks();
		});
		sitewideHandlerAjax.fail( function( data, textStatus, jqXHR ) {
			XNAT.app.abu.abuConfigs.hideLinks();	
			console.log("GetAutomationHandlers result - ", jqXHR); 
		});

	});
	initializeBuildAjax.fail( function( data, textStatus, jqXHR ) {
		XNAT.app.abu.abuConfigs.hideLinks();	
		console.log("GetAutomationHandlers result - ", jqXHR); 
	});
}

XNAT.app.abu.hasContextEvents = function(usage,dataType){
	var events = XNAT.app.abu.automationEvents;
	var uploaderConfig = XNAT.app.abu.uploaderConfig;
	if (events.length>0) {
		for (var i=0; i<events.length; i++) {
			var currEvent = events[i];
			for (var j=0; j<uploaderConfig.length; j++) {
				var currConfig = uploaderConfig[j];
				if (currEvent.event==currConfig.event && currEvent.scope==currConfig.eventScope) {
					if (usage == 'Launch' && currConfig.launchWithoutUploads && XNAT.app.abu.checkConfigContext(currConfig,dataType)) {
						return true;
					}
					else if (usage == 'Upload' && (currConfig.launchFromResourceUploads || currConfig.launchFromCacheUploads) && XNAT.app.abu.checkConfigContext(currConfig,dataType)) {
						return true;
					}
				}
			}
		}
	} 
	return false;
}

XNAT.app.abu.eventHandlerChange = function(){
	var eventHandler = $('#eventHandlerSelect').val();
	XNAT.app.abu.updateOptionsCheckboxes(eventHandler);
	if (typeof eventHandler === 'undefined' || eventHandler == null || eventHandler.length<1 && ($("#handlerDefaultOption").html() == 'NONE DEFINED' || $("#handlerDefaultOption").html() == 'SELECT')) {
		$("#xmodal-abu-process-button").prop("disabled","disabled");
		//$("#abu-process-button-text").html("&nbsp;");
		//$("#abu-process-button").css("visibility","hidden");
	} else if ((XNAT.app.abu.usageSelect=='Launch') || (abu._fileUploader._uploadStarted && abu._fileUploader._filesInProgress<1)) {
		$("#xmodal-abu-process-button").prop("disabled",false);
		//$("#abu-process-button-text").html("&nbsp;");
		//$("#abu-process-button").css("visibility","visible");
	}
	XNAT.app.abu.filesProcessed = false;
}

XNAT.app.abu.initUploaderConfig = function(){

	var uploaderConfigAjax = $.ajax({
		type : "GET",
 		url:serverRoot+'/data/projects/' + XNAT.data.context.project +'/config/automation_uploader/configuration?contents=true',
		cache: false,
		async: false,
		context: this,
		dataType: 'json'
	 });
	uploaderConfigAjax.done( function( data, textStatus, jqXHR ) {

		if (typeof data !== 'undefined' && $.isArray(data)) {
			// Configurations must have trigger IDs (remove old-style (pre XNAT 1.7) configurations)
			for (var i = data.length -1; i >= 0 ; i--) {
				var triggerId = data[i].eventTriggerId;
				if (typeof triggerId == 'undefined' || triggerId.length<1) {
					data.splice(i,1);
				}
			}
			XNAT.app.abu.uploaderConfig = data;
		} else {
			XNAT.app.abu.uploaderConfig = [];
		}
                XNAT.app.abu.initUploaderSiteConfig();
	});

	uploaderConfigAjax.fail( function( data, textStatus, error ) {
		XNAT.app.abu.uploaderConfig = [];
                XNAT.app.abu.initUploaderSiteConfig();
	});

}

XNAT.app.abu.initUploaderSiteConfig = function(){

	var uploaderSiteConfigAjax = $.ajax({
		type : "GET",
 		url:serverRoot+'/data/config/automation_uploader/configuration?contents=true',
		cache: false,
		async: false,
		context: this,
		dataType: 'json'
	 });
	uploaderSiteConfigAjax.done( function( data, textStatus, jqXHR ) {
		if (typeof data !== 'undefined' && $.isArray(data) && data.length>0) {
			// Configurations must have trigger IDs (remove old-style (pre XNAT 1.7) configurations)
			for (var i = data.length -1; i >= 0 ; i--) {
				var triggerId = data[i].eventTriggerId;
				if (typeof triggerId == 'undefined' || triggerId.length<1) {
					data.splice(i,1);
				}
			}
			Array.prototype.push.apply(XNAT.app.abu.uploaderConfig,data);
		}
	});
	uploaderSiteConfigAjax.fail( function( data, textStatus, error ) {
		console.log("Get site uploader configuration result - ", error); 
	});

}

XNAT.app.abu.checkConfigContext = function(currConfig,dataType) {
	if (typeof(currConfig.contexts) === 'undefined' || currConfig.contexts.length<1) {
		return true;
	}
	for (var i=0;i<currConfig.contexts.length;i++) {
		if (typeof dataType !== 'undefined') {
			if (currConfig.contexts[i]==dataType) {
				return true;
			}
		} 
		if (
			(!(typeof dataType !== 'undefined' && dataType.indexOf("ScanData")>=0)) && 
			((currConfig.contexts[i]==XNAT.data.context.xsiType) ||
				(XNAT.data.context.isSubjectAssessor && currConfig.contexts[i]=="xnat:subjectAssessorData") || 
				(XNAT.data.context.isImageAssessor && currConfig.contexts[i]=="xnat:imageAssessorData") || 
				(XNAT.data.context.isImageSession && currConfig.contexts[i]=="xnat:imageSessionData") || 
				(XNAT.data.context.isImageScan && currConfig.contexts[i]=="xnat:imageScanData") || 
				(XNAT.data.context.isSubject && currConfig.contexts[i]=="xnat:subjectData"))) {
			return true;
		} 
	}
	return false;
}

XNAT.app.abu.populateEventHandlerSelect = function(){
	var events = XNAT.app.abu.automationEvents;
	var type = $(XNAT.app.abu.currentLink).attr("data-type");
	var props = $(XNAT.app.abu.currentLink).attr("data-props");
	XNAT.app.abu.contextResourceConfigs = XNAT.app.abu.abuConfigs.getAllConfigsByType(type,props);		
	var resourceConfigs = XNAT.app.abu.contextResourceConfigs;
	var uploaderConfig = XNAT.app.abu.uploaderConfig;
	var usageSelect = $('#usageSelect').val();
	var resourceSelect = $('#resourceSelect').val();
	$('#eventHandlerSelect').find('option').remove();
	if (events.length>0) {
		$('#eventHandlerSelect').append('<option id="handlerDefaultOption" value="">' + 
			((usageSelect=='Launch' || resourceSelect==XNAT.app.abu.cacheConstant) ?  'SELECT' : 'DEFAULT') + '</option>'); 
		outerLoop:
		for (var i=0; i<events.length; i++) {
			var currEvent = events[i];
			for (var j=0; j<uploaderConfig.length; j++) {
				var currConfig = uploaderConfig[j];
				if (currEvent.triggerId==currConfig.eventTriggerId && currEvent.scope==currConfig.eventScope) {
					var doAssign = true;
					if (usageSelect == 'Upload' && resourceSelect==XNAT.app.abu.cacheConstant && !currConfig.launchFromCacheUploads) {
						doAssign = false;
					} else if (usageSelect == 'Launch' && !currConfig.launchWithoutUploads) {
						doAssign = false;
					} else if (usageSelect == 'Upload' && resourceSelect != XNAT.app.abu.cacheConstant) {
						if (!currConfig.launchFromResourceUploads) {
							doAssign = false;
						} else if ((typeof currConfig.resourceConfigs === 'undefined') ||
								 (currConfig.resourceConfigs.length>0 && $.inArray(resourceSelect,currConfig.resourceConfigs)<0)) {
							doAssign = false;
						}
					}
					if (!XNAT.app.abu.checkConfigContext(currConfig,type)) {
						doAssign = false;
					}
					if (doAssign) {
						$('#eventHandlerSelect').append('<option value="' + currEvent.triggerId + '" class="' + currEvent.scope + '">' +
						 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.event) + '</option>');
					}
					continue outerLoop;
				}
			}
			// We now don't want to include event handlers with no upload configuration in the display.  The default for handlers is to not use the uploader.
			//$('#eventHandlerSelect').append('<option value="' + currEvent.triggerId + '" class="' + currEvent.scope + '">' +
			//	 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.scriptId) + '</option>');
		}
		if ($('#eventHandlerSelect').find('option').length==1) {
			$('#handlerDefaultOption').html('NONE DEFINED'); 
		} else if ($('#eventHandlerSelect').find('option').length==2) {
			$($('#eventHandlerSelect').find('option').get(0)).remove();
		}
	} else {
		$('#eventHandlerSelect').append('<option value="">NONE DEFINED</option>'); 
	}
}

XNAT.app.abu.populateWhatToDoSelect = function(){
	var events = XNAT.app.abu.automationEvents;
	var type = $(XNAT.app.abu.currentLink).attr("data-type");
	var props = $(XNAT.app.abu.currentLink).attr("data-props");
	XNAT.app.abu.contextResourceConfigs = XNAT.app.abu.abuConfigs.getAllConfigsByType(type,props);		
	var resourceConfigs = XNAT.app.abu.contextResourceConfigs;
	var uploaderConfig = XNAT.app.abu.uploaderConfig;
	var usageSelect = $('#usageSelect').val();
	var resourceSelect = $('#resourceSelect').val();
	$('#whatToDoSelect').append('<option id="handlerDefaultOption" value="">' + 
		((usageSelect=='Launch' || resourceSelect==XNAT.app.abu.cacheConstant) ?  'SELECT' : 'DEFAULT') + '</option>'); 
	if (XNAT.app.abu.contextResourceConfigs!=undefined && XNAT.app.abu.contextResourceConfigs.length>0) {
		for (var h=0; h<resourceConfigs.length; h++) {
			var resourceMatch = false;
			if (events.length>0) {
				outerLoop:
				for (var i=0; i<events.length; i++) {
					var currEvent = events[i];
					if (currEvent.event == ("Uploaded " + resourceConfigs[h].name)) {
						for (var j=0; j<uploaderConfig.length; j++) {
							var currConfig = uploaderConfig[j];
							if (currEvent.triggerId==currConfig.eventTriggerId && currEvent.scope==currConfig.eventScope) {
								var doAssign = true;
								if ((usageSelect == 'Launch') ||
								   (!(currConfig.launchFromResourceUploads)) ||
								   (!XNAT.app.abu.checkConfigContext(currConfig,type))) {
									doAssign = false;
								}
								if (doAssign) {
									$('#whatToDoSelect').append('<option value="resource- ' + resourceConfigs[h].name + ':launch-' + currEvent.triggerId + '" class="' + currEvent.scope + '">' + 
				                                                (
											(typeof currEvent == 'undefined' || typeof currEvent.description == 'undefined' || currEvent.description.indexOf("-->")<0)
											/*
											Per XNAT-4333, Use name rather than description
 											? (((typeof resourceConfigs[h].description !== 'undefined' && resourceConfigs[h].description.length>0) ? resourceConfigs[h].description : resourceConfigs[h].name) + " --> ") : ""
											*/
 											? (resourceConfigs[h].name + " --> ") : ""
										) +

									 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.event) + '</option>');
									resourceMatch = true;
								}
								continue outerLoop;
							}
						}
						// We now don't want to include event handlers with no upload configuration in the display.  The default for handlers is to not use the uploader.
						//$('#whatToDoSelect').append('<option value="resource-' + resourceConfigs[h].name + ':launch-' + currEvent.triggerId + '" class="' + currEvent.scope + '">' + 
				                //                ((typeof resourceConfigs[h].description !== 'undefined' && resourceConfigs[h].description.length>0) ? resourceConfigs[h].description : resourceConfigs[h].name) + " --> " + 
						//	 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.scriptId) + '</option>');
						//resourceMatch = true;
					}
				}
			} 
			if (!resourceMatch) {
				/*
				Per XNAT-4333, Use name rather than description
				$("#whatToDoSelect").append('<option value="resource-' + resourceConfigs[h].name + '">' + ((typeof resourceConfigs[h].description !== 'undefined' && resourceConfigs[h].description.length>0) ? resourceConfigs[h].description : resourceConfigs[h].name) + '</option>');
				*/
				$("#whatToDoSelect").append('<option value="resource-' + resourceConfigs[h].name + '">' + resourceConfigs[h].name + '</option>');
			}
		}
	} 
	if (events.length>0) {
		outerLoop:
		for (var i=0; i<events.length; i++) {
			var currEvent = events[i];
			for (var j=0; j<uploaderConfig.length; j++) {
				var currConfig = uploaderConfig[j];
				if (currEvent.triggerId==currConfig.eventTriggerId && currEvent.scope==currConfig.eventScope) {
					var doAssign = true;
					if ((usageSelect == 'Launch') ||
					   (!(currConfig.launchFromCacheUploads)) ||
					   (!XNAT.app.abu.checkConfigContext(currConfig,type))) {
						doAssign = false;
					}
					if (doAssign) {
						$('#whatToDoSelect').append('<option value="resource-' + XNAT.app.abu.cacheConstant + ':launch-' + currEvent.triggerId + '" class="' + currEvent.scope + '">' + /*"Upload --> " +*/ 
						 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.event) + '</option>');
					}
					continue outerLoop;
				}
			}
			// We now don't want to include event handlers with no upload configuration in the display.  The default for handlers is to not use the uploader.
			//$('#whatToDoSelect').append('<option value="resource-' + XNAT.app.abu.cacheConstant + ':launch-' + currEvent.triggerId + '" class="' + currEvent.scope + '">' + "Upload --> " + 
			//	 ((typeof(currEvent.description) !== 'undefined' && currEvent.description.length>0) ? currEvent.description : currEvent.scriptId) + '</option>');
		}
	} 
	if ($('#whatToDoSelect').find('option').length==1) {
		$('#whatToDoOption').html('NONE DEFINED'); 
	} else if ($('#whatToDoSelect').find('option').length==2) {
		$('#whatToDoSelect').find('option').get(0).parentElement.removeChild($('#whatToDoSelect').find('option').get(0));
	}
	XNAT.app.abu.whatToDoChange();
}

XNAT.app.abu.initializeAbuUploader = function(usageType){

	if (usageType === "upload") {
		XNAT.app.abu.abuConfigs.modalOpts.title = "Upload Additional Files";
	} else if (usageType === "launch") {
		XNAT.app.abu.abuConfigs.modalOpts.title = "Script Launcher";
	}
	$("div.title").find("span.inner").html(XNAT.app.abu.abuConfigs.modalOpts.title);
	var events = XNAT.app.abu.automationEvents;
	var type = $(XNAT.app.abu.currentLink).attr("data-type");
	var props = $(XNAT.app.abu.currentLink).attr("data-props");
	XNAT.app.abu.contextResourceConfigs = XNAT.app.abu.abuConfigs.getAllConfigsByType(type,props);		
	var resourceConfigs = XNAT.app.abu.contextResourceConfigs;
	var scriptDiv = '';
	var usageSelect = '<div class="abu-xnat-interactivity-area-sub usage-area"><span class="interactivityAreaSpan">Usage:</span> <select id="usageSelect" onchange="XNAT.app.abu.usageSelectAction()">'; 
                if (typeof usageType == 'undefined' || usageType == 'upload') {
			XNAT.app.abu.usageSelect = 'Upload';
			usageSelect+='<option value="Upload">Upload Files</option>'; 
		}
                if (typeof usageType == 'undefined' || usageType == 'launch') {
			if (usageType == 'launch') {
				XNAT.app.abu.usageSelect = 'Launch';
			}
			usageSelect+='<option value="Launch">Script Launcher</option>'; 
		}
	usageSelect+='</select></div>';
	var resourceSelect = '<div class="abu-xnat-interactivity-area-sub upload-area"><span class="interactivityAreaSpan">';
	if (usageType === "upload") {
		resourceSelect+='Configured Resource:</span>';
	} else {
		resourceSelect+='Upload location:</span>';
	}
        resourceSelect+='<select id="resourceSelect" onchange="XNAT.app.abu.updateModalAction()">'; 
	if (XNAT.app.abu.contextResourceConfigs!=undefined && XNAT.app.abu.contextResourceConfigs.length>0) {
		resourceSelect+='<option value="' + XNAT.app.abu.cacheConstant + '">Cache Space</option>'; 
		for (var i=0; i<resourceConfigs.length; i++) {
			resourceSelect+=('<option value="' + resourceConfigs[i].name + '">' + resourceConfigs[i].name + '</option>');
		}
	} else {
		resourceSelect+='<option value="' + XNAT.app.abu.cacheConstant + '">Cache Space</option>'; 
	}
	resourceSelect+='</select></div>';
	var eventSelect = '<div class="abu-xnat-interactivity-area-sub eventhandler-area"><span id="script-select-text" class="interactivityAreaSpan">Script to run:</span> <select id="eventHandlerSelect" onchange="XNAT.app.abu.eventHandlerChange()">'; 
	eventSelect+='</select></div>';
	var whatToDoSelect = '<div class="abu-xnat-interactivity-area-sub whattodo-area"><span id="script-select-text" class="interactivityAreaSpan">Upload Selector:</span> <select id="whatToDoSelect" onchange="XNAT.app.abu.whatToDoChange()">'; 
	whatToDoSelect+='</select></div>';
	scriptDiv+=usageSelect;
	scriptDiv+=resourceSelect;
	scriptDiv+=eventSelect;
	scriptDiv+=whatToDoSelect;
	try {
		xmodal.open(XNAT.app.abu.abuConfigs.modalOpts);
		abu.initializeUploader({
			element:document.getElementById('modalUploadDiv'),
			action:'TBD',
			debug:true,
			doneFunction:function(){
					// Since we're using the update-stats=false parameter for resource uploads, we need to call catalog refresh when we're finished uploading.
					if (abu._fileUploader.uploadsStarted>0 && abu._fileUploader.currentUploads==0) {
						XNAT.app.abu.updateResourceStats();
						// Create workflow if we just uploaded files without any script processing (otherwise, workflow will have been generated there)
						if (!XNAT.app.abu.filesProcessed) {
							XNAT.app.abu.sendWorkflowWhenDone();
						}
					}
					xmodal.close(XNAT.app.abu.abuConfigs.modalOpts.id);
					if (abu._fileUploader.uploadsStarted>0 && abu._fileUploader.currentUploads==0) {
						setTimeout(function(){
							window.location.reload(true);
						},20);
						xmodal.message('Please Wait','Reloading page.  Please wait.');
					}
				},
			uploadStartedFunction:function(){
					if (abu._fileUploader._currentAction.indexOf("import-handler=" + XNAT.app.abu.importHandler)>=0 && (typeof(XNAT.app.abu.buildPath) == 'undefined' || XNAT.app.abu.buildPath == '')) {
						XNAT.app.abu.initializeBuildPath();
					} 
					// Force press of processing button after upload is started so workflow is created.  Only want one workflow per upload.
					// Don't want cancellation of process during upload or incomplete file could end up in resource
					if (abu._fileUploader._currentAction.indexOf("import-handler=" + XNAT.app.abu.importHandler)<0) {
						$("#xmodal-abu-done-button").hide();
					}
					$("#resourceSelect").prop('disabled','disabled');
					if ($("#whatToDoSelect").val() != "") {
						$("#whatToDoSelect").prop('disabled','disabled');
					}
					$("#usageSelect").prop('disabled','disabled');
				},
			uploadCompletedFunction:function(){
					var eventHandler = $('#eventHandlerSelect').val();
					if (typeof eventHandler !== 'undefined' && eventHandler != null && eventHandler.length>0) {
						if ($(".abu-upload-complete-text").length==0) {
							// $("#abu-done-button").removeClass("abu-button-disabled");
							$("#abu-done-button").show();
							$('#xmodal-abu-process-button').hide();
						} else {
							// $("#abu-done-button").addClass("abu-button-disabled");
							$("#abu-done-button").hide();
							$("#xmodal-abu-cancel-button").hide();
						}
						$("#xmodal-abu-process-button").prop("disabled",false);
						$("#xmodal-abu-process-button").show();
						//$("#abu-process-button-text").html("Process Files");
						//$("#abu-process-button").css("visibility","visible");
					} else {
						// $("#abu-done-button-text").html("Done");
						// $("#abu-done-button-text").addClass("abu-done-button-done");
						// $("#abu-done-button-text").removeClass("abu-done-button-cancel");
						// $("#abu-done-button").removeClass("abu-button-disabled");
						$("#xmodal-abu-cancel-button").hide();
						$('#xmodal-abu-done-button').show();
						$('#xmodal-abu-process-button').hide();
					}
				},
			processFunction:function(){
					XNAT.app.abu.processFiles();
					$('#xmodal-abu-process-button').hide();
				},
			showEmailOption:true,
			showCloseOption:true,
			showExtractOption:(usageType !== 'launch'),
			showVerboseOption:false,
			showUpdateOption:false,
		});	
		abu._fileUploader.buildUploaderDiv();
		$(".abu-xnat-interactivity-area-contents").html(scriptDiv);
		XNAT.app.abu.populateEventHandlerSelect();
		abu._fileUploader._currentAction = XNAT.app.abu.automationHandlerUrl(true);
		if (typeof usageType !== 'undefined' && usageType != null) {
			$(".usage-area").css("display","none");
			if (usageType === 'launch') {
				var eventHandler = $('#eventHandlerSelect').val();
				if (eventHandler != undefined && eventHandler != null && eventHandler.length>0) {
					$("#xmodal-abu-process-button").prop("disabled",false);
					$("#xmodal-abu-process-button").show();
				} else {
					// $("#abu-done-button").removeClass("abu-button-disabled");
					// $("#abu-done-button-text").html("Done");
					// $("#abu-done-button-text").addClass("abu-done-button-done");
					// $("#abu-done-button-text").removeClass("abu-done-button-cancel");
					$("#xmodal-abu-process-button").prop("disabled",false);
					$('#xmodal-abu-done-button').show();
				}
				$(".upload-area").hide();
				$(".whattodo-area").hide();
				$(".abu-upload-button")
					.hide()
					.prop("disabled","disabled");
				abu._fileUploader.DRAG_AND_DROP_ON = false;
				$("#xmodal-abu-process-button").html("Run script");
				// $("#abu-done-button-text").html("Cancel");
				// $("#abu-done-button-text").addClass("abu-done-button-cancel");
				// $("#abu-done-button-text").removeClass("abu-done-button-done");
				$("#xmodal-abu-done-button").hide();
				$("#xmodal-abu-process-button").show();
				if ($('#eventHandlerSelect option').size()>1 && $('#eventHandlerSelect').val()=="") {
					$("#xmodal-abu-process-button").prop("disabled","disabled");
					// $("#abu-process-button-text").html("&nbsp;");
					// $("#xmodal-abu-process-button").hide();
				} 
				$("#file-uploader-instructions-sel").css("display","none");
			} else {
				XNAT.app.abu.populateWhatToDoSelect();
				if ($('#whatToDoSelect option').size()>1 && $('#whatToDoSelect').val()=="") {
					$("#abu-upload-button").prop("disabled","disabled");
					$("#abu-upload-button").addClass("abu-button-disabled");
					abu._fileUploader.DRAG_AND_DROP_ON = false;
				} 
				$("#xmodal-abu-process-button").prop("disabled","disabled");
				//$("#abu-process-button-text").html("&nbsp;");
				// $("#xmodal-abu-process-button").hide();
				$(".upload-area").hide();
				$(".eventhandler-area").hide();
			}
		} else {
			$(".whattodo-area").hide();
		}
	} catch(e) {
		console.log(e.stack);
		xmodal.message('Error','ERROR:  Could not parse event handlers');
	}

}

XNAT.app.abu.automationHandlerUrl = function(withBuildPath){
	return serverRoot+"/REST/services/import?import-handler=".replace(/\/\//,"/") + XNAT.app.abu.importHandler + ((withBuildPath) ? "&process=false&buildPath=" + XNAT.app.abu.buildPath : '') + "&XNAT_CSRF=" + window.csrfToken;
}

XNAT.app.abu.usageSelectAction = function(){
	XNAT.app.abu.usageSelect = $('#usageSelect').val();
	if (XNAT.app.abu.usageSelect=='Upload') {
		XNAT.app.abu.populateEventHandlerSelect();
		$("#xmodal-abu-done-button")
			.show()
			.prop("disabled",false);
		$(".abu-upload-button").prop("disabled",false);
		$("#abu-upload-button").removeClass("abu-button-disabled");
		abu._fileUploader.DRAG_AND_DROP_ON = true;
		$("#xmodal-abu-process-button")
			.hide()
			.prop("disabled","disabled");
		//$("#abu-process-button-text").html("&nbsp;");
		$("#script-select-text").html("Post-upload processing script:");
		$("#resourceSelect").prop('disabled',false);
		$(".response_text").html('');
	} else if (XNAT.app.abu.usageSelect=='Launch') { 
		XNAT.app.abu.populateEventHandlerSelect();
		$(".abu-upload-button").prop("disabled","disabled");
		$("#abu-upload-button").addClass("abu-button-disabled");
		abu._fileUploader.DRAG_AND_DROP_ON = false;
		var eventHandler = $('#eventHandlerSelect').val();
		if (eventHandler != undefined && eventHandler != null && eventHandler.length>0) {
			$("#xmodal-abu-process-button").prop("disabled",false);
			// $("#xmodal-abu-process-button").show();
		}
		$("#script-select-text").html("Script to launch:");
		$("#xmodal-abu-process-button").html("Run script");
		$("#xmodal-abu-done-button").hide();
		$("#xmodal-abu-process-button").show();
		$("#resourceSelect").prop('disabled','disabled');
		$(".response_text").html('');
	}
}

XNAT.app.abu.updateModalAction = function(){
	XNAT.app.abu.configuredResource = $('#resourceSelect').val();
	if (XNAT.app.abu.configuredResource==XNAT.app.abu.cacheConstant) {
		abu._fileUploader._currentAction = XNAT.app.abu.automationHandlerUrl();
		XNAT.app.abu.populateEventHandlerSelect();
		return;
	} else {
		var resourceConfigs = XNAT.app.abu.contextResourceConfigs;
		if (XNAT.app.abu.contextResourceConfigs!=undefined && XNAT.app.abu.contextResourceConfigs!=null && XNAT.app.abu.contextResourceConfigs.length>0) {
			for (var i=0; i<resourceConfigs.length; i++) {
				if (XNAT.app.abu.configuredResource==resourceConfigs[i].name) {
					// NOTE:  Setting update-stats=false (no workflow entry for individual files).  The process step will create a workflow entry for the entire upload.
					var subdir = resourceConfigs[i].subdir;
					subdir = (typeof subdir !== 'undefined' && subdir.length > 0) ? "/" + subdir : subdir;
					abu._fileUploader._currentAction = $(XNAT.app.abu.currentLink).attr("data-uri") + "/resources/" + resourceConfigs[i].label + "/files" + subdir + "/##FILENAME_REPLACE##?overwrite=" + resourceConfigs[i].overwrite + "&update-stats=false&XNAT_CSRF=" + window.csrfToken;
					XNAT.app.abu.populateEventHandlerSelect();
					return;
				}
			}
		}
	}
	XNAT.app.abu.populateEventHandlerSelect();
}

XNAT.app.abu.updateOptionsCheckboxes = function(selectVal) {
	// First set default values that will take effect if there is no event handlers (e.g. configured resource uploads with no scripts);
	$("#extractRequestBoxDiv").show();
	$("#extractRequestBox").attr('checked',true);
	$("#closeBoxDiv").hide();
	$("#closeBox").attr('checked',false);
	$("#emailBoxDiv").hide();
	$("#emailBox").attr('checked',false);
	// Now we'll set values for upload requests with associated event handlers
	for (var i=0;i<this.uploaderConfig.length;i++) {
		if (this.uploaderConfig[i].eventTriggerId==selectVal) { // && this.uploaderConfig[i].eventScope==eventHandlerScope) {
			var uploaderConfig = this.uploaderConfig[i];

			if (typeof uploaderConfig.showExtractOption !== 'undefined') {
				if (uploaderConfig.showExtractOption) {
					$("#extractRequestBoxDiv").show();
				} else {
					$("#extractRequestBoxDiv").hide();
				}
			} else {
				$("#extractRequestBox").show();
			}
			if (typeof uploaderConfig.extractOptionChecked !== 'undefined') {
				if (uploaderConfig.extractOptionChecked) {
					$("#extractRequestBox").attr('checked',true);
				} else {
					$("#extractRequestBox").attr('checked',false);
				}
			} else {
				$("#extractRequestBox").attr('checked',true);
			}

			if (typeof uploaderConfig.showCloseWindowOption !== 'undefined') {
				if (uploaderConfig.showCloseWindowOption) {
					$("#closeBoxDiv").show();
				} else {
					$("#closeBoxDiv").hide();
				}
			} else {
				$("#closeBoxDiv").show();
			}
			if (typeof uploaderConfig.closeWindowOptionChecked !== 'undefined') {
				if (uploaderConfig.closeWindowOptionChecked) {
					$("#closeBox").attr('checked',true);
				} else {
					$("#closeBox").attr('checked',false);
				}
			} else {
				$("#closeBox").attr('checked',false);
			}

			if (typeof uploaderConfig.showEmailOption !== 'undefined') {
				if (uploaderConfig.showEmailOption) {
					$("#emailBoxDiv").show();
				} else {
					$("#emailBoxDiv").hide();
				}
			} else {
				$("#emailBoxDiv").show();
			}
			if (typeof uploaderConfig.emailOptionChecked !== 'undefined') {
				if (uploaderConfig.emailOptionChecked) {
					$("#emailBox").attr('checked',true);
				} else {
					$("#emailBox").attr('checked',false);
				}
			} else {
				$("#emailBox").attr('checked',false);
			}
			//console.log($("#extractRequestBox").is(":checked"));
			//console.log($("#closeBox").is(":checked"));
			//console.log($("#emailBox").is(":checked"));

			break;
		}
	}
	abu._fileUploader.updateOptionStatus();
}

XNAT.app.abu.whatToDoChange = function(){
	XNAT.app.abu.whatToDo = $('#whatToDoSelect').val();
        var whatToDo = XNAT.app.abu.whatToDo;
	var resourceSelect = whatToDo.replace(/^resource-/,"").replace(/:launch-.*$/,"").trim();
	var launchSelect = ((whatToDo.indexOf(":launch-")>=0) ? whatToDo.replace(/^.*:launch-/,"") : "").trim();
	$('#resourceSelect').val(resourceSelect);
	XNAT.app.abu.updateModalAction();
	$('#eventHandlerSelect').val(launchSelect);
	XNAT.app.abu.updateOptionsCheckboxes(launchSelect);
	if (typeof abu !== 'undefined' && abu._fileUploader.uploadsStarted>0 && abu._fileUploader.currentUploads==0) {
		$("#xmodal-abu-process-button")
			.prop("disabled","disabled")
			.html("Process files")
			.show();
	}
	if (XNAT.app.abu.usageSelect == 'Upload' && $('#whatToDoSelect option').size()>1 && $('#whatToDoSelect').val()=="") {
		$(".abu-upload-button").prop("disabled","disabled");
		$("#abu-upload-button").addClass("abu-button-disabled");
		abu._fileUploader.DRAG_AND_DROP_ON = false;
	} else if (typeof abu == 'undefined' || abu._fileUploader.uploadsStarted==0) {
		$(".abu-upload-button").prop("disabled",false);
		$("#abu-upload-button").removeClass("abu-button-disabled");
		abu._fileUploader.DRAG_AND_DROP_ON = true;
	} 
	XNAT.app.abu.filesProcessed = false;
}

XNAT.app.abu.initializeBuildPath = function(){
		// NOTE:  This call is made synchronously so we only create a build path if we do a cached upload
		var initializeBuildAjax = $.ajax({
			type : "POST",
	  		url:XNAT.app.abu.automationHandlerUrl(false),
			cache: false,
			async: false,
			context: this,
			dataType: 'text',
			success:function(data) {
				XNAT.app.abu.buildPath=XNAT.app.abu.processReturnedText(data,false);
				abu._fileUploader._currentAction = XNAT.app.abu.automationHandlerUrl(true);
			}
		  });
}

XNAT.app.abu.validatePassedParameters=function() {
	var errorText = '';
	var paramTextEle = $(".passParamText");
	var paramData = {};
	for (var i=0;i<paramTextEle.length;i++) {
		var paramText = $($(paramTextEle).get(i)).html();
		for (var j=0;j<this.paramsToPass.length;j++) {
			if (this.paramsToPass[j].name==paramText) {
				var paramVal = $($(".passParamInput").get(i)).val();
				paramData[paramText] = paramVal;
				var paramRequired = (this.paramsToPass[j].required==undefined || this.paramsToPass[j].required.toString().toLowerCase() != 'false');
				if (paramRequired && (typeof(paramVal)=='undefined' || paramVal.length<1)) {
					errorText+="ERROR:  " + paramText + " is required;  ";
				}
				if (this.paramsToPass[j].type == undefined) { 
					return;
				}
				if (this.paramsToPass[j].type.toString().toLowerCase() == "float" && isNaN(paramVal)) {
					errorText+="ERROR:  " + paramText + " must be a valid floating point number;  ";
				} else if (this.paramsToPass[j].type.toString().toLowerCase() == "integer" && (isNaN(paramVal) || paramVal % 1 !== 0)) {
					errorText+="ERROR:  " + paramText + " must be a valid integer;  ";
				}
				break;
			}
		}
	}
	if (errorText.length>0) {
		$("#passParamErrorDiv").html(errorText);
		return false;
	}
	XNAT.app.abu.paramData = paramData;
	return true;
}

XNAT.app.abu.updateResourceStats=function() {
	if (XNAT.app.abu.usageSelect !== 'Launch') {
		if (abu._fileUploader._currentAction.indexOf("import-handler=" + XNAT.app.abu.importHandler)<0) {
			var updateStatsUrl = "/data/services/refresh/catalog?resource=" + 
				abu._fileUploader._currentAction.replace(/\/files[\/?].*$/i,'').replace(/^\/data\//i,"/archive/").replace(/^\/REST\//i,"/archive/" +
				"&options=populateStats") + "&options=populateStats,append,delete,checksum&XNAT_CSRF=" + window.csrfToken;
			var updateStatsAjax = $.ajax({
				type : "POST",
				url:serverRoot+updateStatsUrl,
				cache: false,
				async: true,
				context: this,
				dataType: 'text'
			  });
			updateStatsAjax.done( function( data, textStatus, jqXHR ) {
				// Do nothing.
			});
			updateStatsAjax.fail( function( data, textStatus, jqXHR ) {
				// Do nothing.
			});
		}
	}
}

XNAT.app.abu.sendWorkflowWhenDone=function() {
	if (XNAT.app.abu.usageSelect !== 'Launch') {
		if (abu._fileUploader._currentAction.indexOf("import-handler=" + XNAT.app.abu.importHandler)<0) {
			var params = {};
			params['project'] = XNAT.data.context.projectID;
			params['process'] = 'true';
			if (typeof(XNAT.app.abu.configuredResource)!=='undefined' && XNAT.app.abu.configuredResource!=null) {
				params['configuredResource'] = XNAT.app.abu.configuredResource;
			}
			params['XNAT_CSRF'] = window.csrfToken;
			if (XNAT.data.context.isSubject) {
				params['subject'] = XNAT.data.context.subjectID;
			}
			if (XNAT.data.context.isExperiment || XNAT.data.context.isImageAssessor || XNAT.data.context.isImageSession || XNAT.data.context.isSubjectAssessor) {
				params['experiment'] = XNAT.data.context.ID;
			}
			params['xsiType'] = XNAT.data.context.xsiType;
			var queryParams = "?import-handler=" + XNAT.app.abu.importHandler + "&" + $.param(params);
			var processAjax = $.ajax({
				type : "POST",
				url:serverRoot+"/REST/services/import" + queryParams,
				cache: false,
				async: true,
				/*
				data: JSON.stringify(this.paramData),
				contentType: "application/json; charset=utf-8",
				data: this.paramData,
				contentType: "application/x-www-form-urlencoded",
				encode: true,
				*/
				context: this,
				dataType: 'text'
			  });
			processAjax.done( function( data, textStatus, jqXHR ) {
				console.log(XNAT.app.abu.processReturnedText(data,true));
			});
			processAjax.fail( function( data, textStatus, jqXHR ) {
				console.log(XNAT.app.abu.processReturnedText(data,true));
			});
		}
	}
}

XNAT.app.abu.processFiles=function() {

		XNAT.app.abu.filesProcessed = true;

		$(".abu-files-processing").css("display","block");
		$(".abu-uploader").css("overflow-x","hidden");
		$(".abu-uploader").css("overflow-y","hidden");

		// Since we're using the update-stats=false parameter, we need to call catalog refresh when we're finished uploading.
		XNAT.app.abu.updateResourceStats();

		var eventHandler = $('#eventHandlerSelect').val();
		var eventHandlerElement = $('#eventHandlerSelect').find('option:selected').get(0);
 		if (typeof(eventHandlerElement) !== 'undefined' && eventHandlerElement != null) {
			var eventHandlerScope = eventHandlerElement.className;
		}

		this.currentUploaderConfig = undefined;
		this.paramsToPass = undefined;
		for (var i=0;i<this.uploaderConfig.length;i++) {
			if (this.uploaderConfig[i].eventTriggerId==eventHandler && this.uploaderConfig[i].eventScope==eventHandlerScope) {
				this.currentUploaderConfig = this.uploaderConfig[i];
				break;
			}
		}
		this.paramsToPass = undefined;
		if (this.currentUploaderConfig.parameters!=undefined && this.currentUploaderConfig.parameters!=null && this.currentUploaderConfig.parameters.length>0) {
			this.paramsToPass = this.currentUploaderConfig.parameters;
		}

		if (typeof(this.paramsToPass) !== 'undefined' && this.paramsToPass != null && this.paramsToPass.length>0) {
			var pModalOpts = {
				width: 740,  
				height: 480,  
				id: 'xmodal-passed-param',  
				title: "Information required",
				content: "<div id='modalParamsToPassDiv'></div>",
				overflow: "hidden",
				ok: 'show',
				okLabel: 'Continue',
				okAction: function(modl){ 
							if (XNAT.app.abu.validatePassedParameters()) {
								XNAT.app.abu.continueProcessing();
								modl.close();
							} else {
								$("#passParamErrorDiv").removeClass("hidden");
							}
	
						 },
				okClose: false,
				cancel: 'Cancel',
				cancelLabel: 'Cancel',
				cancelAction: function(){ xmodal.close(XNAT.app.abu.abuConfigs.modalOpts.id); },
				closeBtn: 'hide'
			};
			xmodal.open(pModalOpts);
			paramText='';
			for (var i=0;i<this.paramsToPass.length;i++) {
				paramText+='<tr><td style="padding-bottom:5px"><span id="passParamText' + i + '" class="passParamText" style="font-weight:bold">' + this.paramsToPass[i].name +
					'</span></td><td style="padding-bottom:5px"><input type="text" size="30" id="passParamInput' + i + '" class="passParamInput" value="' + this.paramsToPass[i].defaultVal + '"></td>' +
					'<td style="padding-bottom:5px;"><span id="passParamDesc' + i + '" class="passParamDesc" style="margin-left:10px;width:300px;float:left;">' + this.paramsToPass[i].description + "</span></td>" +
					'</tr>';
			}
			$('#modalParamsToPassDiv').html('<div id="passParamErrorDiv" class="error hidden"></div><h3>' +
				 ((this.paramsToPass.length>0) ? "Please supply values for the following parameters:" :  "Please supply a value for the following parameter:") +
'				</h3><div style="width:100px"><table>' + paramText + "</table>");
			// Not sure why the setTimeout seems necessary.
			$($(".passParamInput").get(0)).focus();
			setTimeout(function(){
				$($(".passParamInput").get(0)).focus();
			},100);

		} else {
			XNAT.app.abu.paramData = {};
			XNAT.app.abu.continueProcessing();
		}
}

XNAT.app.abu.continueProcessing=function() {

		var params = {};
		params['project'] = XNAT.data.context.projectID;
		params['process'] = 'true';
		if (typeof(XNAT.app.abu.buildPath)!=='undefined' && XNAT.app.abu.buildPath != null && XNAT.app.abu.buildPath!=='') {
			params['buildPath'] = XNAT.app.abu.buildPath;
		}
		if (typeof(XNAT.app.abu.configuredResource)!=='undefined' && XNAT.app.abu.configuredResource!=null) {
			params['configuredResource'] = XNAT.app.abu.configuredResource;
		}
		if (typeof(this.paramData)!=='undefined' && this.paramData!=null && Object.keys(this.paramData).length>0) {
			params['passedParameters'] = encodeURIComponent(JSON.stringify(this.paramData));
		}
		params['XNAT_CSRF'] = window.csrfToken;
		if (XNAT.data.context.isSubject) {
			params['subject'] = XNAT.data.context.subjectID;
		}
		if (XNAT.data.context.isExperiment || XNAT.data.context.isImageAssessor || XNAT.data.context.isImageSession || XNAT.data.context.isSubjectAssessor) {
			params['experiment'] = XNAT.data.context.ID;
		}
		var eventHandler = $('#eventHandlerSelect').val();
		if (eventHandler != undefined && eventHandler != null && eventHandler.length>0) {
			params['eventHandler'] = eventHandler;
		}
		var escapeHtml = (typeof this.currentUploaderConfig !== 'undefined') ? this.currentUploaderConfig.escapeHtml : undefined;
		if (escapeHtml != undefined && escapeHtml != null) {
			params['escapeHtml'] = escapeHtml;
		}
		params['xsiType'] = XNAT.data.context.xsiType;
		var queryParams = "?import-handler=" + XNAT.app.abu.importHandler + "&" + $.param(params);

		var processAjax = $.ajax({
			type : "POST",
			url:serverRoot+"/REST/services/import" + queryParams +
					 (($("#extractRequestBox").length>0) ? (($("#extractRequestBox").is(':checked')) ? "&extract=true" : "&extract=false") : "") +
					 (($("#closeBox").length>0) ? ($("#closeBox").is(':checked')) ? "&sendemail=true" : 
					    (($("#emailBox").length>0) ? (($("#emailBox").is(':checked')) ? "&sendemail=true" : "&sendemail=false") : "") :
					    (($("#emailBox").length>0) ? (($("#emailBox").is(':checked')) ? "&sendemail=true" : "&sendemail=false") : "")) +
					 (($("#verboseBox").length>0) ? (($("#verboseBox").is(':checked')) ? "&verbose=true" : "&verbose=false") : "") +
					 (($("#updateBox").length>0) ? (($("#updateBox").is(':checked')) ? "&update=true" : "&update=false") : "") 
			,
			cache: false,
			async: true,
			/*
			data: JSON.stringify(this.paramData),
			contentType: "application/json; charset=utf-8",
			data: this.paramData,
			contentType: "application/x-www-form-urlencoded",
			encode: true,
			*/
			context: this,
			dataType: 'text'
		  });
		processAjax.done( function( data, textStatus, jqXHR ) {
			$(".abu-upload-list").css('display','none');
			$(".abu-upload-list").html('');
			try {
				$(".response_text").html(XNAT.app.abu.processReturnedText(data,true) + "<br><br>");
			} catch(e) {
				$(".response_text").html("<br><br><h3>Error processing output returned from the server.  Status unknown.</h3>");
			}
			$(".response_text").css('display','inline');
			abu._fileUploader.processingComplete();
			XNAT.app.abu.buildPath='';
			this.paramsToPass = undefined;
			this.paramData = undefined;
			$("#usageSelect").prop('disabled',false);
			//if (XNAT.app.abu.usageSelect=='Upload') {
			//	$("#resourceSelect").prop('disabled',false);
			//} else if (XNAT.app.abu.usageSelect=='Launch') { 
			//	$("#abu-process-button").prop("disabled",false);
			//}
		});
		processAjax.fail( function( data, textStatus, jqXHR ) {
			$(".abu-upload-list").css('display','none');
			$(".abu-upload-list").html('');
			try {
				$(".response_text").html("<h3>ERROR:  Processing not successful:</h3><h3>Server Response:  " + data.statusText +  " (StatusCode=" + data.status + ")</h3>  " + data.responseText);
			} catch(e) {
				$(".response_text").html("<br><br><h3>Error processing output returned from the server.  Status unknown.</h3>");
			}
			$(".response_text").css('display','inline');
			abu._fileUploader.processingComplete();
			XNAT.app.abu.buildPath='';
			this.paramsToPass = undefined;
			this.paramData = undefined;
			//if (XNAT.app.abu.usageSelect=='Upload') {
			//	$("#resourceSelect").prop('disabled',false);
			//} else if (XNAT.app.abu.usageSelect=='Launch') { 
			//	$("#abu-process-button").prop("disabled",false);
			//}
		});
		$("#xmodal-abu-done-button").show();
		setTimeout(function(){
			if (document.getElementById("closeBox")!=null && document.getElementById("closeBox").checked) {
				xmodal.message('Notice',"You will be sent an e-mail upon completion");
				xmodal.close(XNAT.app.abu.abuConfigs.modalOpts.id);
			}
		},500);

}
XNAT.app.abu.processReturnedText = function(ins,returnToBr){
	return (typeof ins !== "undefined" && ins != null) ?
		ins.toString().replace(/[\r\n]+/g,((returnToBr) ? "<br>" : '')).replace(/\/$/,'').replace(/<\/?script>/,"script") : "";
}

XNAT.app.abu.removeUploaderConfiguration=function(configEvent,scope) {
	var isFound = false;
	for (var i=0; i<XNAT.app.abu.uploaderConfig.length; i++) {
		var config = XNAT.app.abu.uploaderConfig[i];
		if (config.event == configEvent && config.eventScope == scope) {
			isFound = true;
			XNAT.app.abu.uploaderConfig.splice(i,1);
			break;
		}
	}
	if (isFound) {
		XNAT.app.abu.putUploaderConfiguration(scope,false);
	}
}

XNAT.app.abu.saveUploaderConfiguration=function(configTriggerId, configEvent, scope) {
	var newConfigObj = { eventTriggerId: configTriggerId, event: configEvent, eventScope: scope };
	newConfigObj.launchFromCacheUploads = $('#ULC_RB_launchFromCacheUploads').is(':checked');
	newConfigObj.launchFromResourceUploads = $('#ULC_RB_launchFromResourceUploads').is(':checked');
	newConfigObj.launchWithoutUploads = $('#ULC_RB_launchWithoutUploads').is(':checked');
	newConfigObj.doNotUseUploader = $('#ULC_RB_doNotUseUploader').is(':checked');
	newConfigObj.escapeHtml = $('#ULC_RB_outputText').is(':checked');

	newConfigObj.showExtractOption = $('#ULC_RB_showExtractOption').is(':checked');
	newConfigObj.extractOptionChecked = $('#ULC_RB_extractOptionChecked').is(':checked');
	newConfigObj.showCloseWindowOption = $('#ULC_RB_showCloseWindowOption').is(':checked');
	newConfigObj.closeWindowOptionChecked = $('#ULC_RB_closeWindowOptionChecked').is(':checked');
	newConfigObj.showEmailOption = $('#ULC_RB_showEmailOption').is(':checked');
	newConfigObj.emailOptionChecked = $('#ULC_RB_emailOptionChecked').is(':checked');

	newConfigObj.parameters = undefined;
	$(".ULC_parametersDiv").each(function() {
		var parameterField = $(this).find(".ULC_parametersField").val();
		var parameterDefault = $(this).find(".ULC_parametersDefault").val();
		var parameterDesc = $(this).find(".ULC_parametersDesc").val();
		if (typeof(parameterField)!=='undefined' && parameterField != null && parameterField.replace('/ /g','').length>0) {
			if (typeof(newConfigObj.parameters)==='undefined' || newConfigObj.parameters == null) {
				newConfigObj.parameters = [];
			}
			var newParam = {};
			newParam.name = parameterField.trim();
			newParam.defaultVal = parameterDefault.trim();
			newParam.description = parameterDesc.trim();
			newParam.type = $(this).find(".ULC_parametersType").val();
			newParam.required = $(this).find(".ULC_parametersRequired").is(':checked');
			newConfigObj.parameters.push(newParam);
		}
	});
	if ( ( $('#ULC_RB_launchFromResourceUploads').is(':checked') || $('#ULC_RB_launchFromCacheUploads').is(':checked') ||
			 $('#ULC_RB_launchWithoutUploads').is(':checked')
		 ) && !$('#ULC_contextsAllCB').is(':checked')) {
		$(".ULC_contextsContext").each(function() {
			var contextVal = $(this).val();
			if (!(typeof(contextVal)==='undefined' || contextVal == null || contextVal.replace(/ /g,'').length<1)) {
				if (typeof(newConfigObj.contexts)==='undefined' || newConfigObj.contexts == null) {
					newConfigObj.contexts = [];
				}
				newConfigObj.contexts.push(contextVal.trim());
			}
		}); 
	}
	if ( $('#ULC_RB_launchFromResourceUploads').is(':checked') && !$('#ULC_resourcesAllCB').is(':checked')) {
		var resourceOptions = $("#ULC_configuredResources option:selected");
		if (resourceOptions.length>0) {
			$(resourceOptions).each(function() {
				var thisVal = $(this).val();
				if (!(typeof(thisVal)==='undefined' || thisVal == null || thisVal.replace(/ /g,'').length<1)) {
					if (typeof(newConfigObj.resourceConfigs)==='undefined' || newConfigObj.resourceConfigs == null) {
						newConfigObj.resourceConfigs = [];
					}
					newConfigObj.resourceConfigs.push(thisVal.trim());
				}
			});
		}
	}
	var isUpdated = false;
	var isFound = false;
	for (var i=0; i<XNAT.app.abu.uploaderConfig.length; i++) {
		var config = XNAT.app.abu.uploaderConfig[i];
		if (config.eventTriggerId == newConfigObj.eventTriggerId && config.eventScope == newConfigObj.eventScope) {
			isFound = true;
			if (!(JSON.stringify(XNAT.app.abu.uploaderConfig[i]) == JSON.stringify(newConfigObj))) {;
				XNAT.app.abu.uploaderConfig[i] = newConfigObj;
				isUpdated = true;
			} 
		}
	}
	if (!isFound) {
		XNAT.app.abu.uploaderConfig.push(newConfigObj);
	}
	if (isFound && !isUpdated) { 
		xmodal.message('Nothing done',"The configuration has not changed.  Nothing done.");
	} else {
		XNAT.app.abu.putUploaderConfiguration(scope,true);
	}
}

XNAT.app.abu.putUploaderConfiguration=function(scope, notify) {

	var scopeArray = [];
	for (var i=0;i<XNAT.app.abu.uploaderConfig.length;i++) {
		if (XNAT.app.abu.uploaderConfig[i].eventScope==scope) {
			scopeArray.push(XNAT.app.abu.uploaderConfig[i]);
		}
	}
	var uploaderConfigAjax = $.ajax({
		type : "PUT",
		notify : notify,
 		url:serverRoot+'/data' + ((scope!='site') ? '/projects/' + XNAT.data.context.project : '') +'/config/automation_uploader/configuration?inbody=true&XNAT_CSRF=' + window.csrfToken,
		cache: false,
		async: false,
		data:  JSON.stringify(scopeArray),
		contentType: "application/text; charset=utf-8"
	 });
	uploaderConfigAjax.done( function( data, textStatus, jqXHR ) {
		if (notify) {
			xmodal.message('Saved','The uploader configuration has been saved');
		}
	});
	uploaderConfigAjax.fail( function( data, textStatus, error ) {
		if (notify) {
			xmodal.message('Error','ERROR:  Configuration was not successfully saved (' + textStatus + ')');
		}
	});
}

XNAT.app.abu.validateUploaderConfiguration=function() {
	var errorList = '';
	if ( ( $('#ULC_RB_launchFromResourceUploads').is(':checked') || $('#ULC_RB_launchFromCacheUploads').is(':checked') ||
			 $('#ULC_RB_launchWithoutUploads').is(':checked')
		 ) && !$('#ULC_contextsAllCB').is(':checked')) {
		var haveContext = false;
		var contextEles = $(".ULC_contextsContext").each(function() {
			var contextVal = $(this).val();
			if (!(typeof(contextVal)==='undefined' || contextVal==null || contextVal.replace(/ /g,'').length<1)) {
				haveContext=true;
				return false;
			}
		}); 
		if (!haveContext) {
			errorList+='<li>Usage indicates the uploader should be used with this handler, but no contexts have been specified</li>';
		}
	}
	if ( $('#ULC_RB_launchFromResourceUploads').is(':checked') && !$('#ULC_resourcesAllCB').is(':checked')) {
		if ($("#ULC_configuredResources option:selected").length<1) {
			errorList+='<li>Usage indicates the uploader should be used with configured resources, but no configured resources have been specified</li>';
		}
	}
	if (errorList.length>0) {
		xmodal.message('Error',"<h3>ERROR:  Invalid configuration</h3>" + errorList);
		return false;
	}
	return true;
}

XNAT.app.abu.configureUploaderForEventHandler=function(configTriggerId, configEvent, scope) {

	var uploaderConfig = XNAT.app.abu.uploaderConfig;
	if (typeof(uploaderConfig) === 'undefined' || uploaderConfig == null) {
		xmodal.message("Couldn't retrieve uploader configuration");
		return;
	}
	var configObj;
	for (var i=0;i<uploaderConfig.length;i++) {
		var objI = uploaderConfig[i];
		if (objI.eventScope == scope && objI.eventTriggerId == configTriggerId) {
			// Clone the object because we may modify it (fill in context values).
			configObj = jQuery.extend(true, {}, objI);
		}
	}
	if (typeof(configObj) === 'undefined' || configObj == null) {
		configObj = {};
		configObj.launchFromCacheUploads = false;
		configObj.launchFromResourceUploads = false;
		configObj.launchWithoutUploads = false;
		configObj.doNotUseUploader = true;
		configObj.escapeHtml = false;

		configObj.showExtractOption = true;
		configObj.extractOptionChecked = true;
		configObj.showCloseWindowOption = true;
		configObj.closeWindowOptionChecked = false;
		configObj.showEmailOption = true;
		configObj.emailOptionChecked = false;

		// best to leave these undefined, I think
		//configObj.parameters = [  ];
		//configObj.contexts = [ 'xnat:projectData','xnat:subjectAssessorData','xnat:imageAssessorData','xnat:imageSessionData','xnat:imageScanData','xnat:subjectData' ];
		//configObj.resourceConfigs = [  ];
	}
	var manageModalOpts = {
		width: 840,  
		height: 680,  
		id: 'xmodal-uploader-config',  
		title: "Configure Uploader for Event Handler",
		content: "<div id='configUploadDiv'></div>",
		buttons: {
			saveConfig: { 
				label: 'Save Configuration',
				close: false,
				action: function( obj ){
					if (XNAT.app.abu.validateUploaderConfiguration()) {
						$('#ULC_RB_emailOptionChecked').prop("disabled",false);
						$('#ULC_RB_showCloseWindowOption').prop("disabled",false);
						XNAT.app.abu.saveUploaderConfiguration(configTriggerId, configEvent, scope);
						obj.close();
					}
				}
			},
			close: { 
				label: 'Cancel',
				isDefault: false
			}
		}
	};
	xmodal.open(manageModalOpts);
	var configHtml = '<h3>Event Handler:  ' + configEvent + '</h3>';
	configHtml+='<p>';
	/** NOTE:  These radio buttons were originally coded as checkboxes, assuming the same scripts might be triggered from different upload/launch contexts. **
 	 **        It was later decided that each event handler should only be triggered from a single upload/launch context.  The code that uses these still   **
         **        treats them as check boxes in case we change our desired usage.                                                                              **/ 


	configHtml+='<div style="margin-left:20px;width:100%"><p><b>Usage:</b>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_launchFromCacheUploads" name="ULC_RB_USAGE" value="launchFromCacheUploads"' +
				 ((configObj.launchFromCacheUploads) ? ' checked' : '') + '> <b> Use for cache space uploads</b> </div>';
	if (scope!='site') {
		configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_launchFromResourceUploads" name="ULC_RB_USAGE" value="launchFromResourceUploads"' +
					 ((configObj.launchFromResourceUploads) ? ' checked' : '') + '> <b> Use for configured resource uploads </b> </div>';
	}
	configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_launchWithoutUploads" name="ULC_RB_USAGE" value="launchWithoutUploads"' +
				 ((configObj.launchWithoutUploads) ? ' checked' : '') + '> <b> Trigger without uploads </b> </div>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_doNotUseUploader" name="ULC_RB_USAGE" value="doNotUseUploader"' +
				 ((configObj.doNotUseUploader) ? ' checked' : '') + '> <b> Do not use uploader </b> </div>';
	configHtml+='</div></p>';
	configHtml+='<p>';
	configHtml+='<div style="margin-left:20px;width:100%"><p><b>Output Type:</b>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_outputHtml" name="ULC_RB_OUTPUT" value="outputHtml"' +
				 ((configObj.escapeHtml) ? '' : 'checked') + '> <b> HTML </b> </div>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="radio" id="ULC_RB_outputText" name="ULC_RB_OUTPUT" value="outputText"' +
				 ((configObj.escapeHtml) ? ' checked' : '') + '> <b> Text (escape HTML characters) </b> </div>';
	configHtml+='</div></p>';

	configHtml+='<p>';
	configHtml+='<div style="margin-left:20px;width:100%"><p><b>Upload window options:</b>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="checkbox" id="ULC_RB_showExtractOption" name="ULC_RB_SHOWEXTRACT" value="showExtractOption"' +
				 ((configObj.showExtractOption) ? ' checked' : '') + '> <b> Show <i>extract compressed files</i> option?</b> </div>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="checkbox" id="ULC_RB_extractOptionChecked" name="ULC_RB_EXTRACTCHECKED" value="extractOptionChecked"' +
				 ((configObj.extractOptionChecked) ? ' checked' : '') + '> <b> <i>Extract compressed files</i> option checked?</b> </div>';


	configHtml+='<div style="margin-left:20px;width:100%;margin-top:10px;"><input type="checkbox" id="ULC_RB_showCloseWindowOption" name="ULC_RB_SHOWCLOSE" value="showCloseWindowOption"' +
				 ((configObj.showCloseWindowOption) ? ' checked' : '') + '> <b> Show <i>close window upon submit</i> option?</b> </div>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="checkbox" id="ULC_RB_closeWindowOptionChecked" name="ULC_RB_CLOSECHECKED" value="closeWindowOptionChecked"' +
				 ((configObj.closeWindowOptionChecked) ? ' checked' : '') + '> <b> <i>Close window upon submit</i> option checked?</b> </div>';


	configHtml+='<div style="margin-left:20px;width:100%;margin-top:10px;"><input type="checkbox" id="ULC_RB_showEmailOption" name="ULC_RB_SHOWEMAIL" value="showEmailOption"' +
				 ((configObj.showEmailOption) ? ' checked' : '') + '> <b> Show <i>send e-mail upon completion</i> option?</b> </div>';
	configHtml+='<div style="margin-left:20px;width:100%"><input type="checkbox" id="ULC_RB_emailOptionChecked" name="ULC_RB_EMAILCHECKED" value="emailOptionChecked"' +
				 ((configObj.emailOptionChecked) ? ' checked' : '') + '> <b> <i>Send e-mail upon completion</i> option checked?</b> </div>';
	configHtml+='<p>';

	configHtml+='<div style="margin-left:20px;width:100%"><p><b>User Supplied Parameters:</b><p><div id="ULC_parameters">';
	for (var i=0;i<((typeof(configObj.parameters)!=='undefined' && configObj.parameters.length>0) ? configObj.parameters.length : 0);i++) {
		var hasValue = (typeof(configObj.parameters)!=='undefined' && configObj.parameters.length>=(i+1));
		var fieldValue = (hasValue && typeof(configObj.parameters[i].name)!==undefined) ? configObj.parameters[i].name : '';
		var stringSelected = (hasValue && typeof(configObj.parameters[i].type)!==undefined && configObj.parameters[i].type=='String') ? 'selected' : '';
		var integerSelected = (hasValue && typeof(configObj.parameters[i].type)!==undefined && configObj.parameters[i].type=='Integer') ? 'selected' : '';
		var floatSelected = (hasValue && typeof(configObj.parameters[i].type)!==undefined && configObj.parameters[i].type=='Float') ? 'selected' : '';
		var fieldRequired = (hasValue && typeof(configObj.parameters[i].required)!==undefined && configObj.parameters[i].required==false) ? '' : 'checked';
		var fieldDefault = (hasValue && typeof(configObj.parameters[i].defaultVal)!==undefined) ? configObj.parameters[i].defaultVal : '';
		var fieldDesc = (hasValue && typeof(configObj.parameters[i].description)!==undefined) ? configObj.parameters[i].description : '';
		configHtml+='<div id="ULC_parametersDiv' + i + '" class="ULC_parametersDiv" style="margin-left:20px;margin-bottom:5px;width:100%">' +
			'<div style="float:left"><div class="abu-config-param-grp"><div class="abu-config-param-row">' +
			'<div class="abu-config-param-div">Name:</div><input type="text" size="20"  id="ULC_parametersField' + i + '" class="ULC_parametersField" value="' + fieldValue + '" > ' +
			'<select style="margin-left:5px" id="ULC_parametersType' + i  + '" class="ULC_parametersType">' +
			'<option value="String" ' + stringSelected + '>String</option>' +
				'<option value="Integer" ' + integerSelected + '>Integer</option>' + 
				'<option value="Float" ' + floatSelected + '>Float</option>' + 
				'</select>  ' +
			'<input type="checkbox" style="margin-left:5px" id="ULC_parametersRequired"' + i + '" ' + fieldRequired + ' class="ULC_parametersRequired"> Required? </div>' +
			'<div class="abu-config-param-row"><div class="abu-config-param-div">Default Value:</div><input type="text" size="20"  id="ULC_parametersDefault' + i + '"  class="ULC_parametersDefault" value="' + fieldDefault + '"></div>' +
			'<div class="abu-config-param-row"><div class="abu-config-param-div">Description Text:</div><input type="text" size="50"  id="ULC_parametersDesc' + i + '"  class="ULC_parametersDesc" value="' + fieldDesc + '"></div></div>' +
			'<button style="margin-left:10px" id="ULC_parametersRemove' + i + '" class="parametersRemove">Remove</button></div>' +
			'</div>';
	} 
	configHtml+='</div>';
	configHtml+='<span style="margin-left:20px"><button class="parametersAdd">Add Parameter</button></span>';
	configHtml+='</div></p>';

	var configuredResources=[];
	var allChecked = (typeof(configObj.resourceConfigs) === 'undefined') ? 'checked' : '';
	if (typeof(XNAT.app.abu.allResourceConfigs)!=='undefined') {
		configuredResources=XNAT.app.abu.allResourceConfigs;
	}
	var selectSize = (configuredResources.length<4) ? 4 : ((configuredResources.length>10) ? 10 : configuredResources.length);
	configHtml+='<p>';
	if (scope!='site') {
		configHtml+='<div style="margin-left:20px;width:100%"><p><b>Configured resources for which this handler is applicable:</b> ' + 
					'<span style="margin-left:10px"><input type="checkbox" id="ULC_resourcesAllCB" ' + allChecked + 
						((configObj.launchFromResourceUploads) ? '' : ' disabled="disabled"') + '>Applicable for all configured resources.</span>';
		if (configuredResources.length<1) {
			configHtml+='<div style="margin-left:20px;width:100%"><p><b>NONE DEFINED</b></p></div>';
		} else  {
			configHtml+='<div style="margin-left:20px;width:100%"><p>' + 
			'<div style="width:100%;float:left;margin-bottom:10px;"><div style="width:auto;float:left;"> <select id="ULC_configuredResources" size=' + selectSize + ' style="max-width:500px;width:200px;" multiple ' + 
				((allChecked || !(configObj.launchFromResourceUploads)) ? 'disabled="disabled"' : '')  + '>';
			for (var i=0;i<configuredResources.length;i++) {
				configHtml+='<option value="' + configuredResources[i].name + '" ' + 
								((typeof(configObj.resourceConfigs)!=='undefined' && $.inArray(configuredResources[i].name,configObj.resourceConfigs)>=0) ? 'selected' : '') +
					 '>' + configuredResources[i].name + '</option>';
			} 
			configHtml+='</select></div><span style="margin-left:10px">NOTE:  Multiple resources may be selected</span></div>';
			configHtml+='</p></div>';
		}
		configHtml+='</div></p>';
	
		configHtml+='<p>';
	}
	var allContextsChecked = '';
	if (typeof(configObj.contexts)==='undefined') {
		allContextsChecked = 'checked';
		configObj.contexts = [ 'xnat:projectData','xnat:subjectAssessorData','xnat:imageAssessorData','xnat:imageSessionData','xnat:imageScanData','xnat:subjectData' ];
	}
	configHtml+='<div style="margin-left:20px;width:100%"><p><b>Context(s) for launch/upload:  <span style="margin-left:10px">' + 
					'<input type="checkbox" id="ULC_contextsAllCB" ' + allContextsChecked + 
					((configObj.launchFromResourceUploads || configObj.launchFromCacheUploads || configObj.launchWithoutUploads) ? '' : ' disabled="disabled"') +
					 '>Applicable across all contexts?</span></b><p><div id="ULC_contexts">';
	for (var i=0;i<((typeof(configObj.contexts)!=='undefined' && configObj.contexts.length>0) ? configObj.contexts.length : 0);i++) {
		var hasValue = (typeof(configObj.contexts)!=='undefined' && configObj.contexts.length>=(i+1));
		var contextValue = (hasValue && typeof(configObj.contexts[i])!==undefined) ? configObj.contexts[i] : '';
		configHtml+='<div id="ULC_contextsDiv' + i + '" class="ULC_contextsDiv" style="margin-left:20px;margin-bottom:5px;width:100%">' + 
					'<input type="text" size="30"  id="ULC_contextsContext' + i + '"  class="ULC_contextsContext" value="' + contextValue + '"> ' + 
					' <button id="ULC_contextsRemove' + i + '" class="contextsRemove">Remove</button></div>';
	} 
	configHtml+='</div><span style="margin-left:20px"><button class="ULC_contextsAdd">Add Context</button></span></div></p>';

	$('#configUploadDiv').html(configHtml); 
	XNAT.app.abu.updateEmailOptionStatus();
	$(".ULC_contextsDiv :input,.ULC_contextsAdd").prop('disabled',$('#ULC_contextsAllCB').is(':checked') || !( $('#ULC_RB_launchFromResourceUploads').is(':checked') || 
		$('#ULC_RB_launchFromCacheUploads').is(':checked') || $('#ULC_RB_launchWithoutUploads').is(':checked')) );

	$('.parametersRemove').click(function(){ 
			$("#" + this.id.replace("ULC_parametersRemove","ULC_parametersDiv")).remove(); 
	 });
	$('.parametersAdd').click(function(){ 
			ULC_addParameter();
	 });
	$('.contextsRemove').click(function(){ 
			$("#" + this.id.replace("ULC_contextsRemove","ULC_contextsDiv")).remove(); 
	 });
	$('.ULC_contextsAdd').click(function(){ 
			ULC_addContext();
	 });
	$('#ULC_resourcesAllCB').change(function(){ 
		$("#ULC_configuredResources").prop('disabled',$('#ULC_resourcesAllCB').is(':checked'));
	 });
	$('#ULC_contextsAllCB').change(function(){ 
		$(".ULC_contextsDiv :input,.ULC_contextsAdd").prop('disabled',$('#ULC_contextsAllCB').is(':checked'));
	 });
	$('#ULC_RB_launchFromResourceUploads').change(function(){ 
		$("#ULC_resourcesAllCB").prop('disabled',!$('#ULC_RB_launchFromResourceUploads').is(':checked'));
		$("#ULC_configuredResources").prop('disabled',$('#ULC_resourcesAllCB').is(':checked') || !$('#ULC_RB_launchFromResourceUploads').is(':checked'));
		$("#ULC_contextsAllCB").prop('disabled',!( $('#ULC_RB_launchFromResourceUploads').is(':checked') || $('#ULC_RB_launchFromCacheUploads').is(':checked') ||
			 $('#ULC_RB_launchWithoutUploads').is(':checked')) );
		$(".ULC_contextsDiv :input,.ULC_contextsAdd").prop('disabled',$('#ULC_contextsAllCB').is(':checked') || !( $('#ULC_RB_launchFromResourceUploads').is(':checked') || 
			$('#ULC_RB_launchFromCacheUploads').is(':checked') || $('#ULC_RB_launchWithoutUploads').is(':checked')) );
	 });
	$('#ULC_RB_launchFromCacheUploads').change(function(){ 
		$("#ULC_contextsAllCB").prop('disabled',!( $('#ULC_RB_launchFromResourceUploads').is(':checked') || $('#ULC_RB_launchFromCacheUploads').is(':checked') ||
			 $('#ULC_RB_launchWithoutUploads').is(':checked')) );
		$(".ULC_contextsDiv :input,.ULC_contextsAdd").prop('disabled',$('#ULC_contextsAllCB').is(':checked') || !( $('#ULC_RB_launchFromResourceUploads').is(':checked') || 
			$('#ULC_RB_launchFromCacheUploads').is(':checked') || $('#ULC_RB_launchWithoutUploads').is(':checked')) );
	 });
	$('#ULC_RB_launchWithoutUploads').change(function(){ 
		$("#ULC_contextsAllCB").prop('disabled',!( $('#ULC_RB_launchFromResourceUploads').is(':checked') || $('#ULC_RB_launchFromCacheUploads').is(':checked') ||
			 $('#ULC_RB_launchWithoutUploads').is(':checked')) );
		$(".ULC_contextsDiv :input,.ULC_contextsAdd").prop('disabled',$('#ULC_contextsAllCB').is(':checked') || !( $('#ULC_RB_launchFromResourceUploads').is(':checked') || 
			$('#ULC_RB_launchFromCacheUploads').is(':checked') || $('#ULC_RB_launchWithoutUploads').is(':checked')) );
	 });
	$('#ULC_RB_closeWindowOptionChecked').change(function() {
		XNAT.app.abu.updateEmailOptionStatus()
		});
	$('#ULC_RB_showEmailOption').change(function() {
		XNAT.app.abu.updateEmailOptionStatus()
		});

	var ULC_addParameter = function(){

		var divs = $(".ULC_parametersDiv");
		var fields = $(".ULC_parametersField");
		var defaultVal = $(".ULC_parametersDefault");
		var desc = $(".ULC_parametersDesc");
		for (var i=0;i<fields.length;i++) {
			var val = $(fields.get(i)).val();
			if (typeof(val)==='undefined' || val == null || val.length<1) {
				$('#ULC_parametersField' + i).focus();
				return;
			}
		}
		var len = (divs.length>0) ? Number(($(divs).last().get(0)).id.replace("ULC_parametersDiv",""))+1 : 1;
		$("#ULC_parameters").append(
				'<div id="ULC_parametersDiv' + len + '" class="ULC_parametersDiv" style="margin-left:20px;margin-bottom:5px;width:100%">' +
				'<div style="float:left"><div class="abu-config-param-grp"><div class="abu-config-param-row"><div class="abu-config-param-div">Name:</div><input type="text" size="20"  id="ULC_parametersField' + i + '" class="ULC_parametersField"> ' +
				'<select style="margin-left:5px" id="ULC_parametersType' + len  + '" class="ULC_parametersType">' +
				'<option value="String">String</option>' + 
				'<option value="Integer">Integer</option>' + 
				'<option value="Float">Float</option>' + 
				'</select>' +
				'<input type="checkbox" style="margin-left:5px" id="ULC_parametersRequired"' + len + '" class="ULC_parametersRequired" checked> Required? </div>' +
				'<div class="abu-config-param-row"><div class="abu-config-param-div">Default Value:</div><input type="text" size="20"  id="ULC_parametersDefault' + i + '"  class="ULC_parametersDefault"></div>' +
				'<div class="abu-config-param-row"><div class="abu-config-param-div">Description Text:</div><input type="text" size="50"  id="ULC_parametersDesc' + i + '"  class="ULC_parametersDesc"></div></div>' +
				'<button style="margin-left:10px" id="ULC_parametersRemove' + len + '" class="parametersRemove">Remove</button></div>' +
				'</div>'

		);
		$('#ULC_parametersRemove' + len).click(function(){ 
			$("#ULC_parametersDiv" + len).remove(); 
		});
		$('#ULC_parametersField' + i).focus();

	}

	var ULC_addContext = function(){

		var divs = $(".ULC_contextsDiv");
		var contexts = $(".ULC_contextsContext");
		for (var i=0;i<contexts.length;i++) {
			var val = $(contexts.get(i)).val();
			if (typeof(val)==='undefined' || val == null || val.length<1) {
				$('#ULC_contextsContext' + i).focus();
				return;
			}
		}
		var len = (divs.length>0) ? Number(($(divs).last().get(0)).id.replace("ULC_contextsDiv",""))+1 : 1;
		$("#ULC_contexts").append(
				'<div id="ULC_contextsDiv' + len + '" class="ULC_contextsDiv" style="margin-left:20px;margin-bottom:5px;width:100%">' + 
				'<input type="text" size="30"  id="ULC_contextsContext' + i + '" class="ULC_contextsContext">' + 
				' <button id="ULC_contextsRemove' + len + '" class="contextsRemove">Remove</button></div>'
		);
		$('#ULC_contextsRemove' + len).click(function(){ 
			$("#ULC_contextsDiv" + len).remove(); 
		});
		$('#ULC_contextsContext' + i).focus();

	}
}

XNAT.app.abu.updateEmailOptionStatus=function() {
	if ($('#ULC_RB_closeWindowOptionChecked').is(":checked")) {
		$('#ULC_RB_emailOptionChecked').prop("checked",true);
		$('#ULC_RB_emailOptionChecked').prop("disabled",true);
		if ($('#ULC_RB_showEmailOption').is(":checked")) {
			$('#ULC_RB_showCloseWindowOption').prop("checked",true);
			$('#ULC_RB_showCloseWindowOption').prop("disabled",true);
		} else {
			$('#ULC_RB_showCloseWindowOption').prop("disabled",false);
		}
	} else {
		$('#ULC_RB_emailOptionChecked').prop("disabled",false);
		$('#ULC_RB_showCloseWindowOption').prop("disabled",false);
	}
}

$(document).ready(XNAT.app.abu.abuConfigs.load);

