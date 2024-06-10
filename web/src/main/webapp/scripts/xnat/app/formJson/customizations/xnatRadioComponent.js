const RadioComponent = Formio.Components.components.radio;
class XnatRadioComponent extends RadioComponent {
    static schema(...extend) {
        return RadioComponent.schema(
            {
                label: "Radio",
                type: "xnatRadio"
            },
            ...extend
        );
    }

    static get builderInfo() {
        return {
            title: "Radio",
            group: "basic",
            icon: "th-list",
            weight: 80,
            documentation: '/userguide/forms/form-components#radio',
            schema: XnatRadioComponent.schema(),
        };
    }

    render() {
        //The Radio value could be true - a boolean; In order to render the value correctly
        //in html mode, the boolean should be a string
        this.dataValue = super.dataValue;
        if (!_.isString(this.dataValue)) {
            this.dataValue = _.toString(this.dataValue);
        }
        return super.render();
    }

}

Formio.Components.addComponent("xnatRadio", XnatRadioComponent);


