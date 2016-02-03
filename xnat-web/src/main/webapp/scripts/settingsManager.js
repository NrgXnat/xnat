//requires settings object
// {"URI":"","radio_ids":["id1","id2"],"current_selected":"id2","msg":"Saving","color":"#DEDEDE","button":"idy"}

function RadioSettingsManager(_dom,_obj){
	this.dom=_dom;
	this.obj=_obj;
	
	if(this.obj.msg==undefined){
		this.obj.msg="Saving";
	}
	if(this.obj.color==undefined){
		this.obj.color="#DEDEDE";
	}
	
	this.disableDOM=function(_val){
		if(_val){
		    if(this.popupLoader==undefined){
		    	if(this.obj.divID){
			    	this.popupLoader=prependLoader(this.dom,this.obj.msg);
					this.popupLoader.render();
		    	}
		    }
			this.dom.style.color=this.obj.color;
		}else{
		    if(this.popupLoader!=undefined){
				this.popupLoader.close();
				this.popupLoader=null;
		    }
			this.dom.style.color="";
		}
		
		for(var settingsC=0;settingsC<this.obj.radio_ids.length;settingsC++){
			document.getElementById(this.obj.radio_ids[settingsC]).disabled=_val;
		}
		
		document.getElementById(this.obj.button).disabled=_val;
	};
	
	this.changeSuccess=function(o){
		this.obj.current_value=this._level;
		this.disableDOM(false);
		this.configButton();
	};
	
	this.changeFailure=function(o){
		if(o.status==401){
            xModalMessage('Session Expired', 'WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.');
			window.location=serverRoot+"/app/template/Login.vm";
		}else{
			this.disableDOM(false);
            xModalMessage('Settings Validation', "ERROR " + o.status + ": Change failed.")
		}
	};
	
	this.setDefault=function(){
		for(var settingsC=0;settingsC<this.obj.radio_ids.length;settingsC++){
			if(this.obj.current_value==document.getElementById(this.obj.radio_ids[settingsC]).value){
				document.getElementById(this.obj.radio_ids[settingsC]).checked=true;
				break;
			}
		}
	};
	
	this.configButton=function(){
		for(var settingsC=0;settingsC<this.obj.radio_ids.length;settingsC++){
			if(document.getElementById(this.obj.radio_ids[settingsC]).checked){
				this._level=document.getElementById(this.obj.radio_ids[settingsC]).value;
				break;
			}
		}
		
		if(this._level==undefined){
			this.setDefault();
			document.getElementById(this.obj.button).disabled=true;
		}else{
			if(this.obj.current_value==this._level){
				document.getElementById(this.obj.button).disabled=true;
			}else{
				document.getElementById(this.obj.button).disabled=false;
			}
		}
	};
	
	this.set=function(){
		for(var settingsC=0;settingsC<this.obj.radio_ids.length;settingsC++){
			if(document.getElementById(this.obj.radio_ids[settingsC]).checked){
				this._level=document.getElementById(this.obj.radio_ids[settingsC]).value;
				break;
			}
		}
		this.settingsCallback={
			success:this.changeSuccess,
			failure:this.changeFailure,
            cache:false, // Turn off caching for IE
			scope:this
		};
		this.disableDOM(true);
		
        YAHOO.util.Connect.asyncRequest('PUT',this.obj.URI + this._level + '?XNAT_CSRF=' + window.csrfToken, this.settingsCallback);
	};
	
	//on init
	this.configButton();
	for(var settingsC=0;settingsC<this.obj.radio_ids.length;settingsC++){
		document.getElementById(this.obj.radio_ids[settingsC]).manager=this;
		document.getElementById(this.obj.radio_ids[settingsC]).onclick=function (){
			this.manager.configButton();
		};
	}
	
	document.getElementById(this.obj.button).manager=this;
	document.getElementById(this.obj.button).onclick=function (){
		this.manager.set();
	};
}

