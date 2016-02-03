/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/dataTableStoredSearch.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
dynamicJSLoad("DataTableSearch","search/dataTableSearch.js");
function DataTableStoredSearch(_div_table_id,_obj,_config,_options){
	//fired after Search XML has beens successfully loaded.
	this.onInit=new YAHOO.util.CustomEvent("init",this);
	this.onComplete=new YAHOO.util.CustomEvent("complete",this);
	
	if(_obj!=undefined){
		this.obj=_obj;
		this.div_table_id=_div_table_id;
		this.config=_config;
		this.options=_options;
	}

	this.init=function(){
		this.loaderGIF=new XNATLoadingGIF(this.div_table_id);
		this.loaderGIF.render();

		var URL = this.obj.URL;
		var questOrAmper = "?";
		if(!(URL===undefined)){
			var questOrAmper = URL.indexOf("?") == -1 ? "?" : "&";
						
		}
		URL += questOrAmper + 'XNAT_CSRF=' + csrfToken;
		if(this.obj.XML){
			this.load();
		}else{
			this.onInit.subscribe(this.load,this);

			//load from search xml from server
			this.initCallback={
				success:this.completeInit,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
			};

			YAHOO.util.Connect.asyncRequest('GET',URL,this.initCallback,null,this);
		}
	};

	this.reload=function(){
		this.loaderGIF=new XNATLoadingGIF(this.div_table_id);
		this.loaderGIF.render();

		var URL = this.obj.URL;
		
		URL += '?XNAT_CSRF=' + csrfToken;
		
		if(this.obj.XML){
			this.load();
		}else{
			this.onInit.subscribe(this.load,this);

			//load from search xml from server
			this.initCallback={
				success:this.completeInit,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
			};

			YAHOO.util.Connect.asyncRequest('GET',URL,this.initCallback,null,this);
		}
	};

	this.initFailure=function(o){
        if (!window.leaving) {
            if(o.status==401){
                xModalMessage('Session Expired', "WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.");
                window.location=serverRoot+"/app/template/Login.vm";
            }
            this.onInit.fire({});
        }
	};
	this.completeInit=function(o){
		this.obj.XML=o.responseText;
	 	this.onInit.fire({});
	};


	this.onRemoveRequest=new YAHOO.util.CustomEvent("remove-request",this);
	this.onSavedSearch=new YAHOO.util.CustomEvent("saved-search",this);

	this.load=function(){
		this.dataTable=new DataTableSearch(this.div_table_id,this.obj,this.config,this.options);
		this.dataTable.onInit.subscribe(this.complete,this,this);
		this.dataTable.onRemoveRequest.subscribe(function(o){
			return this.onRemoveRequest.fire();
		},this,this);
		this.dataTable.onSavedSearch.subscribe(function(o){
			return this.onSavedSearch.fire();
		},this,this);

	  	 if(this.height!=undefined){
	  	 	this.dataTable.setHeight(this.height);
	  	 }
	  	 if(this.width!=undefined){
	  	 	this.dataTable.setWidth(this.width);
	  	 }

		this.dataTable.init({reload:true});
	};

	this.complete=function(){
		this.onComplete.fire();
	};

	this.setHeight=function(_height){
		this.height=_height;
		if(this.dataTable!=undefined)
			this.dataTable.setHeight(_height);
	};

	this.setWidth=function(_width){
		this.width=_width;
		if(this.dataTable!=undefined)
			this.dataTable.setWidth(_width);
	};
}
