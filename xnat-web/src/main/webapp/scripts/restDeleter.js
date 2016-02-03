/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/restDeleter.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/2/13 10:23 AM
 */
RestDeleter = function(_array,_config) {
	RestDeleter.superclass.constructor.call(this,"rest_deleter",_config);
	this.a=_array;
	_config.title="Deletion Manager";
	_config.footer="Are you sure you want to permanently remove this data from the archive?&nbsp;&nbsp;(Data shared into this " + XNAT.app.displayNames.singular.project.toLowerCase() + " will be un-shared, rather than deleted)";
	this.trArray=new Array();

	this.drawContents=function(_div){
		var t=_div.appendChild(document.createElement("table"));
		t.width="100%";
		var tb=t.appendChild(document.createElement("tbody"));
  	    for(var aC=0;aC<this.a.length;aC++){
  	    	if(this.a[aC].canRead && (this.a[aC].allowDelete==undefined||this.a[aC].allowDelete==true)){
	  	   	  	var tr=tb.appendChild(document.createElement("tr"));
	  	   	  	tr.entry=this.a[aC];
	  	   	  	
	  	   	  	var td1=tr.appendChild(document.createElement("td"));
	  	   	  	var td2=tr.appendChild(document.createElement("td"));
	  	   	  	tr.td1=td1;
	  	   	  	tr.td2=td2;
	  	   	  	
	  	   	  	td1.innerHTML=this.a[aC].label;
	  	   	  	tr.pDivColor=td2.appendChild(document.createElement("div"));
	  	   	  	tr.pDivColor.style.width="100%";
	  	   	  	tr.pDivColor.style.backgroundColor="gray";
	  	   	  	tr.pDivColor.style.color="white";
	  	   	  	tr.pDivColor.innerHTML="&nbsp;waiting...";
	  	   	  	this.trArray.push(tr);
  	    	}
  	    } 
		var NUMSPACES=(this.config.defaultHeight/25)-4;
		for (var j=0; j<NUMSPACES; j++){
			var tr=tb.appendChild(document.createElement("tr"));
			var td1=tr.appendChild(document.createElement("td"));
			td1.innerHTML="&nbsp;"
		}
		var tr=tb.appendChild(document.createElement("tr"));
		var td1=tr.appendChild(document.createElement("td"));
		td1.innerHTML="Are you sure you want to permanently remove this data from the archive?<br />(Data shared into this " + XNAT.app.displayNames.singular.project.toLowerCase() + " will be un-shared, rather than deleted.)";
		td1.style.color="red";

		if(showReason){
			var tr=tb.appendChild(document.createElement("tr"));
			var td1=tr.appendChild(document.createElement("td"));
			var lblDiv=td1.appendChild(document.createElement("div"));
			lblDiv.innerHTML="Justification:";
			var sel = td1.appendChild(document.createElement("textarea"));
			sel.cols="48";
			sel.rows="4";
			sel.id="del_event_reason";
			sel.name="del_event_reason";
			td1.appendChild(sel);
		}
		
	    var myButtons = [ { text:"Cancel", handler:this.handleCancel, isDefault:true }, { text:"Delete", handler:{fn:this.handleDelete, scope:this} } ];
		this.popup.cfg.queueProperty("buttons", myButtons);
    }
	
	
	this.handleDelete=function(){
		if(showReason && document.getElementById("del_event_reason").value==""){
            xModalMessage('Delete Action', 'Please specify a justification for this operation.');
			return;
		}
		
		this.setDeleteButtonEnabled(false);	
		this.process();
	}
	
	this.setDeleteButtonEnabled = function(enabled) {
		this.popup._aButtons[1].set("disabled", !enabled);
	}
	
    this.handleCancel=function(){
    	if(this.manager.stopped){
    		// If the cancel action has been configured, use the callback
    		// otherwise reload the page. Added for XNAT-2408.
    		if(_config.cancelAction != undefined){ 
    			_config.cancelAction(); 
    		}else{
    			window.location.reload();
    		}
    	}else if(this.manager.complete){
    		if(this.manager.redirect){
    			window.location=this.manager.redirect;
    		}else if(this.manager.redirectHome){
    			window.location=serverRoot + "/";
    		}else{
    			// If the cancel action has been configured, use the callback
    			// otherwise reload the page. Added for XNAT-2408.
    			if(_config.cancelAction != undefined){ 
    				_config.cancelAction(); 
    			}else{
    				window.location.reload();
    			}
    		}
    	}else if(this.manager.processing){
    		this.manager.stopped=true;
            xModalMessage('Cancel Action', 'Please wait for current process to complete.<br/><br/>Further actions are cancelled.');
    	}else{
    		// If the cancel action has been configured, use the callback
    		// otherwise reload the page. Added for XNAT-2408.
    		if(_config.cancelAction != undefined){ 
    			_config.cancelAction(); 
    		}else{
    			window.location.reload();
    		}
		}
    }
    
    this.stopped=false;
    this.processing=false;

    this.process=function(){
    	if(!this.stopped){
			this.processing=true;
    		var params="?XNAT_CSRF=" + csrfToken;
    		var rF=document.getElementById("removeFiles");
  			if(rF==null || rF.checked){
    			params +="&removeFiles=true"
    		}
  			if(!(document.getElementById("del_event_reason")==null)){
  				params+="&event_reason="+document.getElementById("del_event_reason").value;
  			}
  			params+="&event_type=WEB_FORM";
  			params+="&event_action=Deleted";
  			
    		var matched=false;

    		for(var traC=0;traC<this.trArray.length;traC++){
    			this.currentTR=this.trArray[traC];
    			if(this.currentTR.processed==undefined){
    				if(this.currentTR.entry.redirectHome)this.redirectHome=true;
    				if(this.currentTR.entry.redirect)this.redirect=this.currentTR.entry.redirect;
		  	   	  	this.currentTR.pDivColor.style.backgroundColor="yellow";
		  	   	  	this.currentTR.pDivColor.style.color="black";
		  	   	  	this.currentTR.pDivColor.innerHTML="&nbsp;deleting...";
		  	   	  	
		     		deleteCB={
						success:function(obj1){
							this.currentTR.processed=true;
				  	   	  	this.currentTR.pDivColor.style.backgroundColor="green";
				  	   	  	this.currentTR.pDivColor.style.color="white";
				  	   	  	this.currentTR.pDivColor.innerHTML="&nbsp;complete&nbsp;";
				    		this.process();
						},
						failure:function(o){
							this.stopped=true;
							this.currentTR.pDivColor.style.backgroundColor="red";
				  	   	  	this.currentTR.pDivColor.style.color="black";
				  	   	  	this.currentTR.pDivColor.innerHTML="&nbsp;error&nbsp;";
                            xModalMessage('ERROR ' + o.status, 'Failed to delete ' + this.currentTR.entry.label + '.');
						},
                        cache:false, // Turn off caching for IE
						scope:this
					}
					matched=true;
					YAHOO.util.Connect.asyncRequest('DELETE',serverRoot + this.currentTR.entry.ru + params,deleteCB,null,this);
		    		break;	
    			}
    		}
    		
    		if(!matched){
    			var msg_op = {};
    			if(this.redirect){
    				msg_op.action = function(){ 
    					window.location.href = deleter.redirect;
    				}.bind(deleter);
	    		}else if(this.redirectHome){
	    			msg_op.action = function(){ window.location.href=serverRoot + "/"; }
	    		}else{
	    			msg_op.action = function(){ window.location.reload(); }
	    		}
                xModalMessage('Delete Action','All items were successfully deleted.','OK',msg_op);
    		}
    	}else{
    		closeModalPanel("stopAction");
    	}
    }
};

YAHOO.extend(RestDeleter, BasePopup, {
});