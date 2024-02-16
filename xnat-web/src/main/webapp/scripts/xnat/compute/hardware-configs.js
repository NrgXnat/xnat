console.debug("Loading hardware-configs.js");

var XNAT = getObject(XNAT || {});
XNAT.compute = getObject(XNAT.compute || {});
XNAT.compute.hardwareConfigs = getObject(XNAT.compute.hardwareConfigs || {});

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function () {
    
    XNAT.compute.hardwareConfigs.get = async (id) => {
        console.debug("Fetching hardware config with id: " + id);
        const url = XNAT.url.restUrl('/xapi/compute/hardware/' + id);
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error fetching hardware config with id: ' + id);
        }
    }
    
    XNAT.compute.hardwareConfigs.getAll = async () => {
        console.debug("Fetching all hardware configs");
        const url = XNAT.url.restUrl('/xapi/compute/hardware');
        
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error fetching all hardware configs');
        }
    }
    
    XNAT.compute.hardwareConfigs.create = async (config) => {
        console.debug("Creating hardware config");
        const url = XNAT.url.restUrl('/xapi/compute/hardware');
        
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error creating hardware config');
        }
    };
    
    XNAT.compute.hardwareConfigs.update = async (config) => {
        console.debug("Updating hardware config");
        const id = config.id;
        
        if (!id) {
            throw new Error('Hardware config id is required');
        }
        
        const url = XNAT.url.restUrl(`/xapi/compute/hardware/${id}`);
        
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(config)
        });
        
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Error updating hardware config');
        }
    }
    
    XNAT.compute.hardwareConfigs.save = async (config) => {
        console.debug("Saving hardware config");
        const id = config.id;
        
        if (!id) {
            return XNAT.compute.hardwareConfigs.create(config);
        } else {
            return XNAT.compute.hardwareConfigs.update(config);
        }
    }
    
    XNAT.compute.hardwareConfigs.delete = async (id) => {
        console.debug("Deleting hardware config with id: " + id);
        const url = XNAT.url.restUrl('/xapi/compute/hardware/' + id);
        
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Error deleting hardware config with id: ' + id);
        }
    }
    
    XNAT.compute.hardwareConfigs.manager = async (containerId) => {
        console.debug("Initializing hardware config manager");
        
        let container,
            footer,
            hardwareConfigs = [],
            users           = [],
            projects        = [];
        
        const init = () => {
            container = document.getElementById(containerId);
            
            if (!container) {
                throw new Error('Container element wtih id: ' + containerId + ' not found');
            }
            
            clearContainer();
            renderNewButton();
            
            loadUsers();
            loadProjects();
            
            refreshTable();
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
        
        const loadUsers = async () => {
            let response = await fetchWithTimeout(`/xapi/users`);
            
            if (response.ok) {
                let data = await response.json();
                users = data.filter(u => u !== 'jupyterhub' && u !== 'guest');
            } else {
                throw new Error(`Error fetching users`);
            }
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
        
        const clearContainer = () => {
            container.innerHTML = '';
        }
        
        const renderNewButton = () => {
            footer = container.closest('.panel').querySelector('.panel-footer');
            footer.innerHTML = '';
            
            let button = spawn('div', [
                spawn('div.pull-right', [
                    spawn('button.btn.btn-sm.submit', { html: 'New Hardware', onclick: () => editor(null, 'new') })
                ]),
                spawn('div.clear.clearFix')
            ])
            
            footer.appendChild(button);
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
                    
                    XNAT.compute.hardwareConfigs.update(config).then(() => {
                        XNAT.ui.banner.top(2000, `Hardware ${enabled ? 'Enabled' : 'Disabled'}`, 'success');
                    }).catch((err) => {
                        console.error(err);
                        XNAT.ui.banner.top(4000,
                            `Error ${enabled ? 'Enabling' : 'Disabling'} Hardware`,
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
        
        const displayUsers = (config) => {
            let isAllUsersEnabled = config['scopes']['User']['enabled'];
            let users = config['scopes']['User']['ids'];
            
            if (isAllUsersEnabled) {
                return 'All Users';
            } else {
                if (users.length > 4) {
                    function showUserModal() {
                        XNAT.dialog.message.open({
                            title: 'Enabled Users',
                            content: '<ul><li>' + users.join('</li><li>') + '</li></ul>',
                        });
                    }
                    
                    return spawn('span.show-enabled-users', {
                            style: {
                                'border-bottom': '1px #ccc dashed',
                                'cursor': 'pointer'
                            },
                            data: { 'users': users.join(',') },
                            title: 'Click to view users',
                            onclick: showUserModal
                        },
                        `${users.length} Users Enabled`
                    );
                } else {
                    if (users.length === 0) {
                        return 'No Users Enabled';
                    }
                    
                    return users.sort().join(', ');
                }
            }
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
        
        const editor = (config, mode) => {
            console.debug(`Opening hardware config editor in ${mode} mode`);
            
            let isNew  = mode === 'new',
                isEdit = mode === 'edit',
                isCopy = mode === 'copy';
            
            let title = isNew || isCopy ? 'New Hardware' : 'Edit Hardware';
            
            XNAT.dialog.open({
                title: title,
                content: spawn('div', { id: 'config-editor' }),
                width: 650,
                maxBtn: true,
                beforeShow: () => {
                    const form = document.getElementById('config-editor');
                    form.classList.add('panel');
                    
                    let id = isNew || isCopy ? '' : config.id;
                    let name = isNew || isCopy ? '' : config.hardware?.name;
                    
                    let cpuLimit = isNew ? '' : config.hardware?.cpuLimit || '';
                    let cpuReservation = isNew ? '' : config.hardware?.cpuReservation || '';
                    let memoryLimit = isNew ? '' : config.hardware?.memoryLimit || '';
                    let memoryReservation = isNew ? '' : config.hardware?.memoryReservation || '';
                    
                    let environmentVariables = isNew ? [] : config.hardware?.environmentVariables || [];
                    let constraints = isNew ? [] : config.hardware?.constraints || [];
                    let genericResources = isNew ? [] : config.hardware?.genericResources || [];
                    
                    let siteEnabled = isNew || isCopy ? true : config.scopes?.Site?.enabled || false;
                    let allUsersEnabled = isNew ? true : config.scopes?.User?.enabled || false;
                    let enabledUsers = isNew ? [] : config.scopes?.User?.ids || [];
                    let allProjectsEnabled = isNew ? true : config.scopes?.Project?.enabled || false;
                    let enabledProjects = isNew ? [] : config.scopes?.Project?.ids || [];
                    
                    let idInput = XNAT.ui.panel.input.text({
                        name: 'id',
                        id: 'id',
                        label: 'ID *',
                        description: 'The ID of the hardware configuration',
                        value: id,
                    });
                    
                    idInput.style.display = 'none'; // hide the ID field
                    
                    let nameInput = XNAT.ui.panel.input.text({
                        name: 'name',
                        id: 'name',
                        label: 'Name *',
                        description: 'Provide a user-friendly name for this hardware',
                        value: name,
                    });
                    
                    let cpuAndMemDescriptionSwarm = spawn('div.cpu-mem-header.swarm', [
                        spawn('p', '<strong>CPU and Memory</strong><br> By default, a container has no resource ' +
                            'constraints and can use as much of a given resource as the host’s kernel scheduler allows.' +
                            ' Docker provides ways to control how much memory, or CPU a container can use, setting ' +
                            'runtime configuration flags of the docker run command. ' +
                            'For more information, see the Docker documentation on ' +
                            '<a href="https://docs.docker.com/config/containers/resource_constraints/" target="_blank">' +
                            'resource constraints</a>.'),
                    ]);
                    
                    let cpuReservationInput = XNAT.ui.panel.input.text({
                        name: 'cpuReservation',
                        id: 'cpuReservation',
                        label: 'CPU Reservation',
                        description: 'The number of CPUs to reserve',
                        value: cpuReservation,
                    });
                    
                    let cpuLimitInput = XNAT.ui.panel.input.text({
                        name: 'cpuLimit',
                        id: 'cpuLimit',
                        label: 'CPU Limit',
                        description: 'The maximum number of CPUs that can be used',
                        value: cpuLimit,
                    });
                    
                    let memoryReservationInput = XNAT.ui.panel.input.text({
                        name: 'memoryReservation',
                        id: 'memoryReservation',
                        label: 'Memory Reservation',
                        description: 'The amount of memory that this container will reserve. Allows for suffixes like "0.5G" or "512M".',
                        value: memoryReservation,
                    });
                    
                    let memoryLimitInput = XNAT.ui.panel.input.text({
                        name: 'memoryLimit',
                        id: 'memoryLimit',
                        label: 'Memory Limit',
                        description: 'The maximum amount of memory that this container can use. Allows for suffixes like "8G" or "4G".',
                        value: memoryLimit,
                    });
                    
                    let environmentVariablesHeaderEl = spawn('div.environment-variables', [
                        spawn('p', '<strong>Environment Variables</strong><br> Use this section to define additional ' +
                            'environment variables for the container.'),
                    ]);
                    
                    let addEnvironmentVariableButtonEl = spawn('button.btn.btn-sm.add-environment-variable', {
                        html: 'Add Environment Variable',
                        style: { 'margin-top': '0.75em' },
                        onclick: () => {
                            addEnvironmentVariable();
                        }
                    });
                    
                    let genericResourcesDescription = spawn('div.generic-resources.swarm', [
                        spawn('p', '<strong>Generic Resources</strong><br> Use this section to request generic ' +
                            'resources when the container is scheduled. ' +
                            'See Docker Swarm documentation on ' +
                            '<a href="https://docs.docker.com/engine/reference/commandline/service_create/#generic-resources" target="_blank">generic resources</a> ' +
                            'for more information about allowed constraints.'),
                    ]);
                    
                    let addGenericResourceButton = spawn('button.btn.btn-sm.add-generic-resource.swarm', {
                        html: 'Add Generic Resource',
                        style: { 'margin-top': '0.75em' },
                        onclick: () => {
                            addGenericResource();
                        }
                    });
                    
                    let placementConstraintHeaderEl = spawn('div.placement-constraints.swarm', [
                        spawn('p', '<strong>Placement Constraints</strong><br> Use this section to define placement ' +
                            'constraints when the container is scheduled. ' +
                            'See Docker Swarm documentation on ' +
                            '<a href="https://docs.docker.com/engine/swarm/services/#placement-constraints" target="_blank">service placement constraints</a> and the ' +
                            '<a href="https://docs.docker.com/engine/reference/commandline/service_create/#specify-service-constraints---constraint" target="_blank">docker service create command</a> ' +
                            'for more information about allowed constraints.'),
                    ]);
                    
                    let addSwarmConstraintButtonEl = spawn('button.btn.btn-sm.add-placement-constraint.swarm', {
                        html: 'Add Constraint',
                        style: { 'margin-top': '0.75em' },
                        onclick: () => {
                            addSwarmConstraint();
                        }
                    });
                    
                    let userAndProjectScopesDescription = spawn('div.user-and-project-scopes', [
                        spawn('p', '<strong>Projects and Users</strong><br> Use this section to define which projects ' +
                            'and users will have access to this hardware.'),
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
                        description: 'Enable this hardware for all projects',
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
                        description: 'Select the projects to enable this hardware for',
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
                        nameInput,
                        spawn('hr'),
                        cpuAndMemDescriptionSwarm,
                        cpuReservationInput,
                        cpuLimitInput,
                        memoryReservationInput,
                        memoryLimitInput,
                        spawn('hr'),
                        environmentVariablesHeaderEl,
                        addEnvironmentVariableButtonEl,
                        spawn('hr'),
                        genericResourcesDescription,
                        addGenericResourceButton,
                        spawn('hr'),
                        placementConstraintHeaderEl,
                        addSwarmConstraintButtonEl,
                        spawn('hr'),
                        userAndProjectScopesDescription,
                        siteEnabledInput,
                        allProjectsEnabledInput,
                        projectSelect,
                        allUsersEnabledInput,
                        userSelect,
                    ];
                    
                    form.appendChild(spawn('!', [
                        ...formFields
                    ]));
                    
                    // add the environment variables
                    environmentVariables.forEach((environmentVariable) => {
                        addEnvironmentVariable(environmentVariable);
                    });
                    
                    // add the placement constraints
                    constraints.forEach((constraint) => {
                        addSwarmConstraint(constraint);
                    });
                    
                    // add the generic resources
                    genericResources.forEach((genericResource) => {
                        addGenericResource(genericResource);
                    });
                },
                buttons: [
                    {
                        label: 'Save',
                        isDefault: true,
                        close: false,
                        action: function () {
                            const form = document.getElementById('config-editor');
                            
                            const idElement = form.querySelector('#id');
                            const nameElement = form.querySelector('#name');
                            
                            const cpuReservationElement = form.querySelector('#cpuReservation');
                            const cpuLimitElement = form.querySelector('#cpuLimit');
                            const memoryReservationElement = form.querySelector('#memoryReservation');
                            const memoryLimitElement = form.querySelector('#memoryLimit');
                            
                            const siteEnabledElement = form.querySelector('#siteEnabled');
                            const allUsersEnabledElement = form.querySelector('#allUsersEnabled');
                            const enabledUsersElement = form.querySelector('#enabledUsers');
                            const allProjectsEnabledElement = form.querySelector('#allProjectsEnabled');
                            const enabledProjectsElement = form.querySelector('#enabledProjects');
                            
                            const validators = [];
                            
                            let validateNameElement = XNAT.validate(nameElement).reset().chain();
                            validateNameElement.is('notEmpty').failure('Name is required');
                            validators.push(validateNameElement);
                            
                            let validateCpuReservationElement = XNAT.validate(cpuReservationElement).reset().chain();
                            validateCpuReservationElement.is('allow-empty')
                                                         .is('decimal')
                                                         .is('greater-than', 0)
                                                         .failure('CPU Reservation must be a positive number or empty');
                            validators.push(validateCpuReservationElement);
                            
                            let validateCpuLimitElement = XNAT.validate(cpuLimitElement).reset().chain();
                            validateCpuLimitElement.is('allow-empty')
                                                    .is('decimal')
                                                    .is('greater-than', 0)
                                                    .failure('CPU Limit must be a positive number or empty');
                            validators.push(validateCpuLimitElement);
                            
                            let validateMemoryReservationElement = XNAT.validate(memoryReservationElement).reset().chain();
                            validateMemoryReservationElement.is('allow-empty')
                                                            .is('regex', /^$|^([1-9]+[0-9]*)+[KMGT]$/) // 512M, 2G, etc.
                                                            .failure('Memory Reservation must be a number followed by a suffix of K, M, G, or T or be empty');
                            validators.push(validateMemoryReservationElement);
                            
                            let validateMemoryLimitElement = XNAT.validate(memoryLimitElement).reset().chain();
                            validateMemoryLimitElement.is('allow-empty')
                                .is('regex', /^$|^([1-9]+[0-9]*)+[KMGT]$/) // 512M, 2G, etc.
                                .failure('Memory Limit must be a number followed by a suffix of K, M, G, or T or be empty');
                            validators.push(validateMemoryLimitElement);
                            
                            let envVarsPresent = document.querySelectorAll('input.key').length > 0;
                            if (envVarsPresent) {
                                let validateEnvironmentVariableKeys = XNAT.validate('input.key').chain();
                                validateEnvironmentVariableKeys.is('notEmpty')
                                                               .is('regex', /^[a-zA-Z_][a-zA-Z0-9_]*$/) // must start with a letter or underscore, and only contain letters, numbers, and underscores
                                                               .failure('Keys are required and must be a valid environment variable name');
                                validators.push(validateEnvironmentVariableKeys);
                            }
                            
                            let swarmConstraintsPresent = document.querySelectorAll('div.swarm-constraint-group').length > 0;
                            if (swarmConstraintsPresent) {
                                let validateSwarmConstraintAttributes = XNAT.validate('input.swarm-constraint-attribute').chain();
                                validateSwarmConstraintAttributes.is('notEmpty')
                                                                 .is('regex', /^[a-zA-Z_][a-zA-Z0-9_.-]*$/) // must start with a letter or underscore, and only contain letters, numbers, underscores, hyphens, and periods
                                                                 .failure('Attributes are required and must be a valid swarm constraint attribute');
                                validators.push(validateSwarmConstraintAttributes);
                                
                                let validateSwarmConstraintValues = XNAT.validate('input.swarm-constraint-values').chain();
                                validateSwarmConstraintValues.is('notEmpty')
                                                             .failure('Constraint values are required');
                                validators.push(validateSwarmConstraintValues);
                            }
                            
                            let genericResourcesPresent = document.querySelectorAll('div.resource-group').length > 0;
                            if (genericResourcesPresent) {
                                let validateGenericResourceNames = XNAT.validate('input.resource-name').chain();
                                validateGenericResourceNames.is('notEmpty').failure('Resource names are required')
                                                            .is('regex', /^[a-zA-Z_][a-zA-Z0-9_.-]*$/).failure('Invalid resource name');
                                validators.push(validateGenericResourceNames);
                                
                                let validateGenericResourceValues = XNAT.validate('input.resource-value').chain();
                                validateGenericResourceValues.is('notEmpty').failure('Resource values are required');
                                validators.push(validateGenericResourceValues);
                            }

                            // Validate the form
                            let errorMessages = [];
                            
                            validators.forEach((validator) => {
                                if (!validator.check()) {
                                    validator.messages.forEach(message => errorMessages.push(message));
                                }
                            });
                            
                            if (errorMessages.length > 0) {
                                XNAT.dialog.open({
                                    title: 'Error',
                                    width: 400,
                                    content: '<ul><li>' + errorMessages.join('</li><li>') + '</li></ul>',
                                })
                                return;
                            }
                            
                            config = {
                                id: idElement.value,
                                hardware: {
                                    name: nameElement.value,
                                    cpuReservation: cpuReservationElement.value,
                                    cpuLimit: cpuLimitElement.value,
                                    memoryReservation: memoryReservationElement.value,
                                    memoryLimit: memoryLimitElement.value,
                                    environmentVariables: getEnvironmentVariables(),
                                    constraints: getSwarmConstraints(),
                                    genericResources: getGenericResources(),
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
                            
                            XNAT.compute.hardwareConfigs.save(config).then(() => {
                                XNAT.ui.banner.top(2000, 'Hardware saved', 'success');
                                XNAT.dialog.closeAll();
                                refreshTable();
                            }).catch(err => {
                                XNAT.ui.banner.top(4000, 'Error Saving Hardware', 'error');
                                console.error(err);
                            });
                        }
                    },
                    {
                        label: 'Cancel',
                        close: true,
                        isDefault: false
                    }
                ]
            })
            
        }
        
        const refreshTable = () => {
            XNAT.compute.hardwareConfigs.getAll().then(data => {
                hardwareConfigs = data;
                
                if (hardwareConfigs.length === 0) {
                    clearContainer();
                    container.innerHTML = `<p>No Hardware found</p>`;
                    return;
                }
                
                // Sort the hardware configs by name
                hardwareConfigs = hardwareConfigs.sort((a, b) => {
                    if (a.name < b.name) {
                        return -1;
                    }
                    if (a.name > b.name) {
                        return 1;
                    }
                    return 0;
                });
                
                renderTable();
            }).catch(err => {
                console.error(err);
                clearContainer();
                container.innerHTML = `<p>Error loading Hardware</p>`;
            });
        }
        
        const deleteConfig = (id) => {
            XNAT.compute.hardwareConfigs.delete(id).then(() => {
                XNAT.ui.banner.top(2000, 'Hardware config deleted', 'success');
                refreshTable();
            }).catch(err => {
                XNAT.ui.banner.top(4000, 'Error deleting hardware config', 'error');
                console.error(err);
            });
        }
        
        const addEnvironmentVariable = (envVar) => {
            const formEl = document.getElementById('config-editor');
            const environmentVariablesEl = formEl.querySelector('div.environment-variables');
            
            const keyEl = spawn('input.form-control.key', {
                id: 'key',
                placeholder: 'Key',
                type: 'text',
                value: envVar ? envVar.key : '',
            });
            
            const equalsEl = spawn('span.input-group-addon', {
                style: {
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                }
            }, ['=']);
            
            const valueEl = spawn('input.form-control.value', {
                id: 'value',
                placeholder: 'Value',
                type: 'text',
                value: envVar ? envVar.value : '',
            });
            
            const removeButtonEl = spawn('button.btn.btn-danger', {
                type: 'button',
                title: 'Remove',
                onclick: () => {
                    environmentVariableEl.remove();
                }
            }, [
                spawn('i.fa.fa-trash'),
            ]);
            
            const environmentVariableEl = spawn('div.form-group', {
                style: {
                    marginBottom: '5px',
                }
            }, [
                spawn('div.input-group', {
                    style: {
                        display: 'flex',
                        flexDirection: 'row',
                        justifyContent: 'center',
                        columnGap: '10px',
                    }
                }, [
                    keyEl,
                    equalsEl,
                    valueEl,
                    spawn('span.input-group-btn', [
                        removeButtonEl,
                    ]),
                ]),
            ]);
            
            environmentVariablesEl.appendChild(environmentVariableEl);
        }
        
        const getEnvironmentVariables = () => {
            const formEl = document.getElementById('config-editor');
            const environmentVariablesEl = formEl.querySelector('div.environment-variables');
            
            let environmentVariables = [];
            
            Array.from(environmentVariablesEl.children).forEach((environmentVariableEl) => {
                const keyEl = environmentVariableEl.querySelector('#key');
                const valueEl = environmentVariableEl.querySelector('#value');
                
                if (keyEl === null || valueEl === null) return;
                
                environmentVariables.push({
                    key: keyEl.value,
                    value: valueEl.value,
                });
            });
            
            return environmentVariables;
        }
        
        const addSwarmConstraint = (placementConstraint) => {
            const formEl = document.getElementById('config-editor');
            const placementConstraintsEl = formEl.querySelector('div.placement-constraints.swarm');
            
            let removeButton = spawn('a.close', {
                html: '<i class="fa fa-close" title="Remove Constraint" style="float: right;"></i>',
                onclick: () => {
                    placementConstraintGroup.remove();
                }
            });
            
            let attributeInput = XNAT.ui.panel.input.text({
                name: `swarm-constraint-attribute-${Math.floor(Math.random() * 1000000)}`, // random ID to avoid collisions
                label: 'Node attribute',
                classes: 'required swarm-constraint-attribute',
                description: 'Attribute you wish to constrain. E.g., node.labels.mylabel or node.role',
                value: placementConstraint ? placementConstraint.key : '',
            });
            
            let operatorInput = XNAT.ui.panel.input.radioGroup({
                name: `constraint-operator-${Math.floor(Math.random() * 1000000)}`, // random ID to avoid collisions
                label: 'Comparator',
                classes: 'required swarm-constraint-operator',
                items: { 0: { label: 'Equals', value: 'IN' }, 1: { label: 'Does not equal', value: 'NOT_IN' } },
                value: placementConstraint ? placementConstraint.operator : 'IN',
            })
            
            let valuesInput = XNAT.ui.panel.input.list({
                name: `placement-constraint-values-${Math.floor(Math.random() * 1000000)}`, // random ID to avoid collisions
                label: 'Possible Values For Constraint',
                classes: 'required swarm-constraint-values',
                description: 'Comma-separated list of values on which user can constrain the attribute (or a single value if not user-settable). E.g., "worker" or "spot,demand" (do not add quotes). ',
                value: placementConstraint ? placementConstraint.values.join(',') : '',
            })
            
            let placementConstraintGroup = spawn('div.swarm-constraint-group', {
                style: {
                    border: '1px solid #ccc',
                    padding: '5px',
                    margin: '5px',
                    borderRadius: '10px',
                }
            }, [
                spawn('div.input-group', [
                    removeButton,
                    attributeInput,
                    operatorInput,
                    valuesInput,
                ]),
            ]);
            
            placementConstraintsEl.appendChild(placementConstraintGroup);
        }
        
        const getSwarmConstraints = () => {
            const formEl = document.getElementById('config-editor');
            const constraintGroups = formEl.querySelectorAll('.swarm-constraint-group');
            
            let placementConstraints = [];
            
            Array.from(constraintGroups).forEach((group) => {
                if (group === null) return;
                
                const attributeEl = group.querySelector('.swarm-constraint-attribute');
                const operatorEl = group.querySelector('.swarm-constraint-operator input:checked');
                const valuesEl = group.querySelector('.swarm-constraint-values');
                
                if (attributeEl === null || operatorEl === null || valuesEl === null) return;
                
                placementConstraints.push({
                    key: attributeEl.value,
                    operator: operatorEl.value,
                    values: valuesEl.value.split(',').map((value) => value.trim()),
                });
            });
            
            return placementConstraints;
        }
        
        const addGenericResource = (resource) => {
            const formEl = document.getElementById('config-editor');
            const resourcesEl = formEl.querySelector('div.generic-resources');
            
            let removeButton = spawn('a.close', {
                html: '<i class="fa fa-close" title="Remove Resource" style="float: right;"></i>',
                onclick: () => {
                    resourceGroup.remove();
                }
            });
            
            let nameInput = XNAT.ui.panel.input.text({
                name: `resource-name-${Math.floor(Math.random() * 1000000)}`, // random ID to avoid collisions
                label: 'Name',
                classes: 'required resource-name',
                description: 'Name of the resource. E.g., GPU',
                value: resource ? resource.name : '',
            });
            
            let valueInput = XNAT.ui.panel.input.text({
                name: `resource-value-${Math.floor(Math.random() * 1000000)}`, // random ID to avoid collisions
                label: 'Value',
                classes: 'required resource-value',
                description: 'Value of the resource. E.g., 1',
                value: resource ? resource.value : '',
            });
            
            let resourceGroup = spawn('div.resource-group', {
                style: {
                    border: '1px solid #ccc',
                    padding: '5px',
                    margin: '5px',
                    borderRadius: '10px',
                }
            }, [
                spawn('div.input-group', [
                    removeButton,
                    nameInput,
                    valueInput,
                ]),
            ]);
            
            resourcesEl.appendChild(resourceGroup);
        }
        
        const getGenericResources = () => {
            const formEl = document.getElementById('config-editor');
            const resourceGroups = formEl.querySelectorAll('.resource-group');
            
            let resources = [];
            
            Array.from(resourceGroups).forEach((group) => {
                if (group === null) return;
                
                const nameEl = group.querySelector('.resource-name');
                const valueEl = group.querySelector('.resource-value');
                
                if (nameEl === null || valueEl === null) return;
                
                resources.push({
                    name: nameEl.value,
                    value: valueEl.value,
                });
            });
            
            return resources;
        }
        
        const renderTable = () => {
            let table = XNAT.table.dataTable(hardwareConfigs, {
                header: true,
                sortable: 'name',
                columns: {
                    name: {
                        label: 'Name',
                        filter: true,
                        td: { className: 'word-wrapped align-top' },
                        apply: function () {
                            return this.hardware?.name || 'N/A';
                        }
                    },
                    cpu: {
                        label: 'CPU Res / Lim',
                        filter: true,
                        apply: function () {
                            let cpuReservation = this.hardware?.cpuReservation || '-';
                            let cpuLimit = this.hardware?.cpuLimit || '-';
                            return spawn('div.center', `${cpuReservation} / ${cpuLimit}`);
                        }
                    },
                    memory: {
                        label: 'Memory Res / Lim',
                        filter: true,
                        apply: function () {
                            let memoryReservation = this.hardware?.memoryReservation || '-';
                            let memoryLimit = this.hardware?.memoryLimit || '-';
                            return spawn('div.center', `${memoryReservation} / ${memoryLimit}`);
                        }
                    },
                    projects: {
                        label: 'Project(s)',
                        filter: true,
                        td: { className: 'projects word-wrapped align-top' },
                        apply: function () {
                            return displayProjects(this);
                        }
                    },
                    users: {
                        label: 'User(s)',
                        filter: true,
                        td: { className: 'users word-wrapped align-top' },
                        apply: function () {
                            return displayUsers(this);
                        },
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
            })
            
            clearContainer();
            table.render(`#${containerId}`);
        }
        
        init();
        
        return {
            container: container,
            hardwareConfigs: hardwareConfigs,
            refreshTable: refreshTable
        }
    }
    
}));