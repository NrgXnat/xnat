/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/projectList.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function ProjectList(_menu, _options){
  this.options=_options;
  this.menu=_menu;
  
  if(this.options==undefined){
  	this.options=new Object();
  	this.options.owner=true;
  	this.options.recent=true;
  }
  
	this.init=function(){
		
		//load from search xml from server
		this.initCallback={
			success:this.completeInit,
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
		
		var params="";
		
		if(this.options.recent!=undefined){
			params += "&recent=true";
		}
		
		if(this.options.owner!=undefined){
			params += "&owner=true";
		}
		
		if(this.options.member!=undefined){
			params += "&member=true";
		}
		
		if(this.options.collaborator!=undefined){
			params += "&collaborator=true";
		}
		
		YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects?format=json' + params,this.initCallback,null,this);
	};
	
	this.initFailure=function(o){
        if (!window.leaving) {
            this.displayError("ERROR " + o.status+ ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
        }
	};
	
	this.completeInit=function(o){
		try{
		    this.projectResultSet= eval("(" + o.responseText +")");
		}catch(e){
			this.displayError("ERROR " + o.status+ ": Failed to parse " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
		}
		try{
		    this.render();
		}catch(e){
			this.displayError("ERROR : Failed to render " + XNAT.app.displayNames.singular.project.toLowerCase() + " list.");
		}
	};
	
	this.displayError=function(errorMsg){
		xModalMessage('Error', errorMsg);
	}
	
	this.render=function(){
		var items=new Array();
		for(var pC=0;pC<this.projectResultSet.ResultSet.Result.length;pC++){
			var p=this.projectResultSet.ResultSet.Result[pC];
			items.push({text:p.secondary_id,url:serverRoot + "/app/template/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/" + p.id});
		}
		items.push({text:"View All",url:serverRoot + "/app/template/BrowseProjects.vm"});
		//this.menu=new YAHOO.widget.Menu(this.div_id,{itemdata:items,visible:true, scrollincrement:5,position:"static"});
		this.menu.addItems(items);
		this.menu.render();
	}
}
	
	function prependLoader(div_id,msg){
		if(div_id.id==undefined){
			var div=document.getElementById(div_id);
		}else{
			var div=div_id;
		}
		var loader_div = document.createElement("div");
		loader_div.innerHTML=msg;
		div.parentNode.insertBefore(loader_div,div);
		return new XNATLoadingGIF(loader_div);
	}
