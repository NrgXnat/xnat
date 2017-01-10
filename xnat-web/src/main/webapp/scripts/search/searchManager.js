/*
 * web: searchManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");
dynamicJSLoad("SearchFieldManager","search/searchFieldManager.js");
var Dom = YAHOO.util.Dom,
   Event = YAHOO.util.Event,
    _addColW=800,
    _addColH=550,
    _addColBuffH=140,
    _addColBuff2H=160,
    _addColBuff=40,
    _joinW=750,
    _joinH=450;

function SearchXMLManager(_xml){
	this.fieldMap=new Object();
	this.versionMap=new Object();
	this.xml=_xml;

	this.sfm = new SearchFieldManager("search-field-manager");
	this.sfm.sm=this;

	this.onsubmit=new YAHOO.util.CustomEvent("submit",this);

	this.init=function(){
		if(this.xml!=undefined){
			try{
				var arr,src='',parser = new SAXDriver();
				var handler = new SAXEventHandler();
				parser.setDocumentHandler(handler);
			 	parser.setErrorHandler(handler);
				parser.setLexicalHandler(handler);
				parser.parse(this.xml);// start parsing
				this.searchDOM=handler.root;

			}catch(e){
				xmodal.message('Search Validation', "sxmlM:init:" +e.message);
			}
		}
	}

	this.renderCriteria=function(all_criteria_table){
	  if(all_criteria_table==undefined)all_criteria_table=document.getElementById("current_criteria");
	  try{

		all_criteria_table.innerHTML="";
	    all_criteria_table.style.border="solid thin #DEDEDE";
		all_criteria_table.style.marginTop="5pt";

			var si_t=document.createElement("table");
		si_t.style.width="95%";
			var si_tb=document.createElement("tbody");

			var si_tr=document.createElement("tr");
		var si_th1=document.createElement("th");
		si_th1.colSpan="3";
		si_th1.innerHTML="Current Filter(s) (Show me rows where...)";
		si_tr.appendChild(si_th1);
		si_tb.appendChild(si_tr);

		var si_tr=document.createElement("tr");
			var si_td1=document.createElement("td");
		si_td1.vAlign="top";
		si_td1.colSpan="3";
		if(this.searchDOM.SearchWhere.length==1){
			var tempT=this.searchDOM.SearchWhere[0].getInput(this,190);
			si_td1.appendChild(tempT);
		}else{
			for(var xswC=0;xswC<this.searchDOM.SearchWhere.length;xswC++){
				si_td1.appendChild(this.searchDOM.SearchWhere[xswC].getInput(this,190));
			}
		}


			si_tr.appendChild(si_td1);
			si_tb.appendChild(si_tr);
			si_t.appendChild(si_tb);
		all_criteria_table.appendChild(si_t);
	  }catch(o){
	  	xmodal.message('Search Validation', "sxmlM:renderCriteria:" +o.message);
	  }
	}

	this.renderFilterForm=function(element_name, field_id, oColumn){
		if(oColumn.currentValues==undefined){
			var fieldCallback={
				success:this.processValues,
				failure:this.valuesFailure,
                cache:false, // Turn off caching for IE
				scope:this,
				argument:{"element_name":element_name,"field_id":field_id,"oColumn":oColumn}
			}
            var longRunningOp = '';
            var pagnationText = $('.yui-pg-current').text();
            if(pagnationText){
                var beg = pagnationText.indexOf('(')+1;
                var end = pagnationText.indexOf('R')-1;
                var numRows = pagnationText.substring(beg, end);
                numRows = parseInt(numRows);
                if(numRows > 5000) {
                    longRunningOp = '<span style="color:red;">Note: This operation may take some time to complete due to the number of records being filtered.</span>';
                }
            }
            openModalPanel("load_cv","Loading column values.");
            if(longRunningOp){
                var throbber = $($('#load_cv').children()[1]);
                throbber.append(longRunningOp);
            }
			YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/search/' + this.tableName+'/'+oColumn.key +'?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime(),fieldCallback,null,this);
		}else{
			this.renderFilterForm2(element_name, field_id, oColumn);
		}
		}

	this.valuesFailure=function(o){
        if (!window.leaving) {
            closeModalPanel("load_cv");
            this.renderFilterForm2(o.argument.element_name,o.argument.field_id,o.argument.oColumn);
        }
    }

	this.processValues=function(o){
		closeModalPanel("load_cv");
		var resultset= eval("(" + o.responseText +")");
		o.argument.oColumn.currentValues=resultset.ResultSet.Result;
		this.renderFilterForm2(o.argument.element_name,o.argument.field_id,o.argument.oColumn);
			}

	this.destroy=function(){
		try{if(this.dialogPopup!=undefined)this.dialogPopup.destroy();}catch(o){}
		try{if(this.filterPopup!=undefined)this.filterPopup.destroy();}catch(o){}
		try{if(this.joinPopup!=undefined)this.joinPopup.destroy();}catch(o){}
		}

	this.renderFilterForm2=function(element_name, field_id, oColumn){
		var popupDIV = document.createElement("DIV");
		popupDIV.id="search-column-filter";
		var popupHD = document.createElement("DIV");
		popupHD.className="hd";
		popupDIV.appendChild(popupHD);
		var popupBD = document.createElement("DIV");
		popupBD.className="bd";

		popupBD.style.overflow="auto";
		popupBD.style.padding="10px";

		popupDIV.appendChild(popupBD);
		var filter= this.searchDOM.getColumnFilter(element_name,field_id);
		filter.oColumn=oColumn;
		filter.sm=this;
		filter.xcsContainer=document.createElement("div");

		if(this.searchDOM.SearchWhere[0].ChildSet.length>1){
			var title= document.createElement("div");
			title.innerHTML="Other Filters";
			title.style.fontWeight="700";
			popupBD.appendChild(title);

			for(var fcsC=0;fcsC<this.searchDOM.SearchWhere[0].ChildSet.length;fcsC++){
				var cs=this.searchDOM.SearchWhere[0].ChildSet[fcsC];
				if(!(cs===filter)){
					var of=document.createElement("div");
					of.innerHTML= (fcsC +1) + "." + cs.toString(this.searchDOM.RootElementName)
					popupBD.appendChild(of);
				}
			}
		}

		popupBD.appendChild(document.createElement("br"));

		var filterDIV=document.createElement("div");
		filterDIV.className="withColor withThinBorder";
		filterDIV.style.padding="3px";
		popupBD.appendChild(filterDIV);

		var title= document.createElement("div");
        var obj = window.available_elements.getByName(element_name)
        if(obj.singular){
          obj = obj.singular;
        } else{
          obj = 'Filter';
        }
		title.innerHTML=obj + " " + oColumn.header;
		title.style.fontWeight="700";
		filterDIV.appendChild(title);
        filterDIV.appendChild(document.createElement("br"));
		filterDIV.appendChild(filter.xcsContainer);
		filter.renderFilters();

		var popupFT = document.createElement("DIV");
		popupFT.className="ft";
		popupFT.style.height="20px";
		popupDIV.appendChild(popupFT);

		popupHD.innerHTML="Results Filter";

		var tp_fm=document.getElementById("tp_fm");
		tp_fm.appendChild(popupDIV);

		this.filterPopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,width:"700px",height:"500px",visible:false,fixedcenter:true,modal:true});
		this.filterPopup.sm=this;

	    var myButtons = [ { text:"Submit", handler:handleSubmit, isDefault:true },
						  { text:"Cancel", handler:handleCancel } ];
		this.filterPopup.cfg.queueProperty("buttons", myButtons);

		this.filterPopup.render();


		this.filterPopup.show();

		this.filterPopup.hideEvent.subscribe(function(obj1,obj2,obj3){
			this.filterPopup.destroy();
			this.searchDOM.cleanEmpty();
			YAHOO.util.Event.preventDefault(obj1);
		},this,this);


		this.resizerFilter=new YAHOO.util.Resize("search-column-filter",{
			handles: ['br'],
			autoRatio:false,
			minWidth:500,
			minHeight:300,
			status: false
		});
		this.resizerFilter.on('resize',function(args){
			var panelHeight=args.height;
			this.cfg.setProperty("height",panelHeight +"px");
		},this.filterPopup,true);

		}

	this.renderJoinForm=function(){
		var popupDIV = document.createElement("DIV");
		popupDIV.id="search_edit_popup";
		var popupHD = document.createElement("DIV");
		popupHD.className="hd";
		popupDIV.appendChild(popupHD);
		var popupBD = document.createElement("DIV");
		popupBD.className="bd";

		popupBD.style.overflow="auto";
		popupBD.style.padding="10px";

		popupDIV.appendChild(popupBD);


		var popupFT = document.createElement("DIV");
		popupFT.className="ft";
		popupFT.style.height="20px";
		popupDIV.appendChild(popupFT);

		popupHD.innerHTML="Join to other data";

	    var existingDIV=document.createElement("div");
	    existingDIV.style.border="solid thin #DEDEDE";
	    existingDIV.style.padding="3px";
	    existingDIV.style.overflow="auto";

		popupBD.appendChild(existingDIV);

		if(!this.searchDOM){
			this.searchDOM=new xdat_stored_search();
		}

	//BEGIN current fields section
		var all_fields_table = document.createElement("div");
		all_fields_table.id="current_fields";
		all_fields_table.style.marginTop="5pt";
		//all_fields_table.style.overflow="auto";

		existingDIV.appendChild(all_fields_table);

		var si_t=document.createElement("table");
		var si_tb=document.createElement("tbody");

		var joined=this.searchDOM.getJoinedDataTypes();

		if(joined.length>1){
		var si_tr=document.createElement("tr");
			var si_th1=document.createElement("td");
			si_th1.colSpan="4";
			si_th1.innerHTML="Your search of <b>" + window.available_elements.getByName(this.searchDOM.RootElementName).plural +"</b> has already been joined to: ";
			var jjcC=0;
			for(var jJC=0;jJC<joined.length;jJC++){
				if(joined[jJC]!=this.searchDOM.RootElementName){
					if(jjcC++>0)si_th1.innerHTML+=", ";
					si_th1.innerHTML+="<b>" + window.available_elements.getByName(joined[jJC]).plural + "</b>";
				}
			}
			si_th1.innerHTML+=".<br><br>";
		si_tr.appendChild(si_th1);
		si_tb.appendChild(si_tr);
		}

		var si_tr=document.createElement("tr");
		var si_th1=document.createElement("td");
		si_th1.colSpan="4";
		si_th1.innerHTML="Specify which data-type to join your <b>" + window.available_elements.getByName(this.searchDOM.RootElementName).plural +"</b> search results to.";
		si_tr.appendChild(si_th1);
		si_tb.appendChild(si_tr);

		var si_tr=document.createElement("tr");
		var si_td1=document.createElement("td");
		si_td1.vAlign="top";
		var si_td2=document.createElement("td");
		si_td2.vAlign="top";
		var si_td3=document.createElement("td");
		si_td3.vAlign="top";
		var si_td4=document.createElement("td");
		si_td4.vAlign="top";

		si_td1.innerHTML="New Data Type:";
		this.newDataTypeSelector=si_td2.appendChild(document.createElement("select"));
		this.newDataTypeSelector.options[0]=new Option("SELECT","",true,true);
		for(var aeC=0;aeC<window.available_elements.length;aeC++){
			if(!joined.contains(window.available_elements[aeC].element_name))
				this.newDataTypeSelector.options[this.newDataTypeSelector.options.length]=new Option(window.available_elements[aeC].plural,window.available_elements[aeC].element_name);
					}
		YAHOO.util.Event.addListener(this.newDataTypeSelector,"change",function(e,sm){
			var element_name=this.options[this.selectedIndex].value;
			if(element_name!=""){
				if(sm.versionMap[element_name]==undefined){
					var fieldCallback={
						success:function(o){
							closeModalPanel("load_fields");
							var resultset= eval("(" + o.responseText +")");
							this.versionMap[o.argument]=resultset.ResultSet.versions.DisplayVersions.versions;
							this.fieldMap[o.argument]= resultset.ResultSet.Result;

							this.fillVersionBox(o.argument);
						},
						failure: function(o) {
                            if (!window.leaving) {
                                xmodal.message('Search Validation', "Failed to load fields for " + o.argument.element_name);
                            }
                        },
                        cache:false, // Turn off caching for IE
						scope:sm,
						argument:element_name
				}
					openModalPanel("load_fields","Loading data-type information.");
					YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/search/elements/'+element_name +'?XNAT_CSRF=' + window.csrfToken + '&format=json',fieldCallback,null,this);
				}else{
					sm.fillVersionBox(element_name);
			}
		}
		},this);

		this.versionSelector=si_td3.appendChild(document.createElement("select"));
		this.versionSelector.disabled=true;

		this.versionButton=document.createElement("input");
		this.versionButton.type="button";
		this.versionButton.disabled=true;
		this.versionButton.value="Add";
		YAHOO.util.Event.addListener(this.versionButton,"click",function(e){
			var element_name=this.newDataTypeSelector.options[this.newDataTypeSelector.selectedIndex].value;
			if(element_name!=""){
				var version=this.versionSelector.options[this.versionSelector.selectedIndex].version;
				for(var lvfC=0;lvfC<version.fields.length;lvfC++){
					var tF=version.fields[lvfC];
					var xsf=new xdat_search_field();
		            xsf.ElementName=tF.element_name;
		            xsf.FieldId=tF.id;
			        if(tF.header!=undefined && tF.header!=null){
					     xsf.Header=tF.header;
				}else{
					     xsf.Header=tF.id;
			}
		            this.searchDOM.appendField(xsf);
		}
				this.onsubmit.fire(this.searchDOM);
		}
		},this,true);
		si_td4.appendChild(this.versionButton);

		si_tr.appendChild(si_td1);
		si_tr.appendChild(si_td2);
		si_tr.appendChild(si_td3);
		si_tr.appendChild(si_td4);
		si_tb.appendChild(si_tr);
		si_t.appendChild(si_tb);
		all_fields_table.appendChild(si_t);


		//add to page
		var tp_fm=document.getElementById("tp_fm");
		tp_fm.appendChild(popupDIV);


		this.joinPopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,width:_joinW+"px",height:_joinH+"px",visible:false,fixedcenter:true,modal:true});
		this.joinPopup.sm=this;
		//this.renderFields();

	    var myButtons = [ { text:"Cancel", handler:handleCancel } ];
		this.joinPopup.cfg.queueProperty("buttons", myButtons);

		this.joinPopup.render();


		this.joinPopup.show();

		this.joinPopup.hideEvent.subscribe(function(obj1,obj2,obj3){
			this.joinPopup.destroy();
			YAHOO.util.Event.preventDefault(obj1);
		},this,this);


		this.resizer=new YAHOO.util.Resize("search_edit_popup",{
			handles: ['br'],
			autoRatio:false,
			minWidth:500,
			minHeight:300,
			status: false
		});
		this.resizer.on('resize',function(args){
			var panelHeight=args.height;
			this.joinPopup.cfg.setProperty("height",panelHeight +"px");
		},this,true);
	}

	this.fillVersionBox=function(element_name){
		var versions=this.versionMap[element_name];
		var allVersion=null;
		this.versionSelector.options.length=0;
		for(var lvC=0;lvC<versions.length;lvC++){
			var version=versions[lvC];
			if(version.name=="brief" || version.name=="detailed"){
				var opt=document.createElement("option");
				opt.label=version.name;
				opt.innerHTML=version.name;
				opt.e=element_name;
				opt.TYPE="GRP";
				opt.version=version;
			    this.versionSelector.appendChild(opt);
			}else if(version.name=="all"){
				allVersion=version;
		}
	}

		if(this.versionSelector.options.length==0 && allVersion!=null){
			var opt=document.createElement("option");
				opt.label="all";
				opt.innerHTML="all";
				opt.e=element_name;
				opt.TYPE="GRP";
				opt.version=version;
			    this.versionSelector.appendChild(opt);
		}
		this.versionSelector.disabled=false;
		this.versionButton.disabled=false;
		}

	this.renderAddFields=function(showCriteria){
		this.showCriteria=showCriteria;
		var popupDIV = document.createElement("DIV");
		popupDIV.id="search_edit_popup";
		var popupHD = document.createElement("DIV");
		popupHD.className="hd";
		popupDIV.appendChild(popupHD);
		var popupBD = document.createElement("DIV");
		popupBD.className="bd";

		popupBD.style.overflow="auto";
		popupBD.style.padding="10px";

		popupDIV.appendChild(popupBD);


		var popupFT = document.createElement("DIV");
		popupFT.className="ft";
		popupFT.style.height="20px";
		popupDIV.appendChild(popupFT);

		popupHD.innerHTML="Select columns";

	    var existingDIV=document.createElement("div");
	    existingDIV.style.border="solid thin #DEDEDE";
	    existingDIV.style.padding="3px";
	    existingDIV.style.overflow="auto";

		popupBD.appendChild(existingDIV);

		if(!this.searchDOM){
			this.searchDOM=new xdat_stored_search();
		}

	//BEGIN current fields section
		var all_fields_table = document.createElement("div");
		all_fields_table.id="current_fields";
		all_fields_table.style.marginTop="5pt";
		//all_fields_table.style.overflow="auto";

		existingDIV.appendChild(all_fields_table);

		var si_t=document.createElement("table");
        si_t.id="editColumnsTable";
		si_t.border=0;
        si_t.style.width="100%";
		var si_tb=document.createElement("tbody");

		var si_tr=document.createElement("tr");
		var si_th1=document.createElement("td");
		si_th1.colSpan="4";
		si_th1.innerHTML="Use the up/down arrows to reorder columns.  Use the left/right arrows to add and remove columns.";
		si_tr.appendChild(si_th1);
		si_tb.appendChild(si_tr);

		var si_tr=document.createElement("tr");
		var si_td1=document.createElement("td");
		si_td1.vAlign="top";
		var si_td2=document.createElement("td");
		si_td2.vAlign="top";
		var si_td3=document.createElement("td");
		si_td3.vAlign="top";
		var si_td4=document.createElement("td");
		si_td4.vAlign="top";


		var cfH=si_td2.appendChild(document.createElement("div"));
		cfH.innerHTML="Current Fields";
		cfH.align="center";
		cfH.style.fontStyle="italic";

		var cfDiv=document.createElement("div");
		cfDiv.id="current_fields_select";
        cfDiv.style.width="100%";
		si_td2.appendChild(cfDiv);

		this.cfS=document.createElement("select");
		this.cfS.multiple=true;
        this.cfS.style.maxWidth="none";
        this.cfS.style.width=(_addColW/2-_addColBuff)+"px";
		this.cfS.style.height=(_addColH-_addColBuff2H)+"px";
		this.cfS.deselect=function(){
			for(var dsC=0;dsC<this.options.length;dsC++){
				if(this.options[dsC].selected){
					this.options[dsC].selected=false;
			}
		}
		}
		cfDiv.appendChild(this.cfS);

		//si_tr.appendChild(si_td1);
		si_tr.appendChild(si_td1);
		si_tr.appendChild(si_td2);
		si_tr.appendChild(si_td3);
		si_tr.appendChild(si_td4);
		si_tb.appendChild(si_tr);
		si_t.appendChild(si_tb);
		all_fields_table.appendChild(si_t);
	//END current fields section

        var pfH=si_td4.appendChild(document.createElement("div"));
        pfH.innerHTML="Available Fields";
        pfH.align="center";
        pfH.style.fontStyle="italic";

		var pfDiv=document.createElement("div");
		pfDiv.id="potential_fields_select";
		si_td4.appendChild(pfDiv);

		this.afS=document.createElement("select");
		this.afS.multiple=true;
        this.afS.style.maxWidth="none";
		this.afS.style.width=(_addColW/2-_addColBuff)+"px";
		this.afS.style.height=(_addColH-_addColBuff2H)+"px";
		this.afS.opposite=this.cfS;
		this.afS.deselect=function(){
			for(var dsC=0;dsC<this.options.length;dsC++){
				if(this.options[dsC].selected){
					this.options[dsC].selected=false;
				}
			}
		}
		pfDiv.appendChild(this.afS);

		this.cfS.opposite=this.afS;

		//set left images
		si_td1.vAlign="middle";
		var uImg=si_td1.appendChild(document.createElement("img"));
		uImg.src=serverRoot +"/images/up18.gif";
		uImg.border="0";
		uImg.manager=this;
		uImg.onclick=function(){
			var selections=new Array();
			for(var cfSc=0;cfSc<this.manager.cfS.options.length;cfSc++){
				if(this.manager.cfS.options[cfSc].selected){
					if(cfSc>0){
						if(!this.manager.cfS.options[cfSc-1].selected){
							var tO=this.manager.currentFields.splice(cfSc,1)[0];
							this.manager.currentFields.splice(cfSc-1,0,tO);
							selections.push(cfSc-1);
							this.manager.cfS.options[cfSc-1].selected=true;
							this.manager.cfS.options[cfSc].selected=false;
							//var tO=this.manager.cfS.removeChild(this.manager.cfS.options[cfSc]);
							//this.manager.cfS.insertBefore(tO,this.manager.cfS.options[cfSc-1]);
						}else{
							selections.push(cfSc);
						}
					}else{
						selections.push(0);
					}
	}
			}
			this.manager.renderCurrentFieldsDT(selections);
		}
		uImg.style.cursor="pointer";

		si_td1.appendChild(document.createElement("br"));
		si_td1.appendChild(document.createElement("br"));
		si_td1.appendChild(document.createElement("br"));
		si_td1.appendChild(document.createElement("br"));

		var dImg=si_td1.appendChild(document.createElement("img"));
		dImg.src=serverRoot +"/images/down18.gif";
		dImg.border="0";
		dImg.select=this.cfS;
		dImg.manager=this;
		dImg.onclick=function(){
			var selections=new Array();
			for(var cfSc=this.select.options.length-1;cfSc>=0;cfSc--){
				if(this.select.options[cfSc].selected){
					if(cfSc<(this.select.options.length-1)){
						if(!this.select.options[cfSc+1].selected){
							var tO=this.manager.currentFields.splice(cfSc,1)[0];
							this.manager.currentFields.splice(cfSc+1,0,tO);
							selections.push(cfSc+1);
							this.manager.cfS.options[cfSc+1].selected=true;
							this.manager.cfS.options[cfSc].selected=false;
						}else{
							selections.push(cfSc);
						}
					}else{
						selections.push(cfSc);
					}
				}
			}
			this.manager.renderCurrentFieldsDT(selections);
		}
		dImg.style.cursor="pointer";

		//left-right buttons
		si_td3.vAlign="middle";
		var lImg=si_td3.appendChild(document.createElement("img"));
		lImg.src=serverRoot +"/images/left18.gif";
		lImg.border="0";
		lImg.cfS=this.cfS;
		lImg.afS=this.afS;
		lImg.manager=this;
		lImg.onclick=function(){
			for(var cfSc=0;cfSc<this.afS.options.length;cfSc++){
				if(this.afS.options[cfSc].selected){
					this.manager.currentFields.push({
						"ElementName":this.afS.options[cfSc].element_name,
	  					"FieldId":this.afS.options[cfSc].field_id,
	  					"Header":this.afS.options[cfSc].header,
	  					"Type":this.afS.options[cfSc].type});
                    for(var pfSc=0;pfSc<this.manager.pFs.length;pfSc++){
                        var pF = this.manager.pFs[pfSc];
                        if(pF.ELEMENT_NAME==this.afS.options[cfSc].element_name
                            && pF.FIELD_ID==this.afS.options[cfSc].field_id){
                            // ...then remove it from the potential fields column
                            this.manager.pFs.splice(pfSc,1);
                        }
                    }
				}
			}
			this.manager.renderCurrentFieldsDT();
			this.manager.renderPotentialFields();
		}
		lImg.style.cursor="pointer";

		si_td3.appendChild(document.createElement("br"));
		si_td3.appendChild(document.createElement("br"));
		si_td3.appendChild(document.createElement("br"));
		si_td3.appendChild(document.createElement("br"));

		var rImg=si_td3.appendChild(document.createElement("img"));
		rImg.src=serverRoot +"/images/right18.gif";
		rImg.border="0";
		rImg.cfS=this.cfS;
		rImg.afS=this.afS;
		rImg.manager=this;
		rImg.onclick=function(){
			for(var afSc=0;afSc<this.cfS.options.length;afSc++){
				if(this.cfS.options[afSc].selected){
					for(var cfSC=0;cfSC<this.manager.currentFields.length;cfSC++){
                        var cF = this.manager.currentFields[cfSC];
						if(cF.ElementName==this.cfS.options[afSc].element_name
                            && cF.FieldId==this.cfS.options[afSc].field_id){
                            // Add this field back to the potential fields column...
                            var pF = { // I seem to have no other choice but to recreate this object from the selected current field element.
                                       // I don't see it represented anywhere else in memory when debugging this. (Justin)
                                'DESC':cF.Header,
                                'ELEMENT_NAME':cF.ElementName,
                                'FIELD_ID':cF.FieldId,
                                'HEADER':cF.Header,
                                'SRC':'0',    // Not sure about what this value does exactly
                                'TYPE':cF.Type
                            };
                            this.manager.pFs.push(pF);
                            //...then remove it from the current fields column.
							this.manager.currentFields.splice(cfSC,1);
						}
					}
				}
			}
			this.manager.renderCurrentFieldsDT();
			this.manager.renderPotentialFields();
		}
		rImg.style.cursor="pointer";


		//add to page
		var tp_fm=document.getElementById("tp_fm");
		tp_fm.appendChild(popupDIV);

		this.dialogPopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,width:_addColW+"px",height:_addColH+"px",visible:false,fixedcenter:true,modal:true});
		this.dialogPopup.sm=this;
		//this.renderFields();

	    var myButtons = [ { text:"Submit", handler:handleACSubmit, isDefault:true },
						  { text:"Cancel", handler:handleCancel } ];
		this.dialogPopup.cfg.queueProperty("buttons", myButtons);

		this.dialogPopup.render();


		this.dialogPopup.show();

		this.dialogPopup.hideEvent.subscribe(function(obj1,obj2,obj3){
			this.dialogPopup.destroy();
			YAHOO.util.Event.preventDefault(obj1);
		},this,this);

		this.currentFields=new Array();
		for(var rAfC=0;rAfC<this.searchDOM.SearchField.length;rAfC++){
	  		this.currentFields[rAfC]={"ElementName":this.searchDOM.getSortedFields()[rAfC].ElementName,
	  		"FieldId":this.searchDOM.SearchField[rAfC].FieldId,
	  		"Header":this.searchDOM.SearchField[rAfC].Header,
	  		"Type":this.searchDOM.SearchField[rAfC].Type};
	  	}

		this.renderCurrentFieldsDT();

		this.resizer=new YAHOO.util.Resize("search_edit_popup",{
			handles: ['br'],
			autoRatio:false,
			minWidth:500,
			minHeight:300,
			status: false
		});
		this.resizer.on('resize',function(args){
			var panelHeight=args.height;
			this.dialogPopup.cfg.setProperty("height",panelHeight +"px");
            var colWidth = args.width/2;
            this.cfS.style.width=(colWidth-_addColBuff)+"px";
			this.afS.style.width=(colWidth-_addColBuff)+"px";
			this.cfS.style.height=(args.height-_addColBuff2H)+"px";
			this.afS.style.height=(args.height-_addColBuff2H)+"px";
		},this,true);


//		this.tabView=new FieldTabView("potential_fields_select",_addColH,this);
//	  	this.tabView.load(this.searchDOM.RootElementName);

	  	var ce=new Array();
	  	ce.push(this.searchDOM.RootElementName);
	  	for(var rAfC=0;rAfC<this.searchDOM.SearchField.length;rAfC++){
	  		if(!ce.contains(this.searchDOM.SearchField[rAfC].ElementName)){
	  			ce.push(this.searchDOM.SearchField[rAfC].ElementName);
	  		}
	  	}
	  	ce.toCommaString=function(){
	  		var _r="";
	  		for(var _rC=0;_rC<ce.length;_rC++){
	  			if(_rC>0)_r+=",";
	  			_r+=ce[_rC];
	  		}
	  		return _r;
	  	}

	  	var fcb={
			success:function(o){
				try{
					var resultset= eval("(" + o.responseText +")");
					this.pFs=resultset.ResultSet.Result;
					this.renderPotentialFields();
				}catch(e){
					xmodal.message('Search Validation', e.toString());
				}
			},
			failure: function(o) {
                if (!window.leaving) {
                    xmodal.message('Search Validation', "Failed to load available fields.");
                }
            },
            cache:false, // Turn off caching for IE
			scope:this
		}
		YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/search/elements/'+ ce.toCommaString() +'?XNAT_CSRF=' + window.csrfToken + '&format=json',fcb,null,this);
	}

	this.shouldShowLabels=false;

	this.colors=new Array();
	this.gbc=function(en){
		var tEN=window.available_elements.getByName(en);
		if(tEN!=null){
			if(tEN.lbg!=""){
				return tEN.lbg;
			}
		}
		return "#FFFFFF";
	}

	this.renderPotentialFields=function(){
		while(this.afS.options.length>0){this.afS.removeChild(this.afS.options[0])};

		for(var _efC=0;_efC<this.pFs.length;_efC++){
			if(this.shouldShowLabels || this.pFs[_efC].SRC!=2){
				if(this.pFs[_efC].d==undefined){
					this.pFs[_efC].d=this.pFs[_efC].DESC + " (" + window.available_elements.getByName(this.pFs[_efC].ELEMENT_NAME).singular + ")";
				}
				if(!this.containsField(this.pFs[_efC].ELEMENT_NAME,this.pFs[_efC].FIELD_ID)){
					var tO=new Option(this.pFs[_efC].d);
					tO.element_name=this.pFs[_efC].ELEMENT_NAME;
					tO.field_id=this.pFs[_efC].FIELD_ID;
					tO.header=this.pFs[_efC].HEADER;
					tO.type=this.pFs[_efC].TYPE;
					tO.style.backgroundColor=this.gbc(this.pFs[_efC].ELEMENT_NAME);
					this.afS.options[this.afS.options.length]=tO;
				}
			}
		}
	}

	this.containsField=function(e,f){
		for(var cfSC=0;cfSC<this.currentFields.length;cfSC++){
			if(this.currentFields[cfSC].ElementName==e &&
			 this.currentFields[cfSC].FieldId==f){
				return true;
			}
		}
		return false;
	}

	this.renderCurrentFieldsDT=function(sel){
		while(this.cfS.options.length>0){this.cfS.removeChild(this.cfS.options[0])};

		for(var cfSC=0;cfSC<this.currentFields.length;cfSC++){
			if(this.currentFields[cfSC].d==undefined){
				this.currentFields[cfSC].d=this.currentFields[cfSC].Header + " (" + window.available_elements.getByName(this.currentFields[cfSC].ElementName).singular + ")";
			}
			this.cfS.options[cfSC]=new Option(this.currentFields[cfSC].d);
			this.cfS.options[cfSC].element_name=this.currentFields[cfSC].ElementName;
			this.cfS.options[cfSC].field_id=this.currentFields[cfSC].FieldId;
			this.cfS.options[cfSC].header=this.currentFields[cfSC].Header;
			this.cfS.options[cfSC].style.backgroundColor=this.gbc(this.currentFields[cfSC].ElementName);
			if(sel!=undefined && sel.contains(cfSC))this.cfS.options[cfSC].selected=true;
		}
	}
}

var handleCancel = function() {
	this.sm.init();
	this.destroy();
}

var handleACSubmit = function() {
	this.sm.searchDOM.removeAllFields();
	for(var cfSC=0;cfSC<this.sm.currentFields.length;cfSC++){
		this.sm.searchDOM.addField(this.sm.currentFields[cfSC].ElementName,
			this.sm.currentFields[cfSC].FieldId,
			this.sm.currentFields[cfSC].Header,
			this.sm.currentFields[cfSC].Type);
	}
	this.sm.onsubmit.fire(this.sm.searchDOM);
}

var handleSubmit = function() {
	for(var xswC=0;xswC<this.sm.searchDOM.SearchWhere.length;xswC++){
    var sm = this.sm;
		if(sm.searchDOM.SearchWhere[xswC].needsSubmit(true)){
      xmodal.confirm({
        content: 'You have new filters which have not been added.<br/><br/>Are you sure you want to continue without adding them (they will be lost)?',
        okAction: function(){
          sm.searchDOM.cleanEmpty();
          sm.onsubmit.fire(sm.searchDOM);
        },
        cancelAction: function(){}
      });
      return false;
/*    // Leaving this here as an example to reference for future confirm dialog conversions...
			if(!confirm("You have new filters which have not been added.  Are you sure you want to continue without adding them (they will be lost)?")){
				return; // Cancel
			}else{
				break; // Ok
			}
*/
		} else {
      sm.searchDOM.cleanEmpty();
      sm.onsubmit.fire(sm.searchDOM);
    }
	}
}

