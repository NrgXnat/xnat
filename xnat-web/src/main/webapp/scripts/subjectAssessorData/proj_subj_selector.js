/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/subjectAssessorData/proj_subj_selector.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/13/14 5:34 PM
 */
function ProjectSubjectSelector(_proj_select, _subj_select, _submit_button, _defaultProject, _defaultSubject) {

    this.projectSelect = _proj_select;
    this.subjSelect = _subj_select;
    this.submitButton = _submit_button;
    this.defaultProject = _defaultProject;
    this.defaultSubject = _defaultSubject;

    this.init = function () {

        // load from search xml from server
        this.initCallback = {
            success: this.completeInit,
            failure: this.initFailure,
            cache: false, // Turn off caching for IE
            scope: this
        };

        var params = "";
        //params += "&owner=true";
        //params += "&member=true";
        params += '&creatableTypes=true';

        if (XNAT.data.context.isExperiment && XNAT.data.context.xsiType){
            params += '&data-type=' + XNAT.data.context.xsiType;
        }

        YAHOO.util.Connect.asyncRequest('GET',
            serverRoot +
                '/REST/projects?XNAT_CSRF=' + window.csrfToken +
                '&format=json&timestamp=' + (new Date()).getTime() + params,
            this.initCallback, null, this);
    };


    this.initFailure = function (o) {
        this.displayError("ERROR " + o.status + ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
    };


    this.completeInit = function (o) {
        try {
            this.projectResultSet = eval("(" + o.responseText + ")");
        } catch (e) {
            this.displayError("ERROR " + o.status + ": Failed to parse " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
        }
        try {
            this.renderProjects();
        } catch (e) {
            this.displayError("ERROR : Failed to render " + XNAT.app.displayNames.singular.project.toLowerCase() + " list: " + e.toString());
        }
    };


    this.displayError = function (errorMsg) {
        xModalMessage('Error', errorMsg);
    };


    this.renderProjects = function () {

        var opt ;

        if (this.projectResultSet.ResultSet.Result.length == 0) {

        }
        else {
            this.projBox = document.getElementById(this.projectSelect);
            this.projBox.options[0] = new Option("Select a Project", "");

            for (var pC = 0; pC < this.projectResultSet.ResultSet.Result.length; pC++) {
                var defaultSelected = (this.projectResultSet.ResultSet.Result[pC].id == this.defaultProject);
                opt = new Option(
                    this.projectResultSet.ResultSet.Result[pC].secondary_id.replace(/&apos;/g, '\''),
                    this.projectResultSet.ResultSet.Result[pC].id,
                    defaultSelected, defaultSelected);
                this.projBox.options[pC + 1] = opt;
                if (defaultSelected) {
                    this.projBox.selectedIndex = (this.projBox.options.length - 1);
                }
            }

            this.projBox.disabled = false;

            this.projBox.manager = this;

            this.projBox.onchange = function (o) {
                if (this.selectedIndex > 0) {
                    this.manager.projID = this.options[this.selectedIndex].value;
                    this.manager.loadSubjects();
                    this.manager.loadExpts();
                }
            };

            if (this.projBox.selectedIndex > 0) {
                this.projBox.onchange();
            }
        }
    };


    this.loadSubjects = function (o) {
        try {
            var subjCallback = {
                success: function (o) {
                    try {
                        o.argument.subjectResultSet = eval("(" + o.responseText + ")");
                        o.argument.subjectResultSet.ResultSet.Result.sort(function (a, b) {
                                if (a["label"] < b["label"]) {
                                    return -1;
                                }
                                else if (b["label"] < a["label"]) {
                                    return 1;
                                }
                                else {
                                    return 0;
                                }
                            });
                    } catch (e) {
                        o.argument.displayError("ERROR " + o.status + ": Failed to parse " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
                    }
                    try {
                        o.argument.renderSubjects();
                    } catch (e) {
                        o.argument.displayError("ERROR : Failed to render " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
                    }
                },
                failure: function (o) {
                    xModalMessage('Error', 'Failed to load ' + XNAT.app.displayNames.plural.subject.toLowerCase() + '.');
                },
                cache: false, // Turn off caching for IE
                argument: this
            };

            if (this.subjBox != undefined) {
                this.subjBox.disabled = true;

                while (this.subjBox.length > 0) {
                    this.subjBox.remove(0);
                }
            }

            YAHOO.util.Connect.asyncRequest('GET',
                serverRoot +
                    '/REST/projects/' + this.projID +
                    '/subjects?XNAT_CSRF=' + window.csrfToken +
                    '&format=json&timestamp=' + (new Date()).getTime(),
                subjCallback);

        } catch (e) {
            xModalMessage('Error', 'Failed to load ' + XNAT.app.displayNames.plural.subject.toLowerCase() + '.');
        }
    };


    this.renderSubjects = function (o) {

        this.subjBox = document.getElementById(this.subjSelect);
        this.subjBox.options[0] = new Option("Select a Subject", "");
        //this.subjBox.options[0].style.color = "black";

        var matched = false;
        var opt ;
        for (var sC = 0; sC < this.subjectResultSet.ResultSet.Result.length; sC++) {
            var defaultSelected = !!(this.subjectResultSet.ResultSet.Result[sC].ID == this.defaultSubject || this.subjectResultSet.ResultSet.Result[sC]["label"] == this.defaultSubject);
            if (defaultSelected)
                matched = true;
            var _label = this.subjectResultSet.ResultSet.Result[sC]["label"];
            _label = (_label == undefined || _label == "") ? this.subjectResultSet.ResultSet.Result[sC].ID : _label;
            opt = new Option(_label,
                this.subjectResultSet.ResultSet.Result[sC].ID,
                defaultSelected, defaultSelected);
            this.subjBox.options[sC + 1] = opt;
            //this.subjBox.options[sC + 1].style.color = "black";
            if (defaultSelected) {
                this.subjBox.selectedIndex = (this.subjBox.options.length - 1);
            }
        }
        this.subjBox.disabled = false;

        if (!matched && (this.defaultSubject != "NULL" && this.defaultSubject != "null" && this.defaultSubject != "" && this.defaultSubject != null)){
            opt = new Option(this.defaultSubject, this.defaultSubject, true, true);
            this.subjBox.options[sC + 1] = opt;
            this.subjBox.options[sC + 1].newValue = true;
            this.subjBox.options[sC + 1].style.color = "red";
            this.subjBox.selectedIndex = (this.subjBox.options.length - 1);
            if (YAHOO.env.ua.gecko > 0)
                this.subjBox.style.color = "red";
        }

        this.subjBox.submitButton = this.submitButton;
        if (eval("window.confirmValues") != undefined) {
            this.subjBox.onchange = function () {
                if (YAHOO.env.ua.gecko > 0)
                    this.style.color = this.options[this.selectedIndex].style.color;
                confirmValues(false);
                checkSubmitButton(this.selectedIndex, this.submitButton);
            };

            confirmValues(false);
            checkSubmitButton(this.selectedIndex, this.submitButton);
        }
        else {
            this.subjBox.onchange = function () {
                if (YAHOO.env.ua.gecko > 0)
                    this.style.color = this.options[this.selectedIndex].style.color;
                checkSubmitButton(this.selectedIndex, this.submitButton);
            }
        }
    };

    this.loadExpts = function (o) {
        try {
            var subjCallback = {
                success: function (o) {

                    try {
                        var resultset = (eval("(" + o.responseText + ")")).ResultSet;
                        if (resultset.totalRecords == "0") {
                            if (window.psm != undefined) {
                                window.psm.exptResultSet = [];
                            }
                        }
                        else {
                            if (window.psm != undefined) {
                                window.psm.exptResultSet = resultset.Result;
                            }
                        }
                    } catch (e) {
                        if (window.psm != undefined)
                            window.psm.exptResultSet = [];
                        if (o.argument.displayError != undefined)
                            o.argument.displayError("ERROR " + o.status + ": Failed to parse expt list.");
                    }

                    if (verifyExptId != undefined && verifyExptId != null) {
                       if(verifyExptId() === false){ valid = false; };
                    }
                },
                failure: function (o) {
                    xModalMessage('Error', 'Failed to load experiments.');
                },
                cache: false, // Turn off caching for IE
                argument: this
            };

            YAHOO.util.Connect.asyncRequest('GET',
                serverRoot +
                    '/REST/projects/' + this.projID +
                    '/experiments?XNAT_CSRF=' + window.csrfToken +
                    '&format=json&timestamp=' + (new Date()).getTime(),
                subjCallback);

        } catch (e) {
            xModalMessage('Error', 'Failed to load experiments.');
        }
    }
}


function checkSubmitButton(selectedIndex, submitButton) {
    var button = document.getElementById(submitButton);
    if (button) {
        button.disabled = selectedIndex == 0;
    }
}

var sessionCaretStart=0;
var sessionCaretEnd=0;
var sessionSelecting=false;
function getSessionSelection(e, el){
  var left=37, right=39, key=e.keyCode, len=el.value.length;
  sessionCaretStart=el.selectionStart;
  sessionCaretEnd=el.selectionEnd;
  if(e.shiftKey){
    if(sessionCaretStart==sessionCaretEnd){
      if(key == left){
        sessionSelecting = true;
        sessionCaretStart = sessionCaretStart-1;
      }
      if(key == right){
        sessionCaretEnd = sessionCaretEnd+1;
      }
    } else if (sessionCaretStart<sessionCaretEnd){
      if(key == left){
        if(sessionSelecting){
          sessionCaretStart = sessionCaretStart-1;
        } else {
          sessionCaretEnd = sessionCaretEnd-1;
        }
      }
      if(key == right){
        sessionCaretEnd = sessionCaretEnd+1;
      }
    }
  } else {
    sessionSelecting=false;
    if(key == left){
      sessionCaretStart = sessionCaretStart-1;
      sessionCaretEnd = sessionCaretStart;
    }
    if(key == right){
      sessionCaretEnd = sessionCaretEnd+1;
      sessionCaretStart = sessionCaretEnd;
    }
  }
  if(key != left && key != right){
    sessionCaretEnd = sessionCaretEnd+1;
    sessionCaretStart = sessionCaretEnd;
  }
  if(key == 36){ //home
    sessionCaretStart=0;
    sessionCaretEnd=0;
  }
  if(key == 35){ //end
    sessionCaretStart=len;
    sessionCaretEnd=len;
  }
  if(key == 46){ //del
    sessionCaretStart = sessionCaretStart-1;
    sessionCaretEnd = sessionCaretStart;
  }
  if(key == 8){ //backspace
    sessionCaretStart = sessionCaretStart-2;
    sessionCaretEnd = sessionCaretStart;
  }
//console.log('start: '+sessionCaretStart+'  end: '+sessionCaretEnd+'  sel: '+sessionSelecting+'  key: '+e.keyCode);
};
function setSessionSelection(e, el){
  el.setSelectionRange(sessionCaretStart, sessionCaretEnd);
};

function fixSessionID(val) {
    var temp = val.trim();
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
    newVal = newVal.replace(/[.]/, "_");
    newVal = newVal.replace(/[,]/, "_");
    newVal = newVal.replace(/[\^]/, "_");
    newVal = newVal.replace(/[@]/, "_");
    newVal = newVal.replace(/[!]/, "_");
    newVal = newVal.replace(/[%]/, "_");
    newVal = newVal.replace(/[*]/, "_");
    newVal = newVal.replace(/[#]/, "_");
    newVal = newVal.replace(/[$]/, "_");
    newVal = newVal.replace(/[\\]/, "_");
    newVal = newVal.replace(/[|]/, "_");
    newVal = newVal.replace(/[=]/, "_");
    newVal = newVal.replace(/[+]/, "_");
    newVal = newVal.replace(/[']/, "_");
    newVal = newVal.replace(/["]/, "_");
    newVal = newVal.replace(/[~]/, "_");
    newVal = newVal.replace(/[`]/, "_");
    newVal = newVal.replace(/[:]/, "_");
    newVal = newVal.replace(/[;]/, "_");
    newVal = newVal.replace(/[\/]/, "_");
    newVal = newVal.replace(/[\[]/, "_");
    newVal = newVal.replace(/[\]]/, "_");
    newVal = newVal.replace(/[{]/, "_");
    newVal = newVal.replace(/[}]/, "_");

    if (newVal != temp) {
        xModalMessage('Session Validation', 'Removing invalid characters in ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + '.');
    }
    return newVal;
}


function verifyExptId(obj) {
    try {
        if (elementName != undefined) {

            var pS = document.getElementById(elementName + "/project");

            if (pS.selectedIndex > 0) {

                var p = pS.options[pS.selectedIndex].value;
                var match = null, veid = false;
                if (document.getElementById(elementName + "/label") != null) {

                    var temp_label = document.getElementById(elementName + "/label").value.trim();
                    temp_label = fixSessionID(temp_label);
                    document.getElementById(elementName + "/label").value = temp_label;

                    if (temp_label != '' && window.psm.exptResultSet != undefined) {
                        for (var aSc = 0; aSc < window.psm.exptResultSet.length; aSc++) {
                            if (window.psm.exptResultSet[aSc].label == temp_label) {
                                match = window.psm.exptResultSet[aSc];
                                break;
                            }
                        }
                    }

                    if (match != null) {

                        document.getElementById(elementName + "/ID").value = match.ID;
                        document.getElementById(elementName + "/label").verified = true;
                        if(XNAT.app.isUpload != undefined && XNAT.app.isUpload === true){
                            XNAT.app.handleMatch("* Matches existing " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ". Continuing could modify that " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ".");
                            veid = true;
                         }else{
                            XNAT.app.handleMatch("* Matches existing " + XNAT.app.displayNames.singular.imageSession.toLowerCase()+".");
                            veid = false;
                         }
                    }
                    else {
                        document.getElementById(elementName + "/ID").value = "";
                        document.getElementById(elementName + "/label").verified = true;
                        document.getElementById("label_msg").innerHTML = "";
                        document.getElementById("label_opts").innerHTML = "";

                        veid = true;
                    }

                    return veid;
                }
            }
            else {
                return true;
            }
        }
    } catch (e) {
        xModalMessage('Error', 'Failed to validate expt id:' + e.message);
    }
}


XNAT.app.handleMatch = function (msg) {
    document.getElementById("label_msg").innerHTML = msg;
//	if (document.getElementById("label_opts").innerHTML == "")
//      removed when new archive validator was added
//		document.getElementById("label_opts").innerHTML = "<select name='overwrite' ID='session_overwrite'><option value='append' SELECTED>APPEND</option><option value='delete'>OVERWRITE</option></select>";
};