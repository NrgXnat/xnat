//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang.BooleanUtils;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFTItem;
import org.nrg.xft.compare.ItemComparator;
import org.nrg.xft.db.loaders.XFTItemDBLoader.ItemCache;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperFactory;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWrapperField;

public class FlattenedItem extends FlattenedItemA{
		
	@SuppressWarnings("unchecked")
	public FlattenedItem(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory,Callable<Integer> idGenerator,FlattenedItemA.FilterI filter,XMLWrapperElement root, String refName, List<FlattenedItemA.ItemObject> parents, List<? extends FlattenedItemModifierI> injectors,ItemCache cache) throws Exception{
		super(FlattenedItem.getFlattenedSingleParams(i),i.isHistory(),i.getRowLastModified(),i.getInsertDate(),i.getStatus(),idGenerator.call(),root.getXSIType(),parents);
		
		
		if(this.isHistory){
			setChange_date(FlattenedItemA.parseDate(i.getProps().get("change_date")));
			if(i.getMeta()!=null){
				create_username=XDATUser.getUsername(i.getMeta().getProperty("insert_user_xdat_user_id"));
			}
			modified_username=XDATUser.getUsername(i.getProps().get("change_user"));
			create_event_id=translateNumber(i.getProps().get("xft_version"));
			if(isDeleted()){
				modified_event_id=translateNumber(i.getXFTVersion());
			}
			setPrevious_change_date(FlattenedItemA.parseDate(i.getProps().get("previous_change_date")));
		}else{
			create_event_id=translateNumber(i.getXFTVersion());
			if(i.getMeta()!=null){
				create_username=XDATUser.getUsername(i.getMeta().getProperty("insert_user_xdat_user_id"));
			}
		}

		pks=root.getPkNames();
		displayIdentifiers=root.getDisplayIdentifiers();
		
		this.o=new FlattenedItemA.ItemObject((refName==null)?root.getProperName():refName,this.getLabel(),getPKString(),root.getExtendedXSITypes());
		FlattenedItem.renderChildren(i,root,this,root.getChildren(),includeHistory,idGenerator,filter,injectors,cache);

		if(injectors!=null && injectors.size()>0){
			for(final FlattenedItemModifierI injector:injectors){
				injector.modify(i,includeHistory,idGenerator, filter, root,parents,this);
			}
		}
	}
	
	public Number translateNumber(Object o){
		if(o instanceof Number|| o==null){
			return (Number)o;
		}else{
			return Integer.valueOf(o.toString());
		}
	}

	public FlattenedItem(FlattenedItem fi) throws Exception{
		super(fi);
	}
	
	@SuppressWarnings("unchecked")
	static void renderChildren(XFTItem item,final XMLWrapperElement root,FlattenedItem parent, final List<XMLWrapperField> fields,FlattenedItemA.HistoryConfigI includeHistory,Callable<Integer> idGenerator,FlattenedItemA.FilterI filter, List<? extends FlattenedItemModifierI> injectors, ItemCache cache) throws Exception{
		for(final XMLWrapperField field:fields)
		{
			if(field.isReference() && root.getExtensionFieldName().equalsIgnoreCase(field.getName())){
				XFTItem extension=(XFTItem)item.getProperty(field.getId());
				if(extension!=null){
					XMLWrapperElement ext=convertElement(extension.getGenericSchemaElement());
					renderChildren(extension,ext,parent,ext.getChildren(),includeHistory,idGenerator,filter,injectors,cache);
				}
			}else if(field.isReference()){
				if(field.getExpose()){
					final List<XFTItem> refs = item.getChildItems(field,includeHistory.getIncludeHistory(),cache);
					if(refs.size()>0){
						XMLWrapperElement refElement=convertElement(refs.get(0).getGenericSchemaElement());
						Collections.sort(refs,new ItemComparator());
						if(refs.size()>0){
							List<FlattenedItemA.ItemObject> parents = new ArrayList<FlattenedItemA.ItemObject>(parent.getParents().size()+1);  
							if(parent.getParents()!=null)parents.addAll(parent.getParents()); 
							parents.add(parent.getItemObject());
							
							parent.addChildCollection(field.getXMLPathString(),field.getDisplayName(),buildLayeredProps(refs,includeHistory,idGenerator,filter,refElement,(field.getDisplayName()!=null)?field.getDisplayName():field.getName(),parents,injectors,cache));
						}
					}
				}
			}else{
				renderChildren(item,root,parent,field.getChildren(),includeHistory,idGenerator,filter,injectors,cache);
			}
		}
	}

	@SuppressWarnings("unchecked")
	static FlattenedItemA.FieldTracker getFlattenedSingleParams(XFTItem item,XMLWrapperField field) throws Exception{
		FlattenedItemA.FieldTracker params= new FlattenedItemA.FieldTracker();
		
		parseAttributes(item, field.getAttributes(), params);
	
		handleChildFields(field.getChildren(), item, params);
		
		if (field.isReference())
		{
			if (!field.isMultiple())
			{
				XFTItem ref = (XFTItem)item.getProperty(field.getId());
				if (ref != null && item.getGenericSchemaElement().getExtensionFieldName().equalsIgnoreCase(field.getName()))
				{
					params.putAll(getFlattenedSingleParams(ref));
				}
			}
		}else{
			FlattenedItemA.putValue(field.getXMLPathString(),field.getDisplayName(),parseProperty(field,item.getProperty(field.getId())),params);
		}
		
		return params;
	}

