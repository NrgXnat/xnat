/*
 * web: footer.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


if(XNAT==undefined)XNAT=new Object();
if(XNAT.validators==undefined)XNAT.validators=new Object();
if(XNAT.formValidators==undefined)XNAT.formValidators=new Object();
if(XNAT.app.validatorImpls==undefined)XNAT.app.validatorImpls=new Object();
 
/********************
 Add support for in page validation specification
 */
var forms=0;

//review form and add validation functions for required fields
YAHOO.util.Event.onDOMReady(function(){
    var myforms = document.getElementsByTagName("form");
    for(var iFc=0;iFc<myforms.length;iFc++){
    	if(skipValidationOnForm(myforms[iFc])) {
    		continue;
    	}
        for(var fFc=0;fFc<myforms[iFc].length;fFc++){
            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'required')){
                if(myforms[iFc][fFc].nodeName=="INPUT" || myforms[iFc][fFc].nodeName=="TEXTAREA"){
                	if(myforms[iFc][fFc].nodeName=="INPUT" && myforms[iFc][fFc].type=="radio"){
                		_addValidation(myforms[iFc][fFc],new RadioButtonValidator(myforms[iFc][fFc].name,myforms[iFc][fFc]));
                	}else{
                		_addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.RequiredTextBox));
                   	}
                }else if(myforms[iFc][fFc].nodeName=="SELECT"){
                    _addValidation(myforms[iFc][fFc],new SelectValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.RequiredSelect));
                }
            }

            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'float')){
                if(myforms[iFc][fFc].nodeName=="INPUT"){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.FloatTextBox));
                }
            }

            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'integer')){
                if(myforms[iFc][fFc].nodeName=="INPUT"){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.IntegerTextBox));
                }
            }

            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'alphaNumSP')){
                if(myforms[iFc][fFc].nodeName=="INPUT"){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.AlphaNumSTextBox));
                }
            }

            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'alphaSP')){
                if(myforms[iFc][fFc].nodeName=="INPUT"){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.AlphaSTextBox));
                }
            }
            
            if(myforms[iFc][fFc].nodeName=="INPUT"){
        		if($(myforms[iFc][fFc]).attr('data-regex')!=undefined){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.RegexTextBox,$(myforms[iFc][fFc]).attr('data-regex-message')));
                }
    		}
            
            if(myforms[iFc][fFc].nodeName=="INPUT"){
        		if($(myforms[iFc][fFc]).attr('data-required-if')!=undefined){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],XNAT.app.validatorImpls.PrereqInput,$(myforms[iFc][fFc]).attr('data-required-if-message')));
                }
    		}

            if(YAHOO.util.Dom.hasClass(myforms[iFc][fFc],'max256')){
                if(myforms[iFc][fFc].nodeName=="INPUT"){
                    _addValidation(myforms[iFc][fFc],new TextboxValidator(myforms[iFc][fFc],{isValid:function(_box){
                    	if(_box.value.length>256){
                			return false;
                		}else{
                			return true;
                		}
                    }}));
                }
            }
            
        }
    }

});

function skipValidationOnForm(form) {
	return YAHOO.util.Dom.hasClass(form,'optOutOfXnatDefaultFormValidation');	
}

//this delays the call to add validatoin until after the dom is loaded
function addValidator(_element,_validator){
    YAHOO.util.Event.onDOMReady(function(){
        _addValidation(_element,_validator);
    });
}

//this method should only be called once the dom is loaded
function _addValidation(_element,_validator){
    var element=_element;
    if(_element.nodeName==undefined){
        _element=document.getElementById(_element);
    }

    var _form=_element.form;
    if(_form.ID==undefined){
        _form.ID="form" + forms++;
    }

    if(XNAT.validators[_form.ID]==undefined){
        XNAT.validators[_form.ID]=new Object();
        XNAT.validators[_form.ID].keys=new Array();
    }

    if(XNAT.validators[_form.ID][_element.id]==undefined){
        XNAT.validators[_form.ID][_element.id]=new Array();
        XNAT.validators[_form.ID][_element.id].box=_element;
        XNAT.validators[_form.ID].keys.push(_element.id);
    }
    
    XNAT.validators[_form.ID][_element.id].push(_validator);
}

