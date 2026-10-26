package com.example.pantrymanager;

/** One required ingredient of a recipe (one row of the recipe_ingredients table). */
public class RecipeIngredient {

    private final String ingredientName;
    private final double quantityRequired;
    private final String unit;

    public RecipeIngredient(String ingredientName, double quantityRequired, String unit) {
        this.ingredientName = ingredientName;
        this.quantityRequired = quantityRequired;
        this.unit = unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public double getQuantityRequired() {
        return quantityRequired;
    }

    public String getUnit() {
        return unit;
    }
}