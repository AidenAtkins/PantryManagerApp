[README (2).txt](https://github.com/user-attachments/files/34089098/README.2.txt)
# Smart Pantry Manager

An Android app (Java) that helps reduce food waste. The user records the ingredients they
already have at home, and the app suggests only the recipes they can cook **right now**,
with no shopping trip required.


**Author:** Aiden Atkins - 402310955

## Features

- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list (RecyclerView with a custom adapter) with colour-coded expiry dates
- 17 pre-loaded recipes, seeded on first launch
- Suggested Recipes screen driven by a strict-matching rule
- Recipe Detail screen with the ingredient list and numbered method
- Settings screen (option to ignore expired ingredients when suggesting recipes)
- Bottom navigation bar (Pantry / Recipes / Settings)
- Input validation and friendly empty states

## Database: SQLite

The app uses **SQLite** through `SQLiteOpenHelper`, stored locally on the device.

Why SQLite:
- The data is small and relational (pantry items, recipes, and the ingredients each recipe needs).
- It works fully offline, which suits a kitchen app, and needs no server, account or hosting.
- It gives real persistence: data is still there after the app is closed and reopened.
- It is the approach covered in the module's persistent-data chapter.

Tables: `pantry_items`, `recipes`, and `recipe_ingredients` (one row per ingredient a recipe
requires, linked to `recipes` by a foreign key so matching can be done without parsing strings).

User settings are stored separately in SharedPreferences.

## The strict-matching rule

A recipe is suggested only if **every** ingredient it needs is in the pantry in at least the
required quantity. The logic lives in `RecipeMatcher`, and for each ingredient it:

1. Normalises names (`IngredientNormalizer`): case, spacing and plurals, so "Tomatoes" matches "tomato".
2. Converts units (`UnitConverter`): g/kg, ml/l/tsp/tbsp/cup, and approximate density and
   item-weight tables for common ingredients (e.g. 1 kg of onions counts as about 6 onions).
3. Adds up every pantry row for that ingredient (several batches count as one stock).
4. Compares the total with the quantity the recipe requires.

If units cannot be compared, the ingredient counts as missing, so the app never suggests a
recipe it is unsure about.

## Setup and run

1. Install Android Studio and clone this repository, or download it as a ZIP and extract it.
2. Open the project folder in Android Studio and wait for the Gradle sync to finish.
3. Create an emulator (Device Manager) or enable USB debugging on a physical device.
4. Press **Run** (the green triangle).

The recipes are seeded the first time the app runs. To reset all data, uninstall the app
or choose "Clear storage" in the device's app settings.

### Run the unit tests

In Android Studio, right-click `app/src/test/java/com/example/pantrymanager` and choose
**Run Tests**. The tests cover the matching rule, unit conversion and name normalisation,
and run on the computer without an emulator.

## Project structure

- `MainActivity` - pantry list
- `AddEditIngredientActivity` - add / edit form with validation
- `SuggestedRecipesActivity` - recipes the user can make now
- `RecipeDetailActivity` - ingredients and method
- `SettingsActivity` - preferences
- `PantryDbHelper` - SQLite schema, seed data and queries
- `RecipeMatcher`, `IngredientNormalizer`, `UnitConverter` - matching logic
- `PantryAdapter`, `RecipeAdapter` - RecyclerView adapters

## Known limitations

- Density and item-weight conversions are approximations for a small set of common ingredients.
- The app does not use maps, GPS or any location features.
