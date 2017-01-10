/*
 * web: org.nrg.xnat.turbine.utils.IDGenerator
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.identifier.IDGeneratorI;
import org.nrg.xnat.services.system.HostInfoService;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class IDGenerator.
 */
public class IDGenerator implements IDGeneratorI {
	
	/** The column. */
	String column=null;
	
	/** The table name. */
	String tableName=null;
	
	/** The digits. */
	Integer digits=null;
	
	/** The code. */
	String code=null;
	
	/** The host info. */
	private static String hostInfo=null;
	
	/**
	 * Gets the host info.
	 *
	 * @return the host info
	 */
	private static String getHostInfo(){
		if (hostInfo==null || hostInfo.isEmpty()) {
			hostInfo =  XDAT.getContextService().getBean(HostInfoService.class).getHostNumber();
		}
		return hostInfo;
	}
	
	/** The claimed i ds. */
	private static List<String> claimedIDs= new ArrayList<>();
	
	/** The Constant lock. */
	private static final Object lock=new Object();
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#generateIdentifier()
	 */
	public String generateIdentifier() throws Exception{
		synchronized (lock){
			String site= getSiteID();
			String hostInfo = IDGenerator.getHostInfo();
			// Let's keep the usual ID for the main server and append the host information for shadow/secondary servers
			if (Integer.valueOf(hostInfo)>1) {
				site+=hostInfo;
			}
			
			if(code!=null){
				site +="_"+code;
			}else if(tableName.equalsIgnoreCase("xnat_subjectData")){
				site +="_S";
			}else if(tableName.equalsIgnoreCase("xnat_experimentData")){
				site +="_E";
			}else if(tableName.equalsIgnoreCase("xnat_pvisitData")){
				site +="_V";
			}
			
			String temp_id;
			
			XFTTable table = org.nrg.xft.search.TableSearch.Execute("SELECT DISTINCT " + column + " FROM (SELECT " + column + " FROM " + tableName + " WHERE " + column + " LIKE '" + site + "%' UNION SELECT DISTINCT " + column + " FROM " + tableName + "_history WHERE " + column + " LIKE '" + site + "%') SRCH;", null, null);
	        ArrayList al =table.convertColumnToArrayList(column.toLowerCase());
	        
	        if (al.size()>0 || claimedIDs.size()>0){
	            int count =al.size()+1;
	            String full = StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
	            temp_id = site+ full;
	
	            while (al.contains(temp_id) || claimedIDs.contains(temp_id)){
	                count++;
	                full =StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
	                temp_id = site+ full;
	            }
	            
	            claimedIDs.add(temp_id);
	
	            return temp_id;
	        }else{
	            int count =1;
	            String full = StringUtils.leftPad((new Integer(count)).toString(), digits, '0');
	            temp_id = site+ full;
	            return temp_id;
	        }
		}
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#getColumn()
	 */
	public String getColumn() {
		return column;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#getDigits()
	 */
	public Integer getDigits() {
		return digits;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#getTable()
	 */
	public String getTable() {
		return tableName;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#setColumn(java.lang.String)
	 */
	public void setColumn(String s) {
		this.column=s;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#setDigits(java.lang.Integer)
	 */
	public void setDigits(Integer i) {
		this.digits=i;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#setTable(java.lang.String)
	 */
	public void setTable(String s) {
		this.tableName=s;
	}


	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#getCode()
	 */
	public String getCode() {
		return code;
	}

	/* (non-Javadoc)
	 * @see org.nrg.xft.identifier.IDGeneratorI#setCode(java.lang.String)
	 */
	public void setCode(String s) {
		this.code=s;
	}
	
	/**
	 * Gets the site id.
	 *
	 * @return the site id
	 */
	private String getSiteID(){
		String site_id = XDAT.getSiteConfigPreferences().getSiteId();
		site_id = StringUtils.replace(site_id, " ", "");
		site_id = StringUtils.replace(site_id, "-", "_");
		site_id = StringUtils.replace(site_id, "\"", "");
		site_id = StringUtils.replace(site_id, "'", "");
		site_id = StringUtils.replace(site_id, "^", "");
		return site_id;
	}
}
