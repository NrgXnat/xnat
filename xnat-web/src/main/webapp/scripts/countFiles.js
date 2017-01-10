/*
 * web: countFiles.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//function countFiles(){
//   var url = serverRoot + "/servlet/AjaxServlet?remote-class=org.nrg.xnat.ajax.CountFiles";
//   url = url + "&remote-method=execute";
//   url = url + "&ID=" + image_session_id;
//   if (window.XMLHttpRequest) {
//       req = new XMLHttpRequest();
//   } else if (window.ActiveXObject) {
//       req = new ActiveXObject("Microsoft.XMLHTTP");
//   }
//   req.open("GET", url, true);
//   req.onreadystatechange = callback;
//   req.send(null);
//}
//
//
//// a GLOBAL function named 'callback'?
//function callback() {
//    if (req.readyState == 4) {
//        if (req.status == 200) {
//            // handle response
//            var xmlDoc = req.responseXML;
//            if (xmlDoc)
//            {
//               var sessionRoot = xmlDoc.getElementsByTagName("session")[0];
//               if (sessionRoot)
//               {
//                  for(var i=0;i<sessionRoot.childNodes.length;i++)
//                  {
//                     var scanChild = sessionRoot.childNodes[i];
//                     var scanAttributes = scanChild.attributes;
//                     if (scanAttributes)
//                     {
//                        var scanId = scanAttributes.getNamedItem("ID").value;
//                        var scanStats = scanAttributes.getNamedItem("stats").value;
//                        if (scanId=="misc")
//                        {
//                           var scanDIVID = "STATS_MISC";
//                           var scanDIV = document.getElementById(scanDIVID);
//                           if (scanDIV)
//                           {
//                             var text =    "<TR valign='top' border='0'>";
//                             text = text + "   <TD border=0 align=left colspan='4'><BR><strong>The following files were not listed in the " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + " information document:</TD>";
//                             text = text + "</TR>";
//                             text = text + "<TR valign=top border=0>";
//                             text = text + "<TD border=0 align=left><strong>	<A name=\"LINK5000\" HREF=\"#LINK5000\" onClick=\"return blocking(5000);\"><img ID=\"IMG5000\" src=\"" + serverRoot + "/images/plus.jpg\" border=0> misc </A></strong></TD>";
//                             text = text + "<TD border=0 align=left>unknown</TD>";
//                             text = text + "<TD border=0 align=left NOWRAP>" + scanStats +"</TD>";
//                             text = text + " </TR>";
//
//                             text = text + "<TR><TD colspan=4>";
//                             text = text + "<span ID=\"span5000\" style=\"position:relative; display:none;\">";
//                             text = text + "<TABLE>";
//                             for(var j=0;j<scanChild.childNodes.length;j++)
//                  					           {
//                      					           var fChild = scanChild.childNodes[j];
//                      					           var fAttributes = fChild.attributes;
//                      					           if (fAttributes)
//                      					           {
//                      					               var fName = fAttributes.getNamedItem("name").value;
//                     					                text = text + "<TR><TD>" + fName + "</TD></TR>";
//                    					             }
//                             }
//                             text = text + "</TABLE>";
//                             text = text + "</span>";
//                             text = text + "</TD></TR>";
//
//                             scanDIV.innerHTML=text;
//                           }
//                        }else{
//                           var scanDIVID = "STATS_" + scanId;
//                           var scanDIV = document.getElementById(scanDIVID);
//                           if (scanDIV)
//                           {
//                             scanDIV.innerHTML=scanStats;
//                           }else{
//                           }
//                        }
//                     }
//                  }
//               }
//            }
//       }
//    }
//}
