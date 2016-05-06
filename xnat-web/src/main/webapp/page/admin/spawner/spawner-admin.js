(function(){

    function showJSON(json){
        return xmodal.message({
            title: 'Site Admin JSON',
            maximize: true,
            width: '90%',
            height: '90%',
            content: spawn('pre.json', JSON.stringify(json, null, 2)).outerHTML
        })
    }

    spawn('button|type=button', {
        html: 'View JSON',
        onclick: function(){
            XNAT.xhr.get(XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/siteAdmin'), function(data){
                showJSON(data);
            });
        },
        $: {
            appendTo: '#view-json'
        }
    });

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

            var idsMenu = spawn('select#spawner-ns-ids', [['option|value=!', 'Select an Element']]);

            tds.push(['td', [

                idsMenu,

                ['span', '&nbsp;'],

                ['button', {
                    type: 'button',
                    html: 'View Selected Element',
                    onclick: function(){
                        var getId = $('#spawner-ns-ids').val();
                        if (!getId || getId === '!') {
                            xmodal.message('Select an Element ID');
                            return false;
                        }
                        var elementUrl = XNAT.url.rootUrl('/xapi/spawner/element/' + NS + '/' + getId);
                        $.get(elementUrl, function(data){
                            var _textarea = spawn('textarea.mono', {
                                name: getId,
                                html: data.yaml,
                                style: { width: '500px', height: '250px' }
                            });
                            _textarea.innerHTML = (data.yaml);
                            var _table = XNAT.table({className: 'xnat-table borderless'}).init([
                                ['<b class="label">Namespace:</b> ', data.namespace],
                                ['<b class="label">Element ID:</b> ', data.elementId],
                                [ [['b.label', 'Label: ']], data.label ],
                                //['<b class="label">Label:</b> ', data.label],
                                [ [['b.label', 'Created: ']], new Date(data.created).toString() ],
                                // ['<b class="label">Created:</b> ', new Date(data.created).toString()],
                                ['<b class="label">Modified:</b> ', new Date(data.timestamp).toString()]
                            ]);
                            // anothe way to add a row of data
                            _table.tr().td([['b.label', 'YAML: ']]).td([_textarea]);
                            //_table.tr().td('<b>YAML:</b> ').td([_textarea]);
                            xmodal.open({
                                //size: 'med',
                                width: 700,
                                height: 550,
                                maximize: true,
                                title: 'Element ID: ' + getId,
                                content: _table.get().outerHTML,
                                beforeShow: function(obj){
                                    console.log(obj)
                                },
                                okLabel: 'Save Changes',
                                okAction: function(){
                                    XNAT.xhr.put({
                                        // url: XNAT.url.csrfUrl(elementUrl),
                                        url: (elementUrl),
                                        data: {
                                            // namespace: data.namespace,
                                            // elementId: data.elementId,
                                            yaml: _textarea.value
                                        },
                                        //dataType: 'text/x-yaml',
                                        //contentType: 'application/json',
                                        contentType: 'text/x-yaml',
                                        processData:  false,
                                        success: function(){
                                            xmodal.message('Data saved.')
                                        },
                                        error: function(e){
                                            xmodal.message('An error occured: ' + e)
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

            (function(){
                XNAT.xhr.get(XNAT.url.rootUrl('/xapi/spawner/ids/' + NS), function(ids){
                    $.each(ids, function(){
                        var id = this;
                        idsMenu.appendChild(spawn('option', { value: id, html: id }))
                    });
                });
            })()

        });

        $('#spawner-element-list').append(items);

    }
});
