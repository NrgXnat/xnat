/*
 * web: table.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*!
 * Methods for creating XNAT-specific <table> elements
 */

var XNAT = getObject(XNAT);

(function(factory){

    // add dependencies to 'imports' array
    var imports = [
        'xnat/init',
        'lib/jquery/jquery'
    ];

    if (typeof define === 'function' && define.amd) {
        define(imports, factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory(XNAT, jQuery);
    }
    else {
        return factory(XNAT, jQuery);
    }

}(function(XNAT, $){

    var table,
        element = window.spawn,
        undefined;


    /**
     * Constructor function for XNAT.table()
     * @param [opts] {Object} < table > Element attributes
     * @param [config] {Object} other config options
     * @constructor
     */
    function Table(opts, config){

        this.newTable = function(o, c){
            o = o || opts || {};
            c = c || config;
            this.opts = cloneObject(o);
            this.config = c ? cloneObject(c) : null;
            this.table = element('table', this.opts);
            this.$table = this.table$ = $(this.table);

            this.last = {};

            // 'parent' gets reset on return of chained methods
            this.last.parent = this.table;

            // get 'last' item wrapped in jQuery
            this.last$ = function(el){
                return $(this.last[el || 'parent']);
            };

            this.setLast = function(el){
                this.last.parent = this.last.child =
                    this.last[el.tagName.toLowerCase()] =
                        el;
            };

            this.getLast = function(){
                return this.last.child;
            };

            this._rows = [];
            this._cols = 0; // how many columns?

        };

        this.newTable();

    }

    // alias prototype for less typing
    Table.p = Table.prototype;

    // return last item to use with jQuery methods
    // XNAT.table().tr().$('attr', ['title', 'foo']).td('Bar').$({ addClass: 'bar' }).getHTML();
    // <table><tr title="foo"><td class="bar">Bar</td></tr></table>
    // yes, the HTML is shorter and simpler, but also harder to generate programmatically
    Table.p.$ = function(method, args){
        var $el = $(this.getLast());
        var methods = isPlainObject(method) ? method : null;
        args = args || [];
        if (!methods) {
            methods = {};
            // force an object if not already
            methods[method] = args;
        }
        forOwn(methods, function(name, arg){
            $el[name].apply($el, [].concat(arg));
        });
        return this;
    };

    // jQuery methods we'd like to use:
    var $methods = [
        'append',
        'prepend',
        'addClass',
        'find'
    ];

    $methods.forEach(function(method){
        Table.p[method] = function(args){
            this.$(method, args);
            return this;
        }
    });

    // create a single <td> element
    // just using a single argument
    // if you want to modify the <td>
    // you'll need to pass a config
    // object to set the properties
    // and use append or innerHTML
    // to add the cell content
    Table.p.td = function(opts, content){
        var td = element('td', opts, content);
        this.last.td = td;
        this.last.child = td;
        this.last.tr.appendChild(td);
        return this;
    };

    Table.p.th = function(opts, content){
        var th = element('th', opts, content);
        this.last.th = th;
        this.last.child = th;
        this.last.tr.appendChild(th);
        this._cols++; // do this here?
        return this;
    };

    Table.p.tr = function(opts, data){
        var _this = this;
        var tr = element('tr', opts);
        //data = data || this.data || null;
        if (data) {
            this.last.tr = tr;
            [].concat(data).forEach(function(item, i){
                //if (_this._cols && _this._cols > i) return;
                _this.td(item);
            });
        }
        // only add <tr> elements to <table>, <thead>, <tbody>, and <tfoot>
        if (/(table|thead|tbody|tfoot)/i.test(this.last.parent.tagName)) {
            this.last.parent.appendChild(tr);
        }
        this.last.tr = tr;
        this.last.child = tr;
        // this.setLast(tr);
        // nullify last <th> and <td> elements since this is a new row
        this.last.th = this.last.td = null;
        return this;
    };

    // create a <tr> with optional <td> elements
    // in the <tbody>
    Table.p.row = Table.p.addRow = function(data, opts){
        data = data || [];
        this.tr(opts, data);
        return this;
    };

    // add a <tr> to <tbody>
    Table.p.bodyRow = function(data, opts){
        this.toBody().row(data, opts);
        return this;
    };

    // create *multiple* <td> elements
    Table.p.tds = function(items, opts){
        var _this = this;
        [].concat(items).forEach(function(item){
            if (stringable(item)) {
                _this.td(opts, item);
            }
            // if 'item' isn't stringable, it will be an object
            else {
                _this.td(extend(true, {}, opts, item));
            }
        });
        // don't reset 'last' so we
        // keep using the parent <tr>
        return this;
    };

    Table.p.rows = function(data, opts){
        var _this = this,
            rows  = [],
            cols = (data[0]||[]).length; // first array length determines how many columns
        data = data || [];
        data.forEach(function(row){
            row = row.slice(0, cols);
            rows.push(_this.tr(opts, row))
        });
        this._rows = rows;
        this.append(this._rows);
        return this;
    };

    Table.p.thead = function(opts, data){
        var head = element('thead', opts);
        this.table.appendChild(head);
        // this.last.child = head;
        this.setLast(head);
        return this;
    };

    Table.p.tfoot = function(opts, data){
        var foot = element('tfoot', opts);
        this.table.appendChild(foot);
        // this.last.child = foot;
        this.setLast(foot);
        return this;
    };

    Table.p.tbody = function(opts, data){
        var body = element('tbody', opts);
        this.table.appendChild(body);
        // this.last.child = body;
        this.setLast(body);
        return this;
    };

    // reset last.parent to <tbody>
    Table.p.toBody = Table.p.closestBody = function(){
        this.setLast(this.last.tbody || this.table);
        return this;
    };

    // reset last.parent to <thead>
    Table.p.toHead = Table.p.closestHead = function(){
        this.setLast(this.last.thead || this.table);
        return this;
    };

    // add multiple rows of data?
    Table.p.appendBody = Table.p.appendToBody = function(data){
        var _this = this;
        [].concat(data).forEach(function(row){
            _this.toBody().addRow(row);
        });
        return this;
    };

    Table.p.get = function(){
        return this.table;
    };

    Table.p.$get = Table.p.get$ = function(){
        return $(this.table);
    };

    Table.p.getHTML = Table.p.html = function(){
        return this.table.outerHTML;
    };

    /**
     * Populate table with data
     * @param data {Array} array of row arrays
     * @returns {Table.p} Table.prototype
     */
    Table.p.init = function(data){

        var _this = this,
            obj   = {},
            header,
            cols  = 0;

        // don't init twice?
        if (this.inited) {
            // run .init() again to
            // empty table and load new data
            this.table$.empty();
            //this.newTable();
            //return this
        }

        data = data || [];

        if (Array.isArray(data)) {
            obj.data = data;
        }
        else {
            obj = data || {};
        }

        if (obj.header) {
            // if there's a 'header' property
            // set to true, pick the header from
            // the first row of data
            if (obj.header === true) {
                header = obj.data.shift();
            }
            // otherwise it's set explicitly
            // as an array in the 'header' property
            // and that sets the number of columns
            else {
                header = obj.header;
            }
        }

        // set the number of columns based on
        // the header or first row of data
        cols = (header) ? header.length : (obj.data[0] || []).length;
        this._cols = cols;

        // add the header
        if (header) {
            this.thead().tr();
            [].concat(header).forEach(function(item){
                _this.th(item);
            });
        }

        // always add <tbody> element on .init()
        this.tbody();

        [].concat(obj.data || []).forEach(function(col){
            var i = -1;
            // make a row!
            _this.tr();
            // don't exceed column width of header or first column
            while (++i < cols) {
                _this.td(col[i]);
            }
        });

        this.inited = true;

        return this;

    };

    Table.p.render = function(container, empty){
        var $container;
        if (container) {
            $container = $$(container);
            if (empty){
                $container.empty();
            }
            $container.append(this.table);
        }
        return this.table;
    };

    // 'opts' are options for the <table> element
    // 'config' is for other configurable stuff
    table = function(opts, config){
        return new Table(opts, config);
    };

    // basic XNAT.dataTable widget
    table.dataTable = function(data, opts){

        var tableData = data;

        // tolerate reversed arguments or spawner element object
        if (Array.isArray(opts) || data.spawnerElement) {
            tableData = opts;
            opts = getObject(data);
        }

        // don't modify original object
        opts = cloneObject(opts);

        var allItems = opts.header || (opts.items && opts.items === 'all');

        // properties for spawned element
        opts.element = opts.element || {};

        addClassName(opts.element, 'data-table xnat-table');

        if (opts.sortable) {
            if (opts.sortable === true) {
                addClassName(opts.element, 'sortable');
            }
            else {
                opts.sortable = opts.sortable.split(',').map(function(item){return item.trim()});
            }
        }

        opts.element = extend(true, {
            id: opts.id || randomID('t', false),
            style: {
                width: opts.width || '100%',
                position: 'relative'
            }
        }, opts.element);

        // initialize the table
        var newTable = new Table(opts.element);
        var $table = newTable.$table;
        var $dataRows = [];


        // create a div to hold the table
        // or message (if no data or error)
        var $tableContainer = $.spawn('div.data-table-container', {
            style: { position: 'relative' }
        }, [newTable.table]);
        var tableContainer = $tableContainer[0];

        // if (opts.before) {
        //     $tableContainer.prepend(opts.before);
        // }

        // add the table
        // $tableContainer.append(newTable.table);

        // if (opts.after) {
        //     $tableContainer.append(opts.after);
        // }

        function createTable(rows){

            var props = [], objRows = [],
                DATAREGEX = /^(~data)/,
                HIDDENREGEX = /^(~!)/,
                hiddenItems = [],
                filterColumns = [],
                customFilters = {};

            // convert object list to array list
            if (isPlainObject(rows)) {
                forOwn(rows, function(name, stuff){
                    objRows.push(stuff);
                    // var _obj = {};
                    // _obj[name] = stuff;
                    // objRows.push(_obj);
                });
                rows = objRows; // now it's an array
            }

            // create <thead> element (it's ok if it's empty)
            newTable.thead({ style: { position: 'relative' } });

            // create header row
            if (!allItems && (opts.items || opts.properties)) {

                // if 'val' is a string, it's the text for the <th>
                // if it's an object, get the 'label' property
                //var label = stringable(val) ? val+'' : val.label;
                forOwn(opts.items||opts.properties, function(name, val){
                    props.push(name);
                    // don't create <th> for items labeled as '~data'
                    if (DATAREGEX.test(val)) {
                        hiddenItems.push(name);
                        // return;
                    }
                    // does this column have a filter field?
                    if (typeof val !== 'string' && (val.filter || (opts.filter && opts.filter.indexOf(name) > -1))){
                        filterColumns.push(name);
                        // pass a function that returns an element for a 'custom' filter
                        if (typeof val.filter === 'function'){
                            customFilters[name] = val.filter;
                        }
                    }
                });

                if (opts.header !== false) {
                    newTable.tr();
                    forOwn(opts.items||opts.properties, function(name, val){

                        if (DATAREGEX.test(val)) {
                            hiddenItems.push(name);
                            return;
                        }

                        newTable.th(extend({ html: (val.label || val)}, val.th));

                        if (HIDDENREGEX.test(val.label || val)) {
                            hiddenItems.push(name);
                            $(newTable.last.th).html(name)
                                .addClass('hidden')
                                .dataAttr('prop', name);
                            return;
                        }
                        //if (!opts.sortable) return;
                        if (val.sort || opts.sortable === true || (opts.sortable||[]).indexOf(name) !== -1) {
                            addClassName(newTable.last.th, 'sort');
                            newTable.last.th.appendChild(spawn('i', '&nbsp;'))
                        }

                    });
                }
            }
            else {
                if (allItems) {
                    newTable.tr();
                }
                forOwn(rows[0], function(name, val){
                    if (allItems) {
                        newTable.th(name);
                        if (HIDDENREGEX.test(val)) {
                            addClassName(newTable.last.th, 'hidden');
                        }
                    }
                    props.push(name);
                });
            }

            // define columns to filter, if specified
            if (typeof opts.filter === 'string') {
                opts.filter.split(',').forEach(function(item){
                    item = item.trim();
                    if (filterColumns.indexOf(item) === -1) {
                        filterColumns.push(item);
                    }
                });
            }

            // if we have filters, create a row for them
            if (filterColumns.length) {

                newTable.tr({ className: 'filter' });

                function filterRows(val, name){
                    if (!val) { return false }
                    // save the rows if there are none
                    if (!$dataRows.length) {
                        $dataRows = $table.find('tr[data-id]');
                    }
                    $dataRows.not(function(){
                        return $(this).find('td.'+ name + ':containsNC(' + val + ')').length
                    }).hide();
                }

                props.forEach(function(name){

                    var tdElement = {},
                        $filterInput = '',
                        tdContent = [];

                    // don't create a <td> for hidden items
                    if (hiddenItems.indexOf(name) > -1) {
                        return;
                    }

                    if (filterColumns.indexOf(name) > -1){
                        tdElement.className = 'filter ' + name;

                        if (typeof customFilters[name] === 'function'){
                            tdContent.push(customFilters[name].call(newTable, newTable.table));
                        }
                        else {
                            $filterInput = $.spawn('input.filter-data', {
                                type: 'text',
                                title: name + ':filter',
                                placeholder: 'filter'
                            });

                            $filterInput.on('focus', function(){
                                $(this).select();
                                // clear all filters on focus
                                //$table.find('input.filter-data').val('');
                                // save reference to the data rows on focus
                                // (should make filtering slightly faster)
                                $dataRows = $table.find('tr[data-id]');
                            });

                            $filterInput.on('keyup', function(e){
                                var val = this.value;
                                var key = e.which;
                                if (key == 27){ // key 27 = 'esc'
                                    this.value = val = '';
                                }
                                if (!val || key == 8) {
                                    $dataRows.show();
                                }
                                if (!val) {
                                    // no value, no filter
                                    return false
                                }
                                filterRows(val, name);
                            });
                            tdContent.push($filterInput[0]);
                        }
                    }

                    newTable.td(tdElement, tdContent);

                });
            }

            // create the <tbody>
            newTable.tbody();

            rows.forEach(function(item){

                newTable.tr();

                // iterate properties for each row
                props.forEach(function(name){

                    var hidden = false;
                    var _name = name.replace(/^_*/,'');
                    var itemVal = item[_name];
                    var cellObj = {};
                    var cellContent = '';
                    var tdElement = {
                        className: _name,
                        html: ''
                        // html: itemVal
                    };
                    var _tr = newTable.last.tr;

                    if (opts.items) {
                        cellObj = opts.items[name] || {};
                        if (typeof cellObj === 'string') {
                            // set item label to '~data' to add as a
                            // [data-*] attribute to the <tr>
                            if (DATAREGEX.test(cellObj)) {
                                var dataName = cellObj.split(/[.-]/).slice(1).join('-') || name;
                                newTable.last$('tr').dataAttr(dataName, itemVal);
                                return;
                            }
                            cellContent = itemVal;
                            hidden = HIDDENREGEX.test(cellObj);
                        }
                        else {
                            if (cellObj.td || cellObj.element) {
                                extend(true, tdElement, cellObj.td || cellObj.element);
                            }
                            if (cellObj.value) {
                                // explicitly override value
                                itemVal = cellObj.value;
                            }
                            if (cellObj.className) {
                                addClassName(tdElement, cellObj.className);
                            }
                            // if (cellObj.apply) {
                            //     itemVal = eval(cellObj.apply).apply(item, [itemVal]);
                            // }
                            if (cellObj['apply'] || cellObj['call']) {
                                cellObj['apply'] = cellObj['call'] || cellObj['apply'];
                                if (isFunction(cellObj['apply'])) {
                                    itemVal = cellObj['apply'].apply(item, [].concat(itemVal, _tr)) || itemVal;
                                }
                                else {
                                    itemVal = eval('('+cellObj['apply'].trim()+')').apply(item, [].concat(itemVal, _tr)) || itemVal;
                                }
                            }
                            // special __VALUE__ string gets replaced
                            cellContent = cellObj.content || cellObj.html;
                            if (isString(cellContent)) {
                                cellContent = cellContent.replace(/__VALUE__/g, itemVal);
                            }
                            else {
                                cellContent = itemVal;
                            }
                            hidden = HIDDENREGEX.test(cellObj.label);
                        }
                    }

                    newTable.td(tdElement);

                    var $td = newTable.last$('td').empty().append(cellContent);

                    // evaluate jQuery methods
                    if (cellObj.$) {
                        if (typeof cellObj.$ === 'string') {
                            eval('$(newTable.last.td).'+(cellObj.$).trim());
                        }
                        else {
                            forOwn(cellObj.$, function(method, args){
                                $td[method].apply($td, [].concat(args))
                            });
                        }
                    }

                    if (hidden) {
                        $td.addClass('hidden');
                    }

                });
            });

        }


        function showMessage(){
            tableContainer.innerHTML = '';
            return {
                noData: function(msg){
                    tableContainer.innerHTML = '' +
                        '<div class="no-data">' +
                        (msg || 'Data not available.') +
                        '</div>';
                },
                error: function(msg, error){
                    tableContainer.innerHTML = '' +
                        '<div class="error">' +
                        (msg || '') +
                        (error ? '<br><br>' + error : '') +
                        '</div>';
                }
            };
        }

        // if 'tableData' is a string, use as the url
        if (typeof tableData == 'string') {
            opts.url = tableData;
        }

        // request data for table rows
        if (opts.load || opts.url) {
            XNAT.xhr.get({
                url: XNAT.url.rootUrl(opts.load||opts.url),
                dataType: opts.dataType || 'json',
                success: function(json){
                    // support custom path for returned data
                    if (opts.path) {
                        json = lookupObjectValue(json, opts.path);
                    }
                    else {
                        // handle data returned in ResultSet.Result array
                        json = (json.ResultSet && json.ResultSet.Result) ? json.ResultSet.Result : json;
                    }
                    // make sure there's data before rendering the table
                    if (isEmpty(json)) {
                        showMessage().noData(opts.messages ? opts.messages.noData || opts.messages.empty : '')
                    }
                    else {
                        createTable(json);
                    }
                },
                error: function(obj, status, message){
                    var _msg = opts.messages ? opts.messages.error : '';
                    var _err = 'Error: ' + message;
                    showMessage().error(_msg);
                }
            });
        }
        else {
            createTable(opts.data||tableData.data||tableData);
            // newTable.init(tableData);
        }

        // save a reference to generated rows for
        // (hopefully) better performance when filtering
        $dataRows = $table.find('tr[data-id]');

        if (opts.container) {
            $$(opts.container).append(tableContainer);
        }

        // add properties for Spawner compatibility
        // newTable.element = newTable.spawned = tableContainer;
        // newTable.get = function(){
        //     return tableContainer;
        // };

        return {
            dataTable: newTable,
            table: newTable.table,
            element: tableContainer,
            spawned: tableContainer,
            get: function(){
                return tableContainer;
            }
        };

    };

    // table with <input> elements in the cells
    table.inputTable = function(data, opts){
        var tableData = data;
        // tolerate reversed arguments
        if (Array.isArray(opts)){
            tableData = opts;
            opts = data;
        }
        tableData = tableData.map(function(row){
            return row.map(function(cell){
                if (/string|number/.test(typeof cell)) {
                    return cell + ''
                }
                if (Array.isArray(cell)) {
                    return element('input', extend(true, {}, cell[2], {
                        name:  cell[0],
                        value: cell[1],
                        data:  { value: cell[1] }
                    }));
                }
                cell = extend(true, cell, {
                    data: {value: cell.value}
                });
                return element('input', cell);
            });
        });
        opts = getObject(opts);
        addClassName(opts, 'input-table');
        var newTable = new Table(opts);
        return newTable.init(tableData);
    };

    XNAT.ui = getObject(XNAT.ui||{});
    XNAT.ui.table = XNAT.table = table;
    XNAT.ui.inputTable = XNAT.inputTable = table.inputTable;

}));
