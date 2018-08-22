/*
 * web: customizableSelectBox.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
function CustomSelectBox(_input,_settings){

    this.settings = _settings || {};

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

	var menu = this.select;

	this.render=function(_default){

        var selectManager = this;

        menu.onchange=function(obj){
            if (menu.options[menu.selectedIndex].text == "View more options...") {
                if (selectManager.settings.all_values == undefined) {
                    var initCallback = {
                        success: function(obj){
                            var _json = obj.responseText ? JSON.parse(obj.responseText) : obj;
                            selectManager.settings.all_values = _json.ResultSet.Result;
                            closeModalPanel("values_loading");
                            selectManager.populate();
                        },
                        failure: function(obj){},
                        cache: false, // Turn off caching for IE
                        scope: selectManager
                    };
					openModalPanel("values_loading","Loading additional values...");
					XNAT.xhr.get(selectManager.settings.uri, initCallback);
                    // YAHOO.util.Connect.asyncRequest('GET', selectManager.settings.uri, initCallback, null, selectManager);
                }
                else {
					selectManager.populate();
				}
            }
            else if (menu.options[menu.selectedIndex].text == "Add custom entry...") {
				var creator=new CustomValueCreator({});
				creator.select=this;
				creator.onResponse.subscribe(function(obj1,obj2){
					var new_value=this.new_value;
                    if (selectManager.settings.custom == undefined) selectManager.settings.custom = [];
					selectManager.settings.custom.push(new_value);
					selectManager.populate(null,new_value);
				},creator,true);
				creator.render();
			}
		};

		selectManager.populate=function(obj,_v){

		    menu.innerHTML = '';

		    function newOption(value, label, isDefault, selected){
                var option = document.createElement('option');
                // option.value = window.unescapeAllHTML(value||'');
                option.value = value;
                option.innerHTML = window.escapeHTML(window.unescapeAllHTML(label||''));
                option.defaultSelected = isDefault || false;
                option.selected = selected || false;
                return option;
            }

            var hasDefault = false;

			menu.options[0]=new Option("Select...","NULL");

			if(selectManager.settings.custom!=undefined){
				for(var tC=0;tC<selectManager.settings.custom.length;tC++){
					var v=selectManager.settings.custom[tC];

					menu.appendChild(newOption(v, v, v==_v, v==_v));

					// menu.options[menu.options.length]=new Option(v,v,(v==_v)?true:false,(v==_v)?true:false);

                    if(v==_v){
                        hasDefault = true
						menu.selectedIndex=(menu.options.length-1);
					}
				}
			}

			if(selectManager.settings.all_values!=undefined){
				for(var tC=0;tC<selectManager.settings.all_values.length;tC++){
					var v=selectManager.settings.all_values[tC];

                    menu.appendChild(newOption(
                        v[selectManager.settings.valueField],
                        v[selectManager.settings.displayField],
                        v[selectManager.settings.valueField]==_v,
                        v[selectManager.settings.valueField]==_v
                    ));

                    // menu.options[menu.options.length]=new Option(v[selectManager.settings.valueField],v[selectManager.settings.displayField],(v[selectManager.settings.valueField]==_v)?true:false,(v[selectManager.settings.valueField]==_v)?true:false);

                    if(v[selectManager.settings.valueField]==_v){
                        hasDefault = true
						menu.selectedIndex=(menu.options.length-1);
					}
				}
			}else{
				for(var tC=0;tC<selectManager.settings.local_values.length;tC++){
					var v=selectManager.settings.local_values[tC];

                    menu.appendChild(newOption(
                        v[selectManager.settings.valueField],
                        v[selectManager.settings.displayField],
                        v[selectManager.settings.valueField]==_v,
                        v[selectManager.settings.valueField]==_v
                    ));

                    // menu.options[menu.options.length]=new Option(v[selectManager.settings.valueField],v[selectManager.settings.displayField],(v[selectManager.settings.valueField]==_v)?true:false,(v[selectManager.settings.valueField]==_v)?true:false);

                    if(v[selectManager.settings.valueField]==_v){
                        hasDefault = true
						menu.selectedIndex=(menu.options.length-1);
					}
				}
				if(selectManager.settings.uri!=undefined){
					menu.options[menu.options.length]=new Option("View more options...","");
				}
			}

            if(!hasDefault){

			    if (_v) {
                    // menu.options[menu.options.length]=new Option(_v,_v,true,true);
                    menu.appendChild(newOption(_v, _v, true, true));
                    menu.selectedIndex=(menu.options.length-1);
                }
                else {
                    menu.selectedIndex=0;
                }

            }

			menu.options[menu.options.length]=new Option("Add custom entry...","");

		};

		selectManager.populate(null,_default);
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
		var tr=document.createElement("tr");
		var td1=document.createElement("th");
		var td2=document.createElement("td");

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
