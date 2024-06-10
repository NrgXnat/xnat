/**********************************************
 * Inserts a YUI Calendar to handle date validation & selection
 */
function insertYUICalendar(input,_title){
    //CREATE BUTTON
    var button = document.createElement("input");
    button.type="button";
    button.value=">";
    button.id="cal_"+input.id;

    //CREATE CALENDAR CONTAINER
    var calendarContainer = document.createElement("DIV");
    calendarContainer.className="yui-skin-sam";
    calendarContainer.style.display="inline";

    //INSERT INTO DOM
    if(input.nextSibling==undefined){
        input.parentNode.appendChild(calendarContainer);
    }else{
        input.parentNode.insertBefore(calendarContainer,input.nextSibling);
    }

    calendarContainer.appendChild(button);

    var calendarDIV= document.createElement("DIV");
    calendarContainer.appendChild(calendarDIV);
    calendarDIV.style.position="absolute";

    try {
        var cal1 = new YAHOO.widget.Calendar("cal1", calendarDIV, {context:["cal_" + input.id, "tr", "tl"], title:_title, navigator:true, close:true, visible:false});
    } catch (e) {
        showMessage("page_body", "Notification", "Found exception creating calendar: " + e);
    }

    cal1.text_input = input;
    input.calendar=cal1;

    button.calendar=cal1;
    button.onclick=function(){
        this.calendar.show();
        return false;
    }

    cal1.hider=button;

    cal1.handleSelect = function(type,args,obj) {
        var dates = args[0];
        var date = dates[0];
        var year = date[0], month = date[1].toString(), day = date[2].toString();

        if(month !=null && month!=undefined && month.length==1){
            month="0"+month;
        }

        if(day !=null && day!=undefined && day.length==1){
            day="0"+day;
        }

        this.text_input.value = month + "/" + day + "/" + year;
        this.text_input.calendar.hide();
        try{this.text_input.onblur();}catch(e){};
        const event = new Event('calendarSelected');
        input.dispatchEvent(event);
    }


    input.onchange=function(){
        if(this.value!=""){
            this.value=this.value.replace(/[-]/,"/");
            this.value=this.value.replace(/[.]/,"/");
            if(isValidDate(this.value)){
                this.calendar.select(this.value);
                var selectedDates = this.calendar.getSelectedDates();
                if (selectedDates.length > 0) {
                    var firstDate = selectedDates[0];
                    this.calendar.cfg.setProperty("pagedate", (firstDate.getMonth()+1) + "/" + firstDate.getFullYear());
                    this.calendar.render();
                } else {
                    showMessage("page_body", "Notification", "Invalid date. MM/DD/YYYY");
                }
            }else if(this.value == "NULL"){
                //don't do anything, they're trying to clear out the value.
            }else{
                showMessage("page_body", "Notification", "Invalid date. MM/DD/YYYY");
                this.value="";
                this.focus();
            }
        }
    }

    cal1.selectEvent.subscribe(cal1.handleSelect, cal1, true);

    cal1.render();
    cal1.hide();

    if(input.value!=""){
        input.onchange();
    }
}
