//variable to contain a list of ID's for each table that's been loaded in the page
XNAT.app.resizableTables=new Array();

/*
 * Per CSS, the containing #layout_content div will resize itself
 * to fit wide content, constrained to the width of the window.
 *
 * This function fires when the user resizes the browser window.
 * It measures the width of the window and the standard YUI data table.
 * If the table content is wider than the window, it will resize #layout_content
 * However, #layout_content has a minimum width per CSS that matches the page template.
 */
XNAT.app.tableSizer=function(){
	var windowWidth = $(window).innerWidth();
	var divWidth = $('#layout_content').width(); /* what happens when there are more than one of these on a page? */
	var tableWidth = $('table.x_rs_t').width(); /* what happens if there are more than one of these on a page? */

	for(var tSc=0;tSc<XNAT.app.resizableTables.length;tSc++){
		XNAT.app.setTableHeight(XNAT.app.resizableTables[tSc]);
	}

	if (tableWidth > (windowWidth-60)) {
		var setWidth = windowWidth - 60;
		if(setWidth<925)setWidth=925;
//		$('#search_tabs').css('width',setWidth);

		//remove any manually configured widths - necessary after joining 2 tables
		for(var tSc=0;tSc<XNAT.app.resizableTables.length;tSc++){
			XNAT.app.setItemWidth(XNAT.app.resizableTables[tSc]+"_c",'');
			XNAT.app.setItemWidth(XNAT.app.resizableTables[tSc]+"_p",'');
		}
	}else{
		$('#search_tabs').css('width','');

		//need to adjust table width for each table to make sure scrollbar is close to table
		for(var tSc=0;tSc<XNAT.app.resizableTables.length;tSc++){
			XNAT.app.setTableWidth(XNAT.app.resizableTables[tSc]);
		}
	}

}

XNAT.app.setItemWidth=function(div_id,width){
	var d=YUIDOM.get(div_id);
	if(d!=null){
		var d2=$(d);
		d2.css('width',width);
	}
}

//adjusts the table width for individual tables
XNAT.app.setTableWidth=function(div_id){
	var tableC=YUIDOM.get(div_id);//have to use YUI here because jquery fails to find it.  I think because it contains a period.  But, the YUI element can be passed into jquery.
	if(tableC!=null){
		var tableWidth=$(YUIDOM.getFirstChild(tableC)).width();//need the width of the table within the container div.

		var tabsWidth=$('#search_tabs').width();
		if((tableWidth+18)<tabsWidth){//if table + scrollbar doesn't take up the whole tab
			XNAT.app.setItemWidth(div_id+"_c",(tableWidth+18));//set table overflow container to barely contain table, so the scrollbar isn't way off to the right.
		}
//        var leftBar=0;
//		if($('#leftBar')){
//            leftBar=182;
//        }
//        var tableContainer = $('.xT_c');
//        if(tableContainer){
//            tableContainer.width=tableWidth-leftBar+"px";
//        }
		XNAT.app.setItemWidth(div_id+"_p",tableWidth);//try to set the paging div to be the same length as the table. if its to short it will fix itself
	}
}

XNAT.app.setTableHeight=function(div_id){
    // This is a hack: jQuery does not like the @xnat:... selector. So we'll do it old school and wrap it.
    var container = document.getElementById(div_id);
    container = $(container);
    if($(container)!=null){
		var windowHeight = $(window).innerHeight();
		var tableHeight = $('table.x_rs_t').height();
		var tablePosition = $(container).offset();

		/* max height is total screen height minus space for table header & chrome */
		var maxTableHeight = (tableHeight < windowHeight) ? tableHeight + 60 : windowHeight-60;
		var minTableHeight = 300;

		/* available height is visible screen height below the starting Y point of the table, plus room for table header & chrome */
		var availableTableHeight = windowHeight - (tablePosition.top)- 30;
		availableTableHeight = (availableTableHeight > maxTableHeight) ? maxTableHeight : availableTableHeight;
		availableTableHeight = (availableTableHeight < minTableHeight) ? minTableHeight : availableTableHeight;

		/* set dimensions of table containers */
		$(container).css('height',availableTableHeight);
	}
}

$(window).resize(function() { XNAT.app.tableSizer(); });

