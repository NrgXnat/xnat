
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/mrSessionData/validation.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/13/14 5:34 PM
 */
function confirmValues(_focus){
	if(_focus==undefined)_focus=true;
  var valid =true;
  
  try{
	  var projBox=getValueById(elementName+"/project");
	  if(projBox.value!=""){
	     removeAppendImage(elementName+"/project");
	  }else{
	   	  appendImage(elementName+"/project","/images/checkmarkRed.gif");
	   	  valid=false;
	  }
	  
	  var subBox=getValueById(elementName+"/subject_id");
	  if(subBox.value!=""){
	  	 if(subBox.obj.selectedIndex!=undefined){
	  	 	if(subBox.obj.options[subBox.obj.selectedIndex].style.color=="red"){
                                document.getElementById("subj_msg").innerHTML="* This " + XNAT.app.displayNames.singular.subject.toLowerCase() + " does not exist, and will be automatically created.  To populate demographic details for this " + XNAT.app.displayNames.singular.subject.toLowerCase() + " please use the 'Add New " + XNAT.app.displayNames.singular.subject + "' link.";
	  	 	}else{
	  	 		document.getElementById("subj_msg").innerHTML="";
	  	 	}
	  	 }
	     removeAppendImage(elementName+"/subject_id");
	  }else{
	   	  appendImage(elementName+"/subject_id","/images/checkmarkRed.gif");
	   	  valid=false;
	  }
	  
	  var labelBox=getValueById(elementName+"/label");
	  
	  if(labelBox.obj.validated==undefined)labelBox.obj.value=fixSessionID(labelBox.obj.value);;
	  if(labelBox.value!=""){
	  	 labelBox.obj.validated=false;
	     removeAppendImage(elementName+"/label");
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
	   	  appendImage(elementName+"/label","/images/checkmarkRed.gif");
	   	  valid=false;
	  }
		
	  if(window.scanSet!=undefined){
		  if(!window.scanSet.validate(_focus)){
		  	  valid=false;
		  }
	  }
  
	  return valid;
  }catch(e){
  	  xModalMessage('Error',"An exception has occurred. Please contact technical support for assistance.");
  	  return false;
  }
}

function getValueById(id){
	var box=document.getElementById(id);
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
            xModalMessage('Image Session Validation', 'Removing invalid characters in ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + '.');
        }
        return newVal;
}
