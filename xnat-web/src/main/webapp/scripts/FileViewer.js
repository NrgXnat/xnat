// TODO: HACKITY HACK HACK HACK. Because of scope issues in the file management functions, it's difficult to tell the
// upload form that it needs to reload the page on completion of operations. Eventually table population should come
// completely via REST to the back-end, but we don't currently live in that world.


function FileViewer(_obj){
	this.loading=0;
	this.requestRender=false;
    this.requiresRefresh = false;
    this.maintainLogin = false;
	this.obj=_obj;
	if(this.obj.categories==undefined){
        this.obj.categories = {};
        this.obj.categories.ids = [];
	}
	
	if(this.obj.categories["misc"]==undefined || this.obj.categories["misc"]==null){
        this.obj.categories["misc"] = {};
        this.obj.categories["misc"].cats = [];
	}
	
	this.init=function(refreshCatalog){
		if(this.loading==0){
			this.loading=1;
			this.resetCounts();
            if (refreshCatalog) {
                this.catalogRefresh();
            } else {
                this.getCatalog();
			}
		}else if(this.loading==1){
			//in process
		}else{
			//loaded
			if(this.requestRender){
				this.render();
			}
		}
	};
	
    this.getCatalog = function() {
        var catCallback={
            success:this.processCatalogs,
            failure:this.handleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };

        YAHOO.util.Connect.asyncRequest('GET',this.obj.uri + '/resources?all=true&format=json&file_stats=true&sortBy=category,cat_id,label&timestamp=' + (new Date()).getTime(),catCallback,null,this);
    };

	this.handleFailure=function() {
        if (!window.leaving) {
            closeModalPanel("refresh_file");
            xmodal.message('File Viewer', "Error loading files");
        }
	};
	   
	this.removeFile=function(item){		
		if(showReason){
			var justification=new XNAT.app.requestJustification("file","File Deletion Dialog",this._removeFile,this);
			justification.item=item;	
		}else{
			passThroughfunc = this._removeFile;
			passThroughObj = this;

            window.viewer.requiresRefresh = true;

			var handleYes = function() {
			    //user confirms the deletion of this item;
			    //this method would perform that deletion;
			    //when ready, hide the SimpleDialog:
			    var passthrough= new XNAT.app.passThrough(passThroughfunc,passThroughObj);
			    passthrough.item=item;
			    passthrough.fire();
			    this.hide();
                this.destroy();
			};
			var handleNo = function() {
			    //user cancels item deletion; this method
			    //would handle the cancellation of the
			    //process.
			    //when ready, hide the SimpleDialog:
			    this.hide();
                this.destroy();
			};
			confirm_dialog =  new YAHOO.widget.SimpleDialog("file_remove_confirm",  
		             { width: "300px", 
		               fixedcenter: true, 
		               visible: false, 
		               draggable: false, 
		               modal: true,
		               close: true, 
		               text: "Confirm File Remove?", 
		               icon: YAHOO.widget.SimpleDialog.ICON_WARN, 
		               constraintoviewport: true, 
                    buttons: [
                        { text: "Yes", handler: handleYes},
                        { text: "No", handler: handleNo, isDefault: true  }
                    ]
		             } ); 
			confirm_dialog.setHeader("Confirm Remove!");
			confirm_dialog.render("page_body");
			confirm_dialog.show();
			confirm_dialog.bringToTop();

		}
	};

    this._removeFile = function (arg1, arg2, container) {
        var _this = this,
            event_reason = (container == undefined || container.dialog == undefined) ? "" : container.dialog.event_reason;
        this.initCallback = {
            success: function (obj1) {
                _this.refreshCatalogs("file");
            },
            failure: function (o) {
                closeModalPanel("file");
                displayError("ERROR " + o.status + ": Failed to delete file.");
            },
            cache: false, // Turn off caching for IE
            scope: this
        };

        openModalPanel("file", "Deleting '" + container.item.file_name + "'");

        var params = "";
        params += "event_reason=" + event_reason;
        params += "&event_type=WEB_FORM";
        params += "&event_action=File Deleted";

        YAHOO.util.Connect.asyncRequest('DELETE', container.item.uri + '?XNAT_CSRF=' + csrfToken + '&' + params, this.initCallback, null, this);
    };

    this.removeReconstruction=function(item){
	   if(showReason){
			var justification=new XNAT.app.requestJustification("file","Folder Deletion Dialog",this._removeReconstruction,this);
			justification.item=item;	
		}else{
			passThroughfunc = this._removeReconstruction;
			passThroughObj = this;

           window.viewer.requiresRefresh = true;

			var handleYes = function() {
			    //user confirms the deletion of this item;
			    //this method would perform that deletion;
			    //when ready, hide the SimpleDialog:
			    var passthrough= new XNAT.app.passThrough(passThroughfunc,passThroughObj);
			passthrough.item=item;
			passthrough.fire();
			    this.hide();
                this.destroy();
			};
			var handleNo = function() {
			    //user cancels item deletion; this method
			    //would handle the cancellation of the
			    //process.
			    //when ready, hide the SimpleDialog:
			    this.hide();
                this.destroy();
			};
			confirm_dialog =  new YAHOO.widget.SimpleDialog("file_remove_confirm",  
		             { width: "300px", 
		               fixedcenter: true, 
		               visible: false, 
		               draggable: false, 
		               modal: true,
		               close: true, 
		               text: "Confirm Resource Removal?",
		               icon: YAHOO.widget.SimpleDialog.ICON_WARN, 
		               constraintoviewport: true, 
                    buttons: [
                        { text: "Yes", handler: handleYes},
                        { text: "No", handler: handleNo, isDefault: true  }
                    ]
		             } ); 
			confirm_dialog.setHeader("Confirm Remove!");
			confirm_dialog.render("page_body");
			confirm_dialog.show();
			confirm_dialog.bringToTop();
		}
   };

    this._removeReconstruction = function (arg1, arg2, container) {
        var _this = this,
            event_reason = (container == undefined || container.dialog == undefined) ? "" : container.dialog.event_reason;
        this.initCallback = {
            success: function (obj1) {
                _this.refreshCatalogs("file");
            },
            failure: function (o) {
                closeModalPanel("file");
                displayError("ERROR " + o.status + ": Failed to delete file.");
            },
            cache: false, // Turn off caching for IE
            scope: this
        };

        openModalPanel("file","Deleting resource '" + container.item.reconId +"'");
	    
	   var params="";		
	   params+="event_reason="+event_reason;
	   params+="&event_type=WEB_FORM";
	   params+="&event_action=File Deleted";
	    
	   YAHOO.util.Connect.asyncRequest('DELETE',this.obj.uri +'/reconstructions/' + container.item.reconId + '?XNAT_CSRF=' + csrfToken + '&'+params,this.initCallback,null,this)
   };
   
   this.removeCatalog=function(item){	
		if(showReason){
			var justification=new XNAT.app.requestJustification("file","Folder Deletion Dialog",this._removeCatalog,this);
			justification.item=item;	
		}else{
			passThroughfunc = this._removeCatalog;
			passThroughObj = this;

            window.viewer.requiresRefresh = true;

			var handleYes = function() {
			    //user confirms the deletion of this item;
			    //this method would perform that deletion;
			    //when ready, hide the SimpleDialog:
			    var passthrough= new XNAT.app.passThrough(passThroughfunc,passThroughObj);
			passthrough.item=item;
			passthrough.fire();
			    this.hide();
                this.destroy();
			};
			var handleNo = function() {
			    //user cancels item deletion; this method
			    //would handle the cancellation of the
			    //process.
			    //when ready, hide the SimpleDialog:
			    this.hide();
                this.destroy();
			};
			confirm_dialog =  new YAHOO.widget.SimpleDialog("file_remove_confirm",  
		             { width: "300px", 
		               fixedcenter: true, 
		               visible: false, 
		               draggable: false, 
		               modal: true,
		               close: true, 
		               text: "Confirm Catalog Remove?", 
		               icon: YAHOO.widget.SimpleDialog.ICON_WARN, 
		               constraintoviewport: true, 
                    buttons: [
                        { text: "Yes", handler: handleYes},
                        { text: "No", handler: handleNo, isDefault: true  }
                    ]
		             } ); 
			confirm_dialog.setHeader("Confirm Remove!");
			confirm_dialog.render("page_body");
			confirm_dialog.show();
			confirm_dialog.bringToTop();
		}
   };
   
   this._removeCatalog=function(arg1,arg2,container){
		var _this = this,
            event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
		this.initCallback={
			success:function(obj1){
                _this.refreshCatalogs("file");
			},
			failure:function(o){
                if (!window.leaving) {
                    closeModalPanel("file");
                    displayError("ERROR " + o.status+ ": Failed to delete file.");
                }
			},
            cache:false, // Turn off caching for IE
			scope:this
		};

       window.viewer.requiresRefresh = true;
		
		openModalPanel("file","Deleting folder '" + container.item.file_name +"'");
		var params="";		
		params+="event_reason="+event_reason;
		params+="&event_type=WEB_FORM";
		params+="&event_action=Folder Deleted";
		YAHOO.util.Connect.asyncRequest('DELETE',container.item.uri+ '?XNAT_CSRF=' + csrfToken + '&'+params,this.initCallback,null,this);
   };
   
   this.getScan=function(sc, sid){
   		var gsScans=this.obj.categories[sc];
   		if(gsScans!=undefined && gsScans!=null){
			for(var gssC=0;gssC<gsScans.length;gssC++){
				if(gsScans[gssC].id==sid){
					return gsScans[gssC];
				}
			}
   		}
		
		gsScans=null;
   		return null;
   };
   
   this.clearCatalogs=function(o){
   		var scans;
   		//clear catalogs
        for (var catC = 0; catC < this.obj.categories.ids.length; catC++) {
   			scans=this.obj.categories[this.obj.categories.ids[catC]];
   			for(var sC=0;sC<scans.length;sC++){
   				scans[sC].cats =[];
   			}
   			
   			scans=null;
   		}
   		this.obj.categories["misc"].cats=[];
   };
   
   this.processCatalogs=function(o){
   		closeModalPanel("catalogs");
   		this.clearCatalogs();
   		
    	var catalogs= eval("(" + o.responseText +")").ResultSet.Result;
    	
    	for(var catC=0;catC<catalogs.length;catC++){
    		var scan=this.getScan(catalogs[catC].category,catalogs[catC].cat_id);
    		if(scan!=null){
    			if(scan.cats==null || scan.cats==undefined){
    				scan.cats=[];
    			}
    			scan.cats.push(catalogs[catC]);
    		}else{
    			if(this.obj.categories["misc"].cats==null || this.obj.categories["misc"].cats==undefined){
    				this.obj.categories["misc"].cats=[];
    			}
    			this.obj.categories["misc"].cats.push(catalogs[catC]);
    		}
    	}
    	
    	this.loading=3;
    	
    	if(this.requestRender){
    		this.render();
    	}
   };
   
   this.resetCounts=function(){
        for (var catC = 0; catC < this.obj.categories.ids.length; catC++) {
            var scans=this.obj.categories[this.obj.categories.ids[catC]];
   			scans=null;
   		}
    };
   
   this.refreshCatalogs=function(msg_id){
		closeModalPanel(msg_id);
		openModalPanel("catalogs","Refreshing Catalog Information");
		var catCallback={
			success:this.processCatalogs,
			failure:this.handleFailure,
            cache:false, // Turn off caching for IE
			scope:this
        };
	
		this.requestRender=true;
		YAHOO.util.Connect.asyncRequest('GET',this.obj.uri + '/resources?all=true&format=json&file_stats=true&timestamp=' + (new Date()).getTime(),catCallback,null,this);
    };
	
	this.render=function(){
		if(this.loading==0){
			this.requestRender=true;
			openModalPanel("catalogs","Loading File Summaries");
			this.init();
		}else if(this.loading==1){
			//in process
			this.requestRender=true;
		}else{
	   		if(this.panel!=undefined){
	   			this.panel.destroy();
	   		}
	   	
	   	    this.panel=new YAHOO.widget.Dialog("fileListing",{close:true,
			   width:"780px",height:"550px",underlay:"shadow",modal:true,fixedcenter:true,visible:false,draggable:true});
			this.panel.setHeader("File Manager");
		
			this.catalogClickers=[];
					
			var bd = document.createElement("div");
			
			var treediv=document.createElement("div");
			treediv.id="fileTree";
			treediv.style.overflow="auto";
			treediv.style.height="460px";
			treediv.style.width="740px";
			bd.appendChild(treediv);
			try{
				var tree = new YAHOO.widget.TreeView(treediv);
				var root = tree.getRoot();
									
				var total_size=0;
				
				if(this.obj.categories["misc"].cats.length>0){
				
					var parent = new YAHOO.widget.TaskNode({label: "Resources", expanded: true,checked:true}, root);
					parent.labelStyle = "icon-cf"; 
				
					for(var rCatC=0;rCatC<this.obj.categories["misc"].cats.length;rCatC++){
						var cat=this.obj.categories["misc"].cats[rCatC];
						var catNode=null;
					
						var lbl;
						if(cat.label!=""){
							lbl=cat.label;
						}else{
							lbl="NO LABEL";
						}
						cat.uri=this.obj.uri + "/resources/" + cat.xnat_abstractresource_id;
						cat.canEdit=this.obj.canEdit;
						cat.canDelete=this.obj.canDelete;
						catNode=new YAHOO.widget.CatalogNode({label: lbl}, parent,cat);
						this.catalogClickers.push(catNode);
					}
				}
				
				for(var cC=0;cC<this.obj.categories.ids.length;cC++){
					var catName=this.obj.categories.ids[cC];
					if(this.obj.categories[catName].length>0){
				
						var parent = new YAHOO.widget.TaskNode({label: catName, expanded: true,checked:true}, root);
						parent.labelStyle = "icon-cf"; 
						
						var scans=this.obj.categories[catName];
						for(var rScanC=0;rScanC<scans.length;rScanC++){
							var scan=scans[rScanC];
							var scanNode=null;
							if(scan.cats!=null && scan.cats!=undefined && scan.cats.length>0){
								
								var l = (scan.label!=undefined)?scan.label:scan.id;
								if(parent.label == "reconstructions"){
									l +="&nbsp;&nbsp;<a onclick=\"window.viewer.removeReconstruction({reconId:'" + scan.id + "'});\"><img style='height:14px' border='0' src='" +serverRoot+"/images/delete.gif'/></a>";
								}
								var scanNode=new YAHOO.widget.TaskNode({label:l, expanded: true,checked:true}, parent);
								scanNode.labelStyle = "icon-cf";
								
								for(var scanCC=0;scanCC<scan.cats.length;scanCC++){
									var cat = scan.cats[scanCC];
									cat.uri=this.obj.uri + "/" + catName+ "/" + scan.id + "/resources/" + cat.xnat_abstractresource_id;
									cat.canEdit=this.obj.canEdit;
									cat.canDelete=this.obj.canDelete;
									catNode=new YAHOO.widget.CatalogNode({label: (cat.label!="")?cat.label:"NO LABEL"}, scanNode,cat);
									this.catalogClickers.push(catNode);

								}
							}else{
								scanNode=new YAHOO.widget.TextNode({label:(scan.label!=undefined)?scan.label:scan.id, expanded: false}, parent);
							}
						}
						scans=null;
					}
				}
				
				this.panel.setBody(bd);
					  
				var foot = document.createElement("div");
				foot.style.textAlign="right";
				
				var fTable=document.createElement("table");
				var fTbody=document.createElement("tbody");
				var fTr=document.createElement("tr");
				fTable.appendChild(fTbody);
				fTable.width="100%";
				fTable.align="center";
				fTbody.appendChild(fTr);
				foot.appendChild(fTable);
				
				var fTd1=document.createElement("td");
				fTd1.align="left";
				fTr.appendChild(fTd1);
				
				var fTd2=document.createElement("td");
				fTd2.align="right";
				fTr.appendChild(fTd2);
				
				
				if(this.obj.canEdit){
					var dButton3=document.createElement("input");
					dButton3.type="button";
					dButton3.value="Add Folder";
					fTd1.appendChild(dButton3);
					
					var oPushButtonD3 = new YAHOO.widget.Button(dButton3);
		  		    oPushButtonD3.subscribe("click",function(o){
		  		    	var upload=new AddFolderForm(this.obj);
		  		    	upload.render();
		  		    },this,true);
					
					var dButton1=document.createElement("input");
					dButton1.type="button";
					dButton1.value="Upload Files";
					fTd1.appendChild(dButton1);
					
					var oPushButtonD1 = new YAHOO.widget.Button(dButton1);
		  		    oPushButtonD1.subscribe("click",function(o){
		  		    	try{
			  		    	var upload=new UploadFileForm(this.obj);
			  		    	upload.render();
		  		    	}catch(e){
                            xmodal.message('File Viewer Error', e.toString());
		  		    	}
		  		    },this,true);

                    var updateButton = document.createElement("input");
                    updateButton.type = "button";
                    updateButton.value = "Update File Data";
                    fTd1.appendChild(updateButton);

                    var oPushUpdateButton = new YAHOO.widget.Button(updateButton);
                    oPushUpdateButton.subscribe("click", this.catalogRefresh, this, true);
				}
							
				var dType=document.createElement("select");
				dType.id="download_type_select";
				dType.options[0]=new Option("zip","zip",true,true);
				dType.options[1]=new Option("tar","tar");
				dType.options[2]=new Option("tar.gz","tar.gz");
				dType.style.marginRight="10px";
                dType.style.position="relative";
                dType.style.top="-7px";
				fTd2.appendChild(dType);
				
				var dButton2=document.createElement("input");
				dButton2.type="button";
				dButton2.value="Download";
				fTd2.appendChild(dButton2);
				
				
				var dButton4=document.createElement("input");
				dButton4.type="button";
				dButton4.value="Close";
				fTd2.appendChild(dButton4);
				
				var oPushButtonD4 = new YAHOO.widget.Button(dButton4);
	  		    oPushButtonD4.subscribe("click",function(o){
	  		    	this.panel.hide();
                    if (window.viewer.requiresRefresh) {
                        window.location.reload();
                    }
	  		    },this,true);
				
				var oPushButtonD2 = new YAHOO.widget.Button(dButton2);
	  		    oPushButtonD2.subscribe("click",function(o){
	  		    	var dType=document.getElementById("download_type_select");
	  		    	var resources="";
	  		    	for(var ccC=0;ccC<this.catalogClickers.length;ccC++){
                        // slightly ridiculous, but the only place to check to see if there are subfiles or not is the label,
                        // which will fail to have the number of files listed
	  		    		if(this.catalogClickers[ccC].checked &&
                            this.catalogClickers[ccC].label.indexOf('&nbsp;&nbsp; files') == -1 &&
                            this.catalogClickers[ccC].label.indexOf('&nbsp;&nbsp;0 files') == -1) {

	  		    			if(resources!="")resources+=",";
	  		    			resources+=this.catalogClickers[ccC].xnat_abstractresource_id;
	  		    		}
	  		    	}
	  		    	if(resources==""){
                        xmodal.message('File Viewer', "No files found.");
	  		    		return;
	  		    	}
	  		    	var destination=this.obj.uri + "/resources/"+resources + "/files?structure=improved&all=true&format="+ dType.options[dType.selectedIndex].value;
					
	  		    	this.panel.hide();
	  		    	mySimpleDialog = new YAHOO.widget.SimpleDialog("dlg", { 
						width: "20em", 
						fixedcenter:true,
						modal:true,
	    				visible:false,
						draggable:true });
					mySimpleDialog.setHeader("Preparing Download");
					mySimpleDialog.setBody("Your download should begin within 30 seconds.  If you encounter technical difficulties, you can restart the download using this <a href='" + destination +"'>link</a>.");
					
	  		    	window.location=destination;
	  		    },this,true);
	  		    
				this.panel.setFooter(foot);
					  
				this.panel.selector=this;
				
				tree.render();   
							
				this.loading=3;
				this.requestRender=false;
			}catch(o){
                xmodal.message('File Viewer Error', o.toString());
			}
			this.panel.render("page_body");
			this.panel.show();
		}
   };

    this.catalogRefresh = function () {
        try {
            var updateOptions = {};
            updateOptions.width = 420;
            updateOptions.height = 240;
            updateOptions.title = 'Update File Data';
            updateOptions.content =
                'This operation regenerates the stored metadata from file resources in the archive. ' +
                'This can take anywhere from a few moments to many minutes, so make certain that you ' +
                'want to do this update. Click <b>OK</b> to start the update operation or <b>Cancel</b> ' +
                'if you want to wait.';
            updateOptions.okAction = this.catalogRefreshOk;
            updateOptions.cancelAction = this.catalogRefreshCancel;
            xmodal.open(updateOptions);
        } catch (e) {
            xmodal.message('File Viewer Error', e.toString());
        }
    };

    this.catalogRefreshOk = function () {
        var _this = this,
            catalogRefreshCallback = {
            success: function () {
                xmodal.loading.close();
                xmodal.message({
                    title: _this.obj.objectId + ' Refreshed',
                    content: 'The aggregate file count and size values have been updated for your ' +
                             _this.obj.objectType + '. Click OK to reload the page.',
                    action: function() { window.location.reload() }
                });
            },
            failure: function (o) {
                xmodal.loading.close();
                xmodal.message({
                    title:   'Error',
                    content: 'An unexpected error has occurred while processing ' +
                             _this.obj.objectType + ' ' + _this.obj.objectId + '. Please ' +
                             'contact your administrator. Status code: ' + o.status
                });
            },
            scope: this,
            cache: false
        };
        YAHOO.util.Connect.asyncRequest('POST', this.obj.refresh + '&timestamp=' + (new Date()).getTime(), catalogRefreshCallback, null);
        //xModalCloseNew();
        xmodal.loading.open('Please wait...');
    };

    this.catalogRefreshCancel = function () {
        window.viewer.loading = 0;
        //xModalCloseNew();
    };
}

