/*
 * org.nrg.xdat.om.base.BaseXnatSubjectassessordata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/30/14 11:48 AM
 */
package org.nrg.xdat.om.base;

import org.nrg.dcm.CopyOp;
import org.nrg.transaction.OperationI;
import org.nrg.transaction.TransactionException;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatSubjectassessordata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xnat.helpers.merge.ProjectAnonymizer;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatSubjectassessordata extends AutoXnatSubjectassessordata {

	public BaseXnatSubjectassessordata(ItemI item)
	{
		super(item);
	}

	public BaseXnatSubjectassessordata(UserI user)
	{
		super(user);
	}

	public BaseXnatSubjectassessordata()
	{}

	public BaseXnatSubjectassessordata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    private XnatSubjectdata subject = null;
	public XnatSubjectdata getSubjectData()
	{
	    if (subject==null)
	    {
            if (getSubjectId()!=null)
            {
                ArrayList al = XnatSubjectdata.getXnatSubjectdatasByField("xnat:subjectData/ID",this.getSubjectId(),this.getUser(),false);
                if (al.size()>0)
                {
                    subject = (XnatSubjectdata)al.get(0);
                }
            }
	    }
	    return subject;
	}


    public String getSubjectAge() {
        if (this.getAge()!=null){
            Double d = getAge();
            NumberFormat formatter = NumberFormat.getInstance();
            formatter.setGroupingUsed(false);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            return formatter.format(d);
        }

        XnatSubjectdata s = this.getSubjectData();
        if (s == null) {
            return "--";
        } else {
            try {
                Object o = this.getDate();
                if (o instanceof String)
                {
                    Date expt_date = DateUtils.parseDateTime((String)o);
                    return s.getAge(expt_date);
                }else{
                    Date expt_date = (Date) this.getDate();
                    return s.getAge(expt_date);
                }
            } catch (Exception e) {
                logger.error("", e);
                if (s.getAge()!=null)
                {
                    return s.getAge().toString();
                }else
                    return "--";

            }
        }
    }

	@Override
	public void preSave() throws Exception{
		super.preSave();

		if(this.getSubjectData()==null){
			throw new Exception("Unable to identify subject for:" + this.getSubjectId());
		}
	}

	public void applyAnonymizationScript(final ProjectAnonymizer anonymizer) throws TransactionException{
		if(this instanceof XnatImagesessiondata){
			final BaseXnatSubjectassessordata expt = this;
			File tmpDir = new File(System.getProperty("java.io.tmpdir"), "anon_backup");
			new CopyOp(new OperationI<Map<String,File>>() {
				@Override
				public void run(Map<String, File> a) throws Throwable {
					anonymizer.call();
				}
			}, tmpDir,expt.getSessionDir()).run();
		}
	}
}
