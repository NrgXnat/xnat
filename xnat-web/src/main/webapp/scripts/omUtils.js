/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/omUtils.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");

dynamicJSLoad("xdat_stored_search","generated/xdat_stored_search.js");
dynamicJSLoad("xdat_search_field","generated/xdat_search_field.js");
dynamicJSLoad("xdat_criteria_set","generated/xdat_criteria_set.js");
dynamicJSLoad("xdat_criteria","generated/xdat_criteria.js");

function loadOptions(optionString){
	var options = new Object();
	if (optionString!=null){
		while(optionString.indexOf("[")>-1){
			optionString=optionString.substring(optionString.indexOf("[")+1);
			var nextOption=optionString.substring(0,optionString.indexOf("]"));
			optionString=optionString.substring(optionString.indexOf("]")+1);
			if(nextOption.indexOf("=")==-1){
				options.index=parseInt(nextOption);
			}else if(nextOption.startsWith("@xsi:type")){
				options.xsiType=nextOption.substring(nextOption.indexOf("=")+1).replace(/'/g,"");
			}else{
				options.where=new Object();
				options.where.field=nextOption.substring(0,nextOption.indexOf("="));
				options.where.value=nextOption.substring(nextOption.indexOf("=") + 1);
			}
		}
	}
	
	return options;
}

function instanciateObject(name){
	if(window.classMapping==undefined){
		dynamicJSLoad("ClassMapping","generated/ClassMapping.js");
		window.classMapping=new ClassMapping();
	}
	var fn = window.classMapping.newInstance;
	return fn(name);
}