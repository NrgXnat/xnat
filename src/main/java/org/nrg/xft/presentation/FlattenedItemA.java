//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.utils.DateUtils;

public abstract class FlattenedItemA implements FlattenedItemI{

	public static final String DELETED = "deleted";
	protected FlattenedItemA.ItemObject o;
	final List<FlattenedItemA.ItemObject> parents=new ArrayList<FlattenedItemA.ItemObject>();
	
	FlattenedItemA.FieldTracker ft=new FlattenedItemA.FieldTracker();
	final boolean isHistory;
	Map<String,FlattenedItemA.ChildCollection> children = new Hashtable<String,FlattenedItemA.ChildCollection>();
	List<FlattenedItemI> history = new ArrayList<FlattenedItemI>();
	
	private final Date last_modified;
	private final Date insert_date;
	final String status,xsiType;
	
	private Date previous_change_date;
	
	private Date change_date;
	
	protected String create_username;
	protected String modified_username;
	
	Number modified_event_id=null;
	Number create_event_id=null;
	
	public Integer id;
	
	public List<FileSummary> misc=new ArrayList<FileSummary>();


	protected List<String> pks ;
	protected List<String> displayIdentifiers;
	public static HistoryConfigI GET_ALL = new HistoryConfigI(){
		public boolean getIncludeHistory() {
			return true;
		}
	};
	
	public List<FileSummary> getMisc(){
		return misc;
	}
	
	public static class FileSummary{
		public Integer change_id;
		public Date date;
		public String action;
		public Integer count;
		public FileSummary(Integer change_id, Date date, String action,	Integer count) {
			super();
			this.change_id = change_id;
			this.date = date;
			this.action = action;
			this.count = count;
		}
	}
	
	public static Date standardizeDate(Date d){
		if(d instanceof Timestamp){
			return DateUtils.toDate((Timestamp)d);
		}else{
			return d;
		}
	}
	
	public FlattenedItemA(FlattenedItemA.FieldTracker ft,boolean isHistory,Date last_modified,Date insert_date,String status,Integer id,String xsiType,List<FlattenedItemA.ItemObject> parents){
		this.ft=ft;
		this.isHistory=isHistory;
		this.last_modified=standardizeDate(last_modified);
		this.insert_date=standardizeDate(insert_date);
		this.status=status;
		this.id=id;
		this.xsiType=xsiType;
		this.parents.addAll(parents);
	}
	
	public FlattenedItemA(FlattenedItemA fi) throws Exception{
		this.ft=fi.getFields().copy();
		this.isHistory=fi.isHistory;
		this.last_modified=standardizeDate(fi.last_modified);
		this.insert_date=standardizeDate(fi.insert_date);
		this.status=fi.status;
		this.id=fi.id;

		this.change_date=fi.change_date;
		this.create_username=fi.create_username;
		this.modified_username=fi.modified_username;
		this.create_event_id=fi.create_event_id;
		this.modified_event_id=fi.modified_event_id;
		this.previous_change_date=fi.previous_change_date;
		this.xsiType=fi.xsiType;
		this.pks=fi.pks;
		this.displayIdentifiers=fi.displayIdentifiers;
		
		this.o=fi.o.copy();
	}
	
	public String toString(){
		return "("+this.getItemObject()+"):(" + this.getFields() + "):"+DateUtils.format(this.getInsert_date(), "ddhhmm.SSS")+":"+this.getCreateEventId()+":"+this.getCreateUsername()+":"+this.getModifiedEventId()+":"+this.getModifiedUsername() +"\n";
	}

	public Integer getId() {
		return id; 
	}
	
	public Number getModified_event_id() {
		return modified_event_id;
	}

	public void setModified_event_id(Number modified_event_id) {
		this.modified_event_id = modified_event_id;
	}

	public Number getCreate_event_id() {
		return create_event_id;
	}

