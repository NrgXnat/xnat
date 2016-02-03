/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/justification/justification.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */



XNAT.app.ConfirmWJustification=function(_yuioptions){
	this.yuioptions=_yuioptions;
	this.onResponse=new YAHOO.util.CustomEvent("response",this);

	if(this.yuioptions.width==undefined){
		this.yuioptions.width="400px";
	}	
	
	if(this.yuioptions.height==undefined){
		this.yuioptions.height="150px";
	}	
	
	if(this.yuioptions.close==undefined){
		this.yuioptions.close=true;
	}	
	this.yuioptions.underlay="shadow";
	this.yuioptions.modal=true;
	this.yuioptions.fixedcenter=true;
	this.yuioptions.visible=false;
	
	this.render=function(){
		this.panel=new YAHOO.widget.Dialog("justificationDialog",this.yuioptions);
		this.panel.setHeader(this.yuioptions.header);

		var bd = document.createElement("form");

		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);
		
		//message (optional)
		if(this.yuioptions.message!=undefined){
			tr=document.createElement("tr");
			td1=document.createElement("td");
			td1.colSpan="2";
			td1.innerHTML=this.yuioptions.message;
			
			tr.appendChild(td1);
			tb.appendChild(tr);
		}

		//justification
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");		

		td1.innerHTML="Justification:";
		td1.align="left";
		var sel = document.createElement("textarea");
		sel.cols="24";
		sel.rows="4";
		sel.id="event_reason";
		sel.name="event_reason";
		td2.appendChild(sel);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);
		
		//message (optional)
		if(this.yuioptions.note!=undefined){
			tr=document.createElement("tr");
			td1=document.createElement("td");
			td1.colSpan="2";
			td1.innerHTML=this.yuioptions.note;
			
			tr.appendChild(td1);
			tb.appendChild(tr);
		}

		this.panel.setBody(bd);

		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Confirm",handler:{fn:function(){
				this.selector.event_reason = this.form.event_reason.value;
				if(this.selector.event_reason==""){
                    xModalMessage('Project Validation', 'Please enter a justification.');
					return;
				}
				this.cancel();
				this.selector.onResponse.fire();
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);


		this.panel.render("page_body");
		this.panel.show();
	}
}

XNAT.app.passThrough=function(_function,scope){
	this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
	this.onCompletion.subscribe(_function, this, scope);
	
	this.fire=function(){
		this.onCompletion.fire();
	}
}

XNAT.app.requestJustification=function(_id,_header,_function,scope,yuioptions){
	this.id=_id;
	this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
	
	if(yuioptions==undefined){
		this.options=new Object();
	}else{
		this.options=yuioptions;
	}

	this.options.header=_header;
	
	this.dialog=new XNAT.app.ConfirmWJustification(this.options);
	this.dialog.id=_id;
	this.dialog.header=_header;
	this.dialog.onResponse.subscribe(_function,this,scope);

	this.dialog.render();
}