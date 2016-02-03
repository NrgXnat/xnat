/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/saveSearch.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 9:54 AM
 */
function SavePopupForm(_search,_div,_config){
	this._div=_div;
	this.xml=_search;
	if(_config==undefined){
		this.config=new Object();
	}else{
		this.config=_config;
	}
	
	this.init=function(){
		if(this.xml!=undefined){
			var arr,src='',parser = new SAXDriver();
			var handler = new SAXEventHandler();
			parser.setDocumentHandler(handler);
		 	parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);
			parser.parse(this.xml);// start parsing                        
			this.searchDOM=handler.root;
		
			var popupDIV = document.createElement("DIV");
			popupDIV.id="all_users_popup";
			var popupHD = document.createElement("DIV");
			popupHD.className="hd";
			popupDIV.appendChild(popupHD);
			var popupBD = document.createElement("DIV");
			popupBD.className="bd";
			popupDIV.appendChild(popupBD);
			
			popupHD.innerHTML="Save Search";
			
			var saveOptions = new Object();
			saveOptions.zIndex=999;
			saveOptions.width="320";
			saveOptions.x=240;
			saveOptions.visible=true;
            saveOptions.fixedCenter=true;
			
			if(this._div!=undefined){
				//add to page
				if(this._div.id==undefined){
					var tp_fm=document.getElementById(this._div);
				}else{
					var tp_fm=this._div;
				}
				tp_fm.appendChild(popupDIV);
			}else{
				var tp_fm = document.getElementById("tp_fm");
				tp_fm.appendChild(popupDIV);
				saveOptions.modal=true;
			}
			
			this.savePopupDialog=new YAHOO.widget.Dialog(popupDIV,saveOptions);
			
			var handleCancel = function() {
				this.destroy();
			}
			    
		    this.emailCallback={
				success:this.completeSave,
				failure:this.saveFailure,
                cache:false, // Turn off caching for IE
				scope:this
	        };
	        
	        this.savePopupDialog.saver=this;
			
			var handleSubmit = function() {
				var _id = document.getElementById("save_id").value;
			    var _briefDesc = document.getElementById("save_brief").value;
			    var _desc = document.getElementById("save_desc").value;	
			    var preventComments=false;
			    
			    if(_id==""){
                    xModalMessage('Search Validation', "Please specify an ID for this search.");
			    	return;
			    }else{
			    	if(this.saver.searchDOM.getId()==null || this.saver.searchDOM.getId()=="")
			    	{
			    		this.saver.searchDOM.setId(_id.trim());
			    	}else{
			    		if(this.saver.searchDOM.getId()!=_id.trim()){
			    			this.saver.searchDOM.setId(_id.trim());
			    			preventComments=true;
			    		}
			    	}
			    }
			    
			    if(_briefDesc!=""){
			    	this.saver.searchDOM.setBriefDescription(_briefDesc);
			    }else{
                    xModalMessage('Search Validation', "Please specify a brief description for this search.");
			    	return;
			    }
			    
			    if(_desc!=""){
			    	this.saver.searchDOM.setDescription(_desc);
			    }
		        
			    var params="?XNAT_CSRF="+csrfToken;
			    if(this.saver.config.saveAs){
			    	preventComments=true;
			    	params+="&saveAs=true";
			    }
		        
		        this.saver.savePopupDialog.saveMsgTab.innerHTML="<DIV style='color:red'>Saving...</DIV>";
		        
		        YAHOO.util.Connect.asyncRequest('PUT',serverRoot +'/REST/search/saved/' + _id + params,this.saver.emailCallback,this.saver.searchDOM.toXML("",preventComments),this.saver);
			}
			
		    var myButtons = [ { text:"Submit", handler:handleSubmit, isDefault:true },
							  { text:"Cancel", handler:handleCancel } ];
			this.savePopupDialog.cfg.queueProperty("buttons", myButtons);
					
			this.savePopupDialog.saveMsgTab=document.createElement("DIV");
			this.savePopupDialog.saveMsgTab.style.display='block';
			this.savePopupDialog.saveMsgTab.id='saveMsgTab';
			popupBD.appendChild(this.savePopupDialog.saveMsgTab);
			
			var table = document.createElement("TABLE");
			var tbody = document.createElement("TBODY");
			table.appendChild(tbody);
			
			var tr;
			var td;
			var th;
			var hr;
			
			var div1=document.createElement("DIV");
			div1.style.display='block';
			if(this.searchDOM.getTag()){
				if(this.config.saveAs){
					div1.innerHTML='Saving this search will create a new listing (tab), in addition to the original.';
				}else{
					div1.innerHTML='This will modify the existing tab in the listings.  To create a new tab, use the Save As New Search option.';
				}				
			}else{
				div1.innerHTML='Saving this search will allow you to execute it again in the future.';
			}
			
			popupBD.appendChild(div1);
		
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.border="0";
			hr = document.createElement("HR");
			hr.color="#DEDEDE";
			td.appendChild(hr);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			th= document.createElement("TD");
			th.align="left";
			th.colspan="2";
			th.border="0";
			th.innerHTML="Brief Description:";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.border="0";
			var input = document.createElement("INPUT");
			input.type="text";
			input.id="save_brief";
			if(this.searchDOM.getBriefDescription()){
				input.value=this.searchDOM.getBriefDescription();
				if(!(this.config.saveAs)){
					input.disabled=true;
				}
			}
			input.name="save_brief";
			input.size="42";
            input.maxLength="100";
			this.applyInputStyle(input);
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
		
			
			
			var input = document.createElement("INPUT");
			input.type="hidden";
			input.id="save_id";
			input.name="save_id";
			if(this.searchDOM.getId() && !this.searchDOM.getId().startsWith("@")){
				input.value=this.searchDOM.getId();
			}else{
				input.value="xs"+ (new Date()).getTime();
			}
			td.appendChild(input);
		
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.border="0";
			td.colspan="2";
			hr = document.createElement("HR");
			hr.color="#DEDEDE";
			td.appendChild(hr);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			th= document.createElement("TD");
			th.border="0";
			th.align="left";
			th.colspan="2";
			th.innerHTML="Full Description:";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.border="0";
			var input = document.createElement("textarea");
			input.id="save_desc";
			input.name="save_desc";
			input.rows="10";
			input.cols="31";
            input.maxLength="255";
			this.applyInputStyle(input);
			if(this.searchDOM.getDescription()){
				input.value=this.searchDOM.getDescription();
			}
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
						
			
			popupBD.appendChild(table);
		}
	}
	
	this.saveFailure=function(o){
		if(o.status==403){
            xModalMessage('Search Validation', "Your account does not have permission to save modifications of this search.");
			this.savePopupDialog.saveMsgTab.innerHTML="<DIV style='color:red'>Error. Invalid permissions.</DIV>";	
		}else{
			this.savePopupDialog.saveMsgTab.innerHTML="<DIV style='color:red'>Error " + o.status + ". Failed to save search.</DIV>";	
		}
	};
	
	this.onSavedSearch=new YAHOO.util.CustomEvent("saved-search",this);
	
	this.completeSave=function(o){
		this.savePopupDialog.destroy();
		this.onSavedSearch.fire();
	};
	
	this.render=function(){		
		this.savePopupDialog.render();
	}
	
	this.applyInputStyle = function(e){
		e.style.fontSize = "99%";
	}
}