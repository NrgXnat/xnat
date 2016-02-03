/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/fieldGroupManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
/***

 * Interface for Creating/Modifying Field Definition Groups.
 * 
 * I strongly suspect this class is littered with problems (like memory leaks).  Its in desperate need of a rewrite.
 */

	var special_characters =[".","$","(",")","[","]",",",";",":","*","/",">","<","{","}","-","=","+","!","@","#","%","^","&","|","~","`","?"];

function GroupManager(_group,_div,_createLink,_msgDIV){
this.onSave=new YAHOO.util.CustomEvent("save",this);
this.group=_group;

this.div=_div;

this.createLink=_createLink;

this.msgDIV=_msgDIV;

this.form;



this.clear=function(){

	while(this.div.childNodes.length>0){

		this.div.removeChild(this.div.childNodes[0]);

	}

}



this.close=function(remove){

	if(DEBUG2)writeConsole("GroupManager.close()<br>");

	if(remove==undefined){

		remove=true;

	}

	  this.div.removeChild(this.form);

	  if(remove){

		  var groupManagers=window.groupManagers;

		  for (var groupManagersCounter=0;groupManagersCounter<groupManagers.length;groupManagersCounter++){

		  	var group1=this.group;

		  	var group2=groupManagers[groupManagersCounter].group;

		     if(group1==group2){

		     	window.groupManagers.splice(groupManagersCounter,1);

		     	break;

		     }	     

		  }

	  }

}

this.handleFailedPost=function(o){
	closeModalPanel("save_fg");
    xModalMessage('Field Group Error', "Failed to create set:" + o.status);
}

this.handleSuccessPost=function(o){
	window.addGroup(o);
	this.onSave.fire();
}

this.save=function(){

	if(DEBUG2)writeConsole("GroupManager.save()<br>");

	var newGroup=this.group;

	if (newGroup.getId()!=null && newGroup.getId()!=""){

		if(newGroup.isNew==undefined || window.allGroups.indexOfID(newGroup.getDataType(),newGroup.getId())==-1){

			var removals = new Array();

			var issue = null;

			for(var fieldCounter=0;fieldCounter<newGroup.getFields_field().length;fieldCounter++){

				if (newGroup.getFields_field()[fieldCounter].getName()==""||newGroup.getFields_field()[fieldCounter].getName()==null){

					removals.push(fieldCounter);

				}else{

					if (issue==null){

						var scCounter=0;

						for(scCounter=0;scCounter<special_characters.length;scCounter++){

							if(newGroup.getFields_field()[fieldCounter].getName().indexOf(special_characters[scCounter])>-1){

								issue="Illegal character '" + special_characters[scCounter] + "' in variable name '" + newGroup.getFields_field()[fieldCounter].getName() + "'.";

							}

						}

					}

				}

			}

			removals = removals.reverse();

			for(var removalCounter=0;removalCounter<removals.length;removalCounter++){				

				newGroup.getFields_field().splice(removals[removalCounter],1);

			}

			
			for(var fieldCounter=0;fieldCounter<newGroup.getFields_field().length;fieldCounter++){

				var f =newGroup.getFields_field()[fieldCounter];

				removals = new Array();

				for(var pvCount=0;pvCount<f.getPossiblevalues_possiblevalue().length;pvCount++){

					var pv = f.getPossiblevalues_possiblevalue()[pvCount];

					if(pv.getPossiblevalue()=="" || pv.getPossiblevalue()==null)

					{

						removals.push(pvCount);

					}

				}

				removals = removals.reverse();

				for(var removalCounter=0;removalCounter<removals.length;removalCounter++){	

					var removalIndex=removals[removalCounter];

					f.getPossiblevalues_possiblevalue().splice(removalIndex,1);

				}

			}

			if(issue!=null){

                xModalMessage('Field Group Validation', issue);

				return false;

			}

			if ((DEBUG) && !confirm(newGroup.toXML(""))){

				return;

			}

			 var servlet = serverRoot + "/servlet/AjaxServlet";

			 var postData = "remote-class=org.nrg.xdat.ajax.StoreXML";
		     postData = postData + "&remote-method=execute";
		     postData = postData + "&xml="+this.group.toXML("");
		     postData = postData + "&allowDataDeletion=true";
		     postData = postData + "&XNAT_CSRF=" + csrfToken;
		     postData = postData + "&event_reason=standard";
			  
			 var catCallback={
				success:this.handleSuccessPost,
				failure:this.handleFailedPost,
                 cache:false, // Turn off caching for IE
				scope:this
			 }
			openModalPanel("save_fg","Saving Variable Set...");
			YAHOO.util.Connect.asyncRequest('POST',servlet,catCallback,postData);

			 this.close();

			 return true;

		}else{

			if (this.form && this.form._ID)

				this.form._ID.focus();

            xModalMessage('Field Group Validation', "Error: " + form._ID.value + " already exists.");

			return false;

		}

	}else{

			if (this.form && this.form._ID)

				this.form._ID.focus();

        xModalMessage('Field Group Validation', "Please specify an ID, or cancel group creation.");

		return false;

	}



}



this.draw=function(){

	if(DEBUG2)writeConsole("GroupManager.draw()<br>");

	this.div.style.display="block";

	//build add grouping form

	this.form = document.createElement("FORM");

	var wrapperDIV = document.createElement("DIV");

	wrapperDIV.style.border="1px solid #DEDEDE";

	table = document.createElement("TABLE");

	tbody = document.createElement("TBODY");

	table.appendChild(tbody);

	wrapperDIV.appendChild(table);

	this.form.appendChild(wrapperDIV);

	this.div.appendChild(this.form);

	 
	tr=document.createElement("TR");

	td1=document.createElement("TH");

	td1.align="left";

	if (this.group.isNew!=undefined)

		td1.innerHTML="Custom Variable Set";

	else

		td1.innerHTML="Edit Variable Set";

	td1.colSpan="2";

	tr.appendChild(td1);

	tbody.appendChild(tr);

	 
	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td1.innerHTML="Name:"

	td2=document.createElement("TD");

	if (this.group.isNew!=undefined){

		var input = document.createElement("INPUT");

		input.type="text";

		input.name="_ID";

		input.newGroup=this.group;

		if (this.group.getId()!=null)

			input.value=this.group.getId();

	    input.manager=this;

		input.onchange=function(){

			if(window.allGroups.indexOfID(this.newGroup.getProperty("data-type"),this.value)>-1){

				var tempIndex=window.allGroups.indexOfID(this.newGroup.getProperty("data-type"),this.value);

				var existingGroup=window.allGroups.definitionDataTypeGroups[window.allGroups.indexOf(this.newGroup.getProperty("data-type"))][tempIndex];

				var confirmString = "A group named '" + this.value +"' already exists with " + existingGroup.getFields_field().length + " field(s); ";

				for(var exGFCounter=0;exGFCounter<existingGroup.getFields_field().length;exGFCounter++){

					if(exGFCounter>0)confirmString+=", ";

					confirmString+=existingGroup.getFields_field()[exGFCounter].getName() + " (" +existingGroup.getFields_field()[exGFCounter].getDatatype() + ")";

				}

				confirmString+=".  Would you like to use the pre-existing group in this " + XNAT.app.displayNames.singular.project.toLowerCase() + "?";

				if(confirm(confirmString))

				{

					existingGroup.setProjectSpecific(false);

					var newmanager=new GroupManager(existingGroup,this.manager.div,this.manager.createLink,this.manager.msgDIV);

					this.manager.close();

					window.groupManagers.push(newmanager);

					newmanager.draw();

				}else{

					this.value="";

					this.focus();

				}

			}else{

				this.newGroup.setId(this.value);

			}

		}

		td2.appendChild(input);

	}else{

		td2.appendChild(document.createElement("DIV"));

		td2.childNodes[0].style.display="inline";

		td2.childNodes[0].innerHTML=this.group.getId();

	}

	td2.appendChild(document.createElement("DIV"));

	td2.childNodes[1].style.marginLeft="20";

	td2.childNodes[1].style.display="inline";

	var input = document.createElement("INPUT");

	input.type="checkbox";

	input.group=this.group;

	if (this.group.isProjectSpecific(false))

	{

		input.checked=true;

		input.defaultChecked=true;

	}

	input.onclick=function(){

		this.group.setProjectSpecific(this.checked);

	}

	var labe = document.createElement("LABEL");

	labe.appendChild(input);

	labe.appendChild(document.createElement("DIV"));

	labe.childNodes[1].style.display="inline";

	labe.childNodes[1].innerHTML=XNAT.app.displayNames.singular.project + " Specific?";

	td2.childNodes[1].appendChild(labe);

	tr.appendChild(td1);

	tr.appendChild(td2);

	tbody.appendChild(tr);

	 
	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td1.innerHTML="Description:"

	td2=document.createElement("TD");

	var input = document.createElement("TEXTAREA");

	input.cols="30";

	input.rows="2";

	input.name="description";

	input.newGroup=this.group;

	if (this.group.getDescription()!=null)

		input.value=this.group.getDescription();

	input.onchange=function(){

		this.newGroup.setDescription(this.value);

	}

	td2.appendChild(input);

	tr.appendChild(td1);

	tr.appendChild(td2);

	tbody.appendChild(tr);

	
	tr=document.createElement("TR");

	td1=document.createElement("TH");

	td1.innerHTML="&nbsp;";

	td1.colSpan="2";

	tr.appendChild(td1);

	tbody.appendChild(tr);

	/*****

	tr=document.createElement("TR");

	td1=document.createElement("TH");

	td1.innerHTML="Fields";

	td1.colSpan="2";

	tr.appendChild(td1);

	tbody.appendChild(tr);

	****/

	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td1.innerHTML="<b style='font-size:11px;line-height:13px;font-weight:700;'>Custom Variables</b><br>Please list the variables to be included in this set. (Variable names should be a one word description.)";

	td1.colSpan="2";

	tr.appendChild(td1);

	tbody.appendChild(tr);

	
	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td1.colSpan="2";

	tr.appendChild(td1);

	tbody.appendChild(tr);

	var fieldTable = document.createElement("TABLE");

	var fieldBody = document.createElement("TBODY");

	fieldTable.appendChild(fieldBody);

	td1.appendChild(fieldTable);

	var fieldTR = document.createElement("TR");

	var fieldTD1 = document.createElement("TH");

	fieldTD1.innerHTML="Name";

	var fieldTD2 = document.createElement("TH");

	fieldTD2.innerHTML="Type";

	var fieldTD3 = document.createElement("TH");

	fieldTD3.innerHTML="Required";

	fieldTR.appendChild(fieldTD1);

	fieldTR.appendChild(fieldTD2);

	fieldTR.appendChild(fieldTD3);

	fieldBody.appendChild(fieldTR);

	
	for(var j=0;j<this.group.getFields_field().length;j++){

		var f=this.group.getFields_field()[j];

		//name

		var fieldTR = document.createElement("TR");

		var fieldTD1 = document.createElement("TD");

		var input = document.createElement("INPUT");

		input.type="text";

		input.dataType=this.group.getProperty("data-type");

		input.fieldDefinition=f;

		if (f.getName()!=null)

			input.value=f.getName();

		input.onchange=function(){

			this.value=stringCamelCaps(this.value);

			if(this.value.indexOf("/")>-1){

				this.fieldDefinition.setName(this.value.substring(this.value.lastIndexOf("/")+1));

				this.fieldDefinition.setType("standard");

				this.fieldDefinition.setXmlpath(this.value);

			}else{

				this.fieldDefinition.setName(this.value);

				this.fieldDefinition.setType("custom");

				this.fieldDefinition.setXmlpath(this.dataType +"/fields/field[name=" + this.value.toLowerCase() +"]/field");

			}

		}

		fieldTD1.appendChild(input);

		//datatype

		var fieldTD2 = document.createElement("TD");

		var input = document.createElement("SELECT");

		input.options[0]=new Option("string","string");

		if (f.getDatatype()=="string"){

			input.options[0].selected=true;
			input.selectedIndex=0;
		}

		input.options[1]=new Option("integer","integer");
		if (f.getDatatype()=="integer"){
			input.options[1].selected=true;
			input.selectedIndex=1;
		}

		input.options[2]=new Option("float","float");
		if (f.getDatatype()=="float"){
			input.options[2].selected=true;
			input.selectedIndex=2;
		}

		input.options[3]=new Option("boolean","boolean");
		if (f.getDatatype()=="boolean"){
			input.options[3].selected=true;
			input.selectedIndex=3;
		}

		input.options[4]=new Option("date","date");
		if (f.getDatatype()=="date"){
			input.options[4].selected=true;
			input.selectedIndex=4;
		}

		input.fieldDefinition=f;

		input.onchange=function(){

			this.fieldDefinition.setDatatype(this.options[this.selectedIndex].value);

		}

		fieldTD2.appendChild(input);

		fieldTD1.vAlign="top";

		fieldTR.appendChild(fieldTD1);

		fieldTD2.vAlign="top";

		fieldTR.appendChild(fieldTD2);

		//required

		var fieldTD3 = document.createElement("TD");

		var input = document.createElement("INPUT");

		input.type="checkbox";

		input.fieldDefinition=f;

		if (f.isRequired(false))

		{

			input.checked=true;

			input.defaultChecked=true;

		}

		input.onclick=function(){

			this.fieldDefinition.setRequired(this.checked);

		}

		fieldTD3.appendChild(input);

		fieldTD3.vAlign="top";

		fieldTR.appendChild(fieldTD3);

		//possible values

		var fieldTD4 = document.createElement("TD");
		var fieldTD4Div = document.createElement("DIV");
		fieldTD4.appendChild(fieldTD4Div);

		fieldTD4Div.appendChild(document.createTextNode("Possible Values:"));

		if(f.getPossiblevalues_possiblevalue().length==0){
			var input = document.createElement("INPUT");
			input.type="button";
			input.value="+";
			input.fieldDefinition=f;

			input.onclick=function(){
				var pv = new xnat_fieldDefinitionGroup_field_possibleValue();

				this.fieldDefinition.addPossiblevalues_possiblevalue(pv);

				var input2 = document.createElement("INPUT");
				input2.type="text";
				input2.value=pv.getPossiblevalue();
				input2.pv=pv;

				input2.onchange=function(){
					this.pv.setPossiblevalue(this.value);
				}

				this.parentNode.insertBefore(input2,this);

			}

			fieldTD4Div.appendChild(input);	
		}else{

			for(var pvCount=0;pvCount<f.getPossiblevalues_possiblevalue().length;pvCount++){
				var pv = f.getPossiblevalues_possiblevalue()[pvCount];

				var input2 = document.createElement("INPUT");

				input2.type="text";
				input2.value=pv.getPossiblevalue();
				input2.pv=pv;

				input2.onchange=function(){
					this.pv.setPossiblevalue(this.value);
				}

				fieldTD4Div.appendChild(input2);

			}

			var input = document.createElement("INPUT");
			input.type="button";
			input.value="+";
			input.fieldDefinition=f;
			input.onclick=function(){
				var pv = new xnat_fieldDefinitionGroup_field_possibleValue();
				this.fieldDefinition.addPossiblevalues_possiblevalue(pv);
				var input2 = document.createElement("INPUT");
				input2.type="text";
				input2.pv=pv;
				input2.onchange=function(){
					this.pv.setPossiblevalue(this.value);
				}

				this.parentNode.insertBefore(input2,this);
			}

			fieldTD4Div.appendChild(input);	
		}	

		fieldTR.appendChild(fieldTD4);


		fieldBody.appendChild(fieldTR);

	}

	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td1.colSpan="2";

	var addFieldLink= document.createElement("INPUT");

	addFieldLink.type="button";

	addFieldLink.newGroup=this.group;

	addFieldLink.newGroupManager=this;

	addFieldLink.value="Add Variable";

	addFieldLink.onclick=function(){

		var newField = new xnat_fieldDefinitionGroup_field();

		newField.setDatatype("string");

		this.newGroupManager.group.addFields_field(newField);

		this.newGroupManager.close(false);

		this.newGroupManager.draw();

	}

	td1.appendChild(addFieldLink);

	tr.appendChild(td1);

	tbody.appendChild(tr);

	tr=document.createElement("TR");

	td1=document.createElement("TD");

	td2=document.createElement("TD");

	td2.align="right";

	var input = document.createElement("input");

	input.type="button";
	input.value="Save";
	input.manager=this;
	input.onclick=function(){
		this.manager.save();
	}
	td2.appendChild(input);

	var input = document.createElement("input");
	input.type="button";
	input.value="Cancel";

	input.manager=this;

	input.onclick=function(){
		this.manager.close();
	}

	td2.appendChild(input);

	tr.appendChild(td1);

	tr.appendChild(td2);

	tbody.appendChild(tr);

}

}