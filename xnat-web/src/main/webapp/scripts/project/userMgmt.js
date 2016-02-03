
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/project/userMgmt.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/23/14 1:16 PM
 */
/*******************************
 * Set of javascript functions to facilitate project-user access via AJAX
 */
var sessExpMsg = 'WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.';
var removeButtonFormatter = function(elCell, oRecord, oColumn, oData) {
	elCell.innerHTML="<input type=\"button\" ONCLICK=\"window.userManager.removeUser('" + oRecord.getData("login")  + "','" + oRecord.getData("GROUP_ID")  + "');\" value=\"Remove\"/>";
};

var groupDropdownFormatter = function(elCell, oRecord, oColumn, oData) {
    var user_access = oRecord.getData("displayname");
    var user_login = oRecord.getData("login");
    var access_select = "<select onchange=\"window.userManager.inviteUser('" + user_login + "',this.value)\">";
    $(window.userManager.groups).each(function (i1,v1){
        access_select += "<option value=\"" + v1.id + "\"" + ((user_access === v1.displayname)? " selected" : "") + ">" + v1.displayname + "</option>";
    });
    access_select += "</select>";
    elCell.innerHTML=access_select;
};

var allUsersGroupDropdownFormatter = function(elCell, oRecord, oColumn, oData) {
    var user_access = oRecord.getData("displayname");
    var user_login = oRecord.getData("login");
    var access_select = "<select onchange=\"window.userManager.adjustInviteLists('" + user_login + "',this.value)\">";
    access_select += "<option value=\"\" selected></option>";
    $(window.userManager.groups).each(function (i1,v1){
        access_select += "<option value=\"" + v1.id + "\">" + v1.displayname + "</option>";
    });
    access_select += "</select>";
    elCell.innerHTML=access_select;
};

function prependLoader(div_id,msg){
	if(div_id.id==undefined){
		var div=document.getElementById(div_id);
	}else{
		var div=div_id;
	}
	var loader_div = document.createElement("div");
	loader_div.innerHTML=msg;
	div.parentNode.insertBefore(loader_div,div);
	return new XNATLoadingGIF(loader_div);
}
var Local = {
		make_dialog : function (handleYes, handleNo, msg) {
	var dialog = new YAHOO.widget.SimpleDialog('widget_confirm', {
		visible:false,
		width: '20em',
		zIndex: 9998,
		close: false,
		fixedcenter: true,
		modal: true,
		draggable: true,
		constraintoviewport: true,
		icon: YAHOO.widget.SimpleDialog.ICON_WARN,
		buttons: [
		          { text: 'Yes', handler: handleYes},
		          { text: 'No', handler: handleNo, isDefault: true }
		          ]
	});
	dialog.setHeader("Email User?");
	dialog.setBody(msg);
	dialog.cfg.queueProperty('icon', YAHOO.widget.SimpleDialog.ICON_HELP);
	dialog.cfg.queueProperty('zIndex', 9998);
	dialog.render(document.body);
	return dialog;
},
confirm : function (msg, yesAction, noAction) {
	var dialog = this.make_dialog(yesAction, noAction, msg);
	dialog.show();
}
};

