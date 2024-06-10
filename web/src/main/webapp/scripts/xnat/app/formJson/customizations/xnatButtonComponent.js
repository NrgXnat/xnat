const BtnComponent = Formio.Components.components.button;
class XnatBtnComponent extends BtnComponent {
    static schema(...extend) {
        return BtnComponent.schema(
            {
                label: "Submit",
                type: "xnatButton"
            },
            ...extend
        );
    }

    static get builderInfo() {
        return {
            title: "Button",
            group: "basic",
            icon: "code",
            weight: 110,
            documentation: '/userguide/forms/form-components#button',
            schema: XnatBtnComponent.schema(),
        };
    }
}
XnatBtnComponent.editForm = (...args) => {
    const editForm = BtnComponent.editForm(...args);
    const actionComponent = Formio.Utils.getComponent(editForm.components, "action");
    actionComponent.data.values = [
        {
            label: "Submit",
            value: "submit",
        },
        {
            label: "Reset",
            value: "reset",
        },
        {
            label: "Event",
            value: "event",
        }
    ];
    return editForm;
};
Formio.Components.addComponent("xnatButton", XnatBtnComponent);
