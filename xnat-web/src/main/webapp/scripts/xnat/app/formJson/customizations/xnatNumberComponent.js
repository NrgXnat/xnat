const NumberComponent = Formio.Components.components.number;
class XnatNumberComponent extends NumberComponent {
    static schema(...extend) {
        return NumberComponent.schema(
            {
                type: "xnatNumber",
            },
            ...extend
        );
    }


    static get builderInfo() {
        return {
            title: 'Number',
            icon: 'hashtag',
            group: 'basic',
            documentation: '/userguide/forms/form-components#number',
            weight: 31,
            schema: XnatNumberComponent.schema()
        };
    }

    //The regular expression appears incorrect but is rendered as a pattern attribute
    //It is then handled correctly on the frontend
    setInputMask(input) {
        super.setInputMask(input);
        let numberPattern = '[\\+\\-0-9';
        numberPattern += this.decimalSeparator || '';
        numberPattern += this.delimiter || '';
        numberPattern += ']*';
        input.setAttribute('pattern', numberPattern);
    }


}

Formio.Components.addComponent("xnatNumber", XnatNumberComponent);
