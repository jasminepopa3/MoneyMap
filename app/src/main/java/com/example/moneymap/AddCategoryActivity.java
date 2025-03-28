package com.example.moneymap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddCategoryActivity extends AppCompatActivity {

    private EditText editTextCategoryName;
    private EditText editTextCategoryDescription;
    private Button buttonSaveCategory;
    private Button[] colorButtons; // Array to hold color buttons
    private String selectedColor = "#F75C03"; // Default color (Orange)
    private Button lastSelectedButton; // Track the last selected button
    private Category categoryToEdit; // Categoria care trebuie editată

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        // Referințe la elementele din layout
        editTextCategoryName = findViewById(R.id.editTextCategoryName);
        editTextCategoryDescription = findViewById(R.id.editTextCategoryDescription);
        buttonSaveCategory = findViewById(R.id.buttonSaveCategory);

        // Initialize color buttons
        colorButtons = new Button[]{
                findViewById(R.id.buttonColor1),
                findViewById(R.id.buttonColor2),
                findViewById(R.id.buttonColor3),
                findViewById(R.id.buttonColor4),
                findViewById(R.id.buttonColor5)
        };

        // Verifică dacă a fost transmisă o categorie pentru editare
        if (getIntent().hasExtra("category")) {
            categoryToEdit = (Category) getIntent().getSerializableExtra("category");
            if (categoryToEdit != null) {
                // Precompletează câmpurile cu datele din categoria selectată
                editTextCategoryName.setText(categoryToEdit.getName());
                editTextCategoryDescription.setText(categoryToEdit.getDescription());
                selectedColor = categoryToEdit.getColor();

                // Selectează butonul de culoare corespunzător
                for (int i = 0; i < colorButtons.length; i++) {
                    if (selectedColor.equals(getColorForButton(i))) {
                        colorButtons[i].setBackgroundResource(R.drawable.selected_color_border);
                        lastSelectedButton = colorButtons[i];
                        break;
                    }
                }
            }
        } else {
            // Dacă este o categorie nouă, setează culoarea implicită
            lastSelectedButton = colorButtons[0];
            lastSelectedButton.setBackgroundResource(R.drawable.selected_color_border);
        }

        // Listener pentru butoanele de culoare
        for (int i = 0; i < colorButtons.length; i++) {
            final int index = i;
            colorButtons[i].setOnClickListener(v -> {
                // Remove the border from the last selected button
                lastSelectedButton.setBackgroundResource(R.drawable.button_background);

                // Set the selected color
                selectedColor = getColorForButton(index);

                // Highlight the selected button with a border
                colorButtons[index].setBackgroundResource(R.drawable.selected_color_border);

                // Update the last selected button
                lastSelectedButton = colorButtons[index];
            });
        }

        // Listener pentru butonul de salvare
        buttonSaveCategory.setOnClickListener(v -> saveCategory());
    }

    private String getColorForButton(int index) {
        switch (index) {
            case 0: return "#D4A59A"; // Soft terracotta
            case 1: return "#F7C9B6"; // Peach
            case 2: return "#C08552"; // Warm brown
            case 3: return "#E3B23C"; // Mustard yellow
            case 4: return "#A44A3F"; // Rust red
            default: return "#F75C03"; // Default- orange
        }
    }

    private void saveCategory() {
        String name = editTextCategoryName.getText().toString().trim();
        String description = editTextCategoryDescription.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Creează un map cu datele categoriei
            Map<String, Object> category = new HashMap<>();
            category.put("name", name);
            category.put("description", description);
            category.put("color", selectedColor);

            if (categoryToEdit != null) {
                // Dacă este o categorie existentă, actualizează documentul
                db.collection("users").document(userId).collection("categories")
                        .document(categoryToEdit.getId())
                        .set(category)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Category updated successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error updating category", e);
                            Toast.makeText(this, "Failed to update category", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Dacă este o categorie nouă, adaugă un nou document
                db.collection("users").document(userId).collection("categories")
                        .add(category)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firestore", "Error adding category", e);
                            Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }
}