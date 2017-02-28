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

    function isElement(it){
        return it.nodeType && it.nodeType === 1;
    }

    function isFragment(it){
        return it.nodeType && it.nodeType === 11;
    }

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
        var tableHeader = true;
        var fixedHeader = false;
        var allItems = true;

        // tolerate reversed arguments or spawner element object
        if (Array.isArray(opts) || data.spawnerElement) {
            tableData = opts;
            opts = getObject(data);
        }

        // don't modify original object
        opts = cloneObject(opts);

        tableHeader = firstDefined(opts.header||undefined, tableHeader);
        fixedHeader = firstDefined(opts.fixedHeader||undefined, fixedHeader);

        // this should allow use of items: true or items: 'all'
        allItems = firstDefined(opts.items||undefined, allItems);
        allItems = allItems === 'all' || allItems === true;

        // properties for spawned <table> element
        var tableConfig = opts.table || opts.element || {};

        addClassName(tableConfig, [
            opts.className || '',
            opts.classes || '',
            tableConfig.className || '',
            tableConfig.classes || '',
            'data-table xnat-table'
        ]);

        // normalize 'sortable/sort' parameter
        opts.sortable = opts.sortable || opts.sort;

        if (opts.sortable) {
            if (opts.sortable === true || opts.sortable === 'all') {
                addClassName(tableConfig, 'sortable');
            }
            else {
                opts.sortable = opts.sortable.split(',').map(function(item){return item.trim()});
            }
        }

        tableConfig = extend(true, {
            id: opts.id || randomID('xdt-', false),
            style: { width: opts.width || '100%' }
        }, tableConfig);

        // initialize the table
        var newTable = new Table(tableConfig);
        var $table = newTable.$table;
        var $dataRows = [];
        var dataRows = [];

        var $tableContainer = opts.container ? $$(opts.container) : null;

        // create a div to hold the table
        // or message (if no data or error)
        var $tableWrapper = $.spawn('div.data-table-wrapper');
        var tableWrapper = $tableWrapper[0];

        $tableWrapper.append('<p class="loading">loading...</p>', newTable.table);

        // if (opts.before) {
        //     $tableWrapper.prepend(opts.before);
        // }

        // add the table
        // $tableWrapper.append(newTable.table);

        // if (opts.after) {
        //     $tableWrapper.append(opts.after);
        // }

        function createTableHeader(items, fixed){

        }

        function adjustCellWidths(source, target, other){
            $$(source).each(function(i){
                var source$ = this;
                var target$ = target[i];
                // var other$ = other[i];
                var sourceCellWidth = source$.offsetWidth;
                var targetCellWidth = target$.offsetWidth;
                if (sourceCellWidth > targetCellWidth) {
                    target$.style.width = sourceCellWidth + 'px';
                    // other$.style.width = sourceCellWidth + 'px';
                }
            });
        }

        function normalizeTableCells(table){
            // $(table).each(function(){

                var table$ = $(this);
                var headerRow$ = table$.find('thead').first();
                var bodyRow$ = table$.find('tbody').first();
                // var footerRow$ = table$.find('tfoot').first();

                var headerCells$ = headerRow$.find('> th');
                var bodyCells$ = bodyRow$.find('> td');
                // var footerCells$ = footerRow$.find('> div');

                //var colCount = headerCells$.length;
                //var minWidth = this.offsetWidth/colCount;

                // set min-width to 100/colCount %
                // should be able to just apply this to the header
                //headerCells$.css('width', minWidth + 'px');

                adjustCellWidths(headerCells$, bodyCells$);
                adjustCellWidths(bodyCells$, headerCells$);

                // match the body cells with the header cells
                // adjustCellWidths(headerCells$, bodyCells$, footerCells$);

                // go back and match the header cells with the body cells
                // adjustCellWidths(footerCells$, headerCells$, bodyCells$);

                // one more pass to make sure everything's lined up
                // adjustCellWidths(bodyCells$, footerCells$, headerCells$);

                table$.find('> .loading').addClass('hidden');
                table$.find('> .invisible').removeClass('invisible');

            // });
        }

        function createTable(rows){

            var props = [], objRows = [],
                DATAREGEX = /^(~data)/,
                HIDDENREGEX = /^(~!)/,
                hiddenItems = [],
                filterColumns = [],
                customFilters = {};

            // xmodal.loading.closeAll();
            // xmodal.loading.open();

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
            if (allItems !== true && (opts.items || opts.properties)) {

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
                            newTable.last.th.innerHTML = name;
                            addClassName(newTable.last.th, 'hidden');
                            addDataAttrs(newTable.last.th, { prop: name });
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

                var filterInputs = []; // save reference to filter inputs
                var allFilterValues = '';

                newTable.tr({ className: 'filter' });

                function cacheRows(){
                    // if (!$dataRows.length || $dataRows.length != dataRows.length) {
                    //     $dataRows = dataRows.length ? $(dataRows) : $tableWrapper.find('.table-body').find('tr');
                    // }
                    $dataRows =
                            $dataRows.length ?
                                    $dataRows :
                                    ($tableContainer||$tableWrapper).find('.table-body').find('tr');
                    return $dataRows;
                }

                function filterRows(val, name){
                    if (!val) { return false }
                    val = val.toLowerCase();
                    // save the rows if there are none
                    cacheRows();
                    // var rowStuff = [];
                    // $dataRows.each(function(){
                    //     rowStuff.push({
                    //         row: this,
                    //         args: arguments
                    //     })
                    // });
                    // console.log(rowStuff);
                    $dataRows.not(function(){
                        return $(this).find('td.'+ name + ':containsNC(' + val + ')').length
                    }).hide();
                }

                props.forEach(function(name){

                    var tdElement = opts.items && opts.items[name] ? cloneObject(opts.items[name].th) || {} : {},
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
                                placeholder: 'Filter ' + (opts.items[name].label ? ('by ' + opts.items[name].label) : '')
                            });
                            filterInputs.push($filterInput);

                            $filterInput.on('focus', function(){
                                $(this).select();
                                // clear all filters on focus
                                //$table.find('input.filter-data').val('');
                                // save reference to the data rows on focus
                                // (should make filtering slightly faster)
                                // $dataRows = $table.find('tr[data-filter]');
                                cacheRows();
                            });

                            $filterInput.on('keyup', function(e){
                                var val = this.value;
                                var key = e.which;
                                // don't do anything on 'tab' keyup
                                if (key == 9) return false;
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

                            // $filterInput.on('keyup', function(e){
                            //     var val = this.value;
                            //     var key = e.which;
                            //     // don't do anything on 'tab' keyup
                            //     if (key == 9) return false;
                            //     if (key == 27){ // key 27 = 'esc'
                            //         this.value = val = '';
                            //     }
                            //     allFilterValues = filterInputs.map(function(filter){
                            //         return filter[0].value || '';
                            //     }).join('').trim();
                            //     if (!allFilterValues) {
                            //         $dataRows.show();
                            //     }
                            //     // no value, no filter
                            //     //if (!val) return false;
                            //     filterRows(val, name);
                            // });

                            tdContent.push($filterInput[0]);
                        }
                    }

                    newTable.td(tdElement, tdContent);

                });
            }

            // set body: false to create a body-less table
            // (intended for use on fixed header tables)
            if (firstDefined(opts.body||undefined, false)) {
                return newTable;
            }

            // create the <tbody>
            newTable.tbody({ className: 'table-body' });

            rows.forEach(function(item){

                // set static properties for each <tr>
                // using a 'tr' (SINGULAR) property name

                newTable.tr(opts.tr||{});

                // apply 'trs' (PLURAL) property (function), if present
                // (should return an element config object)
                // trs: function(tr, data){
                //     tr.id = data.username + '-' + data.id
                // }
                if (isFunction(opts.trs)) {
                    opts.trs(newTable.last.tr, item)
                }

                // cache each row
                dataRows.push(newTable.last.tr);

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
                    var dataAttrs = {};
                    var _tr = newTable.last.tr;
                    var applyFn = null;

                    if (filterColumns.length) {
                        dataAttrs.filter = '';
                    }

                    if (opts.items) {
                        cellObj = opts.items[name] || {};
                        if (typeof cellObj === 'string') {
                            // set item label to '~data' to add as a
                            // [data-*] attribute to the <tr>
                            if (DATAREGEX.test(cellObj)) {
                                var dataName = cellObj.split(/[.-]/).slice(1).join('-') || name;
                                var dataObj = {};
                                dataObj[dataName] = itemVal;
                                addDataAttrs(newTable.last.tr, dataObj);
                                // newTable.last$('tr').dataAttr(dataName, itemVal);
                                // dataAttrs[dataName] = itemVal;
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
                                applyFn = cellObj['call'] || cellObj['apply'];
                                if (isFunction(applyFn)) {
                                    itemVal = applyFn.apply(item, [].concat(itemVal, _tr)) || itemVal;
                                }
                                else if (stringable(applyFn)) {
                                    applyFn = (applyFn+'').trim();
                                    // wrap eval() expression in {( expr )}
                                    if (/^(\{\()(.*?)(\)})$/.test(applyFn)) {
                                        applyFn = applyFn.replace(/^(\{\(\s*)/g, '(')
                                                         .replace(/(\s*\)})$/g, ')');
                                        itemVal = eval(applyFn).apply(item, [].concat(itemVal, _tr)) || itemVal;
                                    }
                                    else if (applyFn = lookupObjectValue(window, applyFn)) {
                                        //          ^^^ correct, we're doing assignment in an 'if' statement
                                        if (isFunction(applyFn)) {
                                            itemVal = applyFn.apply(item, [].concat(itemVal, _tr)) || itemVal;
                                        }
                                        else {
                                            itemVal = applyFn;
                                        }
                                    }
                                    else {
                                        itemVal = applyFn;
                                    }
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

                    // addDataAttrs(_tr, dataAttrs);

                    newTable.td(tdElement);

                    // var $td = newTable.last$('td').empty().append(cellContent);

                    // var $td = newTable.last$('td');
                    var _td = newTable.last.td;

                    function appendContent(content){
                        if (stringable(content)) {
                            _td.innerHTML = content + '';
                            // return 'innerHTML';
                        }
                        if (isElement(content) || isFragment(content)) {
                            _td.appendChild(content);
                            // return 'appendChild';
                        }
                        if (isArray(content)) {
                            forEach(content, function(_content){
                                appendContent(_content);
                            });
                            // return 'array';
                        }
                        // console.log('cannot append?');
                        // return 'cannot append?';
                    }

                    if (isArray(cellContent)) {
                        forEach(cellContent, function(_content){
                            appendContent(_content);
                        })
                    }
                    else {
                        appendContent(cellContent);
                        // console.log(appendContent(cellContent));
                    }
                    //
                    // $td.append(cellContent);

                    // evaluate jQuery methods
                    if (cellObj.$) {
                        var $td = $(_td);
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
                        addClassName(_td, 'hidden');
                        // $td.addClass('hidden');
                    }

                });

            });

            $tableWrapper.find('.loading').remove();
            newTable.table$.removeClass('hidden invisible').show();

            // close any 'loading' dialogs that are open
            $(function(){
                if (xmodal && xmodal.loading && xmodal.loading.closeAll) {
                    xmodal.loading.closeAll();
                }
            });

        }

        function showMessage(){
            tableWrapper.innerHTML = '';
            return {
                noData: function(msg){
                    tableWrapper.innerHTML = '' +
                        '<div class="no-data">' +
                        (msg || 'Data not available.') +
                        '</div>';
                },
                error: function(msg, error){
                    tableWrapper.innerHTML = '' +
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

        var loadUrl = opts.load || opts.url;

        // request data for table rows
        if (loadUrl) {
            // use cached data if available
            tableData = XNAT.data[loadUrl];
            if (tableData && tableData.length) {
                createTable(tableData);
            }
            else {
                XNAT.xhr.get({
                    url: XNAT.url.rootUrl(loadUrl),
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
        }
        else {
            createTable(opts.data||tableData.data||tableData);
            // newTable.init(tableData);
        }

        // save a reference to generated rows for
        // (hopefully) better performance when filtering
        // $dataRows = $table.find('tr.filter');

        if (opts.container) {
            $tableContainer.append(tableWrapper);
        }

        // add properties for Spawner compatibility
        // newTable.element = newTable.spawned = tableWrapper;
        // newTable.get = function(){
        //     return tableWrapper;
        // };

        return {
            dataTable: newTable,
            table: newTable.table,
            element: tableWrapper,
            spawned: tableWrapper,
            get: function(){
                normalizeTableCells.call(tableWrapper);
                return tableWrapper;
            },
            // render: newTable.render
            render: function(container, empty, callback){
                var $container = $$(container);
                normalizeTableCells.call(tableWrapper);
                if (empty) {
                    $container.empty();
                }
                $container.append(tableWrapper);
                if (isFunction(callback)) {
                    callback.call(this, newTable);
                }
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
