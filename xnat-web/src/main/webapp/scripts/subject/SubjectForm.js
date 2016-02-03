/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/subject/SubjectForm.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
dynamicJSLoad("loadOptions","omUtils.js");
dynamicJSLoad("xnat_subjectData","generated/xnat_subjectData.js");
dynamicJSLoad("xnat_projectParticipant","generated/xnat_projectParticipant.js");

dynamicJSLoad("xnat_datatypeProtocol","generated/xnat_datatypeProtocol.js");
dynamicJSLoad("xnat_fieldDefinitionGroup","generated/xnat_fieldDefinitionGroup.js");
dynamicJSLoad("xnat_fieldDefinitionGroup_field","generated/xnat_fieldDefinitionGroup_field.js");
dynamicJSLoad("xnat_fieldDefinitionGroup_field_possibleValue","generated/xnat_fieldDefinitionGroup_field_possibleValue.js");
var DEBUG=false;

//callback object should have select(subject),message(str),showMatchedSubjects(subjects) methods.
function SubjectForm(_sub,_div,_callback){
this.div=_div;
this.subject=_sub;
this.originalXML=_sub.toXML("");
this.callback=_callback;

this.setProject=function(proj){
	this.subject.setProject(proj);
}

this.clear=function(){
	while(this.div.childNodes.length>0){
		this.div.removeChild(this.div.childNodes[0]);
	}
}

this.draw=function(){
	this.clear();
	
	var table,tbody,tr,td1,td2,input;
	
	table = document.createElement("TABLE");
	tbody = document.createElement("TBODY");
	
	table.appendChild(tbody);
	this.div.appendChild(table);
	
	//HEADER
	tr = document.createElement("TR");
	td1 = document.createElement("TD");
	td2 = document.createElement("TD");
	var headerDiv = document.createElement("DIV");
	
	td1.appendChild(headerDiv);
	td1.colSpan="2";
	tr.appendChild(td1);
	tbody.appendChild(tr);
	
	//PROJECT
	tr = document.createElement("TR");
	td1 = document.createElement("TH");
	td1.align="left";
	td2 = document.createElement("TD");
	
	td1.innerHTML=XNAT.app.displayNames.singular.project;
	td2.innerHTML=this.subject.getProject();
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
			
	//Identifier
	tr = document.createElement("TR");
	td1 = document.createElement("TH");
	td1.align="left";
	td2 = document.createElement("TD");
	
	td1.innerHTML="<label ID='xnat:subjectData_label'>Label</label>";
	input = document.createElement("INPUT");
	input.type="text";
	input.subjectForm=this;
	if (this.subject.getLabel()!=null)
		input.value=this.subjectForm.subject.getLabel();
    input.onchange=function(){
    	this.subjectForm.subject.setLabel(this.value);
    }
    td2.appendChild(input);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
			
/*
	//Birthdate
	tr = document.createElement("TR");
	td1 = document.createElement("TH");
	td1.align="left";
	td2 = document.createElement("TD");
	
	var demo=this.subject.getDemographics();
	if (demo==undefined){
		dynamicJSLoad("xnat_demographicData","generated/xnat_demographicData.js");
		demo=new xnat_demographicData();
		this.subject.setDemographics(demo);
	}
	
	td1.innerHTML="Birth date";
	input = document.createElement("INPUT");
	input.type="text";
	input.subject=this.subject;
	if (this.subject.getProperty("demographics/dob")!=null)
		input.value=this.subject.getProperty("demographics/dob");
	var cal1x= new CalendarPopup();
	input.cal1x = cal1x;
	input.cal1x.showYearNavigation();
	input.cal1x.showYearNavigationInput();
	input.onselect=function(){
		this.cal1x.select(this,'anchor1x','MM/dd/yyyy'); 
		return false;
	}
	input.onclick=input.onselect;
    input.onchange=function(){
    	var demo=this.subject.getDemographics();
    	demo.setDob(this.value);
    }
    td2.appendChild(input);
    
	var anchor=document.createElement("A");
	anchor.id="anchor1x";
	anchor.name="anchor1x";
	td2.appendChild(anchor);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
			
	//gender
	tr = document.createElement("TR");
	td1 = document.createElement("TH");
	td1.align="left";
	td2 = document.createElement("TD");
	
	td1.innerHTML="Gender";
	input = document.createElement("SELECT");
	input.subject=this.subject;
	input.options[0]= new Option("SELECT","");
	input.options[1]= new Option("Male","male");
	input.options[2]= new Option("Female","female");
	input.options[3]= new Option("Unknown","unknown");
	
	var gender=this.subject.getProperty("demographics/gender");
	if (gender!=null){
		if (gender=="male"){
			input.selectedIndex=1;
		}else if (gender=="female"){
			input.selectedIndex=2;
		}else if (gender=="unknown"){
			input.selectedIndex=3;
		}
	}
    input.onchange=function(){
    	this.subject.getDemographics().setGender(this.value);
    }
    td2.appendChild(input);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
			
	//handedness
	tr = document.createElement("TR");
	td1 = document.createElement("TH");
	td1.align="left";
	td2 = document.createElement("TD");
	
	td1.innerHTML="Handedness";
	input = document.createElement("SELECT");
	input.subject=this.subject;
	input.options[0]= new Option("SELECT","");
	input.options[1]= new Option("Left","left");
	input.options[2]= new Option("Right","right");
	input.options[3]= new Option("Ambidextrous","ambidextrous");
	input.options[4]= new Option("Unknown","unknown");
	
	var handed=this.subject.getProperty("demographics/handedness");
	if (handed!=null){
		if (handed=="left"){
			input.selectedIndex=1;
		}else if (handed=="right"){
			input.selectedIndex=2;
		}else if (handed=="ambidextrous"){
			input.selectedIndex=3;
		}else if (handed=="unknown"){
			input.selectedIndex=4;
		}
	}
    input.onchange=function(){
    	this.subject.getDemographics().setHandedness(this.value);
    }
    td2.appendChild(input);
	
	tr.appendChild(td1);
	tr.appendChild(td2);
	tbody.appendChild(tr);
*/	
	
			
	//PROTOCOL definitions
	if (this._fieldDefinitionGroups==undefined){
		this._fieldDefinitionGroups= this.loadFieldDefinitionGroups();
	}
	
	
	
	if (this._fieldDefinitionGroups.length>0){
		
		//NON-PROJECT SPECIFIC GROUPS
		for(var fDcount=0;fDcount<this._fieldDefinitionGroups.length;fDcount++){
			var fdGroup=this._fieldDefinitionGroups[fDcount];
			if (!fdGroup.isProjectSpecific(true)){
				//BUFFER
				tr = document.createElement("TR");
				td1 = document.createElement("TH");
				td1.colSpan="2";
				td1.innerHTML="&nbsp;";
				
				tr.appendChild(td1);
				tbody.appendChild(tr);
				
				//HEADER
				tr = document.createElement("TR");
				td1 = document.createElement("TH");
				td1.colSpan="2";
				td1.align="left";
				
				if(fdGroup.getDescription()!=null){
					td1.innerHTML=fdGroup.getDescription();
				}else{
					td1.innerHTML=fdGroup.getId();
				}
				
				tr.appendChild(td1);
				tbody.appendChild(tr);
				
				
				//BODY
				tr = document.createElement("TR");
				td1 = document.createElement("TD");
				td1.colSpan="2";
								
				var fieldTable = document.createElement("TABLE");
				var fieldTbody = document.createElement("TBODY");
				fieldTable.appendChild(fieldTbody);
				td1.appendChild(fieldTable);
				
			  	
	
				//INSERT GROUP
				for (var fDfCount=0;fDfCount<fdGroup.getFields_field().length;fDfCount++){
					var fdField = fdGroup.getFields_field()[fDfCount];
					//INSERT FIELD
					var fieldTR=document.createElement("TR");
					var fieldTH1=document.createElement("TH");
					var fieldTH2=document.createElement("TD");
					fieldTH1.innerHTML=fdField.getName();
					fieldTH1.align="left";
					fieldTR.appendChild(fieldTH1);
					
					if (fdField.getDatatype()=="boolean"){
						if (!fdField.isRequired(false)){
							var radio1 = document.createElement("INPUT");
							radio1.type="radio";
							radio1.name=fdField.getXmlpath();
							radio1.subjectManager=this;
							
							radio1.onchange=function(){
								this.subjectManager.subject.setProperty(this.name,"NULL");
							}
							
							var label1 = document.createElement("LABEL");
							label1.appendChild(radio1);
							label1.appendChild(document.createTextNode("&nbsp;No Value"));
							fieldTH2.appendChild(label1);
						}
						//TRUE
						var radio1 = document.createElement("INPUT");
						radio1.type="radio";
						radio1.name=fdField.getXmlpath();
						radio1.subjectManager=this;
						
						radio1.onchange=function(){
							this.subjectManager.subject.setProperty(this.name,false);
						}
						
						var label1 = document.createElement("LABEL");
						label1.appendChild(radio1);
						label1.appendChild(document.createTextNode("&nbsp;False"));
						fieldTH2.appendChild(label1);
						
						//FALSE
						var radio1 = document.createElement("INPUT");
						radio1.type="radio";
						radio1.name=fdField.getXmlpath();
						radio1.subjectManager=this;
						
						radio1.onchange=function(){
							this.subjectManager.subject.setProperty(this.name,true);
						}
						
						var label1 = document.createElement("LABEL");
						label1.appendChild(radio1);
						label1.appendChild(document.createTextNode("&nbsp;True"));
						fieldTH2.appendChild(label1);
						
					}else if (fdField.getDatatype()=="date"){
						var input = document.createElement("INPUT");
						input.type="text";
						input.size="20";
						input.id=fdField.getXmlpath();
						var tmpID=fdField.getXmlpath();
						input.name=fdField.getXmlpath();
						input.subjectManager=this;
						if(this.subject.getProperty(fdField.getXmlpath())!=null){
							input.value=fdField.getXmlpath();
						}
						
						
						fieldTH2.appendChild(input);
						fieldTR.appendChild(fieldTH2);
						
						//INSERT CALENDAR
						var calendarContainer = document.createElement("DIV");
						calendarContainer.className="yui-skin-sam";
						
						
						var calendarDIV= document.createElement("DIV");
						calendarContainer.appendChild(calendarDIV);
					
						var fieldTH2=document.createElement("TD");
						fieldTH2.rowSpan="5";
						fieldTH2.appendChild(calendarContainer);
						
						YAHOO.namespace("example.calendar"); 
					    YAHOO.example.calendar.cal1 = new YAHOO.widget.Calendar("cal1",calendarDIV,{navigator:true}); 
					    
					    
					    YAHOO.example.calendar.input=input;
					    
					    YAHOO.example.calendar.handleSelect = function(type,args,obj) { 
					        var dates = args[0]; 
	    					var date = dates[0]; 
	    					var year = date[0], month = date[1], day = date[2]; 
	  						
	  						var tempinput=document.getElementById(tmpID);
	    					tempinput.value = month + "/" + day + "/" + year;
	    					tempinput.subjectManager.subject.setProperty(tempinput.name,tempinput.value);
					    }
					    
					    input.calendar=YAHOO.example.calendar.cal1;
					   input.onchange=function(){
					   	   if(this.value!=""){
					   	   	this.value=this.value.replace(/[-]/,"/");
					   	   	this.value=this.value.replace(/[.]/,"/");
					   	   	if(isValidDate(this.value)){
					   	   	 YAHOO.example.calendar.cal1.select(this.value); 
					         var selectedDates = YAHOO.example.calendar.cal1.getSelectedDates(); 
					         if (selectedDates.length > 0) { 
							    this.subjectManager.subject.setProperty(this.name,this.value);
					            var firstDate = selectedDates[0]; 
					            YAHOO.example.calendar.cal1.cfg.setProperty("pagedate", (firstDate.getMonth()+1) + "/" + firstDate.getFullYear()); 
					            YAHOO.example.calendar.cal1.render(); 
					         } else {
                                 xModalMessage('Date Validation', 'Invalid date. MM/DD/YYYY');
					         } 
					   	   	}else{
                                xModalMessage('Date Validation', 'Invalid date. MM/DD/YYYY');
					   	   		this.value="";
					   	   		this.focus();
					   	   	}
					   	   }
						}
					 					    
					 	YAHOO.example.calendar.cal1.selectEvent.subscribe(YAHOO.example.calendar.handleSelect, YAHOO.example.calendar.cal1, true);
					 
					    YAHOO.example.calendar.cal1.render(); 
					}else {
						if (fdField.getPossiblevalues_possiblevalue().length>0){
							var select = document.createElement("SELECT");
							select.name=fdField.getXmlpath();
							select.subjectManager=this;
							if (!fdField.isRequired(false)){
								select.options[0]=new Option("(SELECT)","");
							}
							
							for(var pvCount=0;pvCount<fdField.getPossiblevalues_possiblevalue().length;pvCount++){
								var pv = fdField.getPossiblevalues_possiblevalue()[pvCount];
								var index=select.length;
								if (pv.getDisplay()==null){
									select.options[index]=new Option(pv.getPossiblevalue(),pv.getPossiblevalue());
								}else{
									select.options[index]=new Option(pv.getDisplay(),pv.getPossiblevalue());
								}
								if(this.subject.getProperty(fdField.getXmlpath())==pv.getPossiblevalue()){
									select.selectedIndex=index;
								}
							}
							select.onchange=function(){
								var v = this.options[this.selectedIndex].value;
								this.subjectManager.subject.setProperty(this.name,v);
							}
							fieldTH2.appendChild(select);
						}else{
							var input = document.createElement("INPUT");
							input.type="text";
							input.size="20";
							input.name=fdField.getXmlpath();
							input.subjectManager=this;
							if(this.subject.getProperty(fdField.getXmlpath())!=null){
								input.value=fdField.getXmlpath();
							}
							input.onchange=function(){
								this.subjectManager.subject.setProperty(this.name,this.value);
							}
							fieldTH2.appendChild(input);
						}
					}
						fieldTR.appendChild(fieldTH2);
					 
					fieldTbody.appendChild(fieldTR);
				
				}
								
				tr.appendChild(td1);
				tbody.appendChild(tr);
			}
		}
	
		
		//PROJECT SPECIFIC GROUPS
		tr = document.createElement("TR");
		td1 = document.createElement("TD");
		td1.colSpan="2";
		
		var protTable = document.createElement("TABLE");
		var protTbody = document.createElement("TBODY");
		var protTr = document.createElement("TR");
		protTbody.appendChild(protTr);
		protTable.appendChild(protTbody);
		
		td1.appendChild(protTable);
		tr.appendChild(td1);
		tbody.appendChild(tr);
				
		for(var fDcount=0;fDcount<this._fieldDefinitionGroups.length;fDcount++){
			var fdGroup=this._fieldDefinitionGroups[fDcount];
			if (fdGroup.isProjectSpecific(true)){
				var protTD= document.createElement("TD");
				protTD.vAlign="top";
				
				var protDIV = document.createElement("DIV");
				protDIV.className="container";
				protDIV.style.width="180px";
				var header = document.createElement("DIV");
				header.className="withColor containerTitle";
				header.innerHTML="&nbsp;" + fdGroup.getId();
				protDIV.appendChild(header);
				var protBody= document.createElement("DIV");
				protBody.className="containerBody";
				protDIV.appendChild(protBody);
				
				var fieldTable = document.createElement("TABLE");
				var fieldTbody = document.createElement("TBODY");
				fieldTable.appendChild(fieldTbody);
				protBody.appendChild(fieldTable);
				
			  	    
	
				//INSERT GROUP
				for (var fDfCount=0;fDfCount<fdGroup.getFields_field().length;fDfCount++){
					var fdField = fdGroup.getFields_field()[fDfCount];
					//INSERT FIELD
					var fieldTR=document.createElement("TR");
					var fieldTH1=document.createElement("TH");
					var fieldTH2=document.createElement("TD");
					fieldTH1.innerHTML=fdField.getName();
					fieldTH1.align="left";
					fieldTR.appendChild(fieldTH1);
					
					if (fdField.getDatatype()=="boolean"){
						
					}else if (fdField.getDatatype()=="date"){
						var input = document.createElement("INPUT");
						input.type="text";
						input.size="10";
						input.name=fdField.getXmlpath();
						input.subjectManager=this;
						if(this.subject.getProperty(fdField.getXmlpath())!=null){
							input.value=fdField.getXmlpath();
						}
						input.onchange=function(){
							this.subjectManager.subject.setProperty(this.name,this.value);
						}
						
						fieldTR.appendChild(fieldTH2);
					
						//INSERT CALENDAR
						var calendarContainer = document.createElement("DIV");
						calendarContainer.className="yui-skin-sam";
						
						
						var calendarDIV= document.createElement("DIV");
						calendarContainer.appendChild(calendarDIV);
					
						var fieldTH2=document.createElement("TD");
						fieldTH2.rowSpan="5";
						fieldTH2.appendChild(calendarContainer);
						
						YAHOO.namespace("example.calendar");
						YAHOO.example.calendar.init = function() { 
					        YAHOO.example.calendar.cal1 = new YAHOO.widget.Calendar("dob",calendarDIV,{navigator:true}); 
					        YAHOO.example.calendar.cal1.render(); 
					    } 
					 
					 	YAHOO.example.calendar.input=input;
					    
					    YAHOO.example.calendar.handleSelect = function(type,args,obj) { 
					        var dates = args[0]; 
	    					var date = dates[0]; 
	    					var year = date[0], month = date[1], day = date[2]; 
	  
	    					this.input.value = month + "/" + day + "/" + year; 
	    					this.input.subjectManager.subject.setProperty(this.input.name,this.input.value);
					    }
					 
					 	YAHOO.example.calendar.cal1.selectEvent.subscribe(YAHOO.example.calendar.handleSelect, YAHOO.example.calendar.cal1, true);
					 
					    YAHOO.util.Event.onDOMReady(YAHOO.example.calendar.init); 

						fieldTR.appendChild(fieldTH2);
					}else {
						if (fdField.getPossiblevalues_possiblevalue().length>0){
							var select = document.createElement("SELECT");
							select.name=fdField.getXmlpath();
							select.subjectManager=this;
							if (!fdField.isRequired(false)){
								select.options[0]=new Option("(SELECT)","");
							}
							
							for(var pvCount=0;pvCount<fdField.getPossiblevalues_possiblevalue().length;pvCount++){
								var pv = fdField.getPossiblevalues_possiblevalue()[pvCount];
								var index=select.length;
								if (pv.getDisplay()==null){
									select.options[index]=new Option(pv.getPossiblevalue(),pv.getPossiblevalue());
								}else{
									select.options[index]=new Option(pv.getDisplay(),pv.getPossiblevalue());
								}
								if(this.subject.getProperty(fdField.getXmlpath())==pv.getPossiblevalue()){
									select.selectedIndex=index;
								}
							}
							select.onchange=function(){
								var v = this.options[this.selectedIndex].value;
								this.subjectManager.subject.setProperty(this.name,v);
							}
							fieldTH2.appendChild(select);
							fieldTR.appendChild(fieldTH2);
						}else{
							var input = document.createElement("INPUT");
							input.type="text";
							input.size="10";
							input.name=fdField.getXmlpath();
							input.subjectManager=this;
							if(this.subject.getProperty(fdField.getXmlpath())!=null){
								input.value=fdField.getXmlpath();
							}
							input.onchange=function(){
								this.subjectManager.subject.setProperty(this.name,this.value);
							}
							fieldTH2.appendChild(input);
							fieldTR.appendChild(fieldTH2);
						}
					}
					 
					fieldTbody.appendChild(fieldTR);
				
				}
				
				protTD.appendChild(protDIV);
				protTr.appendChild(protTD);
			}
		}
	}
	
	//SAVE
	tr = document.createElement("TR");
	td1 = document.createElement("TD");
	td2 = document.createElement("TD");
	input = document.createElement("INPUT");
	input.type="button";
	input.subjectForm=this;
	input.value="Save";
	input._callback=this.callback;
	input.onclick=function(){
		this.subjectForm.save(this._callback);
	}
	
	td1.appendChild(input);
	td1.colSpan="2";
	td1.align="center";
	tr.appendChild(td1);
	tbody.appendChild(tr);
}


this.loadFieldDefinitionGroups=function(){
	 var xss = new xdat_stored_search();
	 xss.setRootElementName("xnat:datatypeProtocol");
  	 var critset = new xdat_criteria_set();
	 critset.setMethod("OR");
	 
	 var subset= new xdat_criteria_set();
	 subset.setMethod("AND");
	 
  	 var crit = new xdat_criteria(); 
	 crit.setSchemaField("xnat:datatypeProtocol/xnat_projectdata_id");
	 crit.setComparisonType("=");
	 crit.setValue(this.subject.getProject());		
	 subset.addCriteria(crit);
	 
  	 var crit = new xdat_criteria(); 
	 crit.setSchemaField("xnat:datatypeProtocol/data-type");
	 crit.setComparisonType("=");
	 crit.setValue("xnat:subjectData");		
	 subset.addCriteria(crit);
	 
	 critset.addChildSet(subset);
	 	
	 for (var projCount=0;projCount<this.subject.getSharing_share().length;projCount++){
		 var subset= new xdat_criteria_set();
		 subset.setMethod("AND");
		 
	  	 var crit = new xdat_criteria(); 
		 crit.setSchemaField("xnat:datatypeProtocol/xnat_projectdata_id");
		 crit.setComparisonType("=");
		 crit.setValue(this.subject.getSharing_share()[projCount].getProject());		
		 subset.addCriteria(crit);
		 
	  	 var crit = new xdat_criteria(); 
		 crit.setSchemaField("xnat:datatypeProtocol/data-type");
		 crit.setComparisonType("=");
		 crit.setValue("xnat:subjectData");		
		 subset.addCriteria(crit);
		 
		 critset.addChildSet(subset);
	 }
	 	 
	 xss.addSearchWhere(critset);
	
	 var search = xss.toXML("");
	
	 if (window.XMLHttpRequest) {
	    this.req = new XMLHttpRequest();
	  } else if (window.ActiveXObject) {
	    this.req = new ActiveXObject("Microsoft.XMLHTTP");
	  }
	  
	 var url = "remote-class=org.nrg.xdat.ajax.XMLSearch";
     url = url + "&remote-method=execute";
     url = url + "&search="+escape(search);
     url = url + "&allowMultiples=true";
     url = url + "&XNAT_CSRF="+csrfToken;
	  
	 this.req.open("POST", serverRoot + "/servlet/AjaxServlet", false);
	 this.req.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
	 			
	 this.req.send(url);
	 
	 
	 
	 if (this.req!==false) {     
		if (this.req.status==200) {      
			var arr,src='',parser = new SAXDriver();
			var handler = new SAXEventHandler();
			
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);
			
			parser.parse(this.req.responseText);// start parsing                        
			
			var groups = new Array();
			var groupIDs = new Array();
			
			if (handler.items.length>0){
				for(var itemCount=0;itemCount<handler.items.length;itemCount++){
					var fdProt = handler.items[itemCount];
					for(var fdProtGroupCount=0;fdProtGroupCount<fdProt.getDefinitions_definition().length;fdProtGroupCount++){
						var protGroup = fdProt.getDefinitions_definition()[fdProtGroupCount];
						if (!groupIDs.contains(protGroup.getId())){
							groupIDs.push(protGroup.getId());
							groups.push(protGroup);
						}
					}
				}
			}else{
				if (handler.root){
					var fdProt=handler.root;
					for(var fdProtGroupCount=0;fdProtGroupCount<fdProt.getDefinitions_definition().length;fdProtGroupCount++){
						var protGroup = fdProt.getDefinitions_definition()[fdProtGroupCount];
						if (!groupIDs.contains(protGroup.getId())){
							groupIDs.push(protGroup.getId());
							groups.push(protGroup);
						}
					}
				}
			}
			
			
			return groups;
		}
	 }
}

