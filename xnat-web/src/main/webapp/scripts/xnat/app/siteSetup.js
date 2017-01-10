/*
 * web: siteSetup.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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

    // call 'test' until it returns true
    function waitForIt(interval, test, callback){
        var waiting = setInterval(function(){
            if (test()) {
                var called = callback();
                clearInterval(waiting);
                return called;
            }
        }, interval || 10);
        return waiting;
    }

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
                classes: 'btn revert pull-right',
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
                classes: 'xnat-form-panel multi-form',
                method: opts.method || 'POST',
                action: opts.action ? XNAT.url.rootUrl(opts.action) : '#!',
                onsubmit: function(e){

                    e.preventDefault();
                    var $forms = $(this).find('form');
                    $forms.addClass('json silent');

                    xmodal.loading.open('#multi-save');

                    // reset success count on new submission
                    multiform.success = 0;

                    // how many child forms are there?
                    multiform.count = $forms.length;

                    // set error count to form count and subtract
                    // as submissions are successful
                    multiform.errors = 0;

                    // submit ALL enclosed forms
                    $forms.each(function(){
                        var $form = $(this);

                        // copy values for elements with [data-value-from] attributes
                        $form.find('input[data-value-from]').each(function(){
                            var $destInput = $(this);
                            // prevent overwriting existing values
                            if (!$destInput.val()){
                                var source = $destInput.data('valueFrom');
                                // var value = $form.find('[name="' + source + '"]')[0].value;
                                var value = $$(source)[0].value;
                                $(this).val(value);
                            }
                        });

                        XNAT.xhr.form($form, {
                            contentType: 'application/json',
                            validate: function(){

                                var $form = $(this);
                                var errors = 0;
                                var validation = true;

                                $form.dataAttr('errors', 0);

                                $form.find(':input.required').each(function(){
                                    var $input = $(this);
                                    $input.removeClass('invalid');
                                    if ($input.val() === '') {
                                        errors++;
                                        validation = false;
                                        $input.addClass('invalid');
                                    }
                                });

                                $form.dataAttr('errors', errors);

                                if (!validation) {
                                    $form.dataAttr('status','error').addClass('error');
                                    multiform.errors++;
                                    //don't show a dialog for each individual form
                                    //if (!$form.hasClass('silent')) {
                                    //    xmodal.message('Error','Please enter values for the required items and re-submit the form.');
                                    //}
                                }

                                return validation;

                            },
                            //contentType: 'json',
                            success: function(){
                                $form
                                    .dataAttr('status', 'success')
                                    .removeClass('error')
                                    .addClass('success');
                                multiform.success++
                            },
                            error: function(){
                                $form
                                    .dataAttr('status', 'error')
                                    .removeClass('success')
                                    .addClass('error');
                                multiform.errors++
                            }
                        });
                        // $(this).addClass('silent').trigger('submit');
                    });

                    // multiform.errors = $forms.filter('.error').length;

                    function initialize(){
                        XNAT.xhr.postJSON({
                            url: XNAT.url.rootUrl('/xapi/siteConfig'),
                            data: JSON.stringify({initialized:true}),
                            success: function(){
                                xmodal.loading.close('#multi-save');
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
                            xmodal.loading.close('#multi-save');
                            xmodal.message({
                                title: 'Error',
                                content: [
                                    'An error occurred during initialization',
                                    e,
                                    txt
                                ].join(': <br>')
                            })
                        });
                    }

                    function errorCheck(){
                        return multiform.errors || multiform.success === multiform.count;
                    }

                    waitForIt(100, errorCheck, function(){
                        if (multiform.errors) {
                            xmodal.closeAll();
                            xmodal.message('Error', 'Please correct the highlighted errors and re-submit the form.');
                            return false;
                        }
                        initialize();
                    });

                    return false;

                }
            }, [
                
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

