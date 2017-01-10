/*
 * web: SubjectFinder.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
if(displayedFields==undefined)
	var displayedFields=["xnat:subjectData/ID","xnat:subjectData/demographics/gender","xnat:subjectData/demographics/handedness"];
	
if(displayedFieldHeaders==undefined)
	var displayedFieldHeaders=["Accession","Gender","Handedness"];

function SubjectFinder(_project, _div){
this.project=_project;
this.div=_div;

this.messageBox = document.createElement("DIV");
this.div.appendChild(this.messageBox);


this.draw=function(){
	var table,tbody,tr,td;
	table = document.createElement("TABLE");
	tbody =document.createElement("TBODY");
	
	this.div.appendChild(table);
	table.appendChild(tbody);
	
	this.selectBox=document.createElement("DIV");
	tr= document.createElement("TR");
	td= document.createElement("TD");
	td.appendChild(this.selectBox);
	tr.appendChild(td);
	tbody.appendChild(tr);
	
	this.option1Label=document.createElement("DIV");
	this.option1Label.className="option";
	tr= document.createElement("TR");
	td= document.createElement("TD");
	td.appendChild(this.option1Label);
	tr.appendChild(td);
	tbody.appendChild(tr);
		
	this.option1Body=document.createElement("DIV");
	tr= document.createElement("TR");
	td= document.createElement("TD");
	td.appendChild(this.option1Body);
	tr.appendChild(td);
	tbody.appendChild(tr);
	
	this.option2Label=document.createElement("DIV");
	this.option2Label.className="option";
	tr= document.createElement("TR");
	td= document.createElement("TD");
	td.appendChild(this.option2Label);
	tr.appendChild(td);
	tbody.appendChild(tr);
		
	this.option2Body=document.createElement("DIV");
	tr= document.createElement("TR");
	td= document.createElement("TD");
	td.appendChild(this.option2Body);
	tr.appendChild(td);
	tbody.appendChild(tr);
	
	//option1
	var lab = document.createElement("label");
	this.radioOption1 = document.createElement("INPUT");
	this.radioOption1.type="radio";
	this.radioOption1.name="option";
	this.radioOption1.id="option1";
	this.radioOption1.manager=this;
	this.radioOption1.onclick=function(){
		this.manager.showOption1();
	}
	lab.appendChild(this.radioOption1);
	var optionTxt = document.createElement("DIV");
	optionTxt.style.display="inline";
	optionTxt.innerHTML="Option 1: Find " + XNAT.app.displayNames.singular.subject.toLowerCase() + " in archive";
	lab.appendChild(optionTxt);
	this.option1Label.appendChild(lab);
	
	//option2
	var lab = document.createElement("label");
	this.radioOption2 = document.createElement("INPUT");
	this.radioOption2.type="radio";
	this.radioOption2.name="option";
	this.radioOption2.id="option2";
	this.radioOption2.manager=this;
	this.radioOption2.onclick=function(){
		this.manager.showOption2();
	}
	lab.appendChild(this.radioOption2);
	var optionTxt = document.createElement("DIV");
	optionTxt.style.display="inline";
	optionTxt.innerHTML="Option 2: Create a new " + XNAT.app.displayNames.singular.subject.toLowerCase();
	lab.appendChild(optionTxt);
	this.option2Label.appendChild(lab);
}

this.showOption1=function(){
	this.getSearchManager();
	this.option2Body.style.display="none";
	this.option1Body.style.display="block";
	this.option1Label.style.display="block";
	this.option2Label.style.display="block";
	this.radioOption1.checked=true;
	this.radioOption2.checked=false;
}

this.showOption2=function(){
	this.getFormManager();
	this.option1Body.style.display="none";
	this.option2Body.style.display="block";
	this.option1Label.style.display="block";
	this.option2Label.style.display="block";
	this.radioOption2.checked=true;
	this.radioOption1.checked=false;
}

this.showSelect=function(){
	this.option1Body.style.display="none";
	this.option2Body.style.display="none";
	this.option1Label.style.display="none";
	this.option2Label.style.display="none";
}

this.select=function(subject){
	// this.message(""); // removed to resolve XNAT-3845
	var table,tbody,tr,td1,td2;
	emptyChildNodes(this.selectBox);
	this.selectBox.className="withThinBorder";
	
	this.selectBox.appendChild(document.createElement("DIV"));
	this.selectBox.childNodes[0].innerHTML="&nbsp;<b>Selected " + XNAT.app.displayNames.singular.subject + "<BR></b>";
	this.selectBox.childNodes[0].style.textAlign="center";
	this.selectBox.childNodes[0].className="withColor withThinBorder";
	
	var div = document.createElement("DIV")
	
	this.selectBox.appendChild(div);
	
    table = document.createElement("TABLE");
 	div.appendChild(table);
 	
 	tbody = document.createElement("TBODY");
 	table.appendChild(tbody);
 	
 	for(var header1=0;header1<displayedFieldHeaders.length;header1++){
 		if (subject.getProperty(displayedFields[header1])!=null && subject.getProperty(displayedFields[header1])!=""){
		 	tr = document.createElement("TR");
		 	td1 = document.createElement("TH");
		 	td1.align="left";
		 	td1.style.fontWeight="600";
		 	td2 = document.createElement("TD");
		 	td1.innerHTML=displayedFieldHeaders[header1];
		 	td2.innerHTML=subject.getProperty(displayedFields[header1]);
		 	tr.appendChild(td1);
		 	tr.appendChild(td2);
		 	tbody.appendChild(tr);
 		}
 	}
 	
 	
 	tr = document.createElement("TR");
 	td1 = document.createElement("TD");
 	td1.align="center";
 	td1.colSpan="2";
 	var input = document.createElement("INPUT");
 	input.type="button";
 	input.value="Change " + XNAT.app.displayNames.singular.subject;
 	input.manager=this;
 	input.onclick=function(){
 		this.manager.showOption1();
 		this.style.display="none";
 	}
 	td1.appendChild(input);
 	tr.appendChild(td1);
 	tbody.appendChild(tr);
 	
 	this.subject=subject;
 	
 	document.getElementById("subject_id").value=this.subject.getId();
 	
 	this.showSelect();
}

this.message=function(str){
 this.messageBox.innerHTML=str;
this.messageBox.className="message";
}

this.warning=function(str){
 this.messageBox.innerHTML=str;
 this.messageBox.className="warning";
}

this.showMatchedSubjects=function(subjects){
  this.getSearchManager().renderSubjects(subjects);
}

this.getFormManager=function(){
	  if (this.subjectForm!=undefined){
		this.subjectForm.close();
		this.subjectForm=null;
   	  }
	 
   	  if(window.create_subject_link==undefined){
          xmodal.message('Error', 'Unable to load create ' + XNAT.app.displayNames.singular.subject.toLowerCase() + ' form.');
   	  	return;
   	  }
   	  this.subjectForm=window.open(window.create_subject_link, '','width=500,height=550,status=yes,resizable=yes,scrollbars=yes,toolbar=no');
   	  if (this.subjectForm.opener == null) this.subjectForm.opener = self;
   	  //var sub = new xnat_subjectData();
   	  //sub.setProject(this.project);
	  //this.subjectForm=new SubjectForm(sub,this.option2Body,this);
	 
	return this.subjectForm;
}
this.getSearchManager=function(){
   if (this.subjectSearch==undefined)
      this.subjectSearch=new subjectSearch(this.option1Body,project,this);
   			 
   return this.subjectSearch;
}


this.selectById=function(_selected_id){
	this.getSearchManager().submitSearch(_selected_id,true);
}

this.draw();


}
