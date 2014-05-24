//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 18, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
/**
 * @author Tim
 *
 */
public class DefaultReport extends SecureReport {
	static Logger logger = Logger.getLogger(DefaultReport.class);
    public void finalProcessing(RunData data,Context context)
    {
        try {
            context.put("data_item",item.toHTML());
        } catch (Exception e) {
            logger.error("",e);
        }
    }
}