//modifications to stored-search
xdat_stored_search.prototype.removeField=function(name,field){
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(sf.FieldId==field && sf.ElementName==name){
			this.SearchField.splice(sfC,1);
			break;
		}
	}
}
xdat_stored_search.prototype.removeAllFields=function(){
	while(this.SearchField.length>0){
		this.SearchField.splice(0,1);
	}
}

//modifications to stored-search
xdat_stored_search.prototype.getJoinedDataTypes=function(){
	var allDTs=new Array();
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(!allDTs.contains(sf.ElementName)){
			allDTs.push(sf.ElementName);
		}
	}
	return allDTs;
}

xdat_stored_search.prototype.hideField=function(name,field){
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(sf.FieldId==field && sf.ElementName==name){
			sf.Visible=false;
			break;
		}
	}
}

xdat_stored_search.prototype.setFieldSequence=function(name,field,index){
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(sf.FieldId==field && sf.ElementName==name){
			sf.Sequence=index;
			break;
		}
	}
}

xdat_stored_search.prototype.addField=function(element_name,field_id,header,type){
	for(var sfAFC=0;sfAFC<this.SearchField.length;sfAFC++){
		var sf=this.SearchField[sfAFC];
		if(sf.ElementName==element_name && sf.FieldId==field_id){
			return;
		}
	}
	var xsf=new xdat_search_field();
    xsf.ElementName=element_name;
    xsf.FieldId=field_id;
    xsf.Header=header;
    xsf.Type=type;

	this.appendField(xsf);
}

