package org.nrg.xnat.contrast.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(prefix = "_")
public class ContrastBolus {

    public ContrastBolus(final String agent) {
        _agent = agent;
    }

    public void addIngredient(Ingredient ingredient) {
        if(_ingredients == null) {
            _ingredients = new ArrayList<>();
        }
        _ingredients.add(ingredient);
    }

    public void addAdministration(Administration administration) {
        if(_administrations == null) {
            _administrations = new ArrayList<>();
        }
        _administrations.add(administration);
    }

    private String _agent;
    private String _agentNumber;
    private Integer _scanId;
    private String _route;
    private Integer _pk;

    private List<Ingredient> _ingredients;
    private List<Administration> _administrations;

    private String _concentration;
    private String _percentageByVolume;
    private String _ingredientOpaque;
    private String _t1Relaxivity;
    private String _t2Relaxivity;
}
