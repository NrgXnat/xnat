/*
 * core: org.nrg.xft.presentation.ItemHtmlBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.presentation;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.utils.DateUtils;


public class ItemHtmlBuilder extends ItemHistoryBuilder {
	protected final Date as_of_date;
	public ItemHtmlBuilder(){
		this.as_of_date=null;
	}
	
	public ItemHtmlBuilder(Date as_of_date){
		this.as_of_date=as_of_date;
	}
	
	public static String build(Date as_of_date, List<FlattenedItemI> items) throws Exception{
		return (new ItemHtmlBuilder(as_of_date)).call(items);
	}

	public String call(List<FlattenedItemI> items) throws Exception {
		return buildLayeredHTML(items);
	}

	public interface FormatTRI{
		public String assignTRStyle(FlattenedItemI i);
	}
	
	public  String buildLayeredHTML(List<FlattenedItemI> items) throws Exception{
		final StringBuffer sb  = new StringBuffer();
		sb.append("<TABLE>\n");
		
		buildLayeredHTML(new FlattenedItemA.ChildCollection(items,"",""), 0, sb,"",null);
		
		sb.append("</TABLE>");
		return sb.toString();
	}

	public  void buildLayeredHTML(FlattenedItemA.ChildCollection all_props, int level, StringBuffer sb,final String label, Integer parent) throws Exception{
		//column header row

		sb.append("<tr class=\"child" + parent + "\">");
		sb.append("<th class=\"meta\"></th>");
		sb.append("<th class=\"meta\"></th>");
		sb.append("<th class=\"meta\"></th>");
		sb.append("<th class=\"meta rightBar\"></th>");
		
		sb.append("<th align=\"right\" NOWRAP ");
		sb.append(" colspan=\"").append((level+1));
		sb.append("\">");
		if(!StringUtils.isEmpty(label)){
			sb.append(label).append(" (" + all_props.getChildren().size() +")");
		}
		sb.append("</th>");
		
		sb.append("<th align=\"left\">xsi:type</th>");
		for(final String header:all_props.getHeaders().values()){
			sb.append("<th>").append(header).append("</th>");
		}
		sb.append("</tr>\n");
		
		FlattenedItemI previous=null;
		int counter=0; 
		for(final FlattenedItemI params:all_props.getChildren()){	
			outputRow(sb, params, previous, parent, label, level, all_props.getHeaders().keySet());
			previous=params;
			for(FlattenedItemI his: params.getHistory()){
				outputRow(sb, his, previous, parent, label, level, all_props.getHeaders().keySet());
				previous=his;
			}
			
			//final XMLWrapperElement root = convertElement(item.getGenericSchemaElement());
			//renderChildren(item,root,sb,level,root.getChildren());
			  
			renderChildren(params.getChildCollections(),level+1,sb, params.getId());
			counter++;
			previous=params;
		}
	}

	public boolean isNew(FlattenedItemI i) {
		if(as_of_date!=null){
			if(i.getStartDate()!=null && as_of_date.equals(i.getStartDate())){
				return true;
			} 
			
			if(i.getLast_modified()!=null && as_of_date.equals(i.getLast_modified())){
				return true;
			}
		}
		
		return false;
	}

	public boolean isBrandNew(FlattenedItemI i) {
		if(as_of_date!=null){
			if(i.getInsert_date()!=null && DateUtils.isEqualTo(as_of_date,i.getInsert_date())){
				return true;
			} 
		}
		
		return false;
	}

	public boolean isOld(FlattenedItemI i) {
		if(as_of_date!=null){
			if(i.getEndDate()!=null && DateUtils.isEqualTo(as_of_date,i.getEndDate())){
				return true;
			}
		}
		
		return false;
	}
	
	private void outputRow(StringBuffer sb, FlattenedItemI params, FlattenedItemI previous, Integer parent, String label, int level, Set<String> headers){
		sb.append("<tr ");
		sb.append(" class=\"child" + parent );
		if(isOld(params) && params.isDeleted()){
			sb.append(" _old ");
		}else if(isBrandNew(params)){
			sb.append(" _new ");
		}
		sb.append("\">");
		sb.append("<td class=\"meta\" NOWRAP>");
		if(params.getCreateEventId()!=null){
			sb.append(params.getCreateEventId());
		}
		sb.append("</td>");	
		
		sb.append("<td class=\"meta\" NOWRAP>");
		if(params.getModifiedEventId()!=null){
			sb.append(params.getModifiedEventId());
		}
		sb.append("</td>");	
		
		sb.append("<td class=\"meta\" NOWRAP>");
		if(params.getStartDate()!=null)
			sb.append(params.getStartDate());
		sb.append("</td>");	
		
		sb.append("<td class=\"meta rightBar\" NOWRAP>");
		if(params.getEndDate()!=null)
			sb.append(params.getEndDate());
		sb.append("</td>");	
			 
		sb.append("<td align=\"right\" colspan=\"").append((level+1)).append("\" NOWRAP");
		if(isNew(params)){
			sb.append(" class=\"_new\" ");
		}else if(isOld(params)){
			sb.append(" class=\"_old\" ");
		}
		sb.append(">");
		if(!StringUtils.isEmpty(label) && parent!=null){
			if(params.getTotalChildren()>0){
				sb.append("<a onclick=\"toggleRows('child" + params.getId() + "');\">").append(label).append(" (" + params.getTotalChildren()+")</a>");
			}else{
				sb.append(label);
			}
		}
		sb.append("</td>");
		sb.append("<td>").append(params.getXSIType()).append("</td>");	
		
		FlattenedItemI previousMatch=(previous!=null && FlattenedItemA.isLike(previous,params))?previous:null;
		for(final String header:headers){
			sb.append("<td");
			if(isNew(params) && (previousMatch!=null)  && differBy(params,previousMatch,header)){
				sb.append(" class=\"_old\" ");
			}
			sb.append(">");
			if(params.getFields().getParams().get(header)!=null)
				sb.append(params.getFields().getParams().get(header));
			sb.append("</td>");
		}
		sb.append("</tr>\n");
	}
	
	public  void renderChildren(Map<String,FlattenedItemA.ChildCollection> items, int level, StringBuffer sb, Integer parent) throws Exception{
		for(Map.Entry<String,FlattenedItemA.ChildCollection> entry: items.entrySet()){
			buildLayeredHTML(entry.getValue(), level, sb, entry.getValue().getName(),parent);
		}
	}
}
