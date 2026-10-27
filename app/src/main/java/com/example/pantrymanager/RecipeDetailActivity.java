package com.example.pantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Recipe Detail screen from the brief.
 * Receives a recipe id through the launching Intent, loads that recipe from the
 * database, and shows its full ingredient list and numbered method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    // Key for the Intent extra that carries the id of the recipe to show.
    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";
    private static final long NO_RECIPE = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Read the id that SuggestedRecipesActivity put into the Intent.
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, NO_RECIPE);
        Recipe recipe = recipeId == NO_RECIPE ? null : new PantryDbHelper(this).getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.text_detail_name)).setText(recipe.getName());
        ((TextView) findViewById(R.id.text_detail_ingredients)).setText(buildIngredientsText(recipe));
        ((TextView) findViewById(R.id.text_detail_steps)).setText(buildStepsText(recipe.getSteps()));
    }

    // One bullet per ingredient, e.g. "• Egg: 3" or "• Butter: 2 tbsp".
    private String buildIngredientsText(Recipe recipe) {
        StringBuilder sb = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String name = ingredient.getIngredientName();
            String displayName = name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);

            sb.append("• ").append(displayName).append(": ")
                    .append(formatQuantity(ingredient.getQuantityRequired()));

            // "3 unit" reads oddly, so the plain count unit is left out.
            String unit = ingredient.getUnit();
            if (unit != null && !unit.isEmpty() && !unit.equals("unit")) {
                sb.append(" ").append(unit);
            }
        }
        return sb.toString();
    }

    // The seeded method is a run of sentences; split it into numbered steps.
    private String buildStepsText(String steps) {
        if (steps == null || steps.trim().isEmpty()) {
            return getString(R.string.no_steps);
        }
        String[] sentences = steps.trim().split("(?<=[.!?])\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentences.length; i++) {
            if (i > 0) {
                sb.append("\n\n");
            }
            sb.append(i + 1).append(". ").append(sentences[i]);
        }
        return sb.toString();
    }

    // Shows "3" instead of "3.0", but keeps "0.5".
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }
}