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

function renderAliasTokensTable() {
    var $container = $('div#alias-token-table-container');
    $container.empty();

    XNAT.table.dataTable([], {
        id: 'alias-token-list-table',
        load: '/data/services/tokens/show',
        columns: {
            id: '~data-id',
            alias: {
                label: 'Alias'
            },
            secret: {
                label: 'Secret'
            },
            updated: {
                label: 'Last Updated',
                apply: function () {
                    return new Date(this.timestamp).toString();
                }
            },
            expiration: {
                label: 'Expiration (est.)',
                apply: function () {
                    return new Date(this.estimatedExpirationTime).toString();
                }
            },
            deleteToken: {
                label: 'Delete',
                value: "&ndash;", // this gets overwritten with 'html' below
                className: 'center',
                // function that accepts current item as sole argument and parent object as 'this'
                // returned value will be new value - if nothing's returned, value is passed through
                // apply: "{(  function(){ return formatJSON(this) }  )}"
                html: '<a href="#" class="delete-alias-token link nowrap">Delete</a>'
            }
        },
        messages: {
            noData: '' +
            'You don\'t have any alias tokens at this time.',
            error: 'An error occurred retrieving your alias tokens from the system.'
        }
    }).render($container);
}

$(document).ready(function () {
    renderAliasTokensTable();
});

$(document).on('click', 'a.delete-alias-token', function (e) {
    e.preventDefault();

    var _id = $(this).closest('tr').dataAttr('id');
    var _url = XNAT.url.restUrl('/data/services/tokens/invalidate/' + _id);

    XNAT.xhr.getJSON(_url).done(function () {
        console.log('Deleted alias token ' + _id);
        renderAliasTokensTable();
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
