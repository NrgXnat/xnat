// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.cache;

import java.util.Hashtable;
import java.util.Map;

import org.nrg.xft.event.Event;
import org.nrg.xft.event.EventListener;
import org.nrg.xft.event.EventManager;

public class CacheManager extends Object implements EventListener {
	private Map<String,Map<Object,Object>> cache=new Hashtable<String,Map<Object,Object>>();
	
	private static CacheManager cm=null;
	
	public static CacheManager GetInstance(){
		if(cm==null){
			cm=new CacheManager();
			EventManager.AddListener(cm);
		}
		
		return cm;
	}
	
	public void clearAll(){
		for(Map<Object,Object> m:cache.values()){
			m.clear();
		}
		cache.clear();
	}
	
	public Object retrieve(String xsiType,Object id){
		Map<Object,Object> items=cache.get(xsiType);
		if(items==null)return null;
		return items.get(id);
	}
	
	public synchronized void put(String xsiType,Object id,Object i){
		if(i==null)throw new NullPointerException();
		if(id==null)throw new NullPointerException();
		if(xsiType==null)throw new NullPointerException();
		
		Map<Object,Object> items=cache.get(xsiType);
		if(items==null){
			items=new Hashtable<Object,Object>();
			cache.put(xsiType,items);
		}
		
		items.put(id,i);
	}
	
	public synchronized Object remove(String xsiType,Object id){
		Map<Object,Object> items=cache.get(xsiType);
		if(items==null){
			items=cache.put(xsiType, new Hashtable<Object,Object>());
		}
		
		return items.remove(id);
	}
	
	public void handleEvent(Event e) throws Exception {
		if(e.getXsiType()==null)return;
		
		Map<Object,Object> items=cache.get(e.getXsiType());
		
		if(items==null)return;//if null, then we aren't listening to this type yet.
		
		if((e.getAction().equals(Event.CREATE))){
			if(e.getId()!=null && e.getItem()!=null){
				items.put(e.getId(),e.getItem());
			}	
		}else if((e.getAction().equals(Event.UPDATE))){
			if(e.getId()!=null && e.getItem()!=null){
				items.put(e.getId(),e.getItem());
			}else if(e.getId()!=null){
				items.remove(e.getId());
			}else{
				items.clear();
			}
		}else if((e.getAction().equals(Event.DELETE))){
			if(e.getId()!=null){
				items.remove(e.getId());
			}else{
				items.clear();
			}
		}
	}
}
