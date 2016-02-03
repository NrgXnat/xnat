/*****************************************************************************
                    SAXEventHandler Object
*****************************************************************************/

SAXEventHandler = function() {
    /*****************************************************************************
    function:  SAXEventHandler

    author: djoham@yahoo.com

    description:
        this is the constructor for the object which will be the sink for
        the SAX events
    *****************************************************************************/
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
    
}  // end function SAXEventHandler

/*****************************************************************************
                    SAXEventHandler Object SAX INTERFACES
*****************************************************************************/

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
    /*****************************************************************************
    function:  characters

    author: djoham@yahoo.com

    description:
        Fires when character data is found

        ****** NOTE******
        there is no guarantee that this event will only fire once for
        text data. Particularly, in the cases of escaped characters,
        this event can be called multiple times. It is best to keep a
        variable around to collect all of the data returned in this event.
        You'll know all of the text is returned when you get a non-characters
        event.

        The variable that this object keeps for just this purpose is this.characterData

        To ensure that text values are handled properly, each event calls the function
        this._handleCharacterData. This event resets the characterData
        variable for non-characters events. It also calls the function
        this._fullCharacterDataReceived.

        since this event can be called many times, you should put your text handling
        code in the function this._fullCharacterDataReceived if you need to act only
        when you know you have all of the character data for the element.

        data is your full XML string
        start is the beginning of the XML character data being reported to you
        end is the end of the XML character data being reported to you

        the data can be retrieved using the following code:
        var data = data.substr(start, length);

        Generally, you won't have any code here. Place your code in
        this._fullCharacterDataReceived instead

    *****************************************************************************/
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

}  // end function characters


SAXEventHandler.prototype.endDocument = function() {
    /*****************************************************************************
    function:  endDocument

    author: djoham@yahoo.com

    description:
        Fires at the end of the document
    *****************************************************************************/
    //place endDocument event handling code below this line

}  // end function endDocument

    
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
    /*****************************************************************************
    function:  endElement

    author: djoham@yahoo.com

    description:
        Fires at the end of an element

        name == the element name that is ending
    *****************************************************************************/

    //place endElement event handling code below this line
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
}  // end function endElement


SAXEventHandler.prototype.processingInstruction = function(target, data) {
    /*****************************************************************************
    function:  processingInstruction

    author: djoham@yahoo.com

    description:
        Fires when a processing Instruction is found

        In the following processing instruction:
        <?xml version=\"1.0\"?>

        target == xml
        data == version"1.0"
    *****************************************************************************/

    //place processingInstruction event handling code below this line


}  // end function processingInstruction


SAXEventHandler.prototype.setDocumentLocator = function(locator) {
    /*****************************************************************************
    function:  setDocumentLocator

    author: djoham@yahoo.com

    description:
        This is the first event ever called by the parser.

        locator is a reference to the actual parser object that is parsing
        the XML text. Normally, you won't need to trap for this error
        or do anything with the locator object, but if you do need to,
        this is how you get a reference to the object
    *****************************************************************************/
    //place setDocumentLocator event handling code below this line


}  // end function setDocumentLocator


SAXEventHandler.prototype.startElement = function(name, atts) {
    /*****************************************************************************
    function:  startElement

    author: djoham@yahoo.com

    description:
        Fires at the start of an element

        name == the name of the element that is starting
        atts == an array of element attributes

        The attribute information can be retrieved by calling
        atts.getName([ordinal])  -- zero based
        atts.getValue([ordinal]) -- zero based
        atts.getLength()
        atts.getValueByName([attributeName])

    *****************************************************************************/
    //place startElement event handling code below this line
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
    /*****************************************************************************
    function:  startDocument

    author: djoham@yahoo.com

    description:
        Fires at the start of the document
    *****************************************************************************/
    //place startDocument event handling code below this line
	

}  // end function startDocument


/*****************************************************************************
                    SAXEventHandler Object Lexical Handlers
*****************************************************************************/


SAXEventHandler.prototype.comment = function(data, start, length) {
    /*****************************************************************************
    function:  comment

    author: djoham@yahoo.com

    description:
        Fires when a comment is found

        data is your full XML string
        start is the beginning of the XML character data being reported to you
        end is the end of the XML character data being reported to you

        the data can be retrieved using the following code:
        var data = data.substr(start, length);
    *****************************************************************************/
    //place comment event handling code below this line
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
    /*****************************************************************************
    function:  endCDATA

    author: djoham@yahoo.com

    description:
        Fires at the end of a CDATA element
    *****************************************************************************/
    //place endCDATA event handling code below this line


}  // end function endCDATA


SAXEventHandler.prototype.startCDATA = function() {
    /*****************************************************************************
    function:  startCDATA

    author: djoham@yahoo.com

    description:
        Fires at the start of a CDATA element
    *****************************************************************************/
    //place startCDATA event handling code below this line


}  // end function startCDATA


/*****************************************************************************
                    SAXEventHandler Object Error Interface
*****************************************************************************/


SAXEventHandler.prototype.error = function(exception) {
    /*****************************************************************************
    function:  error

    author: djoham@yahoo.com

    description:
        Fires when an error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    //place error event handling code below this line
	alert(exception);

}  // end function error


SAXEventHandler.prototype.fatalError = function(exception) {
    /*****************************************************************************
    function:  fatalError

    author: djoham@yahoo.com

    description:
        Fires when a  fatal error is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    //place fatalError event handling code below this line
alert(exception);

}  // end function fatalError


SAXEventHandler.prototype.warning = function(exception) {
    /*****************************************************************************
    function:  warning

    author: djoham@yahoo.com

    description:
        Fires when a warning is found.

        Information about the exception can be found by calling
        exception.getMessage()
        exception.getLineNumber()
        exception.getColumnNumber()
    *****************************************************************************/
    //place warning event handling code below this line


}  // end function warning


/*****************************************************************************
                   SAXEventHandler Object Internal Functions
*****************************************************************************/


SAXEventHandler.prototype._fullCharacterDataReceived = function(fullCharacterData) {
    /*****************************************************************************
    function:  _fullCharacterDataReceived

    author: djoham@yahoo.com

    description:
        this function is called when we know we are finished getting
        all of the character data. If you need to be sure you handle
        your text processing when you have all of the character data,
        your code for that handling should go here

        fullCharacterData contains all of the character data for the element
    *****************************************************************************/

    //place character (text) event handling code below this line


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
