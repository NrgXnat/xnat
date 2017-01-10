/*
 * web: searchEngine.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");
dynamicJSLoad("xdat_stored_search","generated/xdat_stored_search.js");
/*******************************************
 * SearchManager
 * Used to manage the querying of data from the server.
 * required variables: serverRoot (host)
 */
function HTMLSearchManager(_id,_desc){
	this.id=_id;
	this.desc=_desc;
	
	//fired after Search XML has beens successfully loaded.
	this.onInit=new YAHOO.util.CustomEvent("init",this);
	//fired after Temporary database table has been created, and result summary received.
	this.onPreload=new YAHOO.util.CustomEvent("preload",this);
	//fired after First page has been loaded
	this.onLoad=new YAHOO.util.CustomEvent("load",this);
	
	//whether results should be paged
	this.paging=true;
    this.numPages = null;
    this.currentPage = 0;
    this.totalRecords = null;
	
    this.results = null;
    this.searchXML = null;
	
	this.title = "";
	this.content ="";
	
	//whether the results should automatically be loaded, if true then only the onLoad needs to be listened for.
	this.autoComplete=true;
	
	this.init=function(){
		if(this.autoComplete){
			this.onInit.subscribe(this.preload,this);
			this.onPreload.subscribe(this.load,this);
		}
		//load from search xml from server
		this.initCallback={
			success:this.completeInit,
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
		
		var params = "remote-class=org.nrg.xnat.ajax.RequestSearchXML&remote-method=execute&bundleID=" + this.id + "&XNAT_CSRF="+csrfToken;
 		YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',this.initCallback,params,this);
	};
	
	this.initFailure=function(o){
		//alert("FAILED to load xml.")
	};
	
	this.completeInit=function(o){
		this.searchXML=o.responseText;
		if (o.responseXML)
        {
            var bundleDOM = o.responseXML.getElementsByTagName("bundle")[0];
            if (bundleDOM)
            {
				this.searchDOM=new xdat_stored_search(bundleDOM);
            }
        }
	 	this.onInit.fire({});
	};
	
	this.preload=function(){
  		this.startTime = (new Date()).getTime();
		//create search table
        var queryString = "remote-class=org.nrg.xnat.ajax.RequestSearchData&remote-method=init&search=" + this.searchXML + "&XNAT_CSRF="+csrfToken;
        if (this.numToDisplay){
          queryString +="&rows=" +this.numToDisplay; 
        }
        if (this.sortBy){
          queryString +="&sortBy=" +this.sortBy; 
        }
        if (this.sortOrder){
          queryString +="&sortOrder=" +this.sortOrder; 
        }
        if (this.isNew){
          queryString +="&isNew=" +this.isNew; 
        }
        
        this.preloadCallback={
			success:this.completePreload,
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
        };
        
        YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',this.preloadCallback,queryString,this);
        
        this.isNew=false;
	};
	
	this.completePreload=function(o){
		var xmlDoc = o.responseXML;
        if (xmlDoc)
        {
		    var completeTime = new Date();
		    
		    var results = xmlDoc.getElementsByTagName("results")[0];
		    if (results!=null && results != undefined){
		      this.numPages =parseInt(getAttributeValue("numPages",results));
              this.currentPage =parseInt(getAttributeValue("currentPage",results));
              this.totalRecords =parseInt(getAttributeValue("totalRecords",results));
              this.numToDisplay =parseInt(getAttributeValue("numToDisplay",results));
		    }
		}
		this.onPreload.fire({});
	};
	
	this.load=function(){
		if (this.totalRecords > 0){
			var params = "remote-class=org.nrg.xnat.ajax.RequestSearchData&remote-method=loadPage&search=" + this.id + "&page=0&XNAT_CSRF=" + csrfToken;
			
	        this.loadCallback={
				success:this.completeLoad,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
	        };
	        
	        YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',this.loadCallback,params,this);
	        
	        this.currentPage=0;
		}else{
			this.onLoad.fire();
		}
	};
	
	this.loadPage=function(_page){
		if (this.totalRecords > 0){
			if (_page==undefined){
				_page=0;
			}
			var params = "remote-class=org.nrg.xnat.ajax.RequestSearchData&remote-method=loadPage&search=" + this.id + "&page="+ _page + "&XNAT_CSRF=" + csrfToken;
			
	        this.loadCallback={
				success:this.completeLoad,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
	        };
	        
	        YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',this.loadCallback,params,this);
	        
	        this.currentPage=_page;
		}
	};
	
	this.completeLoad=function(o){
		this.results=o.responseText;
		this.onLoad.fire();
	};
	
	this.setSearchDOM=function(_DOM){
		this.searchDOM=_DOM;
		this.searchXML=_DOM.getXML();
	}
}
