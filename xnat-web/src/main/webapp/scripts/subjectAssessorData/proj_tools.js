/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/subjectAssessorData/proj_tools.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function ProjectLoader(_options) {
    this.options = _options;

    if (this.options == undefined) {
        this.options = new Object();
        this.options.owner = true;
        this.options.member = true;
    }

    this.onLoadComplete = new YAHOO.util.CustomEvent("load-complete", this);

    this.init = function () {

        //load from search xml from server
        this.initCallback = {
            success:this.completeInit,
            failure:this.initFailure,
            cache:false, // Turn off caching for IE
            scope:this
        }

        var params = "";

        if (this.options.recent != undefined) {
            params += "&recent=true";
        }

        if (this.options.owner != undefined) {
            params += "&owner=true";
        }

        if (this.options.member != undefined) {
            params += "&member=true";
        }

        if (this.options.collaborator != undefined) {
            params += "&collaborator=true";
        }

        params += "&prearc_code=true";

        YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/REST/projects?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime() + params, this.initCallback, null, this);
    };

    this.initFailure = function (o) {
        if (!window.leaving) {
            this.displayError("ERROR " + o.status + ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
        }
    };

    this.completeInit = function (o) {
        try {
            this.list = eval("(" + o.responseText + ")").ResultSet.Result;

            if (this.options.selects != undefined) {
                for (var selectC = 0; selectC < this.options.selects.length; selectC++) {
                    var selectBox = this.options.selects[selectC];
                    if (this.options.defaultValue != undefined) {
                        renderProjects(selectBox, this.list, this.options.defaultValue);
                    } else {
                        renderProjects(selectBox, this.list, "");
                    }
                }
            }

            this.onLoadComplete.fire();
        } catch (e) {
            this.displayError("ERROR " + o.status + ": Failed to parse " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
        }

    };

    this.displayError = function (errorMsg) {
        xModalMessage('Error', errorMsg);
    }
}

function sortByLabel(a,b)
{
	var aName = a.label.toLowerCase();
	var bName = b.label.toLowerCase();
	if (aName < bName){
        return -1;
     }else if (aName > bName){
       return  1;
     }else{
       return 0;
     }
}

function SubjectLoader(_options) {
    this.onLoadComplete = new YAHOO.util.CustomEvent("load-complete", this);
    this.options = _options;

    this.load = function (_project, _options) {
        if (_project != undefined)
            this.project = _project;

        if (_options != undefined)
            this.options = _options;

        //load from search xml from server
        this.initCallback = {
            success:this.completeInit,
            failure:this.initFailure,
            cache:false, // Turn off caching for IE
            scope:this
        }

        YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/REST/projects/' + _project + '/subjects?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime(), this.initCallback, null, this);
    };

    this.initFailure = function (o) {
        if (!window.leaving) {
            this.displayError("ERROR " + o.status + ": Failed to load " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
        }
    };

    this.completeInit = function (o) {
        try {
            this.list = eval("(" + o.responseText + ")").ResultSet.Result;
            this.list.sort(sortByLabel);
            this.onLoadComplete.fire();

            if (this.options != undefined && this.options.selects != undefined) {
                for (var selectC = 0; selectC < this.options.selects.length; selectC++) {
                    var selectBox = this.options.selects[selectC];
                    if (this.options.defaultValue != undefined) {
                        renderSubjects(selectBox, this.list, this.options.defaultValue, this.project);
                    } else {
                        renderSubjects(selectBox, this.list, "", this.project);
                    }
                }
            }
        } catch (e) {
            if (o.status != 200) {
                this.displayError("ERROR " + o.status + ": Failed to parse " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
            } else {
                this.displayError("EXCEPTION: " + e.toString());
            }
        }

    };

    this.displayError = function (errorMsg) {
        xModalMessage('Error', errorMsg);
    };
}

function ExptLoader() {
    this.onLoadComplete = new YAHOO.util.CustomEvent("load-complete", this);

    this.load = function (_project) {
        //load from search xml from server
        this.initCallback = {
            success:this.completeInit,
            failure:this.initFailure,
            cache:false, // Turn off caching for IE
            scope:this
        }

        YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/REST/projects/' + _project + '/experiments?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime(), this.initCallback, null, this);
    };

    this.initFailure = function (o) {
        if (!window.leaving) {
            this.displayError("ERROR " + o.status + ": Failed to load experiment list.");
        }
    };

    this.completeInit = function (o) {
        try {
            this.list = eval("(" + o.responseText + ")").ResultSet.Result;
            this.onLoadComplete.fire();
        } catch (e) {
            this.displayError("ERROR " + o.status + ": Failed to parse experiment list.");
        }

    };

    this.displayError = function (errorMsg) {
        xModalMessage('Error', errorMsg);
    };
}

function renderProjects(selectBox, list, defaultValue) {
    while (selectBox.length > 0) {
        selectBox.remove(0);
    }

    selectBox.options[0] = new Option("Select Project", "");

    for (var pC = 0; pC < list.length; pC++) {
        var defaultSelected = (list[pC].id == defaultValue) ? true : false;
        var opt = new Option(list[pC].secondary_id, list[pC].id, defaultSelected, defaultSelected);
        selectBox.options[pC + 1] = opt;
        selectBox.options[pC + 1].pc = list[pC].proj_prearchive_code;
        selectBox.options[pC + 1].qc = list[pC].proj_quarantine;
        if (defaultSelected) {
            selectBox.selectedIndex = (selectBox.options.length - 1);
            var pc = document.getElementById("pc_0");
            if (pc != undefined && pc != null) {
                if (list[pC].proj_prearchive_code == "4") {
                    if (list[pC].proj_quarantine == "0") {
                        document.getElementById("pc_2").click();
                    } else {
                        document.getElementById("pc_1").click();
                    }
                } else {
                    document.getElementById("pc_0").click();
                }
            }
        }
    }

    if (window.projectPostLoadDisabled) {
        selectBox.disabled = window.projectPostLoadDisabled;
    } else {
        selectBox.disabled = false;
    }

}

function renderSubjects(selectBox, list, defaultValue, projectID) {
    while (selectBox.length > 0) {
        selectBox.remove(0);
    }

    if (projectID == undefined) {
        projectID = "";
    }

    selectBox.options[0] = new Option("Select Subject", "");

    for (var sC = 0; sC < list.length; sC++) {
        var _label = list[sC]["label"];
        var defaultSelected = (list[sC].ID == defaultValue || _label == defaultValue) ? true : false;
        _label = (_label == undefined || _label == "") ? list[sC].ID : _label;
        var opt = new Option(_label, list[sC].ID, defaultSelected, defaultSelected);
        selectBox.options[sC + 1] = opt;
        if (defaultSelected) {
            selectBox.selectedIndex = (selectBox.options.length - 1);
        }
    }

    selectBox.disabled = false;
}

function ProjectEditor(_config) {
    this.config = _config;

    this.onModification = new YAHOO.util.CustomEvent("modification", this);

    this.render = function () {
        if (this.panel == undefined) {
            this.panel = new YAHOO.widget.Dialog("projectDialog", {
                close:true,
                //width:"350px",
                //height:"100px",
                //zIndex:9,
                underlay:"shadow",
                modal:true,
                fixedcenter:true,
                visible:false
            });
            this.panel.setHeader(XNAT.app.displayNames.singular.project + " modification");

            var bd = document.createElement("form");

            var table = document.createElement("table");
            var tb = document.createElement("tbody");
            table.appendChild(tb);
            bd.appendChild(table);

            //modality
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");

            td1.innerHTML = XNAT.app.displayNames.singular.project + ":";
            td1.align = "left";
            this.selectBox = document.createElement("select");
            this.selectBox.id = "new_project";
            this.selectBox.name = "new_project";
            td2.appendChild(this.selectBox);
            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            this.panel.setBody(bd);

            this.panel.form = bd;
            
            this.panel.selector = this;
            var buttons = [
                {text:"Modify", handler:{fn:function () {
                    this.checkImageAssessors = function() {
                        if (this.selector.config.imageAssessors && this.selector.config.imageAssessors.length > 0) {
                            var popupDIV = document.createElement("DIV");
                            popupDIV.id="search-column-filter";
                            var popupHD = document.createElement("DIV");
                            popupHD.className="hd";
                            popupHD.innerHTML="Select image assessors to move with session";
                            popupDIV.appendChild(popupHD);
                            var popupBD = document.createElement("DIV");
                            popupBD.className="bd";
                            popupBD.style.overflow="auto";
                            popupDIV.appendChild(popupBD);
                            var popupUL = document.createElement("UL");
                            popupUL.className="ul";
                            popupBD.appendChild(popupUL);
                            for (var i = 0; i < this.selector.config.imageAssessors.length; i++) {
                                var popupLI = document.createElement("LI");
                                popupLI.className="li";
                                popupLI.style.listStyle="none";
                                popupUL.appendChild(popupLI);
                                var popupCheckbox = document.createElement("INPUT");
                                popupCheckbox.id="assessorCheck"+i;
                                popupCheckbox.name=i;
                                popupCheckbox.type="checkbox";
                                popupCheckbox.checked=true;
                                popupCheckbox.onclick = function() {
                                    window.projectEditor.config.imageAssessors[this.name].move = this.checked;
                                }
                                popupLI.appendChild(popupCheckbox);
                                var popupLabel = document.createElement("LABEL");
                                popupLabel.htmlFor="assessorCheck"+i;
                                popupLabel.innerHTML = this.selector.config.imageAssessors[i].label;
                                popupLI.appendChild(popupLabel);
                            }
                            this.assessorsPopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,width:"350px",height:"250px",visible:true,fixedcenter:true,modal:true});
                            var handleCancel = function() {
                                this.hide();
                            };
                            var handleSubmit = function() {
                                this.hide();
                                window.projectEditor.panel.checkIfSessionSubjectIsOwnedByOrSharedIntoTheSessionProject();
                                return;
                            };
                            var myButtons = [ { text:"Submit", handler:handleSubmit, isDefault:true },
                                { text:"Cancel", handler:handleCancel } ];
                            this.assessorsPopup.cfg.queueProperty("buttons", myButtons);
                            this.assessorsPopup.render(document.body);
                            this.assessorsPopup.show();
                        }
                        else {
                            this.checkIfSessionSubjectIsOwnedByOrSharedIntoTheSessionProject();
                        }
                    }
                	
                	this.checkIfSessionSubjectIsOwnedByOrSharedIntoTheSessionProject = function () {
                        var callback = {
	                            success:function (o) {
	                            	// subject is already owned or shared, no additional action needed
	                            	this.modifyProject();
	                            },
	                            failure:function (o) {
                                    if (!window.leaving) {
                                        if( o.status == 404 ) {
                                            // subject not currently owned by or shared into the new project, warn user that we must do this to change the project
                                            if (confirm("As part of this change, the system will attempt to share this " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + "'s " + XNAT.app.displayNames.singular.subject.toLowerCase() + " into the new " + XNAT.app.displayNames.singular.project.toLowerCase() + ".  Is this OK?")) {
                                                this.subjectNeedsToBeSharedIntoNewProject = true;
                                                this.modifyProject();
                                            } else {
                                                this.cancel();
                                            }
                                        }
                                        else {
                                            // some systemic error occurred
                                            xModalMessage('Modify Project Error', 'ERROR (' + o.status + '): Failed to modify ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.');
                                            closeModalPanel("modify_project");
                                        }
                                    }
	                            },
                                cache:false, // Turn off caching for IE
	                            scope:this
                        }

                        this.subjectNeedsToBeSharedIntoNewProject = false;
                        
                        var url = serverRoot + "/REST/projects/" + this.selector.new_project + "/subjects/" + window.currentSubjectLabel + "?format=json&XNAT_CSRF=" + csrfToken;
                        YAHOO.util.Connect.asyncRequest('GET', url, callback);
                	}
                    
                	this.modifyProject = function () {
                        if (confirm("Modifying the primary " + XNAT.app.displayNames.singular.project.toLowerCase() + " of an imaging " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + " will result in the moving of files on the file server into the new " + XNAT.app.displayNames.singular.project.toLowerCase() + "'s storage space.  Are you sure you want to make this change?")) {
                        	if(showReason){
                    			var justification=new XNAT.app.requestJustification("file",XNAT.app.displayNames.singular.project + " Modification Justification",this._modifyProject,this);
                    		}else{
                    			var passthrough= new XNAT.app.passThrough(this._modifyProject,this);
                    			passthrough.fire();
                    		}
                        } else {
                            this.cancel();
                        }
                    }
                	
                	this._modifyProject=function(arg1,arg2,container){
                	    var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
                		openModalPanel("modify_project", "Modifying " + XNAT.app.displayNames.singular.project.toLowerCase() + ", please wait...");

                        var callback = {
                                success:function (o) {
                                	if( this.subjectNeedsToBeSharedIntoNewProject ) {
                                    	this.shareSubjectIntoNewProject();
                                	}
                                    window.currentProject = this.selector.new_project;
                                    closeModalPanel("modify_project");
                                    this.selector.onModification.fire();
                                    this.cancel();
                                },
                                failure:function (o) {
                                    if (!window.leaving) {
                                        xModalMessage('Modify Project Error', 'ERROR (' + o.status + '): Failed to modify ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.');
                                        closeModalPanel("modify_project");
                                    }
                                },
                                cache:false, // Turn off caching for IE
                                scope:this
                        }
                        
                        var params="";		
                 	   	params+="event_reason="+event_reason;
                 	   	params+="&event_type=WEB_FORM";
                 	   	params+="&event_action=Modified project";
                        if (this.selector.config.imageAssessors && this.selector.config.imageAssessors.length > 0) {
                            var commaSeparatedAssessorList = "";
                            for (var i = 0; i < this.selector.config.imageAssessors.length; i++) {
                                if (this.selector.config.imageAssessors[i].move) {
                                    if (commaSeparatedAssessorList) {
                                        commaSeparatedAssessorList += ",";
                                    }
                                    commaSeparatedAssessorList += this.selector.config.imageAssessors[i].id;
                                }
                            }
                            if (commaSeparatedAssessorList) {
                                params += ("&moveAssessors=" + commaSeparatedAssessorList);
                            }
                        }
                        var url = this.selector.config.uri + "/projects/" + this.selector.new_project + "?primary=true&format=json&XNAT_CSRF=" + csrfToken+"&"+params;
                        YAHOO.util.Connect.asyncRequest('PUT', url, callback);
                	}                	
                    
                	this.shareSubjectIntoNewProject = function () {
                		
                		// attempt this as a convenience to the user
                		// if there is a label conflict or other issue, we'll not worry about it here
                		// they can always share in the subject manually

                		var url = serverRoot + "/REST"
                        		+ "/projects/" + window.currentProject
                        		+ "/subjects/" + window.currentSubject
                        		+ "/projects/" + this.selector.new_project 
                        		+ "?XNAT_CSRF=" + csrfToken + "&label=" + window.currentSubjectLabel;
                		
                        YAHOO.util.Connect.asyncRequest('PUT', url, {});
                    }
                    
                    this.selector.new_project = this.form.new_project.options[this.form.new_project.selectedIndex].value;
                    this.selector.new_project_name = this.form.new_project.options[this.form.new_project.selectedIndex].text;

                    if (this.selector.new_project == window.currentProject) {
                        xModalMessage('Modify Project', 'No ' + XNAT.app.displayNames.singular.project.toLowerCase() + ' modification found.');
                        this.cancel();
                    } else if (this.form.new_project.selectedIndex == 0) {
                        xModalMessage('Modify Project', 'Please select a ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.');
                    } else {
                        if (this.selector.config.uri == undefined) {
                            window.currentProject = this.selector.new_project;
                            closeModalPanel("modify_project");
                            this.selector.onModification.fire();
                            this.cancel();
                        } else {
                        	this.checkImageAssessors();
                        }
                    }
                }}, isDefault:true},
                {text:"Cancel", handler:{fn:function () {
                    this.cancel();
                }}}
            ];
            this.panel.cfg.queueProperty("buttons", buttons);
            this.panel.render("page_body");

            this.panel.show();

            if (window.projectLoader == undefined) {
                window.projectLoader = new ProjectLoader({selects:[this.selectBox], defaultValue:window.currentProject, member:true, owner:true});
                openModalPanel("projects_loading", "Loading " + XNAT.app.displayNames.plural.project.toLowerCase() + "...");
                window.projectLoader.onLoadComplete.subscribe(function (obj) {
                    closeModalPanel("projects_loading");
                })
                window.projectLoader.init();
            }
        }
    }
}

