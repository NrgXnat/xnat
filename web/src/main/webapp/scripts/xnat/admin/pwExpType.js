/*
 * web: pwExpType.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

// interactions with 'Security Passwords' section of admin ui

console.log('pwExpType.js');

(function(){
    var fieldInterval, fieldDate, sdtDisabled, sdtInterval, sdtDate, openCal;
    setTimeout(function(){
      fieldInterval = $('#passwordExpirationInterval');
      fieldDate = $('#passwordExpirationDate');
      fieldDate.attr('placeholder', 'MM/DD/YYYY');
      openCal = $('#openCal-passwordExpirationDate');
      openCal.click(openCalendar);
      fieldInterval[0].style.marginTop='10px';
      fieldDate[0].style.width = '90px';
      fieldDate[0].style.marginTop='10px';
      fieldDate.datetimepicker({
        timepicker:false,
        format:'m/d/Y',
        maxDate:'+1970/01/01' // today is max date, disallow future date selection
      });
      sdtDisabled = $('#passwordExpirationTypeDisabled');
      sdtInterval = $('#passwordExpirationTypeInterval');
      sdtDate = $('#passwordExpirationTypeDate');
      sdtDisabled.click(changePasswordExpirationType);
      sdtInterval.click(changePasswordExpirationType);
      sdtDate.click(changePasswordExpirationType);
      changePasswordExpirationType(XNAT.data.siteConfig.passwordExpirationType);
      reuseDisabled = $('#passwordReuseTypeDisabled');
      reuseHistorical = $('#passwordReuseTypeHistorical');
      reuseDisabled.click(changePasswordReuseType);
      reuseHistorical.click(changePasswordReuseType);
      changePasswordReuseType(XNAT.data.siteConfig.passwordReuseRestriction);
    }, 1);

    function openCalendar(){
      fieldDate.datetimepicker('show');
    };

    function changePasswordExpirationType(eventOrValue){
        var value = eventOrValue;
        if (typeof eventOrValue === 'object') {
            if (eventOrValue.target.id == "passwordExpirationTypeDisabled") {
                value = 'Disabled';
            } else if (eventOrValue.target.id == "passwordExpirationTypeInterval") {
                value = 'Interval';
            }
            else {
                value = 'Date';
            }
        }
        sdtDisabled.val(value);
        sdtInterval.val(value);
        sdtDate.val(value);

        var interval = $('div.input-bundle.interval');
        var intervalUnits = $('span#intervalUnits');
        var date = $('div.input-bundle.date');
        var datePicker = $('span#datePicker');

        if (value == 'Disabled') {
            sdtDisabled.prop('checked', true);
            interval.val(-1);
            interval.hide();
            intervalUnits.hide();
            date.hide();
            datePicker.hide();
        } else if (value == 'Interval') {
            sdtInterval.prop('checked', true);
            interval.show();
            intervalUnits.show();
            date.hide();
            datePicker.hide();
        } else {
            sdtDate.prop('checked', true);
            date.show();
            datePicker.show();
            interval.hide();
            intervalUnits.hide();
        }
    }
    
    function changePasswordReuseType(eventOrValue){
        var value = eventOrValue;
        if (typeof eventOrValue === 'object') {
            if (eventOrValue.target.id == "passwordReuseTypeHistorical") {
                value = 'Historical';
            } else {
                value = 'Disabled';
            }
        }
        reuseDisabled.val(value);
        reuseHistorical.val(value);
        var interval = $('div.input-bundle.reuseInterval');
        if (value == 'Disabled') {
            reuseDisabled.prop('checked', true);
            interval.val(-1);
            interval.hide();
        } else if (value == 'Historical') {
            reuseHistorical.prop('checked', true);
            interval.show();
        }
    }
})();
