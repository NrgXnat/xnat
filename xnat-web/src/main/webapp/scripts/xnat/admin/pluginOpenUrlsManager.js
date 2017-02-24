/*
 * web: pluginOpenUrlsManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

$(function(){

	console.log('pluginOpenUrlsManager.js');

	$.ajax({
		type : "GET",
		url:serverRoot + "/xapi/pluginOpenUrls/settings",
		cache: false,
		async: true,
		context: this,
		dataType: 'text'
	})
	.done(function(data, textStatus,jqXHR) {

		var rObj = JSON.parse(data);
		populateDisplay(rObj,!(typeof rObj === 'undefined' || Object.keys(rObj).length<1));
	})
	.fail(function(data, textStatus,jqXHR) {
		console.log("WARNING:  Could not retrieve plugin openurls configuration status");
		populateDisplay(undefined, false);
	})

	var populateDisplay = function(settingsObj, anyDefined) {

		var urlPanel = {
			urlPanel : {
				id:  "openurls-panel",
				kind:  "panel.form",
				label: "Open URLs Defined in Plugins",
				method: "POST",
				contentType: "json",
				url: "/xapi/pluginOpenUrls/settings",
				contents: {
					message: { tag: "div.message.bold",
						   content: "NOTE:  Open URLs defined in plugins must be enabled by an administrator before they are available in the" + 
								" application.  A Tomcat restart is required for changes in the open URL configuration to take effect." },
				}
			}
		};	
		if (anyDefined) {
			urlPanel.urlPanel.contents["subHead"] = { kind: "panel.subhead", name:  "openurls-subhead", label: "Plugin Open URLS" };
			for (var prop in settingsObj) {
				if (settingsObj.hasOwnProperty(prop)) {
					 urlPanel.urlPanel.contents[prop] = { 
							kind:  "panel.input.switchbox",
							id:  prop,
							label:  prop,
							name:  prop,
							title:  prop,
							value:  true,
							onText:  "Authorized",
							offText:  "Not Authorized"
		        		};
				}
			}
		} else {
			urlPanel.urlPanel.contents["subHead"] = {
				 tag: "div",
				 content: "<b>No open URLs have been defined in current plugins.</b>",
				 element: { style: "margin-top: 20px" } 
			};
			urlPanel.urlPanel["footer"] = false;
		}
		XNAT.spawner.spawn(urlPanel).render($("#plugin-open-urls-config-panel"));
		$("#plugin-open-urls-config-panel").css("border","none");
		$("#plugin-open-urls-config-panel").children(".panel-body").css("display","none");

	}

});

