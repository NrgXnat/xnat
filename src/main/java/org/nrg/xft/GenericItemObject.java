// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityValues;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidItemException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.StringUtils;

public abstract class GenericItemObject implements ItemI {
	private static final String TRUE = "true";
	private static final String _1 = "1";
	private static final String NULL = "NULL";
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTItem.class);
	final protected Hashtable<String,Object> props = new Hashtable();

	final protected Hashtable item_counts=new Hashtable();

	/**
	 * properties hashtable
	 * @return
	 */
	public Hashtable getProps() {
		return props;
	}
	
	/**
	 * returns number of populated properties
	 * @return
	 */
	public int getPropertyCount()
	{
		return props.size();
	}
	
	/**
	 * @return
	 */
	public boolean hasProperties()
	{
		if (props.size() > 0)
		{
			return true;
		}else{
			return false;
		}
	}
    
    public void clear()
    {
       try {
           for (XFTItem child:getChildItems())
           {
               child.clear();
           }
           
	    } catch (XFTInitException e) {
	        logger.error("",e);
	    } catch (ElementNotFoundException e) {
	        logger.error("",e);
	    } catch (FieldNotFoundException e) {
	        logger.error("",e);
	    }
	    item_counts.clear();
	    props.clear();
    }
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getChildItems()
	 */
	public ArrayList<XFTItem> getChildItems()throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		ArrayList<XFTItem> al = new ArrayList<XFTItem>();
		for (Object o:this.props.values())
		{
			if (o instanceof XFTItem)
			{
				al.add((XFTItem)o);
			}
		}
				
		al.trimToSize();
		return al;
	}
	
	/**
	 * Get field from hashtable of properties (null if not found)
	 * @param key
	 * @return
	 */
	protected Object getField(final String key) 
	{
		return props.get(key);
		
	}

    
    /**
     * Get field from hashtable of properties (null if not found)
     * @param key
     * @return
     */
	protected Object getField(final String key,final boolean allowMultipleValues) 
    {
        if (allowMultipleValues)
        {
        	final ArrayList<Object> al = new ArrayList<Object>();
            for (String entry:props.keySet())
            {
                if (entry.startsWith(key)){
                    al.add(props.get(entry));
                }
            }
            
            if (al.size()>1)
                return al;
            else
                return props.get(key);
        }else{
            return getField(key);
        }
    }
	/**
	 * Set field in hashtable of properties (Without any validation)
	 * @param key
	 * @param value
	 */
	protected void setField(final String key,final Object value)
	{
	    if (value ==null)
	    {
	        props.remove(key);
	    }else{
	        if (value instanceof XFTItem)
	        {
	            props.put(StringUtils.intern(key),value);
	        }else if (value.toString().equals(NULL))
	        {
	            props.put(StringUtils.intern(key),NULL);
	        }else{
	    		props.put(StringUtils.intern(key),value);
	        }
	    }
	    sv=null;
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, java.lang.String)
	 */
	public boolean getBooleanProperty(final String name,final String default_value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
	    if (default_value.toString().equalsIgnoreCase(_1) || default_value.toString().equalsIgnoreCase(TRUE))
	    {
	        return getBooleanProperty(name,true);
	    }else{
	        return getBooleanProperty(name,false);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String, boolean)
	 */
	public boolean getBooleanProperty(final String name,final boolean default_value) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		final Object o = getProperty(name);
		if (o==null)
		{
			return default_value;
		}else{
			if (o.toString().equalsIgnoreCase(_1) || o.toString().equalsIgnoreCase(TRUE))
			{
				return true;
			}else{
				return false;
			}				
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getBooleanProperty(java.lang.String)
	 */
	public Boolean getBooleanProperty(final String name) throws XFTInitException,ElementNotFoundException,FieldNotFoundException
	{
		final Object o = getProperty(name);
		if (o==null)
		{
			return null;
		}else{
			if (o.toString().equalsIgnoreCase(_1) || o.toString().equalsIgnoreCase(TRUE))
			{
				return Boolean.TRUE;
			}else{
				return Boolean.FALSE;
			}				
		}
	}
	

	
	/**
	 * @param xmlPath
	 * @return
	 */
	public Integer getIntegerProperty(final String xmlPath)
	{
	    try{
			return (Integer)getProperty(xmlPath);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	/**
	 * @param xmlPath
	 * @return
	 */
	public Float getFloatProperty(final String xmlPath)
	{
	    try{
			return (Float)getProperty(xmlPath);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	/**
	 * @param xmlPath
	 * @return
	 */
	public Double getDoubleProperty(final String xmlPath)
	{
	    try{
			return (Double)getProperty(xmlPath);
		}catch(Exception e)
		{
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.nrg.xft.ItemI#getStringProperty(java.lang.String)
	 */
	public String getStringProperty(final String name) throws XFTInitException,ElementNotFoundException,FieldNotFoundException{
		final Object o = getProperty(name);
	    if (o!= null)
	    {
	        if (o.getClass().getName().equalsIgnoreCase("[B"))
			{
				byte[] b = (byte[]) o;
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				try {
					baos.write(b);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return baos.toString();
			}else{
				return o.toString();
			}
	    }else{
	        return null;
	    }
	}
	
	private Object validate(Object o){
		if(o instanceof String){
			String s = (String)o;
			if(s.indexOf("<script")>-1 || s.indexOf("<SCRIPT")>-1){
				s=s.replaceAll(">", "&gt;");
				s=s.replaceAll("<", "&lt;");
			}
			return s;
		}else{
			return o;
		}
	}
	
	/**
	 * If throwException=true, then encountered errors will be thrown when they are encountered and additional properties
	 * will not be loaded.  If it is false, then the error will not be thrown until after all properties have been loaded.
	 * @param hash
	 * @param throwException
	 * @throws ElementNotFoundException
	 * @throws FieldNotFoundException
	 * @throws InvalidValueException
     * @return ArrayList of Exceptions (if throwException = false)
	 */
	public ArrayList<Throwable> setProperties(final Map<String,? extends Object> hash,final boolean throwException) throws ElementNotFoundException,FieldNotFoundException,InvalidValueException
	{
		sv=null;
		final ArrayList<Throwable> exceptions = new ArrayList<Throwable>();
		
		final Hashtable dateErrors = new Hashtable();

        InvalidValueException error = null;
		for (String key:hash.keySet())
		{
			if (key.toLowerCase().startsWith(this.getXSIType().toLowerCase()))
			{
				try {
                    this.setProperty(key,validate(hash.get(key)));
                } catch (XFTInitException e) {
                } catch (ElementNotFoundException e) {
                    if(throwException)
                    {
                        throw e;
                    }else{
                        exceptions.add(e);
                    }
                } catch (FieldNotFoundException e) {
                    if(throwException)
                    {
                        throw new FieldNotFoundException(key);
                    }else{
                        exceptions.add(e);
                    }
                } catch (InvalidValueException e) {
                    dateErrors.put(key,e);
                }
			}else if (StringUtils.OccursBefore(key,":","/") || StringUtils.OccursBefore(key,":","/"))
			{
				String temp=key.replace('.', '/');
				String root=key.substring(0,key.indexOf("/"));
				try {
					GenericWrapperElement local=GenericWrapperElement.GetElement(this.getXSIType());
					
					if(local.instanceOf(root)){
	                    this.setProperty(this.getXSIType()+key.substring(key.indexOf("/")),validate(hash.get(key)));
					}
                } catch (XFTInitException e) {
                } catch (ElementNotFoundException e) {
                } catch (FieldNotFoundException e) {
                    if(throwException)
                    {
                        throw new FieldNotFoundException(key.substring(4));
			}else{
                        exceptions.add(e);
                    }
                } catch (InvalidValueException e) {
                    dateErrors.put(key,e);
                }
			}else{
				if (key.toUpperCase().startsWith("ELEMENT_"))
				{
					final String temp = key.substring(8);
					if (temp.toLowerCase().startsWith(this.getXSIType().toLowerCase()))
					{
						try {
							this.setProperty(temp,validate(hash.get(key)));
						} catch (XFTInitException e) {
		                } catch (ElementNotFoundException e) {
		                    if(throwException)
		                    {
		                        throw e;
		                    }else{
                                exceptions.add(e);
                            }
		                } catch (FieldNotFoundException e) {
		                    if(throwException)
		                    {
		                        throw e;
		                    }else{
                                exceptions.add(e);
                            }
		                } catch (InvalidValueException e) {
		                    dateErrors.put(key,e);
		                }
					}
				}
			}
		}
		
		if (dateErrors.size()>0)
		{
		    if ((dateErrors.size() % 3) != 0)
		    {
		        error = (InvalidValueException)dateErrors.values().toArray()[0];
		    }
		}
		
		if (error != null)
		{
		    throw error;
		}
        
        return exceptions;
	}
    
    public Date getDateProperty(final String name)throws XFTInitException,ElementNotFoundException,FieldNotFoundException,ParseException{
        final Object o = getProperty(name);
        if (o==null)
        {
            return null;
        }else{
            if (o instanceof java.util.Date){
                return (java.util.Date)o;
            }else{
                return org.nrg.xft.utils.DateUtils.parseDate(o.toString());
            }
             
        }
    }
    
	
	public abstract Object getProperty(String id) throws XFTInitException,ElementNotFoundException,FieldNotFoundException;
	public abstract void setProperty(String xmlPath, Object value) throws XFTInitException, ElementNotFoundException,FieldNotFoundException,InvalidValueException;
	public abstract String getXSIType();
//	/**
//	 * properties hashtable
//	 * @param hashtable
//	 */
//	public void setProps(Hashtable hashtable) {
//		props = hashtable;
//	}
	

    
    SecurityValues sv=null;
    public SecurityValues getSecurityValues(){
    	if(sv==null){
    		try {
				sv=ElementSecurity.GetSecurityValues(this);
			} catch (InvalidItemException e) {
				logger.error(e);
			} catch (Exception e) {
				logger.error(e);
			}
    	}
    	
    	return sv;
    }
}
