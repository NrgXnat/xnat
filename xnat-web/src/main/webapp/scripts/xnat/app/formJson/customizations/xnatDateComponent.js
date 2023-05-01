
const TextFieldComponent = Formio.Components.components.textfield;

class XnatDateComponent extends TextFieldComponent {

    static get builderInfo() {
        return {
            title: 'X-Date',
            group: 'advanced',
            icon: 'calendar',
            documentation: '/userguide/forms/form-components#date-time',
            weight: 40,
            schema: XnatDateComponent.schema()
        };
    }

    attach(element) {
        let elt = super.attach(element);
        if (!this.options.readOnly) {
            insertYUICalendar(this.refs.input[0], 'Select Date');
            this.addEventListener(this.refs.input[0], 'calendarSelected', () => {
                this.updateValue(this.refs.input[0].value, {}, 0);
            });
        }
        return elt;
    }

    updateValue(value, flags, index) {
        flags = flags || {};
        const changed = super.updateValue(value, flags);
        this.triggerUpdateValueAt(this.dataValue, flags, index);
        return changed;
    }

}
Formio.Components.addComponent("xnatdate", XnatDateComponent);
