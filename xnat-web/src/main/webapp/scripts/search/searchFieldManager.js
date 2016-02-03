/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/searchFieldManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function SearchFieldManager(div_id){
	this.fieldSets = new Array();
	this.fieldMap=new Object();
	this.versionMap=new Object();
	
	if(div_id){
		this.div=document.getElementById(div_id);
	}
	this.onLoad=new YAHOO.util.CustomEvent("load",this);
	this.onFieldAdd=new YAHOO.util.CustomEvent("on-field-add",this);
	
	this.init=function(init_elements){
		this.render();
		if(init_elements!=undefined){
			for(var eC=0;eC<init_elements.length;eC++){
				this.load(init_elements[eC]);
			}
		}
	}
	
	this.render=function(){
		this.div.innerHTML="";
		
		var element_list=document.createElement("div");
		element_list.appendChild(document.createElement("label"));
		element_list.childNodes[0].innerHTML="Possible Elements:&nbsp;";
			this.selectBox=document.createElement("select");
			var opt=new Option("SELECT","",false);
					this.selectBox.options[0]=opt;
			var sCount=1;
			for(var aeC=0;aeC<window.available_elements.length;aeC++){
				var opt=new Option(window.available_elements[aeC].singular,window.available_elements[aeC].element_name,false);
				this.selectBox.options[sCount++]=opt;
			}
			this.selectBox.id="sf_pe";
			this.selectBox.sfm=this;
			this.selectBox.onchange=function(){
				if(this.selectedIndex>0){
					this.sfm.load(this.options[this.selectedIndex].value);
				}
			}
		element_list.appendChild(this.selectBox);
		
//		var addBut=document.createElement("button");
//		addBut.innerHTML="<img border=\"0\" src=\""+serverRoot +"/images/plus.gif\"/>";
//		addBut.selector=this.selectBox;
//		addBut.sfm=this;
//		addBut.onclick=function(){
//			if(this.selector.selectedIndex>0){
//				this.sfm.load(this.selector.options[this.selector.selectedIndex].value);
//			}
//		}
//		element_list.appendChild(addBut);
		
		var field_tabs_container=document.createElement("div");
		field_tabs_container.style.height="500px";
		field_tabs_container.style.overflow="auto";
		
		var field_tabs=document.createElement("div");
		
		field_tabs_container.appendChild(field_tabs);
				
		this.div.appendChild(element_list);
		this.div.appendChild(document.createElement("br"));
		this.div.appendChild(field_tabs_container);
		
		this.tabView=new YAHOO.widget.TabView(field_tabs,{height:"500px"});
	}
	
	this.loaded=new Array();
	
	this.removeSelectOption=function(element_name){
		var selectBox=document.getElementById("sf_pe");
		for(var oC=0;oC<selectBox.childNodes.length;oC++){
			if(selectBox.childNodes[oC].value==element_name){
				selectBox.removeChild(selectBox.childNodes[oC]);
				break;
			}
		}
	}
	
	
	this.load=function(element_name){
		if(!this.loaded.contains(element_name)){
			this.loaded.push(element_name);
			
			var fieldCallback={
				success:this.processResults,
				failure:this.initFailure,
				scope:this,
                cache:false, // Turn off caching for IE
				argument:element_name
			}
			
			openModalPanel("load_fields","Loading field information.");
			YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/search/elements/'+element_name +'?XNAT_CSRF=' + window.csrfToken + '&format=json&timestamp=' + (new Date()).getTime(),fieldCallback,null,this);
		}
	}
	
	this.processResults=function(o){
		closeModalPanel("load_fields");
		var resultset= eval("(" + o.responseText +")");
		this.versionMap[o.argument]=resultset.ResultSet.versions.DisplayVersions.versions;
		this.fieldMap[o.argument]= resultset.ResultSet.Result;
		this.onLoad.fire({element_name:o.argument,list:this.fieldMap[o.argument]});
	}
	
	this.renderFields=function(element_name){
		if(!this.loaded.contains(element_name)){
			this.loaded.push(element_name);
			
			this.removeSelectOption(element_name);
			
			if(this.tabView){
				var tempTab=new YAHOO.widget.Tab({
			  	    label:window.available_elements.getByName(element_name).singular + '&nbsp;<span style="height:12px;width:1px;">&nbsp;</span>',
			  	    content:'<div id="'+element_name + '_fields_dt">Preparing Results</div>',
			  	    active:true});			  	 
			    this.tabView.addTab(tempTab);
				
				var dataSource = new YAHOO.util.DataSource(serverRoot +'/REST/search/elements/'+element_name +'?');
		   		dataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;
		   		dataSource.responseSchema = {
		     		resultsList : 'ResultSet.Result', // String pointer to result data
		     		fields: ["FIELD_ID","HEADER","SUMMARY","TYPE","REQUIRES_VALUE"] 
		   		};
		   		
		   		var dtConfig= new Object();
		   		dtConfig.initialRequest="format=json";
		   		//dtConfig.scrollable=true;
				//dtConfig.height="500px";
				dtConfig.visible=false;
	
				var newfieldsColumnDefs=[
				  {key:"button",label:"Add",width:28,formatter:function(el, oRecord, oColumn, oData) {
			el.innerHTML=el.innerHTML = "<button type=\"button\" class=\"yui-dt-button\"><img border=\"0\" src=\""+serverRoot +"/images/plus.gif\"/></button>";
    }},
				  {key:"FIELD_ID",label:"Field",sortable:true},
				  {key:"HEADER",label:"Header",sortable:true},
				  {key:"TYPE",label:"Type",sortable:true},
				  {key:"SUMMARY",label:"Summary",formatter:function(elCell, oRecord, oColumn, oData){
						if(oData.length<15){
							elCell.innerHTML=oData;
						}else{
						    elCell.innerHTML=oData.substring(0,12) + "...";
						    elCell.toolTip=new YAHOO.widget.Tooltip(oRecord.getId()+"."+oColumn.key,{container:this,context:elCell,text:oData.replace(/'/g,""),preventoverlap:false});
						}
					}}];
	   			   		
 				var dataTable = new YAHOO.widget.DataTable(element_name + '_fields_dt',newfieldsColumnDefs,dataSource,dtConfig);  
 				dataTable.sfm=this;
   
		        dataTable.subscribe("buttonClickEvent", function(oArgs){   
		         	 var but = oArgs.target;   
		             var oRecord = this.getRecord(but); 
		             if(oRecord.getData("REQUIRES_VALUE")=="true"){
                         xModalMessage('Search Validation', "This property requires a value to determine the proper result.<br/><br/>Support for this has not been added yet.<br/>Please check back at a later date.");
		             }else{
			             var xsf=new xdat_search_field();
			             xsf.ElementName=element_name;
			             xsf.FieldId=oRecord.getData("FIELD_ID");
			             xsf.Header=oRecord.getData("HEADER");
			             this.sfm.onFieldAdd.fire(xsf);
		             }
		        });
			}
		}
	}
	
	this.initFailure=function(o){
		//alert("FAILED to load xml.")
	};
	
	this.completeInit=function(o){
		var loadResults= eval("(" + o.responseText +")");
		this.fieldSets.push(loadResults.ResultSet);
		this.render();
	};
	
}