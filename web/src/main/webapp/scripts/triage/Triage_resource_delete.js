/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/triage/Triage_resource_delete.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */
XNAT.app.TriageResourceDeleter = XNAT.app.TriageResourceDeleter || {};

XNAT.app.TriageResourceDeleter={
	requestDelete:function(index,shortname,source,fsource,date,user){
		this.index=index;
		this.source=source;
		this.shortname=shortname;
		this.fsource=fsource;
		this.user=user;
		this.date=date;

		let confirmHtml = "Are you sure you want to delete "+this.fsource+" uploaded by "+this.user+" on "+this.date+"?<br><br>"
		if (showReason) {
			confirmHtml += "<form id=\"cru_delete_frm\"><div style=\"margin-bottom:16px;\">Justification:<br><textarea id=\"cru_event_reason\" name=\"event_reason\" cols=\"50\" rows=\"3\"></textarea></div></form>"
		}
		xModalConfirm({
	          content: confirmHtml,
	          scroll: false,
	          height: 280,
	          okAction: function(){
	        	 if(showReason && document.getElementById("cru_delete_frm").event_reason.value==""){
	      			showMessage("page_body","Include justification.","Please include a justification for this deletion.");
	      		 }else{
	        	 	const reason = document.getElementById("cru_delete_frm") ?
						 document.getElementById("cru_delete_frm").event_reason.value :
						 "";
	        	 	XNAT.app.TriageResourceDeleter.doDelete(reason);
	      		 }
	          },
	          cancelAction: function(){
	         }
	        });
	},
	doDelete:function(event_reason){
		this.event_reason=event_reason;
		this.delCallback={
            success:this.handleSuccess,
            failure:this.handleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
		if(this.source!=undefined && this.source!=null){
			openModalPanel("delete_resource","Deleting resource " + this.fsource);
			this.tempURL=this.source;
			var params= this.event_reason ? "&event_reason="+this.event_reason : "";
	        YAHOO.util.Connect.asyncRequest('DELETE',this.tempURL+"?XNAT_CSRF=" + csrfToken+params,this.delCallback,null,this);
		}
	},
	handleSuccess:function(o){
		closeModalPanel("delete_resource");
		$('#resItem'+this.index).remove();
		
	},
	handleFailure:function(o){
		closeModalPanel("delete_resource");
		const details = o.responseText ? ":<br><br>" + o.responseText : ".";
	    showMessage("page_body", "Error", "Failed to delete file" + details);
	}
};