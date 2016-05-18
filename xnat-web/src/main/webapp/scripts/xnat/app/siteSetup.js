/*!
 * Spawn form input elements
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

    var siteSetup, undefined;
    
    XNAT.app = getObject(XNAT.app || {});
    
    XNAT.app.siteSetup = siteSetup =
        getObject(XNAT.app.siteSetup || {});

    var multiform = {
        count: 0,
        errors: 0
    };

    siteSetup.multiform = multiform;
    
    // use app.siteSetup.form for Spawner 'kind'

    // creates a panel that submits all forms contained within
    siteSetup.form = function(opts, callback){

        opts = cloneObject(opts);
        opts.element = opts.element || opts.config || {};

        var inner = spawn('div.panel-body', opts.element),
            
            submitBtn = spawn('button', {
                type: 'submit',
                classes: 'btn submit save pull-right',
                html: 'Save All'
            }),

            resetBtn = spawn('button', {
                type: 'button',
                classes: 'btn btn-sm btn-default revert pull-right',
                html: 'Discard Changes',
                onclick: function(e){
                    e.preventDefault();
                    $(this).closest('form.multi-form').find('form').each(function(){
                        $(this).triggerHandler('reload-data');
                    });
                    return false;
                }
            }),

            defaults = spawn('button', {
                type: 'button',
                classes: 'btn btn-link defaults pull-left',
                html: 'Default Settings'
            }),

            footer = [
                submitBtn,
                ['span.pull-right', '&nbsp;&nbsp;&nbsp;'],
                resetBtn,
                // defaults,
                ['div.clear']
            ],

            multiForm = spawn('form', {
                name: opts.name,
                classes: 'xnat-form-panel multi-form panel panel-default',
                method: opts.method || 'POST',
                action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
                onsubmit: function(e){

                    e.preventDefault();
                    var $forms = $(this).find('form');

                    var loader = xmodal.loading.open('#multi-save');

                    // reset error count on new submission
                    multiform.errors = 0;

                    // how many child forms are there?
                    multiform.count = $forms.length;

                    // submit ALL enclosed forms
                    $forms.each(function(){
                        $(this).addClass('silent').trigger('submit');
                    });

                    multiform.errors = $forms.filter('.error').length;

                    if (multiform.errors) {
                        xmodal.closeAll();
                        xmodal.message('Error', 'Please correct the highlighted errors and re-submit the form.');
                        return false;
                    }

                    XNAT.xhr.postJSON({
                        url: XNAT.url.rootUrl('/xapi/siteConfig/batch'),
                        data: JSON.stringify({initialized:true}),
                        success: function(){
                            xmodal.message({
                                title: false,
                                esc: false,
                                content: 'Your XNAT site is ready to use. Click "OK" to continue to the home page.',
                                action: function(){
                                    // window.location.href = XNAT.url.rootUrl('/setup?init=true');
                                    window.location.href = XNAT.url.rootUrl('/');
                                    //$forms.each.triggerHandler('reload-data');
                                }
                            });
                        }
                    }).fail(function(e, txt, jQxhr){
                        xmodal.loading.close(loader.$modal);
                        xmodal.message({
                            title: 'Error',
                            content: [
                                'An error occurred during initialization',
                                e,
                                txt
                            ].join(': <br>')
                        })
                    });

                    xmodal.loading.close(loader.$modal);
                    return false;

                }
            }, [
                ['div.panel-heading', [
                    ['h3.panel-title', opts.title || opts.label]
                ]],

                // 'inner' is where the NEXT spawned item will render
                inner,

                ['div.panel-footer', opts.footer || footer]

            ]);

        // add an id to the outer panel element if present
        if (opts.id || opts.element.id) {
            multiForm.id = opts.id || (opts.element.id + '-panel');
        }

        return {
            target: inner,
            element: multiForm,
            spawned: multiForm,
            get: function(){
                return multiForm
            }
        }
    };
    siteSetup.form.init = siteSetup.form;

    // this script has loaded
    siteSetup.loaded = true;

    return XNAT.app.siteSetup = siteSetup;

}));
