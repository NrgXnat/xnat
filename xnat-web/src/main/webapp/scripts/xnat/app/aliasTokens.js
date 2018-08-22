// "alias": "61e4f5db-33e0-4f06-86e7-a59217d65f5c",
//     "created": 1526359704410,
//     "disabled": 0,
//     "enabled": true,
//     "estimatedExpirationTime": 1526532504410,
//     "id": 6,
//     "secret": "ogHnbC7559jczcWz0Y1jDhHlUpy4AxI8EW6EjYAdArItPGf7FLXJP1OYam68MNQa",
//     "singleUse": false,
//     "timestamp": 1526359704410,
//     "validIPAddresses": [],

console.log('aliasTokens.js');

var XNAT = getObject(XNAT || {});

(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(factory);
    }
    else if (typeof exports === 'object') {
        module.exports = factory();
    }
    else {
        return factory();
    }
}(function() {

    /* ================ *
     * GLOBAL FUNCTIONS *
     * ================ */

    function spacer(width) {
        return spawn('i.spacer', {
            style: {
                display: 'inline-block',
                width: width + 'px'
            }
        })
    }

    /* ================ *
     * ALIAS TOKEN MGMT *
     * ================ */

    var aliasTokens, tokens, undef;

    XNAT.app = getObject(XNAT.app || {});
    XNAT.app.aliasTokens = aliasTokens =
        getObject(XNAT.app.aliasTokens || {});

    XNAT.app.aliasTokens.list = tokens = {};

    // open a dialog to display all the properties of a token
    aliasTokens.viewToken = function(id){
        if (XNAT.app.aliasTokens.list[id]) {
            var token = XNAT.app.aliasTokens.list[id];
            var tokenDialogButtons = [
                {
                    label: 'Close',
                    isDefault: true,
                    close: true
                },
                {
                    label: 'Validate Token',
                    close: false,
                    action: function(){
                        XNAT.app.aliasTokens.validateToken(token.alias,token.secret);
                    }
                }
            ];

            // build nice-looking history entry table
            var tokTable = XNAT.table({
                className: 'xnat-table compact',
                style: {
                    width: '100%',
                    marginTop: '15px',
                    marginBottom: '15px'
                }
            });

            // add table header row
            tokTable.tr()
                .th({ addClass: 'left', html: '<b>Attribute</b>' })
                .th({ addClass: 'left', html: '<b>Value</b>' });

            for (var key in token){
                var val = token[key], formattedVal = '';
                if (Array.isArray(val)) {
                    var items = [];
                    val.forEach(function(item){
                        if (typeof item === 'object') item = JSON.stringify(item);
                        items.push(spawn('li',[ spawn('code',item) ]));
                    });
                    formattedVal = spawn('ul',{ style: { 'list-style-type': 'none', 'padding-left': '0' }}, items);
                } else if (typeof val === 'object' ) {
                    formattedVal = spawn('code', JSON.stringify(val));
                } else if (!val) {
                    formattedVal = spawn('code','false');
                } else {
                    formattedVal = spawn('code',val);
                }

                tokTable.tr()
                    .td('<b>'+key+'</b>')
                    .td([ spawn('div',{ style: { 'word-break': 'break-all','max-width':'600px' }}, formattedVal) ]);
            }

            // display history
            XNAT.ui.dialog.open({
                title: 'Alias Token '+token['alias'],
                width: 800,
                scroll: true,
                content: tokTable.table,
                buttons: tokenDialogButtons
            });
        } else {
            console.log(id);
            XNAT.ui.dialog.open({
                content: 'Sorry, could not display this alias token.',
                buttons: [
                    {
                        label: 'OK',
                        isDefault: true,
                        close: true
                    }
                ]
            });
        }
    };

    // display a sorted table listing all valid tokens
    function renderAliasTokensTable() {
        var $container = $('div#alias-token-table-container');
        $container.empty();

        XNAT.xhr.get({
            url: XNAT.url.rootUrl('/data/services/tokens/show'),
            success: function(data){
                if (!isArray(data)) data = JSON.parse(data);

                // populate data object
                data.forEach(function(token){
                    tokens[token.id] = token;
                });

                // sort data for table display
                var sortedData = data.sort(function(a,b){
                    return (a.timestamp < b.timestamp) ? 1 : -1
                });

                if (sortedData.length){
                    XNAT.table.dataTable([], {
                        id: 'alias-token-list-table',
                        // load: '/data/services/tokens/show',
                        data: sortedData,
                        sortable: 'alias,updated,expiration',
                        trs: function(tr,data){
                            addDataAttrs(tr, {
                                alias: data.alias,
                                secret: data.secret
                            });
                        },
                        columns: {
                            id: '~data-id',
                            alias: {
                                label: 'Alias',
                                apply: function(){
                                    return spawn(
                                        'a.view-alias-token',
                                        {
                                            href: '#!',
                                            style: { 'font-weight': 'bold' }
                                        },
                                        this.alias
                                    );
                                }
                            },
                            enabled: {
                                label: 'Status',
                                apply: function(){
                                    return (this.enabled) ? 'Enabled' : 'Disabled';
                                }
                            },
                            expiration: {
                                label: 'Expiration (est.)',
                                apply: function () {
                                    return new Date(this.estimatedExpirationTime).toLocaleString();
                                }
                            },
                            ACTIONS: {
                                label: 'Actions',
                                className: 'center',
                                apply: function(){
                                    return spawn('!',[
                                        spawn('button.btn.btn-sm.view-alias-token','View'),
                                        spacer(10),
                                        spawn('button.btn.btn-sm.delete-alias-token', { title: 'Invalidates token and removes it from this table' }, 'Delete')
                                    ])
                                }
                            }
                        },
                        messages: {
                            noData: '' +
                            'You don\'t have any alias tokens at this time.',
                            error: 'An error occurred retrieving your alias tokens from the system.'
                        }
                    }).render($container);
                }
                else {
                    // if no data to display
                    $container.append(spawn('p','You don\'t have any alias tokens at this time.'));
                }

            },
            fail: function(e){
                console.log(e);
            }
        });

    }
    aliasTokens.renderAliasTokensTable = renderAliasTokensTable;

    // validate a token
    function validateToken(_alias,_secret){
        XNAT.xhr.getJSON({
            url: XNAT.url.rootUrl('/data/services/tokens/validate/'+_alias+'/'+_secret),
            success: function(data){
                if (data && data.valid !== undef){
                    XNAT.ui.banner.top(2000,'Token valid for '+data.valid, 'success')
                } else {
                    XNAT.ui.dialog.open({
                        content: 'This token is invalid or has expired.',
                        buttons: [
                            {
                                label: 'OK',
                                isDefault: true,
                                action: function(){
                                    XNAT.ui.dialog.closeAll();
                                }
                            }
                        ]
                    });
                    aliasTokens.renderAliasTokensTable();
                }
            }
        });
    }
    aliasTokens.validateToken = validateToken;

    $(document).ready(function () {
        renderAliasTokensTable();
    });

    $(document).on('click', '.view-alias-token', function(e){
        e.preventDefault();

        var _id = $(this).closest('tr').dataAttr('id');
        XNAT.app.aliasTokens.viewToken(_id);
    });

    $(document).on('click', '.validate-alias-token', function(e){
        e.preventDefault();

        var _alias = $(this).closest('tr').dataAttr('alias'),
            _secret = $(this).closest('tr').dataAttr('secret');

        validateToken(_alias,_secret);
    });

    $(document).on('click', '.delete-alias-token', function (e) {
        e.preventDefault();

        var _id = $(this).closest('tr').dataAttr('id');
        var _url = XNAT.url.restUrl('/data/services/tokens/invalidate/' + _id);

        XNAT.ui.dialog.open({
            title: 'Confirm Deletion',
            width: 450,
            content: spawn('!',[
                spawn('p',{style: { 'font-weight': 'bold'}}, 'Are you sure you want to delete this alias token?'),
                spawn('p','Alias tokens are often generated by pipeline launches. Deleting one could cause a pipeline to fail midstream, or affect other processes. Please continue only if you fully understand the risk.')
            ]),
            buttons: [
                {
                    label: 'Confirm Delete',
                    isDefault: 'true',
                    close: false,
                    action: function(){
                        XNAT.xhr.getJSON({
                            url: _url,
                            done: function(){
                                XNAT.ui.dialog.closeAll();
                                console.log('Deleted alias token ' + _id);
                                XNAT.ui.banner.top(2000,'Deleted alias token','success');
                                renderAliasTokensTable();
                            },
                            fail: function(e){
                                XNAT.ui.dialog.closeAll();
                                XNAT.ui.dialog.message('Error',e.statusText,'OK');
                                console.log(e);
                            }
                        })
                    }
                },
                {
                    label: 'Cancel',
                    close: true
                }
            ]
        });
    });

    $(document).on('click', 'button#create-alias-token', function (e) {
        e.preventDefault();

        var _url = XNAT.url.restUrl('/data/services/tokens/issue');

        XNAT.xhr.getJSON(_url).done(function (data) {
            console.log('Created new alias token ' + data.alias);
            renderAliasTokensTable();
        });
    });

}));


