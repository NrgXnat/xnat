/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/imageSessionData/archive.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */

//validation warning dialog
XNAT.app.warnings_dialog = new YAHOO.widget.Dialog("val_warning_dialog", { fixedcenter:true, visible:false, width:"800px", height:"250px", modal:true, close:true, draggable:true,resizable:true});
XNAT.app.warnings_dialog.cfg.queueProperty("buttons", [
    { text:"Cancel", handler:{fn:function(){
    	XNAT.app.warnings_dialog.hide();
    }}},{ text:"Continue",id:'val_warning_continue', handler:{fn:function(){
    	XNAT.app.warnings_dialog.hide();
    	XNAT.app.archiveValidator.approve('delete');
    }, isDefault:true}}]);

//archiveValidator should encapsulate all of the relevant logic used in this feature, besides what is native to the form validation process
XNAT.app.archiveValidator={
	validate:function(){	//issues the REST call to see if this would be archivable
		var callback={
			success:function(o){ 
				closeModalPanel("validation");
	    		this.handleValidation(o);
			},
			failure:function(o){
				closeModalPanel("validation");
				showMessage("page_body","Validation failed.",o.responseText);
			},
	        cache:false, // Turn off caching for IE
			scope:this
		}		
		
		this.project=$(document.getElementById(this.xsiType+'/project')).val();
		this.subject=$(document.getElementById(this.xsiType+'/subject_id')).val();
		this.label=$(document.getElementById(this.xsiType+'/label')).val();
		this.expt_id=$(document.getElementById(this.xsiType+'/ID')).val();
		var validate_service=serverRoot+"/REST/services/validate-archive?format=json&XNAT_CSRF=" + csrfToken;
		
		openModalPanel("validation","Pre-checking archive");

		YAHOO.util.Connect.setForm(document.getElementById("form1"),false);
		YAHOO.util.Connect.asyncRequest('POST',validate_service,callback);
	},
	indexOf:function(_list,_obj){
		for (var i = 0;i < _list.length; i++) {
	         if (_list[i] === _obj) { return i; }
	     }
	     return -1;
	},
	handleValidation:function(o){
		var validation= eval("(" + o.responseText +")");
		var show=new Array();
		var matched=false;
		var failed=false;
		//iterate over the list of reasons why the archive might fail.
		for(var valC=0;valC<validation.ResultSet.Result.length;valC++){
			var val=validation.ResultSet.Result[valC];
			if(val.code=="1"){
				//this just means it matched an existing session, which would have been echoed to the page elsewhere
				matched=true;
			}else if(this.indexOf(this.fail_merge_on,val.code)>-1){
				failed=true;
				val.type="FAIL";//these events are standardly conflicts, but this server is configured for them to fail
				show.push(val);
			}else if(val.type=="FAIL"){
				failed=true;
				show.push(val);
			}else{
				show.push(val);
			}
		}

		var warn_dialog=XNAT.app.warnings_dialog;
		
		if(show.length>0){
			//show conflicts, ask approval to override
			XNAT.app.archiveValidator.warnings="<dl class='header'><dd class='valCode'>Code</dd><dd class='valMessage'>Message</dd></dl>";
			for(var valC=0;valC<show.length;valC++){
				XNAT.app.archiveValidator.warnings+="<dl class='val"+show[valC].type+"'><dd class='valCode'>"+show[valC].type+"-"+show[valC].code+"</dd><dd class='valMessage'>"+show[valC].message+"</dd></dl>"
			}
			
			document.getElementById("val_warning_div").innerHTML=XNAT.app.archiveValidator.warnings;
			if(show.length>3){
				warn_dialog.render(document.body);//need to pre-render it for the height change to take effect.
				warn_dialog.cfg.setProperty("height", "400px");
				document.getElementById("val_warning_div").style.height="280px";
			}else{
				warn_dialog.cfg.setProperty("height", "250px");
				document.getElementById("val_warning_div").style.height="130px";
			}
			warn_dialog.render(document.body);
			warn_dialog.show();
			
			if(failed){
				$('#val_warning_desc').html('Merging the uploaded data into the pre-existing session will fail for the following reasons:');
				$('#val_warning_quest').html('');
				$(warn_dialog.lastButton).hide();
			}else{
				$('#val_warning_desc').html('Merging the uploaded data into the pre-existing session will override the following warnings:');
				$('#val_warning_quest').html('Are you sure you want to proceed?');
				$(warn_dialog.lastButton).show();
			}
			
			this.requiresOverwrite=true;
		}else{
			warn_dialog.hide();
	    	this.approve('append');
		}
	},
	approve:function(val){
		
		//register the desired behavior for form submit to the actual archive process
		var overrideEle=document.getElementById('session_overwrite');
		if(overrideEle==null || overrideEle==undefined){
			//add the missing input
			$('form[name="form1"]').append('<div style="display:none"><input type="text" name="overwrite" id="session_overwrite"/></div>');
		}
		$('#session_overwrite').val(val);

		//mark it as validated so that follow up submit won't re-execute validation
    	this.validated=true;
		
		//resubmit the form and the validation will be skipped this time
    	//simply doing YUIDOM.get("form1").submit() WILL NOT trigger the on submit listeners.  Evidently this is what JAVASCRIPT intended.  Ugh. 
    	//So, we need to manually check the listeners and submit if they succeed.
    	var form1=YUIDOM.get("form1");
    	try{
	    	if(form1.managedSubmit()){
	        	form1.submit();
	    	}
    	}catch(e){
            xModalMessage('Validation Error', 'Error performing validation.');
    	}
	},
	requiresOverwrite:false
};