function scriptGet (_dom,_obj) {
  this.dom=_dom;
  /**
   * {
   *   getStatus : uri,
   *   getScript : uri,
   *   project : "",
   *   enable_radio : "",
   *   disable_radio : "",
   *   mode_list : "",
   *   script_text_area : "",
   *   save_button : ""
   * }
   */
  
  this.obj=_obj;
  this.initial = {status : null, script : null};
  this.current = {status : null, script : null};
  this.onFailure=function(o) {
    if(o.status==401){
       xModalMessage('Session Expired', 'WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.');
      window.location=serverRoot+"/app/template/Login.vm";
    }else if(o.status==404){
      // just means the script doesn't yet exist. This is likely ok.
    }else{
      // this.disableDOM(false);
      xModalMessage('Error' + o.status, 'ERROR (' + o.statusText + ')');
    }
  };
  this.getDifferences=function() {
    var changed = [];
    for (var k in this.initial) {
      if (this.initial[k] !== this.current[k]) {
	changed.push(k);
      }
    }
    return changed;
  };
  this.determineDifference=function () {
    var changed = this.getDifferences();
    if (changed.length !== 0) {
      document.getElementById(this.obj.save_button).disabled=false;

    }
    else {
      document.getElementById(this.obj.save_button).disabled=true;
    }
  };
  this.addListeners=function() {
    var that = this;
    document.getElementById(that.obj.enable_checkbox).onclick=function() {
      that.current.status=this.checked;
      that.determineDifference();
      var text_area = document.getElementById(that.obj.script_text_area);
      if(text_area){
        text_area.disabled=!that.current.status;
      }
    };
/*
    document.getElementById(that.obj.enable_radio).onclick=function() {
      that.current.status=true;
      that.determineDifference();
    };
    document.getElementById(that.obj.disable_radio).onclick=function() {
      that.current.status=false;
      that.determineDifference();
    };
*/
    document.getElementById(that.obj.script_text_area).onkeyup=function() {
      that.current.script = document.getElementById(that.obj.script_text_area).value;
      that.determineDifference();
    };
    var resetInitial = function () {
      that.initial.status = that.current.status;
      that.initial.script = that.current.script;
      document.getElementById(that.obj.save_button).disabled=true;
      xModalMessage('Success','Your settings have been successfully updated.','OK');
    };
    var statusPut = function () {
      var uri = that.obj.putStatus+that.current.status;
      YAHOO.util.Connect.asyncRequest('PUT',uri + "&XNAT_CSRF=" + window.csrfToken,{success : resetInitial, failure : that.onFailure, cache : false, scope : that});
    };
    var scriptPut = function (f) {
      YAHOO.util.Connect.asyncRequest('PUT', that.obj.putScript + "&XNAT_CSRF=" + window.csrfToken,
    		  									{success : f ? f : resetInitial,
    	  										 failure: that.onFailure,
                                                 cache:false, // Turn off caching for IE
    	  										 scope: that},
    	  										 that.current.script);
    };
    var contains = function(a,v) {
      var found = false;
      for (var i = 0; i < a.length; i++) {
	if (a[i] === v) {
	  found = true;
	  break;
	}
      }
      return found;
    };
    document.getElementById(that.obj.save_button).onclick=function() {
      var changes = that.getDifferences();
      if (contains(changes, "script") && contains(changes, "status")) {
	scriptPut(statusPut);
      }
      else if (contains(changes, "script")) {
	scriptPut(null);
      }
      else {
	statusPut();
      }
    };
  };
  this.get=function() {
    var parseResponse = function (o) {
      var resp = o.responseText;
      var parsedResponse = YAHOO.lang.JSON.parse(resp);
      return parsedResponse;
    };
    var statusGet = function (o) {
      var parsedResponse = parseResponse(o);
      var status = false;
      if (parsedResponse.ResultSet.Result.length !== 0) {
    	  if( parsedResponse.ResultSet.Result[0].edit == undefined){ //sort of a hack to get this code to work with generic nrg_config return values
    		  status = parsedResponse.ResultSet.Result[0].status === "enabled" ? true : false;
    	  } else {
          status = parsedResponse.ResultSet.Result[0].edit === "true" ? true : false;
    	  }
      }
      this.initial.status=status;
      this.current.status=status;
      document.getElementById(this.obj.enable_checkbox).checked=status;
      if(status){
        document.getElementById(this.obj.script_text_area).disabled=false;
      } else {
        document.getElementById(this.obj.script_text_area).disabled=true;
      }
      /*
      document.getElementById(this.obj.enable_radio).checked=status;
      document.getElementById(this.obj.disable_radio).checked=!status;
      */
    };
    var scriptGet = function (o) {
      var parsedResponse = parseResponse(o);
      var script = "";
      if (parsedResponse.ResultSet.Result.length !== 0) {
	script = parsedResponse.ResultSet.Result[0].script;
	//sort of a hack to get this code to work with generic nrg_config return values
	if(script == undefined ){
		script = parsedResponse.ResultSet.Result[0].contents;
	}
	
	
	YAHOO.util.Connect.asyncRequest('GET', this.obj.getStatus, {success : statusGet, failure : this.onFailure, cache : false, scope : this});
      }
      this.initial.script = script;
      this.current.script = script;
            
      document.getElementById(this.obj.script_text_area).value=script;
    };
    
    YAHOO.util.Connect.asyncRequest('GET', this.obj.getScript, {success : scriptGet, failure : this.onFailure, cache : false, scope : this});
  };
  this.addListeners();
}

