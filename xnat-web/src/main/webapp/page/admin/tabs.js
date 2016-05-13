(function(){

    var $head = $('head');

    // // append the css to the head
    // $head.spawn('link', {
    //     rel: 'stylesheet',
    //     type: 'text/css',
    //     href: XNAT.url.rootUrl('/scripts/lib/bootstrap/themes/xnat/bootstrap-fixed.css')
    // });

    // append the css to the head
    // $head.spawn('link', {
    //     rel: 'stylesheet',
    //     type: 'text/css',
    //     href: XNAT.url.rootUrl('/page/admin/style.css')
    // });

    // get the JSON and do the setup
    var jsonUrl = XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.yaml');
    //var jsonUrl = XNAT.url.rootUrl('/page/admin/data/config/site-admin-sample-new.json');
    // var jsonUrl = XNAT.url.rootUrl('/xapi/spawner/resolve/siteAdmin/siteAdmin');

    // get the siteConfig object first
    // doing this in JSP for better(?) performance
    //XNAT.xhr.get(XNAT.url.restUrl('/xapi/siteConfig'), function(siteConfig){

    // put those values in an object named XNAT.data.siteConfig
    //  XNAT.data = extend({}, XNAT.data, {
    //    siteConfig: siteConfig
    //});

    // THEN render the tabs...
    $.get({
        url: jsonUrl,
        // dataType: 'text',
        success: function(data){

            // if (typeof data === 'string') {
                data = YAML.parse(data);
            // }

            // console.log(JSON.stringify(data, ' ', 1));

            var adminTabs = XNAT.spawner.spawn(data);
            adminTabs.render('#admin-config-tabs > .xnat-tab-content');
            XNAT.app.adminTabs = adminTabs;

            // xmodal.loading.closeAll();
        }
    });
})();
