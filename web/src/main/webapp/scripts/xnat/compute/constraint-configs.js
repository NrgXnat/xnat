console.debug('Loading constraint-configs.js')

var XNAT = getObject(XNAT || {});
XNAT.compute = getObject(XNAT.compute|| {});
XNAT.compute.constraintConfigs = getObject(XNAT.compute.constraintConfigs || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.compute.constraintConfigs.get = async (id) => {
        console.debug(`Fetching constraint config ${id}`);
        const url = XNAT.url.restUrl(`/xapi/compute/constraints/${id}`);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Failed to fetch constraint config ${id}`);
        }
    }
    
    XNAT.compute.constraintConfigs.getAll = async () => {
        console.debug('Fetching all constraint configs');
        const url = XNAT.url.restUrl('/xapi/compute/constraints');
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Failed to fetch constraint configs');
        }
    }
    
    XNAT.compute.constraintConfigs.create = async (constraintConfig) => {
        console.debug('Creating constraint config');
        const url = XNAT.url.restUrl('/xapi/compute/constraints');
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(constraintConfig)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Failed to create constraint config');
        }
    }
    
    XNAT.compute.constraintConfigs.update = async (constraintConfig) => {
        console.debug(`Updating constraint config ${constraintConfig.id}`);
        const url = XNAT.url.restUrl(`/xapi/compute/constraints/${constraintConfig.id}`);
        
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(constraintConfig)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error(`Failed to update constraint config ${constraintConfig.id}`);
        }
    }
    
    XNAT.compute.constraintConfigs.delete = async (id) => {
        console.debug(`Deleting constraint config ${id}`);
        const url = XNAT.url.restUrl(`/xapi/compute/constraints/${id}`);
        
        const response = await fetch(url, {
            method: 'DELETE',
        });
        
        if (!response.ok) {
            throw new Error(`Failed to delete constraint config ${id}`);
        }
    }
    
    XNAT.compute.constraintConfigs.save = async (constraintConfig) => {
        console.debug(`Saving constraint config ${constraintConfig.id}`);
        if (constraintConfig.id) {
            return XNAT.compute.constraintConfigs.update(constraintConfig);
        } else {
            return XNAT.compute.constraintConfigs.create(constraintConfig);
        }
    }
    
    XNAT.compute.constraintConfigs.manager = async (containerId) => {
        console.debug(`Initializing constraint config manager in container ${containerId}`);
        
        let container,
            footer,
            constraintConfigs = [],
            users = [],
            projects = [];
        
        const init = () => {
            container = document.getElementById(containerId);
            
            if (!container) {
                throw new Error(`Cannot find container with id ${containerId}`);
            }
            
            clearContainer();
            renderNewButton();
            loadProjects();
            refreshTable();
        }
        
        const clearContainer = () => {
            container.innerHTML = '';
        }
        
        const renderNewButton = () => {
            footer = container.closest('.panel').querySelector('.panel-footer');
            footer.innerHTML = '';
            
            let button = spawn('div', [
                spawn('div.pull-right', [
                    spawn('button.btn.btn-sm.submit', { html: 'New Constraint', onclick: () => editor(null, 'new') })
                ]),
                spawn('div.clear.clearFix')
            ])
            
            footer.appendChild(button);
        }

        let fetchWithTimeout = async function(resource, options = {}) {
            const { timeout = 8000 } = options;

            const controller = new AbortController();
            const id = setTimeout(() => controller.abort(), timeout);
            const response = await fetch(resource, {
                ...options,
                signal: controller.signal
            });
            clearTimeout(id);
            return response;
        }
        
        const loadProjects = async () => {
            let response = await fetchWithTimeout(`/data/projects`);
            
            if (response.ok) {
                let data = await response.json();
                data = data.ResultSet?.Result || [];
                data = data.map(p => p['ID']);
                projects = data;
            } else {
                throw new Error(`Error fetching projects`);
            }
        }
        
        const deleteConfig = (id) => {
            XNAT.compute.constraintConfigs.delete(id).then(() => {
                XNAT.ui.banner.top(2000, 'Constraint deleted', 'success');
                refreshTable();
            }).catch(err => {
                XNAT.ui.banner.top(4000, 'Error deleting constraint', 'error');
                console.error(err);
            });
        }
        
        const displayProjects = (config) => {
            let isAllProjectsEnabled = config['scopes']['Project']['enabled'];
            let projects = config['scopes']['Project']['ids'];
            
            if (isAllProjectsEnabled) {
                return 'All Projects';
            } else {
                if (projects.length > 4) {
                    function showProjectModal() {
                        XNAT.dialog.message.open({
                            title: 'Enabled Projects',
                            content: '<ul><li>' + projects.join('</li><li>') + '</li></ul>',
                        });
                    }
                    
                    return spawn('span.show-enabled-projects', {
                            style: {
                                'border-bottom': '1px #ccc dashed',
                                'cursor': 'pointer'
                            },
                            data: { 'projects': projects.join(',') },
                            title: 'Click to view projects',
                            onclick: showProjectModal
                        },
                        `${projects.length} Projects Enabled`
                    );
                } else {
                    if (projects.length === 0) {
                        return 'No Projects Enabled';
                    }
                    
                    return projects.sort().join(', ');
                }
            }
        }
        
        const enabledToggle = (config) => {
            let enabled = config['scopes']['Site']['enabled'];
            let ckbox = spawn('input', {
                type: 'checkbox',
                checked: enabled,
                value: enabled ? 'true' : 'false',
                data: { checked: enabled },
                onchange: () => {
                    let enabled = ckbox.checked;
                    config['scopes']['Site']['enabled'] = enabled;
                    
                    XNAT.compute.constraintConfigs.update(config).then(() => {
                        XNAT.ui.banner.top(2000, `Constraint ${enabled ? 'Enabled' : 'Disabled'}`, 'success');
                    }).catch((err) => {
                        console.error(err);
                        XNAT.ui.banner.top(4000,
                            `Error ${enabled ? 'Enabling' : 'Disabling'} Constraint`,
                            'error'
                        );
                        toggleCheckbox(!enabled);
                    });
                }
            });
            
            let toggleCheckbox = (enabled) => {
                ckbox.checked = enabled;
                ckbox.value = enabled ? 'true' : 'false';
                ckbox.dataset.checked = enabled;
            }
            
            return spawn('label.switchbox', [ckbox, ['span.switchbox-outer', [['span.switchbox-inner']]]]);
        }
        
        const editor = (config, mode) => {
            console.debug(`Opening constraint config editor in ${mode} mode`);
            
            let isNew  = mode === 'new',
                isEdit = mode === 'edit',
                isCopy = mode === 'copy';
            
            let title = isNew || isCopy ? 'New Constraint' : 'Edit Constraint';
            
            XNAT.dialog.open({
                title: title,
                content: spawn('div', { id: 'config-editor' }),
                width: 650,
                maxBtn: true,
                beforeShow: () => {
                    const form = document.getElementById('config-editor');
                    form.classList.add('panel');
                    
                    let id = isNew || isCopy ? '' : config.id;
                    let attribute = isNew ? '' : config.constraint?.key || '';
                    let operator = isNew ? 'IN' : config.constraint?.operator || 'IN';
                    let values = isNew ? '' : config.constraint?.values.join(',') || '';
                    
                    let siteEnabled = isNew || isCopy ? true : config.scopes?.Site?.enabled || false;
                    let allUsersEnabled = isNew ? true : config.scopes?.User?.enabled || false;
                    let enabledUsers = isNew ? [] : config.scopes?.User?.ids || [];
                    let allProjectsEnabled = isNew ? true : config.scopes?.Project?.enabled || false;
                    let enabledProjects = isNew ? [] : config.scopes?.Project?.ids || [];
                    
                    let idInput = XNAT.ui.panel.input.text({
                        name: 'id',
                        id: 'id',
                        label: 'ID *',
                        description: 'The ID of the constraint',
                        value: id,
                    });
                    
                    idInput.style.display = 'none'; // hide the ID field
                    
                    let constraintHeader = spawn('div.placement-constraints.swarm', [
                        spawn('p', '<strong>Constraint</strong><br> Use this section to define a placement ' +
                            'constraint applicable to all jobs submitted to this cluster. ' +
                            'See Docker Swarm documentation on ' +
                            '<a href="https://docs.docker.com/engine/swarm/services/#placement-constraints" target="_blank">service placement constraints</a> and the ' +
                            '<a href="https://docs.docker.com/engine/reference/commandline/service_create/#specify-service-constraints---constraint" target="_blank">docker service create command</a> ' +
                            'for more information about allowed constraints.'),
                    ]);
                    
                    let attributeInput = XNAT.ui.panel.input.text({
                        name: `constraint-attribute`,
                        label: 'Node attribute',
                        classes: 'required swarm-constraint-attribute',
                        description: 'Attribute you wish to constrain. E.g., node.labels.mylabel or node.role',
                        value: attribute,
                    });
                    
                    let operatorInput = XNAT.ui.panel.input.radioGroup({
                        name: `constraint-operator`, // random ID to avoid collisions
                        label: 'Comparator',
                        classes: 'required swarm-constraint-operator',
                        items: { 0: { label: 'Equals', value: 'IN' }, 1: { label: 'Does not equal', value: 'NOT_IN' } },
                        value: operator,
                    })
                    
                    let valuesInput = XNAT.ui.panel.input.list({
                        name: `constraint-values`, // random ID to avoid collisions
                        label: 'Possible Values For Constraint',
                        classes: 'required swarm-constraint-values',
                        description: 'Comma-separated list of values on which user can constrain the attribute (or a single value if not user-settable). E.g., "worker" or "spot,demand" (do not add quotes). ',
                        value: values,
                    })
                    
                    let projectScopesDescription = spawn('div.user-and-project-scopes', [
                        spawn('p', '<strong>Projects</strong><br> Use this section to limit the scope of a constraint to ' +
                            "specific projects (and it's subjects and experiments). "),
                    ]);
                    
                    let siteEnabledInput = XNAT.ui.panel.input.checkbox({
                        name: 'siteEnabled',
                        id: 'siteEnabled',
                        label: 'Site Enabled',
                        description: 'Enable this hardware for all users and projects',
                        value: siteEnabled,
                    });
                    
                    siteEnabledInput.style.display = 'none'; // hide the site enabled field
                    
                    let allUsersEnabledInput = XNAT.ui.panel.input.checkbox({
                        name: 'allUsersEnabled',
                        id: 'allUsersEnabled',
                        label: 'All Users',
                        description: 'Enable this hardware for all users',
                        value: allUsersEnabled,
                    });
                    
                    allUsersEnabledInput.style.display = 'none'; // hide the all users enabled field
                    
                    let userOptions = users.map((user) => {
                        return {
                            value: user,
                            label: user,
                            selected: enabledUsers.includes(user)
                        }
                    });
                    
                    let userSelect = XNAT.ui.panel.select.multiple({
                        name: 'enabledUsers',
                        id: 'enabledUsers',
                        label: 'Users',
                        description: 'Select the users to enable this hardware for',
                        options: userOptions,
                    });
                    
                    userSelect.style.display = 'none'; // hide the user select
                    
                    // Disable the user select if all users are enabled
                    allUsersEnabledInput.querySelector('input').addEventListener('change', (event) => {
                        userSelect.querySelector('select').disabled = allUsersEnabledInput.querySelector('input').checked;
                    });
                    
                    userSelect.querySelector('select').disabled = allUsersEnabledInput.querySelector('input').checked;
                    
                    // The user select is too small by default
                    userSelect.querySelector('select').style.minWidth = '200px';
                    userSelect.querySelector('select').style.minHeight = '125px';
                    
                    let allProjectsEnabledInput = XNAT.ui.panel.input.checkbox({
                        name: 'allProjectsEnabled',
                        id: 'allProjectsEnabled',
                        label: 'All Projects',
                        description: 'Enable this constraint for all projects',
                        value: allProjectsEnabled,
                    });
                    
                    let projectOptions = projects.map((project) => {
                        return {
                            value: project,
                            label: project,
                            selected: enabledProjects.includes(project)
                        }
                    });
                    
                    let projectSelect = XNAT.ui.panel.select.multiple({
                        name: 'enabledProjects',
                        id: 'enabledProjects',
                        label: 'Projects',
                        description: 'Select the projects to enable this constraint for',
                        options: projectOptions,
                    });
                    
                    // Disable the project select if all projects are enabled
                    allProjectsEnabledInput.querySelector('input').addEventListener('change', (event) => {
                        projectSelect.querySelector('select').disabled = allProjectsEnabledInput.querySelector('input').checked;
                    });
                    
                    projectSelect.querySelector('select').disabled = allProjectsEnabledInput.querySelector('input').checked;
                    
                    // The project select is too small by default
                    projectSelect.querySelector('select').style.minWidth = '200px';
                    projectSelect.querySelector('select').style.minHeight = '125px';
                    
                    let formFields = [
                        idInput,
                        constraintHeader,
                        attributeInput,
                        operatorInput,
                        valuesInput,
                        spawn('hr'),
                        projectScopesDescription,
                        siteEnabledInput,
                        allUsersEnabledInput,
                        userSelect,
                        allProjectsEnabledInput,
                        projectSelect,
                    ];
                    
                    form.appendChild(spawn('!', formFields));
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function () {
                            const form = document.getElementById('config-editor');
                            
                            let id = form.querySelector('#id');
                            let attribute = form.querySelector('.swarm-constraint-attribute');
                            let operator = form.querySelector('.swarm-constraint-operator input:checked');
                            let values = form.querySelector('.swarm-constraint-values');
                            
                            let siteEnabledElement = form.querySelector('#siteEnabled');
                            let allUsersEnabledElement = form.querySelector('#allUsersEnabled');
                            let enabledUsersElement = form.querySelector('#enabledUsers');
                            let allProjectsEnabledElement = form.querySelector('#allProjectsEnabled');
                            let enabledProjectsElement = form.querySelector('#enabledProjects');
                            
                            let validators = []
                            
                            let validateAttribute = XNAT.validate(attribute)
                                                        .reset().chain()
                                                        .is('notEmpty').failure('Attribute is required');
                            validators.push(validateAttribute);
                            
                            let validateValues = XNAT.validate(values)
                                                     .reset().chain()
                                                     .is('notEmpty').failure('Value is required');
                            validators.push(validateValues);
                            
                            let errors = [];
                            
                            validators.forEach((validator) => {
                                if (!validator.check()) {
                                    validator.messages.forEach(message => errors.push(message));
                                }
                            });
                            
                            if (errors.length > 0) {
                                XNAT.dialog.open({
                                    title: 'Error',
                                    width: 400,
                                    content: '<ul><li>' + errors.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }
                            
                            config = {
                                id: id.value,
                                constraint: {
                                    key: attribute.value,
                                    operator: operator.value,
                                    values: values.value.split(',').map((value) => value.trim()),
                                },
                                scopes: {
                                    Site: {
                                        scope: 'Site',
                                        enabled: siteEnabledElement.checked,
                                    },
                                    Project: {
                                        scope: 'Project',
                                        enabled: allProjectsEnabledElement.checked,
                                        ids: Array.from(enabledProjectsElement.selectedOptions).map(option => option.value),
                                    },
                                    User: {
                                        scope: 'User',
                                        enabled: allUsersEnabledElement.checked,
                                        ids: Array.from(enabledUsersElement.selectedOptions).map(option => option.value),
                                    },
                                }
                            }
                            
                            XNAT.compute.constraintConfigs.save(config).then(() => {
                                XNAT.ui.banner.top(2000, 'Saved constraint config', 'success');
                                XNAT.dialog.closeAll();
                                refreshTable();
                            }).catch(err => {
                                console.error(err);
                                XNAT.ui.banner.top(4000, 'Error saving constraint config', 'error');
                            });
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true,
                        isDefault: false
                    }
                ]
            });
        }
        
        const refreshTable = () => {
            XNAT.compute.constraintConfigs.getAll().then(data => {
                constraintConfigs = data;
                
                if (constraintConfigs.length === 0) {
                    clearContainer();
                    container.innerHTML = `<p>No constraints found</p>`;
                    return;
                }
                
                renderTable();
            }).catch(err => {
                console.error(err);
                clearContainer();
                container.innerHTML = `<p>Error fetching constraints</p>`;
            })
        }
        
        const renderTable = () => {
            let table = XNAT.table.dataTable(constraintConfigs, {
                header: true,
                sortable: 'nodeAttribute, comparator, values',
                columns: {
                    nodeAttribute: {
                        label: 'Node Attribute',
                        td: { className: 'word-wrapped align-top' },
                        apply: function() {
                            return this.constraint?.key;
                        }
                    },
                    comparator: {
                        label: 'Comparator',
                        td: { className: 'word-wrapped align-top' },
                        apply: function() {
                            return spawn('div.center', [this.constraint?.operator === 'IN' ? '==' : '!=']);
                        }
                    },
                    values: {
                        label: 'Constraint Value(s)',
                        td: { className: 'word-wrapped align-top' },
                        apply: function() {
                            return spawn('div.center', [this.constraint?.values?.join(', ')]);
                        }
                    },
                    projects: {
                        label: 'Project(s)',
                        td: { className: 'projects word-wrapped align-top' },
                        apply: function () {
                            return displayProjects(this);
                        }
                    },
                    enabled: {
                        label: 'Enabled',
                        apply: function () {
                            return spawn('div.center', [enabledToggle(this)]);
                        },
                    },
                    actions: {
                        label: 'Actions',
                        th: { style: { width: '150px' } },
                        apply: function () {
                            return spawn('div.center', [
                                spawn('button.btn.btn-sm',
                                    { onclick: () => editor(this, 'edit') },
                                    '<i class="fa fa-pencil" title="Edit"></i>'
                                ),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm',
                                    { onclick: () => editor(this, 'copy') },
                                    '<i class="fa fa-clone" title="Duplicate"></i>'
                                ),
                                spawn('span', { style: { display: 'inline-block', width: '4px' } }),
                                spawn('button.btn.btn-sm',
                                    { onclick: () => deleteConfig(this.id) },
                                    '<i class="fa fa-trash" title="Delete"></i>'
                                )
                            ]);
                        },
                    }
                }
            });
            
            clearContainer();
            table.render(`#${containerId}`);
        }
        
        init();
        
        return {
            container: container,
            constraintConfigs: constraintConfigs,
            refreshTable: refreshTable
        }
    }
    
}));