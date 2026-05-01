/*
 * web: ConfigureSearchViews.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * @author Tim Olsen
 *
 * Javascript object for usage in ConfigureSearchViews.vm
 */
function ConfigureSearchViews(selectId) {
    this.url = serverRoot + "/REST/search/elements?format=json";
    this.selectId = $(selectId);

    this.init = function () {
        $('#addFieldElement').on('change', function() {
            manager.loadNewFields();
        });

        this.selectId.on('change', function() {
            manager.resetForm();
            manager.loadViews();
        });

        jq("#selectViews").on('change', function() {
            manager.resetForm();
            manager.loadFields();
        });

        $('#addFieldButton').on('click', function() {
            manager.addField();
        });

        var that = this;
        jq.get(this.url )
            .done(function (data) {
                that.selectId.empty();
                that.selectId.append($("<option></option>")
                    .attr("value", "")
                    .text("SELECT"));
                jq(data.ResultSet.Result).each(function(index, obj){
                    that.selectId.append($("<option></option>")
                        .attr("value", obj.ELEMENT_NAME)
                        .text(obj.SINGULAR));
                });

                var addFieldElement = $("#addFieldElement");
                addFieldElement.empty();
                addFieldElement.append($("<option></option>")
                    .attr("value", "")
                    .text("SELECT"));
                jq(data.ResultSet.Result).each(function(index, obj){
                    addFieldElement.append($("<option></option>")
                        .attr("value", obj.ELEMENT_NAME)
                        .text(obj.SINGULAR));
                });
                $("#newFieldContainer").show();
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    };

    this.resetForm = function(){
        this.currentVersion=null;
        this.newField=null;
        jq("#listOfFields").html("");
    };

    this.loadViews = function(){
        var elementName = jq("#selectElements").val();
        if(elementName==null){
            showMessage("page_body", "Error", "Select an element.");
        }
        var selectViews = jq("#selectViews");
        selectViews.empty();

        jq.get(serverRoot + "/REST/search/elements/"+elementName + "/versions?format=json")
            .done(function (data) {
                selectViews.append($("<option></option>")
                    .attr("value", "")
                    .text("SELECT"));
                jq(data.DisplayVersions.versions).each(function(index, obj){
                    var txt=obj.name;
                    if(obj.name==="all"){
                        return;
                    }
                    if(obj.name==="listing"){
                        txt="listing (Primary Listing)"
                    }if(obj.name==="project_bundle"){
                        txt="project_bundle (Default Project Listing)"
                    }
                    selectViews.append($("<option></option>")
                        .attr("value", obj.name)
                        .text(txt));
                });
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    };

    this.loadFields = function(){
        var elementName = jq("#selectElements").val();
        var versionName = jq("#selectViews").val();
        if(elementName==null){
            showMessage("page_body", "Error", "Select an element.");
        }
        if(versionName==null){
            showMessage("page_body", "Error", "Select a version.");
        }

        var that = this;
        jq.get(serverRoot + "/REST/search/elements/"+elementName + "/versions/" + versionName + "?format=json&v2=true")
            .done(function (data) {
                that.currentVersion=data;
                that.renderFields();
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    };

    this.renderFields = function (){
        var listOfFields="";
        jq(this.currentVersion.fields).each(function (fI, field){
            listOfFields+="<TR>";
            listOfFields+="<TD NOWRAP><input type='radio' name='selectedField' onclick='manager.highlightField(" + fI +");' value='" + fI +"'/> " + field.sortOrder + "</TD>";
            listOfFields+="<TD NOWRAP>" + field.elementName + "</TD>";
            listOfFields+="<TD>" + field.fieldId + "<input type='hidden' id='element" + fI +"' value='" + field.elementName + "'/><input type='hidden' id='field" + fI +"' value='" + field.fieldId + "'/></TD>";
            listOfFields+="<TD><input type='text' id='header" + fI +"' value='" + field.header + "' style='width:120px'/></TD>";
            listOfFields+="<TD><input type='text' id='value" + fI +"' value='" + ((field.value==null)?"":field.value) + "' style='width:120px' DISABLED/></TD>";
            listOfFields+="<TD NOWRAP><input type='button' value='Remove' onclick='manager.removeField(" + fI +");'/></TD>";
            listOfFields+="</TR>";
        });
        listOfFields+="<TR><TD></TD><TD></TD><TD></TD><TD></TD></TD><TD><input class='btn1' type='button' onclick='manager.update();' value='Save'/></TD></TR>";
        jq("#listOfFields").html(listOfFields);
    };

    this.setUpDownButtons = function(row){
        $('input[name="upButton"]').prop( "disabled", true );
        $('input[name="downButton"]').prop( "disabled", true );

        if(row>0){
            $('input[name="upButton"]').prop( "disabled", false );
        }

        if(row<(this.currentVersion.fields.length-1)){
            $('input[name="downButton"]').prop( "disabled", false );
        }

    };

    this.highlightField = function (row){
        this.currentRow=row;

        this.setUpDownButtons(this.currentRow);
    };

    this.upButton = function (){
        this.upField(this.currentRow);
        this.currentRow=this.currentRow - 1;
        $("input[name=selectedField][value=" + this.currentRow + "]").attr('checked', 'checked');
        this.setUpDownButtons(this.currentRow);
    };

    this.downButton = function (){
        this.downField(this.currentRow);
        this.currentRow=this.currentRow + 1;
        $("input[name=selectedField][value=" + this.currentRow + "]").attr('checked', 'checked');
        this.setUpDownButtons(this.currentRow);
    };

    this.upField = function (index){
        if(index>0){
            var thisObj = this.currentVersion.fields[index];
            thisObj.sortOrder = index-1;

            var prevObj = this.currentVersion.fields[index-1];
            if(prevObj!=null){
                prevObj.sortOrder=index;
            }

            this.currentVersion.fields.sort(compareFields);
            this.renderFields();
        }
    };

    this.downField = function (index){
        if(index<(this.currentVersion.fields.length-1)){
            var thisObj = this.currentVersion.fields[index];
            thisObj.sortOrder = index+1;

            var prevObj = this.currentVersion.fields[index+1];
            if(prevObj!=null){
                prevObj.sortOrder=index;
            }

            this.currentVersion.fields.sort(compareFields);
            this.renderFields();
        }
    };

    this.removeField = function (index){
        this.currentVersion.fields.splice(index,1);
        this.renderFields();
    };

    this.loadNewFields = function(){
        var elementName = jq("#addFieldElement").val();
        var listOfFields="";
        var selectAddField = jq("#addFieldId");
        selectAddField.empty();
        jq.get(serverRoot + "/REST/search/elements/"+elementName + "?format=json")
            .done(function (data) {
                selectAddField.append($("<option></option>")
                    .attr("value", "")
                    .text("SELECT"));
                jq(data.ResultSet.Result).each(function (index, obj){
                    selectAddField.append($("<option></option>")
                        .attr("value", obj.FIELD_ID)
                        .attr("data-header",obj.HEADER)
                        .attr("data-element",obj.ELEMENT_NAME)
                        .attr("data-requiresValue",obj.REQUIRES_VALUE)
                        .attr("data-value",obj.VALUE)
                        .text(obj.HEADER));
                });
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    };

    this.updateCurrentVersion = function (){
        jq(this.currentVersion.fields).each(function (fI, field){
            var newHeader=jq("#header"+fI).val();
            if(! (new RegExp('^[A-Za-z0-9 _/-/./\\//]+$')).test(newHeader)){
                displayError("Invalid characters specified.");
                return false;
            }
            field.header = newHeader;
        });
    };

    this.update = function ( row) {
        var elementName = jq("#selectElements").val();

        this.updateCurrentVersion();

        openModalPanel("updatingElement","Updating");

        var that = this;
        var value = jq(this.id).val();
            jq.ajax({
                url: serverRoot + "/REST/search/elements/" + elementName + "/versions/modify?version=" + this.currentVersion.versionName + "&XNAT_CSRF=" + window.csrfToken,
                type: 'PUT',
                data: JSON.stringify(this.currentVersion),
                contentType: "application/json"
            })
            .done(function () {
                closeModalPanel("updatingElement");
            })
            .fail(function (xhr, status, error) {
                closeModalPanel("updatingElement");
                showMessage("page_body", "Failure", "Failed to modify fields; [" + xhr.status + "] " + error);
            });
    };

    this.addField=function(){
        if($('#addFieldId').val()!="" && (this.currentVersion!=null && this.currentVersion!=undefined)){
            var selectedOption = $('#addFieldId').find(":selected");
            var newField = {};
            newField.elementName = selectedOption.attr("data-element");
            newField.header = selectedOption.attr("data-header");
            newField.fieldId = selectedOption.val();
            newField.sortOrder = this.currentVersion.fields.length;
            newField.value=selectedOption.attr("data-value");
            if(newField.fieldId.indexOf("=")>-1 && (newField.value==null || newField.value=="")){
                newField.value=newField.fieldId.substring((newField.fieldId.indexOf("=")+1));
            }

            var requiresValue = selectedOption.attr("data-requiresValue");
            var value = selectedOption.attr("data-value");
            if(requiresValue==="true" && (newField.value==null || newField.value=="")){
                this.newField=newField;
                this.renderValueForm(newField);
            }else{
                this.addFieldAndUpdate(newField);
            }
        }
    }

    this.addFieldAndUpdate=function (newField){
        this.currentVersion.fields.push(newField);
        $('#addFieldId').val("");
        $('#addFieldElement').val("");

        this.renderFields();
    };


    this.renderValueForm=function(newField){
        var popupDIV = document.createElement("DIV");
        popupDIV.id="search_value_popup";
        var popupHD = document.createElement("DIV");
        popupHD.className="hd";
        popupDIV.appendChild(popupHD);
        var popupBD = document.createElement("DIV");
        popupBD.className="bd";

        popupBD.style.overflow="auto";
        popupBD.style.padding="10px";

        popupDIV.appendChild(popupBD);


        var popupFT = document.createElement("DIV");
        popupFT.className="ft";
        popupFT.style.height="20px";
        popupDIV.appendChild(popupFT);

        popupHD.innerHTML="Filtered Column Definition";

        var existingDIV=document.createElement("div");
        existingDIV.style.border="solid thin #DEDEDE";
        existingDIV.style.padding="3px";
        existingDIV.style.overflow="auto";

        popupBD.appendChild(existingDIV);

        //BEGIN current fields section
        var all_fields_table = document.createElement("div");
        all_fields_table.id="filter_value_table";
        all_fields_table.style.marginTop="5pt";

        existingDIV.appendChild(all_fields_table);

        var si_t=document.createElement("table");
        var si_tb=document.createElement("tbody");

        var si_tr=document.createElement("tr");
        var si_th1=document.createElement("td");
        si_th1.colSpan="4";
        si_th1.innerHTML="You've selected a column which requires an additional value (<b>" + newField.header +"</b>).  Please specify a value to use to filter your result.";
        si_tr.appendChild(si_th1);
        si_tb.appendChild(si_tr);

        var si_tr=document.createElement("tr");
        var si_td1=document.createElement("td");
        si_td1.vAlign="top";
        var si_td2=document.createElement("td");
        si_td2.vAlign="top";

        si_td1.innerHTML="Filter Value:";
        this.valueDefinitionInput=si_td2.appendChild(document.createElement("input"));
        this.valueDefinitionInput.type="text";

        si_tr.appendChild(si_td1);
        si_tr.appendChild(si_td2);
        si_tb.appendChild(si_tr);
        si_t.appendChild(si_tb);
        all_fields_table.appendChild(si_t);


        //add to page
        var tp_fm=document.getElementById("tp_fm");
        tp_fm.appendChild(popupDIV);


        this.valuePopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:9999,width:"300px",height:"200px",visible:false,fixedcenter:true,modal:true});

        var myButtons = [ { text:"Submit", handler:handleValueSubmit, isDefault:true },
            { text:"Cancel", handler:handleValueCancel } ];
        this.valuePopup.cfg.queueProperty("buttons", myButtons);

        this.valuePopup.render();


        this.valuePopup.show();

        this.valuePopup.hideEvent.subscribe(function(obj1,obj2,obj3){
            this.valuePopup.destroy();
            YAHOO.util.Event.preventDefault(obj1);
        },this,this);
    }
}

function findFieldByElementAndField( element_name, field, fields){
    var matchedField=null;
    jq(fields).each(function (fI,obj){
        if(obj.elementName===element_name && obj.fieldId===field){
            matchedField= obj;
        }
    });
    return matchedField;
}

function compareFields( a, b ) {
    if ( a.sortOrder < b.sortOrder ){
        return -1;
    }
    if ( a.sortOrder > b.sortOrder ){
        return 1;
    }
    return 0;
}




var handleValueCancel = function() {
    manager.newField=null;
    manager.valuePopup.destroy();
}

var handleValueSubmit = function(obj1, obj2, obj3, obj4) {
    var enteredValue=manager.valueDefinitionInput.value;
    try{
        if(enteredValue=="")
        {
            displayError("Please specify a filter value.");
            return false;
        }

        if(! (new RegExp('^[A-Za-z0-9 _]+$')).test(enteredValue)){
            displayError("Invalid characters specified.");
            return false;
        }

        manager.newField.value=enteredValue;
        manager.newField.fieldId=manager.newField.fieldId+"="+enteredValue,
        manager.addFieldAndUpdate(manager.newField);
        manager.valuePopup.destroy();
        manager.newField=null;
    }catch(e){
        displayError(e.message);
    }
}