xdat_stored_search.prototype.appendField=function(newField){
	var topSequence=0;
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(sf.Sequence!=undefined && parseInt(sf.Sequence)>topSequence){
			topSequence=parseInt(sf.Sequence);
		}
	}
	newField.Sequence=topSequence+1;
	this.addSearchField(newField);
}

xdat_stored_search.prototype.getFieldHeader=function(str){
	for(var sfC=0;sfC<this.SearchField.length;sfC++){
		var sf=this.SearchField[sfC];
		if(str.startsWith(sf.ElementName) && str.toLowerCase().indexOf(sf.FieldId.toLowerCase())>0){
			return (sf.Header=='')?sf.FieldId:sf.Header;
		}
	}

	return removeElementName(str,this.RootElementName);
}

xdat_stored_search.prototype.getSortedFields=function(){
	return this.SearchField.sort(function(a,b){
		if(a.Sequence==undefined && b.Sequence==undefined){
			return 0;
		}else if(a.Sequence==undefined){
			return 1;
		}else if(b.Sequence==undefined){
			return -1;
		}else{
			return (parseInt(a.Sequence)-parseInt(b.Sequence));
		}
	});
}


xdat_stored_search.prototype.cleanEmpty=function(){
	var rev=new Array();
	for(this.xcsC=0;this.xcsC<this.SearchWhere.length;this.xcsC++){
		this.SearchWhere[this.xcsC].cleanEmpty();
		if(this.SearchWhere[this.xcsC].Criteria.length==0 && this.SearchWhere[this.xcsC].ChildSet.length==0){
			rev.push(this.xcsC);
		}
	}
	for(var revC=0;revC<rev.length;revC++){
		var ind=rev.reverse()[revC];
		this.SearchWhere.splice(ind,1);
	}
}

