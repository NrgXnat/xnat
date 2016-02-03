/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/xnat_projectData.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function projectTracker(){
this.all_xnat_projectDatas = new Array();

function add_project(i,s,n){
  var p = new xnat_projectData();
  p.id=i;
  p.secondaryId=s;
  p.name=n;
  this.all_xnat_projectDatas.push(p);
}
this.add_project=add_project;

function getProjectById(id){
  for(idCount=0;idCount<this.all_xnat_projectDatas.length;idCount++){
    if(this.all_xnat_projectDatas[idCount].id==id){
      return this.all_xnat_projectDatas[idCount];
    }
  }
}
this.getProjectById=getProjectById;
}

function xnat_projectData(){
  this.id=null;
  this.secondaryId=null;
  this.name=null;
  
  function getDisplayName(){
    return this.secondaryId;
  }
  this.getDisplayName=getDisplayName;
}
