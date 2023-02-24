
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
        let customBasic = {};
        customBasic.title = 'Basic';
        customBasic.default = false;
        customBasic.weight = 0;
        let customBasicComponents = {};
        customBasicComponents.textfield = true;
        customBasicComponents.textarea = true;
        customBasicComponents.xnatNumber = true;
        customBasicComponents.checkbox = true;
        customBasicComponents.selectBoxes = true;
        customBasicComponents.xnatRadio = true;
        customBasicComponents.xnatButton = true;
        customBasicComponents.xnatSelect = true;
        customBasic.components = customBasicComponents;
        builderOptions.customBasic = customBasic;
        let customAdvanced = {};
        customAdvanced.title = 'Advanced';
        customAdvanced.default = false;
        customAdvanced.weight = 10;
        let customAdvancedComponents = {};
        customAdvancedComponents.email =  true;
        customAdvancedComponents.phoneNumber = true;
        customAdvancedComponents.address = true;
        customAdvancedComponents.datetime = true;
        customAdvancedComponents.xnatdate = true;
        customAdvancedComponents.day = true;
        customAdvancedComponents.time =  true;
        customAdvancedComponents.currency = true;
        customAdvancedComponents.survey = true;
        customAdvanced.components = customAdvancedComponents;
        builderOptions.customAdvanced = customAdvanced;
        return builderOptions;
    }

}));
