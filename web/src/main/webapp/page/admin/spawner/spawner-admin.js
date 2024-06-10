/*
 * web: spawner-admin.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

(function(){

    function viewResolvedElements(url, data){
        XNAT.dialog.open({
            maxxed: true,
            maxBtn: true,
            minWidth: 800,
            title: url,
            content: '' +
                prettifyJSON(data) +
            // '<pre>'+JSON.stringify(data, null, 2)+'</pre>' +
            '',
            buttons: [
                {
                    label: 'Close',
                    isDefault: true,
                    close: true
                }
            ]
        });
    }

    XNAT.xhr.getJSON({
        url: XNAT.url.rootUrl('/xapi/spawner/namespaces'),
        success: function(json){

            var items = [];

            $.each(json, function(){

                var NS = this;
                var tds = [];

                tds.push(['td', '<b>' + NS + '</b>']);
                tds.push(['td', [
                    ['button.link', {
                        type: 'button',
                        title: XNAT.url.rootUrl('/xapi/spawner/elements/' + NS),
                        html: 'View Elements',
                        onclick: function(e){
                            e.preventDefault();
                            var _url = this.title;
                            XNAT.xhr.get(_url, function(data){
                                XNAT.dialog.open({
                                    maxxed: true,
                                    maxBtn: true,
                                    minWidth: 800,
                                    title: _url,
                                    content: '' +
                                    prettifyJSON(data) +
                                    // '<pre>'+JSON.stringify(data, null, 2)+'</pre>' +
                                    '',
                                    buttons: [
                                        {
                                            label: 'Close',
                                            isDefault: true,
                                            close: true
                                        }
                                    ]
                                });
                            });
                        }
                    }],
                    ['span', ' &nbsp; &nbsp; '],
                    ['button', {
                        type: 'button',
                        title: NS,
                        html: 'View Resolved Elements',
                        onclick: function(e){
                            e.preventDefault();
                            XNAT.spawner.resolve(NS + '/root').ok(
                                function(obj){
                                    viewResolvedElements(NS + '/root', obj)
                                },
                                function(){
                                    var elem = NS.split(':')[NS.split(':').length-1] || NS;
                                    var nsElem = NS + '/' + elem;
                                    XNAT.spawner.resolve(nsElem).ok(
                                        function(obj){
                                            viewResolvedElements(nsElem, obj)
                                        }
                                    )
                                }
                            );
                            // XNAT.xhr.get(this.title, function(data){
                            //     xmodal.open({
                            //         size: 'med',
                            //         maximize: true,
                            //         content: '<pre>'+JSON.stringify(data, null, 2)+'</pre>'
                            //     });
                            // });
                        }
                    }]
                ]]);

                tds.push(['td', '<span>&nbsp;|&nbsp;</span>']);

                // tds.push(['td', [
                //
                // ]]);

                var idsMenu = spawn('select.spawner-ns-ids', [['option|value=!', 'Select an Element']]);

                tds.push(['td', [

                    idsMenu,

                    ['span', '&nbsp;'],

                    ['button', {
                        type: 'button',
                        html: 'View Selected Element',
                        onclick: function(){
                            var getId = $(idsMenu).val();
                            if (!getId || getId === '!') {
                                XNAT.dialog.alert('Select an Element ID');
                                return false;
                            }
                            var elementUrl = XNAT.url.rootUrl('/xapi/spawner/element/' + NS + '/' + getId);
                            $.get(elementUrl, function(data){
                                var _textarea = spawn('textarea.yaml.mono', {
                                    name: getId,
                                    html: data.yaml,
                                    style: {
                                        width: '96%',
                                        height: '750px',
                                        margin: '5px 10px',
                                        padding: '5px 10px'
                                    }
                                });
                                var tableConfig = {
                                    className: 'xnat-table borderless',
                                    style: { width: '100%', height: '100%' }
                                };
                                var _table = XNAT.table(tableConfig).init([
                                    // [ [['b.label', 'Label: ']], data.label ],
                                    // we could use spawn arg arrays (above), but HTML (below) is fine
                                    ['<b class="label">Namespace:</b> ', data.namespace],
                                    ['<b class="label">Element ID:</b> ', data.elementId],
                                    ['<b class="label">Label:</b> ', data.label],
                                    ['<b class="label">Created:</b> ', new Date(data.created).toString()],
                                    ['<b class="label">Modified:</b> ', new Date(data.timestamp).toString()]
                                ]);
                                // another way to add a row of data
                                _table.tr().td([['b.label', 'YAML: ']]).td();
                                XNAT.dialog.open({
                                    width: 800,
                                    // height: 550,
                                    mask: false,
                                    maxBtn: true,
                                    enter: false,
                                    esc: false,
                                    title: 'Element ID: ' + getId,
                                    content: [
                                        _table.get(),
                                        _textarea,
                                        '<br>&nbsp;<br>'
                                    ],
                                    beforeShow: function(obj){
                                        if (jsdebug) console.log(obj)
                                    },
                                    buttons: [
                                        {
                                            label: 'Save Changes',
                                            close: false,
                                            isDefault: true,
                                            action: function(obj){
                                                XNAT.xhr.put({
                                                    url: elementUrl,
                                                    data: obj.$modal.find('textarea.yaml').val(),
                                                    contentType: 'text/x-yaml',
                                                    processData:  false,
                                                    success: function(){
                                                        XNAT.ui.banner.top(2000, 'Data saved.');
                                                    },
                                                    error: function(e, f, g){
                                                        XNAT.dialog.message(false, ['<b>Error:</b>', e, f, g].join('<br>'));
                                                    }
                                                });
                                            }
                                        }
                                    ]
                                })
                            })
                        }
                    }]
                ]]);

                // spawn and push the row
                items.push(spawn('tr', tds));

                // populate menu with spawner elements for 'NS' namespace
                XNAT.xhr.get(XNAT.url.rootUrl('/xapi/spawner/ids/' + NS), function(ids){
                    $.each(ids, function(){
                        var id = this;
                        idsMenu.appendChild(spawn('option', { value: id, html: id }))
                    });
                    menuInit(idsMenu, { width: '250px' });
                });

            });

            $('#spawner-element-list').append(items);

        }
    });

    // function showJSON(json){
    //     return xmodal.message({
    //         title: 'Site Admin JSON',
    //         maximize: true,
    //         width: '90%',
    //         height: '90%',
    //         content: spawn('pre.json', JSON.stringify(json, null, 2)).outerHTML
    //     })
    // }
    //
    // spawn('button|type=button', {
    //     html: 'View JSON',
    //     onclick: function(){
    //         XNAT.xhr.get(XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/root'), function(data){
    //             showJSON(data);
    //         });
    //     },
    //     $: {
    //         appendTo: '#view-json'
    //     }
    // });

    //$('#view-json').click(function(){});

})();

