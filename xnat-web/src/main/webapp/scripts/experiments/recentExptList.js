/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/experiments/recentExptList.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function MinExptList(_div, _options){

    var exptList = this, // keep proper scope in callbacks
        xhr      = XNAT.xhr,
        undefined;

    exptList.options = _options;
    exptList.div = _div;

    if (exptList.options == undefined) {
        exptList.options = {};
        exptList.options.recent = true;
    }

    exptList.init = function(){
        exptList.initLoader = prependLoader(exptList.div, "Loading recent data");
        exptList.initLoader.render();
        //load from search xml from server
        exptList.initCallback = {
            success: exptList.completeInit,
            failure: exptList.initFailure
        };

        var params = [];

        if (exptList.options.recent != undefined) {
            params.push('recent=true');
        }

        var url = XNAT.url.restUrl('/data/experiments', params);

        xhr.getJSON(url, exptList.initCallback);

    };

    exptList.initFailure = function(o){
        if (!window.leaving) {
            exptList.displayError("ERROR " + o.status + ": Failed to load experiment list.");
        }
        exptList.initLoader.close();
    };

    exptList.completeInit = function(json, status, jqXHR){
        try {
            exptList.exptResultSet = json.ResultSet.Result;
        }
        catch (e) {
            exptList.displayError("ERROR " + status + ": Failed to parse experiment list.");
        }
        exptList.initLoader.close();
        try {
            exptList.render();
        }
        catch (e) {
            exptList.displayError("ERROR : Failed to render experiment list.");
        }
    };

    exptList.displayError = function(errorMsg){
        xmodal.message('Experiment List Error', errorMsg);
    };

    exptList.render = function(){
        
        var display = document.getElementById(exptList.div);
        var t = document.createElement("table");
        t.width = "100%";
        t.cellSpacing = "0px";
        var tb = document.createElement("tbody");

        for (var eC = 0; eC < exptList.exptResultSet.length; eC++) {

            var expt = exptList.exptResultSet[eC];

            var tr = document.createElement("tr");

            if (eC % 2 == 0) {
                tr.className = "even";
            }
            else {
                tr.className = "odd";
            }

            var td = document.createElement("td");
            td.align = "left";

            var projName = expt.project;

            if (expt.project.length > 10) {
                projName = projName.substring(0,9) + '&hellip;';
            }

            td.innerHTML = '<a title="' + expt.project + '" href="' + XNAT.url.dataUrl(['projects', expt.project, '?format=html']) + '">' + projName + '</a>';
            tr.appendChild(td);

            td = document.createElement("td");
            td.align = "left";
            td.innerHTML = expt.type_desc;
            tr.appendChild(td);

            td = document.createElement("td");
            td.align = "left";

            if (expt.label == "") {
                var tempLabel = expt.id;
            }
            else {
                var tempLabel = expt.label;
            }

            var labelLink = "<a";
            labelLink += " href='" + serverRoot + "/app/action/DisplayItemAction/search_element/" + expt.element_name + "/search_field/" + expt.element_name + ".ID/search_value/" + expt.id + "/project/" + expt.project + "'";

            if (tempLabel.length > 18) {
                labelLink += " title='" + tempLabel + "'>" + tempLabel.substring(0, 15) + "...";
            }
            else {
                labelLink += ">" + tempLabel;
            }
            labelLink += "</a>";

            td.innerHTML = labelLink;

            tr.appendChild(td);

            td = document.createElement("TD");
            if (expt.workflow_date != "" && expt.pipeline_name != "") {
                if (expt.workflow_status == "Complete") {
                    td.innerHTML = "";
                }
                else if (expt.workflow_status == "Failed") {
                    td.innerHTML = "<img src='" + serverRoot + "/images/icon-alert-9px.png' title='Failed'/>";
                }
                else if (expt.workflow_status == "Queued") {
                    td.innerHTML = "<img src='" + serverRoot + "/images/icon-queued-9px.png' title='Queued'/>";
                }
                else {
                    td.innerHTML = "<img src='" + serverRoot + "/images/icon-waiting-9px.gif' title='" + expt.workflow_status + "'/>";
                }
            }
            else {
                td.innerHTML = "";
            }
            tr.appendChild(td);

            td = document.createElement("td");
            td.align = "right";

            if (expt.workflow_date != "" && expt.pipeline_name != "") {
                var tdTmp = '<A class="recentDataActivity" title="' + expt.pipeline_name;
                if (expt.workflow_status != "Complete") {
                    tdTmp += ' "' + expt.workflow_status + '"';
                }
                tdTmp += " at " + expt.workflow_date + "'>" + expt.pipeline_name.replace('_', ' ') + "<a>";
                td.innerHTML = tdTmp;
            }
            else if (expt.last_modified != "") {
                td.innerHTML = "<A class='recentDataActivity' title='Modified at " + expt.last_modified + "'>Modified<a>";
            }
            else if (expt.insert_date != "") {
                td.innerHTML = "<A class='recentDataActivity' title='Created at " + expt.insert_date + "'>Created<a>";
            }
            else {
                td.innerHTML = "<span class='recentDataActivity'>Created</span>";
            }

            tr.appendChild(td);
            tb.appendChild(tr);

//			tr.extension=eC+"_rExpt_tr";
//			tr.onclick=function(){
//				var extension=document.getElementById(exptList.extension);
//				extension.style.display=(extension.style.display=="none")?"":"none";
//			}
//			tr.style.cursor="pointer";

            tr = document.createElement("tr");
            tr.id = eC + "_rExpt_tr";
            tr.style.display = "none";
            if (eC % 2 == 0) {
                tr.className = "even";
            }
            else {
                tr.className = "odd";
            }
            td = document.createElement("td");
            td.colSpan = "4";
            td.innerHTML = "&nbsp;";

            tr.appendChild(td);
            tb.appendChild(tr);
        }
        t.appendChild(tb);
        display.appendChild(t);

    }
}

function prependLoader(div_id, msg){
    var div;
    if (div_id.id == undefined) {
        div = document.getElementById(div_id);
    }
    else {
        div = div_id;
    }
    var loader_div = document.createElement("div");
    loader_div.innerHTML = msg;
    div.parentNode.insertBefore(loader_div, div);
    return new XNATLoadingGIF(loader_div);
}
