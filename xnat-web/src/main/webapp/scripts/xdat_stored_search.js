
/*
 * D:/Development/XNAT/1.6/xnat_builder_1_6dev/plugin-resources/webapp/xnat/scripts/xdat_stored_search.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:12 AM
 */
function getCDATAValue(dom)
{
   if (dom.firstChild)
     return dom.firstChild.nodeValue;
   else
     return null;
}
function getTextValue(name,dom)
{
    if(dom.getElementsByTagName(name).length>0)
    {
      return getCDATAValue(dom.getElementsByTagName(name)[0]);
    }else{
      return null;
    }
}
function getAttributeValue(name,dom)
{
   if (dom.attributes.getNamedItem(name)!= undefined)
    	return dom.attributes.getNamedItem(name).value;
   else
        return null;
}

function xdat_stored_search(dom){
  this.id = null;
  this.description = null;
  this.briefDescription = null;
  this.rootElementName=null;
  
  this.sortByElementName=null;
  this.sortByFieldID=null;
  
  this.search_fields = new Array();
  this.search_where = new Array();
  
  if (dom != null){   
               
    this.id=getAttributeValue("ID",dom);
    
    this.description=getAttributeValue("description",dom);
    this.briefDescription=getAttributeValue("brief-description",dom);
    
    this.rootElementName=getTextValue("root_element_name",dom);
    
    var fields = dom.getElementsByTagName("search_field");
    if (fields.length>0)
    for (fx=0;fx<fields.length;fx++) {
       this.search_fields.push(new xdat_search_field(fields[fx]));
    }
    
    var wheres = dom.getElementsByTagName("search_where");
    if (wheres.length>0){
     for (wx=0;wx<wheres.length;wx++) {
        var currentRoot = new xdat_criteria_set(wheres[wx]);
        if (currentRoot.criteria_set.length==0){
          this.search_where.push(new xdat_criteria_set(null));
          this.search_where[0].method="AND";
          this.search_where[0].criteria_set.push(currentRoot);
        }else{
          this.search_where.push(currentRoot);
        }
     }
    }
    
    if (dom.getElementsByTagName("sort_by")[0]!=undefined){
       this.sortByElementName=getTextValue("element_name",dom.getElementsByTagName("sort_by")[0]); 
       this.sortByFieldID=getTextValue("field_ID",dom.getElementsByTagName("sort_by")[0]); 
    }
  }
  
  function getXMLAtts(){
    var _xml= "";
    if(this.id!=null){
      _xml += " ID=\"" + this.id + "\"";
    }
    if(this.description!=null){
      _xml += " description=\"" + this.description + "\"";
    }
    if(this.briefDescription!=null){
      _xml += " brief-description=\"" + this.briefDescription + "\"";
    }
    
    return _xml;
  }
  this.getXMLAtts=getXMLAtts;
  
  function getXMLBody(){
    var _xml= "";
    if(this.rootElementName!=null){
      _xml += "<root_element_name>" + this.rootElementName + "</root_element_name>";
    }
    
    for (sfx=0;sfx<this.search_fields.length;sfx++) {
        _xml += "<search_field " + this.search_fields[sfx].getXMLAtts() + ">" + this.search_fields[sfx].getXMLBody() + "</search_field>";
    }
    
    for (swx=0;swx<this.search_where.length;swx++) {
        _xml += "<search_where " + this.search_where[swx].getXMLAtts() + ">" + this.search_where[swx].getXMLBody() + "</search_where>";
    }
    
    if (this.sortByFieldID!=null || this.sortByElementName !=null){
      _xml +="<sort_by>";
      if (this.sortByElementName!=null){
        _xml += "<element_name>" + this.sortByElementName + "</element_name>";
      }
      if (this.sortByFieldID!=null){
        _xml += "<field_ID>" + this.sortByFieldID + "</field_ID>";
      }
      _xml +="</sort_by>";
    }
    
    return _xml;
  }
  this.getXMLBody=getXMLBody;
  
  function getXML(){    
   var _xml = "<stored_search " + this.getXMLAtts() +">";
    
    _xml +=this.getXMLBody();
    
    _xml +="</stored_search>";
    return _xml;
  }
  this.getXML=getXML;
  
  function removeSearchField(field){
    for (rx=0;rx<this.search_fields.length;rx++) {
      if (this.search_fields[rx].field_ID==field){
         break;
      }
    }
    
    this.search_fields.splice(rx,1);
  }
  this.removeSearchField=removeSearchField;
  
  function removeCustomCriteria(index){
     if (this.search_where[0].criteria_set.length>1){
	    var customWhere = this.search_where[0].criteria_set[1];
        customWhere.criteria.splice(index,1);
	 }
  }
  this.removeCustomCriteria=removeCustomCriteria;
  
  function addCustomCriteria(field,ct,value){
     if (this.search_where[0].criteria_set.length>1){
	    var customWhere = this.search_where[0].criteria_set[1];
	  }else{
	    var customWhere = new xdat_criteria_set(null);
	    customWhere.method="AND";
	    this.search_where[0].criteria_set.push(customWhere);
	  }
	  
	  customWhere.addCriteria(field,ct,value);
  }
  this.addCustomCriteria=addCustomCriteria;
}

