/*!
 * Methods for creating XNAT-specific <table> elements
 */

var XNAT = getObject(XNAT||{});

(function(XNAT, $){

    var table,
        element = spawn.element,
        undefined;

    /**
     * Constructor function for XNAT.table()
     * @param [opts] {Object} < table > Element attributes
     * @param [config] {Object} other config options
     * @constructor
     */
    function Table(opts, config){

        this.opts = opts||{};
        this.config = config||null;

        this.table = element('table', this.opts);
        this.table$ = $(this.table);

        this.last = {};

        // 'parent' gets reset on return of chained methods
        this.last.parent = this.table;

        // get 'last' item wrapped in jQuery
        this.last$ = function(el){
            return $(this.last[el||'parent']);
        };

        this.setLast = function(el){
            this.last.parent =
                this.last[el.tagName.toLowerCase()] =
                    el;
        };

        this._rows = [];
        this.cols = this.columns = [];

        // try to init?
        if (config && config.data){
            this.init(config.data);
        }
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
    Table.p.td = function(content, opts){
        var td = element('td', opts||content, content);
        this.last.tr.appendChild(td);
        //this.setLast(td);
        return this;
    };

    Table.p.th = function(content, opts){
        var th = element('th', opts||content, content);
        this.last.tr.appendChild(th);
        //this.setLast(th);
        return this;
    };

    Table.p.tr = function(data, opts){
        var tr = element('tr', opts);
        //data = data || this.data || null;
        if (data){
            [].concat(data).forEach(function(item){
                tr.appendChild(element('td', item))
            });
        }
        // only add <tr> elements to <table>, <thead>, <tbody>, and <tfoot>
        if (/(table|thead|tbody|tfoot)/.test(this.last.parent.tagName.toLowerCase())){
            this.last.parent.appendChild(tr);
        }
        this.last.tr = tr;
        //this.setLast(tr);
        return this;
    };

    // create a row with <tr> and <td> elements
    // in the <tbody>
    Table.p.row = function(data, opts){
        var tr = element('tr', opts);
        data = data || [];
        [].concat(data).forEach(function(item){
            tr.appendChild(element('td', item));
        });
        (this.last.tbody||this.table).appendChild(tr);
        return this;
    };

    // create *multiple* <td> elements
    Table.p.tds = function(items, opts){
        var last_tr = this.last.tr;
        [].concat(items).forEach(function(item){
            var td;
            if (isPlainObject(item)){
                td = element('td', '', extend(true, item, opts));
            }
            else {
                td = element('td', item, opts);
            }
            last_tr.appendChild(td);
        });
        // don't reset 'last' so we
        // keep using the parent <tr>
        return this;
    };

    Table.p.rows = function(data, opts){
        var _this = this,
            rows = [];
        data = data||[];
        data.forEach(function(row){
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
        this.setLast(this.last.tbody||this.table);
        return this;
    };

    // reset last.parent to <thead>
    Table.p.toHead = Table.p.closestBody = function(){
        this.setLast(this.last.thead||this.table);
        return this;
    };

    Table.p.bodyRow = function(){
        this.toBody();
        this.tr();
        return this;
    };

    // add a SINGLE row of data
    Table.p.addRow = function(data){
        var _this = this;
        this.tr();
        [].concat(data).forEach(function(item){
            // could be an array of arrays
            _this.td(item);
        });
        return this;
    };

    // add multiple rows of data?
    Table.p.appendBody = function(data){
        var _this = this;
        [].concat(data).forEach(function(row){
            _this.toBody();
            _this.addRow(row);
        });
        return this;
    };

    Table.p.get = function(){
        return this.table;
    };

    Table.p.get$ = function(){
        return $(this.table);
    };

    /**
     * Populate table with data
     * @param data {Array} array of row arrays
     * @returns {Table.p} Table.prototype
     */
    Table.p.init = function(data){

        var _this = this,
            obj = {},
            header,
            cols = 0;

        // don't init twice
        if (this.inited) { return this }

        data = data || [];

        if (Array.isArray(data)){
            obj.data = data;
        }
        else {
            obj = data||{};
        }
        if (obj.header){
            // if there's a 'header' property
            // set to true, pick the header from
            // the first row of data
            if (obj.header === true){
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
        cols = (header) ? header.length : (obj.data[0]||[]).length;

        // add the header
        if (header){
            this.thead();
            this.tr();
            [].concat(header).forEach(function(item){
                _this.th(item);
            });
        }

        // always add <tbody> element on .init()
        this.tbody();

        [].concat(obj.data||[]).forEach(function(col){
            var i = -1;
            // make a row!
            _this.tr();
            // don't exceed column width of header or first column
            while (++i < cols){
                _this.td(col[i]);
            }
        });

        this.inited = true;

        return this;

    };
    
    Table.p.render = function(element){
        if (element){
            $$(element).empty().append(this.table);
        }
        return this.table;
    };

    Table.p.html = function(){
        return this.table.outerHTML;
    };

    table = function(data, opts){
        if (!opts){
            opts = data;
            data = [];
        }
        return new Table(data, opts);
    };
    
    // helper for future XNAT DataTable widget
    table.dataTable = function(data, opts){
            
    };

    // table with <input> elements in the cells
    table.inputTable = function(data, opts){
        data = data.map(function(row){
            return row.map(function(cell){
                if (/string|number/.test(typeof cell)){
                    return cell+''
                }
                if (Array.isArray(cell)){
                    return element('input', extend(true, {}, cell[2], {
                        name: cell[0],
                        value: cell[1],
                        data: { value: cell[1] }
                    }));
                }
                cell = extend(true, cell, {
                    data: { value: cell.value }
                });
                return element('input', cell);
            });
        });
        opts = getObject(opts);
        opts.className = 'input-table';
        var table = new Table(opts);
        return table.init(data);
    };

    XNAT.ui = getObject(XNAT.ui);
    XNAT.ui.table = XNAT.table = table;
    XNAT.ui.inputTable = XNAT.inputTable = table.inputTable;

})(XNAT, jQuery);
