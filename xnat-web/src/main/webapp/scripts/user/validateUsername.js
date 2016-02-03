
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/user/validateUsername.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
var ValidChars = "0123456789.";

function isNumeric(sText)
{
   var IsNumber=true;
   var Char;

   for (i = 0; i < sText.length && IsNumber == true; i++) 
   { 
      Char = sText.charAt(i); 
      if (ValidChars.indexOf(Char) == -1) 
         {
         IsNumber = false;
         }
   }
   return IsNumber;
}

// check to see if input is alphanumeric
function isAlphaNumeric(val)
{
  if (val.match(/^[a-zA-Z0-9]+$/))
  {
    return true;
  }
    else
  {
    return false;
  } 
}

function appendImage(obj,img_name){
	if(obj.appendedImage==undefined){
	    obj.appendedImage = document.createElement("img");
	    obj.appendedImage.style.marginLeft="5pt";
	    if(obj.nextSibling==null)
	    {
	    	obj.parentNode.insertBefore(obj.appendedImage,obj.nextSibling);
	    }else{
	    	obj.parentNode.appendChild(obj.appendedImage);
	    }
	}
	obj.appendedImage.src=serverRoot + img_name;
}

function validateUsername(obj,button_id){
	   var valid = false;
	   if (obj.value!=""){
	   	   if(isNumeric(obj.value.charAt(0))){
              xModalMessage('User Validation', 'Username cannot begin with a number.  Please modify.');
	   	      obj.focus();
	   	   }else{
	   	   	   if(obj.value.length>40){
                   xModalMessage('User Validation', 'Username cannot exceed 40 characters');
	   	   		   obj.focus();
	   	   	   }else if(isAlphaNumeric(obj.value)){
	   	   	      valid= true;
	   	   	   }else{
                   xModalMessage('User Validation', 'Username cannot contain special characters.  Please modify.');
	     	       obj.focus();
	   	   	   }
	   	   }
	   }
	   
   		if(valid){
	   	   	   if(obj.appendedImage!=undefined)appendImage(obj,"/images/checkmarkGreen.gif");
   			   if(button_id!=undefined)document.getElementById(button_id).disabled=false;
   		}else{
	   	   	   appendImage(obj,"/images/checkmarkRed.gif");
   			   if(button_id!=undefined)document.getElementById(button_id).disabled=true;
   		}
	   
	   return valid;
}