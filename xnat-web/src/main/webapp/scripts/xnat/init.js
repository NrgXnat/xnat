/*!
 * Initialize XNAT settings
 */

var XNAT = getObject(XNAT);

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
}(function(){

    var undef, init;

    XNAT.init = init = getObject(XNAT.init || {});

    XNAT.init.debug =
        getQueryStringValue('debug') ||
        getQueryStringValue('jsdebug');

    if (XNAT.init.debug === 'true') {
        console.log('/scripts/xnat/init.js');
    }

    // reset or remove cookies based on query string
    (function(){
        var cookiesParam = getQueryStringValue('cookies');
        var cookiePath = XNAT.url.rootUrl();
        if (/[*/~]/.test(cookiesParam.split('|')[1] || '')) {
            cookiePath = '/';
        }
        if (/^reset/i.test(cookiesParam)) {
            XNAT.cookie.resetAll({ path: cookiePath })
        }
        if (/^remove/i.test(cookiesParam)) {
            XNAT.cookie.removeAll({ path: cookiePath });
        }
    })();


    // add build info to page elements *AFTER* DOM load
    $(function(){

        var $doc = $(document);
        var $win = $(window);

        var waitMsgDialog = (function(){
            var dialogId = randomID('waitx', false);
            var dialogCfg = {
                uid: dialogId,
                id: dialogId,
                classes: 'wait static',
                content: ''
            };
            XNAT.dialog.static('', dialogCfg);
            return dialogId;
        })();

        function hideWaitMessage(uid){
            var ID = uid || waitMsgDialog;
            if (XNAT.dialog.dialogs[ID]) {
                XNAT.dialog.dialogs[ID].hide();
            }
        }

        $win.on('pageshow', function(e){
            hideWaitMessage();
        });

        $win.on('pagehide', function(e){
            hideWaitMessage();
        });

        // prevent default click triggers on '#' links
        $doc.on('click', '[href^="#"], [href^="@!"]', function(e){
            e.preventDefault();
        });

        // display 'wait' dialog when requesting data download
        function showWaitMessage(msg){
            var content = spawn('div.message.waiting.md', msg || 'Please wait...');
            return XNAT.dialog.dialogs[waitMsgDialog].update(content).open();
        }

        // display 'wait' dialog for elements with a [data-wait] attribute
        $doc.on('click', 'a[data-wait]', function(e){
            var msg = this.getAttribute('data-wait') || 'Please wait...';
            console.log('waiting...');
            showWaitMessage(msg);
        });

        $doc.on('click', '#actionsMenu a[title="Download Images"]', function(e){
             showWaitMessage('Preparing data for download...')
        });

        $doc.on('click', '#search_tabs a.yuimenuitemlabel', function(e){
            if (this.textContent === 'Download') {
                showWaitMessage('Preparing data for download...');
            }
        });


        // <input type="checkbox" name="bogus" data-values="yes|no" data-proxy="id-of-proxy-input">
        // ...or...
        // <input type="checkbox" name="bogus" title="bogus=yes|no">
        // ...or...
        // <input type="checkbox" name="bogus" title="bogus: yes|no">
        $doc.on('change', 'input.controller[type="checkbox"]', function(){

            var ckbx$ = $(this);
            var ckbx0 = this;
            var NAME = (ckbx0.name || ckbx0.title.split(/[:=]/)[0] || ckbx$.data('name')).trim();
            var values = ['true', 'false'];
            var dataValues = ckbx$.data('values') || ckbx$.data('options');

            ckbx0.name = NAME;

            // if the [title] attribute contains '=' (or ':') and '|'
            // ...it's probably the value options
            if (!dataValues && /.+[:=].+[|]/.test(ckbx0.title)) {
                dataValues = (ckbx0.title.split(/[:=]/)[1] || '').trim();
            }

            if (dataValues) {
                values = (dataValues+'').split('|');
            }

            ckbx0.value = ckbx0.checked ? (values[0]+'').trim() : (values[1]+'').trim();

        });

        // make sure switchboxes track values properly
        // encode the input name and values into the [title] attribute of the outer <label> element:
        // <label class="switchbox" title="myInput=checkedValue|uncheckedValue">
        // ...or...
        // <label class="switchbox" title="myInput: checkedValue|uncheckedValue">
        // $(document).on('change', 'input.switchbox', function(){
        //     var chkbox$ = $(this);
        //     var chkbox0 = chkbox$[0];
        //     var switch$ = chkbox$.closest('label.switchbox');
        //     var switch0 = switch$[0];
        //     var NAME    = switch0.title.split(/[:=]/)[0];
        //     var VALUES  = (switch0.title.split(/[:=]/)[1] || '').split('|') || ['true', 'false'];
        //     var proxy$  = switch$.find('input.proxy');
        //     var proxy0  = proxy$[0];
        //     if (!proxy0) {
        //         proxy0 = spawn('input.proxy|type=hidden');
        //         switch0.appendChild(proxy0);
        //     }
        //     proxy0.name = NAME;
        //     proxy0.value = chkbox0.checked ? (VALUES[0]+'').trim() : (VALUES[1]+'').trim();
        //     // set [name] attribute for the checkbox to an empty string
        //     if (chkbox0.name) {
        //         chkbox0.name = '';
        //     }
        //     chkbox0.value = proxy0.value
        // });

        var siteLogo = $('#header_logo').find('> img');
        // make sure the image in the #header_logo is loaded...
        // ...checks for a [src] attribute ending with a valid
        // image format extension (tolerates query string)
        if (siteLogo.length && !/([.](gif|jpg|jpeg|png|svg)[?]?.*)$/i.test(siteLogo[0].src)) {
            XNAT.xhr.get({
                url: XNAT.url.rootUrl('/xapi/siteConfig/siteLogoPath'),
                dataType: 'text',
                success: function(path){
                    siteLogo.attr('src', XNAT.url.rootUrl(path));
                }
            });
        }

        // add version to title attribute of XNAT logos
        if (window.top.loggedIn !== undef && window.top.loggedIn === true) {

            XNAT.cookie.set('SESSION_ACTIVE', 'true');

            var buildInfoSample = {
                "Application-Name": "XNAT",
                "Manifest-Version": "1.0",
                buildDate: "Sun Jun 05 12:41:24 CDT 2016",
                buildNumber: "Manual",
                commit: "v275-gd2220fd",
                version: "1.7.0",
                displayHostName: "true",
                hostName: "host-01.miskatonic.edu"
            };

            function displayBuildInfo(data){
                var version = XNAT.version = XNAT.data.version = data.version;
                var version_string = version + ', build: ' + data.buildNumber;
                var version_title = 'XNAT version ' + version_string;
                var isNonRelease = /SNAPSHOT|BETA|RC/ig.test(version);
                var build_string = ' ';

                if (isNonRelease) {
                    version_string += ' (' + data.commit + ')';
                    build_string += '<br>' + data.buildDate;
                }
                if (data.displayHostName === "true") {
                    version_string += '<br>Host: ' + data.hostName;
                }

                $('#xnat_power')
                    // .empty()
                    .spawn('a.xnat-version', {
                        href: 'http://www.xnat.org',
                        target: '_blank',
                        title: version_title
                    }, [['img', { src: serverRoot + '/images/xnat_power_small.png' }]])
                    .spawn('small', 'version ' + version_string + build_string);

                // $('#xnat_power').find('a')
                //                 .attr('title', version_title)
                //                 .after('<small>version ' + version_string + build_string + '</small>');

                $('#header_logo').attr('title', version_title);

                // save the complete string
                XNAT.app.version = version_string;

                console.log('version ' + XNAT.version);

            }

            // use existing data if available
            if (XNAT.data && XNAT.data.siteConfig && XNAT.data.siteConfig.buildInfo) {
                displayBuildInfo(XNAT.data.siteConfig.buildInfo);
            }
            else {
                XNAT.xhr.get(serverRoot + '/xapi/siteConfig/buildInfo', function(data){
                    extend(true, XNAT, {
                        data: {
                            siteConfig: {
                                buildInfo: data
                            }
                        }
                    });
                    displayBuildInfo(data);
                });
            }
        }
        else {
            XNAT.cookie.set('SESSION_ACTIVE', 'false');
        }

        var clicker = XNAT.event.click('#header_logo, #xnat_power > a');

        // shift-click the header or footer XNAT logo to TOGGLE debug mode on/off
        clicker.shiftKey(function(e){
            e.preventDefault();
            if (XNAT.cookie.get('debug') === 'on') {
                XNAT.cookie.set('debug', 'off');
                window.location.hash = 'debug=off';
            }
            else {
                XNAT.cookie.set('debug', 'on');
                window.location.hash = 'debug=on';
            }
            window.location.reload();
        });

        // alt-shift-click to open the Swagger page in a new window
        clicker.altShift(function(e){
            e.preventDefault();
            XNAT.ui.popup(XNAT.url.rootUrl('/xapi/swagger-ui.html'));
        });

    });


    // this script has loaded
    init.loaded = true;

    return XNAT.init = init;

}));