YAHOO.widget.TaskNode = function(oData, oParent, expanded, checked) {
	YAHOO.widget.TaskNode.superclass.constructor.call(this,oData,oParent,expanded);
    this.setUpCheck(checked || oData.checked);

};

YAHOO.extend(YAHOO.widget.TaskNode, YAHOO.widget.TextNode, {

    /**
     * True if checkstate is 1 (some children checked) or 2 (all children checked),
     * false if 0.
     * @type boolean
     */
    checked: false,

    /**
     * checkState
     * 0=unchecked, 1=some children checked, 2=all children checked
     * @type int
     */
    checkState: 0,

	/**
     * The node type
     * @property _type
     * @private
     * @type string
     * @default "TextNode"
     */
    _type: "TaskNode",
	
	taskNodeParentChange: function() {
        //this.updateParent();
    },
	
    setUpCheck: function(checked) {
        // if this node is checked by default, run the check code to update
        // the parent's display state
        if (checked && checked === true) {
            this.check();
        // otherwise the parent needs to be updated only if its checkstate 
        // needs to change from fully selected to partially selected
        } else if (this.parent && 2 === this.parent.checkState) {
             this.updateParent();
        }

        // set up the custom event on the tree for checkClick
        /**
         * Custom event that is fired when the check box is clicked.  The
         * custom event is defined on the tree instance, so there is a single
         * event that handles all nodes in the tree.  The node clicked is 
         * provided as an argument.  Note, your custom node implentation can
         * implement its own node specific events this way.
         *
         * @event checkClick
         * @for YAHOO.widget.TreeView
         * @param {YAHOO.widget.Node} node the node clicked
         */
        if (this.tree && !this.tree.hasEvent("checkClick")) {
            this.tree.createEvent("checkClick", this.tree);
        }

		this.tree.subscribe('clickEvent',this.checkClick);
        this.subscribe("parentChange", this.taskNodeParentChange);


    },

    /**
     * The id of the check element
     * @for YAHOO.widget.TaskNode
     * @type string
     */
    getCheckElId: function() { 
        return "ygtvcheck" + this.index; 
    },

    /**
     * Returns the check box element
     * @return the check html element (img)
     */
    getCheckEl: function() { 
        return document.getElementById(this.getCheckElId()); 
    },

    /**
     * The style of the check element, derived from its current state
     * @return {string} the css style for the current check state
     */
    getCheckStyle: function() { 
        return "ygtvcheck" + this.checkState;
    },


   /**
     * Invoked when the user clicks the check box
     */
    checkClick: function(oArgs) { 
		var node = oArgs.node;
		var target = YAHOO.util.Event.getTarget(oArgs.event);
		if (YAHOO.util.Dom.hasClass(target,'ygtvspacer')) {
	        if (node.checkState === 0) {
	            node.check();
	        } else {
	            node.uncheck();
	        }

	        node.onCheckClick(node);
	        this.fireEvent("checkClick", node);
		    return false;
		}
    },

    /**
     * Override to get the check click event
     */
    onCheckClick: function() { 
        
    },

    /**
     * Refresh the state of this node's parent, and cascade up.
     */
    updateParent: function() { 
        var p = this.parent;

        if (!p || !p.updateParent) {
            return;
        }

        var somethingChecked = false;
        var somethingNotChecked = false;

        for (var i=0, l=p.children.length;i<l;i=i+1) {

            var n = p.children[i];

            if ("checked" in n) {
                if (n.checked) {
                    somethingChecked = true;
                    // checkState will be 1 if the child node has unchecked children
                    if (n.checkState === 1) {
                        somethingNotChecked = true;
                    }
                } else {
                    somethingNotChecked = true;
                }
            }
        }

        if (somethingChecked) {
            p.setCheckState( (somethingNotChecked) ? 1 : 2 );
        } else {
            p.setCheckState(0);
        }

        p.updateCheckHtml();
        p.updateParent();
    },

    /**
     * If the node has been rendered, update the html to reflect the current
     * state of the node.
     */
    updateCheckHtml: function() { 
        if (this.parent && this.parent.childrenRendered) {
            this.getCheckEl().className = this.getCheckStyle();
        }
    },

    /**
     * Updates the state.  The checked property is true if the state is 1 or 2
     * 
     * @param state - the new check state
     */
    setCheckState: function(state) { 
        this.checkState = state;
        this.checked = (state > 0);
    },

    /**
     * Check this node
     */
    check: function() { 
        this.setCheckState(2);
        for (var i=0, l=this.children.length; i<l; i=i+1) {
            var c = this.children[i];
            if (c.check) {
                c.check();
            }
        }
        this.updateCheckHtml();
        this.updateParent();
    },

    /**
     * Uncheck this node
     */
    uncheck: function() { 
        this.setCheckState(0);
        for (var i=0, l=this.children.length; i<l; i=i+1) {
            var c = this.children[i];
            if (c.uncheck) {
                c.uncheck();
            }
        }
        this.updateCheckHtml();
        this.updateParent();
    },
    // Overrides YAHOO.widget.TextNode

    getContentHtml: function() {                                                                                                                                           
        var sb = [];                                                                                                                                                       
        sb[sb.length] = '<td';                                                                                                                                             
        sb[sb.length] = ' id="' + this.getCheckElId() + '"';                                                                                                               
        sb[sb.length] = ' class="' + this.getCheckStyle() + '"';                                                                                                           
        sb[sb.length] = '>';                                                                                                                                               
        sb[sb.length] = '<div class="ygtvspacer"></div></td>';                                                                                                             
                                                                                                                                                                           
        sb[sb.length] = '<td><span';                                                                                                                                       
        sb[sb.length] = ' id="' + this.labelElId + '"';                                                                                                                    
        if (this.title) {                                                                                                                                                  
            sb[sb.length] = ' title="' + this.title + '"';                                                                                                                 
        }                                                                                                                                                                  
        sb[sb.length] = ' class="' + this.labelStyle  + '"';                                                                                                               
        sb[sb.length] = ' >';                                                                                                                                              
        sb[sb.length] = this.label;                                                                                                                                        
        sb[sb.length] = '</span></td>';                                                                                                                                    
        return sb.join("");                                                                                                                                                
    }  
});

    function number_format( number, decimals, dec_point, thousands_sep ) {
      	var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
  		var d = dec_point == undefined ? "," : dec_point;
  		var t = thousands_sep == undefined ? "." : thousands_sep, s = n < 0 ? "-" : "";
  		var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
  		return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
    }

	

   function size_format (filesize) {
      if (filesize >= 1073741824) {
         filesize = number_format(filesize / 1073741824, 2, '.', '') + ' GB';
      } else {
         if (filesize >= 1048576) {
             filesize = number_format(filesize / 1048576, 2, '.', '') + ' MB';
         } else {
             if (filesize >= 1024) {
                 filesize = number_format(filesize / 1024, 0) + ' KB';
             } else {
                 filesize = number_format(filesize, 0) + ' bytes';
            }
        }
    }
      return filesize;
   }
   
   YAHOO.widget.CatalogNode = function(oData, oParent, catalog) {
	YAHOO.widget.CatalogNode.superclass.constructor.call(this,oData,oParent,false,(catalog.file_count>0));
	this.cat=catalog;
	this.renderCatalog(catalog);
};

