/*
 * core: org.nrg.xft.presentation.ItemJSONBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

//Copyright 2012 Radiologics, Inc.  All Rights Reserved
package org.nrg.xft.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xft.XFTItem;

public class ItemJSONBuilder {
	
	public ItemJSONBuilder(){
	}

	public JSONObject call(XFTItem i,final FlattenedItemA.HistoryConfigI includeHistory,boolean includeHeaders) throws Exception {
		return buildJSON(i,includeHistory,includeHeaders);
	}
	
	public static JSONObject buildJSON(XFTItem item,final FlattenedItemA.HistoryConfigI includeHistory,boolean includeHeaders) throws Exception{
		List<FlattenedItemI> items=(new ItemPropBuilder()).call(item,includeHistory,null);
		
		return convert(items,includeHeaders);
	}
	
	public static JSONObject convert(List<FlattenedItemI> items,boolean includeHeaders) throws JSONException{
		JSONObject wrapper = new JSONObject();
		JSONArray objects=new JSONArray();
		List<String> headers = new ArrayList<String>();
		for(FlattenedItemI params: items){
			JSONObject o= new JSONObject();	
			
			JSONObject meta= new JSONObject();	
			meta.put("isHistory",params.isHistory());

			if(params.getCreateEventId()!=null){
				meta.put("create_event_id",params.getCreateEventId());
			}

			if(params.getModifiedEventId()!=null){
				meta.put("modified_event_id",params.getModifiedEventId());
			}

			if(params.getStartDate()!=null){
				meta.put("start_date",params.getStartDate());
			}

			if(params.getEndDate()!=null){
				meta.put("end_date",params.getEndDate());
			}

			if(params.getXSIType()!=null){
				meta.put("xsi:type",params.getXSIType());
			}
			o.put("meta", meta);
			
			JSONObject fields=new JSONObject();		
			for(final String key: params.getFields().getParams().keySet()){
				if(!headers.contains(key)){
					headers.add(key);
				}
				final Object value = params.getFields().getParams().get(key);
				try {
					fields.put(key, value);
				} catch (JSONException e) {
					if (value != null && value instanceof Number && (value.equals(Double.NaN) || value.equals(Float.NaN))) {
						fields.put(key, value.toString());
					} else {
						throw e;
					}
				}
			}
			o.put("data_fields",fields);
			
			JSONArray children=new JSONArray();
			for(Entry<String,FlattenedItemA.ChildCollection> entry: params.getChildCollections().entrySet()){
				JSONObject child=convert(entry.getValue().getChildren(),includeHeaders);
				child.put("field",entry.getKey());
				
				children.put(child);
			}
			o.put("children", children);
			objects.put(o);
		}
		
		wrapper.put("items", objects);
		if(includeHeaders){
			wrapper.put("headers", headers);
		}
		return wrapper;
	}
}
