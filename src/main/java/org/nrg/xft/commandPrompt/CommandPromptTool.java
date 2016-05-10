/*
 * org.nrg.xft.commandPrompt.CommandPromptTool
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.commandPrompt;

import org.nrg.xdat.XDATTool;
import org.nrg.xdat.security.user.exceptions.FailedLoginException;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Tim
 *
 */
public abstract class CommandPromptTool {
    public Hashtable variables = new Hashtable();
    public Hashtable arguments = new Hashtable();
    public String[] _args = null;
    public ArrayList metaVariables = new ArrayList();
    
    public String directory = null;
    public String runtimeDir = null;
    public XDATTool tool = null;
    
    public URL url = null;
    
    public CommandPromptTool(String[] args) {
        _args = args;
        _convertArguments(args);

        _getPossibleVariables();
        
        if (arguments.containsKey("h") || arguments.containsKey("help"))
        {
            showUsage();
            return;
        }
        

        if(_loadVariables())
        {
            
            System.out.println("\n\n");
            System.out.println("Use option -help for assistance.");
			System.exit(1);
            return;
        }
        
        File f = new File(".");
        runtimeDir = f.getAbsolutePath();
        
        if (variables.containsKey("quiet"))
        {
            XFT.VERBOSE=false;
        }
        
        int _return = 0;
        try {
            run();
        } catch (org.nrg.xdat.security.user.exceptions.FailedLoginException e) {
            System.out.println(e.getMessage());
            _return= 4;
        } catch (ElementNotFoundException e) {
			e.printStackTrace();
			_return= 4;
		} catch (XFTInitException e) {
			e.printStackTrace();
			_return= 5;
		} catch (SQLException e) {
			e.printStackTrace();
			_return= 6;
		} catch (DBPoolException e) {
			e.printStackTrace();
			_return= 7;
		} catch (FieldNotFoundException e) {
			e.printStackTrace();
			_return= 8;
		} catch (Exception e) {
			e.printStackTrace();
			_return= 9;
		}
		System.exit(_return);
	
    }
    
    public String getService(){
        return null;
    }
    
    public String getHostURL()
    {
        String host = (String)variables.get("host");
        if (host==null)
        {
            return null;
        }else{
            if (!host.endsWith("/"))
            {
                return host + "/";
            }else{
                return host;
            }
        }
    }
    
    public String getSiteURL()
    {
        String host = getHostURL();
        if (host ==null)
        {
            String url = (String)variables.get("url");
            if (url == null)
            {
                return null;
            }
                
            if (!url.endsWith("/"))
            {
                url += "/";
            }
            if (variables.get("project")!=null)
            {
                url += variables.get("project") + "/";
            }
            
            return url;
        }else{
            return host;
        }
    }
    
    
    public String getServiceURL(){
        return getSiteURL() + getService();
    }
    
    public void run() throws DBPoolException,SQLException,FailedLoginException,Exception{
        if (getService()==null)
        {
            _process();
        }else{
            
            boolean URLExists = true;
            try {
                url =new URL(getServiceURL());
                url.getContent();
            } catch (Exception e1) {
                URLExists = false;
    		    if (XFT.VERBOSE)System.out.println("Unable to connect to web service.");
            }
            
            if (URLExists)
            {
                _service();
            }else{
                _process();
            }
        }
    }
    
    public void _service() throws DBPoolException,SQLException,FailedLoginException,Exception
    {
        service();
    }
    
    public void service() throws DBPoolException,SQLException,FailedLoginException,Exception{
        _process();
    }
    
    public void _process() throws DBPoolException,SQLException,FailedLoginException,Exception
    {
        getDirectory();
		
	    if (requireLogin())
	    {
			tool = new XDATTool(directory,(String)variables.get("username"),(String)variables.get("password"));
	    }else{
			tool = new XDATTool(directory);
	    }
                
        process();
    }
    
    public String getDirectory()
    {
        directory = null;
        if ((variables.get("xdir")!=null) && (variables.get("project")!=null))
		{
		    String xdir = (String)variables.get("xdir");
		    String project = (String)variables.get("project");
		    if (!xdir.endsWith(File.separator))
		    {
		        xdir += File.separator;
		    }
		    variables.put("instance",xdir + "deployments" + File.separator + project);
		}
        
        String instance = (String)variables.get("instance");
	    if (instance == null)
	    {
			if (! (new File("InstanceSettings.xml")).exists())
			{
			    if (variables.get("xdir") == null)
			    {
					System.out.println("\nERROR:  Missing instance document 'InstanceSettings.xml'");
					System.out.println("Use the -xdir and -project variables to specify the installation and project dir.");
					System.exit(0);
			    }else{
					System.out.println("\nERROR:  Missing instance document 'InstanceSettings.xml'");
					System.out.println("Use the -project variable to specify the project.");
					System.exit(0);
			    }
			}
			File f = new File("");
			directory = f.getAbsolutePath();
	    }else{
	        directory = (String) instance;
	        if (directory.endsWith("InstanceSettings.xml"))
	        {
	            directory = directory.substring(0,directory.length()-21);
	        }
	    }
	    
	    return directory;
    }
    
