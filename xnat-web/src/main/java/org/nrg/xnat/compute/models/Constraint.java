package org.nrg.xnat.compute.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Constraint {

    public enum Operator {
        IN("=="),
        NOT_IN("!=");

        public final String dockerStyle;

        Operator(String dockerStyle) {
            this.dockerStyle = dockerStyle;
        }
    }

    private String key;
    private Set<String> values;
    private Operator operator;

    /**
     * Convert to list of Docker constraint strings. Example: ["key==value1", "key==value2"]
     * @return List of Docker constraint strings
     */
    public List<String> toList() {
        return values.stream()
                     .map(value -> key + operator.dockerStyle + value)
                     .collect(Collectors.toList());
    }

}
