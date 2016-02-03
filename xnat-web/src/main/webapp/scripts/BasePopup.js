/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/BasePopup.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/26/13 3:34 PM
 */
function BasePopup(_id,_config){
  this.id=_id;
  this.config=_config;
  
  if(this.config.defaultHeight==undefined)this.config.defaultHeight=450;
  if(this.config.defaultWidth==undefined)this.config.defaultWidth=600;
  
  this.initd=false;
  
  this.beforeInit=function(obj){
  	return true;
  }
  
  this.init=function(){
  	if(this.beforeInit(this.config)){
		var popupDIV = document.createElement("DIV");
		popupDIV.id=this.id;
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
		//popupFT.style.height="20px";
		popupDIV.appendChild(popupFT);
		
		popupHD.innerHTML=this.config.title;
		
		//add to page
		var tp_fm=document.getElementById("tp_fm");
		tp_fm.appendChild(popupDIV);
		
		this.popup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,width:this.config.defaultWidth+"px",height:this.config.defaultHeight+"px",visible:false,draggable:true,modal:true,fixedcenter:true});
		this.popup.manager=this;		
		
		this.popup.hideEvent.subscribe(function(obj1,obj2,obj3){
			this.popup.destroy();
			YAHOO.util.Event.preventDefault(obj1);
		},this,this);
		
		
		this.resizer=new YAHOO.util.Resize(_id,{
			handles: ['br'],
			autoRatio:false,
			minWidth:500,
			minHeight:300,
			status: false
		});
		this.resizer.on('resize',function(args){
			var panelHeight=args.height;
			this.popup.cfg.setProperty("height",panelHeight +"px");
		},this,true);
		
		this.drawContents(popupBD);
		
  		this.popup.render();
  		this.popup.show();
		this.initd=true;
  	}
  }
  
  this.drawContents=function(_div){
  	
  }
  
        	
}