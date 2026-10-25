package com.example.pantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the app's single SQLite database: creation, schema, seed data,
 * and (as the app grows) read/write queries for pantry items and recipes.
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pantry_manager.db";
    private static final int DATABASE_VERSION = 1;

    // ---------- Table: pantry_items ----------
    public static final String TABLE_PANTRY_ITEMS = "pantry_items";
    public static final String COL_PANTRY_ID = "_id";
    public static final String COL_PANTRY_NAME = "ingredient_name";
    public static final String COL_PANTRY_QUANTITY = "quantity";
    public static final String COL_PANTRY_UNIT = "unit";
    public static final String COL_PANTRY_EXPIRY = "expiry_date"; // nullable, stored as yyyy-MM-dd

    // ---------- Table: recipes ----------
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_RECIPE_ID = "_id";
    public static final String COL_RECIPE_NAME = "recipe_name";
    public static final String COL_RECIPE_STEPS = "preparation_steps";

    // ---------- Table: recipe_ingredients (child of recipes, one row per required ingredient) ----------
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";       // FK -> recipes._id
    public static final String COL_RI_INGREDIENT_NAME = "ingredient_name";
    public static final String COL_RI_QUANTITY = "quantity_required";
    public static final String COL_RI_UNIT = "unit";

    public PantryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called once, the first time the database is created on a device.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY_ITEMS + " (" +
                COL_PANTRY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PANTRY_NAME + " TEXT NOT NULL, " +
                COL_PANTRY_QUANTITY + " REAL NOT NULL, " +
                COL_PANTRY_UNIT + " TEXT, " +
                COL_PANTRY_EXPIRY + " TEXT" +
                ")");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_NAME + " TEXT NOT NULL, " +
                COL_RECIPE_STEPS + " TEXT" +
                ")");

        // recipe_ingredients is a separate table (rather than a comma-delimited string
        // column on recipes) so that the strict-matching query in a later stage can be
        // done with a simple join/count, instead of manually parsing strings.
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_INGREDIENT_NAME + " TEXT NOT NULL, " +
                COL_RI_QUANTITY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " +
                TABLE_RECIPES + "(" + COL_RECIPE_ID + ")" +
                ")");

        seedRecipes(db);
    }

    // Called if DATABASE_VERSION is increased in a future update.
    // For this project, we just wipe and rebuild rather than migrating data in place.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY_ITEMS);
        onCreate(db);
    }

    /**
     * Reads every row from pantry_items, newest first, and converts each
     * row into a PantryItem object for use in the UI.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_PANTRY_ITEMS,
                null,
                null, null, null, null,
                COL_PANTRY_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PANTRY_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT));
                String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY));
                items.add(new PantryItem(id, name, quantity, unit, expiry));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    /**
     * Inserts a new pantry item. Returns the new row id, or -1 if the insert failed.
     */
    public long insertPantryItem(String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate); // null is stored as NULL (no expiry)
        return db.insert(TABLE_PANTRY_ITEMS, null, values);
    }

    /**
     * Updates an existing pantry item. Returns the number of rows changed (1 on success).
     */
    public int updatePantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PANTRY_NAME, name);
        values.put(COL_PANTRY_QUANTITY, quantity);
        values.put(COL_PANTRY_UNIT, unit);
        values.put(COL_PANTRY_EXPIRY, expiryDate);
        return db.update(TABLE_PANTRY_ITEMS, values, COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Loads a single pantry item by id (used to pre-fill the edit screen).
     * Returns null if no row with that id exists.
     */
    public PantryItem getPantryItemById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PANTRY_ITEMS,
                null,
                COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        PantryItem item = null;
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_NAME));
            double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PANTRY_QUANTITY));
            String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_UNIT));
            String expiry = cursor.getString(cursor.getColumnIndexOrThrow(COL_PANTRY_EXPIRY));
            item = new PantryItem(id, name, quantity, unit, expiry);
        }
        cursor.close();
        return item;
    }

    /**
     * Deletes one pantry item by id. Returns the number of rows removed (1 on success).
     */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY_ITEMS, COL_PANTRY_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Returns true if another pantry item already uses this name (case-insensitive).
     * Pass the id of the item being edited as excludeId so it doesn't clash with itself;
     * pass -1 when adding a new item.
     */
    public boolean pantryItemNameExists(String name, long excludeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_PANTRY_ITEMS,
                new String[]{COL_PANTRY_ID},
                "LOWER(" + COL_PANTRY_NAME + ") = ? AND " + COL_PANTRY_ID + " != ?",
                new String[]{name, String.valueOf(excludeId)},
                null, null, null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Inserts one recipe row plus all of its required-ingredient rows.
    // Used only at first launch, to seed the 15-20 starter recipes the brief requires.
    private void insertRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_RECIPE_NAME, name);
        recipeValues.put(COL_RECIPE_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (Object[] ing : ingredients) {
            ContentValues riValues = new ContentValues();
            riValues.put(COL_RI_RECIPE_ID, recipeId);
            riValues.put(COL_RI_INGREDIENT_NAME, (String) ing[0]);
            riValues.put(COL_RI_QUANTITY, ((Number) ing[1]).doubleValue());
            riValues.put(COL_RI_UNIT, (String) ing[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, riValues);
        }
    }

    // Seed data: 17 recipes with their required ingredients (brief asks for 15-20).
    // Ingredient names are kept lowercase/singular for consistent matching later
    // (e.g. "tomato" not "Tomatoes") — the strict-matching stage will normalize
    // user pantry input the same way.
    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Tomato Egg Stir Fry", "Beat eggs. Heat oil, scramble eggs, set aside. Saute tomato until soft. Combine, season, serve.",
                new Object[][]{
                        {"egg", 3, "unit"},
                        {"tomato", 2, "unit"},
                        {"oil", 1, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Garlic Butter Rice", "Melt butter, saute garlic until fragrant. Stir in cooked rice. Season and serve.",
                new Object[][]{
                        {"rice", 2, "cup"},
                        {"butter", 2, "tbsp"},
                        {"garlic clove", 3, "unit"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Simple Pasta Aglio e Olio", "Boil pasta. Saute garlic in olive oil until golden. Toss pasta in oil, season, serve.",
                new Object[][]{
                        {"pasta", 200, "g"},
                        {"garlic clove", 4, "unit"},
                        {"olive oil", 3, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Cheese Omelette", "Beat eggs with milk. Pour into pan, add cheese, fold once set.",
                new Object[][]{
                        {"egg", 3, "unit"},
                        {"cheese", 50, "g"},
                        {"milk", 2, "tbsp"},
                        {"butter", 1, "tbsp"}
                });

        insertRecipe(db, "Chicken Rice Bowl", "Cook rice. Pan-fry seasoned chicken until cooked through. Serve over rice.",
                new Object[][]{
                        {"chicken breast", 1, "unit"},
                        {"rice", 1, "cup"},
                        {"oil", 1, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Onion Tomato Soup", "Saute onion until soft. Add chopped tomato and water, simmer 15 minutes, blend, season.",
                new Object[][]{
                        {"onion", 1, "unit"},
                        {"tomato", 3, "unit"},
                        {"water", 500, "ml"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Potato Hash", "Dice and pan-fry potato in oil until crisp. Add chopped onion, season, cook through.",
                new Object[][]{
                        {"potato", 2, "unit"},
                        {"onion", 1, "unit"},
                        {"oil", 2, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Basic Pancakes", "Whisk flour, egg, and milk into a batter. Pour onto a hot buttered pan, flip when bubbling.",
                new Object[][]{
                        {"flour", 1, "cup"},
                        {"egg", 1, "unit"},
                        {"milk", 1, "cup"},
                        {"butter", 1, "tbsp"}
                });

        insertRecipe(db, "Bean and Tomato Stew", "Saute onion and garlic. Add tomato and beans, simmer 10 minutes, season.",
                new Object[][]{
                        {"onion", 1, "unit"},
                        {"garlic clove", 2, "unit"},
                        {"tomato", 2, "unit"},
                        {"beans", 400, "g"}
                });

        insertRecipe(db, "Buttered Toast with Cheese", "Toast bread. Spread butter, top with cheese, grill until melted.",
                new Object[][]{
                        {"bread", 2, "unit"},
                        {"butter", 1, "tbsp"},
                        {"cheese", 40, "g"}
                });

        insertRecipe(db, "Chicken Garlic Stir Fry", "Saute garlic in oil, add sliced chicken, cook through, season and serve.",
                new Object[][]{
                        {"chicken breast", 1, "unit"},
                        {"garlic clove", 3, "unit"},
                        {"oil", 1, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Milk Rice Pudding", "Simmer rice in milk with sugar, stirring occasionally, until thickened.",
                new Object[][]{
                        {"rice", 0.5, "cup"},
                        {"milk", 2, "cup"},
                        {"sugar", 2, "tbsp"}
                });

        insertRecipe(db, "Vegetable Egg Fried Rice", "Scramble egg, set aside. Stir-fry rice with chopped onion and carrot, combine with egg.",
                new Object[][]{
                        {"rice", 2, "cup"},
                        {"egg", 2, "unit"},
                        {"onion", 1, "unit"},
                        {"carrot", 1, "unit"},
                        {"oil", 1, "tbsp"}
                });

        insertRecipe(db, "Carrot and Potato Mash", "Boil carrot and potato until soft. Mash with butter and salt.",
                new Object[][]{
                        {"carrot", 2, "unit"},
                        {"potato", 2, "unit"},
                        {"butter", 1, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Cheesy Scrambled Eggs", "Beat eggs, cook gently in butter, fold in cheese just before fully set.",
                new Object[][]{
                        {"egg", 3, "unit"},
                        {"butter", 1, "tbsp"},
                        {"cheese", 30, "g"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Simple Bean Salad", "Combine beans, chopped tomato, and onion. Dress with olive oil and salt.",
                new Object[][]{
                        {"beans", 400, "g"},
                        {"tomato", 1, "unit"},
                        {"onion", 1, "unit"},
                        {"olive oil", 2, "tbsp"},
                        {"salt", 1, "tsp"}
                });

        insertRecipe(db, "Bread and Butter", "Toast bread, spread with butter, serve.",
                new Object[][]{
                        {"bread", 2, "unit"},
                        {"butter", 1, "tbsp"}
                });
    }
}