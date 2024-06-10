/*
 * web: datetimeManagement.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

var XNAT = getObject(XNAT);

(function($, console){

    var datetimeMgr;

    console.log('datetimeManagement.js');

    XNAT.admin =
        getObject(XNAT.admin||{});

    XNAT.admin.datetimeManager = datetimeMgr =
        getObject(XNAT.admin.datetimeManager||{});

    var panel = $(document).find('#admin-date-formatting-panel');

    datetimeMgr.legacyCheck = function(){
        const configObj = XNAT.data.siteConfig;
        const configMatrix = [
            { old: 'UI.date-format', new: ['uiDateFormat'] },
            { old: 'UI.date-time-format', new: ['uiDateTimeFormat'] },
            { old: 'UI.time-format', new: ['uiTimeFormat'] },
            { old: 'UI.date-time-seconds-format', new: ['uiDateTimeSecondsFormat'] },
        ];

        var needsUpdate = false;

        configMatrix.forEach(function(setting){
            if (configObj[setting.old] && !configObj[setting.new]) {
                // find instances where legacy properties are set and new ones have not been migrated.
                needsUpdate = true;
                panel.find('input[name='+setting.new+']').val(configObj[setting.old]);
            }
        });

        if (needsUpdate) {
            panel.append(spawn('div.alert.datetimeWarning','Legacy preferences have been found and their values have been imported but not saved. Please save these settings to update XNAT.'))
        }
    };

    function adminDateTimeHandler(){
        
        // populate input fields if old-style "UI.date-format" prefs are stored.
        datetimeMgr.legacyCheck();

        panel.on('click','.submit',function(){
            panel.find('div.dateTimeWarning').remove();
        })

        // $(document).on('click','#validate-datetime-format',function(e){
        //     e.preventDefault();
        //
        //     var dialogContent = spawn(
        //         "!",[
        //             spawn('p',[
        //                 'The example date ',
        //                 '<b>October 31, 11:59:30 PM</b>',
        //                 'would be displayed as follows, given the user inputs in this panel:'
        //             ]),
        //             spawn('table.xnat-table',[
        //                 spawn('tr',[
        //                     spawn('th','Date Format'),
        //                     spawn('td.dateFormat')
        //                 ]),
        //                 spawn('tr',[
        //                     spawn('th','Time Format'),
        //                     spawn('td.timeFormat')
        //                 ]),
        //                 spawn('tr',[
        //                     spawn('th','Date/Time Format'),
        //                     spawn('td.datetimeFormat')
        //                 ]),
        //                 spawn('tr',[
        //                     spawn('th','Date/Time/Seconds Format'),
        //                     spawn('td.datetimesecondsFormat')
        //                 ])
        //             ])
        //
        //         ]);
        //
        //     XNAT.dialog.open({
        //         title: "Date/Time Format Validation",
        //         content: dialogContent,
        //         beforeShow: function(obj){
        //             // Goal: use the panel inputs to reformat the date string...
        //             // Requires a JavaScript library that can parse Java date formatting syntax and translated it to JS date formatting
        //         }
        //     })
        // });

    }

    $(adminDateTimeHandler);




})(window.jQuery, window.console);
