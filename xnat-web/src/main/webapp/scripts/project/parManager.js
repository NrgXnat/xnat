
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/parManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/29/13 4:03 PM
 */
function PARManager(_div,_obj){
	if(_div.id==undefined){
		this.div=document.getElementById(_div);
	}else{
		this.div=div_id;
	}
	this.obj=_obj;
	
	this.init=function(){
		//load from search xml from server
		this.initCallback={
			success:this.completeInit,
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
				
		YAHOO.util.Connect.asyncRequest('GET',this.obj.URI +'?format=json&stamp='+ (new Date()).getTime(),this.initCallback,null,this);
	}
	
	
	
	this.initFailure=function(o){
        if (!window.leaving) {
            closeModalPanel("par");
            displayError("ERROR " + o.status+ ": Failed to load par list." + e.toString());
        }
	};
	
	this.completeInit=function(o){
		this.pars=null;
		try{
		    this.pars= (eval("(" + o.responseText +")")).ResultSet.Result;
		}catch(e){
			displayError("ERROR " + o.status+ ": Failed to parse par list." + e.toString());
		}
		try{
		    this.render();
		}catch(e){
			displayError("ERROR : Failed to render par list." + e.toString());
			throw(e);
		}
	};
	
	this.accept=function(par_id){
		var callback={
			success:function(o){
				closeModalPanel("par");
				this.init();
				window.projList.init();
			},			
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
			
		openModalPanel("par","Accepting invitation...");
		
		YAHOO.util.Connect.asyncRequest('PUT',serverRoot +'/REST/pars/' + par_id + '?accept=true&format=json&XNAT_CSRF='+csrfToken,callback,null,this);
	}
	
	this.decline=function(par_id,msg){
		var callback={
			success:function(o){
				closeModalPanel("par");
				this.init();
			},			
			failure:this.initFailure,
            cache:false, // Turn off caching for IE
			scope:this
		}
		if(msg==undefined)
		   msg="Declining invitation...";
		openModalPanel("par",msg);
				
		YAHOO.util.Connect.asyncRequest('PUT',serverRoot +'/REST/pars/' + par_id + '?decline=true&format=json&XNAT_CSRF='+csrfToken,callback,null,this);
	}
	
	this.render=function(){
		this.div.innerHTML="";

        if (this.pars.length == 0) {
            jq('#heading').html('Sorry...');
            jq('#pil').html('There are no outstanding project access requests for the current user.');
            return;
        }
		for(var parC=0;parC<this.pars.length;parC++){
			var p=this.pars[parC];
		
			var newDisplay = document.createElement("div");
			
			if(this.obj.projectBased){
				if(p.approval_date!=""){ 
					newDisplay.style.color="gray";
				}
				newDisplay.innerHTML=p.create_date + ' ' + p.email;
				if(p.approved==""){ 
					
				}else if(p.approved){ 
					newDisplay.innerHTML+=" ACCEPTED";
				}else{
					newDisplay.innerHTML+=" DECLINED";
				}
			}else{
			
				if(parC%2==0){
				  newDisplay.className="even";
				}else{
				  newDisplay.className="odd";
				}
				var row=document.createElement("div");
				row.innerHTML="<h3 style='margin-bottom:3px'>" +p.name + "</h3>";
				newDisplay.appendChild(row);
				
				row=document.createElement("div");
				row.innerHTML="<b>" + XNAT.app.displayNames.singular.project + " ID: " + p.id +"</b>";;
				if(p.pi!=undefined && p.pi!=""){
					row.innerHTML+="&nbsp;&nbsp;&nbsp;<b>PI: "+ p.pi +"</b>";
				}
				newDisplay.appendChild(row);
				
				row=document.createElement("div");
				if(p.description!=null && p.description.length>260){
					row.innerHTML=p.description.substring(0,157) + "&nbsp;...";
				}else{
					row.innerHTML=p.description;
				}
				newDisplay.appendChild(row);
				
				this.div.appendChild(newDisplay);
				
				var row=document.createElement("div");
				
				var aB=document.createElement("button");
				aB.innerHTML="Join";
				aB.value="Accept";
				aB.par_id=p.par_id;
				aB.manager=this;
				aB.onclick=function(o,o2){
					this.manager.accept(this.par_id);
				}
				row.appendChild(aB);
				
				var aB=document.createElement("button");
				aB.value="Decline";
				aB.innerHTML="Decline";
				aB.par_id=p.par_id;
				aB.manager=this;
				aB.onclick=function(o,o2){
					this.manager.decline(this.par_id);
				}
				row.appendChild(aB);
				
				newDisplay.appendChild(row);
			}
			
			
			this.div.appendChild(newDisplay);
		}
		//this.menu=new YAHOO.widget.Menu(this.div_id,{itemdata:items,visible:true, scrollincrement:5,position:"static"});

	}
}
