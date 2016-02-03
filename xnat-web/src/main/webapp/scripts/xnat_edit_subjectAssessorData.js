/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/xnat_edit_subjectAssessorData.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:35 AM
 */
function validateSubjectAssessorForm()
{
   var matchesDIV = document.getElementById("subject_id");
   var subject = matchesDIV.value;
   if (subject=="")
   {
     window.manager.showOption1();
     xModalMessage('Subject Assessor Validation', "Please select a " + XNAT.app.displayNames.singular.subject.toLowerCase() + " before proceeding.");
     return false;
   }
   return validateExperimentForm();
}
//overwrites function in xnat_edit_experimentData.js
function submitParentForm(){
   if (matchedExpts.length>1)
   {
      var matchAlert = "The specified data label is in use by multiple stored experiments.  Please use a unique label for this item.";
      matchAlert+="";
      xModalMessage('Subject Assessor Validation', matchAlert);
      submitHistory=false;
      return false;
   }else if(matchedExpts.length>0){
      var matchedExpt=matchedExpts[0];
      if (matchedExpt.xsiType!=elementName)
      {
        xModalMessage('Subject Assessor Validation', 'ERROR:  This ID is already in use for a different experiment.  Please use a different ID.');
        submitHistory=false;
        return false;
      }else{
      	var primaryProject = matchedExpt.getProperty("label");
       if (primaryProject == undefined || primaryProject==null || primaryProject=="")
       {
         primaryProject = matchedExpt.getProperty("sharing/share[project=" +matchedExpt.getProperty("project") + "]/label");
       }
       if (primaryProject == undefined || primaryProject==null || primaryProject=="")
       {
         primaryProject=matchedExpt.getProperty("ID");
       }
       if(confirm("WARNING: " + primaryProject + " already exists. Storing this entry may result in modifications to that entry. Do you want to proceed?"))
       {
         document.getElementById(elementName+"/ID").value=matchedExpt.getProperty("ID");
         submitHistory=true;
         //submit
         document.getElementById("form1").submit();
       }else{
         submitHistory=false;
         return false;
       }
      }
   }else{
         // NO MATCHES FOUND
         submitHistory=true;
         //submit
         document.getElementById("form1").submit();
   }
}