XNAT.app.showTags=function(scanID, file){
	window.open(serverRoot +"/REST/services/dicomdump?src=/archive/projects/"+ XNAT.data.context.projectID + "/subjects/"+ XNAT.data.context.subjectID + "/experiments/"+ XNAT.data.context.ID + "/scans/"+ scanID + "/resources/DICOM/files/"+ file + "&format=html&requested_screen=DicomFileTable.vm");
};

YAHOO.extend(YAHOO.widget.CatalogNode, YAHOO.widget.TaskNode, {
	renderCatalog:function(cat){
		this.xnat_abstractresource_id=cat.xnat_abstractresource_id;
		this.labelStyle = "icon-cf"; 
		
		if(cat.files!=undefined && cat.files!=null){
			this.renderFiles();				
		}else if(cat.file_count>0){
			this.setDynamicLoad(function(node, fnLoadComplete){
		 		var callback={
			      success:function(oResponse){
			        oResponse.argument.catNode.cat.files = (eval("(" + oResponse.responseText + ")")).ResultSet.Result; 
			        oResponse.argument.catNode.renderFiles();   
			        oResponse.argument.fnLoadComplete();
			      },
			      failure:function(oResponse){
			        oResponse.argument.fnLoadComplete();
			      },
                  cache:false, // Turn off caching for IE
			      argument:{"fnLoadComplete":fnLoadComplete,catNode:this}
			    };
			    
				YAHOO.util.Connect.asyncRequest('GET',this.cat.uri + '/files?format=json&timestamp=' + (new Date()).getTime(),callback,null);
			},this);
		}

		if(cat.file_count!=undefined && cat.file_count!=null){
		  this.label+="&nbsp;&nbsp;" + cat.file_count + " files, "+ size_format(cat.file_size);
		}else{
		  this.label+="&nbsp;&nbsp;" + size_format(cat.file_size);
		}
		if(this.cat.format!=""){
		   this.label +="&nbsp;"+ this.cat.format +"";
		}
		if(this.cat.content!=""){
		   this.label +="&nbsp;"+ this.cat.content +"";
		}
		if(this.cat.tags!=""){
		   this.label +="&nbsp;("+ this.cat.tags +")";
		}
		if(this.cat.canDelete)
			this.label +="&nbsp;&nbsp;<a onclick=\"window.viewer.removeCatalog({file_name:'" + cat.label +"',uri:'" + this.cat.uri + "',id:'" + cat.xnat_abstractresource_id + "'});\"><img style='height:14px' border='0' src='" +serverRoot+"/images/delete.gif'/></a>";
	},
    renderFiles:function(){

        var folderArray = [];
        for(var fC=0;fC<this.cat.files.length;fC++){
            var file=this.cat.files[fC];
            var size=parseInt(file.Size);
            var path=file.URI.substring(file.URI.indexOf("/files/")+7);
            var root=file.URI.substring(0,file.URI.indexOf("/files/")+6)

            var splitPath =  path.split("/");
            var fileNode = this;


            for(var sp = 0; sp < splitPath.length; sp++){

                if(sp == splitPath.length - 1){
                    var filename = splitPath[sp];
                    filename=filename.split(".").join(".&shy;");
                    filename=filename.split("_").join("_&shy;");
                    var _html="<a target='_blank' onclick=\"location.href='" +serverRoot + file.URI + "';\">" + filename + "</a>";
                    if(this.cat.label=="DICOM"){
                        _html +="&nbsp; <a  onclick=\"window.open('" +serverRoot + file.URI + "?format=image/jpeg');return false;\">Image</a>";

                        if(XNAT.data.context.isImageSession && XNAT.data.context.projectID!=undefined &&
                            XNAT.data.context.subjectID!=undefined &&
                            XNAT.data.context.ID!=undefined){
                            _html +="&nbsp; <a href='#' onclick=\"XNAT.app.showTags('" + this.cat.cat_id + "','" + path +"');return false;\">Tags</a>";
                        }
                    }
                    if(file.file_format!=""){
                        _html +=" "+ file.file_format +"";
                    }
                    if(file.file_content!=""){
                        _html +=" "+ file.file_content +"";
                    }
                    if(file.file_tags!=""){
                        _html +=" ("+ file.file_tags +")";
                    }
                    _html +="&nbsp; "+size_format(size) +"";

                    if(this.cat.canDelete)
                        _html +="&nbsp; <a onclick=\"window.viewer.removeFile({file_name:'" + path +"',uri:'" + serverRoot + file.URI + "'});\"><img style='height:14px' border='0' src='" +serverRoot+"/images/delete.gif'/></a>";

                    var fileNode=new YAHOO.widget.HTMLNode({html: _html, expanded: false}, fileNode);
                    fileNode.labelStyle = "icon-f";

                } else {
                    var folderArrayKey = '';

                    for(var pathparts = 0; pathparts <= sp; pathparts++) {
                        folderArrayKey += splitPath[pathparts] + '/';
                    }

                    if(   typeof folderArray[folderArrayKey] !== 'undefined' && typeof folderArray[folderArrayKey] !== null ){

                        fileNode = folderArray[folderArrayKey];

                    } else {

                        var newPath = root;
                        for(var np=0;np<=sp;np++){
                            newPath = newPath + "/" + splitPath[np];
                        }
                        newPath = newPath + '/*';
                        var _lbl="<a target='_blank' onclick=\"location.href='" +serverRoot + newPath + "?format=zip';\">" + splitPath[sp] + "</a>"
                        if(file.file_format!=""){
                            _lbl +="&nbsp;"+ file.file_format +"";
                        }
                        if(file.file_content!=""){
                            _lbl +="&nbsp;"+ file.file_content +"";
                        }
                        if(file.file_tags!=""){
                            _lbl +="&nbsp;("+ file.file_tags +")";
                        }

                        if(this.cat.canDelete)
                            _lbl +="&nbsp;&nbsp;<a onclick=\"window.viewer.removeFile({file_name:'" + path +"',uri:'" + serverRoot + newPath + "'});\"><img style='height:14px' border='0' src='" +serverRoot+"/images/delete.gif'/></a>";



                        fileNode=new YAHOO.widget.TaskNode({label: _lbl, expanded: false}, fileNode);
                        fileNode.labelStyle = "icon-f";

                        folderArray[folderArrayKey] = fileNode;

                    }
                }
            }
        }
    }
});
   
