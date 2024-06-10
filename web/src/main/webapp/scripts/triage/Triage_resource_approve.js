/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/triage/Triage_delete.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */
XNAT.app.TriageResourceApprover={
	requestMove:function(index,shortname,source,target,overwrite,event_reason,format,content,fsource,date,user){
		this.index=index;
		this.target=target;
		this.source=source;
		this.shortname=shortname;
		this.overwrite=overwrite;
		this.event_reason=event_reason;
		this.fsource=fsource;
		this.date=date;
		this.user=user;
		this.params="event_type=WEB_FORM";
		this.params+="&event_action=Quarantine Approval"+ this.shortname;
		this.params+="&event_reason=Quarantine Approval";
		this.params+="&overwrite=false";
		this.loadingModal = null;
		if(format!=undefined && format!=null && format!=""){
			this.params+="&format=" + format;
		}
		
		if(content!=undefined && content!=null && content!=""){
			this.params+="&content=" + content;
		}
		
		xModalConfirm({
          content: "Are you sure you want to approve "+this.fsource+" uploaded by "+this.user+" on "+this.date+"?",
          scroll: false,
          height: 240,
          okAction: function(){
              XNAT.app.TriageResourceApprover.doMove();
          },
          cancelAction: function(){
          }
        });
	},
	doMove:function(){
		this.moveCallback={
            success:this.handleSuccess,
            failure:this.handleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
		if(this.source!=undefined && this.source!=null){
			this.loadingModal = xModalLoadingOpen({id: "move_resource", title: "Moving..."});
			this.tempURL=serverRoot+"/data/services/triage/approve?XNAT_CSRF=" + csrfToken;
			this.params=this.params+"&src=" + this.source+"&dest="+this.target
	        YAHOO.util.Connect.asyncRequest('POST',this.tempURL,this.moveCallback,this.params,this);
		} else {
			xModalMessage("Error", "Cannot determine triage source location");
		}
	},
	doMoveWithOverwrite:function(){
		this.overwrite='true';
		this.params="event_type=WEB_FORM";
		this.params+="&event_action=Uploaded "+ this.shortname;
		this.params+="&event_reason="+ this.event_reason;
		this.params+="&overwrite=true";
		this.doMove();
	},
	handleSuccess:function(o){
		if (this.loadingModal) {
			this.loadingModal.close();
			this.loadingModal = null;
		}
	    if(o.responseText!=undefined && (o.responseText.indexOf("Duplicate File")>-1)){
			if(this.overwrite=='true'){
				xModalConfirm({
			          content: "The quarantine files already exist on the server. Would you like to overwrite those files?",
			          okAction: function(){
			        	  XNAT.app.TriageResourceApprover.doMoveWithOverwrite();
			          },
			          cancelAction: function(){
			          }
			        });		
			}else{
				showMessage("page_body", "Alert", "The quarantine files already exist on the server and cannot be overwritten");
				
			}
		}else if(o.responseText!=undefined && (o.responseText.indexOf("Please unlock to continue")>-1)){
						showMessage("page_body", "Alert", "Destination resource is locked. Please unlock to continue.");
		}else{
			showMessage("page_body", "Success", "Move Successful.");
			$('#resItem'+this.index).remove();
		}
	},
	handleFailure:function(o){
		if (this.loadingModal) {
			this.loadingModal.close();
			this.loadingModal = null;
		}
		const details = o.responseText ? ":<br><br>" + o.responseText : ".";
	    showMessage("page_body", "Error", "Failed to move file into archive" + details);
	}
};