this.draw();



this.canSave=function(){
	if (this.originalXML==this.subject.toXML("")){
		return false;
	}else{
		return true;
	}
}


this.save=function(callBack){
//	if(!confirm(this.subject.toXML(""))){
//		return;
//	}
	
	if ((DEBUG) && !confirm(this.subject.toXML(""))){
		return;
	}
	
	if (window.XMLHttpRequest) {
	    var req = new XMLHttpRequest();
	 } else if (window.ActiveXObject) {
	    var req = new ActiveXObject("Microsoft.XMLHTTP");
	 }
	  
	 var url = serverRoot + "/servlet/AjaxServlet";
	  
	 req.open("POST", url, false);
	 req.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
	 req.onreadystatechange = callBack;
	 
	 callBack.req = req;
	 
	 req.send("remote-class=org.nrg.xnat.ajax.StoreSubject&remote-method=execute&subject=" + escape(this.subject.toXML("")) + "&XNAT_CSRF="+csrfToken);
   
	 
	 if (req.readyState == 4) {
        if (req.status == 200) {
            // handle response 
            var arr,src='',parser = new SAXDriver();
			var handler = new SAXEventHandler();
			
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);
			
			parser.parse(req.responseText);// start parsing
			
			if (handler.message){
			   var	msg = handler.message;
               if (msg.indexOf("Matched pre-existing")!=-1)
               {
	               var subjectDIV = document.getElementById("message");
	               this.callback.message("<br><font color='red'>" + msg + "</font><br>The following archive " + XNAT.app.displayNames.plural.subject.toLowerCase() + " matched your search. Please select the correct " + XNAT.app.displayNames.singular.subject.toLowerCase() + ".");
					
				   if (handler.items.length>0){	       
	               		this.callback.showMatchedSubjects(handler.items);
	               		return;
				   }else{
	               		this.callback.showMatchedSubjects([handler.root]);
	               		return;
				   }
	               
               }else{
	                 if (msg.indexOf("Subject Stored.")!=-1)
	                 {
		                   this.callback.message("The " + XNAT.app.displayNames.singular.subject.toLowerCase() + " you entered was stored.");
		                   this.callback.select(handler.root);
	                 }else{
		                   this.callback.message(msg);
		                   this.callback.reset();
	                 }
               }
			}
       }
    }   
}
}