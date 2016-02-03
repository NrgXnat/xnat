/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/restSharer.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function cFormatTextbox(el,oRecord,oColumn,oData){
   	oRecord.setData("new_label",oRecord.getData("primary_label"));
   	var textBoxEl;
    var collection = el.getElementsByTagName("input");

    // Create the form element only once, so we can attach the onChange listener
    if(collection.length === 0) {
        // Create SELECT element
        textBoxEl = document.createElement("input");
        textBoxEl.type="text";
        textBoxEl = el.appendChild(textBoxEl);

        // Add event listener
        YAHOO.util.Event.addListener(textBoxEl,"change",function(e, oSelf) {
    		var elTarget = YAHOO.util.Event.getTarget(e);
    		oSelf.fireEvent("textboxChangeEvent", {event:e, target:elTarget});
		},this);
    }

    textBoxEl = collection[0];

    // Update the form element
    if(textBoxEl) {
    	textBoxEl.value=oRecord.getData("primary_label");
    }
    else {
        el.innerHTML = lang.isValue(oData) ? oData : "";
    }
}

TextDataTable=function(t,fields,dataSrc,options){
	TextDataTable.superclass.constructor.call(this,t,fields,dataSrc,options);
	
	this.textChangeEvent=new YAHOO.util.CustomEvent("textboxChangeEvent",this);
}

YAHOO.extend(TextDataTable, YAHOO.widget.DataTable, {
});

var oRecords = [];
function toggleShareCheckboxes(element) {
    var dialog;
    var checkAll = true;
    element = $(element)[0];
    if(element.id == 'checkAll') {
        element = $('#checkAll');
        dialog = element.parent().parent();
        if(element.val() == 'Check All'){
            checkAll = true;
            element.val('Uncheck All');
        } else {
            checkAll = false;
            element.val('Check All');
        }
    }
    var checkboxes = $('.yui-dt-checkbox', dialog);
    for(var i=0; i<checkboxes.length; i++){
        var checkbox = $(checkboxes[i]);
        if (checkbox.is(':checked')) {
            if(!checkAll){
                checkbox.removeAttr('checked');
            }
        } else {
            if(checkAll){
                checkbox.attr('checked','checked');
            }
        }
    }
    for(var i=0; i<oRecords.length; i++){
        if(!checkAll){
            oRecords[i].setData("checked", false);
        } else {
            oRecords[i].setData("checked", true);
        }
    }
}

