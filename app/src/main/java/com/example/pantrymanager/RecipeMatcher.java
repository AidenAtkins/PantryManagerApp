package com.example.pantrymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The strict-matching rule from the brief: a recipe qualifies ONLY if every one of its
 * ingredients is in the pantry in at least the required quantity.
 *
 * How one ingredient is checked:
 *  1. Names are normalised (case, spacing, plural) so "Tomatoes" matches "tomato".
 *  2. Every pantry row for that ingredient is converted into the recipe's unit
 *     and the amounts are added together (several batches count as one stock).
 *  3. The total must be >= the amount the recipe needs.
 */
public final class RecipeMatcher {

    // Tolerance for floating-point rounding, e.g. 0.1 + 0.2 vs 0.3.
    private static final double EPSILON = 1e-9;

    private RecipeMatcher() {
        // Utility class: no instances.
    }

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> findMakeableRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, List<PantryItem>> pantryByName = groupByNormalizedName(pantry);
        List<Recipe> makeable = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (isMakeable(recipe, pantryByName)) {
                makeable.add(recipe);
            }
        }
        return makeable;
    }

    /** True if this single recipe passes the strict rule. */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        return isMakeable(recipe, groupByNormalizedName(pantry));
    }

    /**
     * How many required ingredients are missing or insufficient. 0 means makeable.
     * Not used by the main list; kept so the optional "Almost There" list (recipes
     * missing exactly 1 ingredient) can be added later without new logic.
     */
    public static int countMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        return countMissing(recipe, groupByNormalizedName(pantry));
    }

    /**
     * Returns the pantry without items whose expiry date is before today.
     * Items with no expiry date are kept. todayIso is "yyyy-MM-dd"; because that format
     * sorts in date order, a plain string comparison is enough (no date parsing needed).
     */
    public static List<PantryItem> withoutExpired(List<PantryItem> pantry, String todayIso) {
        List<PantryItem> kept = new ArrayList<>();
        for (PantryItem item : pantry) {
            String expiry = item.getExpiryDate();
            boolean expired = expiry != null && !expiry.isEmpty() && expiry.compareTo(todayIso) < 0;
            if (!expired) {
                kept.add(item);
            }
        }
        return kept;
    }

    private static boolean isMakeable(Recipe recipe, Map<String, List<PantryItem>> pantryByName) {
        // A recipe with no ingredients is bad data, not "makeable".
        return !recipe.getIngredients().isEmpty() && countMissing(recipe, pantryByName) == 0;
    }

    private static int countMissing(Recipe recipe, Map<String, List<PantryItem>> pantryByName) {
        int missing = 0;
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, pantryByName)) {
                missing++;
            }
        }
        return missing;
    }

    private static boolean hasEnough(RecipeIngredient needed, Map<String, List<PantryItem>> pantryByName) {
        String key = IngredientNormalizer.normalize(needed.getIngredientName());
        List<PantryItem> rows = pantryByName.get(key);
        if (rows == null) {
            return false; // ingredient not in the pantry at all
        }

        double totalInRecipeUnit = 0;
        for (PantryItem row : rows) {
            Double converted = UnitConverter.convert(
                    row.getQuantity(), row.getUnit(), needed.getUnit(), key);
            if (converted != null) {
                totalInRecipeUnit += converted;
            }
            // A row whose unit cannot be compared contributes nothing (strict = safe side).
        }
        return totalInRecipeUnit + EPSILON >= needed.getQuantityRequired();
    }

    // Groups pantry rows by normalised name so each recipe ingredient is one map lookup.
    private static Map<String, List<PantryItem>> groupByNormalizedName(List<PantryItem> pantry) {
        Map<String, List<PantryItem>> map = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = IngredientNormalizer.normalize(item.getIngredientName());
            List<PantryItem> rows = map.get(key);
            if (rows == null) {
                rows = new ArrayList<>();
                map.put(key, rows);
            }
            rows.add(item);
        }
        return map;
    }
}