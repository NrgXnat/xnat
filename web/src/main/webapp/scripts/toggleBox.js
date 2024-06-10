/*
 * web: toggleBox.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
var maxLocation = "fa fa-window-maximize";

	var minLocation = "fa fa-window-minimize";
	
	var restoreLocation = "fa fa-window-restore";


	function toggleBox(name){
	   var box = document.getElementById(name + 'Body');

	   if (box.style.display=="none")

	   {
	     showBox(name);
	   }else{
	     hideBox(name);

	   }
	   
	   resize_id(null);
	}
	
	function setMaxLocation(icon){
	     icon.className=maxLocation;
	}

	function hideBox(name){

	   var icon = document.getElementById(name + 'Icon');

	   var box = document.getElementById(name + 'Body');

	   var container = document.getElementById(name + 'Container');

	   var menu = document.getElementById(name + 'menu');


	     icon.className=maxLocation;
	     
	    var icon2 = document.getElementById(name + 'Icon2');
	     if (icon2)icon2.className=restoreLocation;

     if (box != null){

	       box.style.display="none";
	       
	     }
	     
	     if (menu!=null){

	       menu.style.display="none";

	     }
	   
	}
	   
	function showBox(name){

	   var icon = document.getElementById(name + 'Icon');

	   var box = document.getElementById(name + 'Body');

	   var container = document.getElementById(name + 'Container');

	   var menu = document.getElementById(name + 'menu');


	     icon.className=minLocation;
	     
	    var icon2 = document.getElementById(name + 'Icon2');
	     if (icon2)icon2.className=maxLocation;

     if (box != null){

	       box.style.display="block";
	       
	     }

	     if (menu!=null){

	       menu.style.display="block";

	     }
     

	}

	function toggleAll(except){
   
	   var icon = document.getElementById(except+'Icon');
   
   var allDIVS = document.getElementsByTagName("DIV");
   for (divCount=0;divCount<allDIVS.length;divCount++)
   {
     var thisDiv = allDIVS[divCount];
		      if (((' '+thisDiv.className+' ').indexOf("containerBody") != -1) && (thisDiv.id)) {
		        if(thisDiv.id!=except+"Body")
		        {
		          var thisID = thisDiv.id;
		            var name = thisID.substring(0,thisID.length-4);
		            if ((icon.className + ' ').indexOf(minLocation)!=-1 || (icon.className+ ' ').indexOf(restoreLocation)!=-1)
		            	   showBox(name);
		            	else
		            	   hideBox(name);
		        }
		      }
   }
   
	     
	    var icon2 = document.getElementById(name + 'Icon2');
	     if (icon2)icon2.className=maxLocation;
   
   if ((icon.className + ' ').indexOf(minLocation)!=-1 || (icon.className + ' ').indexOf(restoreLocation)!=-1){
      icon.className=maxLocation;
	     
	  if (icon2)icon2.className=minLocation;
   }else{
      showBox(except);
      icon.className=restoreLocation;
	     
	  if (icon2)icon2.className=minLocation;
   }
            
   resize_id(except);

	}

	function getWinSize(){
   var iWidth = 0, iHeight = 0;

   if (document.documentElement && document.documentElement.clientHeight){
       iWidth = parseInt(window.innerWidth,10);
       iHeight = parseInt(window.innerHeight,10);
   }
   else if (document.body){
       iWidth = parseInt(document.body.offsetWidth,10);
       iHeight = parseInt(document.body.offsetHeight,10);
   }

   return {width:iWidth, height:iHeight};
}

function getMainContainer(){
    alldivs = document.getElementsByTagName("DIV");
	for (di=0;di<alldivs.length;di++) {
		thisTbl = alldivs[di];
		if (((' '+thisTbl.className+' ').indexOf("mainContainerBody") != -1) && (thisTbl.id) ) {
		  return thisTbl;
		}
	}
	
	//return document.getElementById("toggleMain");
}
	
function resize_id(box){
  var marginW = 80;
  var marginH=90;
  if (box!=null)
    var oContent = document.getElementById(box);
  if(!oContent){
    var oContent = getMainContainer();
  }
  if (oContent){
    var leftTable = document.getElementById("leftBarTable");
    var oWinSize = getWinSize();
    if (leftTable){
      marginW = marginW + leftTable.offsetWidth + leftTable.offsetLeft;
    }
    var newWidth = oWinSize.width - oContent.offsetLeft - marginW;

  
    if (newWidth<620){
       newWidth=620;
    }
  
    if (newWidth<oContent.scrollWidth){
       //scroll bar
       marginH+=20;
    }
  
    var DefaultTopTR =document.getElementById("DefaultTopTR");
    var toggleCollection =document.getElementById("toggleCollection");
    var contentHeaderTR =document.getElementById("contentHeaderTR");
    var DefaultBottomTR=document.getElementById("DefaultBottomTR");
  
    var newHeight = oWinSize.height-DefaultTopTR.offsetTop-DefaultTopTR.offsetHeight;
    if(toggleCollection)newHeight = newHeight-toggleCollection.offsetTop-toggleCollection.offsetHeight;
    if (contentHeaderTR)newHeight = newHeight-contentHeaderTR.offsetTop-contentHeaderTR.offsetHeight;
   // newHeight = newHeight-DefaultBottomTR.offsetTop-DefaultBottomTR.offsetHeight;
    newHeight = newHeight-marginH;
  
    if (newHeight>oContent.scrollHeight){
      newHeight = oContent.scrollHeight+marginH + 5;
    }
  
    if (newHeight<200){
      newHeight=200;
    }
    document.getElementById("toggleMain").style.width=newWidth+20;
    oContent.style.width=newWidth;
    oContent.style.height=newHeight;
    oContent.style.minHeight=newHeight;
    //oContent.style.border="1px solid red";
  
    //out(newWidth + ":" + oWinSize.width + ":" + oContent.offsetLeft + ":" + marginW + "||" + newHeight + ":" + oWinSize.height + ":" + marginH);
  }
}
  
  function out(msg){
     document.getElementById("DEBUG_OUT").innerHTML += msg + "<br>";
  }