    public void _getPossibleVariables()
    {
        metaVariables = new ArrayList();
        addPossibleVariable("help","Display usage.",new String[]{"h","help"});
        addPossibleVariable("xdir","File path to the xnat root directory (required with -p): This variable is pre-defined in the executable scripts.");
        addPossibleVariable("project","Name of project to use. (required with -xdir)");
        addPossibleVariable("instance","file path to Instance Settings directory (defaults to run-time directory)");
        addPossibleVariable("username","username.",new String[]{"u","username"},true);
        addPossibleVariable("password","password.",new String[]{"p","password"},true);
        addPossibleVariable("quiet","Disables un-necessary output.");
        addPossibleVariable("host","URL of server for access to web services.",new String[]{"host"});
        addPossibleVariable("url","Default URL of server (used when host is absent).",new String[]{"url"});
        
        definePossibleVariables();
    }
    
    
    private Hashtable _convertArguments(String[] args)
    {
        arguments = new Hashtable();
        for(int i=0; i<args.length; i++){	
            String arg = args[i];
            if (arg.startsWith("-"))
            {
                arg = arg.substring(1);
                if ((i+1) < args.length)
                {
                    String value = args[i+1];
                    if (arguments.get(arg) ==null)
                    {
                        if (value.startsWith("-"))
                        {
                            arguments.put(arg,"true");
                        }else{
                            arguments.put(arg,value);
                            i++;
                        }
                    }else{
                        Object o = arguments.get(arg);
                        ArrayList al = null;
                        if (o instanceof ArrayList){
                           al = ((ArrayList)o);
                        }else{
                            al = new ArrayList();
                            al.add(o);
                        }
                        if (value.startsWith("-"))
                        {
                            al.add("true");
                        }else{
                            al.add(value);
                            i++;
                        }
                        
                        arguments.put(arg,al);
                    }
                }else{
                    if (arguments.get(arg) ==null)
                    {
                        arguments.put(arg,"true");
                    }else{
                        Object o = arguments.get(arg);
                        ArrayList al = null;
                        if (o instanceof ArrayList){
                           al = ((ArrayList)o);
                        }else{
                            al = new ArrayList();
                            al.add(o);
                        }
                       
                        al.add("true");
                        arguments.put(arg,al);
                    }
                }
            }else{
                arguments.put(arg,arg);
            }
        }
        
        return arguments;
    }
    
    private boolean _loadVariables(){
        //COMMON VARIABLES
        boolean missingRequiredVariable=false;
        Iterator iter = this.metaVariables.iterator();
        while (iter.hasNext())
        {
            CommandPromptVariable cpv = (CommandPromptVariable)iter.next();
            Iterator identifiers = cpv.getIdentifiers().iterator();
            boolean found = false;
            while (identifiers.hasNext())
            {
                String id = (String)identifiers.next();
                found =loadVariable(id,cpv);
                
                if (found)
                {
                    break;
                }
            }
            
            if (cpv.is_required() && (!found))
            {
                if (cpv.getName().equalsIgnoreCase("username"))
                {
                    if (requireLogin())
                    {
                        missingRequiredVariable = true;
                        System.out.println("\nMissing Required Variable:");
                        System.out.println(cpv.toString());
                    }
                }else if (cpv.getName().equalsIgnoreCase("password"))
                {
                }else{
                    missingRequiredVariable = true;
                    System.out.println("\nMissing Required Variable:");
                    System.out.println(cpv.toString());
                }
            }
        }
        
        boolean hasPassword = false;
        if (variables.get("username")!=null)
        {
            if (variables.get("password")==null)
            {
                String user = (String)variables.get("username");
                char password[] = null;
                String pass = null;
                File xnatPass = new File("." + File.separator + ".xnatPass");
                if (xnatPass.exists()){
                    pass = getPassword(user,getSiteURL(),xnatPass);
                }
                
                String home = System.getProperty("user.home");
                if (!home.endsWith(File.separator))
                {
                    home += File.separator;
                }
                
                xnatPass = new File(home + ".xnatPass");
                if (xnatPass.exists()){
                    pass = getPassword(user,getSiteURL(),xnatPass);
                    if (pass!=null)
                    {
                        hasPassword = true;
                        variables.put("password",pass);
                    }
                }else{
                    try {
                        xnatPass.createNewFile();
                    } catch (RuntimeException e1) {
                    }catch(IOException e1)
                    {
                        
                    }
                }
                
                try {
                    if (!hasPassword){
                        password = PasswordField.getPassword(System.in,"Enter " +variables.get("username")+"'s Password\n");
                        if (password ==null){
                            
                        }else{
                            variables.put("password",String.valueOf(password));
                            hasPassword=true;
//                            
//                            FileUtils.OutputToFile(user +"@" + getHostURL() + "=" + password,xnatPass.getAbsolutePath());
                        }
                    }
                } catch (IOException e) {
                }
                
                if (!hasPassword && requireLogin())
                {
                    missingRequiredVariable = true;
                    System.out.println("\nMissing Required Variable: password");
                    return true;
                }
            }
        }
        
        return missingRequiredVariable;
    }
    
