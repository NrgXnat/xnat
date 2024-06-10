
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
	  
	  var month=getValueById("xnat:petSessionData.date.month");
	  var year=getValueById("xnat:petSessionData.date.year");
	  var day=getValueById("xnat:petSessionData.date.date");
	  	  
	  if(month.value!="" && month.value!="bad"){
	     removeAppendedIcon("xnat:petSessionData.date.month");
	  }else{
	   	  appendIcon("xnat:petSessionData.date.month","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
	  	  
	  if(day.value!="" && day.value!="bad"){
	     removeAppendedIcon("xnat:petSessionData.date.date");
	  }else{
	   	  appendIcon("xnat:petSessionData.date.date","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
	  	  
	  if(year.value!="" && year.value!="bad"){
	     removeAppendedIcon("xnat:petSessionData.date.year");
	  }else{
	   	  appendIcon("xnat:petSessionData.date.year","fa-asterisk","Required",{ color: '#c66' });
	   	  valid=false;
	  }
  
	  var tracer=getValueById("xnat:petSessionData.tracer.name");
	  if(tracer.value!="" && tracer.value!="bad"){
	    if(document.getElementById("SEL_xnat:petSessionData.tracer.name")!=undefined){
	  		removeAppendedIcon("SEL_xnat:petSessionData.tracer.name");
	  	}else{
	  		removeAppendedIcon("xnat:petSessionData.tracer.name");
	  	}
	  }else{
	  	if(document.getElementById("SEL_xnat:petSessionData.tracer.name")!=undefined){
	  		appendIcon("SEL_xnat:petSessionData.tracer.name","fa-asterisk","Required",{ color: '#c66' });
	  	}else{
	  		appendIcon("xnat:petSessionData.tracer.name","fa-asterisk","Required",{ color: '#c66' });
	  	}
	   	  valid=false;
	  }
	  
	  if(valid){
		  var ih=getValueById("xnat:petSessionData/start_time_injection.hours");
		  if(ih!=null && ih!=undefined){
			var im=getValueById("xnat:petSessionData/start_time_injection.minutes");
			var is=getValueById("xnat:petSessionData/start_time_injection.seconds");
			var eh=getValueById("xnat:petSessionData/start_time_scan.minutes");
			var em=getValueById("xnat:petSessionData/start_time_scan.seconds");
		  	var es=getValueById("xnat:petSessionData/start_time_scan.seconds");
		  	if(ih.value!="" && ih.value!="bad"){
		  		if(im.value!="" && im.value!="bad"){
				    removeAppendedIcon("xnat:petSessionData/start_time_injection.minutes");
				}else{
				   	appendIcon("xnat:petSessionData/start_time_injection.minutes","fa-asterisk","Required",{ color: '#c66' });
				   	valid=false;
				}
		  	}
		  	
		  	if(eh.value!="" && eh.value!="bad"){
		  		if(em.value!="" && em.value!="bad"){
				    removeAppendedIcon("xnat:petSessionData/start_time_scan.minutes");
				}else{
				   	appendIcon("xnat:petSessionData/start_time_scan.minutes","fa-asterisk","Required",{ color: '#c66' });
				   	valid=false;
				}
		  	}
		  	
		  	if(valid){
		  		if(ih.value!="" && ih.value!="bad"){
		  			var imonth=document.createElement("input");
		  			imonth.name="xnat:petSessionData/start_time_injection.month";
		  			imonth.type="hidden";
		  			imonth.value=month.value;
		  			ih.obj.parentNode.appendChild(imonth);
		  			
		  			var idate=document.createElement("input");
		  			idate.name="xnat:petSessionData/start_time_injection.date";
		  			idate.type="hidden";
		  			idate.value=day.value;
		  			ih.obj.parentNode.appendChild(idate);
		  			
		  			var iyear=document.createElement("input");
		  			iyear.name="xnat:petSessionData/start_time_injection.year";
		  			iyear.type="hidden";
		  			iyear.value=year.value;
		  			ih.obj.parentNode.appendChild(iyear);
		  		}
		  		
		  		if(eh.value!="" && eh.value!="bad"){
		  			var emonth=document.createElement("input");
		  			emonth.name="xnat:petSessionData/start_time_scan.month";
		  			emonth.type="hidden";
		  			emonth.value=month.value;
		  			eh.obj.parentNode.appendChild(emonth);
		  			
		  			var edate=document.createElement("input");
		  			edate.name="xnat:petSessionData/start_time_scan.date";
		  			edate.type="hidden";
		  			edate.value=day.value;
		  			eh.obj.parentNode.appendChild(edate);
		  			
		  			var eyear=document.createElement("input");
		  			eyear.name="xnat:petSessionData/start_time_scan.year";
		  			eyear.type="hidden";
		  			eyear.value=year.value;
		  			eh.obj.parentNode.appendChild(eyear);
		  		}
		  	}
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
	if(!box){
		return null;
	}
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
