/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/subject/subjectSearch.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
dynamicJSLoad("loadOptions","omUtils.js");

if(displayedFields==undefined)
	var displayedFields=["xnat:subjectData/ID","xnat:subjectData/demographics/gender","xnat:subjectData/demographics/handedness"];
	
if(displayedFieldHeaders==undefined)
	var displayedFieldHeaders=["Accession","Gender","Handedness"];
		
function subjectSearch(_div, _project,_callback){
this.div=_div;
this.project=project;
this.callback=_callback;

this.searchMessageDiv=document.createElement("DIV");
this.searchMessageDiv.id="SEARCH_MESSAGE_DIV";

this.searchMatchesDiv=document.createElement("DIV");
this.searchMatchesDiv.id="SEARCH_MATCHES_DIV";
this.searchMatchesDiv.style.width="420px";
this.searchMatchesDiv.style.height="150px";
this.searchMatchesDiv.style.backgroundColor="#ffffff";
this.searchMatchesDiv.style.overflow="auto";
this.searchMatchesDiv.style.display="none";

this.comparisonType=document.createElement("SELECT");
this.comparisonType.options[0]=new Option("Equals","Equals");
this.comparisonType.options[1]=new Option("Starts With","Starts With");
this.comparisonType.options[2]=new Option("Ends With","Ends With");
this.comparisonType.options[3]=new Option("Like","Like");

this.searchValue=document.createElement("INPUT");
this.searchValue.type="text";

var instance = this;

this.draw=function(){
	var table = document.createElement("TABLE");
	var tbody = document.createElement("TBODY");
	this.div.appendChild(table);
	table.appendChild(tbody);
	
	var tr = document.createElement("TR");
	tbody.appendChild(tr);
	
	var th = document.createElement("TH");
	th.align="left";
	th.innerHtml=XNAT.app.displayNames.singular.subject;
	tr.appendChild(th);
	
	
	var td = document.createElement("TD");
	td.align="left";
	td.appendChild(this.comparisonType);
	tr.appendChild(td);
	
	var td = document.createElement("TD");
	td.align="left";
	td.appendChild(this.searchValue);
	tr.appendChild(td);
	
	var td = document.createElement("TD");
	td.align="left";
	var button = document.createElement("INPUT");
	button.type="button";
	button.value="Search";
	button.subjectSearch=this;
	button.onclick=function(){
		this.subjectSearch.submitSearch();
	}
	td.appendChild(button);
	tr.appendChild(td);
	
	//messages div
	tr = document.createElement("TR");
	var matchesTD= document.createElement("TD");
	matchesTD.colSpan="4";
	tr.appendChild(matchesTD);
	matchesTD.appendChild(this.searchMessageDiv);
	tbody.appendChild(tr);
	
	//matches div
	tr = document.createElement("TR");
	matchesTD= document.createElement("TD");
	matchesTD.colSpan="4";
	tr.appendChild(matchesTD);
	matchesTD.appendChild(this.searchMatchesDiv);
	tbody.appendChild(tr);
	
	
}

this.draw();

this.getValue=function(){
	 var comparison = this.comparisonType.options[this.comparisonType.selectedIndex].value;
	 var value = this.searchValue.value;
	 if (comparison=="Equals"){
	 }else if(comparison=="Starts With"){
	 	value=value + "%";
	 }else if(comparison=="Ends With"){
	 	value=value + "%";
	 }else if(comparison=="Like"){
	 	value="%"+ value + "%";
	 }else{
	 	value="%"+ value + "%";
	 }
	 return value;
}

this.getComparisonType=function(){
	 var comparison = this.comparisonType.options[this.comparisonType.selectedIndex].value;
	 if (comparison=="Equals"){
	 	return "=";
	 }else if(comparison=="Starts With"){
	 	return "LIKE";
	 }else if(comparison=="Ends With"){
	 	return "LIKE";
	 }else if(comparison=="Like"){
	 	return "LIKE";
	 }else{
	 	return "LIKE";
	 }
}

this.submitSearch=function(_id,_autoSelect){
  if(_id==undefined){
  if (this.searchValue.value!=""){
  		var value=this.getValue();
  	}
  }else{
  	var value=_id;
  }
  
  if (value!=undefined){
  	 this.setMessage("");
  	
	 var xss = new xdat_stored_search();
	 xss.setRootElementName("xnat:subjectData");
  	 var critset = new xdat_criteria_set();
	 critset.setMethod("OR");
	 
	 var comparison=this.getComparisonType();
	 
	 //ID
	 var subset= new xdat_criteria_set();
	 subset.setMethod("AND");
	 
	 var subidset= new xdat_criteria_set();
	 subidset.setMethod("OR");
	 
  	 var crit= new xdat_criteria();
	 crit.setSchemaField("xnat:subjectData/ID");
	 crit.setComparisonType(comparison);
	 crit.setValue(value);	
	 subidset.addCriteria(crit);
	 
	 crit= new xdat_criteria();
	 crit.setSchemaField("xnat:subjectData/label");
	 crit.setComparisonType(comparison);
	 crit.setValue(value);	
	 subidset.addCriteria(crit);
	 
	 subset.addChildSet(subidset);
	 
	 if (this.project){
	  	 crit = new xdat_criteria(); 
		 crit.setSchemaField("xnat:subjectData/project");
		 crit.setComparisonType("=");
		 crit.setValue(this.project);		
		 subset.addCriteria(crit);
	 }
	 
	 critset.addChildSet(subset);
	 
	 //PROJECT ID
	 var subset= new xdat_criteria_set();
	 subset.setMethod("AND");
	 
  	 crit = new xdat_criteria(); 
	 crit.setSchemaField("xnat:subjectData/sharing/share/label");
	 crit.setComparisonType(comparison);
	 crit.setValue(value);	
	 subset.addCriteria(crit);
	 
	 if (this.project){
	  	 crit = new xdat_criteria(); 
		 crit.setSchemaField("xnat:subjectData/sharing/share/project");
		 crit.setComparisonType("=");
		 crit.setValue(this.project);		
		 subset.addCriteria(crit);
	 }
	 
	 critset.addChildSet(subset);
	 
	 xss.addSearchWhere(critset);
	
	 var search = xss.toXML("");
	
	 if (window.XMLHttpRequest) {
	    var req = new XMLHttpRequest();
	  } else if (window.ActiveXObject) {
	    var req = new ActiveXObject("Microsoft.XMLHTTP");
	  }
	  
	 var url = "remote-class=org.nrg.xdat.ajax.XMLSearch";
     url = url + "&remote-method=execute";
     url = url + "&search="+escape(search);
     url = url + "&allowMultiples=false";
     url = url + "&XNAT_CSRF="+csrfToken;
	  
	 req.open("POST", serverRoot + "/servlet/AjaxServlet", false);
	 req.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
	 			
	 req.send(url);
	 
	 
	 if (req!==false) {     
		if (req.status==200) {      
			var arr,src='',parser = new SAXDriver();
			var handler = new SAXEventHandler();
			
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);
			
			parser.parse(req.responseText);// start parsing                        
			
			this.matchedSubjects = new Array();
			var subjectIDs = new Array();
			
			if (handler.items.length>0){
				this.matchedSubjects=handler.items;
			}else{
				if (handler.root){
					this.matchedSubjects[0]=handler.root;
				}
			}
			
			if (this.matchedSubjects.length>0){
				if(this.matchedSubjects.length==1 && _autoSelect!=undefined){
					this.select(this.matchedSubjects[0]);
				}else{
				this.renderSubjects(this.matchedSubjects);
				}
			}else{
				this.setMessage("No matching " + XNAT.app.displayNames.plural.subject.toLowerCase() + " found.");
			}
			
		}
	 }
  }
}