function DataTableSearch(_div_table_id,obj,_config,_options){
	this.setDefaultValue=function(key,value){
		  if(this.options[key]==undefined){
			  this.options[key]=value;
		  }
	  }
  if(obj!=undefined){
    this.obj=obj;
    this.div_table_id=_div_table_id;
    this.xml=obj.XML;
    if(_config==undefined) {
      _config=new Object();
      _config.csrfToken = null;
    }
    this.config=_config;

    if(_options==undefined){
        this.options=new Object();
    }else{
    	this.options=_options;
    }

    this.onInit=new YAHOO.util.CustomEvent("init",this);
	this.onTableInit=new YAHOO.util.CustomEvent("table-init",this);
    this.columnSortEvent = new YAHOO.util.CustomEvent("column-change",this);
    this.paging=true;
    this.onReloadRequest=new YAHOO.util.CustomEvent("request-reload",this);
    this.onXMLChange=new YAHOO.util.CustomEvent("xml-change",this);

    this.setDefaultValue("showReload",true);
    this.setDefaultValue("showOptionsDropdown",true);
    this.setDefaultValue("showFilterDisplay",true);

    this.setDefaultValue("allowInTableMods",true);
  }

  this.init=function(obj){
    this.initCallback={
      success:this.completeInit,
      failure:this.initFailure,
        cache:false, // Turn off caching for IE
      scope:this
    };

    if(this.optionMenu){
      try{
	this.optionMenu.destroy();
      }catch(e){
          showMessage("page_body", "Exception", e.message);
      }
    }

    if(this.reloadButton){
      this.reloadButton.destroy();
    }

    if(this.datatable){
      //this.datatable.destroy();
      YAHOO.util.Event.purgeElement(this.div_table_id, true);
      YAHOO.util.Dom.get(this.div_table_id).innerHTML = "";
    }else{
      try{
//	YAHOO.util.Cookie.remove(this.div_table_id + ".initialRequest");
      }catch(e){}
    }

    var params="XNAT_CSRF=" + window.csrfToken + "&format=json&cache=true";
    if(obj!=undefined && obj.reload!=undefined){
      this.purge();
      params+="&refresh=true";
      try{
        YAHOO.util.Event.purgeElement(this.div_table_id, true);
	document.getElementById(this.div_table_id).innerHTML="Preparing Results...";
	document.getElementById(this.div_table_id+"_p").innerHTML="";
      }catch(e){}
    }



    YAHOO.util.Connect.asyncRequest('POST',serverRoot +'/REST/search?'+params,this.initCallback,this.xml,this);
  };

  this.initFailure=function(o){
      if (!window.leaving) {
          if(o.status==401){
              showMessage("page_body", "Exception", "WARNING: Your session has expired.  You will need to re-login and navigate to the content.");
              window.location=serverRoot+"/app/template/Login.vm";
          }else{
              document.getElementById(this.div_table_id).innerHTML="Failed to create search results.";
          }
          this.onInit.fire();
      }
  };

  this.completeInit=function(o){
    this.initResults= eval("(" + o.responseText +")");
    this.onTableInit.fire();
    this.render();
    var that = this;
    this.paginator.subscribe("rowsPerPageChange", function (e) {
			       that.set_row_cookie(e.newValue);
			     });
    this.onInit.fire();
  };

  this.set_row_cookie=function (newRowsPerPage) {
    var cookie_name = this.div_table_id + ".initialRequest";
    CookieFunctions.set_cookie(cookie_name, "limit", newRowsPerPage);
  };


  this.render=function(){
    this.searchURI=serverRoot + "/REST/search/" + this.initResults.ResultSet.ID;

    var cookie_name = this.div_table_id + ".initialRequest";
    var historyRequest=YAHOO.util.Cookie.get(cookie_name);
    if(historyRequest!=undefined){
      this.config.initialRequest=historyRequest;

      this.config.rowsPerPage=this.parseValueFromURI(historyRequest,"limit");
      if(this.config.rowsPerPage!=undefined){
        this.config.rowsPerPage=parseInt(this.config.rowsPerPage);

        this.config.offset=this.parseValueFromURI(historyRequest,"offset");
        if(this.config.offset!=undefined){
          this.config.offset=parseInt(this.config.offset);
        }
      }
      if(this.config.rowsPerPage ==undefined){
	this.config.rowsPerPage=20;
      }
    }else{
      if(this.config.rowsPerPage ==undefined){
	this.config.rowsPerPage=20;
      }
      this.config.initialRequest="format=json&offset=0&limit="+this.config.rowsPerPage;
    }

    if(historyRequest!=undefined){
      var sortBy=this.parseValueFromURI(historyRequest,"sortBy");
      if(sortBy!=undefined){
	var sortorder=this.parseValueFromURI(historyRequest,"sortOrder");
	var configSort=new Object();
	configSort.key=sortBy;
	if(sortorder!=undefined)configSort.dir=sortorder;
	this.config.sortedBy=configSort;
      }
    }

    if(this.options.showOptionsDropdown){
    	this.paginator_html="<table width='100%'><tr><td id='" + this.div_table_id +"_xT_pT1'>{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink} {RowsPerPageDropdown} <strong>{CurrentPageReport}</strong></td><td align='right'><div id='" + this.div_table_id +"_sv'></div></td><td align='right' id='" + this.div_table_id +"_xT_pT3'><input type='button' id='" + this.div_table_id +"_reload' value='Reload'/></td><td width='82' align='right'><div id='" + this.div_table_id +"_options' class='yuimenubar yuimenubarnav'><div class='bd'><ul class='first-of-type'><li class='yuimenubaritem first-of-type'><a class='yuimenubaritemlabel' href='#" + this.div_table_id + "ot'>Options</a></li></ul></div></div></td></tr><tr><td style='line-height:11px;font-size:11px' id='" + this.div_table_id + "_flt' colspan='4'></td></tr></table>";
    }else{
    	this.paginator_html="<table width='100%'><tr><td id='" + this.div_table_id +"_xT_pT1'>{FirstPageLink} {PreviousPageLink} {PageLinks} {NextPageLink} {LastPageLink} {RowsPerPageDropdown} <strong>{CurrentPageReport}</strong></td><td align='right'><div id='" + this.div_table_id +"_sv'></div></td><td align='right' id='" + this.div_table_id +"_xT_pT3'><input type='hidden' id='" + this.div_table_id +"_reload' value='Reload'/></td><td width='82' align='right'></td></tr><tr><td style='line-height:10px;font-size:9px' id='" + this.div_table_id + "_flt' colspan='4'></td></tr></table>";
    }

    // Set up the Paginator instance.
    this.paginator = new YAHOO.widget.Paginator({
						  containers         : [this.div_table_id + "_p"],
						  pageLinks          : 5,
						  initialPage		   : (this.config.initialPage!=undefined)?this.config.initialPage:1,
						  rowsPerPage        : this.config.rowsPerPage,
						  rowsPerPageOptions : [10,20,40,100,500,5000],
	 						  template           : this.paginator_html,
 						  totalRecords       :  parseInt(this.initResults.ResultSet.totalRecords),
 						  pageReportTemplate : '{currentPage} of {totalPages} Pgs ({totalRecords} Rows)'
						});

    this.paginator.render();

    this.paginator.subscribe('changeRequest',this.handlePagination,this,true);

    this.config.dynamicData=true;

    if(this.height!=undefined){
      this.setHeight(this.height);
    }

    if(this.width!=undefined){
      this.setWidth(this.width);
    }

    //   		if(this.config.rowsPerPage>50 && YAHOO.env.ua.ie > 0){
    //   			this.config.renderLoopSize=100;
    //   			document.getElementById(this.div_table_id).style.backgroundColor='white';
    //   		}
    this.getPage(1);

    if(document.getElementById(this.div_table_id +"_reload")==undefined){
      if(this.options.showFilterDisplay){
    	  YAHOO.util.Event.onAvailable(this.div_table_id +"_flt",this.renderFilterDisplay,null,this);
      }
      if(this.options.showReload){
    	  YAHOO.util.Event.onAvailable(this.div_table_id +"_reload",this.renderReload,null,this);
      }
      if(this.options.showOptionsDropdown){
    	  YAHOO.util.Event.onAvailable(this.div_table_id +"_options",this.renderOptions,true,this);
      }
    }else{
      if(this.options.showFilterDisplay){
    	  this.renderFilterDisplay();
      }
      if(this.options.showReload){
    	  this.renderReload();
      }
      if(this.options.showOptionsDropdown){
    	  this.renderOptions();
      }
    }

      wrangleTabs('#search_tabs');

  };

    this.getPage = function (page) {
        var that = this;
        var initFailure = function (o) {
            if (!window.leaving) {
                if (o.status == 401) {
                    showMessage("page_body", "Exception", "WARNING: Your session has expired.  You will need to re-login and navigate to the content.");
                    window.location = serverRoot + "/app/template/Login.vm";
                }
                /**
                 * If the returnes status is 500, one of the sortBy settings in the cookie was incorrect. In this case
                 * just ignore the cookie and re-run the request.
                 */
                else if (o.status == 500) {
                    var url = that.searchURI + "?format=xList&offset=" + ((page - 1) * that.config.rowsPerPage) + "&limit=" + that.config.rowsPerPage;

                    url += '&XNAT_CSRF=' + csrfToken;

                    YAHOO.util.Connect.asyncRequest('GET', url, initCallback, null, that);
                }
                else {
                    document.getElementById(this.div_table_id).innerHTML = "Failed to create search results.";
                }
            }
        };
        this.onInit.fire();
        var initCallback = {
            success: this.showPage,
            failure: initFailure,
            cache: false, // Turn off caching for IE
            scope: this
        };

        openModalPanel("load_data", "Loading data...");
        //xModalLoadingOpen({title:'Loading data...'});

        this.purge();

        document.getElementById(this.div_table_id).innerHTML = "";
        var url2 = this.searchURI + "?format=xList" + this.generateRequest(page);

        url2 += '&XNAT_CSRF=' + csrfToken;

        YAHOO.util.Connect.asyncRequest('GET', url2, initCallback, null, this);
    };

    this.generateRequest=function(page){
    var request="&offset=" + ((page-1)*this.config.rowsPerPage) + "&limit="+this.config.rowsPerPage;
    if(this.config.sortedBy!=undefined && this.config.sortedBy.key!=undefined){
      request+="&sortBy="+this.config.sortedBy.key;
      if(this.config.sortedBy.dir!=undefined)request+="&sortOrder="+this.config.sortedBy.dir;
    }
    return request;
  }

  var onXChange=function(p_sType, p_aArgs,  p_oButton) {
    var nX = this.cfg.getProperty("x"),	oIFrame;
    var parentContainer = this.cfg.getProperty("container");
    var parentXY = YAHOO.util.Dom.getXY(parentContainer.id);

    if (nX != undefined && nX != null) {

      nX = parentXY[0];
      Dom.setX(this.element, nX);
      // Sync the position of the iframe shim if it is enabled
      oIFrame = this.iframe;
      if (oIFrame) {
	Dom.setX(oIFrame, nX);
      }
      // Sync the value of the "x" property to the new, correct value
      this.cfg.setProperty("x", nX, true);

    }
  };

  var onYChange=function(p_sType, p_aArgs,  p_oButton) {
    var nY = this.cfg.getProperty("y"),	oIFrame;
    var parentContainer = this.cfg.getProperty("container");
    var parentRegion = YAHOO.util.Dom.getRegion(parentContainer.id);

    if (nY != undefined && nY != null) {

      nY = parentRegion.bottom;
      Dom.setY(this.element, nY);
      // Sync the position of the iframe shim if it is enabled
      oIFrame = this.iframe;
      if (oIFrame) {
	Dom.setY(oIFrame, nY);
      }
      // Sync the value of the "y" property to the new, correct value
      this.cfg.setProperty("y", nY, true);

    }
  };

  this.showPage=function(obj){
      closeModalPanel("load_data");
      //xModalLoadingClose();
      this.startTime = (new Date()).getTime();
    var dt=document.getElementById(this.div_table_id);
    dt.innerHTML=obj.responseText;
    //alert(dt.innerHTML);
    var tbl=dt.getElementsByTagName("TABLE")[0];
    var sprite=serverRoot+"/scripts/yui/build/assets/skins/xnat/xnat-sprite.png";
    var thead=tbl.getElementsByTagName("THEAD")[0];
    var tr=thead.getElementsByTagName("TR")[0];

    for(this.theadCount=0;this.theadCount<tr.getElementsByTagName("TH").length;this.theadCount++){
      var th=tr.getElementsByTagName("TH")[this.theadCount];

      th.style.background="#D8D8DA url(" + sprite + ") repeat-x scroll 0 0";
      th.style.fontWeight="500";
      th.style.fontSize="11px";
      th.style.lineHeight="13px";
      th.style.cursor="pointer";

      if(!th.id.match('^diff_')){ // The Diff column is metadata and sorting/filtering operations do not work with it.
          var _th_menu=new YAHOO.widget.Menu(th.id +"_cm",{container:th,context:[th,"tl","bl"],lazyload:true,itemdata:this.getMenuItems()});
          th.contextMenu=_th_menu;
          _th_menu.render(this.div_table_id);
          _th_menu.cfg.subscribeToConfigEvent("x", onXChange, this);
          _th_menu.cfg.subscribeToConfigEvent("y", onYChange, this);

          _th_menu.clickEvent.subscribe(onContextMenuClick,{field:th,dt:this}, this);
          YAHOO.util.Event.addListener(th, "click", _th_menu.show,null,_th_menu);
      }
    }
    //alert(out_txt);

    try{
    	//resize
	    if(!XNAT.app.resizableTables.contains(this.div_table_id)){
	    	XNAT.app.resizableTables.push(this.div_table_id);
	    }

	    XNAT.app.tableSizer();
    }catch(e){}
    if(XNAT.app.tablePageLoadListeners){
        $(XNAT.app.tablePageLoadListeners).each(function(i, listener){
           if($.isFunction(listener)){
               listener(_div_table_id);
           }
        });
    }
  };

  this.purge=function(){
    var dt=document.getElementById(this.div_table_id);
    if(dt==undefined)return;
    var tbl=dt.getElementsByTagName("TABLE")[0];
    if(tbl==undefined)return;
    var thead=tbl.getElementsByTagName("THEAD")[0];
    if(thead==undefined)return;
    var tr=thead.getElementsByTagName("TR")[0];
    if(tr==undefined)return;


    for(this.theadCount=0;this.theadCount<tr.getElementsByTagName("TH").length;this.theadCount++){
      var th=tr.getElementsByTagName("TH")[this.theadCount];

      if(th.contextMenu!=undefined){
	th.contextMenu.destroy();
	try{
	  YAHOO.util.Event.removeListener(th, "click");
	  YAHOO.util.Event.purgeElement(th.id +"_cm", true);
	  document.getElementById(th.id +"_cm").innerHTML="";
	}catch(e){}
      }

    }
  };

  this.sort=function(e,o){
    if(o.config.sortedBy==undefined){
      o.config.sortedBy={key:this.id,dir:"ASC"};
    }else{
      if(o.config.sortedBy.key==this.id){
	if(o.config.sortedBy.dir=="DESC"){
	  o.config.sortedBy.dir="ASC";
	}else{
	  o.config.sortedBy.dir="DESC";
	}
      }else{
	o.config.sortedBy.key=this.id;
	o.config.sortedBy.dir="ASC";
      }
    }
    o.paginator.set('page',0);
    o.getPage(1);
  }

  this.handlePagination=function(state){
    if(state.rowsPerPage!=this.config.rowsPerPage){
      if(((state.rowsPerPage>20 && this.theadCount>60) || (state.rowsPerPage>50 && this.theadCount>40)) && state.rowsPerPage>this.config.rowsPerPage){
 	if(!confirm("WARNING: Large data-sets may not perform well when to many rows are displayed.  Are you sure you want to increase the number of rows displayed?"))
 	  return;
      }
      this.config.rowsPerPage=state.rowsPerPage;
      this.paginator.set('page',0);
      this.getPage(1);
      this.paginator.set('rowsPerPage',this.config.rowsPerPage);
    }else{
      this.paginator.setState(state);
      this.getPage(state.page);
    }
  };

  this.renderFilterDisplay=function(){
    this.loadSearchManager();
    var flt=document.getElementById(this.div_table_id +"_flt");
    var filter=this.sm.searchDOM.renderFilterDisplay();

    var limitText = function(text, limit) {
      if (text.length > limit) {
   	return text.slice(0, limit) + "...";
      }
      return text;
    };

    if(filter==""){
      flt.parentNode.style.display="none";
    }else{
      flt.parentNode.style.display="block";
      var fullFltId = this.div_table_id + "_flt_full";
      var fltHTML = "<span title=\"Click to expand\" style=\"cursor: pointer;\" onclick=\"this.style.display='none';document.getElementById('" + fullFltId + "').style.display='inline';\">" + limitText(filter, 250) + "</span>";
      fltHTML += "<span id=\"" + fullFltId + "\" style=\"display:none\">" + filter + "</span>";
      flt.innerHTML = fltHTML;
    }
  };


  this.parseValueFromURI=function(str,name){
    var limitIndex=str.indexOf(name+"=");
    if(limitIndex>-1){
      var parsed=str.substring(limitIndex+name.length +1);
      if(parsed.indexOf("&")>-1){
	parsed=parsed.substring(0,parsed.indexOf("&"));
      }
      return parsed;
    }
    return undefined;
  };

  this.spreadsheetClick=function(name, eventObj, menuItem){
    this.sendSearch(serverRoot +"/app/action/CSVAction",this.getXML(),document.getElementById(this.div_table_id));
    return true;
  };

  this.loadSearchManager=function(){
    //if(this.sm==undefined){
    dynamicJSLoad('SearchXMLManager','search/searchManager.js');
    this.sm = new SearchXMLManager(this.xml);
    this.sm.onsubmit.subscribe(function(obj1,obj2,obj3){
				 obj3.xml=this.searchDOM.toXML("");
				 obj3.onXMLChange.fire();
				 this.destroy();
				 obj3.init({reload:true});
			       },this);
    this.sm.init();
    //}
    this.sm.tableName=this.initResults.ResultSet.ID;
  };

  this.editClick=function(name, eventObj, menuItem){
    //this.sendSearch(serverRoot +"/app/action/DisplaySearchAction",this.xml,document.getElementById(this.div_table_id));
    this.loadSearchManager();
    this.sm.render();
    return false;
  };

  this.showXMLClick=function(name, eventObj, menuItem){
    this.showXML(this.getXML());
    return true;
  };

  this.downloadClick=function(name, eventObj, menuItem){
    this.sendSearch(serverRoot +"/app/action/DownloadSessionsAction",this.getXML(),document.getElementById(this.div_table_id));
    return true;
  };

  this.menuSend=function(name,eventObj,menuItem){
    this.sendSearch(serverRoot +"/app/action/"+menuItem.value,this.getXML(),document.getElementById(this.div_table_id));
    return true;
  };

  this.emailClick=function(name, eventObj, menuItem){
    dynamicJSLoad("EmailPopupForm","search/emailSearch.js");
    var emailPopup=new EmailPopupForm(this.getXML());

    emailPopup.init();
    emailPopup.render();
    return true;
  };

  this.onRemoveRequest=new YAHOO.util.CustomEvent("remove-request",this);

  this.deleteClick=function(name, eventObj, menuItem){
    if(this.obj.SS_ID){
      if(confirm("Are you sure you want to delete this stored search?"))
      {
	var callback={
	  success:function(o){
	    this.arguments.dts.onRemoveRequest.fire();
	  },
	  failure:function(o){

	  },
        cache:false, // Turn off caching for IE
	  arguments:{"dts":this}
	};
	YAHOO.util.Connect.asyncRequest('DELETE',serverRoot +'/REST/search/saved/' + this.obj.SS_ID + '?format=json&XNAT_CSRF='+csrfToken,callback,null,this);
      }
    }
    return true;
  };

  this.onSavedSearch=new YAHOO.util.CustomEvent("saved-search",this);
  this.saveClick=function(name, eventObj, menuItem){
    dynamicJSLoad("SavePopupForm","search/saveSearch.js");
    var savePopup=new SavePopupForm(this.getXML(),undefined,{"saveAs":false});
    savePopup.onSavedSearch.subscribe(function(o){
					return this.onSavedSearch.fire();
				      },this,this);
    savePopup.init();
    savePopup.render();
    return true;
  };

  this.saveAsClick=function(name, eventObj, menuItem){
    dynamicJSLoad("SavePopupForm","search/saveSearch.js");
    var savePopup=new SavePopupForm(this.getXML(),undefined,{"saveAs":true});
    savePopup.onSavedSearch.subscribe(function(o){
					return this.onSavedSearch.fire();
				      },this,this);
    savePopup.init();
    savePopup.render();
    return true;
  };

  this.addColumnsClick=function(name, eventObj, menuItem){
    this.loadSearchManager();
    this.sm.renderAddFields(false);
    return false;
  };

  this.joinClick=function(name, eventObj, menuItem){
    this.loadSearchManager();
    this.sm.renderJoinForm();
    return false;
  };


  //insert reload button
  this.renderReload=function(obj1,obj2){
    var buttonConfig=new Object();
    buttonConfig.type="push";
    if(this.initResults.ResultSet.last_access!=undefined){
      buttonConfig.type="link";
      buttonConfig.title="Last Update: " + this.initResults.ResultSet.last_access;
    }
    this.reloadButton=new YAHOO.widget.Button(this.div_table_id +"_reload",buttonConfig);
    this.reloadButton.subscribe("click",function(e,obj1,obj2){
				  this.init({reload:true});
				},null,this);
  };

  //insert option menu
  this.renderOptions=function(obj1,obj2){
    this.optionMenu=null;
    this.optionMenu=new YAHOO.widget.MenuBar(this.div_table_id +"_options",{hidedelay:750});
    this.optionMenu.search=this;
    this.optionMenu.en=this.initResults.ResultSet.rootElementName;

    this.optionMenu.subscribe("beforeRender",function(){
				if(this.getRoot()==this){
				  try{
				    var submenuitems= new Array();
				    submenuitems.push({text:'Spreadsheet',onclick:{fn:this.search.spreadsheetClick,scope:this.search}});

				    var spec=window.available_elements.getByName(this.en);
				    if(spec!=null || this.en === "wrk:workflowData") {
				      submenuitems.push({text:'Email',onclick:{fn:this.search.emailClick,scope:this.search}});

				      submenuitems.push({text:'Save Search',onclick:{fn:this.search.saveClick,scope:this.search}});
				      submenuitems.push({text:'Save as New Search',onclick:{fn:this.search.saveAsClick,scope:this.search}});
				      submenuitems.push({text:'Show XML',onclick:{fn:this.search.showXMLClick,scope:this.search}});

				      if((this.en.indexOf("SessionData")>-1))
				      {
					submenuitems.push({text:'Download',onclick:{fn:this.search.downloadClick,scope:this.search}});
				      }

				      if(this.search.obj.ID.startsWith("ss.")){
					submenuitems.push({text:'Delete Saved Search',onclick:{fn:this.search.deleteClick,scope:this.search}});
				      }

				      submenuitems.push({text:'Edit Columns',onclick:{fn:this.search.addColumnsClick,scope:this.search}});
				      submenuitems.push({text:'Join to ...',onclick:{fn:this.search.joinClick,scope:this.search}});


				      if(this.search.options!=undefined && this.search.options!=null && this.search.options.length==0){
					submenuitems=Array.concat(submenuitems,this.search.options);
				      }

				      if(spec!=null && spec.actions!=undefined && spec.actions.length>0){
					for(var sC=0;sC<spec.actions.length;sC++){
					  submenuitems.push({value:spec.actions[sC].action,text:spec.actions[sC].display,onclick:{fn:this.search.menuSend,scope:this.search}});
					}
				      }

                        var menuOptions = getSearchMenuOptions();
                        if (menuOptions) {
                            for (var index = 0, total = menuOptions.length; index < total; index++) {
                                try {
                                    var menuOption = menuOptions[index];
                                    var fnName = 'this.search.handle' + menuOption.label.replace(/\s+/g, '');
                                    eval(fnName + ' = menuOption.handler');
                                    submenuitems.push({text: menuOption.label, onclick: { fn: eval(fnName), scope: this.search}});
                                } catch (e) {
                                showMessage("page_body", "Exception", e.message);
                                }
                            }
                        }
                    }


				    var submenu={
				      id: this.search.div_table_id + "ot",
				      itemdata: submenuitems
				    };
				    this.getItem(0).cfg.setProperty("submenu",submenu);
				  }catch(e){
                      showMessage("page_body", "Exception", e.message);
                  }
				}
			      });
    this.optionMenu.render();
  };

    this.showXML=function (_searchXML){
        showMessage("page_body", "Search XML", "<code id='showXmlContent'></code>", {width:750, height:500});
        var bracketregex = new RegExp('<', 'g');
        var newlineregex = new RegExp('\n', 'g');
        $('#showXmlContent').html(_searchXML.replace(bracketregex, '&lt;').replace(newlineregex, '<br />'));
    };

  this.getXML=function(){
	  return this.xml;
  }
  this.sendSearch=function ( _url, _searchXML, divContent){
    var tempForm = document.createElement("FORM");
    tempForm.method="POST";
    tempForm.action=_url;

    var tempInput=document.createElement("INPUT");

    tempInput.type="hidden";
    tempInput.name="search_xml";
    tempInput.value=_searchXML;

    tempForm.appendChild(tempInput);


    var cs = document.createElement("input");
	cs.type = "hidden";
	cs.name = "XNAT_CSRF";
	cs.value = csrfToken;
	tempForm.appendChild(cs);


    if(divContent!=undefined)
      divContent.appendChild(tempForm);

    tempForm.submit();
  };

  this.setHeight=function(_height){
    this.height=_height;
    if(document.getElementById(this.div_table_id + "_c")!=undefined){
      if(this.initResults!=undefined){//removed numRows>10 check, caused collapse of datatable in IE
   	document.getElementById(this.div_table_id + "_c").style.height=this.height;
      }
    }
  };

  this.setWidth=function(_width){
    this.width=_width;
    if(document.getElementById(this.div_table_id + "_c")!=undefined){
      document.getElementById(this.div_table_id + "_c").style.width=this.width;
    }
  };

  this.getColumnByKey=function(k){
    for(var rcC=0;rcC<this.initResults.ResultSet.Columns.length;rcC++){
      var tcol=this.initResults.ResultSet.Columns[rcC];
      if(tcol.key==k){
	return tcol;
      }
    }
  }

  this.getColumns=function(k){
	var columns=new Array();
    for(var rcC=0;rcC<this.initResults.ResultSet.Columns.length;rcC++){
      columns.push(this.initResults.ResultSet.Columns[rcC]);
    }
    return columns;
  }

  this.getMenuItems=function(){
	  var cMenuItems=[{text:"Sort Up"},{text:"Sort Down"}];

	  if(this.options.allowInTableMods){
          if(this.optionMenu.en!="xnat:projectData"){
              cMenuItems.push({text:"Hide Column"});
              cMenuItems.push({text:"Edit Column"});
              cMenuItems.push({text:"Filter"});
          }
	  }
	  return cMenuItems;
  }
}