function UploadFileForm(_obj){
  	this.obj=_obj;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);
  
	this.render=function(){	
		this.panel=new YAHOO.widget.Dialog("fileUploadDialog",{close:true,
		   width:"480px",height:"420px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Upload File");
				
		var div = document.createElement("form");
		
		
		var table,tbody,tr,td,input;
   	  
//   	  var title=document.createElement("div");
//   	  title.style.marginTop="3px";
//   	  title.style.marginLeft="1px";
//   	  title.innerHTML="File Upload Form";
//   	  div.gappendChild(title);
//
//   	  div.appendChild(document.createElement("br"));

        var collection_form = document.createElement('fieldset');
        collection_form.style.marginTop="0px";
        collection_form.innerHTML = '<h3>Destination</h3>';
        div.appendChild(collection_form);

        table = document.createElement("table");
        tbody = document.createElement("tbody");
        table.appendChild(tbody);
        collection_form.appendChild(table);


        if(this.obj.categories!=undefined){
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Level";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="upload_level";
	   	  input.manager=this;
	   	  input.options[0]=new Option("SELECT","");
	   	  for(var cIdC=0;cIdC<this.obj.categories.ids.length;cIdC++){
	   	    input.options[input.options.length]=new Option(this.obj.categories.ids[cIdC],this.obj.categories.ids[cIdC]);
	   	    input.options[(input.options.length -1)].category=this.obj.categories[this.obj.categories.ids[cIdC]];
	   	  }
	   	  input.options[input.options.length]=new Option("resources","resources");
	   	  input.options[(input.options.length -1)].category=this.obj.categories["misc"];
	   	  
	   	  input.onchange=function(o){
	   	  	var item_select=document.getElementById("upload_item");
	   	  	var coll_select=document.getElementById("upload_collection");
	   	  	
	   	  	if(this.selectedIndex==0){
	   	  		while(item_select.options.length>0){
	   	  			item_select.remove(0);
	   	  		}
	   	  		while(coll_select.options.length>0){
	   	  			coll_select.remove(0);
	   	  		}
	   	  		item_select.disabled=true;
	   	  		coll_select.disabled=true;
	   	  	}else{
	   	  		while(item_select.options.length>0){
	   	  			item_select.remove(0);
	   	  		}
	   	  		while(coll_select.options.length>0){
	   	  			coll_select.remove(0);
	   	  		}
	   	  		var _sOption=this.options[this.selectedIndex];
	   	  		
	   	  		if(_sOption.value=="resources"){
	   	  			item_select.disabled=true;
	   	  			coll_select.disabled=false;
	   	  			
	   	  			while(coll_select.options.length>0){
		   	  			coll_select.remove(0);
		   	  		}
	   	  		
	   	  			for(var cC=0;cC<_sOption.category.cats.length;cC++){
						var cat=_sOption.category.cats[cC];
						if(cat.label==""){
							coll_select.options[coll_select.options.length]=new Option("NO LABEL",cat.xnat_abstractresource_id);
						}else{
							coll_select.options[coll_select.options.length]=new Option(cat.label,cat.xnat_abstractresource_id);
						}
	   	  			}
						if(coll_select.options.length==0){
							coll_select.options[coll_select.options.length]=new Option("NO LABEL","");
						}
	   	  		}else{
	   	  			coll_select.disabled=true;
	   	  			item_select.disabled=false;
		   	  		var scans=_sOption.category;
		   	  		
		   	  		item_select.options[item_select.options.length]=new Option("SELECT","");
		   	  		for(var catC=0;catC<scans.length;catC++){
		   	  			item_select.options[item_select.options.length]=new Option(scans[catC].id,scans[catC].id);
		   	  			item_select.options[(item_select.options.length -1)].scan=scans[catC];
		   	  		}
	   	  		}
	   	  	}
	   	  };
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
	   	  
	   	  
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Item";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="upload_item";
	   	  input.disabled=true;
	   	  input.manager=this;
	   	  input.onchange=function(o){
	   	  	var coll_select=document.getElementById("upload_collection");
	   	  	if(this.selectedIndex>0){
	   	  		coll_select.disabled=false;
	   	  		while(coll_select.options.length>0){
	   	  			coll_select.remove(0);
	   	  		}
	   	  		var _selectedO=this.options[this.selectedIndex];
   	  		
   	  			for(var cC=0;cC<_selectedO.scan.cats.length;cC++){
					var cat=_selectedO.scan.cats[cC];
					if(cat.label==""){
						coll_select.options[coll_select.options.length]=new Option("NO LABEL",cat.xnat_abstractresource_id);
					}else{
						coll_select.options[coll_select.options.length]=new Option(cat.label,cat.xnat_abstractresource_id);
					}
   	  			}
				if(coll_select.options.length==0){
                    xmodal.message('File Viewer', "Please create a folder (using the Add Folder dialog) before attempting to add files at this level.");
					coll_select.disabled=true;
				}
   	  			
	   	  	}else{
	   	  		coll_select.disabled=true;
	   	  		while(coll_select.options.length>0){
	   	  			coll_select.remove(0);
	   	  		}
	   	  	}

	   	  };
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
	   	  
	   	  
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Folder";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="upload_collection";
	   	  input.manager=this;
	   	  input.disabled=true;
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
   	  }else{    
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Folder";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="upload_collection";
	   	  input.manager=this;
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
	   	  
	   	  for(var cC=0;cC<this.obj.categories["misc"].cats.length;cC++){
				var cat=this.obj.categories["misc"].cats[catID];
				if(cat.label==""){
					input.options[input.options.length]=new Option("NO LABEL",cat.xnat_abstractresource_id);
				}else{
					input.options[input.options.length]=new Option(cat.label,cat.xnat_abstractresource_id);
				}
			}
				if(input.options.length==0){
					input.options[input.options.length]=new Option("NO LABEL","");
				}
   	  }
   	  
//   	  div.appendChild(document.createElement("br"));
   	  var file_form = document.createElement("fieldset");
   	  div.appendChild(file_form);
      file_form.style.margin="0px";
   	  //file_form.style.border="1px solid #DEDEDE";
   	  //file_form.appendChild(document.createElement("div"));
   	  file_form.innerHTML="<h3>File Information</h3>";

      table=document.createElement("table");
   	  tbody=document.createElement("tbody");
   	  table.appendChild(tbody);
   	  file_form.appendChild(table);
   	  
   	  //collection
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Rename";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.size=50;
   	  input.id="file_name";
   	  //input.style.fontSize = "99%";
   	  input.manager=this;
   	  td.appendChild(input);
   	  tr.appendChild(td);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //format
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Format";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="file_format";
   	  //input.style.fontSize = "99%";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //content
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Content";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  //input.style.fontSize = "99%";
   	  input.id="file_content";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  div.appendChild(file_form);
   	  
   	  
   	  //tags
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Tags";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  //input.style.fontSize = "99%";
   	  input.id="file_tags";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  //Overwrite
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Overwrite";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.type = 'checkbox';
   	  input.value = 'true';
   	  input.id="folder_overwrite";
   	  //input.style.fontSize = "99%";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //div.appendChild(document.createTextNode("File To Upload:"));
   	  
   	  //var form = document.createElement("form");

      div.id="file_upload";
   	  div.name="file_upload";
   	  //div.appendChild(form);

      //div.appendChild(document.createElement('br'));

      input = document.createElement("input");
   	  input.style.display = 'inline-block';
      input.style.marginTop = '10px';
      input.type="file";
   	  input.id="local_file";
   	  input.name="local_file";
   	  input.size=40;
   	  //input.style.fontSize = "99%";
   	  
   	  div.appendChild(input);
		
		this.panel.setBody(div);
		this.panel.selector=this;
        var buttons = [
            {text: "Upload", handler: {fn: this.uploadFile}, isDefault: true},
			{text:"Close",handler:{fn:function(){
				this.cancel();
            }}}
        ];
		this.panel.cfg.queueProperty("buttons",buttons);
		
		
		this.panel.render("page_body");
		this.panel.show();
	};

	this.uploadFile=function(){
		var coll_select=document.getElementById("upload_collection");
  	    if(coll_select.disabled==true){
            xmodal.message('File Viewer', "Please select a folder for this file.");
  	    	return;
  	    }
  	    
  	    if(document.getElementById("local_file").value==""){
            xmodal.message('File Viewer', "Please select a file to upload.");
  	    	return;
  	    }
  	    
		var collection_name=coll_select.options[coll_select.selectedIndex].value;
		var upload_level = document.getElementById("upload_level").value.trim();
		var upload_item = document.getElementById("upload_item").value.trim();
		var file_tags=document.getElementById("file_tags").value.trim();
		var file_format=document.getElementById("file_format").value.trim();
		var file_content=document.getElementById("file_content").value.trim();
		var file_name=document.getElementById("file_name").value.trim();
		var file_overwrite=document.getElementById("folder_overwrite").checked;
		
		if(file_name[0]=="/"){
			file_name=file_name.substring(1);
		}
		
		var file_params="?file_upload=true&XNAT_CSRF=" + csrfToken;
		
		if (file_content > ''){
			file_params+="&content="+file_content;
		}
		if (file_format > ''){
			file_params+="&format="+file_format;
		}
		if (file_tags > ''){
			file_params+="&tags="+file_tags;
		}
		if (file_overwrite){
			file_params+="&overwrite=true";
		}
			
		var file_dest = this.selector.obj.uri;
		if(collection_name == ''){
			file_dest=this.selector.obj.uri+"/files";
		}else if(upload_level == 'scans'){
			file_dest = this.selector.obj.uri+"/scans/"+ upload_item + "/resources/" + collection_name + "/files";
		}else{
			file_dest=this.selector.obj.uri+"/resources/"+ collection_name + "/files";
		}
		
		if(file_name > ''){
			file_dest +="/"+ file_name;
		}
		
		if(document.getElementById("local_file").value.endsWith(".zip")
		  || document.getElementById("local_file").value.endsWith(".gz")
		  || document.getElementById("local_file").value.endsWith(".xar")){
			if(confirm("Would you like the contents of this archive file to be extracted on the server? Press 'OK' to extract or 'Cancel' to proceed with upload without extracting.")){
				file_params+="&extract=true";
			}
		}
		
		file_dest+=file_params;
		if(showReason){
			var justification=new XNAT.app.requestJustification("add_file","File Upload Dialog",XNAT.app._uploadFile,this);
			justification.file_dest=file_dest;
			justification.file_name=file_name;
		}else{
			var passthrough= new XNAT.app.passThrough(XNAT.app._uploadFile,this);
			passthrough.file_dest=file_dest;
			passthrough.file_name=file_name;
			passthrough.fire();
		}
		
	}
}
   
