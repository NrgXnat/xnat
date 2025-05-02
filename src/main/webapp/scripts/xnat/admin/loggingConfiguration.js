// noinspection JSUnresolvedReference

/*
 * web: loggingConfiguration.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Manage logging configuration
 */

console.log('loggingConfiguration.js');

// const XNAT = getObject(XNAT || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {

    const LEVEL_ERROR = "ERROR";
    const LEVEL_WARN = "WARN";
    const LEVEL_INFO = "INFO";
    const LEVEL_DEBUG = "DEBUG";
    const LEVEL_TRACE = "TRACE";
    const LEVELS = [LEVEL_ERROR, LEVEL_WARN, LEVEL_INFO, LEVEL_DEBUG, LEVEL_TRACE];

    let loggingConfiguration;
    restUrl = XNAT.url.restUrl;

    XNAT.admin = getObject(XNAT.admin || {});

    XNAT.admin.loggingConfiguration = loggingConfiguration = getObject(XNAT.admin.loggingConfiguration || {});

    function loggingConfigUrl(appended) {
        return restUrl('/xapi/logs/loggers' + (appended ? '/' + appended : ''), '', true);
    }

    function loggingDownloadUrl(logName) {
        return restUrl('/xapi/logs/download' + (logName ? '/' + logName : ''), '', true);
    }

    loggingConfiguration.appenders = {};
    loggingConfiguration.loggers = {};

    // get the list of appenders, loggers, and levels
    loggingConfiguration.getAppenders = function () {
        return XNAT.xhr.get({
            url: restUrl('/xapi/logs/appenders', '', true),
            dataType: 'json',
            success: function (data) {
                loggingConfiguration.appenders = {};
                loggingConfiguration.loggers = {};
                // refresh the appenders and loggers arrays every time this function is called
                // data.forEach((appender) => { console.log(appender.name + ", " + appender.path); Object.entries(appender.loggers).forEach(([logger, level]) => console.log(` * ${logger}: ${level}`)); });
                data = data.sort(function(a,b){ return (a.name.toLowerCase() < b.name.toLowerCase) ? -1 : 1 })
                data.forEach((appender) => {
                    const name = appender.name;
                    loggingConfiguration.appenders[name] = {};
                    loggingConfiguration.appenders[name].name = name.toLowerCase();
                    loggingConfiguration.appenders[name].path = appender.path.replace(/^file:\/\//, '');
                    loggingConfiguration.appenders[name].loggers = appender.loggers;
                    Object.entries(appender.loggers).forEach(([logger, level]) => loggingConfiguration.loggers[logger] = level);
                });
            }
        });
    };

    loggingConfiguration.getLevelOptions = function (logger, level) {
        const levelOptions = document.createElement("select");
        levelOptions.id = logger.replaceAll(".", "_") + "_level_select";
        for (const [index, value] of LEVELS.entries()) {
            levelOptions.options[index] = new Option(value, value, index === 0, value === level);
        }
        levelOptions.onchange = function () {
            const newLevel = this.options[this.selectedIndex].value;
            XNAT.xhr.put({
                url: loggingConfigUrl(`${logger}/${newLevel}`, true),
                dataType: 'text',
                success: function () {
                    XNAT.ui.banner.top(2000, `Logger ${logger} level changed from ${level} to ${newLevel}.`, 'success');
                },
                error: function (e, f, g) {
                    XNAT.dialog.message(false, ['<b>Error:</b>', e, f, g].join('<br>'));
                }
            });
        }
        return levelOptions;
    }

    // create table for loggers
    loggingConfiguration.table = function (container, callback) {

        // initialize the table - we'll add to it below
        const loggerTable = XNAT.table({
            className: 'logging-configuration xnat-table data-table',
            style: {
                width: '100%',
                marginTop: '15px',
                marginBottom: '15px'
            }
        });

        // add table header row
        loggerTable.thead().tr()
            .th({addClass: 'left', html: '<b>Log File (Appender)</b>'})
            .th({addClass: 'left', html: '<b>Logger</b>'})
            .th({addClass: 'center', html: '<b>Set Level</b>'});
        loggerTable.tr({addClass: 'filter'})
            .td([ spawn('select#logtableFilterAppender.filterInput',
                { data: { key: 'appender' }},
                [ spawn('option',{ html: 'Select a Log File', value: '' }) ])
            ])
            .td([ spawn('input#logTableFilterLogger.filterInput',
                { data: { key: 'logger' }},
                { placeholder: 'Filter on logger name', style: { padding: '2px'}})
            ])
            .td();

        // start table body
        loggerTable.tbody();

        loggingConfiguration.getAppenders().done(function () {
            const data = loggingConfiguration.appenders;
            Object.entries(data).sort(function(a,b){ return (a[1].path.toLowerCase() < b[1].path.toLowerCase()) ? -1 : 1 }).forEach(([index, appender]) => {
                const appenderName = appender.name;
                const path = (appender.path || 'No file-based logs found');
                const downloadPath = loggingDownloadUrl(path);
                Object.entries(appender.loggers).forEach(([logger, level]) => {
                    loggerTable.tr({data:{ "logger": logger,"appender": appenderName, "logfile": appender.path }})
                        .td({ addClass: 'appender'}, [appender.path ? `<a href='#!' class='download-logfile' data-url='${downloadPath}' style="text-decoration: underline dotted #808080; font-weight: bold" title='Download ${appender.path}' download='${path}'>${appender.path}</a> (${appenderName})` : appenderName])
                        .td({ addClass: 'logger' }, [logger])
                        .td({addClass: 'center'}, [loggingConfiguration.getLevelOptions(logger, level)])
                });
                document.getElementById('logtableFilterAppender').append(spawn('option',{ value: appenderName, html: appender.path }));

                if (container) {
                    $$(container).append(loggerTable.table);
                }

                if (isFunction(callback)) {
                    callback(loggerTable.table);
                }
            });
        });

        loggingConfiguration.$table = $(loggerTable.table);

        return loggerTable.table;
    };

    loggingConfiguration.filterTable = function(){
        const $tableBodyRows = $('table.logging-configuration').find('tbody').find('tr');
        const $filters =  $('table.logging-configuration').find('.filterInput');
        let terms = [];
        $filters.each(function(){
            let term = ($(this)[0].nodeName === 'SELECT') ?
                $(this).find('option:selected').val() :
                $(this).val();
            if (term.length) { terms.push( { 'key': $(this).data('key'), 'term': term })}
        })

        if (!terms.length) {
            $tableBodyRows.removeClass('hidden');
            return false;
        }
        $tableBodyRows.each(function(){
            let thisRow = $(this), showThisRow = true;
            terms.forEach(function(t){
                const key = t.key, term = t.term;
                if (thisRow.data(key).toLowerCase().indexOf(term.toLowerCase()) < 0) { showThisRow = false }
            });
            if (showThisRow) {
                thisRow.removeClass('hidden');
            }
            else {
                thisRow.addClass('hidden');
            }
        })
    };

    // manage clicks and actions
    $(document).on('keyup','#logTableFilterLogger',function(){
        XNAT.admin.loggingConfiguration.filterTable();
    });


    $(document).on('change','#logtableFilterAppender',function(){
        XNAT.admin.loggingConfiguration.filterTable();
    });

    $(document).on('click','.download-logfile',function(){
        const url = $(this).data('url'),
            filename = $(this).html();
        XNAT.xhr.get({
            url: url,
            success: function(){
                window.open(url);
            },
            error: function(e){
                XNAT.dialog.message('Download unsuccessful','There is no data in '+filename+' to download');
                return false;
            }
        });
    });


    loggingConfiguration.init = function (container) {
        const $manager = $$(container || 'div#logging-configuration');

        loggingConfiguration.$container = $manager;

        $manager.append(loggingConfiguration.table());
        // loggingConfiguration.table($manager);

        // add the start, stop, and 'add new' buttons at the bottom
        $manager.append(spawn('div', [['div.clear.clearfix']]));

        return {
            element: $manager[0],
            spawned: $manager[0],
            get: function () {
                return $manager[0]
            }
        };
    };

    function refreshTable() {
        loggingConfiguration.$table.remove();
        loggingConfiguration.table(null, function (table) {
            loggingConfiguration.$container.prepend(table);
        });
    }

    loggingConfiguration.refresh = refreshTable;

    loggingConfiguration.init();

    return XNAT.admin.loggingConfiguration = loggingConfiguration;
}));
