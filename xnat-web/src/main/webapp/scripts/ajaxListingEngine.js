/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/ajaxListingEngine.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */

var closeLocation = serverRoot+"/images/close.gif";
var DEBUG=false;
//SearchCollection contains an array of searches
//SearchCollection.selectedSearch is the currently active search.
function SearchCollection(_reqID){
  this.reqID = _reqID;
    
  this.searches = new Array();
  
  function addSearch(id,desc,divTitle,divContent,divOptions){
    var temp = new SearchManager(id,desc,divTitle,divContent,divOptions);
    temp.parent =this;
    this.searches.push(temp);
  }
  this.addSearch=addSearch;
  
  function addSearchWObject(id,desc,divTitle,divContent,divOptions,searchDOM){
    var manager = new SearchManager(id,desc,divTitle,divContent,divOptions);
    manager.setSearchMetaData(searchDOM);
    this.searches.push(manager);
  }
  this.addSearchWObject=addSearchWObject;
  
  function getSearch(id){
    for (search_count=0;search_count<this.searches.length;search_count++) {
      if (this.searches[search_count].id==id){
         return this.searches[search_count];
      }
    }
  }
  this.getSearch=getSearch;
  
  function selectAndDisplaySearch(search){
    this.selectedSearch=search;
    
    search.populateOptions();
  }
  this.selectAndDisplaySearch=selectAndDisplaySearch;
  
  function select(bundle){
     
      for (y=0;y<this.searches.length;y++)
	  {
	    var search=this.searches[y];
	    if (bundle==search.id)
	    {
	       this.selectAndDisplaySearch(search);
	       if (search.ready || !search.afterInit){
	         document.getElementById(search.id + '_CONTENT').style.display='block';
	         if (!search.complete){
	           document.getElementById(search.id + '_LINK').className='titleBarGrey';
               this.selectedSearch.loadPage(0);
	         }else{
	           document.getElementById(search.id + '_LINK').className='titleBarText';
	         }
	       }else{
	         document.getElementById(search.id + '_CONTENT').style.display='none';
	         if (document.getElementById(search.id + '_LINK').className=='titleBarText')
	           document.getElementById(search.id + '_LINK').className='titleBarLink';
	       }
	    }else{
	       document.getElementById(search.id + '_CONTENT').style.display='none';
	       if (document.getElementById(search.id + '_LINK').className=='titleBarText')
	           document.getElementById(search.id + '_LINK').className='titleBarLink';
	    }
	  }
  }
  this.select=select;
  
  function loadAllSearchMetaData(){
    for (search_count=0;search_count<this.searches.length;search_count++) {
      this.searches[search_count].loadSearchMetaData();
    }
  }
  this.loadAllSearchMetaData=loadAllSearchMetaData;
  
  function populateAllSearches(){
    for (search_count=0;search_count<this.searches.length;search_count++) {
      this.searches[search_count].populate();
    }
  }
  this.populateAllSearches=populateAllSearches;
  
  function loadFirstPages(elementName){
   if(elementName==null)
   {
    for (search_count=0;search_count<this.searches.length;search_count++) {
        this.searches[search_count].loadPage(0);
    }
   }else{
    for (search_count=0;search_count<this.searches.length;search_count++) {
      if (this.searches[search_count].id.indexOf(elementName)!=-1)
      {
        this.searches[search_count].isNew=true;
        this.searches[search_count].postLoadOtherPages=true;
        if(this.searches.length<5)this.searches[search_count].loadPage(0);
        this.selectAndDisplaySearch(this.searches[search_count]);
      }else{
        this.searches[search_count].isNew=true;
        this.searches[search_count].loadSearchMetaData();
      }
    }
    
    if (this.selectedSearch==null){
      this.selectAndDisplaySearch(this.searches[0]);
    }
    
    this.select(this.selectedSearch.id);
   }
  }
  this.loadFirstPages=loadFirstPages;
  
  
  function setRowsToDisplay(num){
    this.selectedSearch.numToDisplay=num;
    this.selectedSearch.populate();
    
    for (search_count=0;search_count<this.searches.length;search_count++) {
      if(this.searches[search_count].id!=this.selectedSearch.id){
        this.searches[search_count].numToDisplay=num;
        this.searches[search_count].populate();
      }
    }
  }
  this.setRowsToDisplay=setRowsToDisplay;
  
  window.collectionInstance=this;
}
function SearchManager(_bundleID,_description,_divTitle,_divContent,_divOptions){
  this.id=_bundleID;
  this.description = _description;
  this.divTitle = document.getElementById(_divTitle);
  this.divContent = document.getElementById(_divContent);
  this.divOptions = document.getElementById(_divOptions);
  this.searchDOM = null;
  
  this.preLoaded = false;
  this.populated = false;
  this.loaded = false;
  
  this.preLoading = false;
  this.populating = false;
  this.loading = false;
  this.complete = false;
  
  
  this.loadRequested=false;
  
  this._xmlReq = null;
  
  this.numPages = null;
  this.currentPage = 0;
  this.totalRecords = null;
  this.numToDisplay = null;
  this.afterInit=false;
    
  var instance = this;
  
  this.newTable = null;
    
    
  this.options = new Array();
    
  function email(toAddresses,subject,from,message,msgTab){
        this.msgTab=msgTab;
        msgTab.innerHTML="<DIV style='color:red'>Sending...</DIV>";
        if (window.XMLHttpRequest) {
	       this._emailReq = new XMLHttpRequest();
	    } else if (window.ActiveXObject) {
	       this._emailReq = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    
	    //SEND META-DATA REQUEST
	    this._emailReq.open("POST", url, true);
	    this._emailReq.onreadystatechange = this.emailCallback;
	    
        this._emailReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
        var email_url = "remote-class=org.nrg.xdat.ajax.EmailCustomSearch&remote-method=send";
        email_url +="&toAddress=" + toAddresses;
        email_url +="&subject=" + subject;
        email_url +="&message=" + message;
        email_url +="&from=" + from;
        email_url +="&search_xml=" + this.searchDOM.getXML();
        email_url +="&XNAT_CSRF="+csrfToken;
        this._emailReq.send(email_url);
        
  }  
  this.email=email;
  
  function emailCallback(){
    var req = instance._emailReq;
    if (req.readyState == 4) {
        if (req.status == 200) {
            // handle response 
            var xmlTEXT = req.responseText;
            if (xmlTEXT)
            {
              var table = document.createElement("table");
              var tbody = document.createElement("tbody");
              var tr = document.createElement("tr");
              var td1 = document.createElement("td");
              var td2 = document.createElement("td");
              td1.innerHTML=xmlTEXT;
              tr.appendChild(td1);
              
                var i =document.createElement("INPUT");
                i.type="button";
                i.value="CLOSE";
                i.onclick=function(){document.getElementById("emailTab").style.display='none';}
                td2.appendChild(i);
                
              tr.appendChild(td2);
              
              tbody.appendChild(tr);
              table.appendChild(tbody);
              
              instance.msgTab.appendChild(table);
			}
		}	
	}
  }
  this.emailCallback=emailCallback;
    
  //request search structure from server
  function loadSearchMetaData(){
    //load meta data
    if (!this.preLoading && !this.loading && !this.populating){
        this.afterInit=true;
    
	    this.preLoading = true;
	    
        if (window.XMLHttpRequest) {
	       this._xmlReq = new XMLHttpRequest();
	    } else if (window.ActiveXObject) {
	       this._xmlReq = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    
	    //SEND META-DATA REQUEST
	    this._xmlReq.open("POST", url, true);
	    this._xmlReq.onreadystatechange = this.metaDataCallback;
	    
        this._xmlReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
   
        this._xmlReq.send("remote-class=org.nrg.xnat.ajax.RequestSearchXML&remote-method=execute&bundleID=" + this.id + "&XNAT_CSRF="+csrfToken);
        
        this.divTitle.className="titleBarGrey";
    }
  }
  this.loadSearchMetaData=loadSearchMetaData;
  
  function metaDataCallback(){
    var req = instance._xmlReq;
    if (req.readyState == 4) {
        if (req.status == 200) {
            // handle response 
            var xmlDoc = req.responseXML;
            var xmlTEXT = req.responseText;
            //alert(xmlTEXT);
            //alert(xmlDoc);
            if (xmlDoc)
            {
               var bundleDOM = xmlDoc.getElementsByTagName("bundle")[0];
               if (bundleDOM)
               {
                var searchObj = new xdat_stored_search(bundleDOM);
                instance.setSearchMetaData(searchObj);      
                
			    instance.preLoading = false;
			    instance.preLoaded = true;
			    if (instance.loadRequested){
			      instance.loadPage(0);
			    }
			    
			    //alert(searchObj.getXML());
               }else{
                 //alert("ERROR: Unknown return type");
               }
			}
		}	
	}
  }
  this.metaDataCallback=metaDataCallback;
  
  function setSearchMetaData(_searchDOM){
    this.searchDOM = _searchDOM;
    this.ready =true;
    this.divTitle.className="titleBarLink";
  }
  this.setSearchMetaData=setSearchMetaData;
  
  function loadOptions(){
	    
        if (window.XMLHttpRequest) {
	       this._optionsReq = new XMLHttpRequest();
	    } else if (window.ActiveXObject) {
	       this._optionsReq = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    
	    //SEND META-DATA REQUEST
	    this._optionsReq.open("POST", url, true);
	    this._optionsReq.onreadystatechange = this.optionsCallback;
	    
        this._optionsReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
   
        this._optionsReq.send("remote-class=org.nrg.xnat.ajax.RequestSearchXML&remote-method=execute&bundleID=" + this.id + "&elementName=" + this.searchDOM.rootElementName + "&XNAT_CSRF="+csrfToken);
  }
  this.loadOptions=loadOptions;
  
  function optionsCallback(){
    var req = instance._optionsReq;
    if (req.readyState == 4) {
        if (req.status == 200) {
            // handle response 
            var xmlDoc = req.responseXML;
            var xmlTEXT = req.responseText;
            //alert(xmlTEXT);
            //alert(xmlDoc);
            if (xmlDoc)
            {
               
			}
		}	
	}
  }
  this.optionsCallback=optionsCallback;
  
  //populate the search on the server and return statistics
  function populate(){
    if (!this.preLoading && !this.loading && !this.populating){
    
      if(!this.preLoaded){
         this.loadRequested=true;
         this.loadSearchMetaData();
      }else{
        this.populating = true;
  		this.startTime = (new Date()).getTime();
	    
        if (window.XMLHttpRequest) {
	       this._xmlReq = new XMLHttpRequest();
	    } else if (window.ActiveXObject) {
	       this._xmlReq = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    
	    //SEND META-DATA REQUEST
	    this._xmlReq.open("POST", url, true);
	    this._xmlReq.onreadystatechange = this.populateCallback;
	    
        this._xmlReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
   
        var searchXML=this.searchDOM.getXML();
        var queryString = "remote-class=org.nrg.xnat.ajax.RequestSearchData&remote-method=init&search=" + searchXML + "&XNAT_CSRF="+csrfToken;
        if (this.numToDisplay){
          queryString +="&rows=" +this.numToDisplay; 
        }
        if (this.sortBy){
          queryString +="&sortBy=" +this.sortBy; 
        }
        if (this.sortOrder){
          queryString +="&sortOrder=" +this.sortOrder; 
        }
        if (this.isNew){
          queryString +="&isNew=" +this.isNew; 
        }
        this._xmlReq.send(queryString);
        
        
          this.divTitle.className="titleBarGrey";
        
	      clearDIV(this.divContent);
		  
		  this.divContent.innerHTML="<DIV>Loading...</DIV>"
		  
		  this.isNew=false;
      }
    }
  }
  this.populate=populate;
  
  function populateCallback(){
    var req = instance._xmlReq;
    if (req.readyState == 4) {
        if (req.status == 200) {            
            // handle response 
            
            var xmlDoc = req.responseXML;
            var xmlTEXT = req.responseText;
            if (xmlDoc)
            {
			    instance.populating = false;
			    instance.loadRequested = false;
			    instance.populated=true;
			    var completeTime = new Date();
			    
			    var results = xmlDoc.getElementsByTagName("results")[0];
			    if (results!=null && results != undefined){
			      instance.numPages =parseInt(getAttributeValue("numPages",results));
                  instance.currentPage =parseInt(getAttributeValue("currentPage",results));
                  instance.totalRecords =parseInt(getAttributeValue("totalRecords",results));
                  instance.numToDisplay =parseInt(getAttributeValue("numToDisplay",results));
			      
			      initRowTab(instance);
			      window.rowsPerPage.value=instance.numToDisplay;
		              var rowSummary = " <FONT class='titleBarComment'>(" + instance.totalRecords + ")</FONT>";
		              instance.divTitle.innerHTML=instance.description + rowSummary ;
		              instance.divTitle.className='titleBarLink';
			            
			      if (instance.totalRecords){
			        if (instance.totalRecords > 0){
                	  instance.divTitle.className="titleBarText";
			          instance.loadPage(0);
			        }else{
			          this.divContent.innerHTML("No results found.");
					  instance.divTitle.className="titleBarLink";
			        }
			      }else{
			        instance.complete=true;
					if (instance.parent.selectedSearch){
					  if (instance.parent.selectedSearch.id==instance.id)
					    instance.divTitle.className="titleBarText";
					  else
					    instance.divTitle.className="titleBarLink";
					}else{
		              instance.divTitle.className="titleBarLink";
					}
                    instance.divTitle.className="titleBarText";
					
			        if(instance.totalRecords==0)
			        {
	                  instance.divContent.innerHTML="No results found.";
					  instance.divTitle.className="titleBarLink";
	                  instance.divTitle.innerHTML=instance.description + " (0)";
			        }else{
	                  instance.divContent.innerHTML="Failed to load results.  Contact site administrator for assistance.";
	                  instance.divTitle.innerHTML=instance.description + " (ERROR)";
			        }
			      }
			      
			    }
            
                //instance.divTitle.className="titleBarText";
			}
		}	
	}
  }
  this.populateCallback=populateCallback;
  
  //load page of results from server
  function loadPage(num){
    if (!this.preLoading && !this.loading && !this.populating){
      
      if(!this.preLoaded){
         this.loadRequested=true;
         this.loadSearchMetaData();
      }else if (!this.populated){
         this.loadRequested=true;
         this.populate();
      }else{
    
        this.loading = true;
	    this.currentPage=num;
        if (window.XMLHttpRequest) {
	       this._xmlReq = new XMLHttpRequest();
	    } else if (window.ActiveXObject) {
	       this._xmlReq = new ActiveXObject("Microsoft.XMLHTTP");
	    }
	    
	    //SEND META-DATA REQUEST
	    this._xmlReq.open("POST", url, true);
	    this._xmlReq.onreadystatechange = this.loadPageCallback;
	    
        this._xmlReq.setRequestHeader("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
   
        this._xmlReq.send("remote-class=org.nrg.xnat.ajax.RequestSearchData&remote-method=loadPage&search=" + this.id + "&page="+ num + "&XNAT_CSRF="+csrfToken);
        
      }
    }
  }
  this.loadPage=loadPage;
  
  
  function loadPageCallback(){
   var req = instance._xmlReq;
    if (req.readyState == 4) {
        if (req.status == 200) {
                
            // handle response 
            
            var xmlTEXT = req.responseText;
            
			instance.loading = false;
			instance.loaded = true;
			            
            if (xmlTEXT){
			      instance.loadRequested = false;
			          
			      clearDIV(instance.divContent);
				  	          
			            
                  instance.divContent.innerHTML=xmlTEXT;
		          instance.newTable = instance.divContent.getElementsByTagName("TABLE")[0];
		          for (rx=0;rx<instance.searchDOM.search_fields.length;rx++) {
		            var sf = instance.searchDOM.search_fields[rx];
		            if (sf.header){
		              if(sf.header=="" || sf.header=="  "){
		                sf.header =instance.newTable.tHead.rows[0].childNodes[rx+1].innerHTML;
		              }
		            }else{
		              sf.header =instance.newTable.tHead.rows[0].childNodes[rx+1].innerHTML;
		            }
		          }
		          
		          if (instance.numPages >1){
		              //CONFIGURE PAGING
		              var pageDIV=document.createElement("DIV");
		              pageDIV.className="paging";
		              
		              //CREATE table wrapper for centering
		              var wrapperTable = document.createElement("TABLE");
		              wrapperTable.style.width="100%";
		              wrapperTable.className="paging";
		              wrapperTBody = document.createElement("TBODY");
		              wrapperTBody.className="paging";
		              wrapperTable.appendChild(wrapperTBody);
		              var wrappertr = document.createElement("TR");
		              wrappertr.className="paging";
		              var wrappertd = document.createElement("TD");
		              wrappertd.className="paging";
		              wrappertd.align="center";
		              wrapperTBody.appendChild(wrappertr);
		              wrappertr.appendChild(wrappertd);
		              
		              
		              pageDIV.appendChild(wrapperTable);
		              
		              
		              var tableDIV = document.createElement("TABLE");
		              var tbodyDIV = document.createElement("TBODY");
		              tableDIV.className="paging";
		              var tr = document.createElement("TR");
		              tr.className="paging";
		             
		              tableDIV.appendChild(tbodyDIV);
		              tbodyDIV.appendChild(tr);
		              
		              var td = document.createElement("TD");
		              td.innerHTML="Pages:&nbsp;";
		              td.className="paging";
		              tr.appendChild(td);
		                   
		              var increment=10;
		              var pagesShown=(increment*2);
		              	    
		              if (instance.currentPage>0){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(0);\"><IMG SRC=\"" + server +"left_end.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              
		              if (instance.currentPage>increment && instance.numPages>pagesShown){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + (instance.currentPage-increment) +");\"><IMG SRC=\"" + server +"left2.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              if (instance.currentPage>0){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + (instance.currentPage-1) +");\"><IMG SRC=\"" + server +"left.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              var i =0;
		              var end =instance.numPages;
		              if (instance.numPages>pagesShown){
		               if (instance.currentPage<(pagesShown/2)){
		                 //first section
		                 end = pagesShown;
		               }else if(instance.currentPage>(end-(pagesShown/2))){
		                 //last section
		                 i = end-pagesShown;
		               }else{
		                 //middle
		                 i = instance.currentPage-increment;
		                 end = instance.currentPage+increment;
		               }
		              }
		              
		              for (;i<end;i++){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 if (i != instance.currentPage){
		                   var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + i + ");\">"+ (i+1) + "</A>";
		                   li.innerHTML=liTEXT;
		                 }else{
		                   var liTEXT = "[" + (i+1) + "]";
		                   li.innerHTML=liTEXT;
		                 }
		                 tr.appendChild(li);
		              }
		              
		              
		              if (instance.currentPage<(instance.numPages-1)){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + (instance.currentPage+1) +");\"><IMG SRC=\"" + server +"right.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              if (instance.currentPage<(instance.numPages-increment) && instance.numPages>pagesShown){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + (instance.currentPage+increment) +");\"><IMG SRC=\"" + server +"right2.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              if (instance.currentPage<(instance.numPages-1)){
		                 var li = document.createElement("TD");
		                 li.className="paging";
		                 var liTEXT = "<A class=\"paging\" ONCLICK=\"window.collectionInstance.getSearch('" + instance.id + "').loadPage(" + (instance.numPages-1) +");\"><IMG SRC=\"" + server +"right_end.gif\"/></A>";
		                 li.innerHTML=liTEXT;
		                 
		                 tr.appendChild(li);
		              }
		              
		              wrappertd.appendChild(tableDIV);
		            
		              instance.newTable.style.width="100%";
		              instance.newTable.style.borderTop="1px solid #AAAAAA";
		              instance.newTable.style.paddingTop="3px";
		              instance.newTable_makeClickableHeaders(instance.newTable);
		              
		              //contentDIV.innerHTML=pageDIV.innerHTML;
		              instance.divContent.insertBefore(pageDIV,instance.newTable);
		              
		          }else{
		              instance.newTable.ID=instance.id + "TABLE";
		              instance.newTable.className='sortable';
		              ts_makeSortable(instance.newTable);
		          }
            }
            
			instance.complete=true;
			if (instance.parent.selectedSearch){
			  if (instance.parent.selectedSearch.id==instance.id){
			    instance.divTitle.className="titleBarText";
			    instance.divContent.style.display="block";
			  }else
			    instance.divTitle.className="titleBarLink";
			}else{
              instance.divTitle.className="titleBarLink";
			}
		          
		    if (instance.endTime==undefined){  
	       		instance.endTime=(new Date()).getTime(); 
	       		log(instance.description +": " + (instance.endTime-instance.startTime) + "ms");    
		    }
		          
	        if(window.resize_id){
	             resize_id(null);
	        }
	        
	        if (instance.postLoadOtherPages){
	          instance.postLoadOtherPages=false;
	          instance.parent.loadFirstPages(null);
	        }
		}	
	}
  }
  this.loadPageCallback=loadPageCallback;
  
  
  function resortTable(lnk,index){
    for (var ci=0;ci<lnk.childNodes.length;ci++) {
		if (lnk.childNodes[ci].tagName && lnk.childNodes[ci].tagName.toLowerCase() == 'span')var span = lnk.childNodes[ci];
	}
	
    if (this.newTable.tHead && this.newTable.tHead.rows.length > 0) {
		var firstRow = this.newTable.tHead.rows[this.newTable.tHead.rows.length-1];
		if (firstRow){
		   var colHead = firstRow.cells[index];
	       if (colHead){
	          if (this.sortBy==colHead.id){
	             if (this.sortOrder=="DESC"){
	                this.sortOrder="ASC";
	             }else{
	                this.sortOrder="DESC";
	             }
	             this.populate();
	          }else{
	             if (colHead.id.indexOf(".")==-1)
	             {
	                this.searchDOM.sortByElementName=this.searchDOM.rootElementName;
                    this.searchDOM.sortByFieldID=colHead.id;
	             }else{
	                this.searchDOM.sortByElementName=colHead.id.substring(0,colHead.id.indexOf("."));
                    this.searchDOM.sortByFieldID=colHead.id.substring(colHead.id.indexOf(".")+1);
	             }
	             this.sortBy=colHead.id;
	             this.sortOrder="ASC";
	             this.populate();
	          }
	       }
	    }
	}
  }
  this.resortTable=resortTable;
  

	function newTable_makeClickableHeaders(t) {
		if (t.rows && t.rows.length > 0) {
			if (t.tHead && t.tHead.rows.length > 0) {
				var firstRow = t.tHead.rows[t.tHead.rows.length-1];
				thead = true;
			} else {
				var firstRow = t.rows[0];
			}
		}
		if (!firstRow) return;
		
		// We have a first row: assume it's the header, and make its contents clickable links
		for (var i=0;i<firstRow.cells.length;i++) {
			var cell = firstRow.cells[i];
			var txt = getCDATAValue(cell);
			if (txt!=null){
			  if (cell.className != "unsortable" && cell.className.indexOf("unsortable") == -1) {
			    if (cell.id==this.sortBy){
			      if (this.sortOrder=="ASC")
				    cell.innerHTML = '<a href="#" class="sortheader" onclick="window.collectionInstance.selectedSearch.resortTable(this,'+i+');return false;">'+txt+'<span class="sortarrow">&nbsp;&nbsp;<img src="'+ image_path + image_up + '" alt="&darr;"/></span></a>';
				  else
				    cell.innerHTML = '<a href="#" class="sortheader" onclick="window.collectionInstance.selectedSearch.resortTable(this,'+i+');return false;">'+txt+'<span class="sortarrow">&nbsp;&nbsp;<img src="'+ image_path + image_down + '" alt="&darr;"/></span></a>';
				}else{
				  cell.innerHTML = '<a href="#" class="sortheader" onclick="window.collectionInstance.selectedSearch.resortTable(this,'+i+');return false;">'+txt+'<span class="sortarrow">&nbsp;&nbsp;<img src="'+ image_path + image_none + '" alt="&darr;"/></span></a>';
				}
			  }
			}
		}
		if (alternate_row_colors) {
			alternate(t);
		}
	}
	this.newTable_makeClickableHeaders=newTable_makeClickableHeaders;
	
	function populateOptions(){
		var options = new OptionSet();
		if (this.divOptions)
			options.populate(this.divOptions,this);
	}
	this.populateOptions=populateOptions;
	
}

function OptionSet(){
    this.options=new Array();//array of OptionItems
    
    function addOption(_text,_destination,_idOnly){
    	this.options.push(new OptionItem(_text,_destination,_idOnly));
    }
    this.addOption=addOption; 
   
    function populate(_div,_search){
    	clearDIV(_div);
    	
    	//ADD ROW COUNT
    	var subDIV = document.createElement("LI");
        subDIV.className="subMenu containerSubMenu";
        subDIV.style.cursor="pointer";
        subDIV.style.paddingRight="3px";
        subDIV.innerHTML="More/Less Rows";
        subDIV.onclick=function(){toggleRowCount(_search);_div.style.display='none';};
        _div.appendChild(subDIV);
            
    	
    	//ADD EMAIL BOX
    	var subDIV = document.createElement("LI");
        subDIV.className="subMenu containerSubMenu";
        subDIV.style.cursor="pointer";
        subDIV.style.paddingRight="3px";
        subDIV.innerHTML="Email Search";
        subDIV.onclick=function(){toggleEmail(_search);_div.style.display='none';};
        _div.appendChild(subDIV);
        
                	
    	//ADD MODIFY BOX
    	var subDIV = document.createElement("LI");
        subDIV.className="subMenu containerSubMenu";
        subDIV.style.cursor="pointer";
        subDIV.style.paddingRight="3px";
        subDIV.innerHTML="Modify Search";
        subDIV.onclick=function(){sendSearch(_div.parentNode,serverRoot +"/app/action/DisplaySearchAction",_search.searchDOM.getXML());_div.style.display='none';};
        _div.appendChild(subDIV);
                	
    	//ADD MODIFY BOX
    	var subDIV = document.createElement("LI");
        subDIV.className="subMenu containerSubMenu";
        subDIV.style.cursor="pointer";
        subDIV.style.paddingRight="3px";
        subDIV.innerHTML="Spreadsheet";
        subDIV.onclick=function(){sendSearch(_div.parentNode,serverRoot +"/app/action/CSVAction",_search.searchDOM.getXML());_div.style.display='none';};
        _div.appendChild(subDIV);
        
        if (_search.searchDOM && _search.searchDOM.rootElementName=="xnat:mrSessionData"){
	    	//ADD DOWNLOAD BOX
	    	var subDIV = document.createElement("LI");
	        subDIV.className="subMenu containerSubMenu";
	        subDIV.style.cursor="pointer";
	        subDIV.style.paddingRight="3px";
	        subDIV.innerHTML="Download";
	        subDIV.onclick=function(){sendSearch(_div.parentNode,serverRoot +"/app/action/DownloadSessionsAction",_search.searchDOM.getXML());_div.style.display='none';};
	        _div.appendChild(subDIV);
        }
        
    	for(var i=0;i<this.options.length;i++){
    		var subDIV = document.createElement("LI");
    		
            subDIV.className="subMenu containerSubMenu";
            subDIV.style.paddingRight="3px";
            subDIV.innerHTML="More/Less Rows";
            subDIV.innerHTML=this.options[i].text;
            _div.appendChild(subDIV);
    	}
    }
    this.populate=populate;
}

function OptionItem(_text,_destination,_idOnly){
	this.text = _text;
	this.destination=_destination;
	this.idOnly=_idOnly;
}

function clearDIV(divContent){
      if ( divContent.hasChildNodes() )
	  {
		    while ( divContent.childNodes.length >= 1 )
		    {
		        divContent.removeChild( divContent.firstChild );       
		    } 
	  }	
}

function sendSearch(divContent, _url, _searchXML){
	var tempForm = document.createElement("FORM");
	tempForm.method="POST";
	tempForm.action=_url;
		
	var tempInput=document.createElement("INPUT");
	
	tempInput.type="hidden";
	tempInput.name="search_xml";
	tempInput.value=_searchXML;
	
	tempForm.appendChild(tempInput);
	
	divContent.appendChild(tempForm);
	tempForm.submit();
}

function initRowTab(_search){
	if(window.rowTab==null || window.rowTab==undefined){
		window.rowTab=document.getElementById("rowTab");
		window.rowsPerPage=document.getElementById("rowsPerPage");
		if (!window.rowTab){
			window.rowTab = document.createElement("DIV");
			window.rowTab.id="rowTab";
			window.rowTab.style.display='none';
			var table = document.createElement("TABLE");
			var tbody = document.createElement("TBODY");
			var tr = document.createElement("TR");
			var td1 = document.createElement("TD");
			var td2 = document.createElement("TD");
			var td3 = document.createElement("TD");
			
			td1.innerHTML="Rows Per Page";
			
			window.rowsPerPage = document.createElement("INPUT");
			window.rowsPerPage.type="text";
			window.rowsPerPage.size="4";
			window.rowsPerPage.id="rowsPerPage";
			td2.appendChild(window.rowsPerPage);
			
			var rowsPerPage = document.createElement("INPUT");
			rowsPerPage.type="button";
			rowsPerPage.value="CHANGE";
			rowsPerPage.onclick=function(){
				if (window.rowsPerPage.value!=""){
					window.collectionInstance.setRowsToDisplay(parseInt(window.rowsPerPage.value));
				}
			};
			
			td2.appendChild(rowsPerPage);
			
			var img = document.createElement("img");
			img.src=closeLocation;
			var link = document.createElement("a");
			link.onclick=function(){
				window.rowTab.style.display='none';
			}
			link.appendChild(img);
			td3.appendChild(link);
			
			tr.appendChild(td1);
			tr.appendChild(td2);
			tr.appendChild(td3);
			
			tbody.appendChild(tr);
			table.appendChild(tbody);
			
			window.rowTab.appendChild(table);
			var parent = _search.divContent.parentNode.parentNode;
			parent.insertBefore(window.rowTab,parent.firstChild);
		}
	}
}

function toggleRowCount(_search){
	initRowTab(_search);
	window.rowTab.style.display=(window.rowTab.style.display=='none') ? 'block' : 'none';
}

function toggleEmail(_search){
	initEmailForm(_search);
	window.emailTab.style.display=(window.emailTab.style.display=='none') ? 'block' : 'none';
}

function initEmailForm(_search){
	if(window.emailTab==null || window.emailTab==undefined){
		window.emailTab=document.getElementById("emailTab");
		if (!window.emailTab){
			window.emailTab = document.createElement("DIV");
			window.emailTab.id='emailTab';
			window.emailTab.style.display='none';
			window.emailTab.style.border='border:1px solid #AAAAAA';
			
			var emailMsgTab=document.createElement("DIV");
			emailMsgTab.style.display='block';
			emailMsgTab.id='emailMsgTab';
			
			window.emailTab.appendChild(emailMsgTab);
			var table = document.createElement("TABLE");
			var tbody = document.createElement("TBODY");
			table.appendChild(tbody);
			
			var tr;
			var td;
			var th;
			var hr;
			
			tr = document.createElement("TR");
			th= document.createElement("TH");
			th.align="left";
			th.height="20";
			th.colspan="2";
			th.style.display.fontSize="+1";
			th.innerHTML="Email Search";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.innerHTML="A link to this search will be included in your message.";
			tr.appendChild(td);
			tbody.appendChild(tr);
		
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			hr = document.createElement("HR");
			hr.color="grey";
			td.colspan="2";
			td.appendChild(hr);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			th= document.createElement("TH");
			th.align="left";
			th.colspan="2";
			th.innerHTML="Recipient's email address:";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			var input = document.createElement("INPUT");
			input.type="text";
			input.id="email_toAddresses";
			input.name="email_toAddresses";
			input.size="30";
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			if (user_email){
				var input = document.createElement("INPUT");
				input.type="hidden";
				input.name="email_from_address"; 
				input.id="email_from_address"; 
				input.value=user_email;
				td.colspan="2";
				td.appendChild(input);
			}else{
				tr = document.createElement("TR");
				th= document.createElement("TH");
				th.align="left";
				th.colspan="2";
				th.innerHTML="Sender's email address:";
				tr.appendChild(th);
				tbody.appendChild(tr);
				
				tr = document.createElement("TR");
				td= document.createElement("TD");
				td.align="left";
				td.colspan="2";	
				var input = document.createElement("INPUT");
				input.type="text";
				input.id="email_from_address";
				input.name="email_from_address";
				input.size="30";
				td.appendChild(input);
				tr.appendChild(td);
				tbody.appendChild(tr);
			}
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			td.innerHTML="(Separate multiple email addresses with commas.)";
			tr.appendChild(td);
			tbody.appendChild(tr);
		
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			hr = document.createElement("HR");
			hr.color="grey";
			td.appendChild(hr);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			th= document.createElement("TH");
			th.align="left";
			th.colspan="2";
			th.innerHTML="Email subject message:";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			var input = document.createElement("INPUT");
			input.type="text";
			input.id="email_subject";
			input.name="subject";
			input.size="30";
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
		
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			hr = document.createElement("HR");
			hr.color="grey";
			td.appendChild(hr);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			th= document.createElement("TH");
			th.align="left";
			th.colspan="2";
			th.innerHTML="Personal message:";
			tr.appendChild(th);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			td.align="left";
			td.colspan="2";
			var input = document.createElement("textarea");
			input.id="email_message";
			input.name="message";
			input.rows="10";
			input.cols="31";
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			tr = document.createElement("TR");
			td= document.createElement("TD");
			
			td.align="left";
			var input = document.createElement("input");
			input.type="button";
			input.name="close";
			input.value="Close";
			input.onclick=function(){
				window.emailTab.style.display='none';
			}
			td.appendChild(input);
			tr.appendChild(td);
			td= document.createElement("TD");
			
			td.align="left";
			var input = document.createElement("input");
			input.type="button";
			input.name="eventSubmit_doPerform";
			input.value="Send";
			input.onclick=function(){
				emailSearch(emailMsgTab);
			}
			td.appendChild(input);
			tr.appendChild(td);
			tbody.appendChild(tr);
			
			window.emailTab.appendChild(table);
	    
			var parent = _search.divContent.parentNode.parentNode;
			parent.insertBefore(window.emailTab,parent.firstChild);
		}
	}
}

//send email
function emailSearch(_div){
  var selectedSearch = window.collectionInstance.selectedSearch;
  
  if (selectedSearch){
    var toAddresses = document.getElementById("email_toAddresses").value;
    var subject = document.getElementById("email_subject").value;
    var from = document.getElementById("email_from_address").value;
    var message = document.getElementById("email_message").value;
    
    selectedSearch.email(toAddresses,subject,from,message,_div);
    //document.getElementById('emailTab').style.display='none';
  }else{
    xModalMessage('Email Validation', "Please select a listing.");
  }
}