xdat_criteria_set.prototype.cleanEmpty=function(sm,cC){
	var rev=new Array();
	var revCrit=new Array();

	for(var xcC=0;xcC<this.Criteria.length;xcC++){
		if(this.Criteria[xcC].Value==null && this.Criteria[xcC].CustomSearch==null){
			revCrit.push(xcC)
		}
	}

	for(var revCritC=0;revCritC<revCrit.length;revCritC++){
		var ind=rev.reverse()[revCritC];
		this.Criteria.splice(ind,1);
	}

	for(this.xcsC=0;this.xcsC<this.ChildSet.length;this.xcsC++){
		this.ChildSet[this.xcsC].cleanEmpty();
		if(this.ChildSet[this.xcsC].Criteria.length==0 && this.ChildSet[this.xcsC].ChildSet.length==0){
			rev.push(this.xcsC);
		}
	}
	for(var revC=0;revC<rev.length;revC++){
		var ind=rev.reverse()[revC];
		this.ChildSet.splice(ind,1);
	}
}

xdat_criteria_set.prototype.renderFilters=function(containerDIV){
    var datetime_local = "datetime-local";
	emptyChildNodes(this.xcsContainer);

	var t=document.createElement("table");
	var tbody=document.createElement("tbody");
	t.appendChild(tbody);
	this.xcsContainer.appendChild(t);

	var tr=document.createElement("tr");
	var td1=document.createElement("td");
	var td2=document.createElement("td");
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);

	for(this.xcC=0;this.xcC<this.Criteria.length;this.xcC++){
		if(this.xcsC>0 || this.xcC>0){
			this.meth=document.createElement("div");
			this.meth.innerHTML=this.Method;
			td1.appendChild(this.meth);
		}

		this.CritDiv=document.createElement("div");

		this.CritDiv.appendChild(document.createElement("a"));
		this.CritDiv.childNodes[0].set=this;
		this.CritDiv.childNodes[0].index=""+this.xcC;
		this.CritDiv.childNodes[0].onclick=function(){
			this.set.Criteria.splice(parseInt(this.index),1);
			this.set.renderFilters();
		}
		this.CritDiv.childNodes[0].appendChild(document.createElement("img"));
		this.CritDiv.childNodes[0].childNodes[0].src=serverRoot +"/images/delete.gif";
		this.CritDiv.childNodes[0].childNodes[0].border="0";
		this.CritDiv.childNodes[0].childNodes[0].style.height="12px";
		this.CritDiv.appendChild(this.Criteria[this.xcC].getInput(this.sm));
		td1.appendChild(this.CritDiv);

	}
	if(this.xcsC>0 || this.xcC>0){
		this.meth=document.createElement("div");
		this.meth.innerHTML=this.Method;
		td1.appendChild(this.meth);
	}
	//new criteria
	this.CritDiv=document.createElement("div");
	td1.appendChild(this.CritDiv);

  var column = this.oColumn;
	this.newComparisonBox=document.createElement("select");
	this.newComparisonBox.options[0]=new Option("SELECT","",true,true);
	this.newComparisonBox.options[1]=new Option("=","=");
	if(column.type=="string"){
		this.newComparisonBox.options[2]=new Option("LIKE","LIKE");
	}else if(column.type=="float" || column.type=="integer")
	{
		this.newComparisonBox.options[2]=new Option(">",">");
		this.newComparisonBox.options[3]=new Option(">=",">=");
		this.newComparisonBox.options[4]=new Option("<","<");
		this.newComparisonBox.options[5]=new Option("<=","<=");
    this.newComparisonBox.options[6]=new Option("!=","!=");
    this.newComparisonBox.options[7]=new Option("IN","IN");
	}else if(column.type=="date"){
    this.newComparisonBox.options[1]=new Option(">",">");
		this.newComparisonBox.options[2]=new Option(">=",">=");
		this.newComparisonBox.options[3]=new Option("<","<");
		this.newComparisonBox.options[4]=new Option("<=","<=");
  }else{
		this.newComparisonBox.options[2]=new Option(">",">");
		this.newComparisonBox.options[3]=new Option(">=",">=");
		this.newComparisonBox.options[4]=new Option("<","<");
		this.newComparisonBox.options[5]=new Option("<=","<=");
		this.newComparisonBox.options[6]=new Option("LIKE","LIKE");
    this.newComparisonBox.options[7]=new Option("!=","!=");
    this.newComparisonBox.options[8]=new Option("IN","IN");
	}
	this.newComparisonBox.options[this.newComparisonBox.options.length]=new Option("BETWEEN","BETWEEN");
	this.newComparisonBox.options[this.newComparisonBox.options.length]=new Option("IS NULL","IS NULL");
	this.newComparisonBox.options[this.newComparisonBox.options.length]=new Option("IS NOT NULL","IS NOT NULL");

	this.newComparisonBox.set=this;
	this.newComparisonBox.onchange=function(){
		this.set.newTip.innerHTML="";
		if(this.selectedIndex>0){
			this.selected_value=this.options[this.selectedIndex].value;
			if(this.selected_value=="IS NOT NULL" || this.selected_value=="IS NULL"){
				this.set.newValueBox.style.display="none";
				this.set.newValueBox.disabled=true;
				this.set.newValueBox.value="";
        this.set.newValueBoxEndSpan.style.display="none";
        this.set.newValueBoxEnd.disabled=true;
        this.set.newValueBoxEnd.value="";
				this.set.newValueSelectBox.style.display="none";
				this.set.newValueSelectBox.disabled=true;
			}else if(this.selected_value=="="){
      
        this.set.newValueBox.value="";
        if(column.currentValues.length < 10000){
          this.set.newValueBox.style.display="none";
          this.set.newValueBox.disabled=true;
          this.set.newValueSelectBox.style.display="inline";
          this.set.newValueSelectBox.disabled=false;
        } else {
          this.set.newValueBox.style.display="inline";
          this.set.newValueBox.disabled=false;
          this.set.newValueSelectBox.style.display="none";
          this.set.newValueSelectBox.disabled=true;
        }
        this.set.newValueBoxEndSpan.style.display="none";
        this.set.newValueBoxEnd.disabled=true;
        this.set.newValueBoxEnd.value="";

			}else if(column.type=="date"){
				this.set.newValueBox.style.display="inline";
				this.set.newValueBox.disabled=false;
				this.set.newValueBox.type=datetime_local;
        this.set.newValueBox.placeholder="MM/DD/YYYY";
        this.set.newValueBoxEndSpan.style.display="none";
        this.set.newValueBoxEnd.disabled=true;
        this.set.newValueBoxEnd.value="";
				this.set.newValueSelectBox.style.display="none";
				this.set.newValueSelectBox.disabled=true;
      }else{
				this.set.newValueBox.style.display="inline";
				this.set.newValueBox.disabled=false;
        this.set.newValueBoxEndSpan.style.display="none";
        this.set.newValueBoxEnd.disabled=true;
        this.set.newValueBoxEnd.value="";
        if(this.set.newValueSelectBox){
          this.set.newValueSelectBox.style.display="none";
          this.set.newValueSelectBox.disabled=true;
        }
			}
			if(this.selected_value=="BETWEEN"){
        if(column.type=="date"){
          this.set.newValueBoxEndSpan.style.display="inline";
          this.set.newValueBoxEnd.disabled=false;
          this.set.newValueBoxEnd.type=datetime_local;
          this.set.newValueBoxEnd.placeholder="MM/DD/YYYY";
        }else{
          this.set.newTip.innerHTML="Separate values with an AND (i.e. 5 AND 7).";
        }
			}else if(this.selected_value=="IN"){
				this.set.newTip.innerHTML="Provide a comma separated list of values (i.e. John,Jane).";
			}
		}else{
			this.set.newValueBox.style.display="none";
			this.set.newValueBox.disabled=true;
			this.set.newValueBox.value="";
      this.set.newValueBoxEndSpan.style.display="none";
      this.set.newValueBoxEnd.disabled=true;
      this.set.newValueBoxEnd.value="";
			this.set.newValueSelectBox.style.display="none";
			this.set.newValueSelectBox.disabled=true;
		}
	}
	this.newComparisonBox.clear=function(){
		while (this.options.length > 0) {
		    this.options[0] = null;
		}
		this.disabled=true;
		this.set.newValueBox.value="";
		this.set.newValueBox.disabled=true;
		this.set.newValueBox.style.display="none";
    this.set.newValueBoxEndSpan.style.display="none";
    this.set.newValueBoxEnd.disabled=true;
    this.set.newValueBoxEnd.value="";
		this.set.newValueSelectBox.disabled=true;
		this.set.newValueSelectBox.style.display="none";
		this.set.newTip.innerHTML="";
	}
	this.CritDiv.appendChild(this.newComparisonBox);
	this.CritDiv.appendChild(document.createTextNode(" "));

	this.newValueBox=document.createElement("input");
	this.newValueBox.type="text";
	this.newValueBox.disabled=true;
	this.newValueBox.style.display="none";
    this.newValueBox.maxLength="100";
	this.CritDiv.appendChild(this.newValueBox);

  this.newValueBoxEndSpan=document.createElement("span");
	this.newValueBoxEndSpan.style.display="none";
  this.newValueBoxEndSpan.innerHTML=" AND ";
	this.newValueBoxEnd=document.createElement("input");
    try{
        this.set.newValueBox.type=datetime_local;
    } catch(e){
        datetime_local = "text";
    }
	this.newValueBoxEnd.type=datetime_local;
	this.newValueBoxEnd.disabled=true;
  this.newValueBoxEnd.maxLength="100";
  this.newValueBoxEndSpan.appendChild(this.newValueBoxEnd);
	this.CritDiv.appendChild(this.newValueBoxEndSpan);

  if(column.currentValues.length < 10000){
    this.newValueSelectBox=document.createElement("select");
    this.newValueSelectBox.style.display="none";
    this.newValueSelectBox.disabled=true;
    // don't populate this select for dates/timestamps because there are too many values and it will crash the browser.
    if(column.type!="date"){
      this.newValueSelectBox.options[0]=new Option("SELECT","",false);
      for(var occvC=0;occvC<column.currentValues.length;occvC++){
          this.newValueSelectBox.options[this.newValueSelectBox.options.length]=new Option(column.currentValues[occvC].values.split(/<[^<>]*>/g).join('') + " (" +column.currentValues[occvC].count +")",column.currentValues[occvC].values);
      }
    }
    this.CritDiv.appendChild(this.newValueSelectBox);
  }
	this.CritDiv.appendChild(document.createTextNode(" "));

	this.newButton=document.createElement("input");
	this.newButton.type="button";
	this.newButton.value="More...";
	this.newButton.set=this;
	this.newButton.onclick=function(){
		if(this.set.isValid()){
			this.set.save();
			this.set.renderFilters();
		}
	}
	this.CritDiv.appendChild(this.newButton);

	this.newTip=document.createElement("display");
	this.newTip.style.display="inline";
	this.newTip.className="filter_tip"
	this.CritDiv.appendChild(this.newTip);

	return this.xcsContainer;
}

