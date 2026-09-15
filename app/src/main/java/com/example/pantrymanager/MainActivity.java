package com.example.pantrymanager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * The app's launcher screen — doubles as the "Pantry List" screen from the brief.
 * Shows all pantry items in a RecyclerView, an empty-state message when there
 * are none, and a "+" button that will open the Add/Edit screen in stage 3.
 */
public class MainActivity extends AppCompatActivity {

    private PantryDbHelper dbHelper;
    private PantryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyStateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Template boilerplate: keeps UI from being drawn under the status/nav bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new PantryDbHelper(this);

        recyclerView = findViewById(R.id.recycler_pantry_items);
        emptyStateText = findViewById(R.id.text_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(v -> {
            // TODO stage 3: launch AddEditIngredientActivity via Intent
            Toast.makeText(this, "Add item screen coming in stage 3", Toast.LENGTH_SHORT).show();
        });
    }

    // Reload every time this screen is shown again, so edits/adds/deletes made
    // on other screens (once they exist) are reflected here without extra code.
    @Override
    protected void onResume() {
        super.onResume();
        loadPantryItems();
    }

    private void loadPantryItems() {
        List<PantryItem> items = dbHelper.getAllPantryItems();

        if (items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        adapter = new PantryAdapter(items, item -> {
            // TODO stage 3: launch AddEditIngredientActivity in edit mode, passing item.getId()
            Toast.makeText(this, "Edit " + item.getIngredientName() + " coming in stage 3", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);
    }
}