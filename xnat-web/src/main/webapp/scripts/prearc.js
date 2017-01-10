/*
 * web: prearc.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//function Prearc(servletURL, name) {
//  var urlbase = servletURL + '?remote-class=org.nrg.xnat.ajax.Prearchive&remote-method='
//  this.listURL = urlbase + 'sessions&prearc=' + name;
//  this.removeURL = urlbase + 'remove&prearc=' + name + '&path=';
//  this.moveURL = urlbase + 'move&from=' + name;
//  this.name = name;
//  this.div = null;
//  this.postSessionList = new Array(0);
//
//  // Callbacks don't get called in object context, so if we need access
//  // to this object we have to set up the callback as a closure.
//  var instance = this;
//
//  // Handle the returned session list and draw on the page.
//  this.sessionListCallback = function() {
//    if (4 == instance.listreq.readyState) {
//      if (200 == instance.listreq.status) {
//				instance.buildSessionTableLayout();
//				var response = instance.listreq.responseXML;
//				if (null == response) {
//					new HtmlAlert(window, 'Unexpected response from server:', instance.listreq.responseText);
//	  			return;
//				}
//				var sessions = response.getElementsByTagName('session');
//				for (var i = 0; i < sessions.length; i++) {
//	  			var session = sessions[i];
//	 				session.normalize();
//	  			var sessionPath = session.getAttribute('path');
//
//	  			var tbody = instance.sessionTable.lastChild;
//
//	  			var tr = document.createElement('tr');
//	  			tbody.appendChild(tr);
//	  			tr.className = session.getAttribute('status');
//
//	  			var nameelem = document.createElement('td');
//	  			tr.appendChild(nameelem);
//
//	  			var archiveURL = session.getAttribute('archiveURL');
//	  			if (archiveURL) {
//	    			var link = document.createElement('a');
//	    			link.setAttribute('href', archiveURL);
//	    			nameelem.appendChild(link);
//	    			nameelem = link;
//	  			}
//	  			nameelem.appendChild(document.createTextNode(session.childNodes[0].nodeValue));
//
//          var subject = session.getAttribute('subject');
//          if (null != subject) {
//            nameelem.appendChild(document.createTextNode(' / ' + subject));
//          }
//
//	  			var addID = session.getAttribute('addID');
//	  			if (null != addID) {
//	    			nameelem.appendChild(document.createTextNode(' (' + addID + ')'));
//	  			}
//
//	  			var removetd = document.createElement('td');
//
//	  			var removeop = document.createElement('input');
//	  			removeop.onclick = instance.createRemoveRequest(session.childNodes[0].nodeValue, sessionPath);
//	  			var icon = Prearc.icons['remove'];
//	  			if (null == icon) {
//	    			removeop.setAttribute('type', 'button');
//	    			removeop.setAttribute('value', 'Remove');
//	  			} else {
//	    			removeop.setAttribute('type', 'image');
//	    			removeop.setAttribute('src', icon);
//	  			}
//	  			removetd.appendChild(removeop);
//	  			tr.appendChild(removetd);
//
//					// Set up a select box for the move operation.  The options will be filled
//					// by the page code, which knows about the possible destinations.
//					var movetd = document.createElement('td');
//					movetd.className = 'move-op';
//					var selector = document.createElement('select');
//   				selector.name = sessionPath;
//					var labelOption = new Option("Move to...", null);
//					selector.options[0] = labelOption;
//   				movetd.appendChild(selector);
//					tr.appendChild(movetd);
//
//	  			var date = Date.from_xsDateTime(session.getAttribute('sessionDateTime'));
//	  			var timestamp = date ? date.toLocaleString() : session.getAttribute('sessionTime');
//	  			var timetd = document.createElement('td');
//	  			timetd.appendChild(document.createTextNode(timestamp));
//	  			tr.appendChild(timetd);
//
//	  			date = Date.from_xsDateTime(session.getAttribute('uploadDateTime'));
//	  			timestamp = date ? date.toLocaleString() : session.getAttribute('uploadTime');
//	  			timetd = document.createElement("td");
//	  			timetd.appendChild(document.createTextNode(timestamp));
//	  			tr.appendChild(timetd);
//				}
//
//				// do post callbacks
//				for (var posti in instance.postSessionList) {
//					instance.postSessionList[posti]();
//				}
//
//      } else {
//          xmodal.message('Prearchive Error', "Error " + instance.listreq.status
//	     			 + " getting " + XNAT.app.displayNames.singular.imageSession.toLowerCase() + " list for prearchive " + instance.name);
//      }
//    } else {
//      if (instance.div.firstChild && 'p' == instance.div.firstChild.tagName) {
//				instance.div.firstChild.appendChild(document.createTextNode('.'));
//      }
//    }
//  }
//
//	this.setRemoveHandler(this.draw);
//
//  this.removeCallback = function() {
//    if (4 == instance.removeReq.readyState) {
//      if (200 != instance.removeReq.status) {
//      	new HtmlAlert(window, 'Failed to remove ' + XNAT.app.displayNames.singular.imageSession.toLowerCase(), instance.removeReq.responseText);
//      }
//      instance.finishRemove();
//    }
//  }
//
//  this.moveCallback = function() {
//  	if (4 == instance.moveReq.readyState) {
//  		if (200 != instance.moveReq.status) {
//  			new HtmlAlert(window, 'Failed to move ' + XNAT.app.displayNames.singular.imageSession.toLowerCase(), instance.moveReq.responseText);
//   		}
//  		for (var i in instance.postMoveSession) {
//  			instance.postMoveSession[i]();
//  		}
//  	} else {
//  		if (instance.div.firstChild && 'p' == instance.div.firstChild.tagName) {
//  			instance.div.appendChild(document.createTextNode('.'));
//  		}
//  	}
//  }
//}
//
//Prearc.prototype.setRemoveHandler = function(f) {
//	this.finishRemove = f;
//}
//
//Prearc.prototype.createRemoveRequest = function(name, path) {
//  var instance = this;
//  return function() {
//    if (confirm('Really remove ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' ' + name + ' from prearchive?')) {
//      if (window.XMLHttpRequest) {
//				instance.removeReq = new XMLHttpRequest();
//      } else if (window.ActiveXObject) {
//				instance.removeReq = new ActiveXObject("Microsoft.XMLHTTP");
//      }
//      instance.removeReq.open("DELETE", instance.removeURL + path + "?XNAT_CSRF="+csrfToken, true);
//      instance.removeReq.onreadystatechange = instance.removeCallback;
//      instance.removeReq.send(null);
//    }
//  }
//}
//
//// Request the list of active sessions, and use sessionListCallback to draw
//// the results into the given div.  If additional arguments are provided,
//// they are callback functions to be executed at the end of sessionListCallback.
//Prearc.prototype.draw = function(div) {
//  if (1 <= arguments.length) {
//    this.div = div;
//		this.postSessionList = new Array(arguments.length - 1);
//    for (var i = 1; i < arguments.length; i++) {
//    	this.postSessionList[i-1] = arguments[i];
//    }
//  }
//  this.requestSessionList();
//}
//
//Prearc.prototype.requestSessionList = function() {
//  if (window.XMLHttpRequest) {
//    this.listreq = new XMLHttpRequest();
//  } else if (window.ActiveXObject) {
//    this.listreq = new ActiveXObject("Microsoft.XMLHTTP");
//  }
//  this.listreq.open("GET", this.listURL, true);
//  this.listreq.onreadystatechange = this.sessionListCallback;
//  this.listreq.send(null);
//
//  // Replace contents with a status message
//  while (this.div.hasChildNodes()) {
//    this.div.removeChild(this.div.firstChild);
//  }
//  var messagep = document.createElement('p');
//  this.div.appendChild(messagep);
//  var message = document.createTextNode('Reloading prearchive contents...');
//  messagep.appendChild(message);
//}
//
//Prearc.prototype.buildSessionTableLayout = function() {
//  // Clear the existing contents
//  while (this.div.hasChildNodes()) {
//    this.div.removeChild(this.div.firstChild);
//  }
//
//  this.sessionTable = document.createElement('table');
//  this.div.appendChild(this.sessionTable);
//  this.sessionTable.className = 'prearc';
//
//  var thead = document.createElement('thead');
//  this.sessionTable.appendChild(thead);
//
//  var tr = document.createElement('tr');
//  thead.appendChild(tr);
//  var th = document.createElement('th');
//  tr.appendChild(th);
//  th.className = 'session_name';
//  th.appendChild(document.createTextNode(XNAT.app.displayNames.singular.imageSession + '/' + XNAT.app.displayNames.singular.subject));
//
//  // empty heading for trash column
//  tr.appendChild(document.createElement('th'));
//
//	// empty heading for move column
//	tr.appendChild(document.createElement('th'));
//
//  th = document.createElement('th');
//  tr.appendChild(th);
//  th.className = 'timestamp';
//  th.appendChild(document.createTextNode(XNAT.app.displayNames.singular.imageSession + ' date/time'));
//
//  th = document.createElement('th');
//  tr.appendChild(th);
//  th.className = 'timestamp';
//  th.appendChild(document.createTextNode('Upload date/time'));
//
//  this.sessionTable.appendChild(document.createElement('tbody'));
//}
//
//// Request that the named session be moved to the named destination,
//// and use moveCallback to redisplay.  If any additional arguments
//// are provided, they are callback functions to be executed instead
//// of the default Prearc.draw() method.
//Prearc.prototype.moveSessionTo = function(session, destName) {
//  while (this.div.hasChildNodes()) {
//    this.div.removeChild(this.div.firstChild);
//  }
//  var p = document.createElement('p');
//  p.appendChild(document.createTextNode('Moving ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' ' + session + ' to ' + destName + '...'));
//  this.div.appendChild(p);
//
//	url = this.moveURL + '&to=' + destName + '&path=' + session;
//	url += '&to=' + destName;
//	url += '&path=' + session;
//	url += '&XNAT_CSRF='+csrfToken;
//
//	if (window.XMLHttpRequest) {
//		this.moveReq = new XMLHttpRequest();
//	} else if (window.ActiveXObject) {
//		this.moveReq = new ActiveXObject("Microsoft.XMLHTTP");
//	}
//	this.moveReq.open("PUT", url, true);
//	this.moveReq.onreadystatechange = this.moveCallback;
//	this.moveReq.send(null);
//
//	// enqueue callbacks
//	if (arguments.length <= 2) {
//		var instance = this;
//		this.postMoveSession = new Array(1);
//		this.postMoveSession[0] = function() { instance.draw(); };
//	} else {
//		this.postMoveSession = new Array(arguments.length - 2);
//  	for (var i = 2; i < arguments.length; i++) {
//    	this.postMoveSession[i-2] = arguments[i];
//  	}
//  }
//}
//
//// Returns an Array of the move-op TD elements.
//// This method makes lots of (asserted) assumptions about the table layout.
//Prearc.prototype.getMoveOpTDs = function() {
//	var theTable = this.div.firstChild;
//	if (null == theTable || 'TABLE' != theTable.tagName) {
//		throw new Error('Could not locate prearchive ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' table');
//	}
//
//	var theTableBody = theTable.lastChild;
//	if (null == theTableBody || 'TBODY' != theTableBody.tagName) {
//		throw new Error('Could not locate prearchive ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' table content');
//	}
//
//	var tds = new Array();
//	for (var rowi = 0; rowi < theTableBody.childNodes.length; rowi++) {	// row 0 is table header
//		var theRow = theTableBody.childNodes[rowi];
//		if (5 != theRow.childNodes.length) {
//			throw new Error('Prearchive ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' table row has improper format');
//		}
//		var moveTD = theRow.childNodes[2];
//		if ('TD' != moveTD.tagName || 'move-op' != moveTD.className) {
//			throw new Error('Prearchive ' + XNAT.app.displayNames.singular.imageSession.toLowerCase() + ' table row has unexpected content: ' + moveTD.tagName + ' (' + moveTD.className + ')');
//		}
//		tds.push(moveTD);
//	}
//	return tds;
//}
//
//Prearc.listPrearcs = function(servletURL, callback) {
//  var url = servletURL + "?remote-class=org.nrg.xnat.ajax.Prearchive&remote-method=prearchives";
//  var req;
//  if (window.XMLHttpRequest) {
//    req = new XMLHttpRequest();
//  } else if (window.ActiveXObject) {
//    req = new ActiveXObject("Microsoft.XMLHTTP");
//  }
//  req.open("GET", url, true);
//  req.onreadystatechange = callback;
//  req.send(null);
//
//  return req;
//}
//
//
//Prearc.icons = new Object();