RestSharer = function(_array,_config) {
	RestSharer.superclass.constructor.call(this,"rest_share",_config);
	this.a=_array.slice(0).reverse(); //copy array and reverse the copy.
	_config.title="Sharing Manager";
	this.trArray=new Array();
	this.oncomplete=new YAHOO.util.CustomEvent("complete",this);
	
	this.drawContents=function(_div){
		var header=_div.appendChild(document.createElement("div"));
        header.innerHTML='<p style="margin-top:10px;margin-bottom:20px;font-size:14px;">Share the following resources into <b>' + this.config.project.label + '</b></p>' +
            '<input id="checkAll" type="button" value="Uncheck All" onclick="toggleShareCheckboxes(this);" style="width:110px;margin-bottom:10px;font-size:12px;"/>' +
            '<style>div.yui-dt table{ width:100%; }</style>';
        header.style.width = 100+'%';
		var t=_div.appendChild(document.createElement("div"));
		var dataSource = new YAHOO.util.DataSource(this.a);
   		dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
   		dataSource.responseSchema = {
     		fields:["redirect","checked","label","canRead","ru","primary_label","xsiType","date","processed","new_label"]
   		};
   		
   this.dt=new TextDataTable(t,[
   		{key:"check",label:"Share", formatter:function(el, oRecord, oColumn, oData) {
            oRecords.push(oRecord);
			var canRead=oRecord.getData("canRead");
			if(canRead){return YAHOO.widget.DataTable.formatCheckbox(el,oRecord,oColumn,true);}
			else{el.innerHTML="N/A";}
   	    }}
   	    ,{label:"Label",key:"primary_label"}
   	    ,{label:"Data-type",key:"xsiType"}
   	    ,{label:"Date",key:"date"}
   		,{label:"New Label",formatter:cFormatTextbox,key:"new_label"}
   		,{label:"",formatter:function(el,oRecord,oColumn,oData){
   			if(oRecord.getData("processed")==1){
   				el.innerHTML="<img border=\"0\" src=\""+serverRoot +"/images/co.gif\"/>";
   			}else if(oRecord.getData("processed")==2){
   				el.innerHTML="<img border=\"0\" src=\""+serverRoot +"/images/checkmarkGreen.gif\"/>";
   			}else if(oRecord.getData("processed")==3){
   				el.innerHTML="<img border=\"0\" src=\""+serverRoot +"/images/checkmarkRed.gif\"/>";
   			}else{
   				el.innerHTML="";
   			}
   		},key:"processed"}
   		],dataSource,
   		{});
   
  	   this.dt.subscribe("textboxChangeEvent", function(oArgs){   
             var elTextbox = oArgs.target;   
             var oRecord = this.getRecord(elTextbox); 
             oRecord.setData("new_label",elTextbox.value);   
       });
  	   this.dt.subscribe("checkboxClickEvent", function(oArgs){   
             var elCheckbox = oArgs.target;   
             var oRecord = this.getRecord(elCheckbox);   
             oRecord.setData("checked",elCheckbox.checked);   
       });   
  	   
		
	    var myButtons = [ { text:"Share", handler:this.handleShare, isDefault:true  },{ text:"Close", handler:this.handleCancel } ];
		this.popup.cfg.queueProperty("buttons", myButtons);
    }
    
    this.handleCancel=function(){
		this.destroy();
    }
    
    this.handleShare=function(){
    	this.manager.process();
    }
    
    this.process=function(){
    	var processing=false;
    	
    	for(var rsDtC=0;rsDtC<this.dt.getRecordSet().getLength();rsDtC++){
    		var oRecord=this.dt.getRecord(rsDtC);
    		if(oRecord.getData("checked") &&
    		 (oRecord.getData("processed")==null|| oRecord.getData("processed")==undefined)){
    			this.dt.updateCell(oRecord,"processed",1);
    			
    			shareCB={
					success:function(o){
						closeModalPanel("a_share");
						this.dt.updateCell(o.argument.oRecord,"processed",2);
						this.process();
					},
					failure:function(o){
						closeModalPanel("a_share");
						if(o.status!=409){
						  this.stopped=true;
						  this.dt.updateCell(o.argument.oRecord,"processed",3);
                          xModalMessage('Error' + o.status, "ERROR : Failed to share " + oRecord.getData("label"));
						}else{
							this.dt.updateCell(o.argument.oRecord,"processed",3);
						  	//alert("Failed to share " + oRecord.getData("label") + ".  \r\n\r\nThis item has either already been shared into this " + XNAT.app.displayNames.singular.project.toLowerCase() + ", or there is already an item in this " + XNAT.app.displayNames.singular.project.toLowerCase() + " with the requested label.");
						    this.process();
                        }
					},
                    cache:false, // Turn off caching for IE
					scope:this,
				    argument:{"oRecord":oRecord}
				}
    			processing=true;
    			openModalPanel("a_share","Sharing data into " + this.config.project.label);
    			var params="?XNAT_CSRF=" + csrfToken;
    			params+="&event_reason=standard sharing"
    			if(oRecord.getData("new_label")!=""){
    				params+="&label="+oRecord.getData("new_label");
    				if(oRecord.getData("redirect")!=null){
    					this.new_label=oRecord.getData("new_label");
    				}
    			}
    			YAHOO.util.Connect.asyncRequest('PUT',serverRoot + oRecord.getData("ru") + "/projects/"+ this.config.project.id+ params,shareCB,null,this);
    			break;
    		}
    	}
    	
    	if(!processing){
    		this.oncomplete.fire(this.new_label);
//            window.location.reload();
    		this.popup.destroy();
    	}
    }
};

YAHOO.extend(RestSharer, BasePopup, {
});