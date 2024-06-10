/*
 * web: ajaxProtocolManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");
dynamicJSLoad("xdat_stored_search","generated/xdat_stored_search.js");
dynamicJSLoad("xdat_search_field","generated/xdat_search_field.js");
dynamicJSLoad("xdat_criteria_set","generated/xdat_criteria_set.js");
dynamicJSLoad("xdat_criteria","generated/xdat_criteria.js");
dynamicJSLoad("xnat_datatypeProtocol","generated/xnat_datatypeProtocol.js");
dynamicJSLoad("xnat_fieldDefinitionGroup","generated/xnat_fieldDefinitionGroup.js");
dynamicJSLoad("xnat_fieldDefinitionGroup_field","generated/xnat_fieldDefinitionGroup_field.js");
dynamicJSLoad("xnat_fieldDefinitionGroup_field_possibleValue","generated/xnat_fieldDefinitionGroup_field_possibleValue.js");

dynamicJSLoad("GroupManager","fieldGroupManager.js");

var DEBUG= false;//pre-display xml
var DEBUG2= false;//writes to console
var ALLOW_EDIT= true;

window.groupManagers=new Array();
window.protocolsManagers=new Array();

function ProtocolManager(_id,_titleID,_bodyID,_opts){
    this.opts=_opts;
    this.id=_id;
    this.titleDIV=_titleID;

    this.bodyDIV=_bodyID;
    this.msgDIV=document.createElement("DIV");
    this.bodyDIV.parentNode.insertBefore(this.msgDIV,this.bodyDIV);
    this.protocol=null;
    var instance=this;

    this.onSave=new YAHOO.util.CustomEvent("save",this);

    window.protocolsManagers.push(this);


    this.bundleCallback=function(o){
        if(DEBUG2)writeConsole("ProtocolManager.bundleCallback():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
        closeModalPanel("def_sch");
        this.onSave.fire();
    }

    this.bundleFailure=function(o){
        if (!window.leaving) {
            if(DEBUG2) {
                writeConsole("ProtocolManager.bundleFailure():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
            }
            closeModalPanel("def_sch");
        }
    }

//resets the bundle for the project to use the new structure
    this.resetBundle=function(){
        if(DEBUG2)writeConsole("ProtocolManager.resetBundle():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");

        var url = serverRoot + "/servlet/AjaxServlet?remote-class=org.nrg.xnat.ajax.ResetProjectBundle";
        url = url + "&remote-method=execute";
        url = url + "&protocol="+this.protocol.getProperty("xnat_abstractProtocol_id");
        url = url + '&timestamp=' + (new Date()).getTime();

        var callback={
            success:this.bundleCallback,
            failure:this.bundleFailure,
            cache:false, // Turn off caching for IE
            scope:this
        }
        openModalPanel("def_sch","Reset default searches...");
        YAHOO.util.Connect.asyncRequest('GET',url,callback,this,true);
        return true;
    }

    this.setMessage=function(msg){
        this.msgDIV.innerHTML=msg;
        this.msgDIV.style.display="block";
    }

    this.hideMessage=function(){
        this.msgDIV.style.display="none";
    }

    this.show=function(){
        if(this.protocol!=null){
            if(DEBUG2)writeConsole("ProtocolManager.show():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
            for(var showCount=0;showCount<window.protocolsManagers.length;showCount++){
                window.protocolsManagers[showCount].bodyDIV.style.display='none';
                if(window.protocolsManagers[showCount].titleDIV)
                    window.protocolsManagers[showCount].titleDIV.className='titleBarLink';
            }
            instance.bodyDIV.style.display='block';
            if(instance.titleDIV)
                instance.titleDIV.className='titleBarText';
            this.requestShow=false;
        }else{
            this.requestShow=true;
            this.prepare();
        }
    }
    if(this.titleDIV) {
        this.titleDIV.onclick=this.show;
    }

    this.handleFailedGet=function(o){
        if (!window.leaving) {
            closeModalPanel("load_prot");
            xmodal.message('Protocol Validation', "Failed to load protocol.");
        }
    }

    this.prepare=function(){
        if(DEBUG2)writeConsole("ProtocolManager.prepare():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");

        var catCallback={
            success:this.prepareCallback,
            failure:this.handleFailedGet,
            cache:false, // Turn off caching for IE
            scope:this
        }
        openModalPanel("load_prot","Loading protocol details...");

        var url=serverRoot + '/REST/projects/' + this.opts.project +'/protocols/' + this.id + '?format=xml&timestamp=' + (new Date()).getTime();
        if(this.opts!=undefined && this.opts.dataType!=undefined)
            url+="&dataType="+ this.opts.dataType;
        YAHOO.util.Connect.asyncRequest('GET',url,catCallback,null,this);

    }

    this.canSave=function(){
        if (this.originalXML==this.protocol.toXML("")){
            return false;
        }else{
            return true;
        }
    }

    this.processSave=function(o){
        closeModalPanel("prot_save");
        var arr,src='',parser = new SAXDriver();
        var handler = new SAXEventHandler();

        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);
        parser.setLexicalHandler(handler);

        parser.parse(o.responseText);// start parsing

        this.protocol=handler.root;
        this.originalXML =this.protocol.toXML("");
        this.draw();
        if(this.titleDIV)
            this.titleDIV.className='titleBarText';

        this.resetBundle();
        return true;
    }


    this.handleFailedSave=function(o){
        closeModalPanel("prot_save");
        xmodal.message('Protocol Validation', "Failed to save modifications.");
    }

    this.save=function(){
        var catCallback={
            success:this.processSave,
            failure:this.handleFailedSave,
            cache:false, // Turn off caching for IE
            scope:this
        }
        openModalPanel("prot_save","Saving modifications...");

        var new_xml=this.protocol.toXML("");
        if(DEBUG){if(!confirm(new_xml)){return;}}
        YAHOO.util.Connect.asyncRequest('PUT',serverRoot + '/REST/projects/' + this.opts.project +'/protocols/' + this.id + '?req_format=xml&format=json&populateFromDB=false&timestamp=' + (new Date()).getTime() + '&XNAT_CSRF=' + csrfToken + '&event_reason=standard',catCallback,new_xml,this);
    }

    this.prepareCallback=function(o){
        closeModalPanel("load_prot");
        var arr,src='',parser = new SAXDriver();
        var handler = new SAXEventHandler();

        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);
        parser.setLexicalHandler(handler);

        parser.parse(o.responseText);// start parsing


        this.protocol=handler.root;
        if(this.protocol!=null){
            this.originalXML =this.protocol.toXML("");
            this.draw();


            if(this.protocol.getProperty("data-type")=="xnat:subjectData"){
                if(this.titleDIV)
                    this.titleDIV.className='titleBarText';
            }
        }

        if (this.requestShow){
            this.show();
        }
    }

    this.modify=function(grp){
        if(DEBUG2)writeConsole("ProtocolManager.modify():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
        var definitions=this.protocol.getDefinitions_definition();
        if (definitions){
            var indexes = new Array();
            for (var modCount=0;modCount<definitions.length;modCount++){
                if(definitions[modCount].getId()==grp.getId()){
                    indexes.push(modCount);
                }
            }

            if (indexes.length==0){
                this.protocol.addDefinitions_definition(grp);
            }else{
                for(var iC=0;iC<indexes.length;iC++){
                    var indexC=indexes.reverse()[iC];
                    this.protocol.getDefinitions_definition().splice(indexC,1);
                }
            }
        }

        //this.draw();
    }

    this.draw=function(){
        if(DEBUG2)writeConsole("ProtocolManager.draw():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
        if(this.titleDIV){
            this.titleDIV.innerHTML=this.protocol.getProperty("name");
            this.titleDIV.className='titleBarLink';
        }

        if (this.protocol.xsiType=='xnat:datatypeProtocol'){
            this.clearProtocolBox();
            this.drawProtocolBox();
        }else if (this.protocol.xsiType=='xnat:abstractProtocol'){
            xmodal.message('Protocol Validation', "WARNING: This " + XNAT.app.displayNames.singular.project.toLowerCase() + " is using a deprecated protocol.")
        }else{

        }
    }

    this.clearProtocolBox=function(){
        while(this.bodyDIV.childNodes.length>0){
            this.bodyDIV.removeChild(this.bodyDIV.childNodes[0]);
        }
    }

    this.drawProtocolBox=function (){
        if(DEBUG2)writeConsole("ProtocolManager.drawProtocolBox():&nbsp;&nbsp;&nbsp;" + this.id + "<br>");
        this.createLink = document.createElement("DIV");
        var createForm = document.createElement("DIV");
        var table = document.createElement("TABLE");
        var tbody = document.createElement("TBODY");
        var tr=document.createElement("TR");
        var td1=document.createElement("TD");
        var td2=document.createElement("TD");
        var div1=document.createElement("DIV");
        var div2=document.createElement("DIV");
        td1.appendChild(div1);
        td1.width="500";
        td1.style.verticalAlign="top";
        td2.style.verticalAlign="top";
        //td2.style.border="solid 1px grey";
        td2.appendChild(div2);

        div1.style.border="1px solid #DEDEDE";

        tr.appendChild(td1);
        tbody.appendChild(tr);

        tr=document.createElement("TR");
        tr.appendChild(td2);
        tbody.appendChild(tr);

        table.appendChild(tbody);
        this.bodyDIV.appendChild(table);

        //build current groupings
        var definitions=this.protocol.getDefinitions_definition();
        var currentDefinitions = new Array();
        if (definitions){
            for (var defCount=0;defCount<definitions.length;defCount++){
                currentDefinitions.push(definitions[defCount].getId());
            }
        }

        var dtIndex = window.allGroups.indexOf(this.protocol.getProperty("data-type"));
        if (dtIndex > -1){
            var grouptable = document.createElement("TABLE");
            var grouptbody = document.createElement("TBODY");
            grouptable.appendChild(grouptbody);
            div1.appendChild(grouptable);

            var grouptr,grouptd1,grouptd2,grouptd3;

            //HEADER ROW
            grouptr= document.createElement("TR");
            grouptd1= document.createElement("TD");
            grouptd1.style.verticalAlign="top";
            grouptd1.colSpan="2";
            grouptr.appendChild(grouptd1);
            grouptbody.appendChild(grouptr);
            grouptd1.innerHTML="<DIV class='edit_header2'>Select from available variables.</DIV>";

            for(var l=0;l<window.allGroups.definitionDataTypeGroups[dtIndex].length;l++){
                var group = window.allGroups.definitionDataTypeGroups[dtIndex][l];

                if ((ArrayIndexOf(currentDefinitions,group.getId())>-1) || (!group.isProjectSpecific(true))){

                    grouptr= document.createElement("TR");
                    grouptd1= document.createElement("TD");
                    grouptd1.style.verticalAlign="top";
                    grouptd2= document.createElement("TD");
                    grouptd2.style.verticalAlign="top";
                    grouptd3= document.createElement("TD");
                    grouptd3.style.verticalAlign="top";
                    grouptr.appendChild(grouptd1);
                    grouptr.appendChild(grouptd2);
                    grouptr.appendChild(grouptd3);
                    grouptbody.appendChild(grouptr);

                    var include_check = document.createElement("INPUT");
                    include_check.type="checkbox";
                    include_check.name=this.protocol.getProperty("data-type");
                    include_check.value=group.getId();
                    include_check.protocolManager=this;
                    include_check.fieldGroup=group;
                    if (ArrayIndexOf(currentDefinitions,group.getId())>-1){
                        include_check.checked=true;
                        include_check.defaultChecked=true;
                    }

                    if (group.getId()=="default"){
                        include_check.checked=true;
                        include_check.defaultChecked=true;
                        include_check.disabled=true;
                    }
                    grouptd1.appendChild(include_check);

                    var fields = document.createElement("DIV");
                    fields.style.display="none";

                    var labe = document.createElement("DIV");
                    //labe.for=group.getId();
                    labe.style.fontWeight="700";
                    labe.className="link";
                    labe.innerHTML=group.getId();
                    labe.fieldsBox=fields;
                    labe.onclick=function(){
                        if(this.fieldsBox.style.display=="block")
                            this.fieldsBox.style.display="none";
                        else
                            this.fieldsBox.style.display="block";
                    }
                    if (group.getDescription()!=null && group.getDescription()!="")
                        labe.innerHTML+=": " + group.getDescription();

                    if (group.getId()!="default"){
                        include_check.onclick=function(){
                            this.protocolManager.modify(this.fieldGroup);
                        }
                    }else{
                        labe.style.color="#AAAAAA";
                    }

                    grouptd2.appendChild(labe);

                    if (group.getFields_field().length>0){
                        var fieldstable = document.createElement("TABLE");
                        var fieldstbody = document.createElement("TBODY");
                        fieldstable.appendChild(fieldstbody);
                        fields.appendChild(fieldstable);
                        grouptd2.appendChild(fields);
                        for(var j=0;j<group.getFields_field().length;j++){

                            var field = group.getFields_field()[j];
                            var fieldTR = document.createElement("TR");
                            var fieldTD1 = document.createElement("TD");
                            var fieldTD2 = document.createElement("TD");
                            fieldTD1.innerHTML=field.getName();
                            fieldTD2.innerHTML="(" + field.getDatatype() +")";
                            fieldTR.appendChild(fieldTD1);
                            fieldTR.appendChild(fieldTD2);
                            fieldstbody.appendChild(fieldTR);
                        }
                    }

                    if(ALLOW_EDIT){
                        var input =document.createElement("INPUT");
                        input.type="button";
                        input.value="EDIT";
                        input.group=group;
                        input.formDestination=createForm;
                        input.msgDIV=this.msgDIV;
                        input.onclick=function(){
													if(this.group.DataType && this.group.Description){
														var unique = true;
														for(var i=0; i<window.groupManagers.length; i++){
															if(window.groupManagers[i]){
																if(window.groupManagers[i].group){
																	if(window.groupManagers[i].group.DataType == this.group.DataType
																		&& window.groupManagers[i].group.Description == this.group.Description){
																		unique = false;
																	}
																}
															}
														}
														if(unique){
															createAndAddManager(this);
														}
													} else {
														createAndAddManager(this);
													}
                        }
                        grouptd3.appendChild(input);
                    }
                }
            }

            var subDiv=document.createElement("DIV");
            subDiv.appendChild(document.createElement("BR"));

//		var input = document.createElement("INPUT");
//		input.type="button";
//		input.value="Save";
//		input.protocolManager=this;
//		input.onclick=function(){
//			this.protocolManager.save();
//		}
//		
//		if (this.canSave()){
//			input.disabled=false;
//		}else{
//			input.disabled=true;
//		}
//		subDiv.appendChild(input);

            var tr=document.createElement("TR");
            var td1=document.createElement("TD");
            tr.appendChild(td1);
            grouptbody.appendChild(tr);
            td1.colSpan="2";
            td1.appendChild(subDiv);
        }


        //create add group link
        createForm.style.display="none";
        //createForm.style.border="1px solid #DEDEDE";
        //createForm.style.borderColor="gray";
        //createForm.style.borderWidth="thin";

        div2.appendChild(createForm);
        div2.appendChild(this.createLink);
        this.createLink.formDestination=createForm;
        this.createLink.className="link";
        this.createLink.dataType=this.protocol.getProperty("data-type");
        this.createLink.msgDIV=this.msgDIV;
        this.createLink.innerHTML="Add a custom variable set";
        this.createLink.onclick=function(){
            this.newGroup = new xnat_fieldDefinitionGroup();
            var newField = new xnat_fieldDefinitionGroup_field();
            newField.setDatatype("string");
            newField.setType("custom");
            this.newGroup.addFields_field(newField);
            this.newGroup.isNew=true;
            this.newGroup.setProjectSpecific("1");
            this.newGroup.setDataType(this.dataType);
            var unique = true;
						for(var i=0; i<window.groupManagers.length; i++){
							if(window.groupManagers[i]){
								if(window.groupManagers[i].group){
									if(window.groupManagers[i].group.DataType == this.dataType){
										if(!window.groupManagers[i].group.Description){
											unique = false;
										}
									}
								}
							}
						}
						if(unique){
							createAndAddManager(this);
						}
        }
    }

}

function createAndAddManager(pMananger){
	var createLink = pMananger.createLink;
	var newGroup = pMananger.newGroup;
	if(!createLink) {createLink = pMananger;}
	if(!newGroup) {newGroup = pMananger.group;}
	var manager=new GroupManager(newGroup,pMananger.formDestination,createLink,pMananger.msgDIV);	
	window.groupManagers.push(manager);
	manager.draw();
};

function AllPossibleGroups(){
    this.definitionDataTypes=new Array();
    this.definitionDataTypeGroups=new Array();

    this.indexOf=function(dataType){
        var match=-1;
        for(var indexCount=0;indexCount<this.definitionDataTypes.length;indexCount++){
            if (this.definitionDataTypes[indexCount]==dataType){
                match=indexCount;
            }
        }

        return match;
    }

    this.indexOfID=function(dataType,id){
        var match=-1;
        for(var indexIDCount=0;indexIDCount<this.definitionDataTypes.length;indexIDCount++){
            if (this.definitionDataTypes[indexIDCount]==dataType){
                for(var indexsubIDCount=0;indexsubIDCount<this.definitionDataTypeGroups[indexIDCount].length;indexsubIDCount++){
                    if (this.definitionDataTypeGroups[indexIDCount][indexsubIDCount].getId()==id){
                        match=indexsubIDCount;
                        break;
                    }
                }
                break;
            }
        }

        return match;
    }

    this.add=function(dataType,group){
        var index = this.indexOf(dataType);
        if (index==-1){
            index = this.definitionDataTypes.length;
            this.definitionDataTypes.push(dataType);
            this.definitionDataTypeGroups.push(new Array());
        }

        found=false;
        tempGroupArray= this.definitionDataTypeGroups[index];
        for(var tempGroupArrayCounter=0;tempGroupArrayCounter<tempGroupArray.length;tempGroupArrayCounter++){
            if(tempGroupArray[tempGroupArrayCounter].getId()==group.getId()){
                tempGroupArray[tempGroupArrayCounter]=group;
                found=true;
            }
        }

        if(!found){
            this.definitionDataTypeGroups[index].push(group);
        }

    }

    this.init=function(){
        if (window.XMLHttpRequest) {
            this.req = new XMLHttpRequest();
        } else if (window.ActiveXObject) {
            this.req = new ActiveXObject("Microsoft.XMLHTTP");
        }

        var url = serverRoot + "/servlet/AjaxServlet?remote-class=org.nrg.xnat.ajax.RequestProtocolDefinitionGroups";
        url = url + "&remote-method=execute&XNAT_CSRF="+csrfToken;

        this.req.open("GET", url, false);
        this.req.send(null);

        if (this.req!==false) {
            if (this.req.status==200) {
                var xmlDoc = this.req.responseXML;
                if (xmlDoc)
                {
                    var fieldDefinitionGroups = xmlDoc.getElementsByTagName("fieldDefinitionGroups")[0];
                    if (fieldDefinitionGroups)
                    {
                        for(var initCount1=0;initCount1<fieldDefinitionGroups.childNodes.length;initCount1++)
                        {
                            var fieldDefinitionGroupsChild = fieldDefinitionGroups.childNodes[initCount1];
                            var fieldDefinitionGroupsAttributes = fieldDefinitionGroupsChild.attributes;
                            if (fieldDefinitionGroupsAttributes)
                            {
                                var dataType = fieldDefinitionGroupsAttributes.getNamedItem("data-type").value;
                                var name = fieldDefinitionGroupsAttributes.getNamedItem("name").value;
                                var description = fieldDefinitionGroupsAttributes.getNamedItem("description").value;
                                var group = new xnat_fieldDefinitionGroup();
                                group.setId(name);
                                group.setDataType(dataType);
                                group.setDescription(description);

                                this.add(dataType,group);
                            }
                        }
                    }

                    for(var initCount2=0;initCount2<this.definitionDataTypeGroups.length;initCount2++){
                        var array = this.definitionDataTypeGroups[initCount2];
                        for(var arrayCounter=0;arrayCounter<array.length;arrayCounter++){
                            var group = array[arrayCounter];

                            var xss = new xdat_stored_search();
                            xss.setRootElementName("xnat:fieldDefinitionGroup");
                            var critset = new xdat_criteria_set();
                            var crit = new xdat_criteria();

                            crit.setSchemaField("xnat:fieldDefinitionGroup/ID");
                            crit.setComparisonType("=");
                            crit.setValue(group.getId());
                            critset.addCriteria(crit);

                            var crit = new xdat_criteria();
                            crit.setSchemaField("xnat:fieldDefinitionGroup/data-type");
                            crit.setComparisonType("=");
                            crit.setValue(group.getProperty("data-type"));
                            critset.addCriteria(crit);

                            critset.setMethod("AND");
                            xss.addSearchWhere(critset);

                            var search = xss.toXML("");

                            if (window.XMLHttpRequest) {
                                this.req = new XMLHttpRequest();
                            } else if (window.ActiveXObject) {
                                this.req = new ActiveXObject("Microsoft.XMLHTTP");
                            }

                            var url = serverRoot + "/servlet/AjaxServlet?remote-class=org.nrg.xdat.ajax.XMLSearch";
                            url = url + "&remote-method=execute";
                            url = url + "&search="+search;
                            url = url + "&allowMultiples=true";
                            url = url + "&XNAT_CSRF="+csrfToken;

                            this.req.open("GET", url, false);
                            this.req.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");

                            this.req.send(null);

                            if (this.req!==false) {
                                if (this.req.status==200) {
                                    var arr,src='',parser = new SAXDriver();
                                    var handler = new SAXEventHandler();

                                    parser.setDocumentHandler(handler);
                                    parser.setErrorHandler(handler);
                                    parser.setLexicalHandler(handler);

                                    parser.parse(this.req.responseText);// start parsing


                                    array[arrayCounter]=handler.root;
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    this.init();
}

window.addGroup=function(o){
    if(DEBUG2)writeConsole("Window.addGroup()" + o.status +"<br>");
    closeModalPanel("save_fg");
    if (o.responseText.indexOf("<error ")>-1){
        var xmlDoc = o.responseXML;
        if (xmlDoc)
        {
            var error = xmlDoc.getElementsByTagName("error")[0];
            var errorAttributes = error.attributes;
            if (errorAttributes)
            {
                xmodal.message('Protocol Validation', "ERROR: " + errorAttributes.getNamedItem("msg").value);
            }
        }
    }else{
        var arr,src='',parser = new SAXDriver();
        var handler = new SAXEventHandler();

        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);
        parser.setLexicalHandler(handler);
        parser.parse(o.responseText);// start parsing

        var newGroup=handler.root;
        window.allGroups.add(newGroup.getDataType(),newGroup);

        for(var addGroupCount=0;addGroupCount<window.protocolsManagers.length;addGroupCount++){
            var temp = window.protocolsManagers[addGroupCount];
            if (temp.opts.dataType==newGroup.getDataType()){
                if(temp.protocol==null){
                    xmodal.message('Protocol Validation', temp.opts.dataType + " Protocol is null.");
                }
                var found = false;
                for(var defAddCount=0;defAddCount<temp.protocol.Definitions_definition.length;defAddCount++){
                    if (temp.protocol.Definitions_definition[defAddCount].Id==newGroup.Id){
                        found = true;
                        temp.protocol.Definitions_definition[defAddCount]=newGroup;
                    }
                }

                if (!found){
                    temp.protocol.addDefinitions_definition(newGroup);
                    temp.save();
                }
                temp.hideMessage();
                break;
            }
        }
    }
}

window.allGroups=new AllPossibleGroups();
