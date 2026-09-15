package com.example.pantrymanager;

/**
 * Simple data model representing one row from the pantry_items table.
 * Holds no logic — just carries data between the database and the UI.
 */
public class PantryItem {

    private long id;
    private String ingredientName;
    private double quantity;
    private String unit;
    private String expiryDate; // nullable, format yyyy-MM-dd

    public PantryItem(long id, String ingredientName, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() {
        return id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getExpiryDate() {
        return expiryDate;
    }
}
