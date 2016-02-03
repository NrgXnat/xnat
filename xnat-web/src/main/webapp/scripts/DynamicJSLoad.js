/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/DynamicJSLoad.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function dynamicJSLoad(name,file){
	if(eval("window."+name)==undefined){
		var url = serverRoot+"/scripts/" + file;
		if(eval("window." + name)==undefined){
	    	var e=document.createElement("script");
			e.src=url;
			e.type="text/javascript";
			document.getElementsByTagName("head")[0].appendChild(e);
	    }
	    
	    if(eval("window." + name)==undefined){
	    	if (window.XMLHttpRequest) {
		       req = new XMLHttpRequest();
		    } else if (window.ActiveXObject) {
		       req = new ActiveXObject("Microsoft.XMLHTTP");
		    }     
		    
    		req.open("GET",url,false); // true= asynch, false=wait until loaded        
    		req.send(null);

	    	if (req!==false) {    
	    		if (req.status==200) {        
	    			// eval the code in the global space (man this has cost me time to figure out how to do it grrr)                        
		    			window.eval(req.responseText); 
		    			window[name]=eval(name);
	    			
		    			if(eval("window." + name)==undefined){
		    			//	logEntry("Failed to load " + name + " (Ready-State:" + req.readyState + "." + req.status + ")");
		    			}else{
		    			//	logEntry("Loaded " + name + " from 2nd method.");
					    }
	    		} else{        
	    		//	logEntry("req.status=" + req.status);
	    		}
	    		
	    	}
	    }else{
	    	//logEntry("Loaded " + name + " from 1st method.");
	    }
	}
	
	return eval("new " + name);
}
