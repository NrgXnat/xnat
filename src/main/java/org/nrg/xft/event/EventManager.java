// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.event;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.nrg.xft.ItemI;

public class EventManager {
	private final static String GLOBAL="G";
	private static EventManager em=null;
	private Map<String,List<EventListener>> listeners=null;
	
	private EventManager(){
		listeners=new Hashtable<String,List<EventListener>>();
	};
		
	public void addListener(String xsiType,EventListener ev){
		List<EventListener> l=listeners.get(xsiType);
		if(l==null){
			l=new ArrayList<EventListener>();
			listeners.put(xsiType, l);
		}
		
		l.add(ev);
	}
		
	public void addListener(EventListener ev){
		List<EventListener> l=listeners.get(GLOBAL);
		if(l==null){
			l=new ArrayList<EventListener>();
			listeners.put(GLOBAL, l);
		}
		
		l.add(ev);
	}
	
	public void removeListener(String xsiType,EventListener ev){
		List<EventListener> l=listeners.get(xsiType);
		if(l!=null){
			if(l.contains(ev)){
				l.remove(ev);
			}
		}
	}
	
	public void trigger(Event e) throws Exception{
		List<EventListener> performed=new ArrayList<EventListener>();
		
		List<EventListener> list=listeners.get(GLOBAL);
		Exception ex=null;
		if(list!=null){
			for(EventListener l:list){
				try {
					if(!performed.contains(l)){
						performed.add(l);
						l.handleEvent(e);
					}
				} catch (Exception e1) {
					if(ex!=null)ex=e1;
				}
			}
		}
		
		list=listeners.get(e.getXsiType());
		if(list!=null){
			for(EventListener l:list){
				try {
					if(!performed.contains(l)){
						performed.add(l);
						l.handleEvent(e);
					}
				} catch (Exception e1) {
					if(ex!=null)ex=e1;
				}
			}
		}
		
		if(ex!=null){
			throw ex;
		}
	}	

	
	public static EventManager GetInstance(){
		if(null==em){
			em= new EventManager();
		}
		return em;
	}
	
	public static void AddListener(EventListener ev){
		GetInstance().addListener(ev);
	}
	
	public static void AddListener(String xsiType,EventListener ev){
		GetInstance().addListener(xsiType,ev);
	}
	
	public static void Trigger(Event e) throws Exception{
		GetInstance().trigger(e);
	}
	
	public static void Trigger(String xsiType, String id, ItemI item, String action) throws Exception{
		Event e = new Event(xsiType,action);
		e.setId(id);
		e.setItem(item);
		Trigger(e);
	}
	
	public static void Trigger(String xsiType, String id, String action) throws Exception{
		Event e = new Event(xsiType,action);
		e.setId(id);
		Trigger(e);
	}
	
	public static void Trigger(String xsiType, String action) throws Exception{
		Event e = new Event(xsiType,action);
		Trigger(e);
	}
}