var CookieFunctions = {
    //Convert an alist like "name=value&some_other_name=some_other_value" to
    //"{name : value, some_other_name : some_other_value}
    alistToJSON : function (alist) {
      var alistObject = new Object();
      if (alist != undefined) {
	var pairs = alist.split("&");
	for (var i = 0; i < pairs.length ; i++) {
	  if (pairs[i] != undefined) {
	    var end_of_key = pairs[i].indexOf("=");
	    if (end_of_key > -1) {
	      var key = pairs[i].substring(0,end_of_key);
	      var value = pairs[i].substring(end_of_key+1);
	      alistObject[key]=value;
	    }
	  }
	}
	return alistObject;
      }
      else {
	return undefined;
      }
    },
    // Convert a json object back into an alist string.
    // ex. "{name : value, some_other_name : some_other_value} =>
    //     "name=value&some_other_name=some_other_value"
    // Note that there is no support for properly translating
    // JSON arrays or nested JSON objects.
    jsonToAlist : function (json) {
      if (json != undefined) {
    	var final_string = "";
    	for (k in json) {
    	  final_string = final_string + k + "=" + json[k] + "&";
    	}
    	return final_string;
      } else {
	return undefined;
      }
    },
    set_cookie : function (cookie_name, key, value) {
      var cookie = YAHOO.util.Cookie.get(cookie_name);
      var alist = undefined;
      if (cookie != undefined) {
	var alistObject = CookieFunctions.alistToJSON(cookie);
	alistObject[key] = value;
	alist = CookieFunctions.jsonToAlist(alistObject);
      }
      else {
	var o = new Object();
	o[key] = value;
 	alist = CookieFunctions.jsonToAlist(o);
      }
      YAHOO.util.Cookie.remove(cookie_name);
      YAHOO.util.Cookie.set(cookie_name,alist, {expires : new Date ("Januarey 25 2025")});
    }
  };



