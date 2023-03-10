
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
        builderOptions.premium = false;
        builderOptions.data = false;
        let customBasic = {};
        customBasic.title = 'Basic';
        customBasic.default = false;
        customBasic.weight = 0;
        let customBasicComponents = {};
        customBasicComponents.checkbox = true;
        customBasicComponents.xnatNumber = true;
        customBasicComponents.textarea = true;
        customBasicComponents.textfield = true;
        customBasicComponents.xnatRadio = true;
        customBasicComponents.xnatSelect = true;
        customBasicComponents.selectBoxes = true;
        customBasic.components = customBasicComponents;
        builderOptions.customBasic = customBasic;
        let customAdvanced = {};
        customAdvanced.title = 'Advanced';
        customAdvanced.default = false;
        customAdvanced.weight = 10;
        let customAdvancedComponents = {};
        customAdvancedComponents.datetime = true;
        customAdvancedComponents.day = true;
        customAdvancedComponents.email =  true;
        customAdvancedComponents.phoneNumber = true;
        customAdvancedComponents.survey = true;
        customAdvancedComponents.time =  true;
        customAdvanced.components = customAdvancedComponents;
        builderOptions.customAdvanced = customAdvanced;
        let customData = {};
        customData.title = 'Data';
        customData.default = false;
        customData.weight = 40;

        let customDataComponents = {};
        customDataComponents.hidden =  true;
        customDataComponents.container =  true;
        customData.components = customDataComponents;
        builderOptions.customData = customData;
        let customNonSearchable = {};
        customNonSearchable.title = 'Non-searchable';
        customNonSearchable.default = false;
        customNonSearchable.weight = 50;
        let customNonSearchableComponents = {};
        customNonSearchableComponents.address =  true;
        customNonSearchableComponents.datagrid =  true;
        customNonSearchableComponents.datamap =  true;
        customNonSearchableComponents.editgrid =  true;
        customNonSearchableComponents.tree =  true;
        customNonSearchable.components = customNonSearchableComponents;
        builderOptions.customNonSearchable = customNonSearchable;
        return builderOptions;
    }

}));
