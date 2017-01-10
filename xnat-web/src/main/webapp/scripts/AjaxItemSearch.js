/*
 * web: AjaxItemSearch.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
//
//dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");
//
//function realtimeXMLLoad(xmlSearch,allowChildren,ajax_servlet_path){
//
//	  if (window.XMLHttpRequest) {
//	    var listreq = new XMLHttpRequest();
//	  } else if (window.ActiveXObject) {
//	    var listreq = new ActiveXObject("Microsoft.XMLHTTP");
//	  }
//
//	 var url = ajax_servlet_path + "?remote-class=org.nrg.xdat.ajax.XMLSearch";
//     url = url + "&remote-method=execute";
//     url = url + "&search="+xmlSearch;
//     url = url + "&allowMultiples="+allowChildren;
//
//	 listreq.open("GET", url, false);
//	 listreq.send(null);
//
//	 if (listreq!==false) {
//		if (listreq.status==200) {
//			// eval the code in the global space (man this has cost me time to figure out how to do it grrr)
//			var arr,src='',parser = new SAXDriver();
//			var handler = new SAXEventHandler();
//
//			parser.setDocumentHandler(handler);
//			parser.setErrorHandler(handler);
//			parser.setLexicalHandler(handler);
//
//			parser.parse(listreq.responseText);// start parsing
//
//			if (handler.items.length>0){
//				return handler.items;
//			}else{
//				return handler.root;
//			}
//		}
//
//	 }
//
//	 return null;
//}
