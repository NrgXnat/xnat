/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/LeftBarTreeView.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function LeftBarTreeView(_config){
 this.config=_config;
 this.treeview_id=this.config.treeview;
 
 
 this.init=function(){	
 	this.tree = new YAHOO.widget.TreeView(this.treeview_id,this.config);   
 	this.tree.lTV=this;
    //turn dynamic loading on for entire tree:   
    //this.tree.setDynamicLoad(this.loadNodeData,this);   
 	 	
 	this.tree.subscribe("expand",function(node){
 		if(node.data.URL==undefined){
       		this.open_array.push(node.data.ID);
       		YAHOO.util.Cookie.remove("open.nodes",{path:"/"});
       		YAHOO.util.Cookie.set("open.nodes",this.open_array.toString(),{path:"/"});
 		}
 	},this,this);  
 	 	
 	this.tree.subscribe("collapse",function(node){
 		if(node.data.URL==undefined){
 			for(var onC=0;onC<this.open_array.length;onC++){
 				if(this.open_array[onC]==node.data.ID){
       				this.open_array.splice(onC,1);
       				break;
 				}
 			}
       		YAHOO.util.Cookie.remove("open.nodes",{path:"/"});
       		if(this.open_array.length>0){
       			YAHOO.util.Cookie.set("open.nodes",this.open_array.toString(),{path:"/"});
       		}else{
       			YAHOO.util.Cookie.set("open.nodes","",{path:"/"});
       		}
 		}
 	},this,this);
 	
 	var opened=YAHOO.util.Cookie.get("open.nodes",{path:"/"});
 	// Opened
 	if(opened!=null && opened!=""){
 		this.open_array=opened.split(',');
 	} else {
 		this.open_array=new Array();
 		this.open_array.push("proj");
 	}
 	
 	this.tree.searches=new Array();
 	this.tree.subscribe("labelClick",function(node){
 		if(this.searches.contains(node.data.ID)){
 			node.tree.lTV.loadSearch(node);
 		}
 	});
 	
 	var root = this.tree.getRoot();
 	
 	if(root.hasChildren()){
 		root.children=new Array();
 	}
 	
 	root.treeManager=this;
 	if(XNAT.app.showLeftBarProjects){
	 	//define project node
	 	this.projNode=new YAHOO.widget.TextNode({label:XNAT.app.displayNames.plural.project,ID:"proj"},root,this.open_array.contains("proj"));
	 	 	
	 	var apNode=new YAHOO.widget.TextNode({label:"Recent",ID:"projectData.r",
	 	title:XNAT.app.displayNames.plural.project+" visited in the last 30 days."},this.projNode,this.open_array.contains("projectData.r"));
	 	apNode.setDynamicLoad(function(node, fnLoadComplete){
	 		var callback={
		      success:function(oResponse){
		        var oResults = eval("(" + oResponse.responseText + ")"); 
		        if((oResults.ResultSet.Result) && (oResults.ResultSet.Result.length)) {  
		           for (var ssC=0; ssC<oResults.ResultSet.Result.length;  ssC++) {   
		               var cpNode=new YAHOO.widget.TextNode({label:oResults.ResultSet.Result[ssC].secondary_id,
	                     		ID:"ss."+oResults.ResultSet.Result[ssC].id,
	                     		href:serverRoot + '/app/template/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/' + oResults.ResultSet.Result[ssC].id,
	                     		TITLE:oResults.ResultSet.Result[ssC].name},oResponse.argument.node,false);
	                   
	 				   cpNode.isLeaf=true;
		           }   
		        } 
		        oResponse.argument.fnLoadComplete();
		      },
		      failure:function(oResponse){
		        oResponse.argument.fnLoadComplete();
		      },
              cache:false, // Turn off caching for IE
		      argument:{"node":node,"fnLoadComplete":fnLoadComplete,"lTV":lTV}
		    };
		    
																				           //YAHOO.util.Connect.asyncRequest('GET',this.obj.URL,this.initCallback,null,this);
		    YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects?XNAT_CSRF=' + window.csrfToken + '&format=json&recent=true&stamp='+ (new Date()).getTime(),callback,null);
		 		
	 	},this);

	 	if(XNAT.app.showLeftBarFavorites){
		 	var fpNode=new YAHOO.widget.TextNode({label:"Favorite",ID:"projectData.f",
		 	title:XNAT.app.displayNames.plural.project+" added to your list of favorites."},this.projNode,this.open_array.contains("projectData.f"));
		 	fpNode.setDynamicLoad(function(node, fnLoadComplete){
		 		var callback={
			      success:function(oResponse){
			        var oResults = eval("(" + oResponse.responseText + ")"); 
			        if((oResults.ResultSet.Result) && (oResults.ResultSet.Result.length)) {  
			           for (var ssC=0; ssC<oResults.ResultSet.Result.length;  ssC++) {   
			               var cpNode=new YAHOO.widget.TextNode({label:oResults.ResultSet.Result[ssC].secondary_id,
		                     		ID:"ss."+oResults.ResultSet.Result[ssC].id,
		                     		href:serverRoot + '/app/template/XDATScreen_report_xnat_projectData.vm/search_element/xnat:projectData/search_field/xnat:projectData.ID/search_value/' + oResults.ResultSet.Result[ssC].id,
		                     		TITLE:oResults.ResultSet.Result[ssC].name},oResponse.argument.node,false);
		                   
		 				   cpNode.isLeaf=true;
			           }   
			        } 
			        oResponse.argument.fnLoadComplete();
			      },
			      failure:function(oResponse){
			        oResponse.argument.fnLoadComplete();
			      },
                  cache:false, // Turn off caching for IE
			      argument:{"node":node,"fnLoadComplete":fnLoadComplete,"lTV":lTV}
			    };
			    
																					           //YAHOO.util.Connect.asyncRequest('GET',this.obj.URL,this.initCallback,null,this);
			    YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects?XNAT_CSRF=' + window.csrfToken + '&format=json&favorite=true&stamp='+ (new Date()).getTime(),callback,null);
			 		
		 	},this);
	 	}
	 	
	 	var opNode=new YAHOO.widget.TextNode({label:"My " + XNAT.app.displayNames.plural.project.toLowerCase(),ID:"projectData.my",
	 	URL:serverRoot +'/REST/projects?format=search_xml&accessible=true',
	 	title:XNAT.app.displayNames.plural.project+" you have access to.",
	 	isLeaf:true},this.projNode,false);
	 	opNode.isLeaf=true;
	 	this.tree.searches.push(opNode.data.ID);
	 	 	
	 	//VIEW ALL
	 	var cpNode=new YAHOO.widget.TextNode({label:"Other " + XNAT.app.displayNames.plural.project.toLowerCase(),
	 	ID:"projectData.a",
	 	URL:serverRoot +'/REST/projects?format=search_xml&accessible=false',
	 	title:XNAT.app.displayNames.plural.project+" you don't have access to, but are available upon request."},this.projNode,false);
	 	cpNode.isLeaf=true;
	 	this.tree.searches.push(cpNode.data.ID);
 	}

 	if(XNAT.app.showLeftBarSearch){
	 	//define stored searches node
	 	this.ssNode=new YAHOO.widget.TextNode({label:"Stored Searches",ID:"ss"},root,this.open_array.contains("ss"));
	 	this.ssNode.setDynamicLoad(this.loadStoredSearches,this);
 	}

 	if(XNAT.app.showLeftBarBrowse){
	 	if(window.available_elements!=undefined){

             function sortByPlural(a,b)
             {
                 var aName = a.plural.toLowerCase();
                 var bName = b.plural.toLowerCase();
                 if (aName < bName){
                     return -1;
                 }else if (aName > bName){
                     return  1;
                 }else{
                     return 0;
                 }
             }
             window.available_elements.sort(sortByPlural);
            //define data node
		 	this.dataNode=new YAHOO.widget.TextNode({label:"Data",ID:"d"},root,this.open_array.contains("d"));
		 	for(var esC=0;esC<window.available_elements.length;esC++){
		 		var es=window.available_elements[esC];
                if (es.element_name === "wrk:workflowData") {continue;} // do not include workflows
		 		var cpNode=new YAHOO.widget.TextNode({label:es.plural,
		 		ID:"d."+es.element_name,
		 		URL:serverRoot +'/REST/search/saved/@' + es.element_name + ''},this.dataNode,false);
	 			cpNode.isLeaf=true;
	 			this.tree.searches.push(cpNode.data.ID);
		 	}
	 	}
 	}
 	
 	try{
 		this.tree.draw();
 	}catch(e){}


 }
 
 this.resetDynamic=function(){
	 this.tree.removeChildren(this.ssNode );
	 this.ssNode.expand();
 }
 
 this.need_expansion=new Array();
 this.expand=function(_id){
 	var node=this.tree.getNodeByProperty("ID",_id);
 	if(node==null){
 		this.need_expansion.push(_id);
 	}else{
 		if(node.tree.searches.contains(_id)){
 			node.tree.fireEvent("labelClick",node);
 		}else{
 			node.expand();
 		}
 	}
 }
  
  
 this.displayTab=function(tabReqObject){
    if(window.tab_module==undefined){
      window.tab_module=new YAHOO.widget.Module(this.config.module,{visible:false}); 
      window.tab_manager=new TabManager(this.config.tabs);
      window.tab_manager.suppress_select=true;
      window.tab_manager.onTabClose.subscribe(this.resetNode);
      window.tab_manager.onTabDelete.subscribe(this.resetParent);
      window.tab_manager.onTabModification.subscribe(this.resetParent);
      window.tab_manager.init();
    }
    window.tab_module.show();
 	window.tab_manager.load(tabReqObject);
 }

 this.resetNode=function(obj1, obj2, obj3){
    var closed_obj=obj2[0];
    if(closed_obj.node)
    	closed_obj.node.tree.removeChildren(closed_obj.node);
 }

 this.resetParent=function(obj1, obj2, obj3){
	this.resetDynamic();
 }
 
 this.loadSearch=function(node,fnLoadComplete){
 	var tabReqObject= node.data;
    tabReqObject.label=node.label;
    tabReqObject.node=node;
    if(tabReqObject.URL!=undefined){
    	if(this.tree.lTV.config.module==undefined || document.getElementById(this.tree.lTV.config.module)==null){
    		window.location=serverRoot +"/app/template/Search.vm/node/"+ tabReqObject.ID;
    	}else{
    	   this.tree.lTV.displayTab(tabReqObject);
    	   if(fnLoadComplete!=undefined)fnLoadComplete();
    	}
    }
 }
 
 this.loadStoredSearches=function(node, fnLoadComplete)  {   
    var tabReqObject= node.data;
    tabReqObject.label=node.label;
    tabReqObject.node=node;
    
    var callback={
      success:function(oResponse){
        var oResults = eval("(" + oResponse.responseText + ")"); 
        if((oResults.ResultSet.Result) && (oResults.ResultSet.Result.length)) {  
    		function sortByDesc(a,b)
     		{
     			var aName = a.brief_description.toLowerCase();
     			var bName = b.brief_description.toLowerCase();
     			if (aName < bName){
     		        return -1;
     		     }else if (aName > bName){
     		       return  1;
     		     }else{
     		       return 0;
     		     }
     		}
    		oResults.ResultSet.Result.sort(sortByDesc);
           for (var ssC=0; ssC<oResults.ResultSet.Result.length;  ssC++) {   
               var cpNode=new YAHOO.widget.TextNode({label:oResults.ResultSet.Result[ssC].brief_description,
                     		ID:"ss."+oResults.ResultSet.Result[ssC].id,
                     		SS_ID:oResults.ResultSet.Result[ssC].id,
                     		URL:serverRoot +'/REST/search/saved/' + oResults.ResultSet.Result[ssC].id + '',
                     		TITLE:oResults.ResultSet.Result[ssC].description},oResponse.argument.node,false);
                   
 				   cpNode.isLeaf=true;
 				   oResponse.argument.lTV.tree.searches.push(cpNode.data.ID);
                   if(oResponse.argument.lTV.need_expansion.contains("ss."+oResults.ResultSet.Result[ssC].id)){
                   	  oResponse.argument.lTV.tree.fireEvent("labelClick",cpNode);
                   }
           }   
        } 
        oResponse.argument.fnLoadComplete();
      },
      failure:function(oResponse){
        oResponse.argument.fnLoadComplete();
      },
      cache:false, // Turn off caching for IE
      argument:{"node":node,"fnLoadComplete":fnLoadComplete,"lTV":lTV}
    };
    
																		           //YAHOO.util.Connect.ayncRequest('GET',this.obj.URL,this.initCallback,null,this);
    YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/search/saved?XNAT_CSRF=' + window.csrfToken + '&format=json&stamp='+ (new Date()).getTime(),callback,null);

 }

    //wrangleTabs('#search_tabs');
    //clickWrangledTab();

}

