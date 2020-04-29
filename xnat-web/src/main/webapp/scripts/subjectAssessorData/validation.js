
/*
 * web: validation.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
function confirmValues(_focus){
	if(_focus==undefined)_focus=true;
  var valid =true;
  
  try{
	  var projBox=getValueById(elementName+"/project");
	  if(projBox.value!=""){
	     removeAppendedIcon(elementName+"/project");
	  }else{
	   	  appendIcon(elementName+"/project","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
	  
	  var subBox=getValueById(elementName+"/subject_id");
	  if(subBox.value!=""){
	  	 if(subBox.obj.selectedIndex!=undefined){
	  	 	if(subBox.obj.options[subBox.obj.selectedIndex].style.color=="red"){
	  	 		document.getElementById("subj_msg").innerHTML="<i class='fa fa-asterisk' title='Required' style='margin-left: 5px;'></i> This " + XNAT.app.displayNames.singular.subject.toLowerCase() + " does not exist, and will be automatically created.  To populate demographic details for this " + XNAT.app.displayNames.singular.subject.toLowerCase() + " please use the 'Add New " + XNAT.app.displayNames.singular.subject + "' link.";
	  	 	}else{
	  	 		document.getElementById("subj_msg").innerHTML="";
	  	 	}
	  	 }
	     removeAppendedIcon(elementName+"/subject_id");
	  }else{
	   	  appendIcon(elementName+"/subject_id","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
	  
	  var labelBox=getValueById(elementName+"/label");
	  
	  if(labelBox.obj.validated==undefined)labelBox.obj.value=fixSessionID(labelBox.obj.value);;
	  if(labelBox.obj.value.toLowerCase()=="null"){
	  	labelBox.obj.value="";
		labelBox.value="";
	  }
	  if(labelBox.value!=""){
	  	 labelBox.obj.validated=false;
	     removeAppendedIcon(elementName+"/label");
			try{
				if(eval("window.verifyExptId")!=undefined){
					if(verifyExptId() === false){ valid = false; };
					
				}
			}catch(e){
				if(!e.message.startsWith("verifyExptId is not defined")){
					throw e;
				}
			}
	  }else{
	  	  labelBox.obj.validated=true;
	   	  appendIcon(elementName+"/label","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
		
	  if(window.scanSet!=undefined){
		  if(!window.scanSet.validate(_focus)){
		  	  valid=false;
		  }
	  }
	

	return valid;
  }catch(e){
  	  xmodal.message('Error',"An exception has occurred. Please contact technical support for assistance.");
  	  return false;
  }
}

function getValueById(id){
	var box=document.getElementById(id);
	if(box==null){ return null; }
	
	if(box.value==undefined){
		if(box.selectedIndex!=undefined){
			return {"value":box.options[box.selectedIndex].value,obj:box};
		}
	}else{
		return {"value":box.value,obj:box};
	}
}

function fixSessionID(val)
{
        var temp = val.trim();
        var newVal = '';
        temp = temp.split(' ');
        for(var c=0; c < temp.length; c++) {
                newVal += '' + temp[c];
        }
        
        newVal = newVal.replace(/[&]/,"_");
        newVal = newVal.replace(/[?]/,"_");
        newVal = newVal.replace(/[<]/,"_");
        newVal = newVal.replace(/[>]/,"_");
        newVal = newVal.replace(/[(]/,"_");
        newVal = newVal.replace(/[)]/,"_");
        newVal = newVal.replace(/[.]/,"_");
        newVal = newVal.replace(/[,]/,"_");
        newVal = newVal.replace(/[\^]/,"_");
        newVal = newVal.replace(/[@]/,"_");
        newVal = newVal.replace(/[!]/,"_");
        newVal = newVal.replace(/[%]/,"_");
        newVal = newVal.replace(/[*]/,"_");
        newVal = newVal.replace(/[#]/,"_");
        newVal = newVal.replace(/[$]/,"_");
        newVal = newVal.replace(/[\\]/,"_");
        newVal = newVal.replace(/[|]/,"_");
        newVal = newVal.replace(/[=]/,"_");
        newVal = newVal.replace(/[+]/,"_");
        newVal = newVal.replace(/[']/,"_");
        newVal = newVal.replace(/["]/,"_");
        newVal = newVal.replace(/[~]/,"_");
        newVal = newVal.replace(/[`]/,"_");
        newVal = newVal.replace(/[:]/,"_");
        newVal = newVal.replace(/[;]/,"_");
        newVal = newVal.replace(/[\/]/,"_");
        newVal = newVal.replace(/[\[]/,"_");
        newVal = newVal.replace(/[\]]/,"_");
        newVal = newVal.replace(/[{]/,"_");
        newVal = newVal.replace(/[}]/,"_");
        if(newVal!=temp){
            xmodal.message('Image Session Validation', 'Removing invalid characters in ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + '.');
        }
        return newVal;
}