function UserManager(user_mgmt_div_id, pID, retrieveAllUsers){
	this.pID=pID;
	this.user_mgmt_div = document.getElementById(user_mgmt_div_id);

	this.project_table_div=document.createElement("DIV");
	this.project_table_div.id="user_table";
	this.user_mgmt_div.appendChild(this.project_table_div);
    this.retrieveAllUsers = retrieveAllUsers;

	this.userColumnDefs=[
	                     {key:"login",label:"Username",sortable:true},
	                     {key:"firstname",label:"Firstname",sortable:true},
	                     {key:"lastname",label:"Lastname",sortable:true},
	                     {key:"email",label:"Email",sortable:true},
	                     {key:"displayname",label:"Group",sortable:true,formatter:groupDropdownFormatter},
                         {key:"button",label:"Remove",formatter:removeButtonFormatter}];

	this.init=function(){
		this.initLoader=prependLoader(this.project_table_div,"Loading " + XNAT.app.displayNames.singular.project.toLowerCase() + " users");
		this.initLoader.render();
		
		if(this.groups==undefined){
			YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects/'+ pID + '/groups?format=json&stamp='+ (new Date()).getTime(),{
				success:this.handleGroupLoad,
				failure:this.handleGroupFailure,
				cache:false, // Turn off caching for IE
				scope:this
			},null,this);
		}
	};
	
	this.loadUsers=function(){
		this.initCallback={
				success:this.completeInit,
				failure:this.initFailure,
            cache:false, // Turn off caching for IE
				scope:this
		};
        var showDeactivatedUsers=(document.getElementById('showDeactivatedUsersCheck').checked?'/true':'');
		YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects/'+ pID + '/users'+showDeactivatedUsers+'?format=json&stamp='+ (new Date()).getTime(),this.initCallback,null,this);
		
	};
	
	this.handleGroupLoad=function(response){
		this.groups= eval("(" + response.responseText +")").ResultSet.Result;
		
		var tmpUploadFrm='' +
            '<div id="grp_dialog" style="visibility:hidden">' +

            '<div class="hd">Manage Groups</div>' +
            '<div class="bd" style="">' +
            '<div class="grp_a" style="padding:10px;overflow:auto;height:410px;">' +

            '<p>Current Groups: </p>' +

            '<table id="groups_box" class="xnat-table" style="margin:5px 0 10px;width:100%;">' +
            // GROUPS TABLE GOES HERE
            '</table>' +

            '<button id="create_group" ' +
            'onclick="window.location.href=\'' + serverRoot + '/app/template/XDATScreen_edit_xdat_userGroup.vm/tag/' + this.pID + '/src/project\'">' +
            'Create Custom User Group' +
            '</button>' +
	        '</div>' +
	        '</div>' +
	        '</div>';

        $("body").append(tmpUploadFrm);

		//initialize modal upload dialog
		XNAT.app.grp_dialog=new YAHOO.widget.Dialog("grp_dialog", { fixedcenter:true, visible:false, width:"340px", height:"500px", modal:true, close:true, draggable:true });
		XNAT.app.grp_dialog.cfg.queueProperty("buttons", [{ text:"Close", handler:{fn:function(){XNAT.app.grp_dialog.hide();}},isDefault:true}]);

		$("<button style='margin-top:10px;' id='' onclick='window.userManager.showGroups();return false;'>Manage Groups</button>").insertAfter("#user_invite_div");
		$("<button style='margin-top:10px;' id='' onclick='window.location=\""+ serverRoot +"/app/template/ManageProjectFeatures.vm/project/" + pID + "\"'>Manage Features</button>").insertAfter("#user_invite_div");

		this.loadUsers();
		
		
		$(window.userManager.groups).each(function (i1,v1){
	        $("#access_level").append("<option value=\"" + v1.id + "\">" + v1.displayname + "</option>");
	    });
	};
	
	this.modifyGroup=function(gID){
		window.location.href=serverRoot + '/app/template/XDATScreen_edit_xdat_userGroup.vm/tag/' + this.pID + '/src/project/search_element/xdat:userGroup/search_field/xdat:userGroup.ID/search_value/'+gID;
	};
	
	this.showGroups=function(){
		var tmpHtml='';
        if(this.renderedGroups==undefined){
			if(this.groups!=undefined && this.groups.length>0){
				tmpHtml+="<thead class='header'>" +
                    "<tr>" +
                    "<th class='col1'>&nbsp;</th>" +
                    "<th class='col2'>Group</th>" +
                    "<th class='col3'>Users</th>" +
                    "</tr>" +
                    "</thead>	";

                tmpHtml+='<tbody>';

                jq.each(this.groups,function(i1,v1){
                    tmpHtml +=
                        "<tr class='highlight'>" +
                        "<td class='col1 center'>";

                        if (v1.displayname == "Owners" || v1.displayname == "Members" || v1.displayname == "Collaborators") {
                            tmpHtml += "&nbsp;";
                        }
                        else {
                            tmpHtml += "<a class='link' href='javascript:' onclick='window.userManager.modifyGroup(\"" + v1.id + "\")'>Edit</a>";
                        }

                    tmpHtml +=
                        "</td>" +
                        "<td class='col2'>" + v1.displayname + "</td>" +
                        "<td class='col3 center'>" + v1.users + "</td>" +
                        "</tr>";
                });

                tmpHtml+='</tbody>';
            }else{
				tmpHtml="<tbody><td class='center' colspan='3' style='color:grey;font-style:italic;'>None</td></tbody>";
			}
			$("#groups_box").html(tmpHtml);
			
			this.renderedGroups=true;
			XNAT.app.grp_dialog.render(document.body);
		}
		XNAT.app.grp_dialog.show();
		
	};
	
	this.handleGroupFailure=function(response){
		this.displayError("ERROR " + o.status+ ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " group list.");
        if(o.status==401){
            xmodal.message('Session Expired', sessExpMsg);
            window.location=serverRoot+"/app/template/Login.vm";
        }
	};

	this.initFailure=function(o){
		this.initLoader.close();
        if (!window.leaving) {
            this.displayError("ERROR " + o.status+ ": Failed to load " + XNAT.app.displayNames.singular.project.toLowerCase() + " user list.");
            if(o.status==401){
                xmodal.message('Session Expired', sessExpMsg);
                window.location=serverRoot+"/app/template/Login.vm";
            }
        }
	};

	this.completeInit=function(o){
		try{
			this.userResultSet= eval("(" + o.responseText +")");
			this.render();
		}catch(e){
			this.displayError("ERROR " + o.status+ ": Failed to parse " + XNAT.app.displayNames.singular.project.toLowerCase() + " user list.");
		}
		this.initLoader.close();
		if(this.retrieveAllUsers && this.allUserResultSet == undefined)
			this.loadAllUsers();
	};

    this.reloadUsersForProject=function(){
        this.reloadCallback={
            success:this.completeInit,
            failure:this.inviteFailure,
            cache:false, // Turn off caching for IE
            scope:this
        };
        var showDeactivatedUsers=(document.getElementById('showDeactivatedUsersCheck').checked?'/true':'');
        YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/projects/'+ this.pID + '/users'+showDeactivatedUsers+'?format=json&stamp='+ (new Date()).getTime(),this.reloadCallback,null,this);
    };

	this.loadAllUsers=function(){
		this.allLoader=prependLoader("add_invite_user_header","Loading users");
		this.allLoader.render();
		this.setFormDisabled(true);
		this.allUsersCallback={
				success:this.completeAllUsers,
				failure:this.allUsersFailure,
            cache:false, // Turn off caching for IE
				scope:this
		};
		YAHOO.util.Connect.asyncRequest('GET',serverRoot +'/REST/users?format=json&stamp='+ (new Date()).getTime(),this.allUsersCallback,null,this);
	};

	this.allUsersFailure=function(o){
		this.displayError("ERROR " + o.status+ ": Failed to load complete user list.");
		this.allLoader.close();
	};

	this.completeAllUsers=function(o){
		try{
			this.allUserResultSet= eval("(" + o.responseText +")");
			this.setFormDisabled(false);
			this.allLoader.close();
			this.createPopup();
		}catch(e){
			this.displayError("ERROR " + o.status+ ": Failed to parse complete user list.");
			this.allLoader.close();
		}
	};

	this.render=function(){
		this.userDataSource = new YAHOO.util.DataSource(this.userResultSet.ResultSet.Result);
		this.userDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		this.userDataSource.responseSchema = {
				fields: ["login","firstname","lastname","email","displayname","GROUP_ID"]
		};

		if (this.userResultSet.ResultSet.Result.length>6)
			var config= {scrollable:true,height:"180"};
		this.userDataTable = new YAHOO.widget.DataTable("user_table", this.userColumnDefs,this.userDataSource,config);
	};

	this.setFormDisabled=function(value){
		var user_invite_div=document.getElementById("user_invite_div");
		var inputs=user_invite_div.getElementsByTagName("input");
		for(var inputCounter in inputs){
			inputs[inputCounter].disabled=value;
		}

		var selects=user_invite_div.getElementsByTagName("select");
		for(var selectsCounter in selects){
			selects[selectsCounter].disabled=value;
		}
	};

	this.resetForm=function(){
		document.getElementById("invite_email").value='';
		document.getElementById("invite_email").focus();
	};

	this.completeInvite=function(o){
		this.completeInit(o);
		this.setFormDisabled(false);
		this.resetForm();
	};

	this.inviteFailure=function(o){
		if(o.status==401){
            xmodal.message('Session Expired', sessExpMsg);
			window.location=serverRoot+"/app/template/Login.vm";
		}
        xmodal.message('User Management Error', 'ERROR ' + o.status +': Operation Failed.');
		this.setFormDisabled(false);
	};

	this.inviteUser= function (emails,access_level) {
		if (emails!=undefined && access_level!=undefined){
			var post_url = serverRoot + "/REST/projects/" + this.pID + "/users/" + access_level ;
			var email_array=emails.split(',');
			var unknownUsers=new Array();
			var knownUsers=new Array();
			for(var email_counter=0;email_counter<email_array.length;email_counter++){
				var email = email_array[email_counter].trim();
				if (!this.userExists(email))
				{
					unknownUsers.push(email);
				}else{
					knownUsers.push(email);
				}
			}
			if(unknownUsers.length>0){
				var confirmMsg=unknownUsers;
				if(unknownUsers.length>1) {
					confirmMsg+=" do not correspond to currently registered accounts. These users ";
				} else {
					confirmMsg+=" does not correspond to a currently registered account. This user ";
				}
				confirmMsg += 'may have an account under another email address.<br/><br/>Click Cancel if you\'d like to edit the email list and try again.<br/><br/>Click OK to go ahead and send an email inviting ';
				if(unknownUsers.length>1) {
					confirmMsg+="these users.";
				} else {
					confirmMsg+="this user.";
				}
                xmodal.confirm({
                    content: confirmMsg,
                    width: 440,
					height: 240,
					okAction: function(){
                        sendMail(true).call();
                    },
                    cancelAction: function(){
                        document.getElementById("invite_email").value='';
//                        return false;
                    }
                });
            }
            var that = this;
            var sendMail = function (send) {
                return function () {
                    if(this.hide != undefined)this.hide();
                    that.setFormDisabled(true);
                    that.insertCallback={
                        success:that.completeInvite,
                        failure:that.inviteFailure,
                        cache:false, // Turn off caching for IE
                        scope:that
                    };
                    var params = "XNAT_CSRF=" + csrfToken + "&format=json";
                    if(send){
                        params+="&sendemail=true";
                    }
                    var showDeactivatedUsers=(document.getElementById('showDeactivatedUsersCheck').checked?'/true':'');
                    YAHOO.util.Connect.asyncRequest('PUT',post_url + "/" + emails + showDeactivatedUsers + "?" + params,that.insertCallback,params,that);
                };
            };
            if(knownUsers.length>0){
                var confirmMsg="Would you like an email to be sent to " + knownUsers;
                if(knownUsers.length>1)
                    confirmMsg+=" to inform them of this addition?";
                else
                    confirmMsg+=" to inform him/her of this addition?";
                xmodal.confirm({
                    content: confirmMsg,
                    okLabel: 'Yes',
                    cancelLabel: 'No',
                    okAction: function(){
                        sendMail(true).call();
                    },
                    cancelAction: function(){
                        sendMail(false).call();
                    }
                });
                return true;
            }
		}
	};

	this.inviteUserFromForm=function(){
		var invite_email=document.getElementById("invite_email");
		var email = invite_email.value;
		if (email!=""){
			var access_level=document.getElementById("access_level").options[document.getElementById("access_level").selectedIndex].value;
			this.inviteUser(email,access_level);
		}
	};

	this.completeRemoval=function(o){
		this.completeInit(o);
		this.setFormDisabled(false);
	};

	this.removeUser=function (login,group){
		this.setFormDisabled(true);

		this.deleteCallback={
				success:this.completeRemoval,
				failure:this.inviteFailure,
            cache:false, // Turn off caching for IE
				scope:this
		};
		var post_url = serverRoot + "/REST/projects/" + this.pID + "/users/" + group;
        var showDeactivatedUsers=(document.getElementById('showDeactivatedUsersCheck').checked?'/true':'');
		YAHOO.util.Connect.asyncRequest('DELETE',post_url + "/" + login + showDeactivatedUsers + "?format=json&XNAT_CSRF="+csrfToken,this.deleteCallback,null,this);
	};

	this.userExists=function (email){
        // If the site is not configured to allow retrieving all users, just assume that the user is valid,
        // since we can't check against that list anyway.
		if (!window.userManager.retrieveAllUsers || window.userManager.allUserResultSet == undefined) {
            return true;
        }
		var all_users= window.userManager.allUserResultSet.ResultSet.Result;
		for(var user_count=0;user_count<all_users.length;user_count++){
			if(all_users[user_count].email==email || all_users[user_count].login==email){
				return true;
			}
		}
        return false;
    };

	this.displayError=function(errorMsg){
		var data_table = document.getElementById("user_table");
		data_table.className="error";
		data_table.innerHTML=errorMsg;
	};

	var selections =new Object();

    this.adjustInviteLists=function(user_name,access_level){
        if (user_name!=undefined && access_level!=undefined){
            // remove the user_name from any array it's currently part of
        	$(selections).each(function (i2,v2){
        		if(v2.contains!=undefined && v2.contains(user_name)){
                    var index = v2.indexOf(user_name);
                    v2.splice(index, 1);
        		}
        	});

            // add it to the array it should be in
            if(selections[access_level]==undefined){
            	selections[access_level]=new Array();
            }
            selections[access_level].push(user_name);
        }
    };

	this.createPopup=function(){
		this.popupLoader=prependLoader("user_list_header","Preparing user list");
		this.popupLoader.render();
		var popupDIV = document.createElement("DIV");
		popupDIV.id="all_users_popup";
		var popupHD = document.createElement("DIV");
		popupHD.className="hd";
		popupDIV.appendChild(popupHD);
		var popupBD = document.createElement("DIV");
		popupBD.className="bd";
		popupDIV.appendChild(popupBD);

		popupHD.innerHTML="Select User(s) and the desired level of access";

		var all_users_table = document.createElement("div");
		all_users_table.id="all_users_table";
		all_users_table.style.marginTop="5px";
		popupBD.appendChild(all_users_table);

		//add to page
		var tp_fm=document.getElementById("tp_fm");
		tp_fm.appendChild(popupDIV);

		this.allUsersPopup=new YAHOO.widget.Dialog(popupDIV,{zIndex:999,visible:false,width:"520px",fixedcenter:true});

		var handleCancel = function() {
			this.hide();
		};

        var handleSubmit = function() {
        	$.each(selections, function(i3,v3){
        		if(v3.length>0){
        			//if there are entries to submit, do the invitation
        			if(window.userManager.inviteUser(v3.toString(),i3)){
        				//clear the todo list
            			selections[i3]=new Array();
        			}
        		}
        	});
            this.hide();
            var popup = window.userManager.allUsersPopup;
            popup.allUsersDataTable = new YAHOO.widget.DataTable("all_users_table", allUserColumnDefs,popup.alluserDataSource,{scrollable:true,height:"300px",width:"500px"});
		};
		var myButtons = [ { text:"Submit", handler:handleSubmit, isDefault:true },
		                  { text:"Cancel", handler:handleCancel } ];
		this.allUsersPopup.cfg.queueProperty("buttons", myButtons);

        var allUserColumnDefs=[
            {key:"displayname",label:"Group",formatter:allUsersGroupDropdownFormatter},
            {key:"login",label:"Username",sortable:true},
            {key:"firstname",label:"Firstname",sortable:true},
            {key:"lastname",label:"Lastname",sortable:true},
            {key:"email",label:"Email",sortable:true}];

        //build all users datatable
		this.allUsersPopup.alluserDataSource = new YAHOO.util.DataSource(this.allUserResultSet.ResultSet.Result);
		this.allUsersPopup.alluserDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		this.allUsersPopup.alluserDataSource.responseSchema = {
				fields: ["login","firstname","lastname","email","displayname"]
		};
		this.allUsersPopup.allUsersDataTable = new YAHOO.widget.DataTable("all_users_table", allUserColumnDefs,this.allUsersPopup.alluserDataSource,{scrollable:true,height:"300px",width:"500px"});

		this.allUsersPopup.render();

		this.allUsersPopup.show();
		this.allUsersPopup.hide();

		document.getElementById("popup_all_users_button").disabled=false;
		this.popupLoader.close();
	};

	/**************
	 * Popup dialog box which allows existing users to be selected and added to project.
	 */
	this.popupAllUsersBox=function(){
        if (!this.retrieveAllUsers) {
            return;
        }
		if(this.allUsersPopup==undefined){
			this.createPopup();
		}

		document.getElementById("tp_fm").style.display="block";

		this.allUsersPopup.show();
	};

	this.init();
}