function xdat_search_field(dom){
  this.element_name =null;
  this.field_ID = null;
  this.sequence = null;
  this.type = null;
  this.header = null;
  this.value = null;
  
  if (dom !=null){
    this.element_name=getTextValue("element_name",dom);
    this.field_ID=getTextValue("field_ID",dom);
    this.sequence=getTextValue("sequence",dom);
    this.type=getTextValue("type",dom);
    this.header=getTextValue("header",dom);
    this.value=getTextValue("value",dom);
  }
  
  function getXMLAtts(){
    return "";
  }
  this.getXMLAtts=getXMLAtts;
  
  function getXMLBody(){
    var _xml= "";
    
    if(this.element_name!=null){
      _xml += "<element_name>" + this.element_name + "</element_name>";
    }
    
    if(this.field_ID!=null){
      _xml += "<field_ID>" + this.field_ID + "</field_ID>";
    }
    
    if(this.sequence!=null){
      _xml += "<sequence>" + this.sequence + "</sequence>";
    }
    
    if(this.type!=null){
      _xml += "<type>" + this.type + "</type>";
    }
    
    if(this.header!=null){
      _xml += "<header>" + this.header + "</header>";
    }
    
    if(this.value!=null){
      _xml += "<value>" + this.value + "</value>";
    }
    
    return _xml;
  }
  this.getXMLBody=getXMLBody;
}

function xdat_criteria_set(dom){
  this.method = null;
  
  this.criteria = new Array();
  this.criteria_set = new Array();
  
  if (dom != null){
    this.method = getAttributeValue("method",dom);
    
    var criterias = dom.getElementsByTagName("criteria");
    if (criterias.length>0)
    for (cx=0;cx<criterias.length;cx++) {
       this.criteria.push(new xdat_criteria(criterias[cx]));
    }
    
    var criteria_sets = dom.getElementsByTagName("child_set");
    if (criteria_sets.length>0)
    for (csx=0;csx<criteria_sets.length;csx++) {
       this.criteria_set.push(new xdat_criteria_set(criteria_sets[csx]));
    }
  }
  
  function addCriteria(field,ct,value){
     var newc = new xdat_criteria(null);
     newc.schema_field=field;
     newc.comparison_type=ct;
     newc.value=value;
     
     this.criteria.push(newc);  
  }
  this.addCriteria=addCriteria;
  
  function getXMLAtts(){
    var _xml= "";
    if(this.method!=null){
      _xml += " method=\"" + this.method + "\"";
    }
    
    return _xml;
  }
  this.getXMLAtts=getXMLAtts;
  
  function getXMLBody(){
    var _xml= "";
    if (this.criteria.length>0){
      for (cx=0;cx<this.criteria.length;cx++) {
        _xml +="<criteria " + this.criteria[cx].getXMLAtts()+ ">" + this.criteria[cx].getXMLBody()+ "</criteria>";
      }
    }
    if (this.criteria_set.length>0){
    for (csx=0;csx<this.criteria_set.length;csx++) {
        _xml +="<child_set " + this.criteria_set[csx].getXMLAtts()+ ">" + this.criteria_set[csx].getXMLBody()+ "</child_set>";
        
      }
    }
    
    return _xml;
  }
  this.getXMLBody=getXMLBody;
}

function xdat_criteria(dom){
  this.override_value_formatting = null;
  this.schema_field = null;
  this.comparison_type = null;
  this.custom_search = null;
  this.value = null;
  
  if (dom != null){
    this.override_value_formatting = getAttributeValue("override_value_formatting",dom);
    
    this.schema_field = getTextValue("schema_field",dom);
    this.comparison_type = getTextValue("comparison_type",dom);
    this.custom_search = getTextValue("custom_search",dom);
    this.value = getTextValue("value",dom);
  }
  
  function getXMLAtts(){
    var _xml = "";
    if(this.override_value_formatting!=null){
      _xml += " override_value_formatting=\"" + this.override_value_formatting + "\"";
    }
    
    return _xml;
  }
  this.getXMLAtts=getXMLAtts;
  
  function getXMLBody(){
    var _xml = "";
    if(this.schema_field!=null){
      _xml += "<schema_field>" + this.schema_field + "</schema_field>";
    }
    if(this.comparison_type!=null){
      _xml += "<comparison_type>" + this.comparison_type + "</comparison_type>";
    }
    if(this.custom_search!=null){
      _xml += "<custom_search>" + this.custom_search + "</custom_search>";
    }
    if(this.value!=null){
      _xml += "<value>" + this.value + "</value>";
    }
    
    return _xml;
  }
  this.getXMLBody=getXMLBody;
}