	static void handleChildFields(List<XMLWrapperField> fields,XFTItem item, FlattenedItemA.FieldTracker params) throws Exception{
		for(XMLWrapperField field:fields){
			params.putAll(FlattenedItem.getFlattenedSingleParams(item, field));
		}
	}

	@SuppressWarnings("unchecked")
	static FlattenedItemA.FieldTracker getFlattenedSingleParams(XFTItem item) throws Exception{
		XMLWrapperElement root = convertElement(item.getGenericSchemaElement());
		FlattenedItemA.FieldTracker params= new FlattenedItemA.FieldTracker();
		
		parseAttributes(item, (List<XMLWrapperField>)root.getAttributes(), params);
		
		FlattenedItem.handleChildFields(root.getChildren(),item,params);
		
	    for(GenericWrapperField field:root.getAllPrimaryKeys()) {
	    	if(!params.getParams().containsKey(field.getXMLPathString())){
				FlattenedItemA.putValue(field.getXMLPathString(),field.getName(),parseProperty(field,item.getProperty(field.getId())),params);
	    	}
	    }
		
		return params;
	}

	static void parseAttributes(XFTItem item, List<XMLWrapperField> fields, FlattenedItemA.FieldTracker params) throws Exception{
		for(XMLWrapperField field:fields){
			if (field.isReference())
			{
				if (!field.isMultiple())
				{
					XFTItem ref = (XFTItem)item.getField(field.getId());
					if (ref != null)
					{
						params.putAll(FlattenedItem.getFlattenedSingleParams(ref));
					}
				}
			}else{
				FlattenedItemA.putValue(field.getXMLPathString(),field.getDisplayName(),parseProperty(field,item.getField(field.getId())),params);
			}
		}
	}
	
	private static Object parseProperty(GenericWrapperField field,Object o){
		if(field!=null && field.getXMLType()!=null && field.getXMLType().getLocalType()!=null && field.getXMLType().getLocalType().equals("boolean") && o!=null){
			if(o.toString().equals("0")){
				return "false";
			}else if(o.toString().equals("1")){
				return "true";
			}else{
				return o;
			}
		}else{
			return o;
		}
	}


	public static XMLWrapperElement convertElement(GenericWrapperElement gwe) throws XFTInitException, ElementNotFoundException
	{
		if(gwe.getFullXMLName().endsWith("_history")){
			gwe=GenericWrapperElement.GetElement(gwe.getFullXMLName().substring(0,gwe.getFullXMLName().indexOf("_history")));
		}
		
		return (XMLWrapperElement)XMLWrapperFactory.GetInstance().convertElement(gwe);
	}

	static List<FlattenedItemI> buildLayeredProps(List<XFTItem> items, FlattenedItemA.HistoryConfigI includeHistory,Callable<Integer> idGenerator,FlattenedItemA.FilterI filter,final XMLWrapperElement root,String refName, List<FlattenedItemA.ItemObject> parents, List<? extends FlattenedItemModifierI> files,ItemCache cache) throws Exception
	{
		if(includeHistory==null){
			includeHistory=FlattenedItemA.GET_ALL;
		}
		if(filter==null){
			filter=new FlattenedItemA.FilterI(){
				public boolean accept(FlattenedItemI i) {
					return true;
				}
			};
		}
		
		final List<FlattenedItemI> all_props=new ArrayList<FlattenedItemI>();
		for(XFTItem i:items){
			final FlattenedItem pi=new FlattenedItem(i,includeHistory,idGenerator,filter,root,refName,parents,files,cache);
			if(filter.accept(pi)){
				all_props.add(pi);
			}
		}
		
		return all_props;
	}
	
	public FlattenedItem filteredCopy(FlattenedItemA.FilterI fi) throws Exception{
		FlattenedItem temp = new FlattenedItem(this);
		
		for(Map.Entry<String,FlattenedItemA.ChildCollection> cc: this.getChildCollections().entrySet()){
			FlattenedItemA.ChildCollection tempCC=cc.getValue().copy(fi);
			if(tempCC.getChildren().size()>0){
				temp.getChildCollections().put(cc.getKey(),tempCC);
			}
		}
		
		temp.getParents().addAll(this.getParents());
		
		return temp;
	}
	
	public static String writeValuesToString(List<String> h, FlattenedItemI fi){
		String s= "";
		for(final String key:h){
			Object temp=fi.getFields().getParams().get(key);
			
			if(temp!=null){
				if(!s.equals("")){
					s+=", ";
				}
				
				s+=temp;
			}
		}
		
		return s;
	}
	
