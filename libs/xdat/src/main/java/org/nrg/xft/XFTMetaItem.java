/*
 * core: org.nrg.xft.XFTMetaItem
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft;

import java.util.Date;

import org.apache.log4j.Logger;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.ItemSearch;


/**
 * @author Tim
 *
 */
public class XFTMetaItem {
	static Logger logger = Logger.getLogger(XFTMetaItem.class);
    private XMLType element = null;
    private XFTItem smallItem = null;
    private XFTItem fullItem = null;
    public XFTMetaItem(XMLType eType,Object meta_data_id)
    {
        try {
            element =eType;
            XFTItem item = ItemSearch.GetItem(eType.getFullForeignType() + "_meta_data.meta_data_id",meta_data_id,null,false);
            fullItem = item;
            smallItem = item;
        } catch (Exception e) {
            logger.error("",e);
        }
    }
    
    public XFTMetaItem(XFTItem item)
    {
        try {
            smallItem = item;
            element = item.getGenericSchemaElement().getType();
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
    }
    
    public Date getActivationDate()
    {
        try {
            return (Date) this.getFullItem().getProperty("activation_date");
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return null;
    }
    
    public Date getInsertDate()
    {
        try {
            return (Date) this.getFullItem().getProperty("insert_date");
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return null;
    }
    
    public String getActivationUser()
    {
        try {
            return (String) this.getFullItem().getProperty("activation_user_login");
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return null;
    }
    
    public String getInsertUser()
    {
        try {
            return (String) this.getFullItem().getProperty("insert_user_login");
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        return null;
    }
    
    public String getStatus()
    {
        try {
            return this.getSmallItem().getStringProperty("status");
        } catch (Exception e) {
            logger.error("",e);
            return ViewManager.ACTIVE;
        }
    }
    
    public boolean isModified()
    {
        try {
            return this.getSmallItem().getBooleanProperty("modified",false);
        } catch (XFTInitException e) {
            logger.error("",e);
            return false;
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return false;
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            return false;
        }
    }
    
    public boolean isShareable()
    {
        try {
            return this.getSmallItem().getBooleanProperty("shareable",false);
        } catch (XFTInitException e) {
            logger.error("",e);
            return false;
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return false;
        } catch (FieldNotFoundException e) {
            logger.error("",e);
            return false;
        }
    }
    
    public Integer getMetaDataId()
    {
        return this.getSmallItem().getIntegerProperty("meta_data_id");
    }
    
    /**
     * @return Returns the fullItem.
     */
    public XFTItem getFullItem() {
        if (fullItem == null)
        {
            Object meta_data_id = null;
            
            try {
                if (smallItem != null)
                {
                    meta_data_id = smallItem.getProperty("meta_data_id");
                }
                
                if (meta_data_id != null)
                {
                    fullItem = ItemSearch.GetItem(element.getFullForeignType() + "_meta_data.meta_data_id",meta_data_id,null,false);
                }
            } catch (Exception e1) {
                logger.error("",e1);
            }
            
            if (fullItem == null)
            {
                try {
                    if (smallItem == null)
                        fullItem = XFTItem.NewItem(GenericWrapperElement.GetElement(element),null);
                    else
                        fullItem = smallItem;
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
            }
        }
        return fullItem;
    }
    /**
     * @return Returns the smallItem.
     */
    public XFTItem getSmallItem() {
        return smallItem;
    }
}
