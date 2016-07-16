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

    // add new element class without destroying existing class
    function addClassName(el, newClass){
        el.className = [].concat(el.className||[], newClass).join(' ').trim();
        return el.className;
    }

    // add new data object item to be used for [data-] attribute(s)
    function addDataObjects(obj, attrs){
        obj.data = obj.data || {};
        forOwn(attrs, function(name, prop){
            obj.data[name] = prop;
        });
        return obj.data;
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
                this.last.parent =
                    this.last[el.tagName.toLowerCase()] =
                        el;
            };

            this._rows = [];
            this._cols = 0; // how many columns?

        };

        this.newTable();

    }


    // alias prototype for less typing
    Table.p = Table.prototype;


    // jQuery methods we'd like to use:
    var $methods = [
        'append',
        'prepend',
        'addClass',
        'find'
    ];

    $methods.forEach(function(method){
        Table.p[method] = function(){
            var $el = this.last$();
            $el[method].apply($el, arguments);
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
        this.last.tr.appendChild(td);
        return this;
    };

    Table.p.th = function(opts, content){
        var th = element('th', opts, content);
        this.last.th = th;
        this.last.tr.appendChild(th);
        return this;
    };

    Table.p.tr = function(opts, data){
        var _this = this;
        var tr = element('tr', opts);
        //data = data || this.data || null;
        if (data) {
            this.last.tr = tr;
            [].concat(data).forEach(function(item, i){
                if (_this._cols && _this._cols > i) return;
                _this.td(item)._cols++;
            });
        }
        // only add <tr> elements to <table>, <thead>, <tbody>, and <tfoot>
        if (/(table|thead|tbody|tfoot)/i.test(this.last.parent.tagName)) {
            this.last.parent.appendChild(tr);
        }
        this.last.tr = tr;
        //this.setLast(tr);
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
            cols = data[0].length; // first array length determines how many columns
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
        this.setLast(head);
        return this;
    };

    Table.p.tbody = function(opts, data){
        var body = element('tbody', opts);
        this.table.appendChild(body);
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

    Table.p.render = function(element, empty){
        var $element;
        if (element) {
            $element = $$(element);
            if (empty){
                $element.empty();
            }
            $element.append(this.table);
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

        // tolerate reversed arguments
        if (Array.isArray(opts)){
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
            style: {
                width: opts.width || '100%'
            }
        }, opts.element);

        // initialize the table
        var newTable = new Table(opts.element);

        function createTable(rows){
            var props = [];
            if (!allItems && (opts.items || opts.properties)) {
                newTable.tr();
                forOwn(opts.items||opts.properties, function(name, val){
                    props.push(name);
                    newTable.th(val);
                    if (!opts.sortable) return;
                    if (opts.sortable === true || opts.sortable.indexOf(name) !== -1) {
                        addClassName(newTable.last.th, 'sort');
                    }
                });
            }
            else {
                if (allItems) {
                    newTable.tr();
                }
                forOwn(rows[0], function(name, val){
                    if (allItems) {
                        newTable.th(name);
                    }
                    props.push(name);
                });
            }
            rows.forEach(function(item){
                newTable.tr();
                props.forEach(function(name){
                    newTable.td({ className: name }, item[name]);
                });
            });
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
                    // handle data returned in ResultSet.Result array
                    json = (json.ResultSet && json.ResultSet.Result) ? json.ResultSet.Result : json;
                    createTable(json);
                }
            });
        }
        else {
            createTable(tableData.data||tableData);
            // newTable.init(tableData);
        }

        if (opts.container) {
            $$(opts.container).append(newTable.table);
        }

        // add properties for Spawner compatibility
        newTable.element = newTable.spawned = newTable.table;
        newTable.get = function(){
            return newTable.table;
        };

        return newTable;

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
