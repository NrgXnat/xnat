/*
 * org.nrg.pipeline.utils.PipelineFileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.pipeline.utils;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.pipeline.xmlbeans.PipelineDocument;
import org.nrg.xdat.om.ArcProject;
import org.nrg.xft.XFT;
import org.nrg.xnat.turbine.utils.ArcSpecManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PipelineFileUtils {

	public static PipelineDocument GetDocument(String pathToPipelineXmlFile) throws Exception {
        if (!pathToPipelineXmlFile.endsWith(".xml")) pathToPipelineXmlFile += ".xml";
        File xmlFile = new File(pathToPipelineXmlFile);
        //Bind the instance to the generated XMLBeans types.
        ArrayList errors = new ArrayList();
        XmlOptions xopt = new XmlOptions();
        xopt.setErrorListener(errors);
         XmlObject xo = XmlObject.Factory.parse(xmlFile, xopt);
         if (errors.size() != 0) {
             throw new XmlException(errors.toArray().toString());
         }
         //String err = XMLBeansUtils.validateAndGetErrors(xo);
         //if (err != null) {
          //   throw new XmlException("Invalid XML " + xmlFile + "\n" + errors);
        //}
        if (!(xo instanceof PipelineDocument)) {
            throw new Exception("Invalid XML file supplied " + pathToPipelineXmlFile + " ==> Expecting a pipeline document");
        }
        PipelineDocument pipelineDoc = (PipelineDocument)xo;
        //String error = XMLBeansUtils.validateAndGetErrors(pipelineDoc);
        //if (error != null) {
          //  throw new XmlException("Invalid XML " + pathToPipelineXmlFile + "\n" + errors);
        //}
        return pipelineDoc;
}


    public static  String getMaxMatching(String file1, String file2, String scanId1, String scanId2) {
        String rtn = null;
        if (file1 == null || file2 == null || scanId1 == null || scanId2 == null) return null;
        int index = 0;
        while (true) {
            if (file2.charAt(index) != file1.charAt(index))
                break;
            index++;
        }
        rtn = file1.substring(0,index);
        if (scanId1.charAt(0) == scanId2.charAt(0)) {
            if (rtn.endsWith("."+scanId1.charAt(0))) {
                rtn = rtn.substring(0, rtn.length()-2);
            }
        }
        System.out.println("Max Matching is " + rtn);
        int slash = rtn.lastIndexOf(File.separator);
        if (slash != -1) {
            rtn = rtn.substring(slash+1,rtn.length());
        }

        if (rtn.endsWith(".")) rtn = rtn.substring(0,rtn.length()-1);

        System.out.println("Returnning formatted " + rtn);
        return rtn;
    }

    public static String getName(String path) {
        String rtn = path;
        int indexOfLastSlash = path.lastIndexOf(File.separator);
        if (indexOfLastSlash != -1) {
            rtn = path.substring(indexOfLastSlash + 1);
        }
        return rtn;
    }

	public static String getBuildDir(String project,  boolean postfixTimestamp) {
		ArcProject arcProject = ArcSpecManager.GetFreshInstance().getProjectArc(project);
		String buildPath = XFT.GetPipelinePath()  ;
		if (arcProject != null) {
			buildPath = arcProject.getPaths().getBuildpath();
		}
		if (postfixTimestamp) {
			Calendar cal = Calendar.getInstance();
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		    String s = formatter.format(cal.getTime());
			buildPath += s + "/" ;
		}
		return buildPath;
	}



}