xdat_criteria_set.prototype.isValid=function(){
	if(this.newComparisonBox.selectedIndex==0){
		xmodal.message('Search Validation', "Please select a Comparison for the additional criteria.");
		this.newComparisonBox.disabled=false;
		this.newComparisonBox.focus();
			return false;
	}

	var c=this.newComparisonBox.options[this.newComparisonBox.selectedIndex].value;
	if(c=="IS NULL" || c=="IS NOT NULL"){
	}else if(c=="="){
    if(this.oColumn.currentValues.length < 10000){
      if(this.newValueSelectBox.selectedIndex==0){
              xmodal.message('Search Validation', "Please specify a value to match against.");
        this.newValueSelectBox.disabled=false;
        this.newValueSelectBox.focus();
        return false;
      }
    } else {
      if(this.newValueBox.value.length == 0){
              xmodal.message('Search Validation', "Please specify a value to match against.");
        this.newValueBox.disabled=false;
        this.newValueBox.focus();
        return false;
      }
    }
	}else if(c=="BETWEEN"){
		if(this.newValueBox.value==""){
            xmodal.message('Search Validation', "Please specify a value to match against.");
			this.newValueBox.disabled=false;
			this.newValueBox.focus();
			return false;
		}
    if(this.oColumn.type=="date"){
      if(this.newValueBoxEnd.value==""){
              xmodal.message('Search Validation', "Please specify an end value to match against.");
        this.newValueBoxEnd.disabled=false;
        this.newValueBoxEnd.focus();
        return false;
      }
    } else if(this.newValueBox.value.indexOf(" AND ")<1){
            xmodal.message('Search Validation', "Please separate values using an AND.  (i.e. 5 AND 10)");
			this.newValueBox.disabled=false;
			this.newValueBox.focus();
			return false;
		}
	}else{
		if(this.newValueBox.value==""){
            xmodal.message('Search Validation', "Please specify a value to match against.");
			this.newValueBox.disabled=false;
			this.newValueBox.focus();
			return false;
		}
	}
	return true;
}

