/*
 * web: LabelEditor.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/**
 * used in xnat:subjectData/share.vm
 */

if(XNAT.app._label==undefined)XNAT.app._label=new Object();

XNAT.app._label.LabelEditorP=function(_config,uri,currentLabel){
	this.config=_config;
	this.uri=uri;
	XNAT.app._label.currentLabel=currentLabel;
	
	if(this.config.header==undefined){
		this.config.header=XNAT.app.displayNames.singular.imageSession;
	}
	
    this.onModification=new YAHOO.util.CustomEvent("modification",this);
    this.onError=new YAHOO.util.CustomEvent("modification-error",this);
	
	this.render=function(){	
      if(this.panel==undefined){
		this.panel=new YAHOO.widget.Dialog("labelDialog",{close:true,
		   width:"350px",height:"100px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Label modification");
				
		var bd = document.createElement("form");
					
		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);	    
		
		//modality
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");
		td3=document.createElement("td");
		
		td1.innerHTML=this.config.header +":";
		td1.align="left";
		
		this.labelInput = document.createElement("input");
		this.labelInput.id="new_label";
		this.labelInput.value=XNAT.app._label.currentLabel;
		this.labelInput.name="new_label";
		
		this.labelContainer=document.createElement("div");
		this.labelContainer.id="complete_container";
		this.labelContainer.width="100px";
		td2.appendChild(this.labelContainer);
		
		this.labelContainer.appendChild(this.labelInput);
				
		tr.appendChild(td1);		
		tr.appendChild(td2);
		tb.appendChild(tr);
		
		this.panel.setBody(bd);
		
		this.panel.selector=this;
		var buttons=[{text:"Modify",handler:{fn:function(){
				var labelBox = document.getElementById("new_label");
				  XNAT.app._label.selectedLabel=labelBox.value.trim();
				  if(XNAT.app._label.selectedLabel==""){
                      xmodal.message('Label Validation', "Please specify a new " + this.selector.config.header + ".");
				  }else if(XNAT.app._label.selectedLabel==XNAT.app._label.currentLabel){
                      xmodal.message('Label Validation', 'No modification found.');
				  }else{
				    var validatedLabel=cleanLabel(XNAT.app._label.selectedLabel);
				    if(validatedLabel!=XNAT.app._label.selectedLabel){
				       labelBox.value=validatedLabel;
                       xmodal.message('Label Validation', "Invalid characters in new " + this.selector.config.header + ".<br/><br/>Review modified value and resubmit.");
				       labelBox.focus();
				       return;
				    }
				    
				    var matchedExisting=false;
				    for(var lC=0;lC<XNAT.app._label.labelLoader.list.length;lC++){
				       if(XNAT.app._label.selectedLabel==XNAT.app._label.labelLoader.list[lC].label){
				          matchedExisting=true;
				          break;
				       }
				    }
				    
				    if(matchedExisting){
                       xmodal.message('Error', "This " + this.selector.config.header + " is already in use in this " + XNAT.app.displayNames.singular.project.toLowerCase() + ".<br/><br/>Please modify and resubmit.");
				       labelBox.focus();
				       return;
				    }
				    
				    
				    if(this.selector.uri==undefined){
			               		XNAT.app._label.currentLabel=XNAT.app._label.selectedLabel;
			        	        closeModalPanel("modify_new_label");	
			        	        this.selector.onModification.fire();
			        	        this.cancel();
					}else{
					    if(confirm("Modifying the " + this.selector.config.header + " of an imaging " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + " will result in the moving of files on the file server within the " + XNAT.app.displayNames.singular.project.toLowerCase() + "'s storage space.  Are you sure you want to make this change?")){
					    	if(showReason){
                    			var justification=new XNAT.app.requestJustification("label_change","Label Modification Justification",XNAT.app.modifyLabel,this);
                    		}else{
                    			var passthrough= new XNAT.app.passThrough(XNAT.app.modifyLabel,this);
                    			passthrough.fire();
                    		}
					     }
				    }
				  }
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);
		this.panel.render("page_body");
		
		this.panel.show();

      }
			
      openModalPanel("labels_loading","Loading " + this.config.header + "s...");
      XNAT.app._label.labelLoader=new XNAT.app._label.labelLoaderP();
      XNAT.app._label.labelLoader.load(uri);
	}
	
	
}


XNAT.app.modifyLabel=function(arg1,arg2,container){
	openModalPanel("modify_new_label","Modifying " + this.selector.config.header +", please wait...");

	var settingsCallback={
    	success:function(o){
       		XNAT.app._label.currentLabel=XNAT.app._label.selectedLabel;
	        closeModalPanel("modify_new_label");	
	        this.selector.onModification.fire();
	        this.cancel();
    	},
        failure:function(o){
            xmodal.message('Error ' + o.status, "ERROR: Failed to modify label.");
            this.selector.onError.fire();
            closeModalPanel("modify_new_label");
    	},
        cache:false, // Turn off caching for IE
        scope:this
    }

	var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
	
    var params="";		
	   	params+="event_reason="+event_reason;
	   	params+="&event_type=WEB_FORM";
	   	params+="&event_action=Renamed shared item";
    	
	YAHOO.util.Connect.asyncRequest('PUT',this.selector.uri +"/" + XNAT.app._label.currentLabel +"/projects/" + this.selector.config.project +"?label=" + XNAT.app._label.selectedLabel +"&format=json&XNAT_CSRF=" + csrfToken + "&"+ params,settingsCallback);
}

XNAT.app._label.labelLoaderP=function(){
    this.load=function(uri){
		this.list=undefined;
		//load from search xml from server
		this.initCallback={
			success:this.completeInit,
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
		
		YAHOO.util.Connect.asyncRequest('GET',uri+'?format=json&columns=label&timestamp=' + (new Date()).getTime(),this.initCallback,null,this);
	};
	
	this.initFailure=function(o){
        if (!window.leaving) {
            this.displayError("ERROR " + o.status+ ": Failed to load current label list.");
        }
	};
	
	this.completeInit=function(o){
		try{
		    this.list= eval("(" + o.responseText +")").ResultSet.Result;
			this.onload();
		}catch(e){
			this.displayError("ERROR " + o.status+ ": Failed to parse current label  list.");
		}
		
	};
	
	this.displayError=function(errorMsg){
		xmodal.message('Error', errorMsg);
	};
	
	this.onload=function(obj){
		closeModalPanel("labels_loading");
		
		document.getElementById("new_label").disabled=false;
	}
}



function cleanLabel(val)
{
        var temp = val.replace(/^\s*|\s*$/g,"");
        var newVal = '';
        temp = temp.split(' ');
        for(var c=0; c < temp.length; c++) {
                newVal += '' + temp[c];
        }
        
        newVal = newVal.replace(/[&]/,"_");
        newVal = newVal.replace(/[?]/,"_");
        newVal = newVal.replace(/[<]/,"_");
        newVal = newVal.replace(/[>]/,"_");
        newVal = newVal.replace(/[(]/,"_");
        newVal = newVal.replace(/[)]/,"_");
        newVal = newVal.replace(/[#]/,"_");
        newVal = newVal.replace(/[%]/,"_");
        newVal = newVal.replace(/[=]/,"_");
        newVal = newVal.replace(/[{]/,"_");
        newVal = newVal.replace(/[}]/,"_");
        newVal = newVal.replace(/[|]/,"_");
        newVal = newVal.replace(/[,]/,"_");
        newVal = newVal.replace(/[`]/,"_");
        newVal = newVal.replace(/[~]/,"_");
        newVal = newVal.replace(/[;]/,"_");
        newVal = newVal.replace(/[:]/,"_");
   	     return newVal;
}
