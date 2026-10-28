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

    // For showing on screen only: "olive oil" -> "Olive Oil". The stored value stays lowercase.
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        boolean startOfWord = true;
        for (char c : ingredientName.toCharArray()) {
            sb.append(startOfWord ? Character.toUpperCase(c) : c);
            startOfWord = (c == ' ');
        }
        return sb.toString();
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
