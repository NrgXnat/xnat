/*
 * web: recon_tools.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
dynamicJSLoad("SAXDriver","lib/xmlsax-min.js");
dynamicJSLoad("SAXEventHandler","lib/SAXEventHandler-min.js");

function reconEditor(_sessionID,_reconID,_options){
	this.sessionID=_sessionID;
  	this.reconID=_reconID;
  	this.options=_options;
  	
  	this.onModification=new YAHOO.util.CustomEvent("modification",this);
  	
  	this.init=function(){
  		if(this.reconID!=undefined){
			//load from search xml from server
			this.initCallback={
				success:this.completeInit,
				failure:this.initFailure,
                cache:false, // Turn off caching for IE
				scope:this
			}
			
			openModalPanel("load_recon","Loading Reconstruction Details.");
			
			YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/experiments/' + this.sessionID +'/reconstructions/' + this.reconID + '?format=xml',this.initCallback,null,this);
  		}else{
			this.classMapping=new ClassMapping();
			var fn = this.classMapping.newInstance;
			this.recon=fn("xnat:reconstructedImageData");
			this.render();
  		}
  	}
	
	this.initFailure=function(o){
        if (!window.leaving) {
            closeModalPanel("load_recon");
            this.displayError("ERROR " + o.status+ ": Failed to load reconstruction list.");
        }
	};
	
	this.completeInit=function(o){
		try{
			closeModalPanel("load_recon");
			var xmlText =o.responseText;
            
            parser = new SAXDriver();
			var handler = new SAXEventHandler();
			
			parser.setDocumentHandler(handler);
			parser.setErrorHandler(handler);
			parser.setLexicalHandler(handler);
			
			parser.parse(xmlText);// start parsing                        
						
			if (handler.root){
				this.recon=handler.root;				
			}else{
                xmodal.message('Reconstructed Image Error', "ERROR: Unable to retrieve recon data.");
			}
			
			if(this.options.button)this.options.button.disabled=false;
		}catch(e){
			this.displayError("ERROR " + o.status+ ": Failed to parse recon.");
		}
			
		this.render();
		
	};
	
	this.displayError=function(errorMsg){
		xmodal.message('Error', errorMsg);
	};
	
	this.render=function(){
		if(this.recon){						
			this.panel=new YAHOO.widget.Dialog("reconDialog",{close:true,
			   width:"390px",height:"300px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
			if(this.reconID==undefined)
				this.panel.setHeader("New Reconstruction Details");
			else
				this.panel.setHeader(this.reconID +" Details");
			
			var bd = document.createElement("form");
			
			var table = document.createElement("table");
			var tb = document.createElement("tbody");
			table.appendChild(tb);
			bd.appendChild(table);
			
			//id
			var tr=document.createElement("tr");
			var td1=document.createElement("th");
			var td2=document.createElement("td");
			
			td1.innerHTML="ID:";
			td1.align="left";			
			if(this.recon.XnatReconstructedimagedataId){
				td2.innerHTML="<input type='hidden' name='xnat:reconstructedImageData/ID' value='" + this.recon.getProperty("ID") + "'/>"+this.recon.getProperty("ID");
			}else{
				td2.innerHTML="<input type='text' name='xnat:reconstructedImageData/ID' value=''/>";
			}
			tr.appendChild(td1);
			tr.appendChild(td2);
			tb.appendChild(tr);	
						
			if(this.recon.XnatReconstructedimagedataId){
				this.panel.method='PUT';
				this.panel.action=serverRoot +'/REST/experiments/' + this.sessionID +'/reconstructions/' + this.reconID + '?req_format=form&XNAT_CSRF='+csrfToken;
				td1.innerHTML+="<input type='hidden' name='xnat:reconstructedImageData/xnat_reconstructedimagedata_id' value='" + this.recon.XnatReconstructedimagedataId + "'/>";
			}else{
				this.panel.method='POST';
				this.panel.action=serverRoot +'/REST/experiments/' + this.sessionID +'/reconstructions?req_format=form&XNAT_CSRF='+csrfToken;
			}
			
			//type
			tr=document.createElement("tr");
			td1=document.createElement("th");
			td2=document.createElement("td");
			
			td1.innerHTML="Type:";			
			td1.align="left";		
			
			var type_container=document.createElement('div');
			td2.appendChild(type_container);
			
			var type_input = document.createElement('input');
			type_input.type='text';
			type_input.id='type';
			type_input.size='20';
			type_input.style.width="180px";
			type_input.name="xnat:reconstructedImageData/type";
			if(this.recon.Type){
				type_input.value=this.recon.Type;
			}
			type_container.appendChild(type_input);
			
			tr.appendChild(td1);
			tr.appendChild(td2);
			tb.appendChild(tr);
		     
				
			//quality
			tr=document.createElement("tr");
			td1=document.createElement("th");
			td2=document.createElement("td");
			
			td1.innerHTML="Base Scan Type:";
			td1.align="left";
			var type_input = document.createElement('input');
			type_input.type='text';
			type_input.id='baseScanType';
			type_input.size='20';
			type_input.style.width="180px";
			type_input.name="xnat:reconstructedImageData/baseScanType";
			if(this.recon.Basescantype){
				type_input.value=this.recon.Basescantype;
			}
			td2.appendChild(type_input);
			tr.appendChild(td1);
			tr.appendChild(td2);
			tb.appendChild(tr);
			
			this.panel.setBody(bd);
			
			this.panel.form=bd;
			this.panel.manager=this;
			
			var buttons=[{text:"Save",handler:{fn:function(){
				var params = parseForm(this.form);
				openModalPanel("save_recon","Saving Reconstruction.");
				YAHOO.util.Connect.asyncRequest(this.method,this.action,{success:function(){
					closeModalPanel("save_recon");
					this.manager.onModification.fire();
					this.cancel();
				},failure:function(){
                    if (!window.leaving) {
                        closeModalPanel("save_recon");
                        xmodal.message('Reconstructed Image Error', "Save reconstructed image data failed.");
                    }
					this.cancel();
				}, cache:false, scope:this},params);
			}},isDefault:true},
			{text:"Cancel",handler:{fn:function(){
				this.cancel();
			}}}];
			this.panel.cfg.queueProperty("buttons",buttons);
			
			
			this.panel.render("page_body");
						
			this.panel.show();
		}
	}
	
}

function loadrecons(session_id,project,tbody_id){
	this.initCallback={
		success:this.completereconLoad,
		failure:function(o){
            if (!window.leaving) {
                closeModalPanel("recon_summary");
                this.displayError("ERROR " + o.status+ ": Failed to load recon list.");
            }
		},
        cache:false, // Turn off caching for IE
		arguments:{"session_id":session_id,"project":project,"tbody_id":tbody_id}
	}
	openModalPanel("recon_summary","Loading reconstruction summary.");
	
	YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/experiments/' + session_id +'/reconstructions?format=json',this.initCallback,null,this);
}

function completereconLoad(obj1){
	closeModalPanel("recon_summary");
	var recons= eval("(" + obj1.responseText +")").ResultSet.Result;
	renderRecons(recons,this.arguments.tbody_id,this.arguments.session_id,this.arguments.project);
}


function reconDeleteDialog(_options){
  this.onResponse=new YAHOO.util.CustomEvent("response",this);
  
	this.render=function(){	
		this.panel=new YAHOO.widget.Dialog("reconDeletionDialog",{close:true,
		   width:"400px",height:"100px",underlay:"shadow",modal:true,fixedcenter:true,visible:false});
		this.panel.setHeader("Reconstruction Deletion Dialog");
				
		var bd = document.createElement("form");
					
		var table = document.createElement("table");
		var tb = document.createElement("tbody");
		table.appendChild(tb);
		bd.appendChild(table);	    
		
		//modality
		tr=document.createElement("tr");
		td1=document.createElement("th");
		td2=document.createElement("td");
		
		td1.innerHTML="Delete associated files from the repository?:";
		td1.align="left";
		var sel = document.createElement("input");
		sel.type="checkbox";
		sel.checked=true;
		sel.defaultChecked=true;
		sel.name="delete_files";
		td2.appendChild(sel);
		tr.appendChild(td1);
		tr.appendChild(td2);
		tb.appendChild(tr);
		
		this.panel.setBody(bd);
		
		this.panel.form=bd;

		this.panel.selector=this;
		var buttons=[{text:"Delete",handler:{fn:function(){
				this.selector.delete_files = this.form.delete_files.checked;
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

function reconDeletor(_options){
	this.options=_options;
	
	this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
	
	this.execute=function(){
		this.deleteDialog=new reconDeleteDialog();
		this.deleteDialog.onResponse.subscribe(function(){
			var delete_files=this.deleteDialog.delete_files;
			
			this.initCallback={
				success:function(obj1){
					closeModalPanel("delete_recon");
					loadrecons(this.arguments.session_id,this.arguments.project,this.arguments.tbody_id);
				},
				failure:function(o){
                    if (!window.leaving) {
                        closeModalPanel("delete_recon");
                        this.displayError("ERROR " + o.status+ ": Failed to load recon list.");
                    }
				},
                cache:false, // Turn off caching for IE
				arguments:this.options
			}
			
			openModalPanel("delete_recon","Delete reconstruction.");
			YAHOO.util.Connect.asyncRequest('DELETE',serverRoot +'/REST/experiments/' + this.options.session_id +'/reconstructions/' + this.options.recon.id +'?format=json&XNAT_CSRF=' + csrfToken,this.initCallback,null,this);
		},this,this);
		
		this.deleteDialog.render();
	}
}

function renderRecons(recons,tbody_id,session_id,project){
	var tbody=document.getElementById(tbody_id);
	
	//clear contents - xdat.js
	emptyChildNodes(tbody);
		
	for(var reconC=0;reconC<recons.length;reconC++){
		var recon = recons[reconC];	
		
		var tr = document.createElement("tr");
				
		td= document.createElement("td");
		td.vAlign="middle";
		var eA=document.createElement("a");
		var eIMG=document.createElement("img");
		eIMG.src=serverRoot+"/images/e.gif";
		eIMG.border=0;
		eA.appendChild(eIMG);
		eA.options={"recon":recon,"session_id":session_id,"project":project,"tbody_id":tbody_id};
		eA.onclick=function(o){
		    window.reconEditor=new reconEditor(this.options.session_id,this.options.recon.id,{project:this.options.project,tbody_id:this.options.tbody_id});
    		window.reconEditor.onModification.subscribe(function(o){
    			loadrecons(this.sessionID,this.options.project,this.options.tbody_id);
    		},this);
    		window.reconEditor.init();
		}
		td.appendChild(eA);
		td.style.width="18px";
		tr.appendChild(td);
		
		td= document.createElement("td");
		td.vAlign="middle";
		var dA=document.createElement("a");
		var dIMG=document.createElement("img");
		dIMG.src=serverRoot+"/images/delete.gif";
		dIMG.border=0;
		dA.appendChild(dIMG);
		dA.options={"recon":recon,"session_id":session_id,"project":project,"tbody_id":tbody_id};
		dA.onclick=function(o){
		    var deletion=new reconDeletor(this.options);
    		deletion.onCompletion.subscribe(function(o){
    			loadrecons(this.sessionID,this.options.project,this.options.tbody_id);
    		},this);
    		deletion.execute();
		}
		td.appendChild(dA);
		td.style.width="18px";
		tr.appendChild(td);
		
		//id
		td= document.createElement("td");
		td.innerHTML=recon.id ;
		tr.appendChild(td);	
		
		//type
		td= document.createElement("td");
		if(recon.type){
			td.innerHTML=recon.type;
		}
		tr.appendChild(td);	
		
		//base
		td= document.createElement("td");
		if(recon.basescantype){
			td.innerHTML=recon.basescantype;
		}
		tr.appendChild(td);
		
		if(window.fileCounter!=undefined){
			//file_count
			td= document.createElement("td");
			td.innerHTML="Loading...";
			tr.appendChild(td);
			window.fileCounter.collection.push({"uri":serverRoot + "/REST/experiments/" + session_id + "/reconstructions/" + recon.id + "/files","div":td});
		}
		
		tbody.appendChild(tr);
	}
	
	window.fileCounter.execute();
}

function showCounts(){
	
}
