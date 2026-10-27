package com.example.pantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * "Suggested Recipes" screen.
 * Loads the pantry and all recipes, runs the strict-matching rule (RecipeMatcher),
 * and lists ONLY the recipes the user can make right now.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private PantryDbHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView noMatchesText;
    private TextView matchCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.suggested_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new PantryDbHelper(this);

        recyclerView = findViewById(R.id.recycler_suggested_recipes);
        noMatchesText = findViewById(R.id.text_no_matches);
        matchCountText = findViewById(R.id.text_match_count);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // Re-run the matching every time the screen is shown, so the list always
    // reflects the pantry as it is right now.
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        // The core business logic: only recipes whose ingredients are ALL covered.
        List<Recipe> makeable = RecipeMatcher.findMakeableRecipes(allRecipes, pantry);

        if (makeable.isEmpty()) {
            // Friendly message instead of a blank screen.
            recyclerView.setVisibility(View.GONE);
            matchCountText.setVisibility(View.GONE);
            noMatchesText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noMatchesText.setVisibility(View.GONE);
            matchCountText.setVisibility(View.VISIBLE);
            matchCountText.setText(getResources().getQuantityString(
                    R.plurals.recipes_you_can_make, makeable.size(), makeable.size()));
        }

        recyclerView.setAdapter(new RecipeAdapter(makeable, recipe -> {
            // Pass only the id; the detail screen loads the full recipe itself.
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        }));
    }
}