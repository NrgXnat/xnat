/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/restLister.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function restLister(_info) {
    this.info = _info;
    this.loading = 0;
    this.loaded = 0;

    this.init = function () {
        if (this.loading == 0) {
            this.loading = 1;
            var scanCallback = {
                success:this.processScans,
                failure:this.handleFailure,
                cache:false, // Turn off caching for IE
                scope:this
            };

            YAHOO.util.Connect.asyncRequest('GET', this.info.uri + '/resources?XNAT_CSRF=' + window.csrfToken + '&all=true&format=json&file_stats=true&timestamp=' + (new Date()).getTime(), scanCallback, null, this);
        }
    }

    this.handleFailure = function (error) {
        // If we're leaving, then this is a complete red herring.
        if (!window.leaving) {
            xModalMessage('Error', "Error loading resources: [" + error.status + "] " + error.statusText);
            this.loading = 0;
        }
    }

    this.processScans = function (o) {
        this.info.scans = [];
        var contents = eval("(" + o.responseText + ")").ResultSet.Result;
        for (var i = 0; i < contents.length; i++) {
            if (contents[i].category == this.info.category) {
                this.info.scans.push(contents[i]);
            }
        }

        for (var i = 0; i < this.info.scans.length; i++) {
            var fileCallback = {
                success:this.processFiles,
                failure:this.handleFailure,
                cache:false, // Turn off caching for IE
                scope:this
            };
            var fileuri = this.info.uri + "/" + this.info.category + "/" + this.info.scans[i].cat_id + "/out/resources/" + this.info.scans[i].xnat_abstractresource_id;
            YAHOO.util.Connect.asyncRequest('GET', fileuri + '/files?XNAT_CSRF=' + window.csrfToken + '&all=true&format=json&timestamp=' + (new Date()).getTime(), fileCallback, null, this);
        }

        // Mark this page as loaded.
        this.loaded = 1;
    }

    this.getScan = function (cid) {
        for (var j = 0; j < this.info.scans.length; j++) {
            if (this.info.scans[j].xnat_abstractresource_id == cid) {
                return this.info.scans[j];
            }
        }
        return null;
    }

    this.processFiles = function (o) {
        var allFiles = eval("(" + o.responseText + ")").ResultSet.Result;
        var cid = allFiles[0].cat_ID;//same for each element in allFiles
//		if (cid==null && allFiles[0].URI!=null) {
//			var tmp = allFiles[0].URI.replace(this.info.uri + "/" + this.info.category + "/", "");
//			var scanID = tmp.substring(0,tmp.indexOf("/"));
//			cid = getCID(scanID);
//		}
        var scan = this.getScan(cid);
        if (scan != null) {
            if (scan.files == null) {
                scan.files = new Array();
            }
            for (var q = 0; q < allFiles.length; q++) {
                scan.files.push(allFiles[q]);
            }
        } else {
            xModalMessage('Error', 'Null ' + cid);
        }
    }
}
