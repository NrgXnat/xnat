
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/fileManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 12:04 PM
 */
function fileManager(_resource){
   this.resource=_resource;
   this.collections=new Array();
   
   this.render=function(){
   		if(this.panel!=undefined){
   			this.panel.destroy();
   		}
   	
   	    this.panel=new YAHOO.widget.Dialog("fileListing",{close:true,
		   width:"760px",height:"600px",underlay:"shadow",modal:true,fixedcenter:true,visible:false,draggable:true});
		this.panel.setHeader("File Manager");
				
		var bd = document.createElement("div");
					
		var table = document.createElement("table");
		var thead = document.createElement("thead");
		table.appendChild(thead);
		
		this.tbody = document.createElement("tbody");
		this.tbody.style.overflow="auto";
		this.tbody.style.height="250px";
		table.appendChild(this.tbody);
		bd.appendChild(table);	    
		
		var tr=document.createElement("tr");
		tr.style.height="20px";
		var td=document.createElement("th");
		td.innerHTML="Collection / File";
		td.noWrap="true";
		tr.appendChild(td);
		
		var td=document.createElement("th");
		td.innerHTML="Format";
		tr.appendChild(td);
		
		var td=document.createElement("th");
		td.innerHTML="Content";
		tr.appendChild(td);
		
		var td=document.createElement("th");
		td.innerHTML="Tags";
		tr.appendChild(td);
		
		var td=document.createElement("th");
		td.innerHTML="Size";
		tr.appendChild(td);
		
		thead.appendChild(tr);
		
		
		this.renderFiles();
		
		
		//build upload form
		var uploadForm=document.createElement("form");
		uploadForm.name="fileUploadForm";
		
		this.renderUploadForm(uploadForm);
		
   	    bd.appendChild(document.createElement("br"));
   	    bd.appendChild(document.createElement("br"));
		bd.appendChild(uploadForm);
		
		this.panel.setBody(bd);

		this.panel.selector=this;
		var buttons=[
			{text:"Close",handler:{fn:function(){
				this.hide();
			}}}];
		this.panel.cfg.queueProperty("buttons",buttons);
		
		
		this.panel.render("page_body");
		this.panel.show();   	
   }	
   
   this.renderFiles=function(){
   		emptyChildNodes(this.tbody);
   	
   	    var tr,td,last_c;
		
		for(var fC=0;fC<this.resource.files.length;fC++){
			var file = this.resource.files[fC];
						
			if(file.collection!=last_c){
		      last_c=file.collection;
		      this.collections.push(file.collection);
		      tr=document.createElement("tr");
				tr.style.height="20px";
		      
		      //collection name
		      td=document.createElement("th");
		      if(last_c==""){
		        td.innerHTML="DEFAULT";
		      }else{
		        td.innerHTML=last_c;
		      }
		      tr.appendChild(td);
		      
		      //format
		      td=document.createElement("th");
		      td.innerHTML=file.coll_format;
		      tr.appendChild(td);
		      
		      //content
		      td=document.createElement("th");
		      td.innerHTML=file.coll_content;
		      tr.appendChild(td);
		      
		      //tags
		      td=document.createElement("th");
		      td.innerHTML=file.coll_tags;
		      tr.appendChild(td);
		      
		      //size
		      tr.appendChild(document.createElement("td"));
		      
		      this.tbody.appendChild(tr);
			}
			
		    tr=document.createElement("tr");
			tr.style.height="20px";
		
		
			td= document.createElement("td");
			td.vAlign="middle";
			var dA=document.createElement("a");
			var dIMG=document.createElement("img");
			dIMG.src=serverRoot+"/images/delete.gif";
			dIMG.border=0;
			dA.counter=fC;
			dA.appendChild(dIMG);
			dA.fileManager=this;
			dA.file=file;
			dA.onclick=function(o){
				if(confirm("Are you sure you want to delete " + this.file.Name + "?")){
					this.initCallback={
						success:function(obj1){
				    		closeModalPanel("file");
							this.resource.files.splice(obj1.argument.counter,1);
							
							this.refreshFileCounts();
							this.renderFiles();
						},
						failure:function(o){
				    		closeModalPanel("file");
							displayError("ERROR " + o.status+ ": Failed to delete file.");
						},
						argument:{"counter":this.counter},
                        cache:false, // Turn off caching for IE
						scope:this.fileManager
					}
					
					openModalPanel("file","Deleting file '" + this.file.Name +"'");
					YAHOO.util.Connect.asyncRequest('DELETE',serverRoot + this.file.URI + "?XNAT_CSRF=" + csrfToken,this.initCallback,null,this);
				}
			}
			td.appendChild(dA);
			td.style.width="18px";
			td.align="right";
			tr.appendChild(td);
		
		
		    td=document.createElement("td");
			var dA=document.createElement("a");
			dA.innerHTML=file.Name;
			dA.target="_blank";
			dA.href=serverRoot + file.URI;
			td.appendChild(dA);
		   td.align="left";
		   tr.appendChild(td);
		
		      
		      //format
		      td=document.createElement("td");
		      td.innerHTML=file.file_format;
		      tr.appendChild(td);
		      
		      //content
		      td=document.createElement("td");
		      td.innerHTML=file.file_content;
		      tr.appendChild(td);
		      
		      //tags
		      td=document.createElement("td");
		      td.innerHTML=file.file_tags;
		      tr.appendChild(td);
		      
		      //size
			//size_format - fileCounter.js
		    td=document.createElement("td");
		    td.noWrap="true";
		   td.innerHTML=size_format(parseInt(file.Size));
		   tr.appendChild(td);
		   
		   this.tbody.appendChild(tr);
		}
   }
   
   this.refreshFileCounts=function(){
   		var sum=0;
		for(var fileC=0;fileC<this.resource.files.length;fileC++){
			sum+=parseInt(this.resource.files[fileC].Size);
		}
		this.resource.anchor.innerHTML=this.resource.files.length +" Files, " + size_format(sum);
		
   }
   
   this.refreshFiles=function(){
   	    var countCallback={
			success:function(obj){
			    closeModalPanel("refresh_file");
				this.resource.files=eval("(" + obj.responseText +")").ResultSet.Result;
				this.refreshFileCounts();
				this.render();
			},
			failure:function(obj){
			    closeModalPanel("refresh_file");
			},
			arguments:this.resource,
            cache:false, // Turn off caching for IE
			scope:this
		}
	
		
		openModalPanel("refresh_file","Refreshing file information.");
						
		YAHOO.util.Connect.asyncRequest('GET',this.resource.uri + '?format=json&timestamp=' + (new Date()).getTime(),countCallback,null,this);
   }
   
   this.renderUploadForm=function(div){
   	  div.style.border="1px solid #DEDEDE";
   	  var table,tbody,tr,td,input;
   	  
   	  //var title=document.createElement("div");
   	  //title.style.marginTop="3px";
   	  //title.style.marginLeft="1px";
   	  //title.innerHTML="<font size='+1' style='weight:700'>File Upload Form</font>";
   	  //div.appendChild(title);
   	  
   	  //div.appendChild(document.createElement("br"));
   	  
   	  parent_table=document.createElement("table");
   	  parent_table.width="95%";
   	  parent_tbody=document.createElement("tbody");
   	  parent_table.appendChild(parent_tbody);
   	  div.appendChild(parent_table);
   	  
   	  parent_tr=document.createElement("tr");
   	  parent_tbody.appendChild(parent_tr);
   	  
   	  parent_td=document.createElement("td");
   	  parent_td.vAlign="top";
   	  parent_tr.appendChild(parent_td);
   	  
   	  var collection_form=document.createElement("div");
   	  collection_form.style.border="1px solid #DEDEDE";
   	  collection_form.appendChild(document.createElement("div"));
   	  collection_form.childNodes[0].innerHTML="<strong>Collection Information</strong>";
   	  table=document.createElement("table");
   	  tbody=document.createElement("tbody");
   	  table.appendChild(tbody);
   	  collection_form.appendChild(table);
   	  
   	  
   	  //collection
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Collection";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="collection_name";
   	  if(this.collections.length>0)
   	  	input.value=this.collections[0];
   	  input.manager=this;
   	  input.onchange=function(o){
   	  	this.value=this.value.trim();
   	  	 if(this.manager.collections.contains(this.value)){
   	  	 	document.getElementById("collection_name_note").style.color="#DEDEDE";
   	  	 	document.getElementById("collection_name_note").innerHTML="Existing collection.";
   	  	 	
   	  	 	document.getElementById("collection_format").disabled=true;
   	  	 	document.getElementById("collection_tags").disabled=true;
   	  	 	document.getElementById("collection_content").disabled=true;
   	  	 }else{
   	  	 	document.getElementById("collection_name_note").style.color="green";
   	  	 	document.getElementById("collection_name_note").innerHTML="New collection.";
   	  	 	
   	  	 	document.getElementById("collection_format").disabled=false;
   	  	 	document.getElementById("collection_tags").disabled=false;
   	  	 	document.getElementById("collection_content").disabled=false;
   	  	 }
   	  };
   	  td.appendChild(input);
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  td.id="collection_name_note";
   	  td.style.color="#DEDEDE";
   	  td.innerHTML="Existing collection.";
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //format
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Format";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="collection_format";
   	  input.disabled=true;
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //content
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Content";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="collection_content";
   	  input.disabled=true;
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //tags
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Tags";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="collection_tags";
   	  input.disabled=true;
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  parent_td.appendChild(collection_form);
   	  
   	  
   	  parent_td=document.createElement("td");
   	  parent_td.vAlign="top";
   	  parent_tr.appendChild(parent_td);
   	  
   	  
   	  var file_form=document.createElement("div");
   	  file_form.style.border="1px solid #DEDEDE";
   	  file_form.appendChild(document.createElement("div"));
   	  file_form.childNodes[0].innerHTML="<strong>File Information</strong>";
   	  table=document.createElement("table");
   	  tbody=document.createElement("tbody");
   	  table.appendChild(tbody);
   	  file_form.appendChild(table);
   	  
   	  //collection
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Rename";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.size=50;
   	  input.id="file_name";
   	  input.manager=this;
   	  td.appendChild(input);
   	  tr.appendChild(td);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //format
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Format";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="file_format";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	  
   	  //content
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Content";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="file_content";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  parent_td.appendChild(file_form);
   	  
   	  
   	  //tags
   	  tr=document.createElement("tr");
		tr.style.height="20px";
   	  
   	  td=document.createElement("th");
   	  td.align="left";
   	  td.innerHTML="Tags";
   	  tr.appendChild(td);
   	  
   	  td=document.createElement("td");
   	  input=document.createElement("input");
   	  input.id="file_tags";
   	  td.appendChild(input);
   	  
   	  tr.appendChild(td);
   	  tbody.appendChild(tr);
   	  
   	     	  
   	  parent_tr=document.createElement("tr");
   	  parent_td=document.createElement("td");
   	  parent_td.colSpan="2"
   	  
   	  parent_td.appendChild(document.createTextNode("File To Upload:"));
   	  
   	  var form = document.createElement("form");
   	  form.id="file_upload";
   	  form.name="file_upload";
   	  parent_td.appendChild(form);
   	  
   	  input =document.createElement("input");
   	  input.type="file";
   	  input.id="local_file";
   	  input.name="local_file";
   	  input.size=100;
   	  
   	  form.appendChild(input);
   	  
   	  
   	  //spacer
   	  parent_tbody.appendChild(parent_tr);
   	  parent_tr.appendChild(parent_td);
   	  parent_tr=document.createElement("tr");
   	  parent_td=document.createElement("td");
   	  parent_td.innerHTML="&nbsp;";
   	  parent_td.colSpan="2"
   	  
   	  
   	  parent_tbody.appendChild(parent_tr);
   	  parent_tr.appendChild(parent_td);
   	  
   	  parent_tr=document.createElement("tr");
   	  parent_td=document.createElement("td");
   	  parent_td.colSpan="2"
   	  parent_td.align="right";
   	  
   	  var form=document.createElement("form");
   	  input = document.createElement("input");
   	  input.type="button";
   	  input.value="Upload";
   	  parent_td.appendChild(input);
   	     	  
   	  parent_tbody.appendChild(parent_tr);
   	  parent_tr.appendChild(parent_td);
   	  
  		    var oPushButtonD = new YAHOO.widget.Button(input);
  		    oPushButtonD.subscribe("click",function(o){
  			var collection_name=document.getElementById("collection_name").value.trim();
  			
			var file_tags=document.getElementById("file_tags").value.trim();
			var file_format=document.getElementById("file_format").value.trim();
			var file_content=document.getElementById("file_content").value.trim();
			var file_name=document.getElementById("file_name").value.trim();
			if(file_name[0]=="/"){
				file_name=file_name.substring(1);
			}
			
			var file_params="?file_upload=true&XNAT_CSRF="+csrfToken;
			
			if(file_content!=""){
				file_params+="&content="+file_content;
			}
			if(file_format!=""){
				file_params+="&format="+file_format;
			}
			if(file_tags!=""){
				file_params+="&tags="+file_tags;
			}
  			
  			if(!this.collections.contains(collection_name) && collection_name!=""){
  				var resource_uri=this.resource.uri.substring(0,this.resource.uri.lastIndexOf("/"));
  				var file_dest =  resource_uri+"/resources/"+collection_name + "/";
  				if(file_name!=""){
  					file_dest +=file_name;
  				}else{
  					file_dest +="files";
  				}
  				
  				file_dest+=file_params;
  				//create collection
  				var collection_tags=document.getElementById("collection_tags").value.trim();
  				var collection_format=document.getElementById("collection_format").value.trim();
  				var collection_content=document.getElementById("collection_content").value.trim();
  				
  				var params = "?label="+collection_name + "&XNAT_CSRF=" + csrfToken;
  				if(collection_content!=""){
  					params+="&content="+collection_content;
  				}
  				if(collection_format!=""){
  					params+="&format="+collection_format;
  				}
  				if(collection_tags!=""){
  					params+="&tags="+collection_tags;
  				}
  				
  				this.initCallback={
					success:function(obj1){
			    		closeModalPanel("collection");
						var form = document.getElementById("file_upload");
						YAHOO.util.Connect.setForm(form,true);
						
						var callback={
							upload:function(obj1){
			    				closeModalPanel("file");
			    				this.refreshFiles();
							},
							argument:obj1.argument,
                            cache:false, // Turn off caching for IE
							scope:this
						}
						openModalPanel("file","Uploading File.");
						
						var method = 'POST';
						if(obj1.argument.file_name!=""){
							method='PUT';
						}
						YAHOO.util.Connect.asyncRequest(method,file_dest,callback);
					},
					failure:function(o){
			    		closeModalPanel("collection");
						displayError("ERROR " + o.status+ ": Failed to create collection.");
					},
					argument:{"file_dest":file_dest,"collection":collection_name,"file_name":file_name},
                    cache:false, // Turn off caching for IE
					scope:this
				}
								
				openModalPanel("collection","Initializing collection '" + collection_name +"'");
				YAHOO.util.Connect.asyncRequest('PUT',resource_uri +"/resources/"+collection_name + params,this.initCallback);
  			}else{
  				var file_dest = this.resource.uri;
  				if(collection_name!=""){
  					file_dest=this.resource.uri.substring(0,this.resource.uri.lastIndexOf("/")) +"/resources/"+collection_name + "/files";
  				}
  				if(file_name!=""){
  					file_dest +="/"+ file_name;
  				}
  				
  				file_dest+=file_params;
  				var form = document.getElementById("file_upload");
				YAHOO.util.Connect.setForm(form,true);
				
				var callback={
					upload:function(obj1){
	    				closeModalPanel("file");
	    				this.refreshFiles();
					},
                    cache:false, // Turn off caching for IE
					scope:this
				}
				openModalPanel("file","Uploading File.")
				
				var method = 'POST';
				if(file_name!=""){
					method='PUT';
				}
				
				YAHOO.util.Connect.asyncRequest(method,file_dest,callback);
  			}
  		},this,true);
   }
}