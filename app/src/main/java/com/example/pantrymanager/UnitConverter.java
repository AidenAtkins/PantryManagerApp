package com.example.pantrymanager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts a quantity from one unit to another so pantry and recipe amounts can be compared.
 *
 * Units fall into three families, each with a base unit:
 *   COUNT  -> "unit"
 *   MASS   -> grams
 *   VOLUME -> millilitres
 * Within a family conversion is exact. Between MASS and VOLUME it is only possible for
 * ingredients listed in the (approximate) density table. Anything else is "not convertible".
 */
public final class UnitConverter {

    private enum Family { COUNT, MASS, VOLUME }

    private static final Map<String, Family> FAMILY = new HashMap<>();
    private static final Map<String, Double> TO_BASE = new HashMap<>();   // factor into g / ml / count
    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, Double> DENSITY_G_PER_ML = new HashMap<>();

    static {
        define("unit", Family.COUNT, 1);
        define("g", Family.MASS, 1);
        define("kg", Family.MASS, 1000);
        define("ml", Family.VOLUME, 1);
        define("l", Family.VOLUME, 1000);
        define("tsp", Family.VOLUME, 5);
        define("tbsp", Family.VOLUME, 15);
        define("cup", Family.VOLUME, 250);

        // Spellings that may appear if units are ever typed instead of picked.
        ALIASES.put("units", "unit");
        ALIASES.put("piece", "unit");
        ALIASES.put("pieces", "unit");
        ALIASES.put("gram", "g");
        ALIASES.put("grams", "g");
        ALIASES.put("kilogram", "kg");
        ALIASES.put("kilograms", "kg");
        ALIASES.put("litre", "l");
        ALIASES.put("litres", "l");
        ALIASES.put("liter", "l");
        ALIASES.put("liters", "l");
        ALIASES.put("teaspoon", "tsp");
        ALIASES.put("teaspoons", "tsp");
        ALIASES.put("tablespoon", "tbsp");
        ALIASES.put("tablespoons", "tbsp");
        ALIASES.put("cups", "cup");

        // Approximate grams per millilitre, for common ingredients (keys are normalised names).
        DENSITY_G_PER_ML.put("salt", 1.2);
        DENSITY_G_PER_ML.put("sugar", 0.85);
        DENSITY_G_PER_ML.put("flour", 0.53);
        DENSITY_G_PER_ML.put("rice", 0.75);
        DENSITY_G_PER_ML.put("butter", 0.95);
        DENSITY_G_PER_ML.put("milk", 1.03);
        DENSITY_G_PER_ML.put("oil", 0.92);
        DENSITY_G_PER_ML.put("olive oil", 0.92);
        DENSITY_G_PER_ML.put("water", 1.0);
        DENSITY_G_PER_ML.put("cheese", 0.4);   // grated
    }

    private UnitConverter() {
        // Utility class: no instances.
    }

    private static void define(String unit, Family family, double factor) {
        FAMILY.put(unit, family);
        TO_BASE.put(unit, factor);
    }

    // Lowercases, trims, resolves aliases. A missing unit is treated as a plain count.
    private static String canonicalUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "unit";
        }
        String u = unit.trim().toLowerCase(Locale.ROOT);
        String alias = ALIASES.get(u);
        return alias != null ? alias : u;
    }

    /**
     * Converts quantity from fromUnit to toUnit for the given (normalised) ingredient name.
     * Returns null if the two units cannot be compared.
     */
    public static Double convert(double quantity, String fromUnit, String toUnit, String normalizedName) {
        String from = canonicalUnit(fromUnit);
        String to = canonicalUnit(toUnit);

        if (from.equals(to)) {
            return quantity; // identical units (even unknown ones) compare directly
        }

        Family fromFamily = FAMILY.get(from);
        Family toFamily = FAMILY.get(to);
        if (fromFamily == null || toFamily == null) {
            return null;
        }

        double base = quantity * TO_BASE.get(from);

        if (fromFamily != toFamily) {
            // A count can never be turned into a weight or volume.
            if (fromFamily == Family.COUNT || toFamily == Family.COUNT) {
                return null;
            }
            Double density = DENSITY_G_PER_ML.get(normalizedName);
            if (density == null) {
                return null;
            }
            // grams -> ml divides by density; ml -> grams multiplies by it.
            base = (fromFamily == Family.MASS) ? base / density : base * density;
        }

        return base / TO_BASE.get(to);
    }
}