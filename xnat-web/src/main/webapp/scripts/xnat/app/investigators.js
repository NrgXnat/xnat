/*
 * web: investigators.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

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

    console.log('investigators.js');

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
        this.menu = null; // this will be updated when a menu is created
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
    Investigators.fn.getSelected = function(i){
        var self = this;
        this.selected = [];
        this.menu$.find(':selected').each(function(){
            self.selected.push(this.value)
        });
        return i || i === 0 ? this.selected[i] : this.selected;
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

    // display confirmation dialog then delete investigator
    Investigators.fn.deleteInvestigator = function(id, name){
        var self = this;
        xmodal.confirm({
            title: 'Delete Investigator?',
            content: 'Delete investigator: ' + '<b>' + name + '</b>?',
            okLabel: 'Delete',
            okClose: false,
            okAction: function(dlg){
                XNAT.xhr['delete']({
                    url: setupUrl(id),
                    success: function(){
                        XNAT.ui.banner.top(2000, 'Investigator deleted.', 'success');
                        dlg.close();
                        investigators.dataTable(self.tableContainer);
                    }
                })
            }
        });
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

        var isPrimary = self.menu ? self.menu.value == investigators.primary : false;

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
                        primary: self.menu ? {
                            kind: 'panel.element',
                            label: false,
                            contents:
                                '<label>' +
                                '<input type="checkbox" class="set-primary">' +
                                ' Set as Primary' +
                                '</label>'
                        } : {
                            tag: 'i.hidden',
                            content: '(no menu, no checkbox)'
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
                        if (self.menu) {
                            obj.$modal.find('input.set-primary').prop('checked', isPrimary);
                        }
                    },
                    okLabel: 'Submit',
                    okClose: false,
                    okAction: function(obj){
                        var _form = obj.$modal.find('form[name="editInvestigator"]'),
                            setPrimary = self.menu ? _form.find('input.set-primary')[0].checked : false;
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
                            success: function(data, status, xhrObj){
                                var selected = data.xnatInvestigatordataId;
                                ui.banner.top(2000, 'Investigator data saved.', 'success');
                                // update other menus, if specified
                                if (self.menu) {
                                    if (menus) {
                                        [].concat(menus).forEach(function(menu){
                                            menu.updateMenu(setPrimary ? selected : '');
                                        })
                                    }
                                    // update the menu associated with the dialog
                                    self.updateMenu([].concat(self.getSelected(), (!setPrimary ? selected : [])));
                                    // set the PI if editing/creating PI
                                    if (setPrimary) {
                                        investigators.primary = selected;
                                    }
                                }
                                // update the investigator table (in site admin)
                                if (self.tableContainer){
                                    self.dataTable();
                                }
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


    Investigators.fn.dataTable = function(container){

        var self = this;

        this.tableContainer = $$(container || '#investigators-list-container');

        function investigatorFieldValue(val){
            return val || '<div class="center">&mdash;</div>'
        }

        function investigatorProjectList(){
            var _data = this;
            var projects = _data.primaryProjects;
            return '' +
                '<div class="primaryProjects center">' +
                    (isArray(projects) && projects.length ? projects.map(function(proj){
                        return '<a title="Go to project page for ' + proj + '" class="link" href="/data/projects/' + proj + '">' + proj + '</a>'
                    }).join(', ') : '&mdash;') +
                '</div>' +
                '<div title="investigatorProjects" class="hidden">' +
                    [].concat(_data.investigatorProjects).join(', ') +
                '</div>';
        }

        var tableConfig = {
            investigatorsTable: {
                kind: 'table.dataTable',
                id: 'xnat-investigators-list',
                load: '/xapi/investigators',
                messages: {
                    noData: 'There are no investigators defined in this system. Click the button below to create one.' ,
                    error: 'An error occurred retrieving investigator information.'
                },
                items: {
                    xnatInvestigatordataId: '~data-id',
                    viewInvestigator: {
                        label: '<div class="hidden"></div>',
                        className: "center",
                        call: function(){
                            var ID = this.xnatInvestigatordataId;
                            return spawn('a.view-investigator.link', {
                                href: '#!',
                                data: { id: ID },
                                on: { click: function(e){
                                    e.preventDefault();
                                    console.log(ID);
                                    self.dialog(ID)
                                }}
                            }, 'View')
                        }//,
                        //content: '<a href="#!" class="view-investigator" data-id="__VALUE__">View</a>'
                    },
                    // "title": {
                    //     label: "Title",
                    //     call: function(val){ return val || '-' }
                    // },
                    fullName: {
                        label: 'Name',
                        sort: true,
                        call: function(){
                            return this.lastname + ', ' + this.firstname
                        }
                    },
                    // firstname: {
                    //     label: "First Name",
                    //     sort: true,
                    //     call: investigatorFieldValue
                    // },
                    // lastname: {
                    //     label: "Last Name",
                    //     sort: true,
                    //     call: investigatorFieldValue
                    // },
                    email: {
                        label: 'Email',
                        sort: true,
                        call: investigatorFieldValue
                    },
                    institution: {
                        label: 'Institution',
                        sort: true,
                        call: investigatorFieldValue
                    },
                    projects: {
                        label: 'PI',
                        call: investigatorProjectList
                    },
                    // investigatorProjects: {
                    //     label: '~!',
                    //     call: investigatorProjectList
                    // }
                    deleteInvestigator: {
                        label: 'Delete',
                        className: 'center',
                        call: function(){
                            var ID = this.xnatInvestigatordataId;
                            var NAME = this.firstname + ' ' + this.lastname;
                            return spawn('button.delete-investigator.btn2.btn-sm.center|type=button', {
                                on: { click: function(){
                                    self.deleteInvestigator(ID, NAME);
                                }}
                            }, 'Delete')
                        }
                    }
                }
            }
        };

        this.spawnedTable = XNAT.spawner.spawn(tableConfig);
        this.table = this.spawnedTable.get();

        if (this.tableContainer) {
            this.tableContainer.empty();
            this.spawnedTable.render(this.tableContainer);
        }

        var new_investigator_btn = '#create-new-investigator';
        var new_investigator_click = 'click.newInvestigator';

        $('body').off(new_investigator_click, new_investigator_btn)
                 .on(new_investigator_click, new_investigator_btn, function(){
                     self.dialog();
                 });

        return this;

    };

    // MAIN INIT FUNCTION
    investigators.init = function(opts){
        return new Investigators(opts);
    };

    investigators.dataTable = function(container){
        var $container = $$(container).empty();
        var _dataTable = investigators.init().dataTable($container);
        return {
            spawned: _dataTable.table,
            element: _dataTable.table,
            get: function(){
                return _dataTable.table
            }
        } ;
    };

    investigators.getAll = function(opts){
        return investigators.init().getAll(opts).xhr;
    };

    investigators.get = function(id){
        return investigators.init().get(id).xhr;
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
