/*
 * web: xnatTaskSettings.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * XNAT Task site settings functions
 */

var XNAT = getObject(XNAT);

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function(){

    var pluginSettings;

    XNAT.admin =
        getObject(XNAT.admin);

    XNAT.admin.xnatTaskSettings = xnatTaskSettings =
        getObject(XNAT.admin.xnatTaskSettings);

    xnatTaskSettings.checkDisplay = function() {

	$.ajax({
		type : "GET",
		url:serverRoot + "/xapi/xnatTask/checkNodeConfigurationStatus",
		cache: false,
		async: true,
		context: this,
		dataType: 'text'
	})
	.done(function(data, textStatus,jqXHR) {

		var rObj = JSON.parse(data);
		if (!(rObj.isConfigured !== 'undefined' && rObj.isConfigured.toString().toLowerCase() == "true")) {
			$("#view-task-settings").hide();
		}

	})
	.fail(function(data, textStatus,jqXHR) {
		console.log("WARNING:  Could not retrieve node/task configuration status");
		$("#view-task-settings").hide();
	})

    }

    xnatTaskSettings.populateDisplay = function() {

        $('#task-settings-tabs').html("<h2>XNAT Task Settings</h2>");
	$.ajax({
		type : "GET",
		url:serverRoot + "/xapi/xnatTask/taskList",
		cache: false,
		async: true,
		context: this,
		dataType: 'text'
	})
	.done(function(data, textStatus,jqXHR) {

		var rObj = JSON.parse(data);
		if (!($.isArray(rObj))) {
			$("#task-settings-tabs").html("<h3>ERROR:  Could not retrieve configuration</h3>")
		}
		var taskTabs = { 
		   root: {
			id: "task-tabs",
			name: "task-tabs",
			kind: 'tabs',
			container: 'div.task-settings-tabs',
			label: "XNAT Task Settings",
			contents: {
			}
		   }
		};
		for (var i=0; i<rObj.length; i++) {
			var taskInfo = rObj[i];
			var taskId = taskInfo.taskId;
			var taskDesc = taskInfo.description;
			var taskConfigStr = taskInfo.configurationElementsYaml;
			var taskConfig = JSON.parse(taskConfigStr);
			console.log(taskConfig);
			if (typeof taskId !== undefined) {
				var taskPanel = {
                                    taskPanel : {
					id: taskId + "-panel",
					kind: 'panel.form',
					label: taskDesc,
					header: true,
					method: "POST",
		    			contentType: "json",
		    			url: "/xapi/siteConfig",
					contents: {
					}
				    }
				};
				var taskTab = {
                                   taskTab:{
					kind: 'tab',
					name: taskId + ' tab',
					label: taskId + ' tab',
					active: true,
					contents: {
						"task-panel" : taskPanel
					}
				   }
				};
				if ($.isArray(taskConfig)) {
					for (var i=0; i<taskConfig.length; i++) {
						try {
							var parsedConfig = YAML.parse(taskConfig[i]);
							if (typeof parsedConfig.kind !== 'undefined' && typeof parsedConfig.name !== 'undefined') {
								taskPanel.taskPanel.contents["task-input" + i] = parsedConfig;
							} else {
								for (var prop in parsedConfig) {
									if (parsedConfig.hasOwnProperty(prop)) {
										if (typeof parsedConfig[prop].kind !== 'undefined' && typeof parsedConfig[prop].name !== 'undefined') {
											taskPanel.taskPanel.contents[prop] = parsedConfig[prop];
										}
									}
								}
							}
						} catch(e) {
							console.log("ERROR:  unable to parse configuration", taskConfig[i]);
						}
					}
				}
				taskTabs.root.contents[taskId + "-contents"]=taskTab;
				XNAT.spawner.spawn(taskPanel).render($("#task-settings-tabs"));
				$(document).ready(function() {
					$("[id^=task-][id$=-resolver]").change(function(e, f) {
						if ($(e.currentTarget).hasClass("ready")) {
							xmodal.confirm({
				       	         		title: 'Save and Reload Page?',
				       	         		content: 'You have changed the execution resolver value.  This resolver may have different resolver properties.  Okay to save ' +
									'this setting and reload page now to display the settings for this resolver?',
				       	         		okClose: true,
				       	         		okAction: function(obj){
									$(e.currentTarget).closest(".xnat-form-panel").find(".save.submit").click();
									window.location.reload();
				       	         		}.bind(e)
							});
						}
					});
				});
				//XNAT.spawner.spawn(taskTabs).render($("#task-settings-tabs"));
			}
		}
	})
	.fail(function(data, textStatus,jqXHR) {
		$("#task-settings-tabs").html("<h3>ERROR:  Could not retrieve configuration</h3>");
	})
    }

    XNAT.admin.xnatTaskSettings = xnatTaskSettings;

}));
