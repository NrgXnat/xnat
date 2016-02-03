SAXEventHandler = function() {
    dynamicJSLoad("ClassMapping","generated/ClassMapping.js");
    this.classMapping=new ClassMapping();
    
    this.items = new Array();
    
    this.prefixtoURIMapping = new Array(new Array(),new Array());
    this.tempValue = "";   // there is no guarantee that the text event will only fire once
                                                 // for element texts. This variable keeps track of the text that has
                                                 // been returned in the text events. It is reset  when a non-text
                                                 //event is fired and should be read at that time
    this.current=null;
    this.root=null;
    
} 
SAXEventHandler.prototype.addPrefixMapping= function (prefix,uri){
	this.prefixtoURIMapping[0].push(prefix);
	this.prefixtoURIMapping[1].push(uri);
}

SAXEventHandler.prototype.getPrefixForURI= function (uri){
	for(var i=0;i<this.prefixtoURIMapping[0].length;i++){
		if(this.prefixtoURIMapping[1][i]==uri){
			return this.prefixtoURIMapping[0][i];
		}
	}
}

SAXEventHandler.prototype.getURIForPrefix= function (prefix){
	for(var i=0;i<this.prefixtoURIMapping[0].length;i++){
		if(this.prefixtoURIMapping[0][i]==prefix){
			return this.prefixtoURIMapping[1][i];
		}
	}
}

SAXEventHandler.prototype.getBaseElement=function (name){
	var fn = this.classMapping.newInstance;
	return fn(name);
}

SAXEventHandler.prototype.characters = function(data, start, length) {
	if (length > 0) {
        var temp = data.substr(start, length);
        if (temp.length!=0 && this.isValidText(temp)){
            if (this.tempValue != null){
                if (this.current.insertNewLine())
                {
                    this.tempValue +="\n" + temp;
                }else{
                    this.tempValue +=temp;
                }
            }else{
                this.tempValue=temp;
            }
        }
    }

}  


SAXEventHandler.prototype.endDocument = function() {

}

    
SAXEventHandler.prototype.isValidText=function(s)
    {
        if (s ==null)
        {
           return false;
        }else{
            s = this.RemoveChar(s,'\n');
            s = this.RemoveChar(s,'\t');
            
            if (s==null || s=="")
            {
                return false;
            }
        }
        
        return true;
    }
    
 SAXEventHandler.prototype.RemoveChar=function(_base, _old)
    {
        while (_base.indexOf(_old) !=-1)
        {
            var index =_base.indexOf(_old);
            if (index==0)
            {
                _base = _base.substring(1);
            }else if (index== (_base.length-1)) {
                _base = _base.substring(0,index);
            }else{
                var pre = _base.substring(0,index);
                _base = pre + _base.substring(index+1);
            }
        }
        
        return _base;
    }

SAXEventHandler.prototype.endElement = function(name) {

    if (name!="matchingResults"){
    	
			var current_header = this.current.getHeader();
            if (this.tempValue!=null && !this.tempValue=="" && this.isValidText(this.tempValue))
            {
                var currentItem = this.current.getItem();
                try {
                    currentItem.setProperty(current_header,this.tempValue);
                } catch (e){
                    throw new SAXException("Unknown Exception <" + current_header +">" + this.tempValue);
                }finally{
                    this.tempValue=null;
                }
            }
            
            if (this.current.getHeader() == "")
            {
                while ((!this.current.root) && this.current.header=="")
                {
                    this.current = this.current.parent;
                }
                this.current.removeHeader();
                
                if (name=="matchingResult" && this.current.root){
                	this.items.push(this.root);
                	this.root=null;
                	this.current=null;
                }
            }else{
                this.current.removeHeader();
                if (this.current.getIsInlineRepeater() && this.current.header == "")
                {
                    while ((!this.current.root) && this.current.header=="")
                    {
                        this.current = this.current.getParent();
                    }
                    this.current.removeHeader();
                }
            }
    }
}  