	public void setCreate_event_id(Number create_event_id) {
		this.create_event_id = create_event_id;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isDeleted()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isDeleted()
	 */
	public boolean isDeleted(){
		return DELETED.equals(status);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getItemObject()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getItemObject()
	 */
	public FlattenedItemA.ItemObject getItemObject() {
		return o;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChange_date()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChange_date()
	 */
	public Date getChange_date() {
		return change_date;
	}

	public void setPrevious_change_date(Date previous_change_date) {
		this.previous_change_date = standardizeDate(previous_change_date);
	}

	public void setChange_date(Date change_date) {
		this.change_date = standardizeDate(change_date);
	}

	public String getCreateUsername(){
		return create_username;
	}
	public String getModifiedUsername(){
		return modified_username;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getFields()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getFields()
	 */
	public FlattenedItemA.FieldTracker getFields() {
		return ft;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isHistory()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isHistory()
	 */
	public boolean isHistory() {
		return isHistory;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getLast_modified()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getLast_modified()
	 */
	public Date getLast_modified() {
		return last_modified;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getInsert_date()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getInsert_date()
	 */
	public Date getInsert_date() {
		return insert_date;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getXSIType()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getXSIType()
	 */
	public String getXSIType() {
		return xsiType;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getPrevious_change_date()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getPrevious_change_date()
	 */
	public Date getPrevious_change_date() {
		return previous_change_date;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChildCollections()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getChildCollections()
	 */
	public Map<String, FlattenedItemA.ChildCollection> getChildCollections(){
		return children;
	}
	
	public void setChildCollections(Map<String, FlattenedItemA.ChildCollection> children){
		this.children=children;
	}

	public void addChildCollection(String path,String display, List<FlattenedItemI> items){
		children.put(path,new FlattenedItemA.ChildCollection(items,path,display));
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getHistory()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getHistory()
	 */
	public List<FlattenedItemI> getHistory() {
		return history;
	}

	public void setHistory(List<FlattenedItemI> history) {
		this.history=history;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getTotalChildren()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getTotalChildren()
	 */
	public int getTotalChildren(){
		int count=0;
		for(Map.Entry<String,FlattenedItemA.ChildCollection> entry: children.entrySet()){
			count+=entry.getValue().getChildren().size();
		}
		return count;
	}
	
	public static class FieldTracker{
		final Map<String,Object> params=new Hashtable<String,Object>();
		final Map<String,String> headers=new Hashtable<String,String>();
		
		public Map<String,Object> getParams(){
			return params;
		}
		
		public Map<String,String> getHeaders(){
			return headers;
		}
		
		public void put(String key, Object v){
			params.put(key,v);
		}
		
		public void addHeader(String key, String v){
			headers.put(key,v);
		}
		
		public void putAll(FieldTracker ft){
			this.getHeaders().putAll(ft.getHeaders());
			this.getParams().putAll(ft.getParams());
		}
		
		public FieldTracker copy(){
			FieldTracker ft= new FieldTracker();
			ft.putAll(this);
			return ft;
		}
		
		public String toString(){
			return params.toString();
		}
	}

	public static interface HistoryConfigI{
		boolean getIncludeHistory();
	}

	public static class ChildCollection{
		private Map<String,String> headers;
		private List<FlattenedItemI> children;
		private final String path;
		private final String name;
		
		public String getPath() {
			return path;
		}
		
		public String getName() {
			return name;
		}
	
		public Map<String,String> getHeaders() {
			return headers;
		}
	
		public List<FlattenedItemI> getChildren() {
			return children;
		}
		
		public void setChildren(List<FlattenedItemI> chilren){
			this.children=chilren;
		}
		
		public ChildCollection copy(FlattenedItemA.FilterI filter) throws Exception{
			return new ChildCollection(FlattenedItemA.filter(children, filter),path, name);
		}
		
		public void buildHeaders(){ 
			headers=new Hashtable<String,String>();
			for(FlattenedItemI i: children){
				addHeaders(headers,i);
				
				for(final FlattenedItemI fi:i.getHistory()){
					addHeaders(headers,fi);
				}
			}
		}
		
		public static void addHeaders(Map<String,String> headers, FlattenedItemI i){
			for(Map.Entry<String,String> entry: i.getFields().getHeaders().entrySet()){
				if(!headers.containsKey(entry.getKey())){
					headers.put(entry.getKey(),entry.getValue());
				}
			}
		}
	
		public ChildCollection(List<FlattenedItemI> children,String path,String name){
			this.children=children;
			this.name=(name==null)?(path.contains("/"))?path.substring(path.indexOf("/")+1):path:name;
			this.path=path;
			buildHeaders();
		}
		
		public String toString(){
			StringBuilder sb= new StringBuilder();
			for(FlattenedItemI i: children){
				sb.append(i);
				
				for(final FlattenedItemI fi:i.getHistory()){
					sb.append("H:").append(fi);
				}
			}
			return sb.toString();
		}
	}

	public static class ItemObject{
		public final String objectHeader;
		public final Object objectLabel;
		public final Object objectId;
		public final List<String> xsiType;
		
		public ItemObject(String objectHeader, Object objectLabel, Object objectId, List<String> xsiType){
			this.objectHeader=objectHeader;
			this.objectLabel=objectLabel;
			this.objectId=objectId;
			this.xsiType=xsiType;
		}
		
		final public static Map<String,String> static_renaming=new HashMap<String,String>(){{
			put("XDATUser","User");
		}};
		
		public String getObjectHeader() {
			if(static_renaming.containsKey(objectHeader)){
				return static_renaming.get(objectHeader);
			}else{
				return objectHeader;
			}
		}
		public Object getObjectLabel() {
			return objectLabel;
		}
		public Object getObjectId() {
			return objectId;
		}
		public List<String> getXsiTypes() {
			return xsiType;
		}
		
		public ItemObject copy(){
			return new ItemObject(objectHeader,objectLabel,objectId,xsiType);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public int compareTo(ItemObject io){
			int i=io.getObjectHeader().compareTo(this.objectHeader);
			
			if(i!=0){
				return i;
			}else{
				return ((Comparable)io.objectId).compareTo(((Comparable)this.objectId));
			}
		}
		
		public boolean equals(ItemObject io){
			return (compareTo(io)==0);
		}
		
		public String toString(){
			return objectHeader+","+objectLabel+","+objectId;
		}
	}

	public static interface FilterI{
		boolean accept(FlattenedItemI i);
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isEqualTo(org.nrg.xft.presentation.FlattenedItemI)
	 */
	public static boolean isEqualTo(FlattenedItemI o1, FlattenedItemI o2){
		if((o1.isHistory()!=o2.isHistory())){
			return false;
		}

		if((!DateUtils.isEqualTo(o1.getStartDate(),o2.getStartDate()))){
			return false;
		}

		if((!DateUtils.isEqualTo(o1.getEndDate(),o2.getEndDate()))){
			return false;
		}
		
		if(FlattenedItemA.isLike(o1,o2)){
			for(Map.Entry<String,Object> entry:o2.getFields().getParams().entrySet()){
				if(o1.getFields().getParams().containsKey(entry.getKey())){
					if(!entry.getValue().equals(o1.getFields().getParams().get(entry.getKey()))){
						return false;
					}
				}else{					
					return false;
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getPks()
	 */
	public List<String> getPks(){
		return pks;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#isLike(org.nrg.xft.presentation.FlattenedItemI)
	 */
	public static boolean isLike(FlattenedItemI o1, FlattenedItemI o2){	
		List<String> pks=o2.getPks();
		
		if(pks.size()==0){
			return false;
		}
		
		boolean matched=true;
		for(final String key:pks){
			Object f1=o2.getFields().getParams().get(key);
			Object f2=((FlattenedItemI)o1).getFields().getParams().get(key);
			
			if(!ObjectUtils.equals(f1, f2)){
				matched=false;
				break;
			}
		}
		
		return matched;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getEndDate()
	 */
	public Date getEndDate(){
		if(getChange_date()!=null){
			return getChange_date();
		}else{
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getStartDate()
	 */
	public Date getStartDate(){
		if(getPrevious_change_date()!=null){
			return getPrevious_change_date();
		}else{
			return getLast_modified();
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getParents()
	 */
	/* (non-Javadoc)
	 * @see org.nrg.xft.presentation.FlattenedItemI#getParents()
	 */
	public List<FlattenedItemA.ItemObject> getParents() {
		return parents;
	}

	public static List<FlattenedItemI> filter(List<FlattenedItemI> items, FlattenedItemA.FilterI filter) throws Exception{
		if(filter==null)return items;
		
		List<FlattenedItemI> temp=new ArrayList<FlattenedItemI>();
		for(FlattenedItemI fi:items){
			if(filter.accept(fi)){
				temp.add(fi.filteredCopy(filter));
			}
		}
		return temp;
	}

	public static Date parseDate(Object d) throws ParseException{
		if(d==null || d instanceof Date){
			return (Date)d;
		}else{
			return DateUtils.parseDateTime(d.toString());
		}
	}

	public static void putValue(String xmlPath, String name, Object value,FlattenedItemA.FieldTracker params){
		if (value != null)
		{
			params.addHeader(xmlPath,name);
			
			if (value.getClass().getName().equalsIgnoreCase("[B"))
			{
				byte[] b = (byte[]) value;
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				try {
					baos.write(b);
				} catch (Exception e) {
				}
				params.put(xmlPath, baos.toString());
			}else{
				params.put(xmlPath, value);
			}
		}
	}

	static Map<Number,Number> xft_version_map=Collections.synchronizedSortedMap(new TreeMap<Number,Number>());;
	public synchronized static Number translateXFTVersion(Number o) {
		final Long key;
		if(o instanceof Long){
			key=(Long)o;
		}else{
			key=Long.valueOf(o.toString());
		}
		
		Number n=xft_version_map.get(key);
		if(n==null){
			try {
				n=(Number)PoolDBUtils.ReturnStatisticQuery(String.format("SELECT event_id FROM xdat_change_info WHERE xdat_change_info_id=%s",key.toString()), "event_id", null, null);
			} catch (Exception e) {}
			
			if(n==null && o instanceof Integer){
				xft_version_map.put(key,o);
				return o;
			}else{
				xft_version_map.put(key,n);
				return n;
			}
		}else{
			return n;
		}
	}

	public Number getCreateEventId() {
		if(getCreate_event_id()==null) return (getStartDate()==null)?null:getStartDate().getTime();
		return translateXFTVersion(getCreate_event_id());
	}

	public Number getModifiedEventId() {
		if(getModified_event_id()==null) return (getEndDate()==null)?null:getEndDate().getTime();
		return translateXFTVersion(getModified_event_id());
	}
}