this.setMessage=function(msg){
	this.callback.message(msg);
}

this.renderSubjects=function(subjects){
	emptyChildNodes(this.searchMatchesDiv);
	var table = document.createElement("TABLE");
	var tbody = document.createElement("TBODY");
	this.searchMatchesDiv.appendChild(table);
	table.appendChild(tbody);
	
	//WRITE headers
	var tr = document.createElement("TR");
	for(var header1=0;header1<displayedFieldHeaders.length;header1++){
		var td1 = document.createElement("TH");
		td1.innerHTML=displayedFieldHeaders[header1];
		tr.appendChild(td1);
	}
	tbody.appendChild(tr);
	
	for(var subCount=0;subCount<subjects.length;subCount++){
		var subject = subjects[subCount];
		var tr = document.createElement("TR");
		for(var fieldCount=0;fieldCount<displayedFieldHeaders.length;fieldCount++){
			var td1 = document.createElement("TD");
			td1.innerHTML=subject.getProperty(displayedFields[fieldCount]);
			tr.appendChild(td1);
		}
		
		td1 = document.createElement("TD");
		var button= document.createElement("INPUT");
		button.type="button";
		button.value="SELECT";
		button.subject=subject;
		button.subjectSearch=this;
		button.onclick=function(){
			this.subjectSearch.select(this.subject);
		}
		td1.appendChild(button);
		tr.appendChild(td1);
		tbody.appendChild(tr);
	}
	
	
	this.searchMatchesDiv.style.display="block";
	this.div.style.display="block";
}

this.select=function(sbj){
	this.searchMatchesDiv.style.display="none";
	this.selectedSubject=sbj;
	this.callback.select(sbj);
}
}