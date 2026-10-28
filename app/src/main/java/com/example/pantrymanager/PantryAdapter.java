package com.example.pantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter that binds a List<PantryItem> to rows using item_pantry.xml.
 * Notifies the caller (via OnItemClickListener) when a row is tapped, so
 * MainActivity can open the edit screen.
 *
 * Expiry dates are colour coded: red = already expired, orange = expires within
 * 3 days, grey = fine.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private static final int EXPIRING_SOON_DAYS = 3;
    private static final int COLOR_EXPIRED = 0xFFB00020;
    private static final int COLOR_SOON = 0xFFE65100;
    private static final int COLOR_NORMAL = 0xFF888888;

    private final List<PantryItem> pantryItems;
    private final OnItemClickListener listener;
    private final String todayIso;  // "yyyy-MM-dd"
    private final String soonIso;   // today + 3 days, same format

    public PantryAdapter(List<PantryItem> pantryItems, OnItemClickListener listener) {
        this.pantryItems = pantryItems;
        this.listener = listener;

        // yyyy-MM-dd strings sort in date order, so dates can be compared as plain strings.
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        todayIso = format.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, EXPIRING_SOON_DAYS);
        soonIso = format.format(cal.getTime());
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

        holder.nameText.setText(item.getDisplayName());

        // "3" for plain counts, "250 g" / "2 cup" for everything else.
        String unit = item.getUnit();
        boolean showUnit = unit != null && !unit.isEmpty() && !unit.equals("unit");
        holder.quantityText.setText(formatQuantity(item.getQuantity()) + (showUnit ? " " + unit : ""));

        String expiry = item.getExpiryDate();
        if (expiry != null && !expiry.isEmpty()) {
            int labelRes;
            int color;
            if (expiry.compareTo(todayIso) < 0) {
                labelRes = R.string.expiry_expired;
                color = COLOR_EXPIRED;
            } else if (expiry.compareTo(soonIso) <= 0) {
                labelRes = R.string.expiry_soon;
                color = COLOR_SOON;
            } else {
                labelRes = R.string.expiry_normal;
                color = COLOR_NORMAL;
            }
            holder.expiryText.setText(holder.itemView.getContext().getString(labelRes, expiry));
            holder.expiryText.setTextColor(color);
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