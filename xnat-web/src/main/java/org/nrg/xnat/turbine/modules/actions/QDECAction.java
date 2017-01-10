/*
 * web: org.nrg.xnat.turbine.modules.actions.QDECAction
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.turbine.modules.actions.ListingAction;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

public class QDECAction extends ListingAction{

	public String getDestinationScreenName(RunData data) {
	        return "QDECScreen.vm";
    }
	
	//This method will invoke a QDEC Analysis
	//The user launches a search and the resultset of that search
	//is sent to the QDEC Analysis with the QDEC parameters selected by the user
	public void doQdec(RunData data, Context context) throws Exception{

		DisplaySearch search = TurbineUtils.getSearch(data);
        search.setPagingOn(false);
        //Load search results into a table
        search.execute(new org.nrg.xdat.presentation.HTMLNoTagsAllFieldsPresenter(),TurbineUtils.getUser(data).getLogin());
        XFTTableI table = search.getPresentedTable();
        
        String analysis_name = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("analysis_name",data));
        analysis_name = getCamelCaps(analysis_name);

        Date now = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        DateFormat timeFormat = new SimpleDateFormat("HHmmss");
        String dateStamp =  dateFormat.format(now) ;
        String timeStamp = timeFormat.format(now);

        String id = dateStamp +"_" + timeStamp + "_"+ analysis_name;

        String qdecTablePath = createQdecTableDatFile(data,table,id);

        ArrayList<String> discrete = getDiscreteVariables(data);
        for (int i = 0; i < discrete.size(); i++) 
        	createLevelsFile(data,context,discrete.get(i),table,id);

        Parameters analysisParameters = createParameters(data,context, table);
       
        ParameterData param = analysisParameters.addNewParameter();
        param.setName("data-table");
        param.addNewValues().setUnique(qdecTablePath);

        
        param = analysisParameters.addNewParameter();
        param.setName("analysis-name");
        param.addNewValues().setUnique(analysis_name);


    	String userFolderPath = getAnalysisFolder(data, id);
        param = analysisParameters.addNewParameter();
        param.setName("working-dir");
        param.addNewValues().setUnique(userFolderPath);

        param = analysisParameters.addNewParameter();
        param.setName("output");
        param.addNewValues().setUnique(userFolderPath + File.separator +  analysis_name +".qdec");

        param = analysisParameters.addNewParameter();
        param.setName("output-relative-path");
        param.addNewValues().setUnique(id + File.separator +  analysis_name +".qdec");
        
        //param = analysisParameters.addNewParameter();
        //param.setName("subjects-dir");
        //param.addNewValues().setUnique(userFolderPath +File.separator+"SUBJECTS_DIR");

        //Launch the job
		String pipelineName = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("hdn_pipelinename",data));
        XnatPipelineLauncher xnatPipelineLauncher = new XnatPipelineLauncher(data,context);
        xnatPipelineLauncher.setAdmin_email(XDAT.getSiteConfigPreferences().getAdminEmail());
        xnatPipelineLauncher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
        xnatPipelineLauncher.setPipelineName(pipelineName);
        xnatPipelineLauncher.setId(id);
        xnatPipelineLauncher.setNeedsBuildDir(false);
        xnatPipelineLauncher.setSupressNotification(true);
        xnatPipelineLauncher.setDataType("xnat:qdecAnalysis");
        
        try {
            String paramFilePath = saveParameters(userFolderPath , analysisParameters);
            xnatPipelineLauncher.setParameterFile(paramFilePath);
            xnatPipelineLauncher.setParameter("parameterFile",paramFilePath);
            xnatPipelineLauncher.launch(null);
            data.setMessage( "<p><b>Your QDEC analysis was successfully launched.  Status email will be sent to you upon its completion.</b></p>");
            data.setScreenTemplate("ClosePage.vm");
        }catch(Exception e) {
            data.setMessage("<p><b>The QDEC Analysis process could not be launched.  Please contact <A HREF=\"mailto:"+XDAT.getNotificationsPreferences().getHelpContactInfo()+"?subject=Error: Performing QDEC Group Analysis" + "\">Report Error to" +TurbineUtils.GetSystemName() + "  Techdesk</A></b></p>");
            data.setScreenTemplate("Error.vm");
        }   
	}
	
	private ArrayList<String> removeSpaces(ArrayList<String> inList) {
		ArrayList<String> rtn = new ArrayList<>();
		for (String aStr: inList) {
			rtn.add(StringUtils.deleteWhitespace(aStr));
		}
		return rtn;
	}

	private Parameters createParameters(RunData data, Context context, XFTTableI table){

        Parameters parameters = Parameters.Factory.newInstance();

        ArrayList<String> discrete = removeSpaces(getDiscreteVariables(data));
        if (discrete.size()>0)  {
        	ParameterData param = parameters.addNewParameter();
        	param.setName("discrete-factor");
            Values discreteValues = param.addNewValues();
            for (int i = 0; i < discrete.size(); i++) {
            	discreteValues.addList(discrete.get(i));
            }
        }
        ArrayList<String> continous = removeSpaces(getContinousVariables(data));
        if (continous.size()>0) {
        	ParameterData param = parameters.addNewParameter();
        	param.setName("continuous-factor");
            Values continousValues = param.addNewValues();
            for (int i = 0; i < continous.size(); i++) {
            	continousValues.addList(continous.get(i));
            }
        }
        
        String morph_measure = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("morph_measure",data));
        ParameterData param = parameters.addNewParameter();
        param.setName("measurement");
        param.addNewValues().setUnique(morph_measure);
        
        String morph_hemisphere = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("morph_hemisphere",data));
        param = parameters.addNewParameter();
        param.setName("hemisphere");
        param.addNewValues().setUnique(morph_hemisphere);
        
        String morph_fwhm = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("morph_fwhm",data));
        param = parameters.addNewParameter();
        param.setName("smoothness");
        param.addNewValues().setUnique(morph_fwhm);
        
        
        //Insert the session ids for creating the SUBJECTS_DIR softlink
        ArrayList<String> session_ids = getSessionIds(data,table);
        param = parameters.addNewParameter();
        param.setName("session-ids");
        Values values = param.addNewValues();
        for (int i = 0; i < session_ids.size(); i++) {
        	values.addList(session_ids.get(i));
        }
        
        UserI user = TurbineUtils.getUser(data);
        param = parameters.addNewParameter();
        param.setName("user");
        param.addNewValues().setUnique(user.getLogin());

        
        
        param = parameters.addNewParameter();
        param.setName("useremail");
        param.addNewValues().setUnique(user.getEmail());

        param = parameters.addNewParameter();
        param.setName("userfullname");
        param.addNewValues().setUnique(XnatPipelineLauncher.getUserName(user));

        param = parameters.addNewParameter();
        param.setName("adminemail");
        param.addNewValues().setUnique(XDAT.getSiteConfigPreferences().getAdminEmail());

        param = parameters.addNewParameter();
        param.setName("xnatserver");
        param.addNewValues().setUnique(TurbineUtils.GetSystemName());

        
        param = parameters.addNewParameter();
        param.setName("mailhost");
        param.addNewValues().setUnique( XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
        
        return parameters;
        
	}
	
	  private String saveParameters(String rootpath, Parameters parameters) throws Exception{
	        File dir = new File(rootpath);
	        if (!dir.exists()) dir.mkdirs();
	        File paramFile = new File(rootpath + File.separator + "Parameters.xml");
	        ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
	        paramDoc.addNewParameters().set(parameters);
	        paramDoc.save(paramFile,new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
	        return paramFile.getAbsolutePath();
	    }

	  private String getCamelCaps(String str) {
		String rtn = "";
		if (str == null) return null;
		str = str.trim();
		String[] parts= str.split(" ");
		if (parts != null ) {
			if (parts.length > 1) {
				for (int i = 0; i < parts.length; i++) {
					rtn += StringUtils.capitalize(parts[i]);
				}
			}else if (parts.length == 1) {
				rtn = parts[0];
			}
		}
		return rtn;
	}
	

	
	private String createQdecTableDatFile(RunData data,XFTTableI table, String folderName) throws Exception {
    	String userFolderPath = getAnalysisFolder(data, folderName);
    	File userFolder = new File(userFolderPath);
		if (!userFolder.exists()) userFolder.mkdirs();
		File qdecTableFile = new File(userFolder.getAbsolutePath() +File.separator + "qdec.table.dat");
		PrintWriter output = null;
        //Create file as:
        ArrayList<String> fileCols = getQdecFileColumns(data);
        ArrayList<String> noSpaceFileCols = removeSpaces(fileCols);
		try {
				qdecTableFile.createNewFile();
				int numOfCols = fileCols.size();
				output
				   = new PrintWriter(new BufferedWriter(new FileWriter(qdecTableFile)));
				for (int i =0; i < numOfCols; i++) {
					output.print(noSpaceFileCols.get(i) + "\t");
				}
				output.println();
				table.resetRowCursor();
				while (table.hasMoreRows()) {
					table.nextRow();
					for (int i =0; i < numOfCols; i++) {
						output.print(table.getCellValue(fileCols.get(i)) + "\t");
					}
					output.println();
				}
        } finally {
	          if (output != null) output.close();
	    }
        return qdecTableFile.getAbsolutePath();
	}
	
	
	private ArrayList<String> getContinousVariables(RunData data) {
		ArrayList<String> rtn = new ArrayList<String>();
        String continuousVar1 = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("continuousVar1",data));
        String continuousVar2 = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("continuousVar2",data));
        if (continuousVar1 != null && !continuousVar1.equalsIgnoreCase("BAD")) {
        	rtn.add(continuousVar1);
        }
        if (continuousVar2 != null && !continuousVar2.equalsIgnoreCase("BAD")) {
        	rtn.add(continuousVar2);
        }
		return rtn;
	}

	private ArrayList<String> getDiscreteVariables(RunData data) {
		ArrayList<String> rtn = new ArrayList<String>();
        String discreteVar1 = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("discreteVar1",data));
        String discreteVar2 = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("discreteVar2",data));
        if (discreteVar1 != null && !discreteVar1.equalsIgnoreCase("BAD")) {
        	rtn.add(discreteVar1);
        }
        if (discreteVar2 != null && !discreteVar2.equalsIgnoreCase("BAD")) {
        	rtn.add(discreteVar2);
        }
        return rtn;
	}
	
	
	
	

	private String getSessionIdColumnName(RunData data) {
		String hdn_id = ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("hdn_id_col",data));
		return hdn_id;
	}
	
	private ArrayList<String> getSessionIds(RunData data, XFTTableI table) {
		ArrayList<String> rtn = new ArrayList<String>();
		String id_col_name = getSessionIdColumnName(data);
		table.resetRowCursor();
		while (table.hasMoreRows()) {
			table.nextRow();
			Object rowColumnValue = table.getCellValue(id_col_name);
			if (rowColumnValue !=  null) {
				rtn.add(rowColumnValue.toString());
			}
		}
		return rtn;
	}
	
	private ArrayList<String> getQdecFileColumns(RunData data) {
		ArrayList<String> rtn = new ArrayList<String>();
        rtn.add(getSessionIdColumnName(data));
        ArrayList<String> discrete = getDiscreteVariables(data);
        if (discrete.size()>0) rtn.addAll(discrete);
        ArrayList<String> continous = getContinousVariables(data);
        if (continous.size()>0) rtn.addAll(continous);
		return rtn;
	}
	
	private String createLevelsFile(RunData data, Context context, String discreteVariable, XFTTableI table, String folderName) throws Exception {
		String rtn = null;
        if (discreteVariable != null && !discreteVariable.equalsIgnoreCase("BAD")) {
        	//Create the .levels file
        	String userFolderPath = getAnalysisFolder(data, folderName);
        	Hashtable distinctValues = getDistinctValues(discreteVariable,table);
        	if (distinctValues != null &&  distinctValues.size() > 0) {
        		File distinctValuesFile = new File(userFolderPath  +File.separator + discreteVariable+".levels");
		        PrintWriter output = null;
        		try {
		        	output = new PrintWriter(new BufferedWriter( new FileWriter(distinctValuesFile) ));
		        	Enumeration keysEnum = distinctValues.keys();
		          	while (keysEnum.hasMoreElements()) {
		          		Object key = keysEnum.nextElement();
        		        output.println(key.toString());
		          	}
		        } finally {
        		          if (output != null) output.close();
        		   }
		        rtn = distinctValuesFile.getAbsolutePath();	
        	}
        	  
        	}
         return rtn;
	   }
	
	private String getAnalysisFolder(RunData data, String subFolder) {
		   return org.nrg.xnat.turbine.utils.ArcSpecManager.GetInstance().getGlobalCachePath() +  TurbineUtils.getUser(data).getLogin() + File.separator + subFolder;		
	}

	
	private Hashtable getDistinctValues(String columnHeader, XFTTableI table) {
		Hashtable distinctValues = new Hashtable();
		table.resetRowCursor();
		while (table.hasMoreRows()) {
			table.nextRow();
			Object rowColumnValue = table.getCellValue(columnHeader);
			if (rowColumnValue !=  null) {
				String colValue = StringUtils.deleteWhitespace(rowColumnValue.toString());
				if (!distinctValues.containsKey(colValue)) {
					distinctValues.put(colValue, "");
				}
			}
		}
		return distinctValues;
	}
	
}