function AddFolderForm(_obj){
  	this.obj=_obj;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);
  
	this.render=function(){	
		this.panel=new YAHOO.widget.Dialog("fileUploadDialog",{close:true,
		   width:"440px",height:"400px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Add Folder");
        window.viewer.requiresRefresh = true;
				
		var div = document.createElement("form");
		
		
		var table,tbody,tr,td,input;
   	  
   	  var title=document.createElement("div");
   	  title.style.marginTop="3px";
   	  title.style.marginLeft="1px";
   	  title.innerHTML="New Folder";
   	  div.appendChild(title);
   	  
   	  div.appendChild(document.createElement("br"));
   	  
   	  var collection_form=document.createElement("div");
   	  div.appendChild(collection_form);
   	  collection_form.style.border="1px solid #DEDEDE";
   	  
   	  table=document.createElement("table");
   	  tbody=document.createElement("tbody");
   	  table.appendChild(tbody);
   	  collection_form.appendChild(table);
   	  
   	  
   	  if(this.obj.categories!=undefined){
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Level";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="folder_level";
   	      //input.style.fontSize = "99%";
	   	  input.manager=this;
	   	  input.options[0]=new Option("SELECT","");
	   	  for(var cIdC=0;cIdC<this.obj.categories.ids.length;cIdC++){
	   	    input.options[input.options.length]=new Option(this.obj.categories.ids[cIdC],this.obj.categories.ids[cIdC]);
	   	  }
	   	  input.options[input.options.length]=new Option("resources","resources");
	   	  
	   	  input.onchange=function(o){
	   	  	var item_select=document.getElementById("folder_item");
	   	  	var coll_select=document.getElementById("folder_collection");
	   	  	
	   	  	if(this.selectedIndex==0){
	   	  		while(item_select.options.length>0){
	   	  			item_select.remove(0);
	   	  		}
	   	  		coll_select.value="";
	   	  		item_select.disabled=true;
	   	  		coll_select.disabled=true;
	   	  	}else{
	   	  		while(item_select.options.length>0){
	   	  			item_select.remove(0);
	   	  		}
	   	  		coll_select.value="";
	   	  		var _v=this.options[this.selectedIndex].value;
	   	  		
	   	  		if(_v=="resources"){
	   	  			item_select.disabled=true;
	   	  			
	   	  			coll_select.value="";
	   	  		
	   	  			coll_select.disabled=false;
	   	  		}else{
	   	  			coll_select.disabled=true;
	   	  			item_select.disabled=false;
		   	  		var cat=this.manager.obj.categories[_v];
		   	  		
		   	  		item_select.options[item_select.options.length]=new Option("SELECT","");
		   	  		for(var catC=0;catC<cat.length;catC++){
		   	  			item_select.options[item_select.options.length]=new Option(cat[catC].id,cat[catC].id);
		   	  		}
	   	  		}
	   	  	}
	   	  };
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
	   	  
	   	  
	   	  //collection
	   	  tr=document.createElement("tr");
			tr.style.height="20px";
	   	  
	   	  td=document.createElement("th");
	   	  td.align="left";
	   	  td.innerHTML="Item";
	   	  tr.appendChild(td);
	   	  
	   	  td=document.createElement("td");
	   	  input=document.createElement("select");
	   	  input.id="folder_item";
	   	  input.disabled=true;
	   	  input.manager=this;
   	      //input.style.fontSize = "99%";
	   	  input.onchange=function(o){
	   	  	var coll_select=document.getElementById("folder_collection");
	   	  	if(this.selectedIndex>0){
	   	  		coll_select.disabled=false;
	   	  		coll_select.value="";
	   	  		var _v=this.options[this.selectedIndex].value;
   	  		  	  			
	   	  	}else{
	   	  		coll_select.disabled=true;
	   	  		coll_select.value="";
	   	  	}

	   	  };
	   	  td.appendChild(input);
	   	  tr.appendChild(td);
	   	  
	   	  tbody.appendChild(tr);
   	  }
   	  //collection
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Folder";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="folder_collection";
   	  //input.style.fontSize = "99%";
   	  input.manager=this;
   	  td.appendChild(input);
   	  tr.appendChild(td);
   	  
   	  tbody.appendChild(tr);
   	  
   	  div.appendChild(document.createElement("br"));
   	  var file_form=document.createElement("div");
   	  div.appendChild(file_form);
   	  file_form.style.border="1px solid #DEDEDE";
   	  file_form.appendChild(document.createElement("div"));
   	  file_form.childNodes[0].innerHTML="<strong>Format Information</strong>";
   	  table=document.createElement("table");
   	  tbody=document.createElement("tbody");
   	  table.appendChild(tbody);
   	  file_form.appendChild(table);
   	     	  
   	  
   	  //format
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Format";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="folder_format";
   	  //input.style.fontSize = "99%";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //content
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Content";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="folder_content";
   	  //input.style.fontSize = "99%";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  div.appendChild(file_form);
   	  
   	  
   	  //tags
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Tags";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="folder_tags";
   	  //input.style.fontSize = "99%";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  		
		this.panel.setBody(div);
		
		this.panel.selector=this;
        var buttons = [
            {text: "Create", handler: {fn: this.addFolder}, isDefault: true},
			{text:"Close",handler:{fn:function(){
				this.cancel();
            }}}
        ];
		this.panel.cfg.queueProperty("buttons",buttons);
		
		
		this.panel.render("page_body");
		this.panel.show();
	};
	
	this.addFolder=function (){
		var coll_select=document.getElementById("folder_collection");
	    if(coll_select.disabled==true){
            xmodal.message('File Viewer', "Please select a folder for this file.");
	    	return;
	    }
	    
	    if(coll_select.value==""){
            xmodal.message('File Viewer', "Please identify a folder name.");
	    	return;
	    }
	    
		var collection_name=coll_select.value;
		
		var file_tags=document.getElementById("folder_tags").value.trim();
		var file_format=document.getElementById("folder_format").value.trim();
		var file_content=document.getElementById("folder_content").value.trim();
		var folder_level=document.getElementById("folder_level");
		if(folder_level!=null && folder_level.selectedIndex==0){
            xmodal.message('File Viewer', "Please select a level");
			return;
		}
		
		var file_params="?n=1&XNAT_CSRF=" + csrfToken;
		
		if(file_content!=""){
			file_params+="&content="+file_content;
		}
		if(file_format!=""){
			file_params+="&format="+file_format;
		}
		if(file_tags!=""){
			file_params+="&tags="+file_tags;
		}
			
			
		if(folder_level==null || folder_level.options[folder_level.selectedIndex].value=="resources"){
			var file_dest = this.selector.obj.uri+"/resources/"+ collection_name;
		}else{
			var folder_item=document.getElementById("folder_item");
			if(folder_item.selectedIndex==0){
                xmodal.message('File Viewer', "Please select an item.");
				return;
			}
			file_dest =this.selector.obj.uri+"/" +
				 folder_level.options[folder_level.selectedIndex].value+ "/"+ 
				 folder_item.options[folder_item.selectedIndex].value+ "/"+ 
				 "resources/"+ 
				 collection_name;
		}				
		
		file_dest+=file_params;
		if(showReason){
			var justification=new XNAT.app.requestJustification("add_folder","Folder Creation Dialog",XNAT.app._addFolder,this);
			justification.file_dest=file_dest;
		}else{
			var passthrough= new XNAT.app.passThrough(XNAT.app._addFolder,this);
			passthrough.file_dest=file_dest;
			passthrough.fire();
		}
		
	}
	
	
}