xdat_criteria_set.prototype.save=function(){
	var e=(this.newElementBox==undefined)?this.element_name:this.newElementBox.options[this.newElementBox.selectedIndex].value;
	var f=(this.newFieldBox==undefined)?this.field_id:this.newFieldBox.options[this.newFieldBox.selectedIndex].value;
	var fTYPE=(this.newFieldBox==undefined)?this.oColumn.type:this.newFieldBox.options[this.newFieldBox.selectedIndex].TYPE;
	var c=this.newComparisonBox.options[this.newComparisonBox.selectedIndex].value;
	var v=this.newValueBox.value;
	if(this.newValueBox.disabled && !this.newValueSelectBox.disabled){
		v=this.newValueSelectBox.options[this.newValueSelectBox.selectedIndex].value;
	}
    if(fTYPE=="date"){
      v = v.replace(/T/g, ' ');
      if(c=="BETWEEN"){
        var ve = this.newValueBoxEnd.value;
        ve = ve.replace(/T/g, ' ');
        v += " AND "+ve;
      }
    }
	var newC=new xdat_criteria();
	newC.setSchemaField(e + "." + f);
	if(c=="IS NULL"){
		newC.setComparisonType("IS");
		newC.setValue("NULL");
	}else if(c=="IS NOT NULL"){
		newC.setComparisonType("IS NOT");
		newC.setValue("NULL");
	}else if(c=="LIKE"){
		newC.setComparisonType(c);
		if(v.indexOf("%")==-1){
			v="%"+v+"%";
		}
		newC.setValue(v);
	}else{
		newC.setComparisonType(c);
		newC.setValue(v);
	}
	this.addCriteria(newC);
	this.newElementBox=undefined;
	this.newFieldBox=undefined;
	this.newComparisonBox=undefined;
	this.newValueBox=undefined;
	this.newValueSelectBox=undefined;
	return true;
}