function SubjectEditor(_config) {
    this.config = _config;

    this.onModification = new YAHOO.util.CustomEvent("modification", this);

    this.render = function () {
        if (this.panel == undefined) {
            this.panel = new YAHOO.widget.Dialog("subjectDialog", {
                close:true,
                //width:"350px",
                //height:"100px",
                // does 'zIndex' do anything?
                //zIndex:9,
                underlay:"shadow",
                modal:true,
                fixedcenter:true,
                visible:false
            });
            this.panel.setHeader(XNAT.app.displayNames.singular.subject + " modification");

            var bd = document.createElement("form");

            var table = document.createElement("table");
            var tb = document.createElement("tbody");
            table.appendChild(tb);
            bd.appendChild(table);

            //modality
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");
            td3 = document.createElement("td");

            td1.innerHTML = XNAT.app.displayNames.singular.subject + ":";
            td1.align = "left";

            window.subjectBox = document.createElement("select");
            window.subjectBox.id = "new_subject";
            window.subjectBox.name = "new_subject";
            td2.appendChild(window.subjectBox);

            tr.appendChild(td1);
            tr.appendChild(td2);

            if (this.config.create_subject_link) {
                td3.appendChild(document.createTextNode("Or, "));
                this.chs = document.createElement("input");
                this.chs.id = "create_subject_button";
                this.chs.type = "button";
                this.chs.value = "CREATE " + XNAT.app.displayNames.singular.subject.toUpperCase();
                this.chs.project = window.currentProject;
                this.chs.create_subject_link = this.config.create_subject_link;
                this.chs.onclick = function () {
                    if (this.project != undefined) {
                        if (window.subjectForm != undefined) {
                            window.subjectForm.close();
                            window.subjectForm = null;
                        }
                        window.subjectForm = window.open(this.create_subject_link, '', 'width=500,height=550,status=yes,resizable=yes,scrollbars=yes,toolbar=no');
                        if (window.subjectForm.opener == null) window.subjectForm.opener = self;
                        return window.subjectForm;
                    } else {
                        xModalMessage('Modify Subject', 'Please select a ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.');
                    }
                }
                td3.appendChild(this.chs);
                tr.appendChild(td3);
            }

            tb.appendChild(tr);

            this.panel.setBody(bd);

            this.panel.form = bd;

            this.panel.selector = this;
            var buttons = [
                {text:"Modify", handler:{fn:function () {
                    this.selector.new_subject = this.form.new_subject.options[this.form.new_subject.selectedIndex].value;
                    this.selector.new_subject_name = this.form.new_subject.options[this.form.new_subject.selectedIndex].text;

                    if (this.selector.new_subject == window.currentSubject) {
                        xModalMessage('Modify Subject', 'No ' + XNAT.app.displayNames.singular.subject.toLowerCase() + ' modification found.');
                        this.cancel();
                    } else if (this.form.new_subject.selectedValue == 0) {
                        xModalMessage('Modify Subject', 'Please select a ' + XNAT.app.displayNames.singular.subject.toLowerCase());
                    } else {
                        if (this.selector.config.uri == undefined) {
                            window.currentSubject = this.selector.new_subject;
                            window.currentSubjectLabel = this.selector.new_subject_name;
                            closeModalPanel("modify_subject");
                            this.selector.onModification.fire();
                            this.cancel();
                        } else {
                            if (confirm("Modifying the " + XNAT.app.displayNames.singular.subject.toLowerCase() + " of an experiment may result in the moving of files on the file server into the new " + XNAT.app.displayNames.singular.subject.toLowerCase() + "'s storage space.  Are you sure you want to make this change?")) {
                            	if(showReason){
                        			var justification=new XNAT.app.requestJustification("file",XNAT.app.displayNames.singular.subject + " Modification Justification",XNAT.app._modifySubject,this);
                        		}else{
                        			var passthrough= new XNAT.app.passThrough(XNAT.app._modifySubject,this);
                        			passthrough.fire();
                        		}
                            } else {
                                this.cancel();
                            }
                        }
                    }
                }}, isDefault:true},
                {text:"Cancel", handler:{fn:function () {
                    this.cancel();
                }}}
            ];
            this.panel.cfg.queueProperty("buttons", buttons);
            this.panel.render("page_body");

            this.panel.show();

            if (window.subjectLoader == undefined) {
                window.subjectLoader = new SubjectLoader({selects:[window.subjectBox], defaultValue:window.currentSubject});


            }
        }

        this.refresh = function () {
            window.subjectLoader.onLoadComplete.subscribe(function (obj) {
                closeModalPanel("subjects_loading");
            });

            openModalPanel("subjects_loading", "Loading " + XNAT.app.displayNames.singular.subject.toLowerCase() + "...");
            window.subjectLoader.load(window.currentProject, {selects:[window.subjectBox], defaultValue:window.currentSubject});
        }

        this.refresh();
    }
}



