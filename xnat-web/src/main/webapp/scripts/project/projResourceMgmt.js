//Copyright 2014 Washington University
//Author: Tim Olsen <tim@deck5consulting.com>

/* 
 * javascript for ResourceManagement.  Used to configure the expected resources for a project
 */
XNAT.app.pResources={
	configs:new Array(),
	settingsDialog:new YAHOO.widget.Dialog("pResource_settings_dialog", { fixedcenter:true, visible:false, width:"800px", height:"660px", modal:true, close:true, draggable:true,resizable:true}),
	begin:function(){
		this.load();

		this.settingsDialog.render(document.body);
		this.settingsDialog.show();
	},
	reset:function(){
		$("#pResource_form").html("");
		$("#pResource_form").hide();
		$("#pResource_exist").html("");
		$("#pResource_exist").height(430);
	},
	menu:function(level){
		$("#pResource_form").html("");
		var temp_html="<div class='colA'><div class='info simple'>What resource are you requiring?</div>" +
				"<div class='row'><div class='rowTitle' for='pResource.name'>Title</div> <input class='pResourceField' required='true' data-required-msg='<b>Title</b> field is required.' data-prop-name='name' type='text' id='pResource.name' value='' placeholder='Natural Language Title'/></div>" +
				"<div class='row'><div class='rowTitle' for='pResource.desc'>Description (optional)</div> <textarea class='pResourceField' data-prop-name='description' id='pResource.desc' placeholder='' /></div>" +
				"</div>";
		temp_html+="<div class='colB'><div class='info simple'>Where will it be stored?</div>";
		if(level=="proj"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:projectData'/>";
		}else if(level=="subj"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:subjectData'/>";
		}else if(level=="sa"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
					"<option value='xnat:subjectAssessorData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isSubjectAssessor && !value.isImageSession){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
		}else if(level=="is"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
			"<option value='xnat:imageSessionData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isImageSession){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
		}else if(level=="scan"){
			temp_html+="<input class='pResourceField' data-prop-name='type' type='hidden' id='pResource.type' value='xnat:imageScanData'/>";
			temp_html+="<div class='row'><div class='rowTitle' for='pResource.filter'>Types (optional)</div> <input type='text' id='pResource.filter' class='pResourceField' data-prop-name='filter' placeholder='TYPE1,TYPE3,TYPE4'>";
			temp_html+="</div>";
		}else if(level=="ia"){
			temp_html+=" <div class='row'><div class='rowTitle' for='pResource.type'>Select data-type </div> <select id='pResource.type' class='pResourceField' data-prop-name='type'>" +
					"<option value='xnat:imageAssessorData'>All</option>";
			$.each(window.available_elements,function( index, value ) {
				if(value.isImageAssessor){
					temp_html+="<option value='" + value.element_name + "'>"+ value.singular+"</option>";
				}
			});
			temp_html+="</select></div>";
			temp_html+="<div class='row'><div class='rowTitle' for='pResource.level'>Level: </div> <select id='pResource.level' class='pResourceField' data-prop-name='level'>";
			temp_html+="<option value='default'>DEFAULT (resources)</option>";
			temp_html+="<option value='out'>outputs dir (out)</option>";
			temp_html+="<option value='in'>inputs dir (in)</option>";
			temp_html+="</select></div>";
		}
		temp_html+=" <div class='row'><div class='rowTitle' for='pResource.label'>Resource Folder</div> <input class='pResourceField' required='true' data-required-msg='<b>Resource Folder</b> is required.' data-prop-name='label' size='10' type='text' id='pResource.label' required=true placeholder='ex. DICOM' data-regex='^[a-zA-Z0-9_-]+$' /></div>";
		temp_html+=" <div class='row'><div class='rowTitle' for='pResource.subdir'>Sub-folder (optional)</div> <input class='pResourceField' data-prop-name='subdir' type='text' id='pResource.subdir' placeholder='(optional) ex. data/sub/dir' size='24' data-regex='^[a-zA-Z0-9_\\-\\/]+$'/></div>";
		temp_html+=" <div class='row'><div class='rowTitle'>&nbsp;</div><input class='pResourceField' style='width:10px;' data-prop-name='overwrite' type='checkbox' id='pResource.overwrite'/> <label for='pResource.overwrite'>Allow overwrite</label></div>";
		temp_html+=" </div>";
		temp_html+=" <div style='clear:both;'></div>";
		temp_html+=" <div class='row3'><button id='cruCancelBut' onclick='$(\"#pResource_form\").html(\"\");$(\"#pResource_form\").hide();$(\"#pResource_exist\").height(430)'>Cancel</button><button class='default' id='cruAddBut' onclick='XNAT.app.pResources.add();'>Add</button></div>";
		temp_html+=" <div class='row4' style=''></div>";
		$("#pResource_form").html(temp_html);
		$("#pResource_form").show();
		$("#pResource_exist").height(430-$("#pResource_form").height());
	},
	add:function(){		
		var valid=true;
		
		var props=new Object();
		
		//iterate over the fields in the form
		$(".pResourceField").each(function(){
			var tmpValue=$(this).val();
			if($(this).attr('required')=='required' && tmpValue==""){
				if($(this).attr('data-required-msg'))
				{
					xModalMessage("Required field",$(this).attr('data-required-msg'));
				}else{
					xModalMessage("Required field", "<b>" + $(this).attr('data-prop-name') + "</b> is required.");
				}
				valid=false;
			}
			
			//check if this form field has a regex defined
			if($(this).attr('data-regex') !=undefined && tmpValue!=""){
				if(! (new RegExp($(this).attr('data-regex'))).test(tmpValue)){
					if($(this).attr('data-regex-msg'))
					{
						xModalMessage("Invalid value",$(this).attr('data-regex-msg'));
					}else{
						xModalMessage("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> has an invalid character.");
					}
					valid=false;
				}
			}
			
			if(tmpValue!="" && (tmpValue.indexOf("'")>-1 || tmpValue.indexOf("\"")>-1)){
				xModalMessage("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> has an invalid character (quote).");
				valid=false;
			}
			
			if(tmpValue!="" && (tmpValue.length>255)){
				xModalMessage("Invalid value", "<b>" + $(this).attr('data-prop-name') + "</b> exceeds size limits.");
				valid=false;
			}
			
			if($(this).attr('type')=="checkbox"){
				props[$(this).attr('data-prop-name')]=$(this).is(':checked');
			}else{
				props[$(this).attr('data-prop-name')]=tmpValue;
			}
		});
		
		if(valid){
			this.configs.push(props);

			this.save();
		}
	},
	save:function(){
		openModalPanel("saveResource","Saving resource configurations.");
		YAHOO.util.Connect.asyncRequest('PUT',serverRoot+"/data/projects/" + this.id +"/config/resource_config/script?inbody=true&XNAT_CSRF=" + window.csrfToken,
        {	
        	success : function()
        	{
        		closeModalPanel('saveResource');
        		XNAT.app.pResources.load();
        	},
        	 failure: function(){
        	 	closeModalPanel('saveResource');
        	 	xModalMessage("Exception","Failed to store configuration.");
        	 },
             cache:false, // Turn off caching for IE
        	 scope: this
        },
         YAHOO.lang.JSON.stringify(this.configs));
	},
	load:function(){
		this.reset();
		
		YAHOO.util.Connect.asyncRequest('GET', serverRoot+'/data/projects/' + this.id +'/config/resource_config/script?format=json', {success : this.handleLoad, failure : this.render, cache : false, scope : this});
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
		}
	    this.render();
	},
	render:function(){
		//identify columns
		if(this.configs!=undefined && this.configs.length>0){
			var tmpHtml="<dl class='header'><dl><dd class='col1'>&nbsp;</dd><dd class='colL col2'>Type</dd><dd class='colM col3'>Name</dd><dd class='colM col4'>Label</dd><dd class='colL col5'>Sub-directory</dd><dd class='colM col6'>Overwrite?</dd><dd class='colS col7'>Options</dd></dl></dl>	";
			jq.each(this.configs,function(i1,v1){
				var elementName=window.available_elements.getByName(v1.type);
				if(elementName!=undefined && elementName.singular!=undefined){
					elementName=elementName.singular;
				}else{
					elementName=v1.type;
				}
				tmpHtml+="<dl class='item'><dd class='col1'><button onclick='XNAT.app.pResources.remove(\"" + i1 +"\");'>Remove</button></dd><dd class='colL col2'>"+ elementName +"</dd><dd class='colM col3'>"+v1.name +"</dd><dd class='colM col4'>"+v1.label +"</dd><dd class='colL col5'>"+v1.subdir +"&nbsp;</dd><dd class='colM col6'>"+((v1.overwrite)?v1.overwrite:"false") +"</dd><dd class='colS col7'>";
				if(v1.level){
					tmpHtml+="Level:"+v1.level;
				}
				if(v1.filter){
					tmpHtml+=v1.filter;
				}
				tmpHtml+="</dd>";
				if(v1.description){
					tmpHtml+="<dd class='colX'><b>Description:</b> "+v1.description +"</dd></dl>";
				}else{
					tmpHtml+="</dl>";
				}
			});
		}else{
			var tmpHtml="<div style='color:grey;font-style:italic;'>None</div>";
		}
		$("#pResource_exist").html(tmpHtml);
	},
	remove:function(index){
		this.configs.splice(index,1);
		this.save();
	}
}
//implements button functionaly
XNAT.app.pResources.settingsDialog.cfg.queueProperty("buttons", [
   { text:"Close", handler:{fn:function(){
	   	XNAT.app.pResources.reset();
	   	XNAT.app.pResources.settingsDialog.hide();
   }},isDefault:true}
   ]);