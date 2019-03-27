/*
 * web: justification.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */



XNAT.app.ConfirmWJustification=function(_yuioptions){
    this.yuioptions=_yuioptions;
    this.onResponse=new YAHOO.util.CustomEvent("response",this);

    if(this.yuioptions.width==undefined){
        this.yuioptions.width="400px";
    }    
    
    if(this.yuioptions.height==undefined){
        this.yuioptions.height="150px";
    }    
    
    if(this.yuioptions.close==undefined){
        this.yuioptions.close=true;
    }    
    this.yuioptions.underlay="shadow";
    this.yuioptions.modal=true;
    this.yuioptions.fixedcenter=true;
    this.yuioptions.visible=false;
    
    this.render=function(){
        this.panel=new YAHOO.widget.Dialog("justificationDialog",this.yuioptions);
        this.panel.setHeader(this.yuioptions.header);

        var bd = document.createElement("form");

        var table = document.createElement("table");
        var tb = document.createElement("tbody");
        table.appendChild(tb);
        bd.appendChild(table);
        
        //message (optional)
        if(this.yuioptions.message!=undefined){
            tr=document.createElement("tr");
            td1=document.createElement("td");
            td1.colSpan="2";
            td1.innerHTML=this.yuioptions.message;
            
            tr.appendChild(td1);
            tb.appendChild(tr);
        }
        
        //form checkbox  (optional)
        if(this.yuioptions.checkbox!=undefined){
            tr=document.createElement("tr");
            td1=document.createElement("td");
            td1.colSpan="2";
            td1.innerHTML=this.yuioptions.checkbox;
            
            tr.appendChild(td1);
            tb.appendChild(tr);
        }

        //justification
        tr=document.createElement("tr");
        td1=document.createElement("th");
        td2=document.createElement("td");        

        td1.innerHTML="Justification:";
        td1.align="left";
        var sel = document.createElement("textarea");
        sel.cols="24";
        sel.rows="4";
        sel.id="event_reason";
        sel.name="event_reason";
        td2.appendChild(sel);
        tr.appendChild(td1);
        tr.appendChild(td2);
        tb.appendChild(tr);
        
        //message (optional)
        if(this.yuioptions.note!=undefined){
            tr=document.createElement("tr");
            td1=document.createElement("td");
            td1.colSpan="2";
            td1.innerHTML=this.yuioptions.note;
            
            tr.appendChild(td1);
            tb.appendChild(tr);
        }

        this.panel.setBody(bd);

        this.panel.form=bd;

        this.panel.selector=this;
        var buttons=[{text:"Confirm",handler:{fn:function(){
                this.selector.event_reason = this.form.event_reason.value;
                if(this.selector.event_reason=="" || !this.selector.event_reason.match(/.*\w.*/g)){
                    xmodal.message('Project Validation', 'Please enter a justification.');
                    return;
                }
                if(this.selector.event_reason.indexOf('#')>-1){
                    xmodal.message('Project Validation', 'Please remove the # character from the justification.');
                    return;
                }
                this.cancel();
                this.selector.onResponse.fire();
            }},isDefault:true},
            {text:"Cancel",handler:{fn:function(){
                this.cancel();
            }}}];
        this.panel.cfg.queueProperty("buttons",buttons);


        this.panel.render("page_body");
        this.panel.show();
    }
}

XNAT.app.passThrough=function(_function,scope){
    this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
    this.onCompletion.subscribe(_function, this, scope);
    
    this.fire=function(){
        this.onCompletion.fire();
    }
}

/**
 * Justification dialog implemented with YUI. (Deprecated - Use requestJustificationModal below.)
 */
XNAT.app.requestJustification=function(_id,_header,_function,scope,yuioptions){
    this.id=_id;
    this.onCompletion=new YAHOO.util.CustomEvent("complete",this);
    
    if(yuioptions==undefined){
        this.options=new Object();
    }else{
        this.options=yuioptions;
    }

    this.options.header=_header;
    
    this.dialog=new XNAT.app.ConfirmWJustification(this.options);
    this.dialog.id=_id;
    this.dialog.header=_header;
    this.dialog.onResponse.subscribe(_function,this,scope);

    this.dialog.render();
}

/*
 * Function creates a new justificationDialog if siteConfig.showChangeJustification is true
 * If siteConfig.showChangeJustification is false, the callback is called. 
 */
XNAT.app.requestJustificationDialog = function(action,opts){
    if(showReason){
        XNAT.app.justificationDialog(action,opts);
    }else{
        action();
    }
}

/**
 * Justification dialog implemented with xnat.ui.dialog.
 */
XNAT.app.justificationDialog = function(callback,_opts){
    var opts = _opts ? _opts : {};

    var event_reason = spawn('textarea#event_reason.event_reason.required|name=event_reason', {
        attr: {rows: 4,cols:24, maxlength: 250},
        style: { width: '100%' }
      });
    var msg           = spawn('p',   opts.message ? opts.message : 'Please provide justification for this action.'); 
    var content       = spawn('div', opts.content);         // Optional content
    var note          = spawn('p',   opts.note);            // Optional note from the caller
    var justifyBox    = spawn('div#justificationBox',[ msg, content, event_reason, note ]);
    
    this.call = function(obj){
        $("div#validationError").remove();
        
        if($("#event_reason").val()=="" || !$("#event_reason").val().match(/.*\w.*/g)){
            var icon = spawn('i.fa.fa-asterisk',{ style:{ color: '#c66' } } );
            var msg  = spawn('div#validationError',[icon," Justification Required"]);
            $("#justificationBox").append(msg);
            $("#event_reason").css("border","solid 1px red");
            return;
        }
        if($("#event_reason").val().indexOf('#')>-1){
            var icon = spawn('i.fa.fa-asterisk',{ style:{ color: '#c66' } } );
            var msg  = spawn('div#validationError',[icon," The '#' character is not allowed."]);
            $("#justificationBox").append(msg);
            $("#event_reason").css("border","solid 1px red");
            return;
        }
        callback($("#event_reason").val()); // Pass the event_reason into the callback.
        obj.close();
    }
    
    XNAT.ui.dialog.open({
        title:          opts.header ? opts.header : 'Justification Required',
        width:          opts.width  ? opts.width  : '400px',
        height:         opts.height ? opts.height : '260px',
        destroyOnClose: true,
        content:        justifyBox,
        afterShow:      function () { $("textarea#event_reason").focus(); }, 
        okLabel:        "Confirm",
        okClose:        false,
        okAction:       this.call
    });
}