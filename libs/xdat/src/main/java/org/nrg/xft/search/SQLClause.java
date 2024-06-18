/*
 * core: org.nrg.xft.search.SQLClause
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.RegExUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.db.PoolDBUtils;

/**
 * @author Tim
 */
public interface SQLClause {

		public static final String _CLOSE = "}";
	  public static final String _OPEN = "${";

    String getElementName();

    String getSQLClause(QueryOrganizerI qo) throws Exception;

    @SuppressWarnings("rawtypes")
    ArrayList getSchemaFields() throws Exception;

    ArrayList<DisplayCriteria> getSubQueries() throws Exception;

    int numClauses();
    default void hackCheck(final String value) throws Exception {
        hackCheck(value, true);
    }

    default void hackCheck(final String value, final boolean hackCheck) throws Exception {
        if (hackCheck && PoolDBUtils.HackCheck(value)) {
            if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                final String type = "VALUE: " + value;
                final String body = RegExUtils.replaceAll(RegExUtils.replaceAll(XDAT.getNotificationsPreferences().getEmailMessageUnauthorizedDataAttempt(),
                                                                                "TYPE",
                                                                                type),
                                                          "USER_DETAILS",
                                                          "");
                AdminUtils.sendAdminEmail("Possible SQL Injection Attempt", body);
            }
            throw new Exception("Invalid search value (" + value + ")");
        }
    }

    class ParamValue
	{
		private Object value;
		private int type;

		public ParamValue(Object v, int t){
			value=v;
			type=t;
		}

		public Object getValue(){
			return value;
		}

		public int getType(){
			return type;
		}
	}

	class ValueTracker
	{
		private Map<String,ParamValue> values= Maps.newHashMap();

		public Map<String,ParamValue> getValues(){
			return values;
		}

		public String trackValue(Object v, int type){
			return trackValue(new ParamValue(v,type));
		}

		public String trackValue(ParamValue pv){
			String key= _OPEN +values.size()+ _CLOSE;
			values.put(key,pv);
			return key;
		}
	}
	    
	public SQLClause templatizeQuery(ValueTracker tracker) throws Exception;
}

