/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/prearchive/PrearchiveDetails_delete.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/6/14 3:48 PM
 */
XNAT.app.scanDeleter={
	requestDelete:function(scan_id){
		this.lastScan=scan_id;
		
		xModalConfirm({
          content: "Are you sure you want to delete scan "+scan_id+ "?",
          okAction: function(){
              XNAT.app.scanDeleter.doDelete();
          },
          cancelAction: function(){
          }
        });
	},
	doDelete:function(){
		this.delCallback={
            success:this.handleSuccess,
            failure:this.handleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
		if(this.lastScan!=undefined && this.lastScan!=null){
			openModalPanel("delete_scan","Deleting scan " + this.lastScan);
			
			this.tempURL=serverRoot+"/REST" + this.url +"/scans/" + this.lastScan;
	        YAHOO.util.Connect.asyncRequest('DELETE',this.tempURL+"?XNAT_CSRF=" + csrfToken,this.delCallback,null,this);
		}
	},
	handleSuccess:function(o){
		closeModalPanel("delete_scan");
		$('#scanTR'+this.lastScan).remove();
		XNAT.app.validator.validate();
		XNAT.app.prearchiveActions.loadLogs();
	},
	handleFailure:function(o){
		closeModalPanel("delete_scan");
	    showMessage("page_body", "Error", "Failed to delete scan. ("+ e.message + ")");
	}
};