XNAT.app._uploadFile=function(arg1,arg2,container){	 
	var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
	var form = document.getElementById("file_upload");
	YAHOO.util.Connect.setForm(form,true);

    var callback = {
        upload: function (response) {
            XNAT.app.maintainLogin = false;
            window.viewer.requiresRefresh = true;
            window.viewer.refreshCatalogs("add_file");
            this.cancel();
            // CNDA-497: Filtered out <pre> text, which is sent on successful completion (i.e. no message)
            if (response.responseText && !response.responseText.match(/^<pre.*?><\/pre>$/)) {
                try {
                    var parsedResponse = YAHOO.lang.JSON.parse(response.responseText);
                    if (parsedResponse.duplicates) {
                        var opt = {};
                        opt.width = 600;
                        opt.height = 400;
                        opt.content = "The following files were not uploaded because they already exist on the server:<br><ul>";
                        for (var index = 0; index < parsedResponse.duplicates.length; index++) {
                            opt.content += "<li>" + parsedResponse.duplicates[index] + "</li>";
                        }
                        opt.content += "</ul>";
                        opt.title = 'File Viewer';
                        xmodal.message(opt);
                    }
                } catch (err) { // Fixes XNAT-2989
                    var options = {};
                    if (response.responseText) {
                        var reasons = response.responseText.match(/\<h3\>(.*[\n\r])+\<\/h3\>/g);
                        if (reasons && reasons[0]) {
                            reasons = "<br><br>" + reasons[0].replace(/\<\/?h3\>/g, "").replace(/[\n\r]/g, "<br>");
                            options = {width: 600, height: 400};
                            xmodal.message('Add Folder Error', "Failed to upload files." + reasons, "OK", options);
                        }
                    }
                }
            }
        },
        failure: function (obj1) {
            XNAT.app.maintainLogin = false;
            xmodal.message('Upload File', obj1.toString());
            this.cancel();
        },
        cache: false, // Turn off caching for IE
        scope: this
    };
	openModalPanel("add_file", "Uploading File.");
	
	var method = 'POST';
	if(container.file_name > ''){
		method='PUT';
	}
	

	var params="&event_reason="+event_reason;
	params+="&event_type=WEB_FORM";
	params+="&event_action=File(s) uploaded";
    XNAT.app.maintainLogin = true;
	YAHOO.util.Connect.asyncRequest(method,container.file_dest+params,callback);
};