function seriesImportFiltersGet(settings) {
    /*
     'container': document.getElementById('series_import_filter_container'),
     'project': '$project.getId()',
     'enable_checkbox': 'enable_series_import_filter',
     'mode_list': 'series_import_filter_mode_list',
     'filters_text_area': 'series_import_filter_text_area',
     'save_button': 'series_import_filter_save'
     */
    this.reservedKeys = ["enabled", "strict", "projectId", "modality", "mode"];
    this.project = settings.project;
    this.enable = document.getElementById(settings.enable_checkbox);
    this.mode = document.getElementById(settings.mode_list);
    this.filters = document.getElementById(settings.filters_text_area);
    this.save = document.getElementById(settings.save_button);
    this.initial = {status: false, mode: null, filters: null};
    this.onFailure = function (o) {
        if (o.status == 401) {
            xModalMessage('Session Expired', 'WARNING: Your session has expired.<br/><br/>You will need to re-login and navigate to the content.');
            window.location = serverRoot + "/app/template/Login.vm";
        } else if (o.status == 404) {
            // just means the script doesn't yet exist. This is likely ok.
        } else {
            // this.disableDOM(false);
            xModalMessage('Error' + o.status, 'ERROR (' + o.statusText + ')');
        }
    };
    this.isDirty = function () {
        if (this.initial.status != this.enable.checked) {
            return true;
        }
        // If the current enabled state isn't different and the enabled state is disabled,
        // then nothing has effectively changed.
        if (!this.initial.status) {
            return false;
        }
        if (this.initial.mode != this.mode.value) {
            return true;
        }
        return this.initial.filters != this.filters.value;
    };
    this.determineDifference = function () {
        this.save.disabled = !this.isDirty();
    };
    this.addListeners = function() {
        var that = this;
        this.enable.onclick = function () {
            that.mode.disabled = that.filters.disabled = !this.checked;
            that.determineDifference();
        };
        this.mode.onchange = function() {
            that.determineDifference();
        };
        this.filters.onchange = this.filters.onkeyup = function() {
            that.determineDifference();
        };
        this.save.onclick = function() {
            var callbacks = {
                success: function() {
                    that.save.disabled = true;
                    that.initial = {  // reset these values so the enabled state of the save button reflects the current saved state.
                        status: that.enable.checked,
                        mode: that.mode.value,
                        filters: that.filters.value
                    };
                    xModalMessage('Saved', 'Your changes to the series import filters for the project ' + that.project + ' have been saved');
                },
                failure: that.onFailure,
                cache: false,
                scope: this };
            if (!that.enable.checked) {
                YAHOO.util.Connect.asyncRequest('PUT', serverRoot + '/data/projects/' + that.project + '/config/seriesImportFilter/config?status=disabled&XNAT_CSRF=' + window.csrfToken, callbacks);
            } else {
                var mode = that.mode.value;
                var filters = that.filters.value;
                // If the mode and filters haven't changed, then we just need to enable the filter.
                if (that.initial.mode == mode && that.initial.filters == filters) {
                    YAHOO.util.Connect.asyncRequest('PUT', serverRoot + '/data/projects/' + that.project + '/config/seriesImportFilter/config?status=enabled&XNAT_CSRF=' + window.csrfToken, callbacks);
                } else {
                    var status = !that.initial.status ? '&status=enabled' : '';
                    var data = getSeriesImportFilterData(mode, filters, true);
                    YAHOO.util.Connect.asyncRequest('PUT', serverRoot + '/data/projects/' + that.project + '/config/seriesImportFilter/config?inbody=true&XNAT_CSRF=' + window.csrfToken + status, callbacks, data);
                }
            }
        };
    };
    this.get = function() {
        this.populateAttributes = function (config) {
            this.mode.disabled = this.filters.disabled = false;
            var contents = JSON.parse(config.contents);
            if (contents) {
                if (contents.mode) {
                    this.mode.value = this.initial.mode = contents.mode;
                }
                if (contents.list) {
                    this.filters.value = this.initial.filters = contents.list;
                } else {
                    var filters = '';
                    for (var key in contents) {
                        if (contents.hasOwnProperty(key)) {
                            if (jq.inArray(key, this.reservedKeys) >= 0) {
                                continue;
                            }
                            filters += key + ': ' + contents[key] + '\n';
                        }
                    }
                    this.filters.value = this.initial.filters = filters;
                }
            }
        };

        this.populate = function(response) {
            var results = YAHOO.lang.JSON.parse(response.responseText);
            var config = results.ResultSet.Result[0]; // TODO: This is way too trusting. There should be validation here.
            var enabled = config.status && (config.status == 'enabled' || config.status == 'true');
            this.initial.status = enabled;

            // Only enable the controls if enabled is true, since they'll already be disabled by default.
            this.populateAttributes.call(this, config);
            if (enabled) {
                this.enable.checked = true;
            } else {
                this.mode.disabled = this.filters.disabled = true;
            }
        };
        var callbacks = { success: this.populate, failure: this.onFailure, cache: false, scope: this };
        YAHOO.util.Connect.asyncRequest('GET', serverRoot + '/data/projects/' + this.project + '/config/seriesImportFilter/config?XNAT_CSRF=' + window.csrfToken + '&format=json', callbacks);
    };
    this.addListeners();
}