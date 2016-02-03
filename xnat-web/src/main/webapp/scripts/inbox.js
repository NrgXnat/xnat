/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//function Inbox(div, servlet_url, project) {
//  if (0 == arguments.length) { return; }   // bail early on prototype creation
//
//  this.div = div;
//  var projectspec = '&project=' + project;
//
//  var baseURL = servlet_url + '?remote-class=org.nrg.xnat.ajax.Inbox';
//  this.listURL = baseURL + '&remote-method=list' + projectspec;
//  this.startImportURL = baseURL + '&remote-method=startImport' + projectspec;
//  this.monitorImportURL = baseURL + '&remote-method=monitorImport' + projectspec;
//  this.removeURL = baseURL + '&remote-method=remove' + projectspec;
//
//  this.refreshActions = [];
//
//  this.loading = false;
//  this.importing = null;
//  this.importInterval = null;
//
//  this.listreq = null;
//  this.importreq = null;
//  this.monitorreq = null;
//
//  // Callbacks don't get called in object context, so if we need access
//  // to this object we have to set the callbacks up as closures.
//  var instance = this;
//
//  this.listCallback = function() {
//    if (4 == instance.listreq.readyState) {	// state: loaded
//      if (200 == instance.listreq.status) {	// status: OK
//	// Clear the inbox area before updating.
//	instance.loading = false;
//	while (null != instance.div.firstChild) {
//	  instance.div.removeChild(instance.div.firstChild);
//	}
//
//	var response = instance.listreq.responseXML;
//	if (null == response) {
//	  // We've probably lost the session.  Reload the page.
//	  window.clearInterval(this.importInterval);
//	  this.importInterval = null;
//      xModalMessage('FTP Inbox', "Unable to get listing; session may have timed out.");
//	  location.reload();
//	  return;
//	}
//
//	var files = response.getElementsByTagName("file");
//	if (files.length > 0) {
//	  var listTable = document.createElement('table');
//	  var caption = document.createElement('caption');
//	  caption.appendChild(document.createTextNode('FTP inbox'));
//	  listTable.appendChild(caption);
//
//	  var thead = document.createElement('thead');
//	  listTable.appendChild(thead);
//
//	  var tr = document.createElement('tr');
//	  thead.appendChild(tr);
//	  var th = document.createElement('th');
//	  tr.appendChild(th);
//	  th.appendChild(document.createTextNode('Received files'));
//
//	  th = document.createElement('th');
//	  tr.appendChild(th);
//	  th.appendChild(document.createTextNode('Size'));
//
//	  var tbody = document.createElement('tbody');
//	  listTable.appendChild(tbody);
//
//	  for (var i = 0; i < files.length; i++) {
//	    var f = files[i];
//
//	    tr = document.createElement('tr');
//	    var td = document.createElement('td');
//	    var selectBox = document.createElement('input');
//	    selectBox.setAttribute('type', 'checkbox');
//	    selectBox.setAttribute('value', f.firstChild.data);
//	    td.appendChild(selectBox);
//	    td.appendChild(document.createTextNode(f.firstChild.data));
//	    tr.appendChild(td);
//
//	    td = document.createElement('td');
//	    td.appendChild(document.createTextNode(f.getAttribute('size') + ' kb'));
//	    tr.appendChild(td);
//
//	    // TODO: marker for folder
//	    // TODO: allow descent into folders
//	    // TODO: allow folder selection?
//
//	    tbody.appendChild(tr);
//	  }
//
//	  // table and actions each get put in their own div for layout
//	  var tableDiv = document.createElement('div');
//	  tableDiv.className = 'content';
//	  tableDiv.appendChild(listTable);
//	  instance.div.appendChild(tableDiv);
//
//	  var actionDiv = document.createElement('div');
//	  actionDiv.id = 'inboxActions';
//	  actionDiv.className = 'actions';
//	  instance.div.appendChild(actionDiv);
//
//	  if ('true' == response.documentElement.getAttribute('locked')) {
//        xModalMessage('FTP Inbox', 'FTP Inbox import in progress');
//	    actionDiv.appendChild(document.createTextNode(
//		    'Inbox contents are being imported into prearchive'));
//	  } else {
//	    var deleteButton = document.createElement('input');
//	    deleteButton.setAttribute('type', 'button');
//	    deleteButton.setAttribute('value', 'Delete selected files');
//	    deleteButton.onclick = function() { instance.doDelete(); };
//	    actionDiv.appendChild(deleteButton);
//
//	    var importButton = document.createElement('input');
//	    importButton.setAttribute('type', 'button');
//	    importButton.setAttribute('value', 'Import selected files');
//	    importButton.onclick = function() { instance.doImport(); };
//	    actionDiv.appendChild(importButton);
//	  }
//
//	  instance.div.appendChild(document.createElement('br'));
//	  instance.div.appendChild(document.createElement('hr'));
//	}
//      } else {
//	instance.loading = false;
//	while (null != instance.div.firstChild) {
//	  instance.div.removeChild(instance.div.firstChild);
//	}
//        xModalMessage('FTP Inbox', "Unable to load FTP inbox (error " + instance.listreq.status + "): " + instance.listreq.responseText);
//      }
//    }
//  }
//
//  this.importCallback = function() {
//    instance.checkImport(instance.importreq);
//  }
//
//  this.monitorCallback = function() {
//    instance.checkImport(instance.monitorreq);
//  }
//
//  this.monitorImport = function() {
//    if (window.XMLHttpRequest) {
//      instance.monitorreq = new XMLHttpRequest();
//    } else if (window.ActiveXObject) {
//      instance.monitorreq = new ActiveXObject("Microsoft.XMLHTTP");
//    }
//    instance.monitorreq.open("GET", instance.monitorImportURL, true);
//    instance.monitorreq.onreadystatechange = this.monitorCallback;
//    instance.monitorreq.send(null);
//  }
//}
//
//// Create and discard a dummy object to force prototype object creation
//new Inbox();
//
//Inbox.prototype.getSelection = function() {
//  // Get the list of selected items
//  var selection = '';
//  var inputs = this.div.getElementsByTagName('input');
//  for (var i = 0; i < inputs.length; i++) {
//    var input = inputs[i];
//    if ('checkbox' == input.getAttribute('type')
//	&& true == input.checked) {
//      selection += '&path=' + input.getAttribute('value');
//    }
//  }
//  return selection;
//}
//
//
//Inbox.prototype.doDelete = function() {
//  var selection = this.getSelection();
//  if ('' == selection) {
//    xModalMessage('FTP Inbox', 'No files have been selected, so none will be deleted.');
//  } else if (confirm('Really delete selected files from inbox?')) {
//    // Clear the inbox space
//    while (null != this.div.firstChild) {
//      this.div.removeChild(this.div.firstChild);
//    }
//
//    // The response from the delete is the same as for a list request
//    // TODO: test for browser support?
//    if (window.XMLHttpRequest) {
//      this.listreq = new XMLHttpRequest();
//    } else if (window.ActiveXObject) {
//      this.listreq = new ActiveXObject("Microsoft.XMLHTTP");
//    }
//    this.listreq.open("GET", this.removeURL + selection, true);
//    this.listreq.onreadystatechange = this.listCallback;
//    this.listreq.send(null);
//  }
//}
//
//Inbox.prototype.doImport = function() {
//  var selection = this.getSelection();
//  if ('' != selection
//      || confirm('No files selected; import all files from inbox?')) {
//    // Clear the inbox space
//    while (null != this.div.firstChild) {
//      this.div.removeChild(this.div.firstChild);
//    }
//    var messageDiv = document.createElement('div');
//    messageDiv.className = 'importlog';
//    this.div.appendChild(messageDiv);
//    var messagep = document.createElement('p');
//    messagep.className = 'header';
//    messagep.appendChild(document.createTextNode('Importing files from FTP inbox'));
//    messageDiv.appendChild(messagep);
//    messageDiv.scrollTop = messageDiv.scrollHeight;
//
//    // TODO: test for browser support?
//    if (window.XMLHttpRequest) {
//      this.importreq = new XMLHttpRequest();
//    } else if (window.ActiveXObject) {
//      this.importreq = new ActiveXObject("Microsoft.XMLHTTP");
//    }
//    this.importreq.open("GET", this.startImportURL + selection, true);
//    this.importreq.onreadystatechange = this.importCallback;
//    this.importreq.send(null);
//
//    var instance = this;
//    this.importInterval = window.setInterval(function() {
//					       instance.monitorImport();
//					     }, 2000);
//  }
//}
//
//
//Inbox.prototype.checkImport = function(req) {
//  if (4 == req.readyState) {
//    if (200 == req.status) {
//      var response = req.responseXML;
//      if (null == response) {
//	// We've probably lost the session.  Reload the page.
//	window.clearInterval(this.importInterval);
//	this.importInterval = null;
//    xModalMessage('FTP Inbox', "Unable to import; session may have timed out.");
//	location.reload();
//	return;
//      }
//
//      var statusa = response.getElementsByTagName("status");
//      if (1 == statusa.length) {
//	var status = statusa[0];
//
//	// idle indicator
//	if (0 == status.childNodes.length) {
//	  var lastMessage = this.div.firstChild.lastChild;
//	  if (null != lastMessage) {
//	    lastMessage.appendChild(document.createTextNode('.'));
//	  }
//	}
//
//	var object = null;
//	for (var i = 0; i < status.childNodes.length; i++) {
//	  var statusline = status.childNodes[i];
//	  statusline.normalize();
//	  var tagName = statusline.tagName;
//	  object = statusline.getAttribute("object");
//
//	  var text = "";
//	  for (var j = 0; j < statusline.childNodes.length; j++) {
//	    // 3 = Node.TEXT_NODE
//	    if (3 == statusline.childNodes[j].nodeType) {
//	      text += statusline.childNodes[j].nodeValue;
//	    }
//	  }
//
//	  var message = document.createElement('p');
//	  message.className = tagName;
//	  message.appendChild(document.createTextNode(text));
//	  var messageDiv = this.div.firstChild;
//	  messageDiv.appendChild(message);
//	  messageDiv.scrollTop = messageDiv.scrollHeight;
//
//	  // The very first status message is a 'processing' on the
//	  // inbox object.  Remember this object, because that's our
//	  // cue for knowing when the import is done.
//	  if (null == this.importing) {
//	    if ('processing' == tagName) {
//	      this.importing = object;
//	    } else {
//          xModalMessage('FTP Inbox', "Received unexpected status message " + tagName);
//	    }
//	  }
//	}
//
//	// If the last message refers to the object being processed,
//	// check for success or failure message.
//	if (null != this.importing && this.importing == object) {
//	  switch (tagName) {
//	  case 'processing':
//	  case 'warning':
//	    break;
//
//	  case 'completed':
//	    this.finishImportWithMessage('Import complete');
//	    break;
//
//	  case 'failure':
//	    this.finishImportWithMessage("Import failed: " + text);
//	    break;
//
//	  case 'default':
//        xModalMessage('FTP Inbox', "Unexpected import status message: " + tagName);
//	    break;
//	  }
//	}
//      } else {
//        xModalMessage('FTP Inbox', "Invalid status response from server: expected one status message, received " + statusa.length);
//      }
//    } else {
//        xModalMessage('FTP Inbox', "Unable to check import status (error " + req.status + "): " + req.responseText);
//    }
//  }
//}
//
//
//Inbox.prototype.doRefreshActions = function() {
//  for (var i in this.refreshActions) {
//    this.refreshActions[i]();
//  }
//}
//
//Inbox.prototype.refresh = function() {
//  // If there's an import in progress, don't interrupt it.
//  if (null == this.importing) {
//    this.selfRefresh();
//  }
//  this.doRefreshActions();
//}
//
//
//Inbox.prototype.selfRefresh = function() {
//  // Clear the inbox space, replace with a message
//  while (null != this.div.firstChild) {
//    this.div.removeChild(this.div.firstChild);
//  }
//  this.div.appendChild(document.createTextNode('Checking FTP inbox...'));
//  this.loading = true;
//
//  // TODO: test for browser support?
//  if (window.XMLHttpRequest) {
//    this.listreq = new XMLHttpRequest();
//  } else if (window.ActiveXObject) {
//    this.listreq = new ActiveXObject("Microsoft.XMLHTTP");
//  }
//  this.listreq.open("GET", this.listURL, true);
//  this.listreq.onreadystatechange = this.listCallback;
//  this.listreq.send(null);
//};
//
//
//Inbox.prototype.addRefreshAction = function(handler) {
//  this.refreshActions.push(handler);
//}
//
//
//Inbox.prototype.finishImportWithMessage = function(msg) {
//  // Cancel the import monitor
//  if (this.importInterval) {
//    window.clearInterval(this.importInterval);
//    this.importInterval = null;
//  }
//
//  // Refresh any dependents first
//  this.doRefreshActions();
//
//  var messageDiv = this.div.firstChild;
//
//  var message = document.createElement('p');
//  message.appendChild(document.createTextNode(msg));
//  messageDiv.appendChild(message);
//
//  var continueButton = document.createElement('input');
//  continueButton.setAttribute('type', 'button');
//  continueButton.setAttribute('value', 'Refresh FTP inbox');
//
//  var instance = this;
//  continueButton.onclick = function() {
//    instance.importing = null;
//    instance.refresh();
//  }
//  messageDiv.appendChild(continueButton);
//  messageDiv.scrollTop = messageDiv.scrollHeight;
//}
