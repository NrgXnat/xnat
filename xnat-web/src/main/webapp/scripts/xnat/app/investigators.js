/*!
 * Functions for creating and modifying projects
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

    var undef, investigators,
        BASE_URL = '/xapi/investigators',
        ui       = XNAT.ui,
        xhr      = XNAT.xhr,
        xurl     = XNAT.url;

    XNAT.app = getObject(XNAT.app||{});
    XNAT.xapi = getObject(XNAT.xapi||{});

    function setupUrl(part){
        part = part ? '/' + part : '';
        return xurl.rootUrl(BASE_URL + part);
    }

    investigators = getObject(XNAT.app.investigators || XNAT.xapi.investigators || {});

    var investigatorDataObj = {
        "id": null,
        "phone": "555-123-4567",
        "firstname": "Bob",
        "lastname": "Robertson",
        "email": "bob@bob.org",
        "title": "Fellow",
        "department": "Important Research Group",
        "institution": "Major University",
        "allFields": [
            "title",
            "firstname",
            "lastname",
            "institution",
            "department",
            "email",
            "phone",
            "ID"
        ],
        "schemaElementName": "investigatorData",
        "fullSchemaElementName": "xnat:investigatorData",
        "xnatInvestigatordataId": 2,
        "xsitype": "xnat:investigatorData",
        "headerString": "\t"
    };

    var modelSchema = {
        "department": "string",
        "email": "string",
        "firstname": "string",
        "id": "string",
        "institution": "string",
        "lastname": "string",
        "phone": "string",
        "title": "string",
        "xnatInvestigatordataId": 0,
        "xsitype": "string"
    };


    function Investigators(opts){
        extend(true, this, opts);
    }

    Investigators.fn = Investigators.prototype;

    Investigators.fn.getAll = function(opts){
        var self = this;
        this.isReady = false;
        this.xhr = xhr.getJSON(extend({
            url: setupUrl()
        }, opts || {})).done(function(data){
            self.isReady = true;
            self.data = data;
        });
        return this;
    };

    Investigators.fn.get = function(id){
        this.getAll({ url: setupUrl(id) });
        return this;
    };

    Investigators.fn.ready = function(callback){
        var self = this;
        return this.xhr.done(callback).done(function(){
            self.isReady = true;
        });
    };
    Investigators.fn.done = Investigators.fn.ready;

    Investigators.fn.setContainer = function(container){
        this.container$ = $$(container);
        this.container = this.container$[0];
        return this;
    };

    Investigators.fn.setMenu = function(menu){
        this.menu$ = $$(menu);
        this.menu = this.menu$[0];
        return this;
    };

    // sets the selected menu item and updates the .chosen() stuff
    Investigators.fn.setSelected = function(selected){
        var self = this;
        [].concat(selected).forEach(function(id){
            self.menu$.filter('[value="' + id + '"]').each(function(){
                this.selected = true;
            });
        });
        this.menu$.change();
        menuUpdate(self.menu);
        return this;
    };

    // renders the <option> elements
    Investigators.fn.createMenuItems = function(selected){
        var self = this;
        selected = [].concat(selected).map(function(item){
            return item+'';
        });
        this.getAll();
        this.xhr.done(function(data){

            self.selected = [];

            var options = data.map(function(item){
                var id = item.xnatInvestigatordataId+'';
                var menuOption = spawn('option', {
                    value: id,
                    html: item.lastname + ', ' + item.firstname
                });
                if (selected.indexOf(id) > -1) {
                    self.selected.push(id);
                    menuOption.selected = true;
                }
                return menuOption;
            });

            // empty the options, then add the updated options
            self.menu$.empty().append(options);

        });
        return this;
    };

    // creates the <select> element, but doesn't put it on the page yet
    Investigators.fn.createMenu = function(opts){
        var menu$ = $.spawn('select.investigator', opts);
        this.setMenu(menu$);
        // menuInit(this.menu, null, width||200);
        return this;
    };
    
    // renders the <select> element into the container
    Investigators.fn.render = function(container, width){
        var self = this;
        if (container !== undef) {
            this.setContainer(container);
        }
        // make sure the request is done before rendering
        this.xhr.done(function(){
            $$(container).append(self.menu);
            menuInit(self.menu, null, width||200);
        });
        return this;
    };

    Investigators.fn.updateMenu = function(selected){
        var self = this;
        this.createMenuItems(selected);
        this.xhr.done(function(){
            //self.menu$.val(selected).change();
            menuUpdate(self.menu);
        });
        return this;
    };

    // putting the .dialog() method on the prototype
    // ties it to the associated menu
    Investigators.fn.dialog = function(id, menuInstance){

        // the menu that gets updated on save
        menuInstance = menuInstance || this;

        function createInput(label, name, validate){
            return {
                kind: 'panel.input.text',
                name: name,
                label: label,
                validate: validate || ''
            }
        }

        function createPanel(){
            return {
                investigatorForm: {
                    kind: 'panel.form',
                    name: 'editInvestigator',
                    url: setupUrl(id),
                    method: id ? 'PUT' : 'POST',
                    contentType: 'json',
                    header: false,
                    footer: false,
                    contents: {
                        title: createInput('Title', 'title'),
                        first: createInput('First Name', 'firstname', 'required'),
                        last: createInput('Last Name', 'lastname', 'required'),
                        institution: createInput('Institution', 'institution'),
                        department: createInput('Department', 'department'),
                        email: createInput('Email', 'email', 'email'),
                        phone: createInput('Phone', 'phone', 'numeric-dash')
                        // ID: createInput('ID', 'ID'),
                        // invId: {
                        //     kind: 'panel.input.hidden',
                        //     name: 'xnat_investigatorData_id',
                        //     value: id || ''
                        // }
                    }
                }
            }
        }

        var invForm = XNAT.spawner.spawn(createPanel());

        var dialog =
                xmodal.open({
                    title: (id ? 'Edit' : 'Create') + ' Investigator',
                    content: '<div class="add-edit-investigator"></div>',
                    beforeShow: function(obj){
                        invForm.render(obj.$modal.find('div.add-edit-investigator'));
                    },
                    okLabel: 'Submit',
                    okClose: false,
                    okAction: function(obj){
                        $(obj.$modal.find('form[name="editInvestigator"]')).submitJSON({
                            delim: '!',
                            validate: function(){
                                var $form = $(this);
                                var errors = 0;

                                var $required = $form.find('input.required');
                                if (!XNAT.validate($required).all('not-empty').check()){
                                    errors++
                                }

                                var $emailInputs = $form.find('input.email').filter(function(){
                                    return !!this.value.trim();
                                });
                                if (!XNAT.validate($emailInputs).all('email').check()){
                                    errors++
                                }

                                return errors === 0;
                            },
                            success: function(data){
                                ui.banner.top(2000, 'New investigator created.', 'success');
                                // update a menu, if specified
                                if (menuInstance instanceof Investigators) {
                                    menuInstance.updateMenu(data.xnatInvestigatordataId);
                                }
                                dialog.close();
                            }
                        });
                        //xhr.form(obj.$modal.find('form'))
                    },
                    width: 500,
                    height: 500,
                    padding: '0px'
                });

        return dialog

    };


    // init function for XNAT.misc.blank
    investigators.init = function(opts){
        return new Investigators(opts);
    };

    investigators.getAll = function(opts){
        return this.init().getAll(opts).xhr;
    };

    investigators.get = function(id){
        return this.init().get(id).xhr;
    };

    // JUST the REST call to create new investigator
    investigators.createNew = function(opts){
        //
    };

    // JUST the REST call to update the investigator
    investigators.update = function(){
        //
    };


    investigators.delete = function(id, opts){
        if (!id) return false;
        return xhr.delete(extend, {
            url: setupUrl(id)
        }, opts || {});
    };

    //////////////////////////////////////////////////

    // this script has loaded
    investigators.loaded = true;

    return XNAT.app.investigators = XNAT.xapi.investigators = investigators;

}));