XNAT.app._modifySubject=function(arg1,arg2,container){
	openModalPanel("modify_subject", "Modifying " + XNAT.app.displayNames.singular.subject.toLowerCase() + ", please wait...");

	var settingsCallback = {
        success:function (o) {
            window.currentSubject = this.selector.new_subject;
            window.currentSubjectLabel = this.selector.new_subject_name;
            closeModalPanel("modify_subject");
            this.selector.onModification.fire();
            this.cancel();
        },
        failure:function (o) {
            if (!window.leaving) {
                xModalMessage('Modify Subject Error', 'ERROR (' + o.status + '): Failed to modify ' + XNAT.app.displayNames.singular.subject.toLowerCase() + '.');
                closeModalPanel("modify_subject");
            }
        },
        scope:this
    }

    var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
    var params="";		
	   	params+="event_reason="+event_reason;
	   	params+="&event_type=WEB_FORM";
	   	params+="&event_action=Modified subject";

    YAHOO.util.Connect.asyncRequest('PUT', serverRoot + "/REST/projects/" + window.currentProject + "/subjects/" + this.selector.new_subject + "/experiments/" + window.currentID + "?format=json&XNAT_CSRF=" + csrfToken+"&"+params, settingsCallback);
	}    

function LabelEditor(_config) {
    this.config = _config;

    if (this.config.header == undefined) {
        this.config.header = XNAT.app.displayNames.singular.imageSession;
    }

    this.onModification = new YAHOO.util.CustomEvent("modification", this);

    this.render = function () {
        if (this.panel == undefined) {
            var bd = document.createElement("form");
            var table = document.createElement("table");
            var tb = document.createElement("tbody");
            table.appendChild(tb);
            bd.appendChild(table);

            window.labelInput = document.createElement("input");
            window.labelInput.id = "new_label";
            window.labelInput.value = window.currentLabel;
            window.labelInput.name = "new_label";

            this.labelContainer = document.createElement("div");
            this.labelContainer.id = "complete_container";
            this.labelContainer.width = "100px";
            this.labelContainer.appendChild(window.labelInput);

            //modality
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");
            td3 = document.createElement("td");

            td1.innerHTML = this.config.header + ":";
            td1.align = "left";

            td2.appendChild(this.labelContainer);

            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            this.panel = new YAHOO.widget.Dialog("labelDialog", {
                close:true,
                //width:"350px",
                //zIndex:9,
                underlay:"shadow",
                modal:true,
                fixedCenter:true,
                visible:false
            });
            this.panel.handleEnter = function () {
                var label = this.form.new_label;
                window.selectedLabel = label.value.trim();
                if (window.selectedLabel == "") {
                    xModalMessage('Label Validation', 'Please specify a new ' + this.selector.config.header + '.');
                } else if (window.selectedLabel == window.currentLabel) {
                    xModalMessage('Label Validation', 'No modification found.');
                } else {
                    var validatedLabel = cleanLabel(window.selectedLabel);
                    if (validatedLabel != window.selectedLabel) {
                        label.value = validatedLabel;
                        xModalMessage('Label Validation', 'Invalid characters in new ' + this.selector.config.header + '.  Review modified value and resubmit.');
                        label.focus();
                        return;
                    }

                    var matchedExisting = false;
                    for (var lC = 0; lC < window.exptLoader.list.length; lC++) {
                        if (window.selectedLabel == window.exptLoader.list[lC].label) {
                            matchedExisting = true;
                            break;
                        }
                    }

                    if (matchedExisting) {
                        xModalMessage('Label Validation', 'This ' + this.selector.config.header + ' is already in use in this ' + XNAT.app.displayNames.singular.project.toLowerCase() + '.<br/><br/>Please modify and resubmit.');
                        label.focus();
                        return;
                    }

                    if (this.selector.config.uri == undefined) {
                        window.currentLabel = window.selectedLabel;
                        closeModalPanel("modify_new_label");
                        this.selector.onModification.fire();
                        this.cancel();
                    } else {
                        if (confirm("Modifying the " + this.selector.config.header + " of an imaging " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + " will result in the moving of files on the file server within the " + XNAT.app.displayNames.singular.project.toLowerCase() + "'s storage space.  Are you sure you want to make this change?")) {
                        	if(showReason){
                    			var justification=new XNAT.app.requestJustification("file",XNAT.app.displayNames.singular.imageSession + " Modification Justification",XNAT.app._modifyLabel,this);
                    		}else{
                    			var passthrough= new XNAT.app.passThrough(XNAT.app._modifyLabel,this);
                    			passthrough.fire();
                    		}
                        }
                    }
                }
            };
            this.panel.handleCancel = function () { this.cancel(); };

            var buttons = [
                {text:"Modify", handler:{fn:this.panel.handleEnter}, isDefault:true},
                {text:"Cancel", handler:{fn:this.panel.handleCancel}}
            ];

            var cancelListener = new YAHOO.util.KeyListener(document, { keys:27 }, { fn:this.panel.handleCancel, scope:this.panel, correctScope:true });
            var enterListener  = new YAHOO.util.KeyListener(document, { keys:13 }, { fn:this.panel.handleEnter,  scope:this.panel, correctScope:true });

            this.panel.setHeader("Label modification");
            this.panel.setBody(bd);
            this.panel.form = bd;
            this.panel.selector = this;
            this.panel.cfg.queueProperty("keyListeners", [cancelListener, enterListener]);
            this.panel.cfg.queueProperty("buttons", buttons);
            this.panel.render("page_body");
            this.panel.show();
        }

        window.exptLoader.onLoadComplete.subscribe(function (obj) {
            closeModalPanel("labels_loading");

            window.labelInput.disabled = false;
        });

        openModalPanel("labels_loading", "Loading " + this.config.header + "s...");
        window.exptLoader.load(window.currentProject);
    }


}


