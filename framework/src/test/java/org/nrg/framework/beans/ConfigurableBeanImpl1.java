package org.nrg.framework.beans;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class ConfigurableBeanImpl1 implements ConfigurableBean {
    public ConfigurableBeanImpl1(final String name, final int luckyNumber) {
        _name = name;
        _luckyNumber = luckyNumber;
    }

    public String getName() {
        return _name;
    }

    public int getLuckyNumber() {
        return _luckyNumber;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConfigurableBeanImpl1)) {
            return false;
        }

        final ConfigurableBeanImpl1 that = (ConfigurableBeanImpl1) other;

        return getLuckyNumber() == that.getLuckyNumber() && (getName() != null ? getName().equals(that.getName()) : that.getName() == null);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + getLuckyNumber();
        return result;
    }

    private final String _name;
    private final int    _luckyNumber;
}
