package com.example.pantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView adapter that binds a List<PantryItem> to rows using the
 * item_pantry.xml layout. Notifies the caller (via OnItemClickListener)
 * when a row is tapped, so MainActivity can decide what to do (e.g. open
 * the edit screen in a later stage).
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> pantryItems;
    private final OnItemClickListener listener;

    public PantryAdapter(List<PantryItem> pantryItems, OnItemClickListener listener) {
        this.pantryItems = pantryItems;
        this.listener = listener;
    }

    // Inflates (creates) a new row view when the RecyclerView needs one.
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    // Fills an existing row view with data for a specific list position.
    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = pantryItems.get(position);

        holder.nameText.setText(item.getIngredientName());

        String quantityDisplay = formatQuantity(item.getQuantity()) +
                (item.getUnit() != null && !item.getUnit().isEmpty() ? " " + item.getUnit() : "");
        holder.quantityText.setText(quantityDisplay);

        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.expiryText.setText("Expires: " + item.getExpiryDate());
            holder.expiryText.setVisibility(View.VISIBLE);
        } else {
            holder.expiryText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    // Displays whole numbers without a trailing ".0" (e.g. "3" not "3.0"),
    // but keeps the decimal for genuinely fractional quantities (e.g. "0.5").
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    // Holds references to the views inside one row, so findViewById() only
    // runs once per row instead of every time the row scrolls into view.
    static class PantryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView quantityText;
        TextView expiryText;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_ingredient_name);
            quantityText = itemView.findViewById(R.id.text_quantity);
            expiryText = itemView.findViewById(R.id.text_expiry);
        }
    }
}