SAXEventHandler.prototype.processingInstruction = function(target, data) {


}  // end function processingInstruction


SAXEventHandler.prototype.setDocumentLocator = function(locator) {

}  // end function setDocumentLocator


SAXEventHandler.prototype.startElement = function(name, atts) {
	this.tempValue=null;
	if ((name=="matchingResults" || name=="matchingResult") && this.root==null){
		if (name=="matchingResults"){
			if(atts && atts !=null){
				for(var i=0;i<atts.getLength();i++)
				{
					var localName = atts.getName(i);
					var value = atts.getValue(i);
					
					if (localName=="message"){
						this.message=value;
					}
				}
			}
		}
	}else{
		
	if(this.root==null){
		var item = this.getBaseElement(name);
		if(atts && atts !=null){
			for(var i=0;i<atts.getLength();i++)
			{
				var localName = atts.getName(i);
				var value = atts.getValue(i);
				
				if (value!=""){
					if (localName.indexOf("xmlns:")>-1)
					{
						this.addPrefixMapping(localName.substring(6),value);
					}else if (localName.indexOf("xsi:")>-1)
					{
						//IGNORE
					}else{
						item.setProperty(localName,value);
					}
				}
			}
		}
		this.current = new SAXReaderObject(item,null,null);
		this.root=item;
	}else{
		this.current.addHeader(name);
		var current_header=this.current.getHeader();
		var currentItem=this.current.getItem();
		var TYPE=currentItem.getFieldType(current_header);
		
		if (TYPE!=null 
			&& (TYPE=="field_inline_repeater" 
				|| TYPE=="field_multi_reference"
				|| TYPE=="field_single_reference"
				|| TYPE=="field_NO_CHILD")){
				var foreignElement = null;
                if (atts != null)
                {
                    for (var i=0;i<atts.getLength();i++)
                    {
                        if (atts.getName(i)=="xsi:type")
                        {
                            foreignElement=atts.getValue(i);
                            var index = foreignElement.indexOf(":");
                            if (index !=-1)
                            {
                                foreignElement = this.getURIForPrefix(foreignElement.substring(0,index)) + foreignElement.substring(index);
                            }
                        }
                    }
                }
                try {
                    if (foreignElement==null)
                    {
                        foreignElement= currentItem.getReferenceFieldName(current_header);
                    }
                    
                    var item = this.getBaseElement(foreignElement);  
                    if (atts != null)
                    {
                        for (var i=0;i<atts.getLength();i++)
                        {
                            if (!(atts.getName(i)=="xsi:type" ))
                            {
                                var local = atts.getName(i);
                                var value= atts.getValue(i);

                                if (! value=="")
                                {
                                    item.setProperty(local,value);
                                }
                            }
                        }
                    }
                    try {
                        this.current.item.setReferenceField(current_header,item);
                        this.current = new SAXReaderObject(item,TYPE,this.current);
                        if (TYPE=="field_inline_repeater" || TYPE=="field_NO_CHILD")
                        {
                            this.current.setIsInlineRepeater(true);
                            var match = false;
                            
                            try {
                                if (item.getFieldType(removeNamespace(name))!=null)
                                {
                                    this.current.addHeader(removeNamespace(name));
                                    match = true;
                                }
                            } catch (e) {
                            }
                            
                            if (!match)
                            {
                                if (item.getFieldType(item.getSchemaElementName())!=null){
                                    this.current.addHeader(item.getSchemaElementName());
                                    match = true;
                                }
                            }
                            
                            if (!match){
                                //throw new SAXException("Invalid XML '" + item.getSchemaElementName() + ":" + current_header + "'");
                            }
                        }
                    } catch (e2) {
                        //throw new SAXException("Invalid XML '" + item.getSchemaElementName() + ":" + current_header + "'");
                    }
                } catch ( e) {
                    //throw new SAXException("INVALID XML STRUCTURE:");
                }
		}else{
                this.current.setFIELD_TYPE(TYPE);
                if (atts != null)
                {
                    for (var i=0;i<atts.getLength();i++)
                    {
                        var local = atts.getName(i);
                        var value= atts.getValue(i);

                        if (! value=="")
                        {
                            try {
                                currentItem.setProperty(current_header + "/" + local,value); 
                            } catch (e1) {
                                throw new SAXException("Invalid value for attribute '" + local +"'");
                            }
                        }
                    }
                }
            }
	}
	}

}  // end function startElement


