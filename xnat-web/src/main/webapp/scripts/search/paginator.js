/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/search/paginator.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function loadPage(_num){
	window.tab_manager.tabView.get("activeTab").search.loadPage(_num);
}

function Paginator(div){
	this.onPageRequest=new YAHOO.util.CustomEvent("pagerequest",this);
	this.numPages=0;
	this.currentPage=1;
	this.increment=5;
	
	this.html = "";
	
	this.init=function(){
		this.html = "";
		this.html+="<DIV class='paging'>";
		this.html+="";
		   
	      
	      //CREATE table wrapper for centering
		this.html+='<TABLE WIDTH="100%" class="paging">';
		this.html+=' <TBODY class="paging">';
		this.html+='  <TR class="paging">';
		this.html+='   <TD ID="pagingWrapper" class="paging">';
		this.html+='    <TABLE>';
		this.html+='     <TBODY class="paging">';
		this.html+='      <TR class="paging">';
		this.html+='       <TD class="paging">Pages:&nbsp;</TD>';
	           
	      var pagesShown=(this.increment*2);
	      	    
	      if (this.currentPage>0){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(0);return false;"><IMG SRC="' + server + 'left_end.gif"/></A></TD>';
	      }
	      
	      
	      if (this.currentPage>this.increment && this.numPages>pagesShown){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (this.currentPage-this.increment) +');return false;"><IMG SRC="' + server + 'left2.gif"/></A></TD>';
	      }
	      
	      if (this.currentPage>0){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (this.currentPage-1) +');return false;"><IMG SRC="' + server + 'left.gif"/></A></TD>';
	      }
	      
	      var i =0;
	      var end =this.numPages;
	      if (this.numPages>pagesShown){
	       if (this.currentPage<(pagesShown/2)){
	         //first section
	         end = pagesShown;
	       }else if(this.currentPage>(end-(pagesShown/2))){
	         //last section
	         i = end-pagesShown;
	       }else{
	         //middle
	         i = this.currentPage-this.increment;
	         end = this.currentPage+this.increment;
	       }
	      }
	      
	      for (;i<end;i++){
	         if (i != this.currentPage){
		         this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (i) +');return false;">' + (i+1) +'</A></TD>';
	         }else{
	           this.html+="<TD>[" + (i+1) + "]</TD>";
	         }
	      }
	      
	      
	      if (this.currentPage<(this.numPages-1)){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (this.currentPage+1) +');return false;"><IMG SRC="' + server + 'right.gif"/></A></TD>';
	      }
	      
	      if (this.currentPage<(this.numPages-this.increment) && this.numPages>pagesShown){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (this.currentPage+this.increment) +');return false;"><IMG SRC="' + server + 'right2.gif"/></A></TD>';
	      }
	      
	      if (this.currentPage<(this.numPages-1)){
		     this.html+='       <TD class="paging"><A class="paging" onclick="loadPage(' + (this.numPages-1) +');return false;"><IMG SRC="' + server + 'right_end.gif"/></A></TD>';
	      }
	      
		this.html+="      </TR>";
		this.html+="     </TBODY>";
		this.html+="    </TABLE>";
		this.html+="   </TD>";
		this.html+="  </TR>";
		this.html+=" </TBODY>";
		this.html+="</TABLE>";
		
	}
}