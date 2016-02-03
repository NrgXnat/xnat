/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/locking.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */


XNAT.app.locker= {
	msg:"lock_div",
	icon:"lock_a",
	
	showUnlock:function(){
		 if(YUIDOM.get(this.msg)!=undefined){
			YUIDOM.get(this.msg).innerHTML="LOCKED: This item has been marked as complete.";
			YUIDOM.setStyle(this.msg,'display','block');
			YUIDOM.setStyle(this.msg,'margin','5px');
		 }
		
		 if(YUIDOM.get(this.icon)!=undefined){
			YUIDOM.setStyle(this.icon,'display','block');
			YUIDOM.get(this.icon).innerHTML="<a onclick='XNAT.app.locker.unlock()'><img id='lock_img' border='0' src='" + XNAT.images.redChk +"'/> Unlock</a>";
   	    }
		 
		 YUIDOM.setStyle(YUIDOM.getElementsByClassName('lockable'), 'display', 'none');
	},
	
	showLock:function(){
		 if(YUIDOM.get(this.msg)!=undefined){
			 YUIDOM.get(this.msg).innerHTML="";
			 YUIDOM.setStyle(this.msg,'display','none');
		 }
		
		 if(YUIDOM.get(this.icon)!=undefined){
			 YUIDOM.setStyle(this.icon,'display','block');
			 YUIDOM.get(this.icon).innerHTML="<a onclick='XNAT.app.locker.lock()'><img id='lock_img' border='0' src='" + XNAT.images.grnChk +"'/> Lock</a>";
   	    }
		 YUIDOM.setStyle(YUIDOM.getElementsByClassName('lockable'), 'display', 'block');
	},
	

	   
	lock:function(){	
		var justification=new XNAT.app.requestJustification("lock_","Lock Item",this._lock,this);
	},
   
   _lock:function(arg1,arg2,container){	   
		var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
		var initCallback={
			success:function(obj1){
				closeModalPanel("lock_");
				XNAT.app.locker.showUnlock();
				XNAT.app.locker.status=true;
			},
			failure:function(o){
	    		closeModalPanel("lock_");
				displayError("ERROR " + o.status+ ": Failed to lock item.");
			},
            cache:false, // Turn off caching for IE
			scope:this
		}

		openModalPanel("lock_","Locking item.");
		if(XNAT.app.current_uri==undefined)xModalMessage('Error', "Missing URI definition");

		var params="";		
		params+="event_reason="+event_reason;
		params+="&event_type=WEB_FORM";
		params+="&event_action=Locked Item";
		params+="&_lock=true";
		params+="&XNAT_CSRF="+csrfToken;

		YAHOO.util.Connect.asyncRequest('PUT',XNAT.app.current_uri+"?"+params,initCallback,null,this);
   },

   
	unlock:function(){	
		if(showReason){
			var justification=new XNAT.app.requestJustification("lock_","Unlock Item",this._unlock,this);
		}else{
			_unlock();
		}
	},
	
	_unlock:function(arg1,arg2,container){
		var event_reason=(container==undefined || container.dialog==undefined)?"":container.dialog.event_reason;
		var initCallback={
				success:function(o){
					closeModalPanel("lock_");
					XNAT.app.locker.showLock();
					XNAT.app.locker.status=false;
				},
				failure:function(o){
					closeModalPanel("lock_");
					displayError("ERROR " + o.status+ ": Failed to unlock item.");
				},
            cache:false, // Turn off caching for IE
				scope:this
		}
		if(XNAT.app.current_uri==undefined)xModalMessage('Error', "Missing URI definition");
		openModalPanel("lock_","Unlocking item.");

		var params="";		
		params+="event_reason="+event_reason;
		params+="&event_type=WEB_FORM";
		params+="&event_action=Unlocked Item";
		params+="&_unlock=true";
		params+="&XNAT_CSRF="+csrfToken;

		YAHOO.util.Connect.asyncRequest('PUT',XNAT.app.current_uri+"?"+params,initCallback,null,this);
	}
}