SAXEventHandler.prototype.startDocument = function() {

}  


SAXEventHandler.prototype.comment = function(data, start, length) {
	if (length > 0) {
        var temp = data.substr(start, length);
        if (temp.length!=0 && this.isValidText(temp)){
            var index = temp.indexOf("hidden_fields[");
            if (index>-1){
            	temp = temp.substring(index+14,temp.indexOf("]",index));
            	var array = temp.split(",");
            	for(var hiddenCounter=0;hiddenCounter<array.length;hiddenCounter++){
            		var token=array[hiddenCounter].split("=");
            		token[1]=token[1].substring(1,token[1].length-1);
            		this.current.getItem().setProperty(token[0],token[1]);
            	}
            }
            
        }
    }

}  // end function comment


SAXEventHandler.prototype.endCDATA = function() {
}  // end function endCDATA


SAXEventHandler.prototype.startCDATA = function() {
}  // end function startCDATA


/*****************************************************************************
                    SAXEventHandler Object Error Interface
*****************************************************************************/


SAXEventHandler.prototype.error = function(exception) {
	alert(exception);

}  // end function error


SAXEventHandler.prototype.fatalError = function(exception) {
alert(exception);

}  // end function fatalError


SAXEventHandler.prototype.warning = function(exception) {
}  // end function warning


/*****************************************************************************
                   SAXEventHandler Object Internal Functions
*****************************************************************************/


SAXEventHandler.prototype._fullCharacterDataReceived = function(fullCharacterData) {
}  // end function _fullCharacterDataReceived


function SAXReaderObject(_item,_type,_parent){
	this.item=_item;
	this.header="";
	this.parent=_parent;
	if (_parent!=null){
		this.root=false;
	}else{
		this.root=true;
	}
	this.isInlineRepeater=false;
	this.FIELD_TYPE=_type;
	
	this.addHeader=function(s){
		s=removeNamespace(s);
		if (this.header==""){
			this.header+=s;
		}else{
			this.header +="/" + s;
		}
	}
	
	this.getHeader=function(){
		return this.header;
	}
	
	this.getItem=function(){
		return this.item;
	}
	
	this.getRoot=function(){
		return this.root;
	}
	
	this.getIsInlineRepeater=function(){
		return this.isInlineRepeater;
	}
	
	this.setIsInlineRepeater=function(s){
		this.isInlineRepeater=s;
	}
	
	this.getFIELD_TYPE=function(){
		return this.FIELD_TYPE;
	}
	
	this.setFIELD_TYPE=function(s){
		this.FIELD_TYPE=s;
	}
	
	this.getParent=function(){
		return this.parent;
	}
	
	this.removeHeader=function(){
		if (this.header.indexOf("/")>-1){
			this.header = this.header.substring(0,this.header.lastIndexOf("/"));
		}else{
			this.header ="";
		}
	}
	
	this.insertNewLine=function(){
		if (this.FIELD_TYPE==null){
			return false;
		}else{
			if (this.FIELD_TYPE=="field_LONG_DATA")
            {
                return true;
            }else{
                return false;
            }
		}
	}
}

function removeNamespace(s){
		if (s.indexOf(":")>-1){
			s= s.substring(s.indexOf(":")+1);
		}
		return s;
}



function logEntry(msg){
	document.getElementById("debugout").value+="\r\n" + msg;
}
