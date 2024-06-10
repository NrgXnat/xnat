/*
 * web: fileuploader.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

var abu = abu || {};

abu.initializeUploader = function(initarr){
	abu._fileUploader = new abu.FileUploader(initarr);
};

abu.initializeImageUploader = function(initarr){
	abu._imageUploader = new abu.FileUploader(initarr);
};

abu.showReturnedText = function(ele) {
	var eleData = $('#' + ele).data('rtn');
	xmodal.message("Server Response",((typeof eleData.status !=='undefined') ? "<h3>RETURN CODE: " + eleData.status + " (" + eleData.statusText + ")</h3><br>" +
		eleData.responseText : eleData), undefined, {height:"400px",width:"800px"});
}

abu.FileUploader = function(o){

	this._options = o;
	this.elementId = $(this._options.element).prop('id');
	// NOTE:  Multiple concurrent cache uploads works fine, but multiple concurrent uploads to a resource often causes failures and can corrupt the catalog.
	// Leave this set to 1 if the uploader supports uploading directly to resources.
	this.MAX_CONCURRENT_UPLOADS = 1;
	this.ALLOW_DRAG_AND_DROP = true;
	this.DRAG_AND_DROP_ON = true;
	this.readyForProcessing = false;
	this.uploadsInProgress = 0;
	this.uploadsStarted = 0;
	this.waitingForUpload = [];
	this.overwriteConfirmIssued = false;
	this.doOverwrite = false;
	this.anyFailedUploads = false;
	this.anySuccessfulUploads = false;
	this.xhrs = {};
	$(this._options.element).html("");

	this.buildUploaderDiv = function() {
		$(this._options.element).append(
			'<div class="abu-uploader">' +
				'<div id="abu-files-processing" class="abu-files-processing">Processing...... </div>' +
				'<a id="file-uploader-instructions-sel" class="abu-uploader-instructions-sel"><span class="icon icon-sm icon-qm"></span>Help</a>' +
				'<div class="abu-upload-drop-area" style="display: none;"><span>Drop files here to upload</span></div>' +
				'<div class="abu-xnat-interactivity-area">' +
					'<div class="abu-xnat-interactivity-area-contents"></div>' +
					'<div class="abu-options-div">' +
					((this._options.showExtractOption) ?
						'<div class="abu-options-cb" id="extractRequestBoxDiv" title = "Extract compressed files on upload (zip, tar, gz)"?>' +
						'<input id="extractRequestBox" type="checkbox" value="1" checked="checked">' +
						'Extract compressed files' +
						'</div>' :
							'<div class="abu-extract-zip"><input id="extractRequestBox" type="hidden" value="1"/></div>'
					) +
					((this._options.showCloseOption) ?
						'<div class="abu-options-cb" id="closeBoxDiv" title = "Close window upon submit and send e-mail upon completion">' +
						'<input id="closeBox" type="checkbox" value="1">' +
						'Close window upon submit' +
						'</div>' : ""
					) +
					((this._options.showEmailOption) ?
						'<div class="abu-options-cb" id="emailBoxDiv" title = "Send e-mail upon completion">' +
						'<input id="emailBox" type="checkbox" value="1">' +
						'Send e-mail upon completion' +
						'</div>' : ""
					) +
					((this._options.showUpdateOption) ?
						'<div class="abu-options-cb" id="updateBoxDiv" title = "Update existing records?">' +
						'<input id="updateBox" type="checkbox" value="1">' +
						'Update existing records?' +
						'</div>' : ""
					) +
					((this._options.showVerboseOption) ?
						'<div class="abu-options-cb" id="verboseBoxDiv" title = "Verbose status output?">' +
						'<input id="verboseBox" type="checkbox" value="1"' +
						'Verbose status output?' +
						'</div>' : ""
					) +
					'<div class="abu-options-cb" id="formatAndContentBoxDiv" style="display:none;margin-bottom:4px;"><table><tr><td>Content:</td><td><input id="contentBox" name="fileContent" type="text"/></td></tr>' +
					'<tr><td>Format:</td><td><input id="formatBox" name="formatBox" type="text"/></td></tr></table></div>'+
					'<div class="abu-options-cb" id="triageMessage" style="display: none;">' +
					'<br>Your files will be uploaded to the project quarantine location and will await review by project administrators' +
					'</div>' +
					'</div>' +
				'</div>' +
			'<div id="abu-upload-button" class="abu-upload-button" style="position: relative; overflow: hidden; direction: ltr;">' +
				'Upload files<input multiple="multiple" type="file" id="file-upload-input" class="abu-button-input btn" ' +
					((this._options.acceptFilePattern) ? 'accept="' + this._options.acceptFilePattern + '"'
						: ''
					) + '>' +
			'</div>' +
			'<div class="abu-list-area"><ul class="abu-upload-list"></ul>' +
				'<div class="response_text" style="display:none"></div>' +
			'</div> '
		);
		// $("#abu-upload-button").click(function() { $("#abu-done-button").removeClass("abu-button-disabled"); });
		$("#xmodal-abu-done-button").click(this.triggerDone);

		// replaced #abu-process-button with #xmodal-abu-process-button, which is defined in the xmodal options
		$("#xmodal-abu-process-button").click(this._options.processFunction);
		$("#xmodal-abu-cancel-button").click(this.cancelUploads);
		$("#" + this.elementId + " #closeBox").change(function(){
			this.updateOptionStatus();
		 }.bind(this));

		$('.abu-uploader-instructions-sel').click(function() {
			var templateV=
				'<div id="file-uploader-instructions" class="abu-uploader-instructions">' +
				'<h3>Instructions</h3>' +
				'<ul>' +
				((this.ALLOW_DRAG_AND_DROP) ?
					'<li>To upload, click the <b>Upload Files</b> button or drag files into the space below the buttons. (Drag-and-drop is supported in FF, Chrome.)</li>' :
					'<li>To upload, click the <b>Upload Files</b> to begin selection of files for upload.</li>') +
				((this._options.maxFiles == 1) ?
						'<li>This uploader supports only a single file upload</li>' :
						'<li>Multiple files may be selected</li>'
				) +
				'<li>Uploads will begin automatically</li>' +
				'<li>Upload of directories is not supported</li>' +
				'<li>When finished uploading, press <b>Done</b> to close the modal, or, if an automation script is to be launched by this upload process, press <b>Process Files</b> to process the uploaded files.</li>' +
				'</ul>' +
				'</div>';
			xmodal.message("Uploader Instructions",templateV, undefined, {height:"400px",width:"800px"});
		}.bind(this));

		this.updateOptionStatus();

		if (this.ALLOW_DRAG_AND_DROP) {
			const dragDrop = $("#" + this.elementId + " .abu-upload-drop-area");
			dragDrop.on('dragleave',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = false;
					if (typeof this.timeout !== "undefined") {
						clearTimeout( this.timeout );
					}
					this.timeout = setTimeout( function(){
						if( !this.showDrag ){
							dragDrop.css('display','none');
							dragDrop.removeClass('abu-upload-drop-area-active');
							try {
								e.preventDefault();
								e.stopPropogation();
							} catch(e) { /* Do nothing */ }
						}
					}.bind(this), 200 ).bind(this);
				}
			}.bind(this));
			dragDrop.on('dragover',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = true;
					this.activateUploadArea(e);
				}
			}.bind(this));
			dragDrop.on('dragenter',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = true;
					this.activateUploadArea(e);
				}
			}.bind(this));
			dragDrop.on('drop',function(e) {
				dragDrop.css('display','none');
				dragDrop.removeClass('abu-upload-drop-area-active');
				if(e.originalEvent.dataTransfer){
					this._options.uploadStartedFunction();
					if(e.originalEvent.dataTransfer.files.length) {
						e.preventDefault();
						e.stopPropagation();
						this.doFileUpload(e.originalEvent.dataTransfer.files);
					}
				}
			}.bind(this));
			$(this._options.element).on('dragleave',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = false;
					if (typeof this.timeout !== "undefined") {
						clearTimeout( this.timeout );
					}
					this.timeout = setTimeout( function(){
						if( !this.showDrag ){
							dragDrop.css('display','none');
							dragDrop.removeClass('abu-upload-drop-area-active');
							try {
								e.preventDefault();
								e.stopPropogation();
							} catch(e) { /* Do nothing */ }
						}
					}.bind(this), 200 ).bind(this);
				}
			}.bind(this));
		$(this._options.element).on('dragover',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = true;
					this.activateUploadArea(e);
				}
			}.bind(this));
			$(this._options.element).on('dragenter',function(e) {
				if (this.DRAG_AND_DROP_ON) {
					this.showDrag = true;
					this.activateUploadArea(e);
				}
			}.bind(this));
		}
		$("#" + this.elementId + " #file-upload-input").change(function(eventData) {
			this._options.uploadStartedFunction();
			this.overwriteConfirmIssued = false;
			if (typeof eventData.target.files !== 'undefined') {
				var fileA = eventData.target.files;
				if (fileA.length==0) {
					$("#xmodal-abu-done-button")
						.show()
						.prop("disabled",false);
				}
				this.doFileUpload(fileA);
			}
		}.bind(this));
	}.bind(this);

	this.processingComplete = function() {
		// $("#xmodal-abu-done-button").html("Done");
		// $("#abu-done-button-text").addClass("abu-done-button-done");
		// $("#abu-done-button-text").removeClass("abu-done-button-cancel");
		// $("#abu-upload-button").css("display","None");
		$("#xmodal-abu-cancel-button").hide();
		$("#xmodal-abu-done-button")
			.prop("disabled",false)
			.show();
		$("#xmodal-abu-process-button")
			.hide()
			.prop("disabled","disabled");
		// $("#abu-process-button-text").html("&nbsp;");
		// $("#abu-process-button").css("visibility","hidden");
		$(".abu-upload-button")
			.hide()
			.prop("disabled","disabled");
		$("#" + this.elementId + " #abu-files-processing").hide();
		$("#" + this.elementId + " .abu-uploader").css("overflow-y","auto")
			.css("overflow-x","hidden");
	}

	this.cancelUploads = function() {
		$.each(this.xhrs, function(key, xhr) {
			// for some reason, there's a guid key added to this object
			if (key.startsWith('idx')) {
				xhr.abort();
			}
		});
	}.bind(this);

	this.doFileUpload = function(fileA) {
		var start_i = $("#" + this.elementId + " form[id^=file-upload-form-]").length;
		$("#xmodal-abu-process-button").prop("disabled","disabled");
		for (var i=0; i<fileA.length; i++) {
			var cFile = fileA[i];
			var adj_i = i + start_i;
			$("#" + this.elementId + " .abu-upload-list").append(
				'<form id="file-upload-form-' + adj_i + '" action="' + this._currentAction.replace("##FILENAME_REPLACE##",cFile.name) +
					 (($("#extractRequestBox").length>0) ? (($("#extractRequestBox").is(':checked')) ? "&extract=true" : "&extract=false") : "") +
					 (($("#emailBox").length>0) ? (($("#emailBox").is(':checked')) ? "&sendemail=true" : "&sendemail=false") : "") +
					 (($("#verboseBox").length>0) ? (($("#verboseBox").is(':checked')) ? "&verbose=true" : "&verbose=false") : "") +
					 (($("#updateBox").length>0) ? (($("#updateBox").is(':checked')) ? "&update=true" : "&update=false") : "") +
					 (($("#contentBox").length>0) ? (($("#contentBox").val().length>0) ? "&content="+$("#contentBox").val() : "") : "") +
					 (($("#formatBox").length>0) ? (($("#formatBox").val().length>0) ? "&format="+$("#formatBox").val() : "") : "") +
					 '" method="POST" enctype="multipart/form-data">' +
				'</form>' +
				'<div id="file-info-div-' + adj_i + '"><span class="abu-upload-file abu-upload-filename">' + cFile.name + '</span><span class="abu-upload-file">' +
					" (" + ((typeof cFile.type !== 'undefined' && cFile.type !== '') ? cFile.type + ", " : '') +
					 this.bytesToSize(cFile.size) + ") </span>" +
					'<div class="abu-progress">' +
						'<div class="abu-bar"></div >' +
						'<div class="abu-percent">0%</div >' +
					'</div>' +
					'<div id="upload-status-div-' + adj_i + '" class="abu-status"></div>' +
					'<div id="upload-cancel-div-' + adj_i + '" class="abu-cancel-div">' +
						'<a class="abu-cancel" data-idx="' + adj_i + '">Cancel</a>' +
					'</div>' +
				'</div>');

			$('#' + this.elementId + ' #upload-cancel-div-' + adj_i + ' a.abu-cancel').click(function(e){
				let idx = $(e.target).data('idx');
				const xhr = this.xhrs['idx' + idx];
				if (xhr) {
					xhr.abort();
				} else {
					//upload hasn't started yet, cancel the request
					$('#' + this.elementId + ' #file-upload-form-' + idx).remove();
					this.fillStatus($('#' + this.elementId + ' #upload-status-div-' + idx), "Canceled", "abort");
					this.removeFromWaitlist(idx);
					this.markUploadDone(idx);
				}
			}.bind(this));

			this.waitingForUpload.push(adj_i);
			var formData = new FormData();
			formData.append("file" + adj_i,cFile,cFile.name);
			this.uploadFile(adj_i,formData);
			this.manageUploads();
		}
	}.bind(this)

	this.updateOptionStatus = function() {
		if ($("#" + this.elementId + " #closeBox").is(':checked')) {
			$("#" + this.elementId + " #emailBox").prop('checked', true)
				.attr('disabled', true);
		} else {
			$("#" + this.elementId + " #emailBox").attr('disabled', false);
		}
	}.bind(this);

	this.bytesToSize = function(bytes) {
	   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
	   if (bytes == 0) return '0 Byte';
	   var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
	   return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
	}

	this.activateUploadArea = function(e) {
		$("#xmodal-abu").find(".scroll").scrollTop(0);
		$("#" + this.elementId + " .abu-upload-drop-area").css('display','inline-block')
			.addClass('abu-upload-drop-area-active');
		try {
			e.preventDefault();
			e.stopPropogation();
		} catch(e) { /* Do nothing */ }
	}.bind(this);

	this.uploaderUploadCompletedFunction = function(anyFailedUploads) {
		if (anyFailedUploads) {
			xmodal.message("Upload failed","WARNING:  One or more file uploads failed.  Please check before running any additional processing.", undefined, { id:"xmodal-abu-upload-failed" });
		}
		this.anyFailedUploads = false;
	}.bind(this);

	this.uploadFile = function(idx, formData) {
		var formSelector = "#" + this.elementId + " #file-upload-form-" + idx;
		var infoSelector = formSelector.replace("-upload-form-","-info-div-");
		var bar = $(infoSelector).find(".abu-bar");
		var percent = $(infoSelector).find(".abu-percent");
		var status = $(infoSelector).find(".abu-status");
		let filename = formData.get("file"+idx).name;
		$(formSelector).on("submit",function(e, uploader) {
			uploader.removeFromWaitlist(idx);
			let formObj = $(this).ajaxSubmit({
				beforeSend: function(arr, $form, options) {

					// Don't allow % and # characters in the filename.
					if (filename.match(/[\[\]%#{}]/g)){
						status.html("<span class='abu-upload-fail'>Filename contains invalid characters ('%','#','[]', and '{}' are not allowed). Please rename file and try again.</a>");
						$(infoSelector).find(".abu-progress").css("display","none");
						status.css("display","inline-block");
						arr.abort();
						this.complete(false);
						return false;
					}

					XNAT.app.timeout.maintainLogin = true;
					var formURL = $form.url;
					if (typeof formURL !== 'undefined' && formURL.toLowerCase().indexOf("overwrite=true")>=0 && formURL.indexOf("/files")>0) {
						// See if file already exists
						var dupURL = formURL.substring(0,formURL.indexOf("/files")+6) + "?format=json";
						this.fileName = formURL.substring(formURL.indexOf("/files")+7);
						if (this.fileName.indexOf("?")>0) {
							this.fileName = this.fileName.substring(0,this.fileName.indexOf("?"))
						}
						this.isOverwrite = false;
						this.doOverwrite = false;
						$.ajax({
							type: "GET",
							url: dupURL,
							async: false,
							dataType: 'json',
						}).done(function(data, textStatus, jqXHR) {
							//console.log(data);
							if (typeof data.ResultSet !== 'undefined' && typeof data.ResultSet.Result !== 'undefined' && Array.isArray(data.ResultSet.Result)) {
								var resultArr = data.ResultSet.Result;
								for (var i=0; i<resultArr.length; i++) {
									if (typeof resultArr[i].Name !== 'undefined' && (resultArr[i].Name == this.fileName  ||
										(typeof resultArr[i].URI !== 'undefined' && resultArr[i].URI.endsWith('/' + this.fileName)))) {
										this.isOverwrite = true;
										if (!uploader.overwriteConfirmIssued) {
											this.doOverwrite = confirm("\nDo you want to overwrite existing files?\n\n" +
													"One or more files you are uploading already exist on the sever.  Press 'OK' to overwrite files or 'Cancel' " +
													"to leave existing files in place.\n\nNOTE:  New files will still be uploaded if you choose not to " +
													"overwrite existing files.\n");
											uploader.doOverwrite = this.doOverwrite;
											uploader.overwriteConfirmIssued = true;
										} else {
											this.doOverwrite = uploader.doOverwrite;
										}
										break;
									}
								}
							}
						}.bind(this));
					}
					$form.data = formData;
					$form.processData=false;
					$form.contentType=false;
					status.empty();
					var percentVal = '0%';
					bar.width(percentVal);
					percent.html(percentVal);
					uploader.uploadsStarted++;
					uploader.uploadsInProgress++;
					if (this.isOverwrite && !this.doOverwrite) {
			 			status.html('<span class="abu-upload-fail">File exists.  Upload cancelled at user request.</a>');
			 			status.css("display","inline-block");
			 			$(infoSelector).find(".abu-progress").css("display","none");
						uploader.uploadsStarted--; // We didn't upload anything
						this.complete(true);
						arr.abort();
						return false;
					}
					return true;
				},
				uploadProgress: function(event, position, total, percentComplete) {
					var percentVal = percentComplete + '%';
					bar.width(percentVal);
					percent.html(percentComplete === 100 ? 'Saving...' : percentVal);
				},
				error: function(xhr, textStatus, result) {
					let err;
					if (textStatus === "abort") {
						err = 'Canceled';
						uploader.uploadsStarted--; // indicate that no catalog refresh is needed
					} else {
						err = 'Failed';
						uploader.anyFailedUploads = true;
					}
					uploader.fillStatus(status, err, result);
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
				 			status.html('<a href="javascript:abu.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-complete abu-upload-complete-text">Upload complete' +
								((this.isOverwrite) ? ' (Existing file overwritten) ' : '') + '</a>');
						} else {
				 			status.html('<span class="abu-upload-complete abu-upload-complete-text">Upload complete' +
								((this.isOverwrite) ? ' (Existing file overwritten) ' : '') + '</span>');
						}
						$("#" + uploader.elementId + " #xmodal-abu-done-button-text").addClass("abu-done-button-file-uploaded");
					} else {
			 			status.html('<a href="javascript:abu.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-fail">Duplicate file and overwrite=false.  Not uploaded.</a>');
						uploader.uploadsStarted--; // We didn't upload anything
					}
			 		status.css("display","inline-block");
			 		$(infoSelector).find(".abu-progress").css("display","none");
					uploader.anySuccessfulUploads = true;
				},
				complete: function(counted = true) {
					uploader.completeUpload(idx, counted);
				}
			});
			uploader.xhrs['idx' + idx] = formObj.data('jqxhr');
			return false;
		});
	}

	this.manageUploads = function() {
		if (this.waitingForUpload.length === 0 && this.uploadsInProgress === 0) {
			if (this.uploadsStarted > 0) {
				this.readyForProcessing = true;
			}
			XNAT.app.timeout.maintainLogin = false;
			this._options.uploadCompletedFunction(this.anyFailedUploads, this.anySuccessfulUploads);
			this.uploaderUploadCompletedFunction(this.anyFailedUploads);
			XNAT.app.timeout.maintainLogin = false;
		} else if (this.waitingForUpload.length > 0) {
			const availableSpaces = this.MAX_CONCURRENT_UPLOADS - this.uploadsInProgress;
			for (let i = 0; i < availableSpaces; i++) {
				const target = this.waitingForUpload[i];
				$("#" + this.elementId + " #file-upload-form-" + target).trigger("submit",this);
			}
		}
	}.bind(this)

	this.triggerDone = function () {
		this._options.doneFunction(this.anySuccessfulUploads);
	}.bind(this);

	this.fillStatus = function(status, err, result) {
		status.data("rtn", result);
		status.html('<a href="javascript:abu.showReturnedText(\'' + $(status).attr('id') + '\')" class="underline abu-upload-fail">' + err + '</a>');
		status.css("display","inline-block");
		status.parent().find(".abu-progress").css("display","none");
		$("#xmodal-abu-cancel-button")
			.show()
			.prop("disabled", false);
	}.bind(this);

	this.removeFromWaitlist = function (idx) {
		const index = this.waitingForUpload.indexOf(idx);
		if (index !== -1) {
			this.waitingForUpload.splice(index, 1);
		}
	}.bind(this);

	this.markUploadDone = function(idx) {
		delete this.xhrs['idx' + idx];
		$('#' + this.elementId + ' #upload-cancel-div-' + idx).remove();
	}.bind(this);

	this.completeUpload = function(idx, counted) {
		this.markUploadDone(idx);
		if (counted) {
			this.uploadsInProgress--;
		}
		this.manageUploads();
	}.bind(this);
}

