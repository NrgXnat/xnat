/*
 * web: scan_tools.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");

window.scanQualityLabels = {};

function setScanQualityOptions(sel,choices,offset,value){
    if(choices==undefined || choices.length==0){
	    choices=['usable','questionable','unusable'];
    }
    for(i=0;i<choices.length;i++){
	var choice=choices[i];
	var selected=(value==undefined)?(i==0):(choice==value);
	sel.options[i+offset]=new Option(choice,choice,selected,selected);
	if(selected){
	    sel.selectedIndex=i+offset;
	}
    }
    
    if(confirmValues!=undefined)confirmValues();
}

function populateScanQualitySelector(server,project,sel,offset,assigned) {
    var choices = null;
    if (!project && 'site' in window.scanQualityLabels) {
        choices = window.scanQualityLabels['site'];
    } else if (project in window.scanQualityLabels) {
        choices = window.scanQualityLabels[project];
    }
    if (choices) {
        setScanQualityOptions(sel, choices, offset, assigned);
        return;
    }

    var url=server+'/data/services/scan-quality-labels';
    if (project){
	    url+='/' + project;
    }
    url+='?XNAT_CSRF='+window.csrfToken+'&format=json';
    YAHOO.util.Connect.asyncRequest('GET',url,
                                    {
                                        success: function (resp) {
                                            var rs = eval('(' + resp.responseText + ')');
                                            var key = Object.keys(rs)[0];
                                            var choices = rs[key];
                                            window.scanQualityLabels[key] = choices;
                                            setScanQualityOptions(sel, choices, offset, assigned);
                                        },
                                        failure: function () {
                                            if (project) {
                                                populateScanQualitySelector(server, undefined, sel, offset, assigned);
                                            } else {
                                                setScanQualityOptions(sel, [], offset, assigned);
                                            }
                                        },
                                        cache: false
                                    });
}

function getNominalType(scan) {
    if (!scan.extension && scan.constructor.name === "xnat_imageScanData") {
        return getImageScanNominalType(scan);
    } else if (scan.extension) {
        return getImageScanNominalType(scan.extension);
    } else {
        return "Unknown";
    }
}

function getImageScanNominalType(scan) {
    if (scan.Type) {
        return scan.Type;
    } else if (scan.SeriesDescription) {
        return scan.SeriesDescription;
    } else {
        return "Unknown";
    }
}

function scanInit(_options){
  	this.options=_options;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);

    if(this.options.modalities==undefined){
    	this.options.modalities=new Array();
    	this.options.modalities.push({"value":"xnat:mrScanData","display":"MR"});
    	this.options.modalities.push({"value":"xnat:ctScanData","display":"CT"});
    	this.options.modalities.push({"value":"xnat:petScanData","display":"PET"});
    }

	this.render=function(){
		this.panel=new YAHOO.widget.Dialog("scanModalityDialog",{close:true,
		   width:"350px",height:"100px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Select Scan Modality");

		var bd = document.createElement("form");

		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);

		//modality
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");

		td1.innerHTML="Modality:";
		td1.align="left";
		var sel = document.createElement("select");
		sel.id="new_modality";
		sel.name="new_modality";
		for(var modC=0;modC<this.options.modalities.length;modC++){
		  sel.options[modC]=new Option(this.options.modalities[modC].display,this.options.modalities[modC].value);
		}
		td2.appendChild(sel);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);

		this.panel.setBody(bd);

		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Select",handler:{fn:function(){
				this.selector.modality = this.form.new_modality.options[this.form.new_modality.selectedIndex].value;
				this.cancel();
				this.selector.onResponse.fire();
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);


		this.panel.render("page_body");
		this.panel.show();
	}
}

function ScanEditor(_sessionID,_scanID,_options){
	this.sessionID=_sessionID;
  	this.scanID=_scanID;
  	this.options=_options;

  	this.onModification=new YAHOO.util.CustomEvent("modification",this);

  	this.init=function(){
  		if(this.scanID!=undefined){
			//load from search xml from server
			this.initCallback={
				success:this.completeInit,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
			}

			openModalPanel("load_scan","Loading Scan Details.");

			YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/experiments/' + this.sessionID +'/scans/' + this.scanID + '?XNAT_CSRF=' + window.csrfToken + '&format=xml',this.initCallback,null,this);
  		}else{
  			this.modalitySelector=new scanInit();
  			this.modalitySelector.onResponse.subscribe(function(){
  				var mod=this.modalitySelector.modality;
  				this.classMapping=new ClassMapping();
  				var fn = this.classMapping.newInstance;
  				this.scan=fn(mod);
  				this.render();
  			},this,this);

			this.modalitySelector.render();
  		}
  	}

	this.initFailure=function(o){
        if (!window.leaving) {
            closeModalPanel("load_scan");
            this.displayError("ERROR " + o.status+ ": Failed to load " + XNAT.app.displayNames.singular.subject.toLowerCase() + " list.");
        }
	};

	this.completeInit=function(o){
		try{
			closeModalPanel("load_scan");
			var xmlText =o.responseText;

            parser = new SAXDriver();
			var handler = new SAXEventHandler();

			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);

			parser.parse(xmlText);// start parsing

			if (handler.root){
				this.scan=handler.root;
			}else{
                xmodal.message('Scan Data Error', 'ERROR: Unable to retrieve scan data.');
			}
			if(this.options.button)this.options.button.disabled=false;
		}catch(e){
            xmodal.message('Scan Data Error', 'ERROR '+o.status+': Failed to parse scan.');
		}
		this.render();

	};

	this.displayError=function(errorMsg){
        xmodal.message('Scan Data Error', errorMsg);
	};
	
	this.render=function(){
        if (this.scan) {
            this.panel = new YAHOO.widget.Dialog("scanDialog", {close: true,
                width: "390px", height: "300px", underlay: "shadow", modal: true, fixedcenter: true, visible: false});
            if (this.scanID == undefined)
                this.panel.setHeader("New Scan Details");
            else
                this.panel.setHeader(this.scanID + " Details");

            var modality = this.scan.xsiType;
            var bd = document.createElement("form");

            var table = document.createElement("table");
            var tb = document.createElement("tbody");
            table.appendChild(tb);
            bd.appendChild(table);

            //id
            var tr = document.createElement("tr");
            var td1 = document.createElement("th");
            var td2 = document.createElement("td");

            td1.innerHTML = "ID:";
            td1.align = "left";
            if (this.scan.extension.XnatImagescandataId) {
                td2.innerHTML = "<input type='hidden' name='" + modality + "/ID' value='" + this.scan.getProperty("ID") + "'/>" + this.scan.getProperty("ID");
            } else {
                td2.innerHTML = "<input type='text' name='" + modality + "/ID' value=''/>";
            }
            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            if (this.scan.extension.XnatImagescandataId) {
                this.panel.method = 'PUT';
                this.panel.action = serverRoot + '/REST/experiments/' + this.sessionID + '/scans/' + this.scanID + '?req_format=form&XNAT_CSRF=' + csrfToken;
                td1.innerHTML += "<input type='hidden' name='" + modality + "/xnat_imageScanData_id' value='" + this.scan.extension.XnatImagescandataId + "'/>";
            } else {
                this.panel.method = 'POST';
                this.panel.action = serverRoot + '/REST/experiments/' + this.sessionID + '/scans?req_format=form&XNAT_CSRF=' + csrfToken;
            }

            //modality
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");

            td1.innerHTML = "Modality:";
            td1.align = "left";
            var modS = "<input type='hidden' name='ELEMENT_0' value='" + modality + "'/>";
            if (modality == "xnat:mrScanData") {
                td2.innerHTML = "MR" + modS;
            } else if (modality == "xnat:petScanData") {
                td2.innerHTML = "PET" + modS;
            } else if (modality == "xnat:ctScanData") {
                td2.innerHTML = "CT" + modS;
            } else {
                td2.innerHTML = modality + modS;
            }

            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            //type
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");

            td1.innerHTML = "Type:";
            td1.align = "left";

            var type_container = document.createElement('div');
            var nominalType = getNominalType(this.scan);
            if (!XNAT.app.sTMod && nominalType) {
                type_container.style.display = 'none';
                td2.innerHTML = nominalType;
            }
            td2.appendChild(type_container);

            this.type_input = document.createElement('input');
            this.type_input.type = 'text';
            this.type_input.id = 'type';
            this.type_input.size = '20';
            this.type_input.style.width = "180px";
            this.type_input.name = modality + "/type";
            if (nominalType) {
                this.type_input.value = nominalType;
            }
            type_container.appendChild(this.type_input);

            this.dToggler = document.createElement("span");
            this.dToggler.id = "toggleTypes";
            type_container.appendChild(this.dToggler);

            this.auto_type_div = document.createElement('div');
            this.auto_type_div.id = 'type_auto_div';
            type_container.appendChild(this.auto_type_div);

            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);


            this.oPushButtonD = new YAHOO.widget.Button({container: this.dToggler});
            this.dToggler.style.display = "none";

            this.initCallback = {
                success: this.loadedTypes,
                failure: this.initTypesFailure,
                cache: false, // Turn off caching for IE
                scope: this
            }
            if (this.options.project == undefined) {
                YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/REST/scan_types?XNAT_CSRF=' + window.csrfToken + '&format=json', this.initCallback, null, this);
            } else {
                YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/REST/projects/' + this.options.project + '/scan_types?XNAT_CSRF=' + window.csrfToken + '&format=json', this.initCallback, null, this);
            }

            //quality
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");

            td1.innerHTML = "Quality:";
            td1.align = "left";
            var sel = document.createElement("select");
            sel.name = modality + "/quality";
            sel.options[0] = new Option("(SELECT)", "");
            populateScanQualitySelector(serverRoot, window.projectScope, sel, 1, this.scan.extension.Quality);
            td2.appendChild(sel);
            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            //notes
            tr = document.createElement("tr");
            td1 = document.createElement("th");
            td2 = document.createElement("td");

            td1.innerHTML = "Notes:";
            td1.align = "left";
            if (this.scan.extension.Note != undefined && this.scan.extension.Note != null)
                td2.innerHTML = "<textarea class='nullable' cols='30' rows='4' name='" + modality + "/note'>" + this.scan.extension.Note + "</textarea>";
            else
                td2.innerHTML = "<textarea class='nullable' cols='30' rows='4' name='" + modality + "/note'></textarea>";

            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);

            this.panel.setBody(bd);

            this.panel.form = bd;
            this.panel.manager = this;

            var buttons = [
                {text: "Save", handler: {fn: function () {
                    var params = parseForm(this.form);
                    var callback = {
                        success: function () {
                            closeModalPanel("save_scan");
                            this.manager.onModification.fire();
                            this.cancel();
                        },
                        failure: function () {
                            if (!window.leaving) {
                                closeModalPanel("save_scan");
                                xmodal.message('Scan Data', 'Save failed!');
                            }
                            this.cancel();
                        },
                        cache: false, // Turn off caching for IE
                        scope: this
                    };
                    openModalPanel("save_scan", "Saving Scan.");
                    YAHOO.util.Connect.asyncRequest(this.method, this.action, callback, params);
                }}, isDefault: true},
                {text: "Cancel", handler: {fn: function () {
                    this.cancel();
                }}}
            ];
            this.panel.cfg.queueProperty("buttons", buttons);


            this.panel.render("page_body");

            this.panel.show();
        }
    }

	this.loadedTypes=function(o){
		this.list= eval("(" + o.responseText +")").ResultSet.Result;
		var oDS=new YAHOO.util.LocalDataSource(this.list);
	    oDS.responseSchema = {fields : ["type"]};

	    this.oAC= new YAHOO.widget.AutoComplete(this.type_input,this.auto_type_div,oDS,{maxResultsDisplayed:200});
	    this.oAC.prehighlightClassName = "yui-ac-prehighlight";
	    this.oAC.useShadow = true;
	    this.oAC.minQueryLength = 0;

		    if(this.list.length>0){
	       //show label button
	       var toggleD = function(e,obj1) {
	          if(!YAHOO.util.Dom.hasClass(obj1.dToggler, "open")) {
	             YAHOO.util.Dom.addClass(obj1.dToggler, "open")
	          }

	          // Is open
	          if(obj1.oAC.isContainerOpen()) {
	             obj1.oAC.collapseContainer();
	          }
	          else {
	             // Is closed
	             obj1.oAC.getInputEl().focus(); // Needed to keep widget active
	             setTimeout(function() { // For IE
	                 obj1.oAC.sendQuery("");
	             },0);
	          }
	       }
	       this.oPushButtonD.on("click", toggleD,this);
	       this.oAC.containerCollapseEvent.subscribe(function(){YAHOO.util.Dom.removeClass(this.dToggler, "open")});
	       this.dToggler.style.display="";
	    }else{
	       this.dToggler.style.display="none";
	    }
	}

	this.initTypesFailure=function(o){
        if (!window.leaving) {
            this.displayError("ERROR " + o.status+ ": Failed to load scan types list.");
        }
	};
}

function loadScans(session_id,project,tbody_id){
	this.initCallback={
		success:this.completeScanLoad,
		failure:function(o){
            if (!window.leaving) {
                closeModalPanel("scan_summary");
                this.displayError("ERROR " + o.status+ ": Failed to load scan list.");
            }
		},
        cache:false, // Turn off caching for IE
		arguments:{"session_id":session_id,"project":project,"tbody_id":tbody_id}
	}
	openModalPanel("scan_summary","Loading scan summary.");

	YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/experiments/' + sesion_id +'/scans?XNAT_CSRF=' + window.csrfToken + '&format=json',this.initCallback,null,this);
}

function completeScanLoad(obj1){
	closeModalPanel("scan_summary");
	var scans= eval("(" + obj1.responseText +")").ResultSet.Result;
	renderScans(scans,this.arguments.tbody_id,this.arguments.session_id,this.arguments.project);
}


function scanDeleteDialog(_options){
  this.onResponse=new YAHOO.util.CustomEvent("response",this);

	this.render=function(){
        if (showReason) {
            var height = "200px";
        }
        else {
            var height = "100px";
        }
		this.panel=new YAHOO.widget.Dialog("scanDeletionDialog",{close:true,
		   width:"400px",height:height,underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Scan Deletion Dialog");

		var bd = document.createElement("form");

		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);

		//delete files
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");

		td1.innerHTML="Delete associated files from the repository?:";
		td1.align="left";
		var sel = document.createElement("input");
		sel.type="checkbox";
		sel.checked=true;
		sel.defaultChecked=true;
		sel.id="delete_files";
		sel.name="delete_files";
		td2.appendChild(sel);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);
		

		//modality
        if (showReason) {
            tr=document.createElement("tr");
            td1=document.createElement("th");
            td2=document.createElement("td");

            td1.innerHTML="Justification:";
            td1.align="left";
            var sel = document.createElement("textarea");
            sel.cols="24";
            sel.rows="4";
            sel.id="event_reason";
            sel.name="event_reason";
            td2.appendChild(sel);
            tr.appendChild(td1);
            tr.appendChild(td2);
            tb.appendChild(tr);
        }
		this.panel.setBody(bd);

		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Delete",handler:{fn:function(){
				this.selector.delete_files = this.form.delete_files.checked;
				if(showReason && this.selector.event_reason==""){
                    xmodal.message('Delete Scan', 'Please enter a justification!');
					return;
				}
                else if (showReason) {
                    this.selector.event_reason = this.form.event_reason.value;
                }
				this.cancel();
				this.selector.onResponse.fire();
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);


		this.panel.render("page_body");
		this.panel.show();
	}
}

function scanDeletor(_options){
	this.options=_options;

	this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
	
	this.execute=function(){
		this.deleteDialog=new scanDeleteDialog();
		this.deleteDialog.onResponse.subscribe(function(){
			var delete_files=this.deleteDialog.delete_files;
			var event_reason=this.deleteDialog.event_reason;
			
			this.initCallback={
				success:function(obj1){
					closeModalPanel("delete_scan");
					this.onCompletion.fire();
                    window.location.reload();
					setTimeout(function(){window.location.reload()},2000);
					
				},
				failure:function(o){
                    if (!window.leaving) {
                        closeModalPanel("delete_scan");
                        this.displayError("ERROR " + o.status+ ": Failed to load scan list.");
                    }
				},
                cache:false, // Turn off caching for IE
				scope:this
				
				
			}
		
			var params="";
			if(delete_files){
				params+="&removeFiles=true";
			}
			
			params+="&event_reason="+event_reason;
			params+="&event_type=WEB_FORM";
			params+="&event_action=Removed scan";

			openModalPanel("delete_scan","Delete scan.");
			YAHOO.util.Connect.asyncRequest('DELETE',serverRoot +'/REST/experiments/' + this.options.session_id +'/scans/' + this.options.scan.getProperty("ID") +'?format=json&XNAT_CSRF=' + csrfToken+params,this.initCallback,null,this);
			
		},this,this);

		this.deleteDialog.render();
		
	}
}

function renderScans(scans,tbody_id,session_id,project){
	var tbody=document.getElementById(tbody_id);

	//clear contents - xdat.js
	emptyChildNodes(tbody);


	for(var scanC=0;scanC<scans.length;scanC++){
		var scan = scans[scanC];

		var tr = document.createElement("tr");

		var td= document.createElement("td");
		td.vAlign="middle";
		if(expandScanFunction!=undefined){
			var rA=document.createElement("a");
			var rIMG=document.createElement("img");
			rIMG.src=serverRoot+"/images/plus.jpg";
			rIMG.border=0;
			rA.appendChild(rIMG);
			rA.options={"tr":tr,"img":rIMG,"scan":scan,"session_id":session_id,"project":project,"tbody_id":tbody_id};
			rA.onclick=expandScanFunction;
			td.appendChild(rA);
			td.style.width="18px";
		}
		tr.appendChild(td);

		td= document.createElement("td");
		td.vAlign="middle";
		var eA=document.createElement("a");
		var eIMG=document.createElement("img");
		eIMG.src=serverRoot+"/images/e.gif";
		eIMG.border=0;
		eA.appendChild(eIMG);
		eA.options={"scan":scan,"session_id":session_id,"project":project,"tbody_id":tbody_id};
		eA.onclick=function(o){
		    window.scanEditor=new ScanEditor(this.options.session_id,this.options.scan.id,{project:this.options.project,tbody_id:this.options.tbody_id});
    		window.scanEditor.onModification.subscribe(function(o){
    			loadScans(this.sessionID,this.options.project,this.options.tbody_id);
    		},this);
    		window.scanEditor.init();
		}
		td.appendChild(eA);
		td.style.width="18px";
		tr.appendChild(td);

		td= document.createElement("td");
		td.vAlign="middle";
		var dA=document.createElement("a");
		var dIMG=document.createElement("img");
		dIMG.src=serverRoot+"/images/delete.gif";
		dIMG.border=0;
		dA.appendChild(dIMG);
		dA.options={"scan":scan,"session_id":session_id,"project":project,"tbody_id":tbody_id};
		dA.onclick=function(o){
		    var deletion=new scanDeletor(this.options);
    		deletion.onCompletion.subscribe(function(o){
    			loadScans(this.sessionID,this.options.project,this.options.tbody_id);
    		},this);
    		deletion.execute();
		}
		td.appendChild(dA);
		td.style.width="18px";
		tr.appendChild(td);

		//id
		td= document.createElement("td");
                if(scan.quality) {
                    td.className="quality-"+scan.quality
                }
                td.innerHTML=scan.id
		tr.appendChild(td);

		//type
		td= document.createElement("td");
		if(scan.type){
			td.innerHTML=scan.type;
		}
		tr.appendChild(td);

		if(window.fileCounter!=undefined){
			//file_count
			td= document.createElement("td");
			td.innerHTML="Loading...";
			tr.appendChild(td);
			window.fileCounter.collection.push({"uri":serverRoot + "/REST/experiments/" + session_id + "/scans/" + scan.id + "/files","div":td});
		}

		//note
		td= document.createElement("td");
		if(scan.note){
			td.innerHTML=scan.note;
		}
		tr.appendChild(td);

		tbody.appendChild(tr);
	}

	window.fileCounter.execute();
}

function ScanSet(_options,_scans){
	if(_scans!=undefined){
		this.scans=_scans;
	}else{
		this.scans=new Array();
	}

	this.new_scans=new Array();

	this.options=_options;
	if(this.options.session_id==undefined){
        xmodal.message('Scan Data Validation', 'Missing session_id');
	}

    this.onLoad=new YAHOO.util.CustomEvent("load",this);

    this.reload=function (){
		this.initCallback={
			success:this.completeScanLoad,
			failure:function(o){
                if (!window.leaving) {
                    closeModalPanel("scan_summary");
                    displayError("ERROR " + o.status+ ": Failed to load scan list.");
                }
			},
			arguments:{"session_id":this.options.session_id},
            cache:false, // Turn off caching for IE
			scope:this
		}
		if(this.options.msg!=undefined){
			openModalPanel("scan_summary",this.options.msg);
		}
		YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/experiments/' + this.options.session_id +'/?XNAT_CSRF=' + window.csrfToken + '&format=json&full=true',this.initCallback,null,this);
	}

	this.completeScanLoad=function (obj1){
		closeModalPanel("scan_summary");
		this.scans=new Array();
		var tempScans= eval("(" + obj1.responseText +")").ResultSet.Result;

  		if(window.classMapping==undefined)window.classMapping=new ClassMapping();

		for(var slC=0;slC<tempScans.length;slC++){
			var tempScan = window.classMapping.newInstance(tempScans[slC].xsiType);
		    tempScan.setProperty("ID",tempScans[slC].ID);
		    tempScan.setProperty("type",tempScans[slC].type);
		    tempScan.setProperty("quality",tempScans[slC].quality);
		    tempScan.setProperty("note",tempScans[slC].note);
		    tempScan.setProperty("startTime",tempScans[slC].extension.Starttime)
		    if(tempScans[slC].parameters_imagetype!=undefined)tempScan.setProperty("parameters/imageType",tempScans[slC].parameters_imagetype);
		    if(tempScans[slC].parameters_seqsequence!=undefined)tempScan.setProperty("parameters/scanSequence",tempScans[slC].parameters_scansequence);
		    if(tempScans[slC].parameters_seqvariant!=undefined)tempScan.setProperty("parameters/seqVariant",tempScans[slC].parameters_seqvariant);
		    if(tempScans[slC].parameters_scanoptions!=undefined)tempScan.setProperty("parameters/scanOptions",tempScans[slC].parameters_scanoptions);
		    if(tempScans[slC].parameters_acqtype!=undefined)tempScan.setProperty("parameters/acqType",tempScans[slC].parameters_acqtype);
		    if(tempScans[slC].parameters_flip!=undefined)tempScan.setProperty("parameters/flip",tempScans[slC].parameters_flip);
		    if(tempScans[slC].frames!=undefined)tempScan.setProperty("frames",tempScans[slC].frames);
		    if(tempScans[slC].series_description!=undefined)tempScan.setProperty("series_description",tempScans[slC].series_description);
		    if(tempScans[slC].stats!=undefined){
		       tempScan.stats=tempScans[slC].stats;
		    }
		    tempScan.setProperty("xnat_imageScanData_id",tempScans[slC].xnat_imagescandata_id);
		    this.scans.push(tempScan);
		}
		this.onLoad.fire();
	}

	this.getAllScans=function(){
		var temp_array = new Array();
		temp_array=temp_array.concat(this.scans);
		temp_array=temp_array.concat(this.new_scans);
		return temp_array;
	}

	this.validate=function(_focus){
		var isValid=true;
		for(var csC=0;csC<this.scans.length;csC++){
			var scan=this.scans[csC];
	  		if(scan.type_input.value==""){
	  			appendImage(scan.type_input,"/images/checkmarkRed.gif");
	  			if(_focus)scan.type_input.focus();
	  			isValid=false;
	  		}else{
  				removeAppendImage(scan.type_input);
	  		}


			if(XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true){
		  		if(scan.qual_input.selectedIndex==0){
		  			appendImage(scan.qual_input,"/images/checkmarkRed.gif");
		  			if(_focus)scan.qual_input.focus();
		  			isValid=false;
		  		}else{
	  				removeAppendImage(scan.qual_input);
		  		}
			}
		}

		for(var csC=0;csC<this.new_scans.length;csC++){
			var scan=this.new_scans[csC];
			if(scan.id_input.value==""){
		  		if(scan.type_input.value!="" || 
		  				((XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true) && (scan.qual_input.selectedIndex>0)) || 
		  				!((scan.note_input.value=="")||(scan.note_input.value=="NULL"))){
		  			removeAppendImage(scan.type_input);
		  			if(XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true)removeAppendImage(scan.qual_input);
		  			appendImage(scan.id_input,"/images/checkmarkRed.gif");
		  			if(_focus)scan.id_input.focus();
		  			isValid=false;
		  		}else{
		  			removeAppendImage(scan.id_input);
		  			removeAppendImage(scan.type_input);
		  			if(XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true)removeAppendImage(scan.qual_input);
		  		}
		  	}else{
		  		removeAppendImage(scan.id_input);
		  		if(scan.type_input.value==""){
		  			appendImage(scan.type_input,"/images/checkmarkRed.gif");
		  			if(_focus)scan.type_input.focus();
		  			isValid=false;
		  		}else{
	  				removeAppendImage(scan.type_input);
		  		}

		  		if(XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true){
			  		if(scan.qual_input.selectedIndex==0){
			  			scan.qual_input.selectedIndex=1;
			  			scan.qual_input.defaultSelectedIndex=1;
			  		}else{
		  				removeAppendImage(scan.qual_input);
			  		}
			  	}
		  	}
		}

		return isValid;
	}
}

function scanXPath(modality,countTable) {
  return "/scans/scan[" + countTable[modality] + "][@xsi:type=" + modality + "]";
}


function scanListingEditor(_tbody,_scanSet,_options){
	if(typeof _tbody == 'string'){
		this.tbody = (document.getElementById(_tbody));
	}else{
		this.tbody=_tbody;
	}

	this.scanSet=_scanSet;
	this.options=_options;

	this.render=function(){
	        //clear contents - xdat.js
		emptyChildNodes(this.tbody);

		var scan,tr,td,temp_scans;

		temp_scans=this.scanSet.getAllScans();

		var scanTypeTable = {};

		for(var scanC=0;scanC<temp_scans.length;scanC++){
		        scan = temp_scans[scanC];

			var modality=scan.xsiType;
			if (scanTypeTable[modality] === undefined) {
				scanTypeTable[modality] = 0;
			}
			else {
				scanTypeTable[modality]++;
			}

            var nominalType = getNominalType(scan);

			tr = document.createElement("tr");

			td= document.createElement("td");
			td.vAlign="middle";
			if(scan.extension.Id!=undefined){
				var dA=document.createElement("a");
				var dIMG=document.createElement("img");
				dIMG.src=serverRoot+"/images/plus.jpg";
				dIMG.id='IMG_scan'+scan.extension.Id;
				dIMG.border=0;
				dA.appendChild(dIMG);

				dA.options={"scan":temp_scans[scanC],"session_id":this.scanSet.options.session_id};
				dA.onclick=function(o){
					var id='_scan'+this.options.scan.extension.Id;
					var current = document.getElementById("span"+id).style.display;
					if (current == '')
					{
						document.getElementById("span"+id).style.display = 'none';
						document.images["IMG"+id].src= serverRoot+ "/images/plus.jpg";
					}else{
						document.getElementById("span"+id).style.display = '';
						document.images["IMG"+id].src= serverRoot+ "/images/minus.jpg";
					}

				}
				td.appendChild(dA);
				td.appendChild(document.createTextNode(" "));
			}
			if((XNAT.app.preventDataDeletion==undefined || !XNAT.app.preventDataDeletion) && (this.scanSet.options.allowDataDeletion==undefined || this.scanSet.options.allowDataDeletion=="true") && window.obj.canDelete){
				var dA=document.createElement("a");
				var dIMG=document.createElement("img");
				dIMG.src=serverRoot+"/images/delete.gif";
				dIMG.border=0;
				dA.appendChild(dIMG);

				dA.index=""+scanC;
				scan.index=dA.index;

				dA.scanSet=this.scanSet;
				dA.options={"scan":temp_scans[scanC],"session_id":this.scanSet.options.session_id};
				dA.onclick=function(o){
					if(this.options.scan.extension.XnatImagescandataId!=undefined){
					    var deletion=new scanDeletor(this.options);
			    		deletion.onCompletion.subscribe(function(o){
			    			this.scanSet.reload();
			    		},this,true);
			    		deletion.execute();
					}else{
						var index=-1;
						for(var nsC=0;nsC<this.scanSet.new_scans.length;nsC++){
							if(this.scanSet.new_scans[nsC]==this.options.scan)
							{
								index=nsC;
								break;
							}
						}
						this.scanSet.new_scans.splice(index,1);
						this.scanSet.onLoad.fire();
					}
				}
				td.appendChild(dA);
			}
			tr.appendChild(td);

			//id
			td= document.createElement("td");
			if(scan.id_input==undefined){
			  scan.id_input=document.createElement("input");

			  scan.id_input.manager=this;
			  scan.id_input.size='5';
			    if(scan.extension.Id!=undefined)scan.id_input.value=scan.extension.Id;
				if(scan.extension.XnatImagescandataId!=undefined){
					scan.id_input.type="hidden";
				}else{
					scan.id_input.type="text";
					//scan.id_input.onchange=this.scanSet.validate(false);
				}
			}
            scan.id_input.id=elementName + scanXPath(modality, scanTypeTable) + "/ID";
            scan.id_input.name=elementName + scanXPath(modality, scanTypeTable) + "/ID";
			if(scan.extension.XnatImagescandataId!=undefined)
				td.appendChild(document.createTextNode(scan.extension.Id))
			td.appendChild(scan.id_input);
			tr.appendChild(td);

			//type
			td= document.createElement("td");
			if(this.scanSet.options.types==undefined || this.scanSet.options.types[modality]==undefined || this.scanSet.options.types[modality].values.length<=1){
				//textbox
				scan.type_input = document.createElement('input');
				scan.type_input.type='text';
                scan.type_input.value = nominalType;
			}else if(scan.type_input==undefined){
				//select
				scan.type_input = document.createElement('select');
				scan.type_input.options[0]=new Option("(SELECT)","");

                if(nominalType) {
					var _stM=false;
                    for(var current = 0; current < this.scanSet.options.types[modality].values.length; current++) {
                        if(this.scanSet.options.types[modality].values[current].value == nominalType) {
							_stM=true;
						}						
					}
					
					if(!_stM){
                        var _tO = {};
                        _tO.value = nominalType;
                        _tO.display = nominalType;
						this.scanSet.options.types[modality].values.push(_tO);
					}					
				}
				
				for(var tC=0;tC<this.scanSet.options.types[modality].values.length;tC++){
					var type=this.scanSet.options.types[modality].values[tC];
                    scan.type_input.options[scan.type_input.options.length]=new Option(type.value,type.display, type.value == nominalType, type.value == nominalType);
                    if (type.value==nominalType) {
						scan.type_input.selectedIndex=(scan.type_input.options.length-1);
					}
				}
				if(this.scanSet.options.types[modality].uri!=undefined){
					scan.type_input.options[scan.type_input.options.length]=new Option("More","");
				}

				scan.type_input.options[scan.type_input.options.length]=new Option("Custom","");

				scan.type_input.modality=modality;
				scan.type_input.uri=this.scanSet.options.types[modality].uri;
				scan.type_input.typeManager=this.scanSet.options.types[modality];

                scan.type_input.onchange = function () {
					if(this.options[this.selectedIndex].text=="More"){
						if(window.scan_types==undefined){
                            window.scan_types={};
						}
						if(window.scan_types[this.modality]==undefined || window.scan_types[this.modality].values==undefined){
							 this.initCallback={
								success:function(obj){
                                    if(window.scan_types==undefined)window.scan_types={};
                                    if(window.scan_types[this.modality]==undefined)window.scan_types[this.modality]={};
									window.scan_types[this.modality].values= eval("(" + obj.responseText +")").ResultSet.Result;
									closeModalPanel("scan_type_loading");
									this.populateAll();
								},
								failure: function(obj){},
                                cache:false, // Turn off caching for IE
								scope:this
                            };
							openModalPanel("scan_type_loading","Loading Scan Types...");
							YAHOO.util.Connect.asyncRequest('GET',this.uri +'&XNAT_CSRF=' + window.csrfToken + '&format=json',this.initCallback,null,this);
						}else{
							this.populateAll();
						}
					}else if(this.options[this.selectedIndex].text=="Custom"){
						var creator=new scanTypeCreator({});
						creator.select=this;
						creator.modality=this.modality;
						creator.onResponse.subscribe(function(obj1,obj2){
							var new_type=this.new_scan_type;
                            if(window.scan_types==undefined)window.scan_types={};
                            if(window.scan_types[this.modality]==undefined)window.scan_types[this.modality]={};
                            if(window.scan_types[this.modality].custom==undefined)window.scan_types[this.modality].custom=[];
							window.scan_types[this.modality].custom.push(new_type);
							this.select.populateAll(null,new_type);
						},creator,true);
						creator.render();
					}
				}

				scan.type_input.populateAll=function(obj,_v){
					while(this.options.length>0){
						this.remove(0);
					}

					this.options[0]=new Option("(SELECT)","");

					if(window.scan_types[this.modality].custom!=undefined){
						for(var tC=0;tC<window.scan_types[this.modality].custom.length;tC++){
							var type=window.scan_types[this.modality].custom[tC];
							this.options[this.options.length]=new Option(type,type,(type==_v)?true:false,(type==_v)?true:false);
							if(type==_v){
								this.selectedIndex=(this.options.length-1);
							}
						}
					}

					if(window.scan_types[this.modality].values!=undefined){
						for(var tC=0;tC<window.scan_types[this.modality].values.length;tC++){
							var type=window.scan_types[this.modality].values[tC];
							this.options[this.options.length]=new Option(type.type,type.type);
						}
					}else{
						for(var tC=0;tC<this.typeManager.values.length;tC++){
							var type=this.typeManager.values[tC];
							this.options[this.options.length]=new Option(type.value,type.display);
						}
						if(this.typeManager.uri!=undefined){
							this.options[this.options.length]=new Option("More","");
						}
					}

					this.options[this.options.length]=new Option("Custom","");
				}
			}

            scan.type_input.style.width="180px";
            //scan.type_input.size="25";
            scan.type_input.id=elementName + scanXPath(modality, scanTypeTable) + "/type";
            scan.type_input.name=elementName +scanXPath(modality, scanTypeTable) + "/type";

            if(!XNAT.app.sTMod && nominalType){
                td.innerHTML=nominalType;
				var d=td.appendChild(document.createElement("div"));
				d.appendChild(scan.type_input);
				d.style.display='none';
			}else{
				td.appendChild(scan.type_input);
			}
			
			tr.appendChild(td);

			if(XNAT.app.concealScanUsability==undefined || XNAT.app.concealScanUsability!=true){
				//type
				td= document.createElement("td");
				if(scan.qual_input==undefined){
				    scan.qual_input=document.createElement("select");
				    scan.qual_input.options[0]=new Option("(SELECT)", "");
                    populateScanQualitySelector(serverRoot, window.projectScope, scan.qual_input, 1, scan.extension.Quality);
				}
				td.appendChild(scan.qual_input);
				tr.appendChild(td);
			}
            scan.qual_input.id=elementName + scanXPath(modality, scanTypeTable) + "/quality";
            scan.qual_input.name=elementName + scanXPath(modality, scanTypeTable) + "/quality";

			//nte
			td= document.createElement("td");
			if(scan.note_input==undefined){
				scan.note_input=document.createElement("input");
				scan.note_input.type="text";
			        if(scan.extension.Note!=undefined && scan.extension.Note != "NULL"){
					scan.note_input.value=scan.extension.Note;
				}
				scan.note_input.size="40";
			}
            scan.note_input.id=elementName + scanXPath(modality,scanTypeTable) + "/note";
            scan.note_input.name=elementName + scanXPath(modality,scanTypeTable) + "/note";
			scan.note_input.className += " nullable";
			td.appendChild(scan.note_input);
			tr.appendChild(td);

			if(scan.stats!=undefined && scan.stats!=""){
				//stats
				td= document.createElement("td");
				if(scan.stats_div==undefined){
					scan.stats_div=document.createElement("div");
					scan.stats_div.innerHTML=scan.stats;
				}
				td.appendChild(scan.stats_div);
				tr.appendChild(td);
			}

			this.tbody.appendChild(tr);

			if(scan.extension.Id!=undefined){
				tr = document.createElement("tr");
				tr.style.display="none";
				tr.id='span_scan'+scan.extension.Id;
				td= document.createElement("td");
				td.colSpan="5";
				td.vAlign="top";
				td.innerHTML="&nbsp;"


				var subtable=document.createElement("table");
				var subtbody=document.createElement("tbody");

				if(scan.extension.SeriesDescription!=undefined && scan.extension.SeriesDescription!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Description";
					subtd2.innerHTML=scan.extension.SeriesDescription;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.extension.Frames!=undefined && scan.extension.Frames!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Frames";
					subtd2.innerHTML=scan.extension.Frames;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.extension.Starttime!=undefined && scan.extension.Starttime!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Time";
					subtd2.innerHTML=scan.extension.Starttime;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_imagetype!=undefined && scan.Parameters_imagetype!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Image Type";
					subtd2.innerHTML=scan.Parameters_imagetype;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_scansequence!=undefined && scan.Parameters_scansequence!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="SEQ Seq";
					subtd2.innerHTML=scan.Parameters_scansequence;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_seqvariant!=undefined && scan.Parameters_seqvariant!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="SEQ Variant";
					subtd2.innerHTML=scan.Parameters_seqvariant;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_scanoptions!=undefined && scan.Parameters_scanoptions!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Options";
					subtd2.innerHTML=scan.Parameters_scanoptions;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_acqtype!=undefined && scan.Parameters_acqtype!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Acq Type";
					subtd2.innerHTML=scan.Parameters_acqtype;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				if(scan.Parameters_flip!=undefined && scan.Parameters_flip!=""){
					var subtr=document.createElement("tr");
					var subtd1=document.createElement("th");
					subtd1.align="left";
					var subtd2=document.createElement("td");

					subtd1.innerHTML="Flip";
					subtd2.innerHTML=scan.Parameters_flip;

					subtr.appendChild(subtd1);
					subtr.appendChild(subtd2);
					subtbody.appendChild(subtr);
				}

				subtable.appendChild(subtbody);
				td.appendChild(subtable);

				tr.appendChild(td);
				this.tbody.appendChild(tr);
			}
		}

	}
}

function scanTypeCreator(_options){
  	this.options=_options;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);

	this.render=function(){
		this.panel=new YAHOO.widget.Dialog("scanTypeDialog",{close:true,
		   width:"350px",height:"100px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Define New Scan Type");

		var bd = document.createElement("form");

		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);

		//modality
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");

		td1.innerHTML="Scan Type:";
		td1.align="left";
		var input = document.createElement("input");
		input.id="new_scan_type";
		input.name="new_scan_type";
		if(this.options.value!=undefined){
			input.value=this.options.value;
		}
		td2.appendChild(input);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);

		this.panel.setBody(bd);

		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Select",handler:{fn:function(){
				this.selector.new_scan_type = this.form.new_scan_type.value;
				this.cancel();
				this.selector.onResponse.fire();
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);


		this.panel.render("page_body");
		this.panel.show();
	}
}
