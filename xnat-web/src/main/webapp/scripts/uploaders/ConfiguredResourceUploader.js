//Copyright 2014 Washington University
//Author: Tim Olsen <tim@deck5consulting.com>

/* 
 * resource dialog is used to upload resources at any level
 */
if(XNAT.app.crConfigs==undefined){
XNAT.app.crConfigs={
	load:function(){
		
		YAHOO.util.Connect.asyncRequest('GET', serverRoot+'/data/projects/' + XNAT.app.crConfigs.project +'/config/resource_config/script?format=json', {success : XNAT.app.crConfigs.handleLoad, failure : function(){}, cache : false, scope : XNAT.app.crConfigs});
	},
	handleLoad:function(obj){
		var parsedResponse = YAHOO.lang.JSON.parse(obj.responseText);
	    var script = "";
	    if (parsedResponse.ResultSet.Result.length !== 0) {
	    	script = parsedResponse.ResultSet.Result[0].script;
			//sort of a hack to get this code to work with generic nrg_config return values
			if(script == undefined ){
				script = parsedResponse.ResultSet.Result[0].contents;
			}
			
			this.configs=YAHOO.lang.JSON.parse(script);
			
			this.showLinks();
		}
	},
	showLinks:function(){
		$("a.uploadLink").each(function(value){
			var type=$(this).attr('data-type');
			var tempConfigs=new Array();

			var props=$(this).attr('data-props');
			
			var tempConfigs=XNAT.app.crConfigs.getAllConfigsByType(type,props)			
			
			if(tempConfigs.length>0){
				if(value.dontHide){
					$(value).color(value.defaultColor);
					$(value).css('cursor:pointer');
				}
				
				$(this).click(function(){
					XNAT.app.crUploader.show(this);
					return false;
				});
				$(this).show();
			}else{
				if(!value.dontHide){
					$(this).hide();
				}
			}
		});
	},
	getConfigsByType:function(type){
		var temp=new Array();
		jq.each(this.configs,function(i1,v1){
			if(v1.type==type){
				temp.push(v1);
			}
		});
		return temp;
	},
	getAllConfigsByType:function(type, props){
		var tmpConfigs=this.getConfigsByType(type);
		
		var typeInfo=window.available_elements.getByName(type);
		if(typeInfo!=undefined){
			if(typeInfo.isSubjectAssessor){
				tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:subjectAssessorData"));
			}
			if(typeInfo.isImageAssessor){
				tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageAssessorData"));
			}
			if(typeInfo.isImageSession){
				tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageSessionData"));
			}
			if(typeInfo.isImageScan){
				tmpConfigs=tmpConfigs.concat(this.getConfigsByType("xnat:imageScanData"));
			}
		}
		
		var tempConfigs2=new Array();
		
		//allow filtering of links
		jq.each(tmpConfigs,function(i1,v1){
			if(props!=undefined && props!=null && v1.filter){
				var filters=v1.filter.split(",");
				var matched=false;
				jq.each(filters,function (i2,v2){
					if(!matched){
						if((v2.trim()==props.trim())){
							matched=true;
						}
					}
				});
				if(matched){
					tempConfigs2.push(v1);
				}
			}else{
				tempConfigs2.push(v1);
			}
		});
		
		return tempConfigs2;
	}
}


YAHOO.util.Event.onDOMReady(XNAT.app.crConfigs.load);

XNAT.app.crUploader={
	ready:false,
	show:function(config){
		this.project=XNAT.app.crConfigs.project;
		this.type=$(config).attr('data-type');
		this.uri=$(config).attr('data-uri');
		this.props=$(config).attr('data-props');
		
		this.updateForm();
		
		XNAT.app.crUploader.dialog.render(document.body);
		XNAT.app.crUploader.dialog.show();
	},
	updateForm:function(obj){
		var configs=XNAT.app.crConfigs.getAllConfigsByType(this.type,this.props);
		
		$('#cruSel').html("");
		
		$('#cruSel').append($("<option value=''>SELECT</option>"));
		
		//render select options
		$.each(configs,function(index,value){
			$('#cruSel').append($("<option value='" + value.name +"' data-message='" + ((value.description)?value.description:"") +"' data-level='" + ((value.level)?value.level:"") +"' data-overwrite='" + ((value.overwrite)?value.overwrite:"") +"' data-label='" + value.label +"' data-subdir='" + value.subdir +"'>" + value.name +"</option>"));
		});
	
		if(this.registered==undefined){
			this.registered=true;
			
			//add onchange event
			$('#cruSel').change(function(){
				if($(this).val()!=""){
					var desc=$("#cruSel option:selected").attr("data-message");
					if(desc!=null && desc!=undefined){
						$('#cruMsg').html(desc);
					}else{
						$('#cruMsg').html("");
					}
				}else{
					$('#cruMsg').html("");
				}
			});
		}
	},
	doUpload:function(allowOverwrite){
		//executes the selected upload operation
		var frm=document.getElementById("cru_upload_frm");
		if($('#cruSel').val()==""){
			showMessage("page_body","Select resource","Please select a resource to upload.");
			return;
		}
		if(requireReason && frm.event_reason.value==""){
			showMessage("page_body","Include justification.","Please include a justification for this upload.");
			return;
		}
		if(frm.upload_file.value==""){
			showMessage("page_body","Select file.","Please use the file selector to choose a file to upload.");
			return;
		}		
		YAHOO.util.Connect.setForm(frm,true);
		
		var callback={
			upload:function(obj1){		
				closeModalPanel("cru_upl_mdl");
				this.handleUpload(obj1);
			},
			scope:this
		}
		
		var selector=$("#cruSel option:selected");
		this.overwrite=selector.attr("data-overwrite");
		this.level=selector.attr("data-level");

		var params="";		
		params+="&event_type=WEB_FORM";
		params+="&event_action=Uploaded "+ $(selector).text();
		params+="&extract=true";
		if(showReason && frm.event_reason.value!=""){
			params+="&event_reason="+frm.event_reason.value;
		}else{
			params+="&event_reason=standard upload";
		}		
		
		openModalPanel("cru_upl_mdl","Uploading Files");
		
		var subdir=$(selector).attr('data-subdir');
		if(subdir!=null && subdir!=undefined){
			if(!subdir.startsWith("/")){
				subdir="/"+subdir;
			}
	
			if(!subdir.endsWith("/")){
				subdir=subdir+"/";
			}
		}else{
			subdir="";
		}
		
		var filepath=frm.upload_file.value;
		while(filepath.indexOf("\\")>-1){
			filepath=filepath.substring(filepath.indexOf("\\")+1);
		}
		
		filepath=subdir+filepath;
		
		if(allowOverwrite){
			params+="&overwrite=true";
		}
		
		var lvl="";
		if(this.level!='default' && this.level!='' && this.level!=undefined){
			lvl="/" + this.level;
		}
		
		YAHOO.util.Connect.asyncRequest('POST',this.uri + lvl  +"/resources/"+ $(selector).attr('data-label') +"/files"+filepath+"?XNAT_CSRF=" + csrfToken +params,callback);
	},
	handleUpload:function(response,o2,o3){
		//handles the response form the upload operation
		//because this is a file upload, both successes and failures will use this method
		if(response.responseText==undefined || response.responseText=="" || response.responseText.match(/^<pre.*?><\/pre>$/)){
			showMessage("page_body","Upload successful.","Your files have been successfully uploaded.");
			document.getElementById("cru_upload_frm").upload_file.value="";
			if(window.viewer!=undefined && window.viewer.loading>0){
				window.viewer.refreshCatalogs();
			}
		}else if(response.responseText!=undefined && (response.responseText.indexOf("File already exists")>-1 || response.responseText.indexOf("duplicates")>-1)){
			if(this.overwrite){
				this.confirm("Duplicate files", "The uploaded files already exist on the server.  Would you like to overwrite those files?",function(){
					//yes
					this.hide();
					XNAT.app.crUploader.doUpload(true);
				},function(){
					//no
					this.hide();
					XNAT.app.crUploader.dialog.hide();					
				});
			}else{
				showMessage("page_body","Failed upload.","The selected files already exist for this session.");
			}
		}else if(response.responseText==undefined){
			//this block is for IE.  For some reason the responseText is coming through as undefined.
			showMessage("page_body","Failed upload.","Unable to upload selected files.");
		}else{
			showMessage("page_body","Failed upload.",response.responseText);
		}
		
		this.dialog.hide();
	},
	confirm : function (header, msg, handleYes, handleNo) {
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
		dialog.setHeader(header);
		dialog.setBody(msg);
		dialog.cfg.queueProperty('icon', YAHOO.widget.SimpleDialog.ICON_HELP);
		dialog.cfg.queueProperty('zIndex', 9998);
		dialog.render(document.body);
		dialog.show();
		return dialog;
	}
}


var tmpUploadFrm='<div id="cru_dialog" style="visibility:hidden">';
tmpUploadFrm+='	   <div class="hd">Upload Files</div>';
tmpUploadFrm+='    <div class="bd" style="">';
tmpUploadFrm+='		<div class="cru_a">';
tmpUploadFrm+='			<form id="cru_upload_frm">';
tmpUploadFrm+='				<div>Select destination directory for uploaded files:</div>';
tmpUploadFrm+='				<div><select id="cruSel"><option value="">SELECT</option></select></div>';
tmpUploadFrm+='				<div id="cruMsg" style="padding:4px;"></div>';
tmpUploadFrm+='				<div style="margin-top:12px;margin-bottom:16px;">Local File: <input type="file" id="cru_upload_file" name="upload_file"/></div>';
if(showReason){
	tmpUploadFrm+='				<div style="margin-bottom:16px;">Justification:<br><textarea id="cru_event_reason" name="event_reason" cols="50" rows="3"></textarea></div>';
}
tmpUploadFrm+='			</form>';
tmpUploadFrm+='		</div>';
tmpUploadFrm+='	</div> ';
tmpUploadFrm+='</div> ';
$("body").append(tmpUploadFrm);

//initialize modal upload dialog
XNAT.app.crUploader.dialog=new YAHOO.widget.Dialog("cru_dialog", { fixedcenter:true, visible:false, width:"500px", height:"300px", modal:true, close:true, draggable:true } ),
XNAT.app.crUploader.dialog.cfg.queueProperty("buttons", [{ text:"Cancel", handler:{fn:function(){XNAT.app.crUploader.dialog.hide();}}},{ text:"Upload", handler:{fn:function(){XNAT.app.crUploader.doUpload();}}, isDefault:true}]);
}