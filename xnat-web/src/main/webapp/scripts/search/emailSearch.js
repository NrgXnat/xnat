/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/emailSearch.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function EmailPopupForm(_search,_div){
	this._div=_div;
	this._search=_search;

	this.init=function(){
		try{
		var popupDIV = document.createElement("DIV");
		popupDIV.id="all_users_popup";
		var popupHD = document.createElement("DIV");
		popupHD.className="hd";
		popupDIV.appendChild(popupHD);
		var popupBD = document.createElement("DIV");
		popupBD.className="bd";
		popupDIV.appendChild(popupBD);

		popupHD.innerHTML="Send Search Results via Email";


		var saveOptions = new Object();
		saveOptions.zIndex=999;
		saveOptions.width="320";
		saveOptions.x=240;
		saveOptions.visible=true;
        saveOptions.fixedcenter=true;

		//add to page
		if(this._div!=undefined){
		//add to page
		if(this._div.id==undefined){
			var tp_fm=document.getElementById(this._div);
		}else{
			var tp_fm=this._div;
		}
		tp_fm.appendChild(popupDIV);
			}else{
				var tp_fm = document.getElementById("tp_fm");
				tp_fm.appendChild(popupDIV);
				saveOptions.modal=true;
			}

		this.emailPopupDialog=new YAHOO.widget.Dialog(popupDIV,saveOptions);

		var handleCancel = function() {
			this.hide();
		}

	    this.emailCallback={
			success:this.completeEmail,
			failure:this.emailFailure,
            cache:false, // Turn off caching for IE
			scope:this
        };

        this.emailPopupDialog.emailer=this;

		var handleSubmit = function() {
			var toAddresses = document.getElementById("email_toAddresses").value;
		    var subject = document.getElementById("email_subject").value;
		    var from = document.getElementById("email_from_address").value;
		    var message = document.getElementById("email_message").value;

	        this.emailer.emailMsgTab.innerHTML="<DIV style='color:red'>Sending...</DIV>";

	        var email_url = "remote-class=org.nrg.xdat.ajax.EmailCustomSearch&remote-method=send";
	        email_url +="&toAddress=" + toAddresses;
	        email_url +="&subject=" + subject;
	        email_url +="&message=" + encodeURIComponent(message);
	        email_url +="&from=" + from;
	        email_url +="&search_xml=" + encodeURIComponent(this.emailer._search);
	        email_url +="&XNAT_CSRF=" + csrfToken;

	        YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/servlet/AjaxServlet',this.emailer.emailCallback,email_url,this.emailer);
		}

        this.deleteOldInputs();

	    var myButtons = [ { text:"Submit", handler:handleSubmit, isDefault:true },
						  { text:"Cancel", handler:handleCancel } ];
		this.emailPopupDialog.cfg.queueProperty("buttons", myButtons);

		this.emailMsgTab=document.createElement("DIV");
		this.emailMsgTab.style.display='block';
		this.emailMsgTab.id='emailMsgTab';
		popupBD.appendChild(this.emailMsgTab);

		var table = document.createElement("TABLE");
		var tbody = document.createElement("TBODY");
		table.appendChild(tbody);

		var tr;
		var td;
		var th;
		var hr;

		var div1=document.createElement("DIV");
		div1.style.display='block';
		div1.innerHTML='A link to this search will be included in your message.';
		popupBD.appendChild(div1);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		hr = document.createElement("HR");
		hr.color="#DEDEDE";
		td.colspan="2";
		td.border="0";
		td.appendChild(hr);
		tr.appendChild(td);
		tbody.appendChild(tr);

        tr = document.createElement("TR");
		th= document.createElement("TD");
		th.align="left";
		th.border="0";
		th.colspan="2";
		th.innerHTML="Recipient's email address:";
		tr.appendChild(th);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.colspan="2";
		var input = document.createElement("INPUT");
		input.type="text";
		input.id="email_toAddresses";
		input.name="email_toAddresses";
		input.size="42";
		this.applyInputStyle(input);
		td.appendChild(input);
		td.border="0";
		tr.appendChild(td);
		tbody.appendChild(tr);

		if (user_email!=undefined && user_email!=""){
			var input = document.createElement("INPUT");
			input.type="hidden";
			input.name="email_from_address";
			input.id="email_from_address";
			input.value=user_email;
			this.applyInputStyle(input);
			td.colspan="2";
			td.border="0";
			td.appendChild(input);
		}else{
			tr = document.createElement("TR");
			th= document.createElement("TD");
			th.align="left";
			th.border="0";
			th.colspan="2";
			th.innerHTML="Sender's email address:";
			tr.appendChild(th);
			tbody.appendChild(tr);

			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.border="0";
			var input = document.createElement("INPUT");
			input.type="text";
			input.id="email_from_address";
			input.name="email_from_address";
			input.size="42";
			this.applyInputStyle(input);
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
		}

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.colspan="2";
		td.border="0";
		td.innerHTML="(Separate multiple email addresses with commas.)";
		tr.appendChild(td);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.colspan="2";
		td.border="0";
		hr = document.createElement("HR");
		hr.color="#DEDEDE";
		td.appendChild(hr);
		tr.appendChild(td);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		th= document.createElement("TD");
		th.align="left";
		th.colspan="2";
		th.border="0";
		th.innerHTML="Email subject message:";
		tr.appendChild(th);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.colspan="2";
		td.border="0";
		var input = document.createElement("INPUT");
		input.type="text";
		input.id="email_subject";
		input.name="subject";
		input.size="42";
		this.applyInputStyle(input);
		td.appendChild(input);
		tr.appendChild(td);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.border="0";
		td.colspan="2";
		hr = document.createElement("HR");
		hr.color="#DEDEDE";
		td.appendChild(hr);
		tr.appendChild(td);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		th= document.createElement("TD");
		th.border="0";
		th.align="left";
		th.colspan="2";
		th.innerHTML="Personal message:";
		tr.appendChild(th);
		tbody.appendChild(tr);

		tr = document.createElement("TR");
		td= document.createElement("TD");
		td.align="left";
		td.colspan="2";
		td.border="0";
		var input = document.createElement("textarea");
		input.id="email_message";
		input.name="message";
		input.rows="10";
		input.cols="42";
		this.applyInputStyle(input);
		td.appendChild(input);
		tr.appendChild(td);
		tbody.appendChild(tr);


		popupBD.appendChild(table);
		}catch(e){
            xModalMessage('Email Search Error', e.message);
			throw e;
		}
	}

	this.emailFailure=function(o){
		this.emailPopupDialog.emailMsgTab.innerHTML="<DIV style='color:red'>Error. Failed to send email.</DIV>";
	};

	this.completeEmail=function(o){
		this.emailPopupDialog.hide();
	};

	this.render=function(){
		this.emailPopupDialog.render();
	}

	this.applyInputStyle = function(e){
		e.style.fontSize = "99%";
	}

    this.deleteOldInputs = function(){
        if(document.getElementById("email_toAddresses")!=null){
            document.getElementById("email_toAddresses").parentNode.removeChild( document.getElementById("email_toAddresses"));
        }
        if(document.getElementById("email_subject")!=null){
            document.getElementById("email_subject").parentNode.removeChild(document.getElementById("email_subject"));
        }
        if(document.getElementById("email_from_address")!=null){
            document.getElementById("email_from_address").parentNode.removeChild(document.getElementById("email_from_address"));
        }
        if(document.getElementById("email_message")!=null){
            document.getElementById("email_message").parentNode.removeChild(document.getElementById("email_message"));
        }
    }
}
