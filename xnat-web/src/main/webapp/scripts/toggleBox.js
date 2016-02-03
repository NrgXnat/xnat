/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/toggleBox.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
var maxLocation = serverRoot+"/images/maximize.gif";

	var minLocation = serverRoot+"/images/minimize.gif";
	
	var restoreLocation = serverRoot+"/images/restore_down.gif";


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
	
	function setMaxLocation(img){
	     img.src=maxLocation;
	}

	function hideBox(name){

	   var img = document.getElementById(name + 'Image');

	   var box = document.getElementById(name + 'Body');

	   var container = document.getElementById(name + 'Container');

	   var menu = document.getElementById(name + 'menu');


	     img.src=maxLocation;
	     
	    var img2 = document.getElementById(name + 'Image2');
	     if (img2)img2.src=restoreLocation;

     if (box != null){

	       box.style.display="none";
	       
	     }
	     
	     if (menu!=null){

	       menu.style.display="none";

	     }
	   
	}
	   
	function showBox(name){

	   var img = document.getElementById(name + 'Image');

	   var box = document.getElementById(name + 'Body');

	   var container = document.getElementById(name + 'Container');

	   var menu = document.getElementById(name + 'menu');


	     img.src=minLocation;
	     
	    var img2 = document.getElementById(name + 'Image2');
	     if (img2)img2.src=maxLocation;

     if (box != null){

	       box.style.display="block";
	       
	     }

	     if (menu!=null){

	       menu.style.display="block";

	     }
     

	}

	function toggleAll(except){
   
	   var img = document.getElementById(except+'Image');
   
   var allDIVS = document.getElementsByTagName("DIV");
   for (divCount=0;divCount<allDIVS.length;divCount++)
   {
     var thisDiv = allDIVS[divCount];
		      if (((' '+thisDiv.className+' ').indexOf("containerBody") != -1) && (thisDiv.id)) {
		        if(thisDiv.id!=except+"Body")
		        {
		          var thisID = thisDiv.id;
		            var name = thisID.substring(0,thisID.length-4);
		            if ((img.src + ' ').indexOf(minLocation)!=-1 || (img.src + ' ').indexOf(restoreLocation)!=-1)
		            	   showBox(name);
		            	else
		            	   hideBox(name);
		        }
		      }
   }
   
	     
	    var img2 = document.getElementById(name + 'Image2');
	     if (img2)img.src=maxLocation;
   
   if ((img.src + ' ').indexOf(minLocation)!=-1 || (img.src + ' ').indexOf(restoreLocation)!=-1){
      img.src=maxLocation;
	     
	  if (img2)img2.src=minLocation;
   }else{
      showBox(except);
      img.src=restoreLocation;
	     
	  if (img2)img2.src=minLocation;
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