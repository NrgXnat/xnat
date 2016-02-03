/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/tabManager.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 10/3/13 4:26 PM
 */
dynamicJSLoad("DataTableStoredSearch","search/dataTableStoredSearch.js");

var DEBUG=false;


function TabManager(_id){

    if (_id == undefined) {
        this.id = "search_tabs";
    }
    else {
        this.id = _id;
    }
    var navset_id = this.id;

    this.loaded = new Array();
    this.tabView = new YAHOO.widget.TabView(this.id);
    this.tabView.addListener('beforeActiveTabChange', function (e) {
        var dataSrc = e.newValue.get("dataSrc");
        if (e.newValue.search_id != undefined || dataSrc != undefined)
            return true;
        else
            return false;
    });

    this.suppress_select=false;

  this.dataTables=new Array();
  
  this.csrfToken=null;

  this.init=function(array){
    if(array==undefined){
      this.stored_searches=new Array();
    }else{
      this.stored_searches=array;
    }
    YAHOO.util.Event.addListener(window,'resize',this.resizeDataTable,this,this);
    this.resizeDataTable("resize",this);
    this.render();
  };

  this.setCsrfToken=function(str){
	  this.csrfToken=str;
  };
  this.isLoaded=function(str){
    return this.loaded.contains(str);
  };

  this.open=function(obj){
    this.loaded.push(obj.ID);
    this.setOpenTabCookie();
    this.render();
    this.addTab(obj);
  };

  this.onOpenTabsChange=new YAHOO.util.CustomEvent("open-tabs-change",this);
  this.setOpenTabCookie=function(){
    this.onOpenTabsChange.fire();
  };

  this.onTabClose=new YAHOO.util.CustomEvent("close-tab",this);
  this.onTabDelete=new YAHOO.util.CustomEvent("delete-tab",this);
  this.onTabModification=new YAHOO.util.CustomEvent("modify-tab",this);

  this.close=function(_id){
    this.loaded.splice(ArrayIndexOf(this.loaded,_id),1);
    this.setOpenTabCookie();
    this.render();
    this.onTabClose.fire(this.retrieveSearchSpecification({ID:_id}));
  };

  this.retrieveSearchSpecification=function(obj){
    for(var selectCounter=0;selectCounter<this.stored_searches.length;selectCounter++){
      if(this.stored_searches[selectCounter].label==obj.label || this.stored_searches[selectCounter].ID==obj.ID){
        found=true;
        obj=this.stored_searches[selectCounter];
        break;
      }
    }
    return obj;
  };

  this.load=function(obj){
    var found=false;

    if(this.stored_searches==undefined){
      this.stored_searches=new Array();
    }

    if(!this.isLoaded(obj.ID)){
      for(var selectCounter=0;selectCounter<this.stored_searches.length;selectCounter++){
	if(this.stored_searches[selectCounter].label==obj.label || this.stored_searches[selectCounter].ID==obj.ID){
	  found=true;
	  this.open(this.stored_searches[selectCounter]);
	  break;
	}
      }

      if(!found && obj.label!=undefined){
	this.stored_searches.push(obj);
	this.open(obj);
      }
    }else{
      this.tabView.set('activeIndex',this.loaded.indexOf(obj.ID),true);
    }
  };

    this.render = function () {

        if (this.selector == undefined) {
            this.selector = document.createElement("select");
            this.selector.id = "search_selector";
            this.selector.className = "select_add_tab";
            //this.selector.style.fontSize="8px";
            //this.selector.style.lineHeight="9px";
            //this.selector.style.fontWeight="700";
            this.selector.tab_manager = this;

            this.selectorTab = new YAHOO.widget.Tab({label: "+", content: "", disabled: true});
            //var em = this.selectorTab.get('labelEl');
            //var em = $(this.selectorTab).closest('.yui-navset');
            //em.appendChild(this.selector);

            // putting the "Add Tab" <select> floating in the "#search_tabs" element
            // so if the '.yui-navset' needs this, it needs to have id="search_tabs"
            if (!this.suppress_select) {
                var search_tabs = document.getElementById(navset_id);
                search_tabs.appendChild(this.selector);
            }

            // fix for select bug in firefox (http://yuilibrary.com/projects/yui2/ticket/2528333)
            //em.parentNode.removeAttribute('href');

            YAHOO.util.Event.on(this.selector, 'change', function (e) {
                if (this.selectedIndex > 0) {
                    var tempSearch = this.options[this.selectedIndex].value;
                    this.tab_manager.load({ID: tempSearch});
                }
            });
        }

        while (this.selector.options.length > 0) {
            this.selector.remove(0);
        }

        this.selector.options[0] = new Option("Add Tab", "");

        var optionCount = 1;

        // alphabetical sort function used to sort the "AddTab" dropdown list.
        function alphabeticalsort(a,b){
            var z = 0;
            if (a.label.toLowerCase() < b.label.toLowerCase()){ z = -1; }
            if (a.label.toLowerCase() > b.label.toLowerCase()){ z = 1; }
            if (a.label.toLowerCase() == b.label.toLowerCase()){ z = 0; }
            return (z);
        }
        // Sort the list
        this.stored_searches.sort(alphabeticalsort);

        for (var selectCounter = 0; selectCounter < this.stored_searches.length; selectCounter++) {
            if (!this.isLoaded(this.stored_searches[selectCounter].ID)) {
                this.selector.options[optionCount++] = new Option(this.stored_searches[selectCounter].label, this.stored_searches[selectCounter].ID);
            }
        }

        if (this.selector.options.length == 1) {
            if (this.tabView.get("tabs").length > 0 && this.tabView.getTabIndex(this.selectorTab) != null){
                this.tabView.removeTab(this.selectorTab);
            }
        }
        else {
            if (!this.suppress_select) {
                var tab_index = this.tabView.getTabIndex(this.selectorTab);
                if (tab_index == null){
                    this.tabView.addTab(this.selectorTab);
                }
            }
        }
    };


    this.addTab = function (obj) {
        var _id = obj.ID;
        if (obj.content) {
            this.tabView.addTab(new YAHOO.widget.Tab({
                label: obj.label + "<span style='height:12px;width:1px;'></span>",
                dataSrc: obj.content,
                active: true
            }));
        }
        else {
            var tempTab = new YAHOO.widget.Tab({
                label: obj.label + '&nbsp;<span class="close"><img src="' + server + 'close.gif"/></span>',
                content: '<div id="' + _id + '_dt_p" class="xT_p"></div><div id="' + _id + '_dt_c" class="xT_c" style="overflow:auto;"><div id="' + _id + '_dt" class="xT_dt">Preparing Results</div></div>',
                active: true
            });

            tempTab.search_id = _id;
            tempTab.tab_manager = this;

            this.tabView.addTab(tempTab, this.loaded.length - 1);
            //this.tabView.set('activeTab',tempTab,true);
            //this.tabView.addTab(tempTab);

            YAHOO.util.Event.on(tempTab.getElementsByClassName('close')[0], 'click', function(e, tab) {

                var $this_tab = $(this).closest('li');
                var $this_navset = $this_tab.closest('.yui-navset');
                $this_navset.find('li').removeClass('dont_move');
                if ($this_tab.attr('title')==='active'){
                    $this_tab.prev('li').attr('title','active');
                }
                YAHOO.util.Event.preventDefault(e);
                tab.tab_manager.tabView.removeTab(tab);
                tab.tab_manager.close(tab.search_id);

                var $target_tab = $this_navset.find('li[title="active"]');

                var navset_position = $this_navset.offset();
                var target_tab_position = $target_tab.offset();

                if (navset_position.left < target_tab_position.left){
                    $target_tab.addClass('dont_move');
                }

                $target_tab.trigger('click');

            }, tempTab);


            var options = new Array();
            var config = new Object();
            config.csrfToken = this.csrfToken;

            tempTab.dt = new DataTableStoredSearch(_id + "_dt", obj, config);

            tempTab.dt.onComplete.subscribe(function (o) {
                this.resizeDataTable("resize", this);
            }, this, this);

            tempTab.dt.onRemoveRequest.subscribe(function (o) {
                this.tab_manager.tabView.removeTab(this);
                this.tab_manager.close(this.search_id);
                this.tab_manager.onTabDelete.fire(this.tab_manager.retrieveSearchSpecification({ID: this.search_id}));
            }, tempTab, tempTab);

            tempTab.dt.onSavedSearch.subscribe(function (o) {
                this.tab_manager.onTabModification.fire(this.tab_manager.retrieveSearchSpecification({ID: this.search_id}));
            }, tempTab, tempTab);


            this.resizeDataTable("resize", this);

            if (this.height != undefined) {
                tempTab.dt.setHeight(this.height);
            }
            if (this.width != undefined) {
                tempTab.dt.setWidth(this.width);
            }

            tempTab.dt.init();
        }
    };

    function debug_out(msg){
    var log_div=document.getElementById("mylogger");
    if(log_div==undefined){
      log_div=document.createElement("div");
      document.firstChild.appendChild(log_div);
    }
    log_div.innerHTML+=msg;
  }

  this.setHeight=function(_int){
    this.height=_int;
    var tabs =this.tabView.get("tabs");
    for(var tabC=0;tabC<tabs.length;tabC++){
      if(tabs[tabC].dt)
   	tabs[tabC].dt.setHeight(_int);
    }
  };

  this.setWidth=function(_int){
    this.width=_int;
    var tabs =this.tabView.get("tabs");
    for(var tabC=0;tabC<tabs.length;tabC++){
      if(tabs[tabC].dt)
   	tabs[tabC].dt.setWidth(_int);
    }
  };

  // Wrapper around YAHOO.widget.TabView's set function
  this.setActiveTab = function (tab_index){
    this.tabView.set('activeIndex', tab_index);
  };

  this.resizeDataTable=function (obj1,obj2){
    var viewport = {height:parseInt(YAHOO.util.Dom.getClientHeight()),width:parseInt(YAHOO.util.Dom.getClientWidth())};
    var pos = YAHOO.util.Dom.getXY(this.id);
    var h=viewport.height-parseInt(pos[1]) -100;
    var w=viewport.width-parseInt(pos[0]) -60;
    this.setHeight(h);
    this.setWidth(w);
  };
}