XNAT.app._modifyLabel=function(arg1,arg2,container){
	openModalPanel("modify_new_label", "Modifying " + this.selector.config.header + ", please wait...");

	 var settingsCallback = {
         success:function (o) {
             window.currentLabel = window.selectedLabel;
             closeModalPanel("modify_new_label");
             this.selector.onModification.fire();
             this.cancel();
         },
         failure:function (o) {
             if (!window.leaving) {
                 xModalMessage('ERROR (' + o.status + '): Failed to modify ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' ID', o.responseText);
                 closeModalPanel("modify_new_label");
             }
         }, scope:this
     }
	 
    var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
    var params="";		
	   	params+="event_reason="+event_reason;
	   	params+="&event_type=WEB_FORM";
	   	params+="&event_action=Modified label";
     
    YAHOO.util.Connect.asyncRequest('PUT', this.selector.config.uri + "?label=" + window.selectedLabel + "&format=json&XNAT_CSRF=" + csrfToken + "&" + params, settingsCallback);
}  

window.success = function (subject_id) {
    if (window.subjectForm != undefined) {
        window.subjectForm.close();
        window.subjectForm = null;
    }
    window.subjectLoader.load(window.currentProject, {selects:[window.subjectBox], defaultValue:subject_id});
}

window.failure = function (msg) {
    if (!window.leaving) {
        if (window.subjectForm != undefined) {
            window.subjectForm.close();
            window.subjectForm = null;
        }
    }
}

function cleanLabel(val) {
    var temp = val.replace(/^\s*|\s*$/g, "");
    var newVal = '';
    temp = temp.split(' ');
    for (var c = 0; c < temp.length; c++) {
        newVal += '' + temp[c];
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
}