function DefaultAccessibilityManager(_dom,_pID){
	this.dom=_dom;
	this.pID=_pID;

	this.disableDOM=function(_val){
		if(_val){
			if(this.popupLoader==undefined){
				this.popupLoader=prependLoader("accessibility_save","Saving");
				this.popupLoader.render();
			}
			this.dom.style.color="#DEDEDE";
		}else{
			if(this.popupLoader!=undefined){
				this.popupLoader.close();
				this.popupLoader=null;
			}
			this.dom.style.color="";
		}
		document.getElementById("private_access").disabled=_val;
		document.getElementById("protected_access").disabled=_val;
		document.getElementById("public_access").disabled=_val;
		document.getElementById("accessibility_save").disabled=_val;
	};

	this.changeSuccess=function(o){
		this.disableDOM(false);
		document.getElementById('current_accessibility').value=this._level;
	};

	this.changeFailure=function(o){
		if(o.status==401){
            xmodal.message('Session Expired', sessExpMsg);
			window.location=serverRoot+"/app/template/Login.vm";
		}else{
			this.disableDOM(false);
            xmodal.message('User Management Error', 'Error ' + o.status + ': Change failed.');
		}
	};

	this.set=function(){
		if(document.getElementById("private_access").checked){
			this._level=document.getElementById("private_access").value;
		}else if(document.getElementById("protected_access").checked){
			this._level=document.getElementById("protected_access").value;
		}else if(document.getElementById("public_access").checked){
			this._level=document.getElementById("public_access").value;
		}
		this.accessibilityCallback={
				success:this.changeSuccess,
				failure:this.changeFailure,
            cache:false, // Turn off caching for IE
				scope:this
		};
		this.disableDOM(true);

		YAHOO.util.Connect.asyncRequest('PUT',serverRoot + "/REST/projects/" + this.pID + "/accessibility/" + this._level + '?XNAT_CSRF='+csrfToken,this.accessibilityCallback,null,this);
	};

}
