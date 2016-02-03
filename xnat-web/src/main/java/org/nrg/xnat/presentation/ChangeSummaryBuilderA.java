/*
 * org.nrg.xnat.presentation.ChangeSummaryBuilderA
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.presentation;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xft.presentation.FlattenedItem;
import org.nrg.xft.presentation.FlattenedItem.FlattenedFile;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemA.FileSummary;
import org.nrg.xft.presentation.FlattenedItemA.ItemObject;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xft.presentation.ItemHistoryBuilder;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.itemBuilders.WorkflowBasedHistoryBuilder.FileEvent;

import java.util.*;

public abstract class ChangeSummaryBuilderA extends ItemHistoryBuilder{
	final EventBuilderI builder;
	public ChangeSummaryBuilderA(EventBuilderI b){
		if(b==null){
			builder=new DefaultBuilder();
		}else{
			builder=b;
		}
	}

	public static class ChangeSummary{
		final List<ItemEventI> events=new ArrayList<ItemEventI>();
		final Date date;
		Number n;
		
		public ChangeSummary(Date date,Number n){
			this.date=date;
			this.n=n;
		}

		public Date getDate() {
			return date;
		}

		public Number getNumber() {
			if(n==null){
				n=getDate().getTime();
			}
			return n;
		}

		public List<ItemEventI> getEvents() {
			return events;
		}
		
		public String toString(){
			return toString(getSummary());
		}
		
		public int getDominantChangeType(){
			int level=0;
			for(Map.Entry<String,Map<String,ChangeSummaryGroup>> entry:getSummary().entrySet()){
				if(entry.getValue().containsKey(ChangeSummaryBuilderA.REMOVED)){
					return 2;
				}else if(entry.getValue().containsKey(ChangeSummaryBuilderA.MODIFIED)){
					level=1;
				}
			}
			return level;
		}
		
		public static String toString(Map<String, Map<String,ChangeSummaryGroup>> map){
			StringBuilder sb= new StringBuilder();
			int counter=0;
			for(Map.Entry<String, Map<String,ChangeSummaryGroup>> outer: map.entrySet()){
				for(Map.Entry<String,ChangeSummaryGroup> inner: outer.getValue().entrySet()){
					if(counter++>0)sb.append(", ");
					sb.append(inner.getKey()).append(" ").append(inner.getValue().getCount()).append(" ").append(outer.getKey());
					if(inner.getKey().equals(MODIFIED)){
						sb.append(" field(s)");
					}
				}
			}
			return sb.toString();
		}
		
		public static class ChangeSummaryGroup{
			final String dataType;
			final String action;
			int count=0;
			
			public ChangeSummaryGroup(String dataType,String action){
				this.dataType=dataType;
				this.action=action;
			}
			
			public String getDataType() {
				return dataType;
			}

			public String getAction() {
				return action;
			}

			public int getCount() {
				return count;
			}

			public void increment(int cc){
				count+=cc;
			}
			
		}
		
		Map<String,Map<String,ChangeSummaryGroup>> summary=null;
		/**
		 * @return
		 */
		public Map<String,Map<String,ChangeSummaryGroup>> getSummary(){
			if(summary==null){
				summary= new Hashtable<String,Map<String,ChangeSummaryGroup>>();
				
				for(ItemEventI e: getEvents()){
					if(!summary.containsKey(e.getObjectHeader())){
						summary.put(e.getObjectHeader(), new Hashtable<String,ChangeSummaryGroup>());
					}
					
					if(!summary.get(e.getObjectHeader()).containsKey(e.getAction())){
						summary.get(e.getObjectHeader()).put(e.getAction(), new ChangeSummaryGroup(e.getObjectHeader(),e.getAction()));
					}
					
					summary.get(e.getObjectHeader()).get(e.getAction()).increment((e instanceof FileEvent)?((FileEvent)e).getDiff():1);
				}
			}
			return summary;
		}

		public JSONObject toJSON(final String dateFormat) throws JSONException {
			JSONObject o = new JSONObject();
			if(getDate()!=null){
				if(dateFormat==null){
					o.put("date", getDate().getTime());
				}else{
					o.put("date", DateUtils.format(getDate(), dateFormat));
				}
			}
			
			JSONArray a = new JSONArray();
			o.put("changes", a);
			for(ItemEventI ie: getEvents()){
				a.put(ie.toJSON());
			}
			
			return o;
		}
	}
	
	public Map<Date,ChangeSummary> call(List<FlattenedItemI> items) throws Exception {
		Map<Date,ChangeSummary> sb= new HashMap<Date,ChangeSummary>();
		build(new FlattenedItemA.ChildCollection(items,"",""),null,sb,null);
		return sb;
	}	
	
	
	public static List<FlattenedItemI> flatten(final FlattenedItemI fi){
		final List<FlattenedItemI> grouped= new ArrayList<FlattenedItemI>();
		grouped.add(fi);
		
		if(fi.getHistory().size()>0){
			grouped.addAll(fi.getHistory());
			Collections.sort(grouped, new Comparator<FlattenedItemI>(){
				public int compare(FlattenedItemI o1, FlattenedItemI o2) {
					return DateUtils.compare(o1.getStartDate(),o2.getStartDate());
				}});
		}
		return grouped;
	}
	
	protected void addMessage(Map<Date,ChangeSummary> sb, Date date, Number n, ItemEventI s){
		if(date!=null){
			if(!sb.containsKey(date)){
				sb.put(date,new ChangeSummary(date,n));
			}
			sb.get(date).getEvents().add(s);
		}
	}
	
	protected boolean contains(Map<Date,ChangeSummary> sb, Date d, String action,FlattenedItemA.ItemObject io){
		ChangeSummary ies=sb.get(d);
		if(ies==null){
			return false;
		}
		
		for(ItemEventI ie:ies.getEvents()){
			if(ie.getAction().equals(action) && ie.getItemObject().equals(io)){
				return true;
			}
		}
		
		return false;
	}
	
	
	public void  renderChildren(Map<String,FlattenedItemA.ChildCollection> items, Map<Date,ChangeSummary> sb, Integer parent) throws Exception{
		for(Map.Entry<String,FlattenedItemA.ChildCollection> entry: items.entrySet()){
			build(entry.getValue(), entry.getValue().getName(),sb,parent);
		}
	}
	
	protected Date getRelevantDate(FlattenedItemI params,String action){
		if(ADDED.equals(action)){
			return params.getInsert_date();
		}else if(REMOVED.equals(action)){
			return (params.getEndDate()==null)?params.getStartDate():params.getEndDate();
		}else{
			return params.getStartDate();
		}
	}
	

	protected void registerEvent(Map<Date,ChangeSummary> sb, ItemObject io,Number change_id,String action,FieldEventI fe, String username, Date d, List<ItemObject> parents, FlattenedItemI fi){
		registerEvent(sb,io,change_id,d,builder.build(io, action, fe, username, parents,fi));
	}	
	
	protected void registerEvent(Map<Date,ChangeSummary> sb, ItemObject io,Number change_id, Date d, List<ItemEventI> ies){
		for(ItemEventI ie:ies){
			if(MODIFIED.equals(ie.getAction()) || !contains(sb,d,ie.getAction(),io)){
				addMessage(sb,d,change_id,ie);
			}
		}
	}	

	protected void registerEvent(Map<Date,ChangeSummary> sb, FlattenedItemI params,FileSummary fs){
		FlattenedItemA.ItemObject io =new FlattenedItemA.ItemObject("file",null,null,Arrays.asList("system:file".intern()));
		List<FlattenedItemA.ItemObject> local=new ArrayList<FlattenedItemA.ItemObject>();
		local.addAll(params.getParents());
		local.add(io);
		for(ItemEventI ie:builder.build(io, fs.action,null,"",local,params)){
			addMessage(sb,fs.date,fs.change_id,ie);
		}
	}	
	
	public FieldEventI buildFieldEvent(String path, String name, FlattenedItemI f1, FlattenedItemI f2){
		if(DateUtils.isOnOrAfter(f1.getStartDate(), f2.getStartDate())){
			return new FieldEvent(path,name,f1.getFields().getParams().get(path),f2.getFields().getParams().get(path)); 
		}else{
			return new FieldEvent(path,name,f2.getFields().getParams().get(path),f1.getFields().getParams().get(path)); 
		}
	}
	
	public void handleRow(Map<Date,ChangeSummary> sb, List<FlattenedItemI> params, Integer parent, String label,Map<String,String> headers){
		for(int i=0;i<params.size();i++){
			FlattenedItemI fi=params.get(i);
			if(fi instanceof FlattenedItem){
				if(i==0){
					//first
					registerEvent(sb,fi.getItemObject(),((FlattenedItem)fi).getCreateEventId(),ADDED,(FieldEventI)null,((FlattenedItem)fi).getCreateUsername(),fi.getInsert_date(),fi.getParents(),fi);
				}else{
					if(fi.isDeleted() && DateUtils.isEqualTo(fi.getChange_date(),fi.getLast_modified())){
						//last
						registerEvent(sb,fi.getItemObject(),((FlattenedItem)fi).getModifiedEventId(),REMOVED,null,((FlattenedItem)fi).getModifiedUsername(),fi.getEndDate(),fi.getParents(),fi);
						
						if(!DateUtils.isEqualTo(fi.getInsert_date(),fi.getPrevious_change_date())){
							FlattenedItemI previous = params.get(i-1);
							markModifications(sb, headers, (FlattenedItem)fi, (FlattenedItem)previous);
						}
					}else{
						FlattenedItemI previous = params.get(i-1);
						markModifications(sb, headers, (FlattenedItem)fi, (FlattenedItem)previous);
					}
				}
			}else{
				handleFlattenedFile(sb,(FlattenedFile)fi);
			}
		}
	}
	
	protected void handleFlattenedFile(Map<Date,ChangeSummary> sb, FlattenedFile f){
		if(f.getCreateEventId()!=null){
			registerEvent(sb,f.getItemObject(),f.getCreateEventId(),ADDED,(FieldEventI)null,f.getCreateUsername(),f.getInsert_date(),f.getParents(),f);
		}else if(f.getInsert_date()!=null){
			registerEvent(sb,f.getItemObject(),f.getInsert_date().getTime(),ADDED,(FieldEventI)null,f.getCreateUsername(),f.getInsert_date(),f.getParents(),f);
		}
		
		if(f.getModifiedEventId()!=null){
			registerEvent(sb,f.getItemObject(),f.getModifiedEventId(),REMOVED,(FieldEventI)null,f.getModifiedUsername(),f.getEndDate(),f.getParents(),f);
		}else if(f.getEndDate()!=null){
			registerEvent(sb,f.getItemObject(),f.getEndDate().getTime(),REMOVED,(FieldEventI)null,f.getModifiedUsername(),f.getEndDate(),f.getParents(),f);
		}
	}
	
