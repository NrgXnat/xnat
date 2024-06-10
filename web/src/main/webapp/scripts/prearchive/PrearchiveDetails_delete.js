/*
 * web: PrearchiveDetails_delete.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
XNAT.app.scanDeleter={
	requestDelete:function(scan_id, is_scan = true){
		this.lastScan=scan_id;
		this.isScan = is_scan;
		this.type = is_scan ? 'scan' : 'resource';
		xmodal.confirm({
          content: "Are you sure you want to delete " + this.type + " " + scan_id+ "?",
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
			openModalPanel("delete_scan","Deleting " + this.type + " " + this.lastScan);
			const subtype = this.isScan ? "/scans/" : "/resources/";
			this.tempURL = serverRoot + "/REST" + this.url + subtype + this.lastScan;
	        YAHOO.util.Connect.asyncRequest('DELETE',this.tempURL+"?XNAT_CSRF=" + csrfToken,this.delCallback,null,this);
		}
	},
	handleSuccess:function(o){
		closeModalPanel("delete_scan");
		$('#' + this.type + 'TR'+this.lastScan).remove();
		XNAT.app.validator.validate();
		XNAT.app.prearchiveActions.loadLogs();
	},
	handleFailure:function(o){
		closeModalPanel("delete_scan");
	    showMessage("page_body", "Error", "Failed to delete " + this.type + ". ("+ e.message + ")");
	}
};
