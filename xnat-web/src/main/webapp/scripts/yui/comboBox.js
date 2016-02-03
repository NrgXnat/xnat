/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/yui/comboBox.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function XdatYUIComboBox(_input,_options,_values){
	
	if(_options==undefined)
	  this.options=new Object();
	else
	  this.options=_options;
	  
	  this.array=_values;
	  
	if(typeof _input == 'string'){
		this.hiddenField = (document.getElementById(_input));
	}else if(typeof _input == 'object'){
		this.hiddenField = _input;
	}else{
		return;
	}
	
	this.defaultValue=this.hiddenField.value;
	
	if(this.options.toggle_button!=undefined){
		if(typeof this.options.toggle_button == 'string'){
			this.dToggler = (document.getElementById(this.options.toggle_button));
		}else if(typeof this.options.toggle_button == 'object'){
			this.dToggler = this.options.toggle_button;
		}
	}else{
		this.dToggler=document.getElementById("toggle_"+ this.hiddenField.id);
	}
	
	if(this.options.txt_field!=undefined){
		if(typeof this.options.txt_field == 'string'){
			this.txtField = (document.getElementById(this.options.txt_field));
		}else if(typeof this.options.txt_field == 'object'){
			this.txtField = this.options.txt_field;
		}
	}else{
		this.txtField=document.getElementById("txt_"+ this.hiddenField.id);
	}
	
	if(this.options.auto_complete_box!=undefined){
		if(typeof this.options.auto_complete_box == 'string'){
			this.auto_box = (document.getElementById(this.options.auto_complete_box));
		}else if(typeof this.options.auto_complete_box == 'object'){
			this.auto_box = this.options.auto_complete_box;
		}
	}else{
		this.auto_box=document.getElementById("auto_"+ this.hiddenField.id);
	}
	
	if(this.options.container!=undefined){
		if(typeof this.options.container == 'string'){
			this.container = (document.getElementById(this.options.container));
		}else if(typeof this.options.container == 'object'){
			this.container = this.options.container;
		}
	}else{
		this.container=document.getElementById("cont_"+ this.hiddenField.id);
	}
	
	if(this.options.valueKey==undefined){
		this.options.valueKey="value";
	}
	
	if(this.options.idKey==undefined){
		this.options.idKey="name";
	}
	
	this.pushButton = new YAHOO.widget.Button({container:this.dToggler}); 
	this.dToggler.style.display="none";
	
	this.oDS=new YAHOO.util.LocalDataSource(this.array);
	if(this.options.mode==undefined || this.options.mode=="single"){
		this.oDS.responseSchema={fields:["value"]};
	}else{
		this.oDS.responseSchema={fields:["name","value"]};
	}
	
	
	this.oAC=new YAHOO.widget.AutoComplete(this.txtField,this.auto_box,this.oDS,{maxResultsDisplayed:200});
	this.oAC.prehighlightClassName = "yui-ac-prehighlight";   
	this.oAC.minQueryLength = 0;
	
	this.setZIndex=function(zIndex){
		this.container.style.zIndex=zIndex;
		this.auto_box.style.zIndex=zIndex;
		for(var cNaC=0;cNaC<this.auto_box.childNodes.length;cNaC++){
			if(this.auto_box.childNodes[cNaC].style!=undefined){
				this.auto_box.childNodes[cNaC].style.zIndex=zIndex;
			}
		}
	}
	
	this.setZIndex("0");
	
	if(this.options.zIndex!=undefined){
		this.oAC.containerExpandEvent.subscribe(function (sType,aArgs){
			this.setZIndex(this.options.zIndex);
		},null,this);
		
		this.oAC.containerCollapseEvent.subscribe(function (sType,aArgs){
			this.setZIndex("0");
		},null,this);
	}
	
	if(this.options.mode!=undefined && this.options.mode!="single"){
		this.oAC.itemSelectEvent.subscribe(function(sType,aArgs){
	     	   var myAC=aArgs[0];
	     	   var elLI=aArgs[1];
	     	   var oData=aArgs[2];
	     	   this.txtField.value=this.getName(oData[0]);
	     	   this.hiddenField.value=oData[0];
	     },null,this);
	}
     
     this.txtField.combo=this;
     this.txtField.onkeyup=function(){
     	var match=false;
        for(var aC=0;aC<this.combo.array.length;aC++){
           if(this.combo.array[aC].name==this.value || this.combo.array[aC].value==this.value){
             this.combo.hiddenField.value=this.combo.array[aC].value;
             match=true;
             break;
           }
        }
        if(!match)this.combo.hiddenField.value=this.value;
     };
				     
	if(this.array.length>0){
       //show label button     
       var toggleD = function(e) {   
          //YAHOO.util.Event.stopEvent(e);   
          if(!YAHOO.util.Dom.hasClass(this.dToggler, "open")) {   
             YAHOO.util.Dom.addClass(this.dToggler, "open")   
          }   
            
          // Is open   
          if(this.oAC.isContainerOpen()) {   
             this.oAC.collapseContainer();   
          }     
          else {   
             // Is closed 
             this.oAC.getInputEl().focus(); // Needed to keep widget active 
             window.tempOAC=this.oAC;  
             setTimeout(function() { // For IE   
                 window.tempOAC.sendQuery("");   
             },0);   
          }
       }
       this.pushButton.on("click", toggleD,this,this);
       this.oAC.containerCollapseEvent.subscribe(function(){YAHOO.util.Dom.removeClass(this.dToggler, "open")});   
       this.dToggler.style.display="";
    }else{
       this.dToggler.style.display="none";
    }
    
    this.getName=function(value){
    	for(var aC=0;aC<this.array.length;aC++){
           if(this.array[aC].value==value){
             if(this.array[aC].name==undefined){
             	return value;
             }else{
             	return this.array[aC].name;
             }
             break;
           }
        }
    }
}