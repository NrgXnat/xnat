/*
 * web: ConfigureSearchFields.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2022, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * @author Tim Olsen
 *
 * Javascript object for usage in ConfigureSearchFields.vm
 */
function ConfigureSearchFields(selectId) {
    this.url = serverRoot + "/REST/search/elements?format=json";
    this.selectId = $(selectId);

    this.init = function () {
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
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    };

    this.loadFields = function(){
        var elementName = jq("#selectElements").val();
        var listOfFields="";
        jq.get(serverRoot + "/REST/search/elements/"+elementName + "?showAll=true&showGenerated=false&format=json")
            .done(function (data) {
                manager.currentFields=data.ResultSet.Result;

                jq(data.ResultSet.Result).each(function (index, obj){
                    listOfFields+="<TR><TD style='font-size: 11px;'>" + obj.FIELD_ID + "<input type='hidden' id='element" + index +"' value='" + obj.ELEMENT_NAME + "'/><input type='hidden' id='field" + index +"' value='" + obj.FIELD_ID + "'/></TD><TD><input type='text' id='header" + index +"' value='" + obj.HEADER + "' style='width:150px'/></TD><TD><input type='text' id='desc" + index +"' value='" + obj.DESC + "' style='width:150px'/></TD><TD><input type='checkbox' id='searchable" + index +"' " + ((obj.SEARCHABLE==='true')?" CHECKED ":"") + " /></TD><TD NOWRAP><input type='button' onclick='manager.update(" + index +");' value='Update'/> " + ((obj.REQUIRES_VALUE==="true")?"<input type='button' value='Add Value' onclick='manager.addSqlQueryValue(" + index +");'/>":"") + "</TD></TR>";
                });
                jq("#listOfFields").html(listOfFields);
            })
            .fail(function (xhr, status, error) {
                showMessage("page_body", "Error", "Failed to load setting into control " + that.id + ": [" + xhr.status + "] " + error);
            });
    }

    this.update = function ( row) {
        var element = jq("#element"+row).val();
        var field = jq("#field"+row).val();
        var header = jq("#header"+row).val();
        var desc = jq("#desc"+row).val();
        var searchable = jq("#searchable"+row).prop("checked");

        if(! (new RegExp('^[A-Za-z0-9 _()/-/./\\//]+$')).test(header)){
            displayError("Invalid characters specified.");
            return false;
        }
        if(! (new RegExp('^[A-Za-z0-9 _()/-/./\\//]+$')).test(desc)){
            displayError("Invalid characters specified.");
            return false;
        }

         openModalPanel("updatingElement","Updating");

        var that = this;
        var value = jq(this.id).val();
            jq.ajax({
                url: serverRoot + "/REST/search/elements/" + element + "/fields?fieldId=" + field + "&searchable=" + searchable + "&header=" + header + "&description=" + desc + "&XNAT_CSRF=" + window.csrfToken,
                type: 'PUT',
                data: "",
                contentType: "text/plain"
            })
            .done(function () {
                closeModalPanel("updatingElement");
            })
            .fail(function (xhr, status, error) {
                closeModalPanel("updatingElement");
                showMessage("page_body", "Failure", "Failed to modify field " + field + ": [" + xhr.status + "] " + error);
            });
    };

    this.addField = function () {
        var newField = manager.newField;

        openModalPanel("updatingElement","Updating");

        var that = this;
        var value = jq(this.id).val();
        jq.ajax({
            url: serverRoot + "/REST/search/elements/" + newField.ELEMENT_NAME + "/fields?addSqlQueryValue=true&fieldId=" + newField.FIELD_ID + "&header=" + newField.HEADER + "&value=" + newField.value + "&XNAT_CSRF=" + window.csrfToken,
            type: 'PUT',
            data: "",
            contentType: "text/plain"
        })
            .done(function () {
                closeModalPanel("updatingElement");
                manager.loadFields();
            })
            .fail(function (xhr, status, error) {
                closeModalPanel("updatingElement");
                showMessage("page_body", "Failure", "Failed to modify field " + field + ": [" + xhr.status + "] " + error);
            });
    };

    this.addSqlQueryValue=function(index) {
        this.newField=this.currentFields[index];
        this.renderValueForm(this.newField);
    }

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
        si_th1.innerHTML="You've selected a column which requires an additional value (<b>" + newField.HEADER +"</b>).  Please specify a value to use to filter your result.";
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

        var myButtons = [ { text:"Submit", handler:handleValueSubmit, isDefault:true },{ text:"Cancel", handler:handleValueCancel } ];
        this.valuePopup.cfg.queueProperty("buttons", myButtons);

        this.valuePopup.render();


        this.valuePopup.show();

        this.valuePopup.hideEvent.subscribe(function(obj1,obj2,obj3){
            this.valuePopup.destroy();
            YAHOO.util.Event.preventDefault(obj1);
        },this,this);
    }
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
        manager.newField.fieldId=manager.newField.fieldId+"="+enteredValue;
        manager.addField();
        manager.valuePopup.destroy();
        manager.newField=null;
    }catch(e){
        displayError(e.message);
    }
}
