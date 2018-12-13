/*
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 P
 */
XNAT.app.TriageFileApprover={
	requestMove:function(index,shortname,scan_id,target,overwrite,event_reason,format,content){
		this.index=index;
		this.target=target;
		this.lastScan=scan_id;
		this.shortname=shortname;
		this.overwrite=overwrite;
		this.event_reason=event_reason;
		this.params="event_type=WEB_FORM";
		this.params+="&event_action=Uploaded "+ this.shortname;
		this.params+="&event_reason="+ this.event_reason;
		this.params+="&overwrite=false";
		if(format!=undefined && format!=null && format!=""){
			this.params+="&format=" + format;
		}
		
		if(content!=undefined && content!=null && content!=""){
			this.params+="&content=" + content;
		}
		xModalConfirm({
          content: "Are you sure you want to move file "+this.shortname+ "?",
          scroll: false,
          height: 240,
          okAction: function(){
              XNAT.app.TriageFileApprover.doMove();
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
		if(this.lastScan!=undefined && this.lastScan!=null){
			openModalPanel("move_scan","Moving file " + this.shortname);
			
			this.tempURL=serverRoot+"/data/services/triage/approve?XNAT_CSRF=" + csrfToken;
			this.params=this.params+"&src=" + this.lastScan+"&dest="+this.target
	        YAHOO.util.Connect.asyncRequest('POST',this.tempURL,this.moveCallback,this.params,this);

		}
	},
	doMoveWithOverwrite:function(){
		this.overwrite='true';
		this.params="event_type=WEB_FORM";
		this.params+="&event_action=Uploaded "+ this.shortname;
		this.params+="&event_reason="+ this.event_reason;
		this.params+="&overwrite="+ this.overwrite;
		this.doMove();
	},
	handleSuccess:function(o){
		closeModalPanel("move_scan");
	    if(o.responseText!=undefined && (o.responseText.indexOf("Duplicate File")>-1)){
			if(this.overwrite=='true'){
				xModalConfirm({
			          content: "The Quarantine files already exist on the server.  Would you like to overwrite those files?",
			          okAction: function(){
			        	  XNAT.app.TriageFileApprover.doMoveWithOverwrite();
			          },
			          cancelAction: function(){
			          }
			        });		
			}else{
				showMessage("page_body","Failed Approval.","The selected files already exist for this session.");
			}
		}else{
			showMessage("page_body", "Success", "Move Successful.");
			$('#fileItem'+this.index).remove();
		}
	},
	handleFailure:function(o){
		closeModalPanel("move_scan");
	    showMessage("page_body", "Error", "Failed to move file.");
	}
};