XNAT.app._addFolder = function (arg1, arg2, container) {
    var event_reason = (container == undefined || container.dialog == undefined) ? "" : container.dialog.event_reason;
    var callback = {
        success: function (obj1) {
            closeModalPanel("add_folder");
            window.viewer.requiresRefresh = true;
            window.viewer.refreshCatalogs("add_folder");
            this.cancel();
        },
        failure: function (obj1) {
            closeModalPanel("add_folder");
            if (obj1.status == 409) {
                xmodal.message('Add Folder Error', 'Specified resource already exists.');
            } else {
                var options = {};
                if(obj1.responseText){ // Fixes XNAT-2989
                    var reasons = obj1.responseText.match(/\<h3\>(.*[\n\r])+\<\/h3\>/g);
                    if(reasons && reasons[0]){
                        reasons = "<br><br>" + reasons[0].replace(/\<\/?h3\>/g,"").replace(/[\n\r]/g,"<br>");
                        options = {width: 600, height: 400};
                    }else{
                        reasons = "";
                    }
                    xmodal.message('Add Folder Error', "Failed to add folder."  + reasons, "OK", options);
                }
            }
            this.cancel();
        },
        cache: false, // Turn off caching for IE
        scope: this
    };
    openModalPanel("add_folder", "Creating folder.");

    var params = "&event_reason=" + event_reason;
    params += "&event_type=WEB_FORM";
    params += "&event_action=Folder Created";
    YAHOO.util.Connect.asyncRequest('PUT', container.file_dest + params, callback);
};

jq(document).ready(function() {
    // refresh timeout interval so applet doesn't lose its session.
    setInterval(function(){XNAT.app.renewLogin()},60000);
});

XNAT.app.renewLogin = function ()  {
    if (XNAT.app.maintainLogin) {XNAT.app.timeout.handleOk();}
};
