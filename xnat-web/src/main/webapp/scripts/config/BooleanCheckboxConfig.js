/*
 * web: BooleanCheckboxConfig.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//function BooleanCheckboxConfig(checkbox_id, url, allowSiteDefault, _default){
//	this.url=(url.startsWith(serverRoot))?url:serverRoot+url;
//	this.id=checkbox_id;
//	this.allowSiteDefault=(allowSiteDefault==undefined)?false:true;
//	this._default=(_default==undefined)?false:_default;
//
//	this.init=function(){
//		var that=this;
//		$.get(this.url+"?contents=true"+((this.allowSiteDefault)?"&defaultToSiteWide=true":""))
//			.done(function(data){
//				if(data=="true"){
//					$(that.id).attr('checked',true);
//				}else{
//					$(that.id).attr('checked',false);
//				}
//				$(that.id).attr('disabled',false);
//			})
//			.fail(function(data){
//				$(that.id).attr('checked',that._default);
//				$(that.id).attr('disabled',false);
//			});
//	};
//
//	this.toggle=function(){
//		$(this.id).attr('disabled',true);
//		var that=this;
//		$.ajax({
//		    url: this.url+"?inbody=true&XNAT_CSRF=" + window.csrfToken,
//		    type: 'PUT',
//		    data: ""+($(this.id).attr('checked')=="checked"),
//		    contentType:"text/plain"
//		  }).done(function(){
//				$(that.id).attr('disabled',false);
//		  }).fail(function(){
//			  $(that.id).attr('checked',!($(that.id).attr('checked')=="checked"));
//			  $(that.id).attr('disabled',false);
//			  showMessage("page_body","Failure","Failed to modify setting.");
//		  });
//	};
//}
