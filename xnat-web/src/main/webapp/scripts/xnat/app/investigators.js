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
        this.isMulti = this.menu.multiple;
        return this;
    };

    // sets the selected menu item and updates the .chosen() stuff
    Investigators.fn.setSelected = function(selected){
        var self = this;
        selected = selected || self.selected;
        [].concat(selected).forEach(function(id){
            self.menu$.filter('[value="' + id + '"]').each(function(){
                this.selected = true;
            });
        });
        this.menu$.change();
        menuUpdate(self.menu);
        return this;
    };

    // return array of ids of selected investigators
    Investigators.fn.getSelected = function(){
        var self = this;
        this.selected = [];
        this.menu$.find(':selected').each(function(){
            self.selected.push(this.value)
        });
        return this.selected;
    };

    // renders the <option> elements
    Investigators.fn.createMenuItems = function(selected){
        var self = this;
        // retain current selection, if nothing specified
        selected = selected || this.getSelected();
        selected = [].concat(selected).map(function(item){
            if (isDefined(item)) {
                return item+'';
            }
        });
        console.log('selected: ' + selected.join('; '));
        this.getAll();
        this.xhr.done(function(data){

            var _selected = [],
                options = [];

            if (!self.isMulti) {
                if (!selected.length) {
                    options.push(spawn('option|disabled|selected', 'Select...'));
                }
                options.push(spawn('option|value=NULL', 'None'));
            }

            data.forEach(function(item){
                var id = item.xnatInvestigatordataId+'';
                var menuOption = spawn('option', {
                    value: id,
                    html: item.lastname + ', ' + item.firstname
                });
                if (selected.indexOf(id) > -1) {
                    _selected.push(id);
                    menuOption.selected = true;
                }
                options.push(menuOption);
            });

            self.selected = _selected;

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
        // save currently selected items, if 'selected' is undefined
        selected = selected || this.getSelected();
        this.createMenuItems(selected);
        this.xhr.done(function(){
            //self.menu$.val(selected).change();
            menuUpdate(self.menu);
        });
        return this;
    };

    // putting the .dialog() method on the prototype
    // ties it to the associated menu
    Investigators.fn.dialog = function(id, menus){

        // the menu that gets updated on save
        menus = menus || null;

        var self = this;

        function createInput(label, name, validate){
            return {
                kind: 'panel.input.text',
                name: name,
                label: label,
                validate: validate || ''
            }
        }

        var isPrimary = self.menu.value == investigators.primary;

        function investigatorForm(){
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
                        phone: createInput('Phone', 'phone', 'numeric-dash'),
                        primary: {
                            kind: 'panel.element',
                            label: false,
                            contents:
                                '<label>' +
                                '<input type="checkbox" class="set-primary">' +
                                ' Set as Primary' +
                                '</label>'
                        }
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

        var invForm = XNAT.spawner.spawn(investigatorForm());

        var dialog =
                xmodal.open({
                    title: (id ? 'Edit' : 'Create') + ' Investigator',
                    content: '<div class="add-edit-investigator"></div>',
                    beforeShow: function(obj){
                        invForm.render(obj.$modal.find('div.add-edit-investigator'));
                    },
                    afterShow: function(obj){
                        obj.$modal.find('input.set-primary').prop('checked', isPrimary);
                    },
                    okLabel: 'Submit',
                    okClose: false,
                    okAction: function(obj){
                        var _form = obj.$modal.find('form[name="editInvestigator"]'),
                            setPrimary = _form.find('input.set-primary')[0].checked;
                        $(_form).submitJSON({
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
                                var selected = data.xnatInvestigatordataId;
                                ui.banner.top(2000, 'Investigator data saved.', 'success');
                                // update other menus, if specified
                                if (menus) {
                                    [].concat(menus).forEach(function(menu){
                                        menu.updateMenu(setPrimary ? selected : '');
                                    })
                                }
                                // update the menu associated with the dialog
                                self.updateMenu([].concat(self.getSelected(), (!setPrimary ? selected : [])));
                                dialog.close();
                            }
                        });
                        //xhr.form(obj.$modal.find('form'))
                    },
                    width: 500,
                    height: 500,
                    padding: '0px',
                    scroll: false
                });

        return this;

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

    investigators.updateMenus = function(selected){
        if (investigators.menus && !investigators.menus.length) {
            investigators.menus.forEach(function(menu){
                menu.updateMenu(selected);
            })
        }
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
