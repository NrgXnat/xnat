/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/subject/SubjectLabelEditor.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/30/14 11:48 AM
 */

/*jslint white: true, vars: true, browser: true */

XNAT.app.SubjectLabelEditor = function(project) {"use strict";

	var that = this;
	this.labelInput = null;
	this.startingLabel = null;
	this.modifiedLabel = null;
	this.header = XNAT.app.displayNames.singular.subject;
	this.panel = null;
	this.onModification = new YAHOO.util.CustomEvent("labelModification", this);
	this.existingSubjectList = null;
	this.onExistingSubjectLoadComplete = new YAHOO.util.CustomEvent("load-complete", this);

	this.render = function() {
		if (!this.panel) {
			var bd = document.createElement("form");
			bd.setAttribute("onSubmit", "return(false);");
			bd.setAttribute("action", "");
			bd.setAttribute("method", "");
			var table = document.createElement("table");
			var tb = document.createElement("tbody");
			table.appendChild(tb);
			bd.appendChild(table);

			this.labelInput = document.createElement("input");
			this.labelInput.id = "new_label";
			this.labelInput.name = "new_label";

			var labelContainer = document.createElement("div");
			labelContainer.id = "complete_container";
			labelContainer.width = "100px";
			labelContainer.appendChild(this.labelInput);

			//modality
			var tr = document.createElement("tr");
			var td1 = document.createElement("th");
			var td2 = document.createElement("td");
			var td3 = document.createElement("td");

			td1.innerHTML = this.header + ":";
			td1.align = "left";

			td2.appendChild(labelContainer);

			tr.appendChild(td1);
			tr.appendChild(td2);
			tb.appendChild(tr);

			this.panel = new YAHOO.widget.Dialog("subjectLabelDialog", {
				close : true,
				//width:"350px",
				//zIndex:9,
				underlay : "shadow",
				modal : true,
				fixedCenter : true,
				visible : false
			});
			this.panel.handleEnter = function() {
				var label = this.form.new_label;
				that.modifiedLabel = label.value.trim();
				if (that.modifiedLabel === "") {
                    xModalMessage('Subject Label Validation', 'Please specify a new ID.');
					label.focus();
				} else if (that.modifiedLabel === that.startingLabel) {
                    xModalMessage('Subject Label Validation', 'No modification found.');
					label.focus();
				} else {
					var validatedLabel = that.cleanLabel(that.modifiedLabel);
					if (validatedLabel !== that.modifiedLabel) {
						label.value = validatedLabel;
                        xModalMessage('Subject Label Validation', 'Invalid characters in new ID.  Review modified value and resubmit.');
						label.focus();
						return;
					}

					var lC;
					for (lC = 0; lC < that.existingSubjectList.length; lC = lC + 1) {
						if (that.modifiedLabel === that.existingSubjectList[lC].label) {
                            xModalMessage('Subject Label Validation', 'This ID is already in use by a ' + that.header + ' in this ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.<br/><br/>Please modify and resubmit.');
							label.focus();
							return;
						}
					}

					if (confirm("Modifying the label of a " + that.header + " will result in the moving of files on the file server within the " + XNAT.app.displayNames.singular.project.toLowerCase() + "'s storage space.  Are you sure you want to make this change?")) {
						that.modifyLabel();
					}
				}
			};
			this.panel.handleCancel = function() {
				this.cancel();
			};

			var buttons = [{
				text : "Modify",
				handler : {
					fn : this.panel.handleEnter
				},
				isDefault : true
			}, {
				text : "Cancel",
				handler : {
					fn : this.panel.handleCancel
				}
			}];

			var cancelListener = new YAHOO.util.KeyListener(document, {
				keys : 27
			}, {
				fn : this.panel.handleCancel,
				scope : this.panel,
				correctScope : true
			});
			var enterListener = new YAHOO.util.KeyListener(document, {
				keys : 13
			}, {
				fn : this.panel.handleEnter,
				scope : this.panel,
				correctScope : true
			});

			var warn = document.createElement("p");
			warn.innerHTML = "<strong>Warning</strong>: If this project's anonymization script is enabled,<br>this operation could take a long time.";
			warn.appendChild(bd);
			
			this.panel.setHeader("Label modification");
			this.panel.setBody(warn);
			this.panel.form = bd;
			this.panel.selector = this;
			this.panel.cfg.queueProperty("keyListeners", [cancelListener, enterListener]);
			this.panel.cfg.queueProperty("buttons", buttons);
			this.panel.render("page_body");
		}
	};

	this.loadSubjects = function() {

		function displayError(errorMsg) {
			xModalMessage('Error', errorMsg);
		}

		function initFailure(o) {
			if (!window.leaving) {
				displayError("ERROR " + o.status + ": Failed to load " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
			}
		}

		function completeInit(o) {
			try {
				that.existingSubjectList = YAHOO.lang.JSON.parse(o.responseText).ResultSet.Result;
				that.onExistingSubjectLoadComplete.fire();
			} catch (e) {
				if (o.status !== 200) {
					displayError("ERROR " + o.status + ": Failed to parse " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
				} else {
					displayError("EXCEPTION: " + e.toString());
				}
			}

		}

		var initCallback = {
			success : completeInit,
			failure : initFailure,
			cache : false, // Turn off caching for IE
			scope : this
		};

		openModalPanel("labels_loading", "Loading " + this.header + "s...");
		YAHOO.util.Connect.asyncRequest('GET', window.serverRoot + '/REST/projects/' + project + '/subjects?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime(), initCallback, null, this);
	};

	this.onExistingSubjectLoadComplete.subscribe(function() {
		closeModalPanel("labels_loading");
		that.panel.show();
	});

	this.show = function(currentLabel) {
		this.labelInput.value = currentLabel;
		this.startingLabel = currentLabel;
		this.loadSubjects();
	};

	this.cleanLabel = function(val) {
		var temp = val.replace(/^\s*|\s*$/g, "");
		var newVal = "";
		temp = temp.split(' ');
		var c;
		for ( c = 0; c < temp.length; c = c + 1) {
			newVal += temp[c];
		}

		newVal = newVal.replace(/[&]/, "_");
		newVal = newVal.replace(/[?]/, "_");
		newVal = newVal.replace(/[<]/, "_");
		newVal = newVal.replace(/[>]/, "_");
		newVal = newVal.replace(/[(]/, "_");
		newVal = newVal.replace(/[)]/, "_");
		newVal = newVal.replace(/[#]/, "_");
		newVal = newVal.replace(/[%]/, "_");
		newVal = newVal.replace(/[=]/, "_");
		newVal = newVal.replace(/[{]/, "_");
		newVal = newVal.replace(/[}]/, "_");
		newVal = newVal.replace(/[|]/, "_");
		newVal = newVal.replace(/[,]/, "_");
		newVal = newVal.replace(/[`]/, "_");
		newVal = newVal.replace(/[~]/, "_");
		newVal = newVal.replace(/[;]/, "_");
		newVal = newVal.replace(/[:]/, "_");
		return newVal;
	};

	this.modifyLabel = function() {
		var settingsCallback = {
			success : function(o) {
				closeModalPanel("modify_new_label");
				that.onModification.fire(that.modifiedLabel);
				that.panel.cancel();
			},
			failure : function(o) {
				if (!window.leaving) {
                    xModalMessage('ERROR (' + o.status + '): Failed to modify ' + that.header + ' ID', o.responseText);
					closeModalPanel("modify_new_label");
				}
			},
			scope : this
		};

		var params = "";
		params += "&event_type=WEB_FORM";
		params += "&event_action=Modified label";

		openModalPanel("modify_new_label", "Modifying " + this.header + ", please wait...");
		var uri = window.serverRoot + "/REST/projects/" + project +"/subjects/" + this.startingLabel;
		YAHOO.util.Connect.asyncRequest('PUT', uri + "?label=" + this.modifiedLabel + "&XNAT_CSRF=" + window.csrfToken + params, settingsCallback);
	};
};