var onContextMenuClick=function(p_sType, p_aArgs, o){
  var task=p_aArgs[1];
  var cookie_name = this.div_table_id + ".initialRequest";
  if(task) {
    if(o.dt.config.sortedBy==undefined)o.dt.config.sortedBy=new Object();
    // Extract which TR element triggered the context menu
    colRow = o.field;
    if(colRow) {
      switch(task.index) {
      case 2:
        //HIDE Column
        if(o.dt.config.sortedBy.key==colRow.getAttribute("name")){
          xModalMessage('Search Table', "Unable to remove a sorted column. Please sort by a different column and retry.");
        }else{
   	  o.dt.loadSearchManager();
   	  var oColumn= o.dt.getColumnByKey(colRow.getAttribute("name"));
   	  o.dt.sm.searchDOM.removeField(oColumn.element_name,oColumn.id);
   	  o.dt.xml=o.dt.sm.searchDOM.toXML("");
	  o.dt.init({reload:true});
	  YAHOO.util.Cookie.set(cookie_name);
        }
        break;
      case 4:
        //Filter Column
   	var oColumn= o.dt.getColumnByKey(colRow.getAttribute("name"));
        o.dt.loadSearchManager();
        o.dt.sm.renderFilterForm(oColumn.element_name,oColumn.id,oColumn);
        break;
      case 0:
        //sort ASC Column
        o.dt.config.sortedBy.key=colRow.getAttribute("name");
        o.dt.config.sortedBy.dir="ASC";

	o.dt.paginator.set('recordOffset',0);
	o.dt.getPage(1);
	CookieFunctions.set_cookie(cookie_name, "sortBy", colRow.getAttribute("name"));
	CookieFunctions.set_cookie(cookie_name, "sortOrder", "ASC");
        break;
      case 1:
        //sort DESC Column
        o.dt.config.sortedBy.key=colRow.getAttribute("name");
        o.dt.config.sortedBy.dir="DESC";

	o.dt.paginator.set('recordOffset',0);
	o.dt.getPage(1);
	CookieFunctions.set_cookie(cookie_name, "sortBy", colRow.getAttribute("name"));
	CookieFunctions.set_cookie(cookie_name, "sortOrder", "DESC");
        break;
      case 3:
        //Edit Columns
    	var oColumn= o.dt.getColumnByKey(colRow.getAttribute("name"));
        o.dt.loadSearchManager();
        o.dt.sm.renderAddFields(false);
        break;
      }
    }

  }
};
