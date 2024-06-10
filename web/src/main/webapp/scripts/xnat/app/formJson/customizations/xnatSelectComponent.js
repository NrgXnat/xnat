const SelectComponent = Formio.Components.components.select;
class XnatSelectComponent extends SelectComponent {
    static schema(...extend) {
        return SelectComponent.schema(
            {
                label: "Select",
                type: "xnatSelect",
                data: {
                    values: [{ label: '', value: '' }]
                }
            },
            ...extend
        );
    }

    static get builderInfo() {
        return {
            title: "Select",
            group: "basic",
            icon: "th-list",
            weight: 70,
            documentation: '/userguide/forms/form-components#select',
            schema: XnatSelectComponent.schema(),
        };
    }
}
XnatSelectComponent.editForm = (...args) => {
    const editForm = SelectComponent.editForm(...args);
    const dataComponent = Formio.Utils.getComponent(editForm.components, "dataSrc");
    dataComponent.data =
    {
        values: [{label: 'Values', value: 'values'}]
    }
    return editForm;
};
Formio.Components.addComponent("xnatSelect", XnatSelectComponent);
