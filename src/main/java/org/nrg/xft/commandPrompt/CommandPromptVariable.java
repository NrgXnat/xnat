/*
 * org.nrg.xft.commandPrompt.CommandPromptVariable
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.commandPrompt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Tim
 *
 */
public class CommandPromptVariable {
    private ArrayList _identifiers = new ArrayList();
    private String _name = null;
    private String _description = null;
    private boolean _required = false;
    private boolean _multiples = false;
       
    public CommandPromptVariable(String name)
    {
        _name = name;
        _description = name;
        _identifiers.add(name);
    }
    
    public CommandPromptVariable(String name,String description)
    {
        _name = name;
        _description = description;
        _identifiers.add(name);
    }
    
    public CommandPromptVariable(String name,String description, String identifier)
    {
        _name = name;
        _description = description;
        _identifiers.add(identifier);
    }
    
    public CommandPromptVariable(String name,String description, ArrayList identifiers)
    {
        _name = name;
        _description = description;
        _identifiers= identifiers;
    }
    
    public CommandPromptVariable(String name,boolean required)
    {
        _name = name;
        _description = name;
        _identifiers.add(name);
        _required = required;
    }
    
    public CommandPromptVariable(String name,String description,boolean required)
    {
        _name = name;
        _description = description;
        _identifiers.add(name);
        _required = required;
    }
    
    public CommandPromptVariable(String name,String description, String identifier,boolean required)
    {
        _name = name;
        _description = description;
        _identifiers.add(identifier);
        _required = required;
    }
    
    public CommandPromptVariable(String name,String description, ArrayList identifiers,boolean required)
    {
        _name = name;
        _description = description;
        _identifiers= identifiers;
        _required = required;
    }
    
    public CommandPromptVariable(String name,String description, ArrayList identifiers,boolean required,boolean multiples)
    {
        _name = name;
        _description = description;
        _identifiers= identifiers;
        _required = required;
        _multiples=multiples;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return _description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this._description = description;
    }
    /**
     * @return Returns the identifiers.
     */
    public ArrayList getIdentifiers() {
        return _identifiers;
    }
    /**
     * @param identifiers The identifiers to set.
     */
    public void setIdentifiers(ArrayList identifiers) {
        this._identifiers = identifiers;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return _name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this._name = name;
    }
    /**
     * @return Returns the _required.
     */
    public boolean is_required() {
        return _required;
    }
    /**
     * @param _required The _required to set.
     */
    public void set_required(boolean _required) {
        this._required = _required;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        int counter = 0;
        Iterator iter = _identifiers.iterator();
        while (iter.hasNext())
        {
            String id = (String)iter.next();
            if (counter++==0){
                sb.append("-").append(id);
            }else{
                sb.append(" OR -").append(id);
            }
        }
        
        sb.append(": ");
        sb.append(this.getDescription());
        if(this._required)
        {
            sb.append(" (REQUIRED)");
        }else{
            sb.append(" (optional)");
        }
        
        return sb.toString();
    }
    /**
     * @return Returns the _multiples.
     */
    public boolean is_multiples() {
        return _multiples;
    }
    /**
     * @param _multiples The _multiples to set.
     */
    public void set_multiples(boolean _multiples) {
        this._multiples = _multiples;
    }
}
