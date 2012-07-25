//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.event;

import java.util.Calendar;
import java.util.Date;

import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;

public class EventUtils {
	public static EventMetaI TEST_EVENT(final UserI u){
		return new EventMetaI(){
			final Date d= Calendar.getInstance().getTime();
			final String t=FileUtils.getTimestamp(d);
			
			public String getMessage() {
				return null;
			}
	
			public Date getEventDate() {
				return d;
			}
	
			public String getTimestamp() {
				return t;
			}
	
			public UserI getUser() {
				return u;
			}
			
			public Integer getEventId(){
				return null;
			}
			
		};
	}
	
	public static EventMetaI DEFAULT_EVENT(final UserI u, final String msg){
		return new EventMetaI(){
			final Date d= Calendar.getInstance().getTime();
			final String t=FileUtils.getTimestamp(d);
			
			public String getMessage() {
				return msg;
			}
	
			public Date getEventDate() {
				return d;
			}
	
			public String getTimestamp() {
				return t;
			}
	
			public UserI getUser() {
				return u;
			}
			
			public Integer getEventId(){
				return null;
			}
			
		};
	}
	
	public static Date getEventDate(EventMetaI ea,boolean allowNull){
		if(ea==null){
			return (allowNull)?null:Calendar.getInstance().getTime();
		}else{
			return (!allowNull && ea.getEventDate()==null)?Calendar.getInstance().getTime():ea.getEventDate();
		}
	}

	public static String getTimestamp(final EventMetaI ci){
		return (ci==null || org.apache.commons.lang.StringUtils.isEmpty(ci.getTimestamp()))?FileUtils.getTimestamp(Calendar.getInstance().getTime()):ci.getTimestamp();
	}
	
	public static Number getEventId(final EventMetaI ci){
		return (ci==null)?null:ci.getEventId();
	}
	
	public static TYPE getType(final String s,TYPE t){
		if(EventUtils.TYPE.PROCESS.toString().equals(s)){
			return EventUtils.TYPE.PROCESS;
		}else if(EventUtils.TYPE.WEB_FORM.toString().equals(s)){
			return EventUtils.TYPE.WEB_SERVICE;
		}else if(EventUtils.TYPE.WEB_SERVICE.toString().equals(s)){
			return EventUtils.TYPE.WEB_FORM;
		}else{
			return t;
		}
	}
	
	
	public static EventDetails newEventInstance(CATEGORY category, TYPE type, String action, String reason, String comment){
		return new EventDetails(category,type,action,reason,comment);
	}
	
	public static EventDetails newEventInstance(CATEGORY category, TYPE type, String action){
		return new EventDetails(category,type,action,null,null);
	}

	
	public static String getAddModifyAction(String xsiType,boolean isNew){
		return ((isNew)?"Added ":"Modified ") + ElementSecurity.GetSingularDescription(xsiType);
	}
	
	public static String getDeleteAction(String xsiType){
		return "Removed " + ElementSecurity.GetSingularDescription(xsiType);
	}
	
	//event fields
	public final static String EVENT_REASON="event_reason";
	public static final String EVENT_ID = "event_id";
	public static final String EVENT_TYPE = "event_type";
	public static final String EVENT_ACTION = "event_action";
	public static final String EVENT_COMMENT = "event_comment";
	
	public static enum TYPE{WEB_FORM,WEB_SERVICE,PROCESS,STORE_XML,SOAP};
	public static enum CATEGORY{PROJECT_ADMIN,PROJECT_ACCESS,SIDE_ADMIN,DATA};
	
	//pipelines
	public static final String ARCPUT="ArcPut";
	public static final String RENAME="Rename";
	public static final String TRANSFER="Transfer";
	public static final String TRIGGER_PIPELINES="Trigger Pipelines";
	public static final String DICOM_PULL="Pull Data from DICOM";
	
	//actions
	public final static String CREATE_VIA_WEB_SERVICE="Added via web service";
	public final static String MODIFY_VIA_WEB_SERVICE="Modify via web service";
	public final static String SHARE_VIA_WEB_SERVICE="Share via web service";
	public final static String CREATE_RESOURCE="Create resource";
	public final static String UPLOAD_FILE="Upload File";
	public final static String REMOVE_FILE="Remove File";
	public final static String CREATE_VIA_WEB_FORM="Added via web form";
	public final static String MODIFY_VIA_WEB_FORM="Modify via web form";
	public static final String DELETE_VIA_WEB_SERVICE = "Removal via web service";
	public static final String ADD_USER_TO_PROJECT = "Add user to project";
	public static final String REMOVE_USER_TO_PROJECT = "Remove user from project";
	public static final String MODIFY_VIA_STORE_XAR = "Modify via store xar";
	public static final String MODIFY_VIA_STORE_XML="Modified via store xml";
	public static final String PIPELINE="Pipeline";
	public static final String TAGGED_BASED_UPLOAD="Web-based Tagged file upload";
	public static final String MODIFY_PROJECT_ACCESS="Modify Project accessibility";
	public static final String REMOVE_CONFIGURED_PIPELINE="Remove configured pipeline";
	public static final String MODIFY_CONFIGURED_PIPELINE="Modify configured pipelines";
	public static final String REMOVE_PIPELINE="Removed Pipeline from Project";
	public static final String CONFIGURE_PROJECT_ACCESS="Configure project access";
	public static final String AUTO_CREATE_SUBJECT="Auto-created for experiment";
	public static final String RENAME_IN_SHARED_PROJECT="Rename in shared project";
	public static final String MODIFY_PROJECT="Modify project";
	public static final String CONFIGURED_PROJECT_SHARING="Configured project sharing";
	public static final String ADDED_MISC_FILES="Added misc files";
	public static final String STORE_XAR="Store XAR";
	public static final String STORE_XML="Store XML";
	public static final String REMOVE_CATALOG="Remove Resource Catalog";
	public static final String REJECT_PROJECT_REQUEST="Reject Project Access Request";
	public static final String APPROVE_PROJECT_REQUEST="Approve Project Access Request";
	public static final String INVITE_USER_TO_PROJECT = "Invited new user to project";
	public static final String UNKNOWN = "unknown action";
}