//	final static String[] addfiles=new String[]{"file_count".intern(),"file_size".intern()};
	final static List<String> conceal=Arrays.asList("file_size");
	final static String resource_type="xnat:abstractResource".intern();
	final static String resource_count="file_count".intern();
	
	protected void markModifications(Map<Date,ChangeSummary> sb, Map<String,String> headers, FlattenedItem fi, FlattenedItem previous){
		for(Map.Entry<String,String> header:headers.entrySet()){
			if(!conceal.contains(header.getKey())){
				if(differBy(fi,previous,header.getKey())){
					registerEvent(sb,fi.getItemObject(),fi.getCreateEventId(),fi.getStartDate(),builder.build(fi.getItemObject(), MODIFIED, buildFieldEvent(header.getKey(),header.getValue(),fi,previous), (fi.getModifiedUsername()==null)?fi.getCreateUsername():fi.getModifiedUsername(), fi.getParents(),fi));
				}
			}
		}
	}

	protected  void build(FlattenedItemA.ChildCollection all_props,final String label,Map<Date,ChangeSummary> sb, Integer parent) throws Exception{
		//build distinct list of headers
		int counter=0; 
		for(final FlattenedItemI params:all_props.getChildren()){		
			if(params.getHistory().size()>0){
				final List<FlattenedItemI> grouped=flatten(params);
				handleRow(sb, grouped, parent, label,all_props.getHeaders());
			}else{
				if(params instanceof FlattenedItem){
					registerEvent(sb,params.getItemObject(),((FlattenedItem) params).getCreateEventId(),ADDED,null,params.getCreateUsername(),params.getInsert_date(),params.getParents(),params);
					if(params.isDeleted() && DateUtils.isEqualTo(params.getChange_date(),params.getLast_modified())){
						//last
						registerEvent(sb,params.getItemObject(),((FlattenedItem) params).getModifiedEventId(),REMOVED,null,params.getModifiedUsername(),params.getEndDate(),params.getParents(),params);
					}
					
					for(FileSummary o: params.getMisc()){
						registerEvent(sb,params,o);
					}
				}else{
					handleFlattenedFile(sb,(FlattenedFile)params);
				}
			}
			  
			renderChildren(params.getChildCollections(),sb, params.getId());
			counter++;
		}
	}
	
	public final static String REMOVED="Removed";
	public final static String ADDED="Added";
	public final static String MODIFIED="Modified";
	

	public interface ItemEventI {

		public abstract FlattenedItemA.ItemObject getParent();

		public abstract FlattenedItemA.ItemObject getItemObject();

		public abstract String getUsername();

		public abstract String getObjectHeader();

		public abstract Object getObjectLabel();

		public abstract String getAction();

		public abstract FieldEventI getField();

		public abstract List<FlattenedItemA.ItemObject> getParents();

		public abstract JSONObject toJSON() throws JSONException;

	}
	
	public static class ItemEvent implements ItemEventI{
		final FlattenedItemA.ItemObject object;
		final FlattenedItemA.ItemObject parent;
		
		final List<FlattenedItemA.ItemObject> parents;
		
		final String action;
		final String username;
		
		final FieldEventI field;
		
		public ItemEvent(FlattenedItemA.ItemObject o, String action, FieldEventI fe,List<FlattenedItemA.ItemObject> parents, String username) {
			super();
			this.object=o;
			this.action = action;
			this.field=fe;
			if(parents !=null && parents.size()>0){
				parent=parents.get(parents.size()-1);
				this.parents=parents.subList(0, parents.size()-1);
			}else{
				this.parents=new ArrayList<FlattenedItemA.ItemObject>();
				parent = null; 
			}
			this.username=username;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getParent()
		 */
		public FlattenedItemA.ItemObject getParent(){
			return parent;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getItemObject()
		 */
		public FlattenedItemA.ItemObject getItemObject() {
			return object;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getUsername()
		 */
		public String getUsername(){
			return username;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getObjectHeader()
		 */
		public String getObjectHeader() {
			return object.getObjectHeader();
		}
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getObjectLabel()
		 */
		public Object getObjectLabel() {
			return object.objectLabel;
		}
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getAction()
		 */
		public String getAction() {
			return action;
		}
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getField()
		 */
		public FieldEventI getField() {
			return field;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#getParents()
		 */
		public List<FlattenedItemA.ItemObject> getParents(){
			return parents;
		}
		
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.ItemEventI#toJSON()
		 */
		public JSONObject toJSON() throws JSONException{
			JSONObject o= new JSONObject();
			
			o.put("objectHeader", object.objectHeader);
			o.put("objectLabel", object.objectLabel);
			o.put("action", action);
			if(field!=null){
				o.put("field", field.toJSON());
			}
			
			return o;
		}
	}
	
	public interface FieldEventI {
		public String getXsiPath();
		public String getFieldLabel();
		public abstract JSONObject toJSON() throws JSONException;

		public Object getNewValue();

		public Object getOldValue();
	}
	
	public static class FieldEvent implements FieldEventI{
		final String xsiPath;
		final String fieldLabel;
		final Object newValue;
		final Object oldValue;
		
		public FieldEvent(String xsiPath, String fieldLabel, Object newValue, Object oldValue) {
			super();
			this.xsiPath = xsiPath;
			this.fieldLabel = fieldLabel;
			this.newValue = newValue;
			this.oldValue = oldValue;
		}

		public String getXsiPath() {
			return xsiPath;
		}

		public String getFieldLabel() {
			return fieldLabel;
		}

		public Object getNewValue() {
			return newValue;
		}

		public Object getOldValue() {
			return oldValue;
		}
		/* (non-Javadoc)
		 * @see org.nrg.xft.presentation.FieldEventI#toJSON()
		 */
		public JSONObject toJSON() throws JSONException{
			JSONObject o= new JSONObject();
			
			o.put("xsiPath", xsiPath);
			o.put("fieldLabel", fieldLabel);
			o.put("newValue", newValue);
			o.put("oldValue", oldValue);
			
			return o;
		}
	}
	
	public interface EventBuilderI{
		public List<ItemEventI> build(ItemObject io,String action,FieldEventI fe, String username, List<ItemObject> parents, FlattenedItemI fi);
	}
	
	public static class DefaultBuilder implements EventBuilderI{
		public List<ItemEventI> build(ItemObject io,String action,FieldEventI fe, String username, List<ItemObject> parents, FlattenedItemI fi){
			List<ItemEventI> items= Lists.newArrayList();
			items.add(new ItemEvent(io
					,action
					,fe
					,parents,username));
			return items;
		}
	}
}
