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
        return it && it.nodeType && it.nodeType === 1;
    }

    function isFragment(it){
        return it && it.nodeType && it.nodeType === 11;
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

    Table.p.filterRows = function(colIndex, filter){

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

    // set this.body to <tbody> element
    // (at specified index if more than one is present)
    Table.p.getBody = function(idx){
        // https://en.wikipedia.org/wiki/Body_Count
        var bodyCount = this.table.tBodies.length;
        var lastBody  = bodyCount ? bodyCount-1 : 0;
        var bodyIndex = idx !== undefined ? idx : lastBody;
        this.body = this.table.tBodies[bodyIndex] || null;
        return this;
    };

    // return a real array of <tr> elements
    // var newTable = XNAT.table().init(data);
    // var tableRows = newTable.getBody().getRows();
    Table.p.getRows = function(start, end){
        this.bodyRows = [];
        if (this.body === undefined) {
            this.getBody();
        }
        if (!this.body) {
            this.bodyRows = toArray(this.body.rows);
        }
        if (this.bodyRows.length) {
            if (firstDefined(start, end, null)) {
                start = start || 0;
                end = end || this.body.rows.length-1;
                this.bodyRows = this.bodyRows.slice(start, end);
            }
        }
        return this;
    };

    // return a 2-D array of cell contents;
    // if rowIndex is specified, get just that row
    Table.p.getCells = function(rowIndex){
        this.cells = [];
        if (this.bodyRows === undefined) {
            this.getRows();
        }
        if (rowIndex !== undefined) {

        }
        this.cells = this.bodyRows[rowIndex||0].map(function(row){
            return row;
        });
    };

    // return a 2-D array of all cells' HTML content
    // EXCLUDING header and footer rows
    Table.p.getCellContent = function(tbodyIndex){
        var tbodyi  = tbodyIndex || 0;
        var rows = this.table.tBodies[tbodyi].rows;
        var rowLen  = rows.length;
        var rowData = new Array(rowLen);
        var curRow;
        var rowi = -1;
        var coli = -1;
        var colLen, colData;
        while (++rowi < rowLen){
            curRow = rows[rowi];

        }
        forEach(this.table.tBodies[tbodyi].rows, function(tbody){

        });
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

        // allow 'items' or 'columns' or 'properties'
        // to specify property names for column items   [*L*]
        opts.items = opts.items || opts.columns || opts.properties || undefined;

        // this should allow use of items: true or items: 'all'
        allItems = firstDefined(opts.items, allItems);
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
            if (typeof opts.sortAjax === 'function' || typeof opts.sortAndFilterAjax === 'function') {
                // Skip jquery sort, we'll add our own.
                // Perhaps better to "overload" utils.js > sortTableToo ??
                addClassName(tableConfig, 'sort-ready');
            }
        }

        tableConfig = extend(true, {
            id: opts.id || randomID('xdt', false),
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
        var $tableWrapper = $.spawn('div.data-table-wrapper', {
            style: {
                // apply height to wrapper for scrolling
                height: opts.height || 'auto',
                minHeight: opts.minHeight || 'auto',
                maxHeight: opts.maxHeight || 'auto',
                width:  opts.width || '100%',
                'overflow-y': 'auto'
            }
        });
        var tableWrapper = $tableWrapper[0];

        if (opts.fixed === true) {
            $tableWrapper.addClass('fixed');
        }

        if (opts.body === false) {
            $tableWrapper.addClass('no-body');
            $table.addClass('no-body');
        }

        if (opts.header === false) {
            $tableWrapper.addClass('no-header');
            $table.addClass('no-header');
        }

        if (opts.footer === false) {
            $tableWrapper.addClass('no-footer');
            $table.addClass('no-footer');
        }

        $tableWrapper.addClass('loading')
                     .append('<p class="loading">loading...</p>', newTable.table);

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

                // var headerCells$ = headerRow$.find('> th');
                // var bodyCells$ = bodyRow$.find('> td');
                var headerCells$ = headerRow$.find('tr').first().find('th');
                var bodyCells$ = bodyRow$.find('tr').first().find('td');
                // var footerCells$ = footerRow$.find('> div');

                //var colCount = headerCells$.length;
                //var minWidth = this.offsetWidth/colCount;

                // set min-width to 100/colCount %
                // should be able to just apply this to the header
                //headerCells$.css('width', minWidth + 'px');

                // adjustCellWidths(headerCells$, bodyCells$);
                // adjustCellWidths(bodyCells$, headerCells$);

                // match the body cells with the header cells
                // adjustCellWidths(headerCells$, bodyCells$, footerCells$);

                // go back and match the header cells with the body cells
                // adjustCellWidths(footerCells$, headerCells$, bodyCells$);

                // one more pass to make sure everything's lined up
                // adjustCellWidths(bodyCells$, footerCells$, headerCells$);

                table$.find('> .loading').hidden(true);
                table$.find('> .invisible').removeClass('invisible');

            // });
        }

        function appendContent(container, content){
            if (stringable(content)) {
                container.innerHTML = content + '';
                // return 'innerHTML';
            }
            if (isElement(content) || isFragment(content)) {
                container.appendChild(content);
                // return 'appendChild';
            }
            if (isArray(content)) {
                forEach(content, function(_content){
                    appendContent(container, _content);
                });
                // return 'array';
            }
            // console.log('cannot append?');
            // return 'cannot append?';
        }

        function createTable(rows){

            var props = [], objRows = [],
                LOOKUPREGEX = XNAT.parse.REGEX.lookupPrefix,
                EVALREGEX = XNAT.parse.REGEX.evalPrefix,
                DATAREGEX = /^(~data)/,
                HIDDENREGEX = /^(~!)/,
                hiddenItems = [],
                filterColumns = [],
                customFilters = {};

            // xmodal.loading.closeAll();
            // xmodal.loading.open();

            // handle 'rows' as a string for lookup or eval
            if (isString(rows)){
                if (LOOKUPREGEX.test(rows) || EVALREGEX.test(rows)){
                    XNAT.parse(rows).done(function(result){
                        rows = result;
                    });
                }
            }

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

            if (!Array.isArray(rows)) {
                rows = [].concat(rows);
            }

            // create <thead> element (it's ok if it's empty)
            newTable.thead({ style: { position: 'relative' } });

            // create header row
            if (allItems !== true && opts.items) {

                // if 'val' is a string, it's the text for the <th>
                // if it's an object, get the 'label' property
                //var label = stringable(val) ? val+'' : val.label;
                forOwn(opts.items, function(name, val){
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
                    forOwn(opts.items, function(name, val){

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
                            newTable.last.th.id = 'sort-by-' + name;
                            newTable.last.th.appendChild(spawn('i.arrows', '&nbsp;'));

                            if (typeof opts.sortAjax === 'function' || typeof opts.sortAndFilterAjax === 'function') {
                                var sortFn = opts.sortAjax;
                                if (typeof opts.sortAndFilterAjax === 'function') {
                                    sortFn = function(sortCol, sortDir){
                                        opts.sortAndFilterAjax.call(newTable, "sort", sortCol, sortDir)
                                    }
                                }
                                $(newTable.last.th).on('click', function() {
                                    // Coped from utils.js tableSort>click.sort function
                                    var $th = $(this),
                                        sorted = /asc|desc/i.test(this.className),
                                        sortClass = 'asc';

                                    if (sorted) {
                                        // if already sorted, switch to descending order
                                        if (/asc/i.test(this.className)) {
                                            sortClass = 'desc';
                                        } else {
                                            sortClass = '';
                                        }
                                    }

                                    // only modify cells in the same row, don't allow secondary sort
                                    $th.closest('tr').find('th.sort').removeClass('asc desc');

                                    if (sortClass) {
                                        $th.addClass(sortClass);
                                        sortFn.call(newTable, name, sortClass);
                                    }
                                });
                            }
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

                // add css rules for hiding filtered items
                document.head.appendChild(spawn('style|type=text/css', filterColumns.map(function(filtername){
                    return 'tr.filter-' + filtername + '{display:none;}';
                })));

                var filterInputs = []; // save reference to filter inputs
                var allFilterValues = '';

                newTable.tr({ className: 'filter' });

                function cacheRows(){
                    if (!$dataRows.length || $dataRows.length != dataRows.length) {
                        $dataRows = dataRows.length ?
                            $(dataRows) :
                            ($tableContainer||$tableWrapper).find('.table-body').find('tr');
                    }
                    return $dataRows;
                }

                function filterRows(val, name){
                    if (!val) { return false }
                    val = val.toLowerCase();
                    var filterClass = 'filter-' + name;
                    // cache the rows if not cached yet
                    cacheRows();
                    $dataRows.addClass(filterClass).filter(function(){
                        return $(this).find('td.' + name).containsNC(val).length
                    }).removeClass(filterClass);
                }

                props.forEach(function(name){

                    var tdElement = opts.items && opts.items[name] ? cloneObject(opts.items[name].th) || {} : {},
                        $filterInput = '',
                        $filterSubmit = '',
                        tdContent = [];

                    // don't create a <td> for hidden items
                    if (hiddenItems.indexOf(name) > -1) {
                        return;
                    }

                    if (filterColumns.indexOf(name) > -1){
                        tdElement.className = 'filter ' + name;
                        tdElement.id = 'filter-by-' + name;

                        if (typeof customFilters[name] === 'function'){
                            tdContent.push(customFilters[name].call(newTable, newTable.table));
                        }
                        else {

                            // TODO: move filtering functionality to work for ANY table, not just spawned XNAT.ui.dataTable() tables

                            $filterInput = $.spawn('input.filter-data', {
                                type: 'text',
                                title: 'Use the enter key to filter on ' + name,
                                placeholder: 'Filter ' + (opts.items[name].label ? ('by ' + opts.items[name].label) : ''),
                                style: 'width: 100%;',
                            });
                            $filterSubmit = $.spawn(
                                'div.filter-submit',
                                '<i class="fa fa-arrow-right"></i>'
                            );
                            filterInputs.push($filterInput);

                            if (typeof opts.filterAjax === 'function' || typeof opts.sortAndFilterAjax === 'function') {
                                $filterInput.on('focus', function(){
                                    $(this).select();
                                });

                                var filterFn = opts.filterAjax;
                                if (typeof opts.sortAndFilterAjax === 'function') {
                                    filterFn = function(newTable, fname, fval) {
                                        opts.sortAndFilterAjax.call(newTable, "filter", fname, fval)
                                    };
                                }

                                $filterSubmit.on('click',function(e){
                                    // e.preventDefault();
                                    var val = $(this).parents('td').find('.filter-data').val();
                                    filterFn.call(newTable, name, val);
                                });

                                $filterInput.on('keyup',function(e){
                                    var val = this.value;
                                    var inputWidth = $(this).width();

                                    // // hide submit icon if long text is entered
                                    // if (val.length > inputWidth/8) {
                                    //     $filterInput.addClass('truncated')
                                    // }
                                    // else {
                                    //     $filterInput.removeClass('truncated')
                                    // }

                                    // start hiding submit icon as user types
                                    var $inputSubmit = $(this).next('.filter-submit');
                                    var opacity = Math.max(0.2, (1.0 - (val.length / (inputWidth/12))));
                                    $inputSubmit.css('opacity',opacity);

                                    if (e.key === 'Enter' || e.keyCode === '13') {
                                        filterFn.call(newTable, name, val);
                                    }
                                })

                                // $filterInput.on('keyup', function(){
                                //     var val = this.value;
                                //     setTimeout(function() {
                                //         filterFn.call(newTable, name, val);
                                //     }, 500);
                                // });
                            }
                            else {

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
                                    var inputWidth = $(this).width();

                                    // // hide submit icon if long text is entered
                                    // if (val.length > inputWidth/12) {
                                    //     $filterInput.addClass('truncated')
                                    // }
                                    // else {
                                    //     $filterInput.removeClass('truncated')
                                    // }

                                    // start hiding submit icon as user types
                                    var $inputSubmit = $(this).next('.filter-submit');
                                    var opacity = Math.max(0.2, (1.0 - (val.length / (inputWidth/12))));
                                    $inputSubmit.css('opacity',opacity);


                                    // don't do anything on 'tab' keyup
                                    if (key == 9) return false;
                                    if (key == 27){ // key 27 = 'esc'
                                        this.value = val = '';
                                    }
                                    if (!val || key == 8) {
                                        $dataRows.removeClass('filter-' + name);
                                    }
                                    if (!val) {
                                        // no value, no filter
                                        return false
                                    }
                                    filterRows(val, name);
                                });
                            }

                            tdContent.push($filterInput[0],$filterSubmit);
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
                        else if (typeof cellObj === 'function') {
                            cellContent = cellObj.apply(item, [].concat(itemVal, _tr)) || itemVal;
                        }
                        else {
                            if (cellObj.td || cellObj.element) {
                                extend(true, tdElement, cellObj.td || cellObj.element);
                            }
                            if (cellObj.value) {
                                if (isFunction(cellObj.value)) {
                                    // transform value using a function
                                    // value: function(VALUE, dataObject){  }
                                    itemVal = cellObj.value.apply(item, [itemVal, item]);
                                }
                                else {
                                    // or explicitly override value
                                    itemVal = cellObj.value;
                                }
                            }
                            if (cellObj.className) {
                                addClassName(tdElement, cellObj.className);
                            }
                            // transform cell data before rendering
                            if (cellObj['apply'] || cellObj['call']) {
                                applyFn = cellObj['call'] || cellObj['apply'];
                                if (isFunction(applyFn)) {
                                    itemVal = applyFn.apply(item, [].concat(itemVal, item, _tr)) || itemVal;
                                }
                                else if (stringable(applyFn)) {
                                    applyFn = (applyFn+'').trim();
                                    // strings that start with 'function' will be assumed to be a function
                                    if (/^[(]?function/.test(applyFn)) {
                                        itemVal = window[applyFn].apply(item, [].concat(itemVal, _tr)) || itemVal;
                                    }
                                    // wrap eval() expression in {( expr )} or (( expr ))
                                    else if (XNAT.parse.REGEX.evalTest.test(applyFn)) {
                                        applyFn = applyFn.replace(XNAT.parse.REGEX.evalTrim, '');
                                        itemVal = window[applyFn].apply(item, [].concat(itemVal, _tr)) || itemVal;
                                    }
                                    // or start with standard Spawner 'eval' string
                                    else if (EVALREGEX.test(applyFn)) {
                                        applyFn = applyFn.replace(EVALREGEX, '');
                                        itemVal = window[applyFn].apply(item, [].concat(itemVal, _tr)) || itemVal;
                                    }
                                    else if ((applyFn = lookupObjectValue(window, applyFn))) {
                                        //           ^^^ correct, we're doing assignment in an 'if' statement
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

                    if (isArray(cellContent)) {
                        forEach(cellContent, function(_content){
                            appendContent(_td, _content);
                        })
                    }
                    else {
                        appendContent(_td, cellContent);
                        // console.log(appendContent(cellContent));
                    }
                    //
                    // $td.append(cellContent);

                    // evaluate jQuery methods
                    if (cellObj.$) {
                        var $td = $(_td);
                        if (typeof cellObj.$ === 'string') {
                            window['$(newTable.last.td).' + (cellObj.$).trim()];
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

            $tableWrapper.removeClass('loading').find('.loading').remove();
            newTable.table$.hidden(false);

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

        var loadUrl = XNAT.url.parse(opts.load || opts.url || '');

        // request data for table rows
        if (loadUrl) {
            // use cached data if available
            tableData = XNAT.data[loadUrl];
            if (tableData && tableData.length) {
                createTable(tableData);
            }
            else {
                XNAT.xhr.get({
                    url: loadUrl,
                    dataType: opts.dataType || 'json',
                    success: function(json){
                        var DATA = json;
                        // support custom path for returned data
                        if (opts.path) {
                            DATA = lookupObjectValue(json, opts.path);
                        }
                        else {
                            // handle data returned in ResultSet.Result array
                            DATA = (json && json.ResultSet && json.ResultSet.Result) ? json.ResultSet.Result : json;
                        }
                        // make sure there's data before rendering the table
                        if (isEmpty(json)) {
                            console.log('(no data)');
                            showMessage().noData(opts.messages ? opts.messages.noData || opts.messages.empty : '')
                        }
                        else {
                            // transform data before rendering?
                            if (isFunction(opts.apply || opts.transform)) {
                                DATA = (opts.apply || opts.transform).call(opts, json);
                            }
                            createTable(DATA || json);
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

        var renderDone = false;
        var renderTime = 0;
        var INTERVAL = 10;

        var obj = {
            opts: opts,
            dataTable: newTable,
            table: newTable.table,
            element: tableWrapper,
            spawned: tableWrapper,
            wrapper: tableWrapper,
            get: function(){
                normalizeTableCells.call(tableWrapper);
                return tableWrapper;
            },
            done: function(callback){
                waitForIt(
                    INTERVAL,
                    // test
                    function(){
                        return renderDone;
                    },
                    // success callback
                    function(){
                        // do something with the table after it's created
                        if (isFunction(callback)) {
                            obj.result = callback.call(obj, newTable);
                        }
                    });
                return obj;
            },
            fail: function(n, callback){
                if (!callback){
                    callback = n;
                    n = 5000;
                }
                waitForIt(
                    INTERVAL,
                    // test
                    function(){
                        return (renderTime += INTERVAL) > (n || 30000)
                    },
                    // failure callback
                    function(){
                        callback.call(obj, newTable);
                    })
            },
            // render: newTable.render
            render: function(container, empty, callback){
                var $container = $$(container);
                normalizeTableCells.call(tableWrapper);
                // allow omission of [empty] argument
                if (arguments.length === 2){
                    if (isFunction(empty)) {
                        callback = empty;
                        empty = false;
                    }
                }
                if (empty) {
                    $container.empty();
                }
                $container.append(tableWrapper);
                if (isFunction(callback)) {
                    obj.result = callback.call(obj, newTable);
                }
                renderDone = true;
                return obj;
            }
        };

        return obj;

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
    XNAT.ui.dataTable = XNAT.dataTable = table.dataTable;
    XNAT.ui.inputTable = XNAT.inputTable = table.inputTable;

    let ajaxTable = {};

    ajaxTable.AjaxTable = function(url, tableId, tableContainerId, tableTitle, tableActionText, tableObject,
                                   tableSetupFn, firstLoadDataCallback, allLoadsDataCallback,
                                   tableReloadCallback, labelMap, hibernate = true) {
        // NOTE: your tableObject should contain th column classes that match your td column classes or else column hiding/showing won't work
        this.container = undefined;
        this.url = url;
        this.tableId = tableId;
        this.tableContainerId = tableContainerId; //must exist within a div.tab-container and have class data-table-container
        this.tableTitle = tableTitle;
        this.tableActionText = tableActionText;
        this.tableObject = tableObject;
        this.tableSetupFn = tableSetupFn || diddly;
        this.firstLoadDataCallback = firstLoadDataCallback || diddly;
        this.allLoadsDataCallback = allLoadsDataCallback || diddly;
        this.tableReloadCallback = tableReloadCallback || diddly;
        this.labelMap = labelMap;
        this.hibernate = hibernate;

        this.parseSortAndFilterParams = function(sortOrFilter, column, value) {
            // Always pull filterMap from DOM; ignore column & value
            let filters = {}, label;
            let $table = $("#" + this.tableId);
            let myTable = this;
            $table.find("tr.filter").children().each(function(){
                let value = $(this).find("input.filter-data").val();
                if (value) {
                    label = this.id.replace("filter-by-", "");
                    let column = myTable.labelMap[label].column || label;
                    if (myTable.hibernate) {
                        let op = myTable.labelMap[label].op || 'like';
                        let val = myTable.labelMap[label].type && myTable.labelMap[label].type === 'number' ?
                            parseFloat(value) : value;
                        filters[column] = {operator: op, value: val, backend: 'hibernate'};
                    } else {
                        filters[column] = {like: value, backend: 'sql_' + myTable.labelMap[label].type};
                    }
                }
            });
            if (filters.length === 0) {
                this.filters = undefined;
            } else {
                this.filters = filters;
            }

            if (sortOrFilter === 'filter') {
                // Need to determine how to sort
                let sortth = $table.find('th.sort.asc, th.sort.desc');
                if (sortth.length !== 1) {
                    $table.find('th.sort').removeClass("asc desc");
                    column = undefined;
                    value = undefined;
                } else {
                    column = sortth[0].id.replace("sort-by-", "");
                    value = (/asc/i.test(sortth[0].className)) ? "asc" : "desc";
                }
            }
            if (column) {
                column = myTable.labelMap[column].column || column;
            }
            this.sortCol = column;
            this.sortDir = value;

            //Clear old table and load new one
            this.reload();
        }.bind(this);

        this.spawnAjaxTable = function(data, style_str){
            if (style_str) {
                let before = this.tableObject.before || {};
                let filterCss = before.filterCss || {tag: 'style|type=text/css'};
                filterCss.content = style_str + (filterCss.content || '');
                before.filterCss = filterCss;
                this.tableObject.before = before;
            }
            return extend({
                kind: 'table.dataTable',
                id: this.tableId,
                data: data,
                table: {
                    classes: "clean fixed-header selectable scrollable-table",
                    style: "width: auto;"
                },
                sortAndFilterAjax: this.parseSortAndFilterParams
            }, this.tableObject);
        }.bind(this);

        function unendingScroll(myTable, $tbody) {
            if ($tbody.scrollTop() + $tbody.innerHeight() >= $tbody[0].scrollHeight) {
                myTable.load();
            }
        }

        function addScroll(myTable) {
            // Since $table is destroyed on reload, we want to rerun this with each reload
            // Unending table
            myTable.tableBody.scroll(function() {
                unendingScroll(myTable, $(this));
            });
            ajaxTable.resizeTableCols($("table#" + myTable.tableId));
            myTable.reAddScroll = false;
        }

        this.reload = function() {
            this.tableReloadCallback();
            if (this.tableBody) {
                this.tableBody.find("tr").remove();
                this.reAddScroll = true;
            }
            this.page = 0;
            this.load();
        }.bind(this);

        this.makeTable = function(title) {
            // Do we have a table yet?
            let divId = this.tableContainerId + "-content",
                reloadId = this.tableContainerId + "-reload";
            this.container = $('#' + this.tableContainerId);
            if (!this.tableBody && !this.emptyTable) {
                this.container.append([
                    $('<div class="data-table-titlerow"><h3 class="data-table-title">'+title+'</h3></div>'),
                    $('<div class="data-table-actionsrow clearfix">' +
                        '<span class="textlink-sm data-table-action">' + this.tableActionText+ '</span>' +
                        '<button class="btn btn-sm" id="' + reloadId + '">Reload</button>' +
                        '</div>'),
                    $('<div id="'+divId+'"></div>')
                ]);
                this.container.on('click', '#' + reloadId, this.reload);
            }
            return $("#" + divId);
        }.bind(this);

        this.load = function(){
            let modalId = 'ajax_table';
            // Don't rerun on concurrent scrolling
            if (this.loading) {
                return;
            } else {
                this.loading = true;
            }

            // Add loading indicator
            openModalPanel(modalId, 'Loading');

            let tableSetup = this.tableSetupFn();
            let title = this.tableTitle;
            if (tableSetup && tableSetup.title) {
                title = tableSetup.title;
            }

            let $content = this.makeTable(title);

            // Track "page" for API call
            this.page = this.page || 0;
            this.page++;

            let dataObj = {};
            if (this.sortCol) {
                dataObj['sort_col'] = this.sortCol;
            }
            if (this.sortDir) {
                dataObj['sort_dir'] = this.sortDir;
            }
            if (this.filters) {
                dataObj['filters'] = this.filters;
            }
            dataObj.page = this.page;
            if (tableSetup && tableSetup.dataObj) {
                $.extend(dataObj, tableSetup.dataObj);
            }

            // API call
            XNAT.xhr.postJSON({
                url: XNAT.url.restUrl(this.url),
                data: JSON.stringify(dataObj),
                success: function (data) {
                    let myTable = this;
                    if (!this.tableBody) {
                        // First load
                        if (data.length) {
                            if (this.emptyTable) {
                                $content.empty();
                                this.emptyTable = false;
                            }

                            let style_str = "";
                            let showHideList = $.map(this.labelMap, function(value, key) {
                                style_str += (value['show']) ? '' :  '#' + this.tableId +
                                    ' .' + key +  '{ display:none; } \n';
                                return $.spawn("span.bl-dropdown-item", {},
                                    ajaxTable.addColumnToggleContents(key, value['label'], value['show'])
                                );
                            }.bind(this));

                            ajaxTable.addColumnToggle(showHideList, this.container);

                            XNAT.spawner.spawn({
                                ajaxTable: this.spawnAjaxTable(data, style_str)
                            }).done(function () {
                                myTable.tableBody = this.get$().find("tbody.table-body");
                                this.render($content, function() {
                                    addScroll(myTable);
                                });
                            });


                            this.firstLoadDataCallback(data);
                        } else {
                            $content.html('No history to display.');
                            this.emptyTable = true;
                        }
                    } else {
                        // Next "page" of results
                        if (data.length) {
                            XNAT.spawner.spawn({
                                ajaxTable: this.spawnAjaxTable(data)
                            }).done(function () {
                                // Append only the table rows to the existing tbody
                                myTable.tableBody.append(this.get$().find("tbody.table-body").children());
                                ajaxTable.applyColumnToggle(myTable.container);
                                if (myTable.reAddScroll) {
                                    addScroll(myTable);
                                } else {
                                    ajaxTable.resizeTableCols($(myTable.tableBody).parent("table"));
                                }
                            });
                        } else {
                            // Stop trying, no more results
                            this.tableBody.off("scroll");
                        }
                    }
                    this.allLoadsDataCallback(data)
                }.bind(this),
                error: function(e) {
                    let errHtml = '<p><strong>Error ' + e.status + ': '+ e.statusText+'</strong></p><p>' + e.responseText + '</p>';
                    if (this.filters) {
                        this.filters = undefined;
                        XNAT.ui.dialog.open({
                            title: 'Invalid filter',
                            content: errHtml,
                            destroyOnClose: true,
                            buttons: [{
                                label: 'OK',
                                isDefault: true,
                                close: true
                            }]
                        });
                    } else {
                        this.emptyTable = true;
                        $content.html(errHtml || 'Issue loading content');
                    }
                }.bind(this),
                complete: function() {
                    closeModalPanel(modalId);
                    this.loading = false;
                }.bind(this)
            });
        }.bind(this);

        return this;
    };

    ajaxTable.cssToNumber = function($item, attrName) {
        let ws = $item.css(attrName) || "0";
        return Number(ws.replace(/[^\d\.]/g, ""));
    };

    ajaxTable.toggleColumn = function($container, target, show) {
        let $columns = $container.find("th." + target + ", td." + target);
        if (show) {
            $columns.show();
        } else {
            $columns.hide();
        }
    };

    ajaxTable.addColumnToggleContents = function(colClass, displayName, show) {
        let checked = (show) ? "|checked='checked'" : "";
        return [
            $.spawn("input" + checked, {
                id: "show-" + colClass,
                type: "checkbox"
            }),
            $.spawn("label|for='show-" + colClass + "'", {}, displayName)
        ];
    };

    ajaxTable.applyColumnToggle = function($container){
        let dropdown = "div.show-hide-columns-list.bl-dropdown-menu";
        $container.find(dropdown + ' input').each(function(){
            ajaxTable.toggleColumn($container, this.id.replace("show-", ""), $(this).prop("checked"));
        });
    };

    ajaxTable.addColumnToggle = function(showHideList, $container){
        // Toggle columns
        let $actionsRow = $container.find('.data-table-actionsrow');
        let button = "button.show-hide-columns";
        let dropdown = "div.show-hide-columns-list.bl-dropdown-menu";
        let $button = $actionsRow.find(button);
        if ($button.length) {
            // Just remove so we don't get duplicate actions
            $button.remove();
            $actionsRow.find(dropdown).remove();
        }
        $button = $.spawn(button, {classes: "btn btn-sm"}, ["Columns", "&nbsp;", $.spawn("i.fa.fa-caret-down")]);
        $actionsRow.append($button);
        let $dropdown = $.spawn(dropdown, {}, showHideList);
        $actionsRow.append($dropdown);

        $container.on('click', button, function () {
            if ($dropdown.css("visibility") === "visible") {
                $button.find("i").removeClass("fa-caret-up").addClass("fa-caret-down");
                $dropdown.css({
                    visibility: "hidden",
                    transform: "translate3d(0,0,0)"
                });
            } else {
                let coords = $button.offset();
                let listcoords = $dropdown.offset();
                let leftt = coords['left'] - listcoords['left'],
                    topt = coords['top'] - listcoords['top'] + ajaxTable.cssToNumber($button, "height");
                $(this).find("i").removeClass("fa-caret-down").addClass("fa-caret-up");
                $dropdown.css({
                    visibility: "visible",
                    transform: "translate3d(" + leftt + "px, " + topt + "px, 0)"
                });
            }
            return false;
        });
        $container.on('click', dropdown + ' input', function () {
            ajaxTable.toggleColumn($container, this.id.replace("show-", ""), $(this).prop("checked"));
            ajaxTable.resizeTableCols($container.find('table'),'reload');
            $button.click().click(); // keep it in view, but be sure to transform if table size changes
        });
    };

    ajaxTable.resizeTableCols = function($table,reloadFF = false){
        if (reloadFF) this.reload();
        // if (reloadFF && navigator.userAgent.toLowerCase().indexOf('firefox') > -1) this.reload();
        if ($table.is(':hidden')) {
            $table.on('nowVisible', function() {
                ajaxTable.resizeTableCols($(this),'reload');
            });
        }
        let $headerCells = $table.find("thead tr:first").children(),
            $filterCells = $table.find("thead tr:last").children(),
            $bodyCells = $table.find("tbody tr:first").children();

        // Set common width for thead & tbody cells (needed for scrollable tbody)
        let colWidths = [], pctWidths = [];
        $bodyCells.each(function (i, v) {
            // ignore any columns that have been hidden
            if ($(v).css('display') !== 'none') {
                let wid = Math.max(
                    ajaxTable.cssToNumber($(v), "width"),
                    ajaxTable.cssToNumber($($headerCells[i]), "width")
                );
                if (wid){
                    $(v).css("width", wid.toString()+'px');
                    $($headerCells[i]).css("width", wid.toString()+'px');
                    $($filterCells[i]).css("width", wid.toString()+'px');
                    colWidths.push(wid);
                }
            } else {
                colWidths.push(0);
            }
        });

        // convert pixel widths to percentages of available space
        var availableWidth = $table.parents('.data-table-wrapper').width(),
            interiorWidth;

        if (colWidths.length){
            interiorWidth = colWidths.reduce(function(interiorWidth,wid){ return interiorWidth += parseInt(wid) });
            colWidths.forEach(function(wid,i){
                var pct = (wid/interiorWidth);
                pctWidths[i] = Math.max(100, Math.floor(availableWidth * pct));
            });
        }

        $table.find("tr").each(function(rind, row) {
            $(row).children().each(function (i, v) {
                let wid = pctWidths[i];
                if (wid) $(v).css("width", wid.toString()+'px');
            });
        });
    };

    XNAT.ui.ajaxTable = XNAT.ajaxTable = ajaxTable;
}));
