/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/customizableSelectBox.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function CustomSelectBox(_input,_settings){
	this.settings=_settings;
	
	if(typeof _input == 'string'){
		this.select = (document.getElementById(_input));
	}else if(typeof _input == 'object'){
		this.select = _input;
	}else{
		return;
	}
	
	if(this.settings.valueField==undefined){
		this.settings.valueField="value";
	}
	
	if(this.settings.displayField==undefined){
		this.settings.displayField=this.settings.valueField;
	}
	
	this.render=function(_default){		
		this.select.manager=this;
		
		this.select.onchange=function(obj){
			if(this.options[this.selectedIndex].text=="View more options..."){
				if(this.manager.settings.all_values==undefined){
					 this.initCallback={
						success:function(obj){
							this.manager.settings.all_values= eval("(" + obj.responseText +")").ResultSet.Result;
							closeModalPanel("values_loading");
							this.populate();
						},
						failure:function(obj){},
                         cache:false, // Turn off caching for IE
						scope:this
					}
					openModalPanel("values_loading","Loading additional values...");
					YAHOO.util.Connect.asyncRequest('GET',this.manager.settings.uri,this.initCallback,null,this);
				}else{						
					this.populate();
				}
			}else if(this.options[this.selectedIndex].text=="Add custom entry..."){
				var creator=new CustomValueCreator({});
				creator.select=this;
				creator.onResponse.subscribe(function(obj1,obj2){
					var new_value=this.new_value;
					if(this.select.manager.settings.custom==undefined)this.select.manager.settings.custom=new Array();
					this.select.manager.settings.custom.push(new_value);
					this.select.populate(null,new_value);
				},creator,true);
				creator.render();
			}
		}
		
		this.select.populate=function(obj,_v){
			while(this.options.length>0){
				this.remove(0);
			}

            var hasDefault = false;

			this.options[0]=new Option("(SELECT)","NULL");
					
			if(this.manager.settings.custom!=undefined){
				for(var tC=0;tC<this.manager.settings.custom.length;tC++){
					var v=this.manager.settings.custom[tC];
					this.options[this.options.length]=new Option(v,v,(v==_v)?true:false,(v==_v)?true:false);
					if(v==_v){
                        hasDefault = true
						this.selectedIndex=(this.options.length-1);
					}
				}
			}
			
			if(this.manager.settings.all_values!=undefined){
				for(var tC=0;tC<this.manager.settings.all_values.length;tC++){
					var v=this.manager.settings.all_values[tC];
					this.options[this.options.length]=new Option(v[this.manager.settings.valueField],v[this.manager.settings.displayField],(v[this.manager.settings.valueField]==_v)?true:false,(v[this.manager.settings.valueField]==_v)?true:false);
					if(v[this.manager.settings.valueField]==_v){
                        hasDefault = true
						this.selectedIndex=(this.options.length-1);
					}
				}
			}else{
				for(var tC=0;tC<this.manager.settings.local_values.length;tC++){
					var v=this.manager.settings.local_values[tC];
					this.options[this.options.length]=new Option(v[this.manager.settings.valueField],v[this.manager.settings.displayField],(v[this.manager.settings.valueField]==_v)?true:false,(v[this.manager.settings.valueField]==_v)?true:false);
					if(v[this.manager.settings.valueField]==_v){
                        hasDefault = true
						this.selectedIndex=(this.options.length-1);
					}
				}
				if(this.manager.settings.uri!=undefined){
					this.options[this.options.length]=new Option("View more options...","");
				}
			}

            if(!hasDefault){
                this.options[this.options.length]=new Option(_v,_v,true,true);
                this.selectedIndex=(this.options.length-1);
            }
			
			this.options[this.options.length]=new Option("Add custom entry...","");
		}
		
		this.select.populate(null,_default);
	}
}


function CustomValueCreator(_options){
  	this.options=_options;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);
  
	this.render=function(){	
		this.panel=new YAHOO.widget.Dialog("valueDialog",{
            close:true,
            width:"350px",
            height:"100px",
            underlay:"shadow",
            modal:true,
            fixedcenter:true,
            visible:false
        });
		this.panel.setHeader("Define New Value");
				
		var bd = document.createElement("form");
					
		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);
		
		//modality
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");
		
		td1.innerHTML="New Value:";
		td1.align="left";
		var input = document.createElement("input");
		input.id="new_value";
		input.name="new_value";
		if(this.options.value!=undefined){
			input.value=this.options.value;
		}
		td2.appendChild(input);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);
		
		this.panel.setBody(bd);
		
		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Select",handler:{fn:function(){
				this.selector.new_value = this.form.new_value.value;
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