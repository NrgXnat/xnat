/**
 * example XNAT.dialog configs
 */

XNAT = getObject(XNAT);

(function(dialog){

    if (!dialog) { return }

    var examples = {


        minimal: {
            // renders a 600x400 modal dialog
            // with a title bar
            // with 'OK' and 'Cancel' buttons
            content: 'Content for the dialog.',
            okClose: true
        },


        shortcuts: {

            // shortcuts for footer options
            footerContent: 'Custom content for the left side of the footer.',
            footerHeight: 50, // footer height in pixels
            footerBackground: '#f0f0f0',
            footerBorder: '#e0e0e0',
            footerButtons: true,

            // shortcuts for 'OK' and 'Cancel' buttons
            //ok: true, // OPTIONAL - set to false to suppress (default) 'OK' button
            okLabel: 'Go',
            okAction: function(){ doStuff() },
            okClose: true, // close the dialog when 'ok' is clicked?
            //cancel: false, // OPTIONAL - set to false to suppress 'Cancel' button
            cancelLabel: 'Stop',
            cancelAction: function(){ doOtherStuffInstead() },
            cancelClose: false

        },


        everything: {

            id: 'your-custom-id', // MUST BE UNIQUE (a unique id will be set automatically if undefined)

            className: 'foo bar stuff', // any 'legal' className string

            width: 600,  // specific width/height overrides 'size' preset
            height: 400, // integer or string (string needs px, em, or %)

            minWidth: 300, // min/max
            maxWidth: 960, // useful if using % width or height
            minHeight: 200,
            maxHeight: 720,

            maxxed: true, // open the dialog in maximized view?

            top: '100px', // explicit position from top of viewport (px or %)
            bottom: '0px', // must use string and specify units
            left: '100px',
            // 'right' is not necessary - if 'left' is not 0, 'right' is set to 'auto'

            padding: 20, // amount of padding (in pixels) around the body content

            css: { // or 'style' - jQuery CSS object - custom style for modal body
                'background': 'blue',
                'color': 'white',
                'font-family': 'Comic Sans'
            },

            animation: 'fade' || 'slide' || false, // choose ONE ('fade' is default)
            speed: 100, // duration of animation. may also use 'duration' property

            mask: true, // do we want to mask the page? may also use 'modal' property

            scroll: true, // does the dialog body content need to scroll?
            overflow: true, // set 'overflow:visible' on dialog, .body AND .inner elements

            closeBtn: true, // render a 'close' button in the title bar?
            maxBtn: false, // render a 'maximize' button in the title bar (to expand the dialog to fill the viewport)?
            isDraggable: true, // can we drag this dialog?

            enter: true, // will pressing the 'enter' key trigger the default button?
            esc: true, // will pressing the 'esc' key dismiss the dialog?

            title: 'Information' || false, // 'false' renders a small textless window bar with no buttons

            // use EITHER 'template' or 'content' property ('content' overrides 'template')
            //
            template: $('#template-id'), // jQuery object, selector, or id
            //
            // HTML string or element or document fragment
            content: 'This is a sentence that will show up in the body of the dialog.',
            // can also grab HTML from the DOM $('#content-id').html() - but watch for duplicate IDs

            // 'footer' property is OPTIONAL
            // set footer: false to prevent rendering
            footer: {
                content: 'Custom content to display on the left side of the footer.',
                height: 50, // height in px
                background: '#f0f0f0', // valid CSS color value
                border: '#e0e0e0', // valid CSS color value
                buttons: true // show buttons from 'buttons' object?
            },

            // the 'buttons' property is an ARRAY of button config objects, rather than
            // an object map with named config objects
            buttons: [
                // there would be FOUR buttons on this dialog: ok, wait, cancel (link), close (link)
                {
                    label: 'OK',
                    isDefault: true,
                    close: true,
                    // 'obj' is the instance object created for this dialog
                    action: function(obj){
                        doStuffWithReturnedObject(obj);
                    }
                },
                {
                    label: 'Wait a Second',
                    close: false,
                    disabled: true, // this button will be disabled when rendered
                    action: function(obj){
                        var $emailModal = obj.$modal;
                        if ($emailModal.find('input.email').val()) {
                            obj.close();
                        }
                        else {
                            XNAT.dialog.confirm({
                                content: 'Please enter an email address.',
                                cancelAction: function(){
                                    // close 'parent' modal
                                    obj.close();
                                }
                            });
                        }
                    }
                },
                {
                    label: 'Cancel',
                    link: true, // if present and 'true', displays 'button' as a link
                    close: true
                },
                { // a button named 'close' will ALWAYS close when clicked
                    label: 'Close',
                    link: true
                }
            ],

            // do something with the dialog after it's
            // added to the DOM but before it's displayed
            // 'obj' is the instance object created for this dialog
            // the jQuery object for the dialog itself is:
            // obj.$modal (or obj.__modal)
            beforeShow: function(obj){
                // set the value of <input type="text" class="email">
                var email = obj.$modal.find('input.email').val();
                return XNAT.validate.email(email).check();
            },

            // do something after the dialog is fully rendered
            afterShow: function(obj){
                obj.$modal.find('input.email').focus().select();
            },

            // do something BEFORE closing the dialog
            // 'beforeHideResult' is set with the returned value of 'beforeHide'
            beforeHide: function(obj){
                return obj.dialog$.find('#to-check').val();
            },

            // do something when the dialog is hidden (but still in the DOM)
            onHide: function(obj){
                if (!obj.beforeHideResult) {
                    alert('Not closing.');
                    return false;
                }
                return fooFn();
            },

            // do something after the dialog closes
            afterClose: function(obj){
                doSomethingAfterTheDialogCloses();
            }

        },


        alternates: {
            // alternate property names
            duration: 200, // same as 'speed'
            modal: true, // same as 'mask' (should this dialog be modal?)
            draggable: true // same as, but overridden by 'isDraggable'
        }

    };


    var dialogInstance = XNAT.dialog.init({ content: 'Hello.' });

    dialogInstance.show(200, function(){
        // check 'beforeShowResult' before showing
        if (!this.beforeShowResult) {
            XNAT.dialog.message(false, 'Enter a valid email address.');
        }
    });


    //////////////////////////////////////////////////
    // we can uncomment these if we think we need to
    // make these configs available externally
    //
    XNAT.dialog.examples = examples;
    //
    //////////////////////////////////////////////////

})(XNAT.dialog = getObject(XNAT.dialog || {}));

