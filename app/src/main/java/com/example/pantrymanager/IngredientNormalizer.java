package com.example.pantrymanager;

import java.util.Locale;

/**
 * Turns an ingredient name into a canonical form so that "Tomatoes", "tomato"
 * and " TOMATO " are all treated as the same ingredient.
 */
public final class IngredientNormalizer {

    private IngredientNormalizer() {
        // Utility class: no instances.
    }

    /** Lowercases, trims, collapses spaces, and singularises the last word. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }

        // Only the last word is made singular: "garlic cloves" -> "garlic clove".
        int lastSpace = cleaned.lastIndexOf(' ');
        String prefix = lastSpace == -1 ? "" : cleaned.substring(0, lastSpace + 1);
        String lastWord = lastSpace == -1 ? cleaned : cleaned.substring(lastSpace + 1);

        return prefix + singularize(lastWord);
    }

    // Simple rule-based singularising. Not full English grammar, but covers common pantry words.
    private static String singularize(String word) {
        if (word.length() <= 3) {
            return word;
        }

        // Irregular plurals that the general rules would get wrong.
        switch (word) {
            case "leaves":  return "leaf";
            case "loaves":  return "loaf";
            case "halves":  return "half";
            case "knives":  return "knife";
        }

        // cherries -> cherry, berries -> berry
        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";
        }
        // tomatoes -> tomato, peaches -> peach, radishes -> radish, boxes -> box
        if (word.endsWith("oes") || word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }
        // Words that end in s but are not plurals: hummus, couscous, watercress
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;
        }
        // Default: eggs -> egg, bananas -> banana, cloves -> clove
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }
        return word;
    }
}