xdat_criteria_set.prototype.getInput=function(sm,cC){
	this.t=document.createElement("table");
	this.t.style.backgroundColor="rgb(222,222,"+cC+")";
	this.t.className="withThinBorder";
	this.tb=document.createElement("tbody");
	this.t.appendChild(this.tb);

	this.tr=document.createElement("tr");
	this.td1=document.createElement("td");
	this.td1.noWrap=true;
	this.td1.appendChild(document.createTextNode("Filter-Set "));

	this.methodSEL=document.createElement("select");
	this.td1.appendChild(this.methodSEL);

	if(this.Method==null || this.Method==undefined || this.Method==""){
		this.Method="AND";
	}
	if(this.Method=="AND"){
		this.methodSEL.options[0]=new Option("AND","AND",true,true);
		this.methodSEL.options[1]=new Option("OR","OR",false,false);
	}else{
		this.methodSEL.options[0]=new Option("AND","AND",false,false);
		this.methodSEL.options[1]=new Option("OR","OR",true,true);
	}

	this.methodSEL.ov=this.Method;
	this.methodSEL.set=this;
	this.methodSEL.sm=sm;
	this.methodSEL.onchange=function(){
		if(this.options[this.selectedIndex].value!=this.ov){
			this.set.setMethod(this.options[this.selectedIndex].value);
			if(this.sm.showCriteria==undefined || this.sm.showCriteria==true)this.sm.renderCriteria();
		}
	}

	this.tr.appendChild(this.td1);
	this.tb.appendChild(this.tr);

	for(this.xcsC=0;this.xcsC<this.ChildSet.length;this.xcsC++){
		if(this.xcsC>0){
			this.tempCSTR=document.createElement("tr");
			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(" "));
			this.tempCSTR.appendChild(this.tempCSTD1);

			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(this.Method ));
			this.tempCSTD1.style.fontWeight="700";
			this.tempCSTR.appendChild(this.tempCSTD1);
			this.tb.appendChild(this.tempCSTR);
		}

		this.tempCSTR=document.createElement("tr");
			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(" "));
			this.tempCSTR.appendChild(this.tempCSTD1);
		this.tempCSTD1=document.createElement("td");
		this.tempCSTD1.appendChild(this.ChildSet[this.xcsC].getInput(sm,cC+10));
		this.tempCSTR.appendChild(this.tempCSTD1);
		this.tb.appendChild(this.tempCSTR);
	}
	for(this.xcC=0;this.xcC<this.Criteria.length;this.xcC++){
		if(this.xcsC>0 || this.xcC>0){
			this.tempCSTR=document.createElement("tr");
			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(" "));
			this.tempCSTR.appendChild(this.tempCSTD1);

			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(this.Method ));
			this.tempCSTD1.style.fontWeight="700";
			this.tempCSTR.appendChild(this.tempCSTD1);
			this.tb.appendChild(this.tempCSTR);
		}

		this.tempCSTR=document.createElement("tr");
		this.tempCSTD1=document.createElement("td");
		this.tempCSTD1.appendChild(document.createTextNode(" "));
		this.tempCSTR.appendChild(this.tempCSTD1);

		this.tempCSTD1=document.createElement("td");

		this.tempCSTD1.appendChild(document.createElement("a"));
		this.tempCSTD1.childNodes[0].sm=sm;
		this.tempCSTD1.childNodes[0].set=this;
		this.tempCSTD1.childNodes[0].index=""+this.xcC;
		this.tempCSTD1.childNodes[0].onclick=function(){
			this.set.Criteria.splice(parseInt(this.index),1);
			if(this.sm.showCriteria==undefined || this.sm.showCriteria==true)this.sm.renderCriteria();
		}
		this.tempCSTD1.childNodes[0].appendChild(document.createElement("img"));
		this.tempCSTD1.childNodes[0].childNodes[0].src=serverRoot +"/images/delete.gif";
		this.tempCSTD1.childNodes[0].childNodes[0].border="0";
		this.tempCSTD1.childNodes[0].childNodes[0].style.height="12px";
		this.tempCSTD1.appendChild(this.Criteria[this.xcC].getInput(sm));
		this.tempCSTR.appendChild(this.tempCSTD1);
		this.tb.appendChild(this.tempCSTR);

	}
	if(this.xcsC>0 || this.xcC>0){
		this.tempCSTR=document.createElement("tr");
		this.tempCSTD1=document.createElement("td");
		this.tempCSTD1.appendChild(document.createTextNode(" "));
		this.tempCSTR.appendChild(this.tempCSTD1);

		this.tempCSTD1=document.createElement("td");
		this.tempCSTD1.appendChild(document.createTextNode(this.Method ));
		this.tempCSTD1.style.fontWeight="700";
		this.tempCSTR.appendChild(this.tempCSTD1);
		this.tb.appendChild(this.tempCSTR);
	}
	//new criteria
	this.tempCSTR=document.createElement("tr");
			this.tempCSTD1=document.createElement("td");
			this.tempCSTD1.appendChild(document.createTextNode(" "));
			this.tempCSTR.appendChild(this.tempCSTD1);
	this.tempCSTD1=document.createElement("td");
	this.tempCSTD1.noWrap=true;
	this.newElementBox=document.createElement("select");
	var opt=new Option("SELECT","",false);
	this.newElementBox.options[0]=opt;
	for(var aeC=0;aeC<window.available_elements.length;aeC++){
		var opt=new Option(window.available_elements[aeC].singular,window.available_elements[aeC].element_name,false);
		this.newElementBox.options[this.newElementBox.options.length]=opt;
	}

	this.newElementBox.sm=sm;
	this.newElementBox.set=this;
	this.newElementBox.onchange=function(){
		this.set.newFieldBox.clear();
		if(this.selectedIndex>0){
			var e = this.options[this.selectedIndex].value;
			if(this.sm.sfm.fieldMap[e]!=undefined){
				var list = this.sm.sfm.fieldMap[e];
				this.set.newFieldBox.render(list);
				this.set.newFieldBox.disabled=false;
			}else{
				//load
				this.sm.sfm.onLoad.subscribe(function(evnt,args,args2){
					this.onLoad.unsubscribeAll();
					args2.newFieldBox.render(args[0].list);
				},{newFieldBox:this.set.newFieldBox});

				this.sm.sfm.load(e);
				this.set.newFieldBox.disabled=true;
			}
		}
	}
	this.tempCSTD1.appendChild(this.newElementBox);
			this.tempCSTD1.appendChild(document.createTextNode(" "));

	this.newFieldBox=document.createElement("select");
	this.newFieldBox.sm=sm;
	this.newFieldBox.set=this;
	this.newFieldBox.onchange=function(){
		while (this.set.newComparisonBox.options.length > 0) {
		    this.set.newComparisonBox.options[0] = null;
		}
		if(this.selectedIndex>0){
			this.selectedType=this.options[this.selectedIndex].TYPE;
			this.set.newComparisonBox.options[0]=new Option("SELECT","",true,true);
			this.set.newComparisonBox.options[1]=new Option("=","=");
			if(this.selectedType=="string"){
				this.set.newComparisonBox.options[2]=new Option("LIKE","LIKE");
			}else if(this.selectedType=="float" || this.selectedType=="integer")
			{
				this.set.newComparisonBox.options[2]=new Option(">",">");
				this.set.newComparisonBox.options[3]=new Option(">=",">=");
				this.set.newComparisonBox.options[4]=new Option("<","<");
				this.set.newComparisonBox.options[5]=new Option("<=","<=");
			}else{
				this.set.newComparisonBox.options[2]=new Option(">",">");
				this.set.newComparisonBox.options[3]=new Option(">=",">=");
				this.set.newComparisonBox.options[4]=new Option("<","<");
				this.set.newComparisonBox.options[5]=new Option("<=","<=");
				this.set.newComparisonBox.options[6]=new Option("LIKE","LIKE");
			}
			this.set.newComparisonBox.options[this.set.newComparisonBox.options.length]=new Option("IS NULL","IS NULL");
			this.set.newComparisonBox.options[this.set.newComparisonBox.options.length]=new Option("IS NOT NULL","IS NOT NULL");

			this.set.newComparisonBox.disabled=false;
		}else{
			this.set.newComparisonBox.disabled=true;
		}
	}
	this.newFieldBox.clear=function(){
		while (this.options.length > 0) {
		    this.options[0] = null;
		}
		this.set.newComparisonBox.clear();
	}
	this.newFieldBox.render=function(list){
		this.clear();
		this.options[0]=new Option("SELECT","",false);
		for(var lfC=0;lfC<list.length;lfC++){
			var oI=this.options.length;
		    this.options[oI]=new Option(list[lfC].FIELD_ID,list[lfC].FIELD_ID,false);
		    this.options[oI].HEADER=list[lfC].HEADER;
		    this.options[oI].TYPE=list[lfC].TYPE;
		}
		this.disabled=false;
	}
	this.newFieldBox.disabled=true;
	this.tempCSTD1.appendChild(this.newFieldBox);
			this.tempCSTD1.appendChild(document.createTextNode(" "));

	this.newComparisonBox=document.createElement("select");
	this.newComparisonBox.options[0]=new Option("SELECT","");
	this.newComparisonBox.disabled=true;
	this.newComparisonBox.sm=sm;
	this.newComparisonBox.set=this;
	this.newComparisonBox.onchange=function(){
		if(this.selectedIndex>0){
			this.selected_value=this.options[this.selectedIndex].value;
			if(this.selected_value=="IS NOT NULL" || this.selected_value=="IS NULL"){
				this.set.newValueBox.disabled=true;
				this.set.newValueBox.value="";
			}else{
				this.set.newValueBox.disabled=false;
			}
		}else{
			this.set.newValueBox.disabled=true;
			this.set.newValueBox.value="";
		}
	}
	this.newComparisonBox.clear=function(){
		while (this.options.length > 0) {
		    this.options[0] = null;
		}
		this.disabled=true;
		this.set.newValueBox.value="";
		this.set.newValueBox.disabled=true;
	}
	this.tempCSTD1.appendChild(this.newComparisonBox);
			this.tempCSTD1.appendChild(document.createTextNode(" "));

	this.newValueBox=document.createElement("input");
	this.newValueBox.type="text";
	this.newValueBox.disabled=true;
    this.newValueBox.maxLength="100";
	this.tempCSTD1.appendChild(this.newValueBox);


	this.newButton=document.createElement("input");
	this.newButton.type="button";
	this.newButton.value="Add";
	this.newButton.set=this;
	this.newButton.sm=sm;
	this.newButton.onclick=function(){
		if(this.set.newElementBox.selectedIndex==0){
			xmodal.message('Search Validation', "Please select a data type for the additional criteria.");
			this.set.newElementBox.focus();
				return;
		}
		if(this.set.newFieldBox.selectedIndex==0){
			xmodal.message('Search Validation', "Please select a Field for the additional criteria.");
			this.set.newFieldBox.focus();
				return;
		}
		if(this.set.newComparisonBox.selectedIndex==0){
			xmodal.message('Search Validation', "Please select a Comparison for the additional criteria.");
			this.set.newComparisonBox.disabled=false;
			this.set.newComparisonBox.focus();
				return;
		}

		var c=this.set.newComparisonBox.options[this.set.newComparisonBox.selectedIndex].value;
		if(c!="IS NULL" && c!="IS NOT NULL"){
			if(this.set.newValueBox.value==""){
				xmodal.message('Search Validation', "Please specify a value to match against.");
				this.set.newValueBox.disabled=false;
				this.set.newValueBox.focus();
				return;
			}
		}
		this.set.save();
		if(this.sm.showCriteria==undefined || this.sm.showCriteria==true)this.sm.renderCriteria();
		//alert(this.sm.searchDOM.toXML());
	}
	this.tempCSTD1.appendChild(this.newButton);

	this.tempCSTR.appendChild(this.tempCSTD1);
	this.tb.appendChild(this.tempCSTR);

	this.tempCSTR=document.createElement("tr");
	this.tempCSTD1=document.createElement("td");
	this.tempCSTD1.appendChild(document.createTextNode(" "));
	this.tempCSTR.appendChild(this.tempCSTD1);

	this.tempCSTD1=document.createElement("td");
	this.tempCSTD1.appendChild(document.createTextNode(this.Method ));
	this.tempCSTD1.style.fontWeight="700";
	this.tempCSTR.appendChild(this.tempCSTD1);
	this.tb.appendChild(this.tempCSTR);

	//new set
	this.tempCSTR=document.createElement("tr");
	this.tempCSTD1=document.createElement("td");
	this.tempCSTD1.appendChild(document.createTextNode(" "));
	this.tempCSTR.appendChild(this.tempCSTD1);

	this.tempCSTD1=document.createElement("td");

	var but=document.createElement("input");
	but.type="button";
	but.value="Add Filter Set";
	but.set=this;
	but.sm=sm;
	but.onclick=function(o){
		this.set.ChildSet.push(new xdat_criteria_set());
		if(this.sm.showCriteria==undefined || this.sm.showCriteria==true)this.sm.renderCriteria();
	}
	this.tempCSTD1.appendChild(but);

	this.tempCSTR.appendChild(this.tempCSTD1);
	this.tb.appendChild(this.tempCSTR);

	return this.t;
}
xdat_criteria.prototype.getInput=function(sm){
	return document.createTextNode(" ("+this.toString() + ")");
}

function removeElementName(str,element_name){
	if(str.startsWith(element_name)){
		return str.substring(element_name.length +1);
	}else{
		return str;
	}
}

