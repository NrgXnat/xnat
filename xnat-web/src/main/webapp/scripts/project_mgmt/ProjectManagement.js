/*
 * web: ProjectManagement.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
/**************************
 * Ajax Object for inviting a user to join a project.
 *
 ************************************/
 var ProjectInvitation = {
 	handleSuccess:function(o){
 		var tbody = o.argument[3];
 		var tr = document.createElement("TR");
 		var td1 = document.createElement("TD");
 		var td2 = document.createElement("TD");
 		var td3 = document.createElement("TD");
 		
 		td1.innerHTML=o.argument[0].value;
 		td2.innerHTML=o.argument[1].options[o.argument[1].selectedIndex].value;
 		td3.innerHTML="EMAILED";
 		tr.appendChild(td1);
 		tr.appendChild(td2);
 		tr.appendChild(td3);
 		tbody.appendChild(tr);
 		
 		
 		o.argument[0].value="";
 		o.argument[1].selectedIndex=0;
 		o.argument[0].disabled=false;
 		o.argument[1].disabled=false;
 		o.argument[2].disabled=false;
 		o.argument[4].style.display="block";
 	},
 	handleFailure:function(o){
        xmodal.message('Email Error', 'Unable to send email to '+o.argument[0].value);
 		
 		o.argument[0].value="";
 		o.argument[1].selectedIndex=0;
 		o.argument[0].disabled=false;
 		o.argument[1].disabled=false;
 		o.argument[2].disabled=false;
 	},
 	startRequest:function(email,level,project,callback){
 		callback.argument[0].disabled=true;
 		callback.argument[1].disabled=true;
 		callback.argument[2].disabled=true;
        if (callback.cache == undefined) {
            callback.cache = false;
        }
 		var params = "remote-class=org.nrg.xnat.ajax.GrantProjectAccess&remote-method=invite&email=" + email +"&project=" + project +"&level=" + level + "&XNAT_CSRF=" + csrfToken;
 		YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',callback,params);
 	}
 };
 

 