//add form level validation method.  it should contain 'form' which is the form object and 'validate()' which returns true/false
function addFormValidator(_validator){
    YAHOO.util.Event.onDOMReady(function(){
        _addFormValidation(_validator);
    });
}

//this method should only be called once the dom is loaded
function _addFormValidation(_validator){
  var _form=_validator.form;
  if(_form.ID==undefined){
      _form.ID="form" + forms++;
  }

  if(XNAT.formValidators[_form.ID]==undefined){
      XNAT.formValidators[_form.ID]=new Array();
  }
  
  XNAT.formValidators[_form.ID].push(_validator);
}

function validateBox(box,_checkFunction){
    if(!_checkFunction.isValid(box))
    {
        appendIcon(box,"fa-asterisk","Required",{ color: '#c66' });
        return false;
    }else{
        removeAppendedIcon(box);
        return true;
    }
}

XNAT.app.validatorImpls.RequiredTextBox={
	message:"Required field.",
	isValid:function(_box){
		return (_box.value!="");
	}
};

XNAT.app.validatorImpls.FloatTextBox={
	message:"Value must be a floating point decimal.",
	isValid:function(_box){
		if(_box.value!=""){
			var temp=_box.value.trim().replace(/\s+/g,"");
			if(temp!=_box.value){
				return false;
			}
			if(!isNaN(parseFloat(temp)) && isFinite(temp)){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
};

XNAT.app.validatorImpls.IntegerTextBox={
	message:"Value must be an integer.",
	isValid:function(_box){
		if(_box.value!=""){
			var temp=_box.value.trim().replace(/\s+/g,"");
			if(temp!=_box.value){
				return false;
			}
			if(!isNaN(parseInt(temp)) && isFinite(temp)){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}
};

XNAT.app.validatorImpls.AlphaNumSTextBox={
	message:"Value must be alpha numeric text (no special characters).",
	isValid:function(_box){
		if(_box.value!=""){
			return _box.value.match('^[A-Za-z0-9 ,.]+$');
		}else{
			return true;
		}
	}
};

XNAT.app.validatorImpls.AlphaSTextBox={
    message:"Value must be alphabetic text (no special characters).",
    isValid:function(_box){
        if(_box.value!=""){
            return _box.value.match('^[A-Za-z \'\-]+$');
        }else{
            return true;
        }
    }
};

XNAT.app.validatorImpls.PrereqInput={
	isValid:function(_box){
		this.prereq=$(_box).attr('data-required-if');
		if(!XNAT.app.validator.HasValue(_box) && this.prereq!=undefined){
			if($(this.prereq).length>0){
				return false;//this box is only required if that one is set
			}else{
				return true;
			}
		}else{
			return true;
		}
	}
};

if(XNAT.app.validator==undefined)XNAT.app.validator={};

XNAT.app.validator.HasValue=function(_box){
	if(_box.nodeName=="INPUT" && _box.type=="radio"){
		var passedBoxes=document.getElementsByName(_box.name);
	    var valid=false;
	    for(var stoppedBoxI=0; stoppedBoxI<passedBoxes.length;stoppedBoxI++){
	  	  var stoppedBox=passedBoxes[stoppedBoxI];
	  	  if(stoppedBox.checked){
	  		return true;
	  	  }
	    }
	    return false;
	}else if($(_box).val()==""){
		return false;
	}else{
		return true;
	}
}

XNAT.app.validatorImpls.RegexTextBox={
	isValid:function(_box){
		this.regex=$(_box).attr('data-regex');
		if(_box.value!="" && this.regex!=undefined){
			return _box.value.match(this.regex);
		}else{
			return true;
		}
	}
};

//declaring the constructor
function TextboxValidator(box,_validator,_message) {
	this._validator=_validator;
	if(_message!=undefined){
		this.message=_message;
	}else if(this._validator.message){
		this.message=this._validator.message;
	}
    this.box = box;
}
// declaring instance methods
TextboxValidator.prototype = {
    validate: function () {
        if(this.box.monitored == undefined){
            this.box.monitored=true;
            YAHOO.util.Event.on(this.box,"change",function(env,var2){
                return validateBox(this.box,this._validator);
            },this,true);
        }


        if(!validateBox(this.box,this._validator)){
            this.box.focus();
            return false;
        }else{
            return true;
        }
    }
};

function validateSelect(sel){
    if(sel.options[sel.selectedIndex].value==""){
        appendIcon(sel,"fa-asterisk","Required",{ color: '#c66' });
        return false;
    }else{
        removeAppendedIcon(sel);
        return true;
    }
}

XNAT.app.validatorImpls.RequiredSelect={
	message:"Required field.",
	isValid:function(sel){
		return (sel.options[sel.selectedIndex].value!="");
	}
};


//declaring the constructor
function SelectValidator(box) {
    this.box = box;
}
//declaring instance methods
SelectValidator.prototype = {  validate: function () {
    if(this.box.monitored == undefined){
        this.box.monitored=true;
        YAHOO.util.Event.on(this.box,"change",function(env,var2){
            return validateSelect(this);
        });
    }


    if(!validateSelect(this.box)){
        this.box.focus();
        return false;
    }else{
        return true;
    }
}
};


function RadioButtonValidator(name,box){
	this.name=name;
	
	this.box=box;
}

RadioButtonValidator.prototype = {  validate: function () {
	var passedBoxes=document.getElementsByName(this.name);
    var valid=false;
    for(var stoppedBoxI=0; stoppedBoxI<passedBoxes.length;stoppedBoxI++){
  	  var stoppedBox=passedBoxes[stoppedBoxI];
  	  if(stoppedBox.checked){
  		valid=true;
  	  }
    }
    
    if(!valid){
    	stoppedBox.focus();
        return false;
    }else{
        return true;
    }
}};

YAHOO.util.Event.onDOMReady( function()
{
    var myforms = document.getElementsByTagName("form");
    for (var i=0; i<myforms.length; i++) {
        var myForm = myforms[i];
    	if(skipValidationOnForm(myForm)) {
    		continue;
    	}        
        if(!myForm.ID) {
            myForm.ID = "form" + forms++;
        }

        //take the statically defined onsubmit action and add it as a yui event instead.  it will be executed after form field validation, but before other submit actions
        if (!myForm.userDefinedSubmit) {
            myForm.userDefinedSubmit = myForm.onsubmit;
        }
        myForm.onsubmit = null;

        //function to add validation to any form elements with specific classes (required, etc)
        //an array of validator functions is stored in XNAT.validators.  They are tied to the form by the form's ID.
        //this function iterates over those validators and tracks the overall validation outcome in a variable called _ok which is attached the the array of validators.
        //the _ok may be checked by other functions
        myForm.managedSubmit=function (env, var2) {
            var validators = XNAT.validators[this.ID];
            if(validators!=undefined){
            	try{
	                validators._ok = true;
	                for(var elementIdI=0;elementIdI<validators.keys.length;elementIdI++){
	                	var elementId=validators.keys[elementIdI];
	                	if(validators[elementId] instanceof Array){
		                	try{
		                		validators[elementId]._ok=true;
		                		this.message=undefined;
		                		for (var iVc = 0; iVc < validators[elementId].length; iVc++) {
		                			var tempValidator=validators[elementId][iVc];
		    	                    if (!tempValidator.validate()) {
		    	                        validators[elementId]._ok = false;
		    	                        validators._ok = false;
		    	                        this.focus = validators[elementId][iVc].box;
		    	                        this.message=tempValidator.message;
		    	                    }
		    	                }
		                		
		                		if(validators[elementId]._ok){
		                	        removeAppendedIcon(validators[elementId].box);
		                		}else{
		                	        appendIcon(validators[elementId].box,"fa-asterisk",this.message,{ color: '#c66' });
		                		}
		                	}catch(e){
                                xmodal.message('Email Validation Error', "Error performing validation.");
		                		validators._ok=false;
		                	}
	                	}
	                }
            	}catch(e){
                    xmodal.message('Email Validation Error', "Error performing validation.");
            		validators._ok=false;
            	}
            	
            	//finished form field validation
                if (!validators._ok) {
                	if(env != undefined){
                        YAHOO.util.Event.stopEvent(env);
                	}
                    showContent();
                    if(this.focus!=undefined)
                        this.focus.focus();

                    return false;
                }
            }

        	try{
                //execute user defined form submit action
                var result = (this.userDefinedSubmit) ? this.userDefinedSubmit() : undefined;
                if (result == undefined) {
                    result = true;
                }
                if(!result){
                	if(env != undefined){
                        YAHOO.util.Event.stopEvent(env);
                	}

                    //hack here to make sure that no matter what we are checking custom form validation and creating a modal
                    //if there are any custom form errors. this first check will be done in the case that another error has been found in base validation 
                    if(XNAT.formValidators!=undefined){
                        var formValidators = XNAT.formValidators[this.ID];
                        if(formValidators!=undefined){
                            for(var i=0;i<formValidators.length;i++){
                                if (formValidators[i].validate!=undefined) {
                                    formValidators[i].validate();
                                }
                            }
                        }
                    }

                    showContent();
                    return false;
                }

                //execute additional form level validation which should run after other validation but before form completion
                if(XNAT.formValidators!=undefined){
	                var formValidators = XNAT.formValidators[this.ID];
	                if(formValidators!=undefined){
	                	for(var iFVc=0;iFVc<formValidators.length;iFVc++){
	                		if (formValidators[iFVc].validate!=undefined && !(formValidators[iFVc].validate())) {
	                			if(env != undefined){
	                			    YAHOO.util.Event.stopEvent(env);
	                			}
	                            showContent();
	                            return false;
		                    }
	                	}
	                }
                }
                
                //check for nullable fields and make them NULL if they are ""
                if (this.ID) {
                    for(var iFc=0;iFc<this.length;iFc++){
                        if(YUIDOM.hasClass(this[iFc],'nullable')){
                            if((this[iFc].nodeName=="INPUT" || this[iFc].nodeName=="TEXTAREA") && this[iFc].value==""){
                                this[iFc].value="NULL";
                            }
                        }
                    }
                }
                

                //hide the forms
            	if(!YUIDOM.hasClass(this,'noHide')){//check if we are forbidden from hiding this form
            		concealContent();
            	}
            	
                return result;
        	}catch(e){
                xmodal.message('Email Validation Error', "An error occurred during form validation.");
                if(env != undefined){
        		    YAHOO.util.Event.stopEvent(env);
                }
                showContent();
                return false;
        	}
        };
        YAHOO.util.Event.on(myForm, "submit", myForm.managedSubmit,null,myForm);
    }
});


XNAT.app.toggle=function (_name){
    var elements = document.getElementsByName(_name);
    for(var trI=0;trI<elements.length;trI++){
        if(elements[trI].style.display=="none"){
            elements[trI].style.display="block";
        }else{
            elements[trI].style.display="none";
        }
    }
};

$(function(){

    var $body = $body || $(document.body);

    // add title for <option> of multi-select on hover
    $body.on('hover','select[multiple] > option',function(){
        $(this).attr('title',$(this).text());
    });


    $body.on('click', 'a.nolink, a[href^="#!"]', function(e){
        e.preventDefault();
    });


    $body.on('click', 'button.disabled, a.disabled', function(e){
        e.preventDefault();
        e.stopImmediatePropagation();
        return false;
    });

    // display overflowing contents of truncated table cells in a dialog: XNAT-6398
    $body.on('mouseenter','td.xxl-content',function(){
        $(this).prepend(spawn('b.xxl-highlight'))
    });
    $body.on('mouseleave','td.xxl-content',function(){
        $(this).find('.xxl-highlight').remove();
    });

    $body.on('click','td.xxl-content',function(e){
        var i = $(this).index(),
            content = $(this).html(),
            width = 800;

        content = content.replace('<b class="xxl-highlight"></b>','');

        if (content.indexOf(',') >= 0) {
            width = 600;
            var arr = content.split(','),
                items = [];
            arr.forEach(function(item){
                items.push(spawn('li',item));
            });
            content = spawn('!',[
                spawn('h4',arr.length+" Items"),
                spawn('ul',items)
            ])
        }
        var th = $(this).parents('table').find('thead').find('th')[i];
        var title = $(th).find('div').html() + ' ('+$(th).attr('name')+')';
        XNAT.ui.dialog.message({
            title: 'Contents of '+ title,
            content: content,
            width: width
        });
    })

});

jq(window).load(function(){
    // trying to make the text readable
    jq('[style*="font-size:8px"]').addClass('smallest_text');
    jq('[style*="font-size: 8px"]').addClass('smallest_text');
    jq('[style*="font-size:9px"]').addClass('smallest_text');
    jq('[style*="font-size: 9px"]').addClass('smallest_text');
    jq('[style*="font-size:10px"]').addClass('smaller_text');
    jq('[style*="font-size: 10px"]').addClass('smaller_text');
    jq('[style*="font-size:11px"]').addClass('small_text');
    jq('[style*="font-size: 11px"]').addClass('small_text');

    // ridding <font> tags of their meaning
    jq('font').removeAttr('face size').css('font-family','Arial, Helvetica, sans-serif');

    jq('#actionsMenu ul ul').addClass('shadowed');

    // initialize any more "Chosen" menus
    menuInit();

    // email verification scripts
    //
    function emailFormatVerify(email) {
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }
    //
    // paramaters: email_input = class/id of email input field, empty_alert = true if you want an alert if the email input is empty, confirm_alert = true if you want a confirmation alert
    var emailValidate = function(email_input,empty_alert,confirm_alert){

        var email_value = jq(email_input).val() ;

        if (email_value.length){
            if (emailFormatVerify(email_value)) {
                if (confirm_alert == true) {
                    xmodal.message('Email Validation', "Email verified.");
                }
            }
            else {
                xmodal.message('Email Validation', "Please enter a proper email address in the format: name@domain.com.");
                setTimeout(function(){ email_input.focus(); }, 1);
            }
        }
        else {
            if (empty_alert == true) {
                xmodal.message('Email Validation', "Please enter an email address.");
                setTimeout(function(){ email_input.focus(); }, 1);
            }
        }

    };

    // if we ever want to kill the left bar, just add "no_left_bar" class to <body>
    //jq('body.no_left_bar').find('td.leftBar').remove();

//    // This shouldn't execute if you aren't on the user edit page!!!!!
//    // (it would only execute on an input with class="email_format")
//    // validate email when leaving an email input box and don't show a 'verified' alert
//    // use an "onblur" class on input element if you want to validate on blur
//    jq('input.email_format.onblur').blur(function(){
//        emailValidate(this,false,false);
//    });
//    //
//    // validate email when clicking an element with class="validate_email" with no alert
//    jq('.validate_email').click(function(){
//        emailValidate('input.email_format',false,false);
//    });

    $('#loading').hide();

});


// Set the leaving flag to false on every load.
window.leaving = false;
jq(window).bind('beforeunload', function() {
    // Then before we unload set that flag to true.
    window.leaving = true;
});
