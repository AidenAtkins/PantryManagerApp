package com.example.pantrymanager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import android.content.Intent;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * The app's launcher screen — doubles as the "Pantry List" screen from the brief.
 * Shows all pantry items in a RecyclerView, an empty-state message when there
 * are none.
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
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        FloatingActionButton fabAddItem = findViewById(R.id.fab_add_item);
        fabAddItem.setOnClickListener(v -> {
            // ADD mode: no extras, so the screen opens with a blank form.
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        BottomNavHelper.setup(this, bottomNav, R.id.nav_pantry);
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
            // EDIT mode: pass the item's id so the next screen can load and pre-fill it.
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            intent.putExtra(AddEditIngredientActivity.EXTRA_ITEM_ID, item.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
}