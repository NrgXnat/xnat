/* 
 * resource dialog is used to upload resources as an experiment level resource
 */
XNAT.app.protChk={
	resource_label:"",
	project:"",
	loaded:false,
	ready:false,
	show:function(){
		//if its initialized, then show it, otherwise initialize it.
		if(!this.loaded){
			this.loaded=true;
			this.start();
		}else if(this.ready){
			XNAT.app.protChk.dialog.render(document.body);
			XNAT.app.protChk.dialog.show();
		}
	},
	disableLink:function(){
		//mark link as disabled so that it is obvious that this won't work
		YUIDOM.get('lnch_valid_a').style.color='grey';
		YUIDOM.get('lnch_valid_a').style.cursor='text';
	},
	start:function(){		  		
		//triggers the initial load of the resource list for the select box
		openModalPanel("contents_loading","Loading");
		YAHOO.util.Connect.asyncRequest('GET', serverRoot+'/data/projects/' + this.project + '/pipelines/ProtocolCheck/experiments/'+ this.expt_id +'?format=json', {success : this.handleResLoad, failure : function(){
			closeModalPanel("contents_loading");
			XNAT.app.protChk.disableLink();
		}, cache : false, scope : this});
	},
	handleResLoad:function(obj){
		//handles the response with the available resources for this project
		try{
			var parsedResponse = YAHOO.lang.JSON.parse(obj.responseText);
		    if (parsedResponse.items.length !== 0) {
		    	for(var chiC=0;chiC<parsedResponse.items.length;chiC++){
			    	for(var chiCi=0;chiCi<parsedResponse.items[chiC].children.length;chiCi++){
			    		if(parsedResponse.items[chiC].children[chiCi].field=="parameters/parameter"){
					    	for(var chiCi2=0;chiCi2<parsedResponse.items[chiC].children[chiCi].items.length;chiCi2++){
					    		var param=parsedResponse.items[chiC].children[chiCi].items[chiCi2];
					    		if(param.data_fields["name"]=="catalog_content"){
					    			if(param.data_fields["csvValues"].indexOf(",")==-1)
				    				{
				    					$('#catalog_content_field').append($("<option value='" + param.data_fields["csvValues"] +"'>" + param.data_fields["csvValues"] +"</option>"));
				    				}else{
						    			this.content=param.data_fields["csvValues"].slice(",");
						    			for(var param1=0;param1<this.content.length;param1++){
						    				$('#catalog_content_field').append($("<option value='" + this.content[param1] +"'>" + this.content[param1] +"</option>"));
						    			}
				    				}
					    		}
					    	}
			    		}
			    	}
		    	}

				closeModalPanel("contents_loading");
				this.ready=true;
				this.show();
			}
		}catch(e){
			this.disableLink();
			closeModalPanel("contents_loading");
		}
	},
	doLaunch:function(){
		//executes the selected upload operation
		var frm=document.getElementById("lnch_valid_upload_frm");
		YAHOO.util.Connect.setForm(frm,false);
		
		var callback={
			success:function(obj1){		
				closeModalPanel("val_prot_mdl");
				this.handleCompletion(obj1);
			},
			failure:function(obj1){		
				closeModalPanel("val_prot_mdl");
				this.handleFailure(obj1);
			},
			scope:this
		}

		var params="";		
		
		openModalPanel("val_prot_mdl","Launching Pipeline...");
		YAHOO.util.Connect.asyncRequest('POST',serverRoot+"/app/action/ManagePipeline?XNAT_CSRF=" + csrfToken + "&"+params,callback);
	},
	handleCompletion:function(response){
		showMessage("page_body","Success.","Pipeline launched successfully. This page will be reloaded.");
		
		this.dialog.hide();
		setTimeout(function(){window.location.reload();}, 3000);
		
	},
	handleFailure:function(response){
		showMessage("page_body","Failed to launch pipeline.",response.responseText);
		
		this.dialog.hide();
	}
}


//initialize modal upload dialog
XNAT.app.protChk.dialog=new YAHOO.widget.Dialog("lnch_valid_dialog", { fixedcenter:true, visible:false, width:"500px", height:"300px", modal:true, close:true, draggable:true } ),
XNAT.app.protChk.dialog.cfg.queueProperty("buttons", [{ text:"Close", handler:{fn:function(){XNAT.app.protChk.dialog.hide();}}},{ text:"Launch", handler:{fn:function(){XNAT.app.protChk.doLaunch();}}, isDefault:true}]);