//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Mar 10, 2005
 * 
 */
package org.nrg.xdat.base;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.om.XdatUserI;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xft.ItemI;
import org.nrg.xft.ItemWrapper;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.generators.JavaFileGenerator;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.utils.VelocityUtils;
import org.w3c.dom.Document;

/**
 * @author Tim
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseElement extends ItemWrapper implements ItemI {
	static org.apache.log4j.Logger logger = Logger.getLogger(BaseElement.class);
	private SchemaElement schemaElement=null;
	private Hashtable displayFields = null;
    public BaseElement(ItemI i)
	{
        if (i instanceof XFTItem)
        {
            setItem(i);
        }else{
            if (i instanceof ItemWrapper)
            {
                setItem(((ItemWrapper)i).getItem());
            }
        }
	}

    public BaseElement()
    {

    }

	public BaseElement(UserI user)
	{
		try{setItem(XFTItem.NewItem(getSchemaElementName(),user));}catch(Exception e){logger.error("",e);}
	}

	public BaseElement(Map properties, UserI user)
	{
		try{setItem(XFTItem.NewItem(getSchemaElementName(),properties,false,user));}catch(Exception e){logger.error("",e);}
	}

	public SchemaElement getSchemaElement(){
		if(schemaElement==null)
			try{schemaElement=SchemaElement.GetElement(getSchemaElementName());}catch(Exception e){logger.error("",e);}
		return schemaElement;
	}

	public void setBooleanProperty(String xmlPath,boolean value) throws Exception
	{
	    if(value)
		{
			getItem().setProperty(xmlPath,"1");
		}else{
			getItem().setProperty(xmlPath,"0");
		}
	}

	public void setBooleanProperty(String xmlPath,Boolean value) throws Exception
	{
	    if(value.booleanValue())
		{
			getItem().setProperty(xmlPath,"1");
		}else{
			getItem().setProperty(xmlPath,"0");
		}
	}

	public void setBooleanProperty(String xmlPath,String value) throws Exception
	{
	    boolean b = false;

		if(value.equalsIgnoreCase("1") || value.equalsIgnoreCase("true"))
		{
		    b = true;
		}

		setBooleanProperty(xmlPath,b);
	}

	public void setBooleanProperty(String xmlPath,Object value) throws Exception
	{
	    getItem().setProperty(xmlPath,value);
	}

	public void setProperty(String xmlPath,Object value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
	    getItem().setProperty(xmlPath,value);
	}

	public Object getDisplayField(String fieldID, UserI user)
	{
	    if (displayFields==null)
	    {
	        loadDisplayFields(user);
	    }

	    if (displayFields !=null)
	    {
	        return displayFields.get(fieldID.toLowerCase());
	    }else{
	        return null;
	    }
	}

	public Object getDisplayField(String fieldID)
	{
	    return getDisplayFields().get(fieldID.toLowerCase());
	}

	public Hashtable getDisplayFields()
	{
	    if (displayFields == null)
	    {
	        loadDisplayFields(this.getUser());
	    }
	    return displayFields;
	}

	public void loadDisplayFields(UserI user)
	{
	    if (this.getSchemaElement().getDisplay()!= null)
	    {
	        try {
                StringBuffer sb = new StringBuffer();
                sb.append("SELECT * FROM ").append(getSchemaElement().getDisplayTable());

                int counter=0;
                Iterator iter = getSchemaElement().getAllPrimaryKeys().iterator();
                while(iter.hasNext())
                {
                    SchemaField sf = (SchemaField)iter.next();
                    Object v = getProperty(sf.getXMLPathString(getSchemaElement().getFullXMLName()));

                    if (v!=null)
                    {
                        DisplayField df = getSchemaElement().getDisplayFieldForXMLPath(sf.getXMLPathString(getSchemaElement().getFullXMLName()));
                        if (df==null)
                        {
                            throw new Exception("No display field defined for primary key " + sf.getXMLPathString(getSchemaElement().getFullXMLName()));
                        }else{
                            if (counter++==0)
                            {
                                sb.append(" WHERE ").append(df.getId()).append("=").append(DBAction.ValueParser(v,sf.getWrapped(),false));
                            }else{
                                sb.append(" AND ").append(df.getId()).append("=").append(DBAction.ValueParser(v,sf.getWrapped(),false));
                            }
                        }
                    }else{
                        throw new Exception("Item not properly initialized.");
                    }
                }
                String login="";
        	    if(user != null)
        	    {
        	        login = user.getUsername();
        	    }
               XFTTableI table = TableSearch.Execute(sb.toString(),getSchemaElement().getDbName(),login);
               if(table.size()>0)
               {
                   table.resetRowCursor();
                   displayFields=table.nextRowHash();
               }else{
                   displayFields= new Hashtable();
               }
            } catch (Exception e) {
                logger.error("",e);
            }
	    }else{
            displayFields= new Hashtable();
	    }
	}


	public static long getDateDiff(Calendar c1, Calendar c2)
    {
        long time1 = c1.getTime().getTime();
        long time2 = c2.getTime().getTime();

        if (time1 > time2)
            return -1;

        return ((time2 - time1) / 86400000) + 1;
    }

	public String toString()
	{
	    return this.getItem().toString();
	}

	public Document toJoinedXML() throws Exception
	{
	    return getItem().toJoinedXML();
	}

	public static Class GetGeneratedClass(String elementName)
	{
	    try {
	        GenericWrapperElement gwe = null;
	        try {
                gwe = GenericWrapperElement.GetElement(elementName);
            } catch (XFTInitException e1) {
                logger.error("",e1);
            } catch (ElementNotFoundException e1) {
                logger.error("",e1);
            }


            if (gwe ==null)
            {
                return null;
            }else{
                JavaFileGenerator gen = new JavaFileGenerator();
                return Class.forName("org.nrg.xdat.om." + gen.getSQLClassName(gwe));
            }
        } catch (ClassNotFoundException e) {
            logger.debug(e);
            return null;
        }
	}

	public static ItemI GetGeneratedItem(ItemI item)
	{
	    if (item instanceof XFTItem)
	    {
	        Class c = GetGeneratedClass(item.getXSIType());
	        if (c == null)
	        {
	            return item;
	        }
	        try {
                ItemI o = (ItemI) c.newInstance();

    	        Object[] intArgs = new Object[] {item.getItem()};
    			Class[] intArgsClass = new Class[] {ItemI.class};

    			try {
                    Method m = c.getMethod("setItem",intArgsClass);
                    try {
                        try {
                            m.invoke(o,intArgs);
                            if (o==null)
                            {
                                return item;
                            }else{
                                return o;
                            }

                        } catch (RuntimeException e3) {
                            logger.error("",e3);
                        }
                    } catch (IllegalArgumentException e2) {
                        logger.error("",e2);
                    } catch (InvocationTargetException e2) {
                        logger.error("",e2);
                    }
                } catch (SecurityException e1) {
                    logger.error("",e1);
                } catch (NoSuchMethodException e1) {
                    logger.error("",e1);
                }

            } catch (InstantiationException e) {
                logger.info("No Custom Class found for '" + item.getXSIType()+ "'");
            } catch (IllegalAccessException e) {
                logger.error("",e);
            }
	    }
        return item;
	}

	public static ArrayList WrapItems(ArrayList items)
	{
	    final ArrayList al = new ArrayList();
        final Iterator tempIter = items.iterator();

        while (tempIter.hasNext())
        {
            final ItemI temp = (ItemI)tempIter.next();
            al.add(BaseElement.GetGeneratedItem(temp));
        }
        return al;
	}


    public void log(String text)
    {
        logger.debug(text);
    }

    public String output(String templateName)
    {
        try {
	        boolean velocityInit = false;

	        try {
                Velocity.templateExists(templateName);
                velocityInit=true;
            } catch (Exception e1) {
            }

            if (velocityInit)
            {
                boolean exists= Velocity.templateExists("/screens/" + templateName);
                if (exists)
                {
                    VelocityContext context = new VelocityContext();
                    context.put("item",this);
                    StringWriter sw = new StringWriter();
                    Template template =Velocity.getTemplate("/screens/" + templateName);
                    template.merge(context,sw);
                    return sw.toString();
                }else{
                    logger.info("No Velocity TEXT vm found for " + getItem().getGenericSchemaElement().getFullXMLName());
                    return getItem().toXML_String();
                }
            }else
            {
                VelocityUtils.init();
                boolean exists= Velocity.templateExists(getItem().getGenericSchemaElement().getFormattedName() +"_text.vm");
                String path = XFTManager.GetInstance().getSourceDir() + "src/templates/text/"+ templateName;
                File f = new File(path);
                if (f.exists())
                {
                    VelocityContext context = new VelocityContext();
                    context.put("item",this);
                    StringWriter sw = new StringWriter();

                    Velocity.evaluate(context,sw,"text",FileUtils.GetContents(f));

                    return sw.toString();
                }else {
                    path = XFTManager.GetInstance().getSourceDir() + "src/xnat-templates/text/"+ templateName;
                    f = new File(path);
                    if (f.exists())
                    {
                        VelocityContext context = new VelocityContext();
                        context.put("item",this);
                        StringWriter sw = new StringWriter();

                        Velocity.evaluate(context,sw,"text",FileUtils.GetContents(f));

                        return sw.toString();
                    }else{
                        path = XFTManager.GetInstance().getSourceDir() + "src/xdat-templates/text/"+ templateName;
                        f = new File(path);
                        if (f.exists())
                        {
                            VelocityContext context = new VelocityContext();
                            context.put("item",this);
                            StringWriter sw = new StringWriter();

                            Velocity.evaluate(context,sw,"text",FileUtils.GetContents(f));

                            return sw.toString();
                        }else{
                            path = XFTManager.GetInstance().getSourceDir() + "src/base-templates/text/"+ templateName;
                            f = new File(path);
                            if (f.exists())
                            {
                                VelocityContext context = new VelocityContext();
                                context.put("item",this);
                                StringWriter sw = new StringWriter();

                                Velocity.evaluate(context,sw,"text",FileUtils.GetContents(f));

                                return sw.toString();
                            }else{
            	                logger.info("No Velocity TEXT vm found for " + getItem().getGenericSchemaElement().getFullXMLName() +" at " + path);
            	                return getItem().toXML_String();
            	            }
        	            }
    	            }
                }
            }
        } catch (Exception e) {
            logger.error("",e);
            return getItem().toXML_String();
        }
    }

    public String output()
	{
        try {
            return output(getItem().getGenericSchemaElement().getFormattedName() +"_text.vm");
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return getItem().toXML_String();
        }
	}


    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        getItem().readExternal(in);
    }
    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        getItem().writeExternal(out);
    }

    public ArrayList<ResourceFile> getFileResources(String rootPath){
        return getFileResources(rootPath,false);
    }

    public abstract ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop);


    public Date getInsertDate(){
        return this.getItem().getInsertDate();
    }

    public XdatUserI getInsertUser(){
        return this.getItem().getInsertUser();
    }
}

