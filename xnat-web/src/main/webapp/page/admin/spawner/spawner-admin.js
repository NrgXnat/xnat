/*
 * web: spawner-admin.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

(function(){

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
    //         XNAT.xhr.get(XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/adminPage'), function(data){
    //             showJSON(data);
    //         });
    //     },
    //     $: {
    //         appendTo: '#view-json'
    //     }
    // });

    //$('#view-json').click(function(){});

})();

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
                    html: 'View Raw Elements',
                    onclick: function(e){
                        e.preventDefault();
                        XNAT.xhr.get(this.title, function(data){
                            xmodal.open({
                                size: 'med',
                                maximize: true,
                                content: '<pre>'+JSON.stringify(data, null, 2)+'</pre>'
                            });
                        });
                    }
                }],
                ['span', '&nbsp;'],
                ['button', {
                    type: 'button',
                    title: XNAT.url.rootUrl('/xapi/spawner/resolve/' + NS + '/' + NS),
                    html: 'View Resolved Elements',
                    onclick: function(e){
                        e.preventDefault();
                        XNAT.xhr.get(this.title, function(data){
                            xmodal.open({
                                size: 'med',
                                maximize: true,
                                content: '<pre>'+JSON.stringify(data, null, 2)+'</pre>'
                            });
                        });
                    }
                }]
            ]]);

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
                            xmodal.message('Select an Element ID');
                            return false;
                        }
                        var elementUrl = XNAT.url.rootUrl('/xapi/spawner/element/' + NS + '/' + getId);
                        $.get(elementUrl, function(data){
                            var _textarea = spawn('textarea.yaml.mono', {
                                name: getId,
                                html: data.yaml,
                                style: { width: '500px', height: '250px' }
                            });
                            _textarea.innerHTML = (data.yaml);
                            var _table = XNAT.table({className: 'xnat-table borderless'}).init([
                                // [ [['b.label', 'Label: ']], data.label ],
                                // we could use spawn arg arrays (above), but HTML (below) is fine
                                ['<b class="label">Namespace:</b> ', data.namespace],
                                ['<b class="label">Element ID:</b> ', data.elementId],
                                ['<b class="label">Label:</b> ', data.label],
                                ['<b class="label">Created:</b> ', new Date(data.created).toString()],
                                ['<b class="label">Modified:</b> ', new Date(data.timestamp).toString()]
                            ]);
                            // another way to add a row of data
                            _table.tr().td([['b.label', 'YAML: ']]).td([_textarea]);
                            //_table.tr().td('<b>YAML:</b> ').td([_textarea]);
                            xmodal.open({
                                //size: 'med',
                                width: 700,
                                height: 550,
                                mask: false,
                                maximize: true,
                                enter: false,
                                esc: false,
                                title: 'Element ID: ' + getId,
                                content: _table.get().outerHTML,
                                beforeShow: function(obj){
                                    console.log(obj)
                                },
                                okLabel: 'Save Changes',
                                okClose: false,
                                okAction: function(obj){
                                    XNAT.xhr.put({
                                        url: elementUrl,
                                        data: obj.$modal.find('textarea.yaml').val(),
                                        contentType: 'text/x-yaml',
                                        processData:  false,
                                        success: function(){
                                            xmodal.message('Data saved.')
                                        },
                                        error: function(e, f, g){
                                            xmodal.message(['<b>Error:</b>', e, f, g].join('<br>'))
                                        }
                                    });
                                }
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

