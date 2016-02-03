/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/fileCounter.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function fileCounter(_options) {
	this.running=false;
	
	this.collection = [];
	
	this.execute=function(){
		if(!this.running){
			this.running=true;
			this.start();
		}
	}
	
	this.start=function(){
		if(this.collection.length>0){
			this.openItem=null;
			
			for(var collC=0;collC<this.collection.length;collC++){
				if(this.collection[collC].files==undefined){
					this.openItem=this.collection[collC];
					break;
				}
			}
			
			if(this.openItem!=null){
				this.openItem.destination=this.openItem.div;
				if(typeof this.openItem.destination =='string'){
					this.openItem.destination=document.getElementById(this.openItem.destination);
				}
				
				this.openItem.destination.style.fontWeight="700";
				this.openItem.destination.innerHTML="Loading...";
				
				var countCallback={
					success:this.processResults,
					failure:this.handleFailure,
					arguments:this.openItem,
                    cache:false, // Turn off caching for IE
					scope:this
				}
			
				YAHOO.util.Connect.asyncRequest('GET',this.openItem.uri + '?format=json&timestamp=' + (new Date()).getTime(),countCallback,null,this);
			}else{
				this.running=false;
			}
		}
	}	
	
	this.processResults=function(o){
		try{
			this.openItem.destination.style.fontWeight="500";
			this.openItem.files= eval("(" + o.responseText +")").ResultSet.Result;
			
			this.openItem.destination.innerHTML="";
			
			var sum=0;
			for(var fileC=0;fileC<this.openItem.files.length;fileC++){
				sum+=parseInt(this.openItem.files[fileC].Size);
			}
			
			this.openItem.anchor = document.createElement("a");
			this.openItem.anchor.item=this.openItem;
						
			if(this.openItem.allowManager){
				this.openItem.anchor.onclick=function(o){
					var fm =new fileManager(this.item);
					fm.render();
				}
			}else{
				if(this.openItem.fileExpand){
					this.openItem.anchor.onclick=function(o){
						var list_div=document.getElementById(this.item.fileExpand);
						list_div.style.display="block";
					}
				}
			}
			
			this.openItem.anchor.innerHTML=this.openItem.files.length +" Files, " + size_format(sum);
			
			this.openItem.destination.appendChild(this.openItem.anchor);
			
			if(this.openItem.list_div){
   	    		var tr,td,last_c;
				var list_div=document.getElementById(this.openItem.list_div);
				for(var fC=0;fC<this.openItem.files.length;fC++){
					var file = this.openItem.files[fC];
						
					if(file.collection!=last_c){
				      last_c=file.collection;
				      tr=document.createElement("tr");
						tr.style.height="20px";
				      
				      //collection name
				      td=document.createElement("th");
				      if(last_c==""){
				        td.innerHTML="";
				      }else{
				        td.innerHTML=last_c;
				      }
				      tr.appendChild(td);
				      
				      //format
				      td=document.createElement("th");
				      td.align="left";
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
				      
				      list_div.appendChild(tr);
					}
					
				    tr=document.createElement("tr");
					tr.style.height="20px";				
				
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
				   
				   list_div.appendChild(tr);
				}
			}
		}catch(e){
			this.openItem.destination.style.color="red";
		}
		this.start();
	}

    this.handleFailure = function (o) {
        if (!window.leaving) {
            this.openItem.destination.style.color = "red";
            this.openItem.destination.innerHTML = "ERROR";
            this.openItem.files = [];
            this.start();
        }
    };

    function number_format( number, decimals, dec_point, thousands_sep ) {
      	var n = number, c = isNaN(decimals = Math.abs(decimals)) ? 2 : decimals;
  		var d = dec_point == undefined ? "," : dec_point;
  		var t = thousands_sep == undefined ? "." : thousands_sep, s = n < 0 ? "-" : "";
  		var i = parseInt(n = Math.abs(+n || 0).toFixed(c)) + "", j = (j = i.length) > 3 ? j % 3 : 0;
  		return s + (j ? i.substr(0, j) + t : "") + i.substr(j).replace(/(\d{3})(?=\d)/g, "$1" + t) + (c ? d + Math.abs(n - i).toFixed(c).slice(2) : "");
    }

    function size_format(filesize) {
        if (filesize >= 1073741824) {
            filesize = number_format(filesize / 1073741824, 2, '.', '') + ' GB';
        } else {
            if (filesize >= 1048576) {
                filesize = number_format(filesize / 1048576, 2, '.', '') + ' MB';
            } else {
                if (filesize >= 1024) {
                    filesize = number_format(filesize / 1024, 0) + ' KB';
                } else {
                    filesize = number_format(filesize, 0) + ' bytes';
                }
            }
        }
        return filesize;
    }
}
