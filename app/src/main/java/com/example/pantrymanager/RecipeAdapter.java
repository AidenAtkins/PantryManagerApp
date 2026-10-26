package com.example.pantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter that binds a List<Recipe> to rows using item_recipe.xml.
 * Reports taps through OnRecipeClickListener so the Activity decides what happens next.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.nameText.setText(recipe.getName());

        // Builds "egg, tomato, oil, salt" from the recipe's ingredient list.
        StringBuilder names = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(ingredient.getIngredientName());
        }
        holder.ingredientsText.setText(
                holder.itemView.getContext().getString(R.string.recipe_needs, names.toString()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView ingredientsText;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_recipe_name);
            ingredientsText = itemView.findViewById(R.id.text_recipe_ingredients);
        }
    }
}