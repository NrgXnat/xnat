
//Author: James Dickson <james@radiologics.com>
package org.nrg.xnat.services.triage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TriageManifest {
	
	
	public TriageManifest() {
		super();
	}

	private List<Map<String,String>> properties =new ArrayList<Map<String,String>>();

	public List<Map<String, String>> getProperties() {
		return properties;
	}

	public void setProperties(List<Map<String, String>> properties) {
		this.properties = properties;
	}
	
	public void addEntry(Map<String,String> entry){
		properties.add(entry);
	}
	
	void deleteEntry(Map<String,String> entry){
		if (properties.contains(entry)){
			properties.remove(entry);
		}
	}
	void deleteEntry(String value){
		for (Map<String, String> map : properties) {
			if(map.containsValue(value)){
				properties.remove(map);
				return;
			}
		}	
	}

	public String getMatchingEntry(String key, String value, String otherkey) {
		for (Map<String, String> map : properties) {
			if(map.get(key).endsWith(value)){
				return map.get(otherkey);
			}
		}	
		return null;
	}
	
	public String getFirstMatchingEntry(String otherkey) {
		for (Map<String, String> map : properties) {
			if(map.containsKey(otherkey)){
				return map.get(otherkey);
			}
		}
		return null;
	}
}