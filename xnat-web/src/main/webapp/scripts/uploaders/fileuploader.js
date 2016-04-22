
/**
 * Copyright 2015 Washington University
 * File uploader
 * Author:  Mike Hodge (hodgem@wustl.edu)
 */

var abu = abu || {};

abu.initializeUploader = function(initarr){
	abu._fileUploader = new abu.FileUploader(initarr);
};

abu.FileUploader = function(o){

	this._options = o;
	// NOTE:  Multiple concurrent cache uploads works fine, but multiple concurrent uploads to a resource often causes failures and can corrupt the catalog.
	// Leave this set to 1 if the uploader supports uploading directly to resources.
	this.MAX_CONCURRENT_UPLOADS = 1;
	this.ALLOW_DRAG_AND_DROP = true;
	this.DRAG_AND_DROP_ON = true;
	this.uploadsInProgress = 0;
	this.uploadsStarted = 0;
	$(this._options.element).html(""); 

	this.buildUploaderDiv = function() {
		$(this._options.element).append(
			'<div class="abu-uploader">' +
				'<div id="abu-files-processing" class="abu-files-processing">        Processing...... </div>' +
				'<a id="file-uploader-instructions-sel" class="abu-uploader-instructions-sel" onclick="abu._fileUploader.uploaderHelp()">?</a>' +
				'<div class="abu-upload-drop-area" style="display: none;"><span>Drop files here to upload</span></div>' +
				'<div class="abu-xnat-interactivity-area">' +
				'</div>' +
			'<div id="abu-upload-button" class="abu-upload-button" style="position: relative; overflow: hidden; direction: ltr;">' + 
				'Upload files<input multiple="multiple" type="file" id="file-upload-input" class="abu-button-input">' + 
			'</div>' +
			'<div id="abu-done-button" class="abu-done-button" style="position: relative; overflow: hidden; direction: ltr;">' + 
				'<span id="abu-done-button-text">Cancel</span><input type="image" name="done" class="abu-button-input" style="width:105px">' +
			'</div>' +
			'<div id="abu-process-button" class="abu-process-button " style="position: relative; overflow: hidden; direction: ltr;">' +
				'<span id="abu-process-button-text">Process Files</span>' +
				'<input type="image" name="process" class="abu-button-input" style="width:105px">' +
			'</div>' +
			'<div class="abu-options-div">' +
			((this._options.showExtractOption) ?
				'<div class="abu-options-cb" title = "Extract compressed files on upload (zip, tar, gz)"?>' +
					'<input id="extractRequestBox" type="checkbox" value="1" checked="checked">' +
					'Extract compressed files' +
				'</div>' : 
				'<div class="abu-extract-zip"><input id="extractRequestBox" type="hidden" value="1"/></div>'
			) +
			((this._options.showCloseOption) ?
				'<div class="abu-options-cb" title = "Close window upon submit and send e-mail upon completion">' +
					'<input id="closeBox" type="checkbox" value="1">' +
					'Close window upon submit' +
				'</div>' : "" 
			) +
			((this._options.showEmailOption) ?
				'<div class="abu-options-cb" title = "Send e-mail upon completion">' +
					'<input id="emailBox" type="checkbox" value="1">' +
					'Send e-mail upon completion' +
				'</div>' : "" 
			) +
			((this._options.showUpdateOption) ?
				'<div class="abu-options-cb" title = "Update existing records?">' +
					'<input id="updateBox" type="checkbox" value="1">' +
					'Update existing records?' +
				'</div>' : "" 
			) +
			((this._options.showVerboseOption) ?
				'<div class="abu-options-cb" title = "Verbose status output?">' +
					'<input id="verboseBox" type="checkbox" value="1"' +
					'Verbose status output?' +
				'</div>' : "" 
			) +
			'</div><br>' +
			'<div class="abu-list-area"><ul class="abu-upload-list"></ul>' +
				'<div class="response_text" style="display:none"></div>' +
			'</div> ' 
		); 
		$("#abu-upload-button").mouseenter(function() { $(this).addClass("abu-upload-button-hover"); });
		$("#abu-upload-button").mouseleave(function() { $(this).removeClass("abu-upload-button-hover"); });
		$("#abu-done-button").click(this._options.doneFunction);
		$("#abu-done-button").mouseenter(function() { $(this).addClass("abu-done-button-hover"); });
		$("#abu-done-button").mouseleave(function() { $(this).removeClass("abu-done-button-hover"); });
		$("#abu-process-button").click(this._options.processFunction);
		$("#abu-process-button").mouseenter(function() { $(this).addClass("abu-process-button-hover"); });
		$("#abu-process-button").mouseleave(function() { $(this).removeClass("abu-process-button-hover"); });
		$('#closeBox').change(function(){ 
			if ($('#closeBox').is(':checked')) { 
				$('#emailBox').prop('checked', true);
				$('#emailBox').attr('disabled', true);
			} else {
				$('#emailBox').attr('disabled', false);
			}
		 });

		if (this.ALLOW_DRAG_AND_DROP) {
			$(".abu-upload-drop-area").on('dragover',function(e) {
					if (this.DRAG_AND_DROP_ON) {
						this.activateUploadArea(e);
					}
				}.bind(this)
			);
			$(".abu-upload-drop-area").on('dragenter',function(e) {
					if (this.DRAG_AND_DROP_ON) {
						this.activateUploadArea(e);
					}
				}.bind(this)
			);
			$(".abu-upload-drop-area").on('drop',function(e) {
					$(".abu-upload-drop-area").css('display','none');
					$(".abu-upload-drop-area").removeClass('abu-upload-drop-area-active');
					if(e.originalEvent.dataTransfer){
						if(e.originalEvent.dataTransfer.files.length) {
							e.preventDefault();
							e.stopPropagation();
							this.doFileUpload(e.originalEvent.dataTransfer.files);
						}   
					}
				}.bind(this)
			);
			$(this._options.element).on('dragover',function(e) {
					if (this.DRAG_AND_DROP_ON) {
						this.activateUploadArea(e);
					}
				}.bind(this)
			).bind(this);
			$(this._options.element).on('dragenter',function(e) {
					if (this.DRAG_AND_DROP_ON) {
						this.activateUploadArea(e);
					}
				}.bind(this)
			);
		}	
		$("#file-upload-input").change(function(eventData) {
			this._options.uploadStartedFunction();
			var fileA = eventData.target.files;
			if (typeof eventData.target.files !== 'undefined') {
				var fileA = eventData.target.files;
				this.doFileUpload(fileA);
			}
		}.bind(this));
	}

	this.processingComplete = function() {
		$("#abu-done-button-text").html("Done");
		//$("#abu-process-button").css("display","None");
		//$("#abu-upload-button").css("display","None");
		$("#abu-process-button").addClass("abu-button-disabled");
		$("#abu-upload-button").addClass("abu-button-disabled");
		$("#abu-files-processing").css("display","None");
	}

	this.doFileUpload = function(fileA) {
		var start_i = $('form[id^=file-upload-form-]').length;				
		for (var i=0; i<fileA.length; i++) {
			var cFile = fileA[i];
			var adj_i = i + start_i;
			$(".abu-upload-list").append(
				'<form id="file-upload-form-' + adj_i + '" action="' + this._currentAction.replace("##FILENAME_REPLACE##",cFile.name) +
					 (($("#extractRequestBox").length>0) ? (($("#extractRequestBox").is(':checked')) ? "&extract=true" : "&extract=false") : "") +
					 (($("#emailBox").length>0) ? (($("#emailBox").is(':checked')) ? "&sendemail=true" : "&sendemail=false") : "") +
					 (($("#verboseBox").length>0) ? (($("#verboseBox").is(':checked')) ? "&verbose=true" : "&verbose=false") : "") +
					 (($("#updateBox").length>0) ? (($("#updateBox").is(':checked')) ? "&update=true" : "&update=false") : "") +
					 '" method="POST" enctype="multipart/form-data">' + 
				'</form>' + 
				'<div id="file-info-div-' + adj_i + '"><span class="abu-upload-file">' + cFile.name + '</span><span class="abu-upload-file">' +  
					" (" + ((typeof cFile.type !== 'undefined' && cFile.type !== '') ? cFile.type + ", " : '') +
					 this.bytesToSize(cFile.size) + ") </span>" +  
					'<div class="abu-progress">' +
						'<div class="abu-bar"></div >' +
						'<div class="abu-percent">0%</div >' +
					'</div>' +
					'<div id="upload-status-div-' + adj_i + '" class="abu-status"></div>' +
				'</div>');
			var formData = new FormData();
			formData.append("file" + adj_i,cFile,cFile.name);
			this.uploadFile("#file-upload-form-" + adj_i,formData);
			this.manageUploads();
		}
	}.bind(this)

	this.bytesToSize = function(bytes) {
	   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
	   if (bytes == 0) return '0 Byte';
	   var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
	   return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
	}

	this.activateUploadArea = function(e) {
				$(".abu-upload-drop-area").css('display','inline-block');
				$(".abu-upload-drop-area").addClass('abu-upload-drop-area-active');
				try { 
					e.preventDefault();
					e.stopPropogation();
				} catch(e) { /* Do nothing */ }
	}

	this.uploadFile = function(formSelector,formData) {
		var infoSelector = formSelector.replace("-upload-form-","-info-div-");
		var bar = $(infoSelector).find(".abu-bar");
		var percent = $(infoSelector).find(".abu-percent");
		var status = $(infoSelector).find(".abu-status");
		$(formSelector).on("submit",function(e, uploader) {
			 $(this).ajaxSubmit({
				beforeSend: function(arr, $form, options) {
					$form.data = formData;
					$form.processData=false;
					$form.contentType=false;
					status.empty();
					var percentVal = '0%';
					bar.width(percentVal)
					percent.html(percentVal);
					uploader.uploadsInProgress++;
					uploader.uploadsStarted++;
				},
				uploadProgress: function(event, position, total, percentComplete) {
					var percentVal = percentComplete + '%';
					bar.width(percentVal)
					percent.html(percentVal);
				},
				error: function(result) {
					$(status).data("rtn",result);
			 		status.html('<a href="javascript:abu._fileUploader.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-fail">Failed</a>');
			 		status.css("display","inline-block");
			 		$(infoSelector).find(".abu-progress").css("display","none");
					$("#abu-done-button").removeClass("abu-done-button-disabled");
				},
				success: function(result) {
					$(status).data("rtn",result);
					var percentVal = '100%';
					bar.width(percentVal)
					percent.html(percentVal);
					// Don't create results link if we're just returning the build path
					// check for duplicates
					var isDuplicate = false;
					try {
						var resultObj = JSON.parse(result);
						if (typeof resultObj.duplicates !== 'undefined' && resultObj.duplicates.length==1) {
							isDuplicate = true;
						} 
					} catch(e) {
						// Do nothing for now
					} 
					if (!isDuplicate) {
						if (typeof result.status !== 'undefined' || result.length > 150) {
				 			status.html('<a href="javascript:abu._fileUploader.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-complete abu-upload-complete-text">Upload complete</a>');
						} else {
				 			status.html('<span class="abu-upload-complete abu-upload-complete-text">Upload complete</span>');
						}
					} else {
			 			status.html('<a href="javascript:abu._fileUploader.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-fail">Duplicate file and overwrite=false.  Not uploaded.</a>');
					}
			 		status.css("display","inline-block");
			 		$(infoSelector).find(".abu-progress").css("display","none");
				},
				complete: function(xhr) {
					uploader.uploadsInProgress--;
					if (uploader.uploadsInProgress==0) {
						uploader._options.uploadCompletedFunction();
					}
					uploader.manageUploads();
				}
			}); 
			return false;
		}); 
	}

	this.showReturnedText = function(ele) {
		var eleData = $('#' + ele).data('rtn');
		xModalMessage("Server Response",((typeof eleData.status !=='undefined') ? "<h3>RETURN CODE: " + eleData.status + " (" + eleData.statusText + ")</h3><br>" +
				 eleData.responseText : eleData), undefined, {height:"400px",width:"800px"});
	}

	this.manageUploads = function() {
		var MAX_CONCURRENT_UPLOADS = this.MAX_CONCURRENT_UPLOADS;
		for (var i=1;i<=this.MAX_CONCURRENT_UPLOADS;i++) {
			var uploadsRequested = $('form[id^=file-upload-form-]').length;				
			var uploadsInProgress = this.uploadsInProgress;
			var uploadsStarted = this.uploadsStarted;
			if (uploadsInProgress < MAX_CONCURRENT_UPLOADS && uploadsStarted<uploadsRequested) {
				$("#file-upload-form-" + uploadsStarted).trigger("submit",this);
			} else if (uploadsStarted>=uploadsRequested) {
				break;
			} 
		}
	}.bind(this)

	this.uploaderHelp=function() {
		var templateV=
			'<div id="file-uploader-instructions" class="abu-uploader-instructions">' + 
			'<h3>Instructions</h3>' + 
			'<ul>' +  
			'<li>To upload, click the <b>Upload Files</b> button or drag files into the space below the buttons. (Drag-and-drop is supported in FF, Chrome.)</li>' +
			((this._options.maxFiles == 1) ?
				'<li>This uploader supports only a single file upload</li>' :
				'<li>Multiple files may be selected</li>'
			) +
			'<li>Uploads will begin automatically</li>' + 
			'<li>Upload of directories is not supported</li>' + 
			'<li>When finished uploading, press <b>Process Files</b> to process the uploaded files</li>' + 
			'</ul>' + 
			'</div>';
		xModalMessage("Uploader Instructions",templateV, undefined, {height:"400px",width:"800px"});
	}.bind(this)

}

