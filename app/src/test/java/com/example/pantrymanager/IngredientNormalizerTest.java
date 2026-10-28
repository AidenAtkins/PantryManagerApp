package com.example.pantrymanager;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests for the plural / case / spacing normalisation used by matching. */
public class IngredientNormalizerTest {

    @Test
    public void caseAndSpacing_areIgnored() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomato"));
        assertEquals("tomato", IngredientNormalizer.normalize("  TOMATOES "));
        assertEquals("olive oil", IngredientNormalizer.normalize("olive   oils"));
    }

    @Test
    public void commonPlurals_becomeSingular() {
        assertEquals("tomato", IngredientNormalizer.normalize("tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("egg", IngredientNormalizer.normalize("eggs"));
        assertEquals("bean", IngredientNormalizer.normalize("beans"));
        assertEquals("cherry", IngredientNormalizer.normalize("cherries"));
        assertEquals("peach", IngredientNormalizer.normalize("peaches"));
        assertEquals("box", IngredientNormalizer.normalize("boxes"));
        assertEquals("pie", IngredientNormalizer.normalize("pies"));
        assertEquals("chili", IngredientNormalizer.normalize("chilis"));
        assertEquals("kiwi", IngredientNormalizer.normalize("kiwis"));
    }

    @Test
    public void irregularPlurals_areHandled() {
        assertEquals("leaf", IngredientNormalizer.normalize("leaves"));
        assertEquals("loaf", IngredientNormalizer.normalize("loaves"));
        assertEquals("cookie", IngredientNormalizer.normalize("cookies"));
    }

    @Test
    public void onlyTheLastWordIsMadeSingular() {
        assertEquals("garlic clove", IngredientNormalizer.normalize("garlic cloves"));
        assertEquals("chicken breast", IngredientNormalizer.normalize("Chicken Breasts"));
        assertEquals("cherry tomato", IngredientNormalizer.normalize("cherry tomatoes"));
    }

    @Test
    public void wordsThatMerelyEndInS_areLeftAlone() {
        assertEquals("hummus", IngredientNormalizer.normalize("hummus"));
        assertEquals("couscous", IngredientNormalizer.normalize("couscous"));
        assertEquals("asparagus", IngredientNormalizer.normalize("asparagus"));
        assertEquals("watercress", IngredientNormalizer.normalize("watercress"));
        assertEquals("cheese", IngredientNormalizer.normalize("cheese"));
    }

    @Test
    public void nullAndBlank_becomeEmpty() {
        assertEquals("", IngredientNormalizer.normalize(null));
        assertEquals("", IngredientNormalizer.normalize("   "));
    }
}