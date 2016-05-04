(function(){

    var $head = $('head');

    // // append the css to the head
    // $head.spawn('link', {
    //     rel: 'stylesheet',
    //     type: 'text/css',
    //     href: XNAT.url.rootUrl('/scripts/lib/bootstrap/themes/xnat/bootstrap-fixed.css')
    // });

    // append the css to the head
    $head.spawn('link', {
        rel: 'stylesheet',
        type: 'text/css',
        href: XNAT.url.rootUrl('/page/admin/style.css')
    });

    // get the JSON and do the setup
    var jsonUrl = XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.yaml');
    //var jsonUrl = XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.json');
    //var jsonUrl = XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/siteAdmin');
    $.get({
        url: jsonUrl,
        // dataType: 'text',
        success: function(data){
            var json = YAML.parse(data);
            var adminTabs = XNAT.spawner.spawn(json);
            adminTabs.render('#admin-config-tabs > .xnat-tab-content');
            XNAT.app.adminTabs = adminTabs;
        }
    });

})();
