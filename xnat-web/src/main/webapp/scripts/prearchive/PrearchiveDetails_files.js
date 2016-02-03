/*
* D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/prearchive/PrearchiveDetails_files.js
* XNAT http://www.xnat.org
* Copyright (c) 2014, Washington University School of Medicine
* All Rights Reserved
*
* Released under the Simplified BSD.
*
* Last modified 1/3/14 9:54 AM
*/

XNAT.app.fileCounter={
 	load:function(){
		var catCallback={
            success:this.processCatalogs,
            failure:this.handleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
		
		var tempURL=serverRoot+"/REST" + this.url +"/resources?format=json&sortBy=category,cat_id,label&timestamp=" + (new Date()).getTime();

        YAHOO.util.Connect.asyncRequest('GET',tempURL,catCallback,null,this);
		
		for(var sc=0;sc<this.scans.length;sc++){
			document.getElementById("scan"+this.scans[sc]+"Files").innerHTML="Loading...";
		}
	},
	processCatalogs:function(o){   		
    	var catalogs= eval("(" + o.responseText +")").ResultSet.Result;
    	
    	for(var catC=0;catC<catalogs.length;catC++){
			
    		var scan=document.getElementById("scan"+catalogs[catC].cat_id+"Files");
    		if(scan!=null){
				if(catalogs[catC].file_count!=undefined && catalogs[catC].file_count!=null){
		  			scan.innerHTML=catalogs[catC].file_count + " files, "+ size_format(catalogs[catC].file_size);
				}else{
		  			scan.innerHTML=size_format(catalogs[catC].file_size);
				}
    		}
    	}
		
		for(var sc=0;sc<this.scans.length;sc++){
			if(document.getElementById("scan"+this.scans[sc]+"Files").innerHTML.startsWith("Load")){
				document.getElementById("scan"+this.scans[sc]+"Files").innerHTML="0 files";
			}
		}
   },
	handleFailure:function(o){
		for(var sc=0;sc<this.scans.length;sc++){
			document.getElementById("scan"+this.scans[sc]+"Files").innerHTML="-";
		}
	},
	scans:[]
 };


function number_format(number, decimals, dec_point, thousands_sep) {
    var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
    var d = dec_point == undefined ? "," : dec_point;
    var t = thousands_sep == undefined ? "." : thousands_sep, s = n < 0 ? "-" : "";
    var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
    return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
}


function size_format(filesize) {
    if (filesize >= 1073741824) {
        filesize = number_format(filesize / 1073741824, 2, '.', '') + ' GB';
    }
    else {
        if (filesize >= 1048576) {
            filesize = number_format(filesize / 1048576, 2, '.', '') + ' MB';
        }
        else {
            if (filesize >= 1024) {
                filesize = number_format(filesize / 1024, 0) + ' KB';
            }
            else {
                filesize = number_format(filesize, 0) + ' bytes';
            }
        }
    }
    return filesize;
}