xdat_criteria.prototype.toString=function(element_name,xss){
	return (xss!=null)?xss.getFieldHeader(this.getSchemaField())+ " "+this.getComparisonType()+ " "+this.getValue():removeElementName(this.getSchemaField(),element_name)+ " "+this.getComparisonType()+ " "+this.getValue();
}

xdat_criteria_set.prototype.needsSubmit=function(_autosubmit){
	if(this.newElementBox==undefined || this.newElementBox.selectedIndex>0){
		if(this.newFieldBox==undefined || this.newFieldBox.selectedIndex>0){
			if(this.newComparisonBox!=undefined && this.newComparisonBox.selectedIndex>0){
				if(this.isValid()){
					if(!this.newValueBox.disabled && this.newValueBox.value!=""){
						if(_autosubmit==undefined || _autosubmit!=true){
							return true;
						}else{
							this.save();
						}
					}else if(!this.newValueSelectBox.disabled && this.newValueSelectBox.selectedIndex>0){
						if(_autosubmit==undefined || _autosubmit!=true){
							return true;
						}else{
							this.save();
						}
					}else if(this.newValueSelectBox.disabled && this.newValueBox.disabled){
						if(_autosubmit==undefined || _autosubmit!=true){
							return true;
						}else{
							this.save();
						}
					}
				}else{
					return true;
				}
			}
		}
	}

	for(this.csdC=0;this.csdC<this.ChildSet.length;this.csdC++){
		if(this.ChildSet[this.csdC].needsSubmit(_autosubmit))return true;
	}
	return false;
}


xdat_criteria.prototype.duplicate=function(){
	var cp=new xdat_criteria();
	cp.ComparisonType=this.ComparisonType;
	cp.CustomSearch=this.CustomSearch;
	cp.OverrideValueFormatting=this.OverrideValueFormatting;
	cp.SchemaField=this.SchemaField;
	cp.Value=this.Value;
	return cp;
}

xdat_criteria_set.prototype.duplicate=function(){
	var cp=new xdat_criteria_set();
	cp.Method=this.Method;
	for(this.csdC=0;this.csdC<this.Criteria.length;this.csdC++){
		cp.Criteria.push(this.Criteria[this.csdC].duplicate());
	}
	for(this.csdC=0;this.csdC<this.ChildSet.length;this.csdC++){
		cp.ChildSet.push(this.ChildSet[this.csdC].duplicate());
	}
	return cp;
}

xdat_stored_search.prototype.organize=function(){
	if(this.SearchWhere.length>1){
		var dup = new xdat_criteria_set();
		dup.Method="AND";
		for(var swC=0;swC<this.SearchWhere.length;swC++){
			dup.ChildSet.push(this.SearchWhere[swC].duplicate());
		}

		this.SearchWhere=new Array();
		this.SearchWhere[0]=new xdat_criteria_set();
		this.SearchWhere[0].Method="AND";
		this.SearchWhere[0].ChildSet.push(dup);
	}else if(this.SearchWhere.length==0){
		this.SearchWhere.push(new xdat_criteria_set());
		this.SearchWhere[0].Method="AND";
	}else{
		if(this.SearchWhere[0].Method=="OR" || this.SearchWhere[0].Criteria.length>0){
			var dup = this.SearchWhere[0].duplicate();
			this.SearchWhere[0]=new xdat_criteria_set();
			this.SearchWhere[0].Method="AND";
			this.SearchWhere[0].ChildSet.push(dup);
		}
	}
}

xdat_stored_search.prototype.getColumnFilter=function(element_name,field_id){
	this.organize();

	for(var csC=0;csC<this.SearchWhere[0].ChildSet.length;csC++){
		var cs=this.SearchWhere[0].ChildSet[csC];
		if(cs.isColumnFilter(element_name,field_id))return cs;
	}

	var cs = new xdat_criteria_set();
	cs.Method="OR";
	cs.element_name=element_name;
	cs.field_id=field_id;
	this.SearchWhere[0].ChildSet.push(cs);
	return cs;
}

xdat_criteria_set.prototype.couldBeFilter=function(){
	if(this.Method=="AND"){
		return false;
	}

	if(this.Criteria.length>0){
		return false;
	}

	return true;
}

xdat_criteria_set.prototype.isColumnFilter=function(element_name,field_id){
	if(this.Method=="AND"){
		return false;
	}

	if(this.ChildSet.length>0){
		return false;
	}

	if(this.element_name==element_name && this.field_id==field_id){
		return true;
	}

	if(this.Criteria.length==0){
		return false;
	}

	for(var csC=0;csC<this.Criteria.length;csC++){
		var crit=this.Criteria[csC];
		if(crit.getSchemaField().toLowerCase()!=element_name.toLowerCase() + "." + field_id.toLowerCase() && crit.getSchemaField().toLowerCase()!=element_name.toLowerCase() + "/" + field_id.toLowerCase()){
			return false;
		}
	}

	this.element_name=element_name;
	this.field_id=field_id;
	return true;
}

xdat_criteria_set.prototype.isProjectCriteria=function(){
	if(this.Method=="AND")return false;
	if(this.ChildSet.length>0)return false;
	if(this.Criteria.length!=2)return false;

	if(!this.Criteria[0].getSchemaField().toLowerCase().endsWith("project"))
		return false;
	if(!this.Criteria[1].getSchemaField().toLowerCase().endsWith("project"))
		return false;

	return true;
}

xdat_criteria_set.prototype.toString=function(element_name,xss){
	if(this.isProjectCriteria()){
		return "Project="+ this.Criteria[0].Value;
	}

	if(this.Criteria.length==0 && this.ChildSet.length==1){
		return this.ChildSet[0].toString(element_name,xss);
	}

	if(this.Criteria.length==1 && this.ChildSet.length==0){
		return this.Criteria[0].toString(element_name,xss);
	}

	var _return="";

	var eC=0;
	for(;eC<this.Criteria.length;eC++){
		if(eC>0){
			_return +=" " + this.Method;
		}

		_return+=" (" + this.Criteria[eC].toString(element_name,xss) + ")";
	}

	var tsxcsC=0;
	for(;tsxcsC<this.ChildSet.length;tsxcsC++){
		if(eC++>0){
			_return +=" " + this.Method;
		}
		_return+=" (" + this.ChildSet[tsxcsC].toString(element_name,xss) + ")";
	}
	tsxcsC=null;

	return _return;
}

xdat_criteria_set.prototype.getFieldCount=function(){
	var fields=new Array();

	for(var csC=0;csC<this.Criteria.length;csC++){
		var crit=this.Criteria[csC];
		var t=crit.getSchemaField().toLowerCase();
		if(!fields.contains(t))fields.push(t);
	}

	return fields.length;
}

/*
 * 08/31/2010 (Aditya Siram) :  Some utility routines to be used
 * in this file are put into the LocalSM namespace so as not to pollute
 * the global one.
 */
var LocalSM = {
  /*
   * The given substring is removed the given string
   * eg. "filter_string("h","helloworld") => "elloworld"
   *     "filter_string("%<","%<Test1%>") => "Test1%>"
   * chars : substring to remove
   * string : string to operate on.
   */
  filter_string : function (chars, string) {
    return LocalSM._filter_string(chars,"",string);
  },
  /* A private function of this namespace used by filter_string.
   * Do not call directly.
   */
  _filter_string : function (chars, accum, string) {
    var pos = string.indexOf(chars);
    if (pos === -1) {
      return accum + string;
    }
    else {
      var end_of_chars = pos + chars.length;
      accum = accum + string.substring(0,pos);
      return LocalSM._filter_string(chars,accum,string.substring(end_of_chars));
    }
  }
}

xdat_stored_search.prototype.renderFilterDisplay=function(){
   		if(this.SearchWhere.length==0 ||
   		   (this.SearchWhere.length==1 && (
   		     (this.SearchWhere[0].ChildSet.length==1 && this.SearchWhere[0].ChildSet[0].isProjectCriteria())
   		     || this.SearchWhere[0].isProjectCriteria()))){
   			return "";
   		}else{
   			if(this.SearchWhere.length==1){
			  /*
			   * 08/31/2010 (Aditya Siram) : For some odd reason values returned by the XML parser look
			   * like this %<some_value>% or %some_value%. On a web-page the former is rendered as ?? and
			   * latter as %% so "%<", "%>" and "%" must be filtered out. A better question is why the XML parser
			   * returns these values - but until that is answered the following seems to work. Hopefully
			   * the tokens I am removing are never a valid part of the value.
			   */
			  var step_1 = LocalSM.filter_string("%<", this.SearchWhere[0].toString(this.RootElementName,this));
			  var step_2 = LocalSM.filter_string(">%", step_1);
			  var step_3 = LocalSM.filter_string("%", step_2);
              var step_4 = step_3.split("\)").join("\)&#8203;");
              var step_5 = step_4.split(",").join(",&#8203;");
   			  return "Filter(s):&nbsp;" + step_5;
   			}else{
   				var rfdswT="";
   				for(var rfdswC=0;rfdswC<this.SearchWhere.length;rfdswC++){
   					if(rfdswC>0)rfdswT+=" AND ";
   					rfdswT+=this.SearchWhere[rfdswC].toString(this.RootElementName,this);
   				}
                rfdswT = rfdswT.split("\)").join("\)&#8203;");
                rfdswT = rfdswT.split(",").join(",&#8203;");
   				return "Filter(s):&nbsp;" + rfdswT;
   			}
   		}
}