	public Object getPKString() {		
		List<String> pks=getPks();
		
		if(pks.size()==0){
			return id;
		}
		
		String s= writeValuesToString(pks,this);
		if(s.equals("")){
			return id;
		}
		return s;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getLabel()
	 */
	public Object getLabel() {
		if(displayIdentifiers!=null && displayIdentifiers.size()>0){
			String s= writeValuesToString(displayIdentifiers,this);
			if(!s.equals("")){
				return s;
			}
		}
		
		return getPKString();
	}

	
	public interface FlattenedItemModifierI{
		public void modify(XFTItem i, FlattenedItemA.HistoryConfigI includeHistory,Callable<Integer> idGenerator,FlattenedItemA.FilterI filter,XMLWrapperElement root, List<FlattenedItemA.ItemObject> parents,FlattenedItemI fi);
	}

	public static class FlattenedFile extends FlattenedItemA{
	
		public FlattenedFile(FlattenedItemA.FieldTracker ft, boolean isHistory,
				Date last_modified, Date insert_date,
				Integer id, String xsiType,String modifiedBy, Integer modifiedEventId, Integer createEventId, String label,List<FlattenedItemA.ItemObject> parents,String createdBy) {
			super(ft, isHistory, last_modified, insert_date, (isHistory)?FlattenedItemA.DELETED:"active", id, xsiType,parents);
			
			if(isHistory){
				setChange_date(last_modified);
				modified_username=modifiedBy;
				setPrevious_change_date(insert_date);
			}
			modified_event_id=modifiedEventId;
			create_event_id=createEventId;
			create_username=createdBy;
			
			String lbl=label.intern();
			FlattenedItemA.putValue("uri", "uri", lbl, ft);
			
			pks=Arrays.asList("uri");
			displayIdentifiers=Arrays.asList("uri");
			
			o=new FlattenedItemA.ItemObject("file",lbl,lbl,Arrays.asList("system:file".intern()));
		}
		
		public FlattenedFile(FlattenedFile fi) throws Exception{
			super(fi);
		}
	
		public FlattenedFile filteredCopy(FlattenedItemA.FilterI fi) throws Exception{
			FlattenedFile temp = new FlattenedFile(this);
			
			for(Map.Entry<String,FlattenedItemA.ChildCollection> cc: this.getChildCollections().entrySet()){
				FlattenedItemA.ChildCollection tempCC=cc.getValue().copy(fi);
				if(tempCC.getChildren().size()>0){
					temp.getChildCollections().put(cc.getKey(),tempCC);
				}
			}
			
			temp.modified_event_id=this.modified_event_id;
			temp.create_event_id=this.create_event_id;
			
			temp.getParents().addAll(this.getParents());
			
			return temp;
		}


		public Number getCreateEventId() {
			if(getCreate_event_id()==null) return (getStartDate()==null)?null:getStartDate().getTime();
			return getCreate_event_id();
		}

		public Number getModifiedEventId() {
			if(getModified_event_id()==null) return (getEndDate()==null)?null:getEndDate().getTime();
			return getModified_event_id();
		}
	}


	public static class FlattenedFileSummary extends FlattenedItemA{
		public FlattenedFileSummary(FlattenedItemA.FieldTracker ft, boolean isHistory,
				Date last_modified, Date insert_date,
				Integer id, String xsiType,String modifiedBy, Integer modifiedEventId, Integer createEventId, String label,List<FlattenedItemA.ItemObject> parents,String createdBy,Integer count,String action) {
			super(ft, isHistory, last_modified, insert_date, (isHistory)?FlattenedItemA.DELETED:"active", id, xsiType,parents);
			
			if(isHistory){
				setChange_date(last_modified);
				modified_username=modifiedBy;
				setPrevious_change_date(insert_date);
			}
			modified_event_id=modifiedEventId;
			create_event_id=createEventId;
			create_username=createdBy;
			
			FlattenedItemA.putValue("count", "count", count, ft);
			FlattenedItemA.putValue("action", "action", action, ft);
			FlattenedItemA.putValue("label", "label", label, ft);
			
			pks=Arrays.asList("label");
			displayIdentifiers=Arrays.asList("label");
			
			o=new FlattenedItemA.ItemObject("file:summary",label,label,Arrays.asList("file:summary".intern()));
		}
		
		public FlattenedFileSummary(FlattenedFileSummary fi) throws Exception{
			super(fi);
		}
	
		public FlattenedFileSummary filteredCopy(FlattenedItemA.FilterI fi) throws Exception{
			FlattenedFileSummary temp = new FlattenedFileSummary(this);
			
			for(Map.Entry<String,FlattenedItemA.ChildCollection> cc: this.getChildCollections().entrySet()){
				FlattenedItemA.ChildCollection tempCC=cc.getValue().copy(fi);
				if(tempCC.getChildren().size()>0){
					temp.getChildCollections().put(cc.getKey(),tempCC);
				}
			}
			
			temp.modified_event_id=this.modified_event_id;
			temp.create_event_id=this.create_event_id;
			
			temp.getParents().addAll(this.getParents());
			
			return temp;
		}
	}
}