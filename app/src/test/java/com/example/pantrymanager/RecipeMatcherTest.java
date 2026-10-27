package com.example.pantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Tests for the strict-matching rule (Section 2.3 of the brief). */
public class RecipeMatcherTest {

    // ---------- helpers ----------

    private long nextId = 1;

    private PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(nextId++, name, quantity, unit, null);
    }

    private RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private Recipe recipe(String name, RecipeIngredient... ingredients) {
        Recipe r = new Recipe(nextId++, name, "steps");
        for (RecipeIngredient i : ingredients) {
            r.addIngredient(i);
        }
        return r;
    }

    // ---------- the core strict rule ----------

    @Test
    public void allIngredientsPresent_isMakeable() {
        Recipe r = recipe("Tomato Egg", ing("egg", 3, "unit"), ing("tomato", 2, "unit"));
        List<PantryItem> pantry = Arrays.asList(item("egg", 3, "unit"), item("tomato", 2, "unit"));
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void fourOfFiveIngredients_isNotMakeable() {
        Recipe r = recipe("Five",
                ing("egg", 1, "unit"), ing("onion", 1, "unit"), ing("carrot", 1, "unit"),
                ing("rice", 1, "cup"), ing("oil", 1, "tbsp"));
        List<PantryItem> pantry = Arrays.asList(
                item("egg", 1, "unit"), item("onion", 1, "unit"),
                item("carrot", 1, "unit"), item("rice", 1, "cup"));
        assertFalse(RecipeMatcher.canMake(r, pantry));
        assertEquals(1, RecipeMatcher.countMissingIngredients(r, pantry));
    }

    @Test
    public void insufficientQuantity_isNotMakeable() {
        Recipe r = recipe("Omelette", ing("egg", 3, "unit"));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(item("egg", 2, "unit"))));
    }

    @Test
    public void emptyPantry_matchesNothing() {
        Recipe r = recipe("Toast", ing("bread", 2, "unit"));
        assertTrue(RecipeMatcher.findMakeableRecipes(Arrays.asList(r), new ArrayList<PantryItem>()).isEmpty());
    }

    @Test
    public void recipeWithNoIngredients_isNeverMakeable() {
        Recipe r = recipe("Broken");
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(item("egg", 5, "unit"))));
    }

    @Test
    public void findMakeable_returnsOnlyQualifyingRecipes() {
        Recipe ok = recipe("Toast", ing("bread", 2, "unit"));
        Recipe notOk = recipe("Omelette", ing("egg", 3, "unit"));
        List<Recipe> result = RecipeMatcher.findMakeableRecipes(
                Arrays.asList(ok, notOk), Arrays.asList(item("bread", 4, "unit")));
        assertEquals(1, result.size());
        assertEquals("Toast", result.get(0).getName());
    }

    // ---------- name robustness ----------

    @Test
    public void pluralAndCaseDifferences_stillMatch() {
        Recipe r = recipe("Soup", ing("tomato", 2, "unit"), ing("bean", 400, "g"));
        List<PantryItem> pantry = Arrays.asList(item("  Tomatoes ", 2, "unit"), item("BEANS", 400, "g"));
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    @Test
    public void multiWordNames_matchOnLastWordPlural() {
        Recipe r = recipe("Stir fry", ing("garlic clove", 3, "unit"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(item("garlic cloves", 3, "unit"))));
    }

    // ---------- several batches of the same ingredient ----------

    @Test
    public void multipleRowsOfSameIngredient_areAddedTogether() {
        Recipe r = recipe("Omelette", ing("egg", 3, "unit"));
        List<PantryItem> pantry = Arrays.asList(item("Egg", 2, "unit"), item("Eggs", 1, "unit"));
        assertTrue(RecipeMatcher.canMake(r, pantry));
    }

    // ---------- units ----------

    @Test
    public void sameFamilyUnits_areConverted() {
        Recipe r = recipe("Cheese toast", ing("cheese", 50, "g"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(item("cheese", 0.1, "kg"))));

        Recipe pudding = recipe("Pudding", ing("milk", 2, "cup"));
        assertTrue(RecipeMatcher.canMake(pudding, Arrays.asList(item("milk", 1, "l"))));
        assertFalse(RecipeMatcher.canMake(pudding, Arrays.asList(item("milk", 400, "ml"))));
    }

    @Test
    public void massToVolume_usesDensityForKnownIngredients() {
        Recipe r = recipe("Eggs", ing("salt", 1, "tsp"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(item("salt", 500, "g"))));
    }

    @Test
    public void incompatibleUnits_areNotMatched() {
        // Eggs counted in grams cannot satisfy "3 eggs".
        Recipe r = recipe("Omelette", ing("egg", 3, "unit"));
        assertFalse(RecipeMatcher.canMake(r, Arrays.asList(item("egg", 500, "g"))));

        // Unknown ingredient: no density known, so grams cannot satisfy cups.
        Recipe r2 = recipe("Mystery", ing("quinoa", 1, "cup"));
        assertFalse(RecipeMatcher.canMake(r2, Arrays.asList(item("quinoa", 500, "g"))));
    }

    @Test
    public void missingUnit_isTreatedAsCount() {
        Recipe r = recipe("Toast", ing("bread", 2, "unit"));
        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(item("bread", 2, null))));
    }

    // ---------- expiry filtering (Settings: ignore expired items) ----------

    @Test
    public void withoutExpired_removesOnlyItemsPastTheirExpiryDate() {
        PantryItem expired = new PantryItem(1, "milk", 1, "l", "2026-01-01");
        PantryItem expiresToday = new PantryItem(2, "egg", 3, "unit", "2026-10-27");
        PantryItem noDate = new PantryItem(3, "rice", 1, "cup", null);

        List<PantryItem> result = RecipeMatcher.withoutExpired(
                Arrays.asList(expired, expiresToday, noDate), "2026-10-27");

        assertEquals(2, result.size());
        assertFalse(result.contains(expired));
    }

    @Test
    public void expiredItem_stopsRecipeMatching_onlyWhenFiltered() {
        Recipe r = recipe("Omelette", ing("egg", 3, "unit"));
        PantryItem expiredEggs = new PantryItem(1, "egg", 3, "unit", "2026-01-01");

        assertTrue(RecipeMatcher.canMake(r, Arrays.asList(expiredEggs)));
        assertFalse(RecipeMatcher.canMake(r,
                RecipeMatcher.withoutExpired(Arrays.asList(expiredEggs), "2026-10-27")));
    }

}