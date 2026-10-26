package com.example.pantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Calendar;
import java.util.Locale;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import java.util.regex.Pattern;

/**
 * Add/Edit Ingredient screen from the brief.
 *
 * One Activity handles both modes:
 *  - ADD mode:  launched with no extras.
 *  - EDIT mode: launched with EXTRA_ITEM_ID set to the id of the item to edit;
 *               the form is pre-filled from the database.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    // Key for the Intent extra that carries the id of the item being edited.
    public static final String EXTRA_ITEM_ID = "extra_item_id";
    private static final long NO_ITEM = -1;
    private static final int MAX_NAME_LENGTH = 40;
    private static final double MAX_QUANTITY = 100000;
    // Starts with a letter; then letters, spaces, hyphens or apostrophes only.
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}[\\p{L} '\\-]*$");
    private PantryDbHelper dbHelper;
    private TextInputLayout nameLayout;
    private TextInputLayout quantityLayout;
    private TextInputEditText nameEdit;
    private TextInputEditText quantityEdit;
    private TextInputEditText expiryEdit;
    private Spinner unitSpinner;
    private Button deleteButton;
    private long editingItemId = NO_ITEM; // NO_ITEM means we are adding a new item

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);

        // Keeps the form from being drawn under the status/navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_edit_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new PantryDbHelper(this);

        nameLayout = findViewById(R.id.layout_name);
        quantityLayout = findViewById(R.id.layout_quantity);
        nameEdit = findViewById(R.id.edit_name);
        quantityEdit = findViewById(R.id.edit_quantity);
        expiryEdit = findViewById(R.id.edit_expiry);
        unitSpinner = findViewById(R.id.spinner_unit);
        Button saveButton = findViewById(R.id.button_save);
        Button cancelButton = findViewById(R.id.button_cancel);
        Button clearDateButton = findViewById(R.id.button_clear_date);
        deleteButton = findViewById(R.id.button_delete);
        TextView titleText = findViewById(R.id.text_screen_title);

        // Fill the unit dropdown from the string-array in strings.xml.
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.unit_options, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(unitAdapter);

        // Read the Intent: if it carries an item id, it is in EDIT mode.
        editingItemId = getIntent().getLongExtra(EXTRA_ITEM_ID, NO_ITEM);
        if (editingItemId != NO_ITEM) {
            titleText.setText(R.string.edit_ingredient_title);
            loadItemIntoForm(editingItemId);
            // Delete only makes sense when editing an existing item.
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> confirmDelete());
        }

        expiryEdit.setOnClickListener(v -> showDatePicker());
        clearDateButton.setOnClickListener(v -> expiryEdit.setText(""));
        cancelButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveItem());
    }

    // EDIT mode: look up the item and pre-fill every field.
    private void loadItemIntoForm(long id) {
        PantryItem item = dbHelper.getPantryItemById(id);
        if (item == null) {
            Toast.makeText(this, R.string.item_load_failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameEdit.setText(item.getIngredientName());

        double qty = item.getQuantity();
        quantityEdit.setText(qty == Math.floor(qty)
                ? String.valueOf((long) qty)
                : String.valueOf(qty));

        // Select the saved unit in the spinner (falls back to the first option if not found).
        String[] units = getResources().getStringArray(R.array.unit_options);
        for (int i = 0; i < units.length; i++) {
            if (units[i].equals(item.getUnit())) {
                unitSpinner.setSelection(i);
                break;
            }
        }

        if (item.getExpiryDate() != null) {
            expiryEdit.setText(item.getExpiryDate());
        }
    }

    // Opens a calendar dialog; the chosen date is stored as yyyy-MM-dd text.
    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();

        // If a date is already set, open the picker on that date.
        String existing = expiryEdit.getText() == null ? "" : expiryEdit.getText().toString();
        if (existing.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = existing.split("-");
            cal.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        }

        new DatePickerDialog(this, (view, year, month, day) -> {
            // month is 0-based, so add 1.
            String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            expiryEdit.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Asks for confirmation, then deletes the item and closes the screen.
    private void confirmDelete() {
        String name = nameEdit.getText() == null ? "" : nameEdit.getText().toString();

        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, name))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (dbHelper.deletePantryItem(editingItemId) > 0) {
                        Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
                        finish(); // MainActivity.onResume() reloads the list
                    } else {
                        Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Validates the form, then inserts (ADD) or updates (EDIT) and closes the screen.
    // Validates the form, then inserts (ADD) or updates (EDIT) and closes the screen.
    private void saveItem() {
        nameLayout.setError(null);
        quantityLayout.setError(null);

        // Trim, collapse repeated spaces, lowercase: matches the seeded recipe ingredient names.
        String rawName = nameEdit.getText() == null ? "" : nameEdit.getText().toString();
        String name = rawName.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        String quantityText = quantityEdit.getText() == null ? "" : quantityEdit.getText().toString().trim();

        // ---- Name checks ----
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.error_name_required));
            return;
        }
        if (name.length() > MAX_NAME_LENGTH) {
            nameLayout.setError(getString(R.string.error_name_too_long));
            return;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            nameLayout.setError(getString(R.string.error_name_invalid));
            return;
        }

        // ---- Quantity checks ----
        if (quantityText.isEmpty()) {
            quantityLayout.setError(getString(R.string.error_quantity_required));
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            quantityLayout.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        if (quantity <= 0) {
            quantityLayout.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        if (quantity > MAX_QUANTITY) {
            quantityLayout.setError(getString(R.string.error_quantity_too_large));
            return;
        }

        String unit = unitSpinner.getSelectedItem().toString();

        // Empty date field -> store null (the column is nullable).
        String expiry = expiryEdit.getText() == null ? "" : expiryEdit.getText().toString().trim();
        if (expiry.isEmpty()) {
            expiry = null;
        }

        boolean success;
        if (editingItemId == NO_ITEM) {
            success = dbHelper.insertPantryItem(name, quantity, unit, expiry) != -1;
        } else {
            success = dbHelper.updatePantryItem(editingItemId, name, quantity, unit, expiry) > 0;
        }

        if (success) {
            Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }
}