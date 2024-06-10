
var XNAT = getObject(XNAT || {});

(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(factory);
    } else if (typeof exports === 'object') {
        module.exports = factory();
    } else {
        return factory();
    }
}(function() {

    XNAT.customFormManager =
        getObject(XNAT.customFormManager || {});


    let builderConfigManager;
    XNAT.customFormManager.builderConfigManager = builderConfigManager = getObject(XNAT.customFormManager.builderConfigManager || {});

    builderConfigManager.getBuilderConfig = function() {
        let builderOptions = {};
        builderOptions.basic = false;
        builderOptions.advanced = false;
        builderOptions.layout = false;
        builderOptions.premium = false;
        builderOptions.data = false;

        let customBasic = {};
        customBasic.title = 'Basic';
        customBasic.default = false;
        customBasic.weight = 0;
        let customBasicComponents = {};
        let index = 100;
        [
            'checkbox',
            'xnatNumber',
            'xnatRadio',
            'xnatSelect',
            'textarea',
            'textfield'
        ].forEach(name => {
            createComponent(name, index++);
            customBasicComponents[name] = true;
        });
        customBasic.components = customBasicComponents;
        builderOptions.customBasic = customBasic;

        let customAdvanced = {};
        customAdvanced.title = 'Advanced';
        customAdvanced.default = false;
        customAdvanced.weight = 10;
        let customAdvancedComponents = {};
        index = 200;
        [
            'day',
            'datetime',
            'email',
            'phoneNumber',
            'survey',
            'time'
        ].forEach(name => {
            createComponent(name, index++);
            customAdvancedComponents[name] = true;
        });
        customAdvanced.components = customAdvancedComponents;
        builderOptions.customAdvanced = customAdvanced;

        let customLayout = {};
        customLayout.title = 'Layout';
        customLayout.default = false;
        customLayout.weight = 20;
        let customLayoutComponents = {};
        index = 300;
        [
            'columns',
            'fieldset',
            'htmlelement',
            'panel',
            'table',
            'tabs',
            'well'
        ].forEach(name => {
            createComponent(name, index++);
            customLayoutComponents[name] = true;
        });
        customLayout.components = customLayoutComponents;
        builderOptions.customLayout = customLayout;

        let customNonSearchable = {};
        customNonSearchable.title = 'Non-searchable';
        customNonSearchable.default = false;
        customNonSearchable.weight = 50;
        let customNonSearchableComponents = {};
        index = 500;
        [
            'address',
            'container',
            'datagrid',
            'datamap',
            'editgrid'
        ].forEach(name => {
            createComponent(name, index++);
            customNonSearchableComponents[name] = true;
        });
        customNonSearchable.components = customNonSearchableComponents;
        builderOptions.customNonSearchable = customNonSearchable;
        return builderOptions;
    }

    function createComponent(name, componentWeight) {
        let component = Formio.Components.components[name].builderInfo;
        if (name === "container") {
          component.schema.hideLabel = false;
        }  
        component.weight = componentWeight;
        Object.defineProperty(Formio.Components.components[name], 'builderInfo', {
            get() {
                return component;
            }
        });
    }

}));