    private String getPassword(String user, String host, File propFile)
    {
        Properties props= null;
        
        try {
            InputStream f = new FileInputStream(propFile);
            props = new Properties();
            props.load(f);
            f.close();
            
            String s = FileUtils.GetContents(propFile);

            String formattedHost = host;
            Object pass = props.get(user +"@" + formattedHost);
            if (pass==null){
                if (host.startsWith("http://"))
                {
                    formattedHost=formattedHost.substring(7);
                }
                
                formattedHost=formattedHost.replace(':','.');
                
                pass = props.get(user +"@" + formattedHost);
            }
            if (pass==null){
                pass = props.get(user +"@*");
            }
            
            if (pass==null){
                pass = props.get("*@" + formattedHost);
            }
            
            if (pass!=null)
                return pass.toString();
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean loadVariable(String id, CommandPromptVariable cpv)
    {
        if (arguments.containsKey(id)) {
            if (cpv.is_multiples())
            {
                Object o = arguments.get(id);
                if (o instanceof ArrayList)
                {
        			variables.put(cpv.getName(),o);
        			return true;
                }else{
                    ArrayList al = new ArrayList();
                    al.add(o);
        			variables.put(cpv.getName(),al);
        			return true;
                }
            }else{
                Object o = arguments.get(id);
                if (o instanceof ArrayList)
                {
        			variables.put(cpv.getName(),((ArrayList)o).get(0));
        			return true;
                }else{
        			variables.put(cpv.getName(),o);
        			return true;
                }
            }
        }else{
			return false; 
		}
    }
    
    public void showUsage()
    {
        System.out.println(getName());
		System.out.println(getDescription());
		
		System.out.println(" ");

		System.out.println("Options:");
		Iterator iter = this.metaVariables.iterator();
        while (iter.hasNext())
        {
            CommandPromptVariable cpv = (CommandPromptVariable)iter.next();
    		System.out.println(cpv.toString());
        }
		
        System.out.println(getAdditionalUsageInfo());
    }

    /**
     * @return Return name of Function
     */
    public abstract String getName();
    /**
     * @return Return description of the function's task
     */
    public abstract String getDescription();
    /**
     * @return Return additional info to be output with variable usage in the -help option.
     */
    public abstract String getAdditionalUsageInfo();
        
    /**
     * This is where your custom code should be placed.  The user login and XFT instanciation are done for you. 
     * 
     * Use this object's tool and variables to achieve your task.
     */
    public abstract void process();
    

    public void addPossibleVariable(String name, String description)
    {
        metaVariables.add(new CommandPromptVariable(name,description));
    }
    
    public void addPossibleVariable(String name, String description, String identifier)
    {
        metaVariables.add(new CommandPromptVariable(name,description,identifier));
    }
    
    public void addPossibleVariable(String name, String description, ArrayList identifiers)
    {
        metaVariables.add(new CommandPromptVariable(name,description,identifiers));
    }
    
    public void addPossibleVariable(String name, String description, String[] identifiers)
    {
        ArrayList ids = new ArrayList();
        for (int i=0;i<identifiers.length;i++)
        {
            ids.add(identifiers[i]);
        }
        metaVariables.add(new CommandPromptVariable(name,description,ids));
    }
    
    public void addPossibleVariable(String name, String description,boolean required)
    {
        metaVariables.add(new CommandPromptVariable(name,description,required));
    }
    
    public void addPossibleVariable(String name, String description, String identifier,boolean required)
    {
        metaVariables.add(new CommandPromptVariable(name,description,identifier,required));
    }
    
    public void addPossibleVariable(String name, String description, ArrayList identifiers,boolean required)
    {
        metaVariables.add(new CommandPromptVariable(name,description,identifiers,required));
    }
    
    public void addPossibleVariable(String name, String description, String[] identifiers,boolean required)
    {
        ArrayList ids = new ArrayList();
        for (int i=0;i<identifiers.length;i++)
        {
            ids.add(identifiers[i]);
        }
        metaVariables.add(new CommandPromptVariable(name,description,ids,required));
    }
    
    public void addPossibleVariable(String name, String description, String[] identifiers,boolean required,boolean multiples)
    {
        ArrayList ids = new ArrayList();
        for (int i=0;i<identifiers.length;i++)
        {
            ids.add(identifiers[i]);
        }
        metaVariables.add(new CommandPromptVariable(name,description,ids,required,multiples));
    }
    
    
    /**
     * Add any possible variables using the addPossibleVariable() methods.
     */
    public abstract void definePossibleVariables();
    
    public boolean requireLogin()
    {
        return true;
    }
    
    public String getWorkingDirectory()
    {
        return tool.getWorkDirectory();
    }
    
    public String getSettingsDirectory()
    {
        return tool.getSettingsDirectory().toString();
    }
    
    public UserI getUser()
    {
        return tool